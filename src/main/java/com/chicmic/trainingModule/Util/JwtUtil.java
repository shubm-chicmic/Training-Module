package com.chicmic.trainingModule.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    public static final long JWT_TOKEN_VALIDITY = 12*60*60l;
    private Key key;

    // create the token, If not created
    public String createJwtToken(Map<String, Object> claims, String subject){

        key= Keys.hmacShaKeyFor(secret.getBytes());
        return  Jwts.builder().addClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ JWT_TOKEN_VALIDITY * 1000))
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }
    public String updateJwtToken(String token, String subject){

        key= Keys.hmacShaKeyFor(secret.getBytes());
        Claims claims=  getAllClaimsFromToken(token);

        return  Jwts.builder().addClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ JWT_TOKEN_VALIDITY * 1000))
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }
    // validate the token, if exists
    public boolean validateJwtToken(String token, UserDetails userDetails) throws JwtException{
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpiry(token);
    }
    //helping methods
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    public boolean isTokenExpiry(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    public  <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token.trim());
        return claimsResolver.apply(claims);
    }
    public Claims getAllClaimsFromToken(String token) throws JwtException {
        key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
