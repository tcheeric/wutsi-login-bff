[![](https://github.com/wutsi/wutsi-login-bff/actions/workflows/master.yml/badge.svg)](https://github.com/wutsi/wutsi-login-bff/actions/workflows/master.yml)

[![JDK](https://img.shields.io/badge/jdk-11-brightgreen.svg)](https://jdk.java.net/11/)
[![](https://img.shields.io/badge/maven-3.6-brightgreen.svg)](https://maven.apache.org/download.cgi)
![](https://img.shields.io/badge/language-kotlin-blue.svg)

# wutsi-login-bff

`wutsi-login-bff` provide the experience:

- Signing user
- Verifying the user's PIN
- Onboard new user

# Screens

## Login Screen

This is the endpoint for either signing in users or verifying their PIN.

**Endpoint:** `/`

#### Request Parameters

| Parameters                 | Default Value | Description                                                                                                                                                 |
|----------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| phone-number               |               | REQUIRED: Phone number of the user to authenticate in [E.164](https://en.wikipedia.org/wiki/E.164) format                                                   |
| screen-id                  | `page.login`  | Screen identifier                                                                                                                                           |
| icon                       |               | Code of the icon to display in the title. See [Material Icons](https://github.com/flutter/flutter/blob/master/packages/flutter/lib/src/material/icons.dart) |
| title                      |               | Main title                                                                                                                                                  |
| sub-title                  |               | Sub Title                                                                                                                                                   |
| return-url                 |               | URL where to redirect the user after completing the validation                                                                                              |
| return-to-route            | `true`        | Indicate if the return-url is a `Route` or `Command`.                                                                                                       |
| auth                       | `true`        | If `true` a new authentication token will be created after validating the user PIN                                                                          |
| dark-mode                  | `false`       | If `true` the login screen will be displayed in dark mode                                                                                                   |
| hide-back-button           | `false`       | If `true` the back button in the application bar will be hidden                                                                                             |
| show-change-account-button | `false`       | If `true` the button `Change Account` will be displayed                                                                                                     |

## Onboard Screen

This is the endpoint onboard new user into the app.

**Endpoint:** `/onboard`
