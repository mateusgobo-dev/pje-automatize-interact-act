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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SexoAmbosEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_prioridade_processo")
@org.hibernate.annotations.GenericGenerator(name = "gen_crim_prioridade_processo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_crim_prioridade_processo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PrioridadeProcesso implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PrioridadeProcesso,Integer> {

	private static final long serialVersionUID = 1L;

	private int idPrioridadeProcesso;
	private String prioridade;
	private Integer valorPeso;
	private Integer valorIdadeMinima;
	private Integer valorIdadeMaxima;
	private Boolean ativo;
	private Boolean motivo;
	private String caminhoIcone;
	private String descricaoIcone;
	private String cssAutos;
	private String cssTarefas;
	private SexoAmbosEnum sexo;

	public PrioridadeProcesso() {
	}

	@Id
	@GeneratedValue(generator = "gen_crim_prioridade_processo")
	@Column(name = "id_prioridade_processo", unique = true, nullable = false)
	public int getIdPrioridadeProcesso() {
		return this.idPrioridadeProcesso;
	}

	public void setIdPrioridadeProcesso(int idPrioridadeProcesso) {
		this.idPrioridadeProcesso = idPrioridadeProcesso;
	}

	@Column(name = "ds_prioridade", nullable = false, length = 50, unique = true)
	@NotNull
	@Length(max = 50)
	public String getPrioridade() {
		return this.prioridade;
	}

	public void setPrioridade(String prioridade) {
		this.prioridade = prioridade;
	}

	@Column(name = "vl_peso")
	public Integer getValorPeso() {
		return this.valorPeso;
	}

	public void setValorPeso(Integer valorPeso) {
		this.valorPeso = valorPeso;
	}

	@Column(name = "vl_idade_minima")
	public Integer getValorIdadeMinima() {
		return this.valorIdadeMinima;
	}

	public void setValorIdadeMinima(Integer valorIdadeMinima) {
		this.valorIdadeMinima = valorIdadeMinima;
	}

	@Column(name = "vl_idade_maxima")
	public Integer getValorIdadeMaxima() {
		return this.valorIdadeMaxima;
	}

	public void setValorIdadeMaxima(Integer valorIdadeMaxima) {
		this.valorIdadeMaxima = valorIdadeMaxima;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_motivo", nullable = false)
	@NotNull
	public Boolean getMotivo() {
		return this.motivo;
	}

	public void setMotivo(Boolean motivo) {
		this.motivo = motivo;
	}

	@Column(name = "in_sexo", nullable = false)
	@Enumerated(EnumType.STRING)
	public SexoAmbosEnum getSexo() {
		return this.sexo;
	}

	public void setSexo(SexoAmbosEnum sexo) {
		this.sexo = sexo;
	}

	@Override
	public String toString() {
		return prioridade;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PrioridadeProcesso)) {
			return false;
		}
		PrioridadeProcesso other = (PrioridadeProcesso) obj;
		if (getIdPrioridadeProcesso() != other.getIdPrioridadeProcesso()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPrioridadeProcesso();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PrioridadeProcesso> getEntityClass() {
		return PrioridadeProcesso.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPrioridadeProcesso());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Column(name = "ds_icone_caminho")
	public String getCaminhoIcone() {
		return caminhoIcone;
	}

	public void setCaminhoIcone(String caminhoIcone) {
		this.caminhoIcone = caminhoIcone;
	}

	@Column(name = "ds_icone_label")
	public String getDescricaoIcone() {
		return descricaoIcone;
	}

	public void setDescricaoIcone(String descricaoIcone) {
		this.descricaoIcone = descricaoIcone;
	}

	@Column(name = "ds_icone_css_autos")
	public String getCssAutos() {
		return cssAutos;
	}

	public void setCssAutos(String cssAutos) {
		this.cssAutos = cssAutos;
	}

	@Column(name = "ds_icone_css_tarefa")
	public String getCssTarefas() {
		return cssTarefas;
	}

	public void setCssTarefas(String cssTarefas) {
		this.cssTarefas = cssTarefas;
	}

}
