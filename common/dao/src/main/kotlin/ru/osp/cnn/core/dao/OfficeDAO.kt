package ru.osp.cnn.core.dao

import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.WhiteNumberDO

interface OfficeDAO {
    /**
     * Create new white number.
     * @param newWhiteNumberDO new white number
     * @return created white number
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun createWhiteNumber(newWhiteNumberDO: WhiteNumberDO): WhiteNumberDO

    /**
     * Delete white number.
     * @param whiteNumberId id of deleting white number
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun deleteWhiteNumber(whiteNumberId: Long)

    /**
     * Get collection of white numbers for service.
     * @param serviceId service id
     * @return collection of white numbers
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun getWhiteNumbers(serviceId: Long): List<WhiteNumberDO>

    /**
     * Update service.
     * @param updServiceDO updating service
     * @return updated service
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun updateService(updServiceDO: ServiceDO)

    /**
     * Removes all WhiteNumbers by serviceId
     * @param serviceId
     * @throws DaoException
     */
    @Throws(DaoException::class)
    fun removeAllWhiteNumbersByServiceId(serviceId: Long)

}
