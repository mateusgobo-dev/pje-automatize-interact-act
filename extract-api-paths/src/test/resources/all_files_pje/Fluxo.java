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

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "tb_fluxo")
@org.hibernate.annotations.GenericGenerator(name = "gen_fluxo", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_fluxo"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Fluxo implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Fluxo,Integer>{

	private static final long serialVersionUID = 1L;

	private int idFluxo;
	private Usuario usuarioPublicacao;
	private String codFluxo;
	private String fluxo;
	private Boolean ativo;
	private Integer qtPrazo;
	private Boolean publicado;
	private Date dataInicioPublicacao;
	private Date dataFimPublicacao;
	private Date ultimaPublicacao;
	private String xml;
	private List<Tarefa> tarefas;

	public Fluxo(){
	}

	@Id
	@GeneratedValue(generator = "gen_fluxo")
	@Column(name = "id_fluxo", unique = true, nullable = false)
	public int getIdFluxo(){
		return this.idFluxo;
	}

	public void setIdFluxo(int idFluxo){
		this.idFluxo = idFluxo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_publicacao")
	public Usuario getUsuarioPublicacao(){
		return this.usuarioPublicacao;
	}

	public void setUsuarioPublicacao(Usuario usuarioPublicacao){
		this.usuarioPublicacao = usuarioPublicacao;
	}

	@Column(name = "cd_fluxo", length = 30)
	@Length(max = 30)
	public String getCodFluxo(){
		return this.codFluxo;
	}

	public void setCodFluxo(String codFluxo){
		if (codFluxo != null){
			codFluxo = codFluxo.trim();
		}
		this.codFluxo = codFluxo;
	}

	@Column(name = "ds_fluxo", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getFluxo(){
		return this.fluxo;
	}

	public void setFluxo(String fluxo){
		if (fluxo != null){
			fluxo = fluxo.trim();
		}
		this.fluxo = fluxo;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_xml")
	public String getXml(){
		return this.xml;
	}

	public void setXml(String xml){
		this.xml = xml;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo(){
		return this.ativo;
	}

	public void setAtivo(Boolean ativo){
		this.ativo = ativo;
	}

	@Column(name = "qt_prazo")
	public Integer getQtPrazo(){
		return this.qtPrazo;
	}

	public void setQtPrazo(Integer qtPrazo){
		this.qtPrazo = qtPrazo;
	}

	@Column(name = "in_publicado", nullable = false)
	@NotNull
	public Boolean getPublicado(){
		return this.publicado;
	}

	public void setPublicado(Boolean publicado){
		this.publicado = publicado;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_inicio_publicacao")
	public Date getDataInicioPublicacao(){
		return this.dataInicioPublicacao;
	}

	public void setDataInicioPublicacao(Date dataInicioPublicacao){
		this.dataInicioPublicacao = dataInicioPublicacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_fim_publicacao")
	public Date getDataFimPublicacao(){
		return this.dataFimPublicacao;
	}

	public void setDataFimPublicacao(Date dataFimPublicacao){
		this.dataFimPublicacao = dataFimPublicacao;
	}

	@Override
	public String toString(){
		return fluxo;
	}

	@OneToMany(mappedBy = "fluxo", fetch = FetchType.LAZY)
	public List<Tarefa> getTarefas() {
		return tarefas;
	}

	public void setTarefas(List<Tarefa> tarefas) {
		this.tarefas = tarefas;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!(obj instanceof Fluxo)){
			return false;
		}
		Fluxo other = (Fluxo) obj;
		if (getIdFluxo() != other.getIdFluxo()){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdFluxo();
		return result;
	}

	public void setUltimaPublicacao(Date ultimaPublicacao){
		this.ultimaPublicacao = ultimaPublicacao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_publicacao")
	public Date getUltimaPublicacao(){
		return ultimaPublicacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Fluxo> getEntityClass() {
		return Fluxo.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdFluxo());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
