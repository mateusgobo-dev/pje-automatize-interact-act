package br.jus.cnj.pje.servicos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.NodeTypes;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.Task;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.handler.TaskHandler;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name(NoDeDesvioService.NAME)
public class NoDeDesvioService {
	
	public static final String PREFIXO_NOME_NO_DE_DESVIO = "Nó de Desvio";

	public final static String NAME = "noDeDesvioService";
	private ProcessDefinition instance;
	private Map<Node, List<TaskHandler>> taskNodeMap;

	public void cadastrarNodePanico(ProcessDefinition def) throws Exception {
		int count = 0;
		instance = def;
		Class<?> nodeType = NodeTypes.getNodeType("task-node");
		Node node = null;
		String nomeNoDesvio = getNomeNoDesvio(def);
		Swimlane laneSolicitante = new Swimlane(nomeNoDesvio);
		boolean nodeExists = false;

		// Node noRemocao = null;

		for (Node noLoop : instance.getNodes()) {
			if (noLoop.getName().equals(nomeNoDesvio)) {
				node = noLoop;
				nodeExists = true;
				break;
			}
		}

		if (node == null) {

			EntityManager em = EntityUtil.getEntityManager();
			String localizacaoDirecaoSecretaria = (String) Contexts.getApplicationContext().get(
					"idLocalizacaoDirecaoSecretaria");
			String localizacaoGabineteMagistrado = (String) Contexts.getApplicationContext().get(
					"idLocalizacaoGabineteMagistrado");
			String papelMagistrado = null;
			String papelDiretor = null;
			StringBuilder expression = new StringBuilder();
			String expressao = new String();
			String sql = "select o from Papel o where o.identificador = :dirSecretaria or o.identificador = :magistrado";
			Query query = em.createQuery(sql);
			query.setParameter("dirSecretaria", Papeis.DIRETOR_SECRETARIA);
			query.setParameter("magistrado", Papeis.MAGISTRADO);
			List<Papel> lista = query.getResultList();
			for (Papel pap : lista) {
				if (Authenticator.isDiretorSecretaria(pap.getIdentificador())) {
					papelDiretor = String.valueOf(pap.getIdPapel());
					continue;
				}
				if (Authenticator.isMagistrado(pap.getIdentificador())) {
					papelMagistrado = String.valueOf(pap.getIdPapel());
					continue;
				}
			}
			if (localizacaoDirecaoSecretaria != null && localizacaoGabineteMagistrado != null && papelDiretor != null
					&& papelMagistrado != null) {
				expression.append("#{localizacaoAssignment.getPooledActors('");

				String[] ldr = localizacaoDirecaoSecretaria.split(",");
				for (int i = 0; i < ldr.length; i++) {
					expression.append(ldr[i] + ":" + papelDiretor + ",");
				}
				String[] lgm = localizacaoGabineteMagistrado.split(",");
				for (int i = 0; i < lgm.length; i++) {
					expression.append(lgm[i] + ":" + papelMagistrado + ",");
				}

				expressao = expression.toString();
				expressao = expressao.substring(0, expressao.length() - 1);
				expressao += "')}";
				// expression.append("')}");
			} else {
				throw new Exception("Configurações Inválidas");
			}

			node = (Node) nodeType.newInstance();
			node.setName(nomeNoDesvio);

			laneSolicitante.setPooledActorsExpression(expressao);
			instance.getTaskMgmtDefinition().addSwimlane(laneSolicitante);

		}
		// instance.getNodes().remove(noRemocao);

		Transition ta = null;
		Transition tl = null;

		for (Node no : instance.getNodes()) {
			ta = null;
			tl = null;

			if (no.getNodeType().toString().equals("EndState")) {
				for (Transition tt : no.getArrivingTransitions()) {
					if (tt.getFrom().getName().equals(nomeNoDesvio)) {
						ta = tt;
						break;
					}
				}
				if (ta == null) {
					Transition tSaida = null;
					tSaida = new Transition();
					tSaida.setCondition(null);
					tSaida.setDescription("teste");
					tSaida.setProcessDefinition(instance);
					tSaida.setName(no.getName());
					tSaida.setFrom(node);
					tSaida.setTo(no);
					node.addLeavingTransition(tSaida);
					no.addArrivingTransition(tSaida);
				}
				continue;

			}

			if (no.getNodeType() != node.getNodeType() || no.getName().equals(nomeNoDesvio)) {
				continue;
			}

			if (no.getArrivingTransitions() != null) {
				for (Transition tt : no.getArrivingTransitions()) {
					if (tt.getFrom().getName().equals(nomeNoDesvio)) {
						ta = tt;
						break;
					}
				}
			}

			if (no.getLeavingTransitions() != null) {
				for (Transition tt : no.getLeavingTransitions()) {
					if (tt.getTo().getName().equals(nomeNoDesvio)) {
						tl = tt;
						break;
					}
				}
			}

			// no.getArrivingTransitions().remove(ta);
			// no.getLeavingTransitions().remove(tl);

			Transition tEntrada = null;
			Transition tSaida = null;

			if (tl == null) {
				tEntrada = new Transition();
				tEntrada.setCondition("#{true}");
				tEntrada.setDescription("teste");
				tEntrada.setProcessDefinition(instance);
				tEntrada.setName(node.getName());
				tEntrada.setFrom(no);
				tEntrada.setTo(node);
				node.addArrivingTransition(tEntrada);
				no.addLeavingTransition(tEntrada);
			}
			/*
			 * else{ tEntrada = tl; }
			 */

			if (ta == null) {
				tSaida = new Transition();
				tSaida.setCondition(null);
				tSaida.setDescription("teste");
				tSaida.setProcessDefinition(instance);
				tSaida.setName(no.getName());
				tSaida.setFrom(node);
				tSaida.setTo(no);
				node.addLeavingTransition(tSaida);
				no.addArrivingTransition(tSaida);
			}
			/*
			 * else{ tSaida = ta; }
			 */

			count++;
			// System.out.println(no.getName()+" - "+no.getNodeType().toString());
		}

		if (!nodeExists) {
			if (node instanceof TaskNode) {
				instance.addNode(node);
				getTasks(node);
				TaskNode tn = (TaskNode) node;
				Task t = new Task();
				t.setSwimlane(laneSolicitante);
				t.setProcessDefinition(instance);
				t.setTaskMgmtDefinition(instance.getTaskMgmtDefinition());
				List<TaskHandler> list = taskNodeMap.get(node);
				t.setName(node.getName());
				tn.addTask(t);
				tn.setEndTasks(true);
				TaskHandler th = new TaskHandler(t);
				list.add(th);

			}
		}

	}

	public List<TaskHandler> getTasks(Node currentNode) {
		List<TaskHandler> taskList = new ArrayList<TaskHandler>();
		if (currentNode instanceof TaskNode) {
			TaskNode node = (TaskNode) currentNode;
			if (taskNodeMap == null) {
				taskNodeMap = new HashMap<Node, List<TaskHandler>>();
			}
			taskList = taskNodeMap.get(node);
			if (taskList == null) {
				taskList = TaskHandler.createList(node);
				taskNodeMap.put(node, taskList);
			}
		}

		return taskList;
	}
	
	public static String getNomeNoDesvio(ProcessDefinition pd) {
		return PREFIXO_NOME_NO_DE_DESVIO + " - " + pd.getName();
	}

	public static NoDeDesvioService instance() {
		return ComponentUtil.getComponent(NoDeDesvioService.NAME);
	}
}
