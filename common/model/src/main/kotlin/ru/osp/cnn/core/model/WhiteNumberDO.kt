package ru.osp.cnn.core.model

import java.io.Serializable
import java.util.*

data class WhiteNumberDO(var whiteNumberId: Long? = null, var serviceId: Long? = null, var whiteNumber: String? = null, var createTime: Date? = null) : Serializable
