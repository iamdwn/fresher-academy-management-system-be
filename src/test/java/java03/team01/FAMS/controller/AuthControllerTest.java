package java03.team01.FAMS.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java03.team01.FAMS.model.payload.dto.LoginDto;
import java03.team01.FAMS.model.payload.dto.SignupDto;
import java03.team01.FAMS.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    //unit test for login
    @Test
    public void testLogin() {
        LoginDto loginDto = new LoginDto();
        authController.login(loginDto);
        assertEquals(HttpStatus.OK, authController.login(loginDto).getStatusCode());
    }

    //unit test for signup
    @Test
    public void testSignup() {
        SignupDto signupDto = new SignupDto();
        authController.signup(signupDto);
        assertEquals(HttpStatus.CREATED, authController.signup(signupDto).getStatusCode());
    }

    //unit test for refreshToken
    @Test
    public void testRefreshToken() throws IOException {
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        assertEquals(HttpStatus.OK, authController.refreshToken(request, response).getStatusCode());
    }
}
