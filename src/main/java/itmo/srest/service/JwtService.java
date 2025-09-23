package itmo.srest.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import itmo.srest.entity.User;
import itmo.srest.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration:30}")
    private Long expTime;
    @Value("${jwt.refresh:360}")
    private Long refTime;

    private final TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository){
        this.tokenRepository = tokenRepository;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(User user, Long expTime) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(expTime, ChronoUnit.MINUTES)))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessToken(User user) {
        return generateToken(user, expTime);
    }

    public String generateRefreshToken(User user) {
        return  generateToken(user, refTime);
    }

    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parserBuilder();
        parser.setSigningKey(getSigningKey());
        return parser.build().parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isAccessTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    public boolean isValidAccess(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        boolean isValidToken = tokenRepository.findByAccessToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);
        return username.equals(userDetails.getUsername())
                && isAccessTokenExpired(token)
                && isValidToken;
    }

    public boolean isValidRefresh(String token, User user) {
        String username = extractUsername(token);
        boolean isValidRefreshToken = tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);
        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidRefreshToken;
    }
}
