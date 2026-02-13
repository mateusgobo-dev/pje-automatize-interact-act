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
import javax.validation.constraints.NotNull;


@Entity
@Table(name = RespostaExpediente.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_resposta_expediente", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_resposta_expediente"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RespostaExpediente implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RespostaExpediente,Integer> {

	public static final String TABLE_NAME = "tb_resposta_expediente";
	private static final long serialVersionUID = 1L;

	private int id;
	private Date data;
	private ProcessoDocumento processoDocumento;

	public RespostaExpediente() {
	}

	@Id
	@GeneratedValue(generator = "gen_resposta_expediente")
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "dt_registro", nullable = false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RespostaExpediente> getEntityClass() {
		return RespostaExpediente.class;
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
