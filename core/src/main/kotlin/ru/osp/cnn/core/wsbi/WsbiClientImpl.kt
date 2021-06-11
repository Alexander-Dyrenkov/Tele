package ru.osp.cnn.core.wsbi

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.lanit.beeline.cnn.officebackend.ws.onlineoperations.client.OnlineBillingService
import ru.lanit.beeline.cnn.officebackend.ws.onlineoperations.client.ServiceException_Exception
import ru.osp.cnn.core.service.*
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_WSBI_CALL
import ru.osp.cnn.core.service.StatisticService.Companion.WSBI_CALLS
import ru.osp.cnn.core.wsbi.exception.WsbiFatalException
import ru.osp.cnn.core.wsbi.exception.WsbiInteractionException
import java.rmi.RemoteException

@Component("wsbiClient")
class WsbiClientImpl(@Qualifier("onlineBillingService") private val onlineBillingService: OnlineBillingService,
                     @Qualifier("settings") private val settings: Settings,
                     @Qualifier("statisticManager") private val statisticService: StatisticService) : WsbiClient {
    companion object {
        private val logger = LoggerFactory.getLogger(WsbiClientImpl::class.java)
    }

    @Throws(WsbiException::class)
    override fun disableService(newMsisdn: String) {
        val wsbiSocNames = settings.wsbiSocNames
        logger.debug("Trying to disable service for msisdn {} using SOC names {}", newMsisdn, wsbiSocNames)
        wsbiSocNames.forEach { socName ->
            try {
                disableSoc(socName, newMsisdn)
                processSuccessMonitoring()
                return
            } catch (e: WsbiInteractionException) {
                val errorCode = e.errorCode
                if (!settings.wsbiAcceptableErrorCodes.contains(errorCode)) {
                    logger.debug("Failed to disable service for msisdn = {}", newMsisdn, e)
                    processFailureMonitoring(e)
                    throw e
                }
            } catch (e: Exception) {
                logger.error("Unexpected error while disable service for msisdn = {}", newMsisdn, e)
                processFailureMonitoring(e)
                throw e
            }
        }
        processSuccessMonitoring()
        logger.warn("Can not remove neither SOC with names {} for msisdn = {}, but service will be disabled", wsbiSocNames, newMsisdn)
    }

    private fun disableSoc(serviceName: String, newMsisdn: String) {
        logger.debug("disableSoc: msisdn = {}, serviceName = {}", newMsisdn, serviceName)
        try {
            logger.debug("disableSoc: call removeSoc")
            onlineBillingService.removeSOC(null, removeRegionPrefix(newMsisdn), serviceName, settings.wsbiCallReason)
            logger.debug("disableSoc: after call removeSoc")
        } catch (e: RemoteException) {
            logger.debug("Can not remove SOC = {} for msisdn = {}", serviceName, newMsisdn, e)
            throw WsbiFatalException("Can not remove SOC = $serviceName for msisdn = $newMsisdn. ${e.message}", e)
        } catch (e: ServiceException_Exception) {
            logger.debug("Can not remove SOC = {} for msisdn = {}. WSBI Error Code = {}, description = {}",
                serviceName, newMsisdn, e.faultInfo.errorCode, e.faultInfo.description, e)
            throw WsbiInteractionException("Can not remove SOC = $serviceName for msisdn = $newMsisdn", e)
        }
    }

    private fun removeRegionPrefix(msisdn: String?): String? = msisdn?.substring(1)

    fun processSuccessMonitoring() {
        statisticService.changeStatus(StatisticService.GROUP_WSBI, EVENT_WSBI_CALL, StatisticStatus.SUCCESSFULL)
        statisticService.increaseCounter(WSBI_CALLS)
    }

    fun processFailureMonitoring(e: Exception) {
        statisticService.changeStatus(StatisticService.GROUP_WSBI, EVENT_WSBI_CALL, StatisticStatus.FAILED, e.message)
    }

}
