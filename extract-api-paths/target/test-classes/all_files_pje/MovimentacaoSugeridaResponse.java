package br.jus.pje.nucleo.dto.sinapses;

import java.io.Serializable;
import java.util.List;

public class MovimentacaoSugeridaResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private Classificacao classeConvicto;
	private List<MovimentacaoSugerida> resultados;

	public MovimentacaoSugeridaResponse() {
		super();
	}

	public MovimentacaoSugeridaResponse(Classificacao classeConvicto, List<MovimentacaoSugerida> resultados) {
		super();
		this.classeConvicto = classeConvicto;
		this.resultados = resultados;
	}

	public Classificacao getClasseConvicto() {
		return classeConvicto;
	}

	public void setClasseConvicto(Classificacao classeConvicto) {
		this.classeConvicto = classeConvicto;
	}

	public List<MovimentacaoSugerida> getResultados() {
		return resultados;
	}

	public void setResultados(List<MovimentacaoSugerida> resultados) {
		this.resultados = resultados;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classeConvicto == null) ? 0 : classeConvicto.hashCode());
		result = prime * result + ((resultados == null) ? 0 : resultados.hashCode());
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
		MovimentacaoSugeridaResponse other = (MovimentacaoSugeridaResponse) obj;
		if (classeConvicto == null) {
			if (other.classeConvicto != null)
				return false;
		} else if (!classeConvicto.equals(other.classeConvicto))
			return false;
		if (resultados == null) {
			if (other.resultados != null)
				return false;
		} else if (!resultados.equals(other.resultados))
			return false;
		return true;
	}

}
