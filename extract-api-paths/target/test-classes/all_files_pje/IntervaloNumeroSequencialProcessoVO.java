package br.jus.cnj.pje.entidades.vo;


public class IntervaloNumeroSequencialProcessoVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	private int tamanhoString;
	private int sequenciaInicial;
	private int sequenciaFinal;
	private int TAMANHO_MAXIMO_NUMERO_SEQUENCIAL = 7;
	
	public IntervaloNumeroSequencialProcessoVO(int tamanhoString, int sequenciaInicial, int sequenciaFinal) {
		super();
		this.tamanhoString = tamanhoString;
		this.sequenciaInicial = sequenciaInicial;
		this.sequenciaFinal = sequenciaFinal;
	}
	public int getTamanhoString() {
		return tamanhoString;
	}
	public void setTamanhoString(int tamanhoString) {
		this.tamanhoString = tamanhoString;
	}
	public int getSequenciaInicial() {
		return sequenciaInicial;
	}
	public void setSequenciaInicial(int sequenciaInicial) {
		this.sequenciaInicial = sequenciaInicial;
	}
	public int getSequenciaFinal() {
		return sequenciaFinal;
	}
	public void setSequenciaFinal(int sequenciaFinal) {
		this.sequenciaFinal = sequenciaFinal;
	}
	public Boolean getIntervaloValido() {
		return (tamanhoString <= TAMANHO_MAXIMO_NUMERO_SEQUENCIAL && String.valueOf(sequenciaInicial).length() == String.valueOf(sequenciaFinal).length());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sequenciaFinal;
		result = prime * result + sequenciaInicial;
		result = prime * result + tamanhoString;
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
		IntervaloNumeroSequencialProcessoVO other = (IntervaloNumeroSequencialProcessoVO) obj;
		if (sequenciaFinal != other.sequenciaFinal)
			return false;
		if (sequenciaInicial != other.sequenciaInicial)
			return false;
		if (tamanhoString != other.tamanhoString)
			return false;
		return true;
	}
}
