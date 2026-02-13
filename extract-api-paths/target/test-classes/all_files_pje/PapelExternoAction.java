package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import br.com.infox.access.home.PapelHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.webservice.client.keycloak.KeycloakServiceClient;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name(PapelExternoAction.NAME)
@Scope(ScopeType.PAGE)
public class PapelExternoAction implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public static final String NAME = "papelExternoAction";
	public static final String GROUP_PREFIX = ConfiguracaoIntegracaoCloud.getAppName();
	
	private KeycloakServiceClient keycloakServiceClient;
	private UsuarioManager usuarioManager;
	
	private ClientRepresentation cliente;
	private RoleRepresentation papelExterno;
	private Papel papelInterno;
	private GroupRepresentation group;
	
	private List<ClientRepresentation> clientes;
	private List<RoleRepresentation> papeisExternosDisponiveis = new ArrayList<RoleRepresentation>(0);
	private List<RoleRepresentation> papeisExternosGrupo = new ArrayList<RoleRepresentation>(0);
	
	@Create
	public void init() {
		this.keycloakServiceClient = ComponentUtil.getComponent(KeycloakServiceClient.NAME);
		this.usuarioManager = ComponentUtil.getComponent(UsuarioManager.class);
		this.clientes = this.keycloakServiceClient.findAllClients();
		this.papelInterno = PapelHome.instance().getInstance();
		this.group = this.keycloakServiceClient.createOrRecoverGroup(GROUP_PREFIX + ":" + this.papelInterno.getIdentificador());
	}
	
	public void recuperarPapeisExternos() {
		if(this.keycloakServiceClient != null && this.cliente != null) {
			this.papeisExternosDisponiveis = this.keycloakServiceClient.findAllRolesFromClient(this.cliente.getClientId());
		}
		
		if(this.group != null && this.cliente != null) {
			List<RoleRepresentation> rolesFromGroup = this.keycloakServiceClient.findAllClientRolesFromGroup(group, cliente);
			for (RoleRepresentation rfg : rolesFromGroup) {
				for (RoleRepresentation role : this.papeisExternosDisponiveis) {
					if(rfg.getId().equals(role.getId())) {
						this.papeisExternosGrupo.add(role);
					}
				}
			}
		}
	}
	
	public void atribuirPapelExterno() throws PJeBusinessException {
		this.keycloakServiceClient.removeClientRoleFromGroup(this.group, this.cliente, this.papeisExternosDisponiveis);
		this.keycloakServiceClient.addClientRoleToGroup(this.group, this.cliente, this.papeisExternosGrupo);
		
		this.sujarCadastroUsuarios(this.papelInterno);
	}
	
	private void sujarCadastroUsuarios(Papel papel) throws PJeBusinessException {
		List<Integer> usuarioList = usuarioManager.consultarIdsUsuariosPorPapelHerdado(papel);

		for (Integer integer : usuarioList) {
			System.out.println(integer);
		}
		
		this.usuarioManager.marcarFlagAtualizaSSOPorPapelHerdado(papel);
	}
		
	
	public ClientRepresentation getCliente() {
		return cliente;
	}
	
	public void setCliente(ClientRepresentation cliente) {
		this.cliente = cliente;
	}
	
	public List<RoleRepresentation> getPapeisExternosDisponiveis() {
		return papeisExternosDisponiveis;
	}
	
	public void setPapeisExternosDisponiveis(List<RoleRepresentation> papeisExternosDisponiveis) {
		this.papeisExternosDisponiveis = papeisExternosDisponiveis;
	}

	public List<RoleRepresentation> getPapeisExternosGrupo() {
		return papeisExternosGrupo;
	}
	
	public void setPapeisExternosGrupo(List<RoleRepresentation> papeisExternosGrupo) {
		this.papeisExternosGrupo = papeisExternosGrupo;
	}
	
	public RoleRepresentation getPapelExterno() {
		return papelExterno;
	}
	
	public void setPapelExterno(RoleRepresentation papelExterno) {
		this.papelExterno = papelExterno;
	}
	
	public List<ClientRepresentation> getClientes() {
		return clientes;
	}
	
	public void setClientes(List<ClientRepresentation> clientes) {
		this.clientes = clientes;
	}
	
	public Papel getPapelInterno() {
		return papelInterno;
	}
	
	public void setPapelInterno(Papel papelInterno) {
		this.papelInterno = papelInterno;
	}
	
}
