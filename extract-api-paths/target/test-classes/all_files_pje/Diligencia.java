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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = Diligencia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_diligencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_diligencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Diligencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Diligencia,Integer> {

	public static final String TABLE_NAME = "tb_diligencia";
	private static final long serialVersionUID = 1L;

	private int idDiligencia;
	private ProcessoExpedienteCentralMandado processoExpedienteCentralMandado;
	private TipoResultadoDiligencia tipoResultadoDiligencia;
	
	private PessoaOficialJustica pessoaOficialJustica;
	private ProcessoDocumento processoDocumento;
	private Boolean ativo;
	private Date dtCumprimento;

	public Diligencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_diligencia")
	@Column(name = "id_diligencia", unique = true, nullable = false)
	public int getIdDiligencia() {
		return this.idDiligencia;
	}

	public void setIdDiligencia(int idDiligencia) {
		this.idDiligencia = idDiligencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_exped_central_mandado", nullable = false)
	@NotNull
	public ProcessoExpedienteCentralMandado getProcessoExpedienteCentralMandado() {
		return processoExpedienteCentralMandado;
	}

	public void setProcessoExpedienteCentralMandado(ProcessoExpedienteCentralMandado processoExpedienteCentralMandado) {
		this.processoExpedienteCentralMandado = processoExpedienteCentralMandado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_resultado_diligencia")
	public TipoResultadoDiligencia getTipoResultadoDiligencia() {
		return tipoResultadoDiligencia;
	}

	public void setTipoResultadoDiligencia(TipoResultadoDiligencia tipoResultadoDiligencia) {
		this.tipoResultadoDiligencia = tipoResultadoDiligencia;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.REMOVE)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cumprimento")
	public Date getDtCumprimento() {
		return dtCumprimento;
	}

	public void setDtCumprimento(Date dtCumprimento) {
		this.dtCumprimento = dtCumprimento;
	}

	@Column(name="in_ativo")
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_oficial_justica")
	public PessoaOficialJustica getPessoaOficialJustica() {
		return pessoaOficialJustica;
	}

	public void setPessoaOficialJustica(PessoaOficialJustica pessoaOficialJustica) {
		this.pessoaOficialJustica = pessoaOficialJustica;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Diligencia)) {
			return false;
		}
		Diligencia other = (Diligencia) obj;
		if (getIdDiligencia() != other.getIdDiligencia()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdDiligencia();
		return result;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends Diligencia> getEntityClass() {
		return Diligencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDiligencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
