package com.group2.notification_service.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmailHtmlLayoutTest {

    @Test
    void buildWithNullBody() {
        String html = EmailHtmlLayout.build(null);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertFalse(html.contains("null"));
    }

    @Test
    void buildWithEmptyBody() {
        String html = EmailHtmlLayout.build("   ");
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<body"));
    }

    @Test
    void buildWithPlainText() {
        String text = "Line 1\r\nLine 2\rLine 3\nLine 4";
        String html = EmailHtmlLayout.build(text);
        // All should be converted to <br />\n
        assertTrue(html.contains("Line 1<br />\nLine 2<br />\nLine 3<br />\nLine 4"));
        assertTrue(html.contains("<!DOCTYPE html>"));
    }

    @Test
    void buildPassesThroughFullDocument() {
        String doc = "<!DOCTYPE html><html><head><title>Test</title></head><body>This is a very long document to satisfy the length requirement of thirty characters.</body></html>";
        assertEquals(doc, EmailHtmlLayout.build(doc));
    }

    @Test
    void buildWrapsShortHtmlFragment() {
        // "<html><body>" is 12 chars, < 30 threshold, but isLikelyHtmlFragment matches it.
        // So it gets wrapped.
        String frag = "<html><body>Fragment</body>";
        String html = EmailHtmlLayout.build(frag);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<html><body>Fragment</body>"));
    }

    @Test
    void buildWithEdgeCaseShortDoc() {
        // Exactly 9 chars, should be wrapped
        String doc = "<!doctype";
        String html = EmailHtmlLayout.build(doc);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<!doctype"));
    }

    @Test
    void buildWithAmbiguousHtml() {
        // Contains < but not any recognized tags
        String text = "Score is < 10";
        String html = EmailHtmlLayout.build(text);
        assertTrue(html.contains("Score is &lt; 10"));
        assertTrue(html.contains("<!DOCTYPE html>"));
    }

    @Test
    void isLikelyHtmlFragmentBranches() {
        // Test all conditions in isLikelyHtmlFragment
        assertTrue(EmailHtmlLayout.build("</div>").contains("</div>")); // </
        assertTrue(EmailHtmlLayout.build("<hr/>").contains("<hr/>")); // />
        assertTrue(EmailHtmlLayout.build("<br>").contains("<br>")); // <br
        assertTrue(EmailHtmlLayout.build("<p>").contains("<p>")); // <p
        assertTrue(EmailHtmlLayout.build("<div>").contains("<div>")); // <div
        assertTrue(EmailHtmlLayout.build("<span>").contains("<span>")); // <span
        assertTrue(EmailHtmlLayout.build("<table>").contains("<table>")); // <table
        assertTrue(EmailHtmlLayout.build("<ul>").contains("<ul>")); // <ul
        assertTrue(EmailHtmlLayout.build("<ol>").contains("<ol>")); // <ol
        assertTrue(EmailHtmlLayout.build("<h1>").contains("<h1>")); // <h1
        assertTrue(EmailHtmlLayout.build("<h2>").contains("<h2>")); // <h2
        assertTrue(EmailHtmlLayout.build("<h3>").contains("<h3>")); // <h3
        assertTrue(EmailHtmlLayout.build("<a href=''>").contains("<a href=''")); // <a 
        assertTrue(EmailHtmlLayout.build("<html>").contains("<html>")); // <html
        assertTrue(EmailHtmlLayout.build("<body>").contains("<body>")); // <body
        assertTrue(EmailHtmlLayout.build("<title>").contains("<title>")); // <title
        assertTrue(EmailHtmlLayout.build("<head>").contains("<head>")); // <head
        assertTrue(EmailHtmlLayout.build("<!doctype>").contains("<!doctype>")); // <!doctype
    }

    @Test
    void buildOtpInnerHtmlTests() {
        String otp = "123456";
        String inner = EmailHtmlLayout.buildOtpInnerHtml(otp);
        assertTrue(inner.contains(otp));
        assertTrue(inner.contains("one-time password"));

        String nullInner = EmailHtmlLayout.buildOtpInnerHtml(null);
        assertFalse(nullInner.contains("null"));
    }

    @Test
    void escapeForJsonTests() {
        assertNull(null, EmailHtmlLayout.escapeForJson(null)); // Wait, escapeForJson(null) returns ""
        assertEquals("", EmailHtmlLayout.escapeForJson(null));
        
        String input = "quote: \", backslash: \\, newline: \n, carriage: \r, tab: \t";
        String expected = "quote: \\\", backslash: \\\\, newline: \\n, carriage: \\r, tab: \\t";
        assertEquals(expected, EmailHtmlLayout.escapeForJson(input));
    }

    @Test
    void escapeHtmlTests() {
        assertEquals("", EmailHtmlLayout.escapeHtml(null));
        assertEquals("&amp;&lt;&gt;&quot;", EmailHtmlLayout.escapeHtml("&<>\""));
        assertEquals("normal", EmailHtmlLayout.escapeHtml("normal"));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<EmailHtmlLayout> constructor = EmailHtmlLayout.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
