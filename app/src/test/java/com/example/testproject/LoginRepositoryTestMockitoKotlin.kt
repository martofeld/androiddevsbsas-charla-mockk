package com.example.testproject

import com.nhaarman.mockitokotlin2.*
import io.mockk.mockk
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.lang.Exception

/**
 * Mismos tests que [LoginRepositoryTestMockito] y [LoginRepositoryTestMockK] usando mockito-kotlin
 */
class LoginRepositoryTestMockitoKotlin {

    private val mockLoginApi = mock<LoginApi>()
    private val loginRepository = LoginRepository(mockLoginApi)

    @Test
    fun testLogin() {
        // Tenemos que usar whenever de mockito-kotlin
        whenever(
            mockLoginApi.logIn(
                eq("myemail@gmail.com"),
                any(),
                any(),
                Utils.getPhoneId()
            )
        ).thenReturn(true)

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        verify(mockLoginApi).logIn(any(), eq("myHashedPassword"), any(), Utils.getPhoneId())
    }

    @Test
    fun testLoginWithVerify() {
        // Cuando queremos mockear un Unit (void) mockito nos hace dar vuelta el orden de las llamadas
        // Usar when requiere comillas invertidas
        doNothing().whenever(mockLoginApi).verifyLogin(any())

        loginRepository.login(User("myemail@gmail.com", "myHashedPassword"))

        // La verificacion no es consistente con como hacemos los mocks
        verify(mockLoginApi).verifyLogin("myemail@gmail.com")
    }

    @Test
    @Ignore
    fun testLogin_withMockedToken_fromObject() {
        // TODO: No se puede hacer facilmente con mockito
        // https://stackoverflow.com/questions/37977320/how-to-mock-a-kotlin-singleton-object
        whenever(
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
        whenever(mockLoginApi.getUser())
            .thenReturn(mock())
            .thenReturn(mockk())
            .thenThrow(Exception())

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
        whenever(mockLoginApi.getUser())
            .thenReturn(mock(), mock(), mock(), mock())

        for (i in 1..3) {
            loginRepository.getUser()
        }
    }

    @Test
    fun testLogin_usingCapture_mockitoKotlin() {
        val captor = argumentCaptor<String>()
        `when`(
            mockLoginApi.logIn(
                captor.capture(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )
        ).thenReturn(true)

        loginRepository.login(User("anemail1@gmail.com", "password"))
        loginRepository.login(User("anemail2@gmail.com", "password"))
        loginRepository.login(User("anemail3@gmail.com", "password"))

        // El argument captor de mockito soporta directamente recibir guardar todas las llamadas
        Assert.assertEquals("anemail3@gmail.com", captor.thirdValue)
        Assert.assertEquals("anemail1@gmail.com", captor.allValues.first())
        Assert.assertEquals("anemail3@gmail.com", captor.allValues.last())
    }

    @Test
    fun testLogin_usingCapture_mockito() {
        // Argument capture tampoco funciona con mockito estandar, necesitamos mockito-kotlin
        // y wrappeamos el captor en un capture
        val captor = ArgumentCaptor.forClass(String::class.java)
        `when`(
            mockLoginApi.logIn(
                capture(captor),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )
        ).thenReturn(true)

        loginRepository.login(User("anemail1@gmail.com", "password"))
        loginRepository.login(User("anemail2@gmail.com", "password"))
        loginRepository.login(User("anemail3@gmail.com", "password"))

        // El argument captor de mockito soporta directamente recibir guardar todas las llamadas
        Assert.assertEquals("anemail3@gmail.com", captor.thirdValue)
        Assert.assertEquals("anemail1@gmail.com", captor.allValues.first())
        Assert.assertEquals("anemail3@gmail.com", captor.allValues.last())
    }

}