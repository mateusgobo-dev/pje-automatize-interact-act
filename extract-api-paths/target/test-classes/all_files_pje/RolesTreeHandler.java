package br.com.infox.component.tree;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("rolesTree")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class RolesTreeHandler extends AbstractTreeHandler<Papel>{

	private static final String ROLE_TREE_EVENT = "roleTreeHandlerSelected";

	private static final long serialVersionUID = 1L;

	/** Expressões para montar a árvore invertida (do menor papel para o maior) */
	private static final String QUERY_PAPEIS_INV = "select p from Papel p " + "join p.grupos grupo "
			+ "where grupo = :" + EntityNode.PARENT_NODE + " order by p.nome";
	
	private static final String QUERY_ROOT_INV = "select p from Papel p where not exists ("
			+ "select p1, grupo from Papel p1 join p1.grupos grupo where "
			+ "p = p1 and grupo.identificador not like '/%') "
			+ "and p.identificador not like '/%' group by p order by p.nome";
	
	private static final String QUERY_RECURSOS = "select grupo from Papel p "
			+ " join p.grupos grupo "
			+ " where p = :"
			+ EntityNode.PARENT_NODE + "  order by grupo.nome";
	
	private static final String QUERY_ROOT = "select grupo from Papel p "
			+ "right join p.grupos grupo where grupo.identificador not like '/%' and p is null order by grupo.nome";

	private static final String QUERY_PAPEIS = "select grupo from Papel p " 
			+ "join p.grupos grupo " 
			+ "where p = :"
			+ EntityNode.PARENT_NODE + "  order by grupo.nome";

	private boolean invertida;

	@Override
	protected String getQueryChildren(){
		return null;
	}

	@Override
	protected String[] getQueryChildrenList(){
		String[] queries = new String[2];
		queries[0] = invertida ? QUERY_PAPEIS_INV : QUERY_PAPEIS;
		queries[1] = QUERY_RECURSOS;
		return queries;
	}

	@Override
	protected String getQueryRoots(){
		return invertida ? QUERY_ROOT_INV : QUERY_ROOT;
	}

	@Override
	protected String getEventSelected(){
		return ROLE_TREE_EVENT;
	}

	public void inverter(){
		invertida = !invertida;
		this.clearTree();
	}

	public boolean getInvertida(){
		return invertida;
	}

}