package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.TipoParametroFiltroDinamico;

@Entity
@Table(name=FiltroDinamicoParametro.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_filtro_dinamico_parametro", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_filtro_dinamico_parametro"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class FiltroDinamicoParametro implements Serializable{
	private static final long serialVersionUID = -6370896550049487694L;

	public static final String TABLE_NAME = "tb_filtro_dinamico_parametro";
	
	@Id
	@GeneratedValue(generator = "gen_filtro_dinamico_parametro")
	@Column(name="id_filtro_dinamico_parametro")
	private Integer idParametro;
	@Column(name="ds_parametro")
	private String parametro;
	@Enumerated(EnumType.STRING)
	@Column(name = "tp_parametro", length = 3)
	private TipoParametroFiltroDinamico tipoParametro;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="id_filtro_dinamico_consulta")
	private FiltroDinamicoConsulta consulta;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="id_filtro_dinamico_entidade")
	private FiltroDinamicoEntidade entidade;
	
	public FiltroDinamicoParametro(){
		
	}
	public FiltroDinamicoParametro(String parametro, FiltroDinamicoConsulta consulta) {
		this.parametro = parametro;
		this.consulta = consulta;
	}
	public Integer getIdParametro() {
		return idParametro;
	}
	public void setIdParametro(Integer idParametro) {
		this.idParametro = idParametro;
	}
	public String getParametro() {
		return parametro;
	}
	public void setParametro(String parametro) {
		this.parametro = parametro;
	}
	public TipoParametroFiltroDinamico getTipoParametro() {
		return tipoParametro;
	}
	public void setTipoParametro(TipoParametroFiltroDinamico tipoParametro) {
		this.tipoParametro = tipoParametro;
	}
	public FiltroDinamicoEntidade getEntidade() {
		return entidade;
	}
	public void setEntidade(FiltroDinamicoEntidade entidade) {
		this.entidade = entidade;
	}
	public FiltroDinamicoConsulta getConsulta() {
		return consulta;
	}
	public void setConsulta(FiltroDinamicoConsulta consulta) {
		this.consulta = consulta;
	}
}