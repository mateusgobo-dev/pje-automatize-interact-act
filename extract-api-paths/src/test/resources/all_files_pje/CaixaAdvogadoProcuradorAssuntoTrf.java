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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = CaixaAdvogadoProcuradorAssuntoTrf.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_caixa_adv_proc_assunto", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_caixa_adv_proc_assunto"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CaixaAdvogadoProcuradorAssuntoTrf implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<CaixaAdvogadoProcuradorAssuntoTrf,Integer> {

	public static final String TABLE_NAME = "tb_caixa_adv_proc_assunto";
	private static final long serialVersionUID = 1L;

	private Integer idCaixaAdvogadoProcuradorClasseJudicial;
	private CaixaAdvogadoProcurador caixaAdvogadoProcurador;
	private AssuntoTrf assuntoTrf;

	@Id
	@GeneratedValue(generator = "gen_caixa_adv_proc_assunto")
	@Column(name = "id_caixa_adv_proc_assunto", unique = true, nullable = false)
	public Integer getIdCaixaAdvogadoProcuradorClasseJudicial() {
		return idCaixaAdvogadoProcuradorClasseJudicial;
	}

	public void setIdCaixaAdvogadoProcuradorClasseJudicial(Integer idCaixaAdvogadoProcuradorClasseJudicial) {
		this.idCaixaAdvogadoProcuradorClasseJudicial = idCaixaAdvogadoProcuradorClasseJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = " id_caixa_adv_proc", nullable = false)
	@NotNull
	public CaixaAdvogadoProcurador getCaixaAdvogadoProcurador() {
		return caixaAdvogadoProcurador;
	}

	public void setCaixaAdvogadoProcurador(CaixaAdvogadoProcurador caixaAdvogadoProcurador) {
		this.caixaAdvogadoProcurador = caixaAdvogadoProcurador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_trf", nullable = false)
	@NotNull
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CaixaAdvogadoProcuradorAssuntoTrf)) {
			return false;
		}
		if(getIdCaixaAdvogadoProcuradorClasseJudicial() == null){
			return false;
		}
		CaixaAdvogadoProcuradorAssuntoTrf other = (CaixaAdvogadoProcuradorAssuntoTrf) obj;
		if (!idCaixaAdvogadoProcuradorClasseJudicial.equals(other.getIdCaixaAdvogadoProcuradorClasseJudicial())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCaixaAdvogadoProcuradorClasseJudicial();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CaixaAdvogadoProcuradorAssuntoTrf> getEntityClass() {
		return CaixaAdvogadoProcuradorAssuntoTrf.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdCaixaAdvogadoProcuradorClasseJudicial();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
