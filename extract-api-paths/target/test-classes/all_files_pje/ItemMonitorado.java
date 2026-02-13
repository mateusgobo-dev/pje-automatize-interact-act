package br.jus.cnj.pje.monitoramento;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "statusMonitoracao")
public class ItemMonitorado implements Serializable {

	private static final long	serialVersionUID	= 1L;

	private String				nome;

	private String				mensagem;

	private Boolean				status;

	public ItemMonitorado() {
		super();
	}

	public ItemMonitorado(String nome, String mensagem, Boolean status) {
		super();
		this.nome = nome;
		this.mensagem = mensagem;
		this.status = status;
	}

	@XmlElement(name = "nome")
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@XmlElement(name = "mensagem")
	public String getMensagem() {
		return this.mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public Boolean getStatus() {
		return this.status;
	}

	@XmlElement(name = "status")
	public void setStatus(Boolean status) {
		this.status = status;
	}

}
