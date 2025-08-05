package com.sc.productservice.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Log4j2
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // http
        //     .authorizeRequests(
        //          authorizeRequest -> authorizeRequest
        //                 .anyRequest()
        //                 .authenticated()
        //     )
        //     .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorizeRequest -> authorizeRequest
                                    .anyRequest().authenticated()) 
            .oauth2ResourceServer(oauth2 -> oauth2
                                    .jwt(jwt -> jwt
                                            .jwtAuthenticationConverter(converter())
                                ));
        
        http
            .exceptionHandling(handling -> handling
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                log.error("Access denied! Authorities: " +
                        SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                response.sendError(HttpStatus.FORBIDDEN.value());
            }));
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            Collection<GrantedAuthority> customAuthorities = jwt.getClaimAsStringList("http://Kartikspring.com/roles")
                                                                .stream()
                                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                                .collect(Collectors.toList());

            authorities.addAll(customAuthorities);
            return authorities;
                                                                
        });

        return converter;
    }

    // @Bean
    // public JwtDecoder jwtDecoder() {
    //     return JwtDecoders.fromIssuerLocation("https://dev-51756708.okta.com/oauth2/default");
    // }

    // @Bean
    // public Converter<org.springframework.security.oauth2.jwt.Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    //     // Converter for the scopes (scp claim)
    //     JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
    //     scopeConverter.setAuthorityPrefix("SCOPE_");
    //     scopeConverter.setAuthoritiesClaimName("scp");
        
    //     // Converter for the groups
    //     JwtGrantedAuthoritiesConverter groupConverter = new JwtGrantedAuthoritiesConverter();
    //     groupConverter.setAuthorityPrefix("ROLE_"); // Optional: Add if you want ROLE_ prefix
    //     groupConverter.setAuthoritiesClaimName("groups");
        
    //     // Combine both converters
    //     JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    //     jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
    //         var scopeAuthorities = scopeConverter.convert(jwt);
    //         var groupAuthorities = groupConverter.convert(jwt);
            
    //         // Merge both authorities
    //         var combinedAuthorities = new java.util.ArrayList<>(scopeAuthorities);
    //         combinedAuthorities.addAll(groupAuthorities);
            
    //         return combinedAuthorities;
    //     });
        
    //     return jwtConverter;
    // }
}
