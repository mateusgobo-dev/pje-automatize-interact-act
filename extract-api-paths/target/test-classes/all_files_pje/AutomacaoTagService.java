package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import br.com.infox.pje.manager.ProcessoTrfManager;
import org.apache.commons.lang3.ObjectUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.def.Task;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.manager.SituacaoProcessoManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.CriterioFiltroManager;
import br.jus.cnj.pje.nucleo.manager.FiltroManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTagManager;
import br.jus.cnj.pje.nucleo.manager.TagManager;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.FiltroDTO;
import br.jus.pje.nucleo.entidades.CriterioFiltro;
import br.jus.pje.nucleo.entidades.Filtro;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tag;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoCriterioEnum;

@Name("automacaoTagService")
@Scope(ScopeType.APPLICATION)
public class AutomacaoTagService implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AutomacaoTagService.class.getName());
	public static final String EVENTO_AUTOMACAO_TAG = "pje2:eventoAutomacaoTag";
	public static final String EVENTO_AUTOMACAO_TAG_IDPESSOA = "pje2:eventoAutomacaoTagPorIdPessoa";
	private static final String EVENTO_APOS_TRANSITAR = "pje:eventos:tarefa:transitar";
	private static final String EVENTO_APOS_TRANSITAR_ASSINCRONO = "pje:eventos:tarefa:transitar:assincrono";

	private enum TipoAcaoEnum {
		ADICIONAR,
		REMOVER
	}
	
	private class AutomacaoTagDTO implements Serializable {
		private static final long serialVersionUID = 1L;
		private TipoAcaoEnum acao;
		private Integer idProcessoTrf;
		private List<Integer> idLocalizacaoFisicaList;
		
		public AutomacaoTagDTO(TipoAcaoEnum acao, Integer idProcessoTrf, List<Integer> idLocalizacaoFisicaList) {
			this.acao = acao;
			this.idProcessoTrf = idProcessoTrf;
			this.idLocalizacaoFisicaList = idLocalizacaoFisicaList;
		}

		public TipoAcaoEnum getAcao() {
			return acao;
		}
		public Integer getIdProcessoTrf() {
			return idProcessoTrf;
		}

		public List<Integer> getIdLocalizacaoFisicaList() {
			return idLocalizacaoFisicaList;
		}		
	}

	@Observer(EVENTO_AUTOMACAO_TAG_IDPESSOA)
	@Transactional
	public void vincularTagAoProcessoAssincronoPorPessoa(int idPessoa) {
		ProcessoTrfManager.instance().consultarIdProcessoDistribuidoPorIdPessoaAtivo(idPessoa).stream().forEach(
				p -> Events.instance().raiseEvent(EVENTO_AUTOMACAO_TAG, p));
	}


	@Observer(EVENTO_AUTOMACAO_TAG)
	@Transactional
	public void vincularTagAoProcessoAssincrono(int idProcessoTrf) {
		ProcessoTrf p = EntityUtil.find(ProcessoTrf.class, idProcessoTrf);
		// Se no foi sequer distribudo, no faz nada
		if (!isProcessoDevidamenteAutuado(p)) {
			return;
		}
		
		// Limpa as etiquetas de automao do processo
		ComponentUtil.getComponent(TagManager.class).excluirVinculosProcessoTag(idProcessoTrf);
		// Varre as etiquetas disponveis para ver se alguma deve ser aplicada ao
		// processo
		List<Tag> tagList = listaTags(p);
		for (Tag tag : tagList) {
			if (processoFiltradoEmTag(p, tag, null)) {
				addTagProcesso(tag, p);
			}
		}
		EntityUtil.flush();
	}
	
	private boolean isProcessoDevidamenteAutuado(ProcessoTrf processoTrf) {
		return processoTrf != null && processoTrf.getIdProcessoTrf() > 0 && ProcessoStatusEnum.D.equals(processoTrf.getProcessoStatus()) && processoTrf.getDataDistribuicao() != null;
	}

	
	/**
	 * Retorna verdadeiro se todos os filtros/regras da Tag forem atendidos. 
	 * Um filtro/regra é considerado atendido se pelo um dos seus critérios/condições o for.
	 */
	public boolean processoFiltradoEmTag(ProcessoTrf processoTrf, Tag tag, List<SituacaoProcesso> situacaoProcessoList) {
		return processoFiltradoEmTag(processoTrf, tag, null, situacaoProcessoList);
	}

	public boolean processoFiltradoEmTag(ProcessoTrf processoTrf, Tag tag, List<Filtro> filtrosTagList, List<SituacaoProcesso> situacaoProcessoList) {
		List<Integer> idFiltroList = getIdFiltros(tag, filtrosTagList);
		boolean filtrado = false;
		for (Integer idFiltro : idFiltroList) {
			boolean peloMenoUm = false;
			List<CriterioFiltro> criterios = getCriteriosFiltros(filtrosTagList, idFiltro);
			for (CriterioFiltro criterio : criterios) {
				peloMenoUm = verificarCriterios(processoTrf, criterio, situacaoProcessoList);
				if(peloMenoUm) {
					break;
				}
			}			
			
			filtrado = peloMenoUm;
			if (!filtrado) {
				break;
			}
		}
		return filtrado;
	}
	
	private boolean verificarCriterios(ProcessoTrf processoTrf, CriterioFiltro criterio, List<SituacaoProcesso> situacaoProcessoList) {	
		if (TipoCriterioEnum.TA.equals(criterio.getTipoCriterio()) && ProjetoUtil.isVazio(situacaoProcessoList)) {
			situacaoProcessoList = ComponentUtil.getComponent(SituacaoProcessoManager.class).getByProcessoSemFiltros(processoTrf.getIdProcessoTrf());
		}
		if (TipoCriterioEnum.MO.equals(criterio.getTipoCriterio())) {
			ProcessoEvento ultimoMovimento = ComponentUtil.getComponent(ProcessoEventoManager.class).recuperaUltimaMovimentacao(processoTrf);
			if (criterio.getTipoCriterio().isMatched(processoTrf, criterio, ultimoMovimento)) {
				return true;
			}
		}
		if (TipoCriterioEnum.DO.equals(criterio.getTipoCriterio())) {
			ProcessoDocumento ultimoDocumento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).getUltimoProcessoDocumentoPrincipalAtivo(processoTrf.getProcesso());
			if (criterio.getTipoCriterio().isMatched(processoTrf, criterio, ultimoDocumento)) {
				return true;
			}
		}	
		return criterio.getTipoCriterio().isMatched(processoTrf, criterio, situacaoProcessoList);
	}
	

	private List<CriterioFiltro> getCriteriosFiltros(List<Filtro> filtrosTagList, Integer idFiltro) {
		List<CriterioFiltro> criterios;
		if (ProjetoUtil.isVazio(filtrosTagList)) {
			criterios = ComponentUtil.getComponent(CriterioFiltroManager.class).recuperarCriteriosEntitiesPorIdFiltro(idFiltro);
		} else {
			criterios = filtrosTagList.parallelStream()
					.filter(f -> f.getId().equals(idFiltro))
					.distinct()
					.map(Filtro::getCriterios)
					.flatMap(Set::stream)
					.collect(Collectors.toList());
		}
		return criterios;
	}

	private List<Integer> getIdFiltros(Tag tag, List<Filtro> filtrosTagList) {
		List<Integer> idFiltroList;
		if (ProjetoUtil.isVazio(filtrosTagList)) {
			idFiltroList = ComponentUtil.getComponent(FiltroManager.class).listarFiltrosByTag(tag.getId()).parallelStream()
					.map(FiltroDTO::getId)
					.collect(Collectors.toList());
		} else {
			idFiltroList = filtrosTagList.parallelStream()
					.map(Filtro::getId)
					.collect(Collectors.toList());
		}
		return idFiltroList;
	}

	private List<Tag> listaTags(ProcessoTrf p) {
		ArrayList<Integer> idsLocalizacoes = new ArrayList<Integer>();
		ArrayList<Integer> idsAncestrais = new ArrayList<Integer>();
		
		//Pega todas as localizações em que há tarefa para o processo 
		idsLocalizacoes.addAll(ComponentUtil.getComponent(ProcessoJudicialManager.class).pegaIdsLocalizacaoProcessoTarefa((long)p.getIdProcessoTrf()));
		if(idsLocalizacoes.isEmpty() && Objects.nonNull(p.getOrgaoJulgador()) && ObjectUtils.notEqual(p.getOrgaoJulgador(), null))
		{
			idsLocalizacoes.add(p.getOrgaoJulgador().getLocalizacao().getIdLocalizacao());
		}
		//Monta a árvore de localizações a serem consideradas
		for (Integer idLocalizacao: idsLocalizacoes) {
			idsAncestrais.addAll(ComponentUtil.getComponent(LocalizacaoService.class).obterIdsAncestrais(idLocalizacao));
		}
		
		return ComponentUtil.getComponent(TagManager.class).listarTagsParaAutomacao(idsAncestrais, p.getIdProcessoTrf());
	}
	
	public void addTagProcesso(Tag tag, ProcessoTrf processo){
		if (tag != null && processo != null){
			try{
				ComponentUtil.getComponent(ProcessoTagManager.class).associarProcessoTag((long)processo.getIdProcessoTrf(), tag, null);
			} catch (PJeBusinessException | PJeDAOException e) {
				//swallow
			}
		}
	}
	
	@Observer(Event.EVENTTYPE_TRANSITION)
	public void filtrarProcessosParaAutomacaoEtiquetasDeTarefa(ExecutionContext context){
		Transition t = (Transition) context.getEventSource();
		Task[] tasks = JbpmUtil.getTasksFromTransition(t);
		Task from = tasks[JbpmUtil.FROM_TASK_TRANSITION];
		Task to = tasks[JbpmUtil.TO_TASK_TRANSITION];
		if (from == null && to == null) {
			return;
		}
		Integer idProcesso = (Integer) context.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
		ProcessoTrf processoTrf;
		if (idProcesso == null || idProcesso == 0) {
			processoTrf = ProcessoTrfHome.instance().getInstance();
		} else {
			processoTrf = EntityUtil.getEntityManager().find(ProcessoTrf.class, idProcesso);
		}
		
		if (!isProcessoDevidamenteAutuado(processoTrf)) {
			return;
		}

		OrgaoJulgador oj = processoTrf.getOrgaoJulgador();
		Integer idLocalizacao = oj != null ? oj.getLocalizacao().getIdLocalizacao() : null;
		idLocalizacao = atualizarLocalizacaoSeDeslocada(context, idLocalizacao);

		List<Integer> idLocalizacaoFisicaList = Stream.of(idLocalizacao, Authenticator.getIdLocalizacaoAtual()).filter(Objects::nonNull).collect(Collectors.toList());
		if (ProjetoUtil.isVazio(idLocalizacaoFisicaList)) {
			return;
		}
		if (to != null) {
			Events.instance().raiseTransactionSuccessEvent(EVENTO_APOS_TRANSITAR, new AutomacaoTagDTO(TipoAcaoEnum.ADICIONAR, processoTrf.getIdProcessoTrf(), idLocalizacaoFisicaList));
		}
		if (from != null) {
			Events.instance().raiseTransactionSuccessEvent(EVENTO_APOS_TRANSITAR, new AutomacaoTagDTO(TipoAcaoEnum.REMOVER, processoTrf.getIdProcessoTrf(), idLocalizacaoFisicaList));
		}
	}
	
	@Observer(EVENTO_APOS_TRANSITAR)
	public void processarTags(AutomacaoTagDTO dto) {
		Events.instance().raiseAsynchronousEvent(EVENTO_APOS_TRANSITAR_ASSINCRONO, dto);
	}
	
	@SuppressWarnings("unchecked")
	@Observer(EVENTO_APOS_TRANSITAR_ASSINCRONO)
	@Transactional
	public void processarTagsAssincrono(AutomacaoTagDTO dto) {
		StringBuilder hql = new StringBuilder();
		hql.append(" select distinct f from Filtro f join fetch f.criterios c join fetch f.tags ")
			.append(" where f.id in ")
			.append("(select f2.id from Filtro f2 join f2.criterios c2 where f2.idLocalizacao in (:idLocalizacaoFisicaList)) ");
		Query query = EntityUtil.createQuery(hql.toString());
		query.setParameter("idLocalizacaoFisicaList", dto.getIdLocalizacaoFisicaList());
		List<Filtro> filtroList = query.getResultList();

		if (!filtroList.isEmpty()) {
			try {
				List<SituacaoProcesso> situacaoList = ComponentUtil.getComponent(SituacaoProcessoManager.class).getByProcessoSemFiltros(dto.getIdProcessoTrf());
				List<ProcessoTag> tagAtualProcessoList = ComponentUtil.getComponent(ProcessoTagManager.class).listarTags((long)dto.getIdProcessoTrf());
				filtroList.stream()
					.distinct()
					.map(Filtro::getCriterios)
					.flatMap(Set::stream)
					.filter(c -> TipoCriterioEnum.TA.equals(c.getTipoCriterio()))
					.map(CriterioFiltro::getFiltro)
					.map(Filtro::getTags)
					.flatMap(List::stream)
					.distinct()
					.forEach(t -> avaliarTagParaAplicacao(t, dto.getIdProcessoTrf(), dto.getAcao(), filtroList, situacaoList, tagAtualProcessoList));
				EntityUtil.flush();
			} catch (Exception e) {
				logger.severe(MessageFormat.format("Erro ao processar etiquetas ao transitar processo de id {0}: {1}", dto.getIdProcessoTrf(), e.getMessage()));
			}
		}
	}
	
	private void avaliarTagParaAplicacao(Tag t, Integer idProcessoTrf, TipoAcaoEnum acao, List<Filtro> filtrosList, List<SituacaoProcesso> situacaoList, List<ProcessoTag> tagAtualProcessoList) {
		try {
			List<Filtro> filtrosTarefaList = filtrosList.parallelStream()
					.filter(f -> f.getTags().parallelStream().anyMatch(tag -> tag.equals(t))).collect(Collectors.toList());
			ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, idProcessoTrf);
			ProcessoTagManager processoTagManager = ComponentUtil.getComponent(ProcessoTagManager.class);
			if (processoFiltradoEmTag(processoTrf, t, filtrosTarefaList, situacaoList)) {
				if (TipoAcaoEnum.ADICIONAR.equals(acao) && tagAtualProcessoList.parallelStream().noneMatch(pt -> pt.getTag().getId().equals(t.getId()))) {
					processoTagManager.inserirProcessoTag((long) processoTrf.getIdProcessoTrf(), t.getNomeTag(), t.getIdLocalizacao(), null, false);
				}
			} else if (TipoAcaoEnum.REMOVER.equals(acao) && tagAtualProcessoList.parallelStream().anyMatch(pt -> pt.getTag().getId().equals(t.getId()))) {
				processoTagManager.removerTag((long) processoTrf.getIdProcessoTrf(), t.getId(), false);
			}
		} catch (PJeBusinessException e) {
			logger.warning(MessageFormat.format("Erro ao avaliar ao {0} da etiqueta {1} no processo de id {2}: {3}", acao, t.getNomeTag(), idProcessoTrf, e.getMessage()));
		}
	}

	private Integer atualizarLocalizacaoSeDeslocada(ExecutionContext context, Integer idLocalizacao) {
		Long idProcessInstance = context.getProcessInstance().getId();
		
		if(idProcessInstance != null){
			ProcessoInstanceManager processoInstanceManager = (ProcessoInstanceManager)Component.getInstance("processoInstanceManager");
			try {
				ProcessoInstance pi = processoInstanceManager.findById(idProcessInstance);
				if(pi != null && pi.getIdLocalizacao() != null){
					idLocalizacao = pi.getIdLocalizacao();
				}
			} catch (PJeBusinessException e) {
				//swallow
			}
		}
		return idLocalizacao;
	}
}
