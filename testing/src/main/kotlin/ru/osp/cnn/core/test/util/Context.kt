package ru.osp.cnn.core.test.util

import ru.osp.cnn.core.model.CnnDataAccessConstants.Companion
import ru.osp.cnn.core.model.ServiceStatus

object Context {
    lateinit var msisdn: String
    lateinit var msisdnA: String
    lateinit var msisdnB: String
    lateinit var oldMsisdn: String
    lateinit var moMqCorrelationId: String
    lateinit var moSessionId: String
    var mqCorrelationId: String? = null
    lateinit var mscAddress: String
    val SERVICE_STATUS = mapOf(
        ServiceStatus.DISABLED to "0" ,                // Услуга отключена
        ServiceStatus.SUSPENDED to "1",               // Услуга остановлена
        ServiceStatus.NOTIFICATION_FOR_ALL to "2",    // Включено оповещение для всех
        ServiceStatus.SELECTIVE_NOTIFICATION to "3"   // Включена выборочная нотификация
    )
    val LANGUAGE_CODE = mapOf(
        "ENG" to "045",
        "BEL" to "090",
        "RUS" to "570",
        "UKR" to "720"
    )
    var CNN_DEFAULT_SETTINGS = hashMapOf(
        "AUTO_OFF_CNN_SERVICE" to hashMapOf("ID" to 1, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d{1,9}", "VALUE" to 6),
        "SMS_RESPONSE" to hashMapOf("ID" to 2, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d{1,9}", "VALUE" to 3600),
        "WHITE_NUMBER_LIST_LENGTH" to hashMapOf("ID" to 4, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d{1,9}", "VALUE" to 1000),
        "RESERVE_TECHNICAL_NUMBER_INTERVAL" to hashMapOf("ID" to 5, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d+[smhd]", "VALUE" to "100d"),
        "TRANSPORT_ALARM_EXCEPTIONS" to hashMapOf("ID" to 6, "TYPE" to 1 , "REGULAR_EXPRESSION" to null, "VALUE" to "java.net.ConnectException"),
        "DB_ALARM_CODES" to hashMapOf("ID" to 7, "TYPE" to 1 , "REGULAR_EXPRESSION" to null, "VALUE" to "1438"),
        "DISABLE_SERVICE_ITEM_BACKGROUND_COLOR" to hashMapOf("ID" to 8, "TYPE" to 0 , "REGULAR_EXPRESSION" to "#[0-9A-Z]{6}", "VALUE" to "#C0C0C0")
    )
    lateinit var CNN_SETTINGS: HashMap<String, HashMap<String, Any?>>;
    var ACCOUNT_BLOCK_TYPE = hashMapOf(
        "BLOCK_TYPE_PARAM_VALUE_NONE" to "NONE",
        "BLOCK_TYPE_PARAM_VALUE_INCOMMING" to "INCOMMING",
        "BLOCK_TYPE_PARAM_VALUE_OUTGOING" to "OUTGOING",
        "BLOCK_TYPE_PARAM_VALUE_ALL" to "ALL"
    )
// Коды статуса услуги, используемые при вызове веб сервисов
    var SERVICE_STATUS_WS = hashMapOf(
        0 to "01",                      // Услуга отключена
        1 to "0",                       // Услуга остановлена
        2 to "1",                       // Включено оповещение для всех
        3 to "2"                        // Включена выборочная нотификация
    )

// Типы блокировок абонента
    var SERVICE_SUBSCRIBER_BLOCK_TYPE = hashMapOf(
        Companion.NOT_BLOCKED to "0",            // Не заблокирован
        Companion.BLOCKED to "1",                // Заблокирован на исходящие
        Companion.BLOCKED_INCOMING to "2",       // Заблокирован на входящие
        Companion.BLOCKED_ALL to "3"             // Заблокирован на входящие и исходящие
    )

// Состояния технического номера
    var TECH_NUMBER_STATE = hashMapOf(
        "VACANT" to "0",
        "ASSIGNED" to "1",
        "RELEASED" to "2"
    )

// Типы блокировок абонента
    var SUBSCRIBER_BLOCK_TYPE = hashMapOf(
        Companion.NOT_BLOCKED to "0",        // Не заблокирован
        Companion.BLOCKED to "1",            // Заблокирован на исходящие
        Companion.BLOCKED_INCOMING to "2",   // Заблокирован на входящие
        Companion.BLOCKED_ALL to "3"         // Заблокирован на входящие и исходящие
    )

// Булевские переменные для таблиц
    var BOOLEAN = hashMapOf(
        "TRUE" to 'Y',
        "FALSE" to 'N'
    )

// Шаблоны SMS сообщений
    var SMS_TEMPLATE = hashMapOf(
        "CNN_ENABLE_SERVICE_ERR_FINAL" to "CNN_ENABLE_SERVICE_ERR_FINAL",
        "CNN_DISABLE_SERVICE_ERR_FINAL" to "CNN_DISABLE_SERVICE_ERR_FINAL",
        "CNN_REDIRECTION_INSTRUCTION" to "CNN_REDIRECTION_INSTRUCTION",
        "CNN_REDIRECTION_INSTRUCTION_NE" to "CNN_REDIRECTION_INSTRUCTION_NE",
        "CNN_NEW_NUMBER_MESSAGE" to "CNN_NEW_NUMBER_MESSAGE",
        "CNN_NEW_NUMBER_REPORTED_ANONYM" to "CNN_NEW_NUMBER_REPORTED_ANONYM",
        "CNN_NEW_NUMBER_REPORTED" to "CNN_NEW_NUMBER_REPORTED",
        "CNN_ENABLE_SERVICE_ERROR" to "CNN_ENABLE_SERVICE_ERROR",
        "CNN_DISABLE_SERVICE_ERROR" to "CNN_DISABLE_SERVICE_ERROR",
        "CNN_SUCCESS_DISABLE_SERVICE" to "CNN_SUCCESS_DISABLE_SERVICE",
        "CNN_REDIR_TURNOFF_INSTRUCTION" to "CNN_REDIR_TURNOFF_INSTRUCTION",
        "CNN_INFO_SERVICE_DISABLED" to "CNN_INFO_SERVICE_DISABLED",
        "CNN_INFO_SERVICE_ENABLED" to "CNN_INFO_SERVICE_ENABLED",
        "CNN_INFO_SERVICE_NOT_FOUND" to "CNN_INFO_SERVICE_NOT_FOUND",
        "CNN_REQUEST_FOR_APPROVAL" to "CNN_REQUEST_FOR_APPROVAL",
        "CNN_FIRST_NOTIFICATION" to "CNN_FIRST_NOTIFICATION",
        "CNN_NOTIFICATION_FOR_ALL_IS_ENABLED" to "CNN_NOTIFICATION_FOR_ALL_IS_ENABLED",
        "CNN_NOTIFICATION_FOR_ALL_IS_SUSPENDED" to "CNN_NOTIFICATION_FOR_ALL_IS_SUSPENDED",
        "CNN_SERVICE_RESUMED" to "CNN_SERVICE_RESUMED",
        "CNN_SERVICE_SUSPENDED" to "CNN_SERVICE_SUSPENDED",
        "CNN_SELECTIVE_NOTIF_ENABLED" to "CNN_SELECTIVE_NOTIF_ENABLED"
    )

    var USSD_RESP = hashMapOf(
        "INFO_SERVICE_ENABLED_RESPONSE" to "Услуга уже подключена. Ждите SMS-справку по дополнительным функциям.",
        "INFO_SERVICE_DISABLED_RESPONSE" to "Услуга требует подключения. Ждите SMS-инструкцию по подключению."
    )

    var CALL_TYPE = hashMapOf(
        "OUTGOING" to 0,
        "INCOMING" to 1
    )

    var PARLAY_EVENTS = hashMapOf(
        "P_GCC_ROUTERES_ANSWER" to 3,
        "P_GCC_CALL_EVENT_NOTIFY" to 13,
        "P_GCC_ROUTEREQ" to 14,
        "P_GCC_RELEASE" to 15,
        "P_GCC_DEASIGN_CALL" to 16,
        "P_GCC_SET_CALL_CHARGE_PLAN" to 23,
        "P_UI_SEND_INFO_REQ" to 52,
        "P_UI_SEND_INFO_RES" to 53
    )
    var MO_UI_SERVICE_CODE = "00"
    var UIVR_PREFIXES = hashMapOf(
        "NUMBER_CHANGED_MOBILE_UIVR_PREFIX" to "D0681506",
        "NUMBER_CHANGED_FIXED_UIVR_PREFIX" to "D0681506",
        "SERVICE_DISABLED_UIVR_PREFIX" to "D0681502",
        "SELECTIVE_NOTIFICATION_UIVR_PREFIX" to "D081602",
        "SERVICE_UNAVAILABLE_UIVR_PREFIX" to "D0681501",
        "CLIR_UIVR_PREFIX" to "D081603"
    )
    var ADDRESS_PRESENTATION = hashMapOf(
        "UNDEFINED" to 0,
        "ALLOWED" to 1,
        "RESTRICTED" to 2,
        "ADDRESS_NOT_AVAILABLE" to 3
    )
}
