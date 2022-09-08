package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.Alignment.TopCenter
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.InputType.Submit
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pages/profile")
class ProfilePage(
    private val urlBuilder: URLBuilder,
    private val tenantProvider: TenantProvider
) : AbstractOnboardQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val logo = tenantProvider.logo(tenant)
        return Column(
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
                            action = gotoPage(Page.VERIFICATION)
                        )
                    )
                ),
                Container(
                    alignment = TopCenter,
                    padding = 20.0,
                    child = Column(
                        children = listOf(
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
                                alignment = Center,
                                padding = 10.0,
                                child = Text(
                                    caption = getText("page.profile.title"),
                                    alignment = TextAlignment.Center,
                                    size = Theme.TEXT_SIZE_LARGE,
                                    color = Theme.COLOR_PRIMARY,
                                    bold = true
                                )

                            ),
                            Container(
                                alignment = Center,
                                padding = 10.0,
                                child = Text(
                                    caption = getText("page.profile.sub-title"),
                                    alignment = TextAlignment.Center
                                )
                            ),
                            Form(
                                children = listOf(
                                    Container(
                                        padding = 10.0,
                                        child = Input(
                                            id = "display-name",
                                            name = "displayName",
                                            caption = getText("page.profile.field.display-name.caption"),
                                            required = true,
                                            minLength = 5,
                                            maxLength = 50,
                                            hint = getText("page.profile.field.display-name.hint"),
                                            value = service.getState().displayName
                                        )
                                    ),
                                    Input(
                                        id = "submit",
                                        name = "submit",
                                        type = Submit,
                                        caption = getText("page.profile.field.submit.caption"),
                                        action = Action(
                                            type = Command,
                                            url = urlBuilder.build("commands/save-profile")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }
}
