package br.jus.cnj.pje.visao.beans;

import java.io.Serializable;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;

public class ObjectObserverBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private CloudEventVerbEnum verb;
	private transient Object obj;
	
	public ObjectObserverBean(Object obj) {
		super();
		this.obj = obj;
		this.verb = CloudEventVerbEnum.POST;
	}
	
	public ObjectObserverBean(CloudEventVerbEnum verb, Object obj) {
		super();
		this.verb = verb;
		this.obj = obj;
	}
	
	public CloudEventVerbEnum getVerb() {
		return verb;
	}
	
	public void setVerb(CloudEventVerbEnum verb) {
		this.verb = verb;
	}
	
	public Object getObj() {
		return obj;
	}
	
	public void setObj(Object obj) {
		this.obj = obj;
	}
	
}
