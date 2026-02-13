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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_assunto")
@org.hibernate.annotations.GenericGenerator(name = "gen_assunto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_assunto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Assunto implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Assunto,Integer> {

	private static final long serialVersionUID = 1L;

	private int idAssunto;
	private Assunto assuntoPai;
	private String codAssunto;
	private String assunto;
	private Boolean ativo = Boolean.TRUE;
	private Fluxo fluxo;
	private String caminhoCompleto;
	private List<Assunto> assuntoList = new ArrayList<Assunto>(0);

	public Assunto() {
	}

	@Id
	@GeneratedValue(generator = "gen_assunto")
	@Column(name = "id_assunto", unique = true, nullable = false)
	public int getIdAssunto() {
		return this.idAssunto;
	}

	public void setIdAssunto(int idAssunto) {
		this.idAssunto = idAssunto;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_pai")
	public Assunto getAssuntoPai() {
		return this.assuntoPai;
	}

	public void setAssuntoPai(Assunto assuntoPai) {
		this.assuntoPai = assuntoPai;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fluxo")
	public Fluxo getFluxo() {
		return this.fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}

	@Column(name = "cd_assunto", length = 30)
	@Length(max = 30)
	public String getCodAssunto() {
		return this.codAssunto;
	}

	public void setCodAssunto(String codAssunto) {
		this.codAssunto = codAssunto;
	}

	@Column(name = "ds_assunto", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getAssunto() {
		return this.assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "assuntoPai")
	public List<Assunto> getAssuntoList() {
		return this.assuntoList;
	}

	public void setAssuntoList(List<Assunto> assuntoList) {
		this.assuntoList = assuntoList;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_caminho_completo")
	public String getCaminhoCompleto() {
		return caminhoCompleto;
	}

	public void setCaminhoCompleto(String caminhoCompleto) {
		this.caminhoCompleto = caminhoCompleto;
	}

	@Override
	public String toString() {
		return assunto;
	}

	@Transient
	public List<Assunto> getListAssuntoAtePai() {
		List<Assunto> list = new ArrayList<Assunto>();
		Assunto assuntoPai = getAssuntoPai();
		while (assuntoPai != null) {
			list.add(assuntoPai);
			assuntoPai = assuntoPai.getAssuntoPai();
		}
		return list;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Assunto)) {
			return false;
		}
		Assunto other = (Assunto) obj;
		if (getIdAssunto() != other.getIdAssunto()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdAssunto();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Assunto> getEntityClass() {
		return Assunto.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAssunto());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
