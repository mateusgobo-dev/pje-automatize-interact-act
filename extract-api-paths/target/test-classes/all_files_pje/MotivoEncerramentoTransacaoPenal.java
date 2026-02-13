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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_mtvo_ence_trnscao_penal")
@org.hibernate.annotations.GenericGenerator(name = "gen_mot_encerr_trans_penal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_mot_encerr_trans_penal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MotivoEncerramentoTransacaoPenal implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<MotivoEncerramentoTransacaoPenal,Long> {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String descricao;
	private Boolean ativo;
	private String codigo;

	public static enum TipoMotivo {
		RVG, CTR;
		public Boolean equals(MotivoEncerramentoTransacaoPenal classeTipo) {
			if (classeTipo == null || classeTipo.getCodigo() == null || classeTipo.getCodigo().equals(""))
				return false;
			if (classeTipo.getCodigo().equals(name()))
				return true;
			return false;
		}
	}

	public boolean equals(TipoMotivo tipoMotivo) {
		return this.codigo.equals(tipoMotivo.name());
	}

	@Id
	@GeneratedValue(generator = "gen_mot_encerr_trans_penal")
	@Column(name = "id_mtvo_encrrmnto_trnsco_penal", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	@NotNull
	@Length(max = 60)
	@Column(name = "descricao_motivo", nullable = false)
	public String getDescricao() {
		return descricao;
	}

	@NotNull
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return ativo;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "codigo", nullable = false)
	public String getCodigo() {
		return codigo;
	}

	@Override
	public String toString() {
		return getDescricao();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MotivoEncerramentoTransacaoPenal> getEntityClass() {
		return MotivoEncerramentoTransacaoPenal.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
