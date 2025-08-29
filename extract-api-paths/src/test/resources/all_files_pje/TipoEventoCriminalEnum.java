package br.jus.pje.nucleo.beans.criminal;

public enum TipoEventoCriminalEnum {
	
	NULL("Nulo"), 
	AAU("Atribuição de Autoria dos Fatos"), 
	SEN("Sentença"), 
	IND("Indiciamento"), 
	OFD("Oferecimento da Denúncia"), 
	ADD("Aditamento da Denúncia"), 
	ADQ("Aditamento da Queixa"), 
	RCD("Recebimento da Denúncia"), 
	PRI("Prisão"), 
	FUG("Fuga"), 
	SOL("Soltura"), 
	TRR("Transferência do Réu"), 
	SAP("Sentença Absolutória"), 
	CQA("Cadastro de Queixa"), 
	NRQ("Não Recebimento da Queixa"), 
	SAI("Sentença Absolutória Imprópria"), 
	SAS("Sentença Absolvição Sumária"), 
	SPR("Sentença de Pronúncia"), 
	SEI("Sentença de Impronúncia"), 
	SEP("Sentença de Extinção da Punibilidade"), 
	DIS("Decisão em Instância Superior"),
	CRQ("Cadastrar Recebimento de Queixa"), 
	TRP("Transação Penal"), 
	ETP("Encerrar Transação Penal"), 
	STP("Suspender Transação Penal"), 
	ESP("Encerrar Suspensão do Processo"), 
	SUS("Suspensao do Processo"), 
	SCO("Sentença Condenatória"), 
	SSP("Suspender Suspensao do Processo"), 
	RSP("Retomar Suspensão do Processo"), 
	DES("Desclassificação do Processo"), 
	NRD("Não Recebimento da Denuncia"),
	CIT("Citação"),
	RAD("Recebimento do Aditamento da Denúncia"),
	ADR("Aditamento da Representação"),
	OFR("Oferecimento da Representação"),
	POP("Preso por Outro Processo"),
	RCR("Recebimento da Representação"),
	INT("Internação"),
	SML("Semiliberdade"),
	NRR("Não Recebimento da Representação"),
	RAR("Recebimento do Aditamento da Representação"),
	SIN("Sentença Infracional"),
	SEM("Sentença de Extinção de Medida Socioeducativa"),
	EVA("Evasão"),
	SMS("Suspensão da Medida Socioeducativa"),
	ACA("Ausência das condições da ação"),
	DLC("Declínio de competência"),
	DEC("Decisão"),
	DEM("Desmembramento"),
	HCD("Homologação de composição de danos civis"),
	LCJ("Litispendência e coisa julgada"),
	NPR ("Nulidade do Processo"),
	TRA ("Trancamento"),
	ARI ("Arquivamento da incidência");
	
	private String label;

	TipoEventoCriminalEnum(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	@Override
	public String toString() {
		return this.name();
	}
}
