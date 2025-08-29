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
package br.jus.pje.jt.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "tb_aud_verba_importacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_aud_verba_importacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aud_verba_importacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AudVerbaImportacao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AudVerbaImportacao,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idVerbaImportacao;
	@XmlTransient
	private AudImportacao audImportacao;
	private String nomeVerba;
	private Double valorVerba;

	@Id
	@Column(name = "id_aud_verba_importacao", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_aud_verba_importacao")
	public Integer getIdVerbaImportacao() {
		return idVerbaImportacao;
	}

	public void setIdVerbaImportacao(Integer idVerbaImportacao) {
		this.idVerbaImportacao = idVerbaImportacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aud_importacao", nullable = false)
	public AudImportacao getAudImportacao() {
		return audImportacao;
	}

	public void setAudImportacao(AudImportacao audImportacao) {
		this.audImportacao = audImportacao;
	}

	@Column(name = "nm_verba")
	public String getNomeVerba() {
		return nomeVerba;
	}

	public void setNomeVerba(String nomeVerba) {
		this.nomeVerba = nomeVerba;
	}

	@Column(name = "vl_verba")
	public Double getValorVerba() {
		return valorVerba;
	}

	public void setValorVerba(Double valorVerba) {
		this.valorVerba = valorVerba;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.audImportacao = (AudImportacao) parent;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AudVerbaImportacao> getEntityClass() {
		return AudVerbaImportacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdVerbaImportacao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
