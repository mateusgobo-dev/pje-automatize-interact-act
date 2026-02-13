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

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Range;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class Rubrica
 * @description Classe que representa uma rubrica em uma obrigacao de pagar.
 *              Essa rubrica eh comum a tofos os credores e devedores das
 *              obrigacoes de pagar atomicas associadas a obrigacao de pagar.
 */

@Entity
@Table(name = "tb_rubrica")
@org.hibernate.annotations.GenericGenerator(name = "gen_rubrica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_rubrica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Rubrica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Rubrica,Long>, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2431433950932552110L;
	private Long id;
	private BigDecimal valor;
	private Date dataCalculo;
	private String descricao;
	private TipoRubrica tipoRubrica;
	private ObrigacaoPagar obrigacaoPagar;

	public Rubrica() {
	}

	public Rubrica(TipoRubrica tipoRubrica) {
		this.tipoRubrica = tipoRubrica;
	}

	@Id
	@GeneratedValue(generator = "gen_rubrica")
	@Column(name = "id_rubrica", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "vl_valor")
	@Range(max = 1000000000, min = 0)
	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	@Column(name = "dt_data_calculo", nullable = false)
	public Date getDataCalculo() {
		return dataCalculo;
	}

	public void setDataCalculo(Date dataCalculo) {
		this.dataCalculo = dataCalculo;
	}

	@Column(name = "ds_descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_rubrica", nullable = false)
	@ForeignKey(name = "fk_tb_rubr_tb_tipo_rubr")
	public TipoRubrica getTipoRubrica() {
		return tipoRubrica;
	}

	public void setTipoRubrica(TipoRubrica tipoRubrica) {
		this.tipoRubrica = tipoRubrica;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "id_obrigacao_pagar")
	@ForeignKey(name = "fk_tb_rubr_tb_obr_pagar")
	public ObrigacaoPagar getObrigacaoPagar() {
		return obrigacaoPagar;
	}

	public void setObrigacaoPagar(ObrigacaoPagar obrigacaoPagar) {
		this.obrigacaoPagar = obrigacaoPagar;
	}

	@Transient
	public String getDescricaoCompleta() {
		String descricaoCompleta = "";

		if (this.tipoRubrica != null && this.tipoRubrica.getDescricao() != null) {
			descricaoCompleta = descricaoCompleta + this.tipoRubrica.getDescricao();

			if (this.descricao != null) {
				descricaoCompleta = descricaoCompleta + " - " + this.descricao;
			}
		} else if (this.descricao != null) {
			descricaoCompleta = descricaoCompleta + this.descricao;
		}

		return descricaoCompleta;
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 */
	@Override
	public Rubrica clone() {
		Rubrica r = new Rubrica();
		r.dataCalculo = this.getDataCalculo();
		r.descricao = this.getDescricao();
		r.tipoRubrica = this.getTipoRubrica();
		r.valor = this.getValor();

		return r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descricao == null) ? 0 : descricao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Rubrica)) {
			return false;
		}

		try {
			Rubrica other = (Rubrica) obj;
			if (descricao == null && other.getDescricao() == null) {
				return false;
			} else if (descricao == null) {
				if (other.getDescricao() != null) {
					return false;
				}
			} else if (!descricao.equals(other.getDescricao())) {
				return false;
			}
		} catch (ClassCastException cce) {
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Rubrica> getEntityClass() {
		return Rubrica.class;
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
