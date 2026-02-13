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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;


@Entity
@Table(name = ComposicaoSessao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_composicao_sessao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_composicao_sessao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComposicaoSessao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComposicaoSessao,Integer> {

	public static final String TABLE_NAME = "tb_composicao_sessao";
	private static final long serialVersionUID = 1L;

	private int idComposicaoSessao;
	private SessaoJT sessao;
	private OrgaoJulgador orgaoJulgador;
	private Boolean presenteSessao = Boolean.TRUE;
	private PessoaMagistrado magistradoPresente;
	private PessoaMagistrado magistradoSubstituto;
	private Boolean presidente = Boolean.FALSE;
	
	public ComposicaoSessao() {
	}

	@Id
	@GeneratedValue(generator = "gen_composicao_sessao")
	@Column(name = "id_composicao_sessao", unique = true, nullable = false)
	public int getIdComposicaoSessao() {
		return this.idComposicaoSessao;
	}

	public void setIdComposicaoSessao(int idComposicaoSessao) {
		this.idComposicaoSessao = idComposicaoSessao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao", nullable = false)
	@NotNull
	public SessaoJT getSessao() {
		return sessao;
	}

	public void setSessao(SessaoJT sessao) {
		this.sessao = sessao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable = false)
	@NotNull
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	@Column(name = "in_presente_sessao", nullable = false)
	@NotNull
	public Boolean getPresenteSessao() {
		return presenteSessao;
	}

	public void setPresenteSessao(Boolean presenteSessao) {
		this.presenteSessao = presenteSessao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_presente")
	public PessoaMagistrado getMagistradoPresente() {
		return magistradoPresente;
	}

	public void setMagistradoPresente(PessoaMagistrado magistradoPresente) {
		this.magistradoPresente = magistradoPresente;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_substituto")
	public PessoaMagistrado getMagistradoSubstituto() {
		return magistradoSubstituto;
	}

	public void setMagistradoSubstituto(PessoaMagistrado magistradoSubstituto) {
		this.magistradoSubstituto = magistradoSubstituto;
	}
	
	@Column(name = "in_presidente", nullable = false)
	@NotNull
	public Boolean getPresidente() {
		return presidente;
	}

	public void setPresidente(Boolean presidente) {
		this.presidente = presidente;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdComposicaoSessao();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ComposicaoSessao)) {
			return false;
		}
		ComposicaoSessao other = (ComposicaoSessao) obj;
		if (getIdComposicaoSessao() != other.getIdComposicaoSessao()){
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ComposicaoSessao> getEntityClass() {
		return ComposicaoSessao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdComposicaoSessao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
