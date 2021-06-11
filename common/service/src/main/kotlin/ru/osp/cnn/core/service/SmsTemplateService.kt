package ru.osp.cnn.core.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import ru.osp.cnn.core.model.SmsTemplateSettingName
import ru.osp.cnn.core.service.util.CoreServiceBoUtils
import java.lang.IllegalArgumentException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


interface SmsTemplateService {

    /**
     * Ищет подходящее сообщение для данного шаблона и языка и заменяет в нем при необходимости доп. параметры
     *
     * @param langCode код языка, для которого будет искаться сообщение
     * @param smsTemplateSettingName название шаблона, в котором лежит текст
     * @param params дополнительные параметры, которые будут заменены в шаблоне
     *
     * @return итоговое сообщение либо null, если подходящее сообщение не было найдено
     */
    fun getMessageByTemplateSettingName(langCode: String, smsTemplateSettingName: SmsTemplateSettingName, params: Map<String, String>?): String?
}

typealias SmsSettings = Map<SmsTemplateSettingName, String>

@Component("smsTemplateService")
class SmsTemplateServiceImpl(
    @Qualifier("settings")
    private val settings: Settings,
    @Value("\${sms.templates.directory}")
    private val smsTemplatesDirectory: String,
    applicationContext: ApplicationContext
): SmsTemplateService {
    companion object {
        private val logger = LoggerFactory.getLogger(SmsTemplateServiceImpl::class.java)

        private const val SMS_TEMPLATE_PATTERN = "\\d{3}"
    }

    private val smsSettingsMap = hashMapOf<String, SmsSettings>()

    init {
        val smsTemplates: Array<Resource> = applicationContext.getResources("file:$smsTemplatesDirectory/sms_templates_*.properties");
        val pattern: Pattern = Pattern.compile(SMS_TEMPLATE_PATTERN)
        val defaultLanguageCode = settings.defaultLanguageCode
        var defaultBundleExists = false
        smsTemplates.forEach { smsTemplateResource ->
            val fileName = smsTemplateResource.file.name
            val resourceBundleName = getBundlePropertiesName(fileName)
            logger.debug("resourceBundleName: {}", resourceBundleName)
            if (resourceBundleName == null) {
                logger.error("file with name {} is not property file", fileName)
            } else {
                val resourceBundle = PropertyResourceBundle(smsTemplateResource.inputStream)
                val languageCode = findLanguageCode(resourceBundleName, pattern)
                if (languageCode == null) {
                    logger.error("file with name {} does not matches sms template pattern", resourceBundleName)
                } else {
                    if (languageCode == defaultLanguageCode) {
                        defaultBundleExists = true
                    }
                    val smsSettings = hashMapOf<SmsTemplateSettingName, String>()
                    SmsTemplateSettingName.values().forEach { smsTemplateSettingName ->
                        val settingName = smsTemplateSettingName.settingName
                        try {
                            val settingValue = resourceBundle.getString(settingName)
                            smsSettings[smsTemplateSettingName] = settingValue
                        } catch (e: MissingResourceException) {
                            throw IllegalArgumentException("setting with name = '$settingName' is missing in bundle with name '$fileName'", e)
                        }
                    }
                    smsSettingsMap[languageCode] = smsSettings
                }
            }
        }
        if (smsSettingsMap.isEmpty()) {
            throw IllegalArgumentException("no settings file found!")
        }
        if (!defaultBundleExists) {
            throw IllegalStateException("Default sms template for language code = $defaultLanguageCode is missing!")
        }
    }

    override fun getMessageByTemplateSettingName(langCode: String, smsTemplateSettingName: SmsTemplateSettingName, params: Map<String, String>?): String? {
        logger.debug("getMessageByTemplateSettingName langCode={} smsTemplateSettingName={} params={}", langCode, smsTemplateSettingName, params)
        val settingValue = smsSettingsMap[langCode]?.get(smsTemplateSettingName)
        val result = settingValue?.let {
            CoreServiceBoUtils.formatMessageTemplate(it, params)
        }
        logger.debug("getMessageByTemplateSettingName result={}", result)
        return result
    }

    private fun getBundlePropertiesName(fileName: String): String? {
        return fileName.split(".properties").firstOrNull()
    }

    private fun findLanguageCode(fileName: String, langCodePattern: Pattern): String? {
        var langCode: String? = null
        val matcher: Matcher = langCodePattern.matcher(fileName)
        if (matcher.find()) {
            langCode = matcher.group()
        }
        return langCode
    }
}
