package br.jus.cnj.pje.webservice;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.CloudEventUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

@Name(PjeEurekaRegister.NAME)
@Scope(ScopeType.APPLICATION)
@Startup()
@Install()
public class PjeEurekaRegister {

	public static final String NAME = "pjeEurekaRegister";
	
	public static final String GATEWAY_SERVICE_NAME = "gateway-service";
	
	@Logger
	private Log logger;

	private PjeDataCenterInstanceConfig instanceConfig;
	
	private String instanceId;
	
	private EurekaClient eurekaClient;

	@Create
	public void init(){
		try{
			Boolean deveResgistrarNoServiceDiscovery = ConfiguracaoIntegracaoCloud.isRegistrarServiceDiscovery();
			if(deveResgistrarNoServiceDiscovery != null && deveResgistrarNoServiceDiscovery){
				this.instanceConfig = new PjeDataCenterInstanceConfig();
				this.instanceConfig.setInstanceId(CloudEventUtil.generateInstanceId());
				this.instanceId = this.instanceConfig.getInstanceId();
				InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
				ApplicationInfoManager appInfo = new ApplicationInfoManager(instanceConfig, instanceInfo);
				appInfo.setInstanceStatus(InstanceStatus.STARTING);
				this.eurekaClient = new DiscoveryClient(appInfo, new PjeEurekaClientConfig());
				if(this.eurekaClient != null) {
					appInfo.setInstanceStatus(InstanceStatus.UP);								
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Recupera a url do gateway-service a partir do eureka-client.
	 * Caso a variável de ambiente ENV_PJE2_CLOUD_URL_GATEWAY esteja configurada, irá retornar o valor da variável 
	 * 
	 * @param secure indica se esta requisição deve ser HTTP ou HTTPS
	 * @return url da instancia, ou null caso nenhuma instancia seja encontrada
	 */
	public String getUrlGatewayService(Boolean secure) {
		
		String url = null;
		
		if(StringUtils.isEmpty(ConfiguracaoIntegracaoCloud.getUrlPje2Gateway())) {
			secure = secure == null ? false : secure;
			try {
				InstanceInfo instance = this.eurekaClient.getNextServerFromEureka(GATEWAY_SERVICE_NAME, secure);
				url = instance.getHomePageUrl();
			} catch (RuntimeException re){
				logger.error("Nenhuma instancia encontrada para: " + GATEWAY_SERVICE_NAME);
			}
		} else {
			url = ConfiguracaoIntegracaoCloud.getUrlPje2Gateway();
		}
		
		return url;
	}
	
	@Destroy
	public void destroy() {
		this.getEurekaClient().shutdown();
	}
	
	public static PjeEurekaRegister instance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	public EurekaInstanceConfig getInstanceConfig() {
		return instanceConfig;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public EurekaClient getEurekaClient() {
		return eurekaClient;
	}

	/**
	 * @return URL do Domicílio Eletrônico.
	 */
	public String getURLDomicilioEletronico() {
		return getURLDomicilioEletronico(null);
	}
	
	/**
	 * @param resource Recurso.
	 * @return URL do Domicílio Eletrônico com recurso que será usado.
	 */
	public String getURLDomicilioEletronico(String resource) {
		String gateway = getUrlGatewayService(false).trim();

		StringBuilder url = new StringBuilder(gateway);
		url.append(gateway.endsWith("/") ? "" : "/");
		url.append(ParametroUtil.instance().getDomicilioEletronicoServiceName());
		if (resource != null) {
			url.append(resource);
		}

		return url.toString();
	}
	
	/**
	 * @return URL da Comunicação Processual.
	 */
	public String getURLComunicacaoProcessual() {
		return getURLComunicacaoProcessual(null);
	}
	
	/**
	 * @param resource Recurso.
	 * @return URL da Comunicação Processual com recurso que será usado.
	 */
	public String getURLComunicacaoProcessual(String resource) {
		String gateway = getUrlGatewayService(false).trim();

		StringBuilder url = new StringBuilder(gateway);
		url.append(gateway.endsWith("/") ? "" : "/");
		url.append(ParametroUtil.instance().getDomicilioEletronicoComunicacaoProcessualServiceName());
		if (resource != null) {
			url.append(resource);
		}

		return url.toString();
	}
	
}
