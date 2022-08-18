package com.wutsi.application.login.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.login.entity.AccountEntity
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.core.tracing.spring.SpringTracingRequestInterceptor
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.AuthenticationResponse
import com.wutsi.platform.tenant.WutsiTenantApi
import com.wutsi.platform.tenant.dto.GetTenantResponse
import com.wutsi.platform.tenant.dto.Logo
import com.wutsi.platform.tenant.dto.MobileCarrier
import com.wutsi.platform.tenant.dto.PhonePrefix
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.Cache
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.client.RestTemplate
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

abstract class AbstractEndpointTest {
    companion object {
        val DEVICE_ID = UUID.randomUUID().toString()
        const val LOCAL_PHONE_NUMBER = "15147550011"
        const val PHONE_NUMBER = "+$LOCAL_PHONE_NUMBER"
    }

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    protected lateinit var tracingContext: TracingContext

    @MockBean
    private lateinit var tenantApi: WutsiTenantApi

    @MockBean
    protected lateinit var cache: Cache

    @MockBean
    protected lateinit var securityApi: WutsiSecurityApi

    @MockBean
    protected lateinit var togglesProvider: TogglesProvider

    @Autowired
    private lateinit var messages: MessageSource

    protected lateinit var rest: RestTemplate

    protected lateinit var accessToken: String

    lateinit var traceId: String

    @BeforeTest
    open fun setUp() {
        traceId = UUID.randomUUID().toString()
        doReturn(DEVICE_ID).whenever(tracingContext).deviceId()
        doReturn(traceId).whenever(tracingContext).traceId()
        doReturn("1").whenever(tracingContext).tenantId()

        rest = RestTemplate()
        rest.interceptors.add(SpringTracingRequestInterceptor(tracingContext))

        val tenant = Tenant(
            id = 1,
            name = "test",
            logos = listOf(
                Logo(type = "PICTORIAL", url = "http://www.goole.com/images/1.png")
            ),
            countries = listOf("CM"),
            languages = listOf("en", "fr"),
            currency = "XAF",
            currencySymbol = "CFA",
            dateFormat = "dd MMM yyyy",
            timeFormat = "HH:mm",
            dateTimeFormat = "dd MMM yyyy, HH:mm",
            domainName = "www.wutsi.com",
            mobileCarriers = listOf(
                MobileCarrier(
                    code = "OM",
                    phonePrefixes = listOf(
                        PhonePrefix(
                            country = "CA",
                            prefixes = listOf("+1")
                        ),
                        PhonePrefix(
                            country = "CM",
                            prefixes = listOf("+237")
                        )
                    )
                )
            )
        )
        doReturn(GetTenantResponse(tenant)).whenever(tenantApi).getTenant(any())

        val account = AccountEntity(
            deviceId = DEVICE_ID,
            phoneNumber = "+15147550011",
            country = "CA",
            language = "en",
            displayName = "Ray Sponsible",
            pin = "123456",
            verificationId = 1111,
            cityId = 2225940L
        )
        doReturn(account).whenever(cache).get(eq(DEVICE_ID), eq(AccountEntity::class.java))

        accessToken = UUID.randomUUID().toString()
        val authResponse = AuthenticationResponse(
            accessToken = accessToken
        )
        doReturn(authResponse).whenever(securityApi).authenticate(any())
    }

    protected fun assertEndpointEquals(expectedPath: String, url: String) {
        val request = emptyMap<String, String>()
        val response = rest.postForEntity(url, request, Map::class.java)

        assertJsonEquals(expectedPath, response.body)
    }

    private fun assertJsonEquals(expectedPath: String, value: Any?) {
        val input = AbstractEndpointTest::class.java.getResourceAsStream(expectedPath)
        val expected = mapper.readValue(input, Any::class.java)

        val writer = mapper.writerWithDefaultPrettyPrinter()
        assertEquals(writer.writeValueAsString(expected), writer.writeValueAsString(value))
    }

    protected fun getText(key: String, args: Array<Any?> = emptyArray()) =
        messages.getMessage(key, args, LocaleContextHolder.getLocale()) ?: key
}
