package com.abchina.http.utils;

import com.abchina.http.session.Cookies;

public class CookiesUtils {
    public static Cookies parse(String rawCookie) {
        String[] cookieArray = rawCookie.split("; ");
        Cookies cookies = new Cookies();
        for (int i = 0; i < cookieArray.length; i++) {
            String cookie = cookieArray[i];
            String[] c = cookie.split("=");
            cookies.put(c[0], c[1]);
        }
        return cookies;
    }
}
