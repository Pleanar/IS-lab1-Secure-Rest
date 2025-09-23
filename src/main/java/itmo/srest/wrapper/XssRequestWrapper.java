package itmo.srest.wrapper;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.owasp.encoder.Encode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input)
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("'", "&#39;")
                .replaceAll("\"", "&quot;");
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitizedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitizedValues[i] = sanitize(values[i]);
        }
        return sanitizedValues;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return sanitize(value);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Enumeration<String> headers = super.getHeaders(name);
        List<String> sanitizedHeaders = new ArrayList<>();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            sanitizedHeaders.add(sanitize(header));
        }
        return Collections.enumeration(sanitizedHeaders);
    }
}
