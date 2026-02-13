/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence À  União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.filters.CaixaFilter;

@Entity
@Table(name = CaixaFiltro.TABLE_NAME)
@PrimaryKeyJoinColumn(name = "id_caixa_filtro")
@FilterDefs(value = {
		@FilterDef(name = CaixaFilter.FILTER_LETRA_OAB, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_NUMERO_OAB, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_UF_OAB, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_DATA_NASCIMENTO, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_NUMERO_CPF, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_NUMERO_CNPJ, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_NOME_PARTE, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) }),
		@FilterDef(name = CaixaFilter.FILTER_ASSUNTO, parameters = { @ParamDef(type = CaixaFilter.TYPE_INT, name = CaixaFilter.FILTER_PARAM_ID_PROCESSO) })		
})
@Filters(value = { @Filter(name = CaixaFilter.FILTER_LETRA_OAB, condition = CaixaFilter.CONDITION_LETRA_OAB),
		@Filter(name = CaixaFilter.FILTER_NUMERO_OAB, condition = CaixaFilter.CONDITION_NUMERO_OAB),
		@Filter(name = CaixaFilter.FILTER_UF_OAB, condition = CaixaFilter.CONDITION_UF_OAB),
		@Filter(name = CaixaFilter.FILTER_DATA_NASCIMENTO, condition = CaixaFilter.CONDITION_DATA_NASCIMENTO),
		@Filter(name = CaixaFilter.FILTER_NUMERO_CPF, condition = CaixaFilter.CONDITION_NUMERO_CPF),
		@Filter(name = CaixaFilter.FILTER_NUMERO_CNPJ, condition = CaixaFilter.CONDITION_NUMERO_CNPJ),	
		@Filter(name = CaixaFilter.FILTER_NOME_PARTE, condition = CaixaFilter.CONDITION_NOME_PARTE),
		@Filter(name = CaixaFilter.FILTER_ASSUNTO, condition = CaixaFilter.CONDITION_ASSUNTO)
		}
)
public class CaixaFiltro extends Caixa implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_caixa_filtro";

	private static final long serialVersionUID = 1L;

	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;

	private Integer numeroSequencia;
	private Integer numeroDigitoVerificador;
	private Integer ano;
	private Integer numeroOrigemProcesso;
	private Integer numeroIdentificacaoOrgaoJustica;
	
	private String valorNumeroProcesso;
	
	private String nomeParte;
	private String numeroCpfCnpjParte;
	private Date nascimentoInicialParte;
	private Date nascimentoFinalParte;
	private Estado ufOABParte;
	private String numeroOABParte;
	private String letraOABParte;

	private Double valorCausa;
	private Date dataDistribuicaoInicio;
	private Date dataDistribuicaoFim;
	private Date dataChegadaTarefaInicio;
	private Date dataChegadaTarefaFim;

	private Eleicao eleicao;
	private OrgaoJulgador orgaoJulgador;
	private Date dataAutuacaoInicial;
	private Date dataAutuacaoFinal;
	private PrioridadeProcesso prioridadeProcesso;
	
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	private List<PrioridadeProcesso> prioridadeProcessoList = new ArrayList<PrioridadeProcesso>(0);
	
    private String ramoJustica;
    private String respectivoTribunal;
    private Cargo cargo;
	
	public CaixaFiltro() { }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial")
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assunto_trf")
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
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
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "vl_nr_processo")
	public String getValorNumeroProcesso() {
		return valorNumeroProcesso;
	}

	public void setValorNumeroProcesso(String valorNumeroProcesso) {
		this.valorNumeroProcesso = valorNumeroProcesso;
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

	@Column(name = "vl_causa")
	public Double getValorCausa() {
		return valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao_inicio")
	public Date getDataDistribuicaoInicio() {
		return dataDistribuicaoInicio;
	}

	public void setDataDistribuicaoInicio(Date dataDistribuicaoInicio) {
		this.dataDistribuicaoInicio = dataDistribuicaoInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao_fim")
	public Date getDataDistribuicaoFim() {
		return dataDistribuicaoFim;
	}

	public void setDataDistribuicaoFim(Date dataDistribuicaoFim) {
		this.dataDistribuicaoFim = dataDistribuicaoFim;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_chegada_tarefa_inicio")
	public Date getDataChegadaTarefaInicio() {
		return dataChegadaTarefaInicio;
	}

	public void setDataChegadaTarefaInicio(Date dataChegadaTarefaInicio) {
		this.dataChegadaTarefaInicio = dataChegadaTarefaInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_chegada_tarefa_fim")
	public Date getDataChegadaTarefaFim() {
		return dataChegadaTarefaFim;
	}

	public void setDataChegadaTarefaFim(Date dataChegadaTarefaFim) {
		this.dataChegadaTarefaFim = dataChegadaTarefaFim;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@Transient
	public List<PrioridadeProcesso> getPrioridadeProcessoList() {
		return prioridadeProcessoList;
	}

	public void setPrioridadeProcessoList(List<PrioridadeProcesso> prioridadeProcessoList) {
		this.prioridadeProcessoList = prioridadeProcessoList;
	}

	@Override
	public String toString() {
		return getNomeCaixa();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

    @Column(name = "nr_identificacao_orgao_justica")
	public Integer getNumeroIdentificacaoOrgaoJustica() {
		return numeroIdentificacaoOrgaoJustica;
	}

	public void setNumeroIdentificacaoOrgaoJustica(Integer numeroIdentificacaoOrgaoJustica) {
		this.numeroIdentificacaoOrgaoJustica = numeroIdentificacaoOrgaoJustica;
		if(numeroIdentificacaoOrgaoJustica != null){
			String numeroOrgaoJustica = numeroIdentificacaoOrgaoJustica.toString();
			this.setRamoJustica(numeroOrgaoJustica.substring(0, 1));
			this.setRespectivoTribunal(numeroOrgaoJustica.substring(1));
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_autuacao_inicio")
	public Date getDataAutuacaoInicial() {
		return dataAutuacaoInicial;
	}

	public void setDataAutuacaoInicial(Date dataAutuacaoInicial) {
		this.dataAutuacaoInicial = dataAutuacaoInicial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_autuacao_fim")
	public Date getDataAutuacaoFinal() {
		return dataAutuacaoFinal;
	}

	public void setDataAutuacaoFinal(Date dataAutuacaoFinal) {
		this.dataAutuacaoFinal = dataAutuacaoFinal;
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
	@JoinColumn(name = "id_eleicao")
	public Eleicao getEleicao() {
		return eleicao;
	}

	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo")
	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}
	
	@Transient
	public String getRespectivoTribunal(){
		return respectivoTribunal;
	}
	
	public void setRespectivoTribunal(String respectivoTribunal) {       
		this.respectivoTribunal = respectivoTribunal;
    }

	@Transient
	public String getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	@Override
	@Transient
	public Class<? extends Caixa> getEntityClass() {
		return CaixaFiltro.class;
	}
}