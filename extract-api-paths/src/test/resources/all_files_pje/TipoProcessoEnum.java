package br.jus.pje.nucleo.beans.criminal;

public enum TipoProcessoEnum {

	CRI("Criminal"),
	INF("Infracional"),
	T("Todos");
	
	private String descricao;
	
	TipoProcessoEnum(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return this.descricao;
	}
	
}
