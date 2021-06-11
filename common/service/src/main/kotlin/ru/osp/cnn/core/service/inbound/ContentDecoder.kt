package ru.osp.cnn.core.service.inbound

import org.springframework.stereotype.Component
import java.util.regex.Matcher
import java.util.regex.Pattern

interface ContentDecoder {
    fun decode(content: String): String
    fun canDecode(content: String): Boolean
}

@Component("contentDecoder")
class UnicodeCharsSequenceDecoder : ContentDecoder {
    companion object {
        private val UNICODE_CHAR_PATTERN: Pattern = Pattern.compile("([\\dABCDIF]{4})")
        private val UNICODE_CHARS_SEQUENCE_PATTERN: Pattern = Pattern.compile("([\\dABCDIF]{4})*")
    }

    override fun decode(content: String): String {
        val m: Matcher = UNICODE_CHAR_PATTERN.matcher(content)
        val sb = StringBuilder()
        while (m.find()) {
            sb.append(m.group(1).toInt(16).toChar())
        }
        return sb.toString()
    }

    override fun canDecode(content: String): Boolean {
        return if (content.isNotBlank()) {
            val m: Matcher = UNICODE_CHARS_SEQUENCE_PATTERN.matcher(content)
            m.matches()
        } else {
            return false
        }
    }
}
