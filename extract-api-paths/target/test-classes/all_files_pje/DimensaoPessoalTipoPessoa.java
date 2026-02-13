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
@javax.persistence.Cacheable(true)
@Table(name = "tb_dpessoal_tipopessoa")
@org.hibernate.annotations.GenericGenerator(name = "gen_dpessoal_tipopessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dpessoal_tipopessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DimensaoPessoalTipoPessoa implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DimensaoPessoalTipoPessoa,Integer> {

	private Integer idDimensaoPessoalTipoPessoa;

	private AssociacaoDimensaoPessoalEnum tipoAssociacao;

	private ProcessoParteParticipacaoEnum polo;

	private TipoPessoa tipoPessoa;

	private DimensaoPessoal dimensaoPessoal;

	@Id
	@GeneratedValue(generator = "gen_dpessoal_tipopessoa")
	@Column(name = "id_dpessoal_tipopessoa", unique = true, nullable = false)
	public Integer getIdDimensaoPessoalTipoPessoa() {
		return idDimensaoPessoalTipoPessoa;
	}

	public void setIdDimensaoPessoalTipoPessoa(Integer idDimensaoPessoalTipoPessoa) {
		this.idDimensaoPessoalTipoPessoa = idDimensaoPessoalTipoPessoa;
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
	@JoinColumn(name = "id_tipo_pessoa")
	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DimensaoPessoalTipoPessoa> getEntityClass() {
		return DimensaoPessoalTipoPessoa.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdDimensaoPessoalTipoPessoa();
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
