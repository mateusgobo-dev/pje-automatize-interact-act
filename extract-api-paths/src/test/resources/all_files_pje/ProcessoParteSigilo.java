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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SigiloStatusEnum;

@Entity
@Table(name = "tb_processo_parte_sigilo")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_parte_sigilo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_parte_sigilo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteSigilo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteSigilo,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoParteSigilo;
	private ProcessoParte processoParte;
	private String motivo;
	private Date dataAlteracao;
	private PessoaFisica usuarioCadastro;
	private SigiloStatusEnum status;

	public ProcessoParteSigilo() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_parte_sigilo")
	@Column(name = "id_processo_parte_sigilo", unique = true, nullable = false)
	public int getIdProcessoParteSigilo() {
		return idProcessoParteSigilo;
	}

	public void setIdProcessoParteSigilo(int idProcessoParteSigilo) {
		this.idProcessoParteSigilo = idProcessoParteSigilo;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	@NotNull
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@Column(name = "ds_motivo", length = 200, nullable = false)
	@Length(max = 200)
	@NotNull
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao", nullable = false)
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastro")
	public PessoaFisica getUsuarioCadastro() {
		return usuarioCadastro;
	}

	public void setUsuarioCadastro(PessoaFisica usuarioCadastro) {
		this.usuarioCadastro = usuarioCadastro;
	}

	@Column(name = "in_situacao_parte", length = 1)
	@Enumerated(EnumType.STRING)
	public SigiloStatusEnum getStatus() {
		return status;
	}

	public void setStatus(SigiloStatusEnum status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoParteSigilo)) {
			return false;
		}
		ProcessoParteSigilo other = (ProcessoParteSigilo) obj;
		if (getIdProcessoParteSigilo() != other.getIdProcessoParteSigilo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParteSigilo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteSigilo> getEntityClass() {
		return ProcessoParteSigilo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteSigilo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
