package com.wutsi.application.login.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.login.endpoint.login.dto.LoginRequest
import com.wutsi.application.login.exception.AuthenticationException
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.AuthenticationRequest
import feign.FeignException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val accountApi: WutsiAccountApi,
    private val securityApi: WutsiSecurityApi,
    private val logger: KVLogger,
    private val mapper: ObjectMapper,

    @Value("\${wutsi.platform.security.api-key}") private val apiKey: String
) {
    fun login(phoneNumber: String, auth: Boolean, request: LoginRequest): String? {
        logger.add("phone_number", phoneNumber)
        logger.add("auth", auth)
        try {
            // Check password
            val account = findAccount(phoneNumber)
            accountApi.checkPassword(account.id, request.pin)

            // Authenticate
            return if (auth) {
                authenticate(phoneNumber)
            } else {
                null
            }
        } catch (ex: FeignException) {
            val response = toErrorResponse(ex)
            throw AuthenticationException("Authentication failed", response?.error)
        }
    }

    private fun findAccount(phoneNumber: String): AccountSummary {
        val accounts = accountApi.searchAccount(
            SearchAccountRequest(
                phoneNumber = phoneNumber
            )
        ).accounts
        if (accounts.isNotEmpty()) {
            val account = accounts[0]
            if (account.status != "ACTIVE") {
                throw AuthenticationException("Account not active")
            }
            return account
        } else {
            throw AuthenticationException("Account not found")
        }
    }

    private fun authenticate(phoneNumber: String): String {
        val accessToken = securityApi.authenticate(
            AuthenticationRequest(
                type = "runas",
                phoneNumber = phoneNumber,
                apiKey = apiKey
            )
        ).accessToken
        logger.add("access_token", "***")

        return accessToken
    }

    private fun toErrorResponse(ex: FeignException): ErrorResponse? =
        try {
            mapper.readValue(ex.contentUTF8(), ErrorResponse::class.java)
        } catch (ex: Exception) {
            null
        }
}
