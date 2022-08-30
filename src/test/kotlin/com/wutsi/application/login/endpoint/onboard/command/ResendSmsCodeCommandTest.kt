package com.wutsi.application.login.endpoint.onboard.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.entity.AccountEntity
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType.Prompt
import com.wutsi.flutter.sdui.enums.DialogType.Information
import com.wutsi.platform.sms.WutsiSmsApi
import com.wutsi.platform.sms.dto.SendVerificationResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["qa"])
internal class ResendSmsCodeCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var smsApi: WutsiSmsApi

    @Test
    fun submit() {
        val verificationId = 22222L
        doReturn(SendVerificationResponse(id = verificationId)).whenever(smsApi).sendVerification(any())

        val url = "http://localhost:$port/commands/resend-sms-code"
        val request = emptyMap<String, Any>()
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(Prompt, action.type)
        assertNotNull(action.prompt)
        assertEquals(Information.name, action.prompt?.attributes?.get("type"))

        val account = argumentCaptor<AccountEntity>()
        verify(cache).put(eq(DEVICE_ID), account.capture())
        kotlin.test.assertEquals(verificationId, account.firstValue.verificationId)
    }
}
