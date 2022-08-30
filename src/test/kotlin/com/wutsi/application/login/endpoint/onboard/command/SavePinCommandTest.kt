package com.wutsi.application.login.endpoint.onboard.command

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.endpoint.onboard.dto.SavePinRequest
import com.wutsi.application.login.entity.AccountEntity
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType.Page
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SavePinCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun submit() {
        val url = "http://localhost:$port/commands/save-pin"
        val request = SavePinRequest(
            pin = "123456"
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(Page, action.type)
        assertEquals("page:/${com.wutsi.application.login.endpoint.Page.CONFIRM_PIN}", action.url)
        assertNull(action.prompt)

        val account = argumentCaptor<AccountEntity>()
        verify(cache).put(eq(DEVICE_ID), account.capture())
        assertEquals(request.pin, account.firstValue.pin)
    }
}
