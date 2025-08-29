package br.jus.pje.nucleo.dto;

import java.util.List;

public class TipoProcedimentoOrigemDTO  extends PJeServiceApiDTO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String  dsTipoProcedimento;
	private Boolean ativo;
	private List<TipoOrigemDTO> tipoOrigemList;
	
	public TipoProcedimentoOrigemDTO() {
		super();
	}

	public TipoProcedimentoOrigemDTO(String dsTipoProcedimento, Boolean inAtivo) {
		super();
		this.dsTipoProcedimento = dsTipoProcedimento;
		this.ativo = inAtivo;
	}

	public TipoProcedimentoOrigemDTO(String dsTipoProcedimento, Boolean inAtivo, List<TipoOrigemDTO> tipoOrigemList) {
		super();
		this.dsTipoProcedimento = dsTipoProcedimento;
		this.ativo = inAtivo;
		this.tipoOrigemList = tipoOrigemList;
	}
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDsTipoProcedimento() {
		return dsTipoProcedimento;
	}

	public void setDsTipoProcedimento(String dsTipoProcedimento) {
		this.dsTipoProcedimento = dsTipoProcedimento;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean inAtivo) {
		this.ativo = inAtivo;
	}

	public List<TipoOrigemDTO> getTipoOrigemList() {
		return tipoOrigemList;
	}

	public void setTipoOrigemList(List<TipoOrigemDTO> tipoOrigemList) {
		this.tipoOrigemList = tipoOrigemList;
	}

}
