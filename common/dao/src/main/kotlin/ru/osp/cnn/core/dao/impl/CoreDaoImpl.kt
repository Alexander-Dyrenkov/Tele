package ru.osp.cnn.core.dao.impl

import org.hibernate.HibernateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.osp.cnn.core.dao.BaseDAO
import ru.osp.cnn.core.dao.CoreDAO
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.SmsControlRecordDO
import java.util.*

@Component("coreDao")
class CoreDaoImpl : BaseDAO(), CoreDAO {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(CoreDaoImpl::class.java)
    }


    /**
     * Create new sms control record.
     * @param newSmsControlRecordDO sms control record object
     * @return  created sms control record
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    override fun saveOrUpdateSmsControlRecord(newSmsControlRecordDO: SmsControlRecordDO): SmsControlRecordDO {
        return try {
            getSession().saveOrUpdate(newSmsControlRecordDO)
            newSmsControlRecordDO
        } catch (e: HibernateException) {
            log.warn("Error in saveOrUpdateSmsControlRecord(). Can't create SmsControlRecordDO object {}.", newSmsControlRecordDO, e)
            log.error("Can't create SmsControlRecordDO object {}. Exception: {}", newSmsControlRecordDO, e.toString())
            throw DaoException("Can't create SmsControlRecordDO object $newSmsControlRecordDO.", e)
        }
    }

    override fun findAllSmsRecordsByService(service: ServiceDO): List<SmsControlRecordDO> {
        return getSession().getNamedQuery("findSmsControlRecordsByServiceId")
                .setLong("serviceId", service.serviceId!!)
                .list() as List<SmsControlRecordDO>
    }

    override fun findAllSmsRecordsByServiceAndCallerNumber(service: ServiceDO, callerNumber: String): List<SmsControlRecordDO> {
        return getSession().getNamedQuery("findSmsControlRecordsByServiceIdAndCallerNumber")
                .setLong("serviceId", service.serviceId!!)
                .setString("callerNumber", callerNumber)
                .list() as List<SmsControlRecordDO>
    }

    override fun findSmsRecordByServiceAndShortNumber(service: ServiceDO, shortNumber: String): SmsControlRecordDO {
        return getSession().getNamedQuery("findSmsControlRecordsByServiceAndCalledNumber")
                .setLong("serviceId", service.serviceId!!)
                .setString("shortNumber", shortNumber)
                .uniqueResult() as SmsControlRecordDO
    }

    /**
     * Delete sms control record.
     * @param recordId record id
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    override fun deleteSmsControlRecord(recordId: Long) {
        try {
            val currentSession = getSession()
            val smsRecord = currentSession.load(SmsControlRecordDO::class.java, recordId) as SmsControlRecordDO
            currentSession.delete(smsRecord)
        } catch (e: HibernateException) {
            log.warn("Error in deleteSmsControlRecord(). Can't delete SmsControlRecordDO with recordId = {}.", recordId, e)
            log.error("Can't delete SmsControlRecordDO with recordId = {}. Exception: {}", recordId, e.toString())
            throw DaoException("Can't delete SmsControlRecordDO with recordId = $recordId.", e)
        }
    }

    /**
     * Get service by msisdn.
     * @param msisdn msisdn
     * @return  service
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    override fun getServiceByNewMsisdn(msisdn: String): ServiceDO? {
        return try {
            getSession().getNamedQuery("getServiceByNewMsisdn")
                    .setParameter("msisdn", msisdn)
                    .uniqueResult() as ServiceDO?
        } catch (e: HibernateException) {
            log.warn("Error in getServiceByNewMsisdn(). Can't get ServiceDO objects with newMsisdn = {}.", msisdn, e)
            log.error("Can't get ServiceDO objects with newMsisdn = {}. Exception: {}", msisdn, e.toString())
            throw DaoException("Can't find ServiceDO object with msisdn = $msisdn.", e)
        }
    }

    @Throws(DaoException::class)
    override fun findServiceByNewMsisdnAndStatuses(newMsisdn: String?, serviceStatuses: EnumSet<ServiceStatus>?): ServiceDO? {
        return try {
            getSession().getNamedQuery("findServiceByNewMsisdnAndStatuses")
                    .setParameter("msisdn", newMsisdn)
                    .setParameterList("statuses", serviceStatuses)
                    .uniqueResult() as ServiceDO?
        } catch (e: Exception) {
            log.error("Error in findServiceByNewMsisdnAndStatuses() msisdn = [{}] and serviceStatuses = {}.", arrayOf(newMsisdn, serviceStatuses.toString(), e))
            throw DaoException("Error in findServiceByNewMsisdnAndStatuses() msisdn = [$newMsisdn] and serviceStatuses = $serviceStatuses.", e)
        }
    }

    @Throws(DaoException::class)
    override fun getServiceByTechNumber(techNumber: String): ServiceDO? {
        return try {
            getSession().getNamedQuery("getServiceByTechNumber")
                    .setParameter("techNumber", techNumber)
                    .uniqueResult() as ServiceDO?
        } catch (e: HibernateException) {
            log.error("Can't get ServiceDO objects with techNumber = {}. Exception: {}", techNumber, e.toString())
            throw DaoException("Can't find ServiceDO object by techNumber $techNumber.", e)
        }
    }

    /**
     * Get service by short number.
     * @param msisdn msisdn
     * @param shortNumber short number
     * @return service
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    fun getServiceByShortNumber(msisdn: String?, shortNumber: String): ServiceDO {
        return try {
            getSession().getNamedQuery("getServiceByShortNumber")
                    .setParameter("shortNumber", shortNumber)
                    .setParameter("msisdn", msisdn)
                    .uniqueResult() as ServiceDO
        } catch (e: HibernateException) {
            log.warn("Error in getServiceByShortNumber(). Can't get ServiceDO with short number = {}.", shortNumber, e)
            log.error("Can't get ServiceDO with short number = {}. Exception: ", shortNumber, e.toString())
            throw DaoException("Can't find ServiceDO object with short number = "
                    + shortNumber + ".", e)
        }
    }

    /**
     * Get collection of services with activation date before given date.
     * @param activationDate activation date
     * @return collection of services
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    override fun getServicesBeforeActivationDate(activationDate: Date): List<ServiceDO> {
        return try {
            getSession().getNamedQuery("getServicesBeforeActivationDate")
                    .setParameter("activationDate", activationDate)
                    .list() as List<ServiceDO>
        } catch (e: HibernateException) {
            log.warn("Error in getServicesBeforeActivationDate(). Can't get collection of ServiceDO with activation date before {}.", activationDate, e)
            log.error("Can't get collection of ServiceDO with activation date before {}. Exception: ", activationDate, e.toString())
            throw DaoException("Can't get collection of ServiceDO with activation date before $activationDate.", e)
        }
    }

    @Throws(DaoException::class)
    override fun getServicesBeforeAsyncOperationDate(asyncOperationDate: Date): List<ServiceDO> {
        return try {
            getSession().getNamedQuery("getServicesBeforeAsyncOperationDate")
                    .setParameter("asyncOperationDate", asyncOperationDate)
                    .list() as List<ServiceDO>
        } catch (e: HibernateException) {
            throw DaoException("Can't get collection of ServiceDO with asyncOperationDate before $asyncOperationDate.", e)
        }
    }

    @Throws(DaoException::class)
    override fun deleteExpiredSmsControlRecords(onDate: Date) {
        try {
            getSession().getNamedQuery("deleteExpiredSmsControlRecordsOnDate")
                    .setParameter("sendDate", onDate)
                    .executeUpdate()
        } catch (e: HibernateException) {
            throw DaoException("Can't remove expired sms control records due to ${e.message}", e)
        }
    }

    @Throws(DaoException::class)
    override fun removeAllSmsControlRecordsByServiceId(serviceId: Long) {
        try {
            val count = getSession().getNamedQuery("removeAllSmsByServiceId")
                    .setParameter("serviceId", serviceId)
                    .executeUpdate()
                    .toLong()
            log.debug("removeAllByServiceId operation removed $count records")
        } catch (e: HibernateException) {
            throw DaoException("Error in removeAllSmsByServiceId() serviceId: ${e.message}", e)
        }
    }

}
