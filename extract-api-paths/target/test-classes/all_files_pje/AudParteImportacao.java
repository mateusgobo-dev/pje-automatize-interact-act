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
@Table(name = "tb_aud_parte_importacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_aud_parte_importacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_aud_parte_importacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AudParteImportacao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AudParteImportacao,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idAudParteImportacao;
	@XmlTransient
	private AudImportacao audImportacao;
	private Integer idProcessoParte;

	private String docCpfCnpjParte;
	private String nomeGenitorParte;
	private String nomeGenitoraParte;
	private String nomeAdvParte;
	private String docCpfAdvParte;
	private String oabAdvParte;
	private String ufAdvParte;
	private String nomeParte;
	private String tipoParte;
	private String poloAtivoParte;
	private String partePresente;

	@Id
	@Column(name = "id_aud_parte_importacao", unique = true, nullable = false)
	@GeneratedValue(generator = "gen_aud_parte_importacao")
	public Integer getIdAudParteImportacao() {
		return idAudParteImportacao;
	}

	public void setIdAudParteImportacao(Integer idAudParteImportacao) {
		this.idAudParteImportacao = idAudParteImportacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aud_importacao", nullable = false)
	public AudImportacao getAudImportacao() {
		return audImportacao;
	}

	public void setAudImportacao(AudImportacao audImportacao) {
		this.audImportacao = audImportacao;
	}

	@Column(name = "id_proc_parte")
	public Integer getIdProcessoParte() {
		return idProcessoParte;
	}

	public void setIdProcessoParte(Integer idProcessoParte) {
		this.idProcessoParte = idProcessoParte;
	}

	@Column(name = "nr_oab_adv_parte")
	public String getOabAdvParte() {
		return oabAdvParte;
	}

	public void setOabAdvParte(String oabAdvParte) {
		this.oabAdvParte = oabAdvParte;
	}

	@Column(name = "nm_genitora_parte")
	public String getNomeGenitoraParte() {
		return nomeGenitoraParte;
	}

	@Column(name = "ds_nome_adv_parte")
	public String getNomeAdvParte() {
		return nomeAdvParte;
	}

	@Column(name = "nr_doc_cpf_adv")
	public String getDocCpfAdvParte() {
		return docCpfAdvParte;
	}

	public void setDocCpfAdvParte(String docCpfAdvParte) {
		this.docCpfAdvParte = docCpfAdvParte;
	}

	public void setDocCpfCnpjParte(String docCpfParte) {
		this.docCpfCnpjParte = docCpfParte;
	}

	public void setNomeGenitorParte(String nomeGenitorParte) {
		this.nomeGenitorParte = nomeGenitorParte;
	}

	@Column(name = "nr_doc_cpf_parte")
	public String getDocCpfCnpjParte() {
		return docCpfCnpjParte;
	}

	@Column(name = "nm_genitor_parte")
	public String getNomeGenitorParte() {
		return nomeGenitorParte;
	}

	public void setNomeGenitoraParte(String nomeGenitoraParte) {
		this.nomeGenitoraParte = nomeGenitoraParte;
	}

	public void setNomeAdvParte(String nomeAdvParte) {
		this.nomeAdvParte = nomeAdvParte;
	}

	@Column(name = "cd_uf_adv_parte")
	public String getUfAdvParte() {
		return ufAdvParte;
	}

	public void setUfAdvParte(String ufAdvParte) {
		this.ufAdvParte = ufAdvParte;
	}

	@Column(name = "ds_nome_parte")
	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	@Column(name = "ds_tipo_parte")
	public String getTipoParte() {
		return tipoParte;
	}

	public void setTipoParte(String tipoParte) {
		this.tipoParte = tipoParte;
	}

	@Column(name = "in_polo_ativo_parte")
	public String getPoloAtivoParte() {
		return poloAtivoParte;
	}

	public void setPoloAtivoParte(String poloAtivoParte) {
		this.poloAtivoParte = poloAtivoParte;
	}

	@Column(name = "in_parte_presente")
	public String getPartePresente() {
		return partePresente;
	}

	public void setPartePresente(String partePresente) {
		this.partePresente = partePresente;
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.audImportacao = (AudImportacao) parent;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AudParteImportacao> getEntityClass() {
		return AudParteImportacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAudParteImportacao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
