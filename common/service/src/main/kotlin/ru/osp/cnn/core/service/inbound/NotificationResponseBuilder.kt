package ru.osp.cnn.core.service.inbound

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.osp.cnn.core.model.SelectiveNotificationResponse
import ru.osp.smppgateway.MessageReceivedMethodParams


interface NotificationResponseBuilder {
    @Throws(NotificationResponseBuildException::class)
    fun build(deliverSm: MessageReceivedMethodParams?): SelectiveNotificationResponse?
}

@Component("notificationResponseBuilder")
class NotificationResponseBuilderImpl(
    @Qualifier("contentDecoder")
    private val decoder: ContentDecoder,
): NotificationResponseBuilder {
    companion object {
        private val logger = LoggerFactory.getLogger(NotificationResponseBuilderImpl::class.java)
    }

    override fun build(deliverSm: MessageReceivedMethodParams?): SelectiveNotificationResponse? {
        logger.debug("build deliverSm='{}'", deliverSm)
        val result = deliverSm?.text?.let { text ->
            val smsContent =
                if (decoder.canDecode(text)) {
                    decoder.decode(text)
                } else {
                    null
                }
            val originatingAddress: String = deliverSm.sourceAddress.address
            val destinationAddress: String = deliverSm.destinationAddress.address
            if (smsContent != null) {
                SelectiveNotificationResponse(originatingAddress, destinationAddress, recognize(smsContent))
            } else {
                logger.error("cannot decode sms text = '{}'", text)
                null
            }
        }
        logger.debug("build result='{}'", result)
        return result
    }

    @Throws(NotificationResponseBuildException::class)
    protected fun recognize(smsContent: String): SelectiveNotificationResponse.Command {
        for (command in SelectiveNotificationResponse.Command.values()) {
            if (command.commandCode == smsContent) {
                return command
            }
        }
        throw NotificationResponseBuildException("Can not recognize command in sms content: $smsContent")
    }
}
