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

public enum TipoSituacaoPautaBlocoEnum {

	/**
	 * Os processos do bloco estão em discussão no momento 
	 */
	EJ("Em julgamento"),
	
	/**
	 * Os processos do bloco estão aguardando julgamento 
	 */
	AJ("Aguardando julgamento"),
	
	/**
	 * Os processos do bloco foram julgados 
	 */
	JG("Julgado"), 
	
	/**
	 * Os processos do bloco foram retirados de julgamento 
	 */
	NJ("Retirado de julgamento"),
	
	/**
	 * Os processos do bloco foram adiados 
	 */
	AD("Adiado para a próxima sessão");


	private String label;

	TipoSituacaoPautaBlocoEnum(String label) {
		this.label = label;
	}

	/**
	 * Retorna o Enum pelo valor passado por descrição da situação do bloco
	 * Se não pertençe ao domínio retorna null.
	 * 
	 * @return TipoSituacaoPautaEnum
	 */

	public static TipoSituacaoPautaBlocoEnum getEnum(String descricaoSituacao) {
		for (TipoSituacaoPautaBlocoEnum item : values()) {
			if (item.name().equals(descricaoSituacao)) {
				return item;
			}
		}
		return null;
	}
	
	
	public String getLabel() {
		return this.label;
	}

	public static String getAcaoBtnLegenda(TipoSituacaoPautaBlocoEnum situacao) {
		String retorno = "";
		switch(situacao) {
			case EJ:
				retorno = "emjulgamento";
				break;
			case NJ:
				retorno = "retirado";
				break;
			case JG:
				retorno = "julgado";
				break;
			case AJ:
				retorno = "aguardando";
				break;
			case AD:
				retorno = "adiado";
				break;
		}
		return retorno;
	}
	
	public static boolean verificarSituacaoJulgado(TipoSituacaoPautaBlocoEnum situacao) {
		return situacao == TipoSituacaoPautaBlocoEnum.NJ || situacao == TipoSituacaoPautaBlocoEnum.JG || situacao == TipoSituacaoPautaBlocoEnum.AD;
	}
	
	public static boolean verificarAlteracaoJulgado(TipoSituacaoPautaBlocoEnum situacaoNova, TipoSituacaoPautaBlocoEnum situacaoAntiga) {
		return (verificarSituacaoJulgado(situacaoNova) == !verificarSituacaoJulgado(situacaoAntiga));
	}
}
