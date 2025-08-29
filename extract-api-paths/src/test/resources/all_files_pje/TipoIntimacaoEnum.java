package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto.enums;

import br.jus.pje.nucleo.enums.PJeEnum;

/**
 * Enum TipoIntimacaoEnum.
 * 
 * @author Adriano Pamplona
 **/
public enum TipoIntimacaoEnum implements PJeEnum {
	LIMINAR(1, "Liminar"),
	OBRIGACAO_DE_FAZER(2, "Obrigação de fazer"),
	SENTENCA(3, "Sentença"),
	ACORDAO(4, "Acordão"),
	TRANSITO_EM_JULGADO(5, "Trânsito em julgado"),
	OFICIO(6, "Ofício"),
	OUTRO(7, "Outro");
	
	private Integer codigo;
	private String descricao;

	TipoIntimacaoEnum(Integer codigo, String descricao) {
		this.codigo = codigo;
		this.descricao = descricao;
	}

	public Integer getCodigo() {
		return this.codigo;
	}
	
	@Override
	public String getLabel() {
		return this.descricao;
	}

	/**
	 * Retorna e enum do código passado por parametro.
	 * 
	 * @param codigo Codigo do enum. 
	 * @return Enum do código passado por parametro.
	 */
	public static TipoIntimacaoEnum get(Integer codigo) {
		TipoIntimacaoEnum resultado = null;
		
		TipoIntimacaoEnum[] enuns = values();
		for (int indice = 0; indice < enuns.length && resultado == null; indice++) {
			TipoIntimacaoEnum temp = enuns[indice];
			if (temp.codigo.equals(codigo)) {
				resultado = temp;
			}
		}

		return resultado;
	}
}
