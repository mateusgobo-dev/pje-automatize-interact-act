package br.jus.cnj.pje.amqp.model.dto;


import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.util.JSONUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.cnj.pje.pjecommons.model.amqp.RoutingKey;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.Crypto.SignatureAlgorithm;
import br.jus.pje.nucleo.util.Crypto.Type;
import br.jus.pje.nucleo.util.StringUtil;

@SuppressWarnings("rawtypes")
public class CloudEventBuilder {
	
	private Object entity;
	private CloudEventVerbEnum event;
	private Class clazz;
	private Object payload;
	private String entityName;
	private Crypto crypto;
	
	public CloudEventBuilder() {
		super();
		crypto = new Crypto(ProjetoUtil.getChaveCriptografica());
	}
	
	public CloudEventBuilder ofPayloadType(Class type) {
		this.clazz = type;
		return this;
	}	
	
	public CloudEventBuilder withEntity(Object entity) {
		this.entity = entity;
		return this;
	}
	
	public CloudEventBuilder withPayload(Object payload) {
		this.payload = payload;
		return this;
	} 
	
public CloudEventBuilder withEvent(CloudEventVerbEnum event) {
		this.event = event;
		return this;
	}
	
	public CloudEventBuilder withEntityName(String entityName) {
		this.entityName = entityName;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public CloudEvent build() throws JsonProcessingException, PJeBusinessException {
		if((payload != null || entity != null) && event != null && clazz != null) {
			CloudEvent cloudEvent = new CloudEvent();
			try {
				if(entity != null) {
					CloudEventPayload cePayload = this.getInstance();
					cloudEvent.setPayload(cePayload.convertEntityToPayload(this.entity));					
				} else if(payload != null) {
					cloudEvent.setPayload(payload);
				}
				cloudEvent.setUuid(UUID.randomUUID().toString());
				cloudEvent.setAppName(ConfiguracaoIntegracaoCloud.getAppName());
				cloudEvent.setAppVersion(ConfiguracaoIntegracaoCloud.getAppVersion());
				cloudEvent.setTimestamp(new Date().getTime());
				cloudEvent.setPayloadHash(Crypto.encodeMD5(this.getMapper().writeValueAsBytes(cloudEvent.getPayload())));
				cloudEvent.setHashAlgorithm(Type.MD5.toString());
				cloudEvent.setPayloadHashSigned(crypto.encodeDES(cloudEvent.getPayloadHash()));
				cloudEvent.setSignatureAlgorithm(SignatureAlgorithm.DES.getCodigo());
				cloudEvent.setRoutingKey(this.getRoutingKey().getRoutingKeyValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return cloudEvent;
		} else {
			throw new PJeBusinessException("Não há argumentos suficiente para montar o CloudEvent");
		}
	}
	
	public static CloudEventBuilder instance() {
		return new CloudEventBuilder();
	}
	
	private CloudEventPayload getInstance() throws Exception {
		return (CloudEventPayload) Class.forName(clazz.getName()).getConstructor().newInstance();
	}
	
	private ObjectMapper getMapper() {
		return JSONUtil.novoObjectMapper();
	}
	
	public RoutingKey getRoutingKey() {
		String orgaoJustica = ParametroUtil.instance().recuperarNumeroOrgaoJustica();
		String grau = ParametroUtil.instance().getCodigoInstanciaAtual().substring(0, 1);
		String appName = ConfiguracaoIntegracaoCloud.getAppName();
		String entityName = StringUtil.isEmpty(this.entityName) ? entity.getClass().getSimpleName() : this.entityName;
		
		return new RoutingKey(appName, orgaoJustica, grau, entityName, event);
	}
}
