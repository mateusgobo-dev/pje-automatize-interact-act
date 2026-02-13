package br.com.infox.pje.action;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaPushManager;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.util.StringUtil;

@Name(LoginPushAction.NAME)
@Scope(ScopeType.PAGE)
public class LoginPushAction {
	public static final String NAME = "loginPushAction";

	private String nrDocumento;
	private String email;
	private String senha;
	private boolean documentoCPF = true;
	
	@In
	private PessoaPushManager pessoaPushManager;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private Identity identity;

	/**
	 * Método responsável por realizar a autenticação do usuário push.
	 */
	public void login() {
		String login = null;
		
		if (StringUtils.isNotBlank(this.nrDocumento)) {
			if (isNumeroDocumentoValido(this.nrDocumento)) {
				login = this.nrDocumento;
			}
		} else if (StringUtils.isNotBlank(this.email)) {
			login = this.email;
		}
		
		PessoaPush pessoaPush = pessoaPushManager.recuperarPessoaPushByLogin(login);
		if (pessoaPush != null && pessoaPushManager.authenticate(pessoaPush, this.senha)) {
			identity.acceptExternallyAuthenticatedPrincipal(new SimplePrincipal(login));
			identity.getCredentials().setUsername(login);
			identity.addRole("/pages/Push/listView.seam");
			identity.addRole("/pages/Push/confirmarCadastro.seam");
			
			Contexts.getSessionContext().set(Authenticator.PESSOA_PUSH_LOGADA, pessoaPush);
			
			Redirect redirect = Redirect.instance();
			redirect.setViewId("/Push/listView.xhtml");
			redirect.execute();
		} else {
			FacesMessages.instance().add(Severity.INFO, "Usuário ou senha inválidos!");
		}
	}
	
	/**
	 * Método responsável por validar o número do documento informado pelo usuário.
	 * 
	 * @param numeroDocumento Número do documento.
	 * @return Verdadeiro se o número do documento for válido. Falso, caso contrário.
	 */
	private boolean isNumeroDocumentoValido(String numeroDocumento) {
		return InscricaoMFUtil.validarCpfCnpj(numeroDocumento);
	}

	// GETTERs AND SETTERs
	
	public String getNrDocumento() {
		return nrDocumento;
	}

	public void setNrDocumento(String nrDocumento) {
		if (!nrDocumento.equals(StringUtil.CPF_EMPTYMASK) && !nrDocumento.equals(StringUtil.CNPJ_EMPTYMASK)) {
			this.nrDocumento = nrDocumento;
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public boolean isDocumentoCPF() {
		return documentoCPF;
	}

	public void setDocumentoCPF(boolean documentoCPF) {
		this.documentoCPF = documentoCPF;
	}

}
