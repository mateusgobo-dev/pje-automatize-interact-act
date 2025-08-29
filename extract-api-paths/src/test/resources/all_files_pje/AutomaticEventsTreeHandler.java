package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.home.api.IProcessoDocumentoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.ibpm.service.AssinaturaDocumentoService;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.SujeitoAtivoEnum;

/**
 * Classe que realiza a tarefa de registrar os eventos e exibir a tree
 * proveniente de uma lista de Agrupamentos.
 * 
 * @author Infox
 * 
 */
@Name(AutomaticEventsTreeHandler.NAME)
@Scope(ScopeType.PAGE)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
public class AutomaticEventsTreeHandler extends AbstractTreeHandler<Evento> {

	public static final String LISTA_EVENTO_VARIABLE = "listaEvento";
	private static final LogProvider log = Logging.getLogProvider(AutomaticEventsTreeHandler.class);
	private static final long serialVersionUID = 1L;

	public static final String NAME = "automaticEventsTree";
	public static final String REGISTRA_EVENTO_EVENT = "EvenstTreeHandler.registrarEvento";
	public static final String REGISTRA_EVENTO_EVENT_SEM_BPM = "EventsTreeHandler.registrarEventoSemBpm";
	public static final String REGISTRA_EVENTO_PD_EVENT = "EvenstTreeHandler.registrarEventoProcessoDocumento";
	public static final String AFTER_REGISTER_EVENT = "afterRegisterEvents";
	public static final String GRAVAR_ALERTA_EVENT = "gravarAlertaEvent";
	public static final String EH_CLASSE_CRIMINAL_EVENT = "ehClasseCriminalEvent";

	private Map<Evento, List<Evento>> rootsSelectedMap = new HashMap<Evento, List<Evento>>();
	private List<EventoBean> eventoBeanList = new ArrayList<EventoBean>();
	private String idEventos;
	private boolean isAllRegistred;
	private boolean renderTree = true;
	private boolean registred;

	private List<EventoBean> eventosCriminaisSelected = new ArrayList<EventoBean>();

	private Integer agrupamentosInstance;

	public static AutomaticEventsTreeHandler instance() {
		return (AutomaticEventsTreeHandler) org.jboss.seam.Component.getInstance(AutomaticEventsTreeHandler.NAME);
	}

	/**
	 * Verifica se existe eventos a serem selecionados na treeView, caso todos
	 * existentes sejam obrigatórios a lista dos roots será vazia.
	 */
	public List<EntityNode<Evento>> getRoots(Integer agrupamentos) {
		if (agrupamentos != null && agrupamentos > 0) {
			// Prevenir erro, quando não forem informados ids de agrupamentos.
			return Collections.emptyList();
		}
		if (this.agrupamentosInstance == null || this.agrupamentosInstance != agrupamentos) {
			if (idEventos == null) {
				idEventos = getIdEventos(agrupamentos);
			}
			rootList = new ArrayList<EntityNode<Evento>>();
			Query queryRoots = getEntityManager().createQuery(getQueryRoots(agrupamentos));
			EntityNode<Evento> entityNode = createNode();
			entityNode.setIgnore(getEntityToIgnore());
			rootList = entityNode.getRoots(queryRoots);
			if (rootList.size() == 0) {
				renderTree = false;
			}
			setAllRegistred(!"".equals(idEventos) && rootList.size() == 0);
		}

		return rootList;
	}

	public void carregaEventos() {
		if (ProcessoHome.instance().getTipoProcessoDocumento() == null) {
			return;
		}

		if (JbpmUtil.getProcessVariable(LISTA_EVENTO_VARIABLE) != null) {
			List<EventoBean> listEB = JbpmUtil.getProcessVariable(LISTA_EVENTO_VARIABLE);
			IProcessoDocumentoHome pdHome = ComponentUtil.getComponent(IProcessoDocumentoHome.NAME);
			for (EventoBean eb : listEB) {
				if ((eb.getIdTipoProcessoDocumento() == ProcessoHome.instance().getTipoProcessoDocumento()
						.getIdTipoProcessoDocumento())
						&& eb.getIdProcessoDocumento() == pdHome.getInstance().getIdProcessoDocumento()) {

					Evento evento = findById(eb.getIdEvento());
					Evento eventoAux = findById(eb.getIdEvento());
					if (rootList != null && rootList.size() > 0) {
						while (eventoAux != null) {
							for (EntityNode<Evento> root : rootList) {
								if (root.getEntity().equals(eventoAux)) {
									setSelecao(evento, Boolean.TRUE, eb);
									if (!eventoBeanList.contains(eb))
										eventoBeanList.add(eb);
									eventoAux = null;
									break;
								}
							}
							if (eventoAux != null) {
								Evento eventoAuxiliar = findById(eventoAux.getIdEvento());
								if (eventoAuxiliar.getEventoSuperior() == null) {
									eventoAux = null;
								} else {
									eventoAux = eventoAuxiliar.getEventoSuperior();
								}
							}
						}
					}
				}
			}
		}
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
		if (!"".equals(idEventos)) {
			builder.append("and ea.evento.idEvento not in ( ");
			builder.append(idEventos);
			builder.append(") ");
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
	@SuppressWarnings("unchecked")
	protected String getIdEventos(Integer agrupamentos) {
		StringBuilder idList = new StringBuilder();
		if (agrupamentos != null && agrupamentos > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select ea from EventoAgrupamento ea ").append("where ea.agrupamento.idAgrupamento in (")
					.append(agrupamentos).append(")");
			sb.append(" AND ea.evento.ativo = true ");
			Query q = getEntityManager().createQuery(sb.toString());
			List<EventoAgrupamento> geList = q.getResultList();
			eventoBeanList.clear();
			for (int i = 0; i < geList.size(); i++) {
				if (geList.get(i).getEvento().getEventoList().size() == 0) {
					EventoBean eb = new EventoBean();
					eb.setIdEvento(geList.get(i).getEvento().getIdEvento());
					eb.setDescricaoMovimento(geList.get(i).getEvento().toString());
					eb.setMultiplo(geList.get(i).getMultiplo());
					eb.setExcluir(false);
					eb.setQuantidade(1);
					eventoBeanList.add(eb);
					if (i > 0) {
						if (idList.length() > 0) {
							idList.append(", ");
						}
					}
					idList.append(geList.get(i).getEvento().getIdEvento());
				}
			}
		}
		return idList.toString();
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
		Evento evento = findById(eb.getIdEvento());
		Evento rootDad = getDad(evento);
		if (rootsSelectedMap.get(rootDad) != null && rootsSelectedMap.get(rootDad).size() <= 1) {
			rootsSelectedMap.remove(rootDad);
		} else if (rootsSelectedMap.get(rootDad) != null) {
			rootsSelectedMap.get(rootDad).remove(evento);
		}
	}

	public List<EventoBean> getEventoBeanList() {
		return eventoBeanList;
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
		if (selected.getEventoList().isEmpty()) {
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
			clearTree();
		}
	}

	public void setSelecao(Evento selected, boolean isMultiplo, EventoBean eb) {
		if (selected.getEventoList().isEmpty()) {
			super.setSelected(selected);
			Evento parent = getDad(selected);
			List<Evento> l = rootsSelectedMap.get(parent);
			if (l == null) {
				l = new ArrayList<Evento>();
				rootsSelectedMap.put(parent, l);
				l.add(selected);
				eventoBeanList.add(eb);
			} else if (!l.contains(selected)) {
				l.add(selected);
				eventoBeanList.add(eb);
			}
		}
	}

	private void addSelected(Evento selected, List<Evento> l, boolean isMultiplo) {
		IProcessoDocumentoHome documentoHome = ComponentUtil.getComponent(IProcessoDocumentoHome.NAME);
		l.add(selected);
		EventoBean eb = new EventoBean();
		eb.setIdEvento(selected.getIdEvento());
		eb.setDescricaoMovimento(selected.toString());
		eb.setMultiplo(isMultiplo);
		eb.setExcluir(true);
		eb.setQuantidade(1);
		eb.setIdProcessoDocumento(documentoHome.getInstance().getIdProcessoDocumento());

		if (ProcessoHome.instance().getTipoProcessoDocumento() != null) {
			eb.setIdTipoProcessoDocumento(ProcessoHome.instance().getTipoProcessoDocumento()
					.getIdTipoProcessoDocumento());
		}

		eventoBeanList.add(eb);
	}

	@Override
	public void selectListener(NodeSelectedEvent ev) {
		HtmlTree tree = (HtmlTree) ev.getSource();
		treeId = tree.getId();
		EventsEntityNode en = (EventsEntityNode) tree.getData();
		setSelected(en.getEntity(), isMultiplo(en));
		Events.instance().raiseEvent(getEventSelected(), getSelected());
	}

	/**
	 * Método que varre a árvores até obter o registro pai (root)
	 * 
	 * @param en
	 *            EventsEntityNode que foi selecionado pelo usuário na interface
	 * @return true se o registro selecionado é múltiplo.
	 */
	private boolean isMultiplo(EventsEntityNode en) {
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

		for (EntityNode<Evento> o : rootList) {
			if (o.getEntity().equals(evento)) {
				return evento;
			}
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
		if (isAllRegistred() || rootList == null) {
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

	public void registrarEventosJbpm(ProcessoDocumento pd) {
		List<EventoBean> lista = JbpmUtil.getProcessVariable(LISTA_EVENTO_VARIABLE);
		if (lista == null) {
			lista = new ArrayList<EventoBean>(0);
		}

		lista = limparEventoTemporarioPorTipo(lista, pd);

		for (EventoBean eb : eventoBeanList) {
			eb.setIdProcessoDocumento(pd.getIdProcessoDocumento());
			eb.setIdTipoProcessoDocumento(ProcessoHome.instance().getTipoProcessoDocumento()
					.getIdTipoProcessoDocumento());
			lista.add(eb);
		}

		JbpmUtil.setProcessVariable(LISTA_EVENTO_VARIABLE, lista);
	}

	/*
	 * Limpa todos os eventos do processoDocumento.
	 */
	@SuppressWarnings("static-access")
	private void limparEventoTemporario(List<EventoBean> lista, ProcessoDocumento pd) {
		List<EventoBean> listaEb = new ArrayList<EventoBean>(0);
		if (lista != null) {
			for (EventoBean eventoBean : lista) {
				if (eventoBean.getIdProcessoDocumento() != pd.getIdProcessoDocumento()) {
					listaEb.add(eventoBean);
				}
			}
		}
		JbpmUtil.instance().setProcessVariable(LISTA_EVENTO_VARIABLE, listaEb);
	}

	/*
	 * Limpa os eventos do processoDocumento de acordo com o
	 * tipoProcessoDocumento selecionado.
	 */
	@SuppressWarnings("static-access")
	private List<EventoBean> limparEventoTemporarioPorTipo(List<EventoBean> lista, ProcessoDocumento pd) {
		List<EventoBean> listaEb = new ArrayList<EventoBean>(0);
		for (EventoBean eventoBean : lista) {
			if (!(eventoBean.getIdProcessoDocumento() == pd.getIdProcessoDocumento() && eventoBean
					.getIdTipoProcessoDocumento() == ProcessoHome.instance().getTipoProcessoDocumento()
					.getIdTipoProcessoDocumento())) {
				listaEb.add(eventoBean);
			}
		}
		JbpmUtil.instance().setProcessVariable(LISTA_EVENTO_VARIABLE, listaEb);
		return listaEb;
	}

	/**
	 * Método que registra os eventos selecionados, este método pode ser
	 * invocado por vários lugares, incluindo a assinatura digital. Ele também
	 * limpa a tree e seta uma variável para verificar se existe ou não mais
	 * eventos a serem registrados nessa tarefa.
	 */
	@Observer(REGISTRA_EVENTO_EVENT)
	@SuppressWarnings("unchecked")
	public void registraEventos() {
		ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, ProcessoHome.instance()
				.getIdProcessoDocumento());
		if (pd != null) {
			AssinaturaDocumentoService assinaturaDocumentoService = ComponentUtil
					.getComponent(AssinaturaDocumentoService.NAME);
			if (assinaturaDocumentoService.isDocumentoAssinado(pd.getIdProcessoDocumento())) {
				inserirEventosProcesso(pd);
				if (ProcessInstance.instance() != null && JbpmUtil.getProcessVariable(LISTA_EVENTO_VARIABLE) != null)
					limparEventoTemporario((List<EventoBean>) JbpmUtil.getProcessVariable(LISTA_EVENTO_VARIABLE), pd);
			} else {
				registrarEventosJbpm(pd);
			}
		} else {
			inserirEventosProcesso(null);
		}

	}

	@Observer(REGISTRA_EVENTO_PD_EVENT)
	public void registraEventosProcessoDocumento(ProcessoDocumento pd) {
		inserirEventosProcesso(pd);
	}

	private void inserirEventosProcesso(ProcessoDocumento pd) {
		try {
			if (eventoBeanList == null || eventoBeanList.size() == 0) {
				return;
			}
			Usuario usuario = (Usuario) Contexts.getSessionContext().get(Authenticator.USUARIO_LOGADO);
			Processo processo = null;

			Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
			if (idProcesso != null) {
				processo = EntityUtil.find(Processo.class, idProcesso);
			} else {
				processo = JbpmUtil.getProcesso();
			}
			StringBuilder sb = new StringBuilder();
			sb.append("insert into tb_processo_evento (tp_processo_evento, id_processo, id_evento, id_usuario, "
					+ "dt_atualizacao");
			org.jbpm.taskmgmt.exe.TaskInstance taskInstance = TaskInstance.instance();
			org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
			if (processInstance != null) {
				sb.append(", id_process_instance");
			}
			if (taskInstance != null) {
				sb.append(", id_tarefa");
				sb.append(", id_jbpm_task");
			}
			if (pd != null) {
				sb.append(", id_processo_documento");
			}
			sb.append(") ").append("values ('E', :processo, :evento, :usuario, :data");
			if (processInstance != null) {
				sb.append(", :processInstance");
			}
			if (taskInstance != null) {
				sb.append(", :tarefa");
				sb.append(", :idJbpm");
			}
			if (pd != null) {
				sb.append(", :idProcessoDocumento");
			}
			sb.append(")");
			Query q = EntityUtil.createNativeQuery(sb, "tb_processo_evento");
			for (EventoBean eb : eventoBeanList) {
				for (int i = 0; i < eb.getQuantidade(); i++) {
					q.setParameter("processo", processo.getIdProcesso());
					// q.setParameter("evento", eb.getIdEvento());
					q.setParameter("evento", eb.getIdEvento());
					q.setParameter("usuario", usuario.getIdUsuario());
					q.setParameter("data", new Date());
					Tarefa t = null;
					if (taskInstance != null) {
						q.setParameter("idJbpm", taskInstance.getId());
						t = JbpmUtil
								.getTarefa(taskInstance.getName(), processInstance.getProcessDefinition().getName());
						q.setParameter("tarefa", t.getIdTarefa());
					}
					if (processInstance != null) {
						q.setParameter("processInstance", processInstance.getId());
					}
					if (pd != null) {
						q.setParameter("idProcessoDocumento", pd.getIdProcessoDocumento());
					}
					q.executeUpdate();
				}
			}
			clearTree();
			rootsSelectedMap = new HashMap<Evento, List<Evento>>();
			eventoBeanList.clear();
			registred = true;
			Events.instance().raiseEvent(AFTER_REGISTER_EVENT);
		} catch (Exception ex) {
			String action = "registrar os eventos do tipo tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"registraEventos()", "AutomaticEventsTreeHandler", "BPM"));
		}
	}

	public void setAllRegistred(boolean isAllRegistred) {
		this.isAllRegistred = isAllRegistred;
	}

	public boolean isAllRegistred() {
		return isAllRegistred;
	}

	/**
	 * Verifica se o evento já foi lançado.
	 * 
	 * @param documento
	 * @return
	 */
	public Boolean verificaRegistroEventos(ProcessoDocumento documento) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(o.processoDocumento) ");
		sql.append("from ProcessoEvento o where o.processo.idProcesso = :idProcesso ");
		sql.append("and o.processoDocumento.idProcessoDocumento = :idProcessoDoc ");
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("idProcesso", documento.getProcesso().getIdProcesso());
		q.setParameter("idProcessoDoc", documento.getIdProcessoDocumento());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
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
			sb.append("insert into tb_processo_evento (tp_processo_evento,id_processo, ")
					.append("id_evento, id_usuario, dt_atualizacao,id_processo_documento) values ('E',:processo, ")
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
			clearTree();
			rootsSelectedMap = new HashMap<Evento, List<Evento>>();
			eventoBeanList.clear();
			registred = true;
			Events.instance().raiseEvent(AFTER_REGISTER_EVENT);
		} catch (Exception ex) {
			String action = "registrar os eventos do tipo tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"registraEventos()", "TarefaEventoTreeHandler", "BPM"));
		}
	}

	/**
	 * Metodo que verifica se pelo menos um evento foi selecionado pelo usuario
	 * 
	 * @return
	 */
	public boolean possuiEventoBeanSelecionado() {
		List<EventoBean> beanList = getEventoBeanList();
		return beanList != null && !beanList.isEmpty();
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
		rootList = null;
		idEventos = null;
	}

	@Override
	public void clearTree() {
		registred = false;
		renderTree = true;
		super.clearTree();
	}	
	
}