package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.login.endpoint.AbstractEndpointTest
import com.wutsi.application.shared.service.EnvironmentDetector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FinalPageTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var env: EnvironmentDetector

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        url = "http://localhost:$port/pages/final"
    }

    @Test
    fun index() = assertEndpointEquals("/pages/final.json", url)
}
