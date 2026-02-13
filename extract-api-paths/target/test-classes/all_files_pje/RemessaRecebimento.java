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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.entidades.IEntidade;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;

@Entity
@Table(name = RemessaRecebimento.NAME, schema="jt")
@org.hibernate.annotations.GenericGenerator(name = "gen_jt_remessa_recebimento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "jt.sq_tb_remessa_recebimento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RemessaRecebimento implements IEntidade<RemessaRecebimento, Long> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tb_remessa_recebimento";
	
	private Long id;
	private ElementoDominio elementoRemessa;
	private ElementoDominio elementoRecebimento;
	
	@Id
	@GeneratedValue(generator = "gen_jt_remessa_recebimento")
	@Column(name = "id_remessa_recebimento", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "id_elemento_dominio_remessa", nullable = false)
	public ElementoDominio getElementoRemessa() {
		return elementoRemessa;
	}

	public void setElementoRemessa(ElementoDominio elementoRemessa) {
		this.elementoRemessa = elementoRemessa;
	}

	@ManyToOne
	@JoinColumn(name = "id_elem_dominio_recebimento", nullable = false)
	public ElementoDominio getElementoRecebimento() {
		return elementoRecebimento;
	}

	public void setElementoRecebimento(ElementoDominio elementoRecebimento) {
		this.elementoRecebimento = elementoRecebimento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RemessaRecebimento> getEntityClass() {
		return RemessaRecebimento.class;
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
