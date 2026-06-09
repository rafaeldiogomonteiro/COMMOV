package com.example.commov.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginModelTest {
    @Test
    fun withEmail_updatesEmailOnly() {
        val model = LoginModel("", "secret", false)

        val updated = model.withEmail("user@commov.local")

        assertEquals("user@commov.local", updated.email)
        assertEquals("secret", updated.password)
        assertFalse(updated.passwordVisible)
    }

    @Test
    fun withPassword_andVisibility_updateIndependently() {
        val model = LoginModel("user@commov.local", "", false)

        val withPassword = model.withPassword("admin123")
        val withVisibility = withPassword.withPasswordVisible(true)

        assertEquals("admin123", withPassword.password)
        assertTrue(withVisibility.passwordVisible)
    }
}
