package ru.osp.cnn.core.service.util

import org.slf4j.LoggerFactory
import ru.osp.cnn.core.model.CnnDataAccessConstants.Companion.BLOCKED
import ru.osp.cnn.core.model.CnnDataAccessConstants.Companion.BLOCKED_ALL
import ru.osp.cnn.core.model.CnnDataAccessConstants.Companion.BLOCKED_INCOMING
import ru.osp.cnn.core.model.ServiceDO
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.WhiteNumberDO
import ru.osp.commons.cap.neo.AddressRepresentationRestrictedIndicator
import ru.osp.commons.cap.neo.CallingPartyNumber

object CoreServiceBoUtils {
    private val log = LoggerFactory.getLogger(CoreServiceBoUtils::class.java)
    private const val FORMAT_STRING_SMS_PARAM_NAME = "\\$\\{%s\\}"

    fun isServiceActive(service: ServiceDO): Boolean {
        log.debug("isServiceActive ServiceDO = {}", service)
        val serviceActive = (service.status === ServiceStatus.NOTIFICATION_FOR_ALL
                || service.status === ServiceStatus.SELECTIVE_NOTIFICATION)
        log.debug("isServiceActive return = {}", serviceActive)
        return serviceActive
    }

    fun isSubscriberBlocked(service: ServiceDO): Boolean {
        log.debug("isSubscriberBlocked ServiceDO = {}", service)
        val subscriberBlocked = (BLOCKED == service.subscriberBlockType
                || BLOCKED_INCOMING == service.subscriberBlockType
                || BLOCKED_ALL == service.subscriberBlockType)
        log.debug("isSubscriberBlocked return = {}", subscriberBlocked)
        return subscriberBlocked
    }

    /**
     * Is anonymous.
     *
     * @param msisdn msisdn
     * @return match status
     */
    fun isAnonymous(msisdn: String): Boolean {
        log.debug("isAnonymous msisdn = {}", msisdn)
        val anonymous = !msisdn.matches(Regex("\\d+"))
        log.debug("isAnonymous return = {}", anonymous)
        return anonymous
    }

    /**
     * Is address presentation restricted.
     *
     * @param callingPartyNumber callingPartyNumber
     * @return match status
     */
    fun isClirEnabled(callingPartyNumber: CallingPartyNumber): Boolean {
        log.debug("isClirEnabled callingPartyNumber = {}", callingPartyNumber)
        val clirEnabled = callingPartyNumber.addressRepresentationRestrictedIndicator === AddressRepresentationRestrictedIndicator.RESTRICTED
        log.debug("isClirEnabled return = {}", clirEnabled)
        return clirEnabled
    }

    /**
     * Remove leading 7.
     *
     * @param msisdn msisdn
     * @return msisdn without leading 7
     */
    fun removeLeading7(msisdn: String): String {
        log.debug("removeLeading7 msisdn = {}", msisdn)
        var changedMsisdn = msisdn
        if (msisdn.startsWith("7")) {
            changedMsisdn = changedMsisdn.substring(1)
        }
        log.debug("removeLeading7 return = {}", changedMsisdn)
        return changedMsisdn
    }

    /**
     * Check, that originating number in white number list.
     *
     * @param whiteNumbers white numbers list
     * @param callerNumber originating number
     * @return check status
     */
    fun isInWhiteList(whiteNumbers: Collection<WhiteNumberDO>?, callerNumber: String): Boolean {
        log.debug("isInWhiteList whiteNumbers = {}, callernumber={}", whiteNumbers, callerNumber)
        if (whiteNumbers == null) {
            log.debug("isInWhiteList return = false, whiteNumbers == null")
            return false
        }
        for (whiteNumber in whiteNumbers) {
            if (whiteNumber.whiteNumber.equals(callerNumber)) {
                log.debug("isInWhiteList return = true, callerNumber inside whiteNumbers")
                return true
            }
        }
        log.debug("isInWhiteList return = false, callerNumber not in whiteNumbers")
        return false
    }

    /**
     * formatting sms template
     *
     * @param msisdn
     * @param args
     * @return
     */
    fun formatMessageTemplate(msisdn: String, args: Map<String, String>?): String {
        log.debug("formatMessageTemplate message = {}, args = {}", msisdn, args)
        if (args == null || args.isEmpty()) {
            log.debug("formatMessageTemplate return = {}, args == null or args.isEmpty() ", msisdn)
            return msisdn
        }
        var changedMsisdn = msisdn
        for (paramName in args.keys) {
            val formParamName: String = java.lang.String.format(FORMAT_STRING_SMS_PARAM_NAME, paramName)
            changedMsisdn = changedMsisdn.replace(formParamName.toRegex(), args[paramName]!!)
        }
        log.debug("formatMessageTemplate return = {}", changedMsisdn)
        return changedMsisdn
    }

}
