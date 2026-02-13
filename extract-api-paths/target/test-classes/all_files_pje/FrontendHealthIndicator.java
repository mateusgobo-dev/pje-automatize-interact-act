package br.jus.cnj.pje.status;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class FrontendHealthIndicator extends AbstractHealthIndicator{

	protected Client client;
	
	protected WebTarget webTarget;
	
	@Override
	public Health doHealthCheck() {
		ClientResponse resp;
		
		this.client = ClientBuilder.newClient();
		
		this.webTarget = this.client.target(this.getUrlFrontend());
		
		Invocation.Builder invocationBuilder = this.webTarget.request();
		
		try{
			resp = (ClientResponse) invocationBuilder.get();
			if(resp.getStatus() == HttpStatus.SC_OK){
				this.getDetails().put("success", "O pje-legacy consegue se comunicar com o  frontend corretamente.");
				this.getDetails().put("url", this.getUrlFrontend());
				this.setHealth(new Health(Status.UP, this.getDetails()));
			} else {
				this.getDetails().put("error", "Problema de comunicação entre o pje-legacy e o frontend.");
				this.getDetails().put("url", this.getUrlFrontend());
				this.setHealth(new Health(Status.DOWN, this.getDetails()));
			}
		}catch (Exception e) {
			this.getDetails().put("error", "Problema de comunicação entre o pje-legacy e o frontend.");
			this.getDetails().put("url", this.getUrlFrontend());
			this.setHealth(new Health(Status.DOWN, this.getDetails()));			
		}
		
		return this.getHealth();
	}
	
	private String getUrlFrontend() {
		return ConfiguracaoIntegracaoCloud.getUrlPje2Cliente();
	}

}
