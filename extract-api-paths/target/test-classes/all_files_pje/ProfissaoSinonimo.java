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

import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_profissao_sinonimo")
@org.hibernate.annotations.GenericGenerator(name = "gen_profissao_sinonimo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_profissao_sinonimo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProfissaoSinonimo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProfissaoSinonimo,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProfissaoSinonimo;
	private String codCbo;
	private String sinonimo;
	private Boolean ativo;
	private Profissao profissao;

	public ProfissaoSinonimo() {
	}

	@Id
	@GeneratedValue(generator = "gen_profissao_sinonimo")
	@Column(name = "id_profissao_sinonimo", unique = true, nullable = false)
	public int getIdProfissaoSinonimo() {
		return this.idProfissaoSinonimo;
	}

	public void setIdProfissaoSinonimo(int idProfissaoSinonimo) {
		this.idProfissaoSinonimo = idProfissaoSinonimo;
	}

	@Column(name = "cd_profissao", unique = true, length = 15)
	@Length(max = 15)
	public String getCodCbo() {
		return this.codCbo;
	}

	public void setCodCbo(String codCbo) {
		this.codCbo = codCbo;
	}

	@Column(name = "ds_sinonimo", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getSinonimo() {
		return this.sinonimo;
	}

	public void setSinonimo(String sinonimo) {
		this.sinonimo = sinonimo.toUpperCase();
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
	public String toString() {
		return sinonimo + " (SINÔNIMO DE " + profissao.getProfissao() + ")";
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_profissao")
	public Profissao getProfissao() {
		return profissao;
	}

	public void setProfissao(Profissao profissao) {
		this.profissao = profissao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProfissaoSinonimo> getEntityClass() {
		return ProfissaoSinonimo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProfissaoSinonimo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
