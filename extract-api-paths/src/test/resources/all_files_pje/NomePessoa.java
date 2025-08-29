package br.jus.pje.nucleo.entidades;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;

@Entity
@Table(name = NomePessoa.TABLE_NAME)
@Cacheable
public class NomePessoa implements java.io.Serializable{
	public static final String TABLE_NAME = "vs_nomes_pessoa";
	private static final long serialVersionUID = 1L;
	
	private NomePessoaId id;
	private Pessoa pessoa;
	private String nome;
	private TipoNomePessoaEnum tipo;
	
	@EmbeddedId
	public NomePessoaId getId() {
		return this.id;
	}

	public void setId(NomePessoaId id) {
		this.id = id;
	}

	@Column(name = "id_pessoa", insertable = false, updatable = false)
	public Pessoa getPessoa() {
		return pessoa;
	}
	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	@Column(name = "ds_nome_pessoa", insertable = false, updatable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name = "ic_nome", insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public TipoNomePessoaEnum getTipo() {
		return tipo;
	}

	public void setTipo(TipoNomePessoaEnum tipo) {
		this.tipo = tipo;
	}
}

@Embeddable
class NomePessoaId implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	protected Integer pessoaId;
	protected String nomeId;
	protected String tipoId;
	
	public NomePessoaId() {
	}
	
	public NomePessoaId(Integer pessoaId, String nomeId, String tipoId) {
		super();
		this.pessoaId = pessoaId;
		this.nomeId = nomeId;
		this.tipoId = tipoId;
	}

	public Integer getPessoaId() {
		return pessoaId;
	}

	public void setPessoaId(Integer pessoaId) {
		this.pessoaId = pessoaId;
	}

	public String getNomeId() {
		return nomeId;
	}

	public void setNomeId(String nomeId) {
		this.nomeId = nomeId;
	}

	public String getTipoId() {
		return tipoId;
	}

	public void setTipoId(String tipoId) {
		this.tipoId = tipoId;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}

