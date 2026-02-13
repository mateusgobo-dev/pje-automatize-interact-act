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

@Entity
@Table(name = PessoaLocalizacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_pessoa_localizacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_pessoa_localizacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class PessoaLocalizacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<PessoaLocalizacao,Integer> {

	public static final String TABLE_NAME = "tb_pessoa_localizacao";
	private static final long serialVersionUID = 1L;

	private int idPessoaLocalizacao;
	private Pessoa pessoa;
	private Localizacao localizacao;

	public PessoaLocalizacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_pessoa_localizacao")
	@Column(name = "id_pessoa_localizacao", unique = true, nullable = false)
	public int getIdPessoaLocalizacao() {
		return this.idPessoaLocalizacao;
	}

	public void setIdPessoaLocalizacao(int idPessoaLocalizacao) {
		this.idPessoaLocalizacao = idPessoaLocalizacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa")
	public Pessoa getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoa(pessoa.getPessoa());
		} else {
			setPessoa((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return this.localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Override
	public String toString() {
		return localizacao.getLocalizacao();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PessoaLocalizacao)) {
			return false;
		}
		PessoaLocalizacao other = (PessoaLocalizacao) obj;
		if (getIdPessoaLocalizacao() != other.getIdPessoaLocalizacao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdPessoaLocalizacao();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends PessoaLocalizacao> getEntityClass() {
		return PessoaLocalizacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdPessoaLocalizacao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
