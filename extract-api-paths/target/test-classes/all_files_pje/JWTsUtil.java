package br.jus.cnj.pje.webservice.client;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultJwtParser;

public class JWTsUtil {

	private JWTsUtil() {
	}

	public static JWTsUtil createInstance() {
		return new JWTsUtil();
	}

	public boolean isRefreshTokenSSOExpired(String token) {
		if(token == null || token.isEmpty()) {
			return true;
		}
		try {
			Claims claims = decodeTokenClaimsNotSecret(token);
			return isJwtExpirado(claims);
		}catch (ExpiredJwtException e) {
			return true;
		}
		
	}
	
	public Boolean isJwtExpirado(Claims claims) {
		final Date expiration = claims.getExpiration();
		return expiration.before(new Date());
	}
	
	public Claims decodeTokenClaimsNotSecret(String token) {
		String[] splitToken = token.split("\\.");
		String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";

		DefaultJwtParser parser = new DefaultJwtParser();
	        Jwt<?, ?> jwt = parser.parse(unsignedToken);
	        Claims claims = (Claims) jwt.getBody();
		return claims;
	}
	
}
