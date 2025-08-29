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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_peticao_cl_aplicacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_peticao_classe_judicial", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_peticao_classe_judicial"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PeticaoClasseAplicacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PeticaoClasseAplicacao,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPeticaoClasseAplicacao;
	private Peticao peticao;
	private ClasseAplicacao classeAplicacao;
	private Boolean ativo;

	public PeticaoClasseAplicacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_peticao_classe_judicial")
	@Column(name = "id_peticao_classe_aplicacao", unique = true, nullable = false)
	public int getIdPeticaoClasseAplicacao() {
		return this.idPeticaoClasseAplicacao;
	}

	public void setIdPeticaoClasseAplicacao(int idPeticaoClasseAplicacao) {
		this.idPeticaoClasseAplicacao = idPeticaoClasseAplicacao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_peticao", nullable = false)
	@NotNull
	public Peticao getPeticao() {
		return this.peticao;
	}

	public void setPeticao(Peticao peticao) {
		this.peticao = peticao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_classe_aplicacao", nullable = false)
	@NotNull
	public ClasseAplicacao getClasseAplicacao() {
		return this.classeAplicacao;
	}

	public void setClasseAplicacao(ClasseAplicacao classeAplicacao) {
		this.classeAplicacao = classeAplicacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PeticaoClasseAplicacao)) {
			return false;
		}
		PeticaoClasseAplicacao other = (PeticaoClasseAplicacao) obj;
		if (getIdPeticaoClasseAplicacao() != other.getIdPeticaoClasseAplicacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPeticaoClasseAplicacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PeticaoClasseAplicacao> getEntityClass() {
		return PeticaoClasseAplicacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPeticaoClasseAplicacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
