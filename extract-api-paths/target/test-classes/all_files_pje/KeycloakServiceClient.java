package br.jus.cnj.pje.webservice.client.keycloak;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotFoundException;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.servlet.FilterSessionStore;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore.SerializableKeycloakAccount;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.SsoServiceClient;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.util.StringUtil;

@Name(KeycloakServiceClient.NAME)
@Scope(ScopeType.APPLICATION)
public class KeycloakServiceClient implements Serializable, SsoServiceClient{

	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log logger;

	public static final String NAME = "keycloakServiceClient";
	
	public static final String KEYCLOAK_CONFIG_PATH = "/WEB-INF/keycloak.json";
	public static final String STATE_COOKIE_NAME = "OAuth_Token_Request_State";
	
	private Keycloak keycloakClient;
	private KeycloakDeployment keycloakDeployment;
	private String realmId;
	private RealmResource realmResource;
	
	@Create
	public void init() {
		if(ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled()) {
			this.initKeycloakDeployment();
			this.initKeycloakClient();
			this.realmId = this.getKeycloakDeployment().getRealm();
			this.realmResource = this.getKeycloakClient().realm(realmId);
		}
	}
	
	/**
	 * @return Instância da classe.
	 */
	public static KeycloakServiceClient instance() {
		return ComponentUtil.getComponent(KeycloakServiceClient.class);
	}

	@Override
	public void logout() {
		if(ConfiguracaoIntegracaoCloud.getSSOAuthenticationEnabled() && Authenticator.isSSOAuthentication()){
			
			if(Contexts.getSessionContext().get(Constantes.SSO_CONTEXT_NAME) != null){
				
				HttpSession httpSession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

				if (httpSession != null) {
		            SerializableKeycloakAccount account = (SerializableKeycloakAccount) httpSession.getAttribute(KeycloakAccount.class.getName());
		            if (account != null) {
		                account.getKeycloakSecurityContext().logout(this.getKeycloakDeployment());
		            }
		            cleanSession(httpSession);
		        }				
			}
		}
	}
	
	public List<ClientRepresentation> findAllClients(){
		List<ClientRepresentation> clients = new ArrayList<>();
		
		if(this.realmResource != null) {
			clients = this.realmResource.clients().findAll();
		}
		
		return clients; 
	}
	
	public GroupRepresentation createOrRecoverGroup(String groupName) {
		
		GroupRepresentation group;
		GroupRepresentation parentGroup = this.createOrRecoverParentGroup();
		try {
			group = this.getRealmResource() != null
					? this.getRealmResource().getGroupByPath(parentGroup.getPath() + "/" + groupName)
					: null;
		} catch (NotFoundException e) {
			group = new GroupRepresentation();
			group.setName(groupName);
			group.setClientRoles(null);
			group.setPath(parentGroup.getPath() + "/" + groupName);
			this.getRealmResource().groups().group(parentGroup.getId()).subGroup(group);
			group = this.getRealmResource().getGroupByPath(parentGroup.getPath() + "/" + groupName);
		} 
		
		return group;
	}
	
	private GroupRepresentation createOrRecoverParentGroup() {
		GroupRepresentation group;
		
		try {
			group = this.getRealmResource() != null
					? this.getRealmResource().getGroupByPath("/" + ConfiguracaoIntegracaoCloud.getAppName())
					: null;
		} catch (NotFoundException e) {
			group = new GroupRepresentation();
			group.setName(ConfiguracaoIntegracaoCloud.getAppName());
			group.setPath("/" + ConfiguracaoIntegracaoCloud.getAppName());
			this.getRealmResource().groups().add(group);
			group = this.getRealmResource().getGroupByPath("/" + ConfiguracaoIntegracaoCloud.getAppName());
		} 
		
		return group;		
	}
	
	public void addClientRoleToGroup(GroupRepresentation group, ClientRepresentation client, List<RoleRepresentation> roles) {
		if(group != null && client != null && roles != null) {
			if (this.getRealmResource() != null) {
				this.getRealmResource().groups().group(group.getId()).roles().clientLevel(client.getId()).add(roles);
			}
		}
	}
	
	public void removeClientRoleFromGroup(GroupRepresentation group, ClientRepresentation client, List<RoleRepresentation> roles) {
		if(group != null && client != null && roles != null) {
			if (this.getRealmResource() != null) {
				this.getRealmResource().groups().group(group.getId()).roles().clientLevel(client.getId()).remove(roles);
			}
		}
	}
	
	public void includeUserOnGroup(UserRepresentation user, GroupRepresentation group) {
		this.getRealmResource().users().get(user.getId()).joinGroup(group.getId());
	}
	
	public void removeUserFromGroup(UserRepresentation user, GroupRepresentation group) {
		if (this.getRealmResource() != null) {
			this.getRealmResource().users().get(user.getId()).leaveGroup(group.getId());
		}
	}
	
	public void syncUserGroups(Identity identityInstance, String username) {
		GroupRepresentation rootGroup = this.createOrRecoverParentGroup();
		if(rootGroup != null && rootGroup.getSubGroups() != null && !rootGroup.getSubGroups().isEmpty()) {
			UserRepresentation userSSO = this.findUserByUsername(username);
			for(GroupRepresentation childGroup : rootGroup.getSubGroups()) {
				String childGroupCleanName = childGroup.getName().split(":")[1];
				if(identityInstance.hasRole(childGroupCleanName)) {
					this.includeUserOnGroup(userSSO, childGroup);
				} else {
					this.removeUserFromGroup(userSSO, childGroup);
				}
			}
		}
	}	
	
	public List<RoleRepresentation> findAllRolesFromClient(String clientId) {
		List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>(0);

		if(this.realmResource != null) {
			roles = this.realmResource
				.clients()
				.get(this.getClientInternalId(clientId))
				.roles().list();
		}
		
		return roles;
	}
	
	public List<RoleRepresentation> findAllClientRolesFromGroup(GroupRepresentation group, ClientRepresentation client) {
		List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>(0);

		if(this.realmResource != null) {
			roles = this.realmResource
				.groups()
				.group(group.getId())
				.roles().clientLevel(client.getId()).listAll();
		}
		
		return roles;		
	}
	
	public List<RoleRepresentation> findCurrentUserRoles(SerializableKeycloakAccount keycloakAccount) {
		List<RoleRepresentation> roles = this.getKeycloakClient()
			.realm(this.getKeycloakDeployment().getRealm())
			.users()
			.get(keycloakAccount.getPrincipal().getName())
			.roles()
			.clientLevel(this.getClientInternalId(this.getKeycloakDeployment().getResourceName()))
			.listAll();
		return roles;
	}

	public List<RoleRepresentation> findCompositesRoles(String roleName) {
		Set<RoleRepresentation> roles = this.getKeycloakClient()
				.realm(this.getKeycloakDeployment().getRealm())
				.clients()
				.get(this.getClientInternalId(this.getKeycloakDeployment().getResourceName()))
				.roles()
				.get(roleName).getRoleComposites();
			
		return new ArrayList<RoleRepresentation>(roles);		
	}
	
	public RoleRepresentation findRole(String roleName) {
		RoleRepresentation role = new RoleRepresentation();
		
		role = this.getKeycloakClient()
				.realm(this.getKeycloakDeployment().getRealm())
				.clients()
				.get(this.getClientInternalId(this.getKeycloakDeployment().getResourceName()))
				.roles()
				.get(roleName).toRepresentation();
		
		return role;
	}
	
	public UserRepresentation findUserByUsername(String username) {
		UserRepresentation user = null;
		
		List<UserRepresentation> users = this.getRealmResource() != null
				? this.getRealmResource().users().search(username)
				: null;
		
		if(!CollectionUtilsPje.isEmpty(users)) {
			user = users.get(0);
		}
		
		return user;
	}
	
	public void createRoleFromPapel(Papel papel) {	
		
		RoleRepresentation role = null;

		role = new RoleRepresentation();
		role.setClientRole(true);
		role.setComposite(true);
		role.setName(papel.getIdentificador());
		role.setDescription(papel.getNome());
		try {
			this.getKeycloakClient()
				.realm(this.getKeycloakDeployment().getRealm())
				.clients()
				.get(this.getClientInternalId(this.getKeycloakDeployment().getResourceName()))
				.roles().create(role);
		} catch (Exception e) {
			//swallow
		}
	}
	
	public void loadPjeRolesToKeycloak() {
		this.pushAllPapeisToKeycloak();
		this.pushAllGruposToKeycloak();
	}

	public void pushPjeUserToKeycloak(UsuarioLogin usuario, String senha) {
		UserRepresentation user = new UserRepresentation();

		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(senha);
		credential.setTemporary(false);
		List<CredentialRepresentation> credentials = new ArrayList<CredentialRepresentation>();
		credentials.add(credential);

		user.setUsername(usuario.getLogin());
		user.setFirstName(usuario.getNome());
		user.setEmail(usuario.getEmail());
		user.setEnabled(usuario.getAtivo());
		user.setEmailVerified(false);
		user.setCredentials(credentials);

		this.getKeycloakClient().realm(this.getKeycloakDeployment().getRealm()).users().create(user);
	}

	public List<RoleRepresentation> convertListPapelToListRoleRepresentation(List<Papel> papelList){
		List<RoleRepresentation> roleList = new ArrayList<RoleRepresentation>();
		RoleRepresentation role = null;
		
		if(papelList != null && !papelList.isEmpty()) {
			for (Papel papel : papelList) {
				role = this.convertPapelToRoleRepresentation(papel);
				roleList.add(role);
			}
		}
		
		return roleList;
	}
	
	public void removePasswordCredentialsFrom(String username) throws PJeBusinessException {
		try {
			UserRepresentation user = this.findUserByUsername(username);
			if(user != null) {
				List<CredentialRepresentation> credentials = this.keycloakClient.realm(this.getKeycloakDeployment().getRealm()).users().get(user.getId()).credentials();
				
				for (CredentialRepresentation credentialRepresentation : credentials) {
					if(credentialRepresentation.getType().equals(CredentialRepresentation.PASSWORD)) {
						this.keycloakClient.realm(this.getKeycloakDeployment().getRealm()).users().get(user.getId()).removeCredential(credentialRepresentation.getId());
					}
				}
			}
		} catch (Exception e) {
			throw new PJeBusinessException("Ocorreu um erro ao revogar a senha no SSO: " + e.getMessage());
		}
	}
	
	private void pushAllPapeisToKeycloak() {
		PapelManager papelManager = ComponentUtil.getComponent("papelManager");
		
		RoleRepresentation role = null;
		
		try {
			List<Papel> papeis = papelManager.findAll();
			for (Papel papel : papeis) {
				role = new RoleRepresentation();
				role.setClientRole(true);
				role.setComposite(true);
				role.setName(papel.getIdentificador());
				role.setDescription(papel.getNome());
				try {
					this.getKeycloakClient()
						.realm(this.getKeycloakDeployment().getRealm())
						.clients()
						.get(this.getClientInternalId(this.getKeycloakDeployment().getResourceName()))
						.roles().create(role);	
				} catch (Exception e) {
					//swallow
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}		
	}
	
	private void pushAllGruposToKeycloak() {
		PapelManager papelManager = ComponentUtil.getComponent("papelManager");
		
		try {
			List<Papel> papeis = papelManager.findAll();
			for (Papel papel : papeis) {
				try {
					List<RoleRepresentation> rolesToAdd = new ArrayList<RoleRepresentation>();
					
					for(Papel pg : papel.getGrupos()) {
						rolesToAdd.add(this.findRole(pg.getIdentificador()));
					}
					
					this.getKeycloakClient()
						.realm(this.getKeycloakDeployment().getRealm())
						.clients()
						.get(this.getClientInternalId(this.getKeycloakDeployment().getResourceName()))
						.roles()
						.get(papel.getIdentificador())
						.addComposites(rolesToAdd);
				} catch (Exception e) {
					//swallow
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}			
	}
	
	private RoleRepresentation convertPapelToRoleRepresentation(Papel papel) {
		RoleRepresentation role = new RoleRepresentation();
		
		if(papel != null) {
			role = new RoleRepresentation();
			role.setClientRole(true);
			role.setComposite(true);
			role.setName(papel.getIdentificador());
			role.setDescription(papel.getNome());
		}
		
		return role;
	}

	private String getClientInternalId(String clientId) {
		String id = "";
		
		id = this.getKeycloakClient()
				.realm(this.getKeycloakDeployment().getRealm())
				.clients()
				.findByClientId(clientId)
				.get(0)
				.getId();		
		
		return id;
	}
		
	private void initKeycloakDeployment() {
		SSOConfigResolver resolver = new SSOConfigResolver();
		this.keycloakDeployment = resolver.resolve(null);
	}
	
	private void initKeycloakClient() {
		if(this.getKeycloakDeployment() != null) {
			
			String secret = "";
			
			if(!this.keycloakDeployment.getResourceCredentials().isEmpty()) {
				secret = (String)this.keycloakDeployment.getResourceCredentials().values().iterator().next();
			}
			
			this.keycloakClient = KeycloakBuilder.builder()
					.serverUrl(this.getKeycloakDeployment().getAuthServerBaseUrl())
					.realm(this.getKeycloakDeployment().getRealm())
					.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
					.clientId(this.getKeycloakDeployment().getResourceName())
					.clientSecret(secret)
					.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
					.build();
		}		
	}
	
    protected void cleanSession(HttpSession session) {
        session.removeAttribute(KeycloakAccount.class.getName());
        session.removeAttribute(KeycloakSecurityContext.class.getName());
        session.removeAttribute(FilterSessionStore.REDIRECT_URI);
        session.removeAttribute(FilterSessionStore.SAVED_METHOD);
        session.removeAttribute(FilterSessionStore.SAVED_HEADERS);
        session.removeAttribute(FilterSessionStore.SAVED_BODY);
    }
    
    public KeycloakDeployment getKeycloakDeployment() {
    	if(this.keycloakDeployment == null) {
    		this.initKeycloakDeployment();
    	}
		return this.keycloakDeployment;
	}
    
    public Keycloak getKeycloakClient() {
    	if(this.keycloakClient == null) {
    		this.initKeycloakClient();
    	}
		return keycloakClient;
	}
    
    private RealmResource getRealmResource() {
		return this.getKeycloakClient() != null && StringUtil.isEmpty(realmId) == false
				? this.getKeycloakClient().realm(realmId)
				: null;
    }
    
	public void enableUser(String username) throws PJeBusinessException {
		try {
			UserRepresentation user = this.findUserByUsername(username);
			if (user != null && !user.isEnabled()) {
				user.setEnabled(true);
				realmResource.users().get(user.getId()).update(user);
			}
		} catch (Exception e) {
			throw new PJeBusinessException("Ocorreu um erro ao ativar o usuário no SSO: " + e.getMessage());
		}
	}

	public void resetPassword(Usuario usuario, String senha) throws PJeBusinessException {
		try {
			UserRepresentation user = this.findUserByUsername(usuario.getLogin());

			if (user == null) {
				pushPjeUserToKeycloak(usuario, senha);
			} else {
				CredentialRepresentation credential = new CredentialRepresentation();
				credential.setType(CredentialRepresentation.PASSWORD);
				credential.setValue(senha);
				credential.setTemporary(false);

				this.keycloakClient.realm(this.getKeycloakDeployment().getRealm()).users().get(user.getId())
						.resetPassword(credential);
			}
		} catch (Exception e) {
			throw new PJeBusinessException("Ocorreu um reiniciar a senha no SSO: " + e.getMessage());
		}
	}
	
	
	public void updateEmail(String username, String email) throws PJeBusinessException {
		try {
			UserRepresentation user = this.findUserByUsername(username);
			if (user != null && user.isEnabled() && email != null) {
				user.setEmail(email);
				realmResource.users().get(user.getId()).update(user);
			}
		} catch (Exception e) {
			throw new PJeBusinessException("Ocorreu um erro ao atualizar o e-mail no SSO: " + e.getMessage());
		}
	}
	
	/**
	 * Loga com ClientId/Secret e retorna o token.
	 * 
	 * @param clientId
	 * @param secret
	 * @return Token.
	 */
	public String login(String clientId, String secret) {
		String token = null;
		
		try {
			Keycloak kc = KeycloakBuilder.builder()
					.serverUrl(getKeycloakDeployment().getAuthServerBaseUrl())
					.realm(getKeycloakDeployment().getRealm())
					.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
					.clientId(clientId)
					.clientSecret(secret)
					.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
					.build();
				
				AccessTokenResponse accesTokenResponse = kc.tokenManager().getAccessToken();
				token = accesTokenResponse.getToken();
		} catch (Exception e) {
			String mensagem = String.format("Não foi possível logar no client '%s'. Erro: %s", clientId, e.getMessage());
			throw new AplicationException(mensagem);
		}
		return token;
	}
}
 