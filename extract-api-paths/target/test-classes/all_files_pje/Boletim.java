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
package br.jus.pje.jt.entidades.estatistica;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.OrderBy;

/**
 * @author Sérgio Pacheco / Sérgio Simoes
 * @since 1.4.3
 * @category PJE-JT
 * @class Boletim
 * @description Classe que representa a definicao de um boletim de orgao julgador. 
 */
@Entity
@Table(name = Boletim.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_boletim", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_boletim"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Boletim implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Boletim,Integer> {

	public static final String TABLE_NAME = "tb_boletim";
	private static final long serialVersionUID = 1L;
	
	private Integer idBoletim;
	
	private String nome;
	 
	private Date ultimaGeracao;
	 
	private List<Quadro> quadros = new ArrayList<Quadro>(0);

	@Id
	@GeneratedValue(generator = "gen_boletim")
	@Column(name = "id_boletim", unique = true, nullable = false)
	public Integer getIdBoletim() {
		return idBoletim;
	}

	public void setIdBoletim(Integer idBoletim) {
		this.idBoletim = idBoletim;
	}

	@Column(name = "ds_nome", nullable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ultima_geracao")
	public Date getUltimaGeracao() {
		return ultimaGeracao;
	}

	public void setUltimaGeracao(Date ultimaGeracao) {
		this.ultimaGeracao = ultimaGeracao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "boletim")
	@OrderBy(clause = "nr_ordem" )
	public List<Quadro> getQuadros() {
		return quadros;
	}

	public void setQuadros(List<Quadro> quadros) {
		this.quadros = quadros;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Boletim)) {
			return false;
		}
		Boletim outroBoletim = (Boletim) obj;
		return getIdBoletim().equals(outroBoletim.getIdBoletim());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdBoletim() == null) ? 0 : getIdBoletim().hashCode());
		result = prime * result + ((getUltimaGeracao() == null) ? 0 : getUltimaGeracao().hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Boletim> getEntityClass() {
		return Boletim.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdBoletim();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
 
