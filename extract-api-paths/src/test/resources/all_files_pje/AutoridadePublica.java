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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = AutoridadePublica.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_autoridade_publica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_autoridade_publica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AutoridadePublica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<AutoridadePublica,Integer> {

	public static final String TABLE_NAME = "tb_autoridade_publica";
	private static final long serialVersionUID = 1L;

	private int idAutoridade;
	private PessoaFisica pessoa;
	private PessoaAutoridade autoridade;
	private Date dataInicio;
	private Date dataFim;
	private Boolean ativo;

	@Id
	@GeneratedValue(generator = "gen_autoridade_publica")
	@Column(name = "id_autoridade", unique = true, nullable = false)
	public int getIdAutoridade() {
		return idAutoridade;
	}

	public void setIdAutoridade(int idAutoridade) {
		this.idAutoridade = idAutoridade;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa", nullable = false)
	@NotNull
	public PessoaFisica getPessoa() {
		return this.pessoa;
	}

	public void setPessoa(PessoaFisica pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(PessoaFisica)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		setPessoa(pessoa != null ? pessoa.getPessoa() : (PessoaFisica) null);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_autoridade", nullable = false)
	@NotNull
	public PessoaAutoridade getAutoridade() {
		return this.autoridade;
	}

	public void setAutoridade(PessoaAutoridade autoridade) {
		this.autoridade = autoridade;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	@Column(name = "dt_inicio")
	@Temporal(TemporalType.DATE)
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	@Column(name = "dt_fim")
	@Temporal(TemporalType.DATE)
	public Date getDataFim() {
		return dataFim;
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
	@javax.persistence.Transient
	public Class<? extends AutoridadePublica> getEntityClass() {
		return AutoridadePublica.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdAutoridade());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
