package com.wutsi.application.login.endpoint.onboard.page

import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.EnvironmentDetector
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.StringUtil
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.EnvironmentBanner
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.SearchableDropdown
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.InputType.Submit
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/pages/city")
class CityPage(
    private val urlBuilder: URLBuilder,
    private val tenantProvider: TenantProvider,
    private val cityService: CityService,
    private val sharedUIMapper: SharedUIMapper,
    private val env: EnvironmentDetector,
    private val request: HttpServletRequest
) : AbstractOnboardQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val state = service.getState()
        var country = state.country
        if (country.isNullOrEmpty()) {
            country = tenant.countries[0]
        }

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
                                    child = Text(
                                        caption = getText("page.city.title"),
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
                                        caption = getText("page.city.sub-title", arrayOf(getPhoneNumber())),
                                        alignment = TextAlignment.Center
                                    )
                                ),
                                Form(
                                    children = listOf(
                                        Container(
                                            padding = 10.0,
                                            child = SearchableDropdown(
                                                id = "city-id",
                                                name = "cityId",
                                                value = state.cityId?.toString(),
                                                children = cityService.search(null, tenant.countries)
                                                    .sortedBy {
                                                        StringUtil.unaccent(
                                                            sharedUIMapper.toLocationText(it, country).uppercase()
                                                        )
                                                    }
                                                    .map {
                                                        DropdownMenuItem(
                                                            caption = sharedUIMapper.toLocationText(it, country),
                                                            value = it.id.toString()
                                                        )
                                                    }
                                            )
                                        ),
                                        Container(
                                            padding = 10.0,
                                            child = Input(
                                                id = "submit",
                                                name = "submit",
                                                type = Submit,
                                                caption = getText("page.city.button.submit"),
                                                action = Action(
                                                    type = Command,
                                                    url = urlBuilder.build("commands/save-city")
                                                )
                                            )
                                        ),
                                        Button(
                                            id = "skip",
                                            caption = getText("page.city.button.skip"),
                                            type = ButtonType.Text,
                                            action = gotoPage(Page.PIN)
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
