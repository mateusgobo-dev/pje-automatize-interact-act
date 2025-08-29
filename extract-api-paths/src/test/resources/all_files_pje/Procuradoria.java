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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;


@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_procuradoria")
@org.hibernate.annotations.GenericGenerator(name = "gen_procuradoria", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_procuradoria"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Procuradoria implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<Procuradoria,Integer> {

	private static final long serialVersionUID = 1L;

	private int idProcuradoria;
	private String nome;
	private String email;
	private Date dtCriacao;
	private String observacao;
	private Boolean ativo = true;
	private Localizacao localizacao;
	private Boolean acompanhaSessao;
	private TipoProcuradoriaEnum tipo;
	private PessoaJuridica pessoaJuridica;
	
	private List<PessoaProcuradoria> pessoaProcuradoriaList = new ArrayList<PessoaProcuradoria>(
			0);

	@Id
	@GeneratedValue(generator = "gen_procuradoria")
	@Column(name = "id_procuradoria", nullable = false)
	public int getIdProcuradoria() {
		return idProcuradoria;
	}

	public void setIdProcuradoria(int idProcuradoria) {
		this.idProcuradoria = idProcuradoria;
	}

	@Column(name = "ds_nome", length = 150)
	@Length(max = 150)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ds_email", length = 100)
	@Length(max = 100)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao")
	public Date getDtCriacao() {
		return dtCriacao;
	}

	public void setDtCriacao(Date dtCriacao) {
		this.dtCriacao = dtCriacao;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_observacao")
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public String toString() {
		return nome;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "id_localizacao", nullable = false)
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name = "in_procurador_acompanha_sessao")
	public Boolean getAcompanhaSessao() {
		return acompanhaSessao;
	}

	public void setAcompanhaSessao(Boolean acompanhaSessao) {
		this.acompanhaSessao = acompanhaSessao;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Procuradoria)) {
			return false;
		}
		Procuradoria other = (Procuradoria) obj;
		if (getIdProcuradoria() != other.getIdProcuradoria()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcuradoria();
		return result;
	}
	
	@OneToMany(mappedBy = "procuradoria", fetch = FetchType.LAZY)	
	public List<PessoaProcuradoria> getPessoaProcuradoriaList() {
		return pessoaProcuradoriaList;
	}

	public void setPessoaProcuradoriaList(
			List<PessoaProcuradoria> pessoaProcuradoriaList) {
		this.pessoaProcuradoriaList = pessoaProcuradoriaList;
	}

	@Column(name = "in_tipo_procuradoria", length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoProcuradoriaEnum getTipo() {
		return tipo;
	}

	public void setTipo(TipoProcuradoriaEnum tipo) {
		this.tipo = tipo;
	}

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_juridica")
	public PessoaJuridica getPessoaJuridica() {
		return pessoaJuridica;
	}

	public void setPessoaJuridica(PessoaJuridica pessoaJuridica) {
		this.pessoaJuridica = pessoaJuridica;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends Procuradoria> getEntityClass() {
		return Procuradoria.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcuradoria());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
