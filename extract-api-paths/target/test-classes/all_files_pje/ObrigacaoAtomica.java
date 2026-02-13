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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class ObrigacaoAtomica
 * @description Classe que representa uma das relacoes entre um credor e um
 *              devedor de uma obrigacao de pagar.
 */

@Entity
@Table(name = "tb_obrigacao_atomica")
@org.hibernate.annotations.GenericGenerator(name = "gen_ogrigacao_atomica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ogrigacao_atomica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ObrigacaoAtomica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ObrigacaoAtomica,Long> {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Devedor devedor;
	private Credor credor;
	private ObrigacaoPagar obrigacaoPagar;

	@Id
	@GeneratedValue(generator = "gen_ogrigacao_atomica")
	@Column(name = "id_obrigacao_atomica", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "id_devedor")
	@ForeignKey(name = "fk_tb_obrig_atom_tb_dev")
	public Devedor getDevedor() {
		return devedor;
	}

	public void setDevedor(Devedor devedor) {
		this.devedor = devedor;
	}

	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "id_credor")
	@ForeignKey(name = "fk_tb_obrig_atom_tb_cred")
	public Credor getCredor() {
		return credor;
	}

	public void setCredor(Credor credor) {
		this.credor = credor;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "id_obrigacao_pagar")
	@ForeignKey(name = "fk_tb_obrig_atom_tb_obrig_pagar")
	public ObrigacaoPagar getObrigacaoPagar() {
		return obrigacaoPagar;
	}

	public void setObrigacaoPagar(ObrigacaoPagar obrigacaoPagar) {
		this.obrigacaoPagar = obrigacaoPagar;
	}

	@Transient
	public BigDecimal getTotal() {
		return null;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ObrigacaoAtomica> getEntityClass() {
		return ObrigacaoAtomica.class;
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
