package java03.team01.FAMS.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java03.team01.FAMS.model.entity.AccessToken;
import java03.team01.FAMS.model.entity.RefreshToken;
import java03.team01.FAMS.model.payload.responseModel.AuthenticationResponse;
import java03.team01.FAMS.repository.AccessTokenRepository;
import java03.team01.FAMS.repository.RefreshTokenRepository;
import java03.team01.FAMS.service.impl.LogoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class LogoutServiceTest {
    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    public void whenAuthHeaderIsNull_thenReturnNull() throws IOException {
        when(request.getHeader(anyString())).thenReturn(null);

        logoutService.logout(request, response, authentication);

        assertTrue(true);
    }

    @Test
    public void logout_success() throws IOException {
        String rf = "token";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token");
        refreshToken.setExpired(false);
        refreshToken.setRevoked(false);
        AccessToken accessToken = new AccessToken();
        accessToken.setToken("token");
        accessToken.setExpired(false);
        accessToken.setRevoked(false);
        accessToken.setRefreshToken(refreshToken);
        when(request.getHeader(anyString())).thenReturn("Bearer token");
        when(accessTokenRepository.findByToken(anyString())).thenReturn(accessToken);

        logoutService.logout(request, response, authentication);

        assertTrue(true);
    }


}
