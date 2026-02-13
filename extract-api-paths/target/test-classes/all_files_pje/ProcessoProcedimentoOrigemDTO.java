package br.jus.pje.nucleo.dto;

import java.util.Date;

import javax.persistence.Transient;


public class ProcessoProcedimentoOrigemDTO extends PJeServiceApiDTO {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private ProcessoCriminalDTO processo;
	private OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginario;
	private TipoProcedimentoOrigemDTO tipoProcedimentoOrigem;
	private TipoOrigemDTO tipoOrigem;
	private Date dataInstauracao;
	private String numero;
	private Integer ano;
	private Date dataLavratura;
	private Boolean ativo = true;
	private Boolean retombamentoRedistribuicao = false;
	private ProcessoProcedimentoOrigemDTO processoProcedimentoOrigemRetombado;
	private String nrProtocoloPolicia;
	private String uf;

	public ProcessoProcedimentoOrigemDTO() {
		super();
	}

	@Transient
	public String obterDescricao() {
		return numero + " / " + ano;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ProcessoCriminalDTO getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoCriminalDTO processo) {
		this.processo = processo;
	}

	public OrgaoProcedimentoOriginarioDTO getOrgaoProcedimentoOriginario() {
		return orgaoProcedimentoOriginario;
	}

	public void setOrgaoProcedimentoOriginario(OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginario) {
		this.orgaoProcedimentoOriginario = orgaoProcedimentoOriginario;
	}

	public TipoProcedimentoOrigemDTO getTipoProcedimentoOrigem() {
		return tipoProcedimentoOrigem;
	}

	public void setTipoProcedimentoOrigem(TipoProcedimentoOrigemDTO tipoProcedimentoOrigem) {
		this.tipoProcedimentoOrigem = tipoProcedimentoOrigem;
	}

	public TipoOrigemDTO getTipoOrigem() {
		return tipoOrigem;
	}

	public void setTipoOrigem(TipoOrigemDTO tipoOrigem) {
		this.tipoOrigem = tipoOrigem;
	}

	public Date getDataInstauracao() {
		return dataInstauracao;
	}

	public void setDataInstauracao(Date dataInstauracao) {
		this.dataInstauracao = dataInstauracao;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Date getDataLavratura() {
		return dataLavratura;
	}

	public void setDataLavratura(Date dataLavradura) {
		this.dataLavratura = dataLavradura;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Boolean getRetombamentoRedistribuicao() {
		return retombamentoRedistribuicao;
	}

	public void setRetombamentoRedistribuicao(Boolean retombamentoRedistribuicao) {
		this.retombamentoRedistribuicao = retombamentoRedistribuicao;
	}

	public ProcessoProcedimentoOrigemDTO getProcessoProcedimentoOrigemRetombado() {
		return processoProcedimentoOrigemRetombado;
	}

	public void setProcessoProcedimentoOrigemRetombado(
			ProcessoProcedimentoOrigemDTO processoProcedimentoOrigemRetombado) {
		this.processoProcedimentoOrigemRetombado = processoProcedimentoOrigemRetombado;
	}

	public String getNrProtocoloPolicia() {
		return nrProtocoloPolicia;
	}

	public void setNrProtocoloPolicia(String nrProtocoloPolicia) {
		this.nrProtocoloPolicia = nrProtocoloPolicia;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

}
