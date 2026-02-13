package br.jus.pje.nucleo.dto;

import br.jus.pje.nucleo.enums.MotivoAdiamentoJulgamentoProcessoEnum;

public class SugestaoAdiamentoJulgamentoProcessoDTO {

	private String numeroNumeroProcesso;
	private MotivoAdiamentoJulgamentoProcessoEnum motivoAdiamento;
	private String nomeMagistrado;
	
	
	public String getNumeroProcesso() {
		return numeroNumeroProcesso;
	}
	public void setNumeroProcesso(String numeroNumeroProcesso) {
		this.numeroNumeroProcesso = numeroNumeroProcesso;
	}
	public MotivoAdiamentoJulgamentoProcessoEnum getMotivoAdiamento() {
		return motivoAdiamento;
	}
	public void setMotivoAdiamento(MotivoAdiamentoJulgamentoProcessoEnum motivoAdiamento) {
		this.motivoAdiamento = motivoAdiamento;
	}
	public String getNomeMagistrado() {
		return nomeMagistrado;
	}
	public void setNomeMagistrado(String nomeMagistrado) {
		this.nomeMagistrado = nomeMagistrado;
	}
	
	
	
}
