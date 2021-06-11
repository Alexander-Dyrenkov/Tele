package ru.osp.cnn.core.model

enum class SmsTemplateSettingName(val settingName: String) {
    PROCEED_ENABLE_SERVICE_SMS_TEMPLATE("proceed.enable.service.sms.template"),
    PROCEED_ENABLE_SERVICE_NEW_SMS_TEMPLATE("proceed.enable.service.new.sms.template"),
    NEW_NUMBER_SMS_TEMPLATE("new.number.sms.template"),
    NEW_NUMBER_REPORTED_ANONYMOUS_SMS_TEMPLATE("new.number.reported.anonymous.sms.template"),
    NEW_NUMBER_REPORTED_SMS_TEMPLATE("new.number.reported.sms.template"),
    SERVICE_DISABLED_SMS_TEMPLATE("service.disabled.sms.template"),
    SERVICE_DISABLED_OLD_SMS_TEMPLATE("service.disabled.old.sms.template"),
    SERVICE_CREATION_FAILED_SMS_TEMPLATE("service.creation.failed.sms.template"),
    SERVICE_CREATION_FAILED_FINAL_SMS_TEMPLATE("service.creation.failed.final.sms.template"),
    SERVICE_REMOVAL_FAILED_SMS_TEMPLATE("service.removal.failed.sms.template"),
    SERVICE_REMOVAL_FAILED_FINAL_SMS_TEMPLATE("service.removal.failed.final.sms.template"),
    INFO_SERVICE_DISABLED_SMS_TEMPLATE("info.service.disabled.sms.template"),
    INFO_SERVICE_ENABLED_SMS_TEMPLATE("info.service.enabled.sms.template"),
    INFO_SERVICE_NOT_FOUND_SMS_TEMPLATE("info.service.not.found.sms.template"),
    REQUEST_FOR_APPROVAL_SMS_TEMPLATE("request.for.approval.template"),
    FIRST_NOTIFICATION_SMS_TEMPLATE("first.notification.template"),
    NOTOFICATION_FOR_ALL_IS_ENABLED_SMS_TEMPLATE("notification.for.all.is.enabled.template"),
    NOTOFICATION_FOR_ALL_IS_SUSPENDED_SMS_TEMPLATE("notification.for.all.is.suspended.template"),
    SERVICE_RESUMED_SMS_TEMPLATE("service.resumed.template"),
    SERVICE_SUSPENDED_SMS_TEMPLATE("service.suspended.template"),
    SELECTIVE_NOTIFICATION_ENABLED_SMS_TEMPLATE("selective.notification.enabled.template");
}
