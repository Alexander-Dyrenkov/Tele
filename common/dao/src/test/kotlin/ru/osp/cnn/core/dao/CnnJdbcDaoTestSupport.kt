package ru.osp.cnn.core.dao

import org.junit.Assert.assertEquals
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import ru.lanit.commons.dao.UtilDao
import ru.lanit.commons.jdbc.JdbcOperationsExtended
import ru.osp.cnn.core.model.*
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*

open class CnnJdbcDaoTestSupport {
    protected val environmentDao: EnvironmentDAO
    protected val coreDao: CoreDAO
    protected val officeDao: OfficeDAO
    protected val internalJdbcOperations: JdbcOperationsExtended
    protected val internalNamedParameterJdbcOperations: NamedParameterJdbcOperations
    protected val utilDao: UtilDao

    companion object {
        private const val FIND_CNN_SERVICE_BY_SERVICE_ID = "select * from CNN_SERVICE where SERVICE_ID = ?"
        private const val FIND_CNN_TECH_NUMBER_BY_TECH_NUMBER_MSISDN = "select * from CNN_TECH_NUMBER where TECH_NUMBER_MSISDN = ?"
        private const val COUNT_CNN_SERVICE_REQUEST_BY_SERVICE_ID = "select count (*) from CNN_SERVICE where SERVICE_ID = ?"
        private const val ADD_CNN_SERVICE = "insert into CNN_SERVICE (SERVICE_ID, NEW_MSISDN, OLD_MSISDN, SUBSCRIBER_BLOCK_TYPE, ACTIVATION_DATE, IS_FIRST_NOTIFICATION, ASYNC_OPERATION_DATE, LANG_CODE, REMOVAL_DATE, REMOVE_AFTER_DATE, TECH_NUMBER_MSISDN, STATUS) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private const val ADD_CNN_TECH_NUMBER = "insert into CNN_TECH_NUMBER (TECH_NUMBER_MSISDN, TECH_NUMBER_STATE, MARKET_CODE, LAST_OPERATION_DATE) values (?, ?, ?, ?)"
        private const val ADD_WHITE_NUMBER = "insert into CNN_WHITE_NUMBER (WHITE_NUMBER_ID, WHITE_NUMBER, SERVICE_ID, CREATE_TIME) values (?, ?, ?, ?)"
        private const val ADD_SMS_CONTROL_RECORD = "insert into CNN_SMS_CONTROL_RECORD (LIST_ITEM_ID, SHORT_SMS_NUMBER, SEND_DATE, SERVICE_ID, CALLER_NUMBER) values (?, ?, ?, ?, ?)"
        private val TABLES = arrayOf("CNN_LOG", "CNN_SERVICE", "CNN_SMS_CONTROL_RECORD", "CNN_STATE", "CNN_TECH_NUMBER", "CNN_WHITE_NUMBER")

    }

    init {
        try {
            val context: ApplicationContext = ClassPathXmlApplicationContext("cnnDataAccessTestSpringContext.xml")
            coreDao = context.getBean("coreDao") as CoreDAO
            environmentDao = context.getBean("environmentDao") as EnvironmentDAO
            officeDao = context.getBean("officeDao") as OfficeDAO
            utilDao = context.getBean("utilDao") as UtilDao
            internalJdbcOperations = context.getBean("jdbcTemplateExtended") as JdbcOperationsExtended
            internalNamedParameterJdbcOperations = context.getBean("namedParameterJdbcTemplate") as NamedParameterJdbcOperations
        } catch (e: Exception) {
            throw RuntimeException(e.message, e)
        }
    }

    protected fun clearInternalDatabase() {
        for (table in TABLES) {
            internalJdbcOperations.execute("delete from $table")
        }
    }

    protected fun countCnnService(serviceId: Long): Int {
        return internalJdbcOperations.queryForObject(COUNT_CNN_SERVICE_REQUEST_BY_SERVICE_ID, Int::class.java, arrayOf<Any>(serviceId)) ?: 0
    }

    protected fun findServiceDO(serviceId: Long): ServiceDO? {
        return internalJdbcOperations.queryForObject(FIND_CNN_SERVICE_BY_SERVICE_ID, arrayOf<Any>(serviceId), ServiceDORowMapper())
    }

    protected fun addServiceDO(serviceDO: ServiceDO) {
        var isFirstNotification: String? = null
        serviceDO.firstNotification?.let {
            isFirstNotification = if (it) "Y" else "N"
        }
        internalJdbcOperations.update(ADD_CNN_SERVICE,
                serviceDO.serviceId, serviceDO.newMsisdn, serviceDO.oldMsisdn,
                serviceDO.subscriberBlockType,
                serviceDO.activationDate?.let { Timestamp(it.time) },
                isFirstNotification, serviceDO.asyncOperationDate,
                serviceDO.langCode,
                serviceDO.removalDate?.let { Timestamp(it.timeInMillis) },
                serviceDO.removeAfterDate?.let { Timestamp(it.timeInMillis) },
                serviceDO.techNumberMsisdn,
                serviceDO.status?.ordinal)
    }

    protected fun addSmsControlRecordDO(smsControlRecordDO: SmsControlRecordDO) {
        internalJdbcOperations.update(ADD_SMS_CONTROL_RECORD,
                smsControlRecordDO.recordId,
                smsControlRecordDO.shortSmsNumber,
                smsControlRecordDO.sendDate?.let { Timestamp(it.time) },
                smsControlRecordDO.serviceId,
                smsControlRecordDO.callerNumber)
    }

    protected open fun addWhiteNumberDO(whiteNumberDO: WhiteNumberDO) {
        internalJdbcOperations.update(ADD_WHITE_NUMBER,
                whiteNumberDO.whiteNumberId,
                whiteNumberDO.whiteNumber,
                whiteNumberDO.serviceId,
                whiteNumberDO.createTime?.let { Timestamp(it.time) })
    }

    protected inner class ServiceDORowMapper : RowMapper<ServiceDO> {
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): ServiceDO {
            val serviceDO = ServiceDO()
            serviceDO.serviceId = rs.getLong("SERVICE_ID")
            serviceDO.newMsisdn = rs.getString("NEW_MSISDN")
            serviceDO.oldMsisdn = rs.getString("OLD_MSISDN")
            serviceDO.subscriberBlockType = rs.getString("SUBSCRIBER_BLOCK_TYPE")
            serviceDO.activationDate = rs.getTimestamp("ACTIVATION_DATE")
            when (rs.getString("IS_FIRST_NOTIFICATION")) {
                "Y" -> {
                    serviceDO.firstNotification = true
                }
                "N" -> {
                    serviceDO.firstNotification = false
                }
                else -> {
                    serviceDO.firstNotification = null
                }
            }
            serviceDO.asyncOperationDate = rs.getTimestamp("ASYNC_OPERATION_DATE")
            serviceDO.langCode = rs.getString("LANG_CODE")
            val removalDate: Date = rs.getTimestamp("REMOVAL_DATE")
            val removalDateCalendar = Calendar.getInstance()
            removalDateCalendar.time = removalDate
            serviceDO.removalDate = removalDateCalendar
            val removeAfterDate: Date = rs.getTimestamp("REMOVE_AFTER_DATE")
            val removeAfterDateCalendar = Calendar.getInstance()
            removeAfterDateCalendar.time = removeAfterDate
            serviceDO.removeAfterDate = removeAfterDateCalendar
            serviceDO.techNumberMsisdn = rs.getString("TECH_NUMBER_MSISDN")
            serviceDO.status = ServiceStatus.values()[rs.getInt("STATUS")]
            return serviceDO
        }
    }

    protected fun assertEqualDates(date1: Date?, date2: Date?) {
        if (date1 == null && date2 == null) {
            return
        } else if (date1 == null && date2 != null || date1 != null && date2 == null) {
            throw AssertionError("Expected $date1 but found $date2")
        } else {
            assertEquals(date1?.time, date2?.time)
        }
    }

    protected fun assertEqualCalendars(calendar1: Calendar?, calendar2: Calendar?) {
        if (calendar1 == null && calendar2 == null) {
            return
        } else if (calendar1 == null && calendar2 != null || calendar1 != null && calendar2 == null) {
            throw AssertionError("Expected $calendar1 but found $calendar2")
        } else {
            assertEquals(calendar1?.timeInMillis, calendar2?.timeInMillis)
        }
    }

    protected fun assertEqualServiceDOs(serviceDO1: ServiceDO, serviceDO2: ServiceDO) {
        assertEquals(serviceDO1.firstNotification, serviceDO2.firstNotification)
        assertEquals(serviceDO1.newMsisdn, serviceDO2.newMsisdn)
        assertEquals(serviceDO1.oldMsisdn, serviceDO2.oldMsisdn)
        assertEquals(serviceDO1.status, serviceDO2.status)
        assertEquals(serviceDO1.subscriberBlockType, serviceDO2.subscriberBlockType)
        assertEquals(serviceDO1.langCode, serviceDO2.langCode)
        assertEquals(serviceDO1.techNumberMsisdn, serviceDO2.techNumberMsisdn)
        assertEqualDates(serviceDO1.activationDate, serviceDO2.activationDate)
        assertEqualDates(serviceDO1.asyncOperationDate, serviceDO2.asyncOperationDate)
        assertEqualCalendars(serviceDO1.removalDate, serviceDO2.removalDate)
        assertEqualCalendars(serviceDO1.removeAfterDate, serviceDO2.removeAfterDate)
    }

}
