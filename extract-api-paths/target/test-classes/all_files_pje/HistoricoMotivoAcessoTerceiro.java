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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = HistoricoMotivoAcessoTerceiro.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_motivo_aces_terc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_motivo_aces_terc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoMotivoAcessoTerceiro implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoMotivoAcessoTerceiro,Long> {

	public static final String TABLE_NAME = "tb_hist_motivo_aces_terc";
	private static final long serialVersionUID = 1L;

	private Long idHistMotivoAcesTerc;
	private ProcessoTrf processoTrf;
	private Date dtMotivoAcesso;
	private String dsUsuarioAcessou;
	private String nrOabProcuradoria;
	private Usuario usuario;
	private String ip;


	@Id
	@GeneratedValue(generator = "gen_hist_motivo_aces_terc")
	@Column(name = "id_hist_motivo_aces_terc", unique = true, nullable = false)
	public Long getIdHistMotivoAcesTerc() {
		return idHistMotivoAcesTerc;
	}

	public void setIdHistMotivoAcesTerc(Long idHistMotivoAcesTerc) {
		this.idHistMotivoAcesTerc = idHistMotivoAcesTerc;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_motivo_acesso")
	public Date getDtMotivoAcesso() {
		return dtMotivoAcesso;
	}

	public void setDtMotivoAcesso(Date dtMotivoAcesso) {
		this.dtMotivoAcesso = dtMotivoAcesso;
	}

	@Column(name = "ds_usuario_acessou", length = 200, nullable = false)
	@NotNull
	@Length(max = 200)
	public String getDsUsuarioAcessou() {
		return dsUsuarioAcessou;
	}

	public void setDsUsuarioAcessou(String dsUsuarioAcessou) {
		this.dsUsuarioAcessou = dsUsuarioAcessou;
	}

	@Column(name = "nr_oab_procuradoria", length = 200)
	@Length(max = 200)
	public String getNrOabProcuradoria() {
		return nrOabProcuradoria;
	}

	public void setNrOabProcuradoria(String nrOabProcuradoria) {
		this.nrOabProcuradoria = nrOabProcuradoria;
	}
	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	

	@Column(name = "ds_ip", length = 100)
	@Length(max = 100)
	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoMotivoAcessoTerceiro> getEntityClass() {
		return HistoricoMotivoAcessoTerceiro.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return Long.valueOf(getIdHistMotivoAcesTerc());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}
}
