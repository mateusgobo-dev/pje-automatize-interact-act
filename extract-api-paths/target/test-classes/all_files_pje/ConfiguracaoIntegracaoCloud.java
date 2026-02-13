package br.jus.cnj.pje.nucleo;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.util.StringUtil;

public class ConfiguracaoIntegracaoCloud {
	
	public static final String INTEGRACAO_PROPERTIES = "integracao.properties";
	public static final String EUREKA_CLIENT_PROPERTIES = "eureka-client.properties";

	public static final String ENV_PJE2_AUDITORIA_TIPO_PERSISTENCIA = "ENV_PJE2_AUDITORIA_TIPO_PERSISTENCIA";
	public static final String ENV_PJE2_CLIENTE_URL = "ENV_PJE2_CLIENTE_URL";
	public static final String ENV_PJE2_CORS_ENABLED = "ENV_PJE2_CORS_ENABLED";
	public static final String ENV_PJE2_CLOUD_APP_NAME = "ENV_PJE2_CLOUD_APP_NAME";
	public static final String ENV_PJE2_CLOUD_URL_GATEWAY = "ENV_PJE2_CLOUD_URL_GATEWAY";
	public static final String ENV_PJE2_CLOUD_REGISTRA_COM_IP = "ENV_PJE2_CLOUD_REGISTRA_COM_IP";
	public static final String ENV_PJE2_CLOUD_REGISTRAR = "ENV_PJE2_CLOUD_REGISTRAR";  

	public static final String ENV_EUREKA_SERVER_URL = "ENV_EUREKA_SERVER_URL";
	public static final String ENV_EUREKA_CLIENT_HOSTNAME = "ENV_EUREKA_CLIENT_HOSTNAME";
	public static final String ENV_EUREKA_CLIENT_SECURE_PORT = "ENV_EUREKA_CLIENT_SECURE_PORT";
	public static final String ENV_EUREKA_CLIENT_SECURE_PORT_ENABLED = "ENV_EUREKA_CLIENT_SECURE_PORT_ENABLED";
	public static final String ENV_EUREKA_CLIENT_NONSECURE_PORT = "ENV_EUREKA_CLIENT_NONSECURE_PORT";
	public static final String ENV_EUREKA_CLIENT_NONSECURE_PORT_ENABLED = "ENV_EUREKA_CLIENT_NONSECURE_PORT_ENABLED";
	public static final String ENV_EUREKA_SHOULD_USE_DNS = "ENV_EUREKA_SHOULD_USE_DNS"; 
	
	public static final String ENV_PJE2_CLOUD_RABBIT_CONNECTION_REQUIRED = "ENV_PJE2_CLOUD_RABBIT_CONNECTION_REQUIRED";
	public static final String ENV_PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES = "ENV_PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES";
	public static final String ENV_PJE2_CLOUD_RABBIT_HOST = "ENV_PJE2_CLOUD_RABBIT_HOST";
	public static final String ENV_PJE2_CLOUD_RABBIT_PORT = "ENV_PJE2_CLOUD_RABBIT_PORT";
	public static final String ENV_PJE2_CLOUD_RABBIT_VIRTUALHOST = "ENV_PJE2_CLOUD_RABBIT_VIRTUALHOST"; 
	public static final String ENV_PJE2_CLOUD_RABBIT_USERNAME = "ENV_PJE2_CLOUD_RABBIT_USERNAME";
	public static final String ENV_PJE2_CLOUD_RABBIT_PASSWORD = "ENV_PJE2_CLOUD_RABBIT_PASSWORD"; 
	public static final String ENV_PJE2_CLOUD_RABBIT_EXCHANGENAME = "ENV_PJE2_CLOUD_RABBIT_EXCHANGENAME";
	public static final String ENV_PJE2_CLOUD_RABBIT_DEAD_LETTER_EXCHANGENAME = "ENV_PJE2_CLOUD_RABBIT_DEAD_LETTER_EXCHANGENAME";
	public static final String ENV_PJE2_CLOUD_RABBIT_QUEUENAME = "ENV_PJE2_CLOUD_RABBIT_QUEUENAME";
	public static final String ENV_PJE2_CLOUD_RABBIT_ASYNCHRONOUS_MESSAGES = "ENV_PJE2_CLOUD_RABBIT_ASYNCHRONOUS_MESSAGES";

    public static final String ENV_PJE2_CLOUD_RABBIT_CONTROLE_DE_LOTE = "ENV_PJE2_CLOUD_RABBIT_CONTROLE_DE_LOTE";
    public static final String ENV_PJE2_CLOUD_RABBIT_TAMANHO_PARTICAO_LOTE = "ENV_PJE2_CLOUD_RABBIT_TAMANHO_PARTICAO_LOTE";

    public static final String ENV_PJE2_CLOUD_RABBIT_CONSUMER_MAX_DELIVERY_MESSAGES = "ENV_PJE2_CLOUD_RABBIT_CONSUMER_MAX_DELIVERY_MESSAGES";
	public static final String ENV_PJE2_CLOUD_RABBIT_JOBS_PUBLISHER = "ENV_PJE2_CLOUD_RABBIT_JOBS_PUBLISHER";
	public static final String ENV_PJE2_CLOUD_RABBIT_JOBS_CONSUMER = "ENV_PJE2_CLOUD_RABBIT_JOBS_CONSUMER";
	public static final String ENV_PJE2_CLOUD_RABBIT_JOB_DJE_ENABLED = "ENV_PJE2_CLOUD_RABBIT_JOB_DJE_ENABLED";
	
	public static final String ENV_PJE2_CLOUD_RABBIT_AUTORIDADES_CERTIFICADORAS_CONSUMER = "ENV_PJE2_CLOUD_RABBIT_AUTORIDADES_CERTIFICADORAS_CONSUMER";
	public static final String ENV_PJE_AUTORIDADES_CERTIFICADORAS_UPDATE_ONSTARTUP = "ENV_PJE_UPDATE_AUTORIDADES_CERTIFICADORAS_ONSTARTUP";

	public static final String ENV_SSO_URL = "ENV_SSO_URL";
	public static final String ENV_SSO_AUTHSERVER_URL = "ENV_SSO_AUTHSERVER_URL";
	public static final String ENV_SSO_REALM = "ENV_SSO_REALM";
	public static final String ENV_SSO_CLIENT_ID = "ENV_SSO_CLIENT_ID";
	public static final String ENV_SSO_CLIENT_SECRET = "ENV_SSO_CLIENT_SECRET";
	public static final String ENV_SSO_AUTHENTICATION_ENABLED = "ENV_SSO_AUTHENTICATION_ENABLED";
	public static final String ENV_SSO_AUTHORIZATION_ENABLED = "ENV_SSO_AUTHORIZATION_ENABLED";
	public static final String ENV_SSO_SSL_REQUIRED = "ENV_SSO_SSL_REQUIRED";
	public static final String ENV_SSO_CONFIDENTIAL_PORT = "ENV_SSO_CONFIDENTIAL_PORT";
	public static final String ENV_SSO_FALHA_DESABILITADO_MINUTOS = "ENV_SSO_FALHA_DESABILITA_MINUTOS";

	public static final String ENV_PJE2_CRIMINAL_USERNAME = "ENV_PJE2_CRIMINAL_USERNAME"; 
	public static final String ENV_PJE2_CRIMINAL_PASSWORD = "ENV_PJE2_CRIMINAL_PASSWORD";
	
	public static final String ENV_BNMP_API_CREDENTIALS_USERNAME = "ENV_BNMP_API_CREDENTIALS_USERNAME";
	public static final String ENV_BNMP_API_CREDENTIALS_PASSWORD = "ENV_BNMP_API_CREDENTIALS_PASSWORD";
	public static final String ENV_BNMP_API_CREDENTIALS_CLIENT_ID = "ENV_BNMP_API_CREDENTIALS_CLIENT_ID";
	public static final String ENV_BNMP_API_CREDENTIALS_CODIGO_ORGAO = "ENV_BNMP_API_CREDENTIALS_CODIGO_ORGAO";
	public static final String ENV_BNMP_API_URL = "ENV_BNMP_API_URL";
	public static final String ENV_BNMP_WEB_URL = "ENV_BNMP_WEB_URL";
	public static final String ENV_BNMP_API_CREDENTIALS_TOKEN = "ENV_BNMP_API_CREDENTIALS_TOKEN";

	public static final String ENV_SINAPSES_URL = "ENV_SINAPSES_URL";
	public static final String ENV_SINAPSES_USERNAME = "ENV_SINAPSES_USERNAME";
	public static final String ENV_SINAPSES_PASSWORD = "ENV_SINAPSES_PASSWORD";
	public static final String ENV_LO_ONLINE_URL = "ENV_LO_ONLINE_URL";
	public static final String ENV_WOPI_URL_INTERNO = "ENV_WOPI_URL_INTERNO";
	public static final String ENV_WOPI_URL_EXTERNO = "ENV_WOPI_URL_EXTERNO";
	public static final String ENV_URL_CONVERSOR_PDF = "ENV_URL_CONVERSOR_PDF";
	public static final String ENV_WOPI_ACCESS_HEADER_KEY = "ENV_WOPI_ACCESS_HEADER_KEY";
	public static final String ENV_REPOSITORIO_URL = "ENV_REPOSITORIO_URL";

	public static final String ENV_ELASTIC_INSTANCE_REINDEX = "ENV_ELASTIC_INSTANCE_REINDEX";
	public static final String ENV_ELASTIC_INDEX_NAME = "ENV_ELASTIC_INDEX_NAME";
	public static final String ENV_ELASTIC_URL = "ENV_ELASTIC_URL";
	public static final String ENV_ELASTIC_INDEX_NAME_DEFAULT = "pjeprevencaoprd1g";
	public static final String ELASTICSEARCHIDXNAME = "pje:elasticsearch:index:name";

	public static final String EUREKA_CLIENT_HOSTNAME = ParametroUtil.getParametro("pje2.eureka.client.hostname");
	
	public static final String EUREKA_CLIENT_SECURE_PORT = ParametroUtil.getParametro("pje2.eureka.client.securePort");
	
	public static final String EUREKA_CLIENT_SECURE_PORT_ENABLED = ParametroUtil.getParametro("pje2.eureka.client.securePort.enabled");
	
	public static final String EUREKA_CLIENT_NONSECURE_PORT = ParametroUtil.getParametro("pje2.eureka.client.nonSecurePort");
	
	public static final String EUREKA_CLIENT_NONSECURE_PORT_ENABLED = ParametroUtil.getParametro("pje2.eureka.client.nonSecurePort.enabled");
	
	public static final String PJE2_CLIENTE_URL = ParametroUtil.getParametro("pje2.cliente.urlCliente");
	
	public static final String PJE2_CORS_ENABLED = ParametroUtil.getParametro("pje2.cors.enabled");	

	public static final String PJE2_CLOUD_APP_NAME = ParametroUtil.getParametro("pje2.cloud.appName");
	
	public static final String PJE2_CLOUD_URL_GATEWAY = ParametroUtil.getParametro("pje2.cloud.urlGateway");
	
	public static final String EUREKA_SERVER_URL = ParametroUtil.getParametro("eureka.serviceUrl.default");
	
	public static final String EUREKA_SHOULD_USE_DNS = ParametroUtil.getParametro("eureka.shouldUseDns");
	
	public static final String PJE2_CLOUD_REGISTRAR = ParametroUtil.getParametro("pje2.cloud.registrar");
	
	public static final String PJE2_CLOUD_REGISTRA_COM_IP = ParametroUtil.getParametro("pje2.cloud.registraComIp");
	
	public static final String PJE2_CLOUD_RABBIT_CONNECTION_REQUIRED = ParametroUtil.getParametro("pje2.cloud.rabbit.connection.required");

	public static final String PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES = ParametroUtil.getParametro("pje2.cloud.rabbit.publishMessages");
	
	public static final String PJE2_CLOUD_RABBIT_HOST = ParametroUtil.getParametro("pje2.cloud.rabbit.host");

	public static final String PJE2_CLOUD_RABBIT_ASYNCHRONOUS_MESSAGES = ParametroUtil.getParametro("pje2.cloud.rabbit.asynchronousMessages");
	
	public static final String PJE2_CLOUD_RABBIT_PORT = ParametroUtil.getParametro("pje2.cloud.rabbit.port");
	
	public static final String PJE2_CLOUD_RABBIT_VIRTUALHOST = ParametroUtil.getParametro("pje2.cloud.rabbit.virtualHost");
	
	public static final String PJE2_CLOUD_RABBIT_USERNAME = ParametroUtil.getParametro("pje2.cloud.rabbit.username");
	
	public static final String PJE2_CLOUD_RABBIT_PASSWORD = ParametroUtil.getParametro("pje2.cloud.rabbit.password");	
	
	public static final String PJE2_CLOUD_RABBIT_EXCHANGENAME = ParametroUtil.getParametro("pje2.cloud.rabbit.exchangeName");

	public static final String PJE2_CLOUD_RABBIT_DEAD_LETTER_EXCHANGENAME = ParametroUtil.getParametro("pje2.cloud.rabbit.deadLetterExchangeName");

	public static final String PJE2_CLOUD_RABBIT_QUEUENAME = ParametroUtil.getParametro("pje2.cloud.rabbit.queueName");

	public static final String PJE2_CLOUD_RABBIT_CONTROLE_DE_LOTE = ParametroUtil.getParametro("pje2.cloud.rabbit.controleDeLote");

	public static final String PJE2_CLOUD_RABBIT_TAMANHO_PARTICAO_LOTE = ParametroUtil.getParametro("pje2.cloud.rabbit.tamanhoParticaoLote");

	public static final String PJE2_CLOUD_RABBIT_CONSUMER_MAX_DELIVERY_MESSAGES = ParametroUtil.getParametro("pje2.cloud.rabbit.consumer.maxDeliveryMessages");
	
	public static final String PJE2_CLOUD_RABBIT_JOBS_PUBLISHER = ParametroUtil.getParametro("pje2.cloud.rabbit.jobs.publisher");

	public static final String PJE2_CLOUD_RABBIT_JOBS_CONSUMER = ParametroUtil.getParametro("pje2.cloud.rabbit.jobs.consumer");
	
	public static final String PJE2_CLOUD_RABBIT_JOB_DJE_ENABLED = ParametroUtil.getParametro("pje2.cloud.rabbit.job.dje.enabled");

	public static final String PJE2_CLOUD_RABBIT_ACS_CONSUMER = ParametroUtil.getParametro("pje2.cloud.rabbit.autoridadesCertificadoras.consumer");

	public static final String PJE_ACS_UPDATE_ONSTARTUP = ParametroUtil.getParametro("pje2.autoridadesCertificadoras.update.onstartup.enabled");

	public static final String SSO_AUTHENTICATION_ENABLED = ParametroUtil.getParametro("sso.authentication.enabled");
	
	public static final String SSO_AUTHORIZATION_ENABLED = ParametroUtil.getParametro("sso.authorization.enabled");	
	
	public static final String SSO_URL = ParametroUtil.getParametro("sso.url");	

	public static final String SSO_AUTHSERVER_URL = ParametroUtil.getParametro("sso.authserver.url");
	
	public static final String SSO_REALM = ParametroUtil.getParametro("sso.realm");
	
	public static final String SSO_CLIENT_ID = ParametroUtil.getParametro("sso.client.id");
	
	public static final String SSO_CLIENT_SECRET = ParametroUtil.getParametro("sso.client.secret");

	public static final String SSO_SSL_REQUIRED = ParametroUtil.getParametro("sso.ssl.required");
	
	public static final String SSO_CONFIDENTIAL_PORT = ParametroUtil.getParametro("sso.confidential.port");

	public static final String SSO_FALHA_DESABILITADO_MINUTOS = ParametroUtil.getParametro("sso.falha.desabilitado.minutos");

	public static final String PJE2_CRIMINAL_USERNAME = ParametroUtil.getParametro("pje2.criminal.username");	
	
	public static final String PJE2_CRIMINAL_PASSWORD = ParametroUtil.getParametro("pje2.criminal.password");
	
	public static final String APP_VERSION = ParametroUtil.getParametro("app.version");
	private static final String LO_ONLINE_URL = ParametroUtil.getParametro("pje2.loonline.url");
	private static final String WOPI_URL_INTERNO = ParametroUtil.getParametro("pje2.wopi.url.interno");
	private static final String WOPI_URL_EXTERNO = ParametroUtil.getParametro("pje2.wopi.url.externo");
	private static final String URL_CONVERSOR_PDF = ParametroUtil.getParametro("pje2.conversor.pdf.url");

	public static final String BNMP_API_CREDENTIALS_USERNAME = ParametroUtil.getParametro("bnmp.api.credencials.username");
	
	public static final String BNMP_API_CREDENTIALS_PASSWORD = ParametroUtil.getParametro("bnmp.api.credencials.password");
	
	public static final String BNMP_API_CREDENTIALS_CLIENT_ID = ParametroUtil.getParametro("bnmp.api.credencials.clientId");
	
	public static final String BNMP_API_CREDENTIALS_CODIGO_ORGAO = ParametroUtil.getParametro("bnmp.api.credencials.codigoOrgao");
	
	public static final String BNMP_API_URL = ParametroUtil.getParametro("bnmp.api-url");
	
	public static final String BNMP_WEB_URL = ParametroUtil.getParametro("bnmp.web-url");

	public static final String BNMP_API_TOKEN = ParametroUtil.getParametro("bnmp.api-token");

	public static final String SINAPSES_URL = ParametroUtil.getParametro("sinapses.url");
	
	public static final String SINAPSES_USERNAME = ParametroUtil.getParametro("sinapses.username");

	public static final String PJE2_COOKIE_SECURE = ParametroUtil.getParametro("pje2.cookie.secure");

	public static final String SINAPSES_PASSWORD = ParametroUtil.getParametro("sinapses.password");	

	public static final String PJE2_AUDITORIA_TIPO_PERSISTENCIA = ParametroUtil.getParametro("pje2.auditoria.tipoPersistencia");
	
	public static final String REPOSITORIO_URL = ParametroUtil.getParametro("repositorio.url");

	private ConfiguracaoIntegracaoCloud() {
		super();
	}

	public static final String getAuditoriaTipoPersistencia(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_AUDITORIA_TIPO_PERSISTENCIA)) ? ParametroUtil.getParametro(ENV_PJE2_AUDITORIA_TIPO_PERSISTENCIA) : ConfiguracaoIntegracaoCloud.PJE2_AUDITORIA_TIPO_PERSISTENCIA;
	}	
	
	public static final String getUrlPje2Cliente(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLIENTE_URL)) ? ParametroUtil.getParametro(ENV_PJE2_CLIENTE_URL) : ConfiguracaoIntegracaoCloud.PJE2_CLIENTE_URL;
	}
	
	public static final Boolean isCorsEnabled(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CORS_ENABLED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CORS_ENABLED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CORS_ENABLED);
	}	
	
	public static final String getEurekaClientHostname(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_HOSTNAME)) ? ParametroUtil.getParametro(ENV_EUREKA_CLIENT_HOSTNAME) : ConfiguracaoIntegracaoCloud.EUREKA_CLIENT_HOSTNAME;
	}
	
	public static final String getEurekaClientSecurePort(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_SECURE_PORT)) ? ParametroUtil.getParametro(ENV_EUREKA_CLIENT_SECURE_PORT) : ConfiguracaoIntegracaoCloud.EUREKA_CLIENT_SECURE_PORT;
	}	

	public static final Boolean isEurekaClientSecurePortEnabled(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_SECURE_PORT_ENABLED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_SECURE_PORT_ENABLED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.EUREKA_CLIENT_SECURE_PORT_ENABLED);
	}

	public static final String getEurekaClientNonSecurePort(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_NONSECURE_PORT)) ? ParametroUtil.getParametro(ENV_EUREKA_CLIENT_NONSECURE_PORT) : ConfiguracaoIntegracaoCloud.EUREKA_CLIENT_NONSECURE_PORT;
	}	

	public static final Boolean isEurekaClientNonSecurePortEnabled(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_NONSECURE_PORT_ENABLED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_EUREKA_CLIENT_NONSECURE_PORT_ENABLED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.EUREKA_CLIENT_NONSECURE_PORT_ENABLED);
	}	
	
	public static final String getAppName(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_APP_NAME)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_APP_NAME) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_APP_NAME;
	}	

	public static final String getUrlPje2Gateway(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_URL_GATEWAY)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_URL_GATEWAY) :  ConfiguracaoIntegracaoCloud.PJE2_CLOUD_URL_GATEWAY;
	}
	
	public static final String getUrlEurekaServer(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_SERVER_URL)) ? ParametroUtil.getParametro(ENV_EUREKA_SERVER_URL) : ConfiguracaoIntegracaoCloud.EUREKA_SERVER_URL;
	}
	
	public static final Boolean isRegistrarServiceDiscovery() {
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_REGISTRAR)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_REGISTRAR)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_REGISTRAR);
	}

	public static final Boolean isEurekaShouldUseDns() {
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_EUREKA_SHOULD_USE_DNS)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_EUREKA_SHOULD_USE_DNS)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.EUREKA_SHOULD_USE_DNS);
	}
	
	public static final Boolean isRegistraComIp(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_REGISTRA_COM_IP)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_REGISTRA_COM_IP)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_REGISTRA_COM_IP);
	}
	
	public static final Boolean isRabbitConnectionRequired(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_CONNECTION_REQUIRED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_CONNECTION_REQUIRED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_CONNECTION_REQUIRED);
	}

	public static final Boolean isPublishMessages(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_PUBLISH_MESSAGES);
	}
	
	public static final Boolean isRabbitAsynchronousMessages(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_ASYNCHRONOUS_MESSAGES)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_ASYNCHRONOUS_MESSAGES)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_ASYNCHRONOUS_MESSAGES);
	}
	
	public static final String getRabbitHost(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_HOST)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_HOST) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_HOST;
	}
	
	public static final Integer getRabbitPort(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_PORT)) ? Integer.parseInt(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_PORT)) : Integer.parseInt(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_PORT);
	}
	
	public static final String getRabbitVirtualHost(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_VIRTUALHOST)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_VIRTUALHOST) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_VIRTUALHOST;
	}
	
	public static final String getRabbitUsername(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_USERNAME)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_USERNAME) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_USERNAME;
	}		
	
	public static final String getRabbitPassword(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_PASSWORD)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_PASSWORD) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_PASSWORD;
	}

	public static final String getRabbitQueueName(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_QUEUENAME)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_QUEUENAME) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_QUEUENAME;
	}

	public static final Integer getRabbitTamanhoParticaoLote(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_TAMANHO_PARTICAO_LOTE)) ? Integer.parseInt(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_TAMANHO_PARTICAO_LOTE)) : Integer.parseInt(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_TAMANHO_PARTICAO_LOTE);
	}

	/**
	 * Identifica a quantidade de mensagens consumidas por vez pelo consumer channel do rabbit
	 */
	public static final Integer getRabbitConsumerMaxDeliveryMessages(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_CONSUMER_MAX_DELIVERY_MESSAGES)) ? Integer.parseInt(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_CONSUMER_MAX_DELIVERY_MESSAGES)) : Integer.parseInt(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_CONSUMER_MAX_DELIVERY_MESSAGES);
	}

	public static final String getRabbitExchangeName(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_EXCHANGENAME)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_EXCHANGENAME) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_EXCHANGENAME;
	}

	/**
	 * Exchange destino das mensagens em que não foi possível consumir
	 * @return
	 */
	public static final String getRabbitDeadLetterExchangeName(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_DEAD_LETTER_EXCHANGENAME)) ? ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_DEAD_LETTER_EXCHANGENAME) : ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_DEAD_LETTER_EXCHANGENAME;
	}

	/**
	 * Identifica se a instancia atual sera uma publisher de JOB distribuido
	 * - deve haver apenas 1 instância como publisher executando ao mesmo tempo
	 */
	public static final boolean isRabbitJobsPublisher() { 
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_JOBS_PUBLISHER)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_JOBS_PUBLISHER)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_JOBS_PUBLISHER);
	}
	
	/**
	 * Identifica se a instancia atual sera uma consumer de JOB distribuido
	 */
	public static final boolean isRabbitJobsConsumer() { 
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_JOBS_CONSUMER)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_JOBS_CONSUMER)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_JOBS_CONSUMER);
	}

	public static final boolean isRabbitJobDJEEnabled() { 
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_JOB_DJE_ENABLED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_JOB_DJE_ENABLED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_JOB_DJE_ENABLED);
	}
	
	/**
	 * Identifica se a instancia atual sera uma consumer das alteracoes de autoridades certificadoras (ACs)
	 */
	public static final boolean isRabbitAutoridadesCertificadorasConsumer() { 
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_AUTORIDADES_CERTIFICADORAS_CONSUMER)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE2_CLOUD_RABBIT_AUTORIDADES_CERTIFICADORAS_CONSUMER)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE2_CLOUD_RABBIT_ACS_CONSUMER);
	}

	/**
	 * Identifica se a instancia atual buscará por atualizações de autoridades-certificadoras no startup da aplicação
	 */
	public static final boolean isUpdateAutoridadesCertificadorasOnStartup() { 
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_PJE_AUTORIDADES_CERTIFICADORAS_UPDATE_ONSTARTUP)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_PJE_AUTORIDADES_CERTIFICADORAS_UPDATE_ONSTARTUP)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.PJE_ACS_UPDATE_ONSTARTUP);
	}

	public static boolean getSSOAuthenticationEnabled(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_AUTHENTICATION_ENABLED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_SSO_AUTHENTICATION_ENABLED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.SSO_AUTHENTICATION_ENABLED);
	}
	
	public static boolean getSSOAuthorizationEnabled(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_AUTHORIZATION_ENABLED)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_SSO_AUTHORIZATION_ENABLED)) : Boolean.valueOf(ConfiguracaoIntegracaoCloud.SSO_AUTHORIZATION_ENABLED);
	}	
	
	public static final String getSSOUrl(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_URL)) ? ParametroUtil.getParametro(ENV_SSO_URL) : ConfiguracaoIntegracaoCloud.SSO_URL;
	}
	
	public static final String getSSOAuthServerUrl(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_AUTHSERVER_URL)) ? ParametroUtil.getParametro(ENV_SSO_AUTHSERVER_URL) : ConfiguracaoIntegracaoCloud.SSO_AUTHSERVER_URL;
	}
	
	public static final String getSSORealm(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_REALM)) ? ParametroUtil.getParametro(ENV_SSO_REALM) : ConfiguracaoIntegracaoCloud.SSO_REALM;
	}
	
	public static final String getSSOClientId(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_CLIENT_ID)) ? ParametroUtil.getParametro(ENV_SSO_CLIENT_ID) : ConfiguracaoIntegracaoCloud.SSO_CLIENT_ID;
	}	
	
	public static final String getSSOClientSecret(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_CLIENT_SECRET)) ? ParametroUtil.getParametro(ENV_SSO_CLIENT_SECRET) : ConfiguracaoIntegracaoCloud.SSO_CLIENT_SECRET;
	}	
	
	public static final String getSSOSslRequired(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_SSL_REQUIRED)) ? ParametroUtil.getParametro(ENV_SSO_SSL_REQUIRED) : ConfiguracaoIntegracaoCloud.SSO_SSL_REQUIRED;
	}
	
	public static final Integer getSSOConfidentialPort(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_CONFIDENTIAL_PORT)) ? Integer.valueOf(ParametroUtil.getParametro(ENV_SSO_CONFIDENTIAL_PORT)) : Integer.valueOf(ConfiguracaoIntegracaoCloud.SSO_CONFIDENTIAL_PORT);
	}

	public static final Integer getSSOFalhaDesabilitadoMinutos(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SSO_FALHA_DESABILITADO_MINUTOS)) ? Integer.valueOf(ParametroUtil.getParametro(ENV_SSO_FALHA_DESABILITADO_MINUTOS)) : Integer.valueOf(ConfiguracaoIntegracaoCloud.SSO_FALHA_DESABILITADO_MINUTOS);
	}

	public static final String getAppVersion() {
		return ConfiguracaoIntegracaoCloud.APP_VERSION;
	}
	
	public static final String getBnmpUserName(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_USERNAME)) ? ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_USERNAME) : ConfiguracaoIntegracaoCloud.BNMP_API_CREDENTIALS_USERNAME;
	}
	
	public static final String getBnmpPassword(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_PASSWORD)) ? ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_PASSWORD) : ConfiguracaoIntegracaoCloud.BNMP_API_CREDENTIALS_PASSWORD;
	}
	
	public static final String getBnmpClientId(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_CLIENT_ID)) ? ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_CLIENT_ID) : ConfiguracaoIntegracaoCloud.BNMP_API_CREDENTIALS_CLIENT_ID;
	}
	
	public static final String getBnmpCodigoOrgao(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_CODIGO_ORGAO)) ? ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_CODIGO_ORGAO) : ConfiguracaoIntegracaoCloud.BNMP_API_CREDENTIALS_CODIGO_ORGAO;
	}
	
	public static final String getBnmpApiUrl(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_API_URL)) ? ParametroUtil.getParametro(ENV_BNMP_API_URL) : ConfiguracaoIntegracaoCloud.BNMP_API_URL;
	}
	
	public static final String getBnmpWebUrl(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_WEB_URL)) ? ParametroUtil.getParametro(ENV_BNMP_WEB_URL) : ConfiguracaoIntegracaoCloud.BNMP_WEB_URL;
	}
	
	public static final String getBnmpApiToken(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_TOKEN)) ? ParametroUtil.getParametro(ENV_BNMP_API_CREDENTIALS_TOKEN) : ConfiguracaoIntegracaoCloud.BNMP_API_TOKEN;
	}
	
	
	public static final String getSinapsesUrl(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SINAPSES_URL)) ? ParametroUtil.getParametro(ENV_SINAPSES_URL) : ConfiguracaoIntegracaoCloud.SINAPSES_URL;
	}
	
	public static final String getSinapsesUsername(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SINAPSES_USERNAME)) ? ParametroUtil.getParametro(ENV_SINAPSES_USERNAME) : ConfiguracaoIntegracaoCloud.SINAPSES_USERNAME;
	}
	
	public static final String getSinapsesPassword(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_SINAPSES_PASSWORD)) ? ParametroUtil.getParametro(ENV_SINAPSES_PASSWORD) : ConfiguracaoIntegracaoCloud.SINAPSES_PASSWORD;
	}

	public static final String getLOOnlineUrl(){
		return !StringUtil.isEmpty(System.getenv(ENV_LO_ONLINE_URL)) ? System.getenv(ENV_LO_ONLINE_URL) : ConfiguracaoIntegracaoCloud.LO_ONLINE_URL;
	}

	public static final String getWopiUrlInterno(){
		return !StringUtil.isEmpty(System.getenv(ENV_WOPI_URL_INTERNO)) ? System.getenv(ENV_WOPI_URL_INTERNO) : ConfiguracaoIntegracaoCloud.WOPI_URL_INTERNO;
	}

	public static final String getWopiUrlExterno(){
		return !StringUtil.isEmpty(System.getenv(ENV_WOPI_URL_EXTERNO)) ? System.getenv(ENV_WOPI_URL_EXTERNO) : ConfiguracaoIntegracaoCloud.WOPI_URL_EXTERNO;
	}

	public static final String getUrlConversorPDF(){
		return !StringUtil.isEmpty(System.getenv(ENV_URL_CONVERSOR_PDF)) ? System.getenv(ENV_URL_CONVERSOR_PDF) : ConfiguracaoIntegracaoCloud.URL_CONVERSOR_PDF;
	}

	public static String getWopiAccessHeaderKey() {
		return !StringUtil.isEmpty(System.getenv(ENV_WOPI_ACCESS_HEADER_KEY)) ? System.getenv(ENV_WOPI_ACCESS_HEADER_KEY) : ConfiguracaoIntegracaoCloud.ENV_WOPI_ACCESS_HEADER_KEY;
	}

	public static final Boolean isElasticInstanceReindex(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_ELASTIC_INSTANCE_REINDEX)) ? Boolean.valueOf(ParametroUtil.getParametro(ENV_ELASTIC_INSTANCE_REINDEX)) : Boolean.FALSE;
	}

	public static String getElasticIndexName() {
		return !StringUtil.isEmpty(System.getenv(ENV_ELASTIC_INDEX_NAME)) ? System.getenv(ENV_ELASTIC_INDEX_NAME) : ENV_ELASTIC_INDEX_NAME_DEFAULT;
	}

	public static String getElasticUrl() {
		return !StringUtil.isEmpty(System.getenv(ENV_ELASTIC_URL)) ? System.getenv(ENV_ELASTIC_URL) : "";
	}



    public static boolean isCookieSecure() {
        if(PJE2_COOKIE_SECURE != null) {
            return Boolean.valueOf(PJE2_COOKIE_SECURE);
        }
        return false;
    }


	public static final String getRepositorioUrl(){
		return !StringUtil.isEmpty(ParametroUtil.getParametro(ENV_REPOSITORIO_URL)) ? ParametroUtil.getParametro(ENV_REPOSITORIO_URL) : ConfiguracaoIntegracaoCloud.REPOSITORIO_URL;
	}

}
