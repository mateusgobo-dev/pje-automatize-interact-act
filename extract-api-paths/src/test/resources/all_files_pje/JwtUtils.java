package br.jus.pje.api.controllers.v1.migrador;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Response;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Parametros;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtils {
	
	public static Response validarToken(@HeaderParam("Authorization") String authorization) {

		if (authorization == null || !authorization.startsWith("Bearer ")) {
			 return montarResposta(Response.Status.UNAUTHORIZED, "Token ausente ou inválido");
		}

		String token = authorization.substring(7);

		try {

			Jws<Claims> claims = Jwts.parser().setSigningKey(ParametroUtil.getParametro(Parametros.MIGRACAO_CHAVE_TOKEN)).parseClaimsJws(token);
			return null;

		} catch (ExpiredJwtException e) {
	        return montarResposta(Response.Status.UNAUTHORIZED, "Token expirado");
	    } catch (JwtException e) { 
	        return montarResposta(Response.Status.UNAUTHORIZED, "Token inválido");
	    } catch (Exception e) { 
	        return montarResposta(Response.Status.UNAUTHORIZED, "Token inválido");
	    }
	}
	
	public static String generateToken(String login) {

		return Jwts.builder()
				.claim("login", login)
				.claim("nome", Authenticator.getUsuarioLogado().getNome())
				.claim("email", Authenticator.getUsuarioLogado().getEmail())
				.claim("token_type", "Bearer")
				.setExpiration(gerarDataExpiracaoToken())
				.setHeaderParam("typ", "JWT")
				.signWith(SignatureAlgorithm.HS256, ParametroUtil.getParametro(Parametros.MIGRACAO_CHAVE_TOKEN)).compact();
	}
	
	private static Response montarResposta(Response.Status status, String mensagem) {
		Map<String, Object> responseBody = new HashMap<String, Object>();
		responseBody.put("mensagem", mensagem);
		return Response.status(status).entity(responseBody).build();
	}
	
	public static Date gerarDataExpiracaoToken() {
		
		Calendar data = Calendar.getInstance();
		data.set(Calendar.HOUR_OF_DAY, 23);
		data.set(Calendar.MINUTE, 59);
		data.set(Calendar.SECOND, 59);
		data.set(Calendar.MILLISECOND, 999);
		Date expirationDate = data.getTime();
		return expirationDate;
	}
	
	public static Long calcularExpiresIn(Date expirationDate) {

        long nowInSeconds = Instant.now().getEpochSecond();
        long expirationInSeconds = expirationDate.toInstant().getEpochSecond();
        return expirationInSeconds - nowInSeconds;
    }
}
