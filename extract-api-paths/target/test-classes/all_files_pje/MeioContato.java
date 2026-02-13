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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = MeioContato.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_meio_contato", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_meio_contato"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class MeioContato implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<MeioContato,Integer> {

	public static final String TABLE_NAME = "tb_meio_contato";
	private static final long serialVersionUID = 1L;

	private int idMeioContato;
	private TipoContato tipoContato;
	private Pessoa pessoa;
	private String valorMeioContato;
	private String complementoContato;
	private String observacao;
	private Usuario usuarioCadastrador;

	public MeioContato() {
	}

	@Id
	@GeneratedValue(generator = "gen_meio_contato")
	@Column(name = "id_meio_contato", unique = true, nullable = false)
	public int getIdMeioContato() {
		return this.idMeioContato;
	}

	public void setIdMeioContato(int idMeioContato) {
		this.idMeioContato = idMeioContato;
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
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public Pessoa getPessoa() {
		return pessoa;
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

	@Column(name = "vl_meio_contato", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getValorMeioContato() {
		return this.valorMeioContato;
	}

	public void setValorMeioContato(String valorMeioContato) {
		this.valorMeioContato = valorMeioContato;
	}

	@Column(name = "ds_complemento_contato", length = 100)
	@Length(max = 100)
	public String getComplementoContato() {
		return this.complementoContato;
	}

	public void setComplementoContato(String complementoContato) {
		this.complementoContato = complementoContato;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_observacao")
	public String getObservacao() {
		return this.observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario_cadastrador")
	public Usuario getUsuarioCadastrador() {
		return this.usuarioCadastrador;
	}

	public void setUsuarioCadastrador(Usuario usuarioCadastrador) {
		this.usuarioCadastrador = usuarioCadastrador;
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
		if (!(obj instanceof MeioContato)) {
			return false;
		}
		MeioContato other = (MeioContato) obj;
		if (getIdMeioContato() != other.getIdMeioContato()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdMeioContato();
		return result;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends MeioContato> getEntityClass() {
		return MeioContato.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdMeioContato());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
