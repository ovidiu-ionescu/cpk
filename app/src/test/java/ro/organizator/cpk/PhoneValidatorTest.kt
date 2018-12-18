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
        val phoneValidator = PhoneValidator()

        "Phone number with dashes as separators" {
            val permissions = "+31-613358857 Arnold \n" + " 0677935827 Croton +31-403-749-373"
            phoneValidator.isAllowed("+31613358857", permissions) shouldBe true
        }

        "Phone number with spaces" {
            val permissions = "+31 6 13358857 Arnold \n" + " 0677935827 Croton +31-403-749-373"
            phoneValidator.isAllowed("+31613358857", permissions) shouldBe true

        }
    }
}