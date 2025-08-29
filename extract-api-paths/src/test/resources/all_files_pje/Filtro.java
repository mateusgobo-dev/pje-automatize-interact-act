package br.jus.pje.nucleo.entidades;

import java.util.List;
import java.util.Set;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tb_filtro")
@SequenceGenerator(allocationSize = 1, name = "gen_filtro", sequenceName = "sq_tb_filtro")
public class Filtro implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "gen_filtro")
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	@Column(name = "ds_filtro", length = 100)
	private String nomeFiltro;
	@Column(name = "id_localizacao")
	private Integer idLocalizacao;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "id_filtro")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Set<CriterioFiltro> criterios;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinTable(name = "tb_filtro_tag", joinColumns = @JoinColumn(name = "id_filtro"), inverseJoinColumns = @JoinColumn(name = "id_tag"))
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private List<Tag> tags;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNomeFiltro() {
		return nomeFiltro;
	}

	public void setNomeFiltro(String nomeFiltro) {
		this.nomeFiltro = nomeFiltro;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public Integer getIdLocalizacao() {
		return idLocalizacao;
	}

	public void setIdLocalizacao(Integer idLocalizacao) {
		this.idLocalizacao = idLocalizacao;
	}

	public Set<CriterioFiltro> getCriterios() {
		return criterios;
	}

	public void setCriterios(Set<CriterioFiltro> criterios) {
		this.criterios = criterios;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((idLocalizacao == null) ? 0 : idLocalizacao.hashCode());
		result = prime * result + ((nomeFiltro == null) ? 0 : nomeFiltro.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Filtro other = (Filtro) obj;
		if (idLocalizacao == null) {
			if (other.idLocalizacao != null)
				return false;
		} else if (!idLocalizacao.equals(other.idLocalizacao))
			return false;
		if (nomeFiltro == null) {
			if (other.nomeFiltro != null)
				return false;
		} else if (!nomeFiltro.equals(other.nomeFiltro))
			return false;
		return true;
	}
}