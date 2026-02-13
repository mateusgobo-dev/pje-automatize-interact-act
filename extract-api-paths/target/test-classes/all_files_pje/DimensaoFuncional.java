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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Length;


/**
 * @author paulo.cristovao
 * 
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_dimensao_funcional")
@org.hibernate.annotations.GenericGenerator(name = "gen_dimensao_funcional", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dimensao_funcional"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DimensaoFuncional implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DimensaoFuncional,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2806715686480087526L;

	private int idDimensaoFuncional;

	private String dimensaoFuncional;
	
	private List<AutoridadeAfetada> autoridadesAfetadas = new ArrayList<AutoridadeAfetada>(0);

	private List<Competencia> competencias = new ArrayList<Competencia>(0);

	private Boolean ativo = true;

	@Id
	@GeneratedValue(generator = "gen_dimensao_funcional")
	@Column(name = "id_dimensao_funcional", unique = true, nullable = false)
	public int getIdDimensaoFuncional() {
		return idDimensaoFuncional;
	}

	public void setIdDimensaoFuncional(int idDimensaoFuncional) {
		this.idDimensaoFuncional = idDimensaoFuncional;
	}

	@Column(name = "ds_dimensao_funcional", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getDimensaoFuncional() {
		return dimensaoFuncional;
	}

	public void setDimensaoFuncional(String dimensaoFuncional) {
		this.dimensaoFuncional = dimensaoFuncional;
	}

	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="dimensaoFuncional")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<AutoridadeAfetada> getAutoridadesAfetadas() {
		return autoridadesAfetadas;
	}

	public void setAutoridadesAfetadas(List<AutoridadeAfetada> autoridadesAfetadas) {
		this.autoridadesAfetadas = autoridadesAfetadas;
	}

	@ManyToMany
	@JoinTable(name = "tb_competencia_dfuncional", joinColumns = @JoinColumn(name = "id_dimensao_funcional"), inverseJoinColumns = @JoinColumn(name = "id_competencia"))
	public List<Competencia> getCompetencias() {
		return competencias;
	}

	public void setCompetencias(List<Competencia> competencias) {
		this.competencias = competencias;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdDimensaoFuncional();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DimensaoFuncional))
			return false;
		DimensaoFuncional other = (DimensaoFuncional) obj;
		if (getIdDimensaoFuncional() != other.getIdDimensaoFuncional())
			return false;
		return true;
	}

	/**
	 * Verifica se uma ou mais pessoas estão incluí­das nessa dimensão.
	 * 
	 * @param pessoas
	 *            uma ou mais pessoas para comparação.
	 * @return true se ao menos uma das pessoas passadas como parâmetros for
	 *         autoridade contida na lista de autoridades desta dimensão.
	 *         false, se nenhuma pessoa for autoridade contida na lista ou se a
	 *         lista desta dimensão estiver vazia.
	 */
	public boolean estaIncluido(Pessoa... pessoas) {
		List<AutoridadeAfetada> autoridades = getAutoridadesAfetadas();
		if (autoridades == null || autoridades.size() == 0)
			return false;
		for (Pessoa p : pessoas) {
			if (p instanceof PessoaAutoridade){
				for(AutoridadeAfetada aa: autoridades){
					if(aa.getAutoridade().equals(p)){
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return dimensaoFuncional;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DimensaoFuncional> getEntityClass() {
		return DimensaoFuncional.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDimensaoFuncional());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
