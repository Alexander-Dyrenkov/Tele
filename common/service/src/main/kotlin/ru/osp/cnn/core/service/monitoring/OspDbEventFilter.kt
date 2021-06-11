package ru.osp.cnn.core.service.monitoring

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.osp.cnn.core.dao.EnvironmentDAO
import java.sql.SQLException

@Component("ospDbEventFilter")
class OspDbEventFilter(@Qualifier("environmentDao") environmentDAO: EnvironmentDAO) : OspEventFilterBase(environmentDAO) {
    companion object {
        private const val DB_ALARM_CODES_SETTING_NAME = "DB_ALARM_CODES"
    }

    override fun isTransitionEvent(t: Throwable, expectedErrorValue: String): Boolean {
        return t is SQLException && expectedErrorValue == t.errorCode.toString()
    }

    override val settingName: String
        get() = DB_ALARM_CODES_SETTING_NAME

    @Async("ospEventFilterSettingsUpdateScheduler")
    @Scheduled(fixedRateString = "\${update.db.event.filter.interval}")
    @Transactional
    override fun updateSettings() {
        super.updateSettings()
    }

}
