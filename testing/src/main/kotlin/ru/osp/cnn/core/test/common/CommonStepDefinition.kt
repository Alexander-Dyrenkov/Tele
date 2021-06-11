package ru.osp.cnn.core.test

import cucumber.api.java.ru.Дано
import cucumber.api.java.ru.И
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.test.util.Context
import ru.osp.cnn.core.test.util.SetUp


class CommonStepDefinition {
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
    private val setUp = SetUp()
    var cnnService = setUp.getDefaultCnnService()
    var status = SERVICE_STATUS[ServiceStatus.NOTIFICATION_FOR_ALL]
    var languageCode = LANGUAGE_CODE["ENG"]
    var currentDate = setUp.getCurrentDbDate()
    var currentDateInsert = getDbTimeForInsert(currentDate)

    @Дано("msisdn абонента А = {string}, oldMsisdn = {string}, techMsisdn = {string}")
    fun setCallParam(msisdnA: String, oldMsisdn: String, techMsisdn: String) {
        Context.msisdnA = msisdnA

    }
    @И("Включение ошибки теста при ошибке в ответе веб-сервиса")
    fun setFailOn() {
        setFailOnFaultSC(true)
    }

    @И("Очистка таблиц")
    fun tablesClean() {
        cleanAllTables()
    }

    @И("Устанавливаем дефолтные настройки приложения")
    fun defaultSettingsSetting() {
        setUp.setDefaultCnnSettings()
        setUp.updateCnnSettings()
    }

    @И("Добавление течнического номера и ответа заглушки для отправки СМС")
    fun techNumberAdding() {
        addCnnTechNumber(techMsisdn, TECH_NUMBER_STATE.VACANT, marketCode, currentDateInsert)
        addSendSMSResponse()
    }
    @И("Подключение сервиса")
    fun serviceOn() {
        enableService(Context.msisdn, status, Context.oldMsisdn, languageCode, marketCode)
    }
    @И("Установить и проверить актуальный технический номер")
    fun actualTechNumberChecking() {
        var actualCnnTechNumber = findCnnTechNumberByTechNumberMsisdn(techMsisdn)
        var expectedCnnTechNumber = hashMapOf (
            "techNumberMsisdn" to techMsisdn,
            "techNumberState" to TECH_NUMBER_STATE. ASSIGNED,
            "marketCode" to marketCode,
            "lastOperationDate" to currentDate
        )
        checkCnnTechNumber(actualCnnTechNumber, expectedCnnTechNumber)
    }

    @И("Проверка актуального сервиса")
    fun checkActualService() {
        var actualCnnService = findCnnServiceByTechNumberMsisdn(techMsisdn)
        var expectedCnnService = hashMapOf (
            "newMsisdn" to Context.msisdn,
            "oldMsisdn" to Context.oldMsisdn,
            "subscriberBlockType" to SERVICE_SUBSCRIBER_BLOCK_TYPE.NOT_BLOCKED,
            "activationDate" to currentDate,
            "status" to SERVICE_STATUS.NOTIFICATION_FOR_ALL,
            "isFirstNotification" to false,
            "asyncOperationDate" to null,
            "techNumberMsisdn" to techMsisdn,
            "langCode" to languageCode
        )

        checkCnnService(actualCnnService, expectedCnnService)
    }
    @И("Проверка сколько СМС было отправлено")
    fun totalSMSChecking() {
        checkTotalSmsSent(2)
        checkSmsSent(SMS_TEMPLATE.CNN_REDIRECTION_INSTRUCTION_NE, Context.msisdn, 1)
        checkSmsSent(SMS_TEMPLATE.CNN_REDIRECTION_INSTRUCTION, Context.oldMsisdn, 1)
    }
    @Дано("msisdn: {string}, старый msisdn: {string}")
    fun getMsisdnNumbers(msisdn: String, old_msisdn: String) {
        Context.msisdn = msisdn
        Context.oldMsisdn = old_msisdn
    }
    @И("заполнение базы")
    fun setDataBase() {
        cnnService["NEW_MSISDN"] = Context.msisdn;
        cnnService["OLD_MSISDN"] = Context.oldMsisdn;
        cnnService["SUBSCRIBER_BLOCK_TYPE"] = SUBSCRIBER_BLOCK_TYPE.NOT_BLOCKED
        setUp.insertRecord(DbUtil, "CNN_SERVICE", cnnService);
    }
    @И("")
    fun chtoto() {

    }
}