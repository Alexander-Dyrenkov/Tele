package ru.osp.cnn.core.monitoring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.lanit.osp.common.api.date.CurrentDateService
import ru.lanit.osp.commons.statistic.*
import ru.lanit.osp.commons.statistic.api.ProxyStatistic
import ru.lanit.osp.commons.statistic.basic.AverageTimeStatisticImpl
import ru.lanit.osp.commons.statistic.basic.CounterStatisticImpl
import ru.lanit.osp.commons.statistic.basic.StatisticGroupImpl
import ru.lanit.osp.commons.statistic.model.AbstractStatisticManager
import ru.lanit.osp.commons.statistic.status.DelegatingStatusStatisticImpl
import ru.lanit.osp.commons.statistic.support.ProxyCounterStatisticImpl
import ru.osp.cnn.core.service.Settings
import ru.osp.cnn.core.service.StatisticService
import ru.osp.cnn.core.service.StatisticService.Companion.EPCP_LOAD
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_CALL_PROCESS_TIME
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_DB_CALL
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_SMSGW_CALL
import ru.osp.cnn.core.service.StatisticService.Companion.EVENT_WSBI_CALL
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_CALL_CORE
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_DB
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_SMSGW
import ru.osp.cnn.core.service.StatisticService.Companion.GROUP_WSBI
import ru.osp.cnn.core.service.StatisticService.Companion.PROCESSED_CALLS
import ru.osp.cnn.core.service.StatisticService.Companion.PROCESSED_SMS
import ru.osp.cnn.core.service.StatisticService.Companion.RECEIVED_SMS
import ru.osp.cnn.core.service.StatisticService.Companion.SC_CALLS
import ru.osp.cnn.core.service.StatisticService.Companion.WSBI_CALLS
import ru.osp.cnn.core.service.StatisticSeverity
import ru.osp.cnn.core.service.StatisticStatus
import ru.osp.cnn.core.service.monitoring.OspEvent
import ru.osp.cnn.core.service.monitoring.OspEventFilter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@Component("statisticManager")
class StatisticManagerImpl(@Qualifier("statisticManagerConfig") config: Config,
                           @Qualifier("settings") private val settings: Settings,
                           @Qualifier("currentDateService") currentDateService: CurrentDateService
) : AbstractStatisticManager(config), StatisticService {
    init {
        super.currentDateService = currentDateService
        startMonitoring()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(StatisticManagerImpl::class.java)
    }

    override fun restartTasks(config: Config) {
        super.restartTasks()
        startMonitoring()
    }

    private fun startMonitoring() {
        config.abstractWriteStatisticsPeriod?.let { writeStatisticsPeriod ->
            val executorService = config.abstractScheduledExecutorService
            executorService?.let {
                currentDateService?.let { currentDateService ->
                    val fileStatisticJob = FileStatisticJob(this, currentDateService, settings)
                    scheduledFutures.add(executorService.scheduleAtFixedRate(fileStatisticJob, 0, writeStatisticsPeriod, TimeUnit.MILLISECONDS))
                }
            }
        }
    }

    override fun register() {
        EPCP_LOAD = registerStatistic(createCounterStatisticImpl("EPCP_LOAD", "Количество входящих звонков"))
        PROCESSED_CALLS = registerStatistic(createCounterStatisticImpl("PROCESSED_CALLS", "Количество обработанных входящих звонков"))
        EVENT_CALL_PROCESS_TIME = registerStatistic(createAverageTimeStatisticImpl("EVENT_CALL_PROCESS_TIME", "Среднее время обработки входящих звонков"))

        PROCESSED_SMS = registerStatistic(createCounterStatisticImpl("PROCESSED_SMS", "Количество отправленных СМС"))
        RECEIVED_SMS = registerStatistic(createCounterStatisticImpl("RECEIVED_SMS", "Количество полученных СМС"))
        EVENT_SMSGW_CALL = registerStatistic(createDelegatingStatusStatisticImpl("EVENT_SMSGW_CALL", "Статус отправки СМС"))

        SC_CALLS = registerStatistic(createCounterStatisticImpl("SC_CALLS", "Количество запросов от SwitchControl"))
        WSBI_CALLS = registerStatistic(createCounterStatisticImpl("WSBI_CALLS", "Количество запросов в WSBI"))
        EVENT_WSBI_CALL = registerStatistic(createDelegatingStatusStatisticImpl("EVENT_WSBI_CALL", "Статус запросов в WSBI"))

        EVENT_DB_CALL = registerStatistic(createDelegatingStatusStatisticImpl("EVENT_DB_CALL", "Статус запросов в БД"))

        GROUP_CALL_CORE = registerStatisticGroup(StatisticGroupImpl("CALL_CORE", EPCP_LOAD, PROCESSED_CALLS, EVENT_CALL_PROCESS_TIME, EVENT_DB_CALL))
        GROUP_SMSGW = registerStatisticGroup(StatisticGroupImpl("SMSGW", PROCESSED_SMS, RECEIVED_SMS, EVENT_SMSGW_CALL))
        GROUP_WSBI = registerStatisticGroup(StatisticGroupImpl("WSBI", SC_CALLS, WSBI_CALLS, EVENT_WSBI_CALL))
        GROUP_DB = registerStatisticGroup(StatisticGroupImpl("DB", EVENT_DB_CALL))
    }

    private fun createCounterStatisticImpl(name: String, description: String, statisticResetMode: StatisticResetMode = StatisticResetMode.RESET_TO_ZERO): ResettableCounterStatistic {
        return CounterStatisticImpl(name, description, statisticResetMode) { _, _ -> ProxyCounterStatisticImpl() }
    }

    private fun createDelegatingStatusStatisticImpl(name: String, description: String): DelegatingStatusStatistic {
        return DelegatingStatusStatisticImpl(name, description) { ProxyStatusStatisticImpl() }
    }

    private fun createAverageTimeStatisticImpl(name: String, description: String, resetMode: StatisticResetMode = StatisticResetMode.RESET_TO_ZERO): AverageTimeStatistic {
        return AverageTimeStatisticImpl(name, description, resetMode) { _, _ -> ProxyCounterStatisticImpl() }
    }

    override fun changeStatus(statisticGroup: StatisticGroup, statistic: DelegatingStatusStatistic, status: StatisticStatus, errorMessage: String?) {
        val statisticStatus = "${statistic.name}_$status"
        logger.debug("OspMonitorImpl.processComponentStateTransitionEvent() start\n\tcomponentName=${statisticGroup.name}\n\teventName=$statisticStatus\n\terrorMessage=$errorMessage")
        statistic.setDelegate(object : StatusHolder {
            override val value: String
                get() = statisticStatus
        })
        logger.debug("OspMonitorImpl.processComponentStateTransitionEvent() return")
    }

    override fun changeStatus(filter: OspEventFilter, event: OspEvent) {
        if (filter.isTransitionEvent(event)) {
            changeStatus(event.statisticGroup, event.statistic, event.status, event.cause?.message)
        }
    }

    override fun increaseCounter(statistic: CounterStatistic) {
        logger.debug("OspMonitorImpl.increaseCounter()\n\tcounterName=${statistic.name}")
        statistic.increase()
    }

    override fun log(statistic: Statistic, severity: StatisticSeverity) {
        val name = statistic.name
        logger.debug("Logging common event: {}", name);
        val eventString = "${DATE_FORMAT.format(currentDateService!!.currentDate)}$DELIMITER${settings.moduleName}$DELIMITER${settings.nodeName}$DELIMITER$severity$DELIMITER$name$DELIMITER"
        monitoringAlarmLogger.debug(eventString)
    }
}

private val DATE_FORMAT: DateFormat = SimpleDateFormat("dd-MM-yyyy_HH:mm:ss")
private const val DELIMITER = ";"
private const val VALUE_DELIMITER = "="
private const val STATE_POSTFIX = "_STATE"
private val monitoringAlarmLogger = LoggerFactory.getLogger("monitoring.alarm")
private val monitoringStateLogger = LoggerFactory.getLogger("monitoring.state")
private val monitoringStatisticsLogger = LoggerFactory.getLogger("monitoring.statistics")

class ProxyStatusStatisticImpl : ProxyStatistic<String> {
    override var value: String = ""
}

class FileStatisticJob(private val statisticManager: StatisticManager,
                       private val currentDateService: CurrentDateService,
                       private val settings: Settings) : Runnable {
    companion object {
        private val logger = LoggerFactory.getLogger(FileStatisticJob::class.java)
    }

    override fun run() {
        logger.debug("write statistics to file started")
        try {
            val formattedDate = DATE_FORMAT.format(currentDateService.currentDate)

            logComponentStates(formattedDate)
            logCounterValues(formattedDate)
            logger.debug("write statistics to file finished")
        } catch (e: Exception) {
            logger.error("write statistics to file error", e)
        }
    }

    private fun logComponentStates(formattedDate: String) {
        var counterString = "$formattedDate$DELIMITER${settings.moduleName}$DELIMITER${settings.nodeName}$DELIMITER"

        var loggedCounters = 0
        statisticManager.getAllStatisticGroups().forEach { group ->
            group.statistics.forEach { statistic ->
                if (statistic is DelegatingStatusStatistic) {
                    counterString += "${statistic.name}$STATE_POSTFIX$VALUE_DELIMITER${statistic.formattedValue}$DELIMITER"
                    loggedCounters++
                }
            }
        }

        logger.debug("Logging component states: {}", counterString)
        try {
            monitoringStateLogger.debug(counterString)
        } catch (e: IOException) {
            logger.error("logComponentStates error", e)
        }
//        this.mBeanContainer.setState(counterString) TODO ???
    }

    private fun logCounterValues(formattedDate: String) {
        var counterString = "$formattedDate$DELIMITER${settings.moduleName}$DELIMITER${settings.nodeName}$DELIMITER"
        var loggedCounters = 0
        statisticManager.getAllStatisticGroups().forEach { group ->
            group.statistics.forEach { statistic ->
                if (statistic is CounterStatistic) {
                    counterString += "${statistic.name}$VALUE_DELIMITER${statistic.value}$DELIMITER"
                    loggedCounters++
                }
            }
        }

        if (loggedCounters != 0) {
            logger.debug("Logging counter values: {}", counterString)
            try {
                monitoringStatisticsLogger.debug(counterString)
            } catch (e: IOException) {
                logger.error("logCounterValues error", e)
            }
//            this.mBeanContainer.setStatistics(counterString) TODO ???
        }
    }

}
