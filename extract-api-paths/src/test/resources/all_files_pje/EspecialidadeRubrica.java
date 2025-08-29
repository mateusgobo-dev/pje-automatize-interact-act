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
package br.jus.pje.jt.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.nucleo.entidades.Especialidade;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class EspecialidadeRubrica
 * @description Classe que representa uma especialidade mapeada em um
 *              determinado tipo de rubrica.
 */

@Entity
@Table(name = "tb_especialidade_rubrica")
@org.hibernate.annotations.GenericGenerator(name = "gen_especialidade_rubrica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_especialidade_rubrica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class EspecialidadeRubrica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<EspecialidadeRubrica,Long> {

	private static final long serialVersionUID = 1L;

	private Long id;
	private TipoRubrica tipoRubrica;
	private Especialidade especialidade;

	@Id
	@GeneratedValue(generator = "gen_especialidade_rubrica")
	@Column(name = "id_especialidade_rubrica", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_rubrica", nullable = false)
	@ForeignKey(name = "fk_tb_espe_rubr_tb_tipo_rubr")
	public TipoRubrica getTipoRubrica() {
		return tipoRubrica;
	}

	public void setTipoRubrica(TipoRubrica tipoRubrica) {
		this.tipoRubrica = tipoRubrica;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_especialidade", nullable = false)
	@ForeignKey(name = "fk_tb_espe_rubr_tb_especiali")
	public Especialidade getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends EspecialidadeRubrica> getEntityClass() {
		return EspecialidadeRubrica.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
