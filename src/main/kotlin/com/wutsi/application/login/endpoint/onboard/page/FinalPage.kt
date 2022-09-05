package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.EnvironmentDetector
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.EnvironmentBanner
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.Alignment.TopCenter
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/pages/final")
class FinalPage(
    private val tenantProvider: TenantProvider,
    private val urlBuilder: URLBuilder,
    private val env: EnvironmentDetector,
    private val request: HttpServletRequest
) : AbstractOnboardQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val logo = tenantProvider.logo(tenant)
        val state = service.getState()
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
            )
        ).toWidget()
    }
}
