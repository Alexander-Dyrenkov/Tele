package ru.osp.cnn.core.service.util

import org.junit.Assert
import org.junit.Test
import ru.osp.cnn.core.model.ServiceDO

class CoreServiceBoUtilsTest {
    companion object {
        private const val NOT_BLOCKED = "0"
        private const val OUTGOING_BLOCKED = "1"
        private const val INCOMING_BLOCKED = "2"
        private const val ALL_BLOCKED = "3"
    }

    @Test
    fun testIsSubscriberBlocked_serviceNotBlocked() {
        val serviceDO = ServiceDO()
        serviceDO.subscriberBlockType = NOT_BLOCKED
        Assert.assertFalse(CoreServiceBoUtils.isSubscriberBlocked(serviceDO))
    }

    @Test
    fun testIsSubscriberBlocked_serviceOutgoingBlocked() {
        val serviceDO = ServiceDO()
        serviceDO.subscriberBlockType = OUTGOING_BLOCKED
        Assert.assertTrue(CoreServiceBoUtils.isSubscriberBlocked(serviceDO))
    }

    @Test
    fun testIsSubscriberBlocked_serviceIncomingBlocked() {
        val serviceDO = ServiceDO()
        serviceDO.subscriberBlockType = INCOMING_BLOCKED
        Assert.assertTrue(CoreServiceBoUtils.isSubscriberBlocked(serviceDO))
    }

    @Test
    fun testIsSubscriberBlocked_serviceAllBlocked() {
        val serviceDO = ServiceDO()
        serviceDO.subscriberBlockType = ALL_BLOCKED
        Assert.assertTrue(CoreServiceBoUtils.isSubscriberBlocked(serviceDO))
    }

}
