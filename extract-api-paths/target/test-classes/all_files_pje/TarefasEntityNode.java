package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;

public class TarefasEntityNode<E> extends EntityNode<Map<String, Object>>{

	private static final long serialVersionUID = 1L;
	protected ArrayList<TarefasEntityNode<E>> rootNodes;

	public TarefasEntityNode(String queryChildren){
		super(queryChildren);
	}

	public TarefasEntityNode(String[] queryChildren){
		super(queryChildren);
	}

	public TarefasEntityNode(EntityNode<Map<String, Object>> parent,
			Map<String, Object> entity,
			String[] queryChildren){
		super(parent, entity, queryChildren);
	}

	@Override
	protected TarefasEntityNode<Map<String, Object>> createRootNode(Map<String, Object> n){
		return new TarefasEntityNode<Map<String, Object>>(null, n, getQueryChildren());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Map<String, Object>> getChildrenList(String hql, Map<String, Object> entity){
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("taskId", entity.get("idTask"));
		if ("caixa".equals(entity.get("tree")) || "Caixa".equals(entity.get("type"))){
			if (Authenticator.getOrgaoJulgadorAtual() != null && Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
				query.setParameter("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());
				query.setParameter("orgaoJulgador", Authenticator.getOrgaoJulgadorAtual());
			}
			else if (Authenticator.getOrgaoJulgadorAtual() != null && Authenticator.getOrgaoJulgadorColegiadoAtual() == null){
				query.setParameter("orgaoJulgador", Authenticator.getOrgaoJulgadorAtual());
			}
			else if (Authenticator.getOrgaoJulgadorAtual() == null && Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
				query.setParameter("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());
			}
		}
		return query.getResultList();
	}

	@Override
	public String getType(){
		return (String) getEntity().get("type");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TarefasEntityNode<Map<String, Object>> createChildNode(Map<String, Object> n){
		TarefasEntityNode node = new TarefasEntityNode(this, n, getQueryChildren());
		return node;
	}

	public Integer getTaskId(){
		if (getEntity() != null){
			return (Integer) getEntity().get("idTask");
		}
		return 0;
	}

}