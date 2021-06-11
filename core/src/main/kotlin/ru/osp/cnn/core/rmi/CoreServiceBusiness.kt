package ru.osp.cnn.core.rmi

import ru.osp.cnn.core.model.ServiceInfo
import java.rmi.RemoteException

interface CoreServiceBusiness {
    /**
     * Возвращает JavaBean ServiceInfo с информацией об услуге. Данный JavaBean имеет следующие
     * свойства:
     *
     *  * status - строка с информацией о статусе услуги, предназначенная для отправки абоненту;
     *  * oldMsisdn - старый номер абонента.
     *
     *
     * @param msisdn новый номер абонента
     * @return JavaBean с информацией об услуге
     * @throws RemoteException RemoteException
     */
    @Throws(RemoteException::class)
    fun getServiceInfo(msisdn: String): ServiceInfo?

}
