package com.wutsi.application.login.endpoint.login.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.service.EnvironmentDetector
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.account.dto.SearchAccountResponse
import com.wutsi.platform.tenant.entity.ToggleName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class LoginScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var env: EnvironmentDetector

    @MockBean
    private lateinit var accountApi: WutsiAccountApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val accounts = listOf(
            AccountSummary(
                id = 1,
                displayName = "Ray Sponsible",
                country = "CM",
                language = "en",
                status = "ACTIVE"
            )
        )
        doReturn(SearchAccountResponse(accounts)).whenever(accountApi).searchAccount(any())

        val account = Account(
            id = 1,
            displayName = "Ray Sponsible",
            country = "CM",
            language = "en",
            status = "ACTIVE",
            pictureUrl = "https://me.com/1203920.png"
        )
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(any())
    }

    @Test
    fun defaultLoginScreen() {
        val url = "http://localhost:$port?phone=+5147580000"
        assertEndpointEquals("/screens/login.json", url)
    }

    @Test
    fun customLoginScreen() {
        val url =
            "http://localhost:$port?screen-id=test&auth=false&phone=+5147580000&title=Foo&sub-title=Yo+Man&icon=i_c_o_n&return-to-route=false&return-url=https://www.google.ca&dark-mode=true"
        assertEndpointEquals("/screens/login-custom.json", url)
    }

    @Test
    fun hideBackButton() {
        val url = "http://localhost:$port?phone=+5147580000&hide-back-button=true"
        assertEndpointEquals("/screens/hide-back-button.json", url)
    }

    @Test
    fun showChangeAccountButton() {
        doReturn(true).whenever(togglesProvider).isToggleEnabled(ToggleName.SWITCH_ACCOUNT)

        val url = "http://localhost:$port?phone=+5147580000&auth=true"
        assertEndpointEquals("/screens/login-show-change-account-button.json", url)
    }

    @Test
    fun superUser() {
        val superUser = Account(
            id = 1,
            displayName = "Ray Sponsible",
            country = "CM",
            language = "en",
            status = "ACTIVE",
            pictureUrl = "https://me.com/1203920.png",
            superUser = true
        )
        doReturn(GetAccountResponse(superUser)).whenever(accountApi).getAccount(any())

        val url = "http://localhost:$port?phone=+5147580000&auth=true"
        assertEndpointEquals("/screens/login-super-user.json", url)
    }

    @Test
    fun hideChangeAccountButton() {
        doReturn(true).whenever(togglesProvider).isToggleEnabled(ToggleName.SWITCH_ACCOUNT)

        val url = "http://localhost:$port?phone=+5147580000&auth=true&hide-change-account-button=true"
        assertEndpointEquals("/screens/login-hide-change-account-button.json", url)
    }

    @Test
    fun testEnvironment() {
        doReturn(true).whenever(env).test()
        doReturn("1.0.0.110").whenever(env).version()

        val url = "http://localhost:$port?phone=+5147580000"
        assertEndpointEquals("/screens/login-test-env.json", url)
    }
}
