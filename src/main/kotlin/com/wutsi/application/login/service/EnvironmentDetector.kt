package com.wutsi.application.login.service

import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

@Service
class EnvironmentDetector(
    private val env: Environment,
    private val request: HttpServletRequest
) {
    fun test(): Boolean =
        !prod()

    fun prod(): Boolean =
        env.acceptsProfiles(Profiles.of("prod"))

    fun version(): String =
        request.getHeader("X-Client-Version") ?: ""
}
