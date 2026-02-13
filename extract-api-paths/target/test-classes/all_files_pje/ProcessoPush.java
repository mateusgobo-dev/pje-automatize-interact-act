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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "tb_processo_push")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_push", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_push"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoPush implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoPush,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idProcessoPush;
	private Pessoa pessoa;
	private PessoaPush pessoaPush;
	private ProcessoTrf processoTrf;
	private String dsObservacao;
	private Date dtInclusao;
	private Date dtExclusao;

	public ProcessoPush() {	
	}
	
	public ProcessoPush(Pessoa pessoa, ProcessoTrf processoTrf) {
		this.pessoa = pessoa;
		this.processoTrf = processoTrf;
	}

	@Id
	@GeneratedValue(generator = "gen_processo_push")
	@Column(name = "id_processo_push", nullable = false)
	public Integer getIdProcessoPush() {
		return idProcessoPush;
	}

	public void setIdProcessoPush(Integer idProcessoPush) {
		this.idProcessoPush = idProcessoPush;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)}.
	 * 
	 * @param pessoa A pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_push")
	public PessoaPush getPessoaPush() {
		return pessoaPush;
	}

	public void setPessoaPush(PessoaPush pessoaPush) {
		this.pessoaPush = pessoaPush;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		if (processoTrf == null) {
			processoTrf = new ProcessoTrf();
		}
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Column(name = "ds_observacao")
	public String getDsObservacao() {
		return dsObservacao;
	}

	public void setDsObservacao(String dsObservacao) {
		this.dsObservacao = dsObservacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inclusao")
	public Date getDtInclusao() {
		return dtInclusao;
	}

	public void setDtInclusao(Date dtInclusao) {
		this.dtInclusao = dtInclusao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_exclusao")
	public Date getDtExclusao() {
		return dtExclusao;
	}

	public void setDtExclusao(Date dtExclusao) {
		this.dtExclusao = dtExclusao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoPush> getEntityClass() {
		return ProcessoPush.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoPush();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
