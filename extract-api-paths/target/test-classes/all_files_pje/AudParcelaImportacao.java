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
package br.jus.pje.jt.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "tb_aud_parcela_importacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_aud_parcela_import", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aud_parcela_importacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AudParcelaImportacao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AudParcelaImportacao,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAudParcelaImportacao;
	@XmlTransient
	private AudImportacao audImportacao;

	private String numParcela;
	private Double valorParcela;
	private Date dataVencimento;
	private String numBanco;
	private String numAgencia;
	private String numCheque;

	@Id
	@Column(name = "id_aud_parcela_importacao", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_aud_parcela_import")
	public Integer getIdAudParcelaImportacao() {
		return idAudParcelaImportacao;
	}

	public void setIdAudParcelaImportacao(Integer idAudParcelaImportacao) {
		this.idAudParcelaImportacao = idAudParcelaImportacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aud_importacao", nullable = false)
	public AudImportacao getAudImportacao() {
		return audImportacao;
	}

	public void setAudImportacao(AudImportacao audImportacao) {
		this.audImportacao = audImportacao;
	}

	@Column(name = "nr_parcela")
	public String getNumParcela() {
		return numParcela;
	}

	public void setNumParcela(String numParcela) {
		this.numParcela = numParcela;
	}

	@Column(name = "vl_parcela")
	public Double getValorParcela() {
		return valorParcela;
	}

	public void setValorParcela(Double valorParcela) {
		this.valorParcela = valorParcela;
	}

	@Column(name = "dt_vencimento")
	public Date getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	@Column(name = "nm_banco")
	public String getNumBanco() {
		return numBanco;
	}

	public void setNumBanco(String numBanco) {
		this.numBanco = numBanco;
	}

	@Column(name = "nr_agencia")
	public String getNumAgencia() {
		return numAgencia;
	}

	public void setNumAgencia(String numAgencia) {
		this.numAgencia = numAgencia;
	}

	@Column(name = "nr_cheque")
	public String getNumCheque() {
		return numCheque;
	}

	public void setNumCheque(String numCheque) {
		this.numCheque = numCheque;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.audImportacao = (AudImportacao) parent;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AudParcelaImportacao> getEntityClass() {
		return AudParcelaImportacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAudParcelaImportacao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
