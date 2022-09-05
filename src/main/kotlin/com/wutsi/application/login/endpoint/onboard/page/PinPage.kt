package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.EnvironmentDetector
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.EnvironmentBanner
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.PinWithKeyboard
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.Alignment.TopCenter
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/pages/pin")
class PinPage(
    private val urlBuilder: URLBuilder,
    private val env: EnvironmentDetector,
    private val request: HttpServletRequest
) : AbstractOnboardQuery() {
    @PostMapping
    fun index() = Container(
        alignment = Center,
        padding = 20.0,
        child = SingleChildScrollView(
            child = Column(
                children = listOfNotNull(
                    if (env.test()) {
                        EnvironmentBanner(env, request)
                    } else {
                        null
                    },

                    Container(
                        alignment = Center,
                        child = Text(
                            caption = getText("page.pin.title"),
                            alignment = TextAlignment.Center,
                            size = Theme.TEXT_SIZE_LARGE,
                            color = Theme.COLOR_PRIMARY,
                            bold = true
                        )
                    ),
                    Container(
                        alignment = TopCenter,
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.pin.sub-title"),
                            alignment = TextAlignment.Center
                        )
                    ),
                    PinWithKeyboard(
                        id = "pin",
                        name = "pin",
                        hideText = true,
                        maxLength = 6,
                        pinSize = 20.0,
                        action = Action(
                            type = Command,
                            url = urlBuilder.build("commands/save-pin")
                        )
                    )
                )
            )
        )
    ).toWidget()
}
