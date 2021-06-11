package ru.osp.cnn.core.service.monitoring

import ru.lanit.osp.commons.statistic.DelegatingStatusStatistic
import ru.lanit.osp.commons.statistic.StatisticGroup
import ru.osp.cnn.core.service.StatisticStatus

class OspEvent(var statisticGroup: StatisticGroup, var statistic: DelegatingStatusStatistic, var status: StatisticStatus, var cause: Throwable? = null)
