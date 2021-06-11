package ru.osp.cnn.core.model

import java.io.Serializable
import java.util.ArrayList

class SettingDO(var settingId: Long? = null, var name: String? = null, var type: Type? = null, var description: String? = null, var regularExpression: String? = null, var values: Collection<String> = ArrayList()) : Serializable {

    enum class Type {
        SIMPLE, LIST
    }

}
