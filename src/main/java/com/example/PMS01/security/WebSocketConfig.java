package com.example.PMS01.security;

import com.example.PMS01.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Normal WebSocket için endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // SockJS desteği ile endpoint (tarayıcı uyumluluğu için)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");  // mesajların yayınlanacağı kanal
        registry.setApplicationDestinationPrefixes("/app");  // mesajların alınacağı kanal
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Header'dan token'i al
                    String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        throw new MessagingException("Yetkilendirme başarısız: Token bulunamadı");
                    }

                    String token = authorizationHeader.substring(7);
                    try {
                        String email = jwtUtils.extractEmail(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        if (jwtUtils.validateToken(token, email)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authentication);
                        } else {
                            throw new MessagingException("Yetkilendirme başarısız: Geçersiz token");
                        }
                    } catch (Exception e) {
                        throw new MessagingException("Yetkilendirme başarısız: " + e.getMessage());
                    }
                }
                return message;
            }
        });
    }
}