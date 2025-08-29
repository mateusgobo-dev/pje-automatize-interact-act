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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tb_concurso_crime")
@org.hibernate.annotations.GenericGenerator(name = "gen_concurso_crime", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_concurso_crime"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ConcursoCrime implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<ConcursoCrime,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum TipoAgrupamento {
		F("Concurso Formal"), C("Crime Continuado");

		private String label;

		private TipoAgrupamento(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	private Integer id;
	private TipoAgrupamento tipoAgrupamento;
	private List<TipificacaoDelito> tipificacoes = new ArrayList<TipificacaoDelito>(0);

	@Id
	@GeneratedValue(generator = "gen_concurso_crime")
	@Column(name = "id_concurso_crime", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_tipo_agrupamento")
	public TipoAgrupamento getTipoAgrupamento() {
		return tipoAgrupamento;
	}

	public void setTipoAgrupamento(TipoAgrupamento tipoAgrupamento) {
		this.tipoAgrupamento = tipoAgrupamento;
	}

	@ManyToMany
	@JoinTable(name = "tb_conc_crme_tpfcco_delito", joinColumns = { @JoinColumn(name = "id_concurso_crime") }, inverseJoinColumns = { @JoinColumn(name = "id_tipificacao_delito") })
	public List<TipificacaoDelito> getTipificacoes() {
		return tipificacoes;
	}

	public void setTipificacoes(List<TipificacaoDelito> tipificacoes) {
		this.tipificacoes = tipificacoes;
	}

	@Transient
	public String getDelitosAssociadosString() {
		return getDelitosAssociadosString(this);
	}

	public static String getDelitosAssociadosString(ConcursoCrime concursoCrime) {
		StringBuilder returnValue = new StringBuilder();
		for (TipificacaoDelito tipificacaoDelito : concursoCrime.getTipificacoes()) {
			returnValue.append("<b>(</b>");
			returnValue.append(tipificacaoDelito.getNumeroReferencia());
			returnValue.append("<b>)</b>");
			returnValue.append(tipificacaoDelito.getDelitoString());
			returnValue.append(" ");
		}
		return returnValue.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getTipificacoes() == null) ? 0 : getTipificacoes().hashCode());
		result = prime * result + ((getTipoAgrupamento() == null) ? 0 : getTipoAgrupamento().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ConcursoCrime))
			return false;
		ConcursoCrime other = (ConcursoCrime) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} 
		if (getId() != null && other.getId() != null && getId().equals(other.getId())){
			return true;
		}
		if (getTipificacoes() == null) {
			if (other.getTipificacoes() != null)
				return false;
		}
		if(getTipificacoes().equals(other.getTipificacoes())){
			return true;
		}
		if (getTipoAgrupamento() == other.getTipoAgrupamento())
			return true;
		return false;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ConcursoCrime> getEntityClass() {
		return ConcursoCrime.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
