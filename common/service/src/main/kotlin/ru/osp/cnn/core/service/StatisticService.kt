package ru.osp.cnn.core.service

import ru.lanit.osp.commons.statistic.*
import ru.osp.cnn.core.service.monitoring.OspEvent
import ru.osp.cnn.core.service.monitoring.OspEventFilter

interface StatisticService {
    companion object {
        lateinit var GROUP_CALL_CORE: StatisticGroup
        lateinit var GROUP_SMSGW: StatisticGroup
        lateinit var GROUP_WSBI: StatisticGroup
        lateinit var GROUP_DB: StatisticGroup

        lateinit var EPCP_LOAD: CounterStatistic
        lateinit var PROCESSED_CALLS: CounterStatistic
        lateinit var EVENT_CALL_PROCESS_TIME: AverageTimeStatistic

        lateinit var PROCESSED_SMS: CounterStatistic
        lateinit var RECEIVED_SMS: CounterStatistic
        lateinit var EVENT_SMSGW_CALL: DelegatingStatusStatistic

        lateinit var SC_CALLS: CounterStatistic
        lateinit var WSBI_CALLS: CounterStatistic
        lateinit var EVENT_WSBI_CALL: DelegatingStatusStatistic

        lateinit var EVENT_DB_CALL: DelegatingStatusStatistic
    }

    fun changeStatus(statisticGroup: StatisticGroup, statistic: DelegatingStatusStatistic, status: StatisticStatus, errorMessage: String? = null)

    fun changeStatus(filter: OspEventFilter, event: OspEvent)

    fun increaseCounter(statistic: CounterStatistic)

    fun log(statistic: Statistic, severity: StatisticSeverity)

}

enum class StatisticStatus {
    FAILED,
    SUCCESSFULL
}

enum class StatisticSeverity {
    ERROR,
    WARNING,
    INFO
}
