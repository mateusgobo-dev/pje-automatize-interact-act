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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.CaracteristicaFisicaEnum;


@Entity
@Table(name = CaracteristicaFisica.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_caracteristica_fisica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_caracteristica_fisica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CaracteristicaFisica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CaracteristicaFisica,Integer> {	

	private static final long serialVersionUID = 7469222948472085763L;

	public static final String TABLE_NAME = "tb_caracteristica_fisica";
	
	private Integer id;
	private PessoaFisica pessoaFisica;
	private CaracteristicaFisicaEnum caracteristicaFisica;
	
	
	@Id
	@GeneratedValue(generator = "gen_caracteristica_fisica")
	@Column(name = "id_caracteristica_fisica", unique = true, nullable = false)	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "id_pessoa_fisica", nullable = false)
	public PessoaFisica getPessoaFisica() {
		return pessoaFisica;
	}
	
	public void setPessoaFisica(PessoaFisica pessoaFisica) {
		this.pessoaFisica = pessoaFisica;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaFisica(PessoaFisica)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaFisica(PessoaFisicaEspecializada pessoa){
		setPessoaFisica(pessoa != null ? pessoa.getPessoa() : (PessoaFisica) null);
	}
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "cd_caracteristica_fisica", nullable = false)
	public CaracteristicaFisicaEnum getCaracteristicaFisica() {
		return caracteristicaFisica;
	}
	
	public void setCaracteristicaFisica(
			CaracteristicaFisicaEnum caracteristicaFisica) {
		this.caracteristicaFisica = caracteristicaFisica;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CaracteristicaFisica	> getEntityClass() {
		return CaracteristicaFisica	.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
