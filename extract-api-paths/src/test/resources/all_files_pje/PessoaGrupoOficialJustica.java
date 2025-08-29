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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = PessoaGrupoOficialJustica.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pess_grupo_ofic_justica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pess_grupo_ofic_justica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaGrupoOficialJustica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaGrupoOficialJustica,Integer> {

	public static final String TABLE_NAME = "tb_pess_gpo_oficial_jstica";
	private static final long serialVersionUID = 1L;
	private Integer idPessoaGrupoOficialJustica;
	private PessoaFisica pessoa;
	private GrupoOficialJustica grupoOficialJustica;
	private int qtdProcessos;
	private Boolean ativo;
	
	private List<ProcessoExpedienteCentralMandado> processoExpedienteCentralMandadosList = new ArrayList<ProcessoExpedienteCentralMandado>(
			0);

	@Id
	@GeneratedValue(generator = "gen_pess_grupo_ofic_justica")
	@Column(name = "id_pess_grupo_oficial_justica", unique = true, nullable = false)
	public Integer getIdPessoaGrupoOficialJustica() {
		return idPessoaGrupoOficialJustica;
	}

	public void setIdPessoaGrupoOficialJustica(Integer idPessoaGrupoOficialJustica) {
		this.idPessoaGrupoOficialJustica = idPessoaGrupoOficialJustica;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public PessoaFisica getPessoa() {
		return pessoa;
	}

	public void setPessoa(PessoaFisica pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		setPessoa(pessoa != null ? pessoa.getPessoa() : null);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_grupo_oficial_justica", nullable = false)
	@NotNull
	public GrupoOficialJustica getGrupoOficialJustica() {
		return grupoOficialJustica;
	}

	public void setGrupoOficialJustica(GrupoOficialJustica grupoOficialJustica) {
		this.grupoOficialJustica = grupoOficialJustica;
	}

	@OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy = "pessoaGrupoOficialJustica")
	public List<ProcessoExpedienteCentralMandado> getProcessoExpedienteCentralMandadosList() {
		return processoExpedienteCentralMandadosList;
	}

	public void setProcessoExpedienteCentralMandadosList(
			List<ProcessoExpedienteCentralMandado> processoExpedienteCentralMandadosList) {
		this.processoExpedienteCentralMandadosList = processoExpedienteCentralMandadosList;
	}

	@Transient
	public int getQtdProcessos() {
		return qtdProcessos;
	}

	public void setQtdProcessos(int qtdProcessos) {
		this.qtdProcessos = qtdProcessos;
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
		return pessoa.getNomeParte();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdPessoaGrupoOficialJustica() == null) {
			return false;
		}
		if (!(obj instanceof PessoaGrupoOficialJustica)) {
			return false;
		}
		PessoaGrupoOficialJustica other = (PessoaGrupoOficialJustica) obj;
		if (!idPessoaGrupoOficialJustica.equals(other.getIdPessoaGrupoOficialJustica())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaGrupoOficialJustica();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaGrupoOficialJustica> getEntityClass() {
		return PessoaGrupoOficialJustica.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPessoaGrupoOficialJustica();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
