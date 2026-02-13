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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.ParticipacaoObrigacaoEnum;
import br.jus.pje.jt.enums.TipoCredorEnum;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPericia;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class Credor
 * @description Classe que representa um credor em uma obrigacao de pagar
 *              atomica.
 */

@Entity
@Table(name = "tb_credor")
@PrimaryKeyJoinColumn(name = "id_participante_obrigacao")
@ForeignKey(name = "fk_tb_cred_tb_part_obr")
public class Credor extends ParticipanteObrigacao implements Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2102187707983660786L;
	private TipoCredorEnum tipoCredor;
	private ObrigacaoAtomica obrigacaoAtomica;

	public Credor() {
		super.setParticipacaoObrigacao(ParticipacaoObrigacaoEnum.C);
	}

	@Column(name = "tp_tipo_credor", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.csjt.pje.commons.model.type.TipoCredorType")
	public TipoCredorEnum getTipoCredor() {
		return tipoCredor;
	}

	public void setTipoCredor(TipoCredorEnum tipoCredor) {
		this.tipoCredor = tipoCredor;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "credor")
	public ObrigacaoAtomica getObrigacaoAtomica() {
		return obrigacaoAtomica;
	}

	public void setObrigacaoAtomica(ObrigacaoAtomica obrigacaoAtomica) {
		this.obrigacaoAtomica = obrigacaoAtomica;
	}

	@Override
	public void setProcessoParte(ProcessoParte processoParte) {
		super.setProcessoParte(processoParte);
		this.setTipoCredor(this.infereTipoCredor());
	}

	@Override
	public void setProcessoPericia(ProcessoPericia processoPericia) {
		super.setProcessoPericia(processoPericia);
		this.setTipoCredor(this.infereTipoCredor());
	}

	private TipoCredorEnum infereTipoCredor() {
		// Setar para o caso de o participante ser uma parte do processo
		if (isParte()) {
			ProcessoParte processoParte = this.getProcessoParte();

			TipoParte tipoParte = processoParte.getTipoParte();
			
	
			if (processoParte.getPartePrincipal() && processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
				return TipoCredorEnum.A; // AUTOR
			}

			if (processoParte.getPartePrincipal() && processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.P)) {
				return TipoCredorEnum.R; // RÉU
			}

			if ((tipoParte != null) && (tipoParte.getTipoParte().toUpperCase().contains("LEILOEIRO"))) { // TODO:
																											// Checar!!!
				return TipoCredorEnum.L; // LEILOEIRO
			}

			return null;

		} else if (isPerito()) {
			ProcessoPericia processoPericia = getProcessoPericia();

			Especialidade especialidadeRaiz = processoPericia.getEspecialidade().getEspecialidadeRaiz();

			if ((TipoCredorEnum.E).getCodigoEspecialidade().equals(especialidadeRaiz.getCodEspecialidade())) {
				return TipoCredorEnum.E; // PERITO ENGENHEIRO
			}
			if ((TipoCredorEnum.C).getCodigoEspecialidade().equals(especialidadeRaiz.getCodEspecialidade())) {
				return TipoCredorEnum.C; // PERITO CONTADOR
			}
			if ((TipoCredorEnum.D).getCodigoEspecialidade().equals(especialidadeRaiz.getCodEspecialidade())) {
				return TipoCredorEnum.D; // PERITO DOCUMENTOSCOPISTA
			}
			if ((TipoCredorEnum.I).getCodigoEspecialidade().equals(especialidadeRaiz.getCodEspecialidade())) {
				return TipoCredorEnum.I; // PERITO INTÉRPRETE
			}
			if ((TipoCredorEnum.M).getCodigoEspecialidade().equals(especialidadeRaiz.getCodEspecialidade())) {
				return TipoCredorEnum.M; // PERITO MÉDICO
			}
			return TipoCredorEnum.O; // OUTROS PERITOS
		}

		return TipoCredorEnum.U; // UNIÃO

	}

	/**
	 * @author Rafael Carvalho | Tiago Zanon
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 05/10/2011
	 */
	@Override
	public Credor clone() {
		Credor c = new Credor();
		c.tipoCredor = this.getTipoCredor();
		c.setParticipacaoObrigacao(this.getParticipacaoObrigacao());
		c.setProcessoParte(this.getProcessoParte());
		c.setProcessoPericia(this.getProcessoPericia());

		return c;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tipoCredor == null) ? 0 : tipoCredor.hashCode());
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
		if (!(obj instanceof Credor)) {
			return false;
		}
		try {
			Credor other = (Credor) obj;
			if (this.isParte()) {
				if (!this.getProcessoParte().equals(other.getProcessoParte())) {
					return false;
				}
			} else if (this.isPerito()) {
				if (other.getProcessoPericia() == null
						|| this.getProcessoPericia().getIdProcessoPericia() != other.getProcessoPericia()
								.getIdProcessoPericia()) {
					return false;
				}
			}
		} catch (ClassCastException cce) {
			return false;
		}
		return true;
	}
}