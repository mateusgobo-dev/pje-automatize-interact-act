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
import javax.persistence.Table;

@Entity
@Table(name = PessoaExpediente.TABLE_NAME)
public class PessoaExpediente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaExpediente,Integer> {

	public static final String TABLE_NAME = "tb_pessoa_exp";
	private static final long serialVersionUID = 1L;

	private int idPessoaExpediente;
	private ProcessoExpediente processoExpediente;
	private List<PessoaExpedienteDocIdentificacao> pessoaExpDocIdentificacaoList = new ArrayList<PessoaExpedienteDocIdentificacao>();
	private List<PessoaExpedienteEndereco> pessoaExpedienteEnderecoList = new ArrayList<PessoaExpedienteEndereco>();
	private List<PessoaExpedienteMeioContato> pessoaExpedienteMeioContatoList = new ArrayList<PessoaExpedienteMeioContato>();

	 @Id
	 @GeneratedValue(generator = "gen_pessoa_exp")
	 @org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_exp", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_exp"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	 @Column(name = "id_pessoa_exp")
	 public int getIdPessoaExpediente() {
	 return this.idPessoaExpediente;
	 }
	
	 public void setIdPessoaExpediente(int idPessoaExpediente) {
	 this.idPessoaExpediente = idPessoaExpediente;
	 }

	@ManyToOne
	@JoinColumn(name = "id_processo_expediente")
	public ProcessoExpediente getProcessoExpediente() {
		return processoExpediente;
	}

	public void setProcessoExpediente(ProcessoExpediente processoExpediente) {
		this.processoExpediente = processoExpediente;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "pessoaExpediente")
	public List<PessoaExpedienteDocIdentificacao> getPessoaExpDocIdentificacaoList() {
		return pessoaExpDocIdentificacaoList;
	}

	public void setPessoaExpDocIdentificacaoList(List<PessoaExpedienteDocIdentificacao> pessoaExpDocIdentificacaoList) {
		this.pessoaExpDocIdentificacaoList = pessoaExpDocIdentificacaoList;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "pessoaExpediente")
	public List<PessoaExpedienteEndereco> getPessoaExpedienteEnderecoList() {
		return pessoaExpedienteEnderecoList;
	}

	public void setPessoaExpedienteEnderecoList(List<PessoaExpedienteEndereco> pessoaExpedienteEnderecoList) {
		this.pessoaExpedienteEnderecoList = pessoaExpedienteEnderecoList;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "pessoaExpediente")
	public List<PessoaExpedienteMeioContato> getPessoaExpedienteMeioContatoList() {
		return pessoaExpedienteMeioContatoList;
	}

	public void setPessoaExpedienteMeioContatoList(List<PessoaExpedienteMeioContato> pessoaExpedienteMeioContatoList) {
		this.pessoaExpedienteMeioContatoList = pessoaExpedienteMeioContatoList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaExpediente> getEntityClass() {
		return PessoaExpediente.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaExpediente());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
