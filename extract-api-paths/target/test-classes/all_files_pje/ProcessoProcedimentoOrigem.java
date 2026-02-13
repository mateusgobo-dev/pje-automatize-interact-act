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
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_processo_proc_origem")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_proc_origem", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_processo_proc_origem"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoProcedimentoOrigem implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoProcedimentoOrigem,Integer> {

	private static final long serialVersionUID = 1L;

	private int id;
	private ProcessoTrf processoTrf;
	private OrgaoProcedimentoOriginario orgaoProcedimentoOriginario;
	private TipoProcedimentoOrigem tipoProcedimentoOrigem;
	private TipoOrigem tipoOrigem;
	private Date dataInstauracao;
	private String numero;
	private Integer ano;
	private Date dtLocalFato;
	private String dsLocalFato;
	private String dsLongitude;
	private String dsLatitude;
	private String nrProtocoloPolicia;
	private String uf;
	private Boolean ativo;
	private Integer codigoNacional;
	
//	private List<MandadoAlvara> mandadoAlvaraList = new ArrayList<MandadoAlvara>(0);

	public void setId(int id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(generator = "gen_processo_proc_origem")
	@Column(name = "id_processo_proc_origem", unique = true, nullable = false)
	public int getId() {
		return id;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setOrgaoProcedimentoOriginario(OrgaoProcedimentoOriginario orgaoProcedimentoOriginario) {
		this.orgaoProcedimentoOriginario = orgaoProcedimentoOriginario;
	}

	@ManyToOne
	@JoinColumn(name = "id_org_procedimento_originario")
	public OrgaoProcedimentoOriginario getOrgaoProcedimentoOriginario() {
		return orgaoProcedimentoOriginario;
	}

	public void setTipoProcedimentoOrigem(TipoProcedimentoOrigem tipoProcedimentoOrigem) {
		this.tipoProcedimentoOrigem = tipoProcedimentoOrigem;
	}

	@ManyToOne
	@JoinColumn(name = "id_tipo_procedimento_origem")
	public TipoProcedimentoOrigem getTipoProcedimentoOrigem() {
		return tipoProcedimentoOrigem;
	}

	public void setDataInstauracao(Date dataInstauracao) {
		this.dataInstauracao = dataInstauracao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_instauracao", nullable = true)
	public Date getDataInstauracao() {
		return dataInstauracao;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	@Column(name = "nr_procedimento", nullable = true)
	public String getNumero() {
		return numero;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_ano", nullable = true)
	@Max(9999)
	public Integer getAno() {
		return ano;
	}

	@Transient
	public String getNumeroAno() {
		if (getNumero() != null && getAno() != null) {
			return getNumero() + "/" + getAno();
		}

		return "";
	}

	public void setTipoOrigem(TipoOrigem tipoOrigem) {
		this.tipoOrigem = tipoOrigem;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_tipo_origem", nullable = false)
	public TipoOrigem getTipoOrigem() {
		return tipoOrigem;
	}

	@Column(name = "dt_local_fato", nullable = true)
	public Date getDtLocalFato() {
		return dtLocalFato;
	}

	public void setDtLocalFato(Date dtLocalFato) {
		this.dtLocalFato = dtLocalFato;
	}

	@Column(name = "ds_local_fato", nullable = true)
	public String getDsLocalFato() {
		return dsLocalFato;
	}

	public void setDsLocalFato(String dsLocalFato) {
		this.dsLocalFato = dsLocalFato;
	}

	@Column(name = "ds_latitude", nullable = true)
	public String getDsLatitude() {
		return dsLatitude;
	}

	public void setDsLatitude(String dsLatitude) {
		this.dsLatitude = dsLatitude;
	}

	@Column(name = "ds_longitude", nullable = true)
	public String getDsLongitude() {
		return dsLongitude;
	}

	public void setDsLongitude(String dsLongitude) {
		this.dsLongitude = dsLongitude;
	}

	@Column(name = "nr_protocolo_policia", nullable = true)
	public String getNrProtocoloPolicia() {
		return nrProtocoloPolicia;
	}

	public void setNrProtocoloPolicia(String nrProtocoloPolicia) {
		this.nrProtocoloPolicia = nrProtocoloPolicia;
	}
	
	@Column(name = "cd_estado", nullable = true)
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(name = "in_ativo", nullable = false)
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Column(name = "cd_nacional", nullable = true)
	public Integer getCodigoNacional() {
		return codigoNacional;
	}

	public void setCodigoNacional(Integer codigoNacional) {
		this.codigoNacional = codigoNacional;
	}

//	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "processoProcedimentoOrigemList")
//	public List<MandadoAlvara> getMandadoPrisaoList() {
//		return mandadoAlvaraList;
//	}
//
//	public void setMandadoPrisaoList(List<MandadoAlvara> mandadoAlvaraList) {
//		this.mandadoAlvaraList = mandadoAlvaraList;
//	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoProcedimentoOrigem> getEntityClass() {
		return ProcessoProcedimentoOrigem.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
