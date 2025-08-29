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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = ComplementoClasseProcessoTrf.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_complemento_cl_proc_trf", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_complemento_cl_proc_trf"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ComplementoClasseProcessoTrf implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ComplementoClasseProcessoTrf,Integer> {

	public static final String TABLE_NAME = "tb_complem_classe_proc_trf";
	private static final long serialVersionUID = 1L;

	private int idComplementoClasseProcessoTrf;
	private ProcessoTrf processoTrf;
	private ComplementoClasse complementoClasse;
	private String valorComplementoClasseProcessoTrf;

	public ComplementoClasseProcessoTrf() {
	}

	@Id
	@GeneratedValue(generator = "gen_complemento_cl_proc_trf")
	@Column(name = "id_complemento_classe_proc_trf", unique = true, nullable = false)
	public int getIdComplementoClasseProcessoTrf() {
		return this.idComplementoClasseProcessoTrf;
	}

	public void setIdComplementoClasseProcessoTrf(int idComplementoClasseProcessoTrf) {
		this.idComplementoClasseProcessoTrf = idComplementoClasseProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false)
	@NotNull
	public ProcessoTrf getProcessoTrf() {
		return this.processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_complemento_classe")
	public ComplementoClasse getComplementoClasse() {
		return this.complementoClasse;
	}

	public void setComplementoClasse(ComplementoClasse complementoClasse) {
		this.complementoClasse = complementoClasse;
	}

	@Column(name = "ds_complemento_classe", length = 100)
	@Length(max = 100)
	public String getValorComplementoClasseProcessoTrf() {
		return this.valorComplementoClasseProcessoTrf;
	}

	public void setValorComplementoClasseProcessoTrf(String valorComplementoClasseProcessoTrf) {
		this.valorComplementoClasseProcessoTrf = valorComplementoClasseProcessoTrf;
	}

	@Override
	public String toString() {
		return complementoClasse + ": " + valorComplementoClasseProcessoTrf;
	}

	@Transient
	public String getComplementoClasseStr() {
		return complementoClasse.getComplementoClasse();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ComplementoClasseProcessoTrf)) {
			return false;
		}
		ComplementoClasseProcessoTrf other = (ComplementoClasseProcessoTrf) obj;
		if (getIdComplementoClasseProcessoTrf() != other.getIdComplementoClasseProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdComplementoClasseProcessoTrf();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ComplementoClasseProcessoTrf> getEntityClass() {
		return ComplementoClasseProcessoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdComplementoClasseProcessoTrf());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
