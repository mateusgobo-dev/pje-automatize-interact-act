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


public enum ExpedicaoExpedienteEnum implements PJeEnum {

	E("Sistema"),
	P("Diário Eletrônico"), 
	D("Edital"),
	L("Carta Precatória"),
	C("Correios"),
	M("Central de Mandados"),
	T("Telefone"),
	S("Pessoalmente"),
	N("Comunicação"),
	A("Sessão"),
	R("Mural"),
	G("Correios/SPE"),
	I("E-mail");

	private String label;

	ExpedicaoExpedienteEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
	
	public boolean isExpedicaoFisica() {
		return (C.equals(this) || M.equals(this) || L.equals(this) || G.equals(this));
	}
	
	public boolean isExpedicaoRealizadaPessoalmente(){
		return (T.equals(this) || S.equals(this));
	}
	
	public boolean isExigeEndereco(){
		return (M.equals(this) || C.equals(this) || L.equals(this) || G.equals(this));
	}
	
	public boolean isExigeParteCadastradaComCertificado(){
		return E.equals(this);
	}
	
	public boolean isExigeTelefone(){
	    return T.equals(this);
	}
	
}
