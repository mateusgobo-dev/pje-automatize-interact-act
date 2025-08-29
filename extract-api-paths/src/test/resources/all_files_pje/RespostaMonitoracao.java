package br.jus.cnj.pje.monitoramento;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "statusMonitoracao")
public class RespostaMonitoracao implements Serializable {

	private static final long		serialVersionUID	= 1L;

	private Boolean					status;

	private String					mensagem;

	private long					tempo;

	private List<ItemMonitorado>	items;

	public RespostaMonitoracao() {
		super();
		this.items = new ArrayList<ItemMonitorado>();
	}

	@XmlElement(name = "status")
	public Boolean getStatus() {
		return this.status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	@XmlElement(name = "mensagem")
	public String getMensagem() {
		return this.mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	@XmlElement(name = "tempo")
	public long getTempo() {
		return this.tempo;
	}

	public void setTempo(long tempo) {
		this.tempo = tempo;
	}

	@XmlElement(name = "item")
	@XmlElementWrapper(name = "items")
	public List<ItemMonitorado> getItems() {
		return this.items;
	}

	public void setItems(final List<ItemMonitorado> items) {
		this.items = items;
	}

}
