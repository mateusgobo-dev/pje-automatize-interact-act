/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.security.Identity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.identity.PjeIdentity;
import br.jus.cnj.pje.nucleo.manager.UsuarioMobileManager;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioMobile;


@Name(TokenAcessoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class TokenAcessoAction implements Serializable {

	private static final long serialVersionUID = -3527537500356632826L;
	
	public static final String NAME = "tokenAcessoAction";
	
	private String token;
	
	private List<String> mensagens = new ArrayList<String>();
	
	@In
	private UsuarioMobileManager usuarioMobileManager;
	
	@End
	public void cancelar(){
		((PjeIdentity)Identity.instance()).setCancelouToken(true);
		Redirect redir = Redirect.instance();
		redir.setViewId("/home.xhtml");
		redir.execute();
		return;
	}
	
	public void confirmarDados(){
		
		if ( token==null || token.trim().length()==0 ) {
			mensagens.clear();
			mensagens.add("Informe o token");
			return;
		}
		
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		
		try {
			
			List<UsuarioMobile> dispositivosPareados =  ComponentUtil.getComponent(UsuarioMobileManager.class).listaUsuarioMobile(usuarioLogado);
			
			Date data = new Date();
			
			for (UsuarioMobile dispositivo: dispositivosPareados) {
				String tokenAtual = usuarioMobileManager.gerarTokenTempo(dispositivo.getCodigoPareamento(), data);
				
				if ( tokenAtual.equals( this.token ) ) {
					((PjeIdentity)Identity.instance()).setTokenValido(true);
					((PjeIdentity)Identity.instance()).setLogouComCertificado(true);
					Redirect redir = Redirect.instance();
					redir.setViewId("/home.xhtml");
					redir.execute();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			mensagens.clear();
			mensagens.add("No foi possível verificar o token");
			return;
		}
		
		mensagens.clear();
		mensagens.add("Token inválido");

	}
	
	public List<String> getMensagens() {
		return mensagens;
	}

	public void setMensagens(List<String> mensagens) {
		this.mensagens = mensagens;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	

}
