package com.example.testproject

// This could be an interface from retrofit!
interface LoginApi {

    fun logIn(email: String, password: String, userToken: String, phoneId: String): Boolean

    fun verifyLogin(email: String)

    fun getUser(): User

}