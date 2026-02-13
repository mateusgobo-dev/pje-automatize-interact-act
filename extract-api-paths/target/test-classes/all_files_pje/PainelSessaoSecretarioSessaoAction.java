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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.util.Strings;

import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.list.ModeloProclamacaoJulgamentoList;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.NotaSessaoJulgamentoManager;
import br.jus.pje.nucleo.dto.SessaoJulgamentoDTO;
import br.jus.pje.nucleo.dto.SessaoJulgamentoFiltroDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.SessaoResultadoVotacaoEnum;
import br.jus.pje.nucleo.enums.SituacaoProcessoSessaoEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

/**
 * Componente de controle da tela do secretario da sessao
 */
@Name(PainelSessaoSecretarioSessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PainelSessaoSecretarioSessaoAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "painelSessaoSecretarioSessaoAction";
	
	private Integer idSessao;
	private Sessao sessao;
	private Date dataRegistroEvento = null;
	private Date dataRealizacaoSessao = null;
	private List<SessaoJulgamentoDTO> processosEmPautaDTOs = new ArrayList<>();
	private List<SessaoJulgamentoDTO> processosJulgadosDTOs = new ArrayList<>();
	private List<SessaoComposicaoOrdem> listaComposicaoOrdemGV = new ArrayList<>();
	private Set<Integer> keyToUpdate;
	private static final SimpleDateFormat formatterHora = new SimpleDateFormat("HH:mm:ss");
	private SessaoJulgamentoDTO rowAtualModal;
	private String horaUltimaAtualizacaoResultadoSessao;
	private boolean exibeMPConfirmacaoEncerrarJulgamento = false;

	@Logger
	private transient Log logger;

	// Campos de filtragem //
	private String numeroProcesso;
	
	private AssuntoTrf campoAssunto;
	
	private ClasseJudicial campoClasse; 
	
	private PrioridadeProcesso prioridade; 
	
	private List<PrioridadeProcesso> prioridades; 
	
	private Date dataInicialDistribuicao; 
	
	private Date dataFinalDistribuicao; 
	
	private String nomeParte; 
	
	private String codigoIMF;
	
	private String codigoOAB; 
	
	private OrgaoJulgador orgaoFiltro;
	
	private TipoVoto tipoVotoRelator; 
	
	private SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum;
	
	private TipoInclusaoEnum tipoInclusaoEnum;
	
	private SituacaoProcessoSessaoEnum situacaoProcEnum;
	
	private Boolean possuiProclamacaoAntecipada;
	
	private TipoPessoa tipoPessoa;
	// Fim dos campos de filtragem //
	
	private boolean atualizarProcessosJulgados;
	private boolean atualizarProcessos;
	private ModeloProclamacaoJulgamento modeloProclamacaoJulgamento;
	
	// Variáveis utilizadas para cache.
	private Map<Integer, List<SessaoPautaProcessoTrf>> sessaoPautaProcessosTrf = new LinkedHashMap<>();
	private List<ModeloProclamacaoJulgamento> listagemModeloProclamacaoJulgamento = new ArrayList<>();
	
	/**
	 * Inicializa o objeto
	 */
	@Create
	public void init() {
		try {
			keyToUpdate = new HashSet<>();
			idSessao = ComponentUtil.getSessaoHome().getSessaoIdSessao();
			dataRegistroEvento = ComponentUtil.getSessaoHome().getInstance().getDataRegistroEvento();
			dataRealizacaoSessao = ComponentUtil.getSessaoHome().getInstance().getDataRealizacaoSessao();
			horaUltimaAtualizacaoResultadoSessao = formatterHora.format(new Date());
			if (idSessao != null) {
	 			sessao = ComponentUtil.getSessaoJulgamentoManager().findById(idSessao);
			}
			recuperarSessaoPautaProcessosTrf(false);
			recuperarSessaoPautaProcessosTrf(true);
		} catch (Exception e) {
			logger.error(Severity.FATAL, "Erro ao inicializar o controlador do painel do secretario da sessão na sessão: {0}.", e.getLocalizedMessage());			
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
		this.nomeParte = null;
		this.codigoIMF = null;
		this.codigoOAB = null;	
		this.dataInicialDistribuicao = null;
		this.dataFinalDistribuicao = null;
		this.tipoVotoRelator = null;
		this.sessaoResultadoVotacaoEnum = null;
		this.tipoInclusaoEnum = null;
		this.situacaoProcEnum = null;
		this.possuiProclamacaoAntecipada = null;
		this.tipoPessoa = null;
		
		marcarAtualizacoes();
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public AssuntoTrf getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(AssuntoTrf campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public ClasseJudicial getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(ClasseJudicial campoClasse) {
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

	public List<PrioridadeProcesso> getPrioridades() {
		if(prioridades == null){
			try {
				prioridades = ComponentUtil.getPrioridadeProcessoManager().listActive();
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar recuperar as listas de prioridades: {0}.", e.getLocalizedMessage());
				return Collections.emptyList();
			}
		}
		return prioridades;
	}

	//********** Fim do tratamento de filtragem **********//
	
	/**
	 * Recupera a sessão de julgamento atualmente tratada.
	 * 
	 * @return a sessão
	 */
	public Sessao getSessao() {
		return sessao;
	}
	
	public boolean exibeColunaSelecaoProcessos(boolean painelSecretarioSessao) {
		return (painelSecretarioSessao && dataRegistroEvento == null && dataRealizacaoSessao == null);
	}
	
	public void limparListaProcessosEmPautaDTOs(){
		processosEmPautaDTOs.clear();
	}
	
	public List<SessaoPautaProcessoTrf> recuperarSessaoPautaProcessosTrf(boolean julgados) throws Exception {
		int key = Integer.valueOf(Boolean.valueOf(julgados).hashCode());
		if (!this.sessaoPautaProcessosTrf.containsKey(key)
				|| (atualizarProcessos && !julgados)
				|| (atualizarProcessosJulgados && julgados)) {
			List<SessaoPautaProcessoTrf> lista;
			lista = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarSessaoPautaProcessosTrf(idSessao,
					getSessaoJulgamentoFiltroDTO(), true, true, julgados);
			sessaoPautaProcessosTrf.remove(key);
			sessaoPautaProcessosTrf.put(key, lista);
		}
		return this.sessaoPautaProcessosTrf.get(key);
	}
	
	public List<SessaoJulgamentoDTO> getProcessosDTO() {
		if (processosEmPautaDTOs.isEmpty()) {
			try {
				processosEmPautaDTOs.addAll(listarPautaSessaoUsandoFiltro(false));
			} catch (Exception e) {
				logger.error(Severity.ERROR, "Erro ao recuperar SessaoPautaProcessosTrf: {0}.", e.getLocalizedMessage());
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
				return new ArrayList<>();
			}
		} else if (atualizarProcessos) {
			processosEmPautaDTOs.clear();
			getProcessosDTO();
		}
		this.atualizarProcessos = Boolean.FALSE;
		return processosEmPautaDTOs;
	}
	
	public List<SessaoJulgamentoDTO> getProcessosJulgadosDTO() {
		if (processosJulgadosDTOs.isEmpty()) {
			try {
				processosJulgadosDTOs.addAll(listarPautaSessaoUsandoFiltro(true));
			} catch (Exception e) {
				logger.error(Severity.ERROR, "Erro ao recuperar SessaoPautaProcessosTrf: {0}.", e.getLocalizedMessage());
				FacesMessages.instance().add(Severity.ERROR, e.getMessage());
				return new ArrayList<>();
			}
		} else if (atualizarProcessosJulgados) {
			processosJulgadosDTOs.clear();
			getProcessosJulgadosDTO();
		}
		this.atualizarProcessosJulgados = Boolean.FALSE;
		return processosJulgadosDTOs;
	}
	
	private List<SessaoJulgamentoDTO> listarPautaSessaoUsandoFiltro(boolean julgados) throws Exception {
		return converteSessaoPautaProcessoTrf(this.recuperarSessaoPautaProcessosTrf(julgados));
	}
	
	private List<SessaoJulgamentoDTO> converteSessaoPautaProcessoTrf(List<SessaoPautaProcessoTrf> sessoesPautaProcessosTrf) {
		List<SessaoJulgamentoDTO> retorno = new ArrayList<>(sessoesPautaProcessosTrf.size());
		String dataAtualizacaoDados = formatterHora.format(new Date());
		Integer quantidadeAnotacoes = null;

		Map<Integer, Integer> quantidadeAnotacoesPorProcesso = ComponentUtil.getComponent(NotaSessaoJulgamentoManager.class).contagemNotasPorProcesso(getSessao());

		for (SessaoPautaProcessoTrf sppt : sessoesPautaProcessosTrf) {
			SessaoJulgamentoDTO dto = new SessaoJulgamentoDTO(sppt, this.sessao,
					this.obterNomeParaExibicaoEmProcesso(sppt), dataAtualizacaoDados, Strings.EMPTY);

			quantidadeAnotacoes = quantidadeAnotacoesPorProcesso.get(sppt.getProcessoTrf().getIdProcessoTrf());
			dto.setQuantidadeAnotacoes(quantidadeAnotacoes == null ? 0 : quantidadeAnotacoes);

			retorno.add(dto);
		}

		return retorno;
	}

	private void atualizarCampoRetiradoJulgamento(SessaoJulgamentoDTO dto) {
		dto.setRetiradoJulgamento(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarRetiradaJulgamento(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoAdiadoVista(SessaoJulgamentoDTO dto) {
		dto.setAdiadoVista(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarAdiadoVista(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoPreferencia(SessaoJulgamentoDTO dto) {
		dto.setPreferencia(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarPreferencia(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoSustentacaoOral(SessaoJulgamentoDTO dto) {
		dto.setSustentacaoOral(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarSustentacaoOral(dto.getProcessoTrf(), dto.getSessao()));
		dto.setAdvogadoSustentacaoOral(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarAdvogadoSustentacaoOral(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoProclamacaoDecisao(SessaoJulgamentoDTO dto) {
		dto.setProclamacaoDecisao(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarProclamacaoDecisaoOral(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoJulgamentoCelere(SessaoJulgamentoDTO dto) {
		dto.setMaioriaDetectada(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarMaioriaDetectada(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoSituacaoJulgamento(SessaoJulgamentoDTO dto) {
		dto.setSituacaoJulgamento(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarSituacaoJulgamento(dto.getProcessoTrf(), dto.getSessao()));
	}
	
	private void atualizarCampoQuantidadeAnotacoes(SessaoJulgamentoDTO dto) {
		dto.setQuantidadeAnotacoes(ComponentUtil.getComponent(NotaSessaoJulgamentoManager.class).recuperaNotas(dto.getSessao(), dto.getProcessoTrf()).size());
	}

	private String obterNomeParaExibicaoEmProcesso(SessaoPautaProcessoTrf sppt) {
		return String.format("%s X %s", sppt.getConsultaProcessoTrf().getAutor(), sppt.getConsultaProcessoTrf().getReu());
	}
	
	public void toggleProclamacaoDecisao(SessaoJulgamentoDTO sessaoJulgamentoDTO) {
		sessaoJulgamentoDTO.setToggleProclamacaoDecisao(BooleanUtils
				.toStringTrueFalse(Boolean.parseBoolean(sessaoJulgamentoDTO.getToggleProclamacaoDecisao())));

		if (Boolean.parseBoolean(sessaoJulgamentoDTO.getToggleProclamacaoDecisao())) {
			this.atualizarCampoProclamacaoDecisao(sessaoJulgamentoDTO);
			this.updateSPPTEspecifico(sessaoJulgamentoDTO);
			this.modeloProclamacaoJulgamento = null;
		}
	}

	public void atualizarProclamacaoDecisao(SessaoJulgamentoDTO sessaoJulgamentoDTO) {
		if (this.modeloProclamacaoJulgamento == null) {
			sessaoJulgamentoDTO.setProclamacaoDecisao(StringUtils.EMPTY);
		} else {
			sessaoJulgamentoDTO.setProclamacaoDecisao(this.modeloProclamacaoJulgamento.getDescricaoModelo());
		}
		this.updateSPPTEspecifico(sessaoJulgamentoDTO);
	}

	public void salvarProclamacaoDecisao(SessaoJulgamentoDTO dto) {
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().salvarProclamacaoJulgamento(dto.getSessaoPautaProcessoTrf(), dto.getProclamacaoDecisao(), dto.getJulgamentoEnum());

			ComponentUtil.getSessaoProcessoDocumentoVotoManager()
					.atualizarTextoDaProclamacaoDoVotoRelator(dto.getSessaoPautaProcessoTrf());

		} catch (Exception e) {
			logger.error(Severity.ERROR, "Erro ao salvar proclamação de julgamento: Processo: {0} : {1}.", dto.getNumeroProcesso(), e.getLocalizedMessage());
		}
	}
	
	public void atribuirRetirarPrioridadeButton(SessaoJulgamentoDTO dto) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("preferencia",dto.getSessaoPautaProcessoTrf());
		atualizarCampoPreferencia(dto);
		updateSPPT(dto);
	}
	
	public void abrirModalSustentacaoOral(SessaoJulgamentoDTO dto) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("sustentacaoOral", dto.getSessaoPautaProcessoTrf());
		this.rowAtualModal = dto;
	}
	
	public void retirarSustentacaoOral(SessaoJulgamentoDTO dto) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("sustentacaoOral", dto.getSessaoPautaProcessoTrf());
		this.atualizarCampoSustentacaoOral(dto);
		this.updateSPPTEspecifico(dto);
	}
	
	public void atribuirRetirarCeleridadeJulgamentoButton(SessaoJulgamentoDTO dto) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("pautarapida",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
	}
	
	public void colocarEmJulgamentoButton(SessaoJulgamentoDTO dto) {
		if(verificaCondicoesJulgados(dto)) {
			marcarAtualizacoes();
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("emjulgamento",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
	}
	
	public void retirarDeJulgamentoButton(SessaoJulgamentoDTO dto) {
		if(!verificaCondicoesJulgados(dto)) {
			marcarAtualizacoes();
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("retirado",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
	}
	
	public void registrarJulgamentoButton(SessaoJulgamentoDTO dto) {
		if(!verificaCondicoesJulgados(dto)) {
			marcarAtualizacoes();
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("julgado",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
		rowAtualModal = dto;
	}
	
	public void tornarPendenteJulgamentoButton(SessaoJulgamentoDTO dto) {
		if(verificaCondicoesJulgados(dto)) {
			marcarAtualizacoes();
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("aguardando",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
	}
	
	public void adiarProximaSessaoButton(SessaoJulgamentoDTO dto) {
		if(!verificaCondicoesJulgados(dto)) {
			marcarAtualizacoes();
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("adiado",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
	}
	
	public void adiarPedidoVistaButton(SessaoJulgamentoDTO dto) {
		if(!verificaCondicoesJulgados(dto)) {
			marcarAtualizacoes();
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("pedidoVista",dto.getSessaoPautaProcessoTrf());
		updateSPPT(dto);
		rowAtualModal = dto;
	}
	
	public boolean exibeMenuItemColocarEmJulgamento(SessaoJulgamentoDTO dto) {
		return !isJulgamentoFinalizado(dto.getSessaoPautaProcessoTrf()) && ((verificaCondicaoAguardandoJulgamento(dto) || 
				verificaCondicaoRetiradoJulgamento(dto) || 
				verificaCondicaoJulgado(dto) || 
				verificaCondicaoAdiado(dto) || 
				verificaCondicaoPedidoVista(dto)) &&
				ComponentUtil.getSessaoHome().getInstance().getDataAberturaSessao() != null);
	}
	
	public boolean exibeMenuItemRetirarJulgamento(SessaoJulgamentoDTO dto) {
		return !isJulgamentoFinalizado(dto.getSessaoPautaProcessoTrf()) && ((verificaCondicaoAguardandoJulgamento(dto) ||
				verificaCondicaoEmJulgamento(dto) || 
				verificaCondicaoJulgado(dto) || 
				verificaCondicaoAdiado(dto) || 
				verificaCondicaoPedidoVista(dto)) &&
				ComponentUtil.getSessaoHome().getInstance().getDataAberturaSessao() != null);
	}
	
	public boolean exibeMenuItemRegistrarJulgamento(SessaoJulgamentoDTO dto) {
		return !isJulgamentoFinalizado(dto.getSessaoPautaProcessoTrf()) && ((verificaCondicaoAguardandoJulgamento(dto) ||
				verificaCondicaoRetiradoJulgamento(dto) || 
				verificaCondicaoEmJulgamento(dto) ||
				verificaCondicaoAdiado(dto) ||
				verificaCondicaoPedidoVista(dto)) && 
				ComponentUtil.getSessaoHome().getInstance().getDataAberturaSessao() != null);		
	}
	
	public boolean exibeMenuItemTornarPendenteJulgamento(SessaoJulgamentoDTO dto) {
		return !isJulgamentoFinalizado(dto.getSessaoPautaProcessoTrf()) && (verificaCondicaoRetiradoJulgamento(dto) || 
				verificaCondicaoEmJulgamento(dto) || 
				verificaCondicaoJulgado(dto) || 
				verificaCondicaoPedidoVista(dto)) ;		
	}
	
	public boolean exibeMenuItemAdiarProximaSessao(SessaoJulgamentoDTO dto) {
		return !isJulgamentoFinalizado(dto.getSessaoPautaProcessoTrf()) && (verificaCondicaoAguardandoJulgamento(dto) || 
				verificaCondicaoRetiradoJulgamento(dto) || 
				verificaCondicaoEmJulgamento(dto) || 
				verificaCondicaoJulgado(dto) ||
				verificaCondicaoPedidoVista(dto));
	}
	
	public boolean exibeMenuItemPedidoVista(SessaoJulgamentoDTO dto) {
		return !isJulgamentoFinalizado(dto.getSessaoPautaProcessoTrf()) && ((verificaCondicaoAguardandoJulgamento(dto) ||
				verificaCondicaoRetiradoJulgamento(dto) || 
				verificaCondicaoEmJulgamento(dto) || 
				verificaCondicaoJulgado(dto) ||
				verificaCondicaoAdiado(dto)) && 
				ComponentUtil.getSessaoHome().getInstance().getDataAberturaSessao() != null);
	}

	private boolean verificaCondicaoAguardandoJulgamento(SessaoJulgamentoDTO dto) {
		return (dto.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.AJ) && 
				dto.getOrgaoJulgadorVencedor() == null &&
				dto.getRetiradoJulgamento() == false &&
				dto.getAdiadoVista() == null);
	}
	
	private boolean verificaCondicaoRetiradoJulgamento(SessaoJulgamentoDTO dto) {
		return TipoSituacaoPautaEnum.NJ.equals(dto.getSituacaoJulgamento())
				&& AdiadoVistaEnum.AD.equals(dto.getAdiadoVista()) && dto.getRetiradoJulgamento() == Boolean.TRUE;
	}
	
	private boolean verificaCondicaoJulgado(SessaoJulgamentoDTO dto) {
		return TipoSituacaoPautaEnum.JG.equals(dto.getSituacaoJulgamento()) && dto.getAdiadoVista() == null;
	}
	
	private boolean verificaCondicaoAdiado(SessaoJulgamentoDTO dto) {
		return TipoSituacaoPautaEnum.NJ.equals(dto.getSituacaoJulgamento())
				&& AdiadoVistaEnum.AD.equals(dto.getAdiadoVista()) && !dto.getRetiradoJulgamento();
	}
	
	private boolean verificaCondicaoPedidoVista(SessaoJulgamentoDTO dto) {
		return TipoSituacaoPautaEnum.NJ.equals(dto.getSituacaoJulgamento())
				&& AdiadoVistaEnum.PV.equals(dto.getAdiadoVista());
	}
	
	private boolean verificaCondicaoEmJulgamento(SessaoJulgamentoDTO dto) {
		return (dto.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.EJ) &&
				dto.getAdiadoVista() == null && 
				!dto.getRetiradoJulgamento());
	}
	
	private boolean verificaCondicoesJulgados(SessaoJulgamentoDTO dto) {
		return verificaCondicaoRetiradoJulgamento(dto) || verificaCondicaoJulgado(dto) || verificaCondicaoPedidoVista(dto) || verificaCondicaoAdiado(dto);
	}

	public List<SessaoComposicaoOrdem> getListaComposicaoOrdemGV() {
		if(listaComposicaoOrdemGV.isEmpty()) {
			listaComposicaoOrdemGV.addAll(ComponentUtil.getSessaoPautaProcessoTrfHome().getListaComposicaoOrdemGV());
		}
		return listaComposicaoOrdemGV;
	}
	
	/**
	 * Método responsável por gravar a proclamação do julgamento nos processos selecionados pelo usuário
	 * @param finalizar Informa se o sistema deve finalizar o registro da problamação do julgamento 
	 */
	public void gravaDecisao(boolean finalizar) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().gravaDecisao(finalizar);
		
		if (rowAtualModal != null) {
			if(!verificaCondicoesJulgados(rowAtualModal)) {
				marcarAtualizacoes();
			}
			updateSPPT(rowAtualModal);
		} else {
			List<SessaoJulgamentoDTO> listaDtos = converteSessaoPautaProcessoTrf(ComponentUtil.getSessaoPautaProcessoTrfHome().getListaSCO());
			
			for (SessaoJulgamentoDTO dto : listaDtos) {
				updateSPPT(dto);
			}
			marcarAtualizacoes();
		}
		
		ComponentUtil.getSessaoPautaProcessoTrfHome().limpaDecisoes();
	}
	
	/**
	 * Método responsável por gravar o pedido de vista nos processos selecionados pelo usuário
	 * @param finalizar Informa se o sistema deve finalizar o registro da problamação do julgamento 
	 */
	public void gravaVista(boolean finalizar) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().gravaVista(finalizar);
		
		if (rowAtualModal != null) {
			if(!verificaCondicoesJulgados(rowAtualModal)) {
				marcarAtualizacoes();
			}
			updateSPPT(rowAtualModal);
		} else {
			List<SessaoJulgamentoDTO> listaDtos = converteSessaoPautaProcessoTrf(ComponentUtil.getSessaoPautaProcessoTrfHome().getListaSCO());
			
			for (SessaoJulgamentoDTO dto : listaDtos) {
				updateSPPT(dto);
			}			
			marcarAtualizacoes();
		}
		
		ComponentUtil.getSessaoPautaProcessoTrfHome().limpaDecisoes();
	}

	public Set<Integer> getKeyToUpdate() {
		return keyToUpdate;
	}

	public void setKeyToUpdate(Set<Integer> keyToUpdate) {
		this.keyToUpdate = keyToUpdate;
	}
	
	/**
	 * metodo responsavel por receber a chamada de atualizacao de uma linha da tabela.
	 * @param row - linha da tabela a ser atualizada
	 */
	public void updateSPPT(SessaoJulgamentoDTO row) {
		if (row != null) {
			atualizarCamposDinamicos(row);
			updateSPPTEspecifico(row);
		}
	}
	
	private void updateSPPTEspecifico(SessaoJulgamentoDTO row) {
		Integer spptTableIndex = -1;

		if (this.processosEmPautaDTOs.contains(row)) {
			spptTableIndex = this.processosEmPautaDTOs.indexOf(row);
			this.processosEmPautaDTOs.set(spptTableIndex, row);
		} else if (this.processosJulgadosDTOs.contains(row)) {
			spptTableIndex = this.processosJulgadosDTOs.indexOf(row);
			this.processosJulgadosDTOs.set(spptTableIndex, row);
		}
		
		if (spptTableIndex != -1) {
			row.setHoraUltimaAtualizacaoDadosTela(formatterHora.format(new Date()));
		}

		keyToUpdate = Collections.singleton(spptTableIndex);
	}

	private void atualizarCamposDinamicos(SessaoJulgamentoDTO row) {
		atualizarCampoSituacaoJulgamento(row);
		atualizarCampoAdiadoVista(row);
		atualizarCampoRetiradoJulgamento(row);
		row.setOrgaoJulgadorRetiradaJulgamento(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarOrgaoJulgadorRetiradaJulgamento(row.getProcessoTrf(), row.getSessao()));
		row.setOrgaoJulgadorVencedorJulgamento(ComponentUtil.getSessaoPautaProcessoTrfManager().buscarOrgaoJulgadorVencedorJulgamento(row.getProcessoTrf(), row.getSessao()));
		atualizarCampoPreferencia(row);
		atualizarCampoJulgamentoCelere(row);
		atualizarCampoSustentacaoOral(row);
		this.atualizarCampoProclamacaoDecisao(row);
		this.atualizarCampoQuantidadeAnotacoes(row);
	}
	
	public void addListaSCO(SessaoJulgamentoDTO row) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().addListaSCO(row.getSessaoPautaProcessoTrf());
		updateSPPTEspecifico(row);
	}
	
	public void switchCheckAll() {
		SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome = ComponentUtil.getSessaoPautaProcessoTrfHome();
		sessaoPautaProcessoTrfHome.getListaSCO().clear();
		
		if(sessaoPautaProcessoTrfHome.getCheckAll()) {
			sessaoPautaProcessoTrfHome.setCheckAllAlternativo(false);
		} else {
			sessaoPautaProcessoTrfHome.setCheckAllAlternativo(true);
		}
		

		if (sessaoPautaProcessoTrfHome.getCheckAll()) {
			for (SessaoJulgamentoDTO dto : processosEmPautaDTOs) {
				if (!dto.getSessaoPautaProcessoTrf().getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)) {
					sessaoPautaProcessoTrfHome.getListaSCO().add(dto.getSessaoPautaProcessoTrf());
				}
			}
		}		
	}
	
	public boolean getCheckOutAll() {
		return ComponentUtil.getSessaoPautaProcessoTrfHome().getCheckAll();
	}
	
	public void abrirModalAnotacao(SessaoJulgamentoDTO dto) {
		ComponentUtil.getSessaoPautaProcessoTrfHome().acaoBtnLegenda("anotacao", dto.getSessaoPautaProcessoTrf());
		this.rowAtualModal = dto;
	}
	
	public void fecharModalAnotacao() {
		if (this.rowAtualModal != null) {
			this.atualizarCampoQuantidadeAnotacoes(this.rowAtualModal);
			this.updateSPPTEspecifico(this.rowAtualModal);
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().setMostrarNota(Boolean.FALSE);
	}

	public void fecharModalSustentacaoOral() {
		if (this.rowAtualModal != null) {
			this.atualizarCampoSustentacaoOral(this.rowAtualModal);
			this.updateSPPTEspecifico(this.rowAtualModal);
		}
		ComponentUtil.getSessaoPautaProcessoTrfHome().setMostraSustentacaoOral(Boolean.FALSE);
	}
	
	public void gravaAdvogadoSustentacaoOral() {
		ComponentUtil.getSessaoPautaProcessoTrfHome().gravaAdvogadoSustentacaoOral();
	}
	
	public void gravaAdvogadoSustentacaoOralFechaModal() {
		this.gravaAdvogadoSustentacaoOral();
		this.fecharModalSustentacaoOral();
	}
	
	public void cadastrarSessaoPautaProcessoTrf() {
		String msgCadastroPauta = ComponentUtil.getSessaoHome().cadastrarSessaoPautaProcessoTrf();
		if (msgCadastroPauta == null) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Processo cadastrado com sucesso.");
		}		
		processosEmPautaDTOs.clear();
	}
	
	public String getHoraUltimaAtualizacaoResultadoSessao() {
		return horaUltimaAtualizacaoResultadoSessao;
	}
	
	public void atualizarResultadoSessao() {
		setHoraUltimaAtualizacaoResultadoSessao(new Date());
	}
	
	public void setHoraUltimaAtualizacaoResultadoSessao(Date hora) {		
		horaUltimaAtualizacaoResultadoSessao = formatterHora.format(hora);
	}

	public TipoVoto getTipoVotoRelator() {
		return tipoVotoRelator;
	}

	public void setTipoVotoRelator(TipoVoto tipoVotoRelator) {
		this.tipoVotoRelator = tipoVotoRelator;
	}

	public SessaoResultadoVotacaoEnum getSessaoResultadoVotacaoEnum() {
		return sessaoResultadoVotacaoEnum;
	}

	public void setSessaoResultadoVotacaoEnum(SessaoResultadoVotacaoEnum sessaoResultadoVotacaoEnum) {
		this.sessaoResultadoVotacaoEnum = sessaoResultadoVotacaoEnum;
	}
	
	public TipoInclusaoEnum getTipoInclusaoEnum() {
		return tipoInclusaoEnum;
	}

	public void setTipoInclusaoEnum(TipoInclusaoEnum tipoInclusaoEnum) {
		this.tipoInclusaoEnum = tipoInclusaoEnum;
	}

	public SituacaoProcessoSessaoEnum getSituacaoProcEnum() {
		return situacaoProcEnum;
	}

	public void setSituacaoProcEnum(SituacaoProcessoSessaoEnum situacaoProcEnum) {
		this.situacaoProcEnum = situacaoProcEnum;
	}

	public Boolean getPossuiProclamacaoAntecipada() {
		return possuiProclamacaoAntecipada;
	}

	public void setPossuiProclamacaoAntecipada(Boolean possuiProclamacaoAntecipada) {
		this.possuiProclamacaoAntecipada = possuiProclamacaoAntecipada;
	}

	public boolean getAtualizarProcessos() {
		return atualizarProcessos;
	}

	public void setAtualizarProcessos(boolean atualizarProcessos) {
		this.atualizarProcessos = atualizarProcessos;
	}

	public boolean isAtualizarProcessosJulgados() {
		return atualizarProcessosJulgados;
	}

	public void setAtualizarProcessosJulgados(boolean atualizarProcessosJulgados) {
		this.atualizarProcessosJulgados = atualizarProcessosJulgados;
	}
	
	public ModeloProclamacaoJulgamento getModeloProclamacaoJulgamento() {
		return modeloProclamacaoJulgamento;
	}

	public void setModeloProclamacaoJulgamento(ModeloProclamacaoJulgamento modeloProclamacaoJulgamento) {
		this.modeloProclamacaoJulgamento = modeloProclamacaoJulgamento;
	}

	public void marcarAtualizacoes() {
		setAtualizarProcessos(true);
		setAtualizarProcessosJulgados(true);
	}

	public List<ModeloProclamacaoJulgamento> getListagemModeloProclamacaoJulgamento() {
		if (this.listagemModeloProclamacaoJulgamento.isEmpty()) {
			this.listagemModeloProclamacaoJulgamento = ComponentUtil.getComponent(ModeloProclamacaoJulgamentoList.class).list();
		}
		return this.listagemModeloProclamacaoJulgamento;
	}

	public boolean podeFinalizarJulgamento(SessaoPautaProcessoTrf sessaoPauta) {
		return ComponentUtil.getSessaoPautaProcessoTrfManager().podeFinalizarJulgamento(sessaoPauta);	
	}
	
	public boolean isJulgamentoFinalizado(SessaoPautaProcessoTrf sessaoPauta) {
		return sessaoPauta.isJulgamentoFinalizado();
	}
	
	public boolean permiteEmissaoCertidao(SessaoPautaProcessoTrf sessaoPauta) {
		return isJulgamentoFinalizado(sessaoPauta);
	}
	
	public void confirmarEncerramentoJulgamento(SessaoJulgamentoDTO sessaoDTO) {
		setExibeMPConfirmacaoEncerrarJulgamento(true);
		rowAtualModal = sessaoDTO; 
	}
	private SessaoPautaProcessoTrf recuperarSessaoPauta(SessaoPautaProcessoTrf sessaoPauta) {
		SessaoPautaProcessoTrf retorno = null;
		if(sessaoPauta == null) { 
			if(rowAtualModal != null && rowAtualModal.getSessaoPautaProcessoTrf() != null) {
				retorno = rowAtualModal.getSessaoPautaProcessoTrf();
			} 
		} else {
			retorno = sessaoPauta;
		}
		return retorno;
	}
	
	public void encerrarJulgamento(SessaoPautaProcessoTrf sessaoPauta) {
		setExibeMPConfirmacaoEncerrarJulgamento(false);
		sessaoPauta = recuperarSessaoPauta(sessaoPauta);
		if(sessaoPauta == null) { 
			FacesMessages.instance().add(Severity.ERROR, "No foi possvel recuperar os detalhes do processo pautado");
		} else {
			try {
				ComponentUtil.getSessaoPautaProcessoTrfManager().encerrarJulgamento(sessaoPauta, false, true);
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar encerrar julgamento do processo: " + e.getLocalizedMessage() + " " + e.getMessage());
			}
		}
	}
	
	public boolean isExibeMPConfirmacaoEncerrarJulgamento() {
		return exibeMPConfirmacaoEncerrarJulgamento;
	}

	public void setExibeMPConfirmacaoEncerrarJulgamento(boolean exibeMPConfirmacaoEncerrarJulgamento) {
		this.exibeMPConfirmacaoEncerrarJulgamento = exibeMPConfirmacaoEncerrarJulgamento;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}
	
	public SessaoJulgamentoFiltroDTO getSessaoJulgamentoFiltroDTO() {
		return new SessaoJulgamentoFiltroDTO(numeroProcesso, campoAssunto, campoClasse, nomeParte, codigoIMF, codigoOAB,
				prioridade, orgaoFiltro, dataInicialDistribuicao, dataFinalDistribuicao, tipoVotoRelator,
				sessaoResultadoVotacaoEnum, tipoInclusaoEnum, situacaoProcEnum, possuiProclamacaoAntecipada,
				tipoPessoa);
	}
	
	public void filtrarProcessos() {
		marcarAtualizacoes();
	}
}