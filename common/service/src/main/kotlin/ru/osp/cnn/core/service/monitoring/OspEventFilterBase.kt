package ru.osp.cnn.core.service.monitoring

import org.apache.commons.lang.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.dao.EnvironmentDAO
import ru.osp.cnn.core.model.SettingDO

abstract class OspEventFilterBase(private val environmentDAO: EnvironmentDAO) : OspEventFilter {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(OspEventFilterBase::class.java)
    }

    private var setting: SettingDO? = null

    override fun isTransitionEvent(event: OspEvent): Boolean {
        val targetException = ExceptionUtils.getRootCause(event.cause)
        setting?.let {
            val values: Collection<String> = it.values
            for (value in values) {
                if (isTransitionEvent(targetException, value)) {
                    return true
                }
            }
        }
        return false
    }

    override fun updateSettings() {
        log.debug("updateSetting {}", settingName)
        try {
            setting = environmentDAO.getSettingByName(settingName)
        } catch (e: DaoException) {
            log.error("Can't get setting {}", settingName, e)
        }
    }

    protected abstract val settingName: String

    protected abstract fun isTransitionEvent(t: Throwable, expectedErrorValue: String): Boolean

}
