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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Entity
@Table(name = PessoaPerito.TABLE_NAME)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaPerito extends PessoaFisicaEspecializada implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_perito";
	private static final long serialVersionUID = 1L;
	private String orgao;
	private String numeroOrgao;
	private Date dataExpedicaoOrgao;
	private List<PessoaPeritoEspecialidade> pessoaPeritoEspecialidadeList = new ArrayList<PessoaPeritoEspecialidade>(0);
	private List<OrgaoJulgadorPessoaPerito> orgaoJulgadorPessoaPeritoList = new ArrayList<OrgaoJulgadorPessoaPerito>(0);
	private Boolean peritoAtivo;

	public PessoaPerito() {
		setInTipoPessoa(TipoPessoaEnum.F);
	}

	@Column(name = "ds_orgao_registro", length = 50)
	@Length(max = 50)
	public String getOrgao() {
		return orgao;
	}

	public void setOrgao(String orgao) {
		this.orgao = orgao;
	}

	@Column(name = "nr_inscricao_orgao", length = 15)
	@Length(max = 15)
	public String getNumeroOrgao() {
		return numeroOrgao;
	}

	public void setNumeroOrgao(String numeroOrgao) {
		this.numeroOrgao = numeroOrgao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expedicao_registro")
	public Date getDataExpedicaoOrgao() {
		return dataExpedicaoOrgao;
	}

	public void setDataExpedicaoOrgao(Date dataExpedicao) {
		this.dataExpedicaoOrgao = dataExpedicao;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "pessoaPerito")
	public List<PessoaPeritoEspecialidade> getPessoaPeritoEspecialidadeList() {
		return this.pessoaPeritoEspecialidadeList;
	}

	public void setPessoaPeritoEspecialidadeList(List<PessoaPeritoEspecialidade> pessoaPeritoEspecialidadeList) {
		this.pessoaPeritoEspecialidadeList = pessoaPeritoEspecialidadeList;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "pessoaPerito")
	public List<OrgaoJulgadorPessoaPerito> getOrgaoJulgadorPessoaPeritoList() {
		return orgaoJulgadorPessoaPeritoList;
	}

	public void setOrgaoJulgadorPessoaPeritoList(List<OrgaoJulgadorPessoaPerito> orgaoJulgadorPessoaPeritoList) {
		this.orgaoJulgadorPessoaPeritoList = orgaoJulgadorPessoaPeritoList;
	}
	
	@Transient
	public Boolean getPeritoAtivo() {
		if(this.peritoAtivo == null){
			this.peritoAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.PER) == PessoaFisica.PER;
		}else{
			return this.peritoAtivo;
		}
		return peritoAtivo;
	}
	
	public void setPeritoAtivo(Boolean peritoAtivo) {
		this.peritoAtivo = peritoAtivo;
	}
	
	@Override
	public String toString(){
		return getPessoa().getNome().concat(" - ").concat(getPessoa().getDocumentoCpfCnpj());
	}

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaPerito.class;
	}
}