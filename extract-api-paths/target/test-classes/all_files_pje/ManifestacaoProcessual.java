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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = ManifestacaoProcessual.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_manifestacao_processual", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_manifestacao_processual"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ManifestacaoProcessual implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ManifestacaoProcessual,Long> {

	private static final long serialVersionUID = -8335729978644915251L;

	public static final String TABLE_NAME = "tb_manifestacao_processual";

	private Long idManifestacaoProcessual;
	private Date dataRecebimento;
	private String codigoOrigem;
	private String codigoAplicacaoOrigem;
	private ProcessoTrf processoTrf;
	private byte[] bean;
	private List<ManifestacaoProcessualDocumento> manifestacaoProcessualDocumentoList = new ArrayList<ManifestacaoProcessualDocumento>(0);
	private String wsdlOrigemConsulta;
	private String wsdlOrigemEnvio;	

	public ManifestacaoProcessual() {

	}

	public ManifestacaoProcessual(Date dataRecebimento, String codigoOrigem, String codigoAplicacaoOrigem) {
		this.dataRecebimento = dataRecebimento;
		this.codigoOrigem = codigoOrigem;
		this.codigoAplicacaoOrigem = codigoAplicacaoOrigem;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_manifestacao_processual")
	@Column(name = "id_manifestacao_processual", unique = true, nullable = false)
	public Long getIdManifestacaoProcessual() {
		return this.idManifestacaoProcessual;
	}

	public void setIdManifestacaoProcessual(Long idManifestacaoProcessual) {
		this.idManifestacaoProcessual = idManifestacaoProcessual;
	}

	@Column(name = "dt_recebimento", nullable = false)
	public Date getDataRecebimento() {
		return dataRecebimento;
	}

	public void setDataRecebimento(Date dataRecebimento) {
		this.dataRecebimento = dataRecebimento;
	}

	@Column(name = "cd_origem", nullable = false)
	public String getCodigoOrigem() {
		return codigoOrigem;
	}

	public void setCodigoOrigem(String codigoOrigem) {
		this.codigoOrigem = codigoOrigem;
	}

	@Column(name = "cd_aplicacao_origem", nullable = false)
	public String getCodigoAplicacaoOrigem() {
		return codigoAplicacaoOrigem;
	}

	public void setCodigoAplicacaoOrigem(String codigoAplicacaoOrigem) {
		this.codigoAplicacaoOrigem = codigoAplicacaoOrigem;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Type(type = "org.hibernate.type.BinaryType")
	@Column(name="bt_bean")
	public byte[] getBean() {
		return bean;
	}
	
	public void setBean(byte[] bean) {
		this.bean = bean;
	}
	
	@OneToMany(cascade=CascadeType.PERSIST, mappedBy="manifestacaoProcessual", fetch=FetchType.LAZY)
	public List<ManifestacaoProcessualDocumento> getManifestacaoProcessualDocumentoList() {
		return manifestacaoProcessualDocumentoList;
	}
	
	public void setManifestacaoProcessualDocumentoList(
			List<ManifestacaoProcessualDocumento> manifestacaoProcessualDocumentoList) {
		this.manifestacaoProcessualDocumentoList = manifestacaoProcessualDocumentoList;
	}

	@Column(name = "ds_wdsl_origem_consulta")
	public String getWsdlOrigemConsulta() {
		return wsdlOrigemConsulta;
	}

	public void setWsdlOrigemConsulta(String wsdlOrigemConsulta) {
		this.wsdlOrigemConsulta = wsdlOrigemConsulta;
	}

	@Column(name = "ds_wdsl_origem_envio")
	public String getWsdlOrigemEnvio() {
		return wsdlOrigemEnvio;
	}

	public void setWsdlOrigemEnvio(String wsdlOrigemEnvio) {
		this.wsdlOrigemEnvio = wsdlOrigemEnvio;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ManifestacaoProcessual> getEntityClass() {
		return ManifestacaoProcessual.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdManifestacaoProcessual();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
