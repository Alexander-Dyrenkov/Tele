package ru.osp.cnn.core.model

/**
 * Общий интерфейс для SMS
 */
sealed interface Sms {
    /**
     * Адрес, с которого будет отправлена SMS
     */
    val originatingAddress: String

    /**
     * Адрес, на который будет отправлена SMS
     */
    val destinationAddress: String
}

/**
 * SMS с текстом
 *
 * @param text текст, который будет отправлен
 */
data class SmsWithText(
    override val originatingAddress: String,
    override val destinationAddress: String,
    val text: String,
) : Sms

/**
 * SMS с шаблоном
 *
 * @param langCode код языка, для которого нужно найти подходящее сообщение
 * @param smsTemplateSettingName название шаблона, в котором лежит текст SMS
 * @param params дополнительные параметры, которые будут заменены в шаблоне
 */
data class SmsWithTemplate(
    override val originatingAddress: String,
    override val destinationAddress: String,
    val langCode: String,
    val smsTemplateSettingName: SmsTemplateSettingName,
    val params: Map<String, String>,
) : Sms
