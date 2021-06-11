package ru.osp.cnn.core

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

@SpringBootApplication(scanBasePackages = ["ru.osp.cnn"], exclude = [HibernateJpaAutoConfiguration::class])
class Application {
    companion object {
        private val logger = LoggerFactory.getLogger(Application::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.debug("Starting spring boot application...")
            try {
                SpringApplication.run(Application::class.java, *args)
            } catch (e: Throwable) {
                logger.error("Error starting spring boot application", e)
            }
        }
    }

}
