package java03.team01.FAMS.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java03.team01.FAMS.model.entity.AccessToken;
import java03.team01.FAMS.model.entity.RefreshToken;
import java03.team01.FAMS.model.entity.Role;
import java03.team01.FAMS.model.entity.User;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.exception.ResourceNotFoundException;
import java03.team01.FAMS.model.payload.dto.LoginDto;
import java03.team01.FAMS.model.payload.dto.SignupDto;
import java03.team01.FAMS.model.payload.responseModel.AuthenticationResponse;
import java03.team01.FAMS.repository.AccessTokenRepository;
import java03.team01.FAMS.repository.RefreshTokenRepository;
import java03.team01.FAMS.repository.RoleRepository;
import java03.team01.FAMS.repository.UserRepository;
import java03.team01.FAMS.security.JwtTokenProvider;
import java03.team01.FAMS.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    RefreshToken refreshToken;

    private LoginDto loginDto;

    private SignupDto signupDto;

    @BeforeEach
    public void setUp() {
        loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("test");
        loginDto.setPassword("test");

        signupDto = new SignupDto();
        signupDto.setUsername("test");
        signupDto.setEmail("a@gmail.com");
        signupDto.setPassword("test");
        signupDto.setFullName("test");
        signupDto.setDob(LocalDate.of(2003, 1, 1));
        signupDto.setAddress("test");
        signupDto.setGender("test");
        signupDto.setPhone("0123456789");
    }

    //unit tests for login
    @Test
    public void whenUserNotFound_thenThrowException() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        FamsApiException thrown = assertThrows(FamsApiException.class, () -> authService.login(loginDto));

        assertTrue(thrown.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findByUsernameOrEmail(anyString(), anyString());
    }

    @Test
    public void login_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(new User()));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");

        AuthenticationResponse response = authService.login(loginDto);

        assertTrue(response.getAccessToken().equals("accessToken"));
        assertTrue(response.getRefreshToken().equals("refreshToken"));
        verify(userRepository, times(1)).findByUsernameOrEmail(anyString(), anyString());
        verify(jwtTokenProvider, times(1)).generateAccessToken(any());
        verify(jwtTokenProvider, times(1)).generateRefreshToken(any());
    }

    //unit tests for sign up
    @Test
    public void whenUserAlreadyExists_thenThrowException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        FamsApiException thrown = assertThrows(FamsApiException.class, () -> authService.signup(signupDto));

        assertTrue(thrown.getMessage().contains("Username is already exist"));
        verify(userRepository, times(1)).existsByUsername(anyString());
    }

    @Test
    public void whenEmailAlreadyExists_thenThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        FamsApiException thrown = assertThrows(FamsApiException.class, () -> authService.signup(signupDto));

        assertTrue(thrown.getMessage().contains("Email is already exist"));
        verify(userRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    public void whenUserRoleNotSet_thenThrowException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());
        when(modelMapper.map(signupDto, User.class)).thenReturn(new User());
        when(passwordEncoder.encode(signupDto.getPassword())).thenReturn("encryptedPassword");

        FamsApiException thrown = assertThrows(FamsApiException.class, () -> authService.signup(signupDto));

        assertTrue(thrown.getMessage().contains("User Role not found."));
        verify(userRepository, times(1)).existsByUsername("test");
        verify(userRepository, times(1)).existsByEmail("a@gmail.com");
    }

    @Test
    public void signup_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(new Role()));
        when(modelMapper.map(signupDto, User.class)).thenReturn(new User());
        when(passwordEncoder.encode(signupDto.getPassword())).thenReturn("encryptedPassword");

        String response = authService.signup(signupDto);

        assertTrue(response.contains("successfully"));
        verify(userRepository, times(1)).existsByUsername("test");
        verify(userRepository, times(1)).existsByEmail("a@gmail.com");
        verify(roleRepository, times(1)).findByRoleName("ROLE_TRAINER");
    }

    //test for refresh token

    //test for auth header is null
    @Test
    public void whenAuthHeaderIsNull_thenReturnNull() throws IOException {
        when(request.getHeader(anyString())).thenReturn(null);

        AuthenticationResponse res = authService.refreshToken(request, response);

        assertTrue(res == null);
    }

    //test for invalid refresh token
    @Test
    public void whenInvalidRefreshToken_thenReturnNull() throws IOException {
        final String username = "dung";
        when(request.getHeader(anyString())).thenReturn("Bearer token");
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn(username);
        when(refreshToken.isExpired()).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(mock(UserDetails.class));
        when(jwtTokenProvider.isTokenValid("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJkdW5nIiwiaWF0IjoxNzExNjE2MzUyLCJleHAiOjE3MTE3MDI3NTJ9.fduVN7MdThZ_5nlgADb75_CowJSsBlxHv8uwHLqfiK20NURrXbXw1U55B2hhH9DM", "dung")).thenReturn(false);

        FamsApiException thrown = assertThrows(FamsApiException.class, () -> authService.refreshToken(request, response));

        assertTrue(thrown.getMessage().contains("Invalid refresh token"));
        verify(refreshTokenRepository, times(1)).findByToken(anyString());
    }

    //test for refresh token success
    @Test
    public void refreshToken_Success() throws IOException {
        final String username = "dung";
        when(request.getHeader(anyString())).thenReturn("Bearer token");
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn(username);
        when(refreshToken.isExpired()).thenReturn(false);
        when(refreshToken.isRevoked()).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(mock(UserDetails.class));
        when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.of(new User()));

        AuthenticationResponse thrown = authService.refreshToken(request, response);

        assertNotNull(thrown);
        verify(refreshTokenRepository, times(1)).findByToken(anyString());
    }

    //test if username is null then return null
    @Test
    public void whenUsernameIsNull_thenReturnNull() throws IOException {
        final String username = null;
        when(request.getHeader(anyString())).thenReturn("Bearer token");
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn(username);

        AuthenticationResponse res = authService.refreshToken(request, response);

        assertTrue(res == null);
        verify(refreshTokenRepository, times(1)).findByToken(anyString());
    }

    //unit tests for revoke token
    @Test
    public void revokeRefreshToken_Success() {
        String rf = "token";
        refreshToken.setToken("token");
        refreshToken.setExpired(false);
        refreshToken.setRevoked(false);
        AccessToken accessToken = new AccessToken();
        accessToken.setToken("token");
        accessToken.setExpired(false);
        accessToken.setRevoked(false);
        accessToken.setRefreshToken(refreshToken);
        when(accessTokenRepository.findByToken(anyString())).thenReturn(accessToken);

        authService.revokeRefreshToken(rf);

        verify(accessTokenRepository, times(1)).findByToken(anyString());
    }

    //unit tests for revoke all user access tokens
    @Test
    public void revokeAllUserAccessTokens_Success() {
        User user = new User();
        user.setId(1L);
        AccessToken accessToken = new AccessToken();
        accessToken.setUser(user);
        List<AccessToken> accessTokens = new ArrayList<>();
        accessTokens.add(accessToken);
        when(accessTokenRepository.findAllValidTokensByUser(anyLong())).thenReturn(accessTokens);

        authService.revokeAllUserAccessTokens(user);

        verify(accessTokenRepository, times(1)).findAllValidTokensByUser(anyLong());
    }
}
