/**
 * EnumTipoValorCda.java.
 */
package br.jus.pje.nucleo.enums;

/**
 * 
 * @author Hibernatetools
 */
public enum EnumTipoValorCda implements PJeEnum {
	O("Originário"),
	A("Atualização"),
	;
	
	private String	label;
	
	/**
	 * Construtor.
	 * 
	 * @param label
	 */
	private EnumTipoValorCda(String label) {
		setLabel(label);
	}
	
	/**
	 * Retorna e enum do código passado por parametro.
	 * 
	 * @param codigo Codigo do enum. 
	 * @return Enum do código passado por parametro.
	 */
	public static EnumTipoValorCda get(String codigo) {
		EnumTipoValorCda resultado = null;
		
		EnumTipoValorCda[] enuns = values();
		for (int indice = 0; indice < enuns.length && resultado == null; indice++) {
			EnumTipoValorCda temp = enuns[indice];
			if (temp.equals(codigo)) {
				resultado = temp;
			}
		}

		return resultado;
	}
    
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @param label Atribui label.
	 */
	private void setLabel(String label) {
		this.label = label;
	}
}
