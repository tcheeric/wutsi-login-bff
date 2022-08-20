package com.wutsi.application.login.endpoint.onboard.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.service.OnboardService
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.CreateAccountRequest
import com.wutsi.platform.account.dto.CreateAccountResponse
import feign.FeignException
import feign.Request
import feign.Request.HttpMethod.POST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import java.nio.charset.Charset

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateWalletCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var accountApi: WutsiAccountApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/create-wallet"
    }

    @Test
    fun submit() {
        val accountId = 7777L
        doReturn(CreateAccountResponse(accountId)).whenever(accountApi).createAccount(any())

        val response = rest.postForEntity(url, emptyMap<String, String>(), Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val request = argumentCaptor<CreateAccountRequest>()
        verify(accountApi).createAccount(request.capture())
        assertEquals("+15147550011", request.firstValue.phoneNumber)
        assertEquals("CA", request.firstValue.country)
        assertEquals("en", request.firstValue.language)
        assertEquals("Ray Sponsible", request.firstValue.displayName)
        assertEquals("123456", request.firstValue.password)
        assertEquals(true, request.firstValue.addPaymentMethod)
        assertEquals(2225940L, request.firstValue.cityId)
        assertNull(request.firstValue.pictureUrl)

        assertEquals(listOf("true"), response.headers["x-onboarded"])
        assertEquals(listOf(accessToken), response.headers["x-access-token"])

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/", action.url)
        assertEquals(true, action.replacement)
        assertNull(action.prompt)
    }

    @Test
    fun duplicateAccount() {
        doThrow(createException(OnboardService.ACCOUNT_ALREADY_ASSIGNED)).whenever(accountApi).createAccount(any())

        val response = rest.postForEntity(url, emptyMap<String, String>(), Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals(
            "http://localhost:0/?title=You+have+a+Wallet.&sub-title=Enter+your+PIN&phone=%2B15147550011&return-to-route=true&return-url=route%3A%2F&hide-change-account-button=true",
            action.url
        )
    }

    @Test
    fun unexpectedError() {
        doThrow(createException("unexpected-error")).whenever(accountApi).createAccount(any())

        val response = rest.postForEntity(url, emptyMap<String, String>(), Action::class.java)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
    }

    private fun createException(code: String): FeignException =
        FeignException.Conflict(
            "Yo",
            Request.create(POST, "xxx", emptyMap(), null, Charset.defaultCharset(), null),
            """
                {
                    "error":{
                        "code": "$code"
                    }
                }
            """.trimIndent().toByteArray(Charset.defaultCharset()),
            emptyMap()
        )
}
