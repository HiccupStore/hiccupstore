package hiccup.hiccupstore.user.security.config;


import hiccup.hiccupstore.user.security.handler.CustomAccessDeniedHandler;
import hiccup.hiccupstore.user.security.handler.CustomAuthenticationSuccessHandler;
import hiccup.hiccupstore.user.security.provider.CustomAuthenticationProvider;
import hiccup.hiccupstore.user.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
//    private final FormWebAuthenticationDetailsSource AuthenticationDetailsSource;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final DefaultOAuth2UserService defaultOAuth2UserService;

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//        String password = passwordEncoder().encode("1111");
//
//        auth.inMemoryAuthentication().withUser("user").password(password).roles("USER");
//        auth.inMemoryAuthentication().withUser("manager").password(password).roles("MANAGER","USER");
//        auth.inMemoryAuthentication().withUser("admin").password(password).roles("ADMIN","USER","MANAGER");
//                                                                                        //일단이렇게해주는이유 시스템은 ADMIN이 가장높은 권한이란것을 인지하지 못하기때문이다.
//    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //auth.userDetailsService(userDetailsService);
        auth.authenticationProvider(authenticationProvider());
    }

    private AuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider(userDetailsService,passwordEncoder());
    }

    /** static같은 정적자원들에대한 보안필터를 거치지않게 한다. */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        web.ignoring().antMatchers("/image/**","/error");
        web.ignoring().antMatchers("/testimage/**");
        web.ignoring().antMatchers("/smarteditor/**");
        log.info(PathRequest.toStaticResources().toString());
    }

    /** password encoder를 생성하는 방법*/
    @Bean
    protected PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/","/sns*","/join","/joinform","/joincomplete",
                        "/searchUserName","/registercomplete","/login*","/changedorderstatus").permitAll()
                .antMatchers("/mypage*","/userinformationupadte","/userwithdrawal","/mypageproductdelete").hasRole("USER")
                .antMatchers("/managerpage*","/managerpage1vs1*","/manager1vs1write").hasRole("ADMIN")
                .anyRequest().authenticated() //-->이거에 img,jqeury,html 전부다 걸린다.
        .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login_proc")
                .usernameParameter("userName")
                .defaultSuccessUrl("/")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
        .and()
                .exceptionHandling() //exceptionhanlder를 다룰수잇게해주는 api이다.
                .accessDeniedHandler(accessDeniedHandler())
        .and()
                .oauth2Login()
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .userInfoEndpoint()
                .userService(defaultOAuth2UserService); //구글로그인이 된 이후에 후처리가 필요하다. 1.코드받기 2.엑세스토큰
                                                        // 3.사용자프로필을 토대로 회원가입 자동진행 + 추가적인 정보도 추가
                                                        // 구글로그인이 완료되면 accesstoken과 프로필정보를 한번에 받는다.
//        .and()
//                .logout()  --> form post인증이 필요하다.
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/");

        /** 아이디1개당 1개의 session만, csrf보호 , 세션고정보호*/
        http
                .sessionManagement()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
                .sessionRegistry(sessionRegistry())
                .and()
                .sessionFixation().changeSessionId()
                .and()
                .csrf();
    }

    @Bean
    protected AccessDeniedHandler accessDeniedHandler() {
        CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
        accessDeniedHandler.setErrorPage("/denied");
        return accessDeniedHandler;
    }

    // Work around https://jira.spring.io/browse/SEC-2855
    @Bean
    public SessionRegistry sessionRegistry() {
        SessionRegistry sessionRegistry = new SessionRegistryImpl();
        return sessionRegistry;
    }

    // Register HttpSessionEventPublisher
    @Bean
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
    }


}
