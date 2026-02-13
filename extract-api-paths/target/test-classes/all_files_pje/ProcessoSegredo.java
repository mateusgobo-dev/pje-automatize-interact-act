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

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.SegredoStatusEnum;

@Entity
@Table(name = "tb_processo_segredo")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_segredo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_segredo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoSegredo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoSegredo,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoSegredo;
	private ProcessoTrf processoTrf;
	private String motivo;
	private Date dtAlteracao;
	private UsuarioLogin usuarioLogin;
	private Boolean apreciado = Boolean.FALSE;
	private SegredoStatusEnum status;

	@Id
	@GeneratedValue(generator = "gen_processo_segredo")
	@Column(name = "id_processo_segredo", nullable = false)
	public int getIdProcessoSegredo() {
		return idProcessoSegredo;
	}

	public void setIdProcessoSegredo(int idProcessoSegredo) {
		this.idProcessoSegredo = idProcessoSegredo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastro")
	public UsuarioLogin getUsuarioLogin() {
		return usuarioLogin;
	}

	public void setUsuarioLogin(UsuarioLogin usuarioLogin) {
		this.usuarioLogin = usuarioLogin;
	}
	
	/**
	 * Sobrecarga de {@link #setUsuarioLogin(UsuarioLogin)} em razão de PJEII-2726.
	 * 
	 * @param pessoaFisicaEspecializada a pessoa especializada a ser atribuída.
	 */
	public void setUsuarioLogin(PessoaFisicaEspecializada pessoaFisicaEspecializada){
		if(pessoaFisicaEspecializada != null) {
			setUsuarioLogin(pessoaFisicaEspecializada.getPessoa());
		} else {
			setUsuarioLogin((UsuarioLogin)null);
		}
	}

	@Column(name = "ds_motivo", length = 200)
	@Length(max = 200)
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_alteracao")
	public Date getDtAlteracao() {
		return dtAlteracao;
	}

	public void setDtAlteracao(Date dtAlteracao) {
		this.dtAlteracao = dtAlteracao;
	}

	@Column(name = "in_apreciado", nullable = false)
	public Boolean getApreciado() {
		return apreciado;
	}

	public void setApreciado(Boolean apreciado) {
		this.apreciado = apreciado;
	}

	@Column(name = "in_situacao_segredo", length = 1)
	@Enumerated(EnumType.STRING)
	public SegredoStatusEnum getStatus() {
		return status;
	}

	public void setStatus(SegredoStatusEnum status) {
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
		if (!(obj instanceof ProcessoSegredo)) {
			return false;
		}
		ProcessoSegredo other = (ProcessoSegredo) obj;
		if (getIdProcessoSegredo() != other.getIdProcessoSegredo()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoSegredo();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoSegredo> getEntityClass() {
		return ProcessoSegredo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoSegredo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
