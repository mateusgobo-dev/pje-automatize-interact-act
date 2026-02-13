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

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_parametro")
@org.hibernate.annotations.GenericGenerator(name = "gen_parametro", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_parametro"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Parametro implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Parametro,Integer> {

	private static final long serialVersionUID = 1L;

	private int idParametro;
	private String nomeVariavel;
	private String descricaoVariavel;
	private String valorVariavel;
	private Date dataAtualizacao = new Date();
	private Boolean sistema;
	private Usuario usuarioModificacao;
	private Boolean ativo;
	private String esquemaTabelaId;
	private Boolean dadosSensiveis = Boolean.TRUE;

	public Parametro() {
	}

	@Id
	@GeneratedValue(generator = "gen_parametro")
	@Column(name = "id_parametro", unique = true, nullable = false)
	public int getIdParametro() {
		return this.idParametro;
	}

	public void setIdParametro(int idParametro) {
		this.idParametro = idParametro;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_modificacao", nullable = true)
	public Usuario getUsuarioModificacao() {
		return this.usuarioModificacao;
	}

	public void setUsuarioModificacao(Usuario usuarioModificacao) {
		this.usuarioModificacao = usuarioModificacao;
	}

	@Column(name = "nm_variavel", nullable = false, length = 100, unique = true)
	@NotNull
	@Length(max = 100)
	public String getNomeVariavel() {
		return this.nomeVariavel;
	}

	public void setNomeVariavel(String nomeVariavel) {
		this.nomeVariavel = nomeVariavel;
	}

	@Column(name = "ds_variavel", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getDescricaoVariavel() {
		return this.descricaoVariavel;
	}

	public void setDescricaoVariavel(String descricaoVariavel) {
		this.descricaoVariavel = descricaoVariavel;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "vl_variavel", nullable = false)
	@NotNull
	public String getValorVariavel() {
		return this.valorVariavel;
	}

	public void setValorVariavel(String valorVariavel) {
		this.valorVariavel = valorVariavel;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao")
	public Date getDataAtualizacao() {
		return this.dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name = "in_sistema", nullable = false)
	@NotNull
	public Boolean getSistema() {
		return this.sistema;
	}

	public void setSistema(Boolean sistema) {
		this.sistema = sistema;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "ds_esquema_tabela_id", length = 200)
	@Length(max = 200)
	public String getEsquemaTabelaId() {
		return this.esquemaTabelaId;
	}

	public void setEsquemaTabelaId(String esquemaTabelaId) {
		this.esquemaTabelaId = esquemaTabelaId;
	}

	@Override
	public String toString() {
		return nomeVariavel;
	}
	
	@Column(name = "in_dados_sensiveis")
	public Boolean getDadosSensiveis() {
		return dadosSensiveis;
	}
	
	public void setDadosSensiveis(Boolean dadosSensiveis) {
		this.dadosSensiveis = dadosSensiveis;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Parametro)) {
			return false;
		}
		Parametro other = (Parametro) obj;
		if (getIdParametro() != other.getIdParametro()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdParametro();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Parametro> getEntityClass() {
		return Parametro.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdParametro());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
