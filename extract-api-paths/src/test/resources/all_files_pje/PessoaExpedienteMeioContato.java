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

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = PessoaExpedienteMeioContato.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_exp_meio_contato", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_exp_meio_contato"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaExpedienteMeioContato implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaExpedienteMeioContato,Integer> {

	public static final String TABLE_NAME = "tb_pessoa_exp_meio_contato";
	private static final long serialVersionUID = 1L;

	private int idPessoaExpedienteMeioContato;
	private TipoContato tipoContato;
	private PessoaExpediente pessoaExpediente;
	private String valorMeioContato;

	@Id
	@GeneratedValue(generator = "gen_pessoa_exp_meio_contato")
	@Column(name = "id_pessoa_exp_meio_contato")
	public int getIdPessoaExpedienteMeioContato() {
		return this.idPessoaExpedienteMeioContato;
	}

	public void setIdPessoaExpedienteMeioContato(int idPessoaExpedienteMeioContato) {
		this.idPessoaExpedienteMeioContato = idPessoaExpedienteMeioContato;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tipo_contato", nullable = false)
	@NotNull
	public TipoContato getTipoContato() {
		return this.tipoContato;
	}

	public void setTipoContato(TipoContato tipoContato) {
		this.tipoContato = tipoContato;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_exp", nullable = false)
	@NotNull
	public PessoaExpediente getPessoaExpediente() {
		return pessoaExpediente;
	}

	public void setPessoaExpediente(PessoaExpediente pessoaExpediente) {
		this.pessoaExpediente = pessoaExpediente;
	}

	@Column(name = "vl_meio_contato", length = 100)
	@Length(max = 100)
	public String getValorMeioContato() {
		return this.valorMeioContato;
	}

	public void setValorMeioContato(String valorMeioContato) {
		this.valorMeioContato = valorMeioContato;
	}

	@Override
	public String toString() {
		return valorMeioContato;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaExpedienteMeioContato)) {
			return false;
		}
		PessoaExpedienteMeioContato other = (PessoaExpedienteMeioContato) obj;
		if (getIdPessoaExpedienteMeioContato() != other.getIdPessoaExpedienteMeioContato()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaExpedienteMeioContato();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaExpedienteMeioContato> getEntityClass() {
		return PessoaExpedienteMeioContato.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaExpedienteMeioContato());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
