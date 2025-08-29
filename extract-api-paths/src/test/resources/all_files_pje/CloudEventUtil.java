package br.com.infox.cliente.util;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;

public class CloudEventUtil {
	
	/***
	 * Builds a routing key in this format:
	 * <numeroOrgaoJustica>.<grau>.<appName>.<suffix>
	 * examplo: 200.4.pje-cnj.CienciaAutomaticaCloudEvent.POST
	 * 
	 * @param routingKeySuffix
	 * @return
	 */
	public static String generateRoutingKeyFromSuffix(String routingKeySuffix) {
		return generateCustomRoutingKeyFromSuffix(routingKeySuffix, true, true, true);
	}
	
	/***
	 * Builds a routing key in this format:
	 * <numeroOrgaoJustica>.<grau>.<appName>.<suffix>
	 * examplo: 200.4.pje-cnj.CienciaAutomaticaCloudEvent.POST
	 * 
	 * @param routingKeySuffix
	 * @return
	 */
	public static String generateCustomRoutingKeyFromSuffix(String routingKeySuffix, Boolean restrictOrgaoJustica, Boolean restrictGrau, Boolean restrictAppName) {
		if(restrictOrgaoJustica == null) {
			restrictOrgaoJustica = true;
		}
		if(restrictGrau == null) {
			restrictGrau = true;
		}
		
		if(restrictAppName == null) {
			restrictAppName = true;
		}
		
		String orgaoJustica = restrictOrgaoJustica ? ParametroUtil.instance().recuperarNumeroOrgaoJustica() : "*";
		String grau = restrictGrau ? ParametroUtil.instance().getCodigoInstanciaAtual().substring(0, 1) : "*";
		String appName = restrictAppName ? ConfiguracaoIntegracaoCloud.getAppName() : "*";
		String routingKeyPrefix = orgaoJustica + "." + grau + "." + appName;
		
		String routingKey = routingKeyPrefix;
		if(routingKeySuffix != null && routingKeySuffix.length() > 0) {
			routingKey += "." + routingKeySuffix;
		}
		return routingKey;

	}
	
	public static String generateQueueNameFromSuffix(String queueNameSuffix) {
		return generateQueueName(ConfiguracaoIntegracaoCloud.getAppName(), queueNameSuffix);
	}
	
	public static String generateInstanceOnlyQueueNameFromSuffix(String queueNameSuffix) {
		return generateQueueName(generateInstanceId(), queueNameSuffix);
	}
	
	private static String generateQueueName(String queueNamePrefix, String queueNameSuffix) {
		String queueName = queueNamePrefix;
		if(queueNameSuffix != null && queueNameSuffix.length() > 0) {
			queueName += "." + queueNameSuffix;
		}
		return queueName;	
	}
	
	public static String generateInstanceId() {
		return System.getProperty("jboss.server.name")+ ":" + ConfiguracaoIntegracaoCloud.getAppName();
	}
}
