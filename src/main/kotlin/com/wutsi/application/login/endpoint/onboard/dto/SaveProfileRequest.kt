package com.wutsi.application.login.endpoint.onboard.dto

import javax.validation.constraints.NotEmpty

data class SaveProfileRequest(
    @NotEmpty val displayName: String = ""
)
