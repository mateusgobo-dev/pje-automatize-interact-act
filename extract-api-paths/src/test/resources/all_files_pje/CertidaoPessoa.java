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
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = CertidaoPessoa.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_certidao_pessoa", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_certidao_pessoa"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CertidaoPessoa implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CertidaoPessoa,Integer> {

	public static final String TABLE_NAME = "tb_certidao_pessoa";
	private static final long serialVersionUID = 1L;

	private int idCertidaoPessoa;
	private TipoCertidao tipoCertidao;
	private String nome;
	private String numeroCPFouCNPJ;
	private String certidao;
	private Date dataEmissao;
	private String hash;

	public CertidaoPessoa() {
	}

	@Id
	@GeneratedValue(generator = "gen_certidao_pessoa")
	@Column(name = "id_certidao_pessoa", unique = true, nullable = false)
	public int getIdCertidaoPessoa() {
		return idCertidaoPessoa;
	}

	public void setIdCertidaoPessoa(int idCertidaoPessoa) {
		this.idCertidaoPessoa = idCertidaoPessoa;
	}

	@Column(name = "nm_pessoa_certidao", length = 200, nullable = false)
	@Length(max = 200)
	@NotNull
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "nr_cpf_cnpj", length = 30, nullable = false)
	@Length(max = 30)
	@NotNull
	public String getNumeroCPFouCNPJ() {
		return numeroCPFouCNPJ;
	}

	public void setNumeroCPFouCNPJ(String numeroCPFouCNPJ) {
		this.numeroCPFouCNPJ = numeroCPFouCNPJ;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_certidao")
	public String getCertidao() {
		return certidao;
	}

	public void setCertidao(String certidao) {
		this.certidao = certidao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_emissao_certidao")
	public Date getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(Date dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	@Column(name = "ds_hash", length = 40)
	@Length(max = 40)
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_certidao", nullable = false)
	@NotNull
	public TipoCertidao getTipoCertidao() {
		return tipoCertidao;
	}

	public void setTipoCertidao(TipoCertidao tipoCertidao) {
		this.tipoCertidao = tipoCertidao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CertidaoPessoa)) {
			return false;
		}
		CertidaoPessoa other = (CertidaoPessoa) obj;
		if (getIdCertidaoPessoa() != other.getIdCertidaoPessoa()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCertidaoPessoa();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CertidaoPessoa> getEntityClass() {
		return CertidaoPessoa.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdCertidaoPessoa());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
