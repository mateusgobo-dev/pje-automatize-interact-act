package br.jus.pje.nucleo.dto.sinapses;

import java.io.Serializable;

public class MovimentacaoSugerida implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Classificacao classe;
	private Double conviccao;
	
	public MovimentacaoSugerida(Classificacao classe, Double conviccao) {
		super();
		this.classe = classe;
		this.conviccao = conviccao;
	}

	public MovimentacaoSugerida() {
		super();
	}

	public Classificacao getClasse() {
		return classe;
	}

	public void setClasse(Classificacao classe) {
		this.classe = classe;
	}

	public Double getConviccao() {
		return conviccao;
	}

	public void setConviccao(Double conviccao) {
		this.conviccao = conviccao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classe == null) ? 0 : classe.hashCode());
		result = prime * result + ((conviccao == null) ? 0 : conviccao.hashCode());
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
		MovimentacaoSugerida other = (MovimentacaoSugerida) obj;
		if (classe == null) {
			if (other.classe != null)
				return false;
		} else if (!classe.equals(other.classe))
			return false;
		if (conviccao == null) {
			if (other.conviccao != null)
				return false;
		} else if (!conviccao.equals(other.conviccao))
			return false;
		return true;
	}
	
}
