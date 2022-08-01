package com.wutsi.application.login.endpoint.login.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.endpoint.login.dto.LoginRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountResponse
import feign.FeignException
import feign.Request
import feign.Request.HttpMethod.POST
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URLEncoder
import java.nio.charset.Charset

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class LoginCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var accountApi: WutsiAccountApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/login?phone=" + URLEncoder.encode(PHONE_NUMBER, "utf-8")
    }

    @Test
    fun submit() {
        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "ACTIVE")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        // WHEN
        val request = LoginRequest(pin = "123456")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        assertEquals(listOf(accessToken), response.headers["x-access-token"])

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/", action.url)
        assertEquals(true, action.replacement)
    }

    @Test
    fun submitWithLocalPhone() {
        url = "http://localhost:$port/commands/login?phone=$LOCAL_PHONE_NUMBER"

        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "ACTIVE")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        // WHEN
        val request = LoginRequest(pin = "123456")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        assertEquals(listOf(accessToken), response.headers["x-access-token"])

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/", action.url)
        assertEquals(true, action.replacement)
    }

    @Test
    fun submitWithPhoneHavingSpace() {
        url = "http://localhost:$port/commands/login?phone=$PHONE_NUMBER"

        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "ACTIVE")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        // WHEN
        val request = LoginRequest(pin = "123456")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        assertEquals(listOf(accessToken), response.headers["x-access-token"])

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/", action.url)
        assertEquals(true, action.replacement)
    }

    @Test
    fun invalidPassword() {
        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "ACTIVE")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        val ex = createFeignException("xxx")
        doThrow(ex).whenever(accountApi).checkPassword(any(), any())

        // WHEN
        val request = LoginRequest(pin = "777777")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
        assertEquals(getText("message.error.login-failed"), action.prompt?.attributes?.get("message"))
    }

    @Test
    fun authenticationFailed() {
        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "ACTIVE")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        val ex = createFeignException("xxx")
        doThrow(ex).whenever(securityApi).authenticate(any())

        // WHEN
        val request = LoginRequest(pin = "777777")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
        assertEquals(getText("message.error.login-failed"), action.prompt?.attributes?.get("message"))
    }

    @Test
    fun accountNotFound() {
        // GIVEN
        doReturn(SearchAccountResponse(emptyList())).whenever(accountApi).searchAccount(any())

        // WHEN
        val request = LoginRequest(pin = "777777")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
    }

    @Test
    fun accountNotActive() {
        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "SUSPENDED")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        // WHEN
        val request = LoginRequest(pin = "123456")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(DialogType.Error.name, action.prompt?.attributes?.get("type"))
    }

    @Test
    fun verifyPasswordOnlyWithURL() {
        // GIVEN
        val account = AccountSummary(id = System.currentTimeMillis(), status = "ACTIVE")
        doReturn(SearchAccountResponse(listOf(account))).whenever(accountApi).searchAccount(any())

        // WHEN
        val request = LoginRequest(pin = "123456")
        val xphone = URLEncoder.encode(PHONE_NUMBER, "utf-8")
        url =
            "http://localhost:$port/commands/login?auth=false&return-to-route=false&return-url=https://www.google.ca&phone=$xphone"
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        assertFalse(response.headers.keys.contains("x-access-token"))
        verify(securityApi, never()).authenticate(any())

        val action = response.body!!
        assertEquals(ActionType.Command, action.type)
        assertEquals("https://www.google.ca", action.url)
        assertNull(action.replacement)
    }

    private fun createFeignException(errorCode: String) = FeignException.Conflict(
        "",
        Request.create(
            POST,
            "https://www.google.ca",
            emptyMap(),
            "".toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        ),
        """
            {
                "error":{
                    "code": "$errorCode",
                }
            }
        """.trimIndent().toByteArray(),
        emptyMap()
    )
}
