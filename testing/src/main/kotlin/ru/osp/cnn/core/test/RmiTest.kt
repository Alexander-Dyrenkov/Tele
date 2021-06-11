package ru.osp.cnn.core.test

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.remoting.rmi.RmiProxyFactoryBean
import ru.osp.cnn.core.rmi.CoreServiceBusiness
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Configuration
import ru.osp.cnn.core.rmi.sc.SwitchControlCallbackBusiness

@SpringBootApplication(scanBasePackages = ["ru.osp.cnn.core.test"], exclude = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
class RmiTest {
    companion object {
        private val logger = LoggerFactory.getLogger(RmiTest::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.debug("Starting rmi test...")
            try {
                val application = SpringApplication.run(RmiTest::class.java, *args)
                val coreServiceClient: CoreServiceClient = application.getBean("coreServiceClient") as CoreServiceClient
                val switchControlServiceClient: SwitchControlServiceClient = application.getBean("switchControlServiceClient") as SwitchControlServiceClient
                val serviceInfo = coreServiceClient.coreServiceBusiness!!.getServiceInfo("79032222222")
                println("serviceInfo = $serviceInfo")
                switchControlServiceClient.switchControlCallbackBusiness!!.serviceEnabled("79035555555", "79031111111", "100500", "ru")
            } catch (e: Throwable) {
                logger.error("Error starting rmi test", e)
            }
        }
    }

}

@Configuration
class Configuration {
    @Value("\${jmx.rmi.host:0.0.0.0}")
    private lateinit var rmiHost: String

    @Value("\${jmx.rmi.port:1099}")
    private lateinit var rmiPort: String

    @Value("\${jmx.rmi.port:CoreServiceBusiness}")
    private lateinit var rmiCoreServiceName: String

    @Value("\${jmx.rmi.port:SwitchControlCallbackBusiness}")
    private lateinit var rmiSwitchControlServiceName: String

    @Bean("coreServiceClient")
    fun coreServiceClient(@Qualifier("rmiCoreProxyFactoryBean") coreServiceBusiness: CoreServiceBusiness): CoreServiceClient {
        val coreServiceClient = CoreServiceClient()
        coreServiceClient.coreServiceBusiness = coreServiceBusiness
        return coreServiceClient
    }

    @Bean("switchControlServiceClient")
    fun switchControlServiceClient(@Qualifier("rmiSwitchControlProxyFactoryBean") switchControlCallbackBusiness: SwitchControlCallbackBusiness): SwitchControlServiceClient {
        val switchControlServiceClient = SwitchControlServiceClient()
        switchControlServiceClient.switchControlCallbackBusiness = switchControlCallbackBusiness
        return switchControlServiceClient
    }

    @Bean("rmiCoreProxyFactoryBean")
    fun rmiCoreProxyFactoryBean(): RmiProxyFactoryBean {
        val rmiProxyFactoryBean = RmiProxyFactoryBean()
        rmiProxyFactoryBean.serviceUrl = "rmi://$rmiHost:${rmiPort.toInt()}/$rmiCoreServiceName"
        rmiProxyFactoryBean.serviceInterface = CoreServiceBusiness::class.java
        return rmiProxyFactoryBean
    }

    @Bean("rmiSwitchControlProxyFactoryBean")
    fun rmiSwitchControlProxyFactoryBean(): RmiProxyFactoryBean {
        val rmiProxyFactoryBean = RmiProxyFactoryBean()
        rmiProxyFactoryBean.serviceUrl = "rmi://$rmiHost:${rmiPort.toInt()}/$rmiSwitchControlServiceName"
        rmiProxyFactoryBean.serviceInterface = SwitchControlCallbackBusiness::class.java
        return rmiProxyFactoryBean
    }
}

class CoreServiceClient {
    var coreServiceBusiness: CoreServiceBusiness? = null
}

class SwitchControlServiceClient {
    var switchControlCallbackBusiness: SwitchControlCallbackBusiness? = null
}
