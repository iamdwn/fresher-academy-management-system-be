package java03.team01.FAMS.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java03.team01.FAMS.model.entity.AccessToken;
import java03.team01.FAMS.model.entity.RefreshToken;
import java03.team01.FAMS.repository.AccessTokenRepository;
import java03.team01.FAMS.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        AccessToken storedToken = accessTokenRepository.findByToken(jwt);
        RefreshToken refreshToken = storedToken.getRefreshToken();
        if (refreshToken != null) {
            refreshToken.setExpired(true);
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
        if (storedToken != null) {
            storedToken.setRevoked(true);
            storedToken.setExpired(true);
            accessTokenRepository.save(storedToken);
        }
    }
}
