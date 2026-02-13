/**
 * pje-comum
 * Copyright (C) 2009-2017 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.enums;

/**
* Enum contendo 4 dos 6 serviços do MNI implementados no PJe:
*<br>
*<li>consultarAvisosPendentes</li> 
*<li>consultarTeorComunicacao</li>
*<li>consultarProcesso</li>
*<li>entregarManifestacaoProcessual</li>
*<br>
* @since   21/02/2017 
*/
public enum ServicosPJeMNIEnum {

	ConsultarAvisosPendentes("consultarAvisosPendentes"), 
	ConsultarTeorComunicacao("consultarTeorComunicacao"), 
	ConsultarProcesso("consultarProcesso"), 
	EntregarManifestacaoProcessual("entregarManifestacaoProcessual");

	private String label;

	private ServicosPJeMNIEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}	

}
