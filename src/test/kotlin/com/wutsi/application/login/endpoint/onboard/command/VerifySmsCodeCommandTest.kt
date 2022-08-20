package com.wutsi.application.login.endpoint.onboard.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.endpoint.onboard.dto.VerifySmsCodeRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType.Page
import com.wutsi.flutter.sdui.enums.ActionType.Prompt
import com.wutsi.flutter.sdui.enums.ActionType.Route
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountResponse
import com.wutsi.platform.sms.WutsiSmsApi
import feign.FeignException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["qa"])
internal class VerifySmsCodeCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var smsApi: WutsiSmsApi

    @MockBean
    private lateinit var accountApi: WutsiAccountApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/verify-sms-code"

        doReturn(SearchAccountResponse()).whenever(accountApi).searchAccount(any())
    }

    @Test
    fun submit() {
        val request = VerifySmsCodeRequest(code = "000000")
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(Page, action.type)
        assertEquals("page:/${com.wutsi.application.login.endpoint.Page.PROFILE}", action.url)
        assertNull(action.prompt)
    }

    @Test
    fun withExistingAccount() {
        doReturn(SearchAccountResponse(listOf(AccountSummary(id = 777L)))).whenever(accountApi)
            .searchAccount(any())

        val request = VerifySmsCodeRequest(code = "000000")
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(Route, action.type)
        assertEquals(
            "http://localhost:0/?title=You+have+a+Wallet.&sub-title=Enter+your+PIN&phone=%2B15147550011&return-to-route=true&return-url=route%3A%2F&hide-change-account-button=true",
            action.url
        )
        assertNull(action.prompt)
    }

    @Test
    fun verificationFailed() {
        doThrow(FeignException.Conflict::class).whenever(smsApi).validateVerification(any(), any())

        val request = VerifySmsCodeRequest(code = "000000")
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(Prompt, action.type)
        assertNotNull(action.prompt)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
    }

    @Test
    fun invalidDevice() {
        doReturn("??????").whenever(tracingContext).deviceId()

        val request = VerifySmsCodeRequest(code = "000000")
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(Page, action.type)
        assertEquals("page:/${com.wutsi.application.login.endpoint.Page.PHONE}", action.url)
    }
}
