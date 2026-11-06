package com.example.thriftlysa

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test fun acceptsValidEmail() {
        assertTrue(Validation.validEmail("seller@example.com"))
    }

    @Test fun rejectsInvalidEmail() {
        assertFalse(Validation.validEmail("not-an-email"))
    }

    @Test fun requiresMinimumPasswordLength() {
        assertTrue(Validation.validPassword("secure123"))
        assertFalse(Validation.validPassword("123"))
    }

    @Test fun acceptsOnlyPositiveNumericPrices() {
        assertTrue(Validation.validPrice("125.50"))
        assertFalse(Validation.validPrice("0"))
        assertFalse(Validation.validPrice("-20"))
        assertFalse(Validation.validPrice("abc"))
    }

    @Test fun listingRequiresAllFields() {
        assertTrue(Validation.validListing("Jacket", "Good condition", "Clothing",
            "Good", "150", "Cape Town"))
        assertFalse(Validation.validListing("", "Good condition", "Clothing",
            "Good", "150", "Cape Town"))
    }
}
