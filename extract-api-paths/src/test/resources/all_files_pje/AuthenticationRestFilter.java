/**
 * AuthenticationRestFilter.java
 *
 * Data: 07/08/2019
 */
package br.jus.cnj.pje.webservice.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

/**
 * @author Adriano Pamplona
 *
 */
@Name (AuthenticationRestFilter.NAME)
public class AuthenticationRestFilter implements ClientRequestFilter{
	
	public static final String NAME = "authenticationRestFilter";
	
	public static final String PRE_AUTH_TOKEN_HEADER = "X-pje-pre-auth-token"; 
	
	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {	
		requestContext.getHeaders().add(PRE_AUTH_TOKEN_HEADER, gerarToken());
	}

	private String gerarToken() {
		SecurityTokenControler stc = SecurityTokenControler.instance();

		UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();

		String token = usuarioLocalizacao == null ? null
				: stc.gerarChaveAcessoGenerica(String.valueOf(usuarioLocalizacao.getIdUsuarioLocalizacao()));

		return token;
	}
}
