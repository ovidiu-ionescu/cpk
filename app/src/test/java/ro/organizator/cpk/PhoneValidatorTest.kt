package ro.organizator.cpk

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

/**
 * Tests the phone validation
 *
 * Created by ovidiu on 9-9-17.
 */
class PhoneValidatorTest: StringSpec() {

    init {
        val phoneValidator = PhoneParser()

        "Phone number with dashes as separators" {
            val permissions = "+31-613358857 Arnold \n" + " 0677935827 Croton +31-403-749-373"
            phoneValidator.isAllowed("+31613358857", permissions) shouldBe true
            phoneValidator.isAllowed("535210", permissions) shouldBe false
        }

        "Phone number with spaces" {
            val permissions = "+31 6 13358857 Arnold \n" + " 0677935827 Croton +31-403-749-373"
            phoneValidator.isAllowed("+31613358857", permissions) shouldBe true
            phoneValidator.isAllowed("535210", permissions) shouldBe false
        }

        "Parse destinations" {
            val destinations = "+31 6 13358857 Arnold \n" + " 0677935827 Croton"
            val l = phoneValidator.calculateDestinations(destinations)
            l.size shouldBe 2
            l[0].name shouldBe "Arnold"
            l[0].phoneNumber shouldBe "+31613358857"
            l[1].name shouldBe "Croton"
            l[1].phoneNumber shouldBe "0677935827"
        }

        "Parse destinations with empty last name" {
            val destinations = "+31 6 13358857 Arnold \n" + " 0677935827 Croton +31-403-749-373"
            val l = phoneValidator.calculateDestinations(destinations)
            l.size shouldBe 3
            l[0].name shouldBe "Arnold"
            l[0].phoneNumber shouldBe "+31613358857"
            l[1].name shouldBe "Croton"
            l[1].phoneNumber shouldBe "0677935827"
            l[2].name shouldBe ""
            l[2].phoneNumber shouldBe "+31403749373"
        }
    }
}