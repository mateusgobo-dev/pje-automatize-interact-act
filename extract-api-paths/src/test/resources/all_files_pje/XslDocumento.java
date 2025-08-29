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
package br.jus.pje.nucleo.entidades.editor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = XslDocumento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_xsl_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_xsl_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class XslDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<XslDocumento,Integer> {

	public static final String TABLE_NAME = "tb_xsl_documento";
	private static final long serialVersionUID = 1L;
	
	private Integer idXslDocumento;
	private String nome;
	private String conteudo;
	private Boolean ativo;
	
	@Id
	@GeneratedValue(generator = "gen_xsl_documento")
	@Column(name = "id_xsl_documento", unique = true, nullable = false)
	public Integer getIdXslDocumento() {
		return idXslDocumento;
	}
	
	public void setIdXslDocumento(Integer idXslDocumento) {
		this.idXslDocumento = idXslDocumento;
	}
	
	@Column(name = "nm_xsl_documento", length = 100, nullable = false, unique = true)
	@Length(max = 100)
	@NotNull
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Column(name = "vl_xsl_documento", nullable = false)
	@NotNull
	public String getConteudo() {
		return conteudo;
	}
	
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	public String toString() {
		return nome;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdXslDocumento();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof XslDocumento))
			return false;
		if(getIdXslDocumento() == null){
			return false;
		}
		XslDocumento other = (XslDocumento) obj;
		if (!idXslDocumento.equals(other.getIdXslDocumento()))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends XslDocumento> getEntityClass() {
		return XslDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdXslDocumento();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
