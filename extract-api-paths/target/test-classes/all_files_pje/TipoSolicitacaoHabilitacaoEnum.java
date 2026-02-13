package br.jus.pje.jt.enums;

import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.enums.PJeEnum;

public enum TipoSolicitacaoHabilitacaoEnum implements PJeEnum {
	
	S("Habilitação Simples - solicitar a habilitação em meu nome.", true),
	R("Habilitação com substituição - solicitar a habilitação em meu nome, ", false),
	I("Habilitação por substabelecimento - solicitar a habilitação nos autos em nome de terceiros, para partes que eu represento.",true),
	T("Habilitação por substabelecimento e substituição", false);
	
	
	private String label;
	private Boolean visivel;
	

	TipoSolicitacaoHabilitacaoEnum(String label, Boolean visivel) {
		this.label = label;
		this.visivel = visivel;
	}

	@Override
	public String getLabel() {
		return label;
	}
	
	public Boolean isVisivel(){
		return visivel;
	}

	public static List<TipoSolicitacaoHabilitacaoEnum> getVisiveis() {

		List<TipoSolicitacaoHabilitacaoEnum> listEnum = new ArrayList<TipoSolicitacaoHabilitacaoEnum>();
		
		for (TipoSolicitacaoHabilitacaoEnum tsh : TipoSolicitacaoHabilitacaoEnum.values()) {
			if(tsh.isVisivel()){
				listEnum.add(tsh);
			}
		}
		return listEnum;
	}
	

}
