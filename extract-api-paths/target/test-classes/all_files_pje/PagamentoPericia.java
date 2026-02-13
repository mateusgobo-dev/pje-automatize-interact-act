/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.PagamentoEnum;

@Entity
@Table(name = PagamentoPericia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pagamento_pericia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pagamento_pericia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PagamentoPericia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PagamentoPericia,Integer> {

	public static final String TABLE_NAME = "tb_pagamento_pericia";
	private static final long serialVersionUID = 1L;

	private int idPagamentoPericia;
	private ProcessoPericia processoPericia;
	private PessoaServidor pessoaServidor;
	private PagamentoEnum pagamento;
	private Double valorPercentualRequerido;
	private Double valorPercentualPago;
	private Date dataSolicitacao;
	private Date dataPagamento;
	private String nomePerito;
	private String numeroProcesso;
	private String nomePericiado;
	private String especialidade;
	private Double totalSolicitacaoPago = 0.0;

	@Id
	@GeneratedValue(generator = "gen_pagamento_pericia")
	@Column(name = "id_pagamento_pericia", unique = true, nullable = false)
	public int getIdPagamentoPericia() {
		return this.idPagamentoPericia;
	}

	public void setIdPagamentoPericia(int idPagamentoPericia) {
		this.idPagamentoPericia = idPagamentoPericia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_pericia", nullable = false)
	@NotNull
	public ProcessoPericia getProcessoPericia() {
		return this.processoPericia;
	}

	public void setProcessoPericia(ProcessoPericia processoPericia) {
		this.processoPericia = processoPericia;
	}

	@Transient
	public Double getValorPericia() {
		return this.processoPericia.getValorPericia();
	}

	public void setValorPericia(Double valorPericia) {
		this.processoPericia.setValorPericia(valorPericia);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_servidor")
	public PessoaServidor getPessoaServidor() {
		return pessoaServidor;
	}

	public void setPessoaServidor(PessoaServidor pessoaServidor) {
		this.pessoaServidor = pessoaServidor;
	}

	@Column(name = "in_pagamento")
	@Enumerated(EnumType.STRING)
	public PagamentoEnum getPagamento() {
		return pagamento;
	}

	public void setPagamento(PagamentoEnum pagamento) {
		this.pagamento = pagamento;
	}

	@Column(name = "vl_requerido")
	public Double getValorPercentualRequerido() {
		return valorPercentualRequerido;
	}

	public void setValorPercentualRequerido(Double valorPercentualRequerido) {
		this.valorPercentualRequerido = valorPercentualRequerido;
	}

	@Column(name = "vl_pago")
	public Double getValorPercentualPago() {
		return valorPercentualPago;
	}

	public void setValorPercentualPago(Double valorPercentualPago) {
		this.valorPercentualPago = valorPercentualPago;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_solicitacao")
	public Date getDataSolicitacao() {
		return dataSolicitacao;
	}

	public void setDataSolicitacao(Date dataSolicitacao) {
		this.dataSolicitacao = dataSolicitacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_pagamento")
	public Date getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	@Override
	public String toString() {
		return this.processoPericia.getProcessoTrf().getProcesso().getNumeroProcesso();
	}

	@Transient
	public String getEspecialidade() {
		if (getProcessoPericia() != null) {
			especialidade = getProcessoPericia().getEspecialidade().toString();
		}
		return especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}

	public PagamentoPericia() {
	}

	@Transient
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	@Transient
	public String getNomePericiado() {
		return nomePericiado;
	}

	public void setNomePericiado(String nomePericiado) {
		this.nomePericiado = nomePericiado;
	}

	@Transient
	public String getNomePerito() {
		if (getProcessoPericia() != null) {
			nomePerito = getProcessoPericia().getPessoaPerito().getNome();
		}
		return nomePerito;
	}

	public void setNomePerito(String nomePerito) {
		this.nomePerito = nomePerito;
	}

	@Transient
	public Double getTotalSolicitacaoPago() {
		return totalSolicitacaoPago;
	}

	public void setTotalSolicitacaoPago(Double totalSolicitacaoPago) {
		this.totalSolicitacaoPago = totalSolicitacaoPago;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PagamentoPericia)) {
			return false;
		}
		PagamentoPericia other = (PagamentoPericia) obj;
		if (getIdPagamentoPericia() != other.getIdPagamentoPericia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPagamentoPericia();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PagamentoPericia> getEntityClass() {
		return PagamentoPericia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPagamentoPericia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
