package br.com.area51.ufoTracker.config;

import com.nimbusds.jwt.JWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth->
                        auth.requestMatchers("/publico/**").permitAll()
                                .anyRequest().authenticated()
                        )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt-> jwt.jwtAuthenticationConverter(jwtConverter())));
        return http.build();
    }
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter() {
        return jwt -> {

            var authorities =new ArrayList<GrantedAuthority>();
            var realm = (Map<String,Object>) jwt.getClaim("realm_access");
            if(realm!=null){
                var roles = (Collection<String>)realm.get("roles");
                if(roles!=null){
                    roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_"+r)));

                }
            }
            var resource = (Map<String, Object>) jwt.getClaim("resource_access");
            if (resource != null) {
                var client = (Map<String, Object>) resource.get("ufo-tracker"); // <- seu clientId
                if (client != null) {
                    var roles = (Collection<String>) client.get("roles");
                    if (roles != null) {
                        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
                    }
                }
            }
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}
