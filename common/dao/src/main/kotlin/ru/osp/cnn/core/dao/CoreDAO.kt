package ru.osp.cnn.core.dao

import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.SmsControlRecordDO
import java.util.*

interface CoreDAO {
    /**
     * Create new sms control record.
     * @param newSmsControlRecordDO sms control record object
     * @return created sms control record
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun saveOrUpdateSmsControlRecord(newSmsControlRecordDO: SmsControlRecordDO): SmsControlRecordDO

    fun findAllSmsRecordsByService(service: ServiceDO): List<SmsControlRecordDO>

    /**
     * Return 0..* records for provided parameters.
     * @param service must not be null, will throw NPE if provided
     * @param callerNumber caller number
     * @return 0..* records for provided parameters
     */
    fun findAllSmsRecordsByServiceAndCallerNumber(service: ServiceDO, callerNumber: String): List<SmsControlRecordDO>

    fun findSmsRecordByServiceAndShortNumber(service: ServiceDO, shortNumber: String): SmsControlRecordDO

    /**
     * Delete sms control record.
     * @param recordId record id
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun deleteSmsControlRecord(recordId: Long)

    /**
     * Get service by msisdn.
     * @param msisdn msisdn
     * @return  service
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun getServiceByNewMsisdn(msisdn: String): ServiceDO?

    /**
     * Get service by msisdn.
     * @param newMsisdn msisdn
     * @return serviceDO или null, если по критерию не было ничего найдено
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun findServiceByNewMsisdnAndStatuses(newMsisdn: String?, serviceStatuses: EnumSet<ServiceStatus>?): ServiceDO?

    @Throws(DaoException::class)
    fun getServiceByTechNumber(techNumber: String): ServiceDO?

    /**
     * Get collection of services with activation date before given date.
     * @param activationDate activation date
     * @return collection of services
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun getServicesBeforeActivationDate(activationDate: Date): List<ServiceDO>

    /**
     * Get collection of services with asyncOperation date before given date.
     * @param asyncOperationDate activation date
     * @return collection of services
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun getServicesBeforeAsyncOperationDate(asyncOperationDate: Date): List<ServiceDO>

    @Throws(DaoException::class)
    fun deleteExpiredSmsControlRecords(onDate: Date)

    /**
     * Removes all SmsControlRecords by serviceId
     * @param serviceId
     * @throws DaoException
     */
    @Throws(DaoException::class)
    fun removeAllSmsControlRecordsByServiceId(serviceId: Long)
}