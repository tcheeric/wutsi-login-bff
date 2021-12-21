package com.wutsi.application.login.endpoint.login.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.account.dto.SearchAccountResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class LoginScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @MockBean
    private lateinit var accountApi: WutsiAccountApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port?phone=+5147580000"

        val accounts = listOf(
            AccountSummary(
                id = 1,
                displayName = "Ray Sponsible",
                country = "CM",
                language = "en",
                status = "ACTIVE",
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
    fun defaultLoginScreen() = assertEndpointEquals("/screens/login.json", url)

    @Test
    fun customLoginScreen() {
        url =
            "http://localhost:$port?screen-id=test&auth=false&phone=+5147580000&title=Foo&sub-title=Yo+Man&icon=i_c_o_n&return-to-route=false&return-url=https://www.google.ca"
        assertEndpointEquals("/screens/login-custom.json", url)
    }
}
