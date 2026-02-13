package br.com.infox.component.tree;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;

public class TarefaNode<E> extends EntityNode<Map<String, Object>>{

	private static final long serialVersionUID = 1L;
	protected ArrayList<TarefaNode<E>> rootNodes;
	private boolean filhosCarregados;
	private List<TarefaNode<Map<String,Object>>> listaTarefasEntityNodeFake;
	
	private List<TarefaNode<Map<String, Object>>> tarefasNodes;

	public TarefaNode(TarefaNode<Map<String, Object>> parent, Map<String, Object> entityChildren, String queryChildren){
		super(parent, entityChildren, queryChildren);
	}	

	public TarefaNode() {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Map<String, Object>> getChildrenList(String hql, Map<String, Object> entity){
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("taskId", entity.get("nomeTarefa"));
		return query.getResultList();
	}

	@Override
	public String getType(){
		return (String) getEntity().get("type");
	}

	public Integer getTaskId(){
		if (getEntity() != null){
			return (Integer) getEntity().get("idTask");
		}
		return 0;
	}
	
	public List<TarefaNode<Map<String, Object>>> getTarefasNodes(){
		TarefaTree tree = ComponentUtil.getComponent("tarefaTree");
		if(tarefasNodes == null && (getEntity().get("type") != null && getEntity().get("type").equals("Task"))){
			carregarFilhosFake();
		}else if(tarefasNodes != null && tree.getListaIdCaixaAtualizar() != null && tree.getListaIdCaixaAtualizar().contains(getEntity().get("idTask"))){
			filhosCarregados = false;
			carregarFilhos();
			recarregarPai(tree);
			tree.setListaIdCaixaAtualizar(null);
		}
		return tarefasNodes;
	}

	@SuppressWarnings("unchecked")
	private void recarregarPai(TarefaTree tree) {
		Query queryRoots = EntityUtil.getEntityManager().createQuery(tree.getQueryRoots((String) getEntity().get("nomeTarefa")));
		Map<String, Object> nodePai = (Map<String, Object>) queryRoots.getSingleResult();
		setEntity(nodePai);
	}

	public void carregarFilhosFake() {
		tarefasNodes = getListaTarefasEntityNodeFake();
	}

	@SuppressWarnings("unchecked")
	public void carregarFilhos() {
		if(!filhosCarregados){
			tarefasNodes = new ArrayList<TarefaNode<Map<String,Object>>>();
			List<Map<String, Object>> children = getChildrenList(queryChildren[0], getEntity());
			for (Map<String, Object> entityChildren : children){
				TarefaNode<Map<String, Object>> node = new TarefaNode<Map<String,Object>>((TarefaNode<Map<String, Object>>) this, entityChildren, null);
				tarefasNodes.add(node);
			}
			filhosCarregados = true;
		}
	}

	private List<TarefaNode<Map<String,Object>>> getListaTarefasEntityNodeFake(){
		if(listaTarefasEntityNodeFake == null){
			listaTarefasEntityNodeFake = new ArrayList<TarefaNode<Map<String,Object>>>();
			listaTarefasEntityNodeFake.add(new TarefaNode<Map<String, Object>>());
		}
		return listaTarefasEntityNodeFake;
	}
	
}