package com.wutsi.application.login.endpoint.logout.command

import com.wutsi.application.login.endpoint.AbstractCommand
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.LogoutRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/logout")
class LogoutCommand(
    private val securityApi: WutsiSecurityApi
) : AbstractCommand() {
    @PostMapping
    fun index(@RequestBody request: com.wutsi.application.login.endpoint.logout.dto.LogoutRequest): Action {
        securityApi.logout(
            request = LogoutRequest(accessToken = request.accessToken)
        )
        return Action(
            type = ActionType.Route,
            url = "route:/~",
            replacement = true
        )
    }
}
