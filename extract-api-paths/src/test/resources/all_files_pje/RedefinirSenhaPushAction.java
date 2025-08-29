package br.com.infox.pje.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.PushUtil;
import br.com.infox.pje.manager.PessoaPushManager;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.PessoaPush;

@Name(RedefinirSenhaPushAction.NAME)
@Scope(ScopeType.PAGE)
public class RedefinirSenhaPushAction {
	public static final String NAME = "redefinirSenhaPushAction";

	private PessoaPush pessoaPush;
	private String senha;
	private String confimaSenha;
	private boolean exibeModal;
	private String message;
	
	@RequestParameter(value="hash")
	private String hash;
	
	@In
	private PessoaPushManager pessoaPushManager;
	
	@In
	private CadastroTempPushManager cadastroTempPushManager;
	
	@In
	private UsuarioService usuarioService;
	
	/**
	 * Método de inicialização. Responsável por verificar a validade do código de hash e 
	 * inicializar a variável de instância {@link RedefinirSenhaPushAction#pessoaPush}. 
	 */
	@Create
	public void init() {
		if (this.hash != null) {
			CadastroTempPush cadastroTempPush = this.cadastroTempPushManager.recuperarCadastroTempPushByHash(this.hash);
			if (cadastroTempPush != null) {
				this.pessoaPush = pessoaPushManager.recuperarPessoaPushByHash(this.hash);
			} else {
				this.message = FacesUtil.getMessage("entity_messages", "pje.push.urlReativacaoInvalida");
			}
		} else {
			this.message = FacesUtil.getMessage("entity_messages", "pje.push.urlReativacaoInvalida");
		}
	}

	/**
	 * Método responsável por gravar a nova senha.
	 */
	public void gravar() {
		if (isSenhaValida()) {
			pessoaPush.setSenha(PjeUtil.instance().hashSenha(this.senha));
			persistirDados(this.pessoaPush);

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Senha redefinida com sucesso.");

			redirecionar("/Push/loginPush.xhtml");
		}
	}
	
	/**
	 * Método responsável por verificar a validade da senha informada.
	 * 
	 * @return Verdadeiro se a senha for válida. Falso, em caso contrário.
	 */
	private boolean isSenhaValida() {
		if (!this.senha.equals(this.confimaSenha)) {
			FacesMessages.instance().add(Severity.ERROR, "As senha são diferentes.");
			return false;
		}
		if (!PushUtil.validarSenha(this.senha, this.pessoaPush.getNome())) {
			this.exibeModal = true;
			return false;
		}
		return true;
	}
	
	/**
	 * Método responsável por persistir o objeto {@link PessoaPush}.
	 * 
	 * @param pessoaPush {@link PessoaPush}.
	 */
	private void persistirDados(PessoaPush pessoaPush) {
		EntityUtil.getEntityManager().persist(pessoaPush);
		EntityUtil.flush();
	}

	/**
	 * Método responsável por cancelar a operação de redefinição de senha.
	 */
	public void cancelar() {
		redirecionar("/Push/loginPush.xhtml");
	}
	
	/**
	 * Método responsável por redirecionar o usuário.
	 * 
	 * @param viewId Identificador da View.
	 */
	private void redirecionar(String viewId) {
		Redirect redirect = Redirect.instance();
		redirect.setViewId(viewId);
		redirect.execute();
	}
	
	/**
	 * Método responsável sinalizar que a modal será fechada.
	 */
	public void fecharModal() {
		this.exibeModal = false;
	}

	// GETTERs AND SETTERs

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfimaSenha() {
		return confimaSenha;
	}

	public void setConfimaSenha(String confimaSenha) {
		this.confimaSenha = confimaSenha;
	}

	public boolean isExibeModal() {
		return exibeModal;
	}

	public String getMessage() {
		return message;
	}

}
