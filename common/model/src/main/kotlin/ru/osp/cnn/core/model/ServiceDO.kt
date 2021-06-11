package ru.osp.cnn.core.model

import java.io.Serializable
import java.util.*

data class ServiceDO(
    var serviceId: Long? = null,
    var newMsisdn: String? = null,
    var oldMsisdn: String? = null,
    var techNumberMsisdn: String? = null,
    var activationDate: Date? = null,
    var status: ServiceStatus? = null,
    var subscriberBlockType: String? = null,
    var firstNotification: Boolean? = null,
    var asyncOperationDate: Date? = null,
    var langCode: String? = null,
    var removalDate: Calendar? = null,
    var removeAfterDate: Calendar? = null
) : Serializable
