package ru.osp.cnn.core.dao.impl

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.unitils.UnitilsJUnit4TestClassRunner
import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.SmsControlRecordDO
import ru.osp.cnn.core.dao.CnnJdbcDaoTestSupport
import ru.osp.cnn.core.dao.DaoException
import java.util.*

@RunWith(UnitilsJUnit4TestClassRunner::class)
class CoreDaoImplTest : CnnJdbcDaoTestSupport() {
    @Before
    @Throws(Exception::class)
    fun setUp() {
        clearInternalDatabase()
    }

    @Test
    @Throws(Exception::class)
    fun testGetServiceByNewMsisdnAndStatuses() {
        val requiredNewMsisdn = "79030000003"
        val requiredStatus = ServiceStatus.SELECTIVE_NOTIFICATION
        val serviceDO1 = ServiceDO(1L, "79030000001", "79031111111", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        val serviceDO2 = ServiceDO(2L, "79030000002", "79032222222", null, Date(), requiredStatus, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        val serviceDO3 = ServiceDO(3L, requiredNewMsisdn, "79033333333", null, Date(), requiredStatus, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        addServiceDO(serviceDO1)
        addServiceDO(serviceDO2)
        addServiceDO(serviceDO3)
        val serviceDO = coreDao.findServiceByNewMsisdnAndStatuses(requiredNewMsisdn, EnumSet.of(requiredStatus))!!
        assertEqualServiceDOs(serviceDO3, serviceDO)
    }

    @Test
    @Throws(Exception::class)
    fun testGetServiceByNewMsisdnAndStatuses_ServiceNotFound() {
        val requiredNewMsisdn = "79030000003"
        val requiredStatus = ServiceStatus.SELECTIVE_NOTIFICATION
        val serviceDO1 = ServiceDO(1L, "79030000001", "79031111111", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        val serviceDO2 = ServiceDO(2L, "79030000002", "79032222222", null, Date(), requiredStatus, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        val serviceDO3 = ServiceDO(3L, requiredNewMsisdn, "79033333333", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        addServiceDO(serviceDO1)
        addServiceDO(serviceDO2)
        addServiceDO(serviceDO3)
        val serviceDO = coreDao.findServiceByNewMsisdnAndStatuses(requiredNewMsisdn, EnumSet.of(requiredStatus))
        Assert.assertNull(serviceDO)
    }

    @Test(expected = DaoException::class)
    @Throws(Exception::class)
    fun testGetServiceByNewMsisdnAndStatuses_IncorrectParameters() {
        coreDao.findServiceByNewMsisdnAndStatuses(null, null)
    }

    @Test
    @Throws(DaoException::class)
    fun testRemoveSmsControlRecordsByServiceId() {
        val serviceDO1 = ServiceDO(1L, "79030000001", "79030000002", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        val serviceDO2 = ServiceDO(2L, "79030000003", "79030000004", null, Date(), ServiceStatus.DISABLED, "0",
                false, Date(), "RUS", Calendar.getInstance(), Calendar.getInstance())
        val smsControlRecordDO1 = SmsControlRecordDO()
        smsControlRecordDO1.recordId = 1L
        smsControlRecordDO1.shortSmsNumber = "111"
        smsControlRecordDO1.sendDate = Date()
        smsControlRecordDO1.serviceId = serviceDO1.serviceId
        smsControlRecordDO1.callerNumber = "555"
        val smsControlRecordDO2 = SmsControlRecordDO()
        smsControlRecordDO2.recordId = 2L
        smsControlRecordDO2.shortSmsNumber = "112"
        smsControlRecordDO2.sendDate = Date()
        smsControlRecordDO2.serviceId = serviceDO1.serviceId
        smsControlRecordDO2.callerNumber = "555"
        val smsControlRecordDO3 = SmsControlRecordDO()
        smsControlRecordDO3.recordId = 3L
        smsControlRecordDO3.shortSmsNumber = "113"
        smsControlRecordDO3.sendDate = Date()
        smsControlRecordDO3.serviceId = serviceDO2.serviceId
        smsControlRecordDO3.callerNumber = "555"

        addServiceDO(serviceDO1)
        addServiceDO(serviceDO2)
        addSmsControlRecordDO(smsControlRecordDO1)
        addSmsControlRecordDO(smsControlRecordDO2)
        addSmsControlRecordDO(smsControlRecordDO3)

        coreDao.removeAllSmsControlRecordsByServiceId(serviceDO1.serviceId!!)
        val smsControlRecordDOs1: Collection<SmsControlRecordDO> = coreDao.findAllSmsRecordsByService(serviceDO1)
        Assert.assertTrue(smsControlRecordDOs1.isEmpty())
        val smsControlRecordDOs2: Collection<SmsControlRecordDO> = coreDao.findAllSmsRecordsByService(serviceDO2)
        Assert.assertFalse(smsControlRecordDOs2.isEmpty())
    }
}