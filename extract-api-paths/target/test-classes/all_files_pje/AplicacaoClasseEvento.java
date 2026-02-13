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

// Generated 30/10/2008 07:40:27 by Hibernate Tools 3.2.0.CR1

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.SujeitoAtivoEnum;

/**
 * AplicacaoClasseEvento generate by Márlon Assis
 */
@Entity
@Table(name = AplicacaoClasseEvento.TABLE_NAME)
public class AplicacaoClasseEvento implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_aplicacao_classe_evento";
	private static final long serialVersionUID = 1L;

	private AplicacaoClasseEventoId id = new AplicacaoClasseEventoId();

	private AplicacaoClasse aplicacaoClasse;
	private Evento evento;
	private SujeitoAtivoEnum sujeitoAtivo = SujeitoAtivoEnum.M;
	private Boolean sujeitoMonocratico = Boolean.FALSE;;
	private Boolean sujeitoColegiado = Boolean.FALSE;;
	private Boolean sujeitoPresidente = Boolean.FALSE;;
	private Boolean sujeitoVice = Boolean.FALSE;;

	public AplicacaoClasseEvento() {
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "idEvento", column = @Column(name = "id_evento", nullable = false)),
			@AttributeOverride(name = "idAplicacaoClasse", column = @Column(name = "id_aplicacao_classe", nullable = false)) })
	@NotNull
	public AplicacaoClasseEventoId getId() {
		return this.id;
	}

	public void setId(AplicacaoClasseEventoId id) {
		if (evento != null) {
			id.setIdEvento(evento.getIdEvento());
		}
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aplicacao_classe", nullable = false, insertable = false, updatable = false)
	@NotNull
	public AplicacaoClasse getAplicacaoClasse() {
		return this.aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		if (aplicacaoClasse != null) {
			id.setIdAplicacaoClasse(aplicacaoClasse.getIdAplicacaoClasse());
		}
		this.aplicacaoClasse = aplicacaoClasse;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento", nullable = false, insertable = false, updatable = false)
	@NotNull
	public Evento getEvento() {
		return this.evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	@Column(name = "in_sujeito_ativo", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.SujeitoAtivoType")
	public SujeitoAtivoEnum getSujeitoAtivo() {
		return this.sujeitoAtivo;
	}

	public void setSujeitoAtivo(SujeitoAtivoEnum sujeitoAtivo) {
		this.sujeitoAtivo = SujeitoAtivoEnum.M;
	}

	@Column(name = "in_sujeito_monocratico")
	public Boolean getSujeitoMonocratico() {
		return sujeitoMonocratico;
	}

	public void setSujeitoMonocratico(Boolean sujeitoMonocratico) {
		this.sujeitoMonocratico = sujeitoMonocratico;
	}

	@Column(name = "in_sujeito_colegiado")
	public Boolean getSujeitoColegiado() {
		return sujeitoColegiado;
	}

	public void setSujeitoColegiado(Boolean sujeitoColegiado) {
		this.sujeitoColegiado = sujeitoColegiado;
	}

	@Column(name = "in_sujeito_presidente")
	public Boolean getSujeitoPresidente() {
		return sujeitoPresidente;
	}

	public void setSujeitoPresidente(Boolean sujeitoPresidente) {
		this.sujeitoPresidente = sujeitoPresidente;
	}

	@Column(name = "in_sujeito_vice")
	public Boolean getSujeitoVice() {
		return sujeitoVice;
	}

	public void setSujeitoVice(Boolean sujeitoVice) {
		this.sujeitoVice = sujeitoVice;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AplicacaoClasseEvento)) {
			return false;
		}
		AplicacaoClasseEvento other = (AplicacaoClasseEvento) obj;
		if (getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}