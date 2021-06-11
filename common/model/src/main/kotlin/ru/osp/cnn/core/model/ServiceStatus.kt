package ru.osp.cnn.core.model

import java.util.*

enum class ServiceStatus(val value: String) {
    DISABLED("01"), SUSPENDED("0"), NOTIFICATION_FOR_ALL("1"), SELECTIVE_NOTIFICATION("2");

    companion object {
        private val map: MutableMap<String, ServiceStatus> = HashMap()
        val allButDisabled: EnumSet<ServiceStatus>
            get() = EnumSet.of(SUSPENDED, NOTIFICATION_FOR_ALL, SELECTIVE_NOTIFICATION)

        fun getServiceStatusByValue(value: String): ServiceStatus? {
            return map[value]
        }

        init {
            for (status in values()) {
                map[status.value] = status
            }
        }
    }

}
