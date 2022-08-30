package com.wutsi.application.login.endpoint.onboard.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.endpoint.onboard.dto.SendSmsCodeRequest
import com.wutsi.application.login.entity.AccountEntity
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.sms.WutsiSmsApi
import com.wutsi.platform.sms.dto.SendVerificationResponse
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["qa"])
internal class SendSmsCodeCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var smsApi: WutsiSmsApi

    @Test
    fun submit() {
        val verificationId = 1111L
        doReturn(SendVerificationResponse(id = verificationId)).whenever(smsApi).sendVerification(any())

        val url = "http://localhost:$port/commands/send-sms-code"
        val request = SendSmsCodeRequest(
            phoneNumber = PHONE_NUMBER
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Page, action.type)
        assertEquals("page:/1", action.url)
        assertNull(action.prompt)

        val account = argumentCaptor<AccountEntity>()
        verify(cache).put(eq(DEVICE_ID), account.capture())
        assertEquals(request.phoneNumber, account.firstValue.phoneNumber)
        assertEquals("CA", account.firstValue.country)
        assertEquals("en", account.firstValue.language)
        assertEquals(verificationId, account.firstValue.verificationId)
    }
}
