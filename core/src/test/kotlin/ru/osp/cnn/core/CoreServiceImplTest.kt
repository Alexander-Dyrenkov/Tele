package ru.osp.cnn.core

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionOperations
import ru.lanit.commons.dao.UtilDao
import ru.osp.cnn.core.dao.CoreDAO
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.dao.EnvironmentDAO
import ru.osp.cnn.core.dao.OfficeDAO
import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.SettingDO
import ru.osp.cnn.core.rmi.CoreServiceException
import ru.osp.cnn.core.service.*
import ru.osp.cnn.core.wsbi.exception.WsbiFatalException
import ru.osp.cnn.core.wsbi.exception.WsbiInteractionException
import java.text.SimpleDateFormat

@RunWith(MockitoJUnitRunner::class)
class CoreServiceImplTest {
    companion object {
        private val AUTO_OFF_CNN_SERVICE_SETTING: SettingDO = SettingDO()
    }

    private lateinit var coreServiceBo: CoreServiceImpl

    @Mock
    private lateinit var utilDao: UtilDao

    @Mock
    private lateinit var transactionOperations: TransactionOperations

    @Mock
    private lateinit var officeDAO: OfficeDAO

    @Mock
    private lateinit var coreDAO: CoreDAO

    @Mock
    private lateinit var environmentDAO: EnvironmentDAO

    @Mock
    private lateinit var wsbiClient: WsbiClient

    @Mock
    private lateinit var settings : Settings

    @Mock
    private lateinit var smsService : SmsService

    private var services: List<ServiceDO>? = null

    @Before
    @DelicateCoroutinesApi
    @Throws(DaoException::class)
    fun setUp() {
        coreServiceBo = CoreServiceImpl(coreDAO, officeDAO, environmentDAO, settings, transactionOperations, wsbiClient, utilDao, smsService, GlobalScope)

        AUTO_OFF_CNN_SERVICE_SETTING.values = listOf("1")
        val s1 = ServiceDO()
        s1.serviceId = 1L
        s1.newMsisdn = "898572111"
        s1.oldMsisdn = "891572111"
        s1.status = ServiceStatus.NOTIFICATION_FOR_ALL
        val s2 = ServiceDO()
        s2.newMsisdn = "898572112"
        s2.oldMsisdn = "891572112"
        s2.serviceId = 2L
        s2.status = ServiceStatus.SELECTIVE_NOTIFICATION
        val s3 = ServiceDO()
        s3.serviceId = 3L
        s3.newMsisdn = "898572113"
        s3.oldMsisdn = "891572113"
        s3.status = ServiceStatus.SUSPENDED
        services = listOf(s1, s2, s3)
        val currentDate = SimpleDateFormat("dd.MM.yyyy HH:mm").parse("24.05.2021 20:34")
        val beforeActivationDate = SimpleDateFormat("dd.MM.yyyy HH:mm").parse("24.04.2021 20:34")
        Mockito.`when`(environmentDAO.getSettingByName("AUTO_OFF_CNN_SERVICE")).thenReturn(AUTO_OFF_CNN_SERVICE_SETTING)
        Mockito.`when`(utilDao.currentDate).thenReturn(currentDate)
        Mockito.`when`(coreDAO.getServicesBeforeActivationDate(beforeActivationDate)).thenReturn(services)
        whenever(transactionOperations.execute(any<TransactionCallback<Unit>>())).thenAnswer {
            (it.arguments[0] as TransactionCallback<*>).doInTransaction(mock())
        }
    }

    @Test
    @Throws(WsbiException::class, DaoException::class, CoreServiceException::class)
    fun testDisableExpiredServicesWsbiFatalException() {
        Mockito.`when`(wsbiClient.disableService(services!![0].newMsisdn!!)).thenThrow(WsbiFatalException("message", null))

        coreServiceBo.disableExpiredServices()

        expectDisableService(services!![2])
        expectDisableService(services!![1])
    }

    @Test
    @Throws(WsbiException::class, DaoException::class, CoreServiceException::class)
    fun testDisableExpiredServicesDaoException() {
        Mockito.`when`(officeDAO.updateService(services!![0])).thenThrow(DaoException("message"))

        coreServiceBo.disableExpiredServices()

        Mockito.verify(wsbiClient).disableService(services!![0].newMsisdn!!)
        expectDisableService(services!![1])
        expectDisableService(services!![2])
    }

    @Test
    @Throws(WsbiException::class, DaoException::class, CoreServiceException::class)
    fun testDisableExpiredServicesWsbiInteractionException() {
        Mockito.`when`(wsbiClient.disableService(services!![0].newMsisdn!!)).thenThrow(WsbiInteractionException("message", Exception()))
        Mockito.`when`(wsbiClient.disableService(services!![1].newMsisdn!!)).thenThrow(WsbiInteractionException("message", Exception()))
        Mockito.`when`(wsbiClient.disableService(services!![2].newMsisdn!!)).thenThrow(WsbiInteractionException("message", Exception()))
        coreServiceBo.disableExpiredServices()
    }

    @Throws(DaoException::class, WsbiException::class)
    private fun expectDisableService(service: ServiceDO) {
        Mockito.verify(wsbiClient).disableService(service.newMsisdn!!)
        Mockito.verify(officeDAO).updateService(service)
        Mockito.verify(officeDAO).removeAllWhiteNumbersByServiceId(service.serviceId!!)
        Mockito.verify(coreDAO).removeAllSmsControlRecordsByServiceId(service.serviceId!!)
    }

}
