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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

@Entity
@Table(name = "tb_pericia")
@org.hibernate.annotations.GenericGenerator(name = "gen_pericia", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pericia"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Pericia implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Pericia,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idPericia;
	private ProcessoAudiencia processoAudiencia;
	private String nomePerito;
	private String prazoQuesitos;
	private Date dtComumQuesitos;
	private Date dtAutorQuesitos;
	private Date dtReuQuesitos;
	private String prazoLaudo;
	private Date dtInicioPrazoLaudo;
	private String prazoPartes;
	private Date dtInicioConstestarAutor;
	private Date dtInicioConstestarReu;

	@Id
	@GeneratedValue(generator = "gen_pericia")
	@Column(name = "id_pericia", unique = true, nullable = false)
	public Integer getIdPericia() {
		return idPericia;
	}

	public void setIdPericia(Integer idPericia) {
		this.idPericia = idPericia;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_audiencia", nullable = false)
	public ProcessoAudiencia getProcessoAudiencia() {
		return processoAudiencia;
	}

	public void setProcessoAudiencia(ProcessoAudiencia processoAudiencia) {
		this.processoAudiencia = processoAudiencia;
	}

	@Column(name = "ds_nome_perito")
	public String getNomePerito() {
		return nomePerito;
	}

	public void setNomePerito(String nomePerito) {
		this.nomePerito = nomePerito;
	}

	@Column(name = "nr_prazo_quesitos")
	public String getPrazoQuesitos() {
		return prazoQuesitos;
	}

	public void setPrazoQuesitos(String prazoQuesitos) {
		this.prazoQuesitos = prazoQuesitos;
	}

	@Column(name = "dt_comum_quesitos")
	public Date getDtComumQuesitos() {
		return dtComumQuesitos;
	}

	public void setDtComumQuesitos(Date dtComumQuesitos) {
		this.dtComumQuesitos = dtComumQuesitos;
	}

	@Column(name = "dt_autor_quesitos")
	public Date getDtAutorQuesitos() {
		return dtAutorQuesitos;
	}

	public void setDtAutorQuesitos(Date dtAutorQuesitos) {
		this.dtAutorQuesitos = dtAutorQuesitos;
	}

	@Column(name = "dt_reu_quesitos")
	public Date getDtReuQuesitos() {
		return dtReuQuesitos;
	}

	public void setDtReuQuesitos(Date dtReuQuesitos) {
		this.dtReuQuesitos = dtReuQuesitos;
	}

	@Column(name = "nr_prazo_laudo")
	public String getPrazoLaudo() {
		return prazoLaudo;
	}

	public void setPrazoLaudo(String prazoLaudo) {
		this.prazoLaudo = prazoLaudo;
	}

	@Column(name = "dt_ini_prazo_laudo")
	public Date getDtInicioPrazoLaudo() {
		return dtInicioPrazoLaudo;
	}

	public void setDtInicioPrazoLaudo(Date dtInicioPrazoLaudo) {
		this.dtInicioPrazoLaudo = dtInicioPrazoLaudo;
	}

	@Column(name = "nr_prazo_partes")
	public String getPrazoPartes() {
		return prazoPartes;
	}

	public void setPrazoPartes(String prazoPartes) {
		this.prazoPartes = prazoPartes;
	}

	@Column(name = "dt_ini_contestar_autor")
	public Date getDtInicioConstestarAutor() {
		return dtInicioConstestarAutor;
	}

	public void setDtInicioConstestarAutor(Date dtInicioConstestarAutor) {
		this.dtInicioConstestarAutor = dtInicioConstestarAutor;
	}

	@Column(name = "dt_ini_contestar_reu")
	public Date getDtInicioConstestarReu() {
		return dtInicioConstestarReu;
	}

	public void setDtInicioConstestarReu(Date dtInicioConstestarReu) {
		this.dtInicioConstestarReu = dtInicioConstestarReu;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Pericia> getEntityClass() {
		return Pericia.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdPericia();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
