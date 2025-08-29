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

@Entity
@Table(name = "tb_processo_parte_endereco")
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_parte_endereco", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_parte_endereco"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoParteEndereco implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoParteEndereco,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcessoParteEndereco;
	private ProcessoParte processoParte;
	private Endereco endereco;

	public ProcessoParteEndereco() {
	}

	@Id
	@GeneratedValue(generator = "gen_processo_parte_endereco")
	@Column(name = "id_processo_parte_endereco", unique = true, nullable = false)
	public int getIdProcessoParteEndereco() {
		return this.idProcessoParteEndereco;
	}

	public void setIdProcessoParteEndereco(int idProcessoParteEndereco) {
		this.idProcessoParteEndereco = idProcessoParteEndereco;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	@NotNull
	public ProcessoParte getProcessoParte() {
		return this.processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_endereco", nullable = false)
	@NotNull
	public Endereco getEndereco() {
		return this.endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	@Override
	public String toString() {

		return (processoParte != null ? processoParte.toString() : "") + " / "
				+ (endereco != null ? endereco.toString() : "");

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoParteEndereco)) {
			return false;
		}
		ProcessoParteEndereco other = (ProcessoParteEndereco) obj;
		if (getIdProcessoParteEndereco() != other.getIdProcessoParteEndereco()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoParteEndereco();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoParteEndereco> getEntityClass() {
		return ProcessoParteEndereco.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoParteEndereco());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
