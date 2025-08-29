/**
 * pje-web
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.com.jt.pje.manager.VotoManager;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.NotaSessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoLidoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoMultDocsVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.ComposicaoJulgamentoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.SituacaoDocumentoSessao;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.cnj.pje.view.fluxo.VotacaoVogalAction;
import br.jus.cnj.pje.visao.beans.VotacaoBean;
import br.jus.cnj.pje.visao.beans.VotacaoBean.ObjetoVoto;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle da tela do magistrado na sessão, correspondente ao
 * xhtml /Painel/painel_usuario/sessao.xhtml.
 * 
 * @author cristof
 * 
 */
@Name(PainelMagistradoSessaoAction.NAME)
public class PainelMagistradoSessaoAction implements ArquivoAssinadoUploader{

	public static final String NAME = "painelMagistradoSessaoAction";
	
	@RequestParameter("idsess_")
	private Integer idSessao;

	@RequestParameter("idjulg_")
	private Integer idJulgamento;

	@RequestParameter("idoja_")
	private Integer idOrgaoAcompanhado;
	
	@RequestParameter("iddoc_")
	private Integer idDocumento;

	@RequestParameter("cnt_")
	private Long count;
	
	@RequestParameter("oldrow_")
	private Integer oldRow;
	
	@RequestParameter("mostrarvoto_")
	private Boolean mostrarvoto_;
	
	@RequestParameter("anots_")
	private Boolean anotacoes_;

	@RequestParameter("iframe")
	private Boolean iframe;
	
	@RequestParameter("tab_")
	private String tab;
	
	private int ncolor = 0;
	
	private static final String[] colors = {
		 "#1C75AA", "#AA0D12", "#501193", "#63B8FF", "#CD6090",
		 "#B03060", "#09AA0F", "#B22222", "#CD853F", "#BC8F8F",
		 "#CD5C5C", "#21C3C7", "#0DDA41", "#B3EE3A", "#C9C667",
		 "#FFA500", "#8B5A00", "#FF7256", "#8B3626", "#94200F",
		 "#EDC11C", "#AB82FF", "#715C0D", "#FF9066", "#15B79D",
		 "#90B715", "#B72573", "#C72785", "#E35B98", "#16C159",
		 "#5D750D", "#8B864E"};
	
	@In
	private SessaoProcessoMultDocsVotoManager sessaoProcessoMultDocsVotoManager;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;
	
	@In
	private FacesContext facesContext;

	@In
	private FacesMessages facesMessages;
	
	@In
	private Identity identity;
	
	@In
	private NotaSessaoJulgamentoManager notaSessaoJulgamentoManager;
	
	@In
	private PrioridadeProcessoManager prioridadeProcessoManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;

	@In
	private SessaoJulgamentoManager sessaoJulgamentoManager;

	@In
	private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
	
	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private TipoVotoManager tipoVotoManager;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ParametroService parametroService;
	
	@In
	private VotoManager votoManager;
	
	@In
	private DerrubadaVotoManager derrubadaVotoManager;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
 	
 	@In
 	private ProcessoDocumentoLidoManager processoDocumentoLidoManager;
 	
	@In
	private ComposicaoJulgamentoService composicaoJulgamentoService;
	
	@In
	private ProcessoMagistradoManager processoMagistradoManager;
	
 	
 	
	@Logger
	private Log logger;
	
	private Integer idjulg_;
	
	// Campos de filtragem //
	
	private Integer numeroOrdem;
	
	private String numeroProcesso;
	
	private String campoAssunto;
	
	private String campoClasse;
	
	private PrioridadeProcesso prioridade;
	
	private List<PrioridadeProcesso> prioridades;
	
	private Date dataInicialDistribuicao;
	
	private Date dataFinalDistribuicao;
	
	private String nomeParte;
	
	private String codigoIMF;
	
	private String codigoOAB;
	
	private OrgaoJulgador orgaoFiltro;
	
	private TipoVoto tipoVotoFiltro;

	private boolean mostrarVoto = false;
	
	private boolean mostrarAnotacoes = false;
	
	// Fim dos campos de filtragem //
	
	private OrgaoJulgador orgaoAtual;

	private Sessao sessao;
	
	private Map<Integer, VotacaoBean> votacao;

	private Map<Integer, Map<Integer, String>> colorsMap;
	
	private Map<Integer, String> nomeOrgao;

	/**
	 * Encerra os julgamentos agendados e filtrados
	 */
	private EntityDataModel<SessaoPautaProcessoTrf> processos;
	
	private List<SessaoPautaProcessoTrf> processosJulgamento;
	
	private Map<String, SessaoProcessoDocumento> elementosJulgamento;
	
	private Map<String, SessaoProcessoDocumento> elementosJulgamento_;
	
	private Set<Integer> linhas;
	
	private List<NotaSessaoJulgamento> anotacoes;
	
	private boolean redigir = false;
	
	private String anotacao;
	
	private SessaoProcessoDocumentoVoto voto;
	
	private List<TipoProcessoDocumento> tiposProcessoDocumento = new ArrayList<TipoProcessoDocumento>(0);
	
	private List<ModeloDocumento> modelosDocumento = new ArrayList<ModeloDocumento>(0);
	
	private ModeloDocumento modeloDocumento;
	
	private ArquivoAssinadoHash arquivoAssinado;

	/**
	 * Objeto responsável por recuperar os dados eventualmente paginados da tela.
	 * 
	 * @author cristof
	 *
	 */
	private class Pautados implements DataRetriever<SessaoPautaProcessoTrf> {

		private Long count;

		private SessaoPautaProcessoTrfManager manager;

		private Sessao sessao;
		
		private Boolean removerEmJulgamento;
		
		public Pautados(Sessao sessao, SessaoPautaProcessoTrfManager manager, Long count,Boolean removerEmJulgamento) {
			this.manager = manager;
			this.count = count;
			this.sessao = sessao;
			this.removerEmJulgamento = removerEmJulgamento;
			
		}

		@Override
		public Integer getId(SessaoPautaProcessoTrf julg) {
			return julg.getIdSessaoPautaProcessoTrf();
		}

		@Override
		public SessaoPautaProcessoTrf findById(Object id) throws Exception {
			if (id == null || !Number.class.isAssignableFrom(id.getClass())) {
				return null;
			} else {
				return manager.findById(((Number) id).intValue());
			}
		}

		@Override
		public List<SessaoPautaProcessoTrf> list(Search search) {
			if (sessao == null) {
				return Collections.emptyList();
			}
			try {
				search.setDistinct(true);
				search.addCriteria(Criteria.equals("sessao", sessao));
				if(removerEmJulgamento){
					search.addCriteria(Criteria.not(Criteria.equals("situacaoJulgamento", TipoSituacaoPautaEnum.EJ)));
				}
				search.addCriteria(Criteria.isNull("dataExclusaoProcessoTrf"));
				search.addOrder("o.numeroOrdem", Order.ASC);
				return manager.list(search);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}

		@Override
		public long count(Search search) {
			if (this.count == null) {
				Integer max = search.getMax();
				search.setMax(0);
				list(search);
				search.setMax(max);
				this.count = manager.count(search);
			}
			return this.count;
		}
	}
	
 	/**
 	 * Metodo que verifica se  obrigatrio ou permitida a assinatura do documento.
 	 * - papel do usuário deve estar em uma exigibilidade de assinatura
 	 * - documento não deve estar juntado
 	 * 
 	 * @return boolean
 	 */
 	public boolean isPermiteAssinar(ProcessoDocumento processoDocumento){
 		Boolean retorno = Boolean.FALSE;
 		if (!tipoProcessoDocumentoPapelService.verificarExigibilidadeAssina(
 				Authenticator.getPapelAtual(),processoDocumento.getTipoProcessoDocumento())
 			&& !processoDocumento.isJuntado()){
 			retorno = Boolean.TRUE;
 		}
 		return retorno;
	}
	
	/**
	 * Metodo responsável por gravar uma copia do documento na tabela onde são armazenados os documentos para multdocs
	 * @param voto
	 * @param documento
	 */
	private void replicarDoc(SessaoProcessoDocumentoVoto voto,ProcessoDocumento documento) {
		if(!verificarDocEmSessaoProcMultDocs(documento)){
			gravarEmMultDocs(voto, documento);
		}
	}
	
	/**
	 * Metodo responsável por verificar se o documento já existe na tabela de multdocs
	 * @param voto
	 * @param documento
	 */
	private boolean verificarDocEmSessaoProcMultDocs(ProcessoDocumento proc) {
		SessaoProcessoMultDocsVoto docSessaMult = sessaoProcessoMultDocsVotoManager.recuperarSessaoProcessoDoc(proc);
		return docSessaMult != null;
	}
	
	/**
	 * Metodo responsável por gravar documento na tabela onde são armazenados os documentos para multdocs
	 * @param voto
	 * @param documento
	 */
	private void gravarEmMultDocs(SessaoProcessoDocumentoVoto voto, ProcessoDocumento documento){
		SessaoProcessoMultDocsVoto sessaoProcessoDocumentoMultDocs = new SessaoProcessoMultDocsVoto();
		sessaoProcessoDocumentoMultDocs.setProcessoDocumento(documento);
		sessaoProcessoDocumentoMultDocs.setSessaoProcessoDocumentoVoto(voto);
		sessaoProcessoDocumentoMultDocs.setOrdemDocumento(getProximoNumeroOrdemDoc(voto));
		try {
			sessaoProcessoMultDocsVotoManager.persist(sessaoProcessoDocumentoMultDocs);
			sessaoProcessoMultDocsVotoManager.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Houve um erro gravar o documento: {0}", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Metodo copiado da VotacaoVogalMultDocsAction
	 * @param voto
	 * @return Proximo Numero Ordem Doc
	 */
	private Integer getProximoNumeroOrdemDoc(SessaoProcessoDocumentoVoto voto) {
		return sessaoProcessoMultDocsVotoManager.recuperarProximoNumeroOrdemDoc(voto);
	}
	
	/**
	 * Retorna uma lista de tipos de documento.
	 * 
	 * @return Uma lista de tipos de documento.
	 */
	public List<TipoProcessoDocumento> getTiposProcessoDocumento() {
		if (tiposProcessoDocumento.isEmpty()) {
			List<TipoProcessoDocumento> listaTipoProcessoDocumentoTemp = tipoProcessoDocumentoManager.findByIds(ParametroUtil.instance().getIdsTipoDocumentoVoto());
			if (getJulgamento(idJulgamento).getProcesso().getOrgaoJulgador()!=getOrgaoAtual()){
				
				for (TipoProcessoDocumento tipoProcessoDocumento : listaTipoProcessoDocumentoTemp) {
					if (!tipoProcessoDocumento.getTipoProcessoDocumento().equals("Voto Relator")){
						tiposProcessoDocumento.add(tipoProcessoDocumento);
					}
				}
			}else{
				tiposProcessoDocumento.addAll(listaTipoProcessoDocumentoTemp);
			}
		}
		return tiposProcessoDocumento;
	}
	
	/**
	 * Inicializa o objeto
	 */
	@Create
	public void init() {
		try {
			if (idSessao != null) {
	 			sessao = sessaoJulgamentoManager.findById(idSessao);
				Pautados pautados = new Pautados(sessao, sessaoPautaProcessoTrfManager, count,sessao.getContinua());
				processos = new EntityDataModel<SessaoPautaProcessoTrf>(SessaoPautaProcessoTrf.class, facesContext, pautados);
				orgaoAtual = Authenticator.getOrgaoJulgadorAtual();
				votacao = new HashMap<Integer, VotacaoBean>();
				colorsMap = new HashMap<Integer, Map<Integer, String>>();
				linhas = new HashSet<Integer>();
				mostrarVoto = mostrarvoto_ == null ? false : mostrarvoto_;
				mostrarAnotacoes = anotacoes_ == null ? false : anotacoes_;
				nomeOrgao = new HashMap<Integer, String>();

				if(iframe != null && iframe){
					Contexts.getConversationContext().set("showTopoSistema",false);
				}
				else{
					Contexts.getConversationContext().set("showTopoSistema",true);
				}

			}
		} catch (PJeBusinessException e) {
			logger.error(Severity.FATAL, "Erro ao inicializar o controlador do painel do magistrado na sessão: {0}.", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public List<SessaoPautaProcessoTrf> getProcessosJulgamento() {
		if(processosJulgamento == null && sessao != null){
			try {
				Search s = new Search(SessaoPautaProcessoTrf.class);
				s.setDistinct(true);
				s.addCriteria(Criteria.equals("sessao", sessao));
				s.addCriteria(Criteria.equals("situacaoJulgamento", TipoSituacaoPautaEnum.EJ));
				s.addCriteria(Criteria.isNull("dataExclusaoProcessoTrf"));
				if(sessao.getContinua() != null && sessao.getContinua()){
					List<Criteria> crits = new ArrayList<Criteria>();
					addCriterias("processoTrf.", crits);
					s.addCriteria(crits);
				}

				s.addOrder("numeroOrdem", Order.ASC);
				processosJulgamento = sessaoPautaProcessoTrfManager.list(s); 
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				processosJulgamento = Collections.emptyList();
			}
		}
		return processosJulgamento;
	}
    
    public void acompanharRelator(boolean integral){
		if(idJulgamento != null){
			try {
				getJulgamento(idJulgamento).acompanharRelator(integral);
				facesMessages.add(Severity.INFO, "Acompanhamento registrado com sucesso.");
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao realizar comando de acompanhamento ao relator: {0}", e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Metodo responsável por incluir o tipo de voto não conhece ao processo.
	 * 
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-17870
	 */
	public void incluirTipoVotoNaoConhece(){
		if(idJulgamento != null){
			try {
				getJulgamento(idJulgamento).incluirTipoVotoNaoConhece();
				facesMessages.addFromResourceBundle(Severity.INFO,"pautaSessao.votoNaoConheceSucesso");
			} catch (PJeBusinessException e) {
				facesMessages.addFromResourceBundle(Severity.ERROR,"pautaSessao.votoNaoConheceErro");
				logger.error("Erro ao realizar comando de não conhece: {0}", e.getLocalizedMessage());
			}
		}
	}
	
	public void abrirDivergencia(){
		if(idJulgamento != null){
			try {
				getJulgamento(idJulgamento).abrirDivergencia();
				facesMessages.add(Severity.INFO, "Divergência aberta com sucesso.");
				return;
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao realizar comando de abertura de divergência: {0}", e.getLocalizedMessage());
			}
		}else{
			logger.error("Erro ao abrir a divergência: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao tentar abrir a divertência: identificador do julgamento não encontrado.");
		}
	}


	public void votoVogal(TipoVoto tipoVoto){
		if(idJulgamento != null){
			try {
				getJulgamento(idJulgamento).votar(tipoVoto);
				facesMessages.add(Severity.INFO, "Voto registrado com sucesso.");
				return;
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao registrar voto : {0}", e.getLocalizedMessage());
			}
		}else{
			logger.error("Erro ao abrir a divergência: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao tentar abrir a divertência: identificador do julgamento não encontrado.");
		}
	}
	
	public void acompanhar() {
		if (idJulgamento == null) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar acompanhar o voto: identificador do processo não encontrado.");
		}
		if (idOrgaoAcompanhado == null) {
			facesMessages.add(Severity.ERROR, "Não há órgão julgador a ser acompanhado!");
			return;
		}
		
		try {
			OrgaoJulgador ojAcompanhado = orgaoJulgadorManager.findById(idOrgaoAcompanhado);
			VotacaoBean votacao = getJulgamento(idJulgamento);
			
			if (votacao.getJulgamento().getParticipaVotacao(getOrgaoAtual())) {
				if (idOrgaoAcompanhado == votacao.getOrgaoRelator().getIdOrgaoJulgador()) {
					acompanharRelator(true);
				} else {
					try {
						votacao.acompanharDivergencia(ojAcompanhado);
						facesMessages.add(Severity.INFO, "Acompanhamento de divergência registrado.");
						idJulgamento = null;
					} catch (PJeBusinessException e) {
						facesMessages.add(Severity.ERROR, "Erro ao registrar o acompanhamento de divergência");
						logger.error("Erro ao registrar o acompanhamento de divergência: {0}",e.getLocalizedMessage());
					}
				}
			}
		} catch (PJeBusinessException e1) {
			facesMessages.add(Severity.ERROR, "Erro ao registrar o acompanhamento de divergência");
			e1.printStackTrace();
		}
		
	}
	
	public void acompanharDivergencia(ProcessoTrf processoTrf, OrgaoJulgador ojAcompanhado) {
		
		try {
			
			TipoVoto tipoVoto = tipoVotoManager.recuperaTipoDivergente();
			
			if (tipoVoto!=null){
			
				voto = new SessaoProcessoDocumentoVoto();
				voto.setSessao(sessao);
				voto.setCheckAcompanhaRelator(false);
				voto.setDestaqueSessao(false);
				voto.setImpedimentoSuspeicao(false);
				voto.setProcessoTrf(processoTrf);
				voto.setOrgaoJulgador(orgaoAtual);
				voto.setDtVoto(new Date());
				voto.setLiberacao(true);
				voto.setTipoVoto(tipoVoto);
				voto.setOjAcompanhado(ojAcompanhado);
				
				sessaoProcessoDocumentoManager.persistAndFlush(voto);
				
				if (derrubadaVotoManager != null) {
					derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(getVoto());
				}	
				
				carregarJulgamento(idJulgamento, true);
				
				facesMessages.add(Severity.INFO, "Acompanhamento de divergência registrado.");
			}
			else{
				facesMessages.add(Severity.ERROR, "Erro ao registrar o acompanhamento de divergência.");
				logger.error("Erro ao registrar o acompanhamento de divergência: processo não localizado.");
			}
			
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao registrar o acompanhamento de divergência");
			logger.error("Erro ao registrar o acompanhamento de divergência: {0}",e.getLocalizedMessage());
		}
	}

	
	public void inverterImpedimento(){
		if(idJulgamento != null){
			try {
				getJulgamento(idJulgamento).inverterImpedimento();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao declarar impedimento: {0}", e.getLocalizedMessage());
			}
		}else{
			logger.error("Erro declarar impedimento: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao declarar impedimento: identificador do julgamento não encontrado.");
		}
	}
	
	public void removerVoto() {
		if (idJulgamento != null) {
			try {
				SessaoPautaProcessoTrf processoPautadoSessao = sessaoPautaProcessoTrfManager.findById(idJulgamento);
				SessaoProcessoDocumentoVoto spdv = votoManager.getVotoProprio(processoPautadoSessao);
				if (spdv != null) {
					ProcessoDocumento pd = spdv.getProcessoDocumento();
					List<SessaoProcessoDocumentoVoto> votosAcompanhantes = sessaoProcessoDocumentoVotoManager
							.getVotosAcompanhantes(spdv, orgaoAtual);
					for (SessaoProcessoDocumentoVoto vot : votosAcompanhantes) {
						vot.setOjAcompanhado(vot.getOrgaoJulgador());
						sessaoProcessoDocumentoVotoManager.persist(vot);
					}
					derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(spdv);
					sessaoProcessoDocumentoVotoManager.remove(spdv);
					if (pd != null) {
						ProcessoDocumentoBinManager.instance().remove(pd.getProcessoDocumentoBin());
						ProcessoDocumentoManager.instance().remove(pd);
					}
					sessaoProcessoDocumentoVotoManager.flush();
					facesMessages.add(Severity.INFO, "Voto removido com sucesso.");
				}
				carregarJulgamento(idJulgamento, true);
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao remover voto: {0}", e.getLocalizedMessage());
			}
		} else {
			logger.error("Erro ao remover voto: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao remover voto: identificador do julgamento não encontrado.");
		}
	}

	public void redigirVoto(){
		if(idJulgamento != null){
			redigir = true;
			try{
				getJulgamento(idJulgamento).redigirVoto();
			}
			catch(PJeBusinessException e){
				logger.error("Erro ao persistir documento.");
				facesMessages.add(Severity.ERROR, "Erro ao persistir documento.");
			}
		}else{
			logger.error("Erro ao iniciar edição de voto: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao iniciar edição de voto: identificador do julgamento não encontrado.");
		}
	}
		
	/**
	 * Faz a validação se o Órgão Julgador que está realizando o voto está
	 * marcado como votante na composição da sessão de julgamento.
	 * 
	 * Caso ainda não exista sessão definida então deixa o usuário registrar o
	 * voto.
	 * 
	 * @see VotacaoVogalAction#recuperarProcessoPautadoSessao(ProcessoTrf, Sessao)
	 * @see SessaoPautaProcessoTrf#getParticipaVotacao(OrgaoJulgador)
	 * 
	 * @param voto {@link SessaoProcessoDocumentoVoto} voto que está sendo realizado
	 * 
	 * @return <code>true</code> nos casos em que o processo não está pautado em
	 *         uma sessão de julgamento ou quando está pautado em uma sessão é o
	 *         Órgão Julgador está marcado como votante na sessão.
	 * 
	 *         <code>false</code> quando o processo está pautado em uma sessão
	 *         de julgamento e o órgão julgador não faz parte da composição ou o
	 *         órgão faz parte da sessão e o mesmo não está marcado como votante
	 *         na sessão
	 * 
	 */
	private boolean isOrgaoVotanteNaSessao(OrgaoJulgador orgaoJulgador,
			Sessao sessaoJulgamento) {
		
		boolean retorno = true;
		
		if (orgaoJulgador == null) {
			retorno = false;
			
		} else if (sessaoJulgamento != null) {
			SessaoPautaProcessoTrf processoPautado = recuperarProcessoPautadoSessao(
					voto.getProcessoTrf(), sessaoJulgamento);
						
			if (processoPautado != null) {
				retorno = processoPautado.getParticipaVotacao(orgaoJulgador);	
			}
			
		}
		
		return retorno;
	}
	
	/**
	 * Recupera da sessão de julgamento a pauta que contém o processo.
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} a ser pesquisado na sessão de julgamento
	 * 
	 * @param sessaoJulgamento
	 *            {@link Sessao} sessão de julgamento que contém a lista de de
	 *            processos pautados
	 * 
	 * @return a composição de pauta processo {@link SessaoPautaProcessoTrf} que
	 *         representa o processo pautado na sessão de julgamento
	 */
	private SessaoPautaProcessoTrf recuperarProcessoPautadoSessao(
			ProcessoTrf processo, Sessao sessaoJulgamento) {
		
		SessaoPautaProcessoTrf processoPautadoRetorno = null;
		if (sessaoJulgamento != null && processo != null) {
			List<SessaoPautaProcessoTrf> processosPautados = sessaoJulgamento
					.getSessaoPautaProcessoTrfList();
			
			if (processosPautados != null) {
				for (SessaoPautaProcessoTrf processoPautado : processosPautados) {
					if (processoPautado.getDataExclusaoProcessoTrf() == null && processoPautado.getProcessoTrf().equals(processo)) {
						processoPautadoRetorno = processoPautado;
					}
				}			
			}
		}
		
		return processoPautadoRetorno;
	}
	
	/** 
	 * Metodo que grava documentos do tipo relatório e ementa do relator
	 * {@link https://www.cnj.jus.br/jira/browse/PJEII-17870}
	 */
	public void gravarDoc(){
		if(idJulgamento != null && idDocumento != null && idDocumento > 0){
			for(SessaoProcessoDocumento spd: getElementosJulgamento().values()){
				if(spd.getProcessoDocumento() != null
						&& spd.getProcessoDocumento().getIdProcessoDocumento() == idDocumento.intValue()
						&& spd.getProcessoDocumento().getProcessoDocumentoBin() != null
						&& spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() != null 
						&& !spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento().isEmpty()){
					try {
						SessaoProcessoDocumentoVoto voto = null;
						
						if(spd instanceof SessaoProcessoDocumentoVoto){
                             voto = (SessaoProcessoDocumentoVoto)spd;
                             voto.setDtVoto(new Date());
                             spd.getProcessoDocumento().setExclusivoAtividadeEspecifica(Boolean.TRUE);
                        }
						documentoJudicialService.persist(spd.getProcessoDocumento(), true);
						documentoJudicialService.flush();
						
						if (voto != null){
							derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(voto);
						}	
						
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
				}
			}
			facesMessages.add(Severity.INFO, "Gravação bem sucedida.");
		}else{
			logger.error("Erro ao iniciar edição de voto: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao iniciar edição de voto: identificador do julgamento não encontrado.");
		}
	}
	
	
	/**
	 * Realiza a gravação do voto.
	 * 
	 * @return <code>true</code> caso a gravação tenha sido bem sucedida ou
	 *         <code>false</code> caso algum erro tenha ocorrido.
	 */
	public boolean gravarVoto(){
		try {
			AjaxDataUtil ajaxDataUtil = ComponentUtil.getComponent(AjaxDataUtil.NAME, ScopeType.EVENT);
			
			recuperaVoto();
			
			if (!isOrgaoVotanteNaSessao(voto.getOrgaoJulgador(), sessao)) {
				facesMessages.addFromResourceBundle(Severity.ERROR, "votacaoVogal.votoNaoRegistrado.orgaoJulgadorNaoVotante");
				ajaxDataUtil.erro();
				return false;
			}
			sessaoProcessoDocumentoManager.persist(voto);
			sessaoProcessoDocumentoManager.flush();
			
			//Realiza replicação do voto em multdocs
			replicarDoc(voto, voto.getProcessoDocumento());
			
			if (voto != null){
				derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(voto);
			}	
			
			facesMessages.add(Severity.INFO, "Voto registrado com sucesso.");
			ajaxDataUtil.sucesso();
			return true;
			
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao tentar gravar o voto: {0}.", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
			return false;
		}
	}
	
	/** 
	 * Metodo que grava voto e assina em sequencia (copiado da VotacaoVogalMultDocs)
	 */
	public void finalizarAssinatura(){
		try {
			if (gravarVoto()) {
				trataAssinaturaPorPjeOffice();
				documentoJudicialService.finalizaDocumento(voto.getProcessoDocumento(), voto.getProcessoTrf(), null, false, true, false, Authenticator.getPessoaLogada(), false);
				documentoJudicialService.flush();
				facesMessages.add(Severity.INFO, "Assinatura bem sucedida.");
			}			
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Houve um erro ao finalizar o documento: {0}", e.getLocalizedMessage());
		}		
	}
	
	public boolean podeAlterarVoto(SessaoProcessoDocumento sessaoProcessoDocumento, SessaoPautaProcessoTrf sessaoPautaProcessoTrf){
		Boolean retorno = true;
		if (sessaoProcessoDocumento != null) {
			ProcessoDocumentoBin processoDocumentoBin = sessaoProcessoDocumento.getProcessoDocumento().getProcessoDocumentoBin();
			if(processoDocumentoBin != null && CollectionUtilsPje.isNotEmpty(processoDocumentoBin.getSignatarios()) 
					&& !TipoSituacaoPautaEnum.JG.equals(sessaoPautaProcessoTrf.getSituacaoJulgamento())) {
				
				retorno = false;
			}
		}
		return retorno;
	}
	
	/** 
	 * Metodo que remove assinatura do voto (copiado da VotacaoVogalMultDocs)
	 */
	public void removerAssinaturaVoto(ProcessoDocumento processoDocumento){
		if(processoDocumento != null){
			this.assinaturaDocumentoService.removeAssinatura(processoDocumento);
			facesMessages.add(Severity.INFO, "Assinatura removida com sucesso.");
		}
	}
	
	private void trataAssinaturaPorPjeOffice(){
		arquivoAssinado = (ArquivoAssinadoHash)Contexts.getSessionContext().get("votoArquivoAssinadoHash");
		Contexts.getSessionContext().remove("votoArquivoAssinadoHash");
		if(arquivoAssinado != null 
				&& voto !=null 
				&& voto.getProcessoDocumento() != null 
				&& voto.getProcessoDocumento().getProcessoDocumentoBin() != null){
			voto.getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
			voto.getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
		}		
	}
	
	/** 
	 * Metodo que recupera voto criado no componente da tela
	 */
	private void recuperaVoto(){
		if(idJulgamento != null && idDocumento != null && idDocumento > 0){
			for(SessaoProcessoDocumento spd: getElementosJulgamento().values()){
				if(spd.getProcessoDocumento() != null
						&& spd.getProcessoDocumento().getIdProcessoDocumento() == idDocumento.intValue()
						&& spd.getProcessoDocumento().getProcessoDocumentoBin() != null
						&& spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() != null 
						&& !spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento().isEmpty()){
					
					SessaoProcessoDocumentoVoto voto = null;
					if(spd instanceof SessaoProcessoDocumentoVoto){
						spd.getProcessoDocumento().setProcessoDocumento(spd.getProcessoDocumento().getTipoProcessoDocumento().toString()); 
						voto = (SessaoProcessoDocumentoVoto)spd;
                        voto.setDtVoto(new Date());
                        voto.setProcessoDocumento(spd.getProcessoDocumento());
                        setVoto(voto);
                    }
						
				}
			}
		}else{
			logger.error("Erro ao recuperar voto: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao recuperar voto.");
		}
	}

	public void gravarAnotacao(){
		if(idJulgamento != null && idJulgamento > 0 && anotacao != null && !anotacao.isEmpty()){
			try {
				SessaoPautaProcessoTrf spt = sessaoPautaProcessoTrfManager.findById(idJulgamento);
				NotaSessaoJulgamento nota = new NotaSessaoJulgamento();
				nota.setAtivo(true);
				nota.setDataCadastro(new Date());
				nota.setNotaSessaoJulgamento(anotacao);
				nota.setOrgaoJulgador(orgaoAtual);
				nota.setProcessoTrf(spt.getProcessoTrf());
				nota.setSessao(spt.getSessao());
				nota.setUsuarioCadastro(Authenticator.getUsuarioLogado());
				notaSessaoJulgamentoManager.persistAndFlush(nota);
				anotacao = "";
				facesMessages.add(Severity.INFO, "Anotação incluída com sucesso.");
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Não foi possível gravar a anotação.");
				e.printStackTrace();
			}
		}else{
			facesMessages.add(Severity.ERROR, "Não foi possível gravar a anotação.");
		}
	}
	
	/**
	 * Verifica se o voto do relator afeta o votos dos vogais
	 * 
	 * @param idJulg id sessao pauta processo trf
	 * @return String com a mensagem referente a alteracao ou nao do voto
	 */
	public String verificaValidadeVotoRelator(SessaoPautaProcessoTrf processoPautadoSessao) {
		return derrubadaVotoManager.verificaValidadeVotoRelator(processoPautadoSessao);
	}

	public String getNomeOrgao(Integer id){
		if (id>0){
			if(nomeOrgao.get(id) == null){
				try {
					nomeOrgao.put(id, orgaoJulgadorManager.findById(id).getOrgaoJulgador());
				} catch (PJeBusinessException e) {
					logger.error("Erro ao tentar recuperar o nome do órgão.");
				}
			}
			return nomeOrgao.get(id);
		}
		return "";
	}
	
	public void exibirDecisao(){
		facesMessages.add(Severity.INFO, "Minha mensagem");
	}
	
	/**
	 * Verifica se o relator do julgamento do processo já proferiu voto.
	 * 
	 * @param processoPautadoSessao {@link SessaoPautaProcessoTrf}.
	 * @return Verdadeiro, se o relator proferiu o voto. Falso, caso contrário.
	 */
	public boolean isRelatorVotou(SessaoPautaProcessoTrf processoPautadoSessao){
		return getVotoRelator(processoPautadoSessao) != null;
	}
	
	public Map<String, SessaoProcessoDocumento> getElementosJulgamento(){
		if(idJulgamento != null && idOrgaoAcompanhado != null && !idJulgamento.equals(idjulg_)){
			idjulg_ = idJulgamento;
			elementosJulgamento_ = new HashMap<String, SessaoProcessoDocumento>();
			for(String k: getElementosJulgamento_().keySet()){
				SessaoProcessoDocumento spd = getElementosJulgamento_().get(k); 
				if(spd.getOrgaoJulgador().getIdOrgaoJulgador() == idOrgaoAcompanhado 
						|| (SessaoProcessoDocumentoVoto.class.isAssignableFrom(spd.getClass())
								&& ((SessaoProcessoDocumentoVoto) spd).getOjAcompanhado().getIdOrgaoJulgador() == idOrgaoAcompanhado
								&& !((SessaoProcessoDocumentoVoto) spd).getImpedimentoSuspeicao())){
					
					if (spd.getProcessoDocumento()==null || spd.getProcessoDocumento().getIdProcessoDocumento()==0){
						spd.setProcessoDocumento(criaDocVotoVazio(spd.getProcessoDocumento(), spd.getProcessoDocumento().getProcessoTrf()));
						spd = sessaoProcessoDocumentoVotoManager.persist((SessaoProcessoDocumentoVoto) spd);
					}
					
					if (getModeloDocumento()!=null && StringUtils.isNotEmpty(getModeloDocumento().getModeloDocumento())){
						spd.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(getModeloDocumento().getModeloDocumento());
					}
					
					elementosJulgamento_.put(k, spd);
				}
			}
			linhas.add(idJulgamento);
			if(oldRow != null && oldRow > 0){
				linhas.add(oldRow);
			}
		}
		return elementosJulgamento_;
	}
	
	/**
	 * Método pra definir um comportamento diferente na visão para documento enviado.
	 * @param doc
	 * @return 	1 - Se o orgão julgador selecionado for diferente do orgão julgador da sessão
	 * 			2 - Se o orgão julgador selecionado for igual ao orgão julador da sessão E o documento estiver assinado.
	 * 			3 - Se o orgão julgador selecionado for igual ao orgão julador da sessão E o documento NÃO estiver assinado. 
	 */
	public SituacaoDocumentoSessao validaApresentacaoEmenta(SessaoProcessoDocumento doc){
		if(!doc.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual())){
			return SituacaoDocumentoSessao.NAO_RELATOR;
		}else if(doc.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual()) && 
				!doc.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()){
			return SituacaoDocumentoSessao.RELATOR_DOCUMENTO_ASSINADO;
		}else if(doc.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual()) && 
				 doc.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()){
			return SituacaoDocumentoSessao.RELATOR_DOCUMENTO_NAO_ASSINADO;
		}
		return null;
	}

	/** 
	 * Metodo que grava documento de voto vazio (copiado da VotacaoVogalMultDocs)
	 */
	private ProcessoDocumento criaDocVotoVazio(ProcessoDocumento processoDocumento,ProcessoTrf processoTrf) {
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setModeloDocumento("  ");
		processoDocumento.setProcessoDocumentoBin(pdb);
		
		if (processoTrf.getOrgaoJulgador()!=getOrgaoAtual()){
			for (TipoProcessoDocumento tpd : getTiposProcessoDocumento()) {
				if (tpd.getTipoProcessoDocumento().equals("Voto Escrito")){
					processoDocumento.setTipoProcessoDocumento(tpd);
				}
			}
		}
		
		try {
			documentoJudicialService.persist(processoDocumento, true);
			documentoJudicialService.flush();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Erro ao gravar o documento do voto: {0}", e.getMessage());
		}
		return processoDocumento;
	}
	
	private Map<String, SessaoProcessoDocumento> getElementosJulgamento_(){
		carregarElementos();
		return elementosJulgamento;
	}
	
	public void carregarElementos(){
		if(idJulgamento != null && idOrgaoAcompanhado != null && elementosJulgamento == null){
			elementosJulgamento = getJulgamento(idJulgamento).getObjetosVotacao();
		}
	}
	
	public Integer getIdJulgamento() {
		return idJulgamento;
	}
	
	public Integer getIdOrgaoAcompanhado() {
		return idOrgaoAcompanhado;
	}
	
	/**
	 * Recupera o placar para um dado julgamento.
	 * 
	 * @param julg o identificador do julgamento
	 * @return o placar correspondente, se esse julgamento pertencer a esta sessão
	 */
	public Map<Integer, Set<Integer>> getPlacar(Integer julg){
		return getPlacar(julg, false);
	}
	
	public Set<Integer> getImpedidos(Integer julg){
		return getPlacar(julg, false).get(-2);
	}
	
	public Set<Integer> getOmissos(Integer julg){
		return getPlacar(julg, false).get(-1);
	}
	
	private Map<Integer, Set<Integer>> getPlacar(Integer julg, boolean reload){
		if(julg == null || (julg == 0 && idJulgamento != null)){
			julg = idJulgamento;
		}
		if(julg == 0){
			return Collections.emptyMap();
		}
		carregarJulgamento(julg, false);
		return votacao.get(julg).getPlacar();
	}
	
	public VotacaoBean getJulgamento(Integer julg){
		if(julg == null || julg == 0){
			julg = idJulgamento;
		}
		carregarJulgamento(julg, false);
		return votacao.get(julg);
	}
	
	private void carregarJulgamento(Integer julg, boolean reload){
		if(julg != null && julg > 0 && (votacao.get(julg) == null || reload)){
			try {
				SessaoPautaProcessoTrf julgamento = sessaoPautaProcessoTrfManager.findById(julg);
				Map<String, TipoProcessoDocumento> tiposDocumentos = getTiposDocumentos();
				VotacaoBean v = new VotacaoBean(julgamento.getProcessoTrf(), orgaoAtual, sessao, julgamento, true, sessaoProcessoDocumentoVotoManager, sessaoProcessoDocumentoManager, tiposDocumentos,
						tipoVotoManager, documentoJudicialService, derrubadaVotoManager);
				votacao.put(julg, v);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Map<String, TipoProcessoDocumento> getTiposDocumentos() {
		Map<String, TipoProcessoDocumento> ret = new HashMap<String, TipoProcessoDocumento>();
		ret.put(ObjetoVoto.RELATORIO.toString(), ParametroUtil.instance().getTipoProcessoDocumentoRelatorio());
		ret.put(ObjetoVoto.EMENTA.toString(), ParametroUtil.instance().getTipoProcessoDocumentoEmenta());
		ret.put(ObjetoVoto.VOTO.toString(), ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		ret.putAll(tipoProcessoDocumentoManager.getMapTipoProcessoDocumento(ParametroUtil.instance().getIdsTipoDocumentoVoto()));
		
		return ret;
	}

	/**
	 * Retorna o voto do relator para a sessao pauta processo.
	 * 
	 * @param processoPautadoSessao
	 * @return Retorna o voto do relator
	 */
	public SessaoProcessoDocumentoVoto getVotoRelator(SessaoPautaProcessoTrf processoPautadoSessao){
		try {
			if(processoPautadoSessao == null){
				processoPautadoSessao = sessaoPautaProcessoTrfManager.findById(idJulgamento);
			}
			return votoManager.getVotoRelator(processoPautadoSessao);
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível recuperar o voto do relator");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Retorna o voto do orgao julgador atual (logado) em relação ao procesos pautado em questão 
	 * 
	 * @param processoPautadoSessao objeto representando um processo pautado em uma sessão de julgamento
	 * @return SessaoProcessoDocumentoVoto o voto do orgao julgador atual
	 */
	public SessaoProcessoDocumentoVoto getVotoProprio(SessaoPautaProcessoTrf processoPautadoSessao){
		return votoManager.getVotoProprio(processoPautadoSessao);
	}
	
	//******** Início do tratamento de filtragem *********//
	
	public void filtrar(){
		try {
			List<Criteria> crits = new ArrayList<Criteria>();
			addCriterias("processoTrf.", crits);
			processos.setCriterias(crits);
		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar incluir os critérios de pesquisa.");
		}
	}
	
	/**
	 * Efetua a limpeza dos campos de filtragem
	 */
	public void limparCamposFiltro(){
		this.campoClasse = null;
		this.campoAssunto = null;
		this.numeroProcesso = null;
		this.prioridade = null;
		this.orgaoFiltro = null;
		this.tipoVotoFiltro = null;
		this.nomeParte = null;
		this.codigoIMF = null;
		this.codigoOAB = null;	
		this.dataInicialDistribuicao = null;
		this.dataFinalDistribuicao = null;
	}
	
	/**
	 * Acrescenta eventuais critérios de pesquisa de página a uma dada pesquisa.
	 * 
	 * @param prefix o prefixo JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriterias(String prefix, List<Criteria> criterias){
		addCriteriaAssunto(prefix, criterias);
		addCriteriaClasse(prefix, criterias);
		addCriteriaProcesso(prefix, criterias);
		addCriteriaDataDistribuicao(prefix, criterias);
		addCriteriaPrioridade(prefix, criterias);
		addCriteriaOrgao(prefix, criterias);
		addCriteriaTipoVoto(prefix, criterias);
		addCriteriaNomeParte(prefix, criterias);
		addCriteriaIMF(prefix, criterias);
		addCriteriaOAB(prefix, criterias);
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao número do processo.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaProcesso(String prefix, List<Criteria> criterias){
		if(numeroProcesso != null && !StringUtil.fullTrim(numeroProcesso).isEmpty()){
			Criteria crit;
			try {
				crit = findByNumeracaoUnicaParcial(prefix, numeroProcesso);
				if(crit != null){
					criterias.add(crit);
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados à prioridade.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaPrioridade(String prefix, List<Criteria> criterias){
		if(prioridade != null && prioridade.getIdPrioridadeProcesso() > 0){
			criterias.add(Criteria.equals(prefix + "prioridadeProcessoList.idPrioridadeProcesso", prioridade.getIdPrioridadeProcesso()));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados à data de distribuição.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaDataDistribuicao(String prefix, List<Criteria> criterias){
		if(dataInicialDistribuicao != null){
			criterias.add(Criteria.greaterOrEquals(prefix + "dataDistribuicao", dataInicialDistribuicao));
		}
		if(dataFinalDistribuicao != null){
			criterias.add(Criteria.lessOrEquals(prefix + "dataDistribuicao", dataFinalDistribuicao));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao assunto.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaAssunto(String prefix, List<Criteria> criterias){
		if(campoAssunto != null && !StringUtil.fullTrim(campoAssunto).isEmpty()){
			criterias.add(Criteria.or(
					Criteria.equals(prefix + "processoAssuntoList.assuntoTrf.codAssuntoTrf", campoAssunto),
					Criteria.contains(prefix + "processoAssuntoList.assuntoTrf.assuntoTrf", campoAssunto)));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao órgão julgador.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaOrgao(String prefix, List<Criteria> criterias){
		if(orgaoFiltro != null){
			criterias.add(Criteria.equals(prefix + "orgaoJulgador", orgaoFiltro));
		}
	}

	/**
	 * Acrescenta à pesquisa os filtros relacionados ao tipo de voto do relator.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaTipoVoto(String prefix, List<Criteria> criterias){
		if(tipoVotoFiltro  != null){
			criterias.add(Criteria.exists(" select 1 from SessaoProcessoDocumentoVoto spdv where spdv.sessao = o.sessao and processoTrf = o.processoTrf and orgaoJulgador = o.processoTrf.orgaoJulgador and spdv.tipoVoto.idTipoVoto = "+ tipoVotoFiltro.getIdTipoVoto()));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados à classe judicial.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaClasse(String prefix, List<Criteria> criterias){
		if(campoClasse != null && !StringUtil.fullTrim(campoClasse).isEmpty()){
			criterias.add(Criteria.or(
					Criteria.equals(prefix + "classeJudicial.codClasseJudicial", campoClasse),
					Criteria.contains(prefix + "classeJudicial.classeJudicial", campoClasse),
					Criteria.equals(prefix + "classeJudicial.classeJudicialSigla", campoClasse)));
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao nome da parte.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaNomeParte(String prefix, List<Criteria> criterias){
		if(nomeParte == null || StringUtil.fullTrim(nomeParte).isEmpty()){
			return;
		}
		String nome = StringUtil.fullTrim(nomeParte);
		if(nome.split(" ").length > 1){
			nome = nome.replaceAll(" ", "%");
			Criteria nomeDocumento = Criteria.contains(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.nome", nome);
			nomeDocumento.setRequired(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList", false);
			Criteria n3 = Criteria.and(
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true),
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false),
					nomeDocumento);
			Criteria n1 = Criteria.contains(prefix + "processoParteList.pessoa.nome", nome);
			Criteria n2 = Criteria.contains(prefix + "processoParteList.pessoa.pessoaNomeAlternativoList.pessoaNomeAlternativo", nome);
			n2.setRequired(prefix + "processoParteList.pessoa.pessoaNomeAlternativoList", false);
			Criteria n4 = Criteria.equals(prefix + "processoParteList.inSituacao", ProcessoParteSituacaoEnum.A);
			Criteria orC = Criteria.or(n3, n2, n1);
			Criteria andC = Criteria.and(n4, orC);
			criterias.add(andC);
		}else{
			facesMessages.add(Severity.WARN, "É necessário informar ao menos dois nomes para realizar a consulta por nome.");
		}
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao CPF ou CNPJ.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaIMF(String prefix, List<Criteria> criterias){
		InscricaoMFUtil.InscricaoMF inscricaoMF = InscricaoMFUtil.criarInscricaoMF(codigoIMF, "CPF");
		if (inscricaoMF == null || inscricaoMF.inscricao.isEmpty()){
			return;
		}
		Criteria documento = Criteria.and(
				Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", inscricaoMF.tipo),
				Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true),
				Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false),
				inscricaoMF.tipo.equals("CPF") ? 
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", inscricaoMF.inscricao) :
					Criteria.startsWith(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", inscricaoMF.inscricao)
		);
		criterias.add(documento);
	}
	
	/**
	 * Acrescenta à pesquisa os filtros relacionados ao número da OAB.
	 * 
	 * @param prefix o caminho JavaBean para o campo processo judicial
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaOAB(String prefix, List<Criteria> criterias){
		if(codigoOAB == null || codigoOAB.isEmpty()){
			return;
		}
		String oab = StringUtil.fullTrim(codigoOAB).replaceAll(" ", "%");
		if(!oab.isEmpty()){
			Criteria documento = Criteria.and(
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo", "OAB"),
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true),
					Criteria.equals(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false),
					Criteria.contains(prefix + "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", oab));
			criterias.add(documento);
		}
	}
	
	/**
	 * Monta os critérios de filtragem relacionados ao número do processo.
	 * 
	 * Este método assegura que o número dado seja despido de caracteres não numéricos e,
	 * caso tenha:
	 * <li>20 ou mais caracteres numéricos, que os 20 caracteres iniciais sejam considerados o número do processo;</li>
	 * <li>entre 14 e 20 caracteres, que os caracteres sejam considerados preenchidos com zeros na parte inicial</li>
	 * <li>entre 7 e 13 caracteres, que os caracteres sejam considerados, alternativamente, como os campos NNNNNNNN-DV e
	 * N-DV.AAAA</li>
	 * <li>entre 3 e 6 caracteres, que os caracteres sejam considerados, alternativamente, como os campos NNNNNNN e N-DV.</li>
	 * 
	 * Havendo menos que 3 caracteres, o método disparará uma exceção.
	 * 
	 * @param prefix o prefixo JavaBean para o campo processo judicial
	 * @param numero o número a partir do qual serão montados os filtros
	 * @return o critério, provavelmente do tipo {@link Criteria#or(Criteria...)}, com os filtros
	 * por número de processo
	 * @throws PJeBusinessException caso haja alguma exceção na montagem dos filtros.
	 */
	private Criteria findByNumeracaoUnicaParcial(String prefix, String numero) throws PJeBusinessException {
		String nu = numero.replaceAll("\\D", "");
		int comp = nu.length();
		if (comp >= 20){
			Integer numeroSequencia = Integer.parseInt(nu.substring(0, 7));
			Integer numeroDigitoVerificador = Integer.parseInt(nu.substring(7, 9));
			Integer ano = Integer.parseInt(nu.substring(9, 13));
			Integer segmento = Integer.parseInt(nu.substring(13, 14));
			Integer tribunal = Integer.parseInt(nu.substring(14, 16));
			Integer numeroOrigem = Integer.parseInt(nu.substring(16));
			return findByNumeracaoUnica(prefix, numeroSequencia, numeroDigitoVerificador, ano, segmento, tribunal, numeroOrigem);
		}else if (comp < 20 && comp > 13){
			Integer origem = Integer.parseInt(nu.substring(comp - 4));
			Integer tribunal = Integer.parseInt(nu.substring(comp - 6, comp - 4));
			Integer segmento = Integer.parseInt(nu.substring(comp - 7, comp - 6));
			Integer ano = Integer.parseInt(nu.substring(comp - 11, comp - 7));
			Integer dv = Integer.parseInt(nu.substring(comp - 13, comp - 11));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 13));
			return findByNumeracaoUnica(prefix, nnn, dv, ano, segmento, tribunal, origem);
		}else if (comp <= 13 && comp >= 7){
			List<Criteria> crits = new ArrayList<Criteria>();
			Integer ano = Integer.parseInt(nu.substring(comp - 4));
			Integer dv = Integer.parseInt(nu.substring(comp - 6, comp - 4));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 6));
			crits.add(findByNumeracaoUnica(prefix, nnn, dv, ano, null, null, null));
			if (comp < 10){
				dv = Integer.parseInt(nu.substring(comp - 2));
				nnn = Integer.parseInt(nu.substring(0, comp - 2));
				crits.add(findByNumeracaoUnica(prefix, nnn, dv, null, null, null, null));
			}
			return Criteria.or(crits.toArray(new Criteria[crits.size()])); 
		}else if (comp >= 3){
			List<Criteria> crits = new ArrayList<Criteria>();
			Integer dv = Integer.parseInt(nu.substring(comp - 2));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 2));
			crits.add(findByNumeracaoUnica(prefix, nnn, dv, null, null, null, null));
			crits.add(findByNumeracaoUnica(prefix, Integer.parseInt(nu), null, null, null, null, null));
			return Criteria.or(crits.toArray(new Criteria[crits.size()]));
		}else{
			throw new PJeBusinessException("É necessário inserir pelo menos três dígitos para pesquisar pelo número do processo (N-DV).");
		}
	}

	/**
	 * Monta um critério de consulta por número de processo, assegurando que campos nulos não sejam considerados.
	 * 
	 * @param prefix o caminho JavaBean até o processo judicial na pesquisa
	 * @param numero o campo NNNNNNN
	 * @param dv o campo DV
	 * @param ano o campo AAAA
	 * @param segmento o campo J
	 * @param tribunal o campo TR
	 * @param origem o campo OOOO
	 * @return o critério montado
	 */
	private Criteria findByNumeracaoUnica(String prefix, Integer numero, Integer dv, Integer ano, Integer segmento, Integer tribunal, Integer origem){
		if (numero == null && dv == null && ano == null && (segmento == null || tribunal == null) && origem == null){
			throw new IllegalArgumentException("A consulta segundo a numeração única exige ao menos um dos campos de sua composição.");
		}
		List<Criteria> crits = new ArrayList<Criteria>();
		if (numero != null)
			crits.add(Criteria.equals(prefix + "numeroSequencia", numero));
		if (dv != null)
			crits.add(Criteria.equals(prefix + "numeroDigitoVerificador", dv));
		Integer numeroOrgaoJustica = null;
		if (ano != null)
			crits.add(Criteria.equals(prefix + "ano", ano));
		if (segmento != null && tribunal != null){
			numeroOrgaoJustica = segmento * 100 + tribunal;
			crits.add(Criteria.equals(prefix + "numeroOrgaoJustica", numeroOrgaoJustica));
		}
		if (origem != null)
			crits.add(Criteria.equals(prefix + "numeroOrigem", origem));
		return Criteria.and(crits.toArray(new Criteria[crits.size()]));
	}
	
	public String getColor(Integer oj, Integer idJulgamento){
		if(colorsMap.get(idJulgamento) == null){
			Map<Integer, String> map = new HashMap<Integer, String>();
			if(votacao.get(idJulgamento) != null && votacao.get(idJulgamento).getMapaCores() != null && !StringUtil.isNullOrEmpty(votacao.get(idJulgamento).getMapaCores().get(oj))){
				map.put(oj,votacao.get(idJulgamento).getMapaCores().get(oj));
			}else{
				map.put(oj, colors[0]);
			}
			colorsMap.put(idJulgamento, map);
		}else if(colorsMap.get(idJulgamento).get(oj) == null) {
			if(votacao.get(idJulgamento) != null && votacao.get(idJulgamento).getMapaCores() != null && !StringUtil.isNullOrEmpty(votacao.get(idJulgamento).getMapaCores().get(oj))){
				colorsMap.get(idJulgamento).put(oj,votacao.get(idJulgamento).getMapaCores().get(oj));
			}else{
				colorsMap.get(idJulgamento).put(oj, colors[ncolor % colors.length]);
			}

		}
		ncolor++;
		return colorsMap.get(idJulgamento).get(oj);
	}
	
	public Integer getNumeroOrdem() {
		return numeroOrdem;
	}

	public void setNumeroOrdem(Integer numeroOrdem) {
		this.numeroOrdem = numeroOrdem;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(String campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public String getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(String campoClasse) {
		this.campoClasse = campoClasse;
	}

	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}

	public Date getDataInicialDistribuicao() {
		return dataInicialDistribuicao;
	}

	public void setDataInicialDistribuicao(Date dataInicialDistribuicao) {
		this.dataInicialDistribuicao = dataInicialDistribuicao;
	}

	public Date getDataFinalDistribuicao() {
		return dataFinalDistribuicao;
	}

	public void setDataFinalDistribuicao(Date dataFinalDistribuicao) {
		this.dataFinalDistribuicao = dataFinalDistribuicao;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getCodigoIMF() {
		return codigoIMF;
	}

	public void setCodigoIMF(String codigoIMF) {
		this.codigoIMF = codigoIMF;
	}

	public String getCodigoOAB() {
		return codigoOAB;
	}

	public void setCodigoOAB(String codigoOAB) {
		this.codigoOAB = codigoOAB;
	}

	public OrgaoJulgador getOrgaoFiltro() {
		return orgaoFiltro;
	}

	public void setOrgaoFiltro(OrgaoJulgador orgaoFiltro) {
		this.orgaoFiltro = orgaoFiltro;
	}
	
	public void setTipoVotoFiltro(TipoVoto tipoVotoFiltro){
		this.tipoVotoFiltro = tipoVotoFiltro;
	}
	
	public TipoVoto getTipoVotoFiltro(){
		return this.tipoVotoFiltro;
	}

	public List<PrioridadeProcesso> getPrioridades() {
		if(prioridades == null){
			try {
				prioridades = prioridadeProcessoManager.listActive();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar as listas de prioridades: {0}.", e.getLocalizedMessage());
				return Collections.emptyList();
			}
		}
		return prioridades;
	}

	//********** Fim do tratamento de filtragem **********//
	/**
	 * Recupera o órgão julgador atual.
	 * 
	 * @return o órgão julgador
	 */
	public OrgaoJulgador getOrgaoAtual() {
		return orgaoAtual;
	}
	
	/**
	 * Recupera a sessão de julgamento atualmente tratada.
	 * 
	 * @return a sessão
	 */
	public Sessao getSessao() {
		return sessao;
	}

	/**
	 * Recupera o modelo de dados tratável por uma tag <rich:dataModel> para paginação.
	 * 
	 * @return o modelo de dados
	 */
	public EntityDataModel<SessaoPautaProcessoTrf> getProcessos() {
		return processos;
	}
	
	public boolean isRedigir() {
		return redigir;
	}
	
	public Set<Integer> getLinhas() {
		return linhas;
	}
	
	public Integer getOldRow() {
		return oldRow;
	}
	
	public boolean isMostrarVoto() {
		return mostrarVoto;
	}
	
	public void setMostrarVoto(boolean mostrarVoto) {
		this.mostrarVoto = mostrarVoto;
	}

	public boolean isMostrarAnotacoes() {
		return mostrarAnotacoes;
	}

	public List<NotaSessaoJulgamento> getAnotacoes() {
		if(anotacoes == null && idJulgamento != null && idJulgamento > 0){
			SessaoPautaProcessoTrf spt;
			try {
				spt = sessaoPautaProcessoTrfManager.findById(idJulgamento);
				anotacoes = notaSessaoJulgamentoManager.recuperaNotas(spt.getSessao(), spt.getProcessoTrf());
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Não foi possível recuperar as notas.");
				e.printStackTrace();
			}
		}
		return anotacoes;
	}
	
	public String getAnotacao() {
		return anotacao;
	}
	
	public void setAnotacao(String anotacao) {
		this.anotacao = anotacao;
	}
	
	public String getProcessosSemJulgamento() {
		return sessaoJulgamentoManager.getProcessosSemJulgamento(getSessao().getIdSessao());
	}
	
	public String getProcessosJulgados() {
		return sessaoJulgamentoManager.getProcessosJulgados(getSessao().getIdSessao());
	}
	
	public String getVista() {
		return sessaoJulgamentoManager.getVista(getSessao().getIdSessao());
	}
	
	public String getAdiado() {
		return sessaoJulgamentoManager.getAdiado(getSessao().getIdSessao());
	}
	public String getRetiradoJulgamento() {
		return sessaoJulgamentoManager.getRetiradoJulgamento(getSessao().getIdSessao());
	}
	public SessaoProcessoDocumentoVoto getVoto() {
		return voto;
	}
	public void setVoto(SessaoProcessoDocumentoVoto voto) {
		this.voto = voto;
	}
	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}
	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		if (modeloDocumento!=null && StringUtils.isNotEmpty(modeloDocumento.getModeloDocumento())){
			this.modeloDocumento = modeloDocumento;
			this.idjulg_=null;
			getElementosJulgamento();
		}
	}

	public List<ModeloDocumento> getModelosDocumento(TipoProcessoDocumento tpd) {
		this.modelosDocumento.clear();
		if(tpd != null){
			try {
				this.modelosDocumento.addAll(documentoJudicialService.getModelosLocais(tpd));
			} catch (PJeBusinessException ex) {
				facesMessages.add(Severity.ERROR, ex.getMessage());
			}
		}
		return this.modelosDocumento;
	}

	public void setModelosDocumento(List<ModeloDocumento> modelosDocumento) {
		this.modelosDocumento = modelosDocumento;
	}

	public String getTab() {
		return tab;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		this.arquivoAssinado = arquivoAssinadoHash;
		Contexts.getSessionContext().set("votoArquivoAssinadoHash", arquivoAssinadoHash);
	}
	
	public String getDownloadLinks(ProcessoDocumento doc){
		if(doc != null){
			return documentoJudicialService.getDownloadLinks(Arrays.asList(doc));
		}else{
			return "";
		}
	}		

	@Override
	public String getActionName() {
		return NAME;
	}
	
	public ArquivoAssinadoHash getArquivoAssinado() {
		return arquivoAssinado;
	}
	
	public void setArquivoAssinado(ArquivoAssinadoHash arquivoAssinado) {
		this.arquivoAssinado = arquivoAssinado;
	}
	
	public void retirarDeJulgamento(){
		if(idJulgamento != null){
			try {
				SessaoPautaProcessoTrf julgamento = sessaoPautaProcessoTrfManager.findById(idJulgamento);
				sessaoPautaProcessoTrfManager.retirarDePauta(julgamento, getOrgaoAtual());
				if(processosJulgamento != null && processosJulgamento.contains(julgamento)){
					processosJulgamento.remove(julgamento);
				}
				julgamento.getProcessoTrf().setPautaVirtual(Boolean.FALSE);
				sessaoPautaProcessoTrfManager.persistAndFlush(julgamento);
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao retirar de julgamento: {0}", e.getLocalizedMessage());
			}
		}else{
			logger.error("Erro ao retirar de julgamento: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao retirar de julgamento: identificador do julgamento não encontrado.");
		}
	}
	
	private void executaAcaoAptidaoProcessoRetiradoPauta(ProcessoTrf processoTrf){
		try{
			if(Authenticator.getOrgaoJulgadorAtual().equals(processoTrf.getOrgaoJulgador())){
				processoJudicialManager.aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), false, null);
			} else {
				processoJudicialManager.aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), false, null);
				processoJudicialManager.aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), true);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}	

	/**
	 * Método usado para liberar a visibilidade do voto do magistrado logado para outros magistrados
	 * de mesmo órgão julgador colegiado.
	 * Esse método atualiza a flag in_liberacao de SessaoPautaDocumento do voto do magistrado.
	 * 
	 * @param liberar true, para liberar o voto para outros magistrados virem e false para ocultar o voto.
	 */
	public void atualizarLiberacaoVoto(boolean liberar){
		if(idJulgamento != null){
			try {
				SessaoPautaProcessoTrf processoPautadoSessao = sessaoPautaProcessoTrfManager.findById(idJulgamento);
				SessaoProcessoDocumentoVoto spdv = votoManager.getVotoProprio(processoPautadoSessao);
				if(spdv != null){
					spdv.setLiberacao(liberar);
					sessaoProcessoDocumentoVotoManager.persistAndFlush(spdv);
					if (liberar)
						facesMessages.add(Severity.INFO, "Voto liberado com sucesso.");
					else 
						facesMessages.add(Severity.INFO, "Voto ocultado com sucesso.");
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao alteração da visibilidade do voto: {0}", e.getLocalizedMessage());
			}
		}else{
			logger.error("Erro ao alteração da visibilidade voto: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao alteração da visibilidade voto: identificador do julgamento não encontrado.");
		}
	}
	
	
	/**
	 * Método que verifica se o voto do magistrado está liberado para ser visto por outros magistrados
	 * @param idJulg
	 * @return true, se estiver liberado; false caso contrário
	 */
	public boolean isVotoLiberado(Integer idJulg){
		boolean isVotoLiberado = false;
		if(idJulg != null){
			try {
				SessaoPautaProcessoTrf processoPautadoSessao = sessaoPautaProcessoTrfManager.findById(idJulg);
				SessaoProcessoDocumentoVoto spdv = votoManager.getVotoProprio(processoPautadoSessao);
				if(spdv != null){
					isVotoLiberado = spdv.getLiberacao();
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
				logger.error("Erro ao verificar se o voto foi liberado: {0}", e.getLocalizedMessage());
			}
		}else{
			logger.error("Erro ao verificar se o voto foi liberado: identificador do julgamento não encontrado.");
			facesMessages.add(Severity.ERROR, "Erro ao verificar se o voto foi liberado: identificador do julgamento não encontrado.");
		}
		return isVotoLiberado;
	}
	
 	public String getDeclararImpedimentoSuspeicao() {
 		return ParametroJtUtil.instance().cnj() ? 
 				Messages.instance().get("sessao.impedidoSuspeito") : Messages.instance().get("sessao.declaradoImpedimentoSuspeicao");
 	}
 	
 	public void registrarPedidoVista(SessaoPautaProcessoTrf julgamento) throws Exception{
 		sessaoPautaProcessoTrfManager.registrarPedidoVista(julgamento, Authenticator.getOrgaoJulgadorAtual(), Authenticator.getOrgaoJulgadorCargoAtual());
 		processosJulgamento = null;
 	}
 	
 	public void retiraPedidoVista(SessaoPautaProcessoTrf julgamento) throws Exception{
 		sessaoPautaProcessoTrfManager.retiraPedidoVista(julgamento);
 		processosJulgamento = null;
 	}
 	
 	public void retirarParaReexame(SessaoPautaProcessoTrf julgamento) throws PJeBusinessException{
 		sessaoPautaProcessoTrfManager.retirarParaReexame(julgamento);
 		executaAcaoAptidaoProcessoRetiradoPauta(julgamento.getProcessoTrf());
 		processosJulgamento = null;
 	}
 	
 	public boolean retiradoJulgamentoParaReexame(SessaoPautaProcessoTrf julgamento){
 		if(julgamento.getAdiadoVista() != null && julgamento.getAdiadoVista().equals(AdiadoVistaEnum.AD) 
 				&& julgamento.getRetiradaJulgamento()
 				&& julgamento.getOrgaoJulgadorRetiradaJulgamento() != null
 				&& !julgamento.getSessao().getContinua()){
 			return true;
 		}
 		return false;
 	}
 	
 	
 	public boolean retiradoJulgamento(SessaoPautaProcessoTrf julgamento){
 		if(julgamento.getAdiadoVista() != null && julgamento.getAdiadoVista().equals(AdiadoVistaEnum.AD) 
 				&& julgamento.getRetiradaJulgamento()
 				&& julgamento.getOrgaoJulgadorRetiradaJulgamento() == null
 				&& !julgamento.getSessao().getContinua()){
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean retiradoJulgamentoParaPautaPresencial(SessaoPautaProcessoTrf julgamento){
 		if(julgamento.getAdiadoVista() != null && julgamento.getAdiadoVista().equals(AdiadoVistaEnum.AD) 
 				&& julgamento.getRetiradaJulgamento()
 				&& julgamento.getSessao().getContinua()){
 			return true;
 		}
 		return false;
 	}

	public List<TipoVoto> recuperaTiposVotosVogais(){
		return tipoVotoManager.tiposVotosVogais();
	}
	
	/**
	 * Recupera o ícone de menu de acordo com o contexto
	 * @param contexto
	 * @return icone do contexto
	 */
	public String recuperaIconeContexto(String contexto){
		String iconeSelecionado = "fa-exclamation";
		Map<String, String> icones = new HashMap<String, String>();
		
		icones.put("C", "fa-thumbs-o-up");
		icones.put("P", "fa-hand-o-up");
		icones.put("D", "fa-thumbs-o-down");
		icones.put("S", "fa-ban");
		icones.put("I", "fa-hand-paper-o");
		icones.put("N", "fa-exclamation-triangle");
		
		if(icones.containsKey(contexto)){
			iconeSelecionado = icones.get(contexto).toString(); 
		}
		
		return iconeSelecionado;
	}
	
 	public Boolean isUsuarioLogadoParticipanteComposicao(SessaoPautaProcessoTrf processoPautado){
 		return composicaoJulgamentoService.isUsuarioLogadoParticipanteComposicao(processoPautado); 		
 	}
 
	public String obterNomeRelator(ProcessoTrf processo){
		return processoMagistradoManager.obterNomeRelator(processo);
	}
	
}