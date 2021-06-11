package ru.osp.cnn.core.service.monitoring

interface OspEventFilter {
    fun isTransitionEvent(event: OspEvent): Boolean

    fun updateSettings()

}
