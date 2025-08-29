package br.jus.pje.nucleo.entidades;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name=FiltroDinamicoConsulta.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_filtro_dinamico_consulta", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_filtro_dinamico_consulta"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class FiltroDinamicoConsulta implements Serializable{
	private static final long serialVersionUID = -398855788040283231L;
	
	public static final String TABLE_NAME = "tb_filtro_dinamico_consulta";
	
	@Id
	@GeneratedValue(generator = "gen_filtro_dinamico_consulta")
	@Column(name="id_filtro_dinamico_consulta")
	private Integer idConsulta;
	@Column(name="ds_nome_consulta")
	private String nomeConsulta;
	@Column(name="ds_hql")
	private String hql;
	@Column(name="ds_nome_funcionalidade")
	private String funcionalidade;
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER, mappedBy="consulta")
	private List<FiltroDinamicoParametro> parametros;
	
	public Integer getIdConsulta() {
		return idConsulta;
	}
	public void setIdConsulta(Integer idConsulta) {
		this.idConsulta = idConsulta;
	}
	public String getNomeConsulta() {
		return nomeConsulta;
	}
	public void setNomeConsulta(String nomeConsulta) {
		this.nomeConsulta = nomeConsulta;
	}
	public String getHql() {
		return hql;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
	public String getFuncionalidade() {
		return funcionalidade;
	}
	public void setFuncionalidade(String funcionalidade) {
		this.funcionalidade = funcionalidade;
	}
	public List<FiltroDinamicoParametro> getParametros() {
		return parametros;
	}
	public void setParametros(List<FiltroDinamicoParametro> parametros) {
		this.parametros = parametros;
	}
}