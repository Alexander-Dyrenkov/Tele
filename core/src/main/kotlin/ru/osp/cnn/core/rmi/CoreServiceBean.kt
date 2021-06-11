package ru.osp.cnn.core.rmi

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.osp.cnn.core.CoreService
import ru.osp.cnn.core.model.ServiceInfo
import ru.osp.cnn.core.service.Settings
import java.rmi.RemoteException

@Component("coreServiceBean")
class CoreServiceBean(@Qualifier("coreService") private val coreService: CoreService,
                      @Qualifier("settings") private val settings: Settings) : CoreServiceBusiness {
    companion object {
        private val log = LoggerFactory.getLogger(CoreServiceBean::class.java)
    }

    @Throws(RemoteException::class)
    override fun getServiceInfo(msisdn: String): ServiceInfo? {
        log.debug("getServiceInfo {}", msisdn)
        var serviceInfo: ServiceInfo?
        try {
            serviceInfo = coreService.getServiceInfo(msisdn)
        } catch (e: CoreServiceException) {
            log.error("getServiceInfo error", e)
            serviceInfo = ServiceInfo()
            serviceInfo.status = settings.serviceUnavailableTechnicalResponse
        }
        return serviceInfo
    }

}
