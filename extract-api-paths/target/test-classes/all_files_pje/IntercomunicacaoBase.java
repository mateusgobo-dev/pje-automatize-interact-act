package br.jus.cnj.pje.intercomunicacao.v223.servico;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.intercomunicacao.v223.beans.ManifestacaoProcessual;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(IntercomunicacaoBase.NAME)
@Scope(ScopeType.EVENT)
public class IntercomunicacaoBase extends IntercomunicacaoAbstract {
	
	public static final String NAME = "intercomunicacaoBase";

	@Override
	protected void autenticar(Object requisicao) throws LoginException {
		Usuario usuarioSistema = ParametroUtil.instance().getUsuarioSistema();
		if(usuarioSistema != null) {
			((ManifestacaoProcessual)requisicao).setIdManifestante(usuarioSistema.getLogin());
			((ManifestacaoProcessual)requisicao).setSenhaManifestante(usuarioSistema.getSenha());
			Authenticator.instance().autenticaManualmenteNoSeamSecurity("sistema");
			Contexts.getSessionContext().set(Authenticator.USUARIO_LOGADO, usuarioSistema);
			Identity identity = Identity.instance();
			if(identity != null){
				identity.addRole(Papeis.CONSULTA_MNI);
			}
			atribuirLogin(requisicao);
		}
	}

	@Override
	protected void finalizarChamadaSeam() {
		try {
			Identity.instance().logout();
		} catch (Exception e) {
			log.error("Não foi possível finalizar a chamada seam: " + e.getLocalizedMessage());
		}
	}
	
	@Override
	protected HttpServletRequest getRequest() {
		return null;
	}

}
