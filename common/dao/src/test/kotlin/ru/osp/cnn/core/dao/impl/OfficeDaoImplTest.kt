package ru.osp.cnn.core.dao.impl

import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.unitils.UnitilsJUnit4TestClassRunner
import ru.osp.cnn.core.dao.CnnJdbcDaoTestSupport
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.WhiteNumberDO
import java.lang.IllegalArgumentException
import java.util.*

@RunWith(UnitilsJUnit4TestClassRunner::class)
class OfficeDaoImplTest : CnnJdbcDaoTestSupport() {

    @Before
    @Throws(Exception::class)
    fun setUp() {
        clearInternalDatabase()
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateService() {
        val originalServiceDO = ServiceDO(1L, "79030000001", "79030000002", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        addServiceDO(originalServiceDO)
        val removalDate = Calendar.getInstance()
        val removeAfterDate = Calendar.getInstance()
        val activationDate = Date()
        val asyncOperationDate = Date()
        val updatedServiceDO = ServiceDO(originalServiceDO.serviceId, "79030000004", "79030000005", "302",
                activationDate, ServiceStatus.NOTIFICATION_FOR_ALL, "1", true, asyncOperationDate, "ENG", removalDate, removeAfterDate)
        officeDao.updateService(updatedServiceDO)
        val resultServiceDO = findServiceDO(1L)

        //Проверка изменившихся свойств
        assertEquals(updatedServiceDO.firstNotification, resultServiceDO?.firstNotification)
        assertEquals(updatedServiceDO.newMsisdn, resultServiceDO?.newMsisdn)
        assertEquals(updatedServiceDO.oldMsisdn, resultServiceDO?.oldMsisdn)
        assertEquals(updatedServiceDO.status, resultServiceDO?.status)
        assertEquals(updatedServiceDO.subscriberBlockType, resultServiceDO?.subscriberBlockType)
        assertEqualDates(updatedServiceDO.activationDate, resultServiceDO?.activationDate)
        assertEqualDates(updatedServiceDO.asyncOperationDate, resultServiceDO?.asyncOperationDate)
        assertEqualCalendars(updatedServiceDO.removalDate, resultServiceDO?.removalDate)
        assertEqualCalendars(updatedServiceDO.removeAfterDate, resultServiceDO?.removeAfterDate)

        //Проверка не изменившихся свойств
        assertEquals(originalServiceDO.langCode, resultServiceDO?.langCode)
        assertEquals(originalServiceDO.techNumberMsisdn, resultServiceDO?.techNumberMsisdn)
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateService_NoData() {
        val updatedServiceDO = ServiceDO(1L, "79030000001", "79030000002", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        officeDao.updateService(updatedServiceDO)
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(Exception::class)
    fun testUpdateService_NullID() {
        val updatedServiceDO = ServiceDO(1L, "79030000001", "79030000002", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        updatedServiceDO.serviceId = null
        officeDao.updateService(updatedServiceDO)
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateService_NullProperties() {
        val originalServiceDO = ServiceDO(1L, "79030000001", "79030000002", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        addServiceDO(originalServiceDO)
        val updatedServiceDO = ServiceDO(originalServiceDO.serviceId, null, null, null, null,
                null, null, null, null, null, null, null)
        officeDao.updateService(updatedServiceDO)
        val resultServiceDO = findServiceDO(originalServiceDO.serviceId!!)

        //Проверка не изменившихся свойств
        assertEqualServiceDOs(originalServiceDO, resultServiceDO!!)
    }

    @Test
    @Throws(DaoException::class)
    fun testRemoveWhiteNumbersByServiceId() {
        val serviceDO1 = ServiceDO(1L, "79030000001", "79030000002", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        addServiceDO(serviceDO1)
        val serviceDO2 = ServiceDO(2L, "79030000003", "79030000004", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        addServiceDO(serviceDO2)
        val whiteNumberDO1 = WhiteNumberDO()
        whiteNumberDO1.whiteNumberId = 1L
        whiteNumberDO1.serviceId = serviceDO1.serviceId
        whiteNumberDO1.createTime = Date()
        whiteNumberDO1.whiteNumber = "79671863001"
        addWhiteNumberDO(whiteNumberDO1)
        val whiteNumberDO2 = WhiteNumberDO()
        whiteNumberDO2.whiteNumberId = 2L
        whiteNumberDO2.serviceId = serviceDO1.serviceId
        whiteNumberDO2.createTime = Date()
        whiteNumberDO2.whiteNumber = "79671863002"
        addWhiteNumberDO(whiteNumberDO2)
        val whiteNumberDO3 = WhiteNumberDO()
        whiteNumberDO3.whiteNumberId = 3L
        whiteNumberDO3.serviceId = serviceDO2.serviceId
        whiteNumberDO3.createTime = Date()
        whiteNumberDO3.whiteNumber = "79671863003"
        addWhiteNumberDO(whiteNumberDO3)

        officeDao.removeAllWhiteNumbersByServiceId(serviceDO1.serviceId!!)
        val whiteNumberDOs1: List<WhiteNumberDO> = officeDao.getWhiteNumbers(serviceDO1.serviceId!!)
        Assert.assertTrue(whiteNumberDOs1.isEmpty())
        val whiteNumberDOs2: List<WhiteNumberDO> = officeDao.getWhiteNumbers(serviceDO2.serviceId!!)
        Assert.assertFalse(whiteNumberDOs2.isEmpty())
    }

}