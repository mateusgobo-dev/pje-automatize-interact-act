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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;

@Entity
@Table(name = "tb_proc_doc_bin_pess_assin")
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_doc_bin_pess_assina", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_doc_bin_pess_assina"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
@IndexedEntity(
		value="assinaturas", 
		id="idProcessoDocumentoBinPessoaAssinatura", 
		owners={"processoDocumentoBin"},
		mappings={
				@Mapping(beanPath="pessoa.idPessoa", mappedPath="id_pessoa"),
				@Mapping(beanPath="pessoa.nome", mappedPath="signatario"),
				@Mapping(beanPath="dataAssinatura", mappedPath="data_assinatura"),
				@Mapping(beanPath="algoritmoDigest", mappedPath="algoritmo"),
				@Mapping(beanPath="assinaturaCMS", mappedPath="cms")
})
public class ProcessoDocumentoBinPessoaAssinatura implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoDocumentoBinPessoaAssinatura,Integer> {

	private static final long serialVersionUID = 808210719625703994L;
	private int idProcessoDocumentoBinPessoaAssinatura;
	private ProcessoDocumentoBin processoDocumentoBin;
	private Pessoa pessoa;
	private String assinatura;
	private Date dataAssinatura = new Date();
	private String algoritmoDigest = "MD5";
	private String certChain;
	private String nomePessoa;
	private Boolean assinaturaCMS = Boolean.FALSE;

	public ProcessoDocumentoBinPessoaAssinatura() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_doc_bin_pess_assina")
	@Column(name = "id_processo_doc_bin_pessoa", unique = true, nullable = false)
	public int getIdProcessoDocumentoBinPessoaAssinatura() {
		return idProcessoDocumentoBinPessoaAssinatura;
	}

	public void setIdProcessoDocumentoBinPessoaAssinatura(int idProcessoDocumentoBinPessoaAssinatura) {
		this.idProcessoDocumentoBinPessoaAssinatura = idProcessoDocumentoBinPessoaAssinatura;
	}

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento_bin", nullable = false)
	@NotNull
	public ProcessoDocumentoBin getProcessoDocumentoBin() {
		return processoDocumentoBin;
	}

	public void setProcessoDocumentoBin(ProcessoDocumentoBin processoDocumentoBin) {
		this.processoDocumentoBin = processoDocumentoBin;
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

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_assinatura", nullable = false)
	@NotNull
	public String getAssinatura() {
		return assinatura;
	}

	public void setAssinatura(String assinatura) {
		this.assinatura = assinatura;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_assinatura", nullable = false)
	@NotNull
	public Date getDataAssinatura() {
		return dataAssinatura;
	}

	public void setDataAssinatura(Date dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_cert_chain", nullable = false)
	@NotNull
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	@Column(name = "ds_nome_pessoa")
	public String getNomePessoa() {
		if (nomePessoa == null && pessoa != null)
			return pessoa.getNome();
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}
	
	/**
	 * Recupera o algoritmo utilizado para a realização da assinatura.
	 * 
	 * @return a descrição textual do algoritmo utilizado para a realização da assinatura, ou o OID respectivo.
	 */
	@Column(name="ds_algoritmo_digest", nullable=false)
	public String getAlgoritmoDigest() {
		return algoritmoDigest;
	}

	/**
	 * Associa a esta assinatura a descrição textual do algoritmo utilizado para sua produção, que pode ser
	 * o seu OID.
	 * 
	 * @param algoritmoDigest o algoritmo utilizado para produção da assinatura.
	 * 
	 */
	public void setAlgoritmoDigest(String algoritmoDigest) {
		this.algoritmoDigest = algoritmoDigest;
	}
	
	@Column(name="ds_assinatura_cms", nullable=false)
	public Boolean getAssinaturaCMS(){
		return this.assinaturaCMS;
	}
	
	public void setAssinaturaCMS(Boolean assinaturaCMS){
		this.assinaturaCMS = assinaturaCMS;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoDocumentoBinPessoaAssinatura)) {
			return false;
		}
		ProcessoDocumentoBinPessoaAssinatura other = (ProcessoDocumentoBinPessoaAssinatura) obj;
		if (getIdProcessoDocumentoBinPessoaAssinatura() != other.getIdProcessoDocumentoBinPessoaAssinatura()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoDocumentoBinPessoaAssinatura();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoDocumentoBinPessoaAssinatura> getEntityClass() {
		return ProcessoDocumentoBinPessoaAssinatura.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdProcessoDocumentoBinPessoaAssinatura();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
