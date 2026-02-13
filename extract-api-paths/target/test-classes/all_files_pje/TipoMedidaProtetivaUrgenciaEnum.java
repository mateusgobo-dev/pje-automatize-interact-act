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

public enum TipoMedidaProtetivaUrgenciaEnum {
	MPArt22I(
			"Art. 22, I - suspensão da posse ou restrição do porte de armas, com comunicação ao órgão competente, nos termos da Lei nº 10.826/2003"
	),
	MPArt22II(
			"Art. 22, II - afastamento do lar, domicílio ou local de convivência com a ofendida"),
	MPArt22IIIa(
			"Art. 22, III, a) proibição de aproximação da ofendida, de seus familiares e das testemunhas, fixando o limite mínimo de distância entre estes e o agressor"
	),
	MPArt22IIIb(
			"Art. 22, III, b) proibição de contato com a ofendida, seus familiares e testemunhas por qualquer meio de comunicação"
	),
	MPArt22IIIc(
			"Art. 22, III, c) proibição de freqüentação de determinados lugares a fim de preservar a integridade física e psicológica da ofendida"
	),
	MPArt22IV(
			"Art. 22, IV - restrição ou suspensão de visitas aos dependentes menores, ouvida a equipe de atendimento multidisciplinar ou serviço similar"
	),
	MPArt22V(
			"Art. 22, V - prestação de alimentos provisionais ou provisórios"),
	MPArt22OutrasMedidas(
			"Art. 22, § 1º - Outras Medidas"
	);

	private String descricao;

	TipoMedidaProtetivaUrgenciaEnum(String descricao){
		this.setDescricao(descricao);

	}

	public void setDescricao(String descricao){
		this.descricao = descricao;
	}

	public String getDescricao(){
		return descricao;
	}

	public String getCodigo(){
		return name();
	}

}
