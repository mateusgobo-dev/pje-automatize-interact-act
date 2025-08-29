package br.com.infox.pje.action;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.view.GenericCrudAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.DocumentoPessoaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioMobileManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioMobile;


@Name(UsuarioMobileAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class UsuarioMobileAction extends GenericCrudAction<UsuarioMobile> {

	private static final long serialVersionUID = 1L;
	
	private String codigoQrCode;
	private String certChainStringLog;
	private String idDocumentoPessoaChain;
	private String cpfTela;
	private String emailTela;
	private String alpha;
	private String certificado;
	private String arquivo;
	private String protocol;
	private List<UsuarioMobile> lista;
	
	private UsuarioMobile usuarioMobile = new UsuarioMobile();
	

	public static final String NAME = "usuarioMobileAction";
	
	@In
	private UsuarioMobileManager usuarioMobileManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private DocumentoPessoaManager documentoPessoaManager;
	
	
	private boolean cadastroQrCode;


	public UsuarioMobileAction(){
		setTab("searchForm");
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		if (usuarioLogado != null){
			this.emailTela = usuarioLogado.getEmail();
			
			if(Authenticator.getPessoaLogada() != null){
				PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager = ComponentUtil.getComponent(PessoaDocumentoIdentificacaoManager.NAME);
				PessoaDocumentoIdentificacao cpfUsuarioLogado = pessoaDocumentoIdentificacaoManager.obterDocumentoCpfPessoa(Authenticator.getPessoaLogada());
				if (cpfUsuarioLogado != null){
					this.cpfTela = cpfUsuarioLogado.getNumeroDocumento();
				}
			}
		}
		
	}
	
	
	/**
	 * 
	 * @param id
	 */
	public void setarInstancia(Integer id) {
		setTab("preForm");
		super.setIdInstance(id);
	}
	
	/**
	 * 
	 * @param instance
	 */
	public void inativar(UsuarioMobile instance){
		instance.setAtivo(false);
		update(instance);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
	}
	
	/**
	 * 
	 * @param instance
	 */
	@Override
	public void setInstance(UsuarioMobile instance) {
		setTab("preForm");
		if(instance != null && instance.getIdUsuarioMobile() != null){
			this.alpha = instance.getCodigoPareamento();
			EntityUtil.getEntityManager().refresh(instance);
		}
	//	super.setInstance(instance);
	}
	

	@Override
	public void onClickFormTab() {
		super.onClickFormTab();
	}

	@Override
	public void newInstance() {
		setUsuarioMobile(new UsuarioMobile());
		setCertChainStringLog(null);
		super.newInstance();
	}
	
	@Override
	public void setTab(String tab) {
		if("searchGrid".equals(tab)){
			newInstance();
		}
		super.setTab(tab);
	}


	@Override
	public void persist() {
		super.persist();
	}

	@Override
	public void update() {
		super.update();
	}
	
	public void refresh() {
		EntityUtil.getEntityManager().refresh(getInstance());
	}
	
	/**
	 * @throws IOException 
	 * @throws PJeException 
	 * 
	 */
	public void gerarCodigoPareamento() throws IOException, PJeException{
		String chave = RandomStringUtils.random(20);
		this.alpha = new Base32().encodeAsString( DigestUtils.getSha1Digest().digest(chave.getBytes()) );
		
		while(usuarioMobileManager.checkCodigoPareamento(this.alpha)){
			chave = RandomStringUtils.random(20);
			this.alpha = new Base32().encodeAsString( DigestUtils.getSha1Digest().digest(chave.getBytes()) );
		}
		
		usuarioMobile = new UsuarioMobile();
		usuarioMobile.setCodigoPareamento(this.alpha);
		usuarioMobile.setUsuario(Authenticator.getUsuarioLogado());
		usuarioMobile.setAtivo(Boolean.TRUE);
		usuarioMobile.setPareamentoRealizado(Boolean.FALSE);
		usuarioMobile.setDataCadastro(new Date());
		EntityUtil.getEntityManager().persist(usuarioMobile);
		EntityUtil.getEntityManager().flush();
		try {
			UsuarioService usuarioService = (UsuarioService) Component.getInstance("usuarioService");
			usuarioService.enviarEmailCodigoPareamento(usuarioMobile);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "E-mail com cdigo de pareamento enviado com sucesso");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		} catch (Exception ex) {
			FacesMessages.instance().add(Severity.ERROR, ex.getMessage());
		}
		this.lista = null;
	}
	
	
	public List<UsuarioMobile> listaUsuarioMobile(Usuario usuario){
		return usuarioMobileManager.listaUsuarioMobile(usuario);
	}
	
	public String getCodigoQrCode() {
		return codigoQrCode;
	}

	public void setCodigoQrCode(String codigoQrCode) {
		this.codigoQrCode = codigoQrCode;
	}
	
	public UsuarioMobile getUsuarioMobile() {
		return usuarioMobile;
	}

	public void setUsuarioMobile(UsuarioMobile usuarioMobile) {
		this.usuarioMobile = usuarioMobile;
	}

	public String getCertChainStringLog() {
		return certChainStringLog;
	}

	public void setCertChainStringLog(String certChainStringLog) {
		this.certChainStringLog = certChainStringLog;
	}

	public String getIdDocumentoPessoaChain() {
		return idDocumentoPessoaChain;
	}

	public void setIdDocumentoPessoaChain(String idDocumentoPessoaChain) {
		this.idDocumentoPessoaChain = idDocumentoPessoaChain;
	}
	
	public boolean isCadastroQrCode() {
		return cadastroQrCode;
	}

	public void setCadastroQrCode(boolean cadastroQrCode) {
		this.cadastroQrCode = cadastroQrCode;
	}

	public String getCpfTela() {
		return cpfTela;
	}

	public void setCpfTela(String cpfTela) {
		this.cpfTela = cpfTela;
	}

	public String getEmailTela() {
		return emailTela;
	}

	public void setEmailTela(String emailTela) {
		this.emailTela = emailTela;
	}

	public String getAlpha() {
		return alpha;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}

	
	public String getCertificado() {
		return certificado;
	}


	public void setCertificado(String certificado) {
		this.certificado = certificado;
	}


	public String getArquivo() {
		return arquivo;
	}


	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}


	public List<UsuarioMobile> carregarLista() {
		return  listaUsuarioMobile(Authenticator.getUsuarioLogado());
	}

	public void inactive(UsuarioMobile usuarioMobile) {
		UsuarioMobileManager usuarioManager = ComponentUtil.getComponent(UsuarioMobileManager.class);
		usuarioManager.inativarUsuarioMobile(usuarioMobile);
	}
	
	public boolean usuarioMobilePareado() {
		UsuarioMobileManager usuarioManager = ComponentUtil.getComponent(UsuarioMobileManager.class);
		UsuarioMobile um = usuarioManager.recuperarUsuarioMobile(cpfTela, emailTela, alpha);
		return usuarioManager.usuarioMobilePareado(um);
	}

	public List<UsuarioMobile> getLista() {
		this.lista = carregarLista();	
		return lista;
	}


	public void setLista(List<UsuarioMobile> lista) {
		this.lista = lista;
	}


	public String getProtocol() {
		return protocol;
	}


	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	
}

