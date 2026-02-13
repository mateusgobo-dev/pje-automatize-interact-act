package br.jus.pje.nucleo.enums;

public enum JulgamentoEnum {

	O("de questão de ordem"), M("de mérito"), P("de preliminar");

	private String label;

	JulgamentoEnum(String label) {
		this.label = label;
	}
	
	/**
	 * Retorna o JulgamentoEnum pelo valor passado por descrição.
	 * Se não pertençe ao domínio retorna null.
	 * 
	 * @param String - descricao do enum ou label.
	 * @return JulgamentoEnum
	 */

	public static JulgamentoEnum getEnum(String descricao) {
		JulgamentoEnum retorno = null;
		for (JulgamentoEnum item : values()) {
			if (item.name().equals(descricao)) {
				retorno = item;
			}
		}
		return retorno;
	}

	public String getLabel() {
		return this.label;
	}

}