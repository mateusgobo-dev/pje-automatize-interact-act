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

public enum EstadosBrasileirosEnum {
	AC("Acre"), AL("Alagoas"), AP("Amapá"), AM("Amazonas"), BA("Bahia"), CE("Ceará"), DF("Distrito Federal"), ES(
			"Espírito Santo"), GO("Goiás"), MA("Maranhão"), MT("Mato Grosso"), MS("Mato Grosso do Sul"), MG(
			"Minas Gerais"), PA("Pará"), PB("Paraíba"), PR("Paraná"), PE("Pernambuco"), PI("Piauí"), RJ(
			"Rio de Janeiro"), RN("Rio Grande do Norte"), RS("Rio Grande do Sul"), RO("Rondônia"), RR("Roraima"), SC(
			"Santa Catarina"), SP("São Paulo"), SE("Sergipe"), TO("Tocantins");

	private String label;

	EstadosBrasileirosEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

}
