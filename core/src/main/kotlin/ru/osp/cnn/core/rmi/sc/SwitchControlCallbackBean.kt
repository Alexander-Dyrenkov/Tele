package ru.osp.cnn.core.rmi.sc

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.osp.cnn.core.CoreService
import ru.osp.cnn.core.rmi.CoreServiceException
import ru.osp.cnn.core.service.StatisticService
import ru.osp.cnn.core.service.StatisticService.Companion.SC_CALLS
import java.rmi.RemoteException

@Component("switchControlCallbackBean")
class SwitchControlCallbackBean(@Qualifier("coreService") private val coreService: CoreService,
                                @Qualifier("statisticManager") private val statisticService: StatisticService) : SwitchControlCallbackBusiness {

    override fun serviceEnabled(newMsisdn: String, oldMsisdn: String, techNumber: String, languageCode: String?) {
        coreService.serviceEnabled(newMsisdn, oldMsisdn, techNumber, languageCode)
        statisticService.increaseCounter(SC_CALLS)
    }

    override fun serviceEnableFailed(newMsisdn: String, languageCode: String?) {
        coreService.serviceEnableFailed(newMsisdn, languageCode)
        statisticService.increaseCounter(SC_CALLS)
    }

    @Throws(RemoteException::class)
    override fun serviceDisabled(newMsisdn: String, oldMsisdn: String, languageCode: String?, serviceId: Long) {
        try {
            coreService.serviceDisabled(newMsisdn, oldMsisdn, languageCode, serviceId)
        } catch (e: CoreServiceException) {
            //to rollback transaction
            throw RemoteException("Error serviceDisabled", e)
        } finally {
            statisticService.increaseCounter(SC_CALLS)
        }
    }

    override fun serviceSuspended(newMsisdn: String, languageCode: String?) {
        coreService.serviceSuspended(newMsisdn, languageCode)
        statisticService.increaseCounter(SC_CALLS)
    }

    override fun serviceResumed(newMsisdn: String, languageCode: String?) {
        coreService.serviceResumed(newMsisdn, languageCode)
        statisticService.increaseCounter(SC_CALLS)
    }

    override fun selectiveNotificationEnabled(newMsisdn: String, languageCode: String?) {
        coreService.selectiveNotificationEnabled(newMsisdn, languageCode)
        statisticService.increaseCounter(SC_CALLS)
    }

}
