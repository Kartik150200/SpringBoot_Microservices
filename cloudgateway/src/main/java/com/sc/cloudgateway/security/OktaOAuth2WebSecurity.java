package com.sc.cloudgateway.security;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class OktaOAuth2WebSecurity {

    public OktaOAuth2WebSecurity(ReactiveClientRegistrationRepository repository) {
        this.repository = repository;
    }

    @Value("${auth0.audience}")
    private String audience;

    private final ReactiveClientRegistrationRepository repository;

    @Bean
    public SecurityWebFilterChain securityFilterChain (ServerHttpSecurity http) throws Exception {
      
        http
            .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.anyExchange().authenticated())
            .oauth2Login(oAuth2LoginSpec -> oAuth2LoginSpec.authorizationRequestResolver(
                authorizationRequestResolver(repository)
            ))
            .oauth2ResourceServer(oauth2 -> oauth2
                                    .jwt(jwt -> jwt
                                            .jwtAuthenticationConverter(converter())
                                ));

            
        
        return http.build();
        
    }

    private ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver(
        ReactiveClientRegistrationRepository repository
    ) {
        DefaultServerOAuth2AuthorizationRequestResolver resolver = new DefaultServerOAuth2AuthorizationRequestResolver(repository);

        resolver.setAuthorizationRequestCustomizer(authBuilderCustomizer());

        return resolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> authBuilderCustomizer() {
        return customizer -> customizer
                .additionalParameters(params -> params.put("audience", audience));
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter converter() {
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

        return new  ReactiveJwtAuthenticationConverterAdapter(converter);
    }

}
