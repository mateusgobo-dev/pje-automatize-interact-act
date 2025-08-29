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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = SecaoJudiciaria.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_secao_judiciaria", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_secao_judiciaria"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SecaoJudiciaria implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SecaoJudiciaria,String> {

	public static final String TABLE_NAME = "tb_secao_judiciaria";
	private static final long serialVersionUID = 1L;

	private String cdSecaoJudiciaria;
	private String secaoJudiciaria;
	private String urlAplicacao;
	private List<HistoricoEstatisticaEventoProcesso> historicoEstatisticaEventoProcessoList;

	public SecaoJudiciaria() {
	}

	@Id
	@GeneratedValue(generator = "gen_secao_judiciaria")
	@Column(name = "cd_secao_judiciaria", unique = true, nullable = false, length = 2)
	@Length(max = 2)
	@NotNull
	public String getCdSecaoJudiciaria() {
		return cdSecaoJudiciaria;
	}

	public void setCdSecaoJudiciaria(String cdSecaoJudiciaria) {
		this.cdSecaoJudiciaria = cdSecaoJudiciaria;
	}

	@NotNull
	@Column(name = "ds_secao_judiciaria", nullable = false, length = 50)
	@Length(max = 50)
	public String getSecaoJudiciaria() {
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(String secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	@Column(name = "ds_url_aplicacao", nullable = false, length = 50)
	@Length(max = 50)
	public String getUrlAplicacao() {
		return urlAplicacao;
	}

	public void setUrlAplicacao(String urlAplicacao) {
		this.urlAplicacao = urlAplicacao;
	}

	@Override
	public String toString() {
		return secaoJudiciaria;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "secaoJudiciaria")
	public List<HistoricoEstatisticaEventoProcesso> getHistoricoEstatisticaEventoProcessoList() {
		return this.historicoEstatisticaEventoProcessoList;
	}

	public void setHistoricoEstatisticaEventoProcessoList(
			List<HistoricoEstatisticaEventoProcesso> historicoEstatisticaEventoProcessoList) {
		this.historicoEstatisticaEventoProcessoList = historicoEstatisticaEventoProcessoList;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SecaoJudiciaria)) {
			return false;
		}
		SecaoJudiciaria other = (SecaoJudiciaria) obj;
		if (!getCdSecaoJudiciaria().equalsIgnoreCase(other.getCdSecaoJudiciaria())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCdSecaoJudiciaria() == null) ? 0 : getCdSecaoJudiciaria().hashCode());
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SecaoJudiciaria> getEntityClass() {
		return SecaoJudiciaria.class;
	}

	@Override
	@javax.persistence.Transient
	public String getEntityIdObject() {
		return getCdSecaoJudiciaria();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
