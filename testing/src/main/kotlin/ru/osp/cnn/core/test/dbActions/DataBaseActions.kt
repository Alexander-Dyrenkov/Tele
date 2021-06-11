package ru.osp.cnn.core.test.dbActions

class DataBaseActions {
    //    fun getCurrentDbDate() {
//        return DbUtil.executeSql("SELECT CURRENT_TIMESTAMP FROM dual").get(0).currentTimestamp
//    }
    fun prepareRequest(msisdn: String, status: String, oldMsisdn: String, languageCode: String, marketCode: String): String {
        return "<ser:enableService xmlns:ser=\"http://lanit.ru/beeline/switchcontrol/service\">" +
                "   <ser:msisdn>" + msisdn + "</ser:msisdn>" +
                "   <ser:serviceName>CNN</ser:serviceName>" +
                "   <ser:serviceParams>" +
                "       <ser:parameters>" +
                "           <ser:key>STATUS</ser:key>" +
                "           <ser:value>" + status + "</ser:value>" +
                "       </ser:parameters>" +
                "       <ser:parameters>" +
                "           <ser:key>OLD_MSISDN</ser:key>" +
                "           <ser:value>" + oldMsisdn + "</ser:value>" +
                "       </ser:parameters>" +
                "       <ser:parameters>" +
                "           <ser:key>LANGUAGE_CODE</ser:key>" +
                "           <ser:value>" + languageCode + "</ser:value>" +
                "       </ser:parameters>" +
                "       <ser:parameters>" +
                "           <ser:key>MARKET_CODE</ser:key>" +
                "           <ser:value>" + marketCode + "</ser:value>" +
                "       </ser:parameters>" +
                "   </ser:serviceParams>" +
                "   <ser:operatorId>1</ser:operatorId>" +
                "</ser:enableService>"
    }
}