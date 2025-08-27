package im.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    // !! 在生产环境中，密钥应该更复杂，并从配置文件或环境变量中读取 !!
    private static final String SECRET_KEY = "8r5g-6a28-1cx-1xer";
    private static final long EXPIRATION_TIME = 86_400_000; // 24 hours
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(Long userId, String username) {
        return JWT.create()
                .withClaim("uid", userId)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            logger.warn("JWT verification failed: {}", exception.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("uid").asLong();
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("username").asString();
        }
        return null;
    }
}
