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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.ParticipacaoObrigacaoEnum;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class Devedor
 * @description Classe que representa um devedor em uma obrigacao de pagar
 *              atomica.
 */

@Entity
@Table(name = "tb_devedor")
@PrimaryKeyJoinColumn(name = "id_participante_obrigacao")
@ForeignKey(name = "fk_tb_dev_tb_part_obr")
public class Devedor extends ParticipanteObrigacao implements Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8788263790465926354L;
	private Integer beneficioOrdem;
	private ObrigacaoAtomica obrigacaoAtomica;

	public Devedor() {
		super.setParticipacaoObrigacao(ParticipacaoObrigacaoEnum.D);
	}

	@Column(name = "nm_beneficio_ordem", nullable = false)
	public Integer getBeneficioOrdem() {
		return beneficioOrdem;
	}

	public void setBeneficioOrdem(Integer beneficioOrdem) {
		this.beneficioOrdem = beneficioOrdem;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "devedor")
	public ObrigacaoAtomica getObrigacaoAtomica() {
		return obrigacaoAtomica;
	}

	public void setObrigacaoAtomica(ObrigacaoAtomica obrigacaoAtomica) {
		this.obrigacaoAtomica = obrigacaoAtomica;
	}

	@Transient
	public String getResponsabilidade() {
		return null;
	}

	@Transient
	public Integer getExigibilidade() {
		return null;
	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 */
	@Override
	public Devedor clone() {
		Devedor d = new Devedor();
		d.beneficioOrdem = this.getBeneficioOrdem();
		d.setProcessoParte(this.getProcessoParte());
		d.setParticipacaoObrigacao(this.getParticipacaoObrigacao());

		return d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beneficioOrdem == null) ? 0 : beneficioOrdem.hashCode());
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
		if (!(obj instanceof Devedor)) {
			return false;
		}

		try {
			Devedor other = (Devedor) obj;

			if (!this.getProcessoParte().equals(other.getProcessoParte())) {
				return false;
			}
		} catch (ClassCastException cce) {
			return false;
		}
		return true;
	}
}
