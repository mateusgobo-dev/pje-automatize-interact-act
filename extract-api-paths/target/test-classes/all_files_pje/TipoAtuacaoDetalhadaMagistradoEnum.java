package br.jus.pje.nucleo.enums;

/**
 * Classe para exibição da atuação detalhada dos magistrados na capa do processo
 * e em relatórios
 */
public enum TipoAtuacaoDetalhadaMagistradoEnum {
	
	RELATOR("Relator"),
	RELATOR_CONVOCADO("Relator Convocado"),
	RELATOR_DESIGNADO("Relator Designado"),
	REVISOR("Revisor"),
	REVISOR_CONVOCADO("Revisor Convocado");
	
	private String label;

	TipoAtuacaoDetalhadaMagistradoEnum(String label) {
		this.label = label;
	}
	public String getLabel() {
		return this.label;
	}
	
	/**
	 * Método responsável por criar um
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum} com base em regras para
	 * exibição do papel do magistrado na capa do processo de forma mais
	 * detalhada.
	 * 
	 * @param tipoRelacao
	 *            {@link TipoRelacaoProcessoMagistradoEnum}
	 *            
	 * @param tipoAtuacao
	 *            {@link TipoAtuacaoMagistradoEnum} que representa a relação
	 *            menos detalhada do magistrado com o processo
	 *            
	 * @param magistradoTitular
	 *            Indica se a relação do magistrado com o processo foi gerada
	 *            através de titularidado do órgão julgador ou por subistituição
	 *            
	 * @return {@link TipoAtuacaoDetalhadaMagistradoEnum} representando uma
	 *         descrição da atuação do magistrado no processo para uso em
	 *         relatórios e capa do processo
	 */
	public static TipoAtuacaoDetalhadaMagistradoEnum valueOf(
			TipoRelacaoProcessoMagistradoEnum tipoRelacao, 
			TipoAtuacaoMagistradoEnum tipoAtuacao,
			boolean magistradoTitular) {	
		
		if (tipoRelacao.equals(TipoRelacaoProcessoMagistradoEnum.REGIM)) {
			if (tipoAtuacao.equals(TipoAtuacaoMagistradoEnum.RELAT)) {
				if (magistradoTitular) {
					return TipoAtuacaoDetalhadaMagistradoEnum.RELATOR;
				} else {
					return TipoAtuacaoDetalhadaMagistradoEnum.RELATOR_CONVOCADO;
				}
				
			} else if (tipoAtuacao.equals(TipoAtuacaoMagistradoEnum.REVIS)) {
				if (magistradoTitular) {
					return TipoAtuacaoDetalhadaMagistradoEnum.REVISOR;
				} else {
					return TipoAtuacaoDetalhadaMagistradoEnum.REVISOR_CONVOCADO;
				}
			}
		} else if (tipoRelacao.equals(TipoRelacaoProcessoMagistradoEnum.DESIG) && tipoAtuacao.equals(TipoAtuacaoMagistradoEnum.RELAT)) {
			return TipoAtuacaoDetalhadaMagistradoEnum.RELATOR_DESIGNADO;
			
		} 
		return null;
	}
	
	public String toString(){
		return this.getLabel();
	}

}