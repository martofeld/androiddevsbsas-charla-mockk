package com.example.testproject

// Could be created via dependency injection
class LoginRepository(private val api: LoginApi) {

    fun login(user: User): Boolean =
        api.logIn(user.email, user.password, PreferencesManager.getUserOldToken(), Utils.getPhoneId())
            .also { PreferencesManager.setUserToken("newToken") }
            .also { api.verifyLogin(user.email) }

    fun getUser(): User =
        api.getUser()

}

