package com.example.testproject

import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Exception

/**
 * Mismos tests que [LoginRepositoryTestMockito] y [LoginRepositoryTestMockitoKotlin] usando mockk
 */
class LoginRepositoryTestMockK {

    private val mockLoginApi = mockk<LoginApi> {
        // TODO: Comentar esta linea para ver que los tests fallan por no estar mockeado el metodo
        every { verifyLogin(any()) } just runs
        every { logIn("myemail@gmail.com", any(), any(), any()) } returns true
    }
    private val loginRepository = LoginRepository(mockLoginApi)

    @Test
    fun testLogin() {
        // Hago override del mock en el lambda
        every { mockLoginApi.logIn("myemail@gmail.com", any(), any(), any()) } returns false

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        verify {
            mockLoginApi.logIn(any(), "myHashedPassword", any(), any())
        }
    }

    @Test
    fun testLoginWithVerify() {
        every { mockLoginApi.verifyLogin(any()) } just runs

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        verify {
            mockLoginApi.verifyLogin("myemail@gmail.com")
        }
    }

    @Test
    fun testLoginWithMockedTokenFromObject() {
        assertEquals("old_user_token", PreferencesManager.getUserOldToken())
        mockkObject(PreferencesManager) {
            // Mockeo el metodo del object
            every { PreferencesManager.getUserOldToken() } returns "a_different_token"

            // Ejecuto el test
            loginRepository.login(User("myemail@gmail.com", "aPassword"))

            // No queremos validar este set
            excludeRecords { PreferencesManager.setUserToken(any()) }

            verifyAll {
                // Tenemos que verificar que se llamaron todos los metodos, si comentamos
                // una de las lineas falla el test
                PreferencesManager.getUserOldToken()
                mockLoginApi.logIn(any(), any(), "a_different_token", any())
                mockLoginApi.verifyLogin(any())
            }
        }
    }

    @Test
    fun testLoginWithMockedPhoneIdFromStatic() {
        assertEquals("PhoneId", Utils.getPhoneId())
        mockkStatic(Utils::class) {
            // Mockeo el metodo del static
            every { Utils.getPhoneId() } returns "TabletId"

            // Ejecuto el test
            loginRepository.login(User("myemail@gmail.com", "aPassword"))

            verifySequence {
                Utils.getPhoneId()
                mockLoginApi.logIn(any(), any(), any(), "TabletId")
                mockLoginApi.verifyLogin(any())
            }
        }
    }

    @Test
    fun testLogin_returningMultipleValues() {
        // Problema del uso de infix, no podemos poner enter antes de cada infix
        every { mockLoginApi.getUser() } returns mockk() andThen mockk<User>() andThenThrows
                Exception("Test Exception")

        for (i in 1..3) {
            try {
                loginRepository.getUser()
            } catch (e: Exception) {
                assertEquals("Test Exception", e.message!!)
            }
        }
    }

    @Test
    fun testLogin_returningMultipleValues_v2() {
        val users: List<User> = listOf(mockk(), mockk(), mockk())
        every { mockLoginApi.getUser() } returnsMany users

        // A lot nicer than needing to use a for
        users.forEach {
            assertEquals(it, loginRepository.getUser())
        }
    }

    @Test
    fun testLogin_usingCapture() {
        val emailSlot = slot<String>()
        every { mockLoginApi.logIn(capture(emailSlot), any(), any(), "PhoneId") } returns true

        loginRepository.login(User("anemail1@gmail.com", "password"))
        loginRepository.login(User("anemail2@gmail.com", "password"))
        loginRepository.login(User("anemail3@gmail.com", "password"))

        // Como vimos, el captured solo mantiene el ultimo valor de la llamada.
        // Si invertimos las lineas falla el test
        // assertEquals("anemail1@gmail.com", emailSlot.captured)
        assertEquals("anemail3@gmail.com", emailSlot.captured)
    }

    @Test
    fun testLogin_usingCaptureList() {
        val emailSlot = mutableListOf<String>()
        every { mockLoginApi.logIn(capture(emailSlot), any(), any(), "PhoneId") } returns true

        loginRepository.login(User("anemail1@gmail.com", "password"))
        loginRepository.login(User("anemail2@gmail.com", "password"))
        loginRepository.login(User("anemail3@gmail.com", "password"))

        // Como vimos, el captured solo mantiene el ultimo valor de la llamada.
        // Si invertimos las lineas falla el test
        assertEquals("anemail1@gmail.com", emailSlot.first())
        assertEquals("anemail3@gmail.com", emailSlot.last())
    }
}