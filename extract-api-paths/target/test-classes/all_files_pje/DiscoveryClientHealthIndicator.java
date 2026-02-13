package br.jus.cnj.pje.status;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;

public class DiscoveryClientHealthIndicator extends AbstractHealthIndicator{

	@Override
	public Health doHealthCheck() {
		
		if(PjeEurekaRegister.instance() != null && PjeEurekaRegister.instance().getEurekaClient() != null) {
			this.getDetails().put("eureka-client", PjeEurekaRegister.instance().getInstanceConfig());
			this.getDetails().put("url", ConfiguracaoIntegracaoCloud.getUrlEurekaServer());
			this.setHealth(new Health(Status.UP, this.getDetails()));
		} else {
			this.getDetails().put("error", "Esta instância não está resgistrada no service discovery");
			this.getDetails().put("url", ConfiguracaoIntegracaoCloud.getUrlEurekaServer());
			this.setHealth(new Health(Status.DOWN, this.getDetails()));			
		}
		
		return this.getHealth();
	}

}
