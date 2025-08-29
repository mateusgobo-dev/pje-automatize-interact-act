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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.jus.pje.nucleo.enums.RemessaStatusEnum;

/**
 * Entidade que faz a relação 1x1 com Processo do core 
 */
@Entity
@Table(name = RemessaProcessoHistorico.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_remessa_proc_historico", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_remessa_proc_historico"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RemessaProcessoHistorico implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RemessaProcessoHistorico,Integer> {

	public static final String TABLE_NAME = "tb_remessa_proc_historico";
	private static final long serialVersionUID = 1L;

	private int idRemessaProcessoHistorico;
	private Date dataCadastro;
	private ProcessoTrf processoTrf;
	private RemessaStatusEnum remessaStatusEnum;
	private Boolean remetido;
	private Integer destino;
	private List<RemessaProcessoHistoricoLog> remessaProcessoHistoricoLogList = new ArrayList<RemessaProcessoHistoricoLog>(
			0);

	public RemessaProcessoHistorico() {
	}

	@Id
	@GeneratedValue(generator = "gen_remessa_proc_historico")
	@Column(name = "id_remessa_processo_historico", unique = true, nullable = false)
	public int getIdRemessaProcessoHistorico() {
		return this.idRemessaProcessoHistorico;
	}

	public void setIdRemessaProcessoHistorico(int idRemessaProcessoHistorico) {
		this.idRemessaProcessoHistorico = idRemessaProcessoHistorico;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro")
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_status", length = 1)
	public RemessaStatusEnum getRemessaStatusEnum() {
		return remessaStatusEnum;
	}

	public void setRemessaStatusEnum(RemessaStatusEnum remessaStatusEnum) {
		this.remessaStatusEnum = remessaStatusEnum;
	}

	@Column(name = "in_remetido")
	public Boolean isRemetido() {
		return remetido;
	}

	public void setRemetido(Boolean remetido) {
		this.remetido = remetido;
	}

	@Column(name = "id_sessao_destino")
	public Integer getDestino() {
		return destino;
	}

	public void setDestino(Integer destino) {
		this.destino = destino;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "remessaProcessoHistorico")
	public List<RemessaProcessoHistoricoLog> getRemessaProcessoHistoricoLogList() {
		return remessaProcessoHistoricoLogList;
	}

	public void setRemessaProcessoHistoricoLogList(List<RemessaProcessoHistoricoLog> remessaProcessoHistoricoLogList) {
		this.remessaProcessoHistoricoLogList = remessaProcessoHistoricoLogList;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RemessaProcessoHistorico> getEntityClass() {
		return RemessaProcessoHistorico.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRemessaProcessoHistorico());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
