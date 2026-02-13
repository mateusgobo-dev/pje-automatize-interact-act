package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=FiltroDinamicoEntidade.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_filtro_dinamico_entidade", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_filtro_dinamico_entidade"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class FiltroDinamicoEntidade implements Serializable{
	private static final long serialVersionUID = -398855788040283231L;
	
	public static final String TABLE_NAME = "tb_filtro_dinamico_entidade";
	
	@Id
	@GeneratedValue(generator = "gen_filtro_dinamico_entidade")
	@Column(name="id_filtro_dinamico_entidade")
	private Integer idEntidade;
	@Column(name="ds_entidade")
	private String entidade;
	@Column(name="ds_nome_atributo_ordenacao")
	private String atributoOrdenacao;
	
	public Integer getIdEntidade() {
		return idEntidade;
	}
	public void setIdEntidade(Integer idEntidade) {
		this.idEntidade = idEntidade;
	}
	public String getEntidade() {
		return entidade;
	}
	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}
	public String getAtributoOrdenacao() {
		return atributoOrdenacao;
	}
	public void setAtributoOrdenacao(String atributoOrdenacao) {
		this.atributoOrdenacao = atributoOrdenacao;
	}
}