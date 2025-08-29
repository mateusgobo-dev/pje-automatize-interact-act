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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = AreaOficial.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_area_oficial", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_area_oficial"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AreaOficial implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AreaOficial,Integer> {

	public static final String TABLE_NAME = "tb_area_oficial";
	private static final long serialVersionUID = 4920838013800447602L;

	private int idAreaOficial;
	private Area area;
	private OficialJusticaCentralMandado oficialJusticaCentralMandado;
	private Boolean permissaoParaTransferir;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_area_oficial")
	@Column(name = "id_area_oficial", unique = true, nullable = false)
	public int getIdAreaOficial() {
		return idAreaOficial;
	}

	public void setIdAreaOficial(int idAreaOficial) {
		this.idAreaOficial = idAreaOficial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_area", nullable = false)
	@NotNull
	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ofcl_jstca_central_mandado", nullable = false)
	@NotNull
	public OficialJusticaCentralMandado getOficialJusticaCentralMandado() {
		return oficialJusticaCentralMandado;
	}

	public void setOficialJusticaCentralMandado(OficialJusticaCentralMandado oficialJusticaCentralMandado) {
		this.oficialJusticaCentralMandado = oficialJusticaCentralMandado;
	}

	@Column(name = "in_perm_transferencia", nullable = false)
	@NotNull
	public Boolean getPermissaoParaTransferir() {
		return permissaoParaTransferir;
	}

	public void setPermissaoParaTransferir(Boolean permissaoParaTransferir) {
		this.permissaoParaTransferir = permissaoParaTransferir;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public static String getTableName() {
		return TABLE_NAME;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AreaOficial> getEntityClass() {
		return AreaOficial.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAreaOficial());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
