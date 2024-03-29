package com.wutsi.application.login.endpoint.login.screen

import com.wutsi.application.login.endpoint.AbstractQuery
import com.wutsi.application.login.endpoint.Page
import com.wutsi.application.login.endpoint.onboard.screen.OnboardScreen
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.EnvironmentDetector
import com.wutsi.application.shared.service.StringUtil.initials
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.EnvironmentBanner
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.PinWithKeyboard
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType.Command
import com.wutsi.flutter.sdui.enums.ActionType.Route
import com.wutsi.flutter.sdui.enums.Alignment.Center
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.account.entity.AccountStatus
import com.wutsi.platform.account.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.tenant.entity.ToggleName
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping
class LoginScreen(
    private val urlBuilder: URLBuilder,
    private val accountApi: WutsiAccountApi,
    private val onboardScreen: OnboardScreen,
    private val env: EnvironmentDetector,
    private val request: HttpServletRequest
) : AbstractQuery() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(LoginScreen::class.java)
    }

    @PostMapping
    fun index(
        @RequestParam(name = "phone") phoneNumber: String,
        @RequestParam(name = "screen-id", required = false) screenId: String? = null,
        @RequestParam(name = "icon", required = false) icon: String? = null,
        @RequestParam(name = "title", required = false) title: String? = null,
        @RequestParam(name = "sub-title", required = false) subTitle: String? = null,
        @RequestParam(name = "return-url", required = false) returnUrl: String? = null,
        @RequestParam(name = "return-to-route", required = false, defaultValue = "true") returnToRoute: Boolean = true,
        @RequestParam(name = "auth", required = false, defaultValue = "true") auth: Boolean = true,
        @RequestParam(name = "dark-mode", required = false, defaultValue = "false") darkMode: Boolean = false,
        @RequestParam(name = "hide-back-button", required = false) hideBackButton: Boolean? = null,
        @RequestParam(
            name = "hide-change-account-button",
            required = false,
            defaultValue = "false"
        ) hideChangeAccountButton: Boolean = false
    ): Widget {
        if (icon != null) {
            LOGGER.warn("icon=$icon - icon parameter is not deprecated")
        }

        try {
            val account = findAccount(phoneNumber)
            val displayName = account.displayName ?: getText("page.login.no-name")
            val textColor = textColor(darkMode)
            val backgroundColor = backgroundColor(darkMode)
            logger.add("account_id", account.id)

            return Screen(
                id = screenId ?: Page.HOME,
                appBar = AppBar(
                    backgroundColor = backgroundColor,
                    foregroundColor = textColor,
                    elevation = 0.0,
                    title = title ?: getText("page.login.app-bar.title"),
                    automaticallyImplyLeading = hideBackButton?.let { !it }
                ),
                backgroundColor = backgroundColor,
                child = SingleChildScrollView(
                    child = Container(
                        alignment = Center,
                        child = Column(
                            children = listOfNotNull(
                                if (env.test()) {
                                    EnvironmentBanner(env, request)
                                } else {
                                    null
                                },

                                Container(
                                    alignment = Center,
                                    child = Row(
                                        mainAxisAlignment = MainAxisAlignment.center,
                                        children = listOf(
                                            Container(
                                                padding = 5.0,
                                                child = CircleAvatar(
                                                    radius = 16.0,
                                                    child = if (account.pictureUrl.isNullOrEmpty()) {
                                                        Text(
                                                            caption = initials(displayName),
                                                            color = textColor
                                                        )
                                                    } else {
                                                        Image(
                                                            url = account.pictureUrl!!
                                                        )
                                                    }
                                                )
                                            ),
                                            Container(
                                                padding = 5.0,
                                                child = Column(
                                                    children = listOf(
                                                        Text(
                                                            caption = displayName,
                                                            bold = true,
                                                            color = textColor
                                                        ),
                                                        Text(
                                                            caption = formattedPhoneNumber(
                                                                account.phone?.number,
                                                                account.phone?.country
                                                            )
                                                                ?: "",
                                                            color = textColor
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    alignment = Center,
                                    child = Text(
                                        caption = subTitle ?: getText("page.login.sub-title"),
                                        color = textColor,
                                        alignment = TextAlignment.Center,
                                        size = Theme.TEXT_SIZE_X_LARGE
                                    )
                                ),
                                Container(
                                    alignment = Center,
                                    child = PinWithKeyboard(
                                        id = "pin",
                                        name = "pin",
                                        hideText = true,
                                        maxLength = 6,
                                        keyboardButtonSize = 70.0,
                                        pinSize = 20.0,
                                        action = Action(
                                            type = Command,
                                            url = urlBuilder.build(
                                                submitUrl(
                                                    phoneNumber,
                                                    auth,
                                                    returnUrl,
                                                    returnToRoute
                                                )
                                            )
                                        ),
                                        color = textColor
                                    )
                                ),
                                if (
                                    auth &&
                                    !hideChangeAccountButton &&
                                    (account.superUser || togglesProvider.isToggleEnabled(ToggleName.SWITCH_ACCOUNT))
                                ) {
                                    Container(
                                        padding = 10.0,
                                        child = Button(
                                            id = "other-account",
                                            type = ButtonType.Text,
                                            caption = getText("page.login.button.another-account"),
                                            action = gotoUrl(
                                                url = urlBuilder.build("/onboard"),
                                                type = Route
                                            )
                                        )
                                    )
                                } else {
                                    null
                                }
                            )
                        )
                    )
                )
            ).toWidget()
        } catch (ex: NotFoundException) {
            LOGGER.warn("Unexpected error when logging in", ex)
            return onboardScreen.index()
        }
    }

    private fun textColor(darkMode: Boolean): String =
        if (darkMode) Theme.COLOR_WHITE else Theme.COLOR_BLACK

    private fun backgroundColor(darkMode: Boolean): String =
        if (darkMode) Theme.COLOR_PRIMARY else Theme.COLOR_WHITE

    private fun findAccount(phoneNumber: String): Account {
        val accounts = accountApi.searchAccount(
            request = SearchAccountRequest(
                status = AccountStatus.ACTIVE.name,
                phoneNumber = phoneNumber,
                limit = 1
            )
        ).accounts
        if (accounts.isEmpty()) {
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.ACCOUNT_NOT_FOUND.urn,
                    parameter = Parameter(
                        name = "phone",
                        type = ParameterType.PARAMETER_TYPE_PAYLOAD,
                        value = phoneNumber
                    )
                )
            )
        } else {
            return accountApi.getAccount(accounts[0].id).account
        }
    }

    private fun submitUrl(phoneNumber: String, auth: Boolean, returnUrl: String?, returnToRoute: Boolean): String {
        val url =
            "commands/login?auth=$auth&return-to-route=$returnToRoute&phone=" + URLEncoder.encode(phoneNumber, "utf-8")
        return if (returnUrl == null) {
            url
        } else {
            url + "&return-url=" + URLEncoder.encode(returnUrl, "utf-8")
        }
    }
}
