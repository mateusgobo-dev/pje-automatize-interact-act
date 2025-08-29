package br.com.infox.pje.action;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.PushUtil;
import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.service.EmailService;
import br.com.infox.pje.manager.PessoaPushManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.enums.SexoEnum;


@Name(ConfirmarCadastroPushAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConfirmarCadastroPushAction {
	public static final String NAME = "confirmarCadastroPushAction";

	private PessoaPush pessoaPush;
	private String senha;
	private String confimaSenha;
	private boolean exibeModal;
	private boolean concorda;
	private String message;
	
	@RequestParameter(value="hash")
	private String hash;

	@In
	private EmailService emailService;

	@In
	private PessoaPushManager pessoaPushManager;
	
	@In
	private CadastroTempPushManager cadastroTempPushManager;
	
	@In
	private UsuarioService usuarioService;
	
	/**
	 * Método de inicialização. Responsável por verificar a validade do código de hash e 
	 * inicializar a variável de instância {@link ConfirmarCadastroPushAction#pessoaPush}. 
	 */
	@Create
	public void init() {
		if (this.hash != null) {
			CadastroTempPush cadastroTempPush = this.cadastroTempPushManager.recuperarCadastroTempPushByHash(this.hash);
			if (cadastroTempPush == null || cadastroTempPush.getDtExpiracao().before(DateService.instance().getDataHoraAtual())) {
				this.message = FacesUtil.getMessage("entity_messages", "pje.push.codigoInvalidoExpirado");
			} else if (cadastroTempPush.getConfirmado()) {
				this.message = FacesUtil.getMessage("entity_messages", "pje.push.cadastroConfirmado");
			} else {
				this.pessoaPush = new PessoaPush(
					cadastroTempPush.getNrDocumento(), cadastroTempPush.getTipoDocumentoIdentificacao(), cadastroTempPush.getDsEmail());
		    }
		} else {
			if (Authenticator.getPessoaLogada() != null) {
				this.message = FacesUtil.getMessage("entity_messages", "pje.push.semAcesso");
			} else {
				this.pessoaPush = this.pessoaPushManager.recuperarPessoaPushByLogin(Identity.instance().getCredentials().getUsername());
				
				CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
				cepSuggestBean.setDefaultValue(this.pessoaPush.getCep());
			}
		}
	}
	

	/**
	 * Método responsável por atualizar as informações de endereço do usuário push.
	 */
	public void atualizarDadosEndereco(){
		CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
		Cep cep = cepSuggestBean.getInstance();
		this.pessoaPush.setCep(cep.getNumeroCep());
		this.pessoaPush.setMunicipio(cep.getMunicipio());
		this.pessoaPush.setEndereco(cep.getNomeLogradouro());
		this.pessoaPush.setBairro(cep.getNomeBairro());		
		this.pessoaPush.setComplemento(cep.getComplemento());
		this.pessoaPush.setNumeroEndereco(cep.getNumeroEndereco());
	}
	
	/**
	 * metodo responsavel por verificar se o cep já está inserido
	 * @return
	 */
	public boolean isCepNuloOuVazio() {
		return StringUtils.isBlank(this.pessoaPush.getCep());
	}
	
	/**
	 * Método responsável por atualizar as informações do usuário push.
	 */
	public void atualizar() {
		if (StringUtils.isNotBlank(this.senha) || StringUtils.isNotBlank(this.confimaSenha)) {
			if (isSenhaValida()) {
				pessoaPush.setSenha(PjeUtil.instance().hashSenha(this.senha));
			} else {
				return;
			}
		}
		try {
			persistirDados(this.pessoaPush);
			FacesMessages.instance().add(Severity.INFO, "O seu cadastro no PJe PUSH foi atualizado com sucesso.");
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro ao processar a operação: " + ex.getMessage());
		}
	}
	
	/**
	 * Método responsável por confirmar as informações do usuário push.
	 */
	public void confirmar() {
		if (isDadosValidos()) {
			try {
				confirmarCadastroTemporario();
				this.pessoaPush.setSenha(PjeUtil.instance().hashSenha(this.senha));
				persistirDados(this.pessoaPush);
				FacesMessages.instance().add(Severity.INFO, "O seu cadastro no PJe PUSH foi realizado com sucesso.");

				redirecionar("/Push/loginPush.xhtml");
			} catch (PJeBusinessException ex) {
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro ao processar a operação: " + ex.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Método responsável por verificar se os dados de cadastro do usuário push são válidos.
	 * 
	 * @return Verdadeiro se os dados são válidos. Falso, caso contrário.
	 */
	private boolean isDadosValidos() {
		if (this.pessoaPush.getCep() == null){
			FacesMessages.instance().add(Severity.ERROR, "CEP inválido.");
			return false;
		}
		return isSenhaValida();
	}
	
	/**
	 * Método responsável por verificar a validade da senha informada.
	 * 
	 * @return Verdadeiro se a senha for válida. Falso, em caso contrário.
	 */
	private boolean isSenhaValida() {
		if (!this.senha.equals(this.confimaSenha)) {
			FacesMessages.instance().add(Severity.ERROR, "As senhas são diferentes.");
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
	 * @throws PJeBusinessException Caso algum erro ocorra durante a gravação.
	 */
	private void persistirDados(PessoaPush pessoaPush) throws PJeBusinessException {
		pessoaPushManager.persistAndFlush(pessoaPush);
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
	 * Método responsável por confirmar o cadastro do usuário no serviço push.
	 * @throws PJeBusinessException Caso algum erro ocorra durante a gravação.
	 */
	private void confirmarCadastroTemporario() throws PJeBusinessException {
		CadastroTempPush cadastroTempPush = this.cadastroTempPushManager.recuperarCadastroTempPushByLogin(this.pessoaPush.getNrDocumento());
		if (cadastroTempPush != null) {
			cadastroTempPush.setConfirmado(true);
			pessoaPush.setCadastroTempPush(cadastroTempPush);
			cadastroTempPushManager.persistAndFlush(cadastroTempPush);			
		} else {
			throw new PJeBusinessException("Cadastro temporário não encontrado.");
		}
	}

	/**
	 * Método responsável sinalizar que a modal será fechada.
	 */
	public void fecharModal() {
		this.exibeModal = false;
	}

	// GETTERs AND SETTERs
	
	public PessoaPush getPessoaPush() {
		return pessoaPush;
	}

	public void setPessoaPush(PessoaPush pessoaPush) {
		this.pessoaPush = pessoaPush;
	}

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

	public boolean isConcorda() {
		return concorda;
	}

	public void setConcorda(boolean concorda) {
		this.concorda = concorda;
	}
	
	public String getMessage() {
		return message;
	}
	
	public SexoEnum[] getSexoEnumValues() {
		return SexoEnum.values();
	}
	
	public boolean isManaged() {
		return this.pessoaPush != null && this.pessoaPush.getIdPessoaPush() != null;
	}

}
