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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.identidade.Papel;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = QuadroAvisoPapel.TABLENAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_quadro_aviso_papel", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_quadro_aviso_papel"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class QuadroAvisoPapel implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<QuadroAvisoPapel,Integer> {

	public static final String TABLENAME = "tb_quadro_aviso_papel";
	private static final long serialVersionUID = 1L;

	private int idQuadroAvisoPapel;
	private QuadroAviso quadroAviso;
	private Papel papel;

	@Id
	@GeneratedValue(generator = "gen_quadro_aviso_papel")
	@Column(name = "id_quadro_aviso_papel", unique = true, nullable = false)
	public int getIdQuadroAvisoPapel() {
		return idQuadroAvisoPapel;
	}

	public void setIdQuadroAvisoPapel(int idQuadroAvisoPapel) {
		this.idQuadroAvisoPapel = idQuadroAvisoPapel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_quadro_aviso")
	public QuadroAviso getQuadroAviso() {
		return quadroAviso;
	}

	public void setQuadroAviso(QuadroAviso quadroAviso) {
		this.quadroAviso = quadroAviso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_papel")
	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof QuadroAvisoPapel)) {
			return false;
		}
		QuadroAvisoPapel other = (QuadroAvisoPapel) obj;
		if (getIdQuadroAvisoPapel() != other.getIdQuadroAvisoPapel()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdQuadroAvisoPapel();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends QuadroAvisoPapel> getEntityClass() {
		return QuadroAvisoPapel.class;
	}
	
	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdQuadroAvisoPapel());
	}
	
	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}
}
