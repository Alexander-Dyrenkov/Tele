package ru.osp.cnn.core.test

import cucumber.api.java.ru.Дано
import cucumber.api.java.ru.Затем
import cucumber.api.java.ru.И
import cucumber.api.java.ru.Тогда
import org.junit.Assert
import ru.osp.cnn.core.model.ServiceStatus
import ru.osp.cnn.core.model.CnnDataAccessConstants.Companion
import ru.osp.cnn.core.test.util.Context
import ru.osp.cnn.core.test.util.SetUp


class CommonStepDefinition {
    private val setUp = SetUp()
    var cnnService = setUp.getDefaultCnnService()
    var status = Context.SERVICE_STATUS[ServiceStatus.NOTIFICATION_FOR_ALL]
    var languageCode = Context.LANGUAGE_CODE["ENG"]
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
        addCnnTechNumber(techMsisdn, Context.TECH_NUMBER_STATE["VACANT"], marketCode, currentDateInsert)
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
            "techNumberState" to Context.TECH_NUMBER_STATE["ASSIGNED"],
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
            "subscriberBlockType" to Context.SERVICE_SUBSCRIBER_BLOCK_TYPE[Companion.NOT_BLOCKED],
            "activationDate" to currentDate,
            "status" to Context.SERVICE_STATUS[ServiceStatus.NOTIFICATION_FOR_ALL],
            "isFirstNotification" to Context.BOOLEAN["FALSE"],
            "asyncOperationDate" to null,
            "techNumberMsisdn" to techMsisdn,
            "langCode" to languageCode
        )

        checkCnnService(actualCnnService, expectedCnnService)
    }
    @И("Проверка сколько СМС было отправлено")
    fun totalSMSChecking() {
        checkTotalSmsSent(2)
        checkSmsSent(Context.SMS_TEMPLATE["CNN_REDIRECTION_INSTRUCTION_NE"], Context.msisdn, 1)
        checkSmsSent(Context.SMS_TEMPLATE["CNN_REDIRECTION_INSTRUCTION"], Context.oldMsisdn, 1)
    }
    @Дано("msisdn: {string}, старый msisdn: {string}")
    fun getMsisdnNumbers(msisdn: String, old_msisdn: String) {
        Context.msisdn = msisdn
        Context.oldMsisdn = old_msisdn
    }
    @И("заполнение базы")
    fun setDataBase() {
        cnnService["NEW_MSISDN"] = Context.msisdn
        cnnService["OLD_MSISDN"] = Context.oldMsisdn
        cnnService["SUBSCRIBER_BLOCK_TYPE"] = Context.SUBSCRIBER_BLOCK_TYPE[Companion.NOT_BLOCKED]
        setUp.insertRecord(DbUtil, "CNN_SERVICE", cnnService)
    }
    @Тогда("выполнение теста")
    fun executionTest() {
        setUp.changeAccountParameters(Context.msisdn, hashMapOf("BLOCK_TYPE" to Context.ACCOUNT_BLOCK_TYPE["BLOCK_TYPE_PARAM_VALUE_INCOMMING"]))
    }
    @Затем("проверки результата")
    fun checkResult() {
        val actualCnnService = setUp.findCnnServiceById(cnnService["SERVICE_ID"])
        Assert.assertEquals("Block type has not changed", actualCnnService.subscriberBlockType, Context.SUBSCRIBER_BLOCK_TYPE[Companion.BLOCKED_INCOMING])
    }
}