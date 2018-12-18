package ro.organizator.cpk

import java.util.regex.Pattern

/**
 * Decides whether a phone number is allowed
 * Extracts all the phone numbers from the settings ignoring everything else and compares with the
 * phone number being checked
 *
 * Created 09-09-2017
 *
 * @author Ovidiu
 */
class PhoneValidator {

    fun isAllowed(phone: String, permissions: String): Boolean {
        val pattern = Pattern.compile("\\+?([\\s-]?\\d)+")
        val m = pattern.matcher(permissions)
        while (m.find()) {
            val allowedPhone = m.group().replace("[\\s-]".toRegex(), "")
            if (phone == allowedPhone) {
                return true
            }
        }
        return false
    }
}
