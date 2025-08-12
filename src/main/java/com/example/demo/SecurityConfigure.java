package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfigure {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    // This bean provides the encoderkj
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // This bean uses the encoder to store the user
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        String rawPassword = "1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // This will print the encoded password to your console when the app starts
        System.out.println("==================================================");
        System.out.println("Encoded password for 'user' is: " + encodedPassword);
        System.out.println("==================================================");

        UserDetails user = User.withUsername("user")
            .password(encodedPassword)
            .roles("USER")
            .build();
            
        return new InMemoryUserDetailsManager(user);
    }
}