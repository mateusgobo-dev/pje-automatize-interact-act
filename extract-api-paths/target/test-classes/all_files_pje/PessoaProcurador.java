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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "tb_pessoa_procurador")
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaProcurador extends PessoaFisicaEspecializada implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Date dataPosse;
	private String numeroMatricula;
	private Estado ufOAB;
	private String numeroOAB;
	private String letraOAB;
	private Date dataExpedicaoOAB;
	private PessoaAdvogadoTipoInscricaoEnum tipoInscricao;
	private Boolean procuradorMpSessao = false;
	private Boolean procuradorAtivo;
	private List<PessoaProcuradoria> pessoaProcuradorias = new ArrayList<PessoaProcuradoria>();

	public PessoaProcurador() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_uf_oab")
	public Estado getUfOAB() {
		return ufOAB;
	}

	public void setUfOAB(Estado ufOAB) {
		this.ufOAB = ufOAB;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse_procurador")
	public Date getDataPosse() {
		return dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}

	@Column(name = "nr_matricula", length = 15)
	@Length(max = 15)
	public String getNumeroMatricula() {
		return numeroMatricula;
	}

	public void setNumeroMatricula(String numeroMatricula) {
		this.numeroMatricula = numeroMatricula;
	}

	@Column(name = "nr_oab", length = 15)
	@Length(max = 15)
	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	@Column(name = "ds_letra_oab", length = 1)
	@Length(max = 1)
	public String getLetraOAB() {
		return letraOAB;
	}

	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expedicao_oab")
	public Date getDataExpedicaoOAB() {
		return dataExpedicaoOAB;
	}

	public void setDataExpedicaoOAB(Date dataExpedicaoOAB) {
		this.dataExpedicaoOAB = dataExpedicaoOAB;
	}

	@Column(name = "in_tipo_inscricao_oab", length = 1)
	@Enumerated(EnumType.STRING)
	public PessoaAdvogadoTipoInscricaoEnum getTipoInscricao() {
		return tipoInscricao;
	}

	public void setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum tipoInscricao) {
		this.tipoInscricao = tipoInscricao;
	}

	@Column(name = "in_procurador_mp_sessao")
	@NotNull
	public Boolean getProcuradorMpSessao() {
		return procuradorMpSessao;
	}

	public void setProcuradorMpSessao(Boolean procuradorMpSessao) {
		this.procuradorMpSessao = procuradorMpSessao;
	}
	
	@Transient
	public Boolean getProcuradorAtivo() {
		if(this.procuradorAtivo == null){
			this.procuradorAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.PRO) == PessoaFisica.PRO;
		}else{
			return this.procuradorAtivo;
		}
		return procuradorAtivo;
	}
	
	public void setProcuradorAtivo(Boolean procuradorAtivo) {
		this.procuradorAtivo = procuradorAtivo;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="id_pessoa")
	public List<PessoaProcuradoria> getPessoaProcuradorias() {
		return pessoaProcuradorias;
	}

	public void setPessoaProcuradorias(List<PessoaProcuradoria> pessoaProcuradorias) {
		this.pessoaProcuradorias = pessoaProcuradorias;
	}

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaProcurador.class;
	}
	
}