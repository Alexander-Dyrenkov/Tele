package ru.osp.cnn.core.model

class CnnDataAccessConstants {
    companion object {
        /**
         * ASC direction of order.
         */
        val SORT_ORDER_ASC = "ASC"

        /**
         * DESC direction of order.
         */
        val SORT_ORDER_DESC = "DESC"

        /**
         * Log date column name.
         */
        val LOG_DATE_COLUMN_NAME = "logDate"

        /**
         * Log operation type column name.
         */
        val LOG_OPERATION_TYPE_COLUMN_NAME = "logDate"

        /**
         * Log user name column name.
         */
        val LOG_USER_NAME_COLUMN_NAME = "userName"

        /**
         * Start of search date interval.
         */
        val START_LOG_DATE_COLUMN_NAME = "startLogDate"

        /**
         * End of search date interval.
         */
        val END_LOG_DATE_COLUMN_NAME = "endLogDate"

        /**
         * Log entity name column name.
         */
        val LOG_ENTITY_NAME_COLUMN_NAME = "entityName"

        /**
         * Service new msisdn column name.
         */
        val NEW_MSISDN_COLUMN_NAME = "newMsisdn"

        /**
         * Service old msisdn column name.
         */
        val OLD_MSISDN_COLUMN_NAME = "oldMsisdn"

        /**
         * Service technical msisdn column name.
         */
        val TECH_NUMBER_MSISDN_COLUMN_NAME = "techNumberMsisdn"

        /**
         * Service status column name.
         */
        val STATUS_COLUMN_NAME = "status"

        /**
         * Service is fist notification column name.
         */
        val IS_FIRST_NOTIFICATION_COLUMN_NAME = "isFirstNotification"

        /**
         * Cnn service name.
         */
        val CNN_SERVICE_NAME = "CNN" // Имя услуги CNN


        // Типы блокировок абонента

        // Типы блокировок абонента
        /**
         * Subscriber is not blocked.
         */
        val NOT_BLOCKED = "0" // Незаблокирован


        /**
         * Subscriber is bloced.
         */
        val BLOCKED = "1" //Заблокирован на исходящие


        /**
         * Subscriber is blocked for incoming.
         */
        val BLOCKED_INCOMING = "2" // Заблокирован на входящие


        /**
         * Subscriber is blocked for all.
         */
        val BLOCKED_ALL = "3" //Заблокирован на входящие и исходящие


        // Типы настроек

        // Типы настроек
        /**
         * Single setting type.
         */
        val SINGLE_SETTING_TYPE: Byte = 0 // Настройка с одним значением


        /**
         * List setting type.
         */
        val LIST_SETTING_TYPE: Byte = 1 //Настройка со списком значений


        /**
         * Date and time format.
         */
        val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

        //Operation types for logging.

        //Operation types for logging.
        /**
         * Create service operation type.
         */
        val OPERATION_TYPE_CREATE_ACCOUNT = "1"

        /**
         * Delete service operation type.
         */
        val OPERATION_TYPE_DELETE_ACCOUNT = "2"

        /**
         * Update setting operation type.
         */
        val OPERATION_TYPE_UPDATE_SETTING = "4"

        /**
         * Delete white number operation type.
         */
        val OPERATION_TYPE_DELETE_WHITE_NUMBER = "6"

        /**
         * Create white number operation type.
         */
        val OPERATION_TYPE_CREATE_WHITE_NUMBER = "5"

        /**
         * Enable service operation type.
         */
        val OPERATION_TYPE_ENABLE_SERVICE = "7"

        /**
         * Disable service operation type.
         */
        val OPERATION_TYPE_DISABLE_SERVICE = "8"

        /**
         * Change msisdn operation type.
         */
        val OPERATION_TYPE_CHANGE_MSISDN = "9"

        /**
         * Change block type operation type.
         */
        val OPERATION_TYPE_CHANGE_BLOCK_TYPE = "10"

        /**
         * Change status operation type.
         */
        val OPERATION_TYPE_SUSPENDED = "11"

        /**
         * Set notification for all operation type.
         */
        val OPERATION_TYPE_NOTIFICATION_FOR_ALL = "12"

        /**
         * Set selective notification operation type.
         */
        val OPERATION_TYPE_SELECTIVE_NOTIFICATION = "13"

        /**
         * Log in to backoffice operation type.
         */
        val OPERATION_TYPE_LOGIN_BACKOFFICE = "14"

        /**
         * Log out from backoffice operation type.
         */
        val OPERATION_TYPE_LOGOUT_BACKOFFICE = "15"

        /**
         * Change LanguageCode.
         */
        val OPERATION_TYPE_CHANGE_LANG_CODE = "16"

        /**
         * Change Account.
         */
        val OPERATION_TYPE_RENEW_ACCOUNT = "17"

        /**
         * Default value for length of white number list.
         */
        val WHITE_NUMBER_LIST_LENGTH_DEFAULT = 10

    }
}