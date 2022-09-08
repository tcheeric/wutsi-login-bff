package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.Alignment.TopCenter
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pages/final")
class FinalPage(
    private val tenantProvider: TenantProvider,
    private val urlBuilder: URLBuilder
) : AbstractOnboardQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val logo = tenantProvider.logo(tenant)
        val state = service.getState()
        return Column(
            children = listOf(
                Row(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
                        IconButton(
                            icon = Theme.ICON_ARROW_BACK,
                            color = Theme.COLOR_BLACK,
                            action = gotoPage(Page.CONFIRM_PIN)
                        )
                    )
                ),
                Container(
                    alignment = Center,
                    padding = 20.0,
                    child = Column(
                        children = listOfNotNull(
                            Container(
                                alignment = Center,
                                padding = 10.0,
                                child = logo?.let {
                                    Image(
                                        url = it,
                                        width = 128.0,
                                        height = 128.0
                                    )
                                }
                            ),
                            Container(
                                alignment = TopCenter,
                                child = Text(
                                    caption = state.displayName ?: "",
                                    alignment = TextAlignment.Center,
                                    size = Theme.TEXT_SIZE_LARGE,
                                    color = Theme.COLOR_PRIMARY,
                                    bold = true
                                )
                            ),
                            Container(
                                alignment = TopCenter,
                                child = Text(
                                    caption = getPhoneNumber(),
                                    alignment = TextAlignment.Center,
                                    size = Theme.TEXT_SIZE_LARGE
                                )
                            ),
                            Container(
                                padding = 20.0
                            ),
                            Button(
                                id = "create-wallet",
                                caption = getText("page.final.field.submit.caption"),
                                action = Action(
                                    type = Command,
                                    url = urlBuilder.build("commands/create-wallet")
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }
}
