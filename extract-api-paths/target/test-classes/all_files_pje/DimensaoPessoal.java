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
@Table(name = "tb_dimensao_pessoal")
@org.hibernate.annotations.GenericGenerator(name = "gen_dimensao_pessoal", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_dimensao_pessoal"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DimensaoPessoal implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<DimensaoPessoal,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2494211079866772547L;

	private int idDimensaoPessoal;

	private String dimensaoPessoal;

	private List<DimensaoPessoalPessoa> pessoasAfetadasList = new ArrayList<DimensaoPessoalPessoa>(0);
	
	private List<DimensaoPessoalTipoPessoa> tiposDePessoasAfetadosList = new ArrayList<DimensaoPessoalTipoPessoa>(0);

	private Boolean ativo = true;
	
	private List<Competencia> competencias;
	
	@Id
	@GeneratedValue(generator = "gen_dimensao_pessoal")
	@Column(name = "id_dimensao_pessoal", unique = true, nullable = false)
	public int getIdDimensaoPessoal() {
		return idDimensaoPessoal;
	}

	public void setIdDimensaoPessoal(int idDimensaoPessoal) {
		this.idDimensaoPessoal = idDimensaoPessoal;
	}

	@Column(name = "ds_dimensao_pessoal", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getDimensaoPessoal() {
		return dimensaoPessoal;
	}

	public void setDimensaoPessoal(String dimensaoPessoal) {
		this.dimensaoPessoal = dimensaoPessoal;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "dimensaoPessoal")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<DimensaoPessoalTipoPessoa> getTiposDePessoasAfetadosList() {
		return tiposDePessoasAfetadosList;
	}

	public void setTiposDePessoasAfetadosList(List<DimensaoPessoalTipoPessoa> tiposDePessoasAfetadosList) {
		this.tiposDePessoasAfetadosList = tiposDePessoasAfetadosList;
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "dimensaoPessoal")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<DimensaoPessoalPessoa> getPessoasAfetadasList() {
		return pessoasAfetadasList;
	}

	public void setPessoasAfetadasList(List<DimensaoPessoalPessoa> pessoasAfetadasList) {
		this.pessoasAfetadasList = pessoasAfetadasList;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToMany
	@JoinTable(name = "tb_competencia_dpessoal", joinColumns = @JoinColumn(name = "id_dimensao_pessoal"), inverseJoinColumns = @JoinColumn(name = "id_competencia"))
	public List<Competencia> getCompetencias() {
		return competencias;
	}

	public void setCompetencias(List<Competencia> competencias) {
		this.competencias = competencias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdDimensaoPessoal();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DimensaoPessoal))
			return false;
		DimensaoPessoal other = (DimensaoPessoal) obj;
		if (getIdDimensaoPessoal() != other.getIdDimensaoPessoal())
			return false;
		return true;
	}

	/**
	 * Verifica se ao menos uma pessoa contida em uma lista estÃ¡ contida nesta
	 * dimensÃ£o.
	 * 
	 * @param pessoas
	 *            Pessoas que serão objetos da verificação
	 * @return true, se a lista contida nesta dimensão tiver ao menos uma das
	 *         pessoas indicadas no parâmetro da função. false, se as listas
	 *         de pessoas ou tipos de pessoas desta dimensão estiverem vazias
	 *         ou se nenhuma das pessoas ou tipos de pessoas das pessoas
	 *         indicadas no parâmetro estiver contida nessas listas.
	 */
	public boolean estaIncluida(Pessoa... pessoas) {
		if ((this.pessoasAfetadasList == null || this.pessoasAfetadasList.size() == 0)
				&& ((this.tiposDePessoasAfetadosList == null) || this.tiposDePessoasAfetadosList.size() == 0)) {
			return false;
		}
		if (this.pessoasAfetadasList != null && this.pessoasAfetadasList.size() != 0) {
			for (Pessoa p : pessoas) {
				for(DimensaoPessoalPessoa dpp: this.pessoasAfetadasList){
					if(dpp.getPessoa().getIdUsuario().equals(p.getIdUsuario())){
						return true;
					}
				}
			}
		}
		if (this.tiposDePessoasAfetadosList != null && this.tiposDePessoasAfetadosList.size() != 0) {
			for (Pessoa p : pessoas) {
				for(DimensaoPessoalTipoPessoa dpt: this.tiposDePessoasAfetadosList){
					if(dpt.getIdDimensaoPessoalTipoPessoa().equals(p.getTipoPessoa().getIdTipoPessoa())){
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return dimensaoPessoal;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DimensaoPessoal> getEntityClass() {
		return DimensaoPessoal.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDimensaoPessoal());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
