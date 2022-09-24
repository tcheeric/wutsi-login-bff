package com.wutsi.application.login.endpoint.onboard.screen

import com.wutsi.application.login.endpoint.AbstractQuery
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.flutter.sdui.Page
import com.wutsi.flutter.sdui.PageView
import com.wutsi.flutter.sdui.Screen
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/onboard")
class OnboardScreen(
    private val urlBuilder: URLBuilder
) : AbstractQuery() {
    @PostMapping
    fun index() = Screen(
        id = com.wutsi.application.login.endpoint.Page.ONBOARD,
        safe = true,
        appBar = null,
        child = PageView(
            children = listOf(
                Page(url = urlBuilder.build("pages/phone")),
                Page(url = urlBuilder.build("pages/verification")),
                Page(url = urlBuilder.build("pages/profile")),
                Page(url = urlBuilder.build("pages/city")),
                Page(url = urlBuilder.build("pages/pin")),
                Page(url = urlBuilder.build("pages/confirm-pin")),
                Page(url = urlBuilder.build("pages/final"))
            )
        )
    ).toWidget()
}
