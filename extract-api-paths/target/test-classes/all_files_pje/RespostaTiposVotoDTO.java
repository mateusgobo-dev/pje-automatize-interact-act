package br.jus.je.pje.business.dto;

import java.util.List;

public class RespostaTiposVotoDTO {
	private Boolean podeAlterar;
	private TipoVotoDTO selecao;
	private List<TipoVotoDTO> tipos;
	
	public Boolean getPodeAlterar() {
		return podeAlterar;
	}
	public void setPodeAlterar(Boolean podeAlterar) {
		this.podeAlterar = podeAlterar;
	}
	public TipoVotoDTO getSelecao() {
		return selecao;
	}
	public void setSelecao(TipoVotoDTO selecao) {
		this.selecao = selecao;
	}
	public List<TipoVotoDTO> getTipos() {
		return tipos;
	}
	public void setTipos(List<TipoVotoDTO> tipos) {
		this.tipos = tipos;
	}
}
