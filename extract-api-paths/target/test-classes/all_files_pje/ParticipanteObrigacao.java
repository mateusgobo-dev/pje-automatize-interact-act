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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.ParticipacaoObrigacaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoPericia;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class ParticipanteObrigacao
 * @description Classe que representa um participante em uma obrigacao de pagar
 *              atomica. Ele pode ser do tipo credor ou devedor.
 */

@Entity
@Table(name = "tb_participante_obrigacao")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.GenericGenerator(name = "gen_participante_obrigacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_participante_obrigacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public abstract class ParticipanteObrigacao implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private ProcessoParte processoParte;
	private ProcessoPericia processoPericia;
	private ParticipacaoObrigacaoEnum participacaoObrigacao;

	@Id
	@GeneratedValue(generator = "gen_participante_obrigacao")
	@Column(name = "id_participante_obrigacao", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_parte")
	@ForeignKey(name = "fk_tb_part_tb_proc_parte")
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	/**
	 * Não pode haver um processo perito e um processo parte para participante
	 * obrigação
	 * 
	 * @param processoPericia
	 */
	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_pericia")
	@ForeignKey(name = "fk_tb_part_tb_proc_pericia")
	public ProcessoPericia getProcessoPericia() {
		return processoPericia;
	}

	/**
	 * Não pode haver um processo perito e um processo parte para participante
	 * obrigação
	 * 
	 * @param processoPericia
	 */
	public void setProcessoPericia(ProcessoPericia processoPericia) {
		this.processoPericia = processoPericia;
	}

	@Column(name = "cd_participacao_obrigacao")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.csjt.pje.commons.model.type.ParticipacaoObrigacaoType")
	public ParticipacaoObrigacaoEnum getParticipacaoObrigacao() {
		return participacaoObrigacao;
	}

	public void setParticipacaoObrigacao(ParticipacaoObrigacaoEnum participacaoObrigacao) {
		this.participacaoObrigacao = participacaoObrigacao;
	}

	@Transient
	public String getNome() {
		String nome = null;

		if (isParte()) {
			nome = processoParte.getNomeParte();

		} else if (isPerito()) {
			nome = processoPericia.getPessoaPerito().getNome();
		} else {
			nome = "União";
		}
		return nome;
	}

	@Transient
	public String getPolo() {
		String polo = null;

		if (processoParte != null) {
			polo = processoParte.getPolo();

		} else {
			polo = "União";
		}
		return polo;
	}

	@Transient
	public boolean isPerito() {
		return processoPericia != null;
	}

	@Transient
	public boolean isParte() {
		return processoParte != null;
	}

	@Transient
	public String getTipoParte() {
		if (isPerito()) {
			return getProcessoPericia().getEspecialidade().getEspecialidade();
		} else if (isParte()) {
			return getProcessoParte().getTipoParte().getTipoParte();
		}

		return null;
	}

}
