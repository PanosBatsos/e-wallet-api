package com.ewallet.api.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    /**
     * Creates and adds an HttpOnly cookie to the HTTP response
     * @param response The HttpServletResponse to add the cookie to
     * @param name The name of the cookie
     * @param value The token of the cookie
     * @param maxAgeSeconds Duration of cookie
     */
    public void addHttpOnlyCookie(HttpServletResponse response , String name , String value , int maxAgeSeconds) {
        Cookie cookie = new Cookie(name , value);
        cookie.setHttpOnly(true); // Protects against XSS
        cookie.setSecure(false); // Change to true in production with HTTPS
        cookie.setPath("/"); // Available for all API paths
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }


    // Deletes a cookie by setting its max age to zero.
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
