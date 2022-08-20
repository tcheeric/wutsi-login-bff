package com.wutsi.application.login.endpoint.onboard.command

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.login.endpoint.onboard.dto.VerifySmsCodeRequest
import com.wutsi.flutter.sdui.Action
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("commands/verify-sms-code")
class VerifySmsCodeCommand : AbstractOnboardCommand() {
    @PostMapping
    fun submit(
        @Valid @RequestBody
        request: VerifySmsCodeRequest
    ): Action {
        try {
            service.verifyCode(request)
            return gotoPage(Page.PROFILE)
        } catch (e: FeignException.Conflict) {
            return promptError("message.error.sms-verification-failed")
        }
    }
}
