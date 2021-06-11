package ru.osp.cnn.core.monitoring

import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.osp.cnn.core.service.StatisticService
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_DB_CALL
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_CALL_CORE
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_DB
import ru.osp.cnn.core.service.StatisticStatus
import ru.osp.cnn.core.service.monitoring.OspEvent
import ru.osp.cnn.core.service.monitoring.OspEventFilter

@Component("dbStateMonitoringAdvice")
@Aspect
class DbStateMonitoringAdvice(@Qualifier("statisticManager") private val statisticService: StatisticService,
                              @Qualifier("ospDbEventFilter") private val ospEventFilter: OspEventFilter) {

    @Throws(Throwable::class)
    @AfterReturning(pointcut = "within(ru.osp.cnn.core.dao..*)")
    fun afterReturning() {
        statisticService.changeStatus(GROUP_CALL_CORE, EVENT_DB_CALL, StatisticStatus.SUCCESSFULL)
        statisticService.changeStatus(GROUP_DB, EVENT_DB_CALL, StatisticStatus.SUCCESSFULL)
    }

    @Throws(Throwable::class)
    @AfterThrowing(pointcut = "within(ru.osp.cnn.core.dao..*)", throwing = "e")
    fun afterThrowing(e: Exception?) {
        statisticService.changeStatus(ospEventFilter, OspEvent(GROUP_CALL_CORE, EVENT_DB_CALL, StatisticStatus.FAILED, e))
        statisticService.changeStatus(ospEventFilter, OspEvent(GROUP_DB, EVENT_DB_CALL, StatisticStatus.FAILED, e))
    }

}
