package ru.osp.cnn.core.service.monitoring

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.osp.cnn.core.dao.EnvironmentDAO

@Component("ospTransportEventFilter")
class OspTransportEventFilter (@Qualifier("environmentDao") environmentDAO: EnvironmentDAO) : OspEventFilterBase(environmentDAO) {
    companion object {
        private const val TRANSPORT_ALARM_EXCEPTIONS_SETTING_NAME = "TRANSPORT_ALARM_EXCEPTIONS"
    }

    override fun isTransitionEvent(t: Throwable, expectedErrorValue: String): Boolean {
        return expectedErrorValue == t.javaClass.name
    }

    override val settingName: String
        get() = TRANSPORT_ALARM_EXCEPTIONS_SETTING_NAME

    @Async("ospEventFilterSettingsUpdateScheduler")
    @Scheduled(fixedRateString = "\${update.transport.event.filter.interval}")
    @Transactional
    override fun updateSettings() {
        super.updateSettings()
    }

}
