/**
 * pje-comum
 * Copyright (C) 2009-2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = CaixaRepresentante.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_caixa_representante", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_caixa_representante"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CaixaRepresentante implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<CaixaRepresentante,Integer>{
	
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "tb_caixa_representante";
	
	private Integer idCaixaRepresentante;
	private PessoaFisica representante;
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;
	
	@Id
	@GeneratedValue(generator = "gen_caixa_representante")
	@Column(name = "id_caixa_representante", unique = true, nullable = false)
	public Integer getIdCaixaRepresentante() {
		return idCaixaRepresentante;
	}
	
	public void setIdCaixaRepresentante(Integer idCaixaRepresentante) {
		this.idCaixaRepresentante = idCaixaRepresentante;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_fisica")
	public PessoaFisica getRepresentante() {
		return representante;
	}
	
	public void setRepresentante(PessoaFisica representante) {
		this.representante = representante;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_caixa_adv_proc")
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}
	
	public void setCaixaAdvogadoProcurador(
			CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CaixaRepresentante)) {
			return false;
		}
		CaixaRepresentante other = (CaixaRepresentante) obj;
		if (getIdCaixaRepresentante() != other.getIdCaixaRepresentante()) {
			return false;
		}
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CaixaRepresentante> getEntityClass() {
		return CaixaRepresentante.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdCaixaRepresentante();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
