package java03.team01.FAMS.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java03.team01.FAMS.model.payload.dto.LoginDto;
import java03.team01.FAMS.model.payload.dto.SignupDto;
import java03.team01.FAMS.model.payload.responseModel.AuthenticationResponse;

import java.io.IOException;

public interface AuthService {
    AuthenticationResponse login(LoginDto loginDto);
    String signup(SignupDto signupDto);
    AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
