package com.chandeh.app.data

/**
 * ثابت‌های منبع TGJU (هاست خارجی — روی اینترنت بین‌الملل در دسترس است).
 *
 * منبع اصلی: صفحه‌ی اصلی www.tgju.org (ساخت‌یافته، داخل <tr data-market-nameslug="...">)
 * منبع جایگزین: وب‌سرویس عمومی platform.tgju.org (گاهی 500 می‌دهد)
 */
object TgjuService {
    const val HOMEPAGE_URL = "https://www.tgju.org/"
    const val SNIPPET_URL =
        "http://platform.tgju.org/fa/api/webservice-snippet/?token=webservice&opts=diff,time&placeholder=tgju-data&items="
}
