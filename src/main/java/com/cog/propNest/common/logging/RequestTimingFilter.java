package com.cog.propNest.common.logging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Logs the HTTP method, full URI (with query string), response status and the
 * elapsed time in milliseconds for every request. This is what produces the
 * "Response Time (s)" numbers used in the index performance analysis — call an
 * endpoint and read the elapsed time straight from the application log.
 *
 * <p>Auto-registered because it is a {@code @Component} implementing {@link Filter}.
 */
@Component
@Order(1)
public class RequestTimingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        long startNanos = System.nanoTime();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            String query = httpRequest.getQueryString();
            String uri = httpRequest.getRequestURI()
                + (query != null ? "?" + query : "");
            log.info("{} {} -> {} ({} ms)",
                httpRequest.getMethod(), uri, httpResponse.getStatus(), elapsedMs);
        }
    }
}
