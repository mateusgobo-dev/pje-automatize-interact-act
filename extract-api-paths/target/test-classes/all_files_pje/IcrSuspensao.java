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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.PropertyUtils;

@Entity
@Table(name = "tb_icr_suspensao")
@PrimaryKeyJoinColumn(name = "id_icr_suspensao")
public class IcrSuspensao extends InformacaoCriminalRelevante{

	private static final long serialVersionUID = 4488059233421281769L;
	private Integer prazoSuspensaoDia;
	private Integer prazoSuspensaoMes;
	private Integer prazoSuspensaoAno;
	private Date dataPrevistaTermino;
	private TipoSuspensao tipoSuspensao;
	private List<CondicaoSuspensaoAssociada> condicaoSuspensaoAssociadaList = new ArrayList<CondicaoSuspensaoAssociada>(0);

	public IcrSuspensao(){
		;
	}

	public IcrSuspensao(InformacaoCriminalRelevante icr){
		copiarPropriedadesIcr(icr);
	}

	// ------ UTILS --------------------------------------------------//

	private void copiarPropriedadesIcr(InformacaoCriminalRelevante icr){

		try{
			PropertyUtils.copyProperties(this, icr);
		} catch (Exception e){
			throw new RuntimeException(e);
		}

	}

	@Column(name = "nr_prazo_suspensao_dia")
	public Integer getPrazoSuspensaoDia(){
		return prazoSuspensaoDia;
	}

	public void setPrazoSuspensaoDia(Integer prazoSuspensaoDia){
		this.prazoSuspensaoDia = prazoSuspensaoDia;
	}

	@Column(name = "nr_prazo_suspensao_mes")
	public Integer getPrazoSuspensaoMes(){
		return prazoSuspensaoMes;
	}

	public void setPrazoSuspensaoMes(Integer prazoSuspensaoMes){
		this.prazoSuspensaoMes = prazoSuspensaoMes;
	}

	@Column(name = "nr_prazo_suspensao_ano")
	public Integer getPrazoSuspensaoAno(){
		return prazoSuspensaoAno;
	}

	public void setPrazoSuspensaoAno(Integer prazoSuspensaoAno){
		this.prazoSuspensaoAno = prazoSuspensaoAno;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "dt_data_prevista_termino")
	public Date getDataPrevistaTermino(){
		return dataPrevistaTermino;
	}

	public void setDataPrevistaTermino(Date dataPrevistaTermino){
		this.dataPrevistaTermino = dataPrevistaTermino;
	}

	@ManyToOne
	@NotNull
	@JoinColumn(name = "id_tipo_suspensao", nullable = false)
	public TipoSuspensao getTipoSuspensao(){
		return tipoSuspensao;
	}

	public void setTipoSuspensao(TipoSuspensao tipoSuspensao){
		this.tipoSuspensao = tipoSuspensao;
	}

	@OneToMany(mappedBy = "icrSuspensao", cascade = CascadeType.ALL)
	public List<CondicaoSuspensaoAssociada> getCondicaoSuspensaoAssociadaList(){
		return condicaoSuspensaoAssociadaList;
	}

	public void setCondicaoSuspensaoAssociadaList(
			List<CondicaoSuspensaoAssociada> condicaoSuspensaoAssociadaList){
		this.condicaoSuspensaoAssociadaList = condicaoSuspensaoAssociadaList;
	}

	@Override
	@javax.persistence.Transient
	public Class<? extends InformacaoCriminalRelevante> getEntityClass() {
		return IcrSuspensao.class;
	}
}
