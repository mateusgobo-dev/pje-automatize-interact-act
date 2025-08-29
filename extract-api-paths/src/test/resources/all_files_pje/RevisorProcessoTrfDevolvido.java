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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;


@Entity
@Table(name = "tb_revisor_proc_devolvido")
@org.hibernate.annotations.GenericGenerator(name = "gen_revisor_proc_devolvido", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_revisor_proc_devolvido"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RevisorProcessoTrfDevolvido implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RevisorProcessoTrfDevolvido,Integer> {

	private static final long serialVersionUID = 1L;

	private int idRevisorProcessoTrfDevolvido;
	private RevisorProcessoTrf revisorProcessoTrf;
	private Usuario usuarioDevolucao;
	private String motivo;

	public RevisorProcessoTrfDevolvido() {
	}

	@Id
	@GeneratedValue(generator = "gen_revisor_proc_devolvido")
	@Column(name = "id_revisor_processo_devolvido", nullable = false)
	public int getIdRevisorProcessoTrfDevolvido() {
		return idRevisorProcessoTrfDevolvido;
	}

	public void setIdRevisorProcessoTrfDevolvido(int idRevisorProcessoTrfDevolvido) {
		this.idRevisorProcessoTrfDevolvido = idRevisorProcessoTrfDevolvido;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_revisor_processo")
	public RevisorProcessoTrf getRevisorProcessoTrf() {
		return revisorProcessoTrf;
	}

	public void setRevisorProcessoTrf(RevisorProcessoTrf revisorProcessoTrf) {
		this.revisorProcessoTrf = revisorProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_devolucao")
	public Usuario getUsuarioDevolucao() {
		return usuarioDevolucao;
	}

	public void setUsuarioDevolucao(Usuario usuarioDevolucao) {
		this.usuarioDevolucao = usuarioDevolucao;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_motivo")
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RevisorProcessoTrfDevolvido)) {
			return false;
		}
		RevisorProcessoTrfDevolvido other = (RevisorProcessoTrfDevolvido) obj;
		if (getIdRevisorProcessoTrfDevolvido() != other.getIdRevisorProcessoTrfDevolvido()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRevisorProcessoTrfDevolvido();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RevisorProcessoTrfDevolvido> getEntityClass() {
		return RevisorProcessoTrfDevolvido.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRevisorProcessoTrfDevolvido());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
