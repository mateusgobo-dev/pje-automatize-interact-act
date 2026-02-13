package br.jus.pje.nucleo.dto;

import java.util.ArrayList;
import java.util.List;

public class TipoOrigemDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String dsTipoOrigem;
	private Boolean inObrigatorioNumeroOrigem;
	private Boolean ativo;

	private List<TipoProcedimentoOrigemDTO> tipoProcedimentoOrigemList = new ArrayList<TipoProcedimentoOrigemDTO>();

	public TipoOrigemDTO() {
		super();
	}

	public TipoOrigemDTO(String dsTipoOrigem, Boolean inObrigatorioNumeroOrigem, Boolean ativo) {
		super();
		this.dsTipoOrigem = dsTipoOrigem;
		this.inObrigatorioNumeroOrigem = inObrigatorioNumeroOrigem;
		this.ativo = ativo;
	}
	
	public TipoOrigemDTO(Integer id, String dsTipoOrigem, Boolean inObrigatorioNumeroOrigem, Boolean ativo) {
		super();
		this.id = id;
		this.dsTipoOrigem = dsTipoOrigem;
		this.inObrigatorioNumeroOrigem = inObrigatorioNumeroOrigem;
		this.ativo = ativo;
	}

	public TipoOrigemDTO(Integer id) {
		this.id = id;
	}

	public String getDsTipoOrigem() {
		return dsTipoOrigem;
	}

	public void setDsTipoOrigem(String dsTipoOrigem) {
		this.dsTipoOrigem = dsTipoOrigem;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Boolean getInObrigatorioNumeroOrigem() {
		return inObrigatorioNumeroOrigem;
	}

	public void setInObrigatorioNumeroOrigem(Boolean obrigatorioNumeroOrigem) {
		this.inObrigatorioNumeroOrigem = obrigatorioNumeroOrigem;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<TipoProcedimentoOrigemDTO> getTipoProcedimentoOrigemList() {
		return tipoProcedimentoOrigemList;
	}

	public void setTipoProcedimentoOrigemList(List<TipoProcedimentoOrigemDTO> tipoProcedimentoOrigemList) {
		this.tipoProcedimentoOrigemList = tipoProcedimentoOrigemList;
	}

}
