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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.identidade.Papel;


@Entity
@Table(name = OficialJusticaCentralMandado.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_ofic_just_cntral_mandado", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ofic_just_cntral_mandado"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class OficialJusticaCentralMandado implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<OficialJusticaCentralMandado,Integer> {

	public static final String TABLE_NAME = "tb_of_just_central_mandado";
	private static final long serialVersionUID = 1L;

	private int idOficialJusticaCentralMandado;
	private CentralMandado centralMandado;
	private UsuarioLocalizacao usuarioLocalizacao;
	private Papel papel;
	private Localizacao localizacao;

	public OficialJusticaCentralMandado() {
	}

	@Id
	@GeneratedValue(generator = "gen_ofic_just_cntral_mandado")
	@Column(name = "id_of_justica_central_mandado", unique = true, nullable = false)
	public int getIdOficialJusticaCentralMandado() {
		return this.idOficialJusticaCentralMandado;
	}

	public void setIdOficialJusticaCentralMandado(int idOficialJusticaCentralMandado) {
		this.idOficialJusticaCentralMandado = idOficialJusticaCentralMandado;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_central_mandado", nullable = false)
	@NotNull
	public CentralMandado getCentralMandado() {
		return this.centralMandado;
	}

	public void setCentralMandado(CentralMandado centralMandado) {
		this.centralMandado = centralMandado;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario_localizacao")
	public UsuarioLocalizacao getUsuarioLocalizacao() {
		return this.usuarioLocalizacao;
	}

	public void setUsuarioLocalizacao(UsuarioLocalizacao usuarioLocalizacao) {
		this.usuarioLocalizacao = usuarioLocalizacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OficialJusticaCentralMandado)) {
			return false;
		}
		OficialJusticaCentralMandado other = (OficialJusticaCentralMandado) obj;
		if (getIdOficialJusticaCentralMandado() != other.getIdOficialJusticaCentralMandado()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdOficialJusticaCentralMandado();
		return result;
	}
	
	@Transient
	public Papel getPapel() {
		return this.papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	@Transient
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends OficialJusticaCentralMandado> getEntityClass() {
		return OficialJusticaCentralMandado.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdOficialJusticaCentralMandado());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
