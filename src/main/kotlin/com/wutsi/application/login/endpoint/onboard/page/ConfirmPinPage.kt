package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.PinWithKeyboard
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.Alignment.TopCenter
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pages/confirm-pin")
class ConfirmPinPage(
    private val urlBuilder: URLBuilder
) : AbstractOnboardQuery() {
    @PostMapping
    fun index() = Column(
        mainAxisAlignment = MainAxisAlignment.start,
        crossAxisAlignment = CrossAxisAlignment.start,
        children = listOfNotNull(
            Row(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    IconButton(
                        icon = Theme.ICON_ARROW_BACK,
                        color = Theme.COLOR_BLACK,
                        action = gotoPage(Page.PIN)
                    )
                )
            ),
            Container(
                alignment = Center,
                padding = 20.0,
                child = Column(
                    children = listOf(
                        Container(
                            alignment = Center,
                            child = Text(
                                caption = getText("page.confirm-pin.title"),
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
                                caption = getText("page.confirm-pin.sub-title"),
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
                                url = urlBuilder.build("commands/confirm-pin")
                            )
                        ),
                        Button(
                            id = "change-pin",
                            caption = getText("page.confirm-pin.field.change-pin.caption"),
                            type = ButtonType.Text,
                            action = gotoPage(Page.PIN)
                        )
                    )
                )
            )
        )
    ).toWidget()
}
