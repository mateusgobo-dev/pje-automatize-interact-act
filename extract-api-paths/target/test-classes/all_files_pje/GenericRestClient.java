package br.jus.cnj.pje.webservice.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.PjeEurekaRegister;

@Scope(ScopeType.EVENT)
public abstract class GenericRestClient {
	
	protected Client client;
	
	protected WebTarget webTarget;
	
	protected abstract String getServicePath();
	
	@Create
	public void init() {
		this.client = ClientBuilder.newClient();
	}
	
	protected String getGatewayPath() {
		return PjeEurekaRegister.instance().getUrlGatewayService(false);
	}
}
