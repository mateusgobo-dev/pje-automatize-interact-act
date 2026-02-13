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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_tempo_audienca_org_julg")
@org.hibernate.annotations.GenericGenerator(name = "gen_tempo_audiencia_org_julg", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tempo_audiencia_org_julg"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TempoAudienciaOrgaoJulgador implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<TempoAudienciaOrgaoJulgador,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTempoAudienciaOrgaoJulgador;
	private OrgaoJulgador orgaoJulgador;
	private TipoAudiencia tipoAudiencia;
	private Integer tempoAudiencia;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_tempo_audiencia_org_julg")
	@Column(name = "id_tempo_audencia_org_julgador")
	public Integer getIdTempoAudienciaOrgaoJulgador() {
		return idTempoAudienciaOrgaoJulgador;
	}

	public void setIdTempoAudienciaOrgaoJulgador(Integer idTempoAudienciaOrgaoJulgador) {
		this.idTempoAudienciaOrgaoJulgador = idTempoAudienciaOrgaoJulgador;
	}

	@ManyToOne()
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne()
	@JoinColumn(name = "id_tipo_audiencia")
	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	@Column(name = "nr_tempo_audiencia")
	public Integer getTempoAudiencia() {
		return tempoAudiencia;
	}

	public void setTempoAudiencia(Integer tempoAudiencia) {
		this.tempoAudiencia = tempoAudiencia;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TempoAudienciaOrgaoJulgador> getEntityClass() {
		return TempoAudienciaOrgaoJulgador.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdTempoAudienciaOrgaoJulgador();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
