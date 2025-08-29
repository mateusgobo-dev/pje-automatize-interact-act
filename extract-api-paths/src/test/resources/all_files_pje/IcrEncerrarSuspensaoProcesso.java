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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.enums.MotivoEncerramentoSuspensaoEnum;

@Entity
@Table(name = "tb_icr_encerrar_susp_proc")
@PrimaryKeyJoinColumn(name = "id_icr_encerrar_susp_processo")
public class IcrEncerrarSuspensaoProcesso extends InformacaoCriminalRelevante implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	private MotivoEncerramentoSuspensaoEnum inMotivoEncerramentoSuspensao;
	private Date dtDecisaoEncerramento;
	private String justificativa;
	private InformacaoCriminalRelevante icrAfetada;
	private static TipoIcrEnum[] tiposDeIcrAceitos = {TipoIcrEnum.SUS};

	public IcrEncerrarSuspensaoProcesso(){
		//
	}

	public IcrEncerrarSuspensaoProcesso(InformacaoCriminalRelevante icr){
		super(icr);
	}

	@Column(name = "ds_justificativa", length = 400)
	@Length(min = 0, max = 400)
	public String getJustificativa(){
		return justificativa;
	}

	public void setJustificativa(String justificativa){
		this.justificativa = justificativa;
	}

	@Column(name = "ds_motivo_encerramento", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.TipoMotivoEncerramentoType")
	public MotivoEncerramentoSuspensaoEnum getInMotivoEncerramentoSuspensao(){
		return inMotivoEncerramentoSuspensao;
	}

	public void setInMotivoEncerramentoSuspensao(MotivoEncerramentoSuspensaoEnum inMotivoEncerramentoSuspensao){
		this.inMotivoEncerramentoSuspensao = inMotivoEncerramentoSuspensao;
	}

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "dt_publicacao", nullable = false)
	public Date getDtDecisaoEncerramento(){
		return dtDecisaoEncerramento;
	}

	public void setDtDecisaoEncerramento(Date dtDecisaoEncerramento){
		this.dtDecisaoEncerramento = dtDecisaoEncerramento;
	}

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "id_icr_vinculada", nullable = false)
	public InformacaoCriminalRelevante getIcrAfetada(){
		return icrAfetada;
	}

	public void setIcrAfetada(InformacaoCriminalRelevante icrAfetada){
		this.icrAfetada = icrAfetada;
	}

	@Transient
	public static TipoIcrEnum[] getTiposDeIcrAceitos(){
		return tiposDeIcrAceitos;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDtDecisaoEncerramento() == null) ? 0 : getDtDecisaoEncerramento().hashCode());
		result = prime * result
			+ ((getInMotivoEncerramentoSuspensao() == null) ? 0 : inMotivoEncerramentoSuspensao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof IcrEncerrarSuspensaoProcesso))
			return false;
		IcrEncerrarSuspensaoProcesso other = (IcrEncerrarSuspensaoProcesso) obj;
		if (getDtDecisaoEncerramento() == null){
			if (other.getDtDecisaoEncerramento() != null)
				return false;
		}
		else if (!getDtDecisaoEncerramento().equals(other.getDtDecisaoEncerramento()))
			return false;
		if (getInMotivoEncerramentoSuspensao() == null){
			if (other.getInMotivoEncerramentoSuspensao() != null)
				return false;
		}
		else if (!getInMotivoEncerramentoSuspensao().equals(other.getInMotivoEncerramentoSuspensao()))
			return false;
		return true;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrEncerrarSuspensaoProcesso.class;
	}
}
