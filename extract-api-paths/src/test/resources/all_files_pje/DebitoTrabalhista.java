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
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.ProcessoParte;

/**
 * Classe de representa um débito trabalhista no Cadastro Nacional de Débito
 * Trabalhista
 * 
 * @author kelly leal
 * @since versão 1.2.3
 * @category PJE-JT
 */
@Entity
@Table(name = DebitoTrabalhista.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_debito_trabalhista", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_debito_trabalhista"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class DebitoTrabalhista implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<DebitoTrabalhista,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3931644883136917939L;

	public static final String TABLE_NAME = "tb_debito_trabalhista";

	private int idDebitoTrabalhista;
	private ProcessoParte processoParte;
	private SituacaoDebitoTrabalhista situacaoDebitoTrabalhista;

	@Id
	@GeneratedValue(generator = "gen_debito_trabalhista")
	@Column(name = "id_debito_trabalhista", unique = true, nullable = false)
	public int getIdDebitoTrabalhista() {
		return idDebitoTrabalhista;
	}

	public void setIdDebitoTrabalhista(int idDebitoTrabalhista) {
		this.idDebitoTrabalhista = idDebitoTrabalhista;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte", nullable = false)
	@NotNull
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_situacao_debito_trabalhista", nullable = false)
	@NotNull
	public SituacaoDebitoTrabalhista getSituacaoDebitoTrabalhista() {
		return situacaoDebitoTrabalhista;
	}

	public void setSituacaoDebitoTrabalhista(SituacaoDebitoTrabalhista situacao) {
		this.situacaoDebitoTrabalhista = situacao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends DebitoTrabalhista> getEntityClass() {
		return DebitoTrabalhista.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdDebitoTrabalhista());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
