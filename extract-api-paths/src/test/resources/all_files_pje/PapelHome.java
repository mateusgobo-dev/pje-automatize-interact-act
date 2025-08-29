package br.com.infox.access.home;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.AssertionFailure;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Role;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.action.RoleAction;

import br.com.infox.ibpm.entity.log.LogException;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.auditoria.LogLoadEvent;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("papelHome")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PapelHome extends AbstractHome<Papel> {

	private static final long serialVersionUID = 1L;

	private Map<Boolean, List<String>> papeisDisponiveis;
	private Map<String, Papel> papelMap;
	private List<String> membros;
	private Map<String, Papel> membrosMap;
	private String identificador;
	private List<String> recursosDisponiveis;
	private List<String> papeis;
	private List<String> recursos;
	private List<Papel> todosHerdados;
	private List<String> papeisNovos;
	private List<String> papeisAntigos;
	private List<String> recursosNovos;
	private List<String> recursosAntigos;
	private List<String> membrosNovos;
	private List<String> membrosAntigos;

	public Integer getPapelId() {
		return (Integer) getId();
	}

	private void clear() {
		papeis = null;
		recursos = null;
		recursosDisponiveis = null;
		papeisDisponiveis = null;
		membros = null;
		membrosMap = null;
		identificador = null;
		todosHerdados = null;
	}

	public void setPapelId(Integer id) {
		Object oid = getId();
		if (oid == null || !oid.equals(id)) {
			super.setId(id);
			Conversation.instance().end();
			clear();
			Papel p = getInstance();
			identificador = p.getIdentificador();
			RoleAction action = getRoleaction();
			action.editRole(p.getIdentificador());
		}
	}
	
	public void atualizaTodosHerdados() {
		this.identificaPapeisHerdados(instance);
	}
	
	private void identificaPapeisHerdados(Papel p) {
		if(p != null) {
			PapelManager papelManager = (PapelManager) Component.getInstance("papelManager");
			todosHerdados = papelManager.getPapeisHerdados(p);
		}
	}

	public List<String> getMembros() {
		if (membros == null || membros.isEmpty()) {
			membros = new ArrayList<String>();
			membrosMap = new HashMap<String, Papel>();
			final List<Principal> list = new ArrayList<Principal>();
			new RunAsOperation(true) {
				@Override
				public void execute() {
					list.addAll(IdentityManager.instance().listMembers(getInstance().getIdentificador()));
				}
			}.run();
			if (list.isEmpty()) {
				return new ArrayList<String>();
			}
			List<String> idPapeis = new ArrayList<String>();
			for (Principal principal : list) {
				idPapeis.add(principal.getName());
			}
			List<Papel> papelList = getPapelList(idPapeis);
			for (Papel papel : papelList) {
				String id = papel.getIdentificador();
				membros.add(id);
				membrosMap.put(id, papel);
			}
			Collections.sort(membros);
		}
		return membros;
	}

	public void setMembros(List<String> membros) {
		this.membros = membros;
	}

	private RoleAction getRoleaction() {
		return ComponentUtil.getComponent("org.jboss.seam.security.management.roleAction");
	}

	@Override
	public void newInstance() {
		super.newInstance();
		clear();
		Contexts.removeFromAllContexts("org.jboss.seam.security.management.roleAction");
	}

	@Override
	public String inactive(Papel instance) {
		return remove(instance);
	}

	@Override
	public String remove(Papel p) {
		setInstance(p);
		String ret = super.remove();
		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("papelGrid");
		grid.refresh();
		return ret;
	}

	public String getNome(String identificador) {
		if (papelMap != null && papelMap.containsKey(identificador)) {
			return papelMap.get(identificador).toString();
		}
		return null;
	}

	public List<String> getPapeis() {
		if (papeis == null) {
			papeis = getRoleaction().getGroups();
			if (papeis == null) {
				papeis = new ArrayList<String>();
			} else {
				removeRecursos(papeis);
			}
		}
		return papeis;
	}

	public void setPapeis(List<String> papeis) {
		this.papeis = papeis;
	}

	/**
	 * Busca os papeis que podem ser atribuidos ao papel atual, removendo
	 * aqueles que são implícitos, isso é, atribuidos por herança de papel
	 * 
	 * @return
	 */
	public List<String> getPapeisDisponiveis(boolean removeMembros) {
		if (papeisDisponiveis == null) {
			papeisDisponiveis = new HashMap<Boolean, List<String>>();
		}
		if (!papeisDisponiveis.containsKey(removeMembros)) {
			List<String> assignableRoles = getRoleaction().getAssignableRoles();
			papeisDisponiveis.put(removeMembros, assignableRoles);
			removePapeisImplicitos(assignableRoles, getPapeis());
			removeRecursos(assignableRoles);
			if (isManaged() && removeMembros) {
				removeMembros(instance.getIdentificador(), assignableRoles);
			} else {
				assignableRoles.removeAll(papeis);
			}
			if (papelMap == null) {
				papelMap = new HashMap<String, Papel>();
			}
			List<Papel> papelList = getPapelList(assignableRoles);
			for (Papel p : papelList) {
				papelMap.put(p.getIdentificador(), p);
			}
			Collections.sort(assignableRoles, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					String n1 = papelMap.get(o1).toString();
					String n2 = papelMap.get(o2).toString();
					return n1.compareTo(n2);
				}
			});
		}
		return papeisDisponiveis.get(removeMembros);
	}
	
	public List<Papel> getTodosHerdados() {
		return todosHerdados;
	}

	public void setTodosHerdados(List<Papel> todosHerdados) {
		this.todosHerdados = todosHerdados;
	}

	public List<String> getRecursos() {
		if (recursos == null) {
			if (IdentityManager.instance().roleExists(getInstance().getIdentificador())) {
				recursos = IdentityManager.instance().getRoleGroups(getInstance().getIdentificador());
				removePapeis(recursos);
			} else {
				recursos = new ArrayList<String>();
			}
		}
		return recursos;
	}

	public void setRecursos(List<String> recursos) {
		this.recursos = recursos;
	}

	public List<String> getRecursosDisponiveis() {
		if (recursosDisponiveis == null) {
			recursosDisponiveis = getRoleaction().getAssignableRoles();
			removePapeisImplicitos(recursosDisponiveis, getPapeis());
			removePapeisImplicitos(recursos, getPapeis());
			removePapeis(recursosDisponiveis);
			if (papelMap == null) {
				papelMap = new HashMap<String, Papel>();
			}
			List<Papel> papelList = getPapelList(recursosDisponiveis);
			for (Papel p : papelList) {
				papelMap.put(p.getIdentificador(), p);
			}
			Collections.sort(recursosDisponiveis, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					String n1 = papelMap.get(o1).toString();
					String n2 = papelMap.get(o2).toString();
					return n1.compareTo(n2);
				}
			});
		}
		return recursosDisponiveis;
	}

	private void removeRecursos(List<String> roles) {
		for (Iterator<String> iterator = roles.iterator(); iterator.hasNext();) {
			String papelId = iterator.next();
			if (papelId.startsWith("/")) {
				iterator.remove();
			}
		}
	}

	private void removePapeis(List<String> roles) {
		for (Iterator<String> iterator = roles.iterator(); iterator.hasNext();) {
			String papelId = iterator.next();
			if (!papelId.startsWith("/")) {
				iterator.remove();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<Papel> getPapelList(List<String> idPapeis) {
		if (idPapeis == null || idPapeis.isEmpty()) {
			return new ArrayList<Papel>();
		}
		List<Papel> papelList = getEntityManager().createQuery("select p from Papel p where identificador in (:list)")
				.setParameter("list", idPapeis).getResultList();
		return papelList;
	}

	private void removePapeisImplicitos(List<String> list, List<String> from) {
		if (from == null) {
			return;
		}
		// ser for o mesmo objeto, clona para evitar
		// ConcurrentModificationException
		if (from.equals(list)) {
			from = new ArrayList<String>(list);
		}
		for (String papel : from) {
			removePapeisImplicitos(papel, list, null);
		}
	}

	/**
	 * Remove o papel da lista, recursivamente
	 * 
	 * @param papel
	 */
	private void removePapeisImplicitos(final String papel, List<String> list, String papelPai) {
		for (final String p : IdentityManager.instance().getRoleGroups(papel)) {
			if((papelPai == null || !p.equals(papelPai)) && !getInstance().getIdentificador().equals(p)) {
				list.remove(p);
				EntityUtil.getEntityManager().flush();
				removePapeisImplicitos(p, list, papel);
			}
		}
	}

	private void removeMembros(final String papel, List<String> roles) {
		final List<Principal> listMembers = new ArrayList<Principal>();
		new RunAsOperation(true) {
			@Override
			public void execute() {
					listMembers.addAll(IdentityManager.instance().listMembers(papel));
			}
		}.run();
		for (Principal p : listMembers) {
			if (p instanceof Role) {
				roles.remove(p.getName());
				removeMembros(p.getName(), roles);
			}
		}
	}

	public String save() {
		int idPapel = getInstance().getIdPapel();
		
		final StringBuilder ret = new StringBuilder();
		new RunAsOperation(true) {

			@Override
			public void execute() {
				ret.append(saveOp());
			}
		}.run();
		String string = ret.toString();
		if (string.equals("success")) {
			registrarLogAlteracoes(papeisNovos, papeisAntigos);
			registrarLogAlteracoes(recursosNovos, recursosAntigos);
			registrarLogAlteracoes(membrosNovos, membrosAntigos);
			
			if (getInstance().getIdentificador().substring(0, 1).equals("/")) {
				FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "recurso_updated"));
			} else {
				if(idPapel == 0){
					FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "papel_created"));
				} else {
					FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "papel_updated"));
				}
			}
		}
		else if(string.equals("fail")) {
			if (getInstance().getIdentificador().substring(0, 1).equals("/")) {
				FacesMessages.instance().add(Severity.INFO, "Já existe um recurso com o identificador "+ getInstance().getIdentificador() +".");
			} else {
				FacesMessages.instance().add(Severity.INFO, "Já existe um papel com o identificador "+ getInstance().getIdentificador() +".");
			}
			newInstance();
		}
		return string;
	}

	private String saveOp() {
		montarListasAntesSalvar();
		identificador = getInstance().getIdentificador();
		getRoleaction().setRole(identificador);
		papeis = new ArrayList<String>(getPapeis());
		papeis.addAll(getRecursos());
		removePapeisImplicitos(papeis, papeis);
		getRoleaction().setGroups(papeis);
		
		String save = "";
		if (isManaged()) {
			if (membros != null) {
				List<String> incluirMembros = new ArrayList<String>(membros);
				incluirMembros.removeAll(membrosMap.keySet());
				for (String membro : incluirMembros) {
					IdentityManager.instance().addRoleToGroup(membro, identificador);
				}
				List<String> excluirMembros = new ArrayList<String>(membrosMap.keySet());
				excluirMembros.removeAll(membros);
				for (String membro : excluirMembros) {
					IdentityManager.instance().removeRoleFromGroup(membro, identificador);
				}
			}
			save = getRoleaction().save();
		} else {
			if(findPapelPorIdentificador(identificador) != null){
				return "fail";
			}else{
				save = getRoleaction().save();
			}
			
			EntityUtil.flush();
			if (identificador.startsWith("/")) {
				IdentityManager.instance().addRoleToGroup("admin", identificador);
			}
			refreshGrid("papelGrid");
		}
		String nome = instance.getNome();
		try {
			getEntityManager().flush();
		} catch (AssertionFailure e) {
			/* ignore */
		}

		instance = getPapel(getRoleaction().getRole());
		instance.setNome(nome);
		EntityUtil.flush();
		clear();
		return save;
	}

	@Observer("roleTreeHandlerSelected")
	public void treeSelected(Papel papel) {
		setPapelId(papel.getIdPapel());
		setTab("form");
		if (papel.getIdentificador().startsWith("/")) {
			Redirect redirect = Redirect.instance();
			redirect.setViewId("/useradmin/recursoListView.xhtml");
			redirect.execute();
		}
	}

	public static PapelHome instance() {
		return ComponentUtil.getComponent("papelHome");
	}

	public Papel getPapel(String identificador) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(Papel.class);
		criteria.add(Restrictions.eq("identificador", identificador));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Papel papel = (Papel)criteria.uniqueResult();
		return papel;
	}

	@Override
	public boolean isEditable() {
		if (Contexts.getApplicationContext().get("permitirCadastrosBasicos") == null) {
			return true;
		}
		return Contexts.getApplicationContext().get("permitirCadastrosBasicos").toString().equalsIgnoreCase("true");
	}
	
	/**
	 * Este método efetua pesquisa por um Papel através do identificador ignorando caso sensitivo 
	 * @param identificador
	 * @return Papel
	 */
	public Papel findPapelPorIdentificador(String identificador)
	{
		String query = "select p from Papel as p where lower(p.identificador) = :identificador";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("identificador", identificador.toLowerCase());
		Papel papel;
		try
		{
			papel = (Papel)q.getSingleResult();
		}
		catch (NoResultException nre)
		{
			return null;
		}
		return papel;
	}	
	
 	/**
  	 * Método responsável por montar as listas de papéis, recursos e membros, antes e depois das alterações.
  	 */
  	private void montarListasAntesSalvar(){
  		if(papeis != null){
  			papeisNovos = new ArrayList<String>();
  			papeisNovos.addAll(papeis);
  		}
  		
  		if(recursos != null){
  			recursosNovos = new ArrayList<String>();
  			recursosNovos.addAll(recursos);
  		}
  		
  		if(membros != null){
  			membrosNovos = new ArrayList<String>();
  			membrosNovos.addAll(membros);
  		}
  		
  		if(getInstance().getGrupos() != null){
  			papeisAntigos    = new ArrayList<String>();
  	 		recursosAntigos  = new ArrayList<String>();
  			
 	 		for (Papel papel : getInstance().getGrupos()) {
 	 			papeisAntigos.add(papel.getIdentificador());
 	 			recursosAntigos.add(papel.getIdentificador());
 	 		}
 	 		
 	 		removeRecursos(papeisAntigos);
 	 		removePapeis(recursosAntigos);
  		}
  		
  		if(getInstance().getHerdeiros() != null){
  			membrosAntigos = new ArrayList<String>();
 	 		for(Papel papel: getInstance().getHerdeiros()){
 	 			membrosAntigos.add(papel.getIdentificador());
 	 		}
  		}
  	}
  	
  	/**
  	 * Método responsável por registrar operção de inclusão e/ou exclusão.
  	 * 
  	 * @param List<String> lista nova
  	 * @param List<String> lista antiga
  	 */
  	private void registrarLogAlteracoes(List<String> novos, List<String> antigos){
  		if(novos != null && antigos != null){
  			List<String> listaAux = new ArrayList<String>();
  		    listaAux.addAll(antigos);
  		    antigos.removeAll(novos);
  		    novos.removeAll(listaAux);
  		    
  		    String[] nomeAtributo = {"idPapel", "nome", "identificador", "condicional"};
  		    Integer idUsuario = Authenticator.getUsuarioLogado().getIdUsuario();
  		    String ip = null;
  		    String url = null;

			try {
				url = LogUtil.getUrlRequest();
				ip = LogUtil.getIpRequest();
			} catch (LogException e) {
				e.printStackTrace();
			}
  		    
  		    if(!antigos.isEmpty()){
  	  		    for(String identificadorItem : antigos) {
  	  		    	Events.instance().raiseEvent(LogLoadEvent.DELETE_EVENT_NAME, instance.getClass(), instance.getIdPapel(), 
  	  		    			getEstadoLogPeloIdentificadorPapel(identificadorItem), nomeAtributo, idUsuario, ip, url);
  	  		    }  	  		    
  	 		}
  	 		
  	 		if(!novos.isEmpty()){
  	  		    for(String identificadorItem : novos) {
  	  	 			Events.instance().raiseEvent(LogLoadEvent.INSERT_EVENT_NAME, instance.getClass(), instance.getIdPapel(), 
  	  	 					getEstadoLogPeloIdentificadorPapel(identificadorItem), nomeAtributo, idUsuario, ip, url);
  	  		    }  	  		    
  	 		}
  		}
 	}
  	
  	private Object[] getEstadoLogPeloIdentificadorPapel(String identificador) {
    	Papel papel = findPapelPorIdentificador(identificador);
    	Object[] estado = {papel.getIdPapel(), papel.getNome(), papel.getIdentificador(), (Boolean)papel.isCondicional()};
    	return estado;
  	}

}