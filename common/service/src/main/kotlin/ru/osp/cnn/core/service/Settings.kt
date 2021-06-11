package ru.osp.cnn.core.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

interface Settings {
    var mobileNumberPattern: String

    var serviceUnavailableTechnicalResponse: String

    var serviceDisablingInProgressResponse: String

    var serviceUnavailableUivrPrefix: String

    var serviceDisabledUivrPrefix: String

    var numberChangedMobileUivrPrefix: String

    var numberChangedFixedUivrPrefix: String

    var selectiveNotificationUivrPrefix: String

    var clirUivrPreffix: String

    var subscriberBlockedReleaseCause: Int

    var cnnSmsNumber: String

    var unknownNoaPrefix: String

    var wsbiSocNames: List<String>

    var wsbiCallReason: String

    var infoServiceEnabledResponse: String

    var infoServiceNotFoundResponse: String

    var smsShortNumbers: Set<String>

    var callProcessTimeThresholdMillis: Int

    var defaultLanguageCode: String

    var smsResponseTimeout: Long

    var wsbiAcceptableErrorCodes: Set<String>

    var esmeGroupName: String

    var smppGatewayUrl: String

    var moduleName: String

    var nodeName: String

}

@Component("settings")
class SettingsImpl : Settings {
    @Value("\${mobile.number.pattern}")
    override lateinit var mobileNumberPattern: String

    @Value("\${service.unavailable.technical.response}")
    override lateinit var serviceUnavailableTechnicalResponse: String

    @Value("\${service.disabling.in.process.response}")
    override lateinit var serviceDisablingInProgressResponse: String

    @Value("\${service.unavailable.uivr.prefix}")
    override lateinit var serviceUnavailableUivrPrefix: String

    @Value("\${service.disabled.uivr.prefix}")
    override lateinit var serviceDisabledUivrPrefix: String

    @Value("\${number.changed.mobile.uivr.prefix}")
    override lateinit var numberChangedMobileUivrPrefix: String

    @Value("\${number.changed.fixed.uivr.prefix}")
    override lateinit var numberChangedFixedUivrPrefix: String

    @Value("\${selective.notification.uivr.prefix}")
    override lateinit var selectiveNotificationUivrPrefix: String

    @Value("\${clir.uivr.prefix}")
    override lateinit var clirUivrPreffix: String

    @Value("\${subscriber.blocked.release.cause}")
    override var subscriberBlockedReleaseCause = 0

    @Value("\${cnn.sms.number}")
    override lateinit var cnnSmsNumber: String

    @Value("\${cnn.unknown.noa.prefix}")
    override lateinit var unknownNoaPrefix: String

    @Value("#{'\${wsbi.soc.names}'.split(',')}")
    override lateinit var wsbiSocNames: List<String>

    @Value("\${wsbi.call.reason}")
    override lateinit var wsbiCallReason: String

    @Value("\${info.service.enabled.response}")
    override lateinit var infoServiceEnabledResponse: String

    @Value("\${info.service.disabled.response}")
    override lateinit var infoServiceNotFoundResponse: String

    @Value("#{'\${wsbi.soc.names}'.split(',')}")
    override lateinit var smsShortNumbers: Set<String>

    @Value("\${call.process.time.threshold.millis}")
    override var callProcessTimeThresholdMillis: Int = 0

    @Value("\${language.code.default}")
    override lateinit var defaultLanguageCode: String

    @Value("\${language.code.default}")
    override var smsResponseTimeout: Long = 0

    @Value("#{'\${wsbi.acceptable.error.codes}'.split(',')}")
    override lateinit var wsbiAcceptableErrorCodes: Set<String>

    @Value("\${esme.group.name}")
    override lateinit var esmeGroupName: String

    @Value("\${smpp.gateway.url}")
    override lateinit var smppGatewayUrl: String

    @Value("\${monitoring.module.name}")
    override lateinit var moduleName: String

    @Value("\${monitoring.node.name}")
    override lateinit var nodeName: String

}
