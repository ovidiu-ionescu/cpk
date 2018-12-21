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

data class Destination(val name: String, val phoneNumber: String)

class PhoneParser {
    val phonePattern = Regex("\\+?([\\s-]?\\d){5,}")
    val phoneSeparator = Regex("[\\s-]")

    /**
     * Parses the destinations string into pairs of phone numbers and name
     */
    fun calculateDestinations(destinations: String): List<Destination> {
        return phonePattern.findAll(destinations).mapNotNull {
            Destination(
                destinations.substring(it.range.last + 1, it.next()?.range?.first ?: destinations.length).trim(),
                it.value.replace(phoneSeparator, "")
            )
        }.toList()
    }

    /**
     * Extracts the phone numbers from the permissions and checks if the phone number supplied is
     * present
     */
    fun isAllowed(phone: String, permissions: String): Boolean {
        return phonePattern.findAll(permissions).any {
            it.value.replace(phoneSeparator, "") == phone
        }
    }
}
