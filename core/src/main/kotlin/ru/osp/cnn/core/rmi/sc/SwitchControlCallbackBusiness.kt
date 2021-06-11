package ru.osp.cnn.core.rmi.sc

import java.rmi.RemoteException

interface SwitchControlCallbackBusiness {
    @Throws(RemoteException::class)
    fun serviceEnabled(newMsisdn: String, oldMsisdn: String, techNumber: String, languageCode: String?)

    @Throws(RemoteException::class)
    fun serviceEnableFailed(newMsisdn: String, languageCode: String?)

    @Throws(RemoteException::class)
    fun serviceDisabled(newMsisdn: String, oldMsisdn: String, languageCode: String?, serviceId: Long)

    @Throws(RemoteException::class)
    fun serviceSuspended(newMsisdn: String, languageCode: String?)

    @Throws(RemoteException::class)
    fun serviceResumed(newMsisdn: String, languageCode: String?)

    @Throws(RemoteException::class)
    fun selectiveNotificationEnabled(newMsisdn: String, languageCode: String?)
}