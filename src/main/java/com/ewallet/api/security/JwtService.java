package com.ewallet.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Extracts email from the given JWT
     * @param token The base64 coded JWT as String
     * @return user's email located in the subject claim
     */
    public String extractEmail(String token) {
        return extractClaim(token , Claims::getSubject);
    }

    // Generic method to extract any claim from the token
    // using Function as resolver
    public  <T> T extractClaim(String token , Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     *  Creates a new JWT for the user without any extra custom claims
     * @param userDetails User's details provided by Spring Security
     * @return The final signed token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>() , userDetails);
    }


    /**
     * The core method for generating a JWT
     * It assembles the header, the payload and signs it using the HS256 algorithm
     *
     * @param extraClaims Additional information to be included in the payload
     * @param userDetails The user details
     * @return The final signed JWT as a String
     */
    private  String generateToken(HashMap<String , Object> extraClaims , UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 minutes until expiration
                .signWith(getSignInKey() , SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * Decodes the JWT using the secret key and extracts the entire payload
     * If the token has been tampered with invalid signature this will throw a SignatureException
     *
     * @param token The JWT to parse
     * @return All the claims contained within the payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public boolean isTokenValid(String token , UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token , Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
