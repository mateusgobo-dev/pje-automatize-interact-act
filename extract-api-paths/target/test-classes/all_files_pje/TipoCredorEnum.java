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
package br.jus.pje.jt.enums;

public enum TipoCredorEnum { // TODO: Corrigir códigos das especialidades raiz

	U("União", false, ""), A("Autor", false, ""), R("Réu", false, ""), G("Advogado", false, ""), E("Engenheiro", true,
			"CREA"), C("Contador", true, "CRC"), D("Documentoscopista", true, "COD_D"), I("Intérprete", true, "COD_I"), M(
			"Médico", true, "CRM"), O("Outro Perito", true, "OUTRO_PERITO"), L("Leiloeiro", false, "");

	private String label;
	private Boolean isPerito;
	private String codigoEspecialidade;

	TipoCredorEnum(String label, Boolean isPerito, String codigoEspecialidade) {
		this.label = label;
		this.isPerito = isPerito;
		this.codigoEspecialidade = codigoEspecialidade;
	}

	public String getLabel() {
		return this.label;
	}

	public Boolean getIsPerito() {
		return isPerito;
	}

	public String getCodigoEspecialidade() {
		return this.codigoEspecialidade;
	}

}
