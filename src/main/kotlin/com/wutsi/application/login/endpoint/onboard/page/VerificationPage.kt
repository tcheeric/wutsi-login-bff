package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.EnvironmentDetector
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.EnvironmentBanner
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.flutter.sdui.enums.InputType.Submit
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/pages/verification")
class VerificationPage(
    private val urlBuilder: URLBuilder,
    private val tenantProvider: TenantProvider,
    private val env: EnvironmentDetector,
    private val request: HttpServletRequest
) : AbstractOnboardQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val logo = tenantProvider.logo(tenant)
        return SingleChildScrollView(
            child = Column(
                children = listOfNotNull(
                    if (env.test()) {
                        EnvironmentBanner(env, request)
                    } else {
                        null
                    },

                    Container(
                        alignment = Center,
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
                                        caption = getText("page.verification.title"),
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
                                        caption = getText("page.verification.sub-title", arrayOf(getPhoneNumber())),
                                        alignment = TextAlignment.Center
                                    )
                                ),
                                Container(
                                    alignment = Center,
                                    child = Button(
                                        caption = getText("page.verification.change-number"),
                                        type = ButtonType.Text,
                                        action = gotoPage(Page.PHONE)
                                    )
                                ),
                                Form(
                                    children = listOf(
                                        Container(
                                            padding = 10.0,
                                            child = Input(
                                                id = "code",
                                                name = "code",
                                                type = InputType.Number,
                                                caption = getText("page.verification.field.code.caption"),
                                                required = true,
                                                hint = getText("page.verification.field.code.hint"),
                                                minLength = 6,
                                                maxLength = 6
                                            )
                                        ),
                                        Container(
                                            padding = 10.0,
                                            child = Input(
                                                id = "submit",
                                                name = "submit",
                                                type = Submit,
                                                caption = getText("page.verification.field.submit.caption"),
                                                action = Action(
                                                    type = Command,
                                                    url = urlBuilder.build("commands/verify-sms-code")
                                                )
                                            )
                                        ),
                                        Button(
                                            id = "resend-code",
                                            caption = getText("page.verification.resend-code"),
                                            type = ButtonType.Text,
                                            action = Action(
                                                type = Command,
                                                url = urlBuilder.build("commands/resend-sms-code")
                                            )
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
