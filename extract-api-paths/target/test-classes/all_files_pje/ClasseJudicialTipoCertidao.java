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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.APTEnum;

/*
 * Esta classe esta foi mapeada em outras classe "TipoCertidaoClasseJudicialHome" e 
 * "AbstractTipoCertidaoClasseJudicialHome")
 * Por ela esta sendo usada em outro cadastro "Tipo Certidão"
 * */

@Entity
@Table(name = ClasseJudicialTipoCertidao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_cl_judicial_tp_certidao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_cl_judicial_tp_certidao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ClasseJudicialTipoCertidao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ClasseJudicialTipoCertidao,Integer> {

	public static final String TABLE_NAME = "tb_cl_judicial_tp_certidao";
	private static final long serialVersionUID = 1L;

	private int idClasseJudicialTipoCertidao;
	private ClasseJudicial classeJudicial;
	private TipoCertidao tipoCertidao;
	private APTEnum poloCertidao;

	public ClasseJudicialTipoCertidao() {
	}

	@Id
	@GeneratedValue(generator = "gen_cl_judicial_tp_certidao")
	@Column(name = "id_classe_judicial_tp_certidao", unique = true, nullable = false)
	public int getIdClasseJudicialTipoCertidao() {
		return this.idClasseJudicialTipoCertidao;
	}

	public void setIdClasseJudicialTipoCertidao(int idClasseJudicialTipoCertidao) {
		this.idClasseJudicialTipoCertidao = idClasseJudicialTipoCertidao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return this.classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_certidao", nullable = false)
	@NotNull
	public TipoCertidao getTipoCertidao() {
		return this.tipoCertidao;
	}

	public void setTipoCertidao(TipoCertidao tipoCertidao) {
		this.tipoCertidao = tipoCertidao;
	}

	@Column(name = "in_polo", length = 1)
	@Enumerated(EnumType.STRING)
	public APTEnum getPoloCertidao() {
		return poloCertidao;
	}

	public void setPoloCertidao(APTEnum poloCertidao) {
		this.poloCertidao = poloCertidao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ClasseJudicialTipoCertidao)) {
			return false;
		}
		ClasseJudicialTipoCertidao other = (ClasseJudicialTipoCertidao) obj;
		if (getIdClasseJudicialTipoCertidao() != other.getIdClasseJudicialTipoCertidao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdClasseJudicialTipoCertidao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ClasseJudicialTipoCertidao> getEntityClass() {
		return ClasseJudicialTipoCertidao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdClasseJudicialTipoCertidao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
