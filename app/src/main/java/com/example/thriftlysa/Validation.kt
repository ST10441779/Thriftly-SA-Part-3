package com.example.thriftlysa

object Validation {

    fun validEmail(email: String): Boolean {

        return android.util.Patterns
            .EMAIL_ADDRESS
            .matcher(email)
            .matches()
    }

    fun validPassword(
        password: String
    ): Boolean {

        return password.length >= 6
    }

    fun validPrice(
        price: String
    ): Boolean {

        val amount =
            price.toDoubleOrNull()

        return amount != null &&
                amount > 0
    }

    fun validListing(
        title: String,
        description: String,
        category: String,
        condition: String,
        price: String,
        location: String
    ): Boolean {

        return title.isNotBlank() &&
                description.isNotBlank() &&
                category.isNotBlank() &&
                condition.isNotBlank() &&
                validPrice(price) &&
                location.isNotBlank()
    }
}