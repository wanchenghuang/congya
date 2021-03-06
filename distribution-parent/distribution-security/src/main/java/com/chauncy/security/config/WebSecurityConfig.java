package com.chauncy.security.config;

import com.chauncy.security.jwt.AuthenticationFailHandler;
import com.chauncy.security.jwt.AuthenticationSuccessHandler;
import com.chauncy.security.jwt.JWTAuthenticationFilter;
import com.chauncy.security.jwt.RestAccessDeniedHandler;
import com.chauncy.security.permission.MyFilterSecurityInterceptor;
import com.chauncy.security.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.cors.CorsUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author huangwancheng
 * @create 2019-05-24 14:55
 * <p>
 * 配置拦截器保护请求
 * Security 核心配置类
 * 开启控制权限至Controller
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  //开启方法权限控制
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${distribution.token.redis}")
    private Boolean tokenRedis;

    @Value("${distribution.tokenExpireTime}")
    private Integer tokenExpireTime;

    @Value("${distribution.token.storePerms}")
    private Boolean storePerms;

    @Autowired
    private IgnoredUrlsProperties ignoredUrlsProperties;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthenticationFailHandler failHandler;

    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private MyFilterSecurityInterceptor myFilterSecurityInterceptor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource;

    @Autowired
    private AuthenticationProvider provider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /*auth
                // 设置UserDetailsService
                .userDetailsService(userDetailsService)

                // 使用BCrypt进行密码的hash
                .passwordEncoder(new BCryptPasswordEncoder());*/

        auth.authenticationProvider(provider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

//        http.csrf().disable();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = http
                .authorizeRequests();
//        registry.requestMatchers(CorsUtils::isPreFlightRequest).permitAll();
        //除配置文件忽略路径其它所有请求都需经过认证和授权
        for (String url : ignoredUrlsProperties.getUrls()) {
            registry.antMatchers(url).permitAll();

        }

        registry.and()
                //表单登录方式
                .formLogin()
                //增加手机号  验证码等字段
                .authenticationDetailsSource(authenticationDetailsSource)
                //请求时未登录跳转接口
                .loginPage("/common/needLogin")
                //登录请求url
                .loginProcessingUrl("/login")
                .permitAll()
                //成功处理类
                .successHandler(successHandler)
                //失败
                .failureHandler(failHandler)
                .and()
                //允许网页iframe
                .headers().frameOptions().disable()
                .and()
                .logout()//退出登录相关配置
//                    .logoutUrl("/signOut")  //自定义退出登录页面
                .logoutSuccessUrl("/common/logout") //退出成功后跳转的页面
                .permitAll()
                .and()
                .authorizeRequests()
                //任何请求
                .anyRequest()
                //需要身份认证
                .authenticated()
                .and()
                //关闭跨站请求防护
                .csrf().disable()
                //前后端分离采用JWT 不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //自定义权限拒绝处理类,权限不足
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                //添加自定义权限过滤器
                .addFilterBefore(myFilterSecurityInterceptor, FilterSecurityInterceptor.class)
                //添加JWT过滤器 除已配置的其它请求都需经过此过滤器
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), tokenRedis, tokenExpireTime, storePerms,
                        redisTemplate, securityUtil));
    }
}
