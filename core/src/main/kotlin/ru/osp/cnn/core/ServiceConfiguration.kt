package ru.osp.cnn.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.slf4j.MDCContext
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.*
import org.springframework.core.io.ClassPathResource
import org.springframework.orm.hibernate5.HibernateTransactionManager
import org.springframework.orm.hibernate5.LocalSessionFactoryBean
import org.springframework.remoting.jaxws.JaxWsPortProxyFactoryBean
import org.springframework.remoting.rmi.RmiServiceExporter
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import ru.lanit.beeline.cnn.officebackend.ws.onlineoperations.client.OnlineBillingService
import ru.lanit.commons.dao.UtilDao
import ru.lanit.commons.dao.impl.OracleUtilDaoImpl
import ru.lanit.commons.jdbc.JdbcOperationsExtended
import ru.lanit.commons.jdbc.JdbcTemplateExtended
import ru.lanit.osp.common.api.date.CurrentDateService
import ru.lanit.osp.common.api.date.impl.SystemCurrentDateServiceImpl
import ru.lanit.osp.commons.statistic.model.AbstractStatisticManager
import ru.osp.cnn.core.rmi.CoreServiceBusiness
import ru.osp.cnn.core.rmi.sc.SwitchControlCallbackBusiness
import ru.osp.commons.cap.neo.GsonUtil
import ru.osp.smppgateway.ParlaySmppModelUtil
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext

@Configuration
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy
class ServiceConfiguration {
    @Value("\${jmx.rmi.port:1099}")
    private lateinit var rmiPort: String

    fun getGsonBuilder(gsonBuilder: GsonBuilder): GsonBuilder {
        return ParlaySmppModelUtil.parlaySmppGsonBuilder(GsonUtil().capGsonBuilder(gsonBuilder))
    }

    @Bean("smsExecutor")
    fun smsExecutor(@Value("\${sms.executor.thread.pool.size}") poolSize: Int): Executor {
        return ThreadPoolTaskExecutor().also {
            it.corePoolSize = poolSize
            it.maxPoolSize = poolSize
            it.setThreadNamePrefix("sms-executor-")
        }
    }

    @Bean("smsCoroutineContext")
    fun smsCoroutineContext(@Qualifier("smsExecutor") smsExecutor: Executor): CoroutineContext {
        return smsExecutor.asCoroutineDispatcher()
    }

    @Bean("smsCoroutineScope")
    fun smsCoroutineScope(@Qualifier("smsCoroutineContext") smsCoroutineContext: CoroutineContext): CoroutineScope {
        return CoroutineScope(smsCoroutineContext + MDCContext())
    }

    @Bean("gson")
    fun gson(): Gson = getGsonBuilder(GsonBuilder()).create()

    @Bean("httpClient")
    fun httpClient(): HttpClient {
        return HttpClient(CIO) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    getGsonBuilder(getGsonBuilder(this))
                }
            }
        }
    }

    @Bean("onlineBillingService")
    fun onlineBillingService(@Value("\${wsbi.ws.server}") wsbiWsServer: String,
                             @Value("\${wsbi.ws.location}") wsbiWsLocation: String): OnlineBillingService {
        val proxyFactoryBean = JaxWsPortProxyFactoryBean()
        proxyFactoryBean.endpointAddress = wsbiWsServer + wsbiWsLocation
        proxyFactoryBean.serviceInterface = OnlineBillingService::class.java
        proxyFactoryBean.serviceName = "OnlineBillingServiceService"
        proxyFactoryBean.namespaceUri = "http://eai.arstel.com"
        proxyFactoryBean.setWsdlDocumentResource(ClassPathResource("OnlineBillingService.wsdl"))
        proxyFactoryBean.afterPropertiesSet()
        return proxyFactoryBean.`object` as OnlineBillingService
    }

    @Bean("hibernateProperties")
    fun hibernateProperties(): Properties {
        val hibernateProperties = Properties()
        hibernateProperties["hibernate.cache.region.factory_class"] = "org.hibernate.cache.jcache.internal.JCacheRegionFactory"
        hibernateProperties["hibernate.cache.provider_class"] = "org.ehcache.jsr107.EhcacheCachingProvider"
        hibernateProperties["hibernate.cache.use_second_level_cache"] = true
        hibernateProperties["hibernate.cache.use_query_cache"] = true
        hibernateProperties["hibernate.dialect"] = "org.hibernate.dialect.Oracle12cDialect"
        hibernateProperties["hibernate.show_sql"] = true
        hibernateProperties["hibernate.use_sql_comments"] = false
        hibernateProperties["hibernate.format_sql"] = false
        hibernateProperties["hibernate.generate_statistics"] = true
        hibernateProperties["hibernate.order_updates"] = true
        hibernateProperties["hibernate.query.substitutions"] = "<![CDATA[ yes='Y', no='N' ]]>"
        hibernateProperties["hibernate.cglib.use_reflection_optimizer"] = true
        hibernateProperties["hibernate.query.factory_class"] = "org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory"
        hibernateProperties["hibernate.transaction.flush_before_completion"] = true
        hibernateProperties["hibernate.connection.autocommit"] = true
        hibernateProperties["hibernate.connection.release_mode"] = "after_transaction"
        hibernateProperties["hibernate.connection.current_session_context_class"] = "org.springframework.orm.hibernate5.SpringSessionContext"
        hibernateProperties["hibernate.connection.pool_size"] = 20
        return hibernateProperties
    }

    @Bean("sessionFactory")
    fun sessionFactory(@Qualifier("hibernateProperties") hibernateProperties: Properties,
                       @Qualifier("dataSource") dataSource: DataSource): LocalSessionFactoryBean {
        val sessionFactory = LocalSessionFactoryBean()
        sessionFactory.setDataSource(dataSource)
        sessionFactory.setMappingResources(
                "ru/osp/cnn/core/dao/mapping/ServiceDO.hbm.xml",
                "ru/osp/cnn/core/dao/mapping/SettingDO.hbm.xml",
                "ru/osp/cnn/core/dao/mapping/SmsControlRecordDO.hbm.xml",
                "ru/osp/cnn/core/dao/mapping/WhiteNumberDO.hbm.xml"
        )
        sessionFactory.hibernateProperties = hibernateProperties
        return sessionFactory
    }

    @Bean("transactionManager")
    fun hibernateTransactionManager(@Qualifier("sessionFactory") sessionFactory: SessionFactory): PlatformTransactionManager {
        val transactionManager = HibernateTransactionManager()
        transactionManager.sessionFactory = sessionFactory
        return transactionManager
    }

    @Primary
    @Bean("dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean("transactionOperations")
    fun transactionOperations(@Qualifier("transactionManager") transactionManager: PlatformTransactionManager): TransactionOperations = TransactionTemplate(transactionManager)

    @Bean("coreServiceBeanRmi")
    fun coreServiceBusinessExporter(@Value("\${jmx.rmi.coreServiceName:CoreServiceBusiness}") rmiCoreServiceName: String,
                                    @Qualifier("coreServiceBean") coreServiceBusiness: CoreServiceBusiness): RmiServiceExporter {
        val serviceInterface: Class<CoreServiceBusiness> = CoreServiceBusiness::class.java
        val exporter = RmiServiceExporter()
        exporter.serviceInterface = serviceInterface
        exporter.service = coreServiceBusiness
        exporter.setServiceName(rmiCoreServiceName)
        exporter.setRegistryPort(rmiPort.toInt())
        return exporter
    }

    @Bean("switchControlCallbackBeanRmi")
    fun switchControlCallbackBusinessExporter(@Value("\${jmx.rmi.switchControlServiceName:SwitchControlCallbackBusiness}") rmiSwitchControlServiceName: String,
                                              @Qualifier("switchControlCallbackBean") switchControlCallbackBusiness: SwitchControlCallbackBusiness): RmiServiceExporter {
        val serviceInterface: Class<SwitchControlCallbackBusiness> = SwitchControlCallbackBusiness::class.java
        val exporter = RmiServiceExporter()
        exporter.serviceInterface = serviceInterface
        exporter.service = switchControlCallbackBusiness
        exporter.setServiceName(rmiSwitchControlServiceName)
        exporter.setRegistryPort(rmiPort.toInt())
        return exporter
    }

    @Bean("jdbcOperationsExtended")
    fun jdbcOperationsExtended(@Qualifier("dataSource") dataSource: DataSource): JdbcOperationsExtended {
        return JdbcTemplateExtended(dataSource)
    }

    @Bean("utilDao")
    fun utilDao(@Qualifier("jdbcOperationsExtended") jdbcOperationsExtended: JdbcOperationsExtended): UtilDao {
        return OracleUtilDaoImpl(jdbcOperationsExtended)
    }

    @Bean("currentDateService")
    fun currentDateService(): CurrentDateService {
        return SystemCurrentDateServiceImpl()
    }

    @Bean("periodicalTaskScheduler")
    fun periodicalTaskScheduler(@Value("\${scheduler.thread.pool.size:2}") schedulerThreadPoolSize: Int): Executor {
        return Executors.newScheduledThreadPool(schedulerThreadPoolSize)
    }

    @Bean("ospEventFilterSettingsUpdateScheduler")
    fun ospEventFilterSettingsUpdateScheduler(@Value("\${monitoring.scheduler.thread.pool.size:2}") schedulerThreadPoolSize: Int): Executor {
        return Executors.newScheduledThreadPool(schedulerThreadPoolSize)
    }

    @Bean("statisticTaskScheduler")
    fun statisticTaskScheduler(@Value("\${statistic.scheduler.thread.pool.size:1}") schedulerThreadPoolSize: Int): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(schedulerThreadPoolSize)
    }

    @Bean("statisticManagerConfig")
    fun statisticManagerConfig(@Value("\${statistic.reset.period:60}") resetStatisticPeriod: Long,
                               @Value("\${statistic.update.auto.period:5}") updateAutoStatisticPeriod: Long,
                               @Value("\${statistic.write.statistic.period}") writeStatisticPeriod: Long?,
                               @Qualifier("statisticTaskScheduler") scheduler: ScheduledExecutorService): AbstractStatisticManager.Config {
        return AbstractStatisticManager.Config(resetStatisticPeriod, updateAutoStatisticPeriod, writeStatisticPeriod, scheduler)
    }

}
