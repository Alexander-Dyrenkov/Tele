package ru.osp.cnn.core.dao

import ru.osp.cnn.core.model.SettingDO

interface EnvironmentDAO {
    /**
     * Returns a setting by its name.
     * @param settingName setting name
     * @return setting setting object
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun getSettingByName(settingName: String): SettingDO

}
