/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justica
 *
 * A propriedade intelectual deste programa, como coigo-fonte
 * e como sua derivacao compilada, pertence a� Unicao Federal,
 * dependendo o uso parcial ou total de autorizacao expressa do
 * Conselho Nacional de Justica.
 *
 **/
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = PrazoMinimoMarcacaoAudiencia.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_prazo_min_marc_aud", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_prazo_min_marc_aud"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PrazoMinimoMarcacaoAudiencia implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PrazoMinimoMarcacaoAudiencia,Integer> {

	private static final long serialVersionUID = -4714764337694000333L;

	public static final String TABLE_NAME = "tb_prazo_min_marc_aud";

	private int idPrazoMinimoMarcacaoAudiencia;
	private OrgaoJulgador orgaoJulgador;
	private TipoAudiencia tipoAudiencia;
	private Integer prazo;
	
	public PrazoMinimoMarcacaoAudiencia() {
	}

	@Id
	@GeneratedValue(generator = "gen_prazo_min_marc_aud")
	@Column(name = "id_prazo_min_marc_aud", unique = true, nullable = false)
	public int getIdPrazoMinimoMarcacaoAudiencia() {
		return this.idPrazoMinimoMarcacaoAudiencia;
	}

	public void setIdPrazoMinimoMarcacaoAudiencia(int idPrazoMinimoMarcacaoAudiencia) {
		this.idPrazoMinimoMarcacaoAudiencia = idPrazoMinimoMarcacaoAudiencia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return this.orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_audiencia", nullable = false)
	@NotNull
	public TipoAudiencia getTipoAudiencia() {
		return this.tipoAudiencia;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	@Column(name = "vl_prazo", nullable = false)
	@NotNull
	public Integer getPrazo() {
		return this.prazo;
	}

	public void setPrazo(Integer prazo) {
		this.prazo = prazo;
	}


	@Override
	public String toString() {
		return getOrgaoJulgador() + " - " + getTipoAudiencia() + " - " + getPrazo();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPrazoMinimoMarcacaoAudiencia();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PrazoMinimoMarcacaoAudiencia))
			return false;
		PrazoMinimoMarcacaoAudiencia other = (PrazoMinimoMarcacaoAudiencia) obj;
		if (getIdPrazoMinimoMarcacaoAudiencia() != other.getIdPrazoMinimoMarcacaoAudiencia())
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PrazoMinimoMarcacaoAudiencia> getEntityClass() {
		return PrazoMinimoMarcacaoAudiencia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPrazoMinimoMarcacaoAudiencia());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
