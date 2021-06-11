package ru.osp.cnn.core.service

interface WsbiClient {

    /**
     * Notify billing about service disabling.
     * @param newMsisdn new msisdn
     */
    @Throws(WsbiException::class)
    fun disableService(newMsisdn: String)
}
