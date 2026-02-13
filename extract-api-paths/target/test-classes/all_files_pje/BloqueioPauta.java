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

import java.text.SimpleDateFormat;
import java.util.Date;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_bloqueio_pauta")
@org.hibernate.annotations.GenericGenerator(name = "gen_bloqueio_pauta", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_bloqueio_pauta"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class BloqueioPauta implements IEntidade<BloqueioPauta, Integer> {

	private static final long serialVersionUID = 1L;
	private int idBloqueioPauta;
	private Sala salaAudiencia;
	private Date dtInicial;
	private Date dtFinal;
	private Boolean ativo;
	private String descricao;

	@Id
	@GeneratedValue(generator = "gen_bloqueio_pauta")
	@Column(name = "id_bloqueio_pauta", unique = true, nullable = false)
	public int getIdBloqueioPauta() {
		return idBloqueioPauta;
	}

	public void setIdBloqueioPauta(int idBloqueioPauta) {
		this.idBloqueioPauta = idBloqueioPauta;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sala")
	@NotNull
	public Sala getSalaAudiencia() {
		return salaAudiencia;
	}

	public void setSalaAudiencia(Sala salaAudiencia) {
		this.salaAudiencia = salaAudiencia;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicial", nullable = false)
	@NotNull
	public Date getDtInicial() {
		return dtInicial;
	}

	public void setDtInicial(Date dtInicial) {
		this.dtInicial = dtInicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_final", nullable = false)
	@NotNull
	public Date getDtFinal() {
		return dtFinal;
	}

	public void setDtFinal(Date dtFinal) {
		this.dtFinal = dtFinal;
	}

	@Column(name = "bl_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "ds_descricao", unique = false, nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	@Transient
	public String getDtInicialFormatada() {
		return getDataFormatada(getDtInicial());
	}

	@Transient
	public String getDtFinalFormatada() {
		return getDataFormatada(getDtFinal());
	}

	@Transient
	private String getDataFormatada(Date data) {
		if (data == null) {
			return "";
		} else {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(data);
		}
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends BloqueioPauta> getEntityClass() {
		return BloqueioPauta.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdBloqueioPauta());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
