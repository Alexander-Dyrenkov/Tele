package ru.osp.cnn.core.test.util

class SetUp {
    fun getNextCnnSequenceValue(): Int {
        return DbUtil.executeSql("select CNN_SEQUENCE.nextval as next from dual")[0].next + 1;
    }
    fun getCurrentDbDate(shift: Any? = null) {
        if (shift != null) {
            if (shift.toString().indexOf("-") != -1) {
                return DbUtil.executeSql("select sysdate" + shift + " system from dual")[0].system.getTime();
            } else {
                return DbUtil.executeSql("select sysdate+" + shift + " system from dual")[0].system.getTime();
            }
        }
        return DbUtil.executeSql("select sysdate from dual")[0].sysdate.getTime();
    }
    fun dateToSqlTimestamp(date: Any?) {
        return java.sql.Timestamp(date.getTime());
    }
    fun getDefaultCnnService(): HashMap<String, Any?> {
        return hashMapOf(
            "SERVICE_ID" to getNextCnnSequenceValue(),
            "NEW_MSISDN" to null,
            "OLD_MSISDN" to null,
            "SUBSCRIBER_BLOCK_TYPE" to SUBSCRIBER_BLOCK_TYPE.NOT_BLOCKED,
            "ACTIVATION_DATE" to dateToSqlTimestamp(getCurrentDbDate(null)),
            "STATUS" to SERVICE_STATUS.NOTIFICATION_FOR_ALL,
            "IS_FIRST_NOTIFICATION" to false,
            "ASYNC_OPERATION_DATE" to null,
            "TECH_NUMBER_MSISDN" to null,
            "LANG_CODE" to LANGUAGE_CODE.RUS,
            "REMOVAL_DATE" to null,
            "REMOVE_AFTER_DATE" to null
        )
    }
    //Добавляет в указанную таблицу запись
//Запись представляется в виде объекта {column1:value1, column2:value2 ...}
//где column1 и column2 названия колонок в таблице, а value1 и value2 соответсвующие им значения для добавляемой записи
    fun insertRecord(dbUtil, tableName: String, record: HashMap<String, Any?>) {
        var columnsNames = "";
        var valuePlaceholders = "";
        var values: Array<String>;
        for ((column, value) in record) {
            valuePlaceholders += "?,";
            columnsNames += "$column,";
            values.push(value);
            CommonUtil.log("$column: $value")
        }
        valuePlaceholders = valuePlaceholders.slice(0, valuePlaceholders.length - 1);
        columnsNames = columnsNames.slice(0, columnsNames.length - 1);
        var insertRequest = "INSERT INTO " + tableName + " (" + columnsNames + ") VALUES (" + valuePlaceholders + ")";
        dbUtil.executeUpdate(insertRequest, values);
    }
    fun setDefaultCnnSettings() {
        lateinit var newSetting: HashMap<String, Any?>;
        for ((settingName, value) in Context.CNN_DEFAULT_SETTINGS) {
            for ((propertyKey, propertyValue) in Context.CNN_DEFAULT_SETTINGS[settingName].orEmpty()) {
                newSetting[propertyKey] = Context.CNN_DEFAULT_SETTINGS[settingName]!![propertyKey];
            }
            Context.CNN_SETTINGS[settingName] = newSetting;
        }
    }
    fun updateCnnSettings() {

    }
}