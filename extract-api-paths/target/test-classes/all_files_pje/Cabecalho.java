package br.jus.pje.nucleo.entidades.editor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tb_cabecalho")
@org.hibernate.annotations.GenericGenerator(name = "gen_cabecalho", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_cabecalho"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Cabecalho implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<Cabecalho,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idCabecalho;
	private String cabecalho;
	private String conteudo;
	private Boolean ativo = true;

	@Id
	@GeneratedValue(generator = "gen_cabecalho")
	@Column(name = "id_cabecalho", unique = true, nullable = false)
	public Integer getIdCabecalho() {
		return idCabecalho;
	}

	public void setIdCabecalho(Integer idCabecalho) {
		this.idCabecalho = idCabecalho;
	}

	@Column(name = "ds_cabecalho", length = 100, nullable = false)
	public String getCabecalho() {
		return cabecalho;
	}

	public void setCabecalho(String cabecalho) {
		this.cabecalho = cabecalho;
	}

	@Column(name = "ds_conteudo", nullable = false)
	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	@Column(name = "in_ativo", nullable = false)
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
		result = prime * result + getIdCabecalho();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Cabecalho))
			return false;
		Cabecalho other = (Cabecalho) obj;
		if (getIdCabecalho() == null || !getIdCabecalho().equals(other.getIdCabecalho()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return cabecalho;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Cabecalho> getEntityClass() {
		return Cabecalho.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdCabecalho();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
