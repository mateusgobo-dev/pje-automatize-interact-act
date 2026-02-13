package br.jus.pje.nucleo.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParteDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long idProcesso;
	private Long idProcessoParteLegacy;
	private Long idPessoaLegacy;
	private String rji;
	private ProcessoParteSituacaoEnum situacaoParte = ProcessoParteSituacaoEnum.A;
	
	public ParteDTO() {
		super();
	}

	public ParteDTO(Long id, Long idProcesso, Long idProcessoParteLegacy, Long idPessoaLegacy, String rji, ProcessoParteSituacaoEnum situacaoParte) {
		super();
		this.id = id;
		this.idProcesso = idProcesso;
		this.idProcessoParteLegacy = idProcessoParteLegacy;
		this.idPessoaLegacy = idPessoaLegacy;
		this.rji = rji;
		this.situacaoParte = situacaoParte;
	}
	
	public ParteDTO(Long idProcessoParteLegacy, Long idPessoaLegacy, String rji, ProcessoParteSituacaoEnum situacaoParte){
		super();
		this.idProcessoParteLegacy = idProcessoParteLegacy;
		this.idPessoaLegacy = idPessoaLegacy;
		this.rji = rji;	
		this.situacaoParte = situacaoParte;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getIdProcesso() {
		return idProcesso;
	}
	
	public void setIdProcesso(Long idProcesso) {
		this.idProcesso = idProcesso;
	}
	
	public Long getIdProcessoParteLegacy() {
		return idProcessoParteLegacy;
	}
	
	public void setIdProcessoParteLegacy(Long idProcessoParteLegacy) {
		this.idProcessoParteLegacy = idProcessoParteLegacy;
	}
	
	public Long getIdPessoaLegacy() {
		return idPessoaLegacy;
	}
	
	public void setIdPessoaLegacy(Long idPessoaLegacy) {
		this.idPessoaLegacy = idPessoaLegacy;
	}
	
	public String getRji() {
		return rji;
	}
	
	public void setRji(String rji) {
		this.rji = rji;
	}
	
	public ProcessoParteSituacaoEnum getSituacaoParte() {
		return situacaoParte;
	}
	
	public void setSituacaoParte(ProcessoParteSituacaoEnum situacaoParte) {
		this.situacaoParte = situacaoParte;
	}
}
