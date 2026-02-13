package br.com.infox.cliente.home;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.infox.ibpm.service.EmailService;
import br.com.infox.pje.service.IntimacaoPartesService;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.view.fluxo.PreparaAtoComunicacaoAction;
import br.jus.cnj.pje.view.fluxo.ProcessoJudicialAction;
import br.jus.csjt.pje.business.pdf.GeradorPdfUnificado;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name("processoExpedienteHome")
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class ProcessoExpedienteHome extends AbstractProcessoExpedienteHome<ProcessoExpediente> implements
		IProcessoExpedienteHome {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(ProcessoExpedienteHome.class);

	private Boolean visualizarAbas = Boolean.FALSE;
	private Boolean visualizarAbaDiligenciaDoc = Boolean.FALSE;
	private Boolean visualizarAbaExpediente = Boolean.FALSE;
	private Boolean visualizarComboCentralMandado = Boolean.FALSE;
	private Boolean mostrarBtnCadastrar = Boolean.FALSE;
	private boolean mostrarAssociarPessoaExpediente;
	private String numeroProcesso;
	private String nomeDiligenciado;
	private Boolean documentoInserido = Boolean.FALSE;
	private Boolean isNewInstance = Boolean.TRUE;
	private Boolean assinado = Boolean.FALSE;
	private List<ProcessoParte> listaPP = new ArrayList<ProcessoParte>();
	private String nomeParteIntimada;
	private Boolean inserirHashPetInicial = Boolean.FALSE;
	private boolean aviso = false;
	private boolean vincularAto = false;
	private ProcessoDocumento atoMagistrado;
	private InformacaoPagina informacaoPagina = new InformacaoPagina();
	private boolean mostrarFuncaoLimpar;
	private boolean mostrarFuncaoVerificarAlteracao;
	private ProcessoExpediente expedienteNaoEnviado;
	private String erroValidacaoProcessoParte;
	private IntimacaoPartesService intimacaoPartesService;

	public IntimacaoPartesService getIntimacaoPartesService() {
		if (intimacaoPartesService == null) {
			intimacaoPartesService = getComponent(IntimacaoPartesService.NAME);
		}
		return intimacaoPartesService;
	}

	// Método chamado ao selecionar a tarefa Cadastrar Expediente no fluxo.
	public void fluxoCadastrarExpediente() {
		String idProcessoExpedienteEditar = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("idProcessoExpedienteEditar");
		if (idProcessoExpedienteEditar != null) {
			Integer idProcessoExpediente = Integer.parseInt(idProcessoExpedienteEditar);
			ProcessoExpediente processoExpedienteEditar = getEntityManager().find(ProcessoExpediente.class,
					idProcessoExpediente);
			editar(processoExpedienteEditar);
		} else {
			newInstance();
			ProcessoParteExpedienteHome.instance().newInstance();
			setIsNewInstance(Boolean.FALSE);
		}
		setVisualizarAbas(Boolean.TRUE);
	}

	// Método chamado ao selecionar a tarefa Visualizar Expedientes no fluxo.
	public void fluxoVisualizarExpediente() {
		newInstance();
		ProcessoParteExpedienteHome.instance().newInstance();
		setMostrarBtnCadastrar(Boolean.FALSE);
	}

	public void excluir(ProcessoExpediente pe) {
		pe.setDtExclusao(new Date());
		getEntityManager().merge(pe);
		getEntityManager().flush();

		setExpedienteNaoEnviado(null);
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("tipoProcessoDocumentoSuggest");
		setVincularAto(false);
		setAtoMagistrado(null);
		setMostrarBtnCadastrar(Boolean.TRUE);
		setIsNewInstance(Boolean.TRUE);
		setVisualizarAbas(Boolean.FALSE);
		setVisualizarAbaExpediente(Boolean.FALSE);
		setVisualizarAbaDiligenciaDoc(Boolean.FALSE);
		setAssinado(Boolean.FALSE);
		documentoInserido = Boolean.FALSE;

		setVisualizarComboCentralMandado(Boolean.FALSE);
		ProcessoExpedienteCentralMandadoHome.instance().setCentralMandado(null);
		
		ProcessoParteHome pph = ProcessoParteHome.instance();
		pph.newInstance();
		ProcessoDocumentoHome.instance().newInstance();
		ProcessoDocumentoHome.instance().getInstance().setProcessoDocumentoBin(null);
		TipoDiligenciaHome.instance().newInstance();

		super.newInstance();
		if (getInstance().getTipoProcessoDocumento() != null) {
			if (getInstance().getTipoProcessoDocumento().getInTipoExpediente() == TipoExpedienteEnum.C) {
				getInstance().setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.E);
			}
		}
	}

	public void iniciarNovoCadastro() {
		newInstance();
		ProcessoParteExpedienteHome.instance().newInstance();
		iniciarCadastro();
		FacesMessages.instance().clear();
		setIsNewInstance(Boolean.TRUE);
	}

	public void limpar() {
		newInstance();
		iniciarCadastro();
		setMostrarFuncaoLimpar(false);
	}

	public Boolean getVisualizarAbas() {
		return visualizarAbas;
	}

	public void setVisualizarAbas(Boolean visualizarAbas) {
		this.visualizarAbas = visualizarAbas;
	}

	public void setAssinado(Boolean assinado) {
		this.assinado = assinado;
	}

	public Boolean getAssinado() {
		return assinado;
	}

	public Boolean getVisualizarAbaDiligenciaDoc() {
		return visualizarAbaDiligenciaDoc;
	}

	public void setVisualizarAbaDiligenciaDoc(Boolean visualizarAbaDiligenciaDoc) {
		this.visualizarAbaDiligenciaDoc = visualizarAbaDiligenciaDoc;
	}

	public Boolean getVisualizarAbaExpediente() {
		return visualizarAbaExpediente;
	}

	public void setVisualizarAbaExpediente(Boolean visualizarAbaExpediente) {
		this.visualizarAbaExpediente = visualizarAbaExpediente;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		return super.beforePersistOrUpdate();
	}

	public void setNomeParteIntimada(String nomeParteIntimada) {
		this.nomeParteIntimada = nomeParteIntimada;
	}

	public String getNomeParteIntimada() {
		return nomeParteIntimada;
	}

	// Método chamado para enviar e-mail as partes, ao assinar digitalmente.
	@SuppressWarnings("unchecked")
	public void enviarEmail() {
		EmailService emailService = ComponentUtil.getComponent(EmailService.class);
		
		// verificando lista das partes.
		for (ProcessoParte processoParte : listaPP) {
			processoParte = EntityUtil.find(ProcessoParte.class, processoParte.getIdProcessoParte());
			setNomeParteIntimada(processoParte.getNomeParte());
			String body = ParametroUtil.instance().getModeloDocumentoEmailExpediente().getModeloDocumento();
			body = (String) Expressions.instance().createValueExpression(body).getValue();
	
			String tipoPessoa = processoParte.getTipoParte().getTipoParte();

			// Condição para pessoas Fisicas do tipo Advogado que possuem e-mail cadastrado.
			Pessoa pessoa = processoParte.getPessoa();
			if (pessoa.getInTipoPessoa() == TipoPessoaEnum.F) {
				if (pessoa.getEmail() != null && 
						!pessoa.getEmail().equals("") && tipoPessoa.equalsIgnoreCase("advogado")) {
					
					emailService.enviarEmail(pessoa, "Aviso de Intimação\\Citação das Partes", body);
				}
				/*
				 *  Condição para pessoas que não possuem e-mail cadastrado mas não são do tipo 
				 *  Advogado ou Procurador, onde é enviado e-mail para seu Representante.
				 */
				else {
					List<ProcessoParteRepresentante> processoParteRepresentantes = processoParte.getProcessoParteRepresentanteList();
					if (processoParteRepresentantes.size() > 0) {
						for (int i = 0; i < processoParteRepresentantes.size(); i++) {
							String tipoRepresentante = processoParteRepresentantes.get(i).getRepresentante().getTipoPessoa().getTipoPessoa();
							if (processoParteRepresentantes.get(i).getRepresentante() != null && 
									(tipoRepresentante.equalsIgnoreCase("advogado") || tipoRepresentante.equalsIgnoreCase("procurador"))) {
								
								emailService.enviarEmail(processoParteRepresentantes.get(i).getRepresentante(), 
										"Aviso de Intimação\\Citação das Partes", body);
							}
						}
					}
				}
			} else {
				// Condição para Pessoa Jurídica, onde é enviado e-mail para seus Procuradores.
				if (processoParte.getPessoa().getInTipoPessoa() == TipoPessoaEnum.J) {
					StringBuilder sb = new StringBuilder();
					sb.append("select ppp.pessoaProcurador from PessoaProcuradorProcuradoria ppp "
							+ "where ppp.pessoaProcuradoriaEntidade.pessoa IN (select ppe.pessoa from PessoaProcuradoriaEntidade ppe "
							+ "where ppe.pessoa IN (select pp.pessoa from ProcessoParte pp "
							+ "where pp.pessoa = :pessoa))");
					Query q1 = EntityUtil.getEntityManager().createQuery(sb.toString());
					q1.setParameter("pessoa", processoParte.getPessoa());

					List<PessoaProcurador> listPessoaProcurador = q1.getResultList();

					if (listPessoaProcurador.size() > 0) {
						for (int i = 0; i < listPessoaProcurador.size(); i++) {
							if (listPessoaProcurador.get(i).getEmail() != null) {								
								emailService.enviarEmail(listPessoaProcurador.get(i).getPessoa(), 
										"Aviso de Intimação\\Citação das Partes", body);
							}
						}
					}
				}
			}
		}
	}

	public void inserirHashPetInicial() {
		StringBuilder textoDocSb = new StringBuilder();
		textoDocSb.append(ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento());
		textoDocSb.append("<br/><br/>");
		textoDocSb.append("<b>" + MSG_PETICAO_INICIAL + "</b>");
		// pegando petição
		// inicial------------------------------------------------------------------------------------------------------
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumento o ");
		sb.append("where o.processo = :processo ");
		sb.append("and o.tipoProcessoDocumento = :tipoDocPetIni");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", ProcessoTrfHome.instance().getInstance().getProcesso());
		q.setParameter("tipoDocPetIni", Util.instance().eval("tipoProcessoDocumentoPeticaoInicial"));
		q.setMaxResults(1);
		ProcessoDocumento petIni = (ProcessoDocumento) EntityUtil.getSingleResult(q);
		if (petIni != null) {
			textoDocSb.append(ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(petIni.getProcessoDocumentoBin()));
		}
		textoDocSb.append("<br/><br/><br/>");
		textoDocSb.append(ParametroUtil.getFromContext("hashDocumentoUrl", true));
		ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(textoDocSb.toString());
	}

	public boolean inserirAtualizarDoc() {
		if (vincularAto) {
			if (atoMagistrado == null) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Deve haver ao menos um Ato do Magistrado selecionado.");
				return false;
			}
		}			
		
		if (getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M) && getInstance().getProcessoExpedienteDiligenciaList().size() == 0){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, MSG_SELECIONE_UMA_OU_MAIS_DILIGENCIAS);
			return false;
		}
		
		if (numeroPartesSelecionadas() == 0) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma ou mais Partes do Processo");
			return false;
		}
		try {
			VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(ProcessoDocumentoBinHome.instance()
					.getCertChain());
			boolean enviado = enviarExpediente();
			if (enviado) {
				String msgOK = "Expediente " + instance.getMeioExpedicaoExpediente().getLabel() + " enviado com sucesso.";
				
				iniciarNovoCadastro();
				
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, msgOK);
				return true;
			}			
			
			if (!documentoInserido) {
				ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome.instance();
				if (processoDocHome.persistComAssinatura() != null) {
					documentoInserido = Boolean.TRUE;
					ProcessoDocumentoBinHome.instance().assinarDocumento();
					assinado = Boolean.TRUE;
					if (inserirHashPetInicial) {
						inserirHashPetInicial();
					}

					persist();
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso !");
				}
			} else {
				ProcessoDocumentoHome.instance().update();
				ProcessoDocumentoBinHome.instance().update();
			}
			return true;
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao assinar o documento: " + e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return false;
	}

	/**
	 * 
	 * Metodo para inserir só o documento no banco de dados sem persistir o
	 * expediente.
	 * 
	 */
	public void inserirAtualizarDocDEJT() {
		try {
			VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(ProcessoDocumentoBinHome.instance()
					.getCertChain());
			if (!documentoInserido) {
				ProcessoDocumentoHome processoDocHome = ProcessoDocumentoHome.instance();

				processoDocHome.persistComAssinatura();

				documentoInserido = Boolean.TRUE;

				assinado = Boolean.TRUE;
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso !");
			} else {
				ProcessoDocumentoBinHome.instance().update();
			}
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao assinar o documento: " + e.getMessage(), e);
		}
	}

	@Override
	public String persist() {
		String persist = null;

		getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
		getInstance().setDtCriacao(new Date());

		if (ProcessoTrfHome.instance().getInstance().getOrgaoJulgador() != null) {
			if ((getInstance().getTipoProcessoDocumento().getAnexar() == null || getInstance()
					.getTipoProcessoDocumento().getAnexar())
					&& (!getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M) && !getInstance()
							.getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.P))) {
				FacesMessages.instance().add(Severity.ERROR, MSG_TIPOS_INCOMPATIVEIS);
			} else {
				if (getInstance().getProcessoDocumento() == null) {
					getInstance().setProcessoDocumento(ProcessoDocumentoHome.instance().getInstance());
				}

				getEntityManager().persist(getInstance());
				ProcessoParteExpedienteHome.instance().inserir();
				ProcessoDocumentoExpedienteHome.instance().inserirNaoAnexo(getInstance(),
						ProcessoDocumentoHome.instance().getInstance());
				/*
				 * Se o meio de envio for "Central de Mandados", deve ser
				 * habilitada as abas de "Diligência" e
				 * "Documentos para Impressão". Senão, o sistema deve voltar
				 * para a aba inicial de expediente.
				 */

				if (getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M)) {
					try{
						ProcessoExpedienteCentralMandadoHome.instance().inserirEnvioCentral(getInstance());
					} catch (Exception e){
						log.error("Erro ao persistir o Expediente: "+e.getMessage());
					}
					setVisualizarAbaDiligenciaDoc(Boolean.TRUE);
					setTab("diligenciaTab");
					FacesMessages.instance().clear();
				} else if (!getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.P)) {
					/*
					 * Foi adicionado novas opções para envio: C-Correios,
					 * D-Edital e L-Carta
					 */
					iniciarNovoCadastro();
				}
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, MSG_PROCESSO_NAO_VINCULADO_ORGAO_JULGADOR);
		}
		refreshGrid("processoParteExpedienteMenuGrid");
		return persist;
	}

	public void iniciarCadastro() {
		setVisualizarAbas(Boolean.TRUE);
		setTab("cadastrarProcessoExpedienteTab");
		ProcessoDocumentoHome.instance().newInstance();
		visualizarComboCentralMandado = false;
		ProcessoExpedienteCentralMandadoHome.instance().newInstance();		
		ProcessoParteExpedienteHome.instance().setPartesListTodos(null);

	}

	private void atualizarPartesList() {
		ProcessoParteExpedienteHome.instance().getPartesList().clear();
		for (ProcessoParte pp : ProcessoParteExpedienteHome.instance().getPartesListTodos()) {
			if (pp.getCheckado()) {
				ProcessoParteExpedienteHome.instance().getPartesList().add(pp);
			}
		}
	}

	public static ProcessoExpedienteHome instance() {
		return ComponentUtil.getComponent(ProcessoExpedienteHome.class);
	}

	public boolean gravar() {
		if (vincularAto) {
			if (atoMagistrado == null) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Deve haver ao menos um Ato do Magistrado selecionado.");
				return false;
			}
		}		

		if (numeroPartesSelecionadas() == 0) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, MSG_SELECIONE_UMA_OU_MAIS_PARTES_PROCESSO);
			return false;
		}
		
		if (!isManaged()) {
				getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
				getInstance().setDtCriacao(new Date());
				getInstance().setInTemporario(false);
				super.persist();

				FacesMessages.instance().clear();
		}
		atualizarListaPartesExpediente();

		if (getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M)) {
			try {
				ProcessoExpedienteCentralMandadoHome.instance().atualizarEnvioCentral(getInstance());
			} catch (Exception e) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
				return false;
			}
		}
		if (getInstance().getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.E) {
			erroValidacaoProcessoParte = validarRestricoesIntimacaoPartes();
			if (erroValidacaoProcessoParte != null) {
				return false;
			}

			removerPartesComPendencias();
			removerProcessoCaixaIntimacaoAutomatica();
		}
		ProcessoDocumentoBinHome.instance().setIsAssinarDocumento(false);
		atualizarNaoAnexo();

		getInstance().setProcessoDocumento(ProcessoDocumentoHome.instance().getInstance());
		getInstance().setInTemporario(false);
		getEntityManager().merge(getInstance());
		getEntityManager().flush();


		armazenarInformacoesGravadas();

		FacesMessages.instance().clear();
		if (getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M)) {
			FacesMessages.instance().add(Severity.INFO, "Expediente Central de Mandados enviado com sucesso!");
		} else {
			FacesMessages.instance().add(Severity.INFO, "Expediente gravado com sucesso!");
		}
		return true;
	}

	private void armazenarInformacoesGravadas() {
		atualizarPartesList();

		informacaoPagina.parteList = new ArrayList<ProcessoParte>();
		for (ProcessoParte pp : ProcessoParteExpedienteHome.instance().getPartesList()) {
			ProcessoParte parte = new ProcessoParte();
			parte.setCheckado(pp.getCheckado());
			parte.setPessoa(pp.getPessoa());
			parte.setPrazoLegal(pp.getPrazoLegal());
			informacaoPagina.parteList.add(parte);
		}
		informacaoPagina.atoMagistrado = atoMagistrado;
		informacaoPagina.tipoExpediente = getInstance().getTipoProcessoDocumento();
		informacaoPagina.meioExpedicao = getInstance().getMeioExpedicaoExpediente();
		informacaoPagina.documentoSigiloso = ProcessoDocumentoHome.instance().getInstance().getDocumentoSigiloso();
		informacaoPagina.modeloDocumento = ProcessoDocumentoHome.instance().getModeloDocumentoCombo();
		informacaoPagina.inserirHashPeticaoInicial = inserirHashPetInicial;
		informacaoPagina.modelo = ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento();
	}

	public boolean houveAlteracao() {
		atualizarPartesList();

		boolean parteListIgual = true;
		outer: for (ProcessoParte pp : ProcessoParteExpedienteHome.instance().getPartesList()) {
			for (ProcessoParte parte : informacaoPagina.parteList) {
				if (pp.getPessoa().equals(parte.getPessoa()) 
						&& pp.getCheckado().equals(parte.getCheckado())
						&& pp.getPrazoLegal() != null
						&& pp.getPrazoLegal().equals(parte.getPrazoLegal())) {
					continue outer;
				}
			}
			parteListIgual = false;
			break;
		}

		boolean houveAlteracao = !(parteListIgual
				&& isEqual(informacaoPagina.atoMagistrado, atoMagistrado)
				&& isEqual(informacaoPagina.tipoExpediente, getInstance().getTipoProcessoDocumento())
				&& isEqual(informacaoPagina.meioExpedicao, getInstance().getMeioExpedicaoExpediente())
				&& informacaoPagina.documentoSigiloso == ProcessoDocumentoHome.instance().getInstance()
						.getDocumentoSigiloso()
				&& isEqual(informacaoPagina.modeloDocumento, ProcessoDocumentoHome.instance().getModeloDocumentoCombo())
				&& informacaoPagina.inserirHashPeticaoInicial == inserirHashPetInicial && isEqual(
				informacaoPagina.modelo, ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento()));
		return houveAlteracao;

	}

	public void editar() {
		editar(expedienteNaoEnviado);
	}

	public void editar(ProcessoExpediente processoExpediente) {
		setId(processoExpediente.getIdProcessoExpediente());
		setInstance(processoExpediente);

		ProcessoParteExpedienteHome.instance().setPartesListTodos(null);
		List<ProcessoParte> ppList = ProcessoParteExpedienteHome.instance().getPartesListTodos();
		for (ProcessoParte pp : ppList) {
			ProcessoParteExpediente parte = getProcessoParteExpediente(pp.getPessoa());
			if (parte != null) {
				pp.setCheckado(true);
				pp.setPrazoLegal(parte.getPrazoLegal());
			} else {
				pp.setCheckado(false);
			}
		}

		if(processoExpediente.getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M)){
			List<CentralMandado> listaCentralMandado = buscaCentralMandado();
			visualizarComboCentralMandado = listaCentralMandado.size() > 1;
			
			ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = getProcessoExpedienteCentralMandado(processoExpediente);
			if (processoExpedienteCentralMandado != null) {
				ProcessoExpedienteCentralMandadoHome.instance().setCentralMandado(processoExpedienteCentralMandado.getCentralMandado());
			}
		}		
		
		ProcessoDocumentoExpediente pde = getProcessoDocumentoExpediente(processoExpediente);
		if (pde != null) {
			atoMagistrado = pde.getProcessoDocumentoAto();
			vincularAto = atoMagistrado != null;

			ProcessoDocumentoHome.instance().setId(pde.getProcessoDocumento().getIdProcessoDocumento());
			ProcessoDocumentoBinHome.instance().setId(pde.getProcessoDocumento().getProcessoDocumentoBin().getIdProcessoDocumentoBin());
		} else {
			atoMagistrado = null;
			vincularAto = false;
		}
		setIsNewInstance(false);
		armazenarInformacoesGravadas();
	}

	public ProcessoExpedienteCentralMandado getProcessoExpedienteCentralMandado(ProcessoExpediente pe) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoExpedienteCentralMandado o ");
		sb.append("where o.processoExpediente = :pe ");
		sb.append("and o.centralMandado is not null ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pe", pe);
		ProcessoExpedienteCentralMandado pecm = (ProcessoExpedienteCentralMandado) q.getResultList().get(0);
		
		return pecm;

	}	
	
	private ProcessoParteExpediente getProcessoParteExpediente(Pessoa pessoa) {
		for (ProcessoParteExpediente parte : getInstance().getProcessoParteExpedienteList()) {
			if (parte.getPessoaParte().equals(pessoa)) {
				return parte;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T refreshEntity(T obj) {
		try {
			getEntityManager().refresh(obj);
		} catch (Exception e) {
			obj = (T) getEntityManager().find(obj.getClass(), EntityUtil.getEntityIdObject(obj));
		}
		return obj;
	}

	private boolean isEqual(Object obj, Object obj2) {
		if (obj != null) {
			return obj.equals(obj2);
		}
		return obj2 == null;
	}

	private int numeroPartesSelecionadas() {
		atualizarPartesList();
		return ProcessoParteExpedienteHome.instance().getPartesList().size();
	}

	@SuppressWarnings("unused")
	private void atualizarPartesExpedienteList() {
		ProcessoParteExpedienteHome.instance().getPartesList().clear();
		List<EntityNode<ProcessoParte>> ppa = ProcessoParteHome.instance().getSearchTree2GridPoloAtivoList().getList();
		for (EntityNode<ProcessoParte> pp : ppa) {
			if (pp.getSelected()) {
				ProcessoParteExpedienteHome.instance().getPartesList().add(pp.getEntity());
			}
		}
		List<EntityNode<ProcessoParte>> ppp = ProcessoParteHome.instance().getSearchTree2GridPoloPassivoList()
				.getList();
		for (EntityNode<ProcessoParte> pp : ppp) {
			if (pp.getSelected()) {
				ProcessoParteExpedienteHome.instance().getPartesList().add(pp.getEntity());
			}
		}
		List<EntityNode<ProcessoParte>> ppo = ProcessoParteHome.instance().getSearchTree2GridOutrosParticipantesList()
				.getList();
		for (EntityNode<ProcessoParte> pp : ppo) {
			if (pp.getSelected()) {
				ProcessoParteExpedienteHome.instance().getPartesList().add(pp.getEntity());
			}
		}
	}

	private void atualizarNaoAnexo() {
		String hql = "delete from ProcessoDocumentoExpediente o where o.processoExpediente = :pe and o.anexo = false";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("pe", getInstance());
		q.executeUpdate();

		ProcessoDocumentoHome.instance().getInstance()
				.setProcessoDocumento(getInstance().getTipoProcessoDocumento().getTipoProcessoDocumento());
		ProcessoDocumentoHome.instance().getInstance()
				.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumento());
		if (ProcessoDocumentoHome.instance().isManaged()) {
			ProcessoDocumentoHome.instance().update();
		} else {
			ProcessoDocumentoHome.instance().persist();
		}

		ProcessoDocumentoExpedienteHome.instance().getInstance().setProcessoDocumentoAto(atoMagistrado);
		ProcessoDocumentoExpedienteHome.instance().inserirNaoAnexo(getInstance(),
				ProcessoDocumentoHome.instance().getInstance());
	}

	private void atualizarListaPartesExpediente() {
		String hql = "delete from tb_proc_parte_expediente where id_processo_parte_expediente = :pe";
		getEntityManager().createNativeQuery(hql).setParameter("pe", getInstance().
				getIdProcessoExpediente()).executeUpdate();
		EntityUtil.flush(getEntityManager());

		ProcessoParteExpedienteHome.instance().inserir();
	}

	public void inicializarAtoMagistrado() {
		if (!vincularAto) {
			setAtoMagistrado(null);
		} else {
			setAtoMagistrado(getAtoMagistradoList().size() > 0 ? getAtoMagistradoList().get(0) : null);
		}
	}

	public void mudarAba() {
		// PJE-JT:Desenvolvedor Haroldo Arouca :PJE-439 PJE-109
		// 2011-09-06:Alteracoes feitas pela JT
		if (getInstance().getTipoProcessoDocumento().getInTipoExpediente() == TipoExpedienteEnum.M
				|| getInstance().getTipoProcessoDocumento().getInTipoExpediente() == TipoExpedienteEnum.O) {
			getInstance().setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.M);
		}
		// PJE-JT:Fim

		List<ProcessoParte> partesSelecionadas = ProcessoParteExpedienteHome.instance().getPartesList();
		partesSelecionadas.clear();

		SearchTree2GridList<ProcessoParte> poloAtivoTree = ProcessoParteHome.instance()
				.getSearchTree2GridPoloAtivoList();
		SearchTree2GridList<ProcessoParte> poloPassivoTree = ProcessoParteHome.instance()
				.getSearchTree2GridPoloPassivoList();
		SearchTree2GridList<ProcessoParte> outrosPartTree = ProcessoParteHome.instance()
				.getSearchTree2GridOutrosParticipantesList();

		List<EntityNode<ProcessoParte>> entidades = poloAtivoTree.getList();
		entidades.addAll(poloPassivoTree.getList());
		entidades.addAll(outrosPartTree.getList());

		for (EntityNode<ProcessoParte> noh : entidades) {
			if (noh.getSelected()) {
				if (noh.getEntity().getPrazoLegal() == null)
					noh.getEntity().setPrazoLegal(0);
				noh.getEntity().setCheckado(true);
				partesSelecionadas.add(noh.getEntity());
			}
		}
		if (partesSelecionadas.size() <= 0) {
			FacesMessages.instance().add(Severity.ERROR, "Selecione uma ou mais Partes do Processo");
		} else {
			ProcessoDocumentoHome.instance().getInstance()
					.setProcessoDocumento(getInstance().getTipoProcessoDocumento().getTipoProcessoDocumento());
			ProcessoDocumentoHome.instance().getInstance()
					.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumento());
			/*
			 * PJE-JT: Rafael Carvalho: [PJE-288] - Expediente gerado não
			 * reflete as alterações do usuário: 2011-10-24 Processar novamente
			 * o modelo sempre que a aba for alterada, corrige o problema acima.
			 */

			ProcessoDocumentoHome.instance().processarModelo();

			/* PJE-JT: Fim. [PJE-288] */
			List<ProcessoParte> list = ProcessoParteExpedienteHome.instance().getPartesListTodos();
			list = new ArrayList<ProcessoParte>();
			list.addAll(partesSelecionadas);
			ProcessoParteExpedienteHome.instance().setPartesListTodos(list);
			setVisualizarAbaExpediente(Boolean.TRUE);
			setTab("enviarExpedientesTab");
		}
	}

	/**
	 * Verifica se alguma parte não teve prazo preenchido
	 * 
	 * @return true se houver alguma parte sem prazo preenchido.
	 */
	public boolean existePrazoNaoPreenchido() {
		atualizarPartesList();
		for (ProcessoParte pp : ProcessoParteExpedienteHome.instance().getPartesListTodos()) {
			if (pp.getCheckado()) {
				ProcessoParteExpedienteHome.instance().getPartesList().add(pp);
			}
		}
		for (ProcessoParte pp : ProcessoParteExpedienteHome.instance().getPartesList()) {
			if (pp.getPrazoLegal() == null) {
				return true;
			}
		}
		return false;

	}

	public boolean temExpedienteVinculado(ProcessoDocumento pd) {
		String hql = "select count(pde) from ProcessoDocumentoExpediente pde "
				+ " where pde.processoDocumento.processoDocumentoBin.certChain is not null "
				+ "  and pde.processoDocumento.processoDocumentoBin.signature is not null "
				+ "  and pde.anexo = false " + "  and pde.processoDocumentoAto = :pd";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("pd", pd);
		Long retorno = EntityUtil.getSingleResultCount(q);
		return retorno > 0;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNomeDiligenciado(String nomeDiligenciado) {
		this.nomeDiligenciado = nomeDiligenciado;
	}

	public String getNomeDiligenciado() {
		return nomeDiligenciado;
	}

	public void setMostrarBtnCadastrar(Boolean mostrarBtnCadastrar) {
		this.mostrarBtnCadastrar = mostrarBtnCadastrar;
	}

	public Boolean getMostrarBtnCadastrar() {
		return mostrarBtnCadastrar;
	}

	public void setVisualizarComboCentralMandado(Boolean visualizarComboCentralMandado) {
		this.visualizarComboCentralMandado = visualizarComboCentralMandado;
	}

	public Boolean getVisualizarComboCentralMandado() {
		return visualizarComboCentralMandado;
	}

	public void setIsNewInstance(Boolean isNewInstance) {
		this.isNewInstance = isNewInstance;
	}

	public Boolean getIsNewInstance() {
		return isNewInstance;
	}

	public void marcarDesmarcarImpresso(ProcessoExpediente obj) {
		Date data = null;
		if (!isImpresso(obj)) {
			data = new Date();
		}

		for (ProcessoDocumentoExpediente t : obj.getProcessoDocumentoExpedienteList()) {
			t.setDtImpressao(data);
			getEntityManager().merge(t);
		}
		getEntityManager().flush();
		refreshGrid("processoExpedienteSetorGrid");
	}

	public Boolean isImpresso(ProcessoExpediente obj) {
		for (ProcessoDocumentoExpediente processoDocumentoExpediente : obj.getProcessoDocumentoExpedienteList()) {
			if (processoDocumentoExpediente.getDtImpressao() == null) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	@SuppressWarnings("unchecked")
	public void verificarExpedientes() {
		String sql = "select o from ProcessoParteExpediente o "
				+ "where o.processoJudicial.processo.idProcesso = :processo";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processo", ProcessoTrfHome.instance().getInstance().getProcesso().getIdProcesso());

		List<ProcessoParteExpediente> ppeList = q.getResultList();
		for (ProcessoParteExpediente processoParteExpediente : ppeList) {
			if(processoParteExpediente.getPessoaParte() != null){
				if (processoParteExpediente.getPessoaParte().getIdUsuario().equals(Authenticator.getUsuarioLogado().getIdUsuario())){
					if (processoParteExpediente.getDtCienciaParte() == null){
						processoParteExpediente.setDtCienciaParte(new Date());
						
	        			ProcessoParteExpedienteHome.instance().cienciaIntimacao(processoParteExpediente);
						processoParteExpediente.setCienciaSistema(Boolean.FALSE);
						
						getEntityManager().merge(processoParteExpediente);
						getEntityManager().flush();
					}
				}
			}
		}
	}
	
	public boolean isDocumentosVinculados(ProcessoExpediente processoExpediente) {
		for (ProcessoDocumentoExpediente processoDocumentoExpediente : processoExpediente.getProcessoDocumentoExpedienteList()) {
			if (processoDocumentoExpediente.getAnexo()) {
				return true;
			}
		}
		return false;
	}
	
	
	public void gerarPDFDocumentosVinculados(ProcessoExpediente processoExpediente) {
		List<ProcessoDocumento> processoDocumentoList = new ArrayList<ProcessoDocumento>();

		for (ProcessoDocumentoExpediente processoDocumentoExpediente : processoExpediente.getProcessoDocumentoExpedienteList()) {
			if (processoDocumentoExpediente.getAnexo()) {  // Será gerado PDF apenas dos documentos vinculados ao expediente.
				processoDocumentoList.add(processoDocumentoExpediente.getProcessoDocumento());
			}
		}

		ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType("application/pdf");

		String filename = processoTrf.getNumeroProcesso() + ".pdf";
		
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		OutputStream out = null;
		try {
			GeradorPdfUnificado geradorPdf = new GeradorPdfUnificado();
			geradorPdf.setResurcePath(new Util().getUrlProject());
			geradorPdf.setGerarInfoClasseAtual(false);
			out = response.getOutputStream();
			geradorPdf.gerarPdfUnificado(processoTrf, processoDocumentoList, out);
			out.flush();
			getPjeUtil().registrarCookieTemporizadorDownload(response);
			facesContext.responseComplete();
		} catch (IOException ex) {
			FacesMessages.instance().add(Severity.ERROR, "Error while downloading the file: " + filename);
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private PjeUtil getPjeUtil() {
		return ComponentUtil.getComponent(PjeUtil.class);
	}
	
	/**
	 * Método responsável por retirar a pendência da manifestação caso
	 * exista expedientes com pendência de manifestação, para os perfis de
	 * Advogado e Procurador.
	 */
	public void retirarPendencia() {
		List<ProcessoParteExpediente> list = null;

		if (Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("advogado")) {
			list = listaProcessoParteExpedienteAdvogado();
		} else if (Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("procurador")
				|| Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("procChefe")) {
			list = listaProcessoParteExpedienteProcurador();
		}

		if (list != null && list.size() > 0) {
			for (ProcessoParteExpediente ppe : list) {
				ppe.setPendenteManifestacao(false);
				getEntityManager().merge(ppe);
				getEntityManager().flush();
			}
		}
	}

	/**
	 * Método que irá verificar a existência de Expedientes com
	 * pendência,para um Advogado ou Procurador,retornando true, caso exista e
	 * false, caso não exista.
	 * 
	 * @return Boolean
	 */
	public Boolean existeExpedientePendente() {
		List<ProcessoParteExpediente> list = null;

		if (Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("advogado")) {
			list = listaProcessoParteExpedienteAdvogado();
		} else if (Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("procurador")
				|| Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("procChefe")) {
			list = listaProcessoParteExpedienteProcurador();
		}

		if (list != null && list.size() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Retorna uma lista de Expedientes com pendência de manifestação,
	 * para um Advogado.
	 * 
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoParteExpediente> listaProcessoParteExpedienteAdvogado() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ppe from ProcessoParteExpediente ppe ");
		sb.append("where ppe.processoJudicial.idProcessoTrf = :processoTrf ");
		sb.append("and ppe.dtCienciaParte IS NOT NULL ");
		sb.append("and ppe.pendenteManifestacao = true ");
		sb.append("and (ppe.pessoaParte.idUsuario = :pessoaAdvogado OR ");
		sb.append("ppe.pessoaParte IN (select pp.pessoa from ProcessoParte pp "
				+ "where pp.processoTrf = ppe.processoJudicial "
				+ "and pp.inParticipacao IN (select ppr.inParticipacao "
				+ "from ProcessoParte ppr where ppr.processoTrf = pp.processoTrf "
				+ "and ppr.pessoa.idUsuario = :pessoaAdvogado ))))");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pessoaAdvogado", Authenticator.getIdUsuarioLogado());
		q.setParameter("processoTrf", ProcessoTrfHome.instance().getInstance().getProcesso().getIdProcesso());

		return q.getResultList();

	}

	/**
	 * Retorna uma lista de Expedientes com pendência de manifestação,
	 * para um Procurador.
	 * 
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoParteExpediente> listaProcessoParteExpedienteProcurador(ProcessoTrf processoJudicial) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o ");
		sb.append("  from ProcessoParteExpediente o ");
		sb.append(" where o.pessoaParte in (select ppp.pessoaProcuradoriaEntidade.pessoa ");
		sb.append("                           from PessoaProcuradorProcuradoria ppp ");
		sb.append("                          where ppp.pessoaProcurador.idUsuario = :pessoaProcurador) ");
		sb.append("   and o.processoJudicial.idProcessoTrf = :processoTrf ");
		sb.append("   and o.dtCienciaParte is not null ");
		sb.append("   and o.pendenteManifestacao = true");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pessoaProcurador", Authenticator.getIdUsuarioLogado());

		q.setParameter("processoTrf", processoJudicial.getIdProcessoTrf());

		return q.getResultList();

	}
	
	private List<ProcessoParteExpediente> listaProcessoParteExpedienteProcurador() {
		ProcessoTrf processoJudicial = null;
		ProcessoJudicialAction pja = ComponentUtil.getComponent(ProcessoJudicialAction.class);
		if(pja.getProcessoJudicial() != null){
			processoJudicial = pja.getProcessoJudicial();
		} else if(ProcessoTrfHome.instance().getInstance() != null){
			processoJudicial = ProcessoTrfHome.instance().getInstance(); 
		}
		
		if(processoJudicial != null){
			return listaProcessoParteExpedienteProcurador(processoJudicial);
		} else {
			return new ArrayList<ProcessoParteExpediente>(0);
		}
	}


	public ProcessoDocumento getProcessoDocumentoExpediente(ProcessoExpedienteCentralMandado pecm) {
		String sql = "select o.processoDocumento from ProcessoDocumentoExpediente o "
				+ "where o.anexo = false and o.processoExpediente = :processoExpediente";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processoExpediente", pecm.getProcessoExpediente());
		q.setMaxResults(1);
		return (ProcessoDocumento) EntityUtil.getSingleResult(q);
	}

	public ProcessoDocumentoExpediente getProcessoDocumentoExpediente(ProcessoExpediente pe) {
		String sql = "select o from ProcessoDocumentoExpediente o "
				+ "where o.anexo = false and o.processoExpediente = :processoExpediente";
		Query q = getEntityManager().createQuery(sql);
		q.setParameter("processoExpediente", pe);
		q.setMaxResults(1);
		return (ProcessoDocumentoExpediente) EntityUtil.getSingleResult(q);
	}

	/**
	 * Método que une todos os documentos do processo expediente na impressão de documentos no painel do oficial de justiça.
	 * 
	 * @param idProcessoExpediente
	 */
	@SuppressWarnings("unchecked")
	public void juntaDocumentosImpressaoCentralMandados(Integer idProcessoExpediente) {
		StringBuilder modelo = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumentoExpediente o ");
		sb.append("where o.processoExpediente.idProcessoExpediente = :idProcessoExpediente ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoExpediente", idProcessoExpediente);
		List<ProcessoDocumentoExpediente> pdeList = q.getResultList();
		for (ProcessoDocumentoExpediente processoDocumentoExpediente : pdeList) {
			modelo.append("<b>Tipo do Documento:</b> "+processoDocumentoExpediente.getProcessoDocumento().getTipoProcessoDocumento());
			modelo.append("<br />");
			modelo.append("<b>Descrição:</b> "+processoDocumentoExpediente.getProcessoDocumento().getProcessoDocumento());
			modelo.append("<br />");
			modelo.append("<br />");
			modelo.append(processoDocumentoExpediente.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
			modelo.append("<br />");
			modelo.append("<br />");
			modelo.append("=================================================");
			modelo.append("<br />");
		}
		ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(modelo.toString());

	}	
	
	private String listToString(Collection<?> c) {
		return c.toString().replaceAll("\\[", "").replaceAll("\\]", "");
	}

	/**
	 * Método que verifica para qual central de mandado o expediente poderá
	 * ser enviado. Se houver mais de uma central para a localização atual
	 * do usuário, é exibida a combo de central de mandado para que o
	 * usuário escolher a central. Caso só exista uma central de mandado, o
	 * expediente será enviado pra ela. E caso não exista centrais de
	 * mandados para a localização atual do usuário, é exibida uma
	 * mensagem de erro ao usuário.
	 */
	public void verificaCentrais() {
		List<CentralMandado> listaCentralMandado = buscaCentralMandado();
		if (getInstance().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.M)) {
			if (listaCentralMandado.size() > 1) {
				visualizarComboCentralMandado = true;
			} else if (listaCentralMandado.size() == 1) {
				CentralMandado centralMandado = listaCentralMandado.get(0);
				ProcessoExpedienteCentralMandadoHome.instance().setCentralMandado(centralMandado);
			} else if (listaCentralMandado.size() == 0) {
				if(ParametroUtil.instance().isPrimeiroGrau()){
					FacesMessages.instance().add(Severity.ERROR, MSG_SEM_CENTRAL_MANDADO_1_GRAU);
				}
				else{
					FacesMessages.instance().add(Severity.ERROR, MSG_SEM_CENTRAL_MANDADO_2_GRAU);
				}
				visualizarComboCentralMandado = false;
				getInstance().setMeioExpedicaoExpediente(null);
				return;
			}
		} else {
			visualizarComboCentralMandado = Boolean.FALSE;
		}
		if (!isManaged()) {
			getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			getInstance().setDtCriacao(new Date());
			getInstance().setInTemporario(true);
			super.persist();

			FacesMessages.instance().clear();
		}
	}

	/***
	 * Metódo responsável por obter a lista de centais de mandado de um
	 * determinado órgão julgador
	 * 
	 * @return List<CentralMandado>
	 */
	@SuppressWarnings("unchecked")
	public List<CentralMandado> buscaCentralMandado() {
		LocalizacaoService ls = ((LocalizacaoService)Component.getInstance("localizacaoService", ScopeType.EVENT));
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		Localizacao l = null;
		if(oj != null) {
			l = oj.getLocalizacao();
		} else {
			l = Authenticator.getLocalizacaoAtual();
		}
		List<Localizacao> hierarquia = ls.getHierarchy(l);
		if(hierarquia.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sql = new StringBuilder("SELECT DISTINCT o FROM CentralMandado o ")
				.append("JOIN FETCH o.centralMandadoLocalizacaoList l ")
				.append("WHERE l.localizacao IN (:hierarquia) AND o.ativo = true ")
				.append("ORDER BY o.centralMandado");
		
		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("hierarquia", hierarquia);
		return q.getResultList();
	}
	
	/***
	 * Metódo responsável por obter a lista de centais de mandado cadastrados no
	 * Sistema. Retorna apenas as centrais que possuem localização cadastrada.
	 * 
	 * @return List<CentralMandado>
	 */
	@SuppressWarnings("unchecked")
	public List<CentralMandado> buscaTodasCentraisMandados() {
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct cm from CentralMandado cm ");
		hql.append("inner join cm.centralMandadoLocalizacaoList ");
		hql.append("where cm.ativo = true ");
		hql.append("order by cm.centralMandado ");
		Query q = getEntityManager().createQuery(hql.toString());
		return q.getResultList();
	}

	public void limparCamposExpediente() {
		getInstance().setMeioExpedicaoExpediente(null);
		getInstance().setTipoProcessoDocumento(null);
	}

	/**
	 * Método criado para retornar a lista de centrais de mandados da
	 * localização atual do usuário. Se o usuário logado não tiver em sua
	 * localização um orgão julgador, ele poderá visualizar as centrais de
	 * mandados de todos os orgãos julgadores da sua turma (2º grau); Caso ele
	 * tenha um orgão julgador em sua localização, ele só poderá ver as centrais
	 * de mandados associadas para aquele orgão julgador.
	 * 
	 * @return Lista de Centrais de Mandados para a localização atual do
	 *         usuário.
	 */
	@SuppressWarnings("unchecked")
	public List<CentralMandado> getListaCentralMandado() {
		List<Integer> ojList = new ArrayList<Integer>();
		if (Authenticator.getOrgaoJulgadorAtual() != null || Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			if (Authenticator.getOrgaoJulgadorAtual() == null && Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
				for (OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoOrgaoJulgador : Authenticator
						.getOrgaoJulgadorColegiadoAtual().getOrgaoJulgadorColegiadoOrgaoJulgadorList()) {
					ojList.add(orgaoJulgadorColegiadoOrgaoJulgador.getOrgaoJulgador().getLocalizacao()
							.getIdLocalizacao());
				}
			} else {
				ojList.add(Authenticator.getOrgaoJulgadorAtual().getLocalizacao().getIdLocalizacao());
			}
		}
		StringBuilder hql = new StringBuilder("select distinct cm from CentralMandado cm where cm.ativo = true  ");
		if (Authenticator.getOrgaoJulgadorAtual() != null || Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			hql.append("and cm.idCentralMandado in ");
			hql.append("	(select cml.centralMandado.idCentralMandado from CentralMandadoLocalizacao cml ");
			hql.append("		where cml.localizacao.idLocalizacao in (" + listToString(ojList) + " ) ");
			hql.append("		or cml.localizacao.idLocalizacao in  ");
			hql.append("		(select l.localizacaoPai.idLocalizacao from Localizacao l  ");
			hql.append("			where l.localizacao.idLocalizacao in (" + listToString(ojList) + " ))) ");
		}
		Query q = getEntityManager().createQuery(hql.toString());
		return q.getResultList();

	}

	public void setInserirHashPetInicial(Boolean inserirHashPetInicial) {
		this.inserirHashPetInicial = inserirHashPetInicial;
	}

	public Boolean getInserirHashPetInicial() {
		return inserirHashPetInicial;
	}

	public ExpedicaoExpedienteEnum[] getMeioExpedicaoValues() {
		return ExpedicaoExpedienteEnum.values();
	}

	public void setAviso(boolean aviso) {
		this.aviso = aviso;
	}

	public boolean isAviso() {
		return aviso;
	}

	public boolean verificarPendencia() {
		ProcessoDocumento pd = ProcessoDocumentoHome.instance().getInstance();
		if (pd.getIdProcessoDocumento() != 0) {
			ProcessoTrfHome.instance()
					.setInstance(EntityUtil.find(ProcessoTrf.class, pd.getProcesso().getIdProcesso()));
			if ((Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelAdvogado())
					|| Authenticator.getPapelAtual().getIdentificador().equals("procurador") || Authenticator
					.getPapelAtual().getIdentificador().equals("procChefe"))
					&& ProcessoHome.instance().verificarPessoaAssinatura(pd) && existeExpedientePendente()) {
				return true;
			}
		}
		return false;
	}

	public void setVincularAto(boolean vincularAto) {
		this.vincularAto = vincularAto;
	}

	public boolean isVincularAto() {
		return vincularAto;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getAtoMagistradoList() {
		StringBuilder sb = new StringBuilder();
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		
		sb.append("select o from ProcessoDocumento o ");
		sb.append("where o.ativo = true ");
		sb.append("  and exists(select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa ");
		sb.append("             where pdpa.processoDocumentoBin = o.processoDocumentoBin) ");
		sb.append("  and o.processo.idProcesso = :processoTrf ");
		sb.append("  and (o.tipoProcessoDocumento = :tipoProcessoDocumentoDespacho "); 
		sb.append("       or o.tipoProcessoDocumento = :tipoProcessoDocumentoDecisao "); 
		sb.append("       or o.tipoProcessoDocumento = :tipoProcessoDocumentoAtoOrdinatorio "); 
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append("or o.tipoProcessoDocumento = :tipoProcessoDocumentoSentenca "); 
		} else {
			sb.append("or o.tipoProcessoDocumento = :tipoProcessoDocumentoAcordao ");
		}
		sb.append(")");

		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrfHome.getInstance().getIdProcessoTrf());
		q.setParameter("tipoProcessoDocumentoDespacho", ParametroUtil.instance().getTipoProcessoDocumentoDespacho());
		q.setParameter("tipoProcessoDocumentoDecisao", ParametroUtil.instance().getTipoProcessoDocumentoDecisao());
		q.setParameter("tipoProcessoDocumentoAtoOrdinatorio", ParametroUtil.instance().getTipoProcessoDocumentoAtoOrdinatorio());
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			q.setParameter("tipoProcessoDocumentoSentenca", ParametroUtil.instance().getTipoProcessoDocumentoSentenca());
		}else{
			q.setParameter("tipoProcessoDocumentoAcordao", ParametroUtil.instance().getTipoProcessoDocumentoAcordao());
		}
		List<ProcessoDocumento> procossoDocumentoList = q.getResultList();
		return procossoDocumentoList;
	}

	/**
	 * @author Daniel Rocha (daniel.rocha@trtsp.jus.br)
	 * @since 1.2.0
	 * @category PJE-JT
	 * @return retorna um único string contendo uma lista de nomes-parte e
	 *         prazo, separados por vírgula. Ex: Nome PoloAtivo-5, Nome
	 *         PoloPassivo-10
	 */
	@Deprecated
	public String getPartePrazoList() {
		return ComponentUtil.getComponent(PreparaAtoComunicacaoAction.class).getPartePrazoList();
	}

	/**
	 * @author Guilherme Bispo / Thiago Oliveira
	 * @since 1.2.0
	 * @category PJE-JT
	 * @return retorna um único string contendo o prazo da primeira parte
	 *         selecinada.
	 */
	public String getPrazoPrimeiraParte() {

		List<ProcessoParte> partesSelecionadas = ProcessoParteExpedienteHome.instance().getPartesList();
		partesSelecionadas.clear();

		SearchTree2GridList<ProcessoParte> poloAtivoTree = ProcessoParteHome.instance()
				.getSearchTree2GridPoloAtivoList();
		SearchTree2GridList<ProcessoParte> poloPassivoTree = ProcessoParteHome.instance()
				.getSearchTree2GridPoloPassivoList();
		SearchTree2GridList<ProcessoParte> outrosPartTree = ProcessoParteHome.instance()
				.getSearchTree2GridOutrosParticipantesList();

		List<EntityNode<ProcessoParte>> entidades = poloAtivoTree.getList();
		entidades.addAll(poloPassivoTree.getList());
		entidades.addAll(outrosPartTree.getList());

		for (EntityNode<ProcessoParte> noh : entidades) {
			if (noh.getSelected()) {
				if (!(noh.getEntity().getPrazoLegal() == null))
					return noh.getEntity().getPrazoLegal() + " dias";
			}
		}
		return null;
	}

	public void validarParte(ProcessoParte processoParte) {
		if (getInstance().getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.E && processoParte.getCheckado()) {
			String msg = getIntimacaoPartesService().validarParteParaExpediente(processoParte);
			if (msg != null) {
				FacesMessages.instance().add(Severity.ERROR, "Expediente não permitido por envio eletrônico - " + msg);
				processoParte.setCheckado(false);
			}
		}
		if (!processoParte.getCheckado()) {
			processoParte.setPrazoLegal(null);
		}
	}

	public boolean verificarParteIntimada(ProcessoParte pp) {
		if (atoMagistrado != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from ProcessoDocumentoExpediente o ");
			sb.append("where o.processoDocumentoAto = :pda");
			sb.append("  and o.processoDocumento.processoDocumentoBin.certChain is not null ");
			sb.append("  and o.processoDocumento.processoDocumentoBin.signature is not null ");
			sb.append("  and exists (select p from ProcessoParteExpediente p");
			sb.append("				 where p.processoExpediente = o.processoExpediente");
			sb.append("                and p.pessoaParte = :pp)");
			Query q = EntityUtil.createQuery(sb.toString());
			q.setParameter("pda", atoMagistrado);
			q.setParameter("pp", pp.getPessoa());
			Long retorno = EntityUtil.getSingleResultCount(q);
			return retorno > 0;
		}
		return false;

	}

	public void setAtoMagistrado(ProcessoDocumento atoMagistrado) {
		this.atoMagistrado = atoMagistrado;
	}

	public ProcessoDocumento getAtoMagistrado() {
		return atoMagistrado;
	}

	public void setMostrarFuncaoLimpar(boolean mostrarFuncaoLimpar) {
		this.mostrarFuncaoLimpar = mostrarFuncaoLimpar;
	}

	public boolean isMostrarFuncaoLimpar() {
		return mostrarFuncaoLimpar;
	}

	public void setExpedienteNaoEnviado(ProcessoExpediente expedienteNaoEnviado) {
		this.expedienteNaoEnviado = expedienteNaoEnviado;
	}

	public ProcessoExpediente getExpedienteNaoEnviado() {
		return expedienteNaoEnviado;
	}

	public void setMostrarFuncaoVerificarAlteracao(boolean mostrarFuncaoVerificarAlteracao) {
		this.mostrarFuncaoVerificarAlteracao = mostrarFuncaoVerificarAlteracao;
	}

	public boolean isMostrarFuncaoVerificarAlteracao() {
		return mostrarFuncaoVerificarAlteracao;
	}

	public void setMostrarAssociarPessoaExpediente(boolean mostrarAssociarPessoaExpediente) {
		this.mostrarAssociarPessoaExpediente = mostrarAssociarPessoaExpediente;
	}

	public boolean isMostrarAssociarPessoaExpediente() {
		return mostrarAssociarPessoaExpediente;
	}

	/**
	 * Mantém as informações gravadas da página abaExpediente.xml na tarefa dar
	 * ciência às partes
	 */
	class InformacaoPagina {
		public ProcessoDocumento atoMagistrado;
		public TipoProcessoDocumento tipoExpediente;
		public ExpedicaoExpedienteEnum meioExpedicao;
		public List<ProcessoParte> parteList = new ArrayList<ProcessoParte>();
		public boolean documentoSigiloso;
		public ModeloDocumento modeloDocumento;
		public Boolean inserirHashPeticaoInicial = false;
		public String modelo;
	}

	/**
	 * Método criado para verificar se todos os expedientes de um processo estão
	 * vencidos.
	 * 
	 * @param processoTrf
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean verificaExpedientesVencidos(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from ProcessoParteExpediente o ");
		sb.append("where o.processoJudicial = :processoTrf ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoTrf", processoTrf);
		List<ProcessoParteExpediente> expedientesProcesso = q.getResultList();
		Date dataAtual = new Date();
		if (expedientesProcesso.size() == 0) {
			return false;
		}
		for (ProcessoParteExpediente processoParteExpediente : expedientesProcesso) {
			if (dataAtual.before(processoParteExpediente.getDtPrazoLegal())) {
				return false;
			}
		}
		return true;
	}

	public void setDocumentoInserido(Boolean documentoInserido) {
		this.documentoInserido = documentoInserido;
	}

	public Boolean getDocumentoInserido() {
		return documentoInserido;
	}

	public void setMeioExpedicaoExpedienteDE() {
		this.getInstance().setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.P);
	}

	/**
	 * @author Tiago Zanon
	 * @since 1.2.0
	 * @category PJE-JT
	 * @return string de nomes e endereços das partes selecionadas
	 */
	@Deprecated
	public String getNomeEnderecoPartesSelecionadas() {
		return ComponentUtil.getComponent(PreparaAtoComunicacaoAction.class).getNomeEnderecoPartesSelecionadas();
	}

	private String validarRestricoesIntimacaoPartes() {
		List<String> erros = atualizarListaPendenciasParte();
		if (erros.size() > 0) {
			StringBuilder sb = new StringBuilder(MSG_PENDENCIAS_IDENTIFICADAS + ": ,");
			for (String erro : erros) {
				sb.append("- ");
				sb.append(erro);
				sb.append(",");
			}
			sb.append(MSG_EXPEDIENTE_NAO_PODE_SER_ENVIADO);
			return sb.toString();
		}
		return null;
	}

	private List<String> atualizarListaPendenciasParte() {
		List<String> erros = new ArrayList<String>();
		for (ProcessoParteExpediente processoParteExpediente : getInstance().getProcessoParteExpedienteList()) {
			ProcessoParte processoParte = ProcessoParteExpedienteHome.instance().getParteFromParteList(
					processoParteExpediente.getPessoaParte());
			if (processoParte != null) {
				String msgErro = getIntimacaoPartesService().validarParteParaExpediente(processoParte);
				if (msgErro != null) {
					erros.add(msgErro);

					processoParteExpediente.setPendencia(msgErro);
					getEntityManager().merge(processoParteExpediente);
				}
			}
		}
		getEntityManager().flush();
		return erros;
	}

	private void removerPartesComPendencias() {
		List<ProcessoParteExpediente> processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>(
				getInstance().getProcessoParteExpedienteList());
		for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList) {
			if (processoParteExpediente.getPendencia() != null) {
				getInstance().getProcessoParteExpedienteList().remove(processoParteExpediente);
				getEntityManager().remove(processoParteExpediente);
			}
		}
	}

	private void removerProcessoCaixaIntimacaoAutomatica() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if (!possuiExpedienteComPendencia(processoTrf)) {
			Caixa caixa = processoTrf.getProcesso().getCaixa();
			if (caixa != null
					&& (caixa.equals(ParametroUtil.instance().getCaixaIntimacaoAutoPend()) || caixa
							.equals(ParametroUtil.instance().getCaixaIntimacaoAutoPendSREEO()))) {

				processoTrf.getProcesso().setCaixa(null);
				getEntityManager().flush();
			}
		}
	}

	private boolean possuiExpedienteComPendencia(ProcessoTrf processoTrf) {
		String hql = "select count(o) from ProcessoExpediente o "
				+ "where exists(select p from ProcessoParteExpediente p where p.pendencia is not null and p.processoExpediente = o)"
				+ "  and o.processoTrf = :processoTrf";
		Query query = getEntityManager().createQuery(hql).setParameter("processoTrf", processoTrf);
		return EntityUtil.getSingleResultCount(query) > 0;
	}

	public boolean isPessoaExpediente(Pessoa pessoa) {
		return false;
	}
	
	public void setErroValidacaoProcessoParte(String erroValidacaoProcessoParte) {
		this.erroValidacaoProcessoParte = erroValidacaoProcessoParte;
	}

	public String getErroValidacaoProcessoParte() {
		return erroValidacaoProcessoParte;
	}

	public Boolean liberadoConsultaPublica(ProcessoDocumento pd){ 
		String hql = "select o from ProcessoDocumentoTrfLocal o " +
					 "where o.processoDocumento.idProcessoDocumento = :idProcessoDocumento";
		Query q = EntityUtil.createQuery(hql);
		q.setParameter("idProcessoDocumento", pd.getIdProcessoDocumento());
		ProcessoDocumentoTrfLocal pdtl = EntityUtil.getSingleResult(q);
		return pdtl != null && pdtl.getLiberadoConsultaPublica() != null && pdtl.getLiberadoConsultaPublica();
	}	
	
	public boolean enviarExpediente() {
		if (atoMagistrado != null && ProcessoDocumentoBinHome.isModeloVazio(ProcessoDocumentoBinHome.instance().getInstance())) {
			ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(atoMagistrado.getProcessoDocumentoBin().getModeloDocumento());
		}
		
		boolean gravado = gravar();
		if (gravado) {
			ProcessoParteExpedienteHome.instance().removerPessoaExpedienteNaoVinculada(getInstance());
			
			if (inserirHashPetInicial){
				inserirHashPetInicial();
			}
			ProcessoDocumentoBinHome.instance().assinarDocumento();
			if (instance.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.E) {
				enviarEmail();
			}
			
			try {
				RegistraEventoAction.instance().registraPorNome(ParametroUtil.instance().getAgrupamentoExpedicaoDocumento().getAgrupamento());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return gravado;
	}	

	public ProcessoDocumentoBinPessoaAssinatura getUltimaPessoaAssinatura(ProcessoDocumentoBin processoDocumentoBin) {
		ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager = ComponentUtil.getComponent(ProcessoDocumentoBinPessoaAssinaturaManager.class);
		return processoDocumentoBinPessoaAssinaturaManager.getUltimaAssinaturaDocumento(processoDocumentoBin);
	}
	
	public boolean getIsExibeBotaoCadastrarExpedientes() {
		return Authenticator.isUsuarioInterno() && this.getMostrarBtnCadastrar();
	}
}
