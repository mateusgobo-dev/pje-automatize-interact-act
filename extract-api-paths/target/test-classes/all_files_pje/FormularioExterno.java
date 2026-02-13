package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = FormularioExterno.TABLE_NAME)
@javax.persistence.Cacheable(false)
@org.hibernate.annotations.GenericGenerator(name = "gen_formulario_externo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_formulario_externo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class FormularioExterno implements Serializable, IEntidade<FormularioExterno, Integer>{

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "tb_formulario_externo";

	@Id
	@GeneratedValue(generator = "gen_formulario_externo")
	@Column(name = "id_formulario_externo", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "ds_nome", nullable = false)
	private String nome;
	
	@Column(name = "ds_url", nullable = false)
	private String url;
	
	@Column(name = "in_ativo", nullable = false)
	private Boolean ativo = Boolean.TRUE;
	
	@ManyToMany
	@JoinTable(
			name = "tb_classe_formulario", 
			joinColumns = @JoinColumn(name = "id_formulario_externo"), 
			inverseJoinColumns = @JoinColumn(name = "id_classe_judicial"))
	private List<ClasseJudicial> classesJudiciaisFormulario =  new ArrayList<ClasseJudicial>(0);
	
	public FormularioExterno() {
		super();
	}

	public FormularioExterno(Integer id, String nome, String url, Boolean ativo) {
		super();
		this.id = id;
		this.nome = nome;
		this.url = url;
		this.ativo = ativo;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public List<ClasseJudicial> getClassesJudiciaisFormulario() {
		return classesJudiciaisFormulario;
	}

	public void setClassesJudiciaisFormulario(List<ClasseJudicial> classesJudiciaisFormulario) {
		this.classesJudiciaisFormulario = classesJudiciaisFormulario;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.nome;
	}

	@Override
	@Transient
	public Class<? extends FormularioExterno> getEntityClass() {
		return FormularioExterno.class;
	}

	@Override
	@Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@Transient
	public boolean isLoggable() {
		return false;
	}

}
