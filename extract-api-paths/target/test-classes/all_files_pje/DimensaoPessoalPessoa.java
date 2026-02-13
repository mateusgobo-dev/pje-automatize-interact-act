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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@SuppressWarnings("serial")
@Entity
@Table(name = "tb_dpessoal_pessoa")
@org.hibernate.annotations.GenericGenerator(name = "gen_dpessoal_pessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dpessoal_pessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DimensaoPessoalPessoa implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DimensaoPessoalPessoa,Integer> {

	private Integer idDimensaoPessoalPessoa;

	private AssociacaoDimensaoPessoalEnum tipoAssociacao;

	private ProcessoParteParticipacaoEnum polo;

	private Pessoa pessoa;

	private DimensaoPessoal dimensaoPessoal;

	@Id
	@GeneratedValue(generator = "gen_dpessoal_pessoa")
	@Column(name = "id_dpessoal_pessoa", unique = true, nullable = false)
	public Integer getIdDimensaoPessoalPessoa() {
		return idDimensaoPessoalPessoa;
	}

	public void setIdDimensaoPessoalPessoa(Integer idDimensaoPessoalPessoa) {
		this.idDimensaoPessoalPessoa = idDimensaoPessoalPessoa;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "tp_associacao")
	public AssociacaoDimensaoPessoalEnum getTipoAssociacao() {
		return tipoAssociacao;
	}

	public void setTipoAssociacao(AssociacaoDimensaoPessoalEnum tipoAssociacao) {
		this.tipoAssociacao = tipoAssociacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_participacao")
	public ProcessoParteParticipacaoEnum getPolo() {
		return polo;
	}

	public void setPolo(ProcessoParteParticipacaoEnum polo) {
		this.polo = polo;
	}

	@ManyToOne
	@JoinColumn(name = "id_dimensao_pessoal")
	public DimensaoPessoal getDimensaoPessoal() {
		return dimensaoPessoal;
	}

	public void setDimensaoPessoal(DimensaoPessoal dimensaoPessoal) {
		this.dimensaoPessoal = dimensaoPessoal;
	}

	@ManyToOne
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DimensaoPessoalPessoa> getEntityClass() {
		return DimensaoPessoalPessoa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdDimensaoPessoalPessoa();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
