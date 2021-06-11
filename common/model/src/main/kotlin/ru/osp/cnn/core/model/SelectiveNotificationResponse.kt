package ru.osp.cnn.core.model

data class SelectiveNotificationResponse(val originatingAddress: String, val destinationAddress: String, val command: Command) {
    enum class Command(val commandCode: String) {
        SUSPEND_SERVICE("0"), ENABLE_NOTIFICATION_FOR_ALL("1"), APPROVE_NOTIFICATION("2");
    }
}
