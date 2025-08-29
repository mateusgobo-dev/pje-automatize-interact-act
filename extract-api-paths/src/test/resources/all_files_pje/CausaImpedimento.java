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


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = CausaImpedimento.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_causa_impedimento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_causa_impedimento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CausaImpedimento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CausaImpedimento,Integer> {

	public static final String TABLE_NAME = "tb_causa_impedimento";
	private static final long serialVersionUID = 1L;

	private int idCausaImpedimento;
	private String descricaoCausaImpedimento;
	private String textoLei;
	private Boolean ativo;

	public CausaImpedimento() {
	}

	@Id
	@GeneratedValue(generator = "gen_causa_impedimento")
	@Column(name = "id_causa_impedimento", unique = true, nullable = false)
	public int getIdImpedimento() {
		return this.idCausaImpedimento;
	}

	public void setIdImpedimento(int idCausaImpedimento) {
		this.idCausaImpedimento = idCausaImpedimento;
	}

	@Column(name = "ds_causa_impedimento", nullable = false,  length = 30)
	@Length(max = 30) @NotNull
	public String getDescricaoCausaImpedimento() {
		return this.descricaoCausaImpedimento;
	}

	public void setDescricaoCausaImpedimento(String descricaoCausaImpedimento) {
		this.descricaoCausaImpedimento = descricaoCausaImpedimento;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_texto_lei", length = 200)
	public String getTextoLei() {
		return textoLei;
	}

	public void setTextoLei(String textoLei) {
		this.textoLei = textoLei;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String toString(){
		return this.descricaoCausaImpedimento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CausaImpedimento> getEntityClass() {
		return CausaImpedimento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdImpedimento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
