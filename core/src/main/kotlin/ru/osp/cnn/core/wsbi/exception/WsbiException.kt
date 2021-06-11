package ru.osp.cnn.core.wsbi.exception

import ru.lanit.beeline.cnn.officebackend.ws.onlineoperations.client.ServiceException_Exception
import ru.osp.cnn.core.service.WsbiException
import java.text.MessageFormat

class WsbiInteractionException : WsbiException {
    var errorCode: String? = null
    var description: String? = null

    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(message: String?, exception: ServiceException_Exception) : super(MessageFormat.format("{0} WSBI Error Code = {1}, description = {2}",
        message, exception.faultInfo.errorCode, exception.faultInfo.description), exception) {
        errorCode = exception.faultInfo.errorCode
        description = exception.faultInfo.description
    }
}

class WsbiFatalException(message: String?, cause: Throwable?) : WsbiException(message, cause)
