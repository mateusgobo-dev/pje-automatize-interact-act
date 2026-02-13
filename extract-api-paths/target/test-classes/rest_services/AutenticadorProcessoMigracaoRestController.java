package br.jus.pje.api.controllers.v1.migrador;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(AutenticadorProcessoMigracaoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1/migracao/auth/token/migracao-processo")
public class AutenticadorProcessoMigracaoRestController {

	public static final String NAME = "autenticadorProcessoMigracaoRestController";

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@FormParam("username") String username, @FormParam("password") String password) {
		
	    try {
	    	
	        if (authenticate(username, password) != null) {
	            String token = JwtUtils.generateToken(username);
	            Map<String, String> response = new HashMap<>();
	            response.put("access_token", token);
	            response.put("expires_in", JwtUtils.calcularExpiresIn(JwtUtils.gerarDataExpiracaoToken()).toString()); 
            	response.put("token_type", "Bearer"); 
	            return Response.ok(response).build();
	        } else {
	            throw new LoginException();
	        }
	    } catch (Exception e) {
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("Erro", "Credenciais inválidas");
	        return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse).build();
	    }
	}

	private Usuario authenticate(String username, String password) throws LoginException {
		
		Authenticator authenticator = (Authenticator) Component.getInstance(Authenticator.class, true);

		return authenticator.authenticateMigration(username, password);
	}
}
