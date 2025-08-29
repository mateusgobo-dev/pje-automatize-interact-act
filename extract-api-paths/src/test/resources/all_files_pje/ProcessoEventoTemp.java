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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tb_processo_evento_temp")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_evento_temp", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_evento_temp"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoEventoTemp implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoEventoTemp,Integer>{

	private static final long serialVersionUID = 1L;

	private int idProcessoEventoTemp;
	private Evento evento;
	private ProcessoDocumento processoDocumento;
	private Processo processo;
	private Usuario usuario;
	private Date dataInsercao;
	private Long idJbpmTask;
	private TipoProcessoDocumento tipoProcessoDocumento;

	@Id
	@GeneratedValue(generator = "gen_processo_evento_temp")
	@Column(name = "id_processo_evento_temp", unique = true, nullable = false)
	public int getIdProcessoEventoTemp(){
		return idProcessoEventoTemp;
	}

	public void setIdProcessoEventoTemp(int idProcessoEventoTemp){
		this.idProcessoEventoTemp = idProcessoEventoTemp;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento", nullable = false)
	@NotNull
	public Evento getEvento(){
		return evento;
	}

	public void setEvento(Evento evento){
		this.evento = evento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo", nullable = false)
	@NotNull
	public Processo getProcesso(){
		return processo;
	}

	public void setProcesso(Processo processo){
		this.processo = processo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario(){
		return usuario;
	}

	public void setUsuario(Usuario usuario){
		this.usuario = usuario;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_insercao")
	public Date getDataInsercao(){
		return dataInsercao;
	}

	public void setDataInsercao(Date dataInsercao){
		this.dataInsercao = dataInsercao;
	}

	@Column(name = "id_jbpm_task")
	public Long getIdJbpmTask(){
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask){
		this.idJbpmTask = idJbpmTask;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento){
		this.processoDocumento = processoDocumento;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento(){
		return processoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento){
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_processo_documento")
	public TipoProcessoDocumento getTipoProcessoDocumento(){
		return tipoProcessoDocumento;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!(obj instanceof ProcessoEventoTemp)){
			return false;
		}
		ProcessoEventoTemp other = (ProcessoEventoTemp) obj;
		if (getIdProcessoEventoTemp() != other.getIdProcessoEventoTemp()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoEventoTemp();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoEventoTemp> getEntityClass() {
		return ProcessoEventoTemp.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoEventoTemp());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
