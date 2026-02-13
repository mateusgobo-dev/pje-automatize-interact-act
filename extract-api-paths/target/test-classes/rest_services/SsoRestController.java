package br.jus.cnj.pje.api.controllers.v1;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.webservice.criminal.dto.PJePapel;
import br.jus.cnj.pje.webservice.criminal.dto.PjeUser;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name(SsoRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1")
public class SsoRestController {

	public static final String NAME = "ssoRestController";
	
	private static final String ALGORITHM = "AES";
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager;
	
    @GET
    @Path("/usuarios/sso/{login}")
    @Produces(MediaType.APPLICATION_JSON)
	public Response recuperarUsuario(@Context HttpServletRequest request, @PathParam("login") String login) {
		PjeUser user = new PjeUser();
		
		login = this.decrypt(login, ConfiguracaoIntegracaoCloud.getSSOClientSecret());
		
		Usuario usuario = this.usuarioService.findByLogin(login);

		if(usuario != null) {
			user = new PjeUser(usuario.getLogin(), usuario.getSenha(), !usuario.getBloqueio() && usuario.getAtivo(), usuario.getNome(), usuario.getEmail());
		}
		
		return Response.ok(user).build(); 
	}

	@GET
	@Path("/usuarios/sso/{login}/papeis")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recuperarPapeisUsuario(@Context HttpServletRequest request, @PathParam("login") String login) {
		login = this.decrypt(login, ConfiguracaoIntegracaoCloud.getSSOClientSecret());
		Usuario usuario = this.usuarioService.findByLogin(login);

		if (usuario != null) {
			return Response.ok(this.recuperarPapeis(usuario)).build();
		}

		return Response.ok(Collections.emptyList()).build();
	}
    
    @GET
    @Path("/usuarios/sso")
    @Produces(MediaType.APPLICATION_JSON)
	public Response validarLogin(@Context HttpServletRequest request, @HeaderParam("x-pje-sso-auth") String auth) {
		PjeUser user = new PjeUser();
		
		String[] credentials = this.getUserCredentials(auth);
		
		Usuario usuario = this.usuarioService.findByLogin(credentials[0]);
		if(this.usuarioManager.authenticate(usuario, credentials[1])) {
			user = new PjeUser(usuario.getLogin(), usuario.getSenha(), !usuario.getBloqueio() && usuario.getAtivo(), usuario.getNome(), usuario.getEmail());
		}
		
		return Response.ok(user).build(); 
	}
    
    private String[] getUserCredentials(String authString){
        
        String decodedAuth = "";
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[0];
        byte[] bytes = Base64.getUrlDecoder().decode(authInfo);
        decodedAuth = new String(bytes);
         
        return decodedAuth.split(":");
    }
    
	public String decrypt(final String encryptedValue, final String secretKey) {
		String decryptedValue = null;
		try {

			final Key key = generateKeyFromString(secretKey);
			final Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			final byte[] decorVal = Base64.getUrlDecoder().decode(encryptedValue);
			final byte[] decValue = c.doFinal(decorVal);
			decryptedValue = new String(decValue);
		} catch (Exception ex) {
			System.out.println("The Exception is=" + ex);
		}

		return decryptedValue;
	} 
	
	private Key generateKeyFromString(final String secKey) throws Exception {
		final byte[] keyVal = secKey.replaceAll("-", "").getBytes();
	    final Key key = new SecretKeySpec(keyVal, ALGORITHM);
	    return key;
	}
	
	private Set<PJePapel> recuperarPapeis(Usuario usuario) {
		List<UsuarioLocalizacao> uls = this.usuarioLocalizacaoManager.recuperarLocalizacoes(usuario.getIdUsuario());
		Set<PJePapel> papeis = new HashSet<>();

		for (UsuarioLocalizacao usuarioLocalizacao : uls) {
			papeis.add(new PJePapel(usuarioLocalizacao.getPapel().getNome(),
					usuarioLocalizacao.getPapel().getIdentificador()));
		}

		return papeis;
	}

}
