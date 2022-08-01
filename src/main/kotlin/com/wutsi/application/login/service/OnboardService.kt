package com.wutsi.application.login.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.login.endpoint.onboard.dto.SaveCityRequest
import com.wutsi.application.login.endpoint.onboard.dto.SavePinRequest
import com.wutsi.application.login.endpoint.onboard.dto.SaveProfileRequest
import com.wutsi.application.login.endpoint.onboard.dto.SendSmsCodeRequest
import com.wutsi.application.login.endpoint.onboard.dto.VerifySmsCodeRequest
import com.wutsi.application.login.entity.AccountEntity
import com.wutsi.application.login.exception.PhoneAlreadyAssignedException
import com.wutsi.application.login.exception.PinMismatchException
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.CreateAccountRequest
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType.PARAMETER_TYPE_HEADER
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.core.tracing.spring.RequestTracingContext
import com.wutsi.platform.core.util.URN
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.AuthenticationRequest
import com.wutsi.platform.sms.WutsiSmsApi
import com.wutsi.platform.sms.dto.SendVerificationRequest
import feign.FeignException
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.Cache
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
class OnboardService(
    private val smsApi: WutsiSmsApi,
    private val accountApi: WutsiAccountApi,
    private val securityApi: WutsiSecurityApi,
    private val logger: KVLogger,
    private val mapper: ObjectMapper,
    private val tracingContext: RequestTracingContext,
    private val countryDetector: CountryDetector,
    private val cache: Cache,

    @Value("\${wutsi.platform.security.api-key}") private val apiKey: String,
) {
    companion object {
        val ACCOUNT_ALREADY_ASSIGNED: String =
            com.wutsi.platform.account.error.ErrorURN.PHONE_NUMBER_ALREADY_ASSIGNED.urn
        val DEVICE_NOT_FOUND: String = URN.of("error", "app-onboard", "device-not-found").value
    }

    fun getState(): AccountEntity =
        cache.get(tracingContext.deviceId(), AccountEntity::class.java)
            ?: throw NotFoundException(
                error = Error(
                    code = DEVICE_NOT_FOUND,
                    parameter = Parameter(
                        name = TracingContext.HEADER_DEVICE_ID,
                        type = PARAMETER_TYPE_HEADER,
                        value = tracingContext.deviceId()
                    )
                )
            )

    private fun save(state: AccountEntity): AccountEntity {
        cache.put(tracingContext.deviceId(), state)
        return state
    }

    fun resendSmsCode() {
        val state = getState()
        try {
            sendSmsCode(SendSmsCodeRequest(state.phoneNumber))
        } finally {
            log(state)
        }
    }

    fun sendSmsCode(request: SendSmsCodeRequest) {
        val state = sendSmsCode(request.phoneNumber)
        log(state)
    }

    private fun sendSmsCode(phoneNumber: String): AccountEntity {
        val country = countryDetector.detect(phoneNumber)
        val language = LocaleContextHolder.getLocale().language

        // Send verification
        val verificationId = smsApi.sendVerification(
            SendVerificationRequest(
                phoneNumber = phoneNumber,
                language = language
            )
        ).id

        // Update state
        return save(
            AccountEntity(
                deviceId = tracingContext.deviceId(),
                phoneNumber = phoneNumber,
                country = country,
                language = language,
                verificationId = verificationId
            )
        )
    }

    fun verifyCode(request: VerifySmsCodeRequest) {
        val state = getState()
        try {
            smsApi.validateVerification(
                id = state.verificationId,
                code = request.code
            )

            if (findAccount(state) != null) {
                throw PhoneAlreadyAssignedException(state.phoneNumber)
            }
        } finally {
            log(state)
        }
    }

    fun createProfile(request: SaveProfileRequest) {
        val state = getState()
        try {
            state.displayName = request.displayName
            save(state)
        } finally {
            log(state)
        }
    }

    fun savePin(request: SavePinRequest) {
        val state = getState()
        try {
            state.pin = request.pin
            save(state)
        } finally {
            log(state)
        }
    }

    fun confirmPin(request: SavePinRequest) {
        val state = getState()
        try {
            if (state.pin != request.pin)
                throw PinMismatchException()
        } finally {
            log(state)
        }
    }

    fun saveCity(request: SaveCityRequest) {
        val state = getState()
        try {
            state.cityId = request.cityId
            save(state)
        } finally {
            log(state)
        }
    }

    fun createWallet(): String {
        val state = getState()
        try {
            val accountId = createAccount(state)
            logger.add("account_id", accountId)
            return authenticate(state)
        } catch (ex: FeignException) {
            val response = toErrorResponse(ex)
            if (response.error.code == ACCOUNT_ALREADY_ASSIGNED) {
                throw PhoneAlreadyAssignedException(state.phoneNumber)
            }
            throw ex
        } finally {
            log(state)
        }
    }

    private fun log(state: AccountEntity) {
        logger.add("phone_number", state.phoneNumber)
        logger.add("display_name", state.displayName)
        logger.add("country", state.country)
        logger.add("account_id", state.accountId)
        logger.add("city_id", state.cityId)
        logger.add("language", state.language)
        logger.add("payment_phone_number", state.paymentPhoneNumber)
        logger.add("verification_id", state.verificationId)
        logger.add("verification_id", state.pin?.let { "***" })
    }

    private fun findAccount(state: AccountEntity): AccountSummary? {
        val accounts = accountApi.searchAccount(SearchAccountRequest(phoneNumber = state.phoneNumber)).accounts
        return if (accounts.isNotEmpty())
            accounts[0]
        else
            null
    }

    private fun createAccount(state: AccountEntity): Long =
        accountApi.createAccount(
            CreateAccountRequest(
                phoneNumber = state.phoneNumber,
                displayName = state.displayName,
                language = state.language,
                country = state.country,
                password = state.pin,
                addPaymentMethod = true,
                cityId = state.cityId
            )
        ).id

    private fun authenticate(state: AccountEntity): String =
        securityApi.authenticate(
            AuthenticationRequest(
                type = "runas",
                phoneNumber = state.phoneNumber,
                apiKey = apiKey
            )
        ).accessToken

    private fun toErrorResponse(ex: FeignException): ErrorResponse {
        val buff = ex.responseBody().get()
        val bytes = ByteArray(buff.remaining())
        buff.get(bytes)
        return mapper.readValue(bytes, ErrorResponse::class.java)
    }
}
