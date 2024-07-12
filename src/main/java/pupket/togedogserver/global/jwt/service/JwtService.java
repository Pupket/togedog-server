package pupket.togedogserver.global.jwt.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pupket.togedogserver.domain.token.entity.RefreshToken;
import pupket.togedogserver.domain.token.repository.RefreshTokenRepository;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.JwtException;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.security.CustomUserDetail;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtService {

    private final Key key;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository memberRepository;

    public JwtService(@Value("${jwt.secret}") String secretKey,
                      @Value("${jwt.token.access-token-expiration-time}") long accessTokenExpirationTime,
                      @Value("${jwt.token.refresh-token-expiration-time}") long refreshTokenExpirationTime,
                      RefreshTokenRepository refreshTokenRepository, UserRepository memberRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.memberRepository = memberRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }


    public JwtToken generateToken(Authentication authentication) {
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(generateAccessToken(authentication))
                .refreshToken(generateRefreshToken(authentication))
                .build();
    }

    // 리프레시 토큰을 이용해 액세스 토큰을 재발급
    public JwtToken reissueTokenByRefreshToken(String oldRefreshToken) {

        validateToken(oldRefreshToken);

        RefreshToken oldRefreshTokenDB = refreshTokenRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new JwtException(ExceptionCode.NOT_FOUND_REFRESH_TOKEN));
        refreshTokenRepository.deleteByRefreshToken(oldRefreshTokenDB.getRefreshToken());
        Authentication authentication = getAuthenticationFromMemberId(oldRefreshTokenDB.getMemberId());

        // 새로운 토큰 생성
        String newAccessToken = generateAccessToken(authentication);
        String newRefreshToken = generateRefreshToken(authentication);


        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public Authentication getAuthenticationFromAccessToken(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new JwtException(ExceptionCode.INVALID_TOKEN);
        }


        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());


        CustomUserDetail userDetail = new CustomUserDetail(claims.getSubject(), "",
                Long.parseLong(String.valueOf(claims.get("id"))), authorities);
        return new UsernamePasswordAuthenticationToken(userDetail, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            deleteRefreshTokenDB(token);
            throw handlingJwtException(e);
        }
    }

    private String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // CustomUserDetail로 형변환 후 꺼내기
        CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenExpirationTime);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("id", customUserDetail.getUuid())
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    private String generateRefreshToken(Authentication authentication) {
        CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();

        long now = (new Date()).getTime();
        Date refreshTokenExpiresIn = new Date(now + refreshTokenExpirationTime);

        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();

        // 사용자 ID로 기존 리프레시 토큰이 있는지 확인
        refreshTokenRepository.findByMemberId(customUserDetail.getUuid()).ifPresentOrElse(
                existingRefreshToken -> {
                    // 리프레시 토큰이 존재한다면 업데이트
                    existingRefreshToken.updateRefreshToken(refreshToken);
                    log.info("토큰 업데이트 완료");
                    refreshTokenRepository.save(existingRefreshToken);
                },
                () -> {
                    // 리프레시 토큰 DB 저장 (새로운 토큰)
                    refreshTokenRepository.save(RefreshToken.of(refreshToken, customUserDetail.getUuid()));
                    log.info("새로운 토큰 생성 및 저장 완료");
                }
        );
        return refreshToken;
    }

    private Authentication getAuthenticationFromMemberId(Long memberId) {
        // 회원 ID로 사용자 정보 조회
        User user = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER));
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        // 권한 정보를 담을 컬렉션 생성
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);

        // CustomUserDetail 객체 생성
        CustomUserDetail customUserDetail = new CustomUserDetail(user.getEmail(), user.getPassword(),
                user.getUuid(), authorities);

        // Authentication 객체 생성 및 반환
        return new UsernamePasswordAuthenticationToken(customUserDetail, "", authorities);
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    @Transactional
    public void deleteRefreshTokenDB(String refreshToken) {
        try {
            refreshTokenRepository.deleteByRefreshToken(refreshToken);
        } catch (Exception e) {
            String tokenInfo = refreshToken == null ? "null" : refreshToken;
            log.info("Failed to delete refreshToken, Token: {}", tokenInfo);
            log.info("e={}", e.getMessage());
            throw new JwtException(ExceptionCode.INVALID_TOKEN);
        }
    }

    private JwtException handlingJwtException(Exception e) {
        if (e instanceof SecurityException || e instanceof MalformedJwtException) {
            return new JwtException(ExceptionCode.INVALID_TOKEN);
        } else if (e instanceof ExpiredJwtException) {
            return new JwtException(ExceptionCode.TOKEN_EXPIRED);
        } else if (e instanceof UnsupportedJwtException) {
            return new JwtException(ExceptionCode.UNSUPPORTED_TOKEN);
        } else if (e instanceof IllegalArgumentException) {
            return new JwtException(ExceptionCode.NOT_FOUND_TOKEN);
        } else {
            return new JwtException();
        }
    }
}
