package br.com.infox.cliente.home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.RelacaoJulgamento;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.ProcessoAdiadoVistaList;
import br.com.infox.pje.list.ProcessoAdiadoVistaPVList;
import br.com.infox.pje.list.SessaoPautaAptosParaInclusaoPautaDemaisRelatoresList;
import br.com.infox.pje.list.SessaoPautaRelacaoJulgamentoList;
import br.com.infox.pje.list.SessaoPautaSecretarioProcessoNuloList;
import br.com.infox.trf.webservice.ConsultaInstanciaInferiorIntercomunicacao;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.entidades.vo.SessaoComposicaoVotoLoteVO;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.dto.AptoPublicacaoDTO;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.ProcessoParteUtils;
import br.jus.cnj.pje.vo.PlacarSessaoVO;
import br.jus.csjt.pje.business.pdf.XhtmlParaPdf;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoAdiadoVista;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.JulgamentoEnum;
import br.jus.pje.nucleo.enums.SessaoResultadoVotacaoEnum;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;
import br.jus.pje.nucleo.enums.SituacaoPautaEnum;
import br.jus.pje.nucleo.enums.SituacaoProcessoSessaoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(SessaoPautaProcessoTrfHome.NAME)
@AutoCreate
public class SessaoPautaProcessoTrfHome extends AbstractHome<SessaoPautaProcessoTrf> {

	public static final String NAME = "sessaoPautaProcessoTrfHome";
	
	private static final long serialVersionUID = -8747106865498792072L;
	private static final LogProvider log = Logging.getLogProvider(SessaoPautaProcessoTrfHome.class);
	private static final int ID_ORGAO_JULGADOR_RELATOR_SELECIONADO = -1;
	
	/*
	 * Váariveis do placar.
	 */
	private static final int VOTOS_OMISSOS = -1;
	private static final int VOTOS_IMPEDIDOS = -2;
	private static final int VOTOS_DIVERGENTES = -3;
	private static final int VOTOS_NAO_CONHECE = -4;
	
	@Out(required = false)
	public static final String TODOS_PROCESSOS_PEDIDO_VISTA = "T";
	@Out(required = false)
	public static final String TODOS_PROCESSOS_PEDIDO_VISTA_APRECIADO = "P";
	@Out(required = false)
	public static final String TODOS_PROCESSOS_PEDIDO_VISTA_NAO_APRECIADA = "N";
	
	private Boolean checkBox = Boolean.FALSE;
	private TipoInclusaoEnum tipoInclusaoEnum;
	private String modeloDocumentoBin;
	private ProcessoDocumentoBin processoDocumentoBin;
	private PessoaMagistrado vencedor;
	private List<SessaoPautaProcessoTrf> listaSCO = new ArrayList<SessaoPautaProcessoTrf>();
	private List<ConsultaProcessoAdiadoVista> listProcesso = new ArrayList<ConsultaProcessoAdiadoVista>(0);
	private List<ConsultaProcessoAdiadoVista> listProcessoAdiadoPedidoVista = new ArrayList<ConsultaProcessoAdiadoVista>(0);
	private List<SessaoPautaProcessoTrf> listVoto = new ArrayList<SessaoPautaProcessoTrf>(0);
	
	private OrgaoJulgador orgaoJulgadorVencedor;
	private OrgaoJulgador orgaoJulgadorPediuVista;
	private OrgaoJulgadorCargo orgaoJulgadorCargoPediuVista;
	
	private Boolean mostraPlacar = Boolean.FALSE;
	private Boolean mostraJulgado = Boolean.FALSE;
	private Boolean mostraSustentacaoOral = Boolean.FALSE;
	private Boolean mostrarNota = Boolean.FALSE;
	private Boolean mostraVotacaoLote = Boolean.FALSE;
	private String advogadoSustentacaoOral;
	private String proclamacaoDecisao;
	private JulgamentoEnum julgamentoEnum = JulgamentoEnum.M;
	private String tituloModalProcessoDecisao;
	private Boolean checkAll = Boolean.FALSE;
	private String msgListaSco;
	private Boolean msgErro;
	private String msgInfo;
	private ProcessoDocumento pd = new ProcessoDocumento();
	private String resultSustentacaoOral;
	private List<OrgaoJulgador> resultListPedidoVista = new ArrayList<OrgaoJulgador>();
	private Boolean checkAllAIPDR = Boolean.FALSE;
	private Boolean checkAllAPBL = Boolean.FALSE;
	private List<ProcessoTrf> processosNaoRevisados = new ArrayList<ProcessoTrf>();
	private List<ProcessoTrf> listProcessoTrf = new ArrayList<ProcessoTrf>();
	private Boolean isJulgado;
	private Boolean mostraPedidoVista = Boolean.FALSE;
	private Boolean utilizarNovoTxtTodosProcessos = Boolean.FALSE;
	private Boolean possuemProclamacaoAntecipada = Boolean.FALSE;
	private String labelRadioButtonProclamacao;
	private String itemLabelRadioButton;
	private String opcaoInclusaoPauta = TODOS_PROCESSOS_PEDIDO_VISTA;
	private ConsultaProcessoAdiadoVista processoVistaInclusao;
	private List<OrgaoJulgador> listOrgaoJulgadorAcompanhado = new ArrayList<OrgaoJulgador>();
	private List<SessaoComposicaoVotoLoteVO> listSessaoComposicaoVotoLote = new ArrayList<SessaoComposicaoVotoLoteVO>();
	private boolean exibirModalVotoVogal = false;
	
	private Map<Integer, String> nomeOrgao;
	
	private Map<Integer, Map<Integer, Set<Integer>>> placares = new HashMap<Integer, Map<Integer,Set<Integer>>>();
	
	private Map<SessaoPautaProcessoTrf, Boolean> mapaPublicacao = new HashMap<SessaoPautaProcessoTrf, Boolean>();
	
	private PublicadorDJE publicadorDJE = null;
	
	public static SessaoPautaProcessoTrfHome instance() {
		SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class);
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = sessaoPautaProcessoTrfHome.getInstance();
		if (sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor() == null && sessaoPautaProcessoTrf.getProcessoTrf() != null){
			sessaoPautaProcessoTrf.setOrgaoJulgadorVencedor(sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
		}
		return sessaoPautaProcessoTrfHome;
		
	}

	public Boolean destaqueProcesso(SessaoPautaProcessoTrf row) {
		StringBuilder sb = new StringBuilder();

		sb.append("select o.destaqueSessao from SessaoProcessoDocumentoVoto o where " + "o.sessao = :sessao"
				+ " and o.processoDocumento.processo.idProcesso = :idProcesso");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("sessao", row.getSessao());
		query.setParameter("idProcesso", row.getProcessoTrf().getProcesso().getIdProcesso());
		if (query.getResultList().size() == 0)
			return Boolean.FALSE;
		return (Boolean) query.getResultList().get(0);
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> sessaoComposicaoOrdemOJ() {
		StringBuilder sql = new StringBuilder();
		sql.append("select o.orgaoJulgador from SessaoComposicaoOrdem o ");
		sql.append("where o.sessao = :sessao ");
		sql.append("order by o.orgaoJulgador.orgaoJulgadorOrdemAlfabetica");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("sessao", SessaoHome.instance().getInstance());
		List<OrgaoJulgador> resultList = query.getResultList();
		return resultList;
	}

	private Integer valorPlacar(String contexto) {
		Long cont = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).contagemVotos(getInstance().getSessao(), getInstance().getProcessoTrf(), contexto);
		return cont.intValue();
	}

	public HashMap<String, Integer> getPlacar() {
		HashMap<String, Integer> o = new HashMap<String, Integer>();
		o.put("procedente", valorPlacar("C"));
		o.put("parcialmente", valorPlacar("P"));
		o.put("contra", valorPlacar("D"));
		return o;
	}
	
	public Set<Integer> getOmissos(Sessao sessao, ProcessoTrf processo){
		return ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).getOmissos(sessao, processo, true);
	}
	
	public Set<Integer> getImpedidos(Sessao sessao, ProcessoTrf processo){
		return ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).getImpedidos(sessao, processo, true);
	}
	

	// método não mais chamado pela grid do resultado dos processos da sessão do
	// painel do secretário de sessão
	public HashMap<String, String> getResultadoSessao() {
		HashMap<String, String> o = new HashMap<String, String>();
		SessaoHome a = SessaoHome.instance();
		o.put("processosSemJulgamento", a.getProcessosSemJulgamento());
		o.put("processosJulgados", a.getProcessosJulgados());
		o.put("vista", a.getVista());
		o.put("adiado", a.getAdiado());
		o.put("retiradoJulgamento", a.getRetiradoJulgamento());
		return o;
	}

	public List<SituacaoProcessoSessaoEnum> situacaoProcessoSessaoEnumItems() {
		List<SituacaoProcessoSessaoEnum> lista = new ArrayList<SituacaoProcessoSessaoEnum>();
		lista.add(SituacaoProcessoSessaoEnum.AD);
		lista.add(SituacaoProcessoSessaoEnum.AJ);
		lista.add(SituacaoProcessoSessaoEnum.AN);
		lista.add(SituacaoProcessoSessaoEnum.EJ);
		lista.add(SituacaoProcessoSessaoEnum.JG);
		lista.add(SituacaoProcessoSessaoEnum.PR);
		lista.add(SituacaoProcessoSessaoEnum.PV);
		lista.add(SituacaoProcessoSessaoEnum.RJ);
		lista.add(SituacaoProcessoSessaoEnum.SO);
		lista.add(SituacaoProcessoSessaoEnum.JC);
		return lista;
	}

	/**
	 * Este método é responsável por verificar se existem votos sem documentos ou que não foram produzidos
	 * para um determinado processo na sessão de julgamento.
	 * 
	 * @return true, se existem documentos nao assinados, false, se não existem documentos a serem assinados
	 */
	public boolean verificaDocumentosNaoAssinados()
	{
		if (listaSCO.isEmpty()) return true;
		for(SessaoComposicaoOrdem scol : listaSCO.get(0).getSessao().getSessaoComposicaoOrdemList())
		{
			if(scol.getPresenteSessao())
			{
				ProcessoTrf processoTrf = listaSCO.get(0).getProcessoTrf();
				List<SessaoProcessoDocumento> sessaoProcDoc = getDocumentosSessaoOj(processoTrf, scol.getSessao(), scol.getOrgaoJulgador());
				if(!sessaoProcDoc.isEmpty())
				{
					// O Orgão julgador produziu documentos para o processo
					// Itera a lista de documentos para verificar se todos estão assinados
					for(SessaoProcessoDocumento sProcDoc : sessaoProcDoc)
					{
						if(sProcDoc.getProcessoDocumento().getProcessoDocumentoBin().getDataAssinatura() == null)
						{
							// Encontrou documento não assinado
							return true;
						}
					}
				}
				else
				{
					// O órgão julgador está presente mas não produziu nenhum documento
					SessaoProcessoDocumentoVoto spdv = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).recuperarVoto(scol.getSessao(), processoTrf, scol.getOrgaoJulgador());
					// Não está impedido ou suspeito
					if(spdv == null){
						return true;
					}
				}		
			}
		}
		
		return false;
	}
		
	public void acaoProcesso(String acao) {
		if (acao.equals("julgado")) {
			if(listaSCO.size() == 1){
				setProclamacaoDecisao(listaSCO.get(0).getProclamacaoDecisao());
				setJulgamentoEnum(listaSCO.get(0).getJulgamentoEnum());
				setOrgaoJulgadorVencedor(listaSCO.get(0).getOrgaoJulgadorVencedor() != null ? listaSCO.get(0).getOrgaoJulgadorVencedor() : listaSCO.get(0).getProcessoTrf().getOrgaoJulgador());
			} else if (isMovimentacaoEmLote()) {
				mostrarRadioButtonsProclamacaoJulgamento();
			}
			this.mostraJulgado = Boolean.TRUE;
			this.tituloModalProcessoDecisao = "Proclamação do Julgamento";
		} else if (acao.equals("sustentacaoOral")) {
			mostraSustentacaoOral = Boolean.TRUE;
			tituloModalProcessoDecisao = "Pedido de Sustentação Oral";
			if (listaSCO.get(0).getSustentacaoOral()){
				retiraSustentacaoOral();
				mostraSustentacaoOral = Boolean.FALSE;
			}
		} else if (acao.equals("pedidoVista")) {
			mostraPedidoVista = Boolean.TRUE;
			tituloModalProcessoDecisao = "Pedido de Vista";
			
			if (AdiadoVistaEnum.PV.equals(listaSCO.get(0).getAdiadoVista()) && TipoSituacaoPautaEnum.NJ.equals(listaSCO.get(0).getSituacaoJulgamento())) {
				retiraPedidoVistaPautaProcesso();
				mostraPedidoVista = Boolean.FALSE;
			}
		} else if (acao.equals("anotacao")) {
			this.mostrarNota = Boolean.TRUE;
			this.tituloModalProcessoDecisao = "Anotação";
		} else if (acao.equals("votacaoLote")) {
			setTituloModalProcessoDecisao("Votação em Lote");
			setListSessaoComposicaoVotoLote(getListaSessaoComposicaoVotoLote());
			setListOrgaoJulgadorAcompanhado(listarOrgaoaJulgadoresVotantes());
			setExibirModalVotoVogal(true);
			mostraVotacaoLote = Boolean.TRUE;
		}
	}

	/**
	 * Exibe ou esconde os radios buttons e seus labels da proclamação 
	 * de julgamento quando a movimentação é em lote. 
	 */
	private void mostrarRadioButtonsProclamacaoJulgamento() {
		List<SessaoProcessoDocumentoVoto> votos = recuperarVotosProcessosSelecionados();
		possuemProclamacaoAntecipada = isPossuemProclamacaoAntecipada(votos);
		definirLabelRadioButton();
	}

	public String verificaActionInstanciado(String action) {
		List<SessaoPautaProcessoTrf> tempList = this.listaSCO;
		this.listaSCO.clear();
		this.listaSCO.add(instance);
		String retorno = verificaAction(action);
		this.listaSCO = tempList;
		return retorno;
	}

	public void verificaAcaoProcesso(String action) {
		if (action.equals("aguardando") || action.equals("preferencia") || action.equals("adiado")
				|| action.equals("retirado") || action.equals("pautarapida") || action.equals("emjulgamento")) {

			verificaAction(action);
			listaSCO = new ArrayList<>();
		} else {
			acaoProcesso(action);
		}
	}

	public String verificaAction(String action) {
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
		List<String> sucessoList = new ArrayList<String>(0);
		List<String> erroList = new ArrayList<String>(0);
		for (SessaoPautaProcessoTrf sppt : listaSCO) {
			try {
				if (action.equals("aguardando")) {
					sessaoPautaProcessoTrfManager.alterarSituacaoParaAguardandoJulgamento(sppt);
					ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
					processoJudicialService.sinalizarFluxo(sppt.getProcessoTrf(), Variaveis.PJE_FLUXO_COLEGIADO_TORNAR_PENDENTE_JULGAMENTO, Boolean.TRUE, false, false);
				} 
				else if (action.equals("preferencia")) {				
					sessaoPautaProcessoTrfManager.marcarPreferencia(sppt);
				}
				else if (action.equals("pautarapida")) {
					sessaoPautaProcessoTrfManager.marcarPautaRapida(sppt);				
				} 
				else if (action.equals("adiado")) {
					sessaoPautaProcessoTrfManager.adiar(sppt);
				} 
				else if (action.equals("retirado")) {
					executaAcaoAptidaoProcessoRetiradoPauta(sppt.getProcessoTrf());
					sessaoPautaProcessoTrfManager.retirarDePauta(sppt);				
				} 
				else if(action.equals("emjulgamento")){
					sessaoPautaProcessoTrfManager.alterarSituacaoParaEmJulgamento(sppt);
					setMostraPlacar(Boolean.TRUE);
					ProcessoJudicialService processoJudicialService = ComponentUtil.getComponent(ProcessoJudicialService.class);
					processoJudicialService.sinalizarFluxo(sppt.getProcessoTrf(), Variaveis.PJE_FLUXO_COLEGIADO_COLOCAR_EM_JULGAMENTO, Boolean.TRUE, false, false);
				}
				sucessoList.add(sppt.getProcessoTrf().getNumeroProcesso());
			}
			catch (Exception e) {
				erroList.add(sppt.getProcessoTrf().getNumeroProcesso() + " - " + e.getLocalizedMessage());
			}		
		}
		FacesMessages.instance().clear();
		if (!sucessoList.isEmpty()) {
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "sessaoPautaProcessoTrfHome.situacaoAlteradaComSucesso", StringUtils.join(sucessoList, ", "));
		}
		if (!erroList.isEmpty()) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessaoPautaProcessoTrfHome.erroAlterarSituacao", StringUtils.join(erroList, ", "));	
		}
		return null;
	}
	
	private void executaAcaoAptidaoProcessoRetiradoPauta(ProcessoTrf processoTrf){
		try{
			ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent(ProcessoJudicialManager.class);
			processoJudicialManager.aptidaoParaJulgamento(processoTrf.getIdProcessoTrf(), false, null);
		} catch (PJeBusinessException e) {
			log.error("Ocorreu erro em SessaoPautaProcessoTrfHome.executaAcaoAptidaoProcessoRetiradoPauta(): " + e.getLocalizedMessage());
		}
	}

	/**
	 * pega a lista de processos da sessão e adia os que estão "Em Julgamento" e
	 * os que estão "Aguradando Julgamento"
	 */
	public void adiaProcsAguardandoJulgamento() {
		List<SessaoPautaProcessoTrf> spptList = new ArrayList<SessaoPautaProcessoTrf>();
		SessaoPautaSecretarioProcessoNuloList spspnList = ComponentUtil.getComponent(SessaoPautaSecretarioProcessoNuloList.class);
		spspnList.newInstance();
		spptList.addAll(spspnList.list());
		// verifica se exite processo na lista
		if (null != spptList) {
			for (SessaoPautaProcessoTrf sppt : spptList) {
				if (!sppt.isJulgamentoFinalizado() && (sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.AJ)
						|| sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.EJ))) {
					adiarProcesso(sppt);
				}
			}
		}
	}

	/**
	 * Adia uma sessão pauta processo trf.
	 * 
	 * @param sppt
	 *            SessaoPautaProcessoTrf esperado para adiar.
	 */
	public void adiarProcesso(SessaoPautaProcessoTrf sppt) {
		sppt.setAdiadoVista(AdiadoVistaEnum.AD);
		sppt.setSituacaoJulgamento(TipoSituacaoPautaEnum.NJ);
		sppt.setRetiradaJulgamento(Boolean.FALSE);
		setInstance(sppt);
		super.update();
	}

	Map<Integer, List<RelacaoJulgamento>> items = new LinkedHashMap<Integer, List<RelacaoJulgamento>>();

	public String eventoProcesso(String[] key) {
		if (key[2].equals("julgado")) {
			/*
			 * [PJEII-3840] Rodrigo S. Menezes: Somente permitir a finalização de uma sessão de julgamento na tela 
			 * do Painel do Secretário da Sessão após a elaboração/assinatura de todos documentos 
			 * (relatório/voto/ementa/acórdão) dos processos nela julgados.
			 */
			if(verificaDocumentosNaoAssinados() == true)
			{
				FacesMessages.instance().add(Severity.INFO, "Existem documentos não assinados ou não produzidos.");
				return null;
			}
			if (getInstance().getOrgaoJulgadorVencedor() == null
					|| getInstance().getOrgaoJulgadorVencedor().getIdOrgaoJulgador() == 0) {
				return "alert('Informe o Gabinete Vencedor no Julgamento do Processo "
						+ getInstance().getProcessoTrf().getNumeroProcesso() + " '); return false;";
			}
		}
		return "";
	}

	public String[][] getItems() {
		String[][] array = { { "/img/view.gif", "Detalhe do Processo", "", "" },
				{ "/img/aguardando.jpg", "Aguardando Julgamento", "aguardando", "AJ" },
				{ "/img/julgamento.jpg", "Em Julgamento", "emjulgamento", "EJ" },
				{ "/img/martelo.jpg", "Julgado", "julgado", "JG" },
				{ "/img/preferencia.jpg", "Preferência", "preferencia", "" },
				{ "/img/vista.jpg", "Pedido de Vista", "pedidoVista", "" },
				{ "/img/sustentacaoOral.jpg", "Pedido de Sustentação Oral", "sustentacaoOral", "" },
				{ "/img/adiado.jpg", "Adiado para Próxima Sessão", "adiado", "" },
				{ "/img/retirado.jpg", "Retirado de Julgamento", "retirado", "" } };

		return array;
	}

	public String[][] getItems2() {
		String[][] array = { { "/img/view.gif", "Detalhe do Processo", "" }, { "/img/martelo.jpg", "Julgado", "" },
				{ "/img/adiado.jpg", "Adiado para próxima sessão", "" },
				{ "/img/retirado.jpg", "Retirado de Julgamento", "" }, { "/img/vista.jpg", "Pedido de Vista", "" } };

		return array;
	}

	/**
	 * Busca uma lista de Ordem de Composição da Sessão.
	 * Essa lista é utilizada para acessar os órgãos julgadores que podem fazer pedido de vista,
	 * ou seja, os órgãos julgadores pertencentes à sessão, exceto o Relato e aqueles que já emitiram voto. 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<SessaoComposicaoOrdem> getListaComposicaoOrdem() {
		if (listaSCO.isEmpty())
			return new ArrayList<SessaoComposicaoOrdem>();

		StringBuilder sql = new StringBuilder();
		sql.append("select distinct a from SessaoComposicaoOrdem a where a.sessao = :sessao ");
		sql.append(" and (a.magistradoTitularPresenteSessao = true or a.magistradoSubstitutoSessao is not null) ");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("sessao", SessaoHome.instance().getInstance());
		List<SessaoComposicaoOrdem> resultList = query.getResultList();
		return resultList;
	}

	public List<SessaoComposicaoOrdem> getListaComposicaoOrdemGV() {
		
		Sessao sessao = SessaoHome.instance().getInstance();
		
		return sessao.getComposicoesPresentes();
	}

	public String addListaSCO(SessaoPautaProcessoTrf row) {
		if (listaSCO.contains(row)) {
			listaSCO.remove(row);
		} else {
			if(!row.isJulgamentoFinalizado()) {
				listaSCO.add(row);
			}
		}
		setMostraPlacar(Boolean.FALSE);
		return null;
	}

	public void limpaDecisoes() {
		mostraJulgado = Boolean.FALSE;
		mostraSustentacaoOral = Boolean.FALSE;
		mostraPedidoVista = Boolean.FALSE;
		mostrarNota = Boolean.FALSE;
		mostraVotacaoLote = Boolean.FALSE;
	}

	public String existeProcessoEmJulgamento(String[] key) {
		if (this.getListaSCO().size() > 0
				&& !this.getListaSCO().get(0).getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.EJ)
				&& key[2].equals("emjulgamento")) {
			String hql = "select count(o) from SessaoPautaProcessoTrf o where o.sessao = :sessao and o.situacaoJulgamento = 'EJ' and o.dataExclusaoProcessoTrf is null amd o.julgamentoFinalizado = false ";
			Long result = (Long) getEntityManager().createQuery(hql)
					.setParameter("sessao", SessaoHome.instance().getInstance()).getSingleResult();
			if (result > 0) {
				return " alert('Já existe um Processo em Julgamento.'); return false; ";
			}
		}
		return "";
	}

	public void gravaAdvogadoSustentacaoOral() {
		if (listaSCO != null && listaSCO.size() > 0) {
			Boolean ok = true;
			for (SessaoPautaProcessoTrf pauta : listaSCO) {
				try {
					pauta.setAdvogadoSustentacaoOral(advogadoSustentacaoOral);
					pauta.setSustentacaoOral(Boolean.TRUE);
					setInstance(pauta);
					super.update();
					setMostraSustentacaoOral(Boolean.FALSE);
				} catch (Exception e) {
					ok = false;
					log.error("Erro ao gravar a Sustentação Oral. " + e.getMessage());
				}
			}
			FacesMessages.instance().clear();
			if (ok) {
				FacesMessages.instance().add(Severity.INFO, "Sustentação gravada com sucesso!");
			} else {
				FacesMessages.instance().add(Severity.INFO, "Algumas sustentações não foram gravadas corretamente.");
			}
		}
	}
	
	/**
	 * Metodo que retira a opção de sustenção oral
	 */
	public void retiraSustentacaoOral() {
		if (listaSCO != null && listaSCO.size() > 0) {
			Boolean ok = true;
			for (SessaoPautaProcessoTrf pauta : listaSCO) {
				try {
					pauta.setAdvogadoSustentacaoOral("");
					pauta.setSustentacaoOral(Boolean.FALSE);
					setInstance(pauta);
					super.update();
					setMostraSustentacaoOral(Boolean.FALSE);
				} catch (Exception e) {
					ok = false;
					log.error("Erro ao gravar a Sustentação Oral. " + e.getMessage());
				}
			}
			FacesMessages.instance().clear();
			if (ok) {
				FacesMessages.instance().add(Severity.INFO, "Sustentação retirada com sucesso!");
			} else {
				FacesMessages.instance().add(Severity.INFO, "Algumas sustentações não foram retiradas.");
			}
		}
	}
		
	/**
	 * Grava o pedido de vista
	 * @param finalizar Informa se o sistema deve finalizar o registro da proclamação do julgamento
	 * @return
	 */
	public String gravaVista(boolean finalizar) {
		if (getOrgaoJulgadorPediuVista() != null) {
			if(getOrgaoJulgadorCargoPediuVista() != null){
				List<String> sucesso = new ArrayList<String>();
				List<String> erro = new ArrayList<String>();
				
				registraPedidoVistaPautaProcesso(sucesso, erro);
	
				if (!sucesso.isEmpty()) {
					FacesMessages.instance().add(Severity.INFO, "O registro de pedido de vista foi realizado com sucesso para o(s) processo(s): " + StringUtils.join(sucesso, ", "));
				}
	
				if (!erro.isEmpty()) {
					FacesMessages.instance().add(Severity.ERROR, "Erro ao registrar o de pedido de vista para o(s) processo(s): " + StringUtils.join(erro, ", "));	
				}
				
				if (finalizar) {
					listaSCO = new ArrayList<SessaoPautaProcessoTrf>();
					mostraPedidoVista = false;
					orgaoJulgadorPediuVista = null;		
				}
			} else {
				FacesMessages.instance().add(Severity.ERROR, "O cargo do órgão responsável pelo pedido de vista não foi informado");
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, "O órgão responsável pelo pedido de vista não foi informado");
		}
		return null;
	}

	/**
	 * Regista o pedido de vista e adiciona mensagem de erro ou sucesso
	 * @param sucesso Lista de mensagens de sucesso
	 * @param erro Lista de mensagens de erro
	 */
	private void registraPedidoVistaPautaProcesso(List<String> sucesso, List<String> erro) {
		for(SessaoPautaProcessoTrf sppt :listaSCO){				
			try {
				ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).registrarPedidoVista(sppt, getOrgaoJulgadorPediuVista(), getOrgaoJulgadorCargoPediuVista());
				sucesso.add(sppt.getProcessoTrf().getNumeroProcesso());
			}
			catch (Exception e) {
				erro.add(sppt.getProcessoTrf().getNumeroProcesso() + " - " + e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Retira o pedido de vista e adiciona mensagem de erro ou sucesso
	 * @author rafaelmatos
	 * @param sucesso Lista de mensagens de sucesso
	 * @param erro Lista de mensagens de erro
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20149
	 * @since 26/05/2015
	 */
	public void retiraPedidoVistaPautaProcesso() {
		
		if (listaSCO.size() == 0) {
			setMsgListaSco("No mínimo um processo deve ser selecionado.");
			setMsgErro(true);
		} else{
			List<String> sucesso = new ArrayList<String>();
			List<String> erro = new ArrayList<String>();
			
			for(SessaoPautaProcessoTrf sppt :listaSCO){				
				try {
					ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).retiraPedidoVista(sppt);
					sucesso.add(sppt.getProcessoTrf().getNumeroProcesso());
				}
				catch (Exception e) {
					erro.add(sppt.getProcessoTrf().getNumeroProcesso() + " - " + e.getLocalizedMessage());
				}
			}

			if (!sucesso.isEmpty()) {
				FacesMessages.instance().add(Severity.INFO, "O registro de pedido de vista foi retirado com sucesso para o(s) processo(s): " + StringUtils.join(sucesso, ", "));
			}

			if (!erro.isEmpty()) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao retirar o de pedido de vista para o(s) processo(s): " + StringUtils.join(erro, ", "));	
			}
		
			listaSCO = new ArrayList<SessaoPautaProcessoTrf>();
			mostraPedidoVista = false;
			orgaoJulgadorPediuVista = null;	
		}
		
	}
	
	/**
	 * Grava a decisão do julgamento (Proclamação de Julgamento) do gabinete vencedor.
	 * @param finalizar Informa se o sistema deve finalizar o registro da proclamação do julgamento
	 */
	public void gravaDecisao(boolean finalizar) {
		if (!isNullGabineteVencedor()) {
			List<String> sucesso = new ArrayList<>();
			List<String> erro = new ArrayList<>();
			
			for(SessaoPautaProcessoTrf sppt : listaSCO) {
				String numeroProcesso = sppt.getProcessoTrf().getNumeroProcesso();
				try {
					registrarDecisaoJulgamento(sppt);
					sucesso.add(numeroProcesso);
				} catch (Exception e) {
					erro.add(numeroProcesso + " - " + e.getMessage());
				}
			}
			
			if (finalizar) {
				finalizarRegistroDecisao(sucesso, erro);
			}
		}
	}

	/**
	 * Lança mensagem de sucesso e/ou erro do registro da decisão de julgamento
	 * e limpa as variaveis da proclamação de julgamento.
	 * @param sucessos lista de mensagens de sucessos.
	 * @param erros lista de mensagens de erros.
	 */
	private void finalizarRegistroDecisao(List<String> sucessos, List<String> erros) {
		lancarMensagensGravacaoDecisao(sucessos, erros);
		limparVariaveisProclamacaoJulgamento();
	}

	/**
	 * Registra a decisão de julgamento.
	 * @param sppt processo em pauta na sessão.
	 * @throws Exception
	 */
	private void registrarDecisaoJulgamento(SessaoPautaProcessoTrf sppt) throws Exception {
		definirTextoDecisao(sppt);
		definirOrgaoJulgadorVencedor(sppt);
		ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).registrarJulgamento(sppt);
	}

	/**
	 * Defini o texto da decisão do julgamento.
	 * @param sessaoPautaProcessoTrf 
	 */
	private void definirTextoDecisao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		if(isDefinirTextoDecisaoLote()) {
			definirTextoDecisaoLote(sessaoPautaProcessoTrf);
		} else {
			sessaoPautaProcessoTrf.setProclamacaoDecisao(getProclamacaoDecisao());
			sessaoPautaProcessoTrf.setJulgamentoEnum(getJulgamentoEnum());
		}
	}

	/**
	 * Verifica se a definição do texto será em lote e se não deve 
	 * utilizar um novo texto para todos os processos selecionados.
	 * @return true se a movimentação for em lote e se não for utilizado 
	 * um novo texto para todos os processos, caso contrario, retorna false.
	 */
	private boolean isDefinirTextoDecisaoLote() {
		return isMovimentacaoEmLote() && !utilizarNovoTxtTodosProcessos;
	}
	
	/**
	 * Define o orgão julgador vencedor do processo em pauta na sessão.
	 * @param sessaoPautaProcesso processo em pauta na sessão.
	 */
	private void definirOrgaoJulgadorVencedor(SessaoPautaProcessoTrf sessaoPautaProcesso) {
		OrgaoJulgador ojVencedor = getOrgaoJulgadorVencedor();
		if(isDefinirOrgaoJulgadorVencedor()) {
			ojVencedor = sessaoPautaProcesso.getProcessoTrf().getOrgaoJulgador();
		}
		sessaoPautaProcesso.setOrgaoJulgadorVencedor(ojVencedor);
	}

	/**
	 * Verifica se a é uma movimentação em lote e se o órgão 
	 * julgador vencedor será o órgão julgador do processo.
	 * @return true se a movimentação for em lote e se o id 
	 * do orgão julgador selecionado for igual ao id da 
	 * constante ID_ORGAO_JULGADOR_RELATOR_SELECIONADO, caso
	 * contrário, retorna false.
	 */
	private boolean isDefinirOrgaoJulgadorVencedor() {
		return isMovimentacaoEmLote() && getOrgaoJulgadorVencedor() != null 
				&& getOrgaoJulgadorVencedor().getIdOrgaoJulgador() == ID_ORGAO_JULGADOR_RELATOR_SELECIONADO;
	}

	/**
	 * Defini se o texto da decisão será o do editor 
	 * ou da antecipação da proclamação de julgamento.
	 * @param sessaoPautaProcessoTrf
	 */
	private void definirTextoDecisaoLote(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		String textoProclamacaoJulgamento = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).
				recuperarTextoProclamacaoJulgamentoAntecipada(sessaoPautaProcessoTrf);
		
		if(StringUtils.isBlank(textoProclamacaoJulgamento)) {
			textoProclamacaoJulgamento = getProclamacaoDecisao();
		}
		
		sessaoPautaProcessoTrf.setProclamacaoDecisao(StringUtil.normalize(textoProclamacaoJulgamento));
	}
	
	/**
	 * Lança mensagens de sucessos e erros que ocorreram na gravação da decisão.
	 * @param sucessos lista de mensagens de sucessos.
	 * @param erros lista de mensagens de erros.
	 */
	private void lancarMensagensGravacaoDecisao(List<String> sucessos, List<String> erros) {
		if (!sucessos.isEmpty()) {
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "sessaoPautaProcessoTrfHome.julgamentoGravadoComSucesso", StringUtils.join(sucessos, ", "));
		}
		if (!erros.isEmpty()) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessaoPautaProcessoTrfHome.erroGravarJulgamento", StringUtils.join(erros, ", "));	
		}
	}

	/**
	 * Reseta os valores das variaveis de instancias relacionadas
	 * a proclamação de julgamento para os valores padrões.
	 */
	private void limparVariaveisProclamacaoJulgamento() {
		listaSCO = new ArrayList<SessaoPautaProcessoTrf>();
		mostraJulgado = Boolean.FALSE;
		orgaoJulgadorVencedor = null;
		proclamacaoDecisao = null;
		julgamentoEnum = JulgamentoEnum.M;
	}

	public void setSessaoPautaProcessoTrfIdSessaoPautaProcessoTrf(Integer id) {
		setId(id);
	}

	public Integer getSessaoPautaProcessoTrfIdSessaoPautaProcessoTrf() {
		return (Integer) getId();
	}

	@Override
	public String remove(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		if ((sessaoPautaProcessoTrf.getSessao().getDataFechamentoPauta() == null)
				|| (sessaoPautaProcessoTrf.getSessao().getDataFechamentoPauta().before(new Date()))) {
			sessaoPautaProcessoTrf.setDataExclusaoProcessoTrf(new Date());
			getEntityManager().merge(sessaoPautaProcessoTrf);
			getEntityManager().flush();
		} else {
			ProcessoEvento processoEvento = new ProcessoEvento();
			processoEvento.setEvento(new ParametroUtil().getEventoRetiradoPauta());
			processoEvento.setUsuario(Authenticator.getUsuarioLogado());
			processoEvento.setDataAtualizacao(new Date());
			getEntityManager().persist(processoEvento);
			getEntityManager().flush();

			sessaoPautaProcessoTrf.setDataExclusaoProcessoTrf(new Date());
			sessaoPautaProcessoTrf.setUsuarioExclusao(Authenticator.getUsuarioLogado());
			update();
		}
		return "removed";
	}

	public TipoInclusaoEnum[] getTipoInclusaoEnumValues() {
		return TipoInclusaoEnum.values();
	}

	public AdiadoVistaEnum[] getAdiadoVisitaEnumValues() {
		return AdiadoVistaEnum.values();
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}

	public void setListProcesso(List<ConsultaProcessoAdiadoVista> listProcesso) {
		this.listProcesso = listProcesso;
	}

	public List<ConsultaProcessoAdiadoVista> getListProcesso() {
		return listProcesso;
	}

	public List<ConsultaProcessoAdiadoVista> getListProcessoAdiadoPedidoVista() {
		return listProcessoAdiadoPedidoVista;
	}

	public void setListProcessoAdiadoPedidoVista(List<ConsultaProcessoAdiadoVista> listProcessoAdiadoPedidoVista) {
		this.listProcessoAdiadoPedidoVista = listProcessoAdiadoPedidoVista;
	}

	public void criarLista(ConsultaProcessoAdiadoVista obj) {
		if (obj.getCheck()) {
			listProcesso.add(obj);
		} else {
			listProcesso.remove(obj);
		}
	}
	
	public void criarListaAdiadosPedidoVista(ConsultaProcessoAdiadoVista obj) {
		if (obj.getCheck()) {
			listProcessoAdiadoPedidoVista.add(obj);
		} else {
			listProcessoAdiadoPedidoVista.remove(obj);
		}
	}

	public void checkAll(String tipoInclusao) {
		if (tipoInclusao.equals("AD")) {
			ProcessoAdiadoVistaList gridQuery = ComponentUtil.getComponent(ProcessoAdiadoVistaList.class);

			for (ConsultaProcessoAdiadoVista processoAdiadoVista : gridQuery.list()) {
				if (!BooleanUtils.isTrue(processoAdiadoVista.getProcessoTrf().getExigeRevisor())
						|| ProcessoTrfHome.instance().isRevisor(processoAdiadoVista.getProcessoTrf())) {
					processoAdiadoVista.setCheck(checkBox);
					criarLista(processoAdiadoVista);
				}
			}
		} else if (tipoInclusao.equals("PV")) {
			ProcessoAdiadoVistaPVList gridQuery = ComponentUtil.getComponent(ProcessoAdiadoVistaPVList.class);
			List<ConsultaProcessoAdiadoVista> processosAdiados = gridQuery.list();
			if(getOpcaoInclusaoPauta().equals(TODOS_PROCESSOS_PEDIDO_VISTA_APRECIADO)){
				removeProcessosComVistaNaoApreciada(processosAdiados);
			} else if (getOpcaoInclusaoPauta().equals(TODOS_PROCESSOS_PEDIDO_VISTA_NAO_APRECIADA)) {
				processosAdiados.removeAll(processosPedidoVista(true));
			}
			for (ConsultaProcessoAdiadoVista processoAdiadoVistaPV : processosAdiados) {
				processoAdiadoVistaPV.setCheck(checkBox);
				criarListaAdiadosPedidoVista(processoAdiadoVistaPV);
			}
			
		}
	}

	public void marcarTodosAptosPublicacao(){
		if(!mapaPublicacao.isEmpty()){
			if(!checkAllAPBL){
				this.mapaPublicacao.replaceAll((sessaoPautaProcessoTrf, check) -> check=true);
				setCheckAllAPBL(true);
			}else{
				this.mapaPublicacao.replaceAll((sessaoPautaProcessoTrf, check) -> check=false);
				setCheckAllAPBL(false);
			}
		}
	}
	
	private void removeProcessosComVistaNaoApreciada(List<ConsultaProcessoAdiadoVista> processosAdiados){
		processosAdiados.removeAll(processosMarcadosComFluxoVistaAberto());
	}

	public void setTipoInclusao(String tipo) {
		if (tipo.equals("PV")) {
			tipoInclusaoEnum = TipoInclusaoEnum.PV;
		} else {
			tipoInclusaoEnum = TipoInclusaoEnum.AD;
		}

	}

	public  Boolean verificaProcessoRelacao(ProcessoTrf processoTrf,Sessao sessao,boolean lock){
		if(lock){
			getEntityManager().lock(sessao, LockModeType.PESSIMISTIC_WRITE);
			getEntityManager().refresh(sessao);
		}
		return verificaProcessoRelacao(processoTrf,sessao);
	}

	private Boolean verificaProcessoRelacao(ProcessoTrf processoTrf,Sessao sessao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from SessaoPautaProcessoTrf o where ");
		sb.append("o.processoTrf = :processo ");
		sb.append("and o.sessao = :sessao ");
		sb.append("and o.dataExclusaoProcessoTrf = null");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processo", processoTrf);
		q.setParameter("sessao", sessao);

		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	
		/*
	 * [PJEII-3840] Rodrigo S. Menezes: Somente permitir a finalização de uma sessão de julgamento na tela 
	 * do Painel do Secretário da Sessão após a elaboração/assinatura de todos documentos 
	 * (relatório/voto/ementa/acórdão) dos processos nela julgados.
	 */
	/**
	 * Este método é responsável por retornar os documentos de um processo em julgamento na sessão de um determinado
	 * órgão julgador
	 * 
	 * @param processoTrf
	 * @param sessao
	 * @param orgaoJulgador
	 * @return Lista de documentos
	 */
	@SuppressWarnings("unchecked")
	private List<SessaoProcessoDocumento> getDocumentosSessaoOj(ProcessoTrf processoTrf, Sessao sessao, OrgaoJulgador orgaoJulgador)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumento o where ");
		sb.append("o.processoDocumento.processo.idProcesso = :idProcesso ");
		sb.append("and o.sessao.idSessao = :idSessao ");
		sb.append("and o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		sb.append("and o.processoDocumento.ativo = true ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcesso", processoTrf.getIdProcessoTrf());
		q.setParameter("idSessao", sessao.getIdSessao());
		q.setParameter("idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador());
		
		List<SessaoProcessoDocumento> lista = q.getResultList();
		if(lista != null && !lista.isEmpty())
		{
			return lista;
		}
		
		return Collections.emptyList();

	}
	
	public void atualizarDocumentos(ProcessoTrf processoTrf) {
		atualizarDocumentos(processoTrf, true);
	}
	
	public void atualizarDocumentos(ProcessoTrf processoTrf, boolean autoFlush) {
		SessaoPautaProcessoTrf spt = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).getSessaoPautaProcessoTrfNaoJulgado(processoTrf);
		List<SessaoProcessoDocumento> spdList = new ArrayList<SessaoProcessoDocumento>(100);		
		SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoManager.class);
		// Recupera documentos incluídos antes que o processo tenha sido pautado.
		spdList.addAll(sessaoProcessoDocumentoManager.recuperaElementosJulgamento(processoTrf, null, new ProcessoDocumento[0]));
		if(spt != null){
			//Recupera documentos da sessão anterior.
			spdList.addAll(sessaoProcessoDocumentoManager.recuperaElementosJulgamento(spt.getProcessoTrf(), spt.getSessao(), new ProcessoDocumento[0]));
		}
				
		EntityManager em = getEntityManager();
		Sessao sessao = SessaoHome.instance().getInstance();
		for (SessaoProcessoDocumento spd : spdList) {
			spd.setSessao(sessao);
			em.persist(spd);
		}
		spdList.clear();
		
		if (autoFlush)
			em.flush();
	}
    
    /**
     * Grava a sessão de pauta para a lista de pedido de vista.
     */
    public void gravarSessaoPautaPedidoVista() {
        listProcesso.addAll(listProcessoAdiadoPedidoVista);

        gravarSessaoPauta();

        if (listProcesso.isEmpty()) {
            listProcessoAdiadoPedidoVista.clear();
        }
    }

	public void gravarSessaoPauta() {
		if (SessaoHome.instance().getInstance().getDataRealizacaoSessao() != null) {
			FacesMessages.instance().add(Severity.ERROR, "A sessão foi realizada. Não é possível efetuar a inclusão de processos.");
			return;
		}
		int i = 0;
		List<String> listMsg = new ArrayList<String>(0);
		for (ConsultaProcessoAdiadoVista pav : listProcesso) {
			if (verificaProcessoRelacao(pav.getProcessoTrf(),SessaoHome.instance().getInstance(),true)) {
				if (i == 0) {
					FacesMessages.instance().add(Severity.ERROR, "O(s) processo(s) já está(ão) incluso(s) na Relação de Julgamento.");
					FacesMessages.instance().add(Severity.INFO, pav.getProcessoTrf().getNumeroProcesso());
					i++;
				} else {
					FacesMessages.instance().add(Severity.INFO, pav.getProcessoTrf().getNumeroProcesso());
				}
			} else {
				if ((BooleanUtils.isTrue(pav.getProcessoTrf().getExigeRevisor()) || 
						SimNaoFacultativoEnum.S.equals(pav.getProcessoTrf().getClasseJudicial().getExigeRevisor())) && 
							pav.getProcessoTrf().getOrgaoJulgadorRevisor() == null) {
					
					pav.setCheck(false);
					listMsg.add(String.format(" - %s: %s", pav.getProcessoTrf().getProcesso().getNumeroProcesso(), 
							Messages.instance().get("sessaoPautaProcessoTrf.erro.processoSemRevisor")));
					
				} else if ((BooleanUtils.isTrue(pav.getProcessoTrf().getExigeRevisor()) && pav.getProcessoTrf().getRevisado()) || 
						!BooleanUtils.isTrue(pav.getProcessoTrf().getExigeRevisor())) {
					try {
						SessaoPautaProcessoTrf sessaoPautaProcesso = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).pautarProcessoQueFoiAdiadoOuTevePedidoVista(
								SessaoHome.instance().getInstance(), pav.getProcessoTrf(), getTipoInclusaoEnum(), pav.getRetiradoJulgamento());
						sessaoPautaProcesso.setConsultaProcessoTrf(sessaoPautaProcesso.getConsultaProcessoTrf());
					}
					catch (Exception e) {
						FacesMessages.instance().add(Severity.ERROR, "Não foi possível incluir o processo, mensagem interna: " + e.getMessage());
					}
				} else {
					pav.setCheck(false);
					listMsg.add(String.format(" - %s: %s", pav.getProcessoTrf().getProcesso().getNumeroProcesso(), 
							Messages.instance().get("sessaoPautaProcessoTrf.erro.processoNaoRevisado")));
				}
			}
		}
		if (listMsg.size() > 0) {
			FacesMessages.instance().add(Severity.ERROR, "Os seguinte processos não foram incluídos:");
			for (String string : listMsg) {
				FacesMessages.instance().add(Severity.ERROR, string);
			}
		} else {
			FacesMessages.instance().add(Severity.INFO, "Processo(s) incluído(s) em pauta com sucesso.");
		}
		setCheckBox(false);
		listProcesso.clear();
	}

	public void addSessaoPauta(ProcessoTrf processoTrf) {
		SessaoPautaProcessoTrf sessao = new SessaoPautaProcessoTrf();
		sessao.setProcessoTrf(processoTrf);
		sessao.setUsuarioInclusao(Authenticator.getUsuarioLogado());
		sessao.setDataInclusaoProcessoTrf(new Date());
		sessao.setSustentacaoOral(Boolean.FALSE);
		sessao.setTipoInclusao(TipoInclusaoEnum.PA);
		sessao.setPreferencia(Boolean.FALSE);
		sessao.setRetiradaJulgamento(Boolean.FALSE);
		sessao.setSessao(SessaoHome.instance().getInstance());
		getEntityManager().persist(sessao);
		EntityUtil.flush();
	}

	public void upDateSessaoPauta(SessaoPautaProcessoTrf sessao) {
		sessao.setDataExclusaoProcessoTrf(new Date());
		sessao.setUsuarioExclusao(Authenticator.getUsuarioLogado());
		getEntityManager().merge(sessao);
		EntityUtil.flush();
	}

	public String getAssuntoTrf(SessaoPautaProcessoTrf obj) {
		String outPut = obj.getProcessoTrf().getProcessoAssuntoList().toString();
		return outPut;
	}

	@SuppressWarnings("unchecked")
	public String getGabinete(SessaoPautaProcessoTrf obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select a.orgaoJulgador from SessaoComposicaoOrdem a ");
		sb.append("where a.sessao.idSessao = :idSessao and a.sessao.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado ");
		sb.append("in (select o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado from ");
		sb.append("OrgaoJulgadorColegiadoOrgaoJulgador o where o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = ");
		sb.append("a.sessao.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado)");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("idSessao", obj.getSessao().getIdSessao());
		query.setMaxResults(1);
		List<OrgaoJulgador> resultList = query.getResultList();
		if (resultList.size() > 0)
			return resultList.get(0).toString();
		return null;
	}

	public void setTipoInclusaoEnum(TipoInclusaoEnum tipoInclusaoEnum) {
		this.tipoInclusaoEnum = tipoInclusaoEnum;
	}

	public TipoInclusaoEnum getTipoInclusaoEnum() {
		return tipoInclusaoEnum;
	}

	@SuppressWarnings("unchecked")
	public List<SessaoProcessoDocumentoVoto> getSessaoProcessoDocumentoVoto() {
		StringBuilder sql = new StringBuilder("select o from SessaoProcessoDocumentoVoto o ");
		sql.append("where o.sessao = :sessao");

		Query q = getEntityManager().createQuery(sql.toString());
		q.setParameter("sessao", getInstance().getSessao());

		List<SessaoProcessoDocumentoVoto> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista;
		}
		return Collections.EMPTY_LIST;
	}

	public ProcessoDocumento getProcessoDocumentoAcordao() {
		Usuario usuarioLogado = (Usuario) this.getSessionContext().get("usuarioLogado");
		if (usuarioLogado.equals(getSessaoProcessoDocumentoVoto().get(0).getProcessoDocumento().getUsuarioInclusao())) {
			return getSessaoProcessoDocumentoVoto().get(0).getProcessoDocumento();
		} else
			return null;
	}

	public void setModeloDocumentoBin(String modeloDocumentoBin) {
		this.modeloDocumentoBin = modeloDocumentoBin;
	}

	public String getModeloDocumentoBin() {
		return modeloDocumentoBin;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public void setVencedor(PessoaMagistrado vencedor) {
		this.vencedor = vencedor;
	}

	public PessoaMagistrado getVencedor() {
		return vencedor;
	}

	public void setListaSCO(List<SessaoPautaProcessoTrf> listaSCO) {
		this.listaSCO = listaSCO;
	}

	public List<SessaoPautaProcessoTrf> getListaSCO() {
		return listaSCO;
	}

	public List<OrgaoJulgador> getListOrgaoJulgadorAcompanhado() {
		return listOrgaoJulgadorAcompanhado;
	}

	public void setListOrgaoJulgadorAcompanhado(List<OrgaoJulgador> listOrgaoJulgadorAcompanhado) {
		this.listOrgaoJulgadorAcompanhado = listOrgaoJulgadorAcompanhado;
	}

	public void setMostraPlacar(Boolean mostraPlacar) {
		this.mostraPlacar = mostraPlacar;
	}

	public Boolean getMostraPlacar() {
		return mostraPlacar;
	}

	public void setMostraJulgado(Boolean mostraJulgado) {
		this.mostraJulgado = mostraJulgado;
	}

	public Boolean getMostraJulgado() {
		return mostraJulgado;
	}
	
	public Boolean getMostraVotacaoLote() {
		return mostraVotacaoLote;
	}

	public void setMostraVotacaoLote(Boolean mostraVotacaoLote) {
		this.mostraVotacaoLote = mostraVotacaoLote;
	}

	public void setOrgaoJulgadorVencedor(OrgaoJulgador orgaoJulgadorVencedor) {
		this.orgaoJulgadorVencedor = orgaoJulgadorVencedor;
	}

	public OrgaoJulgador getOrgaoJulgadorVencedor() {
		return orgaoJulgadorVencedor;
	}

	public void setProclamacaoDecisao(String proclamacaoDecisao) {
		this.proclamacaoDecisao = proclamacaoDecisao;
	}

	public String getProclamacaoDecisao() {
		return proclamacaoDecisao;
	}

	public void setMostraSustentacaoOral(Boolean mostraSustentacaoOral) {
		this.mostraSustentacaoOral = mostraSustentacaoOral;
	}

	public Boolean getMostraSustentacaoOral() {
		return mostraSustentacaoOral;
	}

	public void setAdvogadoSustentacaoOral(String advogadoSustentacaoOral) {
		this.advogadoSustentacaoOral = advogadoSustentacaoOral;
	}

	public String getAdvogadoSustentacaoOral() {
		if (listaSCO != null && listaSCO.size() > 0) {
			return listaSCO.get(0).getAdvogadoSustentacaoOral();
		} else {
			return "";
		}
	}

	public Integer quantidadeProcessosRetiradoJulgamento() {
		int quant = 0;
		for (SessaoPautaProcessoTrf sessao : getListaSessao()) {
			if ((sessao.getAdiadoVista() != null) && (sessao.getAdiadoVista().equals(AdiadoVistaEnum.AD))
					&& (sessao.getRetiradaJulgamento())) {
				quant++;
			}
		}
		return quant;
	}

	@SuppressWarnings("unchecked")
	public String getRevisor(SessaoPautaProcessoTrf obj) {
		StringBuilder sb = new StringBuilder("select o.orgaoJulgadorRevisor from SessaoComposicaoOrdem o ");
		sb.append("where o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		sb.append("and o.sessao.idSessao = :idSessao");

		Query query = getEntityManager().createQuery(sb.toString());

		if (obj.getProcessoTrf().getOrgaoJulgador() != null) {
			query.setParameter("idOrgaoJulgador", obj.getProcessoTrf().getOrgaoJulgador().getIdOrgaoJulgador());
		} else {
			query.setParameter("idOrgaoJulgador", 0);
		}

		query.setParameter("idSessao", obj.getSessao().getIdSessao());
		query.setMaxResults(1);
		
		List<OrgaoJulgador> resultList = query.getResultList();
		if (resultList.size() > 0) {
			return resultList.get(0).getOrgaoJulgador();
		} else {
			return null;
		}
	}

	public void setListVoto(List<SessaoPautaProcessoTrf> listVoto) {
		this.listVoto = listVoto;
	}

	public List<SessaoPautaProcessoTrf> getListVoto() {
		return listVoto;
	}

	public void limparLista() {
		for (SessaoPautaProcessoTrf sessao : getListVoto()) {
			sessao.setCheck(Boolean.FALSE);
		}
		getListVoto().clear();
	}

	@SuppressWarnings("unchecked")
	private List<SessaoPautaProcessoTrf> getListaSessao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoPautaProcessoTrf o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("  and o.dataExclusaoProcessoTrf is null ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", SessaoHome.instance().getInstance());

		return q.getResultList();
	}

	public Integer quantidadeProcessosSessao() {
		return getListaSessao().size();
	}

	public Integer quantidadeProcessosJulgados() {
		int quant = 0;
		for (SessaoPautaProcessoTrf sessao : getListaSessao()) {
			if (sessao.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
				quant++;
			}
		}
		return quant;
	}

	public Integer quantidadeProcessosPedidoVista() {
		int quant = 0;
		for (SessaoPautaProcessoTrf sessao : getListaSessao()) {
			if ((sessao.getAdiadoVista() != null) && (sessao.getAdiadoVista().equals(AdiadoVistaEnum.PV))) {
				quant++;
			}
		}
		return quant;
	}

	public Integer quantidadeProcessosAdiado() {
		int quant = 0;
		for (SessaoPautaProcessoTrf sessao : getListaSessao()) {
			if ((sessao.getAdiadoVista() != null) && (sessao.getAdiadoVista().equals(AdiadoVistaEnum.AD))
					&& (!sessao.getRetiradaJulgamento())) {
				quant++;
			}
		}
		return quant;
	}

	public void setMostraPedidoVista(Boolean mostraPedidoVista) {
		this.mostraPedidoVista = mostraPedidoVista;
	}

	public Boolean getMostraPedidoVista() {
		return mostraPedidoVista;
	}

	public void setTituloModalProcessoDecisao(String tituloModalProcessoDecisao) {
		this.tituloModalProcessoDecisao = tituloModalProcessoDecisao;
	}

	public String getTituloModalProcessoDecisao() {
		return tituloModalProcessoDecisao;
	}

	public TipoInclusaoEnum[] getTipoInclusaoValues() {
		return TipoInclusaoEnum.values();
	}
	
	public SessaoResultadoVotacaoEnum[] getSessaoResultadoVotacaoValues() {
		return SessaoResultadoVotacaoEnum.values();
	}
	
	public List<TipoVoto> getTipoVotoItensRelator(){
		TipoVotoManager tipoVotoManager = ComponentUtil.getComponent(TipoVotoManager.class);
		return tipoVotoManager.listTipoVotoAtivoComRelator();
	}


	public void limparListaSCO() {
		setListaSCO(new ArrayList<SessaoPautaProcessoTrf>());
	}

	public Boolean getCheckAll() {
		return checkAll;
	}

	public void setCheckAll(Boolean checkAll) {
		setCheckAllAlternativo(checkAll);

		listaSCO.clear();
		if (checkAll) {
			SessaoPautaSecretarioProcessoNuloList list = ComponentUtil.getComponent(SessaoPautaSecretarioProcessoNuloList.class);
			for (SessaoPautaProcessoTrf sppt : list.list()) {
				if (!sppt.isJulgamentoFinalizado() && !sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
					listaSCO.add(sppt);
				}
			}
		}
	}
	
	public void setCheckAllAlternativo (Boolean checkAll) {
		this.checkAll = checkAll;
	}

	public Boolean verificaListaSco(String action) {
		
		

		if (listaSCO.size() == 0) {
			setMsgListaSco("No mínimo um processo deve ser selecionado.");
			return Boolean.FALSE;
		} 
		
		if (listaSCO.size() > 1) {
			if (action.equals("sustentacaoOral")) {
				setMsgListaSco("Só poderá informar o pedido de sustentação oral para um processo por vez.");
				return Boolean.FALSE;
			}
			if (action.equals("anotacao")) {
				setMsgListaSco("Só poderá realizar a anotação de um processo por vez.");
				return Boolean.FALSE;
			}			
		}

		if (action.equals("votacaoLote") && !validaMesmaRelatoriaProcessos()) {
			setMsgListaSco("Todos processos selecionados devem possuir o mesmo relator.");
			return Boolean.FALSE;
		}
		
		if (action.equals("julgado")) {	
			String mensagemValidacao = ComponentUtil.getSessaoPautaProcessoTrfManager().verificarBloqueioRegistroJulgamentoSemVoto(listaSCO);
			if(StringUtil.isNotEmpty(mensagemValidacao)) {				
				setMsgListaSco(mensagemValidacao);
				return Boolean.FALSE;
			}

		}
			
		setMsgListaSco(null);
		return Boolean.TRUE;
	}
	



	public void acaoBtnLegenda(String action,SessaoPautaProcessoTrf sessaoPPT) {
		listaSCO.clear();
		if(!sessaoPPT.isJulgamentoFinalizado()) {
			listaSCO.add(sessaoPPT);
			acaoBtnLegenda(action);
			if (!action.equals("julgado") && !action.equals("julgadolote") && !action.equals("sustentacaoOral") && !action.equals("pedidoVista") && !action.equals("anotacao")) {
				listaSCO.clear();
			}
		}
	}
	
	public void acaoBtnLegenda(String action) {
		if (!verificaListaSco(action)) {
			setMsgErro(Boolean.TRUE);
			return;
		}
		ComponentUtil.getPainelSessaoSecretarioSessaoAction().marcarAtualizacoes();
		verificaAcaoProcesso(action);
	}
	
	/**
	 * Metodo que altera preferencia do processo em lote (verdadeiro, falso) utilizado no menu hamburguer
	 * @author rafaelmatos
	 * @link http://www.cnj.jus.br/jira/browse/PJEII-20149
	 * @since 26/05/2015
	 */
	public void marcarPreferenciaEmLote(Boolean status) {

		if (listaSCO != null && listaSCO.size() > 0) {

			for (SessaoPautaProcessoTrf sppt : listaSCO) {
				try {
					sppt.setPreferencia(status);
					setInstance(sppt);
					super.update();
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, "A situação do processo: {0} foi alterada com sucesso!",
							sppt.getProcessoTrf().getNumeroProcesso());
					ComponentUtil.getPainelSessaoSecretarioSessaoAction().marcarAtualizacoes();
				} catch (Exception e) {
					FacesMessages.instance().add(Severity.ERROR,
							"Erro ao alterar a situação do processo: {0} da pauta, mensagem interna: {1}",
							sppt.getProcessoTrf().getNumeroProcesso(), e.getLocalizedMessage());
				}
			}
			listaSCO.clear();
		} else {
			setMsgListaSco("No mínimo um processo deve ser selecionado.");
		}
	}

	public String getSustentacaoOral(Integer idProcessoTrfSO) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.advogadoSustentacaoOral from SessaoPautaProcessoTrf o where  ");
		sb.append("o.sessao = :sessao and o.processoTrf.idProcessoTrf = :idSO");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("sessao", SessaoHome.instance().getInstance());
		q.setParameter("idSO", idProcessoTrfSO);
		resultSustentacaoOral = ((String) q.getSingleResult());
		return resultSustentacaoOral;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getPedidoVista(Integer idSPPT) {
		StringBuilder sql = new StringBuilder();
		sql.append("select o.orgaoJulgadorPedidoVista from SessaoPautaProcessoTrf o where ");
		sql.append("o.idSessaoPautaProcessoTrf = :idSPPT");
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql.toString());
		query.setParameter("idSPPT", idSPPT);
		setResultListPedidoVista(query.getResultList());
		return resultListPedidoVista;
	}

	public Boolean existeAnotacao(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from NotaSessaoJulgamento o where ");
		sb.append("o.processoTrf.idProcessoTrf = :processo ");
		sb.append("and o.sessao.idSessao = :sessao");
		Query q = getEntityManager().createQuery(sb.toString())
				.setParameter("processo", processoTrf.getIdProcessoTrf())
				.setParameter("sessao", SessaoHome.instance().getInstance().getIdSessao());

		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void setMsgListaSco(String msgListaSco) {
		this.msgListaSco = msgListaSco;
	}

	public String getMsgListaSco() {
		return msgListaSco;
	}

	public Boolean getMsgErro() {
		return msgErro;
	}

	public void setMsgErro(Boolean msgErro) {
		this.msgErro = msgErro;
	}

	public void setMsgInfo(String msgInfo) {
		this.msgInfo = msgInfo;
	}

	public String getMsgInfo() {
		return msgInfo;
	}

	public void setPd(ProcessoDocumento pd) {
		this.pd = pd;
	}

	public ProcessoDocumento getPd() {
		return pd;
	}

	public void setResultListPedidoVista(List<OrgaoJulgador> resultListPedidoVista) {
		this.resultListPedidoVista = resultListPedidoVista;
	}

	public List<OrgaoJulgador> getResultListPedidoVista() {
		return resultListPedidoVista;
	}

	public void setResultSustentacaoOral(String resultSustentacaoOral) {
		this.resultSustentacaoOral = resultSustentacaoOral;
	}

	public String getResultSustentacaoOral() {
		return resultSustentacaoOral;
	}

	
	public void setMostrarNota(Boolean mostrarNota,SessaoPautaProcessoTrf processoSessao) {
		listaSCO.clear();
		if(!processoSessao.isJulgamentoFinalizado()) {
			listaSCO.add(processoSessao);
			setMostrarNota(mostrarNota);
		
			// limpa a instncia para evitar carregamento dos registros de operaes anteriores.
			NotaSessaoJulgamentoHome notaSessaoJulgamentoHome = ComponentUtil
								.getComponent("notaSessaoJulgamentoHome");
			if (notaSessaoJulgamentoHome != null) {
				notaSessaoJulgamentoHome.newInstance();
			}
		}
	}
	
	public void setMostrarNota(Boolean mostrarNota) {
		this.mostrarNota = mostrarNota;
	}

	public Boolean getMostrarNota() {
		return mostrarNota;
	}

	public Boolean existeJulgado() {
		SessaoPautaSecretarioProcessoNuloList list = ComponentUtil.getComponent(SessaoPautaSecretarioProcessoNuloList.class);
		for (SessaoPautaProcessoTrf sppt : list.list()) {
			if (sppt.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public Boolean getCheckAllAIPDR() {
		return checkAllAIPDR;
	}

	public void setCheckAllAIPDR(Boolean checkAllAIPDR) {
		this.checkAllAIPDR = checkAllAIPDR;
		getListProcessoTrf().clear();
		if (checkAllAIPDR) {
			SessaoPautaAptosParaInclusaoPautaDemaisRelatoresList list = ComponentUtil.getComponent(SessaoPautaAptosParaInclusaoPautaDemaisRelatoresList.class);
			for (ProcessoTrf sppt : list.list()) {
				getListProcessoTrf().add(sppt);
			}
		}
	}

	public Boolean getCheckAllAPBL() {
		return checkAllAPBL;
	}

	public void setCheckAllAPBL(Boolean checkAllAPBL) {
		this.checkAllAPBL = checkAllAPBL;
	}

	private void insereRelacaoJulgamento(ProcessoTrf processo) {
		// atualização dos documentos antes de inserir o processo na relação de
		// julgamento
		SessaoProcessoDocumentoVoto votoDoRelator = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).recuperarVotoAntecipado(processo, processo.getOrgaoJulgador());
		atualizarDocumentos(processo);
		SessaoPautaProcessoTrf spd = new SessaoPautaProcessoTrf();
		spd.setSessao(SessaoHome.instance().getInstance());
		spd.setProcessoTrf(processo);
		spd.setUsuarioInclusao(Authenticator.getUsuarioLogado());
		spd.setOrgaoJulgadorUsuarioInclusao(Authenticator.getOrgaoJulgadorAtual());
		spd.setDataInclusaoProcessoTrf(new Date());
		spd.setTipoInclusao(TipoInclusaoEnum.PA);
		spd.setSustentacaoOral(false);
		spd.setPreferencia(false);
		spd.setRetiradaJulgamento(false);
		spd.setSituacaoJulgamento(TipoSituacaoPautaEnum.AJ);
		if (votoDoRelator != null && StringUtils.isNotBlank(votoDoRelator.getTextoProclamacaoJulgamento())) {
			spd.setProclamacaoDecisao(StringUtil.removeHtmlTags(votoDoRelator.getTextoProclamacaoJulgamento()));
		}
		getEntityManager().persist(spd);
		getEntityManager().flush();
	}

	public void gravarAIPDR() {
		if (SessaoHome.instance().getInstance().getDataRealizacaoSessao() != null) {
			FacesMessages.instance().add(Severity.ERROR,
					"A sessão foi realizada. Não é possível efetuar a inclusão de processos.");
			return;
		}
		for (ProcessoTrf processoTrf : listProcessoTrf) {
			insereRelacaoJulgamento(processoTrf);
		}
		listProcessoTrf.clear();
	}

	public String addListalistProcessoTrf(ProcessoTrf row) {
		if (getListProcessoTrf().contains(row)) {
			getListProcessoTrf().remove(row);
		} else {
			getListProcessoTrf().add(row);
		}
		setMostraPlacar(Boolean.FALSE);
		return null;
	}

	public Boolean exibirAbaAptosRelatores() {
		if (SessaoHome.instance().getInstance().getOrgaoJulgadorColegiado().getPresidenteRelacao()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(o) from SessaoComposicaoOrdem o ");
			sb.append("where o.sessao = :sessao ");
			sb.append("and (o.orgaoJulgador = :oj ");
			sb.append("		and o.presidente = true) ");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("sessao", SessaoHome.instance().getInstance());
			q.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
			try {
				Long retorno = (Long) q.getSingleResult();
				return retorno > 0;
			} catch (NoResultException no) {
				return Boolean.FALSE;
			}
		}
		return false;
	}

	public List<ProcessoTrf> getProcessosNaoRevisados() {
		return processosNaoRevisados;
	}

	public void setProcessosNaoRevisados(List<ProcessoTrf> processosNaoRevisados) {
		this.processosNaoRevisados = processosNaoRevisados;
	}

	public List<ProcessoTrf> getListProcessoTrf() {
		return listProcessoTrf;
	}

	public void setListProcessoTrf(List<ProcessoTrf> listProcessoTrf) {
		this.listProcessoTrf = listProcessoTrf;
	}

	/**
	 * Verifica se o usuário logado é relator do processo ou se pertence ao OJ
	 * do processo.
	 * 
	 * @return
	 */
	public boolean isRelator() {
		ProcessoTrfHome.instance().setInstance(getInstance().getProcessoTrf());
		if (ProcessoTrfHome.instance().getRelator(getInstance().getProcessoTrf())
				.equals(Authenticator.getUsuarioLogado())) {
			return true;

		}
		if (ProcessoTrfHome.instance().getInstance().getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual())) {
			return true;
		}
		return false;

	}
	
	public String getVotoRelator() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoProcessoDocumentoVoto o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoDocumento.processo = :processo ");
		sb.append("and o.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.processoDocumento.ativo = true ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("sessao", SessaoPautaProcessoTrfHome.instance().getInstance().getSessao());
		q.setParameter("processo", instance.getProcessoTrf().getProcesso());
		q.setParameter("orgaoJulgador", instance.getProcessoTrf().getOrgaoJulgador());
		SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto) EntityUtil.getSingleResult(q);
		if (voto != null && voto.getTipoVoto() != null) {
			return voto.getTipoVoto().getTipoVoto();
		} else {
			return null;
		}
	}
	
    /**
     * [PJEII-4330] - Utilizado pelo frame elaborarAcordao.xhtml
     * Método utilizado para iniciar a instância do Home para ser utilizada em fluxo
     */
	public void setInstanciaParaFluxo() {
        ProcessoTrf processo = ProcessoJbpmUtil.getProcessoTrf();
        SessaoPautaProcessoTrf sppt = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).getSessaoPautaProcessoTrfJulgado(processo);
        if(sppt != null){
        	SessaoProcessoDocumentoHome.instance().setSessao(sppt.getSessao());
        	SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getComponent(SessaoPautaProcessoTrfHome.class);
        	sessaoPautaProcessoTrfHome.setInstance(sppt);
        }
	}
    
	public void setIsJulgado(Boolean isJulgado) {
		this.isJulgado = isJulgado;
	}

	public Boolean getIsJulgado() {
		return isJulgado;
	}
	
	/**
	 * Verifica se o {@link SessaoPautaProcessoTrf} pode ser votado
	 * @param sppt {@link SessaoPautaProcessoTrf}
	 * @return true se pode votar
	 */
	public Boolean isPodeVotar(SessaoPautaProcessoTrf sppt){
		if(sppt != null && sppt.getSessaoPautaProcessoComposicaoList() != null && !sppt.getSessaoPautaProcessoComposicaoList().isEmpty()){
			for(SessaoPautaProcessoComposicao sppc: sppt.getSessaoPautaProcessoComposicaoList()){
				if(sppc.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual()) ){
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}
	
	public void atualizaOrdem(SessaoPautaProcessoTrf processoPautaNovo){
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().atualizaOrdem(processoPautaNovo);
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR,"Erro ao alterar a ordem da pauta: {0}", e);
			return;
		}
		
		SessaoPautaRelacaoJulgamentoList list = ComponentUtil.getComponent(SessaoPautaRelacaoJulgamentoList.class);
		list.refresh();
	}
	
	public String getNomeOrgao(Integer id){
		if(nomeOrgao == null){
			nomeOrgao = new HashMap<Integer, String>();
		}
		if(nomeOrgao.get(id) == null && id != -1 && id != -2){
			try {
				OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent(OrgaoJulgadorManager.class);
				nomeOrgao.put(id, orgaoJulgadorManager.findById(id).getOrgaoJulgador());
			} catch (PJeBusinessException e) {
				log.error("Ocorreu erro em SessaoPautaProcessoTrfHome.getNomeOrgao(): " + e.getLocalizedMessage());
			}
		}
		return nomeOrgao.get(id);
	}
	
	public Map<Integer, Set<Integer>> getPlacar(Integer idJulgamento){
		if(placares.get(idJulgamento) == null){
			try {
				SessaoPautaProcessoTrf julg = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).findById(idJulgamento);
				if(julg != null){
					SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class);					
					PlacarSessaoVO p = sessaoProcessoDocumentoVotoManager
							.getPlacarCondutores(julg.getSessao(), julg.getProcessoTrf(), julg.getSessao().getDataFechamentoPauta() != null);
					placares.put(idJulgamento,p.getMapaPlacar());
					placares.get(idJulgamento).put(-1, sessaoProcessoDocumentoVotoManager.getOmissos(julg.getSessao(), julg.getProcessoTrf(), julg.getSessao().getDataFechamentoPauta() != null));
					placares.get(idJulgamento).put(-2, sessaoProcessoDocumentoVotoManager.getImpedidos(julg.getSessao(), julg.getProcessoTrf(), julg.getSessao().getDataFechamentoPauta() != null));
					// Remove impedidos da lista de omissos
					placares.get(idJulgamento).get(-1).removeAll(placares.get(idJulgamento).get(-2));
					this.getImpedidos(julg.getSessao(), julg.getProcessoTrf()).isEmpty();
				}
			} catch (PJeBusinessException e) {
				log.error("Ocorreu erro em SessaoPautaProcessoTrfHome.getPlacar(): " + e.getLocalizedMessage());
			}
		}
		return placares.get(idJulgamento);
	}
	
	/**
	 * Recupera os Votos Omissos.
	 * @param julg
	 * @return
	 */
	public Set<Integer> getOmissos(Integer julg){
		return getPlacar(julg).get(VOTOS_OMISSOS);
	}
	
	/**
	 * Recupera os Votos Impedidos.
	 * @param julg
	 * @return
	 */
	public Set<Integer> getImpedidos(Integer julg){
		return getPlacar(julg).get(VOTOS_IMPEDIDOS);
	}
	
	/**
	 * Recupera os Votos Divergentes.
	 * @param julg
	 * @return
	 */
	public Set<Integer> getDivergentes(Integer julg){
		return getPlacar(julg).get(VOTOS_DIVERGENTES);
	}
	
	/**
	 * Recupera os Votos Não Conhece.
	 * @param julg
	 * @return
	 */
	public Set<Integer> getNaoConhece(Integer julg){
		return getPlacar(julg).get(VOTOS_NAO_CONHECE);
	}

	/**
	 * Retorna um array contendo as situações de pauta.
	 * 
	 * @return Values of SituacaoPautaEnum.
	 */
	public SituacaoPautaEnum[] getSituacaoPautaItems() {
		return SituacaoPautaEnum.values();
	}
	
	/**
	 * Retorna uma lista dos anos das sessões de julgamento.
	 * 
	 * @return Lista dos anos das sessões de julgamento.
	 */
	public List<Integer> getAnosSessaoJulgamento() {
		return ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).getAnosSessaoJulgamento();
	}

    /**
     * Retorna todas as sessões de julgamento de determinado ano ordenadas pelo apelido da sessão.
     * 
     * @param ano Representa o ano das sessões de julgamento a ser pesquisada.
     * @return Todas as sessões de julgamento de determinado ano ordenadas pelo apelido da sessão.
     */
	public List<Sessao> getSessoesJulgamento(Integer ano) {
		SessaoManager sessaoManager = ComponentUtil.getComponent(SessaoManager.class);
		return sessaoManager.getSessoesJulgamento(ano);
	}

	
	/**
	 * Consulta o processo na instância inferior para buscar a descrição do órgão julgador de origem.
	 * 
	 * @param processoTrf Representa o processo a ser pesquisado.
	 * @return A descrição do órgão julgador de origem do processo
	 */
	public String getDescricaoOrigemProcesso(ProcessoTrf processoTrf) {
		String retorno = "Não informado";
		try {
			ProcessoTrf processoJudicial = 
					ConsultaInstanciaInferiorIntercomunicacao.instance().consultarProcesso(processoTrf.getNumeroProcesso());
			if (processoJudicial != null) {
				StringBuilder descricao = new StringBuilder();
				descricao.append("Órgão julgador: ");
				descricao.append(processoJudicial.getOrgaoJulgador().getOrgaoJulgador());
				descricao.append("\nClasse judicial: ");
				String codigoClasseJudicial = String.valueOf(processoJudicial.getClasseJudicialStr());
				ClasseJudicialManager classeJudicialManager = ComponentUtil.getComponent(ClasseJudicialManager.class);
				ClasseJudicial classe =  classeJudicialManager.findByCodigo(codigoClasseJudicial);
				descricao.append(classe);
				retorno = descricao.toString();
			} 
		} catch (Exception e) {
			setMsgInfo("Não foi possível recuperar informações da instância de origem" );
			log.error("Erro na consulta do processo na instância inferior para buscar a descrição do órgão julgador de origem: " + e.getMessage());
		}
		return retorno;
	}

	/**
	 * Retorna uma lista dos nomes dos juízes que assinaram os documentos do tipo sentença de determinado processo.
	 * 
	 * @param processoTrf Representa o processo a ser pesquisado.
	 * @return Lista dos nomes dos juízes que assinaram os documentos do tipo sentença de determinado processo.
	 */
	public List<String> getNomesAssinantesSentencas(ProcessoTrf processoTrf) {
		ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getComponent(ProcessoDocumentoManager.class);
		List<ProcessoDocumento> listaDocumentos = processoDocumentoManager.getDocumentosPorTipo(
				processoTrf, ParametroUtil.instance().getTipoProcessoDocumentoSentenca().getIdTipoProcessoDocumento());
		
		HashSet<String> nomesAssinantesSentencas = new HashSet<String>(0);
		if (listaDocumentos != null) {
			ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager = ComponentUtil.getComponent(ProcessoDocumentoBinPessoaAssinaturaManager.class);
			for (ProcessoDocumento processoDocumento : listaDocumentos) {
				nomesAssinantesSentencas.add(
					processoDocumentoBinPessoaAssinaturaManager.getNomeUsuarioUltimaAssinatura(
						processoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin()));
			}			
		}
		return new ArrayList<String>(nomesAssinantesSentencas);
	}

	/**
	 * Método responsável por gerar e disponibilizar um relatório em PDF
	 * dos processos em pauta de julgamento selecionados.
	 */
	public void gerarRelatorioEmPdf() {
		final FacesContext contexto = FacesContext.getCurrentInstance();
		final String nomeArquivo = "relatorio_" + System.currentTimeMillis() + ".pdf";
		final String caminhoRelatorio = "/ProcessoPautaJulgamento/report.xhtml";
		final byte[] relatorioEmBytes = XhtmlParaPdf.converterParaBytes(caminhoRelatorio);
		
		finalizarConversationDocumentStore();
		HttpServletResponse response = (HttpServletResponse) contexto.getExternalContext().getResponse();
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "filename=" + nomeArquivo);
		response.setContentLength(relatorioEmBytes.length);
		
		ServletOutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			outputStream.write(relatorioEmBytes, 0, relatorioEmBytes.length);
			outputStream.flush();
		} catch (Exception e) {
			log.error("Ocorreu erro ao tratar stream em SessaoPautaProcessoTrfHome.gerarRelatorioEmPdf(): " + e.getLocalizedMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				log.error("Ocorreu erro ao fechar stream em SessaoPautaProcessoTrfHome.gerarRelatorioEmPdf(): " + e.getLocalizedMessage());
			}
		}
		contexto.responseComplete();
	}

	private void finalizarConversationDocumentStore() {
		Contexts.getConversationContext().remove("org.jboss.seam.document.documentStore");
	}

	/**
	 * Retorna a descrição formatada em lista dos polos ativos do processo.
	 * 
	 * @param processoTrf
	 * @return
	 */
	public String getDescricaoPessoaPoloAtivo(ProcessoTrf processoTrf) {
		return getDescricaoProcessoParte(processoTrf.getProcessoPartePoloAtivoSemAdvogadoList());
	}

	/**
	 * Retorna a descrição formatada em lista dos polos passivos do processo.
	 * 
	 * @param processoTrf
	 * @return
	 */
	public String getDescricaoPessoaPoloPassivo(ProcessoTrf processoTrf) {
		return getDescricaoProcessoParte(processoTrf.getProcessoPartePoloPassivoSemAdvogadoList());
	}

	/**
	 * Retorna a descrição formatada em lista dos terceiros interessados do processo.
	 * 
	 * @param processoTrf
	 * @return
	 */
	public String getDescricaoPessoaPoloTerceiros(ProcessoTrf processoTrf) {
		return getDescricaoProcessoParte(processoTrf.getListaParteTerceiro());
	}

	/**
	 * Retorna a descrição formatada em lista dos advogados dos polos ativos.
	 * 
	 * @param processoTrf
	 * @return
	 */
	public String getDescricaoAdvogadosPoloAtivo(ProcessoTrf processoTrf) {
		return getDescricaoAdvogados(processoTrf.getListaAdvogadosPoloAtivo());
	}

	/**
	 * Retorna a descrição formatada em lista dos advogados dos polos passivos.
	 * 
	 * @param processoTrf
	 * @return
	 */
	public String getDescricaoAdvogadosPoloPassivo(ProcessoTrf processoTrf) {
		return getDescricaoAdvogados(processoTrf.getListaAdvogadosPoloPassivo());
	}

	/**
	 * Retorna a descrição formatada (nome + oab) da lista de advogados passados como parâmetro.
	 * 
	 * @param listaAdvogados
	 * @return descricaoFormatada
	 */
	public String getDescricaoAdvogados(List<ProcessoParte> listaAdvogados) {
		final String SEPARADOR = " - ";
		final String QUEBRA_LINHA = "\n";
		
		if (listaAdvogados == null || listaAdvogados.isEmpty()) {
			return "Não informado";
		}
		
		StringBuilder descricao = new StringBuilder();
		for (ProcessoParte advogado : listaAdvogados) {
			descricao.append(advogado.getNomeParte());
			descricao.append(SEPARADOR);
			descricao.append(getOabFormatada(advogado.getPessoa()));
			descricao.append(QUEBRA_LINHA);
		}
		return descricao.toString();
	}

	/**
	 * Retorna o número de inscrição na OAB formatada de um advogado.
	 * 
	 * @param advogadoPessoa
	 * @return
	 */
	public String getOabFormatada(Pessoa advogadoPessoa) {
		if (Pessoa.instanceOf(advogadoPessoa, PessoaAdvogado.class)) {
			PessoaAdvogado advogado = ((PessoaFisica) advogadoPessoa).getPessoaAdvogado();
			return advogado.getOabFormatado();
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Retorna a descrição formatada em lista das partes passadas como parâmetro.
	 * 
	 * @param pessoas
	 * @return descricaoFormatada
	 */
	private String getDescricaoProcessoParte(List<ProcessoParte> pessoas) {
		final String QUEBRA_LINHA = "\n";
		
		if (pessoas == null || pessoas.isEmpty()) {
			return "Não informado";
		}
		StringBuilder descricao = new StringBuilder();
		for (ProcessoParte processoParte : pessoas) {
			descricao.append(processoParte.getNomeParte());
			descricao.append(QUEBRA_LINHA);
		}
		return descricao.toString();
	}

	/**
	 * Retorna a descrição formatada em lista de qualquer objeto da lista passada.
	 * 
	 * @param lista
	 * @return descricaoFormatada
	 */
	public String getDescricaoLista(List<?> lista) {
		final String QUEBRA_LINHA = "\n";
		
		StringBuilder descricao = new StringBuilder();
		if(lista == null || lista.size() == 0) {
			descricao.append("Não informado");
		} else {
			for (Object objeto : lista) {
				descricao.append(objeto);
				descricao.append(QUEBRA_LINHA);
			}
		}
		return descricao.toString();
	}

	/**
	 * Retorna a lista de OrgaoJulgador se for em lote retorna os presentes se nao retorna os votantes
	 * @return lista de OrgaoJulgador
	 */
	public List<OrgaoJulgador> getListaComposicaoDinamica() {
		if (isMovimentacaoEmLote()) {
			return listarOrgaosJulgadoresPresentes();
		} else {
			return listarOrgaoaJulgadoresVotantes();
		}
	}

	/**
	 * Recupera a lista de orgãos julgadores votantes.
	 * @return List<OrgaoJulgador> lista de orgãos julgadores.
	 */
	private List<OrgaoJulgador> listarOrgaoaJulgadoresVotantes() {
		if(listaSCO != null && !listaSCO.isEmpty()) {
			return listaSCO.get(0).getOrgaosJulgadoresVotantes();
		}
		return null;
	}

	/**
	 * Recupera a lista de orgão julgadores presentes na sessão.
	 * @return List<OrgaoJulgador> lista de orgãos julgadores.
	 */
	private List<OrgaoJulgador> listarOrgaosJulgadoresPresentes() {
		OrgaoJulgador orgaoJulgadorRelator = criarOrgaoJulgadorRelator();
		List<OrgaoJulgador> orgaosJulgadoresPresentes = obterListaOrgaosJulgadoresPresentes();
		List<OrgaoJulgador> listaOrgaosJulgadores = new ArrayList<OrgaoJulgador>();
		listaOrgaosJulgadores.add(orgaoJulgadorRelator);
		listaOrgaosJulgadores.addAll(orgaosJulgadoresPresentes);
		return listaOrgaosJulgadores;
	}

	/**
	 * Recupera a lista de orgãos julgadores presentes na sessão.
	 * @return lista de orgãos julgadores.
	 */
	private List<OrgaoJulgador> obterListaOrgaosJulgadoresPresentes() {
		return SessaoHome.instance().getInstance().getOrgaosJulgadoresPresentes();
	}

	/**
	 * Cria uma nova instancia de órgão julgador para ser adicionado na listagem 
	 * de orgãos julgadores presentes para ser escolhido como vencedor.
	 * @return orgão julgador
	 */
	private OrgaoJulgador criarOrgaoJulgadorRelator() {
		OrgaoJulgador orgaoJulgadorRelator = new OrgaoJulgador();
		orgaoJulgadorRelator.setIdOrgaoJulgador(ID_ORGAO_JULGADOR_RELATOR_SELECIONADO);
		orgaoJulgadorRelator.setOrgaoJulgador("Órgão julgador do relator");
		return orgaoJulgadorRelator;
	}

	public OrgaoJulgador getOrgaoJulgadorPediuVista() {
		return orgaoJulgadorPediuVista;
	}

	public void setOrgaoJulgadorPediuVista(OrgaoJulgador orgaoJulgadorPediuVista) {
		this.orgaoJulgadorPediuVista = orgaoJulgadorPediuVista;
		if(this.recuperaCargosTitularesOj(this.orgaoJulgadorPediuVista).size() == 1){
			setOrgaoJulgadorCargoPediuVista(this.recuperaCargosTitularesOj(this.orgaoJulgadorPediuVista).get(0));
		} else {
			setOrgaoJulgadorCargoPediuVista(null);
		}
	}
	
	public OrgaoJulgadorCargo getOrgaoJulgadorCargoPediuVista() {
		return orgaoJulgadorCargoPediuVista;
	}
	
	public void setOrgaoJulgadorCargoPediuVista(OrgaoJulgadorCargo orgaoJulgadorCargoPediuVista) {
		this.orgaoJulgadorCargoPediuVista = orgaoJulgadorCargoPediuVista;
	}
	
	public boolean isMovimentacaoEmLote() {
		return listaSCO.size() > 1;
	}

	/**
	 * Verifica se deve ou nao exibir a aba para a publicacao dos processos
	 * @see Parametros.EXIBE_PUBLICACAO_RELACAO_JULGAMENTO
	 * @return true caso o parametro pje:sessao:exibePublicacaoPauta seja 'true'
	 * @return false caso o parametro nao exista ou nao seja 'true'
	 */
	public boolean exibeAbaPublicacaoPauta() {
		ParametroService parametroService = ComponentUtil.getComponent(ParametroService.class);
		return BooleanUtils.toBoolean(parametroService.valueOf(Parametros.EXIBE_PUBLICACAO_RELACAO_JULGAMENTO));
	}
	
	/**
	 * Envia a lista de processos selecionados para publicação no DJe
	 * @see PublicadorDJE
	 */
	public void publicarLista() {
		List<Integer> ids = this.recuperarIdsMapaPublicacao();
		try {
			validarConectorDje();
			verificarProcessosSelecionados(ids); 
			boolean publicado = publicarProcessos(ids);
			lancarMensagemPublicacaoDje(publicado);
			this.mapaPublicacao.clear();
			setCheckAllAPBL(false);
		} catch(PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getCode());
		}
	}

	/**
	 * Verifica se o objeto do conector do DJE está nulo.
	 * @throws PJeBusinessException
	 */
	private void validarConectorDje() throws PJeBusinessException {
		if(!isConectorDjeValido()) {
			throw new PJeBusinessException("pje.sessaoPauta.erro.publicacaoSemConector");
		}
	}
	
	/**
	 * Verifica se o conector do DJE está valido
	 * @return true se for válido.
	 */
	public boolean isConectorDjeValido() {
		if(publicadorDJE == null) {
			try{
				publicadorDJE = ComponentUtil.getComponent("publicadorDJE");;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return publicadorDJE != null;
		
	}

	/**
	 * Verifica se foi selecionado algum processo para ser publicado.
	 * @param idsProcessosPautaSelecionados
	 * @throws PJeBusinessException
	 */
	private void verificarProcessosSelecionados(List<Integer> idsProcessosPautaSelecionados) throws PJeBusinessException {
		if(idsProcessosPautaSelecionados.isEmpty()) {
			throw new PJeBusinessException("pje.sessaoPauta.erro.publicacaoSemProcesso");
		}
	}
	
	/**
	 * Recupera a lista de ids dos processos em pauta selecionados (SessaoPautaProcessoTrf).
	 * @return List<Integer> lista contendo os identificadores únicos dos processos em 
	 * pauta selecionados (SessaoPautaProcessoTrf).
	 */
	private List<Integer> recuperarIdsMapaPublicacao() {
		List<AptoPublicacaoDTO> aptos = new ArrayList<AptoPublicacaoDTO>();
		List<Integer> ids = new ArrayList<Integer>();
		Set<Entry<SessaoPautaProcessoTrf, Boolean>> entries = this.mapaPublicacao.entrySet();
		for(Entry<SessaoPautaProcessoTrf, Boolean> entry : entries) {
			if(entry.getValue().booleanValue()) {
				AptoPublicacaoDTO apto = new AptoPublicacaoDTO(entry.getKey().getNumeroOrdem(), entry.getKey().getIdSessaoPautaProcessoTrf());
				aptos.add(apto);
			}
		}
		Collections.sort(aptos);
		
		for (AptoPublicacaoDTO apto : aptos) {
			ids.add(apto.getIdSessaoPautaProcessoTrf());
		}

		return ids;
	}
	
	/**
	 * Prepara e efetua a publicacao dos processos
	 * @param ids Identificadores dos processos(SessaoPautaProcessoTrf)
	 * @throws PJeBusinessException
	 */
	private boolean publicarProcessos(List<Integer> ids) throws PJeBusinessException {
		ParametroUtil parametroUtil = ParametroUtil.instance();
		
		Pessoa pessoaPublica = parametroUtil.getPessoaDestinacaoCienciaPublica();
		TipoProcessoDocumento tipoProcessoDocumento =  parametroUtil.getTipoProcessoDocumentoIntimacaoPauta();
		ModeloDocumento modeloDocumento = parametroUtil.getModeloIntimacaoPauta();
		
		return efetuarPublicacaoDje(ids, pessoaPublica, tipoProcessoDocumento, modeloDocumento);
	}

	/**
	 * Efetua a publicacao dos processos selecionados.
	 * @param ids
	 * @param pessoaPublica
	 * @param tipoProcessoDocumento
	 * @param modeloDocumento
	 * @return true se a publicação for efetuada com sucesso, caso contrario, retornará false.
	 * @throws PJeBusinessException
	 */
	private boolean efetuarPublicacaoDje(List<Integer> ids,
			Pessoa pessoaPublica, TipoProcessoDocumento tipoProcessoDocumento,
			ModeloDocumento modeloDocumento) throws PJeBusinessException {
		
		validarValoresParametros(pessoaPublica, tipoProcessoDocumento, modeloDocumento);
		
		boolean publicacaoEfetuada = true;
		
		for(Integer id : ids) {
			publicacaoEfetuada = this.publicarItemLista(id, new Pessoa[]{pessoaPublica}, 
					modeloDocumento.getIdModeloDocumento(), 
					tipoProcessoDocumento.getIdTipoProcessoDocumento());
			if(!publicacaoEfetuada) {
				break;
			}
		}
		
		return publicacaoEfetuada;
	}
	
	/**
	 * Apresenta uma mensagem de sucesso ou erro para o usuário.	
	 * @param publicado se os processos selecionados foram publicados no dje, 
	 * então o valor é true, caso contrario, será false.
	 */
	private void lancarMensagemPublicacaoDje(boolean publicado) {
		if (publicado) {
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pje.sessaoPauta.msg.processosEnviadosCorretamente");
		} else {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pje.sessaoPauta.erro.processosNaoPublicadosDje");
		}
	}
	
	/**
	 * Valida os valores recuperados pelos parametros informados.
	 * @param pessoaPublica
	 * @param tipoProcessoDocumento
	 * @param modeloDocumento
	 * @throws PJeBusinessException
	 */
	private void validarValoresParametros(Pessoa pessoaPublica, TipoProcessoDocumento tipoProcessoDocumento, ModeloDocumento modeloDocumento) 
			throws PJeBusinessException {

		validarPessoaPublica(pessoaPublica); 
		validarTipoProcessoDocumento(tipoProcessoDocumento);
		validarModeloDocumento(modeloDocumento);
	}
	
	/**
	 * Valida pessoa publica
	 * @param pessoaPublica
	 * @throws PJeBusinessException
	 */
	private void validarPessoaPublica(Pessoa pessoaPublica) throws PJeBusinessException {
		if(pessoaPublica == null || pessoaPublica.getIdUsuario() == null || pessoaPublica.getIdUsuario() == 0) {
			throw new PJeBusinessException("pje.sessaoPauta.erro.parametroIndefinidoOuInvalido", Parametros.ID_DESTINACAO_PESSOA_CIENCIA_PUBLICA);
		}
	}

	/**
	 * Valida tipo do processo documento
	 * @param tipoProcessoDocumento
	 * @throws PJeBusinessException
	 */
	private void validarTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) throws PJeBusinessException {
		if(tipoProcessoDocumento == null || tipoProcessoDocumento.getIdTipoProcessoDocumento() == 0) {
			throw new PJeBusinessException("pje.sessaoPauta.erro.parametroIndefinidoOuInvalido", Parametros.ID_TIPO_DOCUMENTO_INTIMACAO_PAUTA);
		}
	}
	
	/**
	 * Valida modelo do documento
	 * @param modeloDocumento
	 * @throws PJeBusinessException
	 */
	private void validarModeloDocumento(ModeloDocumento modeloDocumento)
			throws PJeBusinessException {
		if(modeloDocumento == null || modeloDocumento.getIdModeloDocumento() == 0) {
			throw new PJeBusinessException("pje.sessaoPauta.erro.parametroIndefinidoOuInvalido", Parametros.ID_MODELO_DOCUMENTO_INTIMACAO_PAUTA);
		}
	}

	/**
	 * Envia o processo para o publicacao no DJe
	 * @see PublicadorDJE
	 * @param id identificador da SessaoPautaProcessoTrf
	 * @param pessoa destinatario da publicacao
	 * @param idModulo identificador do modelo do documento que sera utilizado para gerar o expediente
	 * @param idTipoDocumento identificador do tipo do documento que sera utilizado no expediente
	 */
	private boolean publicarItemLista(Integer id, Pessoa[] destinatarios, Integer idModelo, Integer idTipoDocumento) 
			throws PJeBusinessException {
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).findById(id);
		Sessao sessao = sessaoPautaProcessoTrf.getSessao();
		ProcessoTrf processoTrf = sessaoPautaProcessoTrf.getProcessoTrf();
		Date dataCerta = sessao.getMomentoInicio();
		
		this.preencherVariaveisContextoPublicacao(sessao, processoTrf);
		AtoComunicacaoService atoComunicacaoService = ComponentUtil.getComponent(AtoComunicacaoService.class);
		return atoComunicacaoService
				.intimarEletronicamente(processoTrf, sessao, destinatarios, TipoPrazoEnum.S, null, dataCerta, idModelo, idTipoDocumento, true);
	}
	
	/**
	 * Define as variaveis necessarias para a geracao do documento da publicacao
	 * @param sessao A sessao a qual a publicacao esta relacionada
	 * @param processoTrf O processo a qual a publicacao esta relacionada
	 */
	private void preencherVariaveisContextoPublicacao(Sessao sessao, ProcessoTrf processoTrf) {
		Contexts.getEventContext().set(Variaveis.VARIAVEL_PROCESSO_JUDICIAL, processoTrf.getProcesso().getNumeroProcesso());
		Contexts.getEventContext().set(Variaveis.VARIAVEL_CLASSE_JUDICIAL, processoTrf.getClasseJudicial());
		Contexts.getEventContext().set(Variaveis.VARIAVEL_ORGAO_JULGADOR, processoTrf.getOrgaoJulgador().getOrgaoJulgador());
		Contexts.getEventContext().set(Variaveis.VARIAVEL_POLO_ATIVO, ProcessoParteUtils.obterNomesPartesPoloAtivo(processoTrf, "<br />", true));
		Contexts.getEventContext().set(Variaveis.VARIAVEL_POLO_PASSIVO, ProcessoParteUtils.obterNomesPartesPoloPassivo(processoTrf, "<br />", true));
		Contexts.getEventContext().set(Variaveis.VARIAVEL_LOCAL_SESSAO_JULGAMENTO, sessao.getOrgaoJulgadorColegiadoSalaHorario().getSala().getSala());
		Contexts.getEventContext().set(Variaveis.VARIAVEL_DATA_SESSAO_JULGAMENTO, DateUtil.dateToString(sessao.getDataSessao()));
		Contexts.getEventContext().set(Variaveis.VARIAVEL_HORA_SESSAO_JULGAMENTO, DateUtil.dateToHour(sessao.getMomentoInicio()));
		Contexts.getEventContext().set(Variaveis.VARIAVEL_TIPO_SESSAO_JULGAMENTO, sessao.getTipoSessao().getTipoSessao());
		
		tratarComplementoJE(processoTrf);
	}
	
	/**
	 * Adicionar os parametros relacionados a justica eleitoral caso o complemento esteja disponivel
	 * @param processoTrf O processo a qual a publicacao esta relacionada
	 */
	private void tratarComplementoJE(ProcessoTrf processoTrf) {
		if(processoTrf.getComplementoJE() != null) {
			if(processoTrf.getSegredoJustica() != null && processoTrf.getSegredoJustica()) {
				Contexts.getEventContext().set(Variaveis.VARIAVEL_ESTADO, "SIGILOSO");
				Contexts.getEventContext().set(Variaveis.VARIAVEL_MUNICIPIO, "SIGILOSO");
			} else {
				ComplementoProcessoJE complementoJE = processoTrf.getComplementoJE();
				Contexts.getEventContext().set(Variaveis.VARIAVEL_ESTADO, complementoJE.getEstadoEleicao().getCodEstado());
				Contexts.getEventContext().set(Variaveis.VARIAVEL_MUNICIPIO, complementoJE.getMunicipioEleicao().getMunicipio());
			}
		}
	}
	
	/** 
	 * Recupera a situação da publicação no DJE, do processo na sessão.
	 * @return Se o processo não tiver sido enviado para publicação, retorna "Não";
	 * Se foi enviado mas ainda não foi publicado, retorna "Sim, aguardando publicação";
	 * Caso contrário, se foi enviado e publicado "Sim, publicado em <datas_de_publicacao>".
	 */
	public String statusPublicacaoDje(SessaoPautaProcessoTrf sessaoPautaProcesso) {
		String status = StringUtils.EMPTY;
		try {
			ProcessoParteExpediente ppe = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).recuperarExpedientePublicadoDje(sessaoPautaProcesso);
			status = obtemStatusDaPublicacao(ppe);
		} catch (PontoExtensaoException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pje.sessaoPauta.erro.consultaPublicacaoDje");
		}
		return status;
	}

	/**
	 * Obtem o status da publicação do processo no DJE.
	 * @param ppe
	 * @return String
	 * @throws PontoExtensaoException
	 */
	private String obtemStatusDaPublicacao(ProcessoParteExpediente ppe) throws PontoExtensaoException {
		String status = "Não";
		if(ppe != null) {
			Date dataPublicacaoDje = null;
			try {
				dataPublicacaoDje = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).recuperarDataPublicacaoDje(ppe);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
			if(dataPublicacaoDje != null) {
				status = "Sim, publicado em " + DateUtil.dateToString(dataPublicacaoDje);
			} else {
				status = "Sim, aguardando publicação";
			}
		}
		return status;
	}

	public Map<SessaoPautaProcessoTrf, Boolean> getMapaPublicacao() {
		return this.mapaPublicacao;
	}
	
	public void setMapaPublicacao(Map<SessaoPautaProcessoTrf, Boolean> mapaPublicacao) {
		this.mapaPublicacao = mapaPublicacao;
	}
	
	/**
	 * Carrega o texto da proclamação de julgamento para ser exibido para o usuário
	 * @param orgaoJulgadorVencedor gabinete vencedor do processo em pauta na sessão.
	 */
	public void carregarProclamacaoJulgamento(OrgaoJulgador orgaoJulgadorVencedor) {
		setOrgaoJulgadorVencedor(orgaoJulgadorVencedor);
		if (!isMovimentacaoEmLote() && isRelatorProcessoEhVencedor(orgaoJulgadorVencedor)) {
			SessaoPautaProcessoTrf sessaoPautaProcessoSelecionado = obtemSessaoPautaProcessoSelecionado();
			copiarProclamacaoAntecipadaParaProclamacaoDecisao(sessaoPautaProcessoSelecionado);
		}
	}
	
	/**
	 * Copia o texto da proclamação antecipada para a proclamação decisória 
	 * do processo em pauta na sessão selecionado.
	 * @param sessaoPautaProcessoTrf processo em pauta na sessão selecionado.
	 */
	private void copiarProclamacaoAntecipadaParaProclamacaoDecisao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		String textoProclamacaoJulgamento = ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).
				recuperarTextoProclamacaoJulgamentoAntecipada(sessaoPautaProcessoTrf);
		if(StringUtils.isNotBlank(textoProclamacaoJulgamento)) {
			setProclamacaoDecisao(textoProclamacaoJulgamento);
			setJulgamentoEnum(sessaoPautaProcessoTrf.getJulgamentoEnum());
		}
	}
	
	/**
	 * O relator do processo é o mesmo do órgão julgador vencedor.
	 * @param orgaoJulgadorVencedor - 
	 * @return 
	 */
	private boolean isRelatorProcessoEhVencedor(OrgaoJulgador orgaoJulgadorVencedor) {
		SessaoPautaProcessoTrf sessaoPautaProcessoSelecionado = obtemSessaoPautaProcessoSelecionado();
		return ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).isRelatorProcessoEhOrgaoVencedor(
				sessaoPautaProcessoSelecionado, orgaoJulgadorVencedor);
	}
	
	/**
	 * Obtem o processo em pauta na sessão selecionado.
	 * @return SessaoPautaProcessoTrf item selecionado.
	 */
	public SessaoPautaProcessoTrf obtemSessaoPautaProcessoSelecionado() {
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = null;
		if( listaSCO.size() == 1 ) {
			sessaoPautaProcessoTrf = listaSCO.get(0);
		}
		return sessaoPautaProcessoTrf;
	}
	
	/**
	 * Obtem o nome do gabinete vencedor.
	 * @param orgaoJulgador órgão julgador do qual será obtido o nome.
	 * @return String nome do gabinete vencedor.
	 */
	public String obterNomeGabineteVencedor(OrgaoJulgador orgaoJulgador) {
		if(isMovimentacaoEmLote()) {
			return orgaoJulgador.getOrgaoJulgador();
		}
		return obterNomeOrgaoJulgador(orgaoJulgador);
	}
	
	/**
	 * Obtem o nome do órgão julgador do processo que está em pauta na sessão.
	 * @param orgaoJulgador órgão julgador do qual será obtido o nome.
	 * @return String nome do órgão julgador do processo em pauta na sessão.
	 */
	public String obterNomeOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		return ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).obterNomeOrgaoJulgador(listaSCO.get(0), orgaoJulgador);
	}

	/**
	 * Recupera os votos dos processos em pauta na sessão selecionados.
	 * @return List<SessaoProcessoDocumentoVoto> lista de votos.
	 */
	public List<SessaoProcessoDocumentoVoto> recuperarVotosProcessosSelecionados() {
		Sessao sessao = SessaoHome.instance().getInstance();
		List<ProcessoTrf> processos = recuperarProcessosEmPautaSelecionados();
		if(sessao != null && !processos.isEmpty()) {
			return ComponentUtil.getComponent(SessaoProcessoDocumentoVotoManager.class).recuperarVotosPorSessaoEhProcessos(sessao , processos);
		}
		return Collections.emptyList();
	}

	/**
	 * Defini os labels que serão exibidos no campo radio button da proclamação de julgamento.
	 */
	private void definirLabelRadioButton() {
		if(possuemProclamacaoAntecipada) {
			setLabelRadioButtonProclamacao("sessaoPautaProcessoTrfHome.labelTodosProcessosPossuemTexto");
			setItemLabelRadioButton("sessaoPautaProcessoTrfHome.itemLabelUtilizarTextos");
		} else {
			setLabelRadioButtonProclamacao("sessaoPautaProcessoTrfHome.labelExistemProcessosSemTexto");
			setItemLabelRadioButton("sessaoPautaProcessoTrfHome.itemLabelUtilizarNovoTextosApenasProcessoSemTexto");
		}
	}

	/**
	 * Verifica se os votos possuem texto da proclamação de julgamento antecipada.
	 * @param votos lista de votos.
	 * @return true se todos os textos estiverem válidos.
	 */
	private boolean isPossuemProclamacaoAntecipada(List<SessaoProcessoDocumentoVoto> votos) {
		return votos.size() == listaSCO.size() && validarTextosProclamacaoAntecipadaVotos(votos);
	}
	
	/**
	 * Recupera os processos em pauta na sessão selecionados.
	 * @return List<ProcessoTrf>lista de processos.
	 */
	public List<ProcessoTrf> recuperarProcessosEmPautaSelecionados() {
		List<ProcessoTrf> processos = new ArrayList<ProcessoTrf>(0);
		for(SessaoPautaProcessoTrf sppt : listaSCO) {
			processos.add(sppt.getProcessoTrf());
		}
		return processos;
	}
	
	/**
	 * Valida-se os textos da proclamação de julgamento antecipada dos votos estão preenchido.
	 * @param votos lista de votos que contém os textos a serem validados.
	 * @return true se todos os textos forem diferentes de nulo ou não possuirem valores em branco.
	 */
	public boolean validarTextosProclamacaoAntecipadaVotos(List<SessaoProcessoDocumentoVoto> votos) {
		boolean retorno = true;
		
		for(SessaoProcessoDocumentoVoto voto : votos) {
			if(!validarTextoProclamacaoAntecipadaVoto(voto)) {
				retorno = false;
				break;
			}
		}
		return retorno;
	}
	
	/**
	 * Verifica se o texto da proclamação de julgamento antecipada do voto não está nulo ou em branco.
	 * @param voto que contém o texto a ser validado.
	 * @return true se o texto for diferente de nulo ou não for ter um valor em branco.
	 */
	public boolean validarTextoProclamacaoAntecipadaVoto(SessaoProcessoDocumentoVoto voto) {
		return voto != null && StringUtils.isNotBlank(voto.getTextoProclamacaoJulgamento());
	}
	
	/**
	 * Verifica se o gabinete (órgão julgador) vencedor está nulo.
	 * @return true se o gabinete vencedor estiver nulo, false caso contrário.
	 */
	public boolean isNullGabineteVencedor() {
		return orgaoJulgadorVencedor == null || orgaoJulgadorVencedor.getIdOrgaoJulgador() == 0;
	}

	/**
	 * Controla a exibição/ocultação dos votos para os usuário que não são
	 * magistrados antes do processo ser colocado em julgamento.
	 */
	public boolean exibirVotosProcesso(SessaoPautaProcessoTrf sppt) {
		if (ParametroUtil.instance().isOcultarVotosAntecipadosNaoMagistrado()
				&& !Authenticator.isPapelAtualMagistrado()
				&& (!(TipoSituacaoPautaEnum.EJ.equals(sppt.getSituacaoJulgamento()) || TipoSituacaoPautaEnum.JG.equals(sppt.getSituacaoJulgamento())))) {
			return false;
		}
		return true;
	}
	
	/**
	 * Controla a exibição/ocultação do editor da proclamação de julgamento.
	 * @return true se não for uma movimentação em lote ou se for uma movimentação 
	 * em lote e se os processos não possuirem proclamação de julgamento antecipada
	 * ou se for utilizar novo texto para todos os processos.
	 */
	public boolean exibirEditorProclamacaoJulgamento() {
		return !isMovimentacaoEmLote() || (isMovimentacaoEmLote() && (!possuemProclamacaoAntecipada || utilizarNovoTxtTodosProcessos));
	}
	
	public Boolean getUtilizarNovoTxtTodosProcessos() {
		return utilizarNovoTxtTodosProcessos;
	}

	public void setUtilizarNovoTxtTodosProcessos(Boolean utilizarNovoTxtTodosProcessos) {
		this.utilizarNovoTxtTodosProcessos = utilizarNovoTxtTodosProcessos;
	}
	
	public void setPossuemProclamacaoAntecipada(Boolean possuemProclamacaoAntecipada) {
		this.possuemProclamacaoAntecipada = possuemProclamacaoAntecipada;
	}
	
	public Boolean getPossuemProclamacaoAntecipada() {
		return possuemProclamacaoAntecipada;
	}

	public String getLabelRadioButtonProclamacao() {
		return labelRadioButtonProclamacao;
	}

	public void setLabelRadioButtonProclamacao(String labelRadioButtonProclamacao) {
		this.labelRadioButtonProclamacao = labelRadioButtonProclamacao;
	}

	public String getItemLabelRadioButton() {
		return itemLabelRadioButton;
	}

	public void setItemLabelRadioButton(String itemLabelRadioButton) {
		this.itemLabelRadioButton = itemLabelRadioButton;
	}
	
	public Map<Integer, Map<Integer, Set<Integer>>> getPlacares() {
		return placares;
	}
	
	public void setPlacares(Map<Integer, Map<Integer, Set<Integer>>> placares) {
		this.placares = placares;
	}
	
	public boolean processoRetiradoJulgamentoUltimaSessao(ProcessoTrf processoTrf){
		SessaoPautaProcessoTrf julgamento = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).recuperaUltimaPautaProcesso(processoTrf);
		if(julgamento != null && julgamento.getOrgaoJulgadorRetiradaJulgamento() != null){
			return true;
		}
		return false;
	}
	
 	/**
 	 * Monta o link padrao para o detalhemento do processo
 	 * 
 	 * @param idProcessoTrf Id do processoo
 	 * @return link
 	 */
 	public String montarLinkDetalheProcesso(Integer idProcessoTrf) {
 		return UrlUtil.montarLinkDetalheProcessoDefault(idProcessoTrf);
 	}
 	
 	public List<OrgaoJulgadorCargo> recuperaCargosTitularesOj(OrgaoJulgador orgaoJulgador){
 		
 		List<OrgaoJulgadorCargo> retorno = new ArrayList<OrgaoJulgadorCargo>();
 		
 		for(OrgaoJulgadorCargo ojca : orgaoJulgador.getOrgaoJulgadorCargoList()){
 			if(!ojca.getAuxiliar()){
 				retorno.add(ojca);
 			}
 		}
 		
 		return retorno;
 	}
 	
 	/**
	 * @return dentre os processos marcados na grid de aptos para inclusão em pauta, 
	 * retorna aqueles que possuem fluxo de pedido de vista aberto
	 */
	public List<ConsultaProcessoAdiadoVista> processosMarcadosComFluxoVistaAberto(){
		List<ConsultaProcessoAdiadoVista> listaProcessoFluxoVista = new ArrayList<ConsultaProcessoAdiadoVista>(0);
		for(ConsultaProcessoAdiadoVista processoVista : processosPedidoVista()){
			if(processoPossuiFluxoPedidoVistaAberto(processoVista) && !isVotoAntecipadoElaborado(processoVista)){
				listaProcessoFluxoVista.add(processoVista);
			}
		}
		return listaProcessoFluxoVista;
	}
	
	/**
	 * @param idProcesso Identificador do processo
	 * @return true caso o processo tenha fluxo de pedido de vista aberto,
	 * caso contrário, retorna false
	 */
	private boolean processoPossuiFluxoPedidoVistaAberto(ConsultaProcessoAdiadoVista processoVista){
		SessaoManager sessaoManager = ComponentUtil.getComponent(SessaoManager.class);
		Fluxo fluxo = sessaoManager.getFluxoPedidoVista();
		FluxoManager fluxoManager = ComponentUtil.getComponent(FluxoManager.class);
		if(fluxoManager.existeProcessoNoFluxoEmExecucao(processoVista.getIdProcessoTrf(), fluxo.getFluxo())){
			return true;
		}
		return false;
	}
	
	/**
	 * Método responsável por abrir uma modal para confirmação de adição de
	 * processo vista
	 * 
	 * @param processoVista
	 *            o processo 
	 * @return <code>boolean</code>, <code>true</code> caso o processo tenha
	 *         sido marcado e não tenha voto antecipado elaborado
	 */
	public boolean abrirModalAdicaoProcessoVistaIndividual(ConsultaProcessoAdiadoVista processoVista) {
		boolean retorno = false;
		
		if (processoVista != null && (processoVista.getCheck() != null && processoVista.getCheck())) {
			if (!isVotoAntecipadoElaborado(processoVista)) {
				retorno = true;
			}
		}
		
		return retorno;
	}
	
	/**
	 * @return retorna true caso a opção de selecionar todos os processos da grid seja marcada 
	 * e a lista de processos marcados em aptos para inclusão em pauta possua
	 * algum processo com fluxo de pedido de vista aberto
	 */
	public boolean listaPossuiProcessoFluxoPedidoVistaAberto(){
		if(listProcessoAdiadoPedidoVista.isEmpty() && getCheckBox()){
			for(ConsultaProcessoAdiadoVista processoVista : processosPedidoVista()){
				if((processoVista.getCheck() == null || !processoVista.getCheck()) 
						&& processoPossuiFluxoPedidoVistaAberto(processoVista)){
					return true;
				}
			}
		}
		if(!getCheckBox()){
			limparMarcacaoProcessosVista();
		}
		return false;
	}
	
	/**
	 * Desmarca da lista de aptos para inclusão em pauta os processos com 
	 * fluxo de pedido de vista aberto
	 */
	public void desmarcaProcessosPossuiVistaNaoApreciada(){
		listProcessoAdiadoPedidoVista = new ArrayList<ConsultaProcessoAdiadoVista>(0);
	}

	public String getOpcaoInclusaoPauta() {
		return opcaoInclusaoPauta;
	}

	public void setOpcaoInclusaoPauta(String opcaoInclusaoPauta) {
		this.opcaoInclusaoPauta = opcaoInclusaoPauta;
	}

	public ConsultaProcessoAdiadoVista getProcessoVistaInclusao() {
		return processoVistaInclusao;
	}

	public void setProcessoVistaInclusao(ConsultaProcessoAdiadoVista processoVistaInclusao) {
		this.processoVistaInclusao = processoVistaInclusao;
	}

	public void limparMarcacaoProcessosVista(){
		listProcessoAdiadoPedidoVista.clear();
		setCheckBox(Boolean.FALSE);
		for (ConsultaProcessoAdiadoVista processoAdiadoVistaPV : processosPedidoVista()) {
			processoAdiadoVistaPV.setCheck(Boolean.FALSE);
		}
	}
	
	/**
	 * Método responsável por limpar a marcação individual de checkbox do
	 * processo vista
	 */
	public void limparMarcacaoProcessoVista() {
		setCheckBox(Boolean.FALSE);
		listProcessoAdiadoPedidoVista.clear();
		getProcessoVistaInclusao().setCheck(false);
	}
	
	private List<ConsultaProcessoAdiadoVista> processosPedidoVista(){
		ProcessoAdiadoVistaPVList gridQuery = ComponentUtil.getComponent(ProcessoAdiadoVistaPVList.class);
		return gridQuery.getResultList();
	}
	
	public void prepararInclusaoProcessoVista(ConsultaProcessoAdiadoVista processoVista){
		criarListaAdiadosPedidoVista(processoVista);
		setProcessoVistaInclusao(processoVista);
	}
	

	/**
	 * Método responsável por listar os processos adiados de acordo com o
	 * parâmetro passado. Se o parâmetro for <code>true</code>, o método irá
	 * buscar processos com o voto antecipado elaborado (apreciados), de acordo com o método
	 * {@link #isVotoAntecipadoElaborado(ConsultaProcessoAdiadoVista)}, se
	 * <code>false</code> o método irá buscar processos com o voto antecipado
	 * não elaborado (não apreciado).
	 * 
	 * @param obterProcessoComVotoAntecipadoElaborado
	 *            se <code>true</code>, o método irá filtrar os processos com
	 *            voto antecipado elaborado, se <code>false</code> filtrará por
	 *            processos com voto antecipado não elaborado
	 * @return <code>List</code>, processos adiados com pedido de vista
	 */
	public List<ConsultaProcessoAdiadoVista> processosPedidoVista(boolean obterProcessoComVotoAntecipadoElaborado){
		List<ConsultaProcessoAdiadoVista> processosPedidoVista = new ArrayList<ConsultaProcessoAdiadoVista>(0);
		for (ConsultaProcessoAdiadoVista processoVista : processosPedidoVista()) {
			if (obterProcessoComVotoAntecipadoElaborado) {
				if (isVotoAntecipadoElaborado(processoVista)) {
					processosPedidoVista.add(processoVista);
				}				
			} else {
				if (!isVotoAntecipadoElaborado(processoVista)) {
					processosPedidoVista.add(processoVista);
				}
			}
		}
		return processosPedidoVista;
	}
	
	/**
	 * Método responsável por verificar se o voto antecipado já foi elaborado
	 * 
	 * @param processoVista
	 *            o processo que se deseja verificar o voto antecipado
	 * @return <code>boolean</code>, <code>true</code> caso o voto antecipado já
	 *         tenha sido elaborado
	 */
	public boolean isVotoAntecipadoElaborado(ConsultaProcessoAdiadoVista processoVista) {
		return SessaoHome.instance().votoAntecipadoElaborado(processoVista.getProcessoTrf(), 
				processoVista.getSessaoPautaProcessoTrf().getOrgaoJulgadorPedidoVista());
	}
	
	/**
	 * Verifica se todos processos selecionados possuem o mesmo relator.
	 * @return
	 */
	private boolean validaMesmaRelatoriaProcessos(){
		OrgaoJulgador orgaoJulgadorRelator = null;
		
		for (SessaoPautaProcessoTrf sppt : listaSCO) {
			if(orgaoJulgadorRelator == null){
				orgaoJulgadorRelator = sppt.getProcessoTrf().getOrgaoJulgador();
			}
			
			if(!orgaoJulgadorRelator.equals(sppt.getProcessoTrf().getOrgaoJulgador())){
				return false;
			}
		}
		return true;
	}
	
	private List<SessaoComposicaoVotoLoteVO> getListaSessaoComposicaoVotoLote(){		
		List<SessaoComposicaoOrdem> composicaoOrdemList = getListaComposicaoOrdem();
		List<SessaoComposicaoVotoLoteVO> listSessaoComposicaoVotoLote = new ArrayList<SessaoComposicaoVotoLoteVO>(composicaoOrdemList.size());

		OrgaoJulgador orgaoJulgadorRelator = ProjetoUtil.isVazio(listaSCO) ? null : listaSCO.get(0).getProcessoTrf().getOrgaoJulgador();

		for(SessaoComposicaoOrdem sessaoComposicaoOrdem :  composicaoOrdemList){
			SessaoComposicaoVotoLoteVO vo = new SessaoComposicaoVotoLoteVO();
			vo.setOrgaoJulgador(sessaoComposicaoOrdem.getOrgaoJulgador());	
			vo.setRelator(orgaoJulgadorRelator.equals(sessaoComposicaoOrdem.getOrgaoJulgador()));
			
			listSessaoComposicaoVotoLote.add(vo);
		}
		return listSessaoComposicaoVotoLote;
	}

	public List<SessaoComposicaoVotoLoteVO> getListSessaoComposicaoVotoLote() {
		return listSessaoComposicaoVotoLote;
	}

	public void setListSessaoComposicaoVotoLote(List<SessaoComposicaoVotoLoteVO> listSessaoComposicaoVotoLote) {
		this.listSessaoComposicaoVotoLote = listSessaoComposicaoVotoLote;
	}

	public boolean isExibirModalVotoVogal() {
		return exibirModalVotoVogal;
	}

	public void setExibirModalVotoVogal(boolean exibirModalVotoVogal) {
		this.exibirModalVotoVogal = exibirModalVotoVogal;
	}

	public void limparVariaveisVotacaoLote() {
		mostraVotacaoLote = false;
		listSessaoComposicaoVotoLote = null;
		listaSCO = new ArrayList<SessaoPautaProcessoTrf>();
	}

	public List<JulgamentoEnum> getListaTiposJulgamentoEnum(){
		return Arrays.asList(JulgamentoEnum.values());
	}

	public JulgamentoEnum getJulgamentoEnum() {
		return julgamentoEnum;
	}

	public void setJulgamentoEnum(JulgamentoEnum julgamentoEnum) {
		this.julgamentoEnum = julgamentoEnum;
	}

}
