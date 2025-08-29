package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.enums.TipoFiliacaoEnum;

@Entity
@Table(name = "tb_pessoa_filiacao")
@SequenceGenerator(allocationSize = 1, name = "gen_pessoa_filiacao", sequenceName = "sq_tb_pessoa_filiacao")
public class PessoaFiliacao implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private PessoaFisica pessoaFisica;
	private String filiacao;
	private Boolean ativo = Boolean.TRUE;
	private TipoFiliacaoEnum tipoFiliacao = TipoFiliacaoEnum.M;
	
	public PessoaFiliacao() {
		super();
	}

	public PessoaFiliacao(Long id, PessoaFisica pessoaFisica, String filiacao, Boolean ativo, TipoFiliacaoEnum tipoFiliacao) {
		super();
		this.id = id;
		this.pessoaFisica = pessoaFisica;
		this.filiacao = filiacao;
		this.ativo = ativo;
		this.tipoFiliacao = tipoFiliacao;
	}
	
	@Id
	@GeneratedValue(generator = "gen_pessoa_filiacao")
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_fisica", nullable = false)
	public PessoaFisica getPessoaFisica() {
		return pessoaFisica;
	}
	
	public void setPessoaFisica(PessoaFisica pessoaFisica) {
		this.pessoaFisica = pessoaFisica;
	}

	@Column(name = "ds_filiacao")
	@NotNull
	public String getFiliacao() {
		return filiacao;
	}

	public void setFiliacao(String filiacao) {
		this.filiacao = filiacao;
	}

	@Column(name = "in_ativo")
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_tipo_filiacao", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoFiliacaoEnum getTipoFiliacao() {
		return tipoFiliacao;
	}

	public void setTipoFiliacao(TipoFiliacaoEnum tipoFiliacao) {
		this.tipoFiliacao = tipoFiliacao;
	}
	
	
	
}
