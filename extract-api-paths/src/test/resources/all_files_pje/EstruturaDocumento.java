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
package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;

@Entity
@Table(name="tb_estrutura_documento")
@org.hibernate.annotations.GenericGenerator(name = "gen_estrutura_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_estrutura_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EstruturaDocumento implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<EstruturaDocumento,Integer> {
	
	private static final long serialVersionUID = 1L;
	
	private Integer idEstruturaDocumento;
	private String estruturaDocumento;
	private Cabecalho cabecalho;
	private Date dataCriacao;
	private Pessoa pessoaCriacao;
	private Boolean ativo;
	private XslDocumento xslDocumento;
	
	private List<EstruturaDocumentoTopico> estruturaDocumentoTopicoList = new ArrayList<EstruturaDocumentoTopico>();
	
	@Id
	@GeneratedValue(generator = "gen_estrutura_documento")
	@Column(name = "id_estrutura_documento", unique = true, nullable = false)
	public Integer getIdEstruturaDocumento() {
		return idEstruturaDocumento;
	}
	
	public void setIdEstruturaDocumento(Integer idModeloDocumentoEstruturado) {
		this.idEstruturaDocumento = idModeloDocumentoEstruturado;
	}
	
	@Column(name="ds_estrutura_documento", nullable = false, unique = true)
	public String getEstruturaDocumento() {
		return estruturaDocumento;
	}
	
	public void setEstruturaDocumento(String estruturaDocumento) {
		this.estruturaDocumento = estruturaDocumento;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cabecalho")
	public Cabecalho getCabecalho() {
		return cabecalho;
	}

	public void setCabecalho(Cabecalho cabecalho) {
		this.cabecalho = cabecalho;
	}
	
	@Column(name="dt_criacao", nullable = false)
	public Date getDataCriacao() {
		return dataCriacao;
	}
	
	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="id_pessoa_criacao", nullable = false)
	public Pessoa getPessoaCriacao() {
		return pessoaCriacao;
	}
	
	public void setPessoaCriacao(Pessoa pessoaCriacao) {
		this.pessoaCriacao = pessoaCriacao;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaCriacao(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaCriacao(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaCriacao(pessoa.getPessoa());
		} else {
			setPessoaCriacao((Pessoa)null);
		}
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="estruturaDocumento")
	@OrderBy(value="ordem")
	public List<EstruturaDocumentoTopico> getEstruturaDocumentoTopicoList() {
		return estruturaDocumentoTopicoList;
	}

	public void setEstruturaDocumentoTopicoList(
			List<EstruturaDocumentoTopico> modeloDocumentoEstruturadoTopicoList) {
		this.estruturaDocumentoTopicoList = modeloDocumentoEstruturadoTopicoList;
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="id_xsl_documento", nullable = false)
	public XslDocumento getXslDocumento() {
		return xslDocumento;
	}

	public void setXslDocumento(XslDocumento xslDocumento) {
		this.xslDocumento = xslDocumento;
	}
	
	@Override
	public String toString() {
		return estruturaDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EstruturaDocumento> getEntityClass() {
		return EstruturaDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdEstruturaDocumento();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
