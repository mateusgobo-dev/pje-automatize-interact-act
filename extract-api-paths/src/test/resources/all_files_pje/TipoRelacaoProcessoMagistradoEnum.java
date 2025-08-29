package br.jus.pje.nucleo.enums;

/**
 * 
 * Tipos de justificativas/motivos que levam um magistrado a se relacionar/vincular a um processo.
 * 
 * @see {@link TipoAtuacaoDetalhadaMagistradoEnum}
 *
 */
public enum TipoRelacaoProcessoMagistradoEnum {

	/**
	 * Representa a vinculação regimental de magistrado titular o substituto ao
	 * processo.
	 * 
	 * Uma vinculação regimental de Magistrado auxiliar tem efeito na capa do
	 * processo, passado a aparecer como Relator Convocado. Se for revisor será
	 * o Revisor Convocado.
	 */
	REGIM("Vinculação Regimental"),
	
	/**
	 * Tipo de vinculação para quando o magistrado for vogal em uma sessão e
	 * este vencer. Ele será designado como relator (Relator Designado)
	 */
	DESIG("Designação"),
	
	/**
	 * Tipo de vinculação para quando o magistrado quiser reservar o processo
	 * para o si para posterior vinculação (regimental). Util quando o
	 * magistrado substituto, por motivos de metas, já quiser reservar alguns
	 * processos para vinculação antes que o seu período de substituição acabe.
	 * 
	 */
	RESER("Reserva Processual"),
	
	/**
	 * Tipo de vinculação utilizada quando há uma convoçação extraordinária do
	 * presidente para que magistrados atuem nos processos.
	 */
	CONVO("Convocação Extraordinária");
	
	private String label;

	TipoRelacaoProcessoMagistradoEnum(String label) {
		this.label = label;
	}
	
	
	public String getLabel() {
		return this.label;
	}

}