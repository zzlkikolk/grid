package com.jerryz.grid.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 将 snake_case 请求参数补充为 camelCase 别名，兼容 Spring MVC 对 VO 的驼峰字段绑定。
 * 示例：page_num -> pageNum, asset_code -> assetCode
 */
@Component
public class SnakeCaseRequestParamFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest wrappedRequest = new SnakeCaseParamHttpServletRequestWrapper(request);
        filterChain.doFilter(wrappedRequest, response);
    }

    private static class SnakeCaseParamHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String[]> params;

        public SnakeCaseParamHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            this.params = new HashMap<>(request.getParameterMap());

            Map<String, String[]> aliasParams = new HashMap<>();
            for (Map.Entry<String, String[]> entry : this.params.entrySet()) {
                String key = entry.getKey();
                if (!key.contains("_")) {
                    continue;
                }
                String camelKey = snakeToCamel(key);
                if (!camelKey.equals(key) && !this.params.containsKey(camelKey)) {
                    aliasParams.put(camelKey, entry.getValue());
                }
            }
            this.params.putAll(aliasParams);
        }

        @Override
        public String getParameter(String name) {
            String[] values = params.get(name);
            if (values != null && values.length > 0) {
                return values[0];
            }
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(params);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.get(name);
        }

        private String snakeToCamel(String key) {
            String[] parts = key.split("_");
            if (parts.length == 0) {
                return key;
            }

            StringBuilder sb = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                if (parts[i].isEmpty()) {
                    continue;
                }
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    sb.append(parts[i].substring(1));
                }
            }
            return sb.toString();
        }
    }
}
