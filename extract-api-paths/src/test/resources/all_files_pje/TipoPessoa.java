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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_tipo_pessoa")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_pessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_pessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoPessoa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoPessoa,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoPessoa;
	private String codTipoPessoa;
	private String tipoPessoa;
	private Boolean ativo;
	private TipoPessoa tipoPessoaSuperior;

	private List<TipoPessoa> tipoPessoaList = new ArrayList<TipoPessoa>(0);
	private List<TipoPessoaQualificacao> tipoPessoaQualificacaoList = new ArrayList<TipoPessoaQualificacao>(0);
	private List<TipoProcessoDocumentoTipoPessoa> tipoProcessoDocumentoTipoPessoaList = new ArrayList<TipoProcessoDocumentoTipoPessoa>(
			0);
	private List<Pessoa> pessoaList = new ArrayList<Pessoa>(0);
	private List<TipoProcessoDocumento> tipoProcessoDocumentoList = new ArrayList<TipoProcessoDocumento>(0);
	private Integer prazoExpedienteAutomatico;

	public TipoPessoa() {
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_pessoa")
	@Column(name = "id_tipo_pessoa", unique = true, nullable = false)
	public int getIdTipoPessoa() {
		return this.idTipoPessoa;
	}

	public void setIdTipoPessoa(int idTipoPessoa) {
		this.idTipoPessoa = idTipoPessoa;
	}

	@Column(name = "cd_tipo_pessoa", length = 15)
	@Length(max = 15)
	public String getCodTipoPessoa() {
		return codTipoPessoa;
	}

	public void setCodTipoPessoa(String codTipoPessoa) {
		this.codTipoPessoa = codTipoPessoa;
	}

	@Column(name = "ds_tipo_pessoa", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getTipoPessoa() {
		return this.tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_pessoa_superior")
	public TipoPessoa getTipoPessoaSuperior() {
		return this.tipoPessoaSuperior;
	}

	public void setTipoPessoaSuperior(TipoPessoa tipoPessoaSuperior) {
		this.tipoPessoaSuperior = tipoPessoaSuperior;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "tipoPessoaSuperior")
	@OrderBy("tipoPessoa")
	public List<TipoPessoa> getTipoPessoaList() {
		return this.tipoPessoaList;
	}

	public void setTipoPessoaList(List<TipoPessoa> tipoPessoaList) {
		this.tipoPessoaList = tipoPessoaList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoPessoa")
	public List<TipoPessoaQualificacao> getTipoPessoaQualificacaoList() {
		return this.tipoPessoaQualificacaoList;
	}

	public void setTipoPessoaQualificacaoList(List<TipoPessoaQualificacao> tipoPessoaQualificacaoList) {
		this.tipoPessoaQualificacaoList = tipoPessoaQualificacaoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "tipoPessoa")
	public List<TipoProcessoDocumentoTipoPessoa> getTipoProcessoDocumentoTipoPessoaList() {
		return this.tipoProcessoDocumentoTipoPessoaList;
	}

	public void setTipoProcessoDocumentoTipoPessoaList(
			List<TipoProcessoDocumentoTipoPessoa> tipoProcessoDocumentoTipoPessoaList) {
		this.tipoProcessoDocumentoTipoPessoaList = tipoProcessoDocumentoTipoPessoaList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "tipoPessoa")
	public List<Pessoa> getPessoaList() {
		return this.pessoaList;
	}

	public void setPessoaList(List<Pessoa> pessoaList) {
		this.pessoaList = pessoaList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_tp_proc_doc_tipo_pessoa", joinColumns = { @JoinColumn(name = "id_tipo_pessoa", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_processo_documento", nullable = false, updatable = false) })
	public List<TipoProcessoDocumento> getTipoProcessoDocumentoList() {
		return this.tipoProcessoDocumentoList;
	}

	public void setTipoProcessoDocumentoList(List<TipoProcessoDocumento> tipoProcessoDocumentoList) {
		this.tipoProcessoDocumentoList = tipoProcessoDocumentoList;
	}

	@Column(name = "tp_prazo_expediente_automatico")
	public Integer getPrazoExpedienteAutomatico() {
		return prazoExpedienteAutomatico;
	}

	public void setPrazoExpedienteAutomatico(Integer prazoExpedienteAutomatico) {
		this.prazoExpedienteAutomatico = prazoExpedienteAutomatico;
	}
	
	@Transient
	public boolean isChildrenOf(TipoPessoa tp){
		if(tp == null){
			return false;
		}
		TipoPessoa ancestor = this.getTipoPessoaSuperior();
		while(ancestor != null){
			if(tp.equals(ancestor)){
				return true;
			}else{
				ancestor = ancestor.getTipoPessoaSuperior();
			}
		}
		return false;
	}
	
	@Transient
	public boolean isAncestorOf(TipoPessoa tp){
		if(tp == null){
			return false;
		}
		for(TipoPessoa child: this.getTipoPessoaList()){
			if(child.equals(tp)){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return tipoPessoa;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoPessoa)) {
			return false;
		}
		TipoPessoa other = (TipoPessoa) obj;
		if (getIdTipoPessoa() != other.getIdTipoPessoa()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoPessoa();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoPessoa> getEntityClass() {
		return TipoPessoa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoPessoa());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
