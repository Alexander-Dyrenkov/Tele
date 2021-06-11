package ru.osp.cnn.core.http

import org.apache.http.entity.ContentType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.osp.cnn.core.CoreService
import ru.osp.cnn.core.rmi.CoreServiceException
import ru.osp.cnn.core.service.Settings
import ru.osp.cnn.core.service.inbound.NotificationResponseBuildException
import ru.osp.cnn.core.service.inbound.NotificationResponseBuilder
import ru.osp.cnn.core.service.util.MdcUtil.withMdcContext
import ru.osp.cnn.core.service.StatisticService
import ru.osp.cnn.core.service.StatisticService.Companion.EPCP_LOAD
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_CALL_PROCESS_TIME
import ru.osp.cnn.core.service.StatisticService.Companion.PROCESSED_CALLS
import ru.osp.cnn.core.service.StatisticSeverity
import ru.osp.commons.cap.neo.*
import ru.osp.smppgateway.CommandStatus
import ru.osp.smppgateway.DeclineMethodParams
import ru.osp.smppgateway.MessageReceivedMethodParams
import ru.osp.smppgateway.ParlaySmppMessage

@RestController
class InboundController(@Qualifier("coreService") val coreService: CoreService,
                        @Qualifier("settings") val settings: Settings,
                        @Qualifier("statisticManager") val statisticService: StatisticService,
                        @Qualifier("notificationResponseBuilder") val notificationResponseBuilder: NotificationResponseBuilder) {
    companion object {
        private val log = LoggerFactory.getLogger(InboundController::class.java)
    }

    @PostMapping("/call")
    suspend fun handleCall(@RequestBody body: MethodCall): ResponseEntity<MethodCall> = withMdcContext {
        val startProcessTime = System.currentTimeMillis()
        return try {
            statisticService.increaseCounter(EPCP_LOAD)
            val callSessionId = (body.commonParameters as GWYCommonParameters).callSessionId
            val result = when (body.methodName) {
                AppMethod.CallEventNotify.name -> {
                    val headers = HttpHeaders()
                    headers.add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                    val result = coreService.processIncomingCall(callSessionId, body.methodParameters as CallEventNotify)
                    ResponseEntity(result, headers, HttpStatus.OK)
                }
                AppMethod.CallEnded.name, AppMethod.CallAborted.name, AppMethod.CallFaultDetected.name, AppMethod.CallStateChanged.name, AppMethod.ResourceCompleted.name ->
                    getDeassignResponse(callSessionId)
                else ->
                    ResponseEntity<MethodCall>(HttpStatus.BAD_REQUEST)
            }
            statisticService.increaseCounter(PROCESSED_CALLS)
            val processTime = System.currentTimeMillis() - startProcessTime
            if (processTime > settings.callProcessTimeThresholdMillis) {
                statisticService.log(EVENT_CALL_PROCESS_TIME, StatisticSeverity.INFO)
            }
            result
        } catch (e: CoreServiceException) {
            log.error("Error processing an incoming call", e)
            ResponseEntity<MethodCall>(HttpStatus.INTERNAL_SERVER_ERROR)
        } finally {
            log.debug("handleCall return")
        }
    }

    @PostMapping("/sms")
    suspend fun handleSms(@RequestBody message: ParlaySmppMessage): ResponseEntity<ParlaySmppMessage> = withMdcContext {
        log.debug("handleSms message='{}'", message)
        return try {
            val messageReceivedParams = message.methodParameters as? MessageReceivedMethodParams
            val selectiveNotificationResponse = notificationResponseBuilder.build(messageReceivedParams)
            val result = coreService.processSelectiveNotificationResponse(selectiveNotificationResponse)
            ResponseEntity.ok(result)
        } catch (e: CoreServiceException) {
            log.error("handleSms core service error", e)
            ResponseEntity.ok(
                ParlaySmppMessage(
                    methodParameters = DeclineMethodParams(CommandStatus.ESME_RSYSERR)
                )
            )
        } catch (e: NotificationResponseBuildException) {
            log.error("handleSms notification build error", e)
            ResponseEntity.ok(
                ParlaySmppMessage(
                    methodParameters = DeclineMethodParams(CommandStatus.ESME_RSYSERR)
                )
            )
        } finally {
            statisticService.increaseCounter(StatisticService.RECEIVED_SMS)
            log.debug("handleSms return")
        }
    }

    private fun getDeassignResponse(callSessionId: Long): ResponseEntity<MethodCall> {
        log.debug("{} getDeassignResponse {}", callSessionId)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
        val response = MethodCall(GWYMethod.DeassignCall.name, AppCommonParameters(callSessionId, ""), null)
        return ResponseEntity(response, headers, HttpStatus.OK)
    }

}
