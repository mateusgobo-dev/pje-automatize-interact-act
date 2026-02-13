package br.jus.cnj.pje.webservice.controller.status;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.status.ConsultaReceitaHealthIndicator;
import br.jus.cnj.pje.status.CriminalHealthIndicator;
import br.jus.cnj.pje.status.DatabaseHealthIndicator;
import br.jus.cnj.pje.status.DiscoveryClientHealthIndicator;
import br.jus.cnj.pje.status.FrontendHealthIndicator;
import br.jus.cnj.pje.status.GatewayHealthIndicator;
import br.jus.cnj.pje.status.Health;
import br.jus.cnj.pje.status.RabbitmqHealthIndicator;
import br.jus.cnj.pje.status.SSOHealthIndicator;
import br.jus.cnj.pje.status.Status;
import br.jus.cnj.pje.webservice.controller.status.dto.InfoDTO;

@Name(StatusRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("pje-legacy/status")
public class StatusRestController implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "statusRestController"; 
	
	@GET
	@Path("/health")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheck(){
		
		RabbitmqHealthIndicator rabbitHealthIdicator = new RabbitmqHealthIndicator();
		DatabaseHealthIndicator databaseHealthIdicator = new DatabaseHealthIndicator();
		DiscoveryClientHealthIndicator discoveryClientHealthIndicator = new DiscoveryClientHealthIndicator();
		GatewayHealthIndicator gatewayHealthIndicator = new GatewayHealthIndicator();
		CriminalHealthIndicator criminalHealthIndicator = new CriminalHealthIndicator();
		ConsultaReceitaHealthIndicator consultaReceitaHealthIndicator = new ConsultaReceitaHealthIndicator();
		FrontendHealthIndicator frontendHealthIndicator = new FrontendHealthIndicator();
		SSOHealthIndicator ssoHealthIndicator = new SSOHealthIndicator(); 
		
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("database", databaseHealthIdicator.doHealthCheck());
		details.put("message-broker", rabbitHealthIdicator.doHealthCheck());
		details.put("dicovery-client", discoveryClientHealthIndicator.doHealthCheck());
		details.put("gateway", gatewayHealthIndicator.doHealthCheck());
		details.put("criminal", criminalHealthIndicator.doHealthCheck());
		details.put("receita-cpf", consultaReceitaHealthIndicator.doHealthCheck());
		details.put("frontend", frontendHealthIndicator.doHealthCheck());
		details.put("sso", ssoHealthIndicator.doHealthCheck());
		
		Health health = new Health(Status.UP, details);
				
				
		Response res = Response.ok(health).build();
		
		return res;
	}
	
	@GET
	@Path("/info")
	@Produces(MediaType.APPLICATION_JSON)
	public Response info(){
		
		ParametroUtil pUtil = ComponentUtil.getParametroUtil();
		
		InfoDTO info = new InfoDTO();
		info.setVersion(ConfiguracaoIntegracaoCloud.getAppVersion());
		info.setTribunal(pUtil.getNomeSistema() + pUtil.getSiglaTribunal());
		info.setInstancia(pUtil.getCodigoInstanciaAtual());
		info.setTipoJustica(pUtil.getTipoJustica());
		
		Response res = Response.ok(info).build();
		
		return res;
	}	
	
	@GET
	@Path("/health/message-broker")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckMessageBroker() {

		RabbitmqHealthIndicator healthIdicator = new RabbitmqHealthIndicator();
		
		Response res = Response.ok(healthIdicator.doHealthCheck()).build();
		
		return res;
	}
	
	@GET
	@Path("/health/database")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckDatabase() {

		DatabaseHealthIndicator healthIdicator = new DatabaseHealthIndicator();
		
		Response res = Response.ok(healthIdicator.doHealthCheck()).build();
		
		return res;
	}
	
	@GET
	@Path("/health/discovery-client")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckEurekaClient() {

		DiscoveryClientHealthIndicator discoveryClientHealthIndicator = new DiscoveryClientHealthIndicator();
		
		Response res = Response.ok(discoveryClientHealthIndicator.doHealthCheck()).build();
		
		return res;
	}
	
	@GET
	@Path("/health/gateway")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckGateway() {

		GatewayHealthIndicator gatewayHealthIndicator = new GatewayHealthIndicator();
		
		Response res = Response.ok(gatewayHealthIndicator.doHealthCheck()).build();
		
		return res;
	}
	
	
	@GET
	@Path("/health/criminal")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckCriminal() {

		CriminalHealthIndicator criminalHealthIndicator = new CriminalHealthIndicator();
		
		Response res = Response.ok(criminalHealthIndicator.doHealthCheck()).build();
		
		return res;
	}		

	@GET
	@Path("/health/receita-cpf")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckReceitaCpf() {
		
		ConsultaReceitaHealthIndicator consultaReceitaHealthIndicator = new ConsultaReceitaHealthIndicator();
		
		Response res = Response.ok(consultaReceitaHealthIndicator.doHealthCheck()).build();
		
		return res;
	}
	
	@GET
	@Path("/health/frontend")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckFrontend() {
		
		FrontendHealthIndicator frontendHealthIndicator = new FrontendHealthIndicator();
		
		Response res = Response.ok(frontendHealthIndicator.doHealthCheck()).build();
		
		return res;
	}	

	@GET
	@Path("/health/sso")
	@Produces(MediaType.APPLICATION_JSON)
	public Response healthcheckSSO() {
		
		SSOHealthIndicator ssoHealthIndicator = new SSOHealthIndicator();
		
		Response res = Response.ok(ssoHealthIndicator.doHealthCheck()).build();
		
		return res;
	}

	@GET
	@Path("/whoami")
	@Produces(MediaType.APPLICATION_JSON)
	public Response whoami() {
		
		Map<String, Object> whoami = new LinkedHashMap<String, Object>();
		
		whoami.put("Usuário", Identity.instance().getPrincipal().getName());
		whoami.put("Lotação", Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao() + " - " + Authenticator.getUsuarioLocalizacaoAtual().toString());
		
		Response res = Response.ok(whoami).build();
		return res;
	}
	
	@GET
	@Path("/envs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response envs() {
		
		Map<String, Object> envs = new LinkedHashMap<String, Object>();
		
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_AUDITORIA_TIPO_PERSISTENCIA, ConfiguracaoIntegracaoCloud.getAuditoriaTipoPersistencia());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLIENTE_URL, ConfiguracaoIntegracaoCloud.getUrlPje2Cliente());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CORS_ENABLED, ConfiguracaoIntegracaoCloud.getUrlPje2Cliente());

		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_CLIENT_HOSTNAME, ConfiguracaoIntegracaoCloud.getEurekaClientHostname());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_CLIENT_SECURE_PORT, ConfiguracaoIntegracaoCloud.getEurekaClientSecurePort());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_CLIENT_SECURE_PORT_ENABLED, ConfiguracaoIntegracaoCloud.isEurekaClientSecurePortEnabled().toString());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_CLIENT_NONSECURE_PORT, ConfiguracaoIntegracaoCloud.getEurekaClientNonSecurePort());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_CLIENT_NONSECURE_PORT_ENABLED, ConfiguracaoIntegracaoCloud.isEurekaClientNonSecurePortEnabled().toString());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_SERVER_URL, ConfiguracaoIntegracaoCloud.getUrlEurekaServer());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_EUREKA_SHOULD_USE_DNS, ConfiguracaoIntegracaoCloud.isEurekaShouldUseDns().toString());

		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_APP_NAME, ConfiguracaoIntegracaoCloud.getAppName());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_URL_GATEWAY, ConfiguracaoIntegracaoCloud.getUrlPje2Gateway());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_REGISTRAR, ConfiguracaoIntegracaoCloud.isRegistrarServiceDiscovery().toString());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_REGISTRA_COM_IP, ConfiguracaoIntegracaoCloud.isRegistraComIp().toString());

		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_HOST, ConfiguracaoIntegracaoCloud.getRabbitHost());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_PORT, String.valueOf(ConfiguracaoIntegracaoCloud.getRabbitPort()));
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_VIRTUALHOST, ConfiguracaoIntegracaoCloud.getRabbitVirtualHost());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_USERNAME, ConfiguracaoIntegracaoCloud.getRabbitUsername());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_PASSWORD, "******");
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_QUEUENAME, ConfiguracaoIntegracaoCloud.getRabbitQueueName());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_EXCHANGENAME, ConfiguracaoIntegracaoCloud.getRabbitExchangeName());
		envs.put(ConfiguracaoIntegracaoCloud.ENV_PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES, ConfiguracaoIntegracaoCloud.isPublishMessages().toString());

		Response res = Response.ok(envs).build();
		return res;
	}
	
}
