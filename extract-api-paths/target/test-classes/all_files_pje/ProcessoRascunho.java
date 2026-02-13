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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.type.ProcessoCriminalType;


@Entity
@Table(name = ProcessoRascunho.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name = "gen_processo_rascunho", sequenceName = "sq_tb_processo_rascunho")
@TypeDefs({@TypeDef(name = "ProcessoCriminalType", typeClass = ProcessoCriminalType.class)})
public class ProcessoRascunho implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_processo_rascunho";

	private Integer idProcessoRascunho;
	private ProcessoTrf processo;
	private ProcessoCriminalDTO jsonProcessoCriminal;
	
	@Id
	@GeneratedValue(generator = "gen_processo_rascunho")
	@Column(name = "id_processo_rascunho", nullable = false)
	public Integer getIdProcessoRascunho() {
		return idProcessoRascunho;
	}

	public void setIdProcessoRascunho(Integer idProcessoRascunho) {
		this.idProcessoRascunho = idProcessoRascunho;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf")
	public ProcessoTrf getProcesso() {
		return processo;
	}
	
	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}
	
	@Column(name = "json_processo_criminal")
	@Type(type = "ProcessoCriminalType")
	public ProcessoCriminalDTO getJsonProcessoCriminal() {
		return jsonProcessoCriminal;
	}

	public void setJsonProcessoCriminal(ProcessoCriminalDTO jsonProcProcedimentoOrigem) {
		this.jsonProcessoCriminal = jsonProcProcedimentoOrigem;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idProcessoRascunho == null) ? 0 : idProcessoRascunho.hashCode());
		result = prime * result + ((processo == null) ? 0 : processo.hashCode());
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
		ProcessoRascunho other = (ProcessoRascunho) obj;
		if (idProcessoRascunho == null) {
			if (other.idProcessoRascunho != null)
				return false;
		} else if (!idProcessoRascunho.equals(other.idProcessoRascunho))
			return false;
		if (processo == null) {
			if (other.processo != null)
				return false;
		} else if (!processo.equals(other.processo))
			return false;
		return true;
	}
}
