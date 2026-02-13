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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.VisualizacaoProcessoEnum;

@Entity
@Table(name = "tb_pessoa_servidor")
@SecondaryTables({ 
	@SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) 
})
public class PessoaServidor extends PessoaFisicaEspecializada{

	private static final long serialVersionUID = 1L;

	private String numeroMatricula;
	private Date dataPosse;
	private VisualizacaoProcessoEnum visualizacaoProcesso;
	private Boolean checkado = Boolean.FALSE;
	private Boolean checkVisibilidade = Boolean.FALSE;
	private Boolean servidorAtivo;

	public PessoaServidor() {
	}

	@Column(name = "nr_matricula", length = 15)
	@Length(max = 15)
	public String getNumeroMatricula() {
		return numeroMatricula;
	}

	public void setNumeroMatricula(String numeroMatricula) {
		this.numeroMatricula = numeroMatricula;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse")
	public Date getDataPosse() {
		return this.dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}

	@Column(name = "in_visualizacao_processo", length = 1)
	@Enumerated(EnumType.STRING)
	public VisualizacaoProcessoEnum getVisualizacaoProcesso() {
		return visualizacaoProcesso;
	}

	public void setVisualizacaoProcesso(VisualizacaoProcessoEnum visualizacaoProcesso) {
		this.visualizacaoProcesso = visualizacaoProcesso;
	}

	@Transient
	public Boolean getCheckado() {
		return checkado;
	}

	public void setCheckado(Boolean checkado) {
		this.checkado = checkado;
	}
	
	@Transient
	public Boolean getServidorAtivo() {
		if(this.servidorAtivo == null){
			this.servidorAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.SER) == PessoaFisica.SER;
		}
		return this.servidorAtivo;
	}
	
	public void setServidorAtivo(Boolean servidorAtivo) {
		this.servidorAtivo = servidorAtivo;
	}
	
	@Transient
	public Boolean getCheckVisibilidade() {
		return checkVisibilidade;
	}

	public void setCheckVisibilidade(Boolean checkVisibilidade) {
		this.checkVisibilidade = checkVisibilidade;
	}

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaServidor.class;
	}
}