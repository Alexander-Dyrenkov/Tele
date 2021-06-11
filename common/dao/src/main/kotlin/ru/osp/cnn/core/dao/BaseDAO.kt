package ru.osp.cnn.core.dao

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

open class BaseDAO {

    @Autowired
    lateinit var sessionFactory: SessionFactory

    fun getSession(): Session = sessionFactory.currentSession
}