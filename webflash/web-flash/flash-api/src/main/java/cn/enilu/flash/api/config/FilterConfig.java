
package cn.enilu.flash.api.config;

import cn.enilu.flash.api.interceptor.CrosXssFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.Map;

/**
 * 设置跨站脚本过滤
 */
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean xssFilterRegistration(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        registrationBean.setFilter(new CrosXssFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("CrosXssFilter");
        registrationBean.setOrder(9999);
        Map<String,String> initParameters = new HashMap<>();
        initParameters.put("excludes", "/favicon.ico,/img/*,/js/*,/css/*");
        initParameters.put("isIncludeRichText", "false");
        registrationBean.setInitParameters(initParameters);
        return registrationBean;
    }
}
