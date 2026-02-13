/**
 * EnumTipoDevedor.java.
 */
package br.jus.pje.nucleo.enums;

/**
 * 
 * @author Hibernatetools
 */
public enum EnumTipoDevedor implements PJeEnum {
	P("Principal"),
	C("Correspondente"),
	S("Solidário"),
	;
	
	private String	label;
	
	/**
	 * Construtor.
	 * 
	 * @param codigo
	 * @param label
	 */
	private EnumTipoDevedor(String label) {
		setLabel(label);
	}
	
	/**
	 * Retorna e enum do código passado por parametro.
	 * 
	 * @param codigo Codigo do enum. 
	 * @return Enum do código passado por parametro.
	 */
	public static EnumTipoDevedor get(String codigo) {
		EnumTipoDevedor resultado = null;
		
		EnumTipoDevedor[] enuns = values();
		for (int indice = 0; indice < enuns.length && resultado == null; indice++) {
			EnumTipoDevedor temp = enuns[indice];
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
	 * @param label Atribui codigo.
	 */
	private void setLabel(String label) {
		this.label = label;
	}
}
