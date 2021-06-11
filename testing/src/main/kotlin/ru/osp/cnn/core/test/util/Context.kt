package ru.osp.cnn.core.test.util

object Context {
    lateinit var msisdn: String
    lateinit var msisdnA: String
    lateinit var msisdnB: String
    lateinit var oldMsisdn: String
    lateinit var moMqCorrelationId: String
    lateinit var moSessionId: String
    var mqCorrelationId: String? = null
    lateinit var mscAddress: String
    var CNN_DEFAULT_SETTINGS = hashMapOf(
        "AUTO_OFF_CNN_SERVICE" to hashMapOf("ID" to 1, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d{1,9}", "VALUE" to 6),
        "SMS_RESPONSE" to hashMapOf("ID" to 2, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d{1,9}", "VALUE" to 3600),
        "WHITE_NUMBER_LIST_LENGTH" to hashMapOf("ID" to 4, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d{1,9}", "VALUE" to 1000),
        "RESERVE_TECHNICAL_NUMBER_INTERVAL" to hashMapOf("ID" to 5, "TYPE" to 0 , "REGULAR_EXPRESSION" to "\\d+[smhd]", "VALUE" to "100d"),
        "TRANSPORT_ALARM_EXCEPTIONS" to hashMapOf("ID" to 6, "TYPE" to 1 , "REGULAR_EXPRESSION" to null, "VALUE" to "java.net.ConnectException"),
        "DB_ALARM_CODES" to hashMapOf("ID" to 7, "TYPE" to 1 , "REGULAR_EXPRESSION" to null, "VALUE" to "1438"),
        "DISABLE_SERVICE_ITEM_BACKGROUND_COLOR" to hashMapOf("ID" to 8, "TYPE" to 0 , "REGULAR_EXPRESSION" to "#[0-9A-Z]{6}", "VALUE" to "#C0C0C0")
    )
    lateinit var CNN_SETTINGS: HashMap<String, Any?>;
}
