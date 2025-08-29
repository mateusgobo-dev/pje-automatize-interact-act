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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;


@Entity
@javax.persistence.Cacheable(true)
@Table(name = CaixaAdvogadoProcurador.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_caixa_adv_proc", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_caixa_adv_proc"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class CaixaAdvogadoProcurador implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<CaixaAdvogadoProcurador,Integer> {

	public static final String TABLE_NAME = "tb_caixa_adv_proc";
	private static final long serialVersionUID = 1L;

	private Integer idCaixaAdvogadoProcurador;
	private String nomeCaixaAdvogadoProcurador;
	private String dsCaixaAdvogadoProcurador;
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Jurisdicao jurisdicao;
	private Localizacao localizacao;

	private Integer numeroSequencia;
	private Integer numeroDigitoVerificador;
	private Integer ano;
	private Integer numeroOrigemProcesso;

	private String nomeParte;
	private String numeroCpfCnpjParte;
	private Date nascimentoInicialParte;
	private Date nascimentoFinalParte;
	private Estado ufOABParte;
	private String numeroOABParte;
	private String letraOABParte;

	private PrioridadeProcesso prioridadeProcesso;
	private Date dataDistribuicaoInicial;
	private Date dataDistribuicaoFinal;
	private Date dataCriacaoExpedienteInicial;
	private Date dataCriacaoExpedienteFinal;
	private String intervaloNumeroProcesso;	
	private boolean ativa;

	private List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>(0);
	private List<CaixaRepresentante> caixaRepresentanteList = new ArrayList<CaixaRepresentante>(0);
	private List<PeriodoInativacaoCaixaRepresentante> periodoInativacaoCaixaRepresentanteList  = new ArrayList<PeriodoInativacaoCaixaRepresentante>(0);

	@Id
	@GeneratedValue(generator = "gen_caixa_adv_proc")
	@Column(name = "id_caixa_adv_proc", unique = true, nullable = false)
	public Integer getIdCaixaAdvogadoProcurador() {
		return idCaixaAdvogadoProcurador;
	}

	public void setIdCaixaAdvogadoProcurador(Integer idCaixaAdvogadoProcurador) {
		this.idCaixaAdvogadoProcurador = idCaixaAdvogadoProcurador;
	}

	@Column(name = "nm_caixa", length = 200, nullable = false)
	@NotNull
	@Length(max = 200)
	public String getNomeCaixaAdvogadoProcurador() {
		return nomeCaixaAdvogadoProcurador;
	}

	public void setNomeCaixaAdvogadoProcurador(String nomeCaixaAdvogadoProcurador) {
		this.nomeCaixaAdvogadoProcurador = nomeCaixaAdvogadoProcurador;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_caixa")
	public String getDsCaixaAdvogadoProcurador() {
		return dsCaixaAdvogadoProcurador;
	}

	public void setDsCaixaAdvogadoProcurador(String dsCaixaAdvogadoProcurador) {
		this.dsCaixaAdvogadoProcurador = dsCaixaAdvogadoProcurador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_prioridade_processo")
	public PrioridadeProcesso getPrioridadeProcesso() {
		return prioridadeProcesso;
	}

	public void setPrioridadeProcesso(PrioridadeProcesso prioridadeProcesso) {
		this.prioridadeProcesso = prioridadeProcesso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_jurisdicao")
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao")
	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	@Column(name = "nr_sequencia")
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	@Column(name = "nr_digito_verificador")
	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	@Column(name = "nr_ano")
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_origem_processo")
	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}

	@Column(name = "ds_nome_parte", length = 150)
	@Length(max = 150)
	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	@Column(name = "nr_cpf_cnpj_parte", length = 30)
	@Length(max = 30)
	public String getNumeroCpfCnpjParte() {
		return numeroCpfCnpjParte;
	}

	public void setNumeroCpfCnpjParte(String numeroCpfCnpjParte) {
		this.numeroCpfCnpjParte = numeroCpfCnpjParte;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao_inicio")
	public Date getDataDistribuicaoInicial() {
		return dataDistribuicaoInicial;
	}

	public void setDataDistribuicaoInicial(Date dataDistribuicaoInicial) {
		this.dataDistribuicaoInicial = dataDistribuicaoInicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao_fim")
	public Date getDataDistribuicaoFinal() {
		return dataDistribuicaoFinal;
	}

	public void setDataDistribuicaoFinal(Date dataDistribuicaoFinal) {
		this.dataDistribuicaoFinal = dataDistribuicaoFinal;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao_expediente_inicio")
	public Date getDataCriacaoExpedienteInicial() {
		return dataCriacaoExpedienteInicial;
	}

	public void setDataCriacaoExpedienteInicial(Date dataCriacaoExpedienteInicial) {
		this.dataCriacaoExpedienteInicial = dataCriacaoExpedienteInicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao_expediente_fim")
	public Date getDataCriacaoExpedienteFinal() {
		return dataCriacaoExpedienteFinal;
	}

	public void setDataCriacaoExpedienteFinal(Date dataCriacaoExpedienteFinal) {
		this.dataCriacaoExpedienteFinal = dataCriacaoExpedienteFinal;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ano_nasc_parte_inicio")
	public Date getNascimentoInicialParte() {
		return nascimentoInicialParte;
	}

	public void setNascimentoInicialParte(Date nascimentoInicialParte) {
		this.nascimentoInicialParte = nascimentoInicialParte;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_ano_nasc_parte_fim")
	public Date getNascimentoFinalParte() {
		return nascimentoFinalParte;
	}

	public void setNascimentoFinalParte(Date nascimentoFinalParte) {
		this.nascimentoFinalParte = nascimentoFinalParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_uf_oab_parte")
	public Estado getUfOABParte() {
		return ufOABParte;
	}

	public void setUfOABParte(Estado ufOABParte) {
		this.ufOABParte = ufOABParte;
	}

	@Column(name = "nr_oab_parte", length = 15)
	@Length(max = 15)
	public String getNumeroOABParte() {
		return numeroOABParte;
	}

	public void setNumeroOABParte(String numeroOABParte) {
		this.numeroOABParte = numeroOABParte;
	}

	@Column(name = "ds_letra_oab_parte", length = 1)
	@Length(max = 1)
	public String getLetraOABParte() {
		return letraOABParte;
	}

	public void setLetraOABParte(String letraOABParte) {
		this.letraOABParte = letraOABParte;
	}

	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "vl_nr_processo_intervalo")
	public String getIntervaloNumeroProcesso() {
		return intervaloNumeroProcesso;
	}
	
	public void setIntervaloNumeroProcesso(String intervaloNumeroProcesso) {
		this.intervaloNumeroProcesso = intervaloNumeroProcesso;
	}
	
	
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_caixa_adv_proc_classe", joinColumns = { @JoinColumn(name = "id_caixa_adv_proc", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false) })
	public List<ClasseJudicial> getClasseJudicialList() {
		return classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_caixa_adv_proc_assunto", joinColumns = { @JoinColumn(name = "id_caixa_adv_proc", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_assunto_trf", nullable = false, updatable = false) })
	public List<AssuntoTrf> getAssuntoTrfList() {
		return assuntoTrfList;
	}

	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}
	
	@OneToMany(mappedBy = "caixaAdvogadoProcurador", cascade = {CascadeType.REMOVE})
	public List<CaixaRepresentante> getCaixaRepresentanteList() {
		return caixaRepresentanteList;
	}
	
	public void setCaixaRepresentanteList(
			List<CaixaRepresentante> caixaRepresentanteList) {
		this.caixaRepresentanteList = caixaRepresentanteList;
	}
	
	@OneToMany(mappedBy = "caixaAdvogadoProcurador", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
	public List<PeriodoInativacaoCaixaRepresentante> getPeriodoInativacaoCaixaRepresentanteList() {
		return periodoInativacaoCaixaRepresentanteList;
	}
	
	public void setPeriodoInativacaoCaixaRepresentanteList(
			List<PeriodoInativacaoCaixaRepresentante> periodoInativacaoCaixaRepresentanteList) {
		this.periodoInativacaoCaixaRepresentanteList = periodoInativacaoCaixaRepresentanteList;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ojc")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
	@Transient
	public boolean isAtiva(){
		return this.ativa;
	}
	
	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getIdCaixaAdvogadoProcurador() == null) {
			return false;
		}
		if (!(obj instanceof CaixaAdvogadoProcurador)) {
			return false;
		}
		CaixaAdvogadoProcurador other = (CaixaAdvogadoProcurador) obj;
		if (!idCaixaAdvogadoProcurador.equals(other.getIdCaixaAdvogadoProcurador())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdCaixaAdvogadoProcurador();
		return result;
	}
	
	@Override
	public String toString() {
		return nomeCaixaAdvogadoProcurador;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends CaixaAdvogadoProcurador> getEntityClass() {
		return CaixaAdvogadoProcurador.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdCaixaAdvogadoProcurador();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
