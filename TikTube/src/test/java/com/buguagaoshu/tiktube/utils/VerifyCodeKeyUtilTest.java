package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.config.WebConstant;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifyCodeKeyUtilTest {

    @Test
    void issuedKeyIsRandomAndComesBackThroughCookie() {
        MockHttpServletResponse imageResponse = new MockHttpServletResponse();
        String first = VerifyCodeKeyUtil.issue(imageResponse);
        String second = VerifyCodeKeyUtil.issue(new MockHttpServletResponse());

        assertNotNull(first);
        assertEquals(32, first.length());
        assertNotEquals(first, second, "每次取图都必须换一个新的键");

        Cookie cookie = imageResponse.getCookie(WebConstant.COOKIE_VERIFY_KEY);
        assertNotNull(cookie, "键必须通过 Cookie 下发，前端才不需要改动");
        assertEquals(first, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals(1800, cookie.getMaxAge());

        MockHttpServletRequest submitRequest = new MockHttpServletRequest();
        submitRequest.setCookies(cookie);
        assertEquals(first, VerifyCodeKeyUtil.read(submitRequest));
    }

    @Test
    void missingOrEmptyCookieReadsAsNull() {
        assertNull(VerifyCodeKeyUtil.read(new MockHttpServletRequest()));

        MockHttpServletRequest blank = new MockHttpServletRequest();
        blank.setCookies(new Cookie(WebConstant.COOKIE_VERIFY_KEY, ""));
        assertNull(VerifyCodeKeyUtil.read(blank));

        MockHttpServletRequest otherCookie = new MockHttpServletRequest();
        otherCookie.setCookies(new Cookie(WebConstant.COOKIE_TOKEN, "jwt-value"));
        assertNull(VerifyCodeKeyUtil.read(otherCookie));
    }
}
