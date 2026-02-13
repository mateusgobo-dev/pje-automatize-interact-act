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

@Entity
@Table(name = OjClasseTipoAudiencia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_oj_classe_tp_audiencia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_oj_classe_tp_audiencia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OjClasseTipoAudiencia implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<OjClasseTipoAudiencia,Integer>{

	private static final long serialVersionUID = 4666332113471411265L;

	public static final String TABLE_NAME = "tb_oj_classe_tp_audiencia";
	
	private Integer idOjClasseTipoAud;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private TipoAudiencia tipoAudiencia;
	private Date dtInicio;
	private Date dtFim;

	@Id
	@GeneratedValue(generator = "gen_oj_classe_tp_audiencia")
	@Column(name = "id_oj_classe_tipo_aud")
	public Integer getIdOjClasseTipoAud() {
		return idOjClasseTipoAud;
	}

	public void setIdOjClasseTipoAud(Integer idOjClasseTipoAud) {
		this.idOjClasseTipoAud = idOjClasseTipoAud;
	}

	@ManyToOne
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne
	@JoinColumn(name = "id_classe_judicial")
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_audiencia")
	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	@Column(name = "dt_inicio")
	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	@Column(name = "dt_fim")
	public Date getDtFim() {
		return dtFim;
	}

	public void setDtFim(Date dtFim) {
		this.dtFim = dtFim;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OjClasseTipoAudiencia> getEntityClass() {
		return OjClasseTipoAudiencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdOjClasseTipoAud();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
