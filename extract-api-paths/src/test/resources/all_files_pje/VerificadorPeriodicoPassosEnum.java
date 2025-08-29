package br.jus.pje.nucleo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VerificadorPeriodicoPassosEnum implements PJeEnum {

	SINALIZA_PROCESSOS_AGUARDANDO_AUDIENCIA("Sinaliza Processos Aguardando Audiencia"),
	REGISTRAR_CIENCIA_AUTOMATICA("Registrar Ciencia Automatica"),
	CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO("Ciencia Automatizada Diario Eletronico"),
	CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO_POR_DATA("Ciencia Automatizada Diario Eletronico por Data"),
	CIENCIA_AUTOMATIZADA_DIARIO_ELETRONICO_POR_MATERIA("Ciencia Automatizada Diario Eletronico por Materia"),
	REGISTRAR_DECURSO_PRAZO("Registrar Decurso Prazo"),
	SINALIZA_PROSSEGUIMENTO_SEM_PRAZO("Sinaliza Prosseguimento Sem Prazo"),
	FECHAR_PAUTA_AUTOMATICAMENTE("Fechar Pauta Automaticamente"),
	ENCERRAMENTO_PRAZO_NAO_PROCESSUAL("Encerramento Prazo Nao Processual");

	private String label;

	private VerificadorPeriodicoPassosEnum(String label) {
		this.label = label;
	}

	@JsonValue
	public String getLabel() {
		return this.label;
	}
}