/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.enums;

public enum TipoSituacaoPautaEnum {

	/**
	 * O processo está em discussão no momento. 
	 */
	EJ("Em julgamento"),
	
	/**
	 * O processo está aguardando o julgamento 
	 */
	AJ("Aguardando julgamento"),
	
	/**
	 * O processo já foi julgado 
	 */
	JG("Julgado"), 
	
	/**
	 * O processo não está julgado. 
	 */
	NJ("Não julgado"),
	
	/**
	 * Cancelamento da inclusão em pauta com pauta fechada 
	 */
	CP("Inclusão em pauta cancelada");


	private String label;

	TipoSituacaoPautaEnum(String label) {
		this.label = label;
	}

	/**
	 * Método para verificar se a String descricaoSituacao está contida no Enum
	 * retornando true caso esteja contida.
	 * 
	 * @param string
	 *            descricaoAdiamento do enum ou label que será verificado.
	 * @return boolean
	 */
	public static boolean isComtem(String descricaoSituacao) {
		for (TipoSituacaoPautaEnum item : values()) {
			if (item.name().equals(descricaoSituacao)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Retorna o AdiadoVistaEnum pelo valor passado por descrição da situação da
	 * Sessão. Se não pertençe ao domínio retorna null.
	 * 
	 * @return TipoSituacaoPautaEnum
	 */

	public static TipoSituacaoPautaEnum getEnum(String descricaoAdiamento) {
		for (TipoSituacaoPautaEnum item : values()) {
			if (item.name().equals(descricaoAdiamento)) {
				return item;
			}
		}
		return null;
	}
	
	
	public String getLabel() {
		return this.label;
	}

	public static boolean verificarSituacaoJulgado(TipoSituacaoPautaEnum situacao) {
		return situacao == TipoSituacaoPautaEnum.NJ || situacao == TipoSituacaoPautaEnum.JG;
	}
}
