package br.jus.pje.nucleo.enums;

public enum SituacaoPautaEnum {

	PA("Incluído em Pauta", TipoInclusaoEnum.PA), 
	AD("Adiados", TipoInclusaoEnum.AD), 
	PV("Pedido de Vista", TipoInclusaoEnum.PV), 
	ME("Incluído em mesa", TipoInclusaoEnum.ME);

	private String label;
	private TipoInclusaoEnum tipoInclusaoEnum;

	SituacaoPautaEnum(String label) {
		this.label = label;
	}

	SituacaoPautaEnum(String label, TipoInclusaoEnum tipoInclusaoEnum) {
		this.tipoInclusaoEnum = tipoInclusaoEnum;
		this.label = label;
	}

	public TipoInclusaoEnum getTipoInclusaoEnum() {
		return tipoInclusaoEnum;
	}

	public String getLabel() {
		return this.label;
	}

}
