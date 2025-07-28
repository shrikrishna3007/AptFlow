package com.project.aptflow.unittest.service.auth;

import com.project.aptflow.exceptions.BadRequestException;
import com.project.aptflow.exceptions.UnAuthorizedException;
import com.project.aptflow.service.auth.impl.JWTServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Service Unit Tests")
public class JWTServiceUnitTest {
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JWTServiceImpl jwtServiceImpl;

    private String testSecret;
    private String validToken;
    private String expiredToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        testSecret = "bRoJbzEqliDgTlEco2Hokf4AafkzgeF2XZQxMtvckmI=";
        ReflectionTestUtils.setField(jwtServiceImpl, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtServiceImpl, "accessTokenExpiration", 1000 * 60 * 60 * 24);
        ReflectionTestUtils.setField(jwtServiceImpl, "refreshTokenExpiration", 1000 * 60 * 60 * 24 * 7);

        when(userDetails.getUsername()).thenReturn("username");

        validToken = jwtServiceImpl.generateToken(userDetails);

        expiredToken = Jwts.builder()
                .setSubject("username")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret.getBytes())))
                .compact();

        invalidToken = "invalid-Token";
    }

    @Nested
    @DisplayName("Generate Access Token Tests")
    class GenerateAccessTokenTests {
        @Test
        @DisplayName("Should Generate Valid Token With Correct Claims: Success")
        void generateToken_SuccessTest() {
            String token = jwtServiceImpl.generateToken(userDetails);
            assertThat(token).isNotNull();
            assertThat(token.contains(".")).isTrue();
            assertThat(jwtServiceImpl.extractUsername(token)).isEqualTo("username");
        }

        @Test
        @DisplayName("Should Generate Different Valid Tokens For Different Users: Success")
        void generateToken_Multiple_Token_Generation_SuccessTest() {
            UserDetails anotherUser = mock(UserDetails.class);
            when(anotherUser.getUsername()).thenReturn("anotherUser");

            String token1 = jwtServiceImpl.generateToken(userDetails);
            String token2 = jwtServiceImpl.generateToken(anotherUser);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should Generate Token With 24 Hours Expiration")
        void generateToken_With_24_Hour_Expiration(){
            String token = jwtServiceImpl.generateToken(userDetails);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long expectedExpiration = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
            Long actualExpiration = claims.getExpiration().getTime();

            assertThat(Math.abs(actualExpiration - expectedExpiration)<5000).isTrue();
        }
    }

    @Nested
    @DisplayName("Generate Refresh Token Tests")
    class GenerateRefreshTokenTests{
        @Test
        @DisplayName("Generate Refresh Token With Extra Claims: Success")
        void generateRefreshToken_SuccessTest(){
            Map<String,Object> extraClaims = new HashMap<>();
            extraClaims.put("role","USER");
            extraClaims.put("customClaim","customValue");
            // Act
            String refreshToken = jwtServiceImpl.generateRefreshToken(extraClaims,userDetails);
            // Assert
            assertThat(refreshToken).isNotNull();
            assertThat(jwtServiceImpl.extractUsername(refreshToken)).isEqualTo("username");
        }

        @Test
        @DisplayName("Should Generate Refresh Token With 7 Days Expiration")
        void generateRefreshToken_With_7_Days_Expiration(){
            Map<String,Object> extraClaims = Map.of("role","USER");
            // Act
            String refreshToken = jwtServiceImpl.generateRefreshToken(extraClaims,userDetails);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret)))
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            Long expectedExpiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7;
            Long actualExpiration = claims.getExpiration().getTime();

            assertThat(Math.abs(actualExpiration - expectedExpiration)<5000).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests{
        @Test
        @DisplayName("Should Extract Username From Valid Token")
        void extractUsername_SuccessTest() {
            String username = jwtServiceImpl.extractUsername(validToken);
            assertThat(username).isEqualTo("username");
        }

        @Test
        @DisplayName("Should Throw Exception When Extracting Username From Invalid Token")
        void extractUsername_From_InvalidToken(){
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(invalidToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Invalid token");
        }

        @Test
        @DisplayName("Should Throw Exception When Extracting Username From Expired Token")
        void extractUsername_From_ExpiredToken(){
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(expiredToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Token has expired");
        }

        @Test
        @DisplayName("Should Throw Exception When Token Is Null")
        void extractUsername_When_TokenIsNull(){
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(null))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Invalid token");
        }

        @Test
        @DisplayName("Should Throw Exception When Token Is Empty")
        void extractUsername_When_TokenIsEmpty(){
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(""))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessage("Invalid token");
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests{
        @Test
        @DisplayName("Should Validate Token Successfully For Correct User")
        void isTokenValid_SuccessTest(){
            boolean isValid = jwtServiceImpl.isTokenValid(validToken,userDetails);
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should Reject Token For Different User")
        void isTokenValid_Failure_DifferentUserTest(){
            UserDetails differentUser = mock(UserDetails.class);
            when(differentUser.getUsername()).thenReturn("differentUser");

            boolean isValid = jwtServiceImpl.isTokenValid(validToken,differentUser);
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should Reject Expired Token")
        void isTokenValid_Failure_ExpiredToken(){
            try {
                boolean isValid = jwtServiceImpl.isTokenValid(expiredToken,userDetails);
                assertThat(isValid).isFalse();
            } catch (JwtException e) {
                assertTrue(true);
            }
        }

        @Test
        @DisplayName("Should Reject Invalid Token")
        void isTokenValid_Failure_InvalidToken(){
            boolean isValid = jwtServiceImpl.isTokenValid(invalidToken,userDetails);
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should Reject Null Token")
        void isTokenValid_Failure_NullToken(){
            boolean isValid = jwtServiceImpl.isTokenValid(null,userDetails);
            assertThat(isValid).isFalse();
        }

        @SuppressWarnings("ConstantConditions")
        @Test
        @DisplayName("Should Reject Token When UserDetails Is Null")
        void isTokenValid_Failure_UserDetailsIsNull(){
            assertThatThrownBy(()->jwtServiceImpl.isTokenValid(validToken,null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Refresh Token Validation Tests")
    class RefreshTokenValidationTests{
        @Test
        @DisplayName("Should Validate Refresh Token And Return Username")
        void validateRefreshToken_SuccessTest(){
            // Arrange
            Map<String,Object> extraClaims = Map.of("role","USER");
            String refreshToken = jwtServiceImpl.generateRefreshToken(extraClaims,userDetails);
            // Act
            String userName = jwtServiceImpl.validateRefreshToken(refreshToken);
            // Assert
            assertThat(userName).isEqualTo("username");
        }

        @Test
        @DisplayName("Should Return Null For Invalid Refresh Token")
        void validateRefreshToken_Failure_InvalidRefreshToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateRefreshToken(invalidToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid or expired refresh token");
        }

        @Test
        @DisplayName("Should Return Null For Expired Refresh Token")
        void validateRefreshToken_Failure_ExpiredRefreshToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateRefreshToken(expiredToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid or expired refresh token");
        }

        @Test
        @DisplayName("Should Return Null For Null Refresh Token")
        void validateRefreshToken_Failure_NullRefreshToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateRefreshToken(null))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid or expired refresh token");
        }

        @Test
        @DisplayName("Should Return Null For Empty Refresh Token")
        void validateRefreshToken_Failure_EmptyRefreshToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateRefreshToken(""))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid or expired refresh token");
        }
    }

    @Nested
    @DisplayName("Password Reset Token Tests")
    class PasswordResetTokenTests{
        @Test
        @DisplayName("Should Generate Reset Token With Correct Claim")
        void generateResetToken_With_CorrectClaimTest(){
            String email ="test@gmail.com";
            String resetToken = jwtServiceImpl.generateResetToken(email);
            assertThat(resetToken).isNotNull();
            assertThat(jwtServiceImpl.validateResetToken(resetToken)).isEqualTo(email);
        }

        @Test
        @DisplayName("Should Generate Reset Token With 15 Minute Expiration")
        void generateResetToken_With_15Minutes_Expiration(){
            String email = "test@gamil.com";
            String resetToken = jwtServiceImpl.generateResetToken(email);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret)))
                    .build()
                    .parseClaimsJws(resetToken)
                    .getBody();

            Long expectedExpiration = System.currentTimeMillis() + Duration.ofMinutes(15).toMillis();
            Long actualExpiration = claims.getExpiration().getTime();

            assertThat(Math.abs(actualExpiration - expectedExpiration)<5000).isTrue();
            assertThat(claims.get("type")).isEqualTo("reset");
        }

        @Test
        @DisplayName("Should Throw Exception When Type Claim Is Missing")
        void generateResetToken_Without_TypeClaim(){
            String tokenWithoutType = Jwts.builder()
                    .setSubject("test@gmail.com")
                    .setIssuedAt(new Date())
                    .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret)))
                    .compact();

            assertThatThrownBy(()->jwtServiceImpl.validateResetToken(tokenWithoutType))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid reset token type");
        }

        @Test
        @DisplayName("Should Throw Exception When Type Claim Is Wrong")
        void generateResetToken_With_WrongTypeClaim(){
            String tokenWithWrongType = Jwts.builder()
                    .setClaims(Map.of("type", "access"))
                    .setSubject("test@example.com")
                    .setIssuedAt(new Date())
                    .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                    .signWith(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(testSecret)))
                    .compact();
            assertThatThrownBy(()->jwtServiceImpl.validateResetToken(tokenWithWrongType))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid reset token type");
        }

        @Test
        @DisplayName("Should Throw Exception For Invalid Reset Token")
        void validateResetToken_InvalidResetToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateResetToken(invalidToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid reset token.");
        }

        @Test
        @DisplayName("Should Throw Exception For Expired Reset Token")
        void validateResetToken_ExpiredResetToken(){
            String expiredResetToken = Jwts.builder()
                    .setClaims(Map.of("type","reset"))
                    .setSubject("test@gmail.com")
                    .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                    .setExpiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret)))
                    .compact();

            assertThatThrownBy(()->jwtServiceImpl.validateResetToken(expiredResetToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Reset Token Has Expired.");
        }

        @Test
        @DisplayName("Should Throw Exception For Null Reset Token")
        void validateResetToken_NullResetToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateResetToken(null))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid reset token.");
        }

        @Test
        @DisplayName("Should Throw Exception For Empty Reset Token")
        void validateResetToken_EmptyResetToken(){
            assertThatThrownBy(()->jwtServiceImpl.validateResetToken(""))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid reset token.");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests{
        @Test
        @DisplayName("Should Throw Exception For Malformed Token")
        void malformedToken_ThrowExceptionTest(){
            String malformedToken = "is.a.malformed.token";
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(malformedToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("Should Throw Exception For Empty JWT Parts")
        void emptyJwtParts_ThrowExceptionTest(){
            String malformedToken = "abc..xyz";
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(malformedToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("Should Throw Exception For Whitespace Access Token")
        void whitespaceAccessToken_ThrowExceptionTest(){
            String malformedToken = "   ";
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(malformedToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("Should Throw Exception When UserDetails Username Is Null")
        void generateToken_With_NullUsernameTest(){
            UserDetails mockUserDetails = mock(UserDetails.class);
            when(mockUserDetails.getUsername()).thenReturn(null);
            assertThatThrownBy(()->jwtServiceImpl.generateToken(mockUserDetails))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should Throw Exception When UserDetails Username Is Empty")
        void generateToken_With_EmptyUsernameTest(){
            when(userDetails.getUsername()).thenReturn(" ");
            assertThatThrownBy(()->jwtServiceImpl.generateToken(userDetails))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Username cannot be null or empty");
        }

        @Test
        @DisplayName("Should Throw Exception When Token Is Signed With Wrong Key")
        void tokenWithWrongSigningKey_ThrowExceptionTest(){
            String forgedToken = Jwts.builder()
                    .setSubject("username")
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode("JZ51VNZ+YFu+prXfuIURV52x++WCNCpbSjA5s5CQYhI=")))
                    .compact();

            assertThatThrownBy(()->jwtServiceImpl.extractUsername(forgedToken))
                    .isInstanceOf(UnAuthorizedException.class)
                    .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("Should Reject Token With Wrong Secret Key")
        void isTokenValid_Failure_WrongSecretKey(){
            String tokenWithWrongSecretKey = Jwts.builder()
                    .setSubject("username")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode("JZ51VNZ+YFu+prXfuIURV52x++WCNCpbSjA5s5CQYhI=")))
                    .compact();

            boolean isValid = jwtServiceImpl.isTokenValid(tokenWithWrongSecretKey,userDetails);
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should Reject Malformed Token Structure")
        void extractUsername_Failure_MalformedTokenStructure(){
            String malformedToken = "header.payload";
            assertThatThrownBy(()->jwtServiceImpl.extractUsername(malformedToken))
                    .isInstanceOf(UnAuthorizedException.class);
        }

        @Test
        @DisplayName("Should Handle Token About To Expire")
        void isTokenValid_TokenAboutToExpire(){
            String almostExpiredToken = Jwts.builder()
                    .setSubject("username")
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret)))
                    .compact();
            boolean isValid = jwtServiceImpl.isTokenValid(almostExpiredToken,userDetails);
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should Handle Different Secret Key Formats")
        void generateToken_DifferentSecretKeyFormats(){
            String alternateSecretKey = "JZ51VNZ+YFu+prXfuIURV52x++WCNCpbSjA5s5CQYhI=";
            ReflectionTestUtils.setField(jwtServiceImpl, "secretKey", alternateSecretKey);
            String token = jwtServiceImpl.generateToken(userDetails);
            assertThat(token).isNotNull();
            assertThat(jwtServiceImpl.extractUsername(token)).isEqualTo("username");
        }

        // Token Tampering
        @Test
        @DisplayName("Should Reject Token With Modified Payload")
        void isTokenValid_Failure_TokenTampering(){
            String[] tokenParts = validToken.split("\\.");
            // Payload modified [Middle Part]
            String tamperedToken = tokenParts[0] + ".prXfuIURV52x." + tokenParts[2];
            boolean isValid = jwtServiceImpl.isTokenValid(tamperedToken,userDetails);
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should Handle Very Long Username")
        void generateToken_VeryLongUsername(){
            UserDetails longNameUserDetails = mock(UserDetails.class);
            String longUsername = "a".repeat(100);
            when(longNameUserDetails.getUsername()).thenReturn(longUsername);

            String token = jwtServiceImpl.generateToken(longNameUserDetails);
            assertThat(jwtServiceImpl.extractUsername(token)).isEqualTo(longUsername);
        }

        @Test
        @DisplayName("Should Handle Special Characters In Username")
        void generateToken_SpecialCharactersInUsername(){
            UserDetails specialUserDetails = mock(UserDetails.class);
            when(specialUserDetails.getUsername()).thenReturn("user@domain.com#$");
            String token = jwtServiceImpl.generateToken(specialUserDetails);
            assertThat(jwtServiceImpl.extractUsername(token)).isEqualTo("user@domain.com#$");
        }
    }
}
