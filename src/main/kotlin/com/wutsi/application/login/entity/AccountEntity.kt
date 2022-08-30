package com.wutsi.application.login.entity

import java.io.Serializable
import java.time.OffsetDateTime

data class AccountEntity(
    val deviceId: String = "",
    val phoneNumber: String = "",
    val otpToken: String = "",
    var displayName: String? = null,
    var pin: String? = null,
    var language: String = "",
    var country: String = "",
    var accountId: Long? = null,
    var cityId: Long? = null,

    var paymentPhoneNumber: String? = null,
    val created: OffsetDateTime = OffsetDateTime.now()
) : Serializable
