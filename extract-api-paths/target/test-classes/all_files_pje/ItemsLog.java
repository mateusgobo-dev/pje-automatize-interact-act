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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import br.jus.pje.nucleo.entidades.log.Ignore;
import br.jus.pje.nucleo.enums.CriticidadeEnum;

@Entity
@Table(name = ItemsLog.TABLE_NAME)
@Ignore
@org.hibernate.annotations.GenericGenerator(name = "gen_items_log", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_items_log"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ItemsLog implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ItemsLog,Integer> {

	public static final String TABLE_NAME = "tb_items_log";
	private static final long serialVersionUID = 1L;

	private Integer idItem;
	private ProcessoTrfLog processoTrfLog;
	private Date dataItem;
	String item;
	CriticidadeEnum inCriticidade;

	public ItemsLog() {
		this.dataItem = new Date();
	}

	@Id
	@GeneratedValue(generator = "gen_items_log")
	@Column(name = "id_item_log", unique = true, nullable = false)
	public Integer getIdItem() {
		return this.idItem;
	}

	public void setIdItem(Integer idItem) {
		this.idItem = idItem;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf_log")
	public ProcessoTrfLog getProcessoTrfLog() {
		return processoTrfLog;
	}

	public void setProcessoTrfLog(ProcessoTrfLog processoTrfLog) {
		this.processoTrfLog = processoTrfLog;
	}

	@Column(name = "dt_item", nullable = false)
	public Date getDataItem() {
		return dataItem;
	}

	public void setDataItem(Date dataItem) {
		this.dataItem = dataItem;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_item")
	public String getItem() {
		return this.item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	@Transient
	public String getItemFormatado() {
		return getItem() != null ? getItem().replace("\n", "<br/>") : "";
	}
	
	@Column(name = "in_criticidade", length = 1)
	@Enumerated(EnumType.STRING)
	public CriticidadeEnum getInCriticidade() {
		return this.inCriticidade;
	}

	public void setInCriticidade(CriticidadeEnum inCriticidade) {
		this.inCriticidade = inCriticidade;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ItemsLog> getEntityClass() {
		return ItemsLog.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdItem();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
