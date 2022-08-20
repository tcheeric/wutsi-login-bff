package com.wutsi.application.login.endpoint.onboard.command

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.login.endpoint.onboard.dto.SaveCityRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/commands/save-city")
class SaveCityCommand : AbstractOnboardCommand() {
    @PostMapping
    fun submit(
        @Valid @RequestBody
        request: SaveCityRequest
    ): Action {
        service.saveCity(request)
        return gotoPage(Page.PIN)
    }
}
