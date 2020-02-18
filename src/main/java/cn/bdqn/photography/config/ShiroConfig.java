package cn.bdqn.photography.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableAutoConfiguration
public class ShiroConfig {

    //Filter工厂，设置对应的过滤条件和跳转条件 ShiroFilterFactoryBean
    @Bean
    public ShiroFilterFactoryBean getShirFilterFactoryBean(@Qualifier("getDefaultWebSecurityManager")
                                                            DefaultWebSecurityManager defaultWebSecurityManager){
        ShiroFilterFactoryBean filterFactoryBean=new ShiroFilterFactoryBean();
        //设置安全管理器
        filterFactoryBean.setSecurityManager(defaultWebSecurityManager);
        //shiro内置过滤器
        /*
        anon: 无需认证可以访问
        authc: 必须认证才能访问
        user: 必须拥有记住我才能访问
        perms: 拥有对某个资源的权限才能访问
        roles: 拥有某个角色权限才能访问
         */
        Map<String, String> filterMap=new LinkedHashMap<>(); //链式map 更好的增加和删除 查询则没有HasMap好数组方式好
        System.out.println("欢迎来到shiro安全世界！");
        //用户有add权限才可以执行的添加功能
        //filterMap.put("/user/addUser","perms[add]");
        filterMap.put("/shoot-user/logo","anon");
        filterMap.put("/shoot-user/index","anon");
        filterMap.put("/shoot-user/register","anon");
        filterMap.put("/shoot-user/add","anon");
        filterMap.put("/shoot-user/subLogin","anon");

        filterMap.put("/shoot-user/about","perms[query]");

        //要有相应的角色和授权才能访问的页面
        //filterMap.put("/user/delete","roles[系统管理员],perms[delete]");

        filterFactoryBean.setFilterChainDefinitionMap(filterMap);

        //如果没有认证到login页面
        filterFactoryBean.setLoginUrl("/shoot-user/login");
        //未授权
        //filterFactoryBean.setUnauthorizedUrl("/user/unauth");


        return filterFactoryBean;
    }

    /**
     * 密码匹配凭证管理器
     *
     * @return
     */
    @Bean(name = "hashedCredentialsMatcher")
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 采用MD5方式加密
        hashedCredentialsMatcher.setHashAlgorithmName("MD5");
        // 设置加密次数
        hashedCredentialsMatcher.setHashIterations(1024);
        hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
        System.out.println("密码匹配凭证管理器");
        return hashedCredentialsMatcher;
    }


    //将自己的验证方式加入容器
    @Bean(name = "userRealm")
    public UserRealm userRealm(@Qualifier("hashedCredentialsMatcher") HashedCredentialsMatcher matcher) {
        UserRealm authRealm = new UserRealm();
        authRealm.setAuthorizationCachingEnabled(false);
        authRealm.setCredentialsMatcher(matcher);
        return authRealm;
    }

    //DefaultWebSecurityManager 创建管理对象  并注入userRealm
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(@Qualifier("hashedCredentialsMatcher") HashedCredentialsMatcher matcher){
        DefaultWebSecurityManager securityManager=new DefaultWebSecurityManager();
        //关联userRealm
        securityManager.setRememberMeManager(rememberMeManager());
        securityManager.setRealm(userRealm(matcher));
        return securityManager;
    }

    /**
     * 配置shiro跟spring的关联
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("getDefaultWebSecurityManager") SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * cookie对象;
     * @return
     */
    public SimpleCookie rememberMeCookie(){
        //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //cookie生效时间30天,单位秒;
        simpleCookie.setMaxAge(2592000);
        return simpleCookie;
    }

    /**
     * cookie管理对象;记住我功能
     * @return
     */
    public CookieRememberMeManager rememberMeManager(){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        // cookieRememberMeManager.setCipherKey用来设置加密的Key,参数类型byte[],字节数组长度要求16
        // cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3Kprsdag=="));
        cookieRememberMeManager.setCipherKey("ZHANGXIAOHEI_CAT".getBytes());
        return cookieRememberMeManager;
    }

   /* @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //将自定义的realm交给SecurityManager管理
        //securityManager.setRealm(userRealm());
        // 自定义缓存实现 使用redis
        //securityManager.setCacheManager(cacheManager());
        // 自定义session管理 使用redis
        //securityManager.setSessionManager(SessionManager());
        // 使用记住我
        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }*/


    //整合shiroDialect 用来整合shiro thymeleaf
    @Bean
    public ShiroDialect getShiroDialect(){
        return new ShiroDialect();
    }

}