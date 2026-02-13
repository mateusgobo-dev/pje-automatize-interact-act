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
package br.jus.pje.nucleo.entidades.lancadormovimento;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_elemento_dominio")
@org.hibernate.annotations.GenericGenerator(name = "gen_elemento_dominio", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_elemento_dominio"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ElementoDominio implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ElementoDominio,Long> {

	private static final long serialVersionUID = -1475466563718751613L;
	private Long idElementoDominio;
	private String valor;
	private String codigoGlossario;
	private Boolean ativo;
	private Dominio dominio;

	public ElementoDominio() {
	}

	@Id
	@GeneratedValue(generator = "gen_elemento_dominio")
	@Column(name = "id_elemento_dominio", nullable = false)
	public Long getIdElementoDominio() {
		return idElementoDominio;
	}

	public void setIdElementoDominio(Long idElementoDominio) {
		this.idElementoDominio = idElementoDominio;
	}

	@Column(name = "ds_valor", nullable = false, length = 200)
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Column(name = "cd_glossario")
	public String getCodigoGlossario() {
		return codigoGlossario;
	}

	public void setCodigoGlossario(String codigoGlossario) {
		this.codigoGlossario = codigoGlossario;
	}

	@Column(name = "in_ativo")
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_dominio", nullable = false)
	@ForeignKey(name = "id_dominio_elemento_fkey")
	public Dominio getDominio() {
		return dominio;
	}

	public void setDominio(Dominio dominio) {
		this.dominio = dominio;
	}

	@Override
	public String toString() {
		return getValor();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ElementoDominio> getEntityClass() {
		return ElementoDominio.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdElementoDominio();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
