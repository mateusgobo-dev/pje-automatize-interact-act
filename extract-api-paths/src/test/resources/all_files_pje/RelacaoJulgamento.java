package br.com.infox.cliente;

import java.io.Serializable;

public class RelacaoJulgamento implements Serializable{

	private static final long serialVersionUID = 1L;

	private String descricao;
	private String link;
	private String url;

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	public void setLink(String link){
		this.link = link;
	}

	public String getLink(){
		return link;
	}

	public void setDescricao(String descricao){
		this.descricao = descricao;
	}

	public String getDescricao(){
		return descricao;
	}
}
