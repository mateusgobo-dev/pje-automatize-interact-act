package br.com.infox.cliente.component.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.ProcessoTrfInicialAdvogadoList;
import br.com.itx.util.EntityUtil;

public class JurisdicaoEntityNode extends EntityNode<Map<String, Object>>{

	private static final long serialVersionUID = 1L;
	protected ArrayList<JurisdicaoEntityNode> rootNodes;

	public JurisdicaoEntityNode(String queryChildren){
		super(queryChildren);
	}

	public JurisdicaoEntityNode(String[] queryChildren){
		super(queryChildren);
	}

	public JurisdicaoEntityNode(EntityNode<Map<String, Object>> parent,
			Map<String, Object> entity,
			String[] queryChildren){
		super(parent, entity, queryChildren);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Map<String, Object>> getChildrenList(String query, Map<String, Object> entity){
		ProcessoTrfInicialAdvogadoList processoTrfInicialAdvogadoList = ProcessoTrfInicialAdvogadoList.instance();
		if (entity.get("type").equals("Jurisdicao")){
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct new map(c.idCaixaAdvogadoProcurador as idCaixaAdvogadoProcurador, ");
			sb.append("c.nomeCaixaAdvogadoProcurador as nomeCaixaAdvogadoProcurador, ");
			sb.append("c.jurisdicao as jurisdicao, ");
			sb.append("'ProcessoCaixaAdvogadoProcurador' as type, ");
			sb
					.append("(select count(distinct cpt.idProcessoTrf) from ConsultaProcessoTrf cpt where cpt.idProcessoTrf in (select pcap.processoTrf.idProcessoTrf from ProcessoCaixaAdvogadoProcurador pcap where pcap.caixaAdvogadoProcurador.idCaixaAdvogadoProcurador = c.idCaixaAdvogadoProcurador)");
			if (processoTrfInicialAdvogadoList.getCaixaPendentes()){
				sb.append("and cpt.idProcessoTrf in (select ppe.processoJudicial.idProcessoTrf from ProcessoParteExpediente ppe ");
				sb.append("where ppe.dtCienciaParte is not null and ");
				sb.append("ppe.pendenteManifestacao = true and ");
				sb.append("ppe.pessoaParte in (#{pessoaAdvogadoHome.pessoaAdvogadoProcurador}))) as qtd) ");
			}
			else{
				sb.append(") as qtd) ");
			}
			sb.append("from CaixaAdvogadoProcurador c ");
			sb.append("where c.jurisdicao = :jurisdicao ");
			sb.append("and c.localizacao = :localizacao ");
			sb.append("order by c.nomeCaixaAdvogadoProcurador");
			return EntityUtil.createQuery(sb.toString()).setParameter("jurisdicao", entity.get("jurisdicao"))
					.setParameter("localizacao", Authenticator.getLocalizacaoAtual()).getResultList();
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
	protected EntityNode<Map<String, Object>> createChildNode(Map<String, Object> n){
		/*
		 * if(ParametroUtil.instance().isPrimeiroGrau()){ return new OrgaoJulgadorEntityNode(this, n, this.queryChildren); }else{ return new
		 * OrgaoJulgadorColegiadoEntityNode(this, n, this.queryChildren); }
		 */
		return new JurisdicaoEntityNode(this, n, this.queryChildren);
	}

	@Override
	protected JurisdicaoEntityNode createRootNode(Map<String, Object> n){
		return new JurisdicaoEntityNode(null, n, getQueryChildren());
	}

}
