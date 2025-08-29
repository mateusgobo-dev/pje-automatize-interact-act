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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.log.Ignore;

@Entity
@Ignore
@Table(name = ProcessoLocalizacaoIbpm.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_localizacao_ibpm", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_localizacao_ibpm"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoLocalizacaoIbpm implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoLocalizacaoIbpm,Long> {

	public static final String TABLE_NAME = "tb_proc_localizacao_ibpm";

	private static final long serialVersionUID = 1L;

	private long idProcessoLocalizacaoIbpm;
	private Processo processo;
	private Long idProcessInstanceJbpm;
	private Localizacao localizacao;
	private Papel papel;
	private Long idTaskJbpm;

	public ProcessoLocalizacaoIbpm() {
	}

	@Id
	@GeneratedValue(generator = "gen_proc_localizacao_ibpm")
	@Column(name = "id_processo_localizacao", unique = true, nullable = false)
	public long getIdProcessoLocalizacaoIbpm() {
		return idProcessoLocalizacaoIbpm;
	}

	public void setIdProcessoLocalizacaoIbpm(long idProcessoLocalizacaoIbpm) {
		this.idProcessoLocalizacaoIbpm = idProcessoLocalizacaoIbpm;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo", nullable = false, updatable = false)
	@NotNull
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@Column(name = "id_processinstance_jbpm")
	public Long getIdProcessInstanceJbpm() {
		return idProcessInstanceJbpm;
	}

	public void setIdProcessInstanceJbpm(Long idProcessInstanceJbpm) {
		this.idProcessInstanceJbpm = idProcessInstanceJbpm;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_localizacao", nullable = false)
	@NotNull
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_papel")
	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@Column(name = "id_task_jbpm", nullable = false)
	@NotNull
	public Long getIdTaskJbpm() {
		return idTaskJbpm;
	}

	public void setIdTaskJbpm(Long idTaskJbpm) {
		this.idTaskJbpm = idTaskJbpm;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoLocalizacaoIbpm)) {
			return false;
		}
		ProcessoLocalizacaoIbpm other = (ProcessoLocalizacaoIbpm) obj;
		if (idProcessoLocalizacaoIbpm != other.getIdProcessoLocalizacaoIbpm()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(idProcessoLocalizacaoIbpm);
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoLocalizacaoIbpm> getEntityClass() {
		return ProcessoLocalizacaoIbpm.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return Long.valueOf(idProcessoLocalizacaoIbpm);
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return false;
	}

}
