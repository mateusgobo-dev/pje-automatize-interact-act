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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_rpv_eletronica")
public class RpvEletronica implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idRpv;
	private String documentoRpv;
	private String signature;
	private String certChain;
	private PessoaMagistrado magistradoAssinatura;
	private Date dataAssinatura;

	public RpvEletronica() {
	}

	@Id
	@Column(name = "id_rpv", unique = true, nullable = false)
	public int getIdRpv() {
		return this.idRpv;
	}

	public void setIdRpv(int idRpv) {
		this.idRpv = idRpv;
	}

	@Column(name = "ds_rpv", nullable = false)
	@NotNull
	public String getdocumentoRpv() {
		return documentoRpv;
	}

	public void setDocumentoRpv(String documentoRpv) {
		this.documentoRpv = documentoRpv;
	}

	@Column(name = "ds_assinatura")
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Column(name = "ds_cert_chain")
	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_validacao")
	public PessoaMagistrado getMagistradoAssinatura() {
		return this.magistradoAssinatura;
	}

	public void setMagistradoAssinatura(PessoaMagistrado magistradoAssinatura) {
		this.magistradoAssinatura = magistradoAssinatura;
	}

	@Column(name = "dt_assinatura")
	public Date getDataAssinatura() {
		return dataAssinatura;
	}

	public void setDataAssinatura(Date dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		Integer idObj = null;
		if(RpvEletronica.class.isAssignableFrom(obj.getClass())){
			idObj = (Integer) ((RpvEletronica) obj).getIdRpv();
		}else if(Rpv.class.isAssignableFrom(obj.getClass())){
			idObj = (Integer) ((Rpv) obj).getIdRpv();
		}else{
			return false;
		}
		if (idObj == null || !idObj.equals(getIdRpv())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdRpv();
		return result;
	}
}