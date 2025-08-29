package br.jus.pje.nucleo.dto.sinapses;

import java.io.Serializable;

public class MovimentacaoSugeridaRequest implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String tipo;
	private String conteudo;
	
	public MovimentacaoSugeridaRequest(String tipo, String conteudo) {
		super();
		this.tipo = tipo;
		this.conteudo = conteudo;
	}

	public MovimentacaoSugeridaRequest() {
		super();
		// TODO Auto-generated constructor stub
	}	
	
	public String getTipo() {
		return tipo;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public String getConteudo() {
		return conteudo;
	}
	
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conteudo == null) ? 0 : conteudo.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MovimentacaoSugeridaRequest other = (MovimentacaoSugeridaRequest) obj;
		if (conteudo == null) {
			if (other.conteudo != null)
				return false;
		} else if (!conteudo.equals(other.conteudo))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}
	
}
