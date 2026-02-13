package br.jus.pje.nucleo.enums;

public enum SegredoEntreProcessosJudiciaisEnum {
	SOMENTE_P1_TRAMITA_EM_SEGREDO(1),
	SOMENTE_P2_TRAMITA_EM_SEGREDO(2),
	NENHUM_DOS_PROCESSOS_TRAMITAM_EM_SEGREDO(3),
	OS_DOIS_PROCESSOS_TRAMITAM_EM_SEGREDO_SENDO_P1_MAIS_ANTIGO_QUE_P2(4),
	OS_DOIS_PROCESSOS_TRAMITAM_EM_SEGREDO_SENDO_P2_MAIS_ANTIGO_QUE_P1(5),
	SEGREDO_NAO_IDENTIFICADO(6);

	private int codigoSegredo;

	private SegredoEntreProcessosJudiciaisEnum(int codigoSegredo) {
		this.codigoSegredo = codigoSegredo;
	}
	
	public int getCodigoSegredo() {
		return codigoSegredo;
	}
}
