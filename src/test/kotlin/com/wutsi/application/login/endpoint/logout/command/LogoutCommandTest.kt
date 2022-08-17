package com.wutsi.application.login.endpoint.logout.command

import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.login.endpoint.logout.dto.LogoutRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class LogoutCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/logout"
    }

    @Test
    fun submit() {
        // WHEN
        val request = LogoutRequest(accessToken = "xxx")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(securityApi).logout(com.wutsi.platform.security.dto.LogoutRequest(request.accessToken))

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/~", action.url)
        assertEquals(true, action.replacement)
    }
}
