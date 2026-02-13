package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

import br.com.infox.access.home.PapelHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("papelTree")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PapelTreeHandler extends AbstractTreeHandler<Papel>{

	public static final String PAPEL_TREE_EVENT = "papelTreeHandlerSelected";

	private static final long serialVersionUID = 1L;

	private static final String QUERY_PAPEIS = "select grupo from Papel p " + "join p.grupos grupo " + "where p = :"
			+ EntityNode.PARENT_NODE + " and grupo.identificador not like '/%' order by grupo.nome";

	@Override
	protected String getQueryChildren(){
		return QUERY_PAPEIS;
	}

	@Override
	protected String getQueryRoots(){
		Papel papel = getPapelAtual();
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

	protected Papel getPapelAtual(){
		UsuarioLocalizacao usuarioLocalizacao = (UsuarioLocalizacao) Contexts.getSessionContext().get(
				"usuarioLogadoLocalizacaoAtual");
		if (usuarioLocalizacao != null){
			return usuarioLocalizacao.getPapel();
		}
		return null;
	}

	@Override
	public List<EntityNode<Papel>> getRoots(){		
		if (!Authenticator.isPapelAdministrador() && Identity.instance().hasRole(Papeis.ADMINISTRADOR_CADASTRO_USUARIO)){
			return getNodesAdministradorCadastroUsuario();
		} else {
			List<EntityNode<Papel>> nodes = new ArrayList<EntityNode<Papel>>();
			String[] queries = new String[1];
			queries[0] = getQueryChildren();
			EntityNode<Papel> node = new EntityNode<Papel>(null, getPapelAtual(), queries);
			nodes.add(node);
			return nodes;
		}	
	}
	
	/**
	 * Função auxiliar do getRoots() para retornar uma List somente com os nós(papeis) filhos de Admin e Administrador.
	 * 
	 * @return
	 */
	private List<EntityNode<Papel>> getNodesAdministradorCadastroUsuario(){
		List<EntityNode<Papel>> nodes = new ArrayList<EntityNode<Papel>>();
		String[] queries = new String[1];
		queries[0] = getQueryChildren();
		PapelHome papelHome = new PapelHome();
		Papel papelAdmin = papelHome.findPapelPorIdentificador("admin");
		
		for (Papel papelFilho : papelAdmin.getGrupos()){
			EntityNode<Papel> node = new EntityNode<Papel>(null, papelFilho, queries);				

			if (!papelFilho.getIdentificador().contains("/")){
				if (!papelFilho.getIdentificador().equals("administrador")){
					nodes.add(node);
				}else{
					for (Papel papelAdminsitradorFilho : papelFilho.getGrupos()){
						EntityNode<Papel> nodeAdministrador = new EntityNode<Papel>(null, papelAdminsitradorFilho, queries);
						if (!papelAdminsitradorFilho.getIdentificador().contains("/")){
							nodes.add(nodeAdministrador);
						}	
					}
				}
			}	
		}
	    return nodes;
	}

	@Override
	protected String getEventSelected(){
		return PAPEL_TREE_EVENT;
	}

}