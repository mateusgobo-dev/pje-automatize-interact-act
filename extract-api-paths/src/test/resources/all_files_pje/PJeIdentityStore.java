/**
 *  pje-web
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.identity;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.security.management.IdentityStore;
import org.keycloak.representations.idm.RoleRepresentation;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * @author cristof
 *
 */
@Name("org.jboss.seam.security.identityStore")
@Install(precedence = Install.DEPLOYMENT)
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class PJeIdentityStore implements IdentityStore {
	
	private FeatureSet featureSet;
	
	private PessoaService pessoaService;
	
	private UsuarioService usuarioService;
	
	private UsuarioManager usuarioManager;
	
	private PapelManager papelManager;
	
	private KeycloakServiceClient keycloakServiceClient;
	
	private Boolean isSSOAuthenticationEnabled = Boolean.FALSE;
	
	private Boolean isSSOAuthorizationEnabled = Boolean.FALSE;
	
	@Create
	public void init(){
		featureSet = new FeatureSet();
		featureSet.enableAll();
		pessoaService = (PessoaService) Component.getInstance("pessoaService");
		usuarioService = (UsuarioService) Component.getInstance("usuarioService");
		usuarioManager = (UsuarioManager) Component.getInstance("usuarioManager");
		papelManager = (PapelManager) Component.getInstance("papelManager");
		keycloakServiceClient = ComponentUtil.getComponent(KeycloakServiceClient.NAME);
		isSSOAuthenticationEnabled = PjeUtil.instance().isSSOAuthenticationEnabled();
		isSSOAuthorizationEnabled = ConfiguracaoIntegracaoCloud.getSSOAuthorizationEnabled();
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#supportsFeature(org.jboss.seam.security.management.IdentityStore.Feature)
	 */
	@Override
	public boolean supportsFeature(Feature feature) {
		return featureSet.supports(feature);
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#createUser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean createUser(String username, String password) {
		throw new UnsupportedOperationException("Este repositório de usuário ainda não implementa a operação de criar usuário. Por favor, utilize UsuarioService.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean createUser(String username, String password, String firstname, String lastname) {
		try {
			Usuario usuario = new Usuario();
			usuario.setLogin(username);
			usuario.setNome(firstname);
			usuario.setSenha(null);//sera gerada automaticamente
			usuarioManager.persistAndFlush(usuario);
			
			return true;
		} catch (PJeBusinessException e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#deleteUser(java.lang.String)
	 */
	@Override
	public boolean deleteUser(String name) {
		throw new UnsupportedOperationException("Este repositório de usuário ainda não implementa a operação de apagar usuário. Por favor, utilize UsuarioService.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#enableUser(java.lang.String)
	 */
	@Override
	public boolean enableUser(String name) {
		throw new UnsupportedOperationException("Este repositório de usuário ainda não implementa a operação de ativar usuário. Por favor, utilize UsuarioService.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#disableUser(java.lang.String)
	 */
	@Override
	public boolean disableUser(String name) {
		throw new UnsupportedOperationException("Este repositório de usuário ainda não implementa a operação de desativar usuário. Por favor, utilize UsuarioService.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#isUserEnabled(java.lang.String)
	 */
	@Override
	public boolean isUserEnabled(String name) {
		Usuario u = usuarioManager.findByLogin(name);
		return u.getAtivo();
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#changePassword(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean changePassword(String name, String password) {
		try {
			usuarioManager.alterarSenha(name, password);
			return true;
		} catch (PJeBusinessException e) {
			ResourceBundle bundle = SeamResourceBundle.getBundle();
			String message = bundle.getString(e.getCode());
			throw new IllegalArgumentException("Erro ao alterar a senha. "+message);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#userExists(java.lang.String)
	 */
	@Override
	public boolean userExists(String name) {
		Usuario u = usuarioManager.findByLogin(name);
		return u != null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#createRole(java.lang.String)
	 */
	@Override
	public boolean createRole(String role) {
		try {
			return papelManager.criarPapel(role);
		} catch (PJeBusinessException e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#grantRole(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean grantRole(String name, String role) {
		try{
			Usuario u = usuarioManager.findByLogin(name);
			Papel p = papelManager.findByCodeName(role);
			if(u == null || p == null){
				return false;
			}
			boolean ret = u.getPapelSet().add(p);
			if(ret){
				usuarioManager.flush();
			}
			return ret;
		}catch(Exception e){
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#revokeRole(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean revokeRole(String name, String role) {
		try{
			Usuario u = usuarioManager.findByLogin(name);
			Papel p = papelManager.findByCodeName(role);
			if(u == null || p == null){
				return false;
			}
			boolean ret = u.getPapelSet().remove(p);
			if(ret){
				usuarioManager.flush();
			}
			return ret;
		}catch(Exception e){
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#deleteRole(java.lang.String)
	 */
	@Override
	public boolean deleteRole(String role) {
		throw new UnsupportedOperationException("Operação ainda não implementada.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#roleExists(java.lang.String)
	 */
	@Override
	public boolean roleExists(String name) {
		boolean exists = false;
		
		try {
			if(this.isSSOAuthenticationEnabled && this.isSSOAuthorizationEnabled) {
				exists = this.keycloakServiceClient.findRole(name) != null;
			} else {
				exists = papelManager.findByCodeName(name) != null;
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		return exists;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#addRoleToGroup(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addRoleToGroup(String role, String group) {
		try{
			Papel p = papelManager.findByCodeName(role);
			Papel g = papelManager.findByCodeName(group);
			if(p == null || g == null){
				return false;
			}
			boolean ret = p.getGrupos().add(g);
			if(ret){
				papelManager.flush();
			}
			return ret;
		}catch(Exception e){
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#removeRoleFromGroup(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeRoleFromGroup(String role, String group) {
		try{
			Papel g = papelManager.findByCodeName(group);
			Papel p = papelManager.findByCodeName(role);
			if(g == null || p == null){
				return false;
			}
			boolean ret = p.getGrupos().remove(g);
			if(ret){
				papelManager.flush();
			}
			return ret;
		}catch(Exception e){
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#listUsers()
	 */
	@Override
	public List<String> listUsers() {
		List<String> ret = new ArrayList<String>();
		try {
			for(Usuario u: usuarioManager.findAll()){
				ret.add(u.getLogin());
			}
			return ret;
		} catch (PJeBusinessException e) {
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#listUsers(java.lang.String)
	 */
	@Override
	public List<String> listUsers(String filter) {
		throw new UnsupportedOperationException("Operação não implementada.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#listRoles()
	 */
	@Override
	public List<String> listRoles() {
		List<String> ret = new ArrayList<String>();
		try{
			for(Papel p: papelManager.findAll()){
				ret.add(p.getIdentificador());
			}
			return ret;
		}catch(PJeBusinessException e){
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#listGrantableRoles()
	 */
	@Override
	public List<String> listGrantableRoles() {
		try{
			return papelManager.recuperaAtribuiveis();
		}catch(PJeBusinessException e){
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#getGrantedRoles(java.lang.String)
	 */
	@Override
	public List<String> getGrantedRoles(String name) {
		return getPapeis(name, false);
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#getImpliedRoles(java.lang.String)
	 */
	@Override
	public List<String> getImpliedRoles(String name) {
		if(this.isSSOAuthenticationEnabled && this.isSSOAuthorizationEnabled) {
			return getPapeisFromSSO(name, true);
		} else {
			return getPapeis(name, true);			
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#getRoleGroups(java.lang.String)
	 */
	@Override
	public List<String> getRoleGroups(String name) {
		List<String> ret = new ArrayList<String>();
		Papel p;
		try {
			p = papelManager.findByCodeName(name);
			if(p == null){
				return Collections.emptyList();
			}
			for(Papel aux: p.getGrupos()){
				ret.add(aux.getIdentificador());
			}
		} catch (PJeBusinessException e) {
			return Collections.emptyList();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#listMembers(java.lang.String)
	 */
	@Override
	public List<Principal> listMembers(String role) {
		Papel p;
		try {
			p = papelManager.findByCodeName(role);
			papelManager.refresh(p);
			if(p != null && p.getHerdeiros() != null && !p.getHerdeiros().isEmpty()){
				List<Principal> ret = new ArrayList<Principal>(p.getHerdeiros().size());
				for(Papel aux: p.getHerdeiros()){
					ret.add(new SimplePrincipal(aux.getIdentificador()));
				}
				return ret;
			}
		} catch (PJeBusinessException e) {
			// swallowed
		}
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.security.management.IdentityStore#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticate(String username, String password) {
		Usuario usuario = usuarioManager.findByLogin(username);
		if(usuario != null) {
			return usuarioManager.authenticate(usuario, password);
		}
		return false;
	}
	
	private UsuarioLocalizacao recuperaLocalizacaoAtual(Usuario usuario){
		try {
			Pessoa p = pessoaService.findById(usuario.getIdUsuario());
			List<UsuarioLocalizacao> localizacoesAtivas = usuarioService.getLocalizacoesAtivas(p);
			if (localizacoesAtivas != null && !localizacoesAtivas.isEmpty()){
				UsuarioLocalizacao loc = localizacoesAtivas.get(0);
				return loc;
			}else{
				return null;
			}
		} catch (PJeBusinessException e) {
			return null;
		}
	}
	
	private List<String> getPapeisFromSSO(String login, boolean incluirHerdados) {
		try {
			List<String> ret = new ArrayList<String>();
	
			UsuarioLocalizacao ul = (UsuarioLocalizacao) Component.getInstance("usuarioLogadoLocalizacaoAtual", ScopeType.SESSION);
			Usuario u = usuarioManager.findByLogin(login);
			if(ul != null && ul.getIdUsuarioLocalizacao() == 0){
				ul = recuperaLocalizacaoAtual(u);
			}
			
			if(ul == null){
				return Collections.emptyList();
			}
			
			ul = ((UsuarioLocalizacaoManager) Component.getInstance("usuarioLocalizacaoManager")).findById(ul.getIdUsuarioLocalizacao());
			
			if(ul != null && ul.getPapel() != null){
				RoleRepresentation keycloakRole = this.keycloakServiceClient.findRole(ul.getPapel().getIdentificador());
				if(keycloakRole != null) {
					this.carregaPapeisSSO(keycloakRole.getName(), ret);
				}
			}
			
			return ret; 
		} catch (PJeBusinessException e) {
			return Collections.emptyList();
		}		
	}
	
	private List<String> getPapeis(String login, boolean incluirHerdados){
		try {
			List<String> ret = new ArrayList<String>();
			UsuarioLocalizacao ul = (UsuarioLocalizacao) Component.getInstance("usuarioLogadoLocalizacaoAtual", ScopeType.SESSION);
			if(ul != null && ul.getIdUsuarioLocalizacao() == 0){
				Usuario u = usuarioManager.findByLogin(login);
				ul = recuperaLocalizacaoAtual(u);
			}
			if(ul == null){
				return Collections.emptyList();
			}
			ul = ((UsuarioLocalizacaoManager) Component.getInstance("usuarioLocalizacaoManager")).findById(ul.getIdUsuarioLocalizacao());
			if(ul != null && ul.getPapel() != null){
				Papel p = ul.getPapel();
				if(incluirHerdados){
					carregaPapeis(p, ret);
				}else{
					ret.add(p.getIdentificador());
				}
			}
			return ret;
		} catch (PJeBusinessException e) {
			return Collections.emptyList();
		}
	}
	
	private void carregaPapeis(Papel root, List<String> lista){
		List<Papel> papelList = papelManager.getPapeisHerdados(root);
		for (Papel papel : papelList) {
			lista.add(papel.getIdentificador());
		}
	}
	
	private void carregaPapeisSSO(String papel, List<String> lista){
		if(lista.contains(papel)){
			return;
		}else{
			lista.add(papel);
			List<RoleRepresentation> composites = keycloakServiceClient.findCompositesRoles(papel);
			for(RoleRepresentation role: composites){
				carregaPapeisSSO(role.getName(), lista);
			}
		}
	}	

}
