package br.com.infox.cliente.home;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.event.DropEvent;

import br.com.infox.bpm.taskPage.FGPJE.TaskNamesPrimeiroGrau;
import br.com.infox.cliente.Util;
import br.com.infox.cliente.component.tree.LotesTreeHandler;
import br.com.infox.cliente.component.tree.TarefasTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.TarefaTree;
import br.com.infox.ibpm.home.CaixaHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.Lote;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(PainelUsuarioHome.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
@SuppressWarnings("unchecked")
public class PainelUsuarioHome implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "painelUsuarioHome";

	private Map<String, Object> selected;
	private List<Integer> processoIdList;
	private Long qtdProcessoIdList;
	private boolean mostraSegredo;
	private int rpvStatus;
	private boolean renderMotivoPendencias = false;
	private Map<Integer, Boolean> podeEditarCaixaMap;
	private Integer perfilVisualizacao;
	
	private boolean atualizaGridCaixas = true;

	@Observer(Eventos.SELECIONADA_TAREFA)
	public void onSelected(Object obj) {
		this.selected = (Map<String, Object>) obj;
		Object idCaixaObj = selected.get("idCaixa");
		int idCaixa = idCaixaObj != null ? (Integer) idCaixaObj : -1;
		ParametroUtil pUtil = ParametroUtil.instance();
		renderMotivoPendencias = "Caixa".equals(selected.get("type"))
				&& tarefaRetemMotivoPendencia(Integer.parseInt(selected.get(
						"idTarefa").toString()))
				&& (Integer.parseInt(pUtil.getIdCaixaIntimacaoAutoPend()) == idCaixa || Integer
						.parseInt(pUtil.getIdCaixaIntimacaoAutoPendSREEO()) == idCaixa);
		processoIdList = null;
		GridQuery grid = ComponentUtil.getComponent("consultaProcessoGrid");
		grid.setPage(1);
		//buscarQtdProcessoIdList();
	}

	public boolean isTarefaControlePrazoSelected() {
		if (selected != null && selected.get("nomeTarefa") != null) {
			return selected.get("nomeTarefa").equals(
					TaskNamesPrimeiroGrau.CONTROLE_DE_PRAZO);
		}
		return false;
	}

	private boolean tarefaRetemMotivoPendencia(int idTarefa) {
		ParametroUtil parametroUtil = ParametroUtil.instance();
		boolean ret = false;
		if (parametroUtil.isPrimeiroGrau()) {
			if (parametroUtil.getTarefaDarCienciaPartes() != null
					&& parametroUtil.getTarefaDarCienciaPartes().getIdTarefa() > 0) {
				ret = idTarefa == parametroUtil.getTarefaDarCienciaPartes()
						.getIdTarefa();
			}
		} else {
			ret = idTarefa == parametroUtil.getIdTarefaDarCienciaPartesSREEO()
					|| idTarefa == parametroUtil.getIdTarefaDarCienciaPartes();
		}
		return ret;
	}

	@Observer("selectedLoteTreeEvent")
	public void onLoteSelected(Object obj) {
		this.selected = (Map<String, Object>) obj;
		processoIdList = null;
		GridQuery grid = ComponentUtil.getComponent("consultaProcessoGrid");
		grid.setPage(1);
	}

	public Integer getIdCaixa() {
		if (selected != null) {
			return (Integer) selected.get("idCaixa");
		}
		return null;
	}

	public Integer getIdLote() {
		if (selected != null) {
			return (Integer) selected.get("idLote");
		}
		return null;
	}

	public List<Integer> getProcessoIdList() {
		if (selected != null) {
			if (processoIdList == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("select s.idProcesso from SituacaoProcesso s ");
				sb.append("where s.nomeTarefa = :nomeTarefa ");
				// sb.append("where s.idTarefa = :idTarefa ");
				sb.append(getTreeTypeRestriction());
				if (selected.containsKey("segredo")) {
					if (selected.get("segredo").equals("true")) {
						sb.append("and s.segredoJustica = true ");
					} else {
						sb.append("and (s.segredoJustica = false or s.segredoJustica is null )");
					}
				}
				sb.append("group by s.idProcesso");

				Query query = EntityUtil.getEntityManager().createQuery(
						sb.toString());
				// processoIdList = query.setParameter("idTarefa",
				// getTaskId()).getResultList();
				processoIdList = query.setParameter("nomeTarefa",
						getNomeTarefa()).getResultList();
			}
			if (processoIdList.size() == 0) {
				processoIdList.add(-1);
			}
			return processoIdList;
		}
		return null;
	}

	public Long getQtdProcessoIdList() {
		return qtdProcessoIdList;
	}

	public String getNomeTarefa() {
		if (selected != null) {
			if (selected.containsKey("idCaixa")) {
				return null;
			}
			if (selected.containsKey("nomeTarefa")) {
				return (String) selected.get("nomeTarefa");
			}
			return "-1";
		}
		return "-1";
	}

	public Boolean getSegredo() {
		if (selected != null) {
			if (selected.containsKey("segredo")) {
				if (selected.get("segredo").equals("true")) {
					return Boolean.TRUE;
				} else {
					return Boolean.FALSE;
				}
			}
			return Boolean.FALSE;
		}
		return Boolean.FALSE;
	}

	private String getTreeTypeRestriction() {
		String treeType = (String) selected.get("tree");
		String nodeType = (String) selected.get("type");
		if ("caixa".equals(treeType) && "Task".equals(nodeType)) {
			return "and s.idCaixa is null ";
		}
		if (treeType == null && "Caixa".equals(nodeType)) {
			return "and s.idCaixa is not null ";
		}
		if (treeType == null && "Lote".equals(nodeType)) {
			return MessageFormat.format("and s.idLote = {0} ",
					selected.get("idLote"));
		}
		return "";
	}

	public Boolean existeCaixaRestriction() {
		if(selected == null){
			return null;
		}
		String treeType = (String) selected.get("tree");
		String nodeType = (String) selected.get("type");
		if ("caixa".equals(treeType) && "Task".equals(nodeType)) {
			return true;
		}
		if (treeType == null && "Caixa".equals(nodeType)) {
			return null;
		}
		return null;

	}

	public void processoCaixa(DropEvent evt) {
		Caixa caixa = EntityUtil.find(Caixa.class, evt.getDropValue());
		setProcessoCaixa(getProcessoIdList(evt.getDragValue()), caixa);
		try {
			ConsultaProcessoVO cpt = (ConsultaProcessoVO) evt.getDragValue();
			((GridQuery) ComponentUtil.getComponent("consultaProcessoGrid"))
					.getResultList().remove(cpt);
		} catch (ClassCastException cce) {
			((GridQuery) ComponentUtil.getComponent("consultaProcessoGrid"))
					.getResultList().clear();
		}
	}

	public void setProcessoCaixa(List<Integer> idList, Caixa caixa) {
		for (Integer id : idList) {
			Processo processo = EntityUtil.find(Processo.class, id);
			// Recarregar o no da tarefa(pai) e seus filhos(caixas)
			if (processo.getCaixa() != null
					&& processo.getCaixa().getTarefa() != null) {
				TarefaTree.adicionarIdTarefa(processo.getCaixa().getTarefa()
						.getIdTarefa());
			} else if (caixa != null && caixa.getTarefa() != null) {
				TarefaTree.adicionarIdTarefa(caixa.getTarefa().getIdTarefa());
			}
			processo.setCaixa(caixa);
			EntityUtil.getEntityManager().merge(processo);
		}
		EntityUtil.getEntityManager().flush();
		refresh();
	}

	public void processoLote(DropEvent evt) {
		Lote lote = EntityUtil.find(Lote.class, evt.getDropValue());
		setProcessoLote(evt, lote);
	}

	private void setProcessoLote(DropEvent evt, Lote lote) {
		List<Integer> list = getProcessoIdList(evt.getDragValue());
		EntityManager em = EntityUtil.getEntityManager();
		for (Integer i : list) {
			ProcessoTrf processoTrf = em.getReference(ProcessoTrf.class, i);
			if (!lote.getProcessoTrfList().contains(processoTrf)) {
				lote.getProcessoTrfList().add(processoTrf);
			}
		}
		em.flush();
		refresh();
	}

	private List<Integer> getProcessoIdList(Object o) {
		List<Integer> list = new ArrayList<Integer>();
		if (o instanceof ConsultaProcessoVO) {
			list.add(((ConsultaProcessoVO) o).getIdProcessoTrf());
		} else if (o instanceof List) {
			List<ConsultaProcessoVO> processoList = (List<ConsultaProcessoVO>) o;
			for (ConsultaProcessoVO cpt : processoList) {
				list.add(cpt.getIdProcessoTrf());
			}
		}
		return list;
	}

	public void processoCaixaTarefa(DropEvent evt) {
		setProcessoCaixa(getProcessoIdList(evt.getDragValue()), null);
	}

	public void processoLoteTarefa(DropEvent evt) {
		List<Integer> list = getProcessoIdList(evt.getDragValue());
		EntityManager em = EntityUtil.getEntityManager();
		Lote lote = em.find(Lote.class, selected.get("idLote"));
		for (Integer i : list) {
			ProcessoTrf processoTrf = em.getReference(ProcessoTrf.class, i);
			lote.getProcessoTrfList().remove(processoTrf);
		}
		em.flush();
		refresh();
		try {
			ConsultaProcessoTrf cpt = (ConsultaProcessoTrf) evt.getDragValue();
			((GridQuery) ComponentUtil.getComponent("consultaProcessoGrid"))
					.getResultList().remove(cpt);
		} catch (ClassCastException cce) {
			((GridQuery) ComponentUtil.getComponent("consultaProcessoGrid"))
					.getResultList().clear();
		}
	}

	public Integer getTaskId() {
		if (selected != null) {
			if (selected.containsKey("idTarefa")) {
				return (Integer) selected.get("idTarefa");
			}
			return (Integer) selected.get("idTask");
		}
		return null;
	}

	public void setSelected(Map<String, Object> selected) {
		this.selected = selected;
	}

	public Map<String, Object> getSelected() {
		return selected;
	}

	public void editaCaixa(String idCaixa) {
		if (idCaixa != null && !idCaixa.isEmpty()) {
			Redirect r = new Redirect();
			r.setViewId("/Caixa/listView.xhtml");
			r.setParameter("tab", "form");
			r.setParameter("id", idCaixa);
			r.execute();
		}
	}

	public void editaLote() {
		Redirect r = new Redirect();
		r.setViewId("/Processo/Lote/listView.xhtml");
		r.setParameter("tab", "form");
		r.setParameter("id", selected.get("idLote"));
		r.execute();
	}

	public void refresh() {
		processoIdList = null;
		LotesTreeHandler lotesTree = ComponentUtil.getComponent("lotesTree");
		lotesTree.refresh();
		TarefasTreeHandler tree = ComponentUtil.getComponent("tarefasTree");
		tree.refresh();
		
		if(atualizaGridCaixas){
			GridQuery processosGrid = ComponentUtil.getComponent("consultaProcessoGrid");
			processosGrid.refresh();
		}
	}

	public static PainelUsuarioHome instance() {
		return (PainelUsuarioHome) ComponentUtil.getComponent(NAME);
	}

	public void setProcessoCaixa(Caixa caixa) {
		GridQuery grid = ComponentUtil.getComponent("consultaProcessoGrid");
		if (grid.getResultCount() > 0) {
			List<Integer> idList = getProcessoIdList(grid.getResultList());
			setProcessoCaixa(idList, caixa);
			ConsultaProcessoHome.instance().limparTela("consultaProcessoForm");
		}
	}

	public void setMostraSegredo(boolean mostraSegredo) {
		this.mostraSegredo = mostraSegredo;
		LotesTreeHandler lotesTree = ComponentUtil.getComponent("lotesTree");
		lotesTree.clearTree();
		TarefasTreeHandler tree = ComponentUtil.getComponent("tarefasTree");
		tree.clearTree();
		TarefaTree tarefaTree = ComponentUtil.getComponent("tarefaTree");
		tarefaTree.clearTree();
		if (selected != null) {
			if (selected.containsKey("nomeTarefa")) {
				selected.remove("nomeTarefa");
			}
		}
	}

	public boolean getMostraSegredo() {
		return mostraSegredo;
	}

	public void setSegredo(boolean segredo) {
		PessoaMagistradoHome.instance().limparPesquisa();
		if (selected != null) {
			selected.put("segredo", segredo + "");
		}
		processoIdList = null;
		LotesTreeHandler lotesTree = ComponentUtil.getComponent("lotesTree");
		lotesTree.clearTree();
		TarefasTreeHandler tree = ComponentUtil.getComponent("tarefasTree");
		tree.clearTree();
		tree.setSegredo(segredo);
		TarefaTree tarefaTree = ComponentUtil.getComponent("tarefaTree");
		tarefaTree.clearTree();
		tarefaTree.setSegredo(segredo);
	}

	public int getRpvStatus() {
		return rpvStatus;
	}

	public void setRpvStatus(int status) {
		this.rpvStatus = status;
	}

	public void clearActorId() {
		if (ProcessoHome.instance().acessarFluxo()) {
			try {
				String q = "update tb_processo set nm_actor_id = null where id_processo = :id";
				EntityUtil
						.createNativeQuery(q, "tb_processo")
						.setParameter(
								"id",
								ProcessoHome.instance().getInstance()
										.getIdProcesso()).executeUpdate();
				Util.setToEventContext("closeWindow", true);
			} catch (Exception ex) {
				throw new AplicationException(
						AplicationException
								.createMessage(
										"limpar o actor id do processo que estava sendo visualizado",
										"clearActorId()", "PainelUsuarioHome",
										"PJE"));
			}
		}
	}

	public boolean podeEditarCaixa(int idCaixa) {
		if (podeEditarCaixaMap == null) {
			podeEditarCaixaMap = new HashMap<Integer, Boolean>();
		}
		Boolean ret = podeEditarCaixaMap.get(idCaixa);
		if (ret == null) {
			if (idCaixa == 0) {
				ret = true;
			} else {
				Caixa c = EntityUtil.find(Caixa.class, idCaixa);
				ret = !c.getInSistema();
			}
			podeEditarCaixaMap.put(idCaixa, ret);
		}
		return ret;
	}

	public void removeCaixa(int idCaixa) {
		Caixa c = EntityUtil.find(Caixa.class, idCaixa);
		if (c != null) {
			ParametroUtil pUtil = ParametroUtil.instance();
			Caixa autoPend = pUtil.getCaixaIntimacaoAutoPend();
			Caixa autoPendSREEO = pUtil.getCaixaIntimacaoAutoPendSREEO();

			if (autoPend != null && autoPendSREEO != null) {
				if (c.getNomeCaixa().equals(autoPend.getNomeCaixa())
						|| c.getNomeCaixa()
								.equals(autoPendSREEO.getNomeCaixa())) {
					FacesMessages.instance().add(Severity.ERROR,
							"Esta é uma caixa fixa, não é possível excluí-la.");
					return;
				}
			}
			CaixaHome caixaHome = ComponentUtil.getComponent("caixaHome");
			caixaHome.removeCaixa(idCaixa);
			getSelected().put("type", "");
			// Recarregar o no da tarefa(pai) e seus filhos(caixas)
			TarefaTree.adicionarIdTarefa(c.getTarefa().getIdTarefa());
		}
	}

	@Observer(br.com.infox.ibpm.component.tree.TarefasTreeHandler.CLEAR_TAREFAS_TREE_EVENT)
	public void clearTarefasTreeObs() {

		PainelUsuarioHome.instance().setRpvStatus(0);
	}

	public void setRenderMotivoPendencias(boolean renderMotivoPendencias) {
		this.renderMotivoPendencias = renderMotivoPendencias;
	}

	public boolean getRenderMotivoPendencias() {
		return renderMotivoPendencias;
	}

	public boolean isAtualizaGridCaixas(){
		return atualizaGridCaixas;
	}

	public void setAtualizaGridCaixas(boolean atualizaGridCaixas){
		this.atualizaGridCaixas = atualizaGridCaixas;
	}

	public Integer getPerfilVisualizacao() {
		if(perfilVisualizacao == null && Contexts.getSessionContext().get(Papeis.PERFIL_VISUALIZACAO_PAINEL) != null){
			return (Integer)Contexts.getSessionContext().get(Papeis.PERFIL_VISUALIZACAO_PAINEL) ;
		}
		if(perfilVisualizacao == null){
			return 0;
		}
		return perfilVisualizacao;
	}

	public void setPerfilVisualizacao(Integer perfilVisualizacao) {
		Contexts.getSessionContext().set(Papeis.PERFIL_VISUALIZACAO_PAINEL, perfilVisualizacao);
		this.perfilVisualizacao = perfilVisualizacao;
	}

}