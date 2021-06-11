package ru.osp.cnn.core.test.constants

import org.springframework.boot.context.properties.bind.Bindable.mapOf
import org.springframework.stereotype.Component
import ru.osp.cnn.core.model.ServiceStatus

@Component
class Constants {
    val SERVICE_STATUS = mapOf(
             ServiceStatus.DISABLED to "0" ,                // Услуга отключена
             ServiceStatus.SUSPENDED to "1",               // Услуга остановлена
             ServiceStatus.NOTIFICATION_FOR_ALL to "2",    // Включено оповещение для всех
             ServiceStatus.SELECTIVE_NOTIFICATION to "3"   // Включена выборочная нотификация
    )
//                fun setFailOnFaultSC(fail) {
//            SwitchControlUtil.setFailOnFault(fail);
//        }
}