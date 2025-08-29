package br.jus.csjt.pje.view.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.home.AbstractJusPostulandiHome;
import br.com.infox.cliente.home.DocumentoPessoaHome;
import br.com.infox.cliente.home.PessoaHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.CertificadoLog;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.JusPostulandiManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.DocumentoPessoaManager;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.CadastroUsuarioAction;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPFCNJ;
import br.jus.pje.nucleo.entidades.DocumentoPessoa;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.SexoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

/**
 * Classe para cadastro de jus Postulandi
 */
@Name(CadastroJusPostulandiHome.NAME)
@BypassInterceptors
public class CadastroJusPostulandiHome extends AbstractJusPostulandiHome<PessoaFisica> implements ArquivoAssinadoUploader{

	public static final String NAME = "cadastroJusPostulandiHome";
	private static final LogProvider log = Logging.getLogProvider(CadastroJusPostulandiHome.class);
	private static final long serialVersionUID = 1L;

	private Integer ultimoIdInserido;
	private Date dataInicio;
	private Date dataFim;
	private Endereco endereco = new Endereco();
	private boolean existeInconsistencia = false;
	private boolean veracidadeCadastro = false;
	private boolean exibePopUpInconsistencia = false;
	private StringBuffer inconsistenciasOab;
	private StringBuffer inconsistenciasReceita;
	private boolean verificarNome = true;
	private boolean verificarCPF = true;
	private boolean verificarDadosCertificado = true;
	private DocumentoPessoa documentoPessoa;
	private String email1;
	private String certChain;
	private String signature;
	private Estado estado;
	private String termoLido;
	private String certChainStringLog;
	private Boolean ocorreuErroWsReceita = false;
	private Boolean possuiComprovanteCadastro = false;
	private DocumentoPessoa termoConfirmacaoCadastro;
	private ArrayList<ParAssinatura> assinaturas;
	
	private JusPostulandiManager jusPostulandiManager;
	

	public static CadastroJusPostulandiHome instance() {
		return ComponentUtil.getComponent(CadastroJusPostulandiHome.NAME);
	}

	@Override
	public void newInstance() {
		super.newInstance();

		TipoPessoa tipoJusPostulandi = ParametroUtil.instance().getTipoPessoaJusPostulandi();
		getInstance().setTipoPessoa(tipoJusPostulandi);
		getInstance().setValidado(false);
		ocorreuErroWsReceita = false;
	}

	public boolean validaReceita() {
		boolean validou = true;
		ConsultaClienteReceitaPFCNJ clienteReceitaPF = new ConsultaClienteReceitaPFCNJ();
		DadosReceitaPessoaFisica receitaPessoaFisica = null;
		try {
			receitaPessoaFisica = clienteReceitaPF.consultaDadosSemLogin(getInstance().getNumeroCPF(), false);

			if (this.verificarNome) {
				if (!StringUtil.getUsAscii(receitaPessoaFisica.getNome()).equalsIgnoreCase(
						StringUtil.getUsAscii(getInstance().getNome()))) {
					if (inconsistenciasReceita.length() > 0) {
						inconsistenciasReceita.append(", ");
					}
					inconsistenciasReceita.append("Nome");
					validou = false;
				}
			}
			if (!receitaPessoaFisica.getDataNascimento().equals(getInstance().getDataNascimento())) {
				if (inconsistenciasReceita.length() > 0) {
					inconsistenciasReceita.append(", ");
				}
				inconsistenciasReceita.append("Data de Nascimento");
				validou = false;
			}

			if (!StringUtil.getUsAscii(receitaPessoaFisica.getNomeMae()).equalsIgnoreCase(
					StringUtil.getUsAscii(getInstance().getNomeGenitora()))) {
				if (inconsistenciasReceita.length() > 0) {
					inconsistenciasReceita.append(", ");
				}
				inconsistenciasReceita.append("Nome da Mãe");
				validou = false;
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.WARN, e.getMessage());
			log.warn("Erro no webservice receita: " + e.getMessage(), e);

			if (verificarCPF) {
				if (inconsistenciasReceita.length() > 0) {
					inconsistenciasReceita.append(", ");
				}
				inconsistenciasReceita.append("Sem conexão com a Receita Federal");
			}
			return false;
		}
		return validou;
	}

	public void validarCadastro() {
		existeInconsistencia = false;
		verificarDadosCertificado = true;
		exibePopUpInconsistencia = false;
		if (!veracidadeCadastro) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"O campo que confirma veracidade das informações é obrigatório.");
			return;
		}

		if (Strings.isEmpty(getInstance().getAssinatura())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"É obrigado assinar o cadastro do jus postulandi.");
			return;
		}

		try {
			VerificaCertificadoPessoa.verificaCertificadoPessoa(getInstance().getCertChain(), getInstance());
		} catch (CertificadoException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Erro de validação do certificado: " + e.getMessage());
			return;
		}

		inconsistenciasOab = new StringBuffer();
		inconsistenciasReceita = new StringBuffer();

		if (validaReceita()) {

			try {
				ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroJusPostulandi();
				PessoaFisica pessoaJusPostulandi = getInstance();
				String documentoHtml = md.getModeloDocumento();
				
				CadastroUsuarioAction cadUsuarioAction = getComponent("cadastroUsuarioAction");
				cadUsuarioAction.setPessoa(getInstance());
				
				documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);

				getInstance().setValidado(Boolean.TRUE);
				getInstance().setAtivo(Boolean.TRUE);
				getInstance().setDataValidacao(new Date());

				String msgPersist = persist();
				if (Strings.isEmpty(msgPersist)) {
					return;
				}
				DocumentoPessoa docPessoa = gerarTermodeCompromisso(pessoaJusPostulandi, documentoHtml);

				Redirect.instance().setViewId("/csjt/PessoaJusPostulandi/termoCompromissoHTML.seam");
				Redirect.instance().setParameter("id", docPessoa.getIdDocumentoPessoa());
				Redirect.instance().execute();

				this.existeInconsistencia = false;
			} catch (Exception e) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"Erro ao cadastrar jus Postulandi: " + e.getMessage());
				e.printStackTrace();
			}

		} else {
			if (verificarDadosCertificado)
				this.existeInconsistencia = true;
		}
	}

	public DocumentoPessoa gerarTermodeCompromisso(PessoaFisica pessoaJusPostulandi, String documentoHtml) {
		DocumentoPessoaHome documentoPessoaHome = DocumentoPessoaHome.instance();
		documentoPessoaHome.newInstance();
		DocumentoPessoa docPessoa = documentoPessoaHome.getInstance();
		docPessoa.setDocumentoHtml(documentoHtml);
		docPessoa.setAtivo(Boolean.TRUE);
		docPessoa.setDataInclusao(new Date());
		docPessoa.setPessoa(pessoaJusPostulandi);
		docPessoa.setUsuarioCadastro(pessoaJusPostulandi);
		TipoProcessoDocumento termoCompromisso = ParametroUtil.instance()
				.getTipoProcessoDocumentoTermoCompromissoJusPostulandi();
		docPessoa.setTipoProcessoDocumento(termoCompromisso);
		String msg = documentoPessoaHome.persist();
		if (msg != null) {
			return docPessoa;
		} else {
			return null;
		}
	}

	public void validarComInconsistencias() {
		TipoProcessoDocumento tipoProcessoDocumentoInconsistencia = ParametroUtil.instance()
				.getTipoProcessoDocumentoInconsistenciaJusPostulandi();
		if (tipoProcessoDocumentoInconsistencia == null) {
			FacesMessages fc = FacesMessages.instance();
			fc.clear();
			fc.add(Severity.INFO, "Parametro nao cadastrado");
		}
		ModeloDocumento md = ParametroUtil.instance().getModeloDocumentoInconsistenciaJusPostulandi();
		PessoaFisica pessoaJusPostulandi = getInstance();
		String documentoHtml = md.getModeloDocumento();
		documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);
		DocumentoPessoaHome documentoPessoaHome = DocumentoPessoaHome.instance();

		getInstance().setValidado(Boolean.FALSE);
		getInstance().setAtivo(Boolean.FALSE);

		String msgPersist = persist();
		if (Strings.isEmpty(msgPersist)) {
			return;
		}
		documentoPessoaHome.newInstance();
		DocumentoPessoa docPessoa = documentoPessoaHome.getInstance();
		docPessoa.setDocumentoHtml(documentoHtml);
		docPessoa.setAtivo(Boolean.TRUE);
		docPessoa.setDataInclusao(new Date());
		docPessoa.setPessoa(pessoaJusPostulandi);
		docPessoa.setUsuarioCadastro(pessoaJusPostulandi);
		docPessoa.setTipoProcessoDocumento(tipoProcessoDocumentoInconsistencia);
		documentoPessoaHome.persist();
		this.documentoPessoa = docPessoa;
		this.exibePopUpInconsistencia = true;
		this.existeInconsistencia = false;
		FacesMessages fc = FacesMessages.instance();
		fc.clear();
		fc.add(Severity.INFO, "Cadastro realizado com sucesso. O jus postulandi deverá se dirigir a Sessão / Subseção"
				+ " Judiciária mais próxima para sanar inconsistências no cadastro.");
	}

	public String persist(boolean ignoraAusenciaEndereco) {
		String persist = null;
			PessoaFisica pessoaJusPostulandi = getInstance();
			
			try {
				jusPostulandiManager = (JusPostulandiManager)getComponent(JusPostulandiManager.NAME);
				jusPostulandiManager.persist(pessoaJusPostulandi);
			} catch (PJeBusinessException e1) {
				reportMessage(e1);
				return null;
			}
			
			logGravar(pessoaJusPostulandi);
			boolean cadastraEndereco = true;
			ultimoIdInserido = pessoaJusPostulandi.getIdUsuario();

			FacesMessages fm = FacesMessages.instance();

			EntityManager em = getEntityManager();
			if (cadastraEndereco) {
				endereco.setDataAlteracao(new Date());
				endereco.setUsuario(pessoaJusPostulandi);
				em.persist(endereco);
				pessoaJusPostulandi.getEnderecoList().add(endereco);
				logGravar(endereco);
				
				try {
					jusPostulandiManager = (JusPostulandiManager)getComponent(JusPostulandiManager.NAME);
					jusPostulandiManager.persist(pessoaJusPostulandi);
				} catch (PJeBusinessException e) {
					reportMessage(e);
					return null;
				}
			}
			UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
			PessoaLocalizacao pessoaLocalizacao = new PessoaLocalizacao();

			Localizacao localizacao = new Localizacao();
			localizacao.setAtivo(Boolean.TRUE);
			if (endereco != null && endereco.getIdEndereco() != 0)
				localizacao.setEndereco(endereco);
			localizacao.setLocalizacao(LocalizacaoUtil.formataLocalizacaoJusPostulandi(pessoaJusPostulandi));

			em.persist(localizacao);
			logGravar(localizacao);

			usuarioLocalizacao.setUsuario(pessoaJusPostulandi);
			usuarioLocalizacao.setLocalizacaoFisica(localizacao);
			usuarioLocalizacao.setResponsavelLocalizacao(Boolean.TRUE);
			usuarioLocalizacao.setPapel(ParametroUtil.instance().getPapelJusPostulandi());
			em.persist(usuarioLocalizacao);
			logGravar(usuarioLocalizacao);

			pessoaLocalizacao.setLocalizacao(localizacao);
			pessoaLocalizacao.setPessoa(pessoaJusPostulandi);
			em.persist(pessoaLocalizacao);
			logGravar(pessoaLocalizacao);

			EntityUtil.flush(em);

			persist = "persisted";
			fm.add(Severity.INFO, "Jus Postulandi cadastrado com sucesso");
		return persist;
	}
	
	@Override
	public String update() {
		if(beforePersistOrUpdate()){
			jusPostulandiManager = (JusPostulandiManager)getComponent(JusPostulandiManager.NAME);
			try {
				jusPostulandiManager.persistAndFlush(getInstance());
				setInstance(getInstance());
				PessoaHome.instance().atualizarNomeLocalizacao(getInstance());
				updatedMessage();
				return afterPersistOrUpdate("update");
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}
		
		return null;
	}

	@Override
	public String persist() {
		return this.persist(false);
	}

	private void logGravar(Object obj) {
		log.info("Gravando: " + LogUtil.toStringFields(obj));
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		if (id != null) {
			setarPossuiComprovanteCadastro();
		}
		
		setTab();
	}
	

	public void setUltimoIdInserido(Integer ultimoIdInserido) {
		this.ultimoIdInserido = ultimoIdInserido;
	}

	public Integer getUltimoIdInserido() {
		return ultimoIdInserido;
	}

	public void confirmarCadastro() {
		getInstance().setValidado(Boolean.TRUE);
		getInstance().setAtivo(Boolean.TRUE);
		getInstance().setDataValidacao(new Date());
		super.update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Usuário validado com sucesso.");
	}

	public boolean canNewInstance() {
		return !(new br.com.itx.component.Util().isAjaxRequest()) && !isManaged();
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setTab() {
		super.setTab("form");
	}

	public SexoEnum[] getSexoValues() {
		return SexoEnum.values();
	}

	public void setExisteInconsistencia(boolean existeInconsistencia) {
		this.existeInconsistencia = existeInconsistencia;
	}

	public boolean getExisteInconsistencia() {
		return existeInconsistencia;
	}

	public void setVeracidadeCadastro(boolean veracidadeCadastro) {
		this.veracidadeCadastro = veracidadeCadastro;
	}

	public boolean getVeracidadeCadastro() {
		return veracidadeCadastro;
	}

	public void setExibePopUpInconsistencia(boolean exibePopUpInconsistencia) {
		this.exibePopUpInconsistencia = exibePopUpInconsistencia;
	}

	public boolean getExibePopUpInconsistencia() {
		return exibePopUpInconsistencia;
	}

	public void setDocumentoPessoa(DocumentoPessoa documentoPessoa) {
		this.documentoPessoa = documentoPessoa;
	}

	public DocumentoPessoa getDocumentoPessoa() {
		return documentoPessoa;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getEmail1() {
		return email1;
	}

	public String getCamposDivergencia() {
		StringBuilder sb = new StringBuilder();
		if (inconsistenciasOab != null && inconsistenciasOab.length() > 0) {
			sb.append("OAB: ");
			sb.append(inconsistenciasOab.toString());
			sb.append(". ");
		}
		if (inconsistenciasReceita != null && inconsistenciasReceita.length() > 0) {
			sb.append("Receita Federal: ");
			sb.append(inconsistenciasReceita.toString());
			sb.append(".");
		}
		return sb.toString();
	}

	public String getCamposDivergenciaReceita() {
		StringBuilder sb = new StringBuilder();
		if (inconsistenciasReceita != null && inconsistenciasReceita.length() > 0) {
			sb.append("Receita Federal: ");
			sb.append(inconsistenciasReceita.toString());
			sb.append(".");
		}
		return sb.toString();
	}

	public String getCamposDivergenciaOab() {
		StringBuilder sb = new StringBuilder();
		if (inconsistenciasOab != null && inconsistenciasOab.length() > 0) {
			sb.append("OAB: ");
			sb.append(inconsistenciasOab.toString());
			sb.append(".");
		}
		return sb.toString();
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setTermoLido(String termoLido) {
		this.termoLido = termoLido;
	}

	public String getTermoLido() {
		return termoLido;
	}

	public void setCertChainStringLog(String certChainStringLog) {
		this.certChainStringLog = certChainStringLog;
	}

	public String getCertChainStringLog() {
		return certChainStringLog;
	}

	public void executeLogCertificadoInvalido() {
		String msg = "User: " + LogUtil.toStringForLog(getInstance()) + " certChainStringLog: " + certChainStringLog;
		log.warn(msg);
		CertificadoLog.executeLog(msg);
	}

	public boolean getLogradouroDisabled() {
		if (endereco == null || endereco.getCep() == null || endereco.getCep().getNumeroCep() == null) {
			return true;
		} else if (endereco.getNomeLogradouro() == null) {
			return false;
		} else {
			return true;
		}
	}

	public void consultaDadosReceitaWebService(String cpf) throws Exception {
		String cpfConsulta = InscricaoMFUtil.retiraMascara(cpf);

		try {
			ConsultaClienteReceitaPFCNJ consultaClienteReceitaPF = ComponentUtil.getComponent(ConsultaClienteReceitaPFCNJ.NAME);
			DadosReceitaPessoaFisica dados = consultaClienteReceitaPF.consultaDados(cpfConsulta, true);
			if (dados == null)
				FacesMessages.instance().add(Severity.ERROR,
						"A Receita Federal não retornou dados para esta pessoa jus postulandi.");
			else
				FacesMessages.instance().add(Severity.INFO, "Dados atualizados com sucesso.");

		} catch (Exception e) {
			ocorreuErroWsReceita = true;
		}
	}

	public void removeDados(String idUsuario) throws Exception {
		if (this.exists(idUsuario)) {
			this.getInstance().setAssinatura(null);
			this.getInstance().setCertChain(null);
			super.update();
		}
	}

	public Boolean exists(String idUsuario) {
		int id = Integer.parseInt(idUsuario);
		UsuarioLogin usuarioLogin = EntityUtil.find(UsuarioLogin.class, id);
		if (usuarioLogin != null)
			return true;
		else
			return false;
	}

	public Boolean getOcorreuErroWsReceita() {
		return ocorreuErroWsReceita;
	}

	public void setOcorreuErroWsReceita(Boolean ocorreuErroWsReceita) {
		this.ocorreuErroWsReceita = ocorreuErroWsReceita;
	}
	
	public void continuarCadastroReceita(){
		ocorreuErroWsReceita = false;
	}
	
	public void gerarNovaSenha(){
		//setando como nulo, o manager inativa a senha atual, gera nova senha e hash de ativacao
		getInstance().setSenha(null);
		if(getInstance().getIdUsuario() == null){
			persist();
		}else{
			update();
		}
		
		try {
			UsuarioService usuarioService = getComponent("usuarioService");
			usuarioService.enviarEmailSenha(getInstance());
			reportMessage("pje.pessoaFisicaHome.info.emailEnviadoComSucesso", null, getInstance().getEmail());
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}
	}
	
	public DocumentoPessoa getTermoConfirmacaoCadastro() {
		return termoConfirmacaoCadastro;
	}
	
	public void setTermoConfirmacaoCadastro(DocumentoPessoa termoConfirmacaoCadastro) {
		this.termoConfirmacaoCadastro = termoConfirmacaoCadastro;
	}
	
	public Boolean getPossuiComprovanteCadastro(){
		return possuiComprovanteCadastro;
	}
	
    private void setarPossuiComprovanteCadastro(){
    	DocumentoPessoa documento = ComponentUtil.getComponent(DocumentoPessoaManager.class).getUltimoTermoCompromissoJusPostulandi(getInstance());    	
    	possuiComprovanteCadastro = documento != null && documento.getAssinatura() != null;
    }
	
	public void preparaTermoConfirmacaoCadastro() {
		DocumentoPessoa documento = ComponentUtil.getComponent(DocumentoPessoaManager.class).getUltimoTermoCompromissoJusPostulandi(getInstance());
		
		if (documento == null) {
			ModeloDocumento md = ParametroUtil.instance().getModeloComprovanteCadastroJusPostulandi();
			String documentoHtml = md.getModeloDocumento();
			
			CadastroUsuarioAction.carregarVariaveisDeSessao(getInstance());
			CadastroUsuarioAction cadUsuarioAction = getComponent("cadastroUsuarioAction");
			cadUsuarioAction.setPessoa(getInstance());
			
			documentoHtml = ProcessoDocumentoHome.processarModelo(documentoHtml);

			termoConfirmacaoCadastro = new DocumentoPessoa();
			termoConfirmacaoCadastro.setPessoa(getInstance());
			termoConfirmacaoCadastro.setDocumentoHtml(documentoHtml);
			termoConfirmacaoCadastro.setAtivo(true);
			termoConfirmacaoCadastro.setDataInclusao(new Date());
			termoConfirmacaoCadastro.setUsuarioCadastro(Authenticator.getPessoaLogada());
			TipoProcessoDocumento termoCompromisso = ParametroUtil.instance().getTipoProcessoDocumentoTermoCompromissoJusPostulandi();
			termoConfirmacaoCadastro.setTipoProcessoDocumento(termoCompromisso);
		} else {
			termoConfirmacaoCadastro = documento;
		}
	}
	
	public String getEncodedCertChain(){
		return certChain;
	}

	public void setEncodedCertChain(String certChain){
		this.certChain = certChain;
	}	
	
	public List<ParAssinatura> getAssinaturas(){
		if(assinaturas == null && getTermoConfirmacaoCadastro() != null){
			String contents = null;
			
			try {
				contents = new String(SigningUtilities.base64Encode(getTermoConfirmacaoCadastro()
										.getDocumentoHtml().getBytes()));
			} catch (IOException e) {
				reportMessage(e);
			}
			
			ParAssinatura assinatura = new ParAssinatura();
			assinatura.setConteudo(contents);
			assinaturas = new ArrayList<ParAssinatura>(0);
			assinaturas.add(assinatura);
		}

		return assinaturas;
	}
	
	public void finalizarMultiplos(){
		if(assinaturas != null){
			getTermoConfirmacaoCadastro().setAssinatura(assinaturas.get(0).getAssinatura());
			getTermoConfirmacaoCadastro().setCertChain(getEncodedCertChain());
			
			DocumentoPessoaManager docPessoaManager = getComponent("documentoPessoaManager");
			
			try {
				docPessoaManager.persistAndFlush(getTermoConfirmacaoCadastro());
				
				JusPostulandiManager jusPostulandiManager = getComponent(JusPostulandiManager.NAME);
				setInstance(jusPostulandiManager.refresh(getInstance()));
				refreshGrid("documentoPessoaJusPostulandiGrid");
				possuiComprovanteCadastro = true;
				FacesMessages.instance().add(Severity.INFO, "Termo assinado com sucesso!");				
			} catch (PJeBusinessException e) {
				reportMessage(e);
			}
		}else{
			FacesMessages.instance().add(Severity.ERROR, "Nao foi possivel asinar termo. Assinaturas ainda nao preparadas.");
		}
	}
	
	public class ParAssinatura{
		private String conteudo;
		private String assinatura;
		public String getConteudo() {
			return conteudo;
		}
		public void setConteudo(String conteudo) {
			this.conteudo = conteudo;
		}
		public String getAssinatura() {
			return assinatura;
		}
		public void setAssinatura(String assinatura) {
			this.assinatura = assinatura;
		}
	}
	
	/**
	 * metodo responsavel por verificar se o cep foi preenchido incorretamente (nulo ou em branco).
	 * controla a edicao de campos na tela.
	 * @return true se endereco.cep for nulo ou se o campo numeroCEP for nulo ou vazio.
	 */
	public boolean isCepNulo() {
		return Util.isEnderecoCepNulo(this.endereco);
	}
	
	public void finalizarPJeOfficeAssinador(){
		termoConfirmacaoCadastro.setAssinatura(getSignature());
		termoConfirmacaoCadastro.setCertChain(getCertChain());
		
		DocumentoPessoaManager docPessoaManager = getComponent("documentoPessoaManager");
		
		try {
			docPessoaManager.persistAndFlush(getTermoConfirmacaoCadastro());
			
			JusPostulandiManager pessoaJuspostulandiManager = getComponent(JusPostulandiManager.NAME);
			setInstance(pessoaJuspostulandiManager.refresh(getInstance()));
			refreshGrid("documentoPessoaGrid");
			possuiComprovanteCadastro = true;
			refreshGrid("documentoPessoaJusPostulandiGrid");
			FacesMessages.instance().add(Severity.INFO, "Termo assinado com sucesso!");				
		} catch (PJeBusinessException e) {
			reportMessage(e);
		}		

	}
	
	public String getUrlDocsField() {
		if(getTermoConfirmacaoCadastro() != null){
			DocumentoJudicialService djs = ComponentUtil.getComponent(DocumentoJudicialService.NAME);
			return djs.getDownloadLink(getTermoConfirmacaoCadastro().getDocumentoHtml());
		} else{
			return null;
		}
	}
	

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		setCertChain(arquivoAssinadoHash.getCadeiaCertificado());
		setSignature(arquivoAssinadoHash.getAssinatura());
		
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
}
