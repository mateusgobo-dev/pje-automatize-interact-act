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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.OrdemCreditoEnum;
import br.jus.pje.jt.enums.TipoCredorEnum;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class CredorRubrica
 * @description Classe que representa um tipo de credor suportado por
 *              determinado tipo de rubrica.
 */

@Entity
@Table(name = "tb_credor_rubrica")
@org.hibernate.annotations.GenericGenerator(name = "gen_credor_rubrica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_credor_rubrica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CredorRubrica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<CredorRubrica,Long> {

	private static final long serialVersionUID = 1L;

	private Long id;
	private TipoRubrica tipoRubrica;
	private TipoCredorEnum tipoCredor;
	private OrdemCreditoEnum ordemCredito;

	@Id
	@GeneratedValue(generator = "gen_credor_rubrica")
	@Column(name = "id_credor_rubrica", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_rubrica", nullable = false)
	@ForeignKey(name = "fk_tb_cred_rubr_tb_tipo_rubr")
	public TipoRubrica getTipoRubrica() {
		return tipoRubrica;
	}

	public void setTipoRubrica(TipoRubrica tipoRubrica) {
		this.tipoRubrica = tipoRubrica;
	}

	@Column(name = "tp_tipo_credor", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoCredorEnum getTipoCredor() {
		return tipoCredor;
	}

	public void setTipoCredor(TipoCredorEnum tipoCredor) {
		this.tipoCredor = tipoCredor;
	}

	@Column(name = "tp_ordem_credito", length = 1, nullable = false)
	@Enumerated(EnumType.STRING)
	public OrdemCreditoEnum getOrdemCredito() {
		return ordemCredito;
	}

	public void setOrdemCredito(OrdemCreditoEnum ordemCredito) {
		this.ordemCredito = ordemCredito;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CredorRubrica> getEntityClass() {
		return CredorRubrica.class;
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
