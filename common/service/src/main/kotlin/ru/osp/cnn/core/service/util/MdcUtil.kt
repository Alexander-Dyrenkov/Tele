package ru.osp.cnn.core.service.util

import org.slf4j.MDC
import java.util.*

object MdcUtil {
    val MDC_REQUEST_ID_RANDOM = Random()

    const val MDC_REQUEST_ID_KEY = "msg_id"

    inline fun <T> withMdcContext(sessionId: Long? = MDC_REQUEST_ID_RANDOM.nextLong(), exec: () -> T): T {
        try {
            MDC.put(MDC_REQUEST_ID_KEY, sessionId.toString())
            return exec()
        } finally {
            MDC.clear()
        }
    }
}
