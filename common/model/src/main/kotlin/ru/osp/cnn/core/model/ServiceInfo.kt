package ru.osp.cnn.core.model

import java.io.Serializable

data class ServiceInfo(var status: String? = null, var oldMsisdn: String? = null) : Serializable
