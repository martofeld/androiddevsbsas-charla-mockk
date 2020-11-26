package com.example.testproject

import io.mockk.every
import io.mockk.slot
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.lang.Exception
import java.lang.RuntimeException

/**
 * Mismos tests que [LoginRepositoryTestMockitoKotlin] y [LoginRepositoryTestMockK] usando mockito puro
 */
class LoginRepositoryTestMockito {

    private val mockLoginApi = mock(LoginApi::class.java)
    private val loginRepository = LoginRepository(mockLoginApi)

    @Test
    fun testLogin() {
        // Este test falla porque el eq de mockito devuelve null y kotlin lo especifica como non-null.
        // Otro de los problemas de usar mockito. La unica solucion es usar mockito-kotlin
        `when`(
            mockLoginApi.logIn(
                eq("myemail@gmail.com"),
                anyString(),
                anyString(),
                anyString()
            )
        ).thenReturn(true)

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        verify(mockLoginApi).logIn(any(), eq("myHashedPassword"), any(), Utils.getPhoneId())
    }

    @Test
    fun testLoginWithVerify() {
        // Cuando queremos mockear un Unit (void) mockito nos hace dar vuelta el orden de las llamadas
        // Usar when requiere comillas invertidas
        doNothing().`when`(mockLoginApi).verifyLogin(any())

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        // La verificacion no es consistente con como hacemos los mocks
        verify(mockLoginApi).verifyLogin("myemail@gmail.com")
    }

    @Test
    @Ignore
    fun testLogin_withMockedToken_fromObject() {
        // TODO: No se puede hacer facilmente con mockito
        // https://stackoverflow.com/questions/37977320/how-to-mock-a-kotlin-singleton-object
        `when`(
            mockLoginApi.logIn(
                eq("myemail@gmail.com"),
                any(),
                any(),
                Utils.getPhoneId()
            )
        ).thenReturn(true)

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        verify(mockLoginApi).logIn(
            any(),
            eq("myHashedPassword"),
            eq("A different token"),
            Utils.getPhoneId()
        )
    }

    @Test
    fun testLogin_returningMultipleValues() {
        `when`(mockLoginApi.getUser())
            .thenReturn(mock(User::class.java))
            .thenReturn(mock(User::class.java))
            .thenThrow(RuntimeException("Test Exception"))

        for (i in 1..3) {
            try {
                loginRepository.getUser()
            } catch (e: Exception) {
                Assert.assertEquals("Test Exception", e.message!!)
            }
        }
    }

    @Test
    fun testLogin_returningMultipleValues_v2() {
        `when`(mockLoginApi.getUser())
            .thenReturn(
                mock(User::class.java),
                mock(User::class.java),
                mock(User::class.java),
                mock(User::class.java)
            )

        for (i in 1..3) {
            loginRepository.getUser()
        }
    }

    @Test
    fun testLogin_usingCapture() {
        // Argument capture tampoco funciona con mockito estandar, necesitamos mockito-kotlin
        val captor = ArgumentCaptor.forClass(String::class.java)
        `when`(
            mockLoginApi.logIn(
                captor.capture(),
                anyString(),
                anyString(),
                anyString()
            )
        ).thenReturn(true)

        loginRepository.login(User("anemail1@gmail.com", "password"))
        loginRepository.login(User("anemail2@gmail.com", "password"))
        loginRepository.login(User("anemail3@gmail.com", "password"))

        // Como vimos, el captured solo mantiene el ultimo valor de la llamada.
        // Si invertimos las lineas falla el test
        // assertEquals("anemail1@gmail.com", captor.value)
        assertEquals("anemail3@gmail.com", captor.value)
    }

    @Test
    fun testLogin_usingCaptureList() {
        // Argument capture tampoco funciona con mockito estandar, necesitamos mockito-kotlin
        val captor = ArgumentCaptor.forClass(String::class.java)
        `when`(
            mockLoginApi.logIn(
                captor.capture(),
                anyString(),
                anyString(),
                anyString()
            )
        ).thenReturn(true)

        loginRepository.login(User("anemail1@gmail.com", "password"))
        loginRepository.login(User("anemail2@gmail.com", "password"))
        loginRepository.login(User("anemail3@gmail.com", "password"))

        // Como vimos, el captured solo mantiene el ultimo valor de la llamada.
        // Si invertimos las lineas falla el test
        assertEquals("anemail1@gmail.com", captor.allValues.first())
        assertEquals("anemail3@gmail.com", captor.allValues.last())
    }

}