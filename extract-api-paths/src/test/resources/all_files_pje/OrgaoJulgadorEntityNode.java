package br.com.infox.cliente.component.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;

public class OrgaoJulgadorEntityNode extends EntityNode<Map<String, Object>>{

	private static final long serialVersionUID = 1L;
	protected ArrayList<OrgaoJulgadorEntityNode> rootNodes;

	public OrgaoJulgadorEntityNode(String queryChildren){
		super(queryChildren);
	}

	public OrgaoJulgadorEntityNode(String[] queryChildren){
		super(queryChildren);
	}

	public OrgaoJulgadorEntityNode(EntityNode<Map<String, Object>> parent,
			Map<String, Object> entity,
			String[] queryChildren){
		super(parent, entity, queryChildren);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Map<String, Object>> getChildrenList(String query, Map<String, Object> entity){
		if (entity.get("type").equals("OrgaoJulgador")){
			if (!ParametroUtil.instance().isPrimeiroGrau()){
				StringBuilder sb = new StringBuilder();
				sb.append("select new map(c.idCaixaAdvogadoProcurador as idCaixaAdvogadoProcurador, ");
				sb.append("c.nomeCaixaAdvogadoProcurador as nomeCaixaAdvogadoProcurador, ");
				sb.append("c.orgaoJulgador.idOrgaoJulgador as idOrgaoJulgador, ");
				sb.append("c.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado as idOrgaoJulgadorColegiado, ");
				sb.append("c.jurisdicao as jurisdicao, ");
				sb.append("'CaixaAdvogadoProcurador' as type, ");
				sb
						.append("(select count(distinct cpt.idProcessoTrf) from ConsultaProcessoTrf cpt where cpt.idCaixaAdvogadoProcurador = c.idCaixaAdvogadoProcurador) as qtd) ");
				sb.append("from CaixaAdvogadoProcurador c ");
				sb.append("where c.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
				sb.append("and c.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado ");
				sb.append("and c.jurisdicao = :jurisdicao ");
				sb.append("and c.localizacao = :localizacao ");
				sb.append("order by c.nomeCaixaAdvogadoProcurador");
				return EntityUtil.createQuery(sb.toString())
						.setParameter("idOrgaoJulgador", entity.get("idOrgaoJulgador"))
						.setParameter("idOrgaoJulgadorColegiado", entity.get("idOrgaoJulgadorColegiado"))
						.setParameter("jurisdicao", entity.get("jurisdicao"))
						.setParameter("localizacao", Authenticator.getLocalizacaoAtual()).getResultList();
			}
			else{
				StringBuilder sb = new StringBuilder();
				sb.append("select new map(c.idCaixaAdvogadoProcurador as idCaixaAdvogadoProcurador, ");
				sb.append("c.nomeCaixaAdvogadoProcurador as nomeCaixaAdvogadoProcurador, ");
				sb.append("c.orgaoJulgador.idOrgaoJulgador as idOrgaoJulgador, ");
				sb.append("c.jurisdicao as jurisdicao, ");
				sb.append("'CaixaAdvogadoProcurador' as type, ");
				sb
						.append("(select count(distinct cpt.idProcessoTrf) from ConsultaProcessoTrf cpt where cpt.idCaixaAdvogadoProcurador = c.idCaixaAdvogadoProcurador) as qtd) ");
				sb.append("from CaixaAdvogadoProcurador c ");
				sb.append("where c.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
				sb.append("and c.jurisdicao = :jurisdicao ");
				sb.append("and c.localizacao = :localizacao ");
				sb.append("order by c.nomeCaixaAdvogadoProcurador");
				return EntityUtil.createQuery(sb.toString())
						.setParameter("idOrgaoJulgador", entity.get("idOrgaoJulgador"))
						.setParameter("jurisdicao", entity.get("jurisdicao"))
						.setParameter("localizacao", Authenticator.getLocalizacaoAtual()).getResultList();
			}
		}
		else{
			return new ArrayList<Map<String, Object>>(0);
		}
	}

	@Override
	public String getType(){
		return (String) getEntity().get("type");
	}

	@Override
	protected OrgaoJulgadorEntityNode createRootNode(Map<String, Object> n){
		return new OrgaoJulgadorEntityNode(null, n, getQueryChildren());
	}

	@Override
	protected OrgaoJulgadorEntityNode createChildNode(Map<String, Object> n){
		return new OrgaoJulgadorEntityNode(this, n, getQueryChildren());
	}

}
