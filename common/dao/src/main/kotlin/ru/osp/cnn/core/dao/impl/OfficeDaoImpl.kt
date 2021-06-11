package ru.osp.cnn.core.dao.impl

import org.hibernate.HibernateException
import org.hibernate.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.osp.cnn.core.dao.BaseDAO
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.dao.OfficeDAO
import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.WhiteNumberDO

@Component("officeDao")
class OfficeDaoImpl: BaseDAO(), OfficeDAO {
    companion object {
        private val log = LoggerFactory.getLogger(OfficeDaoImpl::class.java)
    }

    override fun createWhiteNumber(newWhiteNumberDO: WhiteNumberDO): WhiteNumberDO {
        return try {
            val sess: Session = getSession()
            sess.save(newWhiteNumberDO)
            newWhiteNumberDO
        } catch (e: HibernateException) {
            throw convertException("Error in createWhiteNumber()", "Can't create WhiteNumberDO object $newWhiteNumberDO", e)
        }
    }

    override fun deleteWhiteNumber(whiteNumberId: Long) {
        try {
            val sess: Session = getSession()
            val whiteNumberDO = sess.load(WhiteNumberDO::class.java, whiteNumberId) as WhiteNumberDO
            sess.delete(whiteNumberDO)
        } catch (e: HibernateException) {
            throw convertException("Error in deleteWhiteNumber()", "Can't delete WhiteNumberDO object with whiteNumberId = $whiteNumberId", e)
        }
    }

    override fun getWhiteNumbers(serviceId: Long): List<WhiteNumberDO> {
        return try {
            getSession().getNamedQuery("getWhiteNumbersByServiceId")
                    .setParameter("serviceId", serviceId)
                    .list() as List<WhiteNumberDO>
        } catch (e: HibernateException) {
            throw convertException("Error in getWhiteNumbers()", "Can't get whitenumbers with serviceId = $serviceId", e)
        }
    }

    override fun updateService(updServiceDO: ServiceDO) {
        try {
            val sess = getSession()
            val curServiceDO = sess[ServiceDO::class.java, updServiceDO.serviceId]
            if (curServiceDO != null) {
                updServiceDO.activationDate?.let {
                    curServiceDO.activationDate = updServiceDO.activationDate
                }
                updServiceDO.firstNotification?.let {
                    curServiceDO.firstNotification = updServiceDO.firstNotification
                }
                updServiceDO.newMsisdn?.let {
                    curServiceDO.newMsisdn = updServiceDO.newMsisdn
                }
                updServiceDO.oldMsisdn?.let {
                    curServiceDO.oldMsisdn = updServiceDO.oldMsisdn
                }
                updServiceDO.status?.let {
                    curServiceDO.status = updServiceDO.status
                }
                updServiceDO.subscriberBlockType?.let {
                    curServiceDO.subscriberBlockType = updServiceDO.subscriberBlockType
                }
                updServiceDO.asyncOperationDate?.let {
                    curServiceDO.asyncOperationDate = updServiceDO.asyncOperationDate
                }
                updServiceDO.removalDate?.let {
                    curServiceDO.removalDate = updServiceDO.removalDate
                }
                updServiceDO.removeAfterDate?.let {
                    curServiceDO.removeAfterDate = updServiceDO.removeAfterDate
                }
            }
        } catch (e: HibernateException) {
            throw convertException("Error in updateService()", "Can't update ServiceDO object with serviceId = " + updServiceDO.serviceId, e)
        }
    }

    override fun removeAllWhiteNumbersByServiceId(serviceId: Long) {
        try {
            val query = getSession().getNamedQuery("removeAllByServiceId")
            query.setParameter("serviceId", serviceId)
            val count = query.executeUpdate().toLong()
            log.debug("removeAllByServiceId operation removed $count records")
        } catch (e: HibernateException) {
            throw convertException("Error in removeAllByServiceId() serviceId: $serviceId", e.message!!, e)
        }
    }

    private fun convertException(error: String, cause: String, e: Exception): DaoException {
        log.warn("{}: {}.", error, cause, e)
        log.error("{} Exception: {}", cause, e.toString())
        return DaoException("$cause.", e)
    }
}