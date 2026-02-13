package br.jus.cnj.pje.vo;

import br.jus.pje.search.Operator;

public class VariavelSinalizacaoFluxoVO {

	private String nomeVariavel;
	private Object valorEsperado;
	private Operator operador;
	private boolean limitarTarefa;
	private boolean apagarVariavel;
	
	public VariavelSinalizacaoFluxoVO(String nomeVariavel, Object valorEsperado, Operator operador,
			boolean limitarTarefa, boolean apagarVariavel) {
		super();
		this.nomeVariavel = nomeVariavel != null ? nomeVariavel.trim() : nomeVariavel;
		this.valorEsperado = valorEsperado;
		this.operador = operador != null ? operador : Operator.equals;
		this.limitarTarefa = limitarTarefa;
		this.apagarVariavel = apagarVariavel;
	}

	public String getNomeVariavel() {
		return nomeVariavel;
	}

	public Object getValorEsperado() {
		return valorEsperado;
	}

	public Operator getOperador() {
		return operador;
	}

	public boolean isLimitarTarefa() {
		return limitarTarefa;
	}

	public boolean isApagarVariavel() {
		return apagarVariavel;
	}
	
	public boolean isParametrosValidos() {
		return getNomeVariavel() != null && !getNomeVariavel().isEmpty() && 
				getValorEsperado() != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (limitarTarefa ? 1231 : 1237);
		result = prime * result + ((nomeVariavel == null) ? 0 : nomeVariavel.hashCode());
		result = prime * result + ((operador == null) ? 0 : operador.hashCode());
		result = prime * result + ((valorEsperado == null) ? 0 : valorEsperado.hashCode());
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
		VariavelSinalizacaoFluxoVO other = (VariavelSinalizacaoFluxoVO) obj;
		if (limitarTarefa != other.limitarTarefa)
			return false;
		if (nomeVariavel == null) {
			if (other.nomeVariavel != null)
				return false;
		} else if (!nomeVariavel.equals(other.nomeVariavel))
			return false;
		if (operador != other.operador)
			return false;
		if (valorEsperado == null) {
			if (other.valorEsperado != null)
				return false;
		} else if (!valorEsperado.equals(other.valorEsperado))
			return false;
		return true;
	}
	
}
