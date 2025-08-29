package br.com.infox.cliente.component.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.pje.list.ProcessoTrfInicialAdvogadoList;
import br.com.itx.util.EntityUtil;

public class OrgaoJulgadorColegiadoEntityNode extends EntityNode<Map<String, Object>>{

	private static final long serialVersionUID = 1L;
	protected ArrayList<OrgaoJulgadorColegiadoEntityNode> rootNodes;

	public OrgaoJulgadorColegiadoEntityNode(String queryChildren){
		super(queryChildren);
	}

	public OrgaoJulgadorColegiadoEntityNode(String[] queryChildren){
		super(queryChildren);
	}

	public OrgaoJulgadorColegiadoEntityNode(EntityNode<Map<String, Object>> parent,
			Map<String, Object> entity,
			String[] queryChildren){
		super(parent, entity, queryChildren);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Map<String, Object>> getChildrenList(String query, Map<String, Object> entity){
		StringBuilder sb = new StringBuilder();
		sb.append("select new map(max(o.idProcessoTrf) as id, ");
		sb.append("o.idOrgaoJulgador as idOrgaoJulgador, ");
		sb.append("o.orgaoJulgador as nomeOrgaoJulgador, ");
		sb.append("o.jurisdicao as jurisdicao, ");
		sb.append("o.orgaoJulgadorColegiado as orgaoJulgadorColegiado, ");
		sb.append("o.idOrgaoJulgadorColegiado as idOrgaoJulgadorColegiado, ");
		sb.append("count(o.idCaixaAdvogadoProcurador) as qtdEmCaixa, ");
		sb.append("count(o.idProcessoTrf) as qtd, ");
		sb.append("'OrgaoJulgador' as type) ");
		sb.append("  from ConsultaProcessoTrf o ");
		sb.append("where o.jurisdicao = :jurisdicao ");
		sb.append("and o.processoTrf.caixaAdvogadoProcurador.localizacao = #{usuarioLogadoLocalizacaoAtual.localizacaoFisica} ");
		sb.append("and o.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
		sb.append("and o.processoStatus = 'D' ");

		ProcessoTrfInicialAdvogadoList processoTrfInicialAdvogadoList = ProcessoTrfInicialAdvogadoList.instance();
		if (processoTrfInicialAdvogadoList.getCaixaPendentes()){
			sb.append(processoTrfInicialAdvogadoList.getEjbqlFiltroPendentes());
		}

		sb.append("group by o.idOrgaoJulgador, o.orgaoJulgador, o.jurisdicao, o.idOrgaoJulgadorColegiado, o.orgaoJulgadorColegiado ");
		sb.append("order by o.orgaoJulgador ");
		return EntityUtil.createQuery(sb.toString())
				.setParameter("idOrgaoJulgadorColegiado", entity.get("idOrgaoJulgadorColegiado"))
				.setParameter("jurisdicao", entity.get("jurisdicao")).getResultList();
	}

	@Override
	public String getType(){
		return (String) getEntity().get("type");
	}

	@Override
	protected OrgaoJulgadorColegiadoEntityNode createRootNode(Map<String, Object> n){
		return new OrgaoJulgadorColegiadoEntityNode(null, n, getQueryChildren());
	}

	@Override
	protected OrgaoJulgadorEntityNode createChildNode(Map<String, Object> n){
		return new OrgaoJulgadorEntityNode(this, n, getQueryChildren());
	}

}
