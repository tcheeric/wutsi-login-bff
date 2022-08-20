package com.wutsi.application.login.endpoint.onboard.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

data class SavePinRequest(
    @NotEmpty
    @Min(6)
    @Max(6)
    val pin: String = ""
)
