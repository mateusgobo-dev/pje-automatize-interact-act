package br.com.infox.pje.action;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.PushUtil;
import br.com.infox.ibpm.service.EmailService;
import br.com.infox.pje.manager.PessoaPushManager;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.manager.CadastroTempPushManager;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.PessoaPush;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

@Name(EnviarRedefinirSenhaPushAction.NAME)
@Scope(ScopeType.PAGE)
public class EnviarRedefinirSenhaPushAction {
	public static final String NAME = "enviarRedefinirSenhaPushAction";

	private String nrDocumento;
	private String email;
	private boolean documentoCPF = true;

	@In
	private EmailService emailService;
	
	@In
	private PessoaPushManager pessoaPushManager;
	
	@In
	private CadastroTempPushManager cadastroTempPushManager;
	

	/**
	 * Método responsável por proceder com a redefinição de senha do usuário.
	 */
	public void redefinir() {
		redefinir(this.nrDocumento, this.email);
	}
	
	public void redefinir(String documento, String email) {
		String login = null;
		
		if (StringUtils.isNotBlank(documento)) {
			if (isNumeroDocumentoValido(documento)) {
				login = documento;
			}
		} else if (StringUtils.isNotBlank(email)) {
			login = email;
		}
		
		PessoaPush pessoaPush = pessoaPushManager.recuperarPessoaPushByLogin(login);
		if(pessoaPush != null){
			CadastroTempPush cadastroTempPush = this.cadastroTempPushManager.recuperarCadastroTempPushByLogin(login);
			cadastroTempPush.setCdHash(PushUtil.gerarHash(cadastroTempPush.getDsEmail() + cadastroTempPush.getNrDocumento()));
			
			EntityUtil.getEntityManager().persist(cadastroTempPush);
			EntityUtil.flush();
			
			String link = new Util().getUrlProject() + "/Push/redefinirSenha.seam?hash=" + cadastroTempPush.getCdHash();
			enviarEmail(pessoaPush.getNome(), pessoaPush.getEmail(), link);
			inicializarVariaveis();
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Dados inválidos.");
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
	 * Método responsável por enviar email de redefinição de senha ao usuario.
	 * 
	 * @param nome Nome do usuário.
	 * @param email Email do usuário.
	 * @param link Link para redefinição de senha.
	 */
	private void enviarEmail(String nome, String email, String link) {
		StringBuilder textoEmail = new StringBuilder();
		textoEmail.append(String.format("Prezado Sr(a). %s,<br/><br/>", nome));
		textoEmail.append("Foi registrado no sistema uma solicitação de redefinição de senha.<br/>");
		textoEmail.append("Favor clicar no link abaixo para alterar a senha:<br/>");
		textoEmail.append(String.format("<a href=\"%s\">%s</a><br/><br/>", link, link));
		textoEmail.append("Atenciosamente,<br/>");
		textoEmail.append(String.format("%s", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase()));

		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		
		emailService.enviarEmail(usuario, "Redefinição de senha de acesso (Pje PUSH).", textoEmail.toString());
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
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isDocumentoCPF() {
		return documentoCPF;
	}

	public void setDocumentoCPF(boolean documentoCPF) {
		this.documentoCPF = documentoCPF;
	}
	
}
