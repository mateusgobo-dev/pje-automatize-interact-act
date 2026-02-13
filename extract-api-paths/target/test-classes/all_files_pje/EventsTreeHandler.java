package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.function.RichFunction;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.EventoAgrupamentoManager;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.servicos.ILancadorMovimentosAction;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.SujeitoAtivoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe que realiza a tarefa de registrar os eventos e exibir a tree
 * proveniente de uma lista de Agrupamentos.
 * 
 * @author Infox
 * 
 */
@Name(EventsTreeHandler.NAME)
@Scope(ScopeType.PAGE)
public class EventsTreeHandler extends AbstractTreeHandler<Evento> {
	
	private static final LogProvider log = Logging.getLogProvider(EventsTreeHandler.class);
	private static final long serialVersionUID = 1L;

	public static final String NAME = "eventsTree";
	public static final String REGISTRA_EVENTO_EVENT = "EvenstTreeHandler.registrarEvento";
	public static final String REGISTRA_EVENTO_EVENT_SEM_BPM = "EventsTreeHandler.registrarEventoSemBpm";
	public static final String REGISTRA_EVENTO_PD_EVENT = "EvenstTreeHandler.registrarEventoProcessoDocumento";
	public static final String AFTER_REGISTER_EVENT = "afterRegisterEvents";
	public static final String AFTER_DELETE_EVENT = "EvenstTreeHandler.afterDeleteEvent";
	public static final String GRAVAR_ALERTA_EVENT = "gravarAlertaEvent";
	public static final String EH_CLASSE_CRIMINAL_EVENT = "ehClasseCriminalEvent";

	private Map<Evento, List<Evento>> rootsSelectedMap = new HashMap<Evento, List<Evento>>();
	private List<EventoBean> eventoBeanList = new ArrayList<EventoBean>();

	private List<EventoBean> eventosCriminaisSelected = new ArrayList<EventoBean>();
	private List<Evento> eventos = new ArrayList<>();

	private String idEventos;
	private boolean isAllRegistred;
	private boolean renderTree = true;
	private boolean registred;
	private boolean modalAberto;
	private Boolean forceExpandTree;
	private String alertMessage;

	private Integer agrupamentosInstance;
	private String paramPesquisaInstance;
	private EventoBean selectedEventoBean;
	private Boolean complementosValidos = false;

	private Boolean gravarVariavelFluxo = Boolean.TRUE;


	public EventsTreeHandler() {
		gravarVariavelFluxo = Boolean.TRUE;
	}

	public EventsTreeHandler(Boolean gravarVariavelFluxo) {
		this.gravarVariavelFluxo = gravarVariavelFluxo;
	}

	public EventoBean getSelectedEventoBean() {
		return selectedEventoBean;
	}

	public void setSelectedEventoBean(EventoBean selectedEventoBean) {
		this.selectedEventoBean = selectedEventoBean;
	}
	
	public static EventsTreeHandler instance() {
		return (EventsTreeHandler) org.jboss.seam.Component.getInstance(EventsTreeHandler.NAME);
	}

	/**
	 * Verifica se existe eventos a serem selecionados na treeView, caso todos
	 * existentes sejam obrigatórios a lista dos roots será vazia.
	 */
	public List<EntityNode<Evento>> getRoots(Integer agrupamentos) {
		List<EntityNode<Evento>> resultList;
		
		ILancadorMovimentosAction lancadorAction = 
				(ILancadorMovimentosAction) Component.getInstance(ILancadorMovimentosAction.NAME);
		
		boolean estaLancandoMovimentosTemporariamente = podeRegistrarMovimentosTemporarios() && lancadorAction.deveGravarTemporariamente();
		
		if (estaLancandoMovimentosTemporariamente && this.getListaMovimentosNecessitamHomologacao().size() > 0) {
			resultList = getRootsEventosSalvosTemporariamente(agrupamentos);
		} else {
			resultList = getRootsOriginal(agrupamentos);
		}
		
		if (CollectionUtilsPje.isEmpty(this.initialRootList)) {
			this.initialRootList = resultList;
		}
		
		return resultList;
	}
	
	public List<EntityNode<Evento>> getRootsOriginal(Integer agrupamentos) {
		if (this.agrupamentosInstance == null || this.agrupamentosInstance != agrupamentos) {
			this.agrupamentosInstance = agrupamentos;
			idEventos = getIdEventos(agrupamentos);
			rootList = new ArrayList<EntityNode<Evento>>();
			Query queryRoots = getEntityManager().createQuery(getQueryRoots(agrupamentos));
			EntityNode<Evento> entityNode = createNode();
			entityNode.setIgnore(getEntityToIgnore());
			rootList = entityNode.getRoots(queryRoots);
			renderTree = (rootList.size() > 0);
			if (!"".equals(idEventos)) {
				isAllRegistred = true;
			}
			initialRootList = null;
		}
		return rootList;
	}
	
	public List<EntityNode<Evento>> getRootsEventosSalvosTemporariamente(Integer agrupamentos) {
		// TODO Refatorar para usar composição e evitar acoplamento de código de lançadores filhos
		boolean deveRefazerInicializacao = (this.getAgrupamentosInstance() == null || !this.getAgrupamentosInstance().equals(agrupamentos));
		if(deveRefazerInicializacao) {
			clearList();
		}
		List<EntityNode<Evento>> roots = this.getRootsOriginal(agrupamentos); // Acoplamento necessário pela arquitetura de lancadores atual
		return roots;
	}
	
	public List<EventoBean> getListaMovimentosNecessitamHomologacao(){ 
		Processo processo = ProcessoHome.instance().getInstance();
		return getListaMovimentosNecessitamHomologacao(processo);
	}
	
	public List<EventoBean> getListaMovimentosNecessitamHomologacao(Processo processo){ 
		return getListaMovimentosNecessitamHomologacao(processo, TaskInstanceHome.instance().getTaskId());
	}
	
	public List<EventoBean> getListaMovimentosNecessitamHomologacao(Processo processo, Long taskId){ 
		List<EventoBean> retorno = null;		
		org.jbpm.graph.exe.ProcessInstance processInstance = taskId != null ? this.retornaProcessInstance(processo, taskId) : ProcessInstance.instance();

		if (processInstance != null){
			retorno = LancadorMovimentosService.instance().getMovimentosTemporarios(processInstance);
		}
		return retorno != null? retorno: new ArrayList<EventoBean>(0);
	}
	
	protected org.jbpm.graph.exe.ProcessInstance retornaProcessInstance(Processo processo){
		return retornaProcessInstance(processo, TaskInstanceHome.instance().getTaskId());
	}
	
	protected org.jbpm.graph.exe.ProcessInstance retornaProcessInstance(Processo processo, Long taskId){
		return ManagedJbpmContext.instance().getTaskInstance(taskId).getProcessInstance();
	}


	@Override
	public List<EntityNode<Evento>> getRoots() {
		return rootList;
	}

	@Override
	protected EntityNode<Evento> createNode() {
		return new EventsEntityNode(getQueryChildrenList());
	}

	/**
	 * Este método sobrecarregado irá ser usado pelo o getRoots() para obter a
	 * query que irá trazer os registros que deverão aparecer na treeView.
	 * 
	 * @param agrupamentos
	 *            Conjunto de ids dos EventoAgrupamentos a serem exibidos.
	 * @return Query para buscar esses EventoAgrupamentos informados.
	 */
	protected String getQueryRoots(Integer agrupamentos) {
		StringBuilder builder = new StringBuilder();
		builder.append("select ea from EventoAgrupamento ea ");

		if (agrupamentos != null && agrupamentos > 0) {
			builder.append("where ea.agrupamento.idAgrupamento in (");
			builder.append(agrupamentos);
			builder.append(") ");
		}

		// Forçar retorno vazio da query em casos de nenhum agrupamento passado
		else {
			builder.append(" where true = false ");
		}

		builder.append(" AND ea.evento.ativo = true ");
		builder.append("order by ea.evento");
		return builder.toString();
	}

	@Override
	protected String getQueryChildren() {
		String codigoInstancia = ParametroUtil.instance().getCodigoInstanciaAtual();
		StringBuilder queryChildren = new StringBuilder("SELECT e FROM Evento AS e WHERE e.eventoSuperior = :" + EntityNode.PARENT_NODE)
				.append(" AND ")
				.append("	(EXISTS")
				.append("  		(SELECT am.eventoProcessual ")
				.append("			FROM AplicacaoMovimento AS am, AplicabilidadeView ap, Evento eventosFolha ")
				.append(" 			WHERE 1=1 ")
				.append(" 			AND eventosFolha.faixaInferior >= e.faixaInferior AND eventosFolha.faixaSuperior <= e.faixaSuperior ")
				.append(" 			AND eventosFolha.ativo = true ")
				.append(" 			AND am.eventoProcessual.idEvento = eventosFolha.idEvento AND am.ativo is true ")
				.append("			AND am.aplicabilidade.idAplicabilidade IN (ap.idAplicabilidade) ")
				.append("			AND ap.orgaoJustica = #{tipoJustica} ")
				.append("			AND ap.codigoAplicacaoClasse = '" + codigoInstancia + "'");
		if(ParametroUtil.instance().isPrimeiroGrau()) {
			String sujeitoAtivoMonocratico = SujeitoAtivoEnum.M.getLabel();
			queryChildren.append("			AND ap.sujeitoAtivo = '" + sujeitoAtivoMonocratico + "' ");
		}
		queryChildren.append("		)")
				.append("   )")
				.append(" AND e.ativo is true ");
		
		queryChildren.append(" ORDER BY e.evento");

		return queryChildren.toString();
	}

	/**
	 * Método de implementação obrigatória, porém foi sobrescrevido o getRoots
	 * que chama outro QueryRoots(), por isso este perdeu sua funcionalidade
	 * nesta classe.
	 */
	@Override
	protected String getQueryRoots() {
		return null;
	}

	/**
	 * Retorna a lista dos Eventos Agrupamentos referente aos agrupamentos
	 * informados.
	 * 
	 * @return Uma lista com os ids dos eventos agrupamentos concatenados
	 */
	private String getIdEventos(Integer idAgrupamento) {
		if (idAgrupamento != null && idAgrupamento > 0) {
			StringBuilder idList = new StringBuilder();
			List<EventoAgrupamento> geList = ComponentUtil.getComponent(EventoAgrupamentoManager.class).recuperarEventoAgrupamentos(idAgrupamento);
			
			// atualizar a lista de eventos com possíveis movimentos já escolhidos anteriormente.
			// assim, seus complementos previamente ja preenchidos, se for o caso, são recuperados.
			getEventoBeanList();
			// se houver apenas 1 movimento folha no agrupador de movimentos selecionado pré-seleciona o movimento
			if(geList.size() == 1 && geList.get(0).getEvento().getEventoAtivoList().size() == 0) {
				EventoBean eb = new EventoBean();
				eb.setIdEvento(geList.get(0).getEvento().getIdEvento());
				eb.setCodEvento(geList.get(0).getEvento().getCodEvento());
				eb.setDescricaoMovimento(geList.get(0).getEvento().toString());
				eb.setDescricaoCompletaMovimento(geList.get(0).getEvento().getMovimento());
				String caminhoCompleto = geList.get(0).getEvento().getCaminhoCompleto();
				if(StringUtil.isEmpty(caminhoCompleto)) {
					caminhoCompleto = geList.get(0).getEvento().getPathDescription();
					if(StringUtil.isEmpty(caminhoCompleto)) {
						caminhoCompleto = geList.get(0).getEvento().getEvento();
					}
				}
				eb.setDescricaoCaminhoCompletoMovimento(caminhoCompleto);
				eb.setGlossario(geList.get(0).getEvento().getGlossario());
				
				eb.setMultiplo(geList.get(0).getMultiplo());
				eb.setExcluir(true);
				eb.setQuantidade(1);

				preencherEventoBean(eb);
				
				if(!eventoBeanList.contains(eb)){
					eventoBeanList.add(eb);
				}
				sincronizarEventoVariavel(eventoBeanList);
				idList.append(geList.get(0).getEvento().getIdEvento());				

				return idList.toString();
			}
		}

		return "";
	}

	/**
	 * Método que retira da lista e do map dos eventos selecionados o evento
	 * informado no parametro, ele também verifica se é o último evento folha
	 * contido na lista do map, caso sim, ele também remove o seu parent do
	 * entrySet do map.
	 * 
	 * @param obj
	 *            Evento que deseja-se retirar da lista e dos rootsSelectedMap
	 */
	public void removeEvento(Object obj) {
		EventoBean eb = (EventoBean) obj;
		eventoBeanList.remove(eb);
		Evento rootDad = getDad(getEventoById(eb.getIdEvento()));
		if(rootsSelectedMap.get(rootDad) != null){
			if (rootsSelectedMap.get(rootDad).size() <= 1) {
				rootsSelectedMap.remove(rootDad);
			} else {
				rootsSelectedMap.get(rootDad).remove(getEventoById(eb.getIdEvento()));
			}
		}
		unselected();
		sincronizarEventoVariavel(eventoBeanList);
		
		this.alertMessage = "Movimento excluído com sucesso.";
		Events.instance().raiseEvent(AFTER_DELETE_EVENT);
	}
	
	public void sincronizarEventoVariavel(List<EventoBean> eventoBeanList){		
		if(ProcessInstance.instance() != null && gravarVariavelFluxo){
			this.sincronizarEventoBeanListContexto(eventoBeanList, ProcessInstance.instance() );
		}
	}
	
	public void sincronizarEventoBeanListContexto(List<EventoBean> eventoBeanList, org.jbpm.graph.exe.ProcessInstance processInstance){
		if(gravarVariavelFluxo) {
			LancadorMovimentosService.instance().setMovimentosTemporarios(processInstance, eventoBeanList);
		}
	}

	public List<EventoBean> getEventoBeanList() {
		if (TaskInstanceHome.instance().getTaskId() != null && gravarVariavelFluxo){
			org.jbpm.graph.exe.ProcessInstance processInstance = ManagedJbpmContext.instance().getTaskInstance(TaskInstanceHome.instance().getTaskId()).getProcessInstance();
			if (CollectionUtilsPje.isEmpty(eventoBeanList) && processInstance != null){
				eventoBeanList = LancadorMovimentosService.instance().getMovimentosTemporarios(processInstance);
			}
		}
		if (eventoBeanList == null){
			eventoBeanList = new ArrayList<EventoBean>(0);
		}
		return eventoBeanList;
	}
	
	public boolean complementosPreenchidos(){
		boolean ret = true;
		for(EventoBean eb: getEventoBeanList()){
			ret &= eb.getValido();
			if(!ret){
				break;
			}
		}
		return ret;
	}

	public void setEventoBeanList(List<EventoBean> eventoBeanList) {
		this.eventoBeanList = eventoBeanList;
	}

	/**
	 * Adiciona os eventos selecionados na lista de eventos a serem registrados
	 * e também os coloca no rootsSelectedMap, caso ainda não estejam em nenhum
	 * deles.
	 */
	public void setSelected(Evento selected, boolean isMultiplo) {
		/* O nó selecionado deve ser do tipo folha, ou seja, 
		 * não deve ter a ele vinculado nenhuma outra movimentação como filho. */
		if (validEventList(selected)) {
			super.setSelected(selected);
			Evento parent = getDad(selected);
			List<Evento> l = rootsSelectedMap.get(parent);
			if (l == null) {
				l = new ArrayList<Evento>();
				rootsSelectedMap.put(parent, l);
				addSelected(selected, l, isMultiplo);
			} else if (!l.contains(selected)) {
				addSelected(selected, l, isMultiplo);
			}
		} else {
			throw new AplicationException("O movimento selecionado não deve ter a ele vinculado outros movimentos.");
		}
	}

	private boolean validEventList(Evento selected){
		return  selected.getEventoList().isEmpty() || selected.getEventoList().stream().noneMatch(e -> e.getAtivo());
	}

	private void addSelected(Evento selected, List<Evento> l, boolean isMultiplo) {
		if (l != null){
			l.add(selected);
		}
		EventoBean eb = new EventoBean();
		eb.setIdEvento(selected.getIdEvento());
		eb.setCodEvento(selected.getCodEvento());
		eb.setDescricaoMovimento(selected.toString());
		eb.setExcluir(true);
		eb.setMultiplo(isMultiplo);
		eb.setQuantidade(1);
		eb.setDescricaoCompletaMovimento(selected.getMovimento());
		String caminhoCompleto = selected.getCaminhoCompleto();
		if(StringUtil.isEmpty(caminhoCompleto)) {
			caminhoCompleto = selected.getPathDescription();
			if(StringUtil.isEmpty(caminhoCompleto)) {
				caminhoCompleto = selected.getEvento();
			}
		}
		eb.setDescricaoCaminhoCompletoMovimento(caminhoCompleto);
		eb.setGlossario(selected.getGlossario());
		
		preencherEventoBean(eb);
		if(!eventoBeanList.contains(eb)){
			eventoBeanList.add(eb);
		}
		sincronizarEventoVariavel(eventoBeanList);
	}
	
	public void addSelected(Evento selected){
		addSelected(selected, null, false);
	}

	/**
	 * Preenche EventoBean com um MovimentoBean. Toda configuração de
	 * complementos será retornada do banco de dados.
	 * 
	 * @param eb
	 *            EventoBean que será preenchido.
	 * 
	 * @author David, Kelly
	 * 
	 */
	protected void preencherEventoBean(EventoBean eb) {
		ILancadorMovimentosAction lancadorAction = (ILancadorMovimentosAction) Component
				.getInstance(ILancadorMovimentosAction.NAME);
		MovimentoBean movimentoBean = lancadorAction.getMovimentoBeanPreenchido(getEventoById(eb.getIdEvento()));
		eb.getMovimentoBeanList().add(movimentoBean);

		boolean temComplemento = movimentoBean.getComplementoBeanList().size() > 0;

		if (!temComplemento) { // Movimento sem complemento
			eb.setTemComplemento(false);
			// Valido por padrão, não necessita usuário intervir
			eb.setValido(true);
		} else { // Movimento com complemento
			eb.setTemComplemento(true);
			// Inválido por padrão, falta usuário preencher os Complementos
			eb.setValido(false);
		}
	}

	/**
	 * Atualiza a quantidade de MovimentoBeans do EventoBean em questão.
	 * 
	 * @param eb
	 *            EventoBean que será atualizado.
	 * @return null (para manter na mesma página, não redirect)
	 * 
	 * @author David, Kelly
	 * 
	 */
	public String updateQuantidade(EventoBean eb) {
		if (eb.getMovimentoBeanList().size() < eb.getQuantidade()) { // Aumentou
																		// a
																		// quantidade
																		// de
																		// movimentos
			int sizeInicial = eb.getMovimentoBeanList().size();
			for (int i = 0; i < eb.getQuantidade() - sizeInicial; i++) {
				// Adicionar MovimentoBean para cada movimento acrescentado
				preencherEventoBean(eb);
			}
		} else if (eb.getMovimentoBeanList().size() > eb.getQuantidade()) { // Diminuiu
																			// a
																			// quantidade
																			// de
																			// movimentos
			int sizeInicial = eb.getMovimentoBeanList().size();
			for (int i = 0; i < sizeInicial - eb.getQuantidade(); i++) {
				// Remover MovimentoBeans no sentido do último ao primeiro
				eb.getMovimentoBeanList().remove(eb.getMovimentoBeanList().size() - 1);
			}
		}
		setSelectedEventoBean(eb);

		return null;
	}

	@Override
	public void selectListener(NodeSelectedEvent nodeSelectedEvent) {
		HtmlTree tree = (HtmlTree) nodeSelectedEvent.getSource();
		treeId = tree.getId();
		EventsEntityNode eventsEntityNode = (EventsEntityNode) tree.getData();

		if (eventsEntityNode != null){
			try {
				setSelected(eventsEntityNode.getEntity(), isMultiplo(eventsEntityNode));
				Events.instance().raiseEvent(getEventSelected(), getSelected());
				this.alertMessage = "Movimento selecionado com sucesso.";
			} catch (AplicationException ex) {
				this.alertMessage = ex.getMessage();
			}
		}
	}

	/**
	 * Método que varre a árvores até obter o registro pai (root)
	 * 
	 * @param en
	 *            EventsEntityNode que foi selecionado pelo usuário na interface
	 * @return true se o registro selecionado é múltiplo.
	 */
	protected boolean isMultiplo(EventsEntityNode en) {
		if (en != null) {
			boolean isMultiplo = isMultiplo((EventsEntityNode) en.getParent());
			if (isMultiplo) {
				return true;
			}
			return en.isMultiplo();
		}
		return false;
	}

	/**
	 * Retorna qual é o pai do evento informado, sendo como base a lista dos
	 * pais o rootList e não sua estrutura no banco de dados.
	 * 
	 * @param e
	 *            Evento que deseja-se saber o pai.
	 * @return Pai do evento informado contido no rootList.
	 */
	private Evento getDad(Evento evento) {
		if (CollectionUtilsPje.isEmpty(this.initialRootList)){
			getRoots(this.agrupamentosInstance);
		}
		for (EntityNode<Evento> o : this.initialRootList) {
			if (o.getEntity().equals(evento)) {
				return evento;
			}
		}
		if(evento.getEventoSuperior() == null) {
			return evento;
		}
		return getDad(evento.getEventoSuperior());
	}

	/**
	 * Verifica se ao menos um registro de cada root da tree exibida foi
	 * selecionado.
	 * 
	 * @return True caso já estejam selecionados.
	 */
	public boolean getAllRootsSelected() {
		if (isAllRegistred || rootList == null) {
			return true;
		}
		if (rootsSelectedMap.keySet() == null) {
			return false;
		}
		return getRoots().size() == rootsSelectedMap.keySet().size();
	}

	public void setRootsSelectedMap(Map<Evento, List<Evento>> rootsSelectedMap) {
		this.rootsSelectedMap = rootsSelectedMap;
	}

	public Map<Evento, List<Evento>> getRootsSelectedMap() {
		return rootsSelectedMap;
	}

	/**
	 * Método que registra os eventos selecionados, este método pode ser
	 * invocado por vários lugares, incluindo a assinatura digital. Ele também
	 * limpa a tree e seta uma variável para verificar se existe ou não mais
	 * eventos a serem registrados nessa tarefa.
	 */
	@Observer(REGISTRA_EVENTO_EVENT)
	public void registraEventos(){
		ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, ProcessoHome.instance().getIdProcessoDocumento());
		if (pd == null) {
			try {
				Integer identificadorDocumento = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(TaskInstance.instance());
				if(identificadorDocumento != null) {
					pd = EntityUtil.find(ProcessoDocumento.class, identificadorDocumento);
				}
			} catch (Exception e) {}
		}
		if (pd != null){
			ILancadorMovimentosAction lancadorMovimentosAction = ComponentUtil.getComponent(ILancadorMovimentosAction.NAME);
			// Se estiver lançando movimentos com documento associado, 
			// SOMENTE registrar movimentos SE 
			//            estiver assinado o documento 
			//          OU
			//            se o lançador puder lançar movimentos temporários 
			//                E estiver definido no fluxo para gravar movimentos temporários 
			boolean estaLancandoMovimentosTemporariamente = podeRegistrarMovimentosTemporarios() && lancadorMovimentosAction.deveGravarTemporariamente();
			if (estaLancandoMovimentosTemporariamente || (pd.getProcessoDocumentoBin() != null && pd.getProcessoDocumentoBin().getSignatarios().size() > 0)){
				inserirEventosProcesso(pd);
			}
		} else{
			inserirEventosProcesso(null);
		}
	}
	
	public boolean registraEventosSemFluxo(Processo processo) {
		return inserirEventosProcessoSemFluxo(processo);
	}
	
	public boolean registraEventosSemFluxo(Processo processo, ProcessoDocumento pd) {
		return inserirEventosProcessoSemFluxo(processo, pd);
	}
	
	private boolean inserirEventosProcessoSemFluxo(Processo processo) {
		return inserirEventosProcessoSemFluxo(processo, null);
	}

	private boolean inserirEventosProcessoSemFluxo(Processo processo, ProcessoDocumento pd) {
		try {
			Boolean validaAntesLancamento = validacoesAntesLancamento();
			if(validaAntesLancamento == null || validaAntesLancamento == Boolean.FALSE){
				return false;
			}
			
			ILancadorMovimentosAction lancadorMovimentosAction = ComponentUtil.getComponent(ILancadorMovimentosAction.NAME);
			//Lançar movimentos
			lancadorMovimentosAction.lancarMovimentosSemFluxo(eventoBeanList, pd, processo);
			
			validacoesAposLancamento();
			
			return true;
			
		} catch (Exception ex) {
			String action = "registrar os eventos do tipo tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.
					createMessage(action+ex.getLocalizedMessage(), 
								  "registraEventos()", 
								  "TarefaEventoTreeHandler", 
								  "BPM"));
		}
	}

	/**
	 * Método que realiza validações antes de efetuar o lançamento de eventos, condições:
	 * 1. deve haver agrupamento de movimentos identificado;
	 * 2. se for obrigatório o lançamento de movimentos:
	 * 2.1 deve haver movimentos temporários já selecionados;
	 * 2.2 e os complementos do movimento devem estar validados 
	 * 
	 * @return boolean Verdadeiro caso os lançamentos possam ser feitos. 
	 * Falso caso exista algum erro que impeça o lançamento ou 
	 * caso não existam lançamentos a serem feitos..
	 */
	public Boolean validacoesAntesLancamento() {
		if(isRegistraMovimento()) {
			if(CollectionUtilsPje.isEmpty(getEventoBeanList()) && LancadorMovimentosService.instance().possuiCondicaoLancamentoMovimentoObrigatorio()) {
				return false;
			}
			if (!validarComplementos()) {
				return false;
			}
			return true;
 		}
		return true;
	}

	/**
	 * Método que verifica se será registrado movimento.
	 * 
	 * @return boolean tem registro de movimento
	 */
	private boolean isRegistraMovimento() {
		return (rootsSelectedMap.size() > 0 || (this.agrupamentosInstance != null && this.agrupamentosInstance > 0) && !this.registred);
	}
	
	public boolean possuiLancamentoMovimentoObrigatorio(){
 		return this.isRegistraMovimento();
	}
		
	protected void validacoesAposLancamento() {
		clearTree();
		rootsSelectedMap = new HashMap<Evento, List<Evento>>();
		eventoBeanList.clear();
		registred = true;
		agrupamentosInstance = null;
		if (ProcessInstance.instance() != null){
			LancadorMovimentosService.instance().apagarMovimentosTemporarios();
		}
		if (isRegisterAfterRegisterEvent()) {
			Events.instance().raiseEvent(AFTER_REGISTER_EVENT);
		}
	}

	@Observer(REGISTRA_EVENTO_PD_EVENT)
	public void registraEventosProcessoDocumento(ProcessoDocumento pd) {
		inserirEventosProcesso(pd);
	}

	/**
	 * Lançar Movimentos Processuais selecionados, somente se passar nas
	 * validações.
	 * 
	 * @param pd
	 *            Documento que está sendo anexado ao processo.
	 * 
	 * @author David, Kelly
	 * 
	 */
	private void inserirEventosProcesso(ProcessoDocumento pd) {
		try {
			//Validações
			if (CollectionUtilsPje.isEmpty(eventoBeanList) || !validacoesAntesLancamento()) {
				return;
			}
			
			Processo processo = null;
			if(pd != null) {
				processo = pd.getProcesso();
			} else if(ProcessoHome.instance().getInstance().getIdProcesso() != 0) {
				processo = EntityUtil.find(Processo.class, ProcessoHome.instance().getInstance().getIdProcesso());
			} else {
				processo = JbpmUtil.getProcesso();
			}
			
			org.jbpm.taskmgmt.exe.TaskInstance taskInstance = TaskInstance.instance();
			org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
			Tarefa tarefa = JbpmUtil.getTarefa(taskInstance.getName(), processInstance.getProcessDefinition().getName());
			ILancadorMovimentosAction lancadorMovimentosAction = ComponentUtil.getComponent(ILancadorMovimentosAction.NAME);
			lancadorMovimentosAction.lancarMovimentos(eventoBeanList, pd, processo, taskInstance.getId(), processInstance.getId(), tarefa, podeRegistrarMovimentosTemporarios(), agrupamentosInstance);
			
			boolean estaLancandoMovimentosTemporariamente = podeRegistrarMovimentosTemporarios() && lancadorMovimentosAction.deveGravarTemporariamente();
			if (!estaLancandoMovimentosTemporariamente){
				validacoesAposLancamento();
			}
		} catch (Exception ex) {
			String action = "registrar os eventos do tipo tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.
					createMessage(action+ex.getLocalizedMessage(), 
								  "registraEventos()", 
								  "TarefaEventoTreeHandler", 
								  "BPM"));
		}
	}

	public Boolean getComplementosValidos() {
		return complementosValidos;
	}

	public void setComplementosValidos(Boolean valido) {
		this.complementosValidos = valido;
	}
	
	/**
	 * Retorna se todos os complementos foram preenchidos.
	 * 
	 * @return TRUE se os complementos são válidos
	 * 
	 * @author David, Kelly
	 * 
	 */
	public Boolean validarComplementos() {
		setComplementosValidos(true);
		int iQuantidadeInvalidos = 0; // Limitar somente mostrar mensagem de
										// erro para 2 complementos. Limitação
										// do modal de mensagem.
		FacesMessages.instance().clear();
		for (EventoBean eventoBean : getEventoBeanList()) {
			if (iQuantidadeInvalidos == 2) {
				break;
			}
			if (eventoBean.getTemComplemento() != null && eventoBean.getTemComplemento() == true) {
				if (!eventoBean.getValido()) {
					FacesMessages.instance().add(Severity.ERROR,
							"É obrigatório informar o complemento do movimento processual.");
					setComplementosValidos(false);
					iQuantidadeInvalidos++;
				}
			}
		}
		return complementosValidos;
	}

	@Observer(REGISTRA_EVENTO_EVENT_SEM_BPM)
	public void registraEventos(ProcessoDocumento pd) {
		try {
			if (eventoBeanList == null || eventoBeanList.size() == 0) {
				return;
			}
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			Processo processo = pd.getProcesso();
			StringBuilder sb = new StringBuilder();
			sb.append("insert into tb_processo_evento (id_processo, ")
					.append("id_evento, id_usuario, dt_atualizacao,id_processo_documento) values (:processo, ")
					.append(":evento, :usuario, :data, :documento)");
			Query q = EntityUtil.createNativeQuery(sb, "tb_processo_evento");
			for (EventoBean eb : eventoBeanList) {
				for (int i = 0; i < eb.getQuantidade(); i++) {
					q.setParameter("processo", processo.getIdProcesso());
					q.setParameter("evento", eb.getIdEvento());
					q.setParameter("usuario", usuario.getIdUsuario());
					q.setParameter("data", new Date());
					q.setParameter("documento", pd.getIdProcessoDocumento());
					q.executeUpdate();
				}
			}
			validacoesAposLancamento();
		} catch (Exception ex) {
			String action = "registrar os eventos do tipo tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"registraEventos()", "TarefaEventoTreeHandler", "BPM"));
		}
	}

	protected boolean isRegisterAfterRegisterEvent() {
		return true;
	}

	public boolean getRenderTree() {
		return renderTree;
	}

	public void setRenderTree(Boolean renderTree) {
		this.renderTree = renderTree;
	}

	public Boolean getRegistred() {
		return registred;
	}

	public void setRegistred(boolean registred) {
		this.registred = registred;
	}

	public List<EventoBean> getEventosCriminaisSelected() {
		return eventosCriminaisSelected;
	}

	public void setEventosCriminaisSelected(List<EventoBean> eventosCriminaisSelected) {
		this.eventosCriminaisSelected = eventosCriminaisSelected;
	}

	public void addEventoCriminalSelected(EventoBean eventoBean) {
		getEventosCriminaisSelected().add(eventoBean);
	}

	public void removeEventoCriminalSelected(EventoBean eventoBean) {
		getEventosCriminaisSelected().remove(eventoBean);
	}

	public void clearList() {
		eventoBeanList = new ArrayList<EventoBean>();
		rootsSelectedMap = new HashMap<Evento, List<Evento>>();
		rootList = null;
		idEventos = null;
		agrupamentosInstance = null;
		initialRootList = null;
	}

	public Evento getEventoById(Integer idEvento) {
		return getEntityManager().find(Evento.class, idEvento);
	}

	public void setAgrupamentosInstance(Integer agrupamentosInstance) {
		this.agrupamentosInstance = agrupamentosInstance;
	}

	public Integer getAgrupamentosInstance() {
		return agrupamentosInstance;
	}

	public void setParamPesquisaInstance(String paramPesquisaInstance) {
		this.paramPesquisaInstance = paramPesquisaInstance;
	}

	public String getParamPesquisaInstance() {
		return paramPesquisaInstance;
	}
	
	public void setModalAberto(boolean modalAberto) {
		this.modalAberto = modalAberto;
	}

	public boolean isModalAberto() {
		return modalAberto;
	}

	/*
	 * PJE-JT: David Vieira: [PJEII-531] Lançador de movimento no Fluxo é o único que pode lançar temporariamente movimentos
	 */
	protected boolean podeRegistrarMovimentosTemporarios(){
		return true;
	}

	/* 
	 * Metodo a ser implementado caso alguma restrição tem que ser imposta ao redenrizar nó da arvore
	 */
	public Boolean possueRestricao(EventsEntityNode en){
		return null;
	}

	/**
	 * Método responsável por desmarcar o movimento selecionado na árvore.
	 */
	public void unselected(){
		if (treeId != null){
			UITree tree = (UITree) RichFunction.findComponent(treeId);
			if (tree != null){
				tree.setRowKey(null);
				tree.setSelected();
				tree.saveState(FacesContext.getCurrentInstance());
			}
		}
	}
	
	public boolean validarMovimentacao()
	{
		boolean retorno = false;
		
		if(eventoBeanList == null || eventoBeanList.size() == 0) {
			if (rootsSelectedMap.size() > 0) 
			{
				retorno =  true;
			}
		}
		if (!validarComplementosMovimentacao()) {
			return false;
		}
		else
		{
			retorno =  true;
		}
		return retorno;
	}
	
	public Boolean validarComplementosMovimentacao() {
		Boolean valido = true;
		int iQuantidadeInvalidos = 0;  
 		for (EventoBean eventoBean : eventoBeanList) {
			if (iQuantidadeInvalidos == 2) {
				break;
			}
			if (eventoBean.getTemComplemento() != null && eventoBean.getTemComplemento() == true) {
				if (!eventoBean.getValido()) {
					valido = false;
					iQuantidadeInvalidos++;
				}
			}
		}
		return valido;
	}
	
	/**
	 * Metodo para filtrar as movimentações processuais exibidas no lançador de movimentações.
	 * @author rafael.souza
	 */
	public void buscaMovimentoProcessual() {
		if (StringUtils.isNotEmpty(this.paramPesquisaInstance)) {
			List<EntityNode<Evento>> newRootList = new ArrayList<EntityNode<Evento>>();
			String parametroPesquisa = StringUtil.normalize(this.paramPesquisaInstance);
			
			resetPesquisa();
			
			String codigoEvento, descricaoEvento;
			for(EntityNode<Evento> entityNode : this.rootList) {
				codigoEvento = entityNode.getEntity().getCodEvento();
				descricaoEvento = StringUtil.normalize(entityNode.getEntity().getEvento());
				
				EntityNode<Evento> newEntityNode = entityNode;
				if(verificarIgualdade(parametroPesquisa, codigoEvento, descricaoEvento)) {
					newRootList.add(newEntityNode);
				} else {
					buscaEvento(entityNode, parametroPesquisa, newEntityNode);
					if(newEntityNode.getNodes().isEmpty()) {
						newRootList.remove(newEntityNode);
					} else {
						newRootList.add(newEntityNode);
					}
				}
			}

			if (!newRootList.isEmpty()) {
				this.rootList = newRootList;
				this.forceExpandTree = true;
				try {
					if (eventos.size() == 1 && ComponentUtil.getComponent(EventoManager.class).isLeaf(eventos.get(0))) {
						addSelected(eventos.get(0));
						this.alertMessage = "Movimento processual encontrado e pré-selecionado com sucesso.";
					} else {
						this.alertMessage = "Movimentos processuais encontrados.";
					}
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
				eventos.clear();
			} else {
				limparPesquisa();
				this.alertMessage = "Não foi encontrado nenhum movimento processual com o parâmetro informado.";
			}
		} else {
			this.alertMessage = "Campo obrigatório.";
		}
	}
	
	/**
	 * Metodo recursivo que pecorre para validar o parâmetro da pesquisa que filtra as movimentações 
	 * processuais exibidas no parâmetro de movimentações.
	 * @author rafael.souza
	 */
	public void buscaEvento(EntityNode<Evento> entityNode, String parametroPesquisa, EntityNode<Evento> newEntityNode){
		List<EntityNode<Evento>> removerEvento = new ArrayList<EntityNode<Evento>>();
		
		String codigoEvento, descricaoEvento;
		for(EntityNode<Evento> node : entityNode.getNodes()) {
			codigoEvento = node.getEntity().getCodEvento();
			descricaoEvento = StringUtil.normalize(node.getEntity().getEvento());
			
			if(!verificarIgualdade(parametroPesquisa, codigoEvento, descricaoEvento) && !node.getNodes().isEmpty()) {
				buscaEvento(node, parametroPesquisa, node);
				if(!verificarIgualdade(parametroPesquisa, codigoEvento, descricaoEvento) && node.getNodes().isEmpty()){
					removerEvento.add(node);
				}
			} else if(!verificarIgualdade(parametroPesquisa, codigoEvento, descricaoEvento)) {
				removerEvento.add(node);
			} else {
				eventos.add(node.getEntity());
			}
		}
		newEntityNode.getNodes().removeAll(removerEvento);
	}
	
	private boolean verificarIgualdade(String parametroPesquisa, String codigoEvento, String descricaoEvento) {
		return codigoEvento.equals(parametroPesquisa) || 
				descricaoEvento.toLowerCase().contains(parametroPesquisa.toLowerCase());
	}
	
	/**
	 * Metodo que limpa a pesquisa que filtra as movimentações processuais exibidas no parâmetro de movimentações.
	 * @author rafael.souza
	 */
	public void limparPesquisa() {
		this.paramPesquisaInstance = null;
		resetPesquisa();
	}

	/**
	 * Metodo que reseta a pesquisa que filtra as movimentações processuais exibidas no parâmetro de movimentações.
	 * @author rafael.souza
	 */
	public void resetPesquisa() {
		Integer agrupamentos = this.agrupamentosInstance; 
		this.rootList = null;
		this.initialRootList = null;
		this.agrupamentosInstance = null;
		this.forceExpandTree = false;
		this.alertMessage = null;
		getRoots(agrupamentos);
	}
	
	/**
	 * Método responsável por indicar se os elementos (nós) do componente rich:tree 
	 * devem aparecer expandidos ou contraídos.
	 * 
	 * @param tree {@link UITree}
	 * @return Verdadeiro, a árvore é apresentada expandida. Falso, a árvore é apresentada contraida.
	 */
	public boolean adviseNodeOpened(UITree tree) {
		return this.forceExpandTree != null ? this.forceExpandTree : tree.isExpanded();
	}

	public boolean adviseNodeSelected(UITree tree) {
		return false;
	}
	
	public void inicializeForceExpandTree() {
		this.forceExpandTree = null;
	}

	public String getAlertMessage() {
		return alertMessage;
	}
	
	public void atualizarEventoBeanListPosComplemento(EventoBean evento){
		sincronizarEventoVariavel(getEventoBeanList());
		setModalAberto(!evento.getValido());
		org.jbpm.graph.exe.ProcessInstance processInstance = ManagedJbpmContext.instance().getTaskInstance(TaskInstanceHome.instance().getTaskId()).getProcessInstance();
		if (processInstance != null) {
			eventoBeanList = LancadorMovimentosService.instance().getMovimentosTemporarios(processInstance);
		}
	}
	
	public void atualizarEventoBeanListPosComplementoSemFluxo(EventoBean evento) {
		setModalAberto(!evento.getValido());
		evento.setDescricaoCompletaMovimento(ComponentUtil.getComponent(EventoManager.class).findComplementoByIdEvento(evento.getIdEvento()));
		if (evento.getDescricaoCompletaMovimento() != null && evento.getTemComplemento()) {
			for (MovimentoBean movimentoBean : evento.getMovimentoBeanList()) {
				for (ComplementoBean complementoBean : movimentoBean.getComplementoBeanList()) {
					for (ValorComplementoBean valorComplementoBean : complementoBean.getValorComplementoBeanList()) {					
						evento.setDescricaoCompletaMovimento(evento.getDescricaoCompletaMovimento().replace("#{"+complementoBean.getLabel()+"}", valorComplementoBean.getValor()));
					}
				}
			}
		}
	}



	public Boolean getGravarVariavelFluxo(){
		return this.gravarVariavelFluxo;
	}

	public void setGravarVariavelFluxo(Boolean gravarVariavelFluxo){
		this.gravarVariavelFluxo = gravarVariavelFluxo;
	}
}
