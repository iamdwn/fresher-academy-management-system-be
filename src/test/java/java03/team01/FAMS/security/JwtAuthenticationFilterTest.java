package java03.team01.FAMS.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java03.team01.FAMS.model.entity.AccessToken;
import java03.team01.FAMS.repository.AccessTokenRepository;
import java03.team01.FAMS.repository.RefreshTokenRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JwtAuthenticationFilterTest {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testDoFilterInternal() throws Exception {
        // Mocking the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer fakeToken");

        // Mocking the behavior of JwtTokenProvider
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn("testUser");

        // Mocking the behavior of UserDetailsService
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Mocking the behavior of AccessTokenRepository
        AccessToken accessToken = mock(AccessToken.class);
        when(accessTokenRepository.findByToken(anyString())).thenReturn(accessToken);
        when(accessToken.isRevoked()).thenReturn(false);
        when(accessToken.isExpired()).thenReturn(false);

        // Perform filter operation
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify authentication is set
        verify(userDetailsService).loadUserByUsername("testUser");
        verify(accessTokenRepository).findByToken("fakeToken");
//        verify(userDetails).getAuthorities();
    }

    @Test
    public void testDoFilterInternal_WithValidToken() throws Exception {
        // Mocking the request
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer fakeToken");

        // Mocking the behavior of JwtTokenProvider
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn("testUser");

        // Mocking the behavior of UserDetailsService
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Mocking the behavior of AccessTokenRepository
        AccessToken accessToken = mock(AccessToken.class);
        when(accessTokenRepository.findByToken(anyString())).thenReturn(accessToken);
        when(accessToken.isRevoked()).thenReturn(false);
        when(accessToken.isExpired()).thenReturn(false);

        // Perform filter operation
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify authentication is set
        verify(userDetailsService).loadUserByUsername("testUser");
        verify(accessTokenRepository).findByToken("fakeToken");
//        verify(userDetails).getAuthorities();
//        verify(accessToken).isRevoked();
//        verify(accessToken).isExpired();
        verify(filterChain).doFilter(request, response);
    }

}
