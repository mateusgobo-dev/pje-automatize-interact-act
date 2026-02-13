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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

@Entity
@Table(name = "tb_proc_parte_represntante")
@IndexedEntity(id="idProcessoParteRepresentante", value="representante", owners={"processoParte"},
	mappings={
		@Mapping(beanPath="representante", mappedPath="pessoa"),
		@Mapping(beanPath="tipoRepresentante.tipoParte", mappedPath="tipo")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_proc_parte_representante", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_proc_parte_representante"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteRepresentante implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteRepresentante,Integer> {

	private static final long serialVersionUID = 1L;
	private int idProcessoParteRepresentante;
	private ProcessoParte processoParte;
	private ProcessoParte parteRepresentante;
	private Pessoa representante;
	private TipoParte tipoRepresentante;
	private ProcessoParteSituacaoEnum inSituacao = ProcessoParteSituacaoEnum.A;
	private ProcessoParte processoParteRepresentante;

	@Id
	@GeneratedValue(generator = "gen_proc_parte_representante")
	@Column(name = "id_proc_parte_representante", unique = true, nullable = false)
	public int getIdProcessoParteRepresentante() {
		return this.idProcessoParteRepresentante;
	}

	public void setIdProcessoParteRepresentante(int idProcessoParteRepresentante) {
		this.idProcessoParteRepresentante = idProcessoParteRepresentante;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_parte")
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_parte_representante")
	public ProcessoParte getParteRepresentante() {
		return parteRepresentante;
	}

	public void setParteRepresentante(ProcessoParte parteRepresentante) {
		this.parteRepresentante = parteRepresentante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_representante")
	public Pessoa getRepresentante() {
		return representante;
	}

	public void setRepresentante(Pessoa representante) {
		this.representante = representante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_representante")
	public TipoParte getTipoRepresentante() {
		return tipoRepresentante;
	}

	public void setTipoRepresentante(TipoParte tipoRepresentante) {
		this.tipoRepresentante = tipoRepresentante;
	}

	@Column(name = "in_situacao", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoParteSituacaoEnum getInSituacao() {
		return inSituacao;
	}

	public void setInSituacao(ProcessoParteSituacaoEnum inSituacao) {
		this.inSituacao = inSituacao;
	}

	public void setProcessoParteRepresentante(ProcessoParte processoParteRepresentante) {
		this.processoParteRepresentante = processoParteRepresentante;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_parte_representante", insertable = false, updatable = false)
	public ProcessoParte getProcessoParteRepresentante() {
		return processoParteRepresentante;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processoParte == null) ? 0 : processoParte.hashCode());
		result = prime * result + ((representante == null) ? 0 : representante.hashCode());
		result = prime * result + ((tipoRepresentante == null) ? 0 : tipoRepresentante.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessoParteRepresentante other = (ProcessoParteRepresentante) obj;
		if (processoParte == null) {
			if (other.processoParte != null)
				return false;
		} else if (!processoParte.equals(other.processoParte))
			return false;
		if (representante == null) {
			if (other.representante != null)
				return false;
		} else if (!representante.equals(other.representante))
			return false;
		if (tipoRepresentante == null) {
			if (other.tipoRepresentante != null)
				return false;
		} else if (!tipoRepresentante.equals(other.tipoRepresentante))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getProcessoParte() + " - " + getRepresentante();
	}
	
	/**
	 * Método que retorna verdadeiro se a parte representante estiver ativa
	 *  
	 * @return boolean
	 */
	@Transient
	public boolean isAtivo(){
		return getInSituacao().equals(ProcessoParteSituacaoEnum.A);
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteRepresentante> getEntityClass() {
		return ProcessoParteRepresentante.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteRepresentante());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
