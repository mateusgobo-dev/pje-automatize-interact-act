package br.com.infox.cliente.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.exception.AcordaoException;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventosTreeHandler;
import br.com.infox.ibpm.component.tree.EventsTipoDocumentoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.list.ProcessoJulgadoList;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.infox.utils.Constantes;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.UrlUtil;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.RelatorioStatusEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(SessaoProcessoDocumentoHome.NAME)
@Scope(ScopeType.CONVERSATION)
public class SessaoProcessoDocumentoHome extends AbstractHome<SessaoProcessoDocumento> {
	
	private DocumentoJudicialService documentoJudicialService;
	@RequestParameter
	private Integer idSessao;

	private static final long serialVersionUID = 1L;
	public static final String NAME = "sessaoProcessoDocumentoHome";
	private static final LogProvider log = Logging.getLogProvider(SessaoProcessoDocumentoHome.class);

	private static final String NOME_FLUXO_PROCESSANTE = "Processante";
	private static final String NOME_FLUXO_SREEO = "SREEO";
	private static final String NAO_ENCONTRADO = "Não encontrado";

	private List<SessaoPautaProcessoTrf> listaProcesso = new ArrayList<SessaoPautaProcessoTrf>();
	private List<ProcessoDocumento> documentosAssinar = new ArrayList<ProcessoDocumento>();
	private String modeloDocumento;
	private Boolean checkAll = Boolean.FALSE;
	private String textoCertidao;
	private boolean relatorioMagistradoAssinado;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private RelatorioStatusEnum tipoStatusRelatorio;
	private Boolean assinado = Boolean.FALSE;
	private Boolean acordaoAssinado = Boolean.FALSE;
	private boolean carregarRelatorio = true;
	private String conteudoEditorAcordao;
	private boolean divRelatorio = false;
	private boolean divVoto = false;
	private boolean divEmenta = false;
	private SessaoProcessoDocumento docEmenta = new SessaoProcessoDocumento();
	private boolean ementaAssinada = false;
	private SessaoPautaProcessoTrf sessaoPauta;
	private SessaoPautaProcessoTrf sessaoPautaRelatorio;
	private boolean exibeModalConfirmacao = false;
	private ProcessoDocumento ementa;
	@Deprecated
	private boolean liberarConsultaPublica;
	private String nomeFluxo;
	private Map<String,SessaoProcessoDocumento> mapaDocumento = new HashMap<String,SessaoProcessoDocumento>(3); 
	private boolean checkAllAssinatura = Boolean.TRUE;
	private boolean documentoEmEdicao = false;
	private int totalProcessosRemovidos;
	private int totalDocumentosRetiradosDaListaParaAssinar;
	private ProcessoDocumentoManager procDocManager;
	private Sessao sessao;
	private ProcessoTrf processoJudicial;
	
	@Override
	public void create() {
		super.create();
		if(idSessao != null){
			SessaoHome.instance().setId(idSessao);
			setSessao(SessaoHome.instance().getInstance());
		}else{
			setSessao(null);
		}
		procDocManager = ComponentUtil.getProcessoDocumentoManager();
		documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		return super.beforePersistOrUpdate();
	}
	
	public void setSessaoProcessoDocumentoIdSessaoProcessoDocumento(Integer id) {
		setId(id);
	}

	public Integer getSessaoProcessoDocumentoIdSessaoProcessoDocumento() {
		return (Integer) getId();
	}

	public static SessaoProcessoDocumentoHome instance() {
		return ComponentUtil.getComponent(SessaoProcessoDocumentoHome.NAME);
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void addAll(boolean marcarTodos) {
		this.checkAll = marcarTodos;
		if(this.checkAll) {
			ProcessoJulgadoList pjl = ComponentUtil.getComponent(ProcessoJulgadoList.class);
			for (SessaoPautaProcessoTrf row : pjl.list()) {
				if (getDocumento(row) == null) {
					listaProcesso.add(row);
				}
			}
			totalProcessosRemovidos = 0;
		} else {
			totalProcessosRemovidos = listaProcesso.size();
			listaProcesso.clear();
		}
	}

	public RelatorioStatusEnum[] getRelatorioStatusEnumValues() {
		RelatorioStatusEnum[] relatorioStatus = new RelatorioStatusEnum[3];
		relatorioStatus[0] = RelatorioStatusEnum.NN;
		relatorioStatus[1] = RelatorioStatusEnum.FN;
		relatorioStatus[2] = RelatorioStatusEnum.FL;
		return relatorioStatus;
	}

	public Boolean existeSessaoDocumento() {
		ProcessoJulgadoList pjl = ComponentUtil.getComponent(ProcessoJulgadoList.class);
		for (SessaoPautaProcessoTrf row : pjl.list()) {
			if (getDocumento(row) == null) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	public void addProcessoList(SessaoPautaProcessoTrf row) {
		newInstance();
		
		if (listaProcesso.contains(row)) {
			listaProcesso.remove(row);
			totalProcessosRemovidos++;
			this.checkAll = false;
		} else {
			listaProcesso.add(row);
			totalProcessosRemovidos--;
			if(totalProcessosRemovidos == 0) {
				this.checkAll = true;
			}
		}
	}

	/**
	 * Assina documento relatorio do magistrado na relação de julgamento
	 */
	public void assinarRelatorioMagistrado() {
		if (getInstance().getProcessoDocumento() != null) {
			try {
				atualizarRelatorioEmenta();

				ProcessoDocumentoBin pdBin = getInstance().getProcessoDocumento().getProcessoDocumentoBin();
				pdBin.setModeloDocumento(modeloDocumento);
				ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).mergeAndFlush(pdBin);

				ProcessoDocumentoBinHome.instance().setInstance(pdBin);
				FacesMessages.instance().clear();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} else {
			try {
				cadastrarRelatorioMagistrado();
				FacesMessages.instance().clear();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		if (ProcessoDocumentoBinHome.instance().assinarDocumento()) {
			setRelatorioMagistradoAssinado(Boolean.TRUE);
		}
	}

	/**
	 * Cadastra o relatório do magistrado na sessão para determinado processo
	 */
	public void cadastrarRelatorioMagistrado() {
		String ret = null;
		try {
			String signature = ProcessoDocumentoBinHome.instance().getSignature();
			String certChain = ProcessoDocumentoBinHome.instance().getCertChain();

			ProcessoDocumentoHome pdHome = ComponentUtil.getComponent(ProcessoDocumentoHome.class);
			pdHome.newInstance();
			ProcessoHome.instance().setInstance(getSessaoPautaProcessoTrf().getProcessoTrf().getProcesso());
			TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			pdHome.getInstance().setTipoProcessoDocumento(tipo);
			pdHome.getInstance().setProcessoDocumento("Relatório do Magistrado");

			ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(modeloDocumento);
			ProcessoDocumentoBinHome.instance().setCertChain(certChain);
			ProcessoDocumentoBinHome.instance().setSignature(signature);

			pdHome.persist();

			getInstance().setProcessoDocumento(pdHome.getInstance());
			getInstance().setSessao(getSessaoPautaProcessoTrf().getSessao());
			getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());

			SessaoProcessoDocumentoVoto spdv = SessaoProcessoDocumentoVotoHome.instance().getVotoMagistrado(
					getSessaoPautaProcessoTrf());
			if (spdv != null && spdv.getLiberacao()) {
				getInstance().setLiberacao(spdv.getLiberacao());
			}
			getInstance().setTipoInclusao(TipoInclusaoDocumentoEnum.S);

			ret = persist();
			mapaDocumento.remove("R:"+getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf());
		} catch (Exception e) {
			log.error(e.getMessage());
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarDocumento.erro");
			ret = null;
		}
		if (ret != null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "sessao.salvarDocumento.sucesso");
		}
	}

	/**
	 * Atualiza voto do magistrado
	 */
	public void updateVotoMagistrado() {
		getInstance().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
		ProcessoDocumentoHome.instance().update();
		super.update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Relatório alterado com sucesso.");
	}

	/**
	 * Remove relatório do magistrado daquele processo naquela sessão
	 */
	public void removerRelatorioMagistrado() {
		ProcessoDocumento pd = getInstance().getProcessoDocumento();
		try {
			// inativa o processo documento
			ProcessoDocumentoHome.instance().setInstance(pd);
			ProcessoDocumentoHome.instance().inactive();
			// inativa o sessao processo documento
			getInstance().getProcessoDocumento().setAtivo(false);
			getInstance().setProcessoDocumento(null);
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			newInstance();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Relatório removido com sucesso.");
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void removerDocumento() {
		ProcessoDocumento pd = getInstance().getProcessoDocumento();
		// inativa o processo documento
		ProcessoDocumentoHome.instance().setInstance(pd);
		ProcessoDocumentoHome.instance().inactive();
		// inativa o sessao processo documento
		getInstance().getProcessoDocumento().setAtivo(false);
		getInstance().setProcessoDocumento(null);
		getEntityManager().merge(getInstance());
		getEntityManager().flush();
		newInstance();
	}

	@SuppressWarnings("unchecked")
	public SessaoProcessoDocumento getSessaoProcessoDocumentoRelatorioMagistrado(SessaoPautaProcessoTrf sppt) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o ");
		sb.append("inner join o.processoDocumento pd where (o.processoDocumento.idProcessoDocumento = pd.idProcessoDocumento)");
		sb.append("and pd.processo.idProcesso = :processo ");
		sb.append("and pd.ativo = true ");
		sb.append("and pd.dataExclusao = null ");
		sb.append("and pd.tipoProcessoDocumento = :tipoProcessoDocumento");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("processo", sppt.getProcessoTrf().getProcesso().getIdProcesso());
		query.setParameter("tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		query.setMaxResults(1);
		
		List<SessaoProcessoDocumento> sessaoProcessoDocumento = query.getResultList();
		if (sessaoProcessoDocumento != null && !sessaoProcessoDocumento.isEmpty()) {
			return sessaoProcessoDocumento.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Busca o relatorio do magistrado e seus status
	 * 
	 * @param p
	 *            SessaoPautaProcessoTrf esperada
	 */
	public void relatorio(SessaoPautaProcessoTrf p) {
		newInstance();
		SessaoProcessoDocumentoVotoHome.instance().verificaStatusSessao(p);
		setDivRelatorio(true);
		setSessaoPautaProcessoTrf(p);
		setInstance(getRelatorioMagistrado(p));
		if (instance != null) {
			modeloDocumento = getInstance().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
			verificaRelatorioAssinado(instance);
			ProcessoDocumentoHome.instance().setInstance(EntityUtil.find(ProcessoDocumento.class, getInstance().getProcessoDocumento().getIdProcessoDocumento()));
		} else {
			ProcessoDocumentoHome.instance().getInstance()
					.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		}
	}

	/**
	 * Verifica se o relatorio esta assinado
	 * 
	 * @param sppv
	 * @return
	 */
	public boolean verificaRelatorioAssinado(SessaoProcessoDocumento spd) {
		boolean assinado = false;

		if (spd != null) {
			assinado = !spd.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty();
			setRelatorioMagistradoAssinado(assinado);			
		}
		return assinado;
	}

	/**
	 * Retorna o relatório do magistrado
	 * 
	 * @param sppt
	 *            Sessão pauta processo esperada para retornar o relatorio do
	 *            magistrado
	 * @return
	 */
	public SessaoProcessoDocumento getRelatorioMagistrado(SessaoPautaProcessoTrf sppt) {
		if (sppt==null){
			return null;
		}
		if(mapaDocumento.containsKey("R:" + sppt.getIdSessaoPautaProcessoTrf())){
			return mapaDocumento.get("R:" + sppt.getIdSessaoPautaProcessoTrf());
		}
		String queryStr = "SELECT o FROM SessaoProcessoDocumento AS o " +
				"	INNER JOIN o.processoDocumento AS pd " +
				"	WHERE (o.processoDocumento.idProcessoDocumento = pd.idProcessoDocumento) " +
				"	AND pd.processo.idProcesso = :processo " +
				"	AND pd.ativo = true " +
				"	AND pd.dataExclusao IS NULL " +
				"	AND pd.tipoProcessoDocumento = :tipoProcessoDocumento" +
				"	AND o.sessao = :sessao" +
				"	AND o.orgaoJulgador = :orgaoJulgador";

		Query q = getEntityManager().createQuery(queryStr);
		q.setParameter("processo", sppt.getProcessoTrf().getProcesso().getIdProcesso());
		q.setParameter("tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		q.setParameter("sessao", sppt.getSessao());
		q.setParameter("orgaoJulgador", sppt.getProcessoTrf().getOrgaoJulgador());
		q.getResultList();
		StringBuilder sb = new StringBuilder("select o from SessaoProcessoDocumento o ");
		sb.append("where o.processoDocumento.tipoProcessoDocumento = :tipoRelatorio ");
		sb.append("and o.processoDocumento.ativo = true  ");
		sb.append("and o.sessao = :sessao ");
		sb.append("and o.processoDocumento.processo = :processo  ");
		sb.append("and o.orgaoJulgador = :oj ");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("tipoRelatorio", ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		query.setParameter("sessao", sppt.getSessao());
		query.setParameter("processo", sppt.getProcessoTrf().getProcesso());
		query.setParameter("oj", sppt.getProcessoTrf().getOrgaoJulgador());
		SessaoProcessoDocumento relatorio = EntityUtil.getSingleResult(query);

		if (relatorio != null
				&& ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(relatorio.getProcessoDocumento(), sppt.getProcessoTrf()
						.getProcesso())) {
			mapaDocumento.put("R:" + sppt.getIdSessaoPautaProcessoTrf(), relatorio);
			return relatorio;
		}
		mapaDocumento.put("R:" + sppt.getIdSessaoPautaProcessoTrf(), null);
		return null;
	}

	/**
	 * Pegar o modelo do documento a partir do processo documento bin
	 */
	public void processarModelo() {
		ProcessoDocumentoHome.instance().processarModelo();
		modeloDocumento = ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento();
	}

	public void carregarModelo() {
		if (getModeloDocumento() == null) {
			setModeloDocumento(ParametroUtil.instance().getModeloCertidaoJulgamento().getModeloDocumento());
		}
	}

	public void addRemoveRowList(SessaoProcessoDocumento documento) {
		if (documentosAssinar.contains(documento.getProcessoDocumento())) {
			documentosAssinar.remove(documento.getProcessoDocumento());
		} else {
			documentosAssinar.add(documento.getProcessoDocumento());
		}
	}

	@Override
	public void newInstance() {
		setCheckAll(Boolean.FALSE);
		setAssinado(false);
		setAcordaoAssinado(false);
		setCarregarRelatorio(false);
		setDivEmenta(false);
		setDivVoto(false);
		setDocEmenta(null);
		setSessaoPauta(null);
		setSessaoPautaRelatorio(null);
		setDivRelatorio(false);
		setEmentaAssinada(false);
		setRelatorioMagistradoAssinado(false);
		setSessaoPautaProcessoTrf(null);
		setModeloDocumento(null);
		SessaoProcessoDocumentoVotoHome.instance().newInstance();
		ProcessoDocumentoHome.instance().newInstance();
		ProcessoDocumentoBinHome.instance().newInstance();
		super.newInstance();
	}

	public void limparCertidao() {
		SessaoPautaProcessoTrf sessaoPauta = null;
		if(getInstance() != null && getInstance().getProcessoDocumento() != null && getInstance().getSessao() != null) {
			sessaoPauta = ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(getInstance().getProcessoDocumento().getProcessoTrf(),getInstance().getSessao());
		}
		setModeloDocumento(ParametroUtil.instance().getModeloCertidaoJulgamento().getModeloDocumento());
		newInstance();
		if(sessaoPauta != null) {
			listaProcesso.add(sessaoPauta);
		}
	}

	public void persistLote() {
		ProcessoDocumentoBinHome procDocBinHome = ComponentUtil.getComponent(ProcessoDocumentoBinHome.class);
		ProcessoDocumentoHome pdHome = ComponentUtil.getComponent(ProcessoDocumentoHome.class);
		String modelo = modeloDocumento;	
		for (SessaoPautaProcessoTrf processo : listaProcesso) {
			ProcessoHome.instance().setInstance(processo.getProcessoTrf().getProcesso());
			setSessaoPautaProcessoTrf(processo);
			ProcessoTrfHome.instance().setProcessoTrf(processo.getProcessoTrf());
			ProcessoTrfHome.instance().setInstance(processo.getProcessoTrf());
			SessaoPautaProcessoTrfHome.instance().setInstance(processo);
			SessaoProcessoDocumento sessaoDocumento = getDocumento(processo);
			if(sessaoDocumento != null) {
				sessaoDocumento.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(ProcessoDocumentoHome.processarModelo(modelo));
				getEntityManager().merge(sessaoDocumento);
				getEntityManager().flush();
			} else {
				pdHome.newInstance(); 
				procDocBinHome.newInstance();
				procDocBinHome.getInstance().setModeloDocumento(ProcessoDocumentoHome.processarModelo(modelo));
				pdHome.getInstance().setProcessoDocumento("Certidão de julgamento");
				TipoProcessoDocumento tpd = ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() != null ? ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento() : ParametroUtil.instance().getTipoProcessoDocumentoCertidao();
				pdHome.getInstance().setTipoProcessoDocumento(tpd);
				pdHome.getInstance().setDataInclusao(new Date());
				pdHome.persistCertidao(processo.getProcessoTrf());
				getInstance().setProcessoDocumento(pdHome.getInstance());
				getInstance().setSessao(SessaoHome.instance().getInstance());
				getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
				persist();
				newInstance();
			}
		}
		listaProcesso.clear();
		setModeloDocumento(null);
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento(s) inserido(s) com sucesso.");
	}

	private Integer quantidadeVoto(Boolean contexto) {
		Integer numVotos = 0;
		Processo processo = recuperaProcessoAtivo();
		if(processo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from SessaoProcessoDocumentoVoto o where ");
			sb.append("o.tipoVoto.relator = false and ");
			if (contexto) {
				sb.append("o.tipoVoto.contexto = 'C' and ");
			}
			sb.append("o.sessao = :sessao and ");
			sb.append("o.processoDocumento.processo = :processo and ");
			sb.append("o.processoDocumento.tipoProcessoDocumento = :voto ");
			sb.append("and o.processoDocumento.ativo = true ");
			Query q = getEntityManager().createQuery(sb.toString())
					.setParameter("sessao", SessaoHome.instance().getInstance())
					.setParameter("processo", processo)
					.setParameter("voto", ParametroUtil.instance().getTipoProcessoDocumentoVoto());
			numVotos = q.getResultList().size();			
		}
		return numVotos;
	}

	private String decisao() {
		String textoDecisao = null;
		Processo processo = recuperaProcessoAtivo();
		if(processo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o.tipoVoto.textoCertidao from SessaoProcessoDocumentoVoto o where ");
			sb.append("o.tipoVoto.relator = true and ");
			sb.append("o.sessao = :sessao and ");
			sb.append("o.processoDocumento.processo = :processo and ");
			sb.append("o.processoDocumento.tipoProcessoDocumento = :voto ");
			sb.append("and o.processoDocumento.ativo = true ");
			Query q = getEntityManager().createQuery(sb.toString())
					.setParameter("sessao", SessaoHome.instance().getInstance())
					.setParameter("processo", processo)
					.setParameter("voto", ParametroUtil.instance().getTipoProcessoDocumentoVoto());
			textoDecisao = EntityUtil.getSingleResult(q);
		}
		return textoDecisao;
	}

	/**
	 * Metodo que retorna o tipo de decisão e seta o texto certidão caso o tipo
	 * de decisão seja unanimidade
	 * 
	 * @return
	 */
	public String tipoDecisao() {
		if (quantidadeVoto(Boolean.FALSE) == quantidadeVoto(Boolean.TRUE)) {
			setTextoCertidao(decisao());
			return "unanimidade";
		}
		return "por maioria";
	}

	public void updateCertidao() {
		getInstance().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);

		ProcessoDocumentoHome.instance().setInstance(getInstance().getProcessoDocumento());
		ProcessoDocumentoHome.instance().update();

		update();

		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento alterado com sucesso.");
	}

	public void editarDocumento(SessaoProcessoDocumento documento) {
		setInstance(documento);
		setModeloDocumento(documento.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		documentoEmEdicao = true;
	}

	public SessaoProcessoDocumento getDocumento(SessaoPautaProcessoTrf row) {
		return ComponentUtil.getSessaoProcessoDocumentoDAO().recuperarCertidaoJulgamentoPorSessaoPautaProcesso(row);
	}

	public PessoaMagistrado getMagistrado(SessaoPautaProcessoTrf row) {		
		return ComponentUtil.getPessoaMagistradoManager().recuperarMagistrado(
				row.getSessao().getIdSessao(), row.getProcessoTrf().getOrgaoJulgador().getIdOrgaoJulgador());
	}

	@Override
	public String update() {
		if (!assina()) { return null; }  // Não permite a gravação do documento vazio.
		if (procuraVotoAntecipadoLiberado()
				&& !instance.getProcessoDocumento().getTipoProcessoDocumento()
						.equals(ParametroUtil.instance().getTipoProcessoDocumentoVoto())) {
			getInstance().setLiberacao(true);
		}
		try{
			ComponentUtil.getDocumentoJudicialService().persist(getInstance().getProcessoDocumento(), true);
			FacesMessages.instance().clear();
		}
		catch(PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento:  " + e.getMessage(), e);
			log.error(e.getMessage());
		}
		return super.update();
	}
	
	/**
	 * Método utilizado para fazer chamada ao "update()", pois o atributo "action" do componente "wi:commandButton" só permite métodos sem retorno, ou seja, métodos de assinatura "void".
	 * 
	 * @return void
	 * @param N/A
	 */
	public void atualizarDocumento(){
		update();
	}

	private void gravarDocumentoAntecipado(ProcessoDocumento pd) {
		if (assina()) {
			getInstance().setProcessoDocumento(pd);
			getInstance().setTipoInclusao(TipoInclusaoDocumentoEnum.A);
			ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
			getInstance().setOrgaoJulgador(processoTrf.getOrgaoJulgador());
			getInstance().setSessao(ComponentUtil.getSessaoJulgamentoManager().getSessaoJulgamento(processoTrf));
			if (procuraVotoAntecipadoLiberado()
					&& !pd.getTipoProcessoDocumento().equals(ParametroUtil.instance().getTipoProcessoDocumentoVoto())) {
				getInstance().setLiberacao(true);
			}
			FacesMessages.instance().clear();

			super.persist();
		}
	}

	/**
	 * grava um documento antecipado na sessao para um processo do tipo
	 * relatorio
	 */
	public void persistRelatorio() {
		persistRelatorio(Boolean.TRUE);
	}

	/**
	 * Grava um documento antecipado na sessao para um processo do tipo relatorio. Caso a sessão de julgamento ainda não esteja registrada, apenas o documento vai ser cadastrado e o vínculo
	 * com a sessão será ignorado
	 */
	public void persistRelatorio(Boolean isLiberarDocumento) {
		// Não permite a gravação do documento vazio.
		if (assina()) {
			ProcessoDocumento pd = new ProcessoDocumento();
			try{
				pd = criarProcessoDocumento(pd, ParametroUtil.instance().getTipoProcessoDocumentoRelatorio(), "Relatório");
			} catch(PJeBusinessException e){
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarRelatorio.erro", e.getMessage());
				log.error(e.getMessage());
			}

			if (getInstance().getIdSessaoProcessoDocumento() > 0) {
				setInstance(getEntityManager().find(getEntityClass(), getInstance().getIdSessaoProcessoDocumento()));
				getInstance().setLiberacao(isLiberarDocumento);
				getInstance().setProcessoDocumento(pd);
				super.update();
			} else {
				gravarDocumentoAntecipado(pd);
			}
		}
	}

	private void assinarDocumento(boolean juntar) throws PJeBusinessException{
		boolean assinado = true;
		ComponentUtil.getDocumentoJudicialService().finalizaDocumento(getInstance().getProcessoDocumento(), ProcessoTrfHome.instance().getInstance(), null, false, true, false, Authenticator.getPessoaLogada(), juntar);
		FacesMessages.instance().add(Severity.INFO,"Documento assinado com sucesso");
		EntityUtil.getEntityManager().flush();
		setAssinado(assinado);
	}

	private void assinarDocumento(){
		try {
			assinarDocumento(false);
		} catch (PJeBusinessException e) {
			log.error(e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível completar a operação",e.getMessage());
		}
		
	}

	public void persistDocumentosAcordao(char tipo) {
            // alterado pela solicitação [PJEII-4330] para não interferir nas funcionalidades existentes
            persistDocumentosAcordao(tipo, false);
        }

	public void persistDocumentosAcordao(char tipo, boolean isFrame) {
		TipoProcessoDocumento tpd = null;
		String nomeDocumento = null;
		if (tipo == 'V') {
			nomeDocumento = "Voto";
			tpd = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
		} else if (tipo == 'R') {
			nomeDocumento = "Relatório";
			tpd = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
		} else if (tipo == 'E') {
			nomeDocumento = "Ementa";
			tpd = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
		} else if (tipo == 'N') {
			nomeDocumento = "Notas Orais";
            String idParametro = ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
            tpd = getEntityManager().getReference(TipoProcessoDocumento.class, new Integer(idParametro));
		} else if (tipo == 'A') {
			nomeDocumento = "Acórdão";
			tpd = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
			getInstance().setLiberacao(true);
			ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(getConteudoEditorAcordao(isFrame));
		}
		ProcessoDocumento pd = new ProcessoDocumento();
		Usuario usuario = Authenticator.getUsuarioLogado();
		SessaoPautaProcessoTrf pautaProcessoTrf = SessaoPautaProcessoTrfHome.instance().getInstance();

		pd.setProcesso(pautaProcessoTrf.getProcessoTrf().getProcesso());
		pd.setProcessoDocumento(nomeDocumento);
		pd.setTipoProcessoDocumento(tpd);
		pd.setUsuarioInclusao(usuario);
		pd.setDataInclusao(new Date());
		pd.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());
		if (tipo == 'E' && !ComponentUtil.getSessaoProcessoDocumentoManager().isRelator(pautaProcessoTrf.getProcessoTrf(), Authenticator.getOrgaoJulgadorAtual())) {
			inativaTipoProcessoDocumentoEmentaSessao();
		}

		ProcessoHome.instance().setInstance(pautaProcessoTrf.getProcessoTrf().getProcesso());

		ProcessoDocumentoHome.instance().setInstance(pd);
		ProcessoDocumentoHome.instance().persist();
		ComponentUtil.getProcessoDocumentoTrfLocalManager().criarDocumentoPublico(pd, liberarConsultaPublica);

		getInstance().setProcessoDocumento(pd);
		getInstance().setSessao(pautaProcessoTrf.getSessao());
		getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());

		FacesMessages.instance().clear();

		super.persist();
	}

	public void persistDocumentosAcordaoComAssinatura(char tipo) {
		if (assina()) {
			persistDocumentosAcordao(tipo);
			assinarDocumento();
		}
	}

	public void updateAcordaoComAssinaturaInteiroTeor() {
		if(EventsTipoDocumentoTreeHandler.instance().getEventoBeanList() == null ||
				EventsTipoDocumentoTreeHandler.instance().getEventoBeanList().isEmpty()){
			FacesMessages.instance().add(Severity.ERROR, "Movimento processual não preenchido corretamente.");
			return;
		}
		else {
			for(EventoBean ev : EventsTipoDocumentoTreeHandler.instance().getEventoBeanList()){
				if(ev.getValido() == null || !ev.getValido()){
					FacesMessages.instance().add(Severity.ERROR, "Movimento processual não preenchido corretamente.");
					return;
				}
			}
				
		}
		update();
                
        // alterado pela solicitação [PJEII-4330] para não interferir nas funcionalidades existentes
		afterPersistOrUpdate(false);
	}

    /**
     * Método criado para ser utilizado pelo frame elaborarAcordao.xhtml
     */
	public void updateAcordaoComAssinaturaInteiroTeorFrame() {
		update();
		afterPersistOrUpdate(true);
	}

	public void persistAcordaoComAssinaturaInteiroTeor() {
		if(EventsTipoDocumentoTreeHandler.instance().getEventoBeanList() == null ||
				EventsTipoDocumentoTreeHandler.instance().getEventoBeanList().isEmpty()){
			FacesMessages.instance().add(Severity.ERROR, "Movimento processual não preenchido corretamente.");
			return;
		}
		else {
			for(EventoBean ev : EventsTipoDocumentoTreeHandler.instance().getEventoBeanList()){
				if(ev.getValido() == null || !ev.getValido()){
					FacesMessages.instance().add(Severity.ERROR, "Movimento processual não preenchido corretamente.");
					return;
				}
			}
				
		}
		persistDocumentosAcordao('A');

        // alterado pela solicitação [PJEII-4330] para não interferir nas funcionalidades existentes
        afterPersistOrUpdate(false);
	}

    /**
     * Método criado para ser utilizado pelo frame elaborarAcordao.xhtml
     */
	public void persistAcordaoComAssinaturaInteiroTeorFrame() {
		persistDocumentosAcordao('A', true);
		afterPersistOrUpdate(true);
	}

	private void afterPersistOrUpdate(boolean isFrame) {
		FacesMessages.instance().clear();
		if (gravarInteiroTeor(isFrame)) {
			Processo processo = recuperaProcessoAtivo();
			if(processo != null) {
				String actorId = processo.getActorId();
				if (actorId != null && !actorId.equals(Authenticator.getUsuarioLogado().getLogin())) {
					FacesMessages.instance().add(Severity.INFO, "Processo bloqueado pelo servidor " + actorId + ".");
				} else {
					intimarPartesAutomaticamente(processo);
				}
			}
		}
	}

	private void intimarPartesAutomaticamente(Processo processo) {
		ParametroUtil parametroUtil = ParametroUtil.instance();
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, processo.getIdProcesso());
		Tarefa tarefa = null;

		if (NOME_FLUXO_PROCESSANTE.equalsIgnoreCase(nomeFluxo)) {
			tarefa = parametroUtil.getTarefaSecretariaProcessante();
		} else if (NOME_FLUXO_SREEO.equalsIgnoreCase(nomeFluxo)) {
			tarefa = parametroUtil.getTarefaSecretariaSREEO();
		}else{
			return;
		}
		movimentarProcesso(processo.getIdProcesso(), tarefa.getTarefa());
		if (ComponentUtil.getIntimacaoPartesService().intimarPartesAutomaticamente(processoTrf, getInstance().getProcessoDocumento())) {
			FacesMessages.instance().add(Severity.INFO, "Expediente Via Sistema enviado com sucesso.");
		} else {
			Caixa caixaIntimacao = null;
			if (NOME_FLUXO_PROCESSANTE.equalsIgnoreCase(nomeFluxo)) {
				tarefa = parametroUtil.getTarefaDarCienciaPartes();
				caixaIntimacao = parametroUtil.getCaixaIntimacaoAutoPend();
			} else if (NOME_FLUXO_SREEO.equalsIgnoreCase(nomeFluxo)) {
				tarefa = parametroUtil.getTarefaDarCienciaPartesSREEO();
				caixaIntimacao = parametroUtil.getCaixaIntimacaoAutoPendSREEO();
			}
			movimentarProcesso(processo.getIdProcesso(), tarefa.getTarefa());
			processo.setCaixa(caixaIntimacao);
			ComponentUtil.getComponent(ProcessoManager.class).mergeAndFlush(processo);
			FacesMessages
					.instance()
					.add(Severity.INFO,
							"Processo remetido para ''Dar Ciências às Partes'', pois contem partes que não podem ser intimadas automaticamente.");
		}
	}

	public void movimentarProcesso(int idProcesso, String tarefaDestino) {
		org.jbpm.taskmgmt.exe.TaskInstance taskInstance = null;

		SituacaoProcesso situacaoProcesso = getSituacaoProcesso(idProcesso);
		ProcessInstance pi = ManagedJbpmContext.instance().getProcessInstance(situacaoProcesso.getIdProcessInstance());
		Token token = pi.getRootToken();
		for (org.jbpm.taskmgmt.exe.TaskInstance t : pi.getTaskMgmtInstance().getTaskInstances()) {
			if (t.getTask().getTaskNode().equals(token.getNode())) {
				taskInstance = t;
			}
		}

		if (taskInstance != null) {
			BusinessProcess.instance().setTaskId(taskInstance.getId());
			if (taskInstance.getStart() == null) {
				BusinessProcess.instance().startTask();
			}
		}
		try {
			if (JbpmUtil.transitionExists(taskInstance, tarefaDestino)) {
				taskInstance.end(tarefaDestino);
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o acordão:  " + e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	/**
	 * @return <code>True</code> caso seja uma Tarefa do Magistrado e
	 *         <code>False</code> caso não seja.
	 * @author João Paulo Lacerda
	 * Método sem sentido, pois amarra a geração de acórdãos ao fluxo do TRF5.
	 */
	@Deprecated
	public Boolean isTarefaDoMagistrado() {
		int idProcesso = SessaoPautaProcessoTrfHome.instance().getInstance().getProcessoTrf().getIdProcessoTrf();
		SituacaoProcesso situacaoProcesso = getSituacaoProcesso(idProcesso);
		if (situacaoProcesso != null) {
			int idTarefaAtual = situacaoProcesso.getIdTarefa();

			nomeFluxo = situacaoProcesso.getNomeFluxo();
			ParametroUtil parametroUtil = ParametroUtil.instance();

			if (NOME_FLUXO_PROCESSANTE.equalsIgnoreCase(nomeFluxo)) {
				if (idTarefaAtual == parametroUtil.getTarefaAnaliseGabinete().getIdTarefa()
						|| idTarefaAtual == parametroUtil.getTipoTarefaAtoMagistrado().getIdTarefa()
						|| idTarefaAtual == parametroUtil.getTarefaMinutar().getIdTarefa()) { // verificar
																								// idTarefaSecretariaProcessante
					return true;
				}
			} else if (NOME_FLUXO_SREEO.equalsIgnoreCase(nomeFluxo)) {
				if (idTarefaAtual == parametroUtil.getTarefaAnaliseGabineteSreeo().getIdTarefa()
						|| idTarefaAtual == parametroUtil.getTipoTarefaAtoMagistradoSreeo().getIdTarefa()
						|| idTarefaAtual == parametroUtil.getTarefaMinutarSreeo().getIdTarefa()) { // verificar
																									// idTarefaSecretariaSreeo
					return true;
				}
			}
		}
		return false;
	}

	public SituacaoProcesso getSituacaoProcesso(int idProcesso) {
		String hql = "select o from SituacaoProcesso o where o.idProcesso = :idProcesso";
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("idProcesso", idProcesso);
		return EntityUtil.getSingleResult(query);
	}

	public void persistAcordaoSemAssinatura() {
		ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true);
		persistDocumentosAcordao('A');
	}

    /**
     * Método criado para ser utilizado no frame elaborarAcordao.xhtml
     */
	public void persistAcordaoSemAssinaturaFrame() {
		ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true);
		persistDocumentosAcordao('A', true);
	}

	public void updateAcordaoSemAssinatura() {
		getInstance().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudoEditorAcordao);
		ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(conteudoEditorAcordao);
		update();
	}

	public void persistEmentaComCertificadoDigital() {
		persistDocumentosAcordao('E');
		assinarDocumento();
	}

	public void updateEmentaComCertificadoDigital() {
		update();
		assinarDocumento();
	}

	/**
     * Método para inserir o Documento de Notas orais
     */
    public void persistNotasOraisComCertificadoDigital() {
		persistDocumentosAcordao('N');
		assinarDocumento();
	}

    /**
     * Método para atualizar o Documento de Notas orais
     */
	public void updateNotasOraisComCertificadoDigital() {
		update();
		assinarDocumento();
	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> getDocumentosVotosAcordao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from SessaoProcessoDocumento o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.ativo = true ");
		sb.append("and o.processoDocumento.processo = :processo ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
		sb.append("and (o.orgaoJulgador = :orgaoJulgador or o.orgaoJulgador = :ojVencedor) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
		q.setParameter("processo", sessaoPautaProcessoTrf.getProcessoTrf().getProcesso());
		q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		q.setParameter("orgaoJulgador", sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
		q.setParameter("ojVencedor", sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor());

		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	private List<SessaoProcessoDocumento> getTodosDocumentosVotosAcordao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select o from SessaoProcessoDocumento o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.ativo = true ");
		sb.append("and o.processoDocumento.processo = :processo ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
		q.setParameter("processo", sessaoPautaProcessoTrf.getProcessoTrf().getProcesso());
		q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		return q.getResultList();
	}

	public boolean existeDocumentoVotoNaoAssinado(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		List<SessaoProcessoDocumento> lista = new ArrayList<SessaoProcessoDocumento>();
		lista.addAll(getTodosDocumentosVotosAcordao(sessaoPautaProcessoTrf));
		for (SessaoProcessoDocumento sessaoProcessoDocumento : lista) {
			if (!ProcessoDocumentoHome.instance().isAssinado(sessaoProcessoDocumento.getProcessoDocumento())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adiciona as quebras de linha e separador ao conteudo do documento
	 * 
	 * @param sbDocumento
	 * @return
	 */
	private StringBuilder adicionaSeparador(StringBuilder sbDocumento) {
		sbDocumento.append("<br />");
		sbDocumento.append("<br />");
		sbDocumento.append("<br />");
		sbDocumento.append("<br />");
		return sbDocumento;
	}

	/**
	 * Verifica se é permitido inserir os votos do relator e voto vencedor no
	 * inteiro teor do acordão, levando em consideração que se o voto do relator
	 * for igual ao voto vencedor este sómente sera juntoato ao inteiro teor uma
	 * vez.
	 * 
	 * @param sppt
	 * @return
	 * @throws AcordaoException
	 */
	private void permiteJuncaoVotosInteiroTeor(SessaoPautaProcessoTrf sppt,
			List<SessaoProcessoDocumento> listVotoRelatorEVencedor) throws AcordaoException {
		String msgRelator = null;
		String msgVencedor = null;
		String msgNaoElaborado = null;
		try {
			boolean vencedorRelator = sppt.getOrgaoJulgadorVencedor().equals(sppt.getProcessoTrf().getOrgaoJulgador());

			if (listVotoRelatorEVencedor.size() > 0) {
				for (SessaoProcessoDocumento sessaoProcessoDocumento : listVotoRelatorEVencedor) {
					if (!ProcessoDocumentoHome.instance().isAssinado(sessaoProcessoDocumento.getProcessoDocumento())) {
						if ( vencedorRelator && sessaoProcessoDocumento.getOrgaoJulgador().equals(sppt.getProcessoTrf().getOrgaoJulgador())) {
							msgRelator = "O voto do relator não está assinado.";
						} else if ( !vencedorRelator && sessaoProcessoDocumento.getOrgaoJulgador().equals(sppt.getOrgaoJulgadorVencedor())) {
							msgVencedor = "O voto vencedor não está assinado.";
						}
					}
				}
			} else {
				msgNaoElaborado = "O voto ainda não foi elaborado.";
			}
		} catch (NullPointerException ex) {
			throw new AcordaoException("Ocorreu um erro ao tentar recuperar o relatório.");
		}
		if (!Strings.isEmpty(msgRelator)) {
			throw new AcordaoException(msgRelator);
		}
		if (!Strings.isEmpty(msgVencedor)) {
			throw new AcordaoException(msgVencedor);
		}
		if (!Strings.isEmpty(msgNaoElaborado)) {
			throw new AcordaoException(msgNaoElaborado);
		}
	}
	
	/**
	 * Junta votos do relator e vencedor ao inteiro teor do acórdão.
	 * 
	 * @param sbDocumento
	 * @return
	 */
	private StringBuilder juntaVotosInteiroTeor(StringBuilder sbDocumento,
			List<SessaoProcessoDocumento> listVotoRelatorEVencedor) {
		for (SessaoProcessoDocumento sessaoProcessoDocumento : listVotoRelatorEVencedor) {
			sbDocumento.append(sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin()
					.getModeloDocumento());
			adicionaSeparador(sbDocumento);
		}
		return sbDocumento;
	}

	/**
	 * Metódo que verifica se é permitido inserir o relatorio em questão no
	 * inteiro teor do acordão
	 * 
	 * @return
	 * @throws AcordaoException
	 */
	private void permiteJuncaoRelatorioInteiroTeor(SessaoPautaProcessoTrf sppt, SessaoProcessoDocumento spd)
			throws AcordaoException {
		try {
			if (spd != null) {
				if (!ProcessoDocumentoHome.instance().isAssinado(spd.getProcessoDocumento())) {
					throw new AcordaoException("O relatório não está assinado.");
				}
			} else {
				throw new AcordaoException("O relatório ainda não foi elaborado.");
			}
		} catch (NullPointerException ex) {
			throw new AcordaoException("Ocorreu um erro ao tentar recuperar o relatório.");
		}
	}

	/**
	 * Método que verifica se é permitido inserir a ementa no inteiro teor do
	 * acordão
	 */
	private void permiteJuncaoEmentaInteiroTeor(ProcessoDocumento pdEmenta) throws AcordaoException {
		try {
			if (!ProcessoDocumentoHome.instance().isAssinado(pdEmenta)) {
				throw new AcordaoException("A ementa não está assinada.");
			}
		} catch (NullPointerException ex) {
			throw new AcordaoException("A ementa ainda não foi elaborada.");
		}
	}

	/**
	 * Junta ementa e inteiro teor ao documento.
	 * 
	 * @return
	 */
	private StringBuilder juntaEmentaProclamacaoInteiroTeor(StringBuilder sbDocumento, String strAcordao) {
		if (strAcordao != "") {
			sbDocumento.append(strAcordao);
		}
		return adicionaSeparador(sbDocumento);
	}

	/**
	 * Recebe o documento e junta o relatorio a ele.
	 * 
	 * @return
	 */
	private StringBuilder juntaRelatorioInteiroTeor(StringBuilder sbDocumento, SessaoProcessoDocumento spd) {
		sbDocumento.append(spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
		return adicionaSeparador(sbDocumento);
	}

    /**
     * Verifica se as Notas Orais já foram assinadas.
     * @throws AcordaoException Exceção lançada caso as notas orais não estejam assinadas
     */
    private void checkListNotasOrais() throws AcordaoException {
        String idParametro = ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
        if(idParametro == null){
        	return;
        }
        TipoProcessoDocumento tpd = getEntityManager().getReference(TipoProcessoDocumento.class, new Integer(idParametro));

        SessaoProcessoDocumento spd = ComponentUtil.getSessaoProcessoDocumentoManager().getSessaoProcessoDocumentoByTipo(
                getSessao(), tpd, recuperaProcessoAtivo());
        ProcessoDocumento pd = spd.getProcessoDocumento();

		try {
			if (!ProcessoDocumentoHome.instance().isAssinado(pd)) {
				throw new AcordaoException("As Notas Orais não estão assinadas.");
			}
		} catch (NullPointerException ex) {
			throw new AcordaoException("As Notas Orais ainda não foram elaboradas.");
		}
    }
        
	private boolean gravarInteiroTeor(boolean isFrame) {
		SessaoPautaProcessoTrf sppt = SessaoPautaProcessoTrfHome.instance().getInstance();
		SessaoProcessoDocumento spd = getRelatorioMagistrado(sppt);
		List<SessaoProcessoDocumento> listVotoRelatorEVencedor = getDocumentosVotosAcordao(sppt);
		ProcessoDocumento pdEmenta = getEmentaElaboradaAssinada();
		try {
			checkListGravarInteiroTeor(sppt, spd, listVotoRelatorEVencedor, pdEmenta);

            // validação feita apenas quando o Acórdão é usado com Frame
            if (isFrame) {
                // verifica se as Notas orais foram assinadas - solicitação [PJEII-4330]
                checkListNotasOrais();
            }
		} catch (AcordaoException aEx) {
			log.error(aEx.getMessage());
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, aEx.getMessage());
			return false;
		}
		try {
			VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(ProcessoDocumentoBinHome.instance()
					.getCertChain());
			if (existeDocumentoVotoNaoAssinado(SessaoPautaProcessoTrfHome.instance().getInstance())) {
				exibeModalConfirmacao = true;
				return false;
			} else {
				gravacaoInteiroTeor(isFrame);
				return true;
			}
		} catch (CertificadoException ce) {
			log.error(ce.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "O certificado não é o mesmo do cadastro do usuário");
		} catch (Exception e) {
			log.error(e.getMessage());
			Util.setMessage(Severity.ERROR, "Erro ao assinar o documento: " + e.getMessage() + ".");
		}
		return false;
	}
	


    /**
     * Método ajustado para não interferir nas funcionalidades já existentes
     * 
     */
	public void gravacaoInteiroTeor() {
		try {
			  gravacaoInteiroTeor(false);
		} catch (PJeBusinessException e) {
			log.error(e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível completar a operação",e.getLocalizedMessage());
		}
      
    }

    /**
     * Método criado para ser utilizado pelo frame elaborarAcordao.xhtml
     * 
     */
	public void gravacaoInteiroTeorFrame() {
		try {
			  gravacaoInteiroTeor(true);
		} catch (PJeBusinessException e) {
			log.error(e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível completar a operação",e.getLocalizedMessage());
		}
    }


	/**
	 * Metodo que faz a gravação propriamente dita do inteiro teor do acordão
	 * 
	 * @param sppt
	 * @param spd
	 * @param listVotoRelatorEVencedor
	 * @param strAcordao
	 * @param sbDocumento
	 * @throws PJeBusinessException 
	 */
	private void gravacaoInteiroTeor(boolean isFrame) throws PJeBusinessException {
		SessaoPautaProcessoTrf sppt = SessaoPautaProcessoTrfHome.instance().getInstance();
		ProcessoDocumento ementa = getEmentaElaboradaAssinada();
		SessaoProcessoDocumento relat = getRelatorioMagistrado(sppt);
		if(ementa != null){
			ementa.setDataJuntada(new Date());
		}
		if(relat.getProcessoDocumento() != null){
			relat.getProcessoDocumento().setDataJuntada(new Date());
		}
		List<SessaoProcessoDocumento> listVotoRelatorEVencedor = getDocumentosVotosAcordao(sppt);
		for(SessaoProcessoDocumento doc: listVotoRelatorEVencedor){
			if(doc.getProcessoDocumento() != null){
				doc.getProcessoDocumento().setDataJuntada(new Date());
			}
		}
	
		String strAcordao = getConteudoEditorAcordao();

		// Monta o documento final
		StringBuilder sbDocumento = new StringBuilder("");

        // Condição adicionada pela solicitação [PJEII-4330]
        if (!isFrame) {
        	String idModelo = ComponentUtil.getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_INTEIRO_TEOR);
        	if(idModelo != null && !idModelo.isEmpty()){
        		try {
                    ModeloDocumento modeloAcordao = ComponentUtil.getModeloDocumentoManager().findById(new Integer(idModelo));
                    String conteudoInteiroTeor = ComponentUtil.getModeloDocumentoManager().obtemConteudo(modeloAcordao);
                    sbDocumento.append(conteudoInteiroTeor);
                } catch (PJeBusinessException ex) {
                    // não vai ocorrer, porque antes já foi verificado a existência do parâmetro
                }
        	}
        	else{
	            sbDocumento = juntaRelatorioInteiroTeor(sbDocumento, relat);
	            sbDocumento = juntaVotosInteiroTeor(sbDocumento, listVotoRelatorEVencedor);
	            sbDocumento = juntaEmentaProclamacaoInteiroTeor(sbDocumento, strAcordao);
	            sbDocumento = juntaComposicaoInteiroTeor(sppt, sbDocumento);
            }
        } else {
            // obtem o parametro com o codigo do Modelo de Documento do Inteiro Teor
            String idModelo = ComponentUtil.getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_INTEIRO_TEOR);
            try {
                ModeloDocumento modeloAcordao = ComponentUtil.getModeloDocumentoManager().findById(new Integer(idModelo));
                String conteudoInteiroTeor = ComponentUtil.getModeloDocumentoManager().obtemConteudo(modeloAcordao);
                sbDocumento.append(conteudoInteiroTeor);
            } catch (PJeBusinessException ex) {
                // não vai ocorrer, porque antes já foi verificado a existência do parâmetro
            }
        }

		if (!assinado) {
			assinarDocumento(true);
		}

		String signature = ProcessoDocumentoBinHome.instance().getSignature();
		String certChain = ProcessoDocumentoBinHome.instance().getCertChain();

		ProcessoDocumentoHome.instance().newInstance();

		ProcessoDocumentoBinHome.instance().setSignature(signature);
		ProcessoDocumentoBinHome.instance().setCertChain(certChain);

		ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(sbDocumento.toString());
		Usuario usuario = Authenticator.getUsuarioLogado();
		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcessoDocumento("Inteiro teor");
		pd.setUsuarioInclusao(usuario);
		pd.setDocumentoSigiloso(Boolean.FALSE);
		pd.setDataInclusao(new Date());
		pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoInteiroTeor());

		ProcessoHome.instance().setInstance(sppt.getProcessoTrf().getProcesso());

		ProcessoDocumentoHome.instance().setInstance(pd);
		ProcessoDocumentoHome.instance().persistComAssinatura();

		if(EventsTipoDocumentoTreeHandler.instance().getEventoBeanList() != null &&
				!EventsTipoDocumentoTreeHandler.instance().getEventoBeanList().isEmpty()){
			ComponentUtil.getLancadorMovimentosAction().lancarMovimentosSemFluxo(EventsTipoDocumentoTreeHandler.instance().getEventoBeanList(), pd, sppt.getProcessoTrf().getProcesso());
		}

		SessaoProcessoDocumento sessaopd = new SessaoProcessoDocumento();
		sessaopd.setProcessoDocumento(pd);
		sessaopd.setLiberacao(true);
		sessaopd.setSessao(SessaoPautaProcessoTrfHome.instance().getInstance().getSessao());
		sessaopd.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());

		getEntityManager().persist(sessaopd);
		getEntityManager().flush();
		
		Events.instance().raiseEvent(Eventos.EVENTO_ACORDAO_GERADO, pd);

		ProcessoDocumentoBinHome.instance().setInstance(pd.getProcessoDocumentoBin());
		setAcordaoAssinado(true);

		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso.");
		setExibeModalConfirmacao(false);
		ComponentUtil.getProcessoDocumentoTrfLocalManager().criarDocumentoPublico(pd, liberarConsultaPublica);
	}

	/**
	 * Verifica se o inteiro teor está pronto para ser gravado.
	 * 
	 * @param sppt
	 * @param spd
	 * @param listVotoRelatorEVencedor
	 * @param pdEmenta
	 */
	private void checkListGravarInteiroTeor(SessaoPautaProcessoTrf sppt, SessaoProcessoDocumento spd,
			List<SessaoProcessoDocumento> listVotoRelatorEVencedor, ProcessoDocumento pdEmenta) throws AcordaoException {
		permiteJuncaoRelatorioInteiroTeor(sppt, spd);
		permiteJuncaoVotosInteiroTeor(sppt, listVotoRelatorEVencedor);
		permiteJuncaoEmentaInteiroTeor(pdEmenta);
	}

	/**
	 * Junta a composição da sessão ao inteiro teor do acódão
	 * 
	 * @param sppt
	 * @param sbDocumento
	 */
	private StringBuilder juntaComposicaoInteiroTeor(SessaoPautaProcessoTrf sppt, StringBuilder sbDocumento) {
		List<OrgaoJulgador> ojList = new ArrayList<OrgaoJulgador>(0);
		ojList.addAll(ComponentUtil.getSessaoManager().getOrgaosJulgadoresDaSessao(sppt.getSessao()));

		for (OrgaoJulgador oj : ojList) {
			sbDocumento.append("<br />");
			sbDocumento.append(oj.toString());
		}
		return adicionaSeparador(sbDocumento);
	}

	private void liberarDocumento(Boolean valorBooleano) {
		int idProcesso = getInstance().getProcessoDocumento().getProcesso().getIdProcesso();
		List<SessaoProcessoDocumento> documentosAntecipados = SessaoProcessoDocumentoHome.instance()
				.documentosAntecipados(idProcesso, false);
		for (SessaoProcessoDocumento spda : documentosAntecipados) {
			spda.setLiberacao(valorBooleano);
			getEntityManager().merge(spda);
			EntityUtil.flush();
		}
	}
	
	public void persistRelatorioComAssinatura(ArquivoAssinadoHash arquivoAssinado ) {
		if (assina()) {
			persistRelatorio();
			if (getInstance().getLiberacao()) {
				liberarDocumento(Boolean.TRUE);
			}else{
				liberarDocumento(Boolean.FALSE);
			}
			if(arquivoAssinado != null){
				this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
				this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
			}
			assinarDocumento();
		}
	}
	
	/**
	 * grava um documento antecipado na sessao para um processo do tipo
	 * relatorio e assina o documento
	 */
	public void persistRelatorioComAssinatura() {
		persistRelatorioComAssinatura(null);
	}

	private boolean assina() {
		if (ProcessoDocumentoBinHome.instance().isModeloVazio()	&& StringUtils.isEmpty(getModeloDocumento()) && ProcessoDocumentoBinHome.instance().getInstance().getFile()==null) {
			FacesMessages.instance().add(Severity.ERROR, "O documento está vazio.");
			return false;
		}
		return true;
	}

	public void persistEmentaComAssinatura() {
		persistEmentaComAssinatura(null);
	}
	
	/**
	 * grava um documento antecipado na sessao para um processo do tipo ementa e
	 * assina o documento
	 */
	public void persistEmentaComAssinatura(ArquivoAssinadoHash arquivoAssinado) {
		if (assina()) {
			persistEmenta();
			if (getInstance().getLiberacao()) {
				liberarDocumento(Boolean.TRUE);
			}else{
				liberarDocumento(Boolean.FALSE);
			}
			if(arquivoAssinado != null){
				this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
				this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
			}			
			assinarDocumento();
		}
	}

	/**
	 * altera um documento antecipado na sessao para um processo e assina o
	 * documento
	 */
	public void updateComAssinatura() {
		if (assina()) {  // Não permite a gravação do documento vazio. 
			assinarDocumento();
		}
	}

	/**
	 * grava um documento antecipado na sessao para um processo do tipo ementa
	 */
	public void persistEmenta() {
		persistEmenta(Boolean.TRUE);
	}
	
	/**
	 * Grava um documento antecipado na sessao para um processo do tipo Ementa. Caso a sessão de julgamento ainda não esteja registrada, apenas o documento vai ser cadastrado e o vínculo
	 * com a sessão será ignorado
	 */
	public void persistEmenta(Boolean isLiberarDocumento) {
		// Não permite a gravação do documento vazio.
		if (assina()) {
			ProcessoDocumento pd = new ProcessoDocumento();
			try{
				pd = criarProcessoDocumento(pd, ParametroUtil.instance().getTipoProcessoDocumentoEmenta(), "Ementa");
			} catch(PJeBusinessException e){
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarEmenta.erro", e.getMessage());
				log.error(e.getMessage());
			}

			if (getInstance().getIdSessaoProcessoDocumento() > 0) {
				setInstance(getEntityManager().find(getEntityClass(), getInstance().getIdSessaoProcessoDocumento()));
				getInstance().setLiberacao(isLiberarDocumento);
				super.update();
			} else {
				gravarDocumentoAntecipado(pd);
			}
		}
	}

	private ProcessoDocumento criarProcessoDocumento(ProcessoDocumento pd, TipoProcessoDocumento tipoProcDoc, String dsTipoProcDoc) throws PJeBusinessException {
		if (getInstance().getProcessoDocumento() != null && getInstance().getProcessoDocumento().getIdProcessoDocumento() > 0) {
			pd = getInstance().getProcessoDocumento();
		} else {
			pd.setProcessoDocumento(dsTipoProcDoc);
			pd.setTipoProcessoDocumento(tipoProcDoc);
			pd.setDataInclusao(new Date());
			pd.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
			pd.setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			pd.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());
			documentoJudicialService.persist(pd, true);
		}
		return pd;
	}
	
	/**
	 * Metodo que retorna uma lista contendos os documentos que foram
	 * antecipados recebendo como parametro o id do processo e um boleano
	 * liberado que determina se são todos os documentos ou apenas os que
	 * liberado for igual a 'N' caso seja falso
	 * 
	 * @param idProcesso
	 * @param todos
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> documentosAntecipados(int idProcesso, boolean todos) {
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		processoTrfHome.setId(idProcesso);
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o where ");
		if (!todos) {
			sb.append("o.liberacao = false and ");
		}
		sb.append("o.processoDocumento.processo.idProcesso = :idProcesso ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento != :tipoDocumento ");
		sb.append("and o.processoDocumento.ativo = true ");
		sb.append("and o.orgaoJulgador = :oj ");
		if (isManaged()) {
			sb.append("and o.idSessaoProcessoDocumento != :id");
		}

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("tipoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoInteiroTeor());
		q.setParameter("idProcesso", idProcesso);
		q.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
		if (isManaged()) {
			q.setParameter("id", getInstance().getIdSessaoProcessoDocumento());
		}

		List<SessaoProcessoDocumento> listaAntecipados = new ArrayList<SessaoProcessoDocumento>();

		for (int i = 0; i < q.getResultList().size(); i++) {
			if (q.getResultList().get(i) instanceof SessaoProcessoDocumento) {
				SessaoProcessoDocumento sessaoProcessoDocumento = (SessaoProcessoDocumento) q.getResultList().get(i);
				if (ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(sessaoProcessoDocumento.getProcessoDocumento(),
						processoTrfHome.getInstance().getProcesso())) {
					listaAntecipados.add(sessaoProcessoDocumento);
				}
			} else {
				SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = (SessaoProcessoDocumentoVoto) q.getResultList().get(i);
				if (ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(sessaoProcessoDocumentoVoto.getDtVoto(),
						processoTrfHome.getInstance().getProcesso())) {
					listaAntecipados.add(sessaoProcessoDocumentoVoto);
				}
			}
		}

		return listaAntecipados.size() > 0 ? listaAntecipados : Collections.EMPTY_LIST;
	}


	public  List<SessaoProcessoDocumento> carregarDocumento(char tipo) {
		List<SessaoProcessoDocumento> listSessaoProcessoDocumento = carregarDocumento(tipo, false);
		if (CollectionUtilsPje.isEmpty(listSessaoProcessoDocumento)) {
			listSessaoProcessoDocumento = carregarDocumentosSemSessaoJulgamento(tipo);
		}
		return listSessaoProcessoDocumento;
	}

	/**
	 * [PJEVII-1858] 
	 * Caso não tenham sido encontrados documentos em sessão, verifica se existem documentos elaborados antes da Sessão de Julgamento.
	 * 
	 * @param tipo Tipo de Documento Relatório, Ementa ou Voto Relator
	 * @param listSessaoProcessoDocumento 
	 * @return
	 */
	private List<SessaoProcessoDocumento> carregarDocumentosSemSessaoJulgamento(char tipo) {
		List<SessaoProcessoDocumento> listSessaoProcessoDocumento = new ArrayList<SessaoProcessoDocumento>();
		TipoProcessoDocumento tipoDoc = null;
		if (tipo == 'V' || tipo == 'R' || tipo == 'E') {
			if (tipo == 'V') {
				tipoDoc = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
			} else if (tipo == 'R') {
				tipoDoc = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			} else {
				tipoDoc = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
			}

			Integer[] tiposDocumento = new Integer[] {tipoDoc.getIdTipoProcessoDocumento()};

			List<ProcessoDocumento> listProcDoc = procDocManager.getDocumentosPorTipo(ProcessoJbpmUtil.getProcessoTrf(), tiposDocumento);
			if (!CollectionUtilsPje.isEmpty(listProcDoc)) {
				SessaoProcessoDocumento sessaoProcDoc = new SessaoProcessoDocumento();
				listSessaoProcessoDocumento = new ArrayList<SessaoProcessoDocumento>(listProcDoc.size());
				if (tipo == 'V') {
					sessaoProcDoc = new SessaoProcessoDocumentoVoto();
				}
				sessaoProcDoc.setProcessoDocumento(listProcDoc.get(0));

				listSessaoProcessoDocumento.add(sessaoProcDoc);
			}
		}
		return listSessaoProcessoDocumento;
	}
	
	private Processo recuperaProcessoAtivo() {
		Processo processo = null;
		if(this.getProcessoJudicial() != null) {
			processo = this.getProcessoJudicial().getProcesso();
		}else {
			processo = ProcessoHome.instance().getInstance();
		}
		return processo;
	}

	/**
	 * metodo que carrega os documentos antecipados do processo que esta sendo
	 * acessado recebe como parametro um char que diz o tipo de documento, que
	 * pode ser 'V' = documento do tipo voto 'v' = voto vencedor 'R' = documento
	 * do tipo relatorio 'A' = documento do tipo acordao
	 * 
	 * @param tipo
	 */
	// TODO Este método está duplicado nas classes
	// SessaoProcessoDocumentoVotoHome (voto(), relatorio() e ementa()) e
	// nas abas VotoRelator, Relatorio e Ementa do painel do magistrado na
	// sessão. Refatorar para unificar os métodos!

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> carregarDocumento(char tipo, boolean emSessao) {
		Processo processo = this.recuperaProcessoAtivo();
		if(processo == null) {
			FacesMessages.instance().add(Severity.ERROR, "Processo não encontrado.");
			return null;
		}

		boolean processoPossuiEmenta = processoPossuiEmenta(processo.getIdProcesso());

		newInstance();
		StringBuilder sb = new StringBuilder();
		TipoProcessoDocumento tpd;
		if (tipo == 'V' || tipo == 'v') {
			sb.append("select o from SessaoProcessoDocumentoVoto o where ");
			tpd = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
		} else {
			sb.append("select o from SessaoProcessoDocumento o where ");
			if (tipo == 'R') {
				carregarRelatorio = false;
				tpd = ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
			} else {
				 if (tipo == 'N') { // adicionado pela solicitação [PJEII-4330] referente a Notas Orais
                    String idParametro = ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_NOTAS_ORAIS);
					tpd = getEntityManager().getReference(TipoProcessoDocumento.class, new Integer(idParametro));
				}else if (tipo == 'A' || !processoPossuiEmenta) {
					tpd = ParametroUtil.instance().getTipoProcessoDocumentoAcordao();
				} else if (tipo == 'E') {
					tpd = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
				} else {
					FacesMessages.instance().add(Severity.ERROR, "Tipo de documento invalido.");
					return null;
				}
			}
		}
		sb.append("o.processoDocumento.processo.idProcesso = :id ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
		sb.append("and o.processoDocumento.ativo = true ");
		if (emSessao) {
			sb.append("and o.sessao = :sessao ");
			if (tipo == 'v' || tipo == 'E' || tipo == 'N') {
				sb.append("and o.orgaoJulgador = :oJVencedor ");
			} else if (tipo == 'V' || tipo == 'R') {
				sb.append("and o.orgaoJulgador = :oj ");
			}
		} else {
			if (tipo == 'v' || tipo == 'E' || tipo == 'N') {
				sb.append("and o.orgaoJulgador = :oJVencedor ");
			} else if (tipo == 'V' || tipo == 'R') {
				sb.append("and o.orgaoJulgador = :oj ");
			}
		}

		Query q = getEntityManager().createQuery(sb.toString());
		if (emSessao) {
			SessaoPautaProcessoTrf sessaoPautaProcessoTrf = SessaoPautaProcessoTrfHome.instance().getInstance();
			q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
			if (tipo == 'v' || tipo == 'E' || tipo == 'N') {
				q.setParameter("oJVencedor", sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor());
			} else if (tipo == 'V' || tipo == 'R') {
				q.setParameter("oj", sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
			}
		} else {
			if (tipo == 'v' || tipo == 'E' || tipo == 'N') {
				SessaoPautaProcessoTrf sessaoPautaProcessoTrf = SessaoPautaProcessoTrfHome.instance().getInstance();
				if(sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() == null){
					try {
						ProcessoJudicialManager pjm = ComponentUtil.getComponent(ProcessoJudicialManager.class);
						q.setParameter("oJVencedor", pjm.findById(processo.getIdProcesso()).getOrgaoJulgador());
					} catch (PJeBusinessException e) {
						return null;
					}
				}else{
					q.setParameter("oJVencedor", sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor());
				}
			} else if (tipo == 'V' || tipo == 'R') {
				try {
					ProcessoJudicialManager pjm = ComponentUtil.getComponent(ProcessoJudicialManager.class);
					q.setParameter("oj",   pjm.findById(processo.getIdProcesso()).getOrgaoJulgador());
				} catch (PJeBusinessException e) {
					return null;
				}
			}
		}
		q.setParameter("id", processo.getIdProcesso());
		q.setParameter("tpd", tpd);

		ProcessoDocumentoBinHome binHome = ProcessoDocumentoBinHome.instance();

		ProcessoHome.instance().setTipoProcessoDocumento(tpd);
		
		List<SessaoProcessoDocumento> listDocumentos = q.getResultList(); 

		//PJEII-5839 - Carregar documentos antecipados caso a sessão não possua documentos
		if(emSessao && (listDocumentos == null || listDocumentos.isEmpty())){
			listDocumentos = carregarDocumento(tipo);
			if(listDocumentos != null && !listDocumentos.isEmpty()){
				SessaoPautaProcessoTrf sessaoPautaProcessoTrf = SessaoPautaProcessoTrfHome.instance().getInstance();
				
				for(SessaoProcessoDocumento documento: listDocumentos){
					if(documento.getSessao() == null){
						documento.setSessao(sessaoPautaProcessoTrf.getSessao());
					}
				}
				
				getEntityManager().flush();
			}
			else{
				return null;
			}
			
		}
		
		for (int i = 0; i < listDocumentos.size(); i++) {
			if (tipo == 'V' || tipo == 'v') {
				SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto) listDocumentos.get(i);
				if (ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(voto.getDtVoto(), processo)) {
					ProcessoDocumentoHome.instance().setInstance(voto.getProcessoDocumento());
					SessaoProcessoDocumentoVotoHome votoHome = SessaoProcessoDocumentoVotoHome.instance();
					votoHome.setInstance(voto);
					votoHome.setVotoAntigo(voto.getTipoVoto());
					binHome.setInstance(votoHome.getInstance().getProcessoDocumento().getProcessoDocumentoBin());
					ProcessoDocumentoBin processoDocumentoBin = votoHome.getInstance().getProcessoDocumento()
							.getProcessoDocumentoBin();
					votoHome.setAssinado(documentoAssinado(processoDocumentoBin));
				}
			} else {
				SessaoProcessoDocumento documento = listDocumentos.get(i);
				if(!processoPossuiEmenta && tipo == 'E' && emSessao){
					String conteudoEditorAcordao = documento.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
					ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(conteudoEditorAcordao);
					return null;
				}				
				if (ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(documento.getProcessoDocumento(), processo)) {
					ProcessoDocumentoHome.instance().setInstance(documento.getProcessoDocumento());
					ProcessoTrfHome.instance().setId(documento.getProcessoDocumento().getProcesso().getIdProcesso());
					setInstance(documento);
					binHome.setInstance(documento.getProcessoDocumento().getProcessoDocumentoBin());
					setAssinado(documentoAssinado(documento.getProcessoDocumento().getProcessoDocumentoBin()));
				}
			}
			
			
		}
		return listDocumentos;
	}

	public void carregarDocumentosAcordao(char tipo) {
		ProcessoHome.instance().setInstance(
				SessaoPautaProcessoTrfHome.instance().getInstance().getProcessoTrf().getProcesso());
		carregarDocumento(tipo, true);
		
		if (tipo == 'A') {
			if (verificaExistsInteiroTeor()) {
				setAcordaoAssinado(true);
			} else {
				if(getInstance().getProcessoDocumento() != null && getInstance().getProcessoDocumento().getProcessoDocumentoBin() != null){
					conteudoEditorAcordao = getInstance().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
				}
				EventosTreeHandler.instance().carregaEventos();
				if (!EventosTreeHandler.instance().getEventoBeanList().isEmpty()) {
					EventsTipoDocumentoTreeHandler.instance().setEventoBeanList(
							EventosTreeHandler.instance().getEventoBeanList());
				}
			}
		}
	}

	private boolean processoPossuiEmenta(Integer processoId){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoProcessoDocumento o where ");
		sb.append("o.processoDocumento.processo.idProcesso = :id ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
		sb.append("and o.processoDocumento.ativo = true ");
		OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
		if(oj != null){
			sb.append("and o.orgaoJulgador = :oj ");	
		}
		Query q = getEntityManager().createQuery(sb.toString());
		if(oj != null){
			q.setParameter("oj", oj);
		}
		q.setParameter("id", processoId);
		q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		Long result = (Long)q.getSingleResult();
		return result > 0;
		
	}

	private boolean documentoAssinado(ProcessoDocumentoBin pdBin) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from ProcessoDocumentoBinPessoaAssinatura o ");
		sb.append("where o.processoDocumentoBin = :pdBin");

		Query q = getEntityManager().createQuery(sb.toString()).setParameter("pdBin", pdBin);

		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void removerAssinatura(ProcessoDocumento pd) {
		try {
			ComponentUtil.getComponent(AssinaturaDocumentoService.class).removeAssinatura(pd);

			setTodosAssinados(false);
			
			ProcessoDocumentoBinHome.instance().setAssinado(false);
			ProcessoDocumentoBinHome.instance().setSignature("");
			ProcessoDocumentoBinHome.instance().setCertChain("");
			cancelarLiberacoes();
			getEntityManager().flush();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Assinatura removida com sucesso.");
		} catch (Exception e) {
			log.error(e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover a assinatura.");
		}
	}
	
	private void setTodosAssinados(boolean assinado) {
		setAssinado(assinado);
		setRelatorioMagistradoAssinado(assinado);
		setEmentaAssinada(assinado);
		SessaoProcessoDocumentoVotoHome.instance().setAssinado(assinado);
		SessaoProcessoDocumentoVotoHome.instance().setVotoMagistradoAssinado(assinado);
	}

	public Boolean verificaExistsInteiroTeor() {
		boolean existe = Boolean.FALSE;
		Processo processo = recuperaProcessoAtivo();
		if(processo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from SessaoProcessoDocumento o ");
			sb.append("where o.sessao = :sessao ");
			sb.append("and o.processoDocumento.processo.idProcesso = :idProcesso ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
			sb.append("and o.processoDocumento.ativo = true");
			
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("sessao", SessaoPautaProcessoTrfHome.instance().getInstance().getSessao());
			q.setParameter("idProcesso", processo.getIdProcesso());
			q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoInteiroTeor());
			
			try {
				Long retorno = (Long) q.getSingleResult();
				existe = retorno > 0;
			} catch (NoResultException no) {
				// void
			}
		}
		return existe;
	}

	public void ementa(SessaoPautaProcessoTrf p) {
		newInstance();
		setDocEmenta(buscaEmenta(p, false));
		setDivEmenta(true);
		setSessaoPautaProcessoTrf(p);
		SessaoProcessoDocumentoVotoHome.instance().verificaStatusSessao(p);
		/**
		 * Ao selecionar Modelo de Documento ,menu: Audiências e sessões > Relação de julgamento >
		 * SessaoPopUp > aba Relação de julgamento, o sistema gera erro no modelo
		 * carregado no editor. O mesmo ocorre ao alternar entre as opções 
		 * Voto,Ementa e Relatório.
		 */
		Contexts.getPageContext().set("idProcessoTrf", p.getProcessoTrf().getIdProcessoTrf());
		if (getDocEmenta() != null) {
			modeloDocumento = getDocEmenta().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
			setEmentaAssinada(verificaDocumentoAssinado(getDocEmenta().getProcessoDocumento()));
			getInstance().setProcessoDocumento(getDocEmenta().getProcessoDocumento());
			ProcessoDocumentoHome.instance().setInstance(getDocEmenta().getProcessoDocumento());
			ProcessoTrfHome.instance().setInstance(p.getProcessoTrf());
		} else {
			setDocEmenta(new SessaoProcessoDocumento());
			ProcessoDocumentoHome.instance().getInstance()
					.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		}
	}

	/**
	 * 
	 * @param sessaoPPT
	 * @param ementaVotacao
	 *            se for true é chamando na aba votação antecipada e nao precisa
	 *            do órgão julgador
	 * @return
	 */
	public SessaoProcessoDocumento buscaEmenta(SessaoPautaProcessoTrf sessaoPPT, boolean ementaVotacao) {
		if (sessaoPPT==null){
			return null;
		}
		if(mapaDocumento.containsKey(String.valueOf(ementaVotacao) + "E:" + sessaoPPT.getIdSessaoPautaProcessoTrf())){
			return mapaDocumento.get(String.valueOf(ementaVotacao) + "E:" + sessaoPPT.getIdSessaoPautaProcessoTrf());
		}
		OrgaoJulgador ojAtual = Authenticator.getOrgaoJulgadorAtual();
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o ");
		sb.append("where o.processoDocumento.ativo = true ");
		sb.append("and o.sessao = :sessao ");
		if (!ementaVotacao) {
			sb.append("and o.orgaoJulgador = :oj ");
		}
		sb.append("and o.processoDocumento.processo = :processo ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tipoProcessoDocumento");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", sessaoPPT.getSessao());
		if (!ementaVotacao) {
			query.setParameter("oj", ojAtual);
		}
		query.setParameter("processo", sessaoPPT.getProcessoTrf().getProcesso());
		query.setParameter("tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		SessaoProcessoDocumento ementa = EntityUtil.getSingleResult(query);

		if (ementa != null
				&& ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(ementa.getProcessoDocumento(), sessaoPPT
						.getProcessoTrf().getProcesso())) {
			mapaDocumento.put(String.valueOf(ementaVotacao) + "E:" + sessaoPPT.getIdSessaoPautaProcessoTrf(),ementa);
			return ementa;
		}
		mapaDocumento.put(String.valueOf(ementaVotacao) + "E:" + sessaoPPT.getIdSessaoPautaProcessoTrf(),null);
		return null;
	}

	public Boolean verificaDocumentoAssinado(ProcessoDocumento pd) {
		return ProcessoDocumentoHome.instance().isAssinado(pd);
	}

	public void updateEmenta() {
		if (!assina()) { return; }  // Não permite a gravação do documento vazio.
		try{
			if(this.ementa != null){
				ComponentUtil.getDocumentoJudicialService().persist(this.ementa, true);
				EntityUtil.getEntityManager().flush();
				FacesMessages.instance().clear();
			}else{
				ProcessoDocumentoHome.instance().getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
				ProcessoDocumentoHome.instance().getInstance().setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());

				if (ProcessoDocumentoHome.instance().getInstance().getTipoProcessoDocumento() == null) {
					ProcessoDocumentoHome.instance().getInstance().setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
				}

				ComponentUtil.getDocumentoJudicialService().persist(ProcessoDocumentoHome.instance().getInstance(), true);
				EntityUtil.getEntityManager().flush();
				FacesMessages.instance().add(Severity.INFO, "Alterações gravadas com sucesso");
			}
		}catch(PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar a ementa:  " + e.getMessage(), e);
			log.error(e.getMessage());
		}
	}

	public void cadastrarEmenta() {
		if (((Pessoa.instanceOf(Authenticator.getUsuarioLogado(), PessoaMagistrado.class)))
				|| Authenticator.getPapelAtual().getIdentificador().equals(Papeis.ASSESSOR)
				|| Authenticator.isMagistrado()) {

			String ret = null;

			try {
				String signature = ProcessoDocumentoBinHome.instance().getSignature();
				String certChain = ProcessoDocumentoBinHome.instance().getCertChain();

				ProcessoDocumentoHome pdHome = ComponentUtil.getComponent(ProcessoDocumentoHome.class);
				pdHome.newInstance();
				ProcessoHome.instance().setInstance(getSessaoPautaProcessoTrf().getProcessoTrf().getProcesso());
				TipoProcessoDocumento tipo = ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
				pdHome.getInstance().setTipoProcessoDocumento(tipo);
				pdHome.getInstance().setProcessoDocumento("Ementa");

				// ProcessoDocumentoBinHome.instance().newInstance();
				ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(modeloDocumento);
				ProcessoDocumentoBinHome.instance().setCertChain(certChain);
				ProcessoDocumentoBinHome.instance().setSignature(signature);

				ProcessoDocumentoHome.instance().persist();

				// Cadastrando a ementa que é do tipo SessaoProcessoDocumento
				ProcessoDocumento pd = ProcessoDocumentoHome.instance().getInstance();
				OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
				//getDocEmenta().setIdSessaoProcessoDocumento(pd.getIdProcessoDocumento());
				getDocEmenta().setOrgaoJulgador(oj);
				getDocEmenta().setProcessoDocumento(pd);
				SessaoProcessoDocumentoVoto spdv = SessaoProcessoDocumentoVotoHome.instance().getVotoMagistrado(
						getSessaoPautaProcessoTrf());
				if (spdv != null && spdv.getLiberacao()) {
					getDocEmenta().setLiberacao(spdv.getLiberacao());
				}
				getDocEmenta().setSessao(SessaoHome.instance().getInstance());
				getEntityManager().persist(getDocEmenta());
				getEntityManager().flush();

				ret = "persisted";
				mapaDocumento.remove(String.valueOf(true)+"E:"+getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf());
				mapaDocumento.remove(String.valueOf(false)+"E:"+getSessaoPautaProcessoTrf().getIdSessaoPautaProcessoTrf());

			} catch (Exception e) {
				log.error(e.getMessage());
				FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento.");
				ret = null;
			}
			if (ret != null) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Ementa cadastrada com sucesso.");
			}

		} else {
			FacesMessages.instance().add(Severity.ERROR, "O usuário atual não é magistrado ou assessor.");
			return;
		}
	}

	public void removerEmenta() {
		try {
			ProcessoDocumentoHome.instance().setInstance(getDocEmenta().getProcessoDocumento());
			ProcessoDocumentoHome.instance().inactive();
			newInstance();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Ementa removida com sucesso.");
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
	}

	public void assinarEmenta() {
		if (getInstance().getProcessoDocumento() != null && getDocEmenta().getIdSessaoProcessoDocumento() != 0) {
			try {
				atualizarRelatorioEmenta();

				ProcessoDocumentoBin pdBin = getDocEmenta().getProcessoDocumento().getProcessoDocumentoBin();
				pdBin.setModeloDocumento(modeloDocumento);
				ComponentUtil.getComponent(ProcessoDocumentoBinManager.class).mergeAndFlush(pdBin);

				ProcessoDocumentoBinHome.instance().setInstance(pdBin);
				setSessaoPauta(null);
				setSessaoPautaRelatorio(null);
				FacesMessages.instance().clear();
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR,"Houve um erro na atualização da ementa. A assinatura foi cancelada.", e.getMessage());
				return;
			}
		} else {
				cadastrarEmenta();
		}
		if (ProcessoDocumentoBinHome.instance().assinarDocumento()) {
			setEmentaAssinada(Boolean.TRUE);
		}

	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumento> getDocumentosSessao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.processo = :processo ");
		sb.append("and o.processoDocumento.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", sessaoPautaProcessoTrf.getSessao());
		q.setParameter("processo", sessaoPautaProcessoTrf.getProcessoTrf().getProcesso());
		List<SessaoProcessoDocumento> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista;
		}
		return Collections.emptyList();
	}

	public boolean procuraVotoAntecipadoLiberado() {
		boolean liberado = Boolean.FALSE;
		Processo processo = recuperaProcessoAtivo();
		if(processo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from SessaoProcessoDocumentoVoto o where ");
			sb.append("o.tipoInclusao = 'A' ");
			sb.append("and o.sessao is null ");
			sb.append("and o.liberacao = true ");
			sb.append("and o.processoDocumento.processo.idProcesso = :id ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
			sb.append("and o.processoDocumento.ativo = true ");
			sb.append("and o.orgaoJulgador = :oj ");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("id", processo.getIdProcesso());
			q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoVoto());
			q.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
			try {
				Long retorno = (Long) q.getSingleResult();
				liberado = retorno > 0;
			} catch (NoResultException no) {
				//void 
			}
		}
		return liberado;
	}

	/**
	 * Este método retorna uma lista de ProcessoDocumento ativos, do tipo Ementa
	 * de acordo com a sessão.
	 * 
	 * @return List<ProcessoDocumento> caso exista registros, uma lista vazia
	 *         caso não.
	 */
	@SuppressWarnings("unchecked")
	private List<ProcessoDocumento> listaTipoProcessoDocumentoEmentaSessao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoDocumento from SessaoProcessoDocumento o ");
		sb.append("where o.processoDocumento.tipoProcessoDocumento.ativo = true ");
		sb.append("and o.sessao.idSessao = :idSessao ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tipoProcessoDocumento ");
		SessaoPautaProcessoTrf sppt = SessaoPautaProcessoTrfHome.instance().getInstance();
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idSessao", sppt.getIdSessaoPautaProcessoTrf());
		q.setParameter("tipoProcessoDocumento", ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		List<ProcessoDocumento> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista;
		}
		return Collections.emptyList();
	}

	private void inativaTipoProcessoDocumentoEmentaSessao() {
		if (listaTipoProcessoDocumentoEmentaSessao().size() > 0) {
			for (ProcessoDocumento pd : listaTipoProcessoDocumentoEmentaSessao()) {
				pd.setAtivo(false);
				getEntityManager().merge(pd);
				getEntityManager().flush();
			}
		}
	}

	public void carregarDocumentoEmenta(char tipo) {
		ProcessoHome.instance().setInstance(
				SessaoPautaProcessoTrfHome.instance().getInstance().getProcessoTrf().getProcesso());
		carregarDocumento(tipo, true);
	}

	@SuppressWarnings("unchecked")
	public ProcessoDocumento getEmentaElaboradaAssinada() {
		ProcessoDocumento ementaAssinada = null;
		Processo processo = recuperaProcessoAtivo();
		if(processo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o.processoDocumento from SessaoProcessoDocumento o ");
			sb.append("where o.sessao.idSessao = :sessao ");
			sb.append("and o.orgaoJulgador.idOrgaoJulgador = :oJVencedor ");
			sb.append("and o.processoDocumento.processo.idProcesso = :idProcesso ");
			sb.append("and o.processoDocumento.tipoProcessoDocumento = :tpd ");
			sb.append("and o.processoDocumento.ativo = true ");
			sb.append("and exists(select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa ");
			sb.append("           where pdpa.processoDocumentoBin = o.processoDocumento.processoDocumentoBin) ");
			
			Query q = getEntityManager().createQuery(sb.toString());
			SessaoPautaProcessoTrf sessaoPPT = SessaoPautaProcessoTrfHome.instance().getInstance();
			q.setParameter("sessao", sessaoPPT.getSessao().getIdSessao());
			q.setParameter("oJVencedor", sessaoPPT.getOrgaoJulgadorVencedor().getIdOrgaoJulgador());
			q.setParameter("idProcesso", processo.getIdProcesso());
			q.setParameter("tpd", ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
			
			for (ProcessoDocumento ementa : (List<ProcessoDocumento>) q.getResultList()) {
				if (ementa != null
						&& ComponentUtil.getSessaoProcessoDocumentoManager().documentoInclusoAposProcessoJulgado(ementa, sessaoPPT.getProcessoTrf().getProcesso())) {
					ementaAssinada = ementa;
					break;
				}
			}
		}
		return ementaAssinada;
	}

	/**
	 * Retorna o valor da Proclamação da decisão de um processo.
	 * @return
	 */	
	public String getProclamacaoDecisao() {
		String proclamacao = "";
		if(getSessao() != null){
			Processo processo = recuperaProcessoAtivo();
			if(processo != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("select o.proclamacaoDecisao from SessaoPautaProcessoTrf o ");
				sb.append("where o.sessao.idSessao = :sessao ");
				sb.append("and o.processoTrf.idProcessoTrf = :idProcesso ");
				sb.append("and o.dataExclusaoProcessoTrf is null");
				
				Query q = getEntityManager().createQuery(sb.toString());
				q.setParameter("sessao", getSessao().getIdSessao());
				q.setParameter("idProcesso", processo.getIdProcesso());
				
				Object resultado = EntityUtil.getSingleResult(q);
				if(resultado != null) {
					proclamacao = resultado.toString();
				}
			}
		}
		return proclamacao;
	}

    /**
     * Criada na solicitação [PJEII-4330]
     * Método criado para não interferir na funcionalidade Acórdão do Menu
     * 
     * @return Conteúdo do Acórdão
     */
	public String getConteudoEditorAcordao() {
        return getConteudoEditorAcordao(false);
    }

	/**
	 * Pega o conteudo da ementa e da proclamação da decisão.
	 * 
	 * @return
	 */

	private String getConteudoEditorAcordao(boolean isFrame) {
        if (!isFrame && conteudoEditorAcordao == null || conteudoEditorAcordao.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            ementa = getEmentaElaboradaAssinada();

            if (ementa != null) {
                sb.append(ementa.getProcessoDocumentoBin().getModeloDocumento());
            }
            if ((getProclamacaoDecisao() != null && !Strings.isEmpty(getProclamacaoDecisao()))
                    || getEmentaElaboradaAssinada() != null) {
                adicionaSeparador(sb);
                sb.append(getProclamacaoDecisao());
            }
            setConteudoEditorAcordao(sb.toString());
        }
		ProcessoDocumento acordao = getAcordao();
		if (acordao != null) {
			acordao.getProcessoDocumentoBin().setModeloDocumento(conteudoEditorAcordao);
			getEntityManager().merge(acordao);
			getEntityManager().flush();
		}

		return conteudoEditorAcordao;
	}


    /**
     * Método utilizado pelo frame elaborarAcordao.xhtml
     * @return Conteudo do Acórdão
     */
	public String getConteudoEditorAcordaoFrame() {
        return getConteudoEditorAcordao(true);
	}

	private ProcessoDocumento getAcordao() {
        return ComponentUtil.getSessaoProcessoDocumentoManager().getProcessoDocumentoBySessaoTipoProcesso(
                getSessao(), ParametroUtil.instance().getTipoProcessoDocumentoAcordao(), 
                recuperaProcessoAtivo());
	}

	public void setConteudoEditorAcordao(String conteudoEditorAcordao) {
		this.conteudoEditorAcordao = conteudoEditorAcordao;
	}

	public void setListaProcesso(List<SessaoPautaProcessoTrf> listaProcesso) {
		this.listaProcesso = listaProcesso;
	}

	public List<SessaoPautaProcessoTrf> getListaProcesso() {
		return listaProcesso;
	}

	public void setCheckAll(Boolean checkAll) {
		this.checkAll = checkAll;
	}

	public Boolean getCheckAll() {
		return checkAll;
	}

	public void setTextoCertidao(String textoCertidao) {
		this.textoCertidao = textoCertidao;
	}

	public String getTextoCertidao() {
		return textoCertidao;
	}
	
	public List<PessoaMagistrado> getPresentes(SessaoPautaProcessoTrf julgamento){
		List<PessoaMagistrado> presentes = ComponentUtil.getComponent(SessaoJulgamentoService.class).getPresentes(julgamento, false);
		Collections.sort(presentes, new Comparator<PessoaMagistrado>() {
			@Override
			public int compare(PessoaMagistrado p1, PessoaMagistrado p2) {
				return p1.getNome().compareTo(p2.getNome());
			}
		});
		return presentes;
	}
	
	public String getMagistrados() {
		String magistrados = "";
		if(getSessao() != null){
			for (SessaoComposicaoOrdem sco : getSessao().getSessaoComposicaoOrdemList()) {
				String nomeMagistradoPresenteSessao = sco.getMagistradoPresenteSessao().getNome();;
				if (sco.getMagistradoSubstitutoSessao() != null) {
					nomeMagistradoPresenteSessao = sco.getMagistradoSubstitutoSessao().getNome();
				}			
				if (!nomeMagistradoPresenteSessao.equalsIgnoreCase(ProcessoTrfHome.instance().getRelator(getSessaoPautaProcessoTrf().getProcessoTrf()).getNome())) {
					if (magistrados.isEmpty()) {
						magistrados = nomeMagistradoPresenteSessao;
					} else {
						magistrados += ", " + nomeMagistradoPresenteSessao;
					}						
				}				
			}
		}else{
			magistrados = NAO_ENCONTRADO;
		}
		return magistrados;
	}

	public void setDocumentosAssinar(List<ProcessoDocumento> documentosAssinar) {
		this.documentosAssinar = documentosAssinar;
	}

	public List<ProcessoDocumento> getDocumentosAssinar() {
		return documentosAssinar;
	}

	public void setAssinado(Boolean assinado) {
		this.assinado = assinado;
	}

	public Boolean getAssinado() {
		return assinado;
	}

	public Boolean getAcordaoAssinado() {
		return acordaoAssinado;
	}

	public void setCarregarRelatorio(boolean carregarRelatorio) {
		this.carregarRelatorio = carregarRelatorio;
	}

	public boolean isCarregarRelatorio() {
		return carregarRelatorio;
	}

	public void setRelatorioMagistradoAssinado(boolean relatorioMagistradoAssinado) {
		this.relatorioMagistradoAssinado = relatorioMagistradoAssinado;
	}

	public boolean getRelatorioMagistradoAssinado() {
		return relatorioMagistradoAssinado;
	}

	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
	}

	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}

	public void setTipoStatusRelatorio(RelatorioStatusEnum tipoStatusRelatorio) {
		this.tipoStatusRelatorio = tipoStatusRelatorio;
	}

	public RelatorioStatusEnum getTipoStatusRelatorio() {
		return tipoStatusRelatorio;
	}

	public Boolean getDivRelatorio() {
		return divRelatorio;
	}

	public void setDivRelatorio(Boolean divRelatorio) {
		this.divRelatorio = divRelatorio;
	}

	public Boolean getDivVoto() {
		return divVoto;
	}

	public void setDivVoto(Boolean divVoto) {
		this.divVoto = divVoto;
	}

	public Boolean getDivEmenta() {
		return divEmenta;
	}

	public void setDivEmenta(Boolean divEmenta) {
		this.divEmenta = divEmenta;
	}

	public boolean isEmentaAssinada() {
		return ementaAssinada;
	}

	public void setEmentaAssinada(boolean ementaAssinada) {
		this.ementaAssinada = ementaAssinada;
	}

	public SessaoPautaProcessoTrf getSessaoPauta() {
		return sessaoPauta;
	}

	public void setSessaoPauta(SessaoPautaProcessoTrf sessaoPauta) {
		this.sessaoPauta = sessaoPauta;
	}

	public SessaoPautaProcessoTrf getSessaoPautaRelatorio() {
		return sessaoPautaRelatorio;
	}

	public void setSessaoPautaRelatorio(SessaoPautaProcessoTrf sessaoPautaRelatorio) {
		this.sessaoPautaRelatorio = sessaoPautaRelatorio;
	}

	public SessaoProcessoDocumento getDocEmenta() {
		return docEmenta;
	}

	public void setDocEmenta(SessaoProcessoDocumento docEmenta) {
		this.docEmenta = docEmenta;
	}

	public void setAcordaoAssinado(Boolean acordaoAssinado) {
		this.acordaoAssinado = acordaoAssinado;
	}

	public boolean getExibeModalConfirmacao() {
		return exibeModalConfirmacao;
	}

	public void setExibeModalConfirmacao(boolean exibeModalConfirmacao) {
		this.exibeModalConfirmacao = exibeModalConfirmacao;
	}

	public void setLiberarConsultaPublica(boolean liberarConsultaPublica) {
		this.liberarConsultaPublica = liberarConsultaPublica;
	}

	public boolean getLiberarConsultaPublica() {
		return liberarConsultaPublica;
	}

	public void limparDocumentosAssinar(){
		setDocumentosAssinar(new ArrayList<ProcessoDocumento>());
		ProcessoDocumentoBinPessoaAssinaturaHome.instance().limpar();
		listaProcesso.clear();
		documentoEmEdicao = false;
	}

    /**
     * @return the sessao
     */
    public Sessao getSessao() {
        return sessao;
    }

    /**
     * @param sessao the sessao to set
     */
    public void setSessao(Sessao sessao) {
        this.sessao = sessao;
    }

	/**
	 * Retorna o nome do procurador da sessão de acordo como foi definido no
	 * método
	 * "br.com.infox.cliente.home.SessaoHome.atualizaProcuradorSessao(int, boolean)".
	 * Buscando o nome do atributo 'procurador' ou do atributo
	 * 'PessoaProcurador' dependendo de como foi definido no metodo citado
	 * acima.
	 * 
	 * @author eduardo.pereira@tse.jus.br
	 * @return String com o nome do procurador
	 * @see br.com.infox.cliente.home.SessaoHome.atualizaProcuradorSessao(int,boolean)
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-20249">PJEII-20249</a>
	 */
	public String getProcurador() {
		String nomeProcurador = StringUtils.EMPTY;
		if (getSessao() != null) {
			nomeProcurador = getSessao().getProcurador();
			if (StringUtils.isBlank(nomeProcurador) && getSessao().getPessoaProcurador() != null) {
				nomeProcurador = getSessao().getPessoaProcurador().getNome();
			}
		}
		return nomeProcurador;
	}

	
	public String getPresidente() {
		String nomePresidente = NAO_ENCONTRADO;
		
		if(getSessao() != null){
			for (SessaoComposicaoOrdem sco : getSessao().getSessaoComposicaoOrdemList()) {			
				if (sco.getPresidente()) {				
					if (sco.getMagistradoSubstitutoSessao() != null) {					
						nomePresidente = sco.getMagistradoSubstitutoSessao().getNome();
					}else{
						nomePresidente = sco.getMagistradoPresenteSessao().getNome();
					}
				}							
			}
		}
		return nomePresidente;
	}

	
	public String getDataSessao() {
		if (getSessao() != null && getSessao().getDataSessao() != null) {
			return (new SimpleDateFormat("dd/MM/yyyy")).format(getSessao().getDataSessao());
		}
		return "";
	}	
	
	/**
	 * Método que será utilizado exclusivamente no xhtml sessaoPopUp evitando o uso do updateEmenta 
	 * e updateVotoMagistrado (este último estava sendo utilizado para atualizar relatório)
	 * Utilizado apenas no botão gravar
	 */
	public void atualizarRelatorioEmenta(){
		boolean erro = false;
		if (getInstance().getProcessoDocumento()==null || !(divEmenta ^ divRelatorio)){
			erro=true;
		}
		if (!erro){
			try {
				if (StringUtil.isEmpty(this.modeloDocumento)){
					FacesMessages.instance().add(Severity.ERROR, "Erro ao atualizar o documento. O documento não pode estar vazio");
					return;
				}
				getInstance().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
				ProcessoDocumentoHome.instance().update();
				super.update();
				FacesMessages.instance().clear();
				if (divEmenta){
					FacesMessages.instance().add(Severity.INFO, "Ementa alterada com sucesso.");
					return;
				}else if (divRelatorio){
					FacesMessages.instance().add(Severity.INFO, "Relatório alterado com sucesso.");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				erro = true;
			}
			
		}		
		if (erro){
			FacesMessages.instance().add(Severity.ERROR, "Erro ao efetuar a operação, favor atualizar a página e tentar novamente");
			return;
		}
	}
	
	private void cancelarLiberacoes(){
		if (divRelatorio)
			getInstance().setLiberacao(Boolean.FALSE);
		if (divEmenta)
			getDocEmenta().setLiberacao(Boolean.FALSE);
		
	}
	
	public boolean isAssinado(SessaoProcessoDocumento certidao) {
		if (certidao != null) {
			return ComponentUtil.getProcessoDocumentoBinPessoaAssinaturaManager().getUltimaAssinaturaDocumento(
					certidao.getProcessoDocumento().getProcessoDocumentoBin()) != null;			
		}
		return false;
	}
	

	/**
	 * metodo para alterar o processoDocumento da lista documentosAssinar para documentosDosProcessosNaSessao e vice versa
	 * @param documento
	 */
	public void adicionaOuRemoveDocumentoDaListaDocumentosAssinar(SessaoProcessoDocumento documento) {
		ProcessoDocumento pd = documento.getProcessoDocumento();
 
		if(documentosAssinar.contains(pd)) {
			documentosAssinar.remove(pd);
			totalDocumentosRetiradosDaListaParaAssinar++;
			this.checkAllAssinatura = false;
		} else {
			documentosAssinar.add(pd);
			totalDocumentosRetiradosDaListaParaAssinar--;
			if(totalDocumentosRetiradosDaListaParaAssinar == 0) {
				this.checkAllAssinatura = true;
			}
		}
	}
	
	/**
	 * Metodo responsavel por, ao clicar no pesquisar/limpar na emissao da certidao dos filtros 
	 * ele adiciona/remove os itens da lista de documentosDosProcessosNaSessao para assinar
	 */
	public void adicionaOuRemoveTodosDocumentosParaAssinar(boolean marcaTodos) {
		this.checkAllAssinatura = marcaTodos;
		
		if (marcaTodos) {
			ProcessoJulgadoList processoJulgadoList = getComponent(ProcessoJulgadoList.NAME);
			for (SessaoPautaProcessoTrf sessaoPautaProcessoTrf : processoJulgadoList.list()) {
				SessaoProcessoDocumento sessaoProcessoDocumento = getDocumento(sessaoPautaProcessoTrf);
				if (sessaoProcessoDocumento != null && !isAssinado(sessaoProcessoDocumento)) {
					documentosAssinar.add(sessaoProcessoDocumento.getProcessoDocumento());
				}
			}
			totalDocumentosRetiradosDaListaParaAssinar = 0;
		} else {
			totalDocumentosRetiradosDaListaParaAssinar = documentosAssinar.size();
			documentosAssinar.clear();
		}
	}
	
	/**
	 * Método responsável por montar o link de acesso aos detalhes do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @return Link de acesso aos detalhes do processo.
	 */
	public String montarLinkProcessoVisualizacao(Integer idProcessoTrf) {
		String chave = SecurityTokenControler.instance().gerarChaveAcessoProcesso(idProcessoTrf);
		return UrlUtil.montarLinkDetalheProcesso(Constantes.URL_DETALHE_PROCESSO.PROCESSO_VISUALIZACAO, idProcessoTrf, chave);
	}
	
	/**
	 * Metodo responsavel por montar o selectItem com a ordem dos processos na sessao que será mostrado na tela.
	 * @return List<Integer>
	 */
	public List<Integer> getOrdemProcessoSessaoSelectItem() {
		return ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarOrdemProcessoSessaoSelectItem(SessaoHome.instance().getInstance());
	}
	
	public boolean getCheckAllAssinatura() {
		return checkAllAssinatura;
	}

	public boolean isDocumentoEmEdicao() {
		return documentoEmEdicao;
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public void setProcessoJudicial(ProcessoTrf processoJudicial) {
		this.processoJudicial = processoJudicial;
	}
}
