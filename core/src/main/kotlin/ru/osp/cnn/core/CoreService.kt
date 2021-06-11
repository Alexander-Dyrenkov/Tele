package ru.osp.cnn.core

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionOperations
import ru.lanit.commons.dao.UtilDao
import ru.osp.cnn.core.dao.CoreDAO
import ru.osp.cnn.core.dao.DaoException
import ru.osp.cnn.core.dao.EnvironmentDAO
import ru.osp.cnn.core.dao.OfficeDAO
import ru.osp.cnn.core.model.*
import ru.osp.cnn.core.rmi.CoreServiceException
import ru.osp.cnn.core.service.*
import ru.osp.cnn.core.service.util.CoreServiceBoUtils
import ru.osp.cnn.core.service.util.CoreServiceBoUtils.isInWhiteList
import ru.osp.commons.cap.neo.*
import ru.osp.smppgateway.AcceptMethodParams
import ru.osp.smppgateway.CommandStatus
import ru.osp.smppgateway.DeclineMethodParams
import ru.osp.smppgateway.ParlaySmppMessage
import java.util.*


interface CoreService {
    fun serviceEnabled(newMsisdn: String, oldMsisdn: String, techNumber: String, languageCode: String?)

    fun serviceEnableFailed(newMsisdn: String, languageCode: String?)

    fun selectiveNotificationEnabled(newMsisdn: String, languageCode: String?)

    fun serviceResumed(newMsisdn: String, languageCode: String?)

    fun serviceSuspended(newMsisdn: String, languageCode: String?)

    @Throws(CoreServiceException::class)
    fun serviceDisabled(newMsisdn: String, oldMsisdn: String, languageCode: String?, serviceId: Long)

    @Throws(CoreServiceException::class)
    fun getServiceInfo(msisdn: String): ServiceInfo?

    @Throws(CoreServiceException::class)
    suspend fun processIncomingCall(callSessionId: Long, callEventNotify: CallEventNotify): MethodCall

    @Throws(CoreServiceException::class)
    fun processSelectiveNotificationResponse(response: SelectiveNotificationResponse?): ParlaySmppMessage

    @Throws(CoreServiceException::class)
    fun disableExpiredServices()

    @Throws(CoreServiceException::class)
    fun removeExpiredSmsControlRecords()
}

@Component("coreService")
@Transactional
class CoreServiceImpl(@Qualifier("coreDao") private val coreDao: CoreDAO,
                      @Qualifier("officeDao") private val officeDao: OfficeDAO,
                      @Qualifier("environmentDao") private val environmentDao: EnvironmentDAO,
                      @Qualifier("settings") private val settings: Settings,
                      @Qualifier("transactionOperations") private val transactionOperations: TransactionOperations,
                      @Qualifier("wsbiClient") private val wsbiClient: WsbiClient,
                      @Qualifier("utilDao") private val utilDao: UtilDao,
                      @Qualifier("smsService") private val smsService: SmsService,
                      @Qualifier("smsCoroutineScope") private val smsCoroutineScope: CoroutineScope) : CoreService {

    companion object {
        private val log = LoggerFactory.getLogger(CoreServiceImpl::class.java)

        private const val SMS_PARAM_NAME_NEW_NUM = "new_num"
        private const val SMS_PARAM_NAME_SUB_NUM = "sub_num"
        private const val SMS_PARAM_NAME_CALLER_NUM = "caller_num"

        private const val AUTO_OFF_CNN_SERVICE_DEFAULT = 6

        private const val MILLIS_IN_SEC = 1000

        private const val REASON_MISSING_ORIG_ADDR = "Missing orig address while selective notification enabled"
        private const val REASON_DIRECT_CALL = "The call is a direct one"
        private const val REASON_SUBSCRIBER_BLOCKED = "Subscriber blocked"
    }

    /**
     * Get service info.
     *
     * @param msisdn msisdn
     * @return ServiceInfo service info
     */
    @Throws(CoreServiceException::class)
    override fun getServiceInfo(msisdn: String): ServiceInfo? {
        log.debug("getServiceInfo() start \n\tmsisdn={}", msisdn)
        var response: String? = null
        var oldMsisdn: String? = null
        try {
            val service = coreDao.findServiceByNewMsisdnAndStatuses(msisdn, ServiceStatus.allButDisabled)
            when {
                service == null -> {
                    response = settings.infoServiceNotFoundResponse
                    sendSms(msisdn, settings.defaultLanguageCode, SmsTemplateSettingName.INFO_SERVICE_NOT_FOUND_SMS_TEMPLATE, emptyMap())
                }
                service.status === ServiceStatus.DISABLED -> {
                    response = settings.serviceDisablingInProgressResponse
                }
                else -> {
                    response = settings.infoServiceEnabledResponse
                    sendSms(msisdn, service.langCode, SmsTemplateSettingName.INFO_SERVICE_ENABLED_SMS_TEMPLATE, emptyMap())
                }
            }
            oldMsisdn = service?.oldMsisdn
        } catch (e: DaoException) {
            processException(e, "Error get serviceInfo for msisdn $msisdn")
        }
        log.debug("getServiceInfo() return: {}", response)
        val serviceInfo = ServiceInfo()
        serviceInfo.status = response
        serviceInfo.oldMsisdn = oldMsisdn
        return serviceInfo
    }

    /**
     * Enable service.
     *
     * @param newMsisdn  new msisdn
     * @param oldMsisdn  old msisdn
     * @param techNumber tech number
     * @throws CoreServiceException CoreService exception
     */
    override fun serviceEnabled(newMsisdn: String, oldMsisdn: String, techNumber: String, languageCode: String?) {
        log.debug("CoreServiceBoImpl.serviceEnabled() start \n\tnewMsisdn={}\n\toldMsisdn={}\n\ttechNumber{}", newMsisdn, oldMsisdn, techNumber)

        val params: MutableMap<String, String> = HashMap()
        params[SMS_PARAM_NAME_NEW_NUM] = techNumber

        sendSms(newMsisdn, languageCode, SmsTemplateSettingName.PROCEED_ENABLE_SERVICE_NEW_SMS_TEMPLATE, params)

        sendSms(oldMsisdn, languageCode, SmsTemplateSettingName.PROCEED_ENABLE_SERVICE_SMS_TEMPLATE, params)

        log.debug("enableService() return")
    }

    /**
     * Enable service error.
     *
     * @param newMsisdn new msisdn
     */
    override fun serviceEnableFailed(newMsisdn: String, languageCode: String?) {
        log.debug("serviceEnableFailed() start {} {}", newMsisdn, languageCode)

        sendSms(newMsisdn, languageCode, SmsTemplateSettingName.SERVICE_CREATION_FAILED_FINAL_SMS_TEMPLATE, null)

        log.debug("serviceEnableFailed() return")
    }

    /**
     * Enable selective notification.
     *
     * @param newMsisdn new Msisdn
     */
    override fun selectiveNotificationEnabled(newMsisdn: String, languageCode: String?) {
        log.debug("enableSelectiveNotification() start \n\tnewMsisdn={}", newMsisdn)

        sendSms(newMsisdn, languageCode, SmsTemplateSettingName.SELECTIVE_NOTIFICATION_ENABLED_SMS_TEMPLATE, null)

        log.debug("enableSelectiveNotification() return")
    }

    /**
     * Resume service.
     *
     * @param newMsisdn new msisdn
     */
    override fun serviceResumed(newMsisdn: String, languageCode: String?) {
        log.debug("serviceResumed() start \n\tnewMsisdn={}", newMsisdn)

        sendSms(newMsisdn, languageCode, SmsTemplateSettingName.SERVICE_RESUMED_SMS_TEMPLATE, null)

        log.debug("serviceResumed() return")
    }

    /**
     * Suspend service.
     *
     * @param newMsisdn newMsisdn
     */
    override fun serviceSuspended(newMsisdn: String, languageCode: String?) {
        log.debug("serviceSuspended() start \n\tnewMsisdn={}", newMsisdn)

        sendSms(newMsisdn, languageCode, SmsTemplateSettingName.SERVICE_SUSPENDED_SMS_TEMPLATE, null)

        log.debug("serviceSuspended() return")
    }

    /**
     * Remove service.
     *
     * @param newMsisdn new mssidn
     * @param serviceId
     */
    @Throws(CoreServiceException::class)
    override fun serviceDisabled(newMsisdn: String, oldMsisdn: String, languageCode: String?, serviceId: Long) {
        log.debug("serviceDisabled() start \n\tnewMsisdn={}\n\toldMsisdn={}", newMsisdn, oldMsisdn)

        try {
            officeDao.removeAllWhiteNumbersByServiceId(serviceId)
            coreDao.removeAllSmsControlRecordsByServiceId(serviceId)
        } catch (e: DaoException) {
            processException(e, "Error trying to remove data after disable service for msisdn $newMsisdn")
        }

        sendSms(newMsisdn, languageCode, SmsTemplateSettingName.SERVICE_DISABLED_SMS_TEMPLATE, null)

        sendSms(oldMsisdn, languageCode, SmsTemplateSettingName.SERVICE_DISABLED_OLD_SMS_TEMPLATE, null)

        log.debug("removeService() return")
    }

    @Throws(CoreServiceException::class)
    override suspend fun processIncomingCall(callSessionId: Long, callEventNotify: CallEventNotify): MethodCall {
        log.debug("{} processIncomingCall() start callEventNotify={}", callSessionId, callEventNotify)
        try {
            val techNumber: String = callEventNotify.destinationAddress.address
            val service = coreDao.getServiceByTechNumber(techNumber) ?: return processIncomingCallOldVersion(callSessionId, callEventNotify)
            if (!CoreServiceBoUtils.isServiceActive(service)) {
                log.debug("Call {} is being routed to service disabled uivr because the service is disabled.", callEventNotify)
                return routeToUivr(callSessionId, callEventNotify, settings.serviceDisabledUivrPrefix, service.langCode)
            }
            if (CoreServiceBoUtils.isSubscriberBlocked(service)) {
                log.debug("Call {} is being released because of subscriber lock.", callEventNotify)
                return getReleaseResponse(callSessionId, callEventNotify, settings.subscriberBlockedReleaseCause)
            }
            if (service.status === ServiceStatus.NOTIFICATION_FOR_ALL) {
                return processIncomingCallNotificationForAll(callSessionId, callEventNotify, service)
            } else if (service.status === ServiceStatus.SELECTIVE_NOTIFICATION) {
                return processIncomingCallSelectiveNotification(callSessionId, callEventNotify, service)
            }
        } catch (e: Exception) {
            log.error("{} Error processing an incoming call [{}]", callSessionId, callEventNotify, e)
            return getDeassignResponse(callSessionId, callEventNotify, "Application error: ${e.message}")
        }
        log.debug("{} processIncomingCall() return", callSessionId)
        return getDeassignResponse(callSessionId, callEventNotify, "Application error")
    }

    @Throws(CoreServiceException::class)
    override fun processSelectiveNotificationResponse(response: SelectiveNotificationResponse?): ParlaySmppMessage {
        log.debug("processSelectiveNotificationResponse response='{}'", response)
        val result: ParlaySmppMessage =
            if (response == null) {
                log.error("processSelectiveNotificationResponse response is null")
                ParlaySmppMessage(DeclineMethodParams(CommandStatus.ESME_RSYSERR))
            } else {
                try {
                    val newNumber: String = response.originatingAddress
                    val service = coreDao.findServiceByNewMsisdnAndStatuses(newNumber, ServiceStatus.allButDisabled)

                    if (service == null) {
                        // we shoud do nothing if there is no service
                        log.warn("Service not found for $newNumber, sms is dropped.")
                        ParlaySmppMessage(AcceptMethodParams)
                    } else {
                        val smsRecord =
                            coreDao.findSmsRecordByServiceAndShortNumber(service, response.destinationAddress)
                        when (response.command) {
                            SelectiveNotificationResponse.Command.ENABLE_NOTIFICATION_FOR_ALL -> {
                                service.status = ServiceStatus.NOTIFICATION_FOR_ALL
                                officeDao.updateService(service)
                                sendSms(service.newMsisdn!!, service.langCode, SmsTemplateSettingName.NOTOFICATION_FOR_ALL_IS_ENABLED_SMS_TEMPLATE, null)
                                if (isMobile(smsRecord.callerNumber!!)) {
                                    sendNewNumberSms(service, smsRecord.callerNumber!!)
                                }
                                coreDao.deleteSmsControlRecord(smsRecord.recordId!!)
                            }
                            SelectiveNotificationResponse.Command.SUSPEND_SERVICE -> {
                                service.status = ServiceStatus.SUSPENDED
                                officeDao.updateService(service)
                                sendSms(service.newMsisdn!!, service.langCode, SmsTemplateSettingName.NOTOFICATION_FOR_ALL_IS_SUSPENDED_SMS_TEMPLATE, null)
                            }
                            SelectiveNotificationResponse.Command.APPROVE_NOTIFICATION -> {
                                if (log.isDebugEnabled) {
                                    log.debug("SMS Control Record found: $smsRecord")
                                }
                                updateWhiteList(service, smsRecord)

                                // Sending SMS to the caller
                                if (isMobile(smsRecord.callerNumber!!)) {
                                    sendNewNumberSms(service, smsRecord.callerNumber!!)
                                }
                                coreDao.deleteSmsControlRecord(smsRecord.recordId!!)
                            }
                        }
                        ParlaySmppMessage(AcceptMethodParams)
                    }
                } catch (e: DaoException) {
                    processException(e, "Failed to process sms from user.")
                    ParlaySmppMessage(DeclineMethodParams(CommandStatus.ESME_RSYSERR))
                }
            }
        log.debug("processSelectiveNotificationResponse result='{}'", result)
        return result
    }

    @Throws(DaoException::class)
    private fun updateWhiteList(service: ServiceDO, smsRecord: SmsControlRecordDO) {
        val whiteNumbers: Collection<WhiteNumberDO> = officeDao.getWhiteNumbers(service.serviceId!!)
        if (!isInWhiteList(whiteNumbers, smsRecord.callerNumber!!)) {
            //ensure that white numbers list doesn't exceed its maximum length
            if (whiteNumbers.size >= getSingleValueIntSettingFromDB("WHITE_NUMBER_LIST_LENGTH")) {
                val wNumbersAsArray = whiteNumbers.toTypedArray()
                officeDao.deleteWhiteNumber(wNumbersAsArray[0].whiteNumberId!!)
            }
            val whiteNumberDO = WhiteNumberDO()
            whiteNumberDO.serviceId = service.serviceId
            whiteNumberDO.whiteNumber = smsRecord.callerNumber
            whiteNumberDO.createTime = Date()
            officeDao.createWhiteNumber(whiteNumberDO)
        }
    }

    /**
     * Process service activation date check.
     */
    @Throws(CoreServiceException::class)
    @Async("periodicalTaskScheduler")
    @Scheduled(cron = "\${expired.service.disable.task.cron}")
    override fun disableExpiredServices() {
        log.debug("disableExpiredServices().")

        var autoDisablePeriod: Int = AUTO_OFF_CNN_SERVICE_DEFAULT
        try {
            autoDisablePeriod = getSingleValueIntSettingFromDB("AUTO_OFF_CNN_SERVICE")
        } catch (e: Exception) {
            log.warn("Failed to get AUTO_OFF_CNN_SERVICE value. Using default")
        }

        val calendar = GregorianCalendar()
        calendar.time = utilDao.currentDate
        calendar.add(Calendar.MONTH, -autoDisablePeriod)

        try {
            val services: Collection<ServiceDO> = coreDao.getServicesBeforeActivationDate(calendar.time)
            for (service in services) {
                if (ServiceStatus.allButDisabled.contains(service.status)) {
                    try {
                        transactionOperations.execute {
                            try {
                                disableService(service)
                                try {
                                    serviceDisabled(service.newMsisdn!!, service.oldMsisdn!!, service.langCode, service.serviceId!!)
                                } catch (e: CoreServiceException) {
                                    log.error("Error disable service {} with activation date before {}. {}", service, calendar.time, e.message)
                                }
                            } catch (e: Exception) {
                                log.error("failed to disable service for {}", service.newMsisdn)
                                throw FailedToDisableServiceException(e)
                            }
                        }
                    } catch (ex: FailedToDisableServiceException) {
                        log.debug("Error disable service {} with activation date before {}", service, calendar.time, ex)
                    } catch (ex: Exception) {
                        log.error("Error disable service {} with activation date before {}", service, calendar.time, ex)
                    }
                }
            }
        } catch (e: DaoException) {
            processException(e, "Error disable services with activation date before $calendar.time")
        }
    }

    /**
     * Remove expired sms control records.
     */
    @Throws(CoreServiceException::class)
    @Async("periodicalTaskScheduler")
    @Scheduled(cron = "\${expired.sms.remove.task.cron}")
    override fun removeExpiredSmsControlRecords() {
        var smsResponseTimeout = settings.smsResponseTimeout
        try {
            smsResponseTimeout = getSingleValueIntSettingFromDB("SMS_RESPONSE").toLong()
        } catch (e: Exception) {
            log.warn("Failed to get SMS_RESPONSE value. Using default")
        }
        val removeDate = Date(System.currentTimeMillis() - smsResponseTimeout * MILLIS_IN_SEC)
        try {
            coreDao.deleteExpiredSmsControlRecords(removeDate)
        } catch (e: DaoException) {
            processException(e, "Error remove SmsControlRecords with a sendTime less $removeDate")
        }
    }

    /**
     * Get integer setting value from DB.
     *
     * @param settingName setting name
     * @return setting value
     * @throws DaoException Dao exception
     */
    @Throws(DaoException::class)
    private fun getSingleValueIntSettingFromDB(settingName: String): Int {
        return environmentDao.getSettingByName(settingName).values.iterator().next().toInt()
    }

    /**
     * Disable service.
     *
     * @param service
     */
    @Throws(DaoException::class, WsbiException::class)
    private fun disableService(service: ServiceDO) {
        log.info("disabling service for {}", service.newMsisdn)
        log.debug("disableService() start \n\tnewNumber={}", service)
        wsbiClient.disableService(service.newMsisdn!!)
        service.status = ServiceStatus.DISABLED
        val currentDate: Date = utilDao.currentDate
        service.asyncOperationDate = currentDate
        val removalDate = Calendar.getInstance()
        removalDate.timeInMillis = currentDate.time
        service.removalDate = removalDate
        val removeAfterDate = Calendar.getInstance()
        removeAfterDate.timeInMillis = currentDate.time
        service.removeAfterDate = removeAfterDate
        officeDao.updateService(service)
        log.debug("disableService() return")
        log.info("service disabled for {}", service.newMsisdn)
    }

    /**
     * Process exception.
     *
     * @param e       exception
     * @param message message
     * @throws CoreServiceException CoreService exception
     */
    @Throws(CoreServiceException::class)
    private fun processException(e: Exception, message: String) {
        log.error(message, e)
        throw CoreServiceException(message + ": " + e.message, e)
    }

    /**
     * Send sms.
     *
     * @param destinationMsisdn  destination msisdn
     * @param smsTemplateSetting contains templateId or/and templateMessage
     * @param params             parameters
     */
    private fun sendSms(destinationMsisdn: String, languageCode: String?, smsTemplateSetting: SmsTemplateSettingName, params: Map<String, String>?) {
        sendSms(destinationMsisdn, settings.cnnSmsNumber, languageCode, smsTemplateSetting, params)
    }

    /**
     * Send sms.
     *
     * @param destinationMsisdn  destination msisdn
     * @param originatingMsisdn  originating msisdn
     * @param smsTemplateSetting contains templateId or/and templateMessage
     * @param params             parameters
     */
    protected fun sendSms(destinationMsisdn: String, originatingMsisdn: String, languageCode: String?, smsTemplateSetting: SmsTemplateSettingName, params: Map<String, String>?) {
        log.debug("sendSms destinationMsisdn = {}, originatingMsisdn = {}, smsTemplateSetting = {}, params = {}", destinationMsisdn, originatingMsisdn, smsTemplateSetting, params)
        smsCoroutineScope.launch {
            val sms = SmsWithTemplate(
                originatingAddress = originatingMsisdn,
                destinationAddress = destinationMsisdn,
                langCode = languageCode ?: settings.defaultLanguageCode,
                smsTemplateSettingName = smsTemplateSetting,
                params = params ?: emptyMap()
            )
            smsService.sendSms(sms).onSuccess {
                log.debug("sms = {} was successfully sent", sms)
            }.onFailure { e ->
                log.error("sms = {} was not successfully sent", sms, e)
            }
        }
        log.debug("sendSms return")
    }

    /**
     * Process notification for all for incoming call.
     *
     * @param callEventNotify    incoming call
     * @param service serviceDO
     */
    private fun processIncomingCallNotificationForAll(callSessionId: Long, callEventNotify: CallEventNotify, service: ServiceDO): MethodCall {
        log.debug("Call {} is being routed to notification all UIVR.", callEventNotify)
        var responseEntity: MethodCall? = null
        try {
            val newNumber = service.newMsisdn
            val languageCode = service.langCode
            val originatingNumber = callEventNotify.sourceAddress.address
            if (isMobile(originatingNumber)) {
                log.debug("Call {} is being routed to new number uivr.", callEventNotify)
                responseEntity = routeToUivr(callSessionId, callEventNotify, settings.numberChangedMobileUivrPrefix, newNumber, languageCode)

                // send the sms to the calling party
                sendNewNumberSms(service, originatingNumber)
            } else { // this can be fixed or not defined
                log.debug("Call {} is being routed to new number uivr.", callEventNotify)
                responseEntity = routeToUivr(callSessionId, callEventNotify, settings.numberChangedFixedUivrPrefix, newNumber, languageCode)
            }

            // send an sms to the called party
            val reportCallingNumber = (!CoreServiceBoUtils.isAnonymous(originatingNumber)
                    && !CoreServiceBoUtils.isClirEnabled(callEventNotify.sourceAddress))
            sendNewNumberReportedSms(service, originatingNumber, reportCallingNumber)
        } catch (e: Exception) {
            log.error("processIncomingCallNotificationForAll", e)
            responseEntity = responseEntity ?: getDeassignResponse(callSessionId, callEventNotify, "Application error: ${e.message}")
        }
        return responseEntity!!
    }

    /**
     * Is mobile.
     *
     * @param msisdn msisdn
     * @return match status
     */
    private fun isMobile(msisdn: String): Boolean {
        return msisdn.matches(Regex(settings.mobileNumberPattern))
    }

    /**
     * Route to UIVR.
     *
     * @param callEventNotify   call
     * @param prefix prefix
     * @return operation result
     */
    private fun routeToUivr(callSessionId: Long, callEventNotify: CallEventNotify, prefix: String, languageCode: String?): MethodCall {
        return routeToUivr(callSessionId, callEventNotify, prefix, null, languageCode)
    }

    /**
     * Route to UIVR.
     *
     * @param callEventNotify      call
     * @param prefix    prefix
     * @param newNumber new msisdn
     * @return operation result
     */
    private fun routeToUivr(callSessionId: Long, callEventNotify: CallEventNotify, prefix: String, newNumber: String?, subscriberLanguageCode: String?): MethodCall {
        val suffix = if (newNumber != null) CoreServiceBoUtils.removeLeading7(newNumber) else ""
        val languageCode = subscriberLanguageCode ?: settings.defaultLanguageCode
        val destination = CalledPartyNumber(settings.unknownNoaPrefix + prefix + languageCode + suffix, NumberingPlanIndicator.ISDN, NatureOfAddressIndicator.INTERNATIONAL, InternalNetworkNumberIndicator.ROUTING_ALLOWED)
        return getRouteResponse(callSessionId, callEventNotify, destination)
    }

    /**
     * Process incoming call for old version subscribers.
     *
     * @param callEventNotify call
     */
    @Throws(CoreServiceException::class)
    private fun processIncomingCallOldVersion(callSessionId: Long, callEventNotify: CallEventNotify): MethodCall {
        log.debug("CoreServiceBoImpl.processIncomingCallOldVersion() start")
        log.debug("   call={}", callEventNotify)
        val newNumber = callEventNotify.destinationAddress.address
        val originatingNumber = callEventNotify.sourceAddress.address
        var responseEntity : MethodCall? = null
        try {
            if (callEventNotify.redirectingNumber == null) {
                log.debug("Call {} is being deassigned because it is a direct call.", callEventNotify)
                return getDeassignResponse(callSessionId, callEventNotify, REASON_DIRECT_CALL)
            }
            val service = coreDao.findServiceByNewMsisdnAndStatuses(newNumber, ServiceStatus.allButDisabled)
            if (service == null) {
                log.debug("Call {} is being routed to service unavailable uivr because of an absense of service subscription.", callEventNotify)
                return routeToUivr(callSessionId, callEventNotify, settings.serviceUnavailableUivrPrefix, settings.defaultLanguageCode)
            }
            if (CoreServiceBoUtils.isSubscriberBlocked(service)) {
                log.debug("Call {} is being released because of subscriber lock.", callEventNotify)
                return getReleaseResponse(callSessionId, callEventNotify, settings.subscriberBlockedReleaseCause)
            }
            if (service.status === ServiceStatus.SELECTIVE_NOTIFICATION) {
                log.debug("Call {} is being routed to selective notification UIVR.", callEventNotify)

                // if we have no originating address at all the app should just deassign the call.
                if (originatingNumber.isBlank()) {
                    return getDeassignResponse(callSessionId, callEventNotify, REASON_MISSING_ORIG_ADDR)
                }

                // check for WHITE_LIST
                val whiteNumbers: Collection<WhiteNumberDO> = officeDao.getWhiteNumbers(service.serviceId!!)
                if (!isInWhiteList(whiteNumbers, originatingNumber)) {
                    val smsControlRecordDO: SmsControlRecordDO
                    if (AddressRepresentationRestrictedIndicator.RESTRICTED !== callEventNotify.sourceAddress.addressRepresentationRestrictedIndicator) {

                        // routing call to the special UIVR for selective notification
                        responseEntity = routeToUivr(callSessionId, callEventNotify, settings.selectiveNotificationUivrPrefix, service.langCode)
                        smsControlRecordDO = getOrCreateSmsControlRecordDO(service, originatingNumber)
                        sendRequestForNumberReportingAcceptanceSms(service, smsControlRecordDO.shortSmsNumber!!, originatingNumber)

                        // Store sms record
                        smsControlRecordDO.sendDate = utilDao.currentDate
                        coreDao.saveOrUpdateSmsControlRecord(smsControlRecordDO)
                        if (service.firstNotification == true) {
                            // send matrix SMS
                            sendSms(newNumber, smsControlRecordDO.shortSmsNumber!!, service.langCode, SmsTemplateSettingName.FIRST_NOTIFICATION_SMS_TEMPLATE, null)
                            service.firstNotification = false
                            officeDao.updateService(service)
                        }
                    } else {
                        // a calling party has CLIR enabled - forward the party to special UIVR.
                        responseEntity = routeToUivr(callSessionId, callEventNotify, settings.clirUivrPreffix, service.langCode)
                    }
                    return responseEntity
                } //else everything is the same as in case of notification_for_all
            } else if (service.status !== ServiceStatus.NOTIFICATION_FOR_ALL) {
                log.debug("Call {} is being routed to service disabled uivr because of disabled service.", callEventNotify)
                return routeToUivr(callSessionId, callEventNotify, settings.serviceDisabledUivrPrefix, service.langCode)
            }
            if (isMobile(originatingNumber)) {
                log.debug("Call {} is being routed to new number uivr.", callEventNotify)
                responseEntity = routeToUivr(callSessionId, callEventNotify, settings.numberChangedMobileUivrPrefix, newNumber, service.langCode)

                // send the sms to the calling party
                sendNewNumberSms(service, originatingNumber)
            } else { // this can be fixed or not defined
                log.debug("Call {} is being routed to new number uivr.", callEventNotify)
                responseEntity = routeToUivr(callSessionId, callEventNotify, settings.numberChangedFixedUivrPrefix, newNumber, service.langCode)
            }

            // send the sms to the called party
            val reportCallingNumber = !CoreServiceBoUtils.isAnonymous(originatingNumber) && !CoreServiceBoUtils.isClirEnabled(callEventNotify.sourceAddress)
            sendNewNumberReportedSms(service, originatingNumber, reportCallingNumber)
        } catch (e: Exception) {
            log.error("Error processing an incoming call [$callEventNotify]", e)
            responseEntity = responseEntity ?: getDeassignResponse(callSessionId, callEventNotify, "Application error: ${e.message}")
        }
        log.debug("processIncomingCallOldVersion() return")
        return responseEntity ?: getDeassignResponse(callSessionId, callEventNotify, "Application error")
    }

    /**
     * Process selective notification for incoming call.
     *
     * @param callEventNotify    incoming call
     * @param service service data object
     */
    private fun processIncomingCallSelectiveNotification(callSessionId: Long, callEventNotify: CallEventNotify, service: ServiceDO): MethodCall {
        log.debug("Call {} is being routed to selective notification UIVR.", callEventNotify)
        var responseEntity: MethodCall? = null
        try {
            val originatingNumber = callEventNotify.sourceAddress.address
            // check the WHITE_LIST
            val whiteNumbers: Collection<WhiteNumberDO> = officeDao.getWhiteNumbers(service.serviceId!!)
            if (isInWhiteList(whiteNumbers, originatingNumber)) {
                // if the originating number is already in White list, treat it as if notification for all were enabled
                responseEntity = processIncomingCallNotificationForAll(callSessionId, callEventNotify, service)
            } else {
                val smsControlRecordDO: SmsControlRecordDO
                if (!CoreServiceBoUtils.isClirEnabled(callEventNotify.sourceAddress)) {
                    // routing call to the special UIVR for selective notification
                    responseEntity = routeToUivr(callSessionId, callEventNotify, settings.selectiveNotificationUivrPrefix, service.langCode)
                    if (CoreServiceBoUtils.isAnonymous(originatingNumber)) {
                        return responseEntity
                    }
                    smsControlRecordDO = getOrCreateSmsControlRecordDO(service, originatingNumber)
                    sendRequestForNumberReportingAcceptanceSms(service, smsControlRecordDO.shortSmsNumber!!, originatingNumber)

                    // Store sms record
                    smsControlRecordDO.sendDate = utilDao.currentDate
                    coreDao.saveOrUpdateSmsControlRecord(smsControlRecordDO)
                    if (service.firstNotification == true) {
                        // send matrix SMS
                        sendSms(service.newMsisdn!!, smsControlRecordDO.shortSmsNumber!!, service.langCode, SmsTemplateSettingName.FIRST_NOTIFICATION_SMS_TEMPLATE, null)
                        service.firstNotification = false
                        officeDao.updateService(service)
                    }
                } else {
                    // the calling party has CLIR enabled - forward the party to special UIVR.
                    responseEntity = routeToUivr(callSessionId, callEventNotify, settings.clirUivrPreffix, service.langCode)
                }
            }
        } catch (e: Exception) {
            log.error("processIncomingCallSelectiveNotification", e)
            responseEntity = responseEntity ?: getDeassignResponse(callSessionId, callEventNotify, "Application error: ${e.message}")
        }
        return responseEntity!!
    }

    private fun getReleaseResponse(callSessionId: Long, callEventNotify: CallEventNotify, releaseCause: Int): MethodCall {
        log.debug("{} getReleaseResponse {} {} {}", callSessionId, callEventNotify, releaseCause, REASON_SUBSCRIBER_BLOCKED)
        return MethodCall(GWYMethod.ReleaseCall.name, AppCommonParameters(callSessionId, ""), ReleaseCall(CauseValue.fromCode(releaseCause), CauseLocation.USER))
    }

    private fun getDeassignResponse(callSessionId: Long, callEventNotify: CallEventNotify, reason: String): MethodCall {
        log.debug("{} getDeassignResponse {} {}", callSessionId, callEventNotify, reason)
        return MethodCall(GWYMethod.DeassignCall.name, AppCommonParameters(callSessionId, ""), null)
    }

    private fun getRouteResponse(callSessionId: Long, callEventNotify: CallEventNotify, destination: CalledPartyNumber): MethodCall {
        log.debug("{} getRouteResponse {} {} {}", callSessionId, callEventNotify, destination)
        return MethodCall(GWYMethod.RouteCall.name, AppCommonParameters(callSessionId, ""), RouteCall(callEventNotify.sourceAddress, destination, callEventNotify.redirectingNumber, callEventNotify.redirectionInformation, callEventNotify.originalCalledNumber, null, emptyList()))
    }

    /**
     * Send sms with new number.
     *
     * @param service           ServiceDO
     * @param originatingMsisdn originating msidn
     */
    private fun sendNewNumberSms(service: ServiceDO, originatingMsisdn: String) {
        val callingParams: MutableMap<String, String> = HashMap()
        callingParams[SMS_PARAM_NAME_NEW_NUM] = service.newMsisdn!!
        sendSms(originatingMsisdn, settings.cnnSmsNumber, service.langCode, SmsTemplateSettingName.NEW_NUMBER_SMS_TEMPLATE, callingParams)
    }

    /**
     * Send reported sms to a new number.
     *
     * @param service             ServiceDO
     * @param originatingMsisdn   originating msisdn
     * @param reportCallingNumber report to calling number
     */
    private fun sendNewNumberReportedSms(service: ServiceDO, originatingMsisdn: String, reportCallingNumber: Boolean) {
        val smsTemplateSettingName: SmsTemplateSettingName
        val calledParams: MutableMap<String, String>?
        if (reportCallingNumber) {
            calledParams = HashMap()
            calledParams[SMS_PARAM_NAME_SUB_NUM] = originatingMsisdn
            smsTemplateSettingName = SmsTemplateSettingName.NEW_NUMBER_REPORTED_SMS_TEMPLATE
        } else {
            calledParams = null
            smsTemplateSettingName = SmsTemplateSettingName.NEW_NUMBER_REPORTED_ANONYMOUS_SMS_TEMPLATE
        }
        sendSms(service.newMsisdn!!, settings.cnnSmsNumber, service.langCode, smsTemplateSettingName, calledParams)
    }

    /**
     * Get sms control record or create if it not exist.
     *
     * @param service      service
     * @param callerNumber callerNumber
     * @return sms control record
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    private fun getOrCreateSmsControlRecordDO(service: ServiceDO, callerNumber: String): SmsControlRecordDO {
        log.debug("getOrCreateSmsControlRecordDO service = {} , callerNumber = {}", service, callerNumber)
        val records: Collection<SmsControlRecordDO> = coreDao.findAllSmsRecordsByServiceAndCallerNumber(service, callerNumber)
        return if (!records.isEmpty()) {
            val receivedSms = records.toTypedArray()[0]
            log.debug("getOrCreateSmsControlRecordDO return(received) = {} ", receivedSms)
            receivedSms
        } else {
            val smsControlRecordDO = SmsControlRecordDO()
            smsControlRecordDO.callerNumber = callerNumber
            smsControlRecordDO.serviceId = service.serviceId
            smsControlRecordDO.shortSmsNumber = getNextShortNumber(service)
            log.debug("getOrCreateSmsControlRecordDO return(created) = {} ", smsControlRecordDO)
            smsControlRecordDO
        }
    }

    /**
     * Send request for number reproting acceptance sms.
     *
     * @param service      ServiceDO
     * @param shortNumber  short number
     * @param callerNumber caller number
     */
    private fun sendRequestForNumberReportingAcceptanceSms(service: ServiceDO, shortNumber: String, callerNumber: String) {
        val smsParams: MutableMap<String, String> = HashMap()
        smsParams[SMS_PARAM_NAME_CALLER_NUM] = callerNumber
        sendSms(service.newMsisdn!!, shortNumber, service.langCode, SmsTemplateSettingName.REQUEST_FOR_APPROVAL_SMS_TEMPLATE, smsParams)
    }

    /**
     * Get next short number.
     *
     * @param service service
     * @return short number
     * @throws DaoException DaoException
     */
    @Throws(DaoException::class)
    private fun getNextShortNumber(service: ServiceDO): String? {
        val records: Collection<SmsControlRecordDO> = coreDao.findAllSmsRecordsByService(service)
        if (records.isEmpty()) {
            return null
        }
        val existingShortNumbers = records.map { it.shortSmsNumber }
        val availableShortNumbers = settings.smsShortNumbers.filter { it !in existingShortNumbers  }
        if (availableShortNumbers.isNotEmpty()) {
            return availableShortNumbers.first()
        }
        val sortedNumbers: List<SmsControlRecordDO> = records.sortedBy { it.sendDate }
        val firstSmsControlRecordDO = sortedNumbers.first()
        coreDao.deleteSmsControlRecord(firstSmsControlRecordDO.recordId!!)
        return firstSmsControlRecordDO.shortSmsNumber
    }

}
