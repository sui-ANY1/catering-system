package com.catering.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            if (!isOpenPath(SaHolder.getRequest().getMethod(), SaHolder.getRequest().getRequestPath())) {
                StpUtil.checkLogin();
            }
        })).addPathPatterns("/**");
    }

    /**
     * 免登录放行路径：
     * - 登录与接口文档
     * - 顾客点餐端只读接口（GET：菜品/分类/桌台/区域）
     * - 购物车（增删查）
     * - 提交订单
     */
    private boolean isOpenPath(String method, String path) {
        if ("/auth/login".equals(path)) {
            return true;
        }
        if (path.startsWith("/doc.html") || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars") || path.startsWith("/swagger-ui")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/dish") || path.startsWith("/category")
                    || path.startsWith("/table") || path.startsWith("/area")) {
                return true;
            }
        }
        if (path.startsWith("/cart")) {
            return true;
        }
        if ("/order/submit".equals(path)) {
            return true;
        }
        return false;
    }
}
