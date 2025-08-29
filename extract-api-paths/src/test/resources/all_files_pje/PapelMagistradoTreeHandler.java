package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("papelMagistradoTree")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PapelMagistradoTreeHandler extends AbstractTreeHandler<Papel>{

	public static final String PAPEL_TREE_EVENT = "papelMagistradoTreeHandlerSelected";

	private static final long serialVersionUID = 1L;

	private static final String QUERY_PAPEIS = "select grupo from Papel p " + "join p.grupos grupo " + "where p = :"
			+ EntityNode.PARENT_NODE + " and grupo.identificador not like '/%' order by grupo.nome";

	@Override
	protected String getQueryChildren(){
		return QUERY_PAPEIS;
	}

	@Override
	protected String getQueryRoots(){
		Papel papel = getPapelMagistrado();// ;Authenticator.getPapelAtual();
		StringBuilder sb = new StringBuilder();
		sb.append("select grupo from Papel p ");
		sb.append("right join p.grupos grupo where grupo.identificador ");
		sb.append("not like '/%' and p is null ");
		if (papel != null){
			sb.append(" and grupo.idPapel = ");
			sb.append(papel.getIdPapel()).append(" ");
		}
		sb.append("order by grupo.nome");
		return sb.toString();
	}

	@Override
	public List<EntityNode<Papel>> getRoots(){
		List<EntityNode<Papel>> nodes = new ArrayList<EntityNode<Papel>>();
		String[] queries = new String[1];
		queries[0] = getQueryChildren();
		EntityNode<Papel> node = new EntityNode<Papel>(null, getPapelMagistrado(), queries);
		nodes.add(node);
		return nodes;
	}

	@Override
	protected String getEventSelected(){
		return PAPEL_TREE_EVENT;
	}

	public Papel getPapelMagistrado(){
		String query = "select o from Papel o where o.identificador = :papel";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("papel", ParametroUtil.instance().getPapelMagistrado().getIdentificador());
		return (Papel) q.getSingleResult();
	}

}