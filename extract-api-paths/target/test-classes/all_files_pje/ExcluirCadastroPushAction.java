package br.com.infox.pje.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.manager.PessoaPushManager;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPushManager;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.ProcessoPush;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ExcluirCadastroPushAction.NAME)
@Scope(ScopeType.PAGE)
public class ExcluirCadastroPushAction {
	public static final String NAME = "excluirCadastroPushAction";
	
	private String nrDocumento;
	private String email;
	private String senha;
	private boolean documentoCPF = true;
	
	@In
	private PessoaPushManager pessoaPushManager;
	
	@In
	private ProcessoPushManager processoPushManager;
	
	@In
	private CadastroTempPushManager cadastroTempPushManager;

	/**
	 * Método responsável por realizar a exclusão do usuário push.
	 */
	public void confirmar() {
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
			try {
				excluirPreCadastro(pessoaPush.getNrDocumento());
				excluirProcessosPush(processoPushManager.recuperarProcessosPush(pessoaPush, null));
				excluirPessoaPush(pessoaPush);
				
				EntityUtil.flush();
				
				FacesMessages.instance().add(Severity.INFO, "Cadastro excluído com sucesso.");
				inicializarVariaveis();
			} catch (PJeBusinessException ex) {
				FacesMessages.instance().add(Severity.ERROR, "Ocorreu um erro ao processar a operação: " + ex.getMessage());
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Usuário ou senha inválidos.");
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
	
	/**
	 * Método responsável por excluir (fisicamente) os processos de um usuario push da sua lista de processos do push.
	 * 
	 * @param processosPush Lista de processos que serão excluídos.
	 * @throws PJeBusinessException Caso algum erro ocorra durante a exclusão.
	 */
	private void excluirProcessosPush(List<ProcessoPush> processosPush) throws PJeBusinessException {
		for (ProcessoPush processoPush : processosPush) {
			this.processoPushManager.remove(processoPush);
		}
	}
	
	/**
	 * Método responsável por excluir (fisicamente) uma {@link PessoaPush}.
	 * 
	 * @param pessoaPush {@link PessoaPush}.
	 * @throws PJeBusinessException Caso algum erro ocorra durante a exclusão.
	 */
	private void excluirPessoaPush(PessoaPush pessoaPush) throws PJeBusinessException {
		this.pessoaPushManager.remove(pessoaPush);
	}
	
	/**
	 * Método responsável por excluir (fisicamente) o pré cadastro de um usuário push.
	 * 
	 * @param login Login do usuario no serviço push.
	 * @throws PJeBusinessException Caso algum erro ocorra durante a exclusão.
	 */
	private void excluirPreCadastro(String login) throws PJeBusinessException {
		CadastroTempPush cadastroTempPush = cadastroTempPushManager.recuperarCadastroTempPushByLogin(login);
		cadastroTempPushManager.remove(cadastroTempPush);
	}		
	
	/**
	 * Método responsável por inicializar os valores das variáveis de instância da classe.
	 */
	private void inicializarVariaveis() {
		this.nrDocumento = null;
		this.email = null;
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

	public boolean isDocumentoCPF() {
		return documentoCPF;
	}

	public void setDocumentoCPF(boolean documentoCPF) {
		this.documentoCPF = documentoCPF;
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
	
}
