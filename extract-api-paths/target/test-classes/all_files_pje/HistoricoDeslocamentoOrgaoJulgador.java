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

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
/**
 * PJEII-3236
 * Guarda o historico de deslocamento e retorno de um processo entre 2 Orgaos Julgadores.
 * Um dos locais de utilizacao: plantao judicial
 * @author Frederico Carneiro
 *
 */
@Entity
@Table(name = HistoricoDeslocamentoOrgaoJulgador.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_hist_desloca_oj", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_hist_desloca_oj"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class HistoricoDeslocamentoOrgaoJulgador implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<HistoricoDeslocamentoOrgaoJulgador,Long> {

	public static final String TABLE_NAME = "tb_hist_desloca_oj";
	private static final long serialVersionUID = 1L;

	private Long idHistoricoDeslocamentoOrgaoJulgador;
	private ProcessoTrf processoTrf;
	private OrgaoJulgador orgaoJulgadorOrigem;
	private OrgaoJulgador orgaoJulgadorDestino;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoOrigem;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoDestino;
	private OrgaoJulgadorCargo orgaoJulgadorCargoOrigem;
	private OrgaoJulgadorCargo orgaoJulgadorCargoDestino;
	private Date dataDeslocamento;
	private Date dataRetorno;

	@Id
	@GeneratedValue(generator = "gen_hist_desloca_oj")
	@Column(name = "id_hist_desloca_oj", unique = true)
	public Long getIdHistoricoDeslocamentoOrgaoJulgador() {
		return idHistoricoDeslocamentoOrgaoJulgador;
	}

	public void setIdHistoricoDeslocamentoOrgaoJulgador(Long idHistoricoDeslocamentoOrgaoJulgador) {
		this.idHistoricoDeslocamentoOrgaoJulgador = idHistoricoDeslocamentoOrgaoJulgador;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	public ProcessoTrf getProcessoTrf(){
		return processoTrf;
	}

	
	public void setProcessoTrf(ProcessoTrf processoTrf){
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_origem", nullable = true)
	public OrgaoJulgador getOrgaoJulgadorOrigem(){
		return orgaoJulgadorOrigem;
	}

	
	public void setOrgaoJulgadorOrigem(OrgaoJulgador orgaoJulgadorOrigem){
		this.orgaoJulgadorOrigem = orgaoJulgadorOrigem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_oj_destino", nullable = true)
	public OrgaoJulgador getOrgaoJulgadorDestino(){
		return orgaoJulgadorDestino;
	}

	
	public void setOrgaoJulgadorDestino(OrgaoJulgador orgaoJulgadorDestino){
		this.orgaoJulgadorDestino = orgaoJulgadorDestino;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ojc_origem", nullable = true)
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoOrigem(){
		return orgaoJulgadorColegiadoOrigem;
	}

	
	public void setOrgaoJulgadorColegiadoOrigem(OrgaoJulgadorColegiado orgaoJulgadorColegiadoOrigem){
		this.orgaoJulgadorColegiadoOrigem = orgaoJulgadorColegiadoOrigem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ojc_destino", nullable = true)
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoDestino(){
		return orgaoJulgadorColegiadoDestino;
	}

	
	public void setOrgaoJulgadorColegiadoDestino(OrgaoJulgadorColegiado orgaoJulgadorColegiadoDestino){
		this.orgaoJulgadorColegiadoDestino = orgaoJulgadorColegiadoDestino;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo_origem", nullable = true)
	public OrgaoJulgadorCargo getOrgaoJulgadorCargoOrigem(){
		return orgaoJulgadorCargoOrigem;
	}

	
	public void setOrgaoJulgadorCargoOrigem(OrgaoJulgadorCargo orgaoJulgadorCargoOrigem){
		this.orgaoJulgadorCargoOrigem = orgaoJulgadorCargoOrigem;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo_destino", nullable = true)
	public OrgaoJulgadorCargo getOrgaoJulgadorCargoDestino(){
		return orgaoJulgadorCargoDestino;
	}

	
	public void setOrgaoJulgadorCargoDestino(OrgaoJulgadorCargo orgaoJulgadorCargoDestino){
		this.orgaoJulgadorCargoDestino = orgaoJulgadorCargoDestino;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_deslocamento", nullable = true)
	public Date getDataDeslocamento(){
		return dataDeslocamento;
	}

	
	public void setDataDeslocamento(Date dataDeslocamento){
		this.dataDeslocamento = dataDeslocamento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_retorno", nullable = true)
	public Date getDataRetorno(){
		return dataRetorno;
	}

	
	public void setDataRetorno(Date dataRetorno){
		this.dataRetorno = dataRetorno;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends HistoricoDeslocamentoOrgaoJulgador> getEntityClass() {
		return HistoricoDeslocamentoOrgaoJulgador.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getIdHistoricoDeslocamentoOrgaoJulgador();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
