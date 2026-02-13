package br.jus.cnj.pje.webservice.mobile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenUtil {
	public static DadosJwt getDadosFromJwt(String token) {
		Claims claims = getAllClaimsFromToken(token);
		String dadosToken = claims.getSubject();
		
        Gson gson = new GsonBuilder().create();

		return gson.fromJson(dadosToken, DadosJwt.class);
	}
	
	private static Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(ParametroUtil.instance().getMobileTokenSecret()).parseClaimsJws(token).getBody();
	}
	
	public static Date getIssuedAtDateFromToken(String token) {
		Claims claims = getAllClaimsFromToken(token);
		return claims.getIssuedAt();
	}

	public static Boolean validateToken(String token) {
		return !isJwtExpirado(token);
	}

	public static Date getDataExpiracao(String token) {
		Claims claims = getAllClaimsFromToken(token);
		return claims.getExpiration();
	}

	private static Boolean isJwtExpirado(String token) {
		final Date expiration = getDataExpiracao(token);
		return expiration.before(new Date());
	}

	public static String gerarJwt(UsuarioMobile usuarioMobile) {
        Map<String, Object> claims = new HashMap<String, Object>();
        DadosJwt dadosToken = new DadosJwt(usuarioMobile.getCodigoPareamento());
        return gerarJwt(claims, dadosToken);
    }
	
	
	private static Date calcularDataExpiracao(Date createdDate) {
		Long tempoExpira = ParametroUtil.instance().getMobileTokenTempoExpiracao();
        return new Date(createdDate.getTime() +  tempoExpira * 60000 );
	}
	
 

    private static String gerarJwt(Map<String, Object> claims, DadosJwt dadosToken) {
        final Date createdDate = new Date();
        final Date expirationDate = calcularDataExpiracao(createdDate);

        Gson gson = new GsonBuilder().create();
        
        String subject = gson.toJson(dadosToken);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, ParametroUtil.instance().getMobileTokenSecret())
                .compact();
    }
    
   
}
