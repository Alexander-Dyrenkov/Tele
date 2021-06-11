package ru.osp.cnn.core.dao.impl

import org.hibernate.HibernateException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.osp.cnn.core.dao.BaseDAO
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.dao.EnvironmentDAO
import ru.osp.cnn.core.model.SettingDO

@Component("environmentDao")
class EnvironmentDaoImpl: BaseDAO(), EnvironmentDAO {
    companion object {
        private val log = LoggerFactory.getLogger(EnvironmentDaoImpl::class.java)
    }

    /**
     * Get setting by name.
     * @param settingName setting name
     * @return setting object
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    override fun getSettingByName(settingName: String): SettingDO {
        return try {
            getSession().getNamedQuery("getSettingByName")
                    .setParameter("setting_name", settingName)
                    .uniqueResult() as SettingDO
        } catch (e: HibernateException) {
            log.warn("Error in getSettingByName(). Can't get setting {}.", settingName, e)
            log.error("Can't get settings {}. Exception: {}", settingName, e.toString())
            throw DaoException("Can't get setting $settingName.", e)
        }
    }

}
