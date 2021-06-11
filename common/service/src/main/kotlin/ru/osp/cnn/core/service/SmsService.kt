package ru.osp.cnn.core.service

import com.github.michaelbull.result.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.osp.cnn.core.model.*
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_SMSGW_CALL
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_SMSGW
import ru.osp.cnn.core.service.StatisticService.Companion.PROCESSED_SMS
import ru.osp.cnn.core.service.monitoring.OspEvent
import ru.osp.cnn.core.service.monitoring.OspEventFilter
import ru.osp.smppgateway.*

interface SmsService {

    /**
     * Отравить SMS
     *
     * @return результат отправки - либо успешный либо ошибка
     */
    suspend fun sendSms(sms: Sms): Result<SuccessfulSmsSending, SmsServiceError>
}

object SuccessfulSmsSending {
    override fun toString() = "SuccessfulSmsSending"
}

sealed class SmsServiceError(message: String? = null, cause: Throwable? = null): Exception(message, cause) {
    class InvalidSmsTemplate(smsTemplateSettingName: SmsTemplateSettingName): SmsServiceError(message = "invalid template setting name=$smsTemplateSettingName")
    class HttpSendingError(e: Throwable): SmsServiceError(cause = e)
    class SmsSendingError(commandStatus: CommandStatus, description: String? = null): SmsServiceError(message = "sms send failure with command_status=$commandStatus and description=${description ?: "none"}")
    class UnspecifiedError(message: String): SmsServiceError(message = message)
}

@Component("smsService")
class SmsServiceImpl(@Qualifier("httpClient") private val httpClient: HttpClient,
                     @Qualifier("smsTemplateService") private val smsTemplateService: SmsTemplateService,
                     @Qualifier("settings") private val settings: Settings,
                     @Qualifier("statisticManager") private val statisticService: StatisticService,
                     @Qualifier("ospTransportEventFilter") private val ospEventFilter: OspEventFilter,
): SmsService {
    companion object {
        private val logger = LoggerFactory.getLogger(SmsServiceImpl::class.java)
    }

    override suspend fun sendSms(sms: Sms): Result<SuccessfulSmsSending, SmsServiceError> {
        logger.debug("sendSms sms={}", sms)
        val smppGatewayUrl = settings.smppGatewayUrl
        val esmeGroupName = settings.esmeGroupName
        val result =
            sms.toParlaySmppMessage(esmeGroupName = esmeGroupName)
                .andThen { parlaySmppMessage ->
                    runCatching {
                        httpClient.post<ParlaySmppMessage>(smppGatewayUrl) {
                            contentType(ContentType.Application.Json)
                            body = parlaySmppMessage
                        }
                    }.mapError { e ->
                        SmsServiceError.HttpSendingError(e)
                    }
                }.andThen { parlaySmppMessage ->
                    when (val params = parlaySmppMessage.methodParameters) {
                        is MessageSentMethodParams -> {
                            processSuccessMonitoring()
                            SuccessfulSmsSending.ok()
                        }
                        is MessageSendFailedMethodParams -> {
                            processFailureMonitoring(null)
                            SmsServiceError.SmsSendingError(
                                commandStatus = params.commandStatus
                            ).err()
                        }
                        is MessageErrorMethodParams -> {
                            processFailureMonitoring(null)
                            SmsServiceError.SmsSendingError(
                                commandStatus = params.commandStatus,
                                description = params.description
                            ).err()
                        }
                        else -> {
                            processFailureMonitoring(null)
                            SmsServiceError.UnspecifiedError("unexpected response from smpp gw = $params").err()
                        }
                    }
                }.onFailure { e ->
                    processFailureMonitoring(e)
                    logger.error("sendSms error", e)
                }
        logger.debug("sendSms result={}", result)
        return result
    }

    private fun processSuccessMonitoring() {
        statisticService.changeStatus(GROUP_SMSGW, EVENT_SMSGW_CALL, StatisticStatus.SUCCESSFULL)
        statisticService.increaseCounter(PROCESSED_SMS)
    }

    private fun processFailureMonitoring(e: Exception?) {
        statisticService.changeStatus(ospEventFilter, OspEvent(GROUP_SMSGW, EVENT_SMSGW_CALL, StatisticStatus.FAILED, e))
    }

    private fun Sms.toParlaySmppMessage(esmeGroupName: String): Result<ParlaySmppMessage, SmsServiceError> {
        logger.debug("toParlaySmppMessage sms={}", this)
        val result = getSmsText(this)
            .map { smsText ->
                val methodParameters = SendMessageMethodParams(
                    esmeGroupName = esmeGroupName,
                    sourceAddress = ParlaySourceAddress(
                        typeOfNumber = TypeOfNumber.INTERNATIONAL,
                        numberingPlanIndicator = NumberingPlanIndicator.ISDN,
                        address = this.originatingAddress
                    ),
                    destinationAddress = ParlayDestinationAddress(
                        typeOfNumber = TypeOfNumber.INTERNATIONAL,
                        numberingPlanIndicator = NumberingPlanIndicator.ISDN,
                        address = this.destinationAddress
                    ),
                    dataCoding = SmppEncoding.UCS2,
                    protocolId = ProtocolId.DEFAULT,
                    message = PayloadString(smsText, payloadType = PayloadType.DEFAULT)
                )
                ParlaySmppMessage(
                    methodParameters = methodParameters
                )
            }
        logger.debug("toParlaySmppMessage result={}", result)
        return result
    }

    private fun getSmsText(sms: Sms): Result<String, SmsServiceError> {
        return when (sms) {
            is SmsWithTemplate -> {
                val smsTemplateSettingName = sms.smsTemplateSettingName
                val text = smsTemplateService.getMessageByTemplateSettingName(sms.langCode, smsTemplateSettingName, sms.params)
                text?.ok() ?: SmsServiceError.InvalidSmsTemplate(smsTemplateSettingName).err()
            }
            is SmsWithText -> {
                sms.text.ok()
            }
        }
    }
}
