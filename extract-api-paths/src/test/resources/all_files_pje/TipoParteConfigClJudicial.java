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
@javax.persistence.Cacheable(true)
@Table(name = "tb_tipo_parte_config_cl_judicial")
@org.hibernate.annotations.GenericGenerator(name = "gen_tp_parte_config_cl_judicial", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tp_parte_config_cl_judicial"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoParteConfigClJudicial implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoParteConfigClJudicial,Integer> {

	private static final long serialVersionUID = 1L;

	private int idTipoParteConfigClJudicial;
	private TipoParteConfiguracao tipoParteConfiguracao;
	private ClasseJudicial classeJudicial;
	
	public TipoParteConfigClJudicial() {
	}

	@Id
	@GeneratedValue(generator = "gen_tp_parte_config_cl_judicial")
	@Column(name = "id_tipo_parte_config_cl_judicial", unique = true, nullable = false)
	public int getIdTipoParteConfigClJudicial() {
		return this.idTipoParteConfigClJudicial;
	}

	public void setIdTipoParteConfigClJudicial(int idTipoParteClasseJudicial) {
		this.idTipoParteConfigClJudicial = idTipoParteClasseJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_parte_configuracao", nullable = false)
	@NotNull
	public TipoParteConfiguracao getTipoParteConfiguracao() {
		return tipoParteConfiguracao;
	}
	
	public void setTipoParteConfiguracao(TipoParteConfiguracao tipoParteConfiguracao) {
		this.tipoParteConfiguracao = tipoParteConfiguracao;
	}
	
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return this.classeJudicial;
	}


	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TipoParteConfigClJudicial)) {
			return false;
		}
		TipoParteConfigClJudicial other = (TipoParteConfigClJudicial) obj;
		if (getIdTipoParteConfigClJudicial() != other.getIdTipoParteConfigClJudicial()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdTipoParteConfigClJudicial();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoParteConfigClJudicial> getEntityClass() {
		return TipoParteConfigClJudicial.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoParteConfigClJudicial());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
