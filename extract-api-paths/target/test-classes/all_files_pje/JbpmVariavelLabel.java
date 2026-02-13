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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_jbpm_variavel_label")
@org.hibernate.annotations.GenericGenerator(name = "gen_jbpm_variavel_label", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_jbpm_variavel_label"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class JbpmVariavelLabel implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<JbpmVariavelLabel,Integer> {

	private static final long serialVersionUID = 1L;

	private int idJbpmVariavelLabel;
	private String nomeVariavel;
	private String nomeTarefa;
	private String nomeFluxo;
	private String labelVariavel;

	public JbpmVariavelLabel() {
	}

	@Id
	@GeneratedValue(generator = "gen_jbpm_variavel_label")
	@Column(name = "id_jbpm_variavel_label", unique = true, nullable = false)
	public int getIdJbpmVariavelLabel() {
		return this.idJbpmVariavelLabel;
	}

	public void setIdJbpmVariavelLabel(int idJbpmVariavelLabel) {
		this.idJbpmVariavelLabel = idJbpmVariavelLabel;
	}

	@Column(name = "nm_variavel", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getNomeVariavel() {
		return this.nomeVariavel;
	}

	public void setNomeVariavel(String variavel) {
		this.nomeVariavel = variavel;
	}

	@Column(name = "ds_label_variavel", nullable = false, length = 1000)
	@NotNull
	@Length(max = 1000)
	public String getLabelVariavel() {
		return this.labelVariavel;
	}

	public void setLabelVariavel(String valorVariavel) {
		this.labelVariavel = valorVariavel;
	}

	@Override
	public String toString() {
		return nomeVariavel;
	}

	public void setNomeTarefa(String nomeTarefa) {
		this.nomeTarefa = nomeTarefa;
	}

	@Column(name = "ds_nome_tarefa", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getNomeTarefa() {
		return nomeTarefa;
	}

	public void setNomeFluxo(String nomeFluxo) {
		this.nomeFluxo = nomeFluxo;
	}

	@Column(name = "ds_nome_fluxo", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getNomeFluxo() {
		return nomeFluxo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof JbpmVariavelLabel)) {
			return false;
		}
		JbpmVariavelLabel other = (JbpmVariavelLabel) obj;
		if (getIdJbpmVariavelLabel() != other.getIdJbpmVariavelLabel()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdJbpmVariavelLabel();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends JbpmVariavelLabel> getEntityClass() {
		return JbpmVariavelLabel.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdJbpmVariavelLabel());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
