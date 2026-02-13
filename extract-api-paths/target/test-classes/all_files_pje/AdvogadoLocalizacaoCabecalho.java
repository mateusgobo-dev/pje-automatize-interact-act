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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.editor.Cabecalho;

@Entity
@Table(name = "tb_adv_loc_cab")
@org.hibernate.annotations.GenericGenerator(name = "gen_adv_loc_cab", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_adv_loc_cab"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AdvogadoLocalizacaoCabecalho implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AdvogadoLocalizacaoCabecalho,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAdvogadoLocalizacaoCabecalho;
	private Cabecalho cabecalho;
	private PessoaFisica usuarioCriacao;
	private Localizacao localizacao;
	private Date dtCriacao;
	private Boolean ativo;

	@Id
	@Column(name="id_adv_loc_cab")
	@GeneratedValue(generator="gen_adv_loc_cab")
	public Integer getIdAdvogadoLocalizacaoCabecalho() {
		return idAdvogadoLocalizacaoCabecalho;
	}

	public void setIdAdvogadoLocalizacaoCabecalho(
			Integer idAdvogadoLocalizacaoCabecalho) {
		this.idAdvogadoLocalizacaoCabecalho = idAdvogadoLocalizacaoCabecalho;
	}

	@ManyToOne
	@JoinColumn(name="id_cabecalho")
	public Cabecalho getCabecalho() {
		return cabecalho;
	}

	public void setCabecalho(Cabecalho cabecalho) {
		this.cabecalho = cabecalho;
	}

	@ManyToOne
	@JoinColumn(name="id_usuario_criacao")
	public PessoaFisica getUsuarioCriacao() {
		return usuarioCriacao;
	}

	public void setUsuarioCriacao(PessoaFisica usuarioCriacao) {
		this.usuarioCriacao = usuarioCriacao;
	}

	@ManyToOne
	@JoinColumn(name="id_localizacao")
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name="dt_criacao")
	public Date getDtCriacao() {
		return dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	@Column(name="in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AdvogadoLocalizacaoCabecalho> getEntityClass() {
		return AdvogadoLocalizacaoCabecalho.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAdvogadoLocalizacaoCabecalho();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
