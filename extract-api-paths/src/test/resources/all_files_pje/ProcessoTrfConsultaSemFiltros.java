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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.util.NumeracaoUnicaUtil;

/**
 * 
 * @author Rodrigo Menezes
 * 
 *         Entidade de ProcessoTrf sem listas e filtros
 * 
 */
@Entity
@Table(name = ProcessoTrf.TABLE_NAME)
public class ProcessoTrfConsultaSemFiltros implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrf;
	private Processo processo;
	private ClasseJudicial classeJudicial;
	private ClasseJudicial classeJudicialOutraInstancia;

	private Integer numeroSequencia;
	private Integer ano;
	private Integer numeroDigitoVerificador;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;

	private Jurisdicao jurisdicao;
	private Double valorCausa;
	private List<ProcessoParte> listaParteAtivo;
	private List<ProcessoParte> listaPartePassivo;
	private List<ProcessoParte> listaParteTerceiro;
	private List<ProcessoParte> listaFiscal;

	private ClasseJudicialInicialEnum inicial;
	private ProcessoStatusEnum processoStatus;
	private Date dataAutuacao;
	private Date dtTransitadoJulgado;

	private Boolean segredoJustica = Boolean.FALSE;
	private String observacaoSegredo;
	private Boolean justicaGratuita;
	private ProcessoTrfApreciadoEnum apreciadoSegredo = ProcessoTrfApreciadoEnum.N;
	private ProcessoTrfApreciadoEnum apreciadoSigilo = ProcessoTrfApreciadoEnum.N;
	private Boolean inOutraInstancia = Boolean.FALSE;
	private boolean inBloqueiaPeticao = false;
	private Boolean apreciadoJusticaGratuita = Boolean.FALSE;

	private Boolean tutelaLiminar;
	private Boolean apreciadoTutelaLiminar;

	private List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
	private List<ProcessoPericia> processoPericiaList = new ArrayList<ProcessoPericia>(0);
	private List<ProcessoAssunto> processoAssuntoList = new ArrayList<ProcessoAssunto>(0);
	private List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);
	private List<PrioridadeProcesso> prioridadeProcessoList = new ArrayList<PrioridadeProcesso>(0);
	private List<ProcessoPrioridadeProcesso> processoPrioridadeProcessoList = new ArrayList<ProcessoPrioridadeProcesso>(
			0);
	private List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfList = new ArrayList<ComplementoClasseProcessoTrf>(
			0);
	private List<ProcessoLote> processoLoteList = new ArrayList<ProcessoLote>(0);

	private String numeroProcesso;

	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Cargo cargo;

	private Localizacao localizacaoInicial;
	private Localizacao estruturaInicial;

	private Date dataDistribuicao;
	private String nomeParte;
	private AssuntoTrf assuntoTrf;
	private TipoPessoa tipoPessoa;

	private ProcessoTrf processoOriginario;
	private ProcessoTrf processoDependencia;

	private Double valorCausaIncidente;
	private Boolean isIncidente = Boolean.FALSE;

	private Boolean selecionadoPauta = Boolean.FALSE;
	private Boolean revisado = Boolean.FALSE;

	private OrgaoJulgador orgaoJulgadorRevisor;

	private Pessoa pessoaRelator;
	private Pessoa pessoaMarcouRevisado;
	private Pessoa pessoaMarcouPauta;
	private Pessoa pessoaMarcouJulgamento;

	private boolean selecionadoJulgamento;

	private EnderecoWsdl enderecoWsdl;

	private ProcessoTrf processoReferencia;

	private String desProcReferencia;
	private Boolean check;

	private Character instancia;
	private PessoaMagistrado pessoaApreciouJusticaGratuita;

	private Date dataSugestaoSessao;
	private Boolean prontoRevisao;

	/**
	 * @return Retorna o id do ProcessoTrf que é igual ao id do processo do core
	 */
	@Id
	@Column(name = "id_processo_trf", unique = true, nullable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(int idProcesso) {
		this.idProcessoTrf = idProcesso;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf", updatable = false, insertable = false)
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", nullable = false, updatable = false, insertable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pes_apreciou_jus_gratuita", updatable = false, insertable = false)
	public PessoaMagistrado getPessoaApreciouJusticaGratuita() {
		return pessoaApreciouJusticaGratuita;
	}

	public void setPessoaApreciouJusticaGratuita(PessoaMagistrado pessoaApreciouJusticaGratuita) {
		this.pessoaApreciouJusticaGratuita = pessoaApreciouJusticaGratuita;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cl_judicial_outra_instancia", updatable = false, insertable = false)
	public ClasseJudicial getClasseJudicialOutraInstancia() {
		return classeJudicialOutraInstancia;
	}

	public void setClasseJudicialOutraInstancia(ClasseJudicial classeJudicialOutraInstancia) {
		this.classeJudicialOutraInstancia = classeJudicialOutraInstancia;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_jurisdicao", updatable = false, insertable = false)
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoParte> getProcessoParteList() {
		return processoParteList;
	}

	public void setProcessoParteList(List<ProcessoParte> processoParteList) {
		this.processoParteList = processoParteList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoPericia> getProcessoPericiaList() {
		return processoPericiaList;
	}

	public void setProcessoPericiaList(List<ProcessoPericia> processoPericiaList) {
		this.processoPericiaList = processoPericiaList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_assunto", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_assunto_trf", nullable = false, updatable = false) })
	public List<AssuntoTrf> getAssuntoTrfList() {
		return this.assuntoTrfList;
	}

	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ComplementoClasseProcessoTrf> getComplementoClasseProcessoTrfList() {
		return this.complementoClasseProcessoTrfList;
	}

	public void setComplementoClasseProcessoTrfList(List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfList) {
		this.complementoClasseProcessoTrfList = complementoClasseProcessoTrfList;
	}

	@Column(name = "nr_sequencia", updatable = false, insertable = false)
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	@Column(name = "nr_ano", updatable = false, insertable = false)
	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	@Column(name = "nr_digito_verificador", updatable = false, insertable = false)
	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	@Column(name = "nr_identificacao_orgao_justica", updatable = false, insertable = false)
	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}

	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_autuacao", updatable = false, insertable = false)
	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	@Column(name = "nr_origem_processo", updatable = false, insertable = false)
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	@Column(name = "vl_causa", updatable = false, insertable = false)
	public Double getValorCausa() {
		return this.valorCausa;
	}

	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	@Column(name = "in_segredo_justica", updatable = false, insertable = false)
	public Boolean getSegredoJustica() {
		return this.segredoJustica;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	@Column(name = "in_justica_gratuita", updatable = false, insertable = false)
	public Boolean getJusticaGratuita() {
		return this.justicaGratuita;
	}

	public void setJusticaGratuita(Boolean justicaGratuita) {
		this.justicaGratuita = justicaGratuita;
	}

	@Column(name = "in_tutela_liminar", updatable = false, insertable = false)
	public Boolean getTutelaLiminar() {
		return this.tutelaLiminar;
	}

	public void setTutelaLiminar(Boolean tutelaLiminar) {
		this.tutelaLiminar = tutelaLiminar;
	}

	@Column(name = "in_apreciado_tutela_liminar", updatable = false, insertable = false)
	public Boolean getApreciadoTutelaLiminar() {
		return apreciadoTutelaLiminar;
	}

	public void setApreciadoTutelaLiminar(Boolean apreciadoTutelaLiminar) {
		this.apreciadoTutelaLiminar = apreciadoTutelaLiminar;
	}

	@Column(name = "ds_observacao_segredo", length = 100, updatable = false, insertable = false)
	@Length(max = 100)
	public String getObservacaoSegredo() {
		return this.observacaoSegredo;
	}

	public void setObservacaoSegredo(String observacaoSegredo) {
		this.observacaoSegredo = observacaoSegredo;
	}

	@Override
	public String toString() {
		return !isNumerado() ? "" : getNumeroProcesso();
	}

	@Column(name = "in_inicial", length = 1, updatable = false, insertable = false)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.ClasseJudicialInicialType")
	public ClasseJudicialInicialEnum getInicial() {
		return this.inicial;
	}

	public void setInicial(ClasseJudicialInicialEnum inicial) {
		this.inicial = inicial;
	}

	@Column(name = "cd_processo_status", length = 1, updatable = false, insertable = false)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.ProcessoStatusType")
	public ProcessoStatusEnum getProcessoStatus() {
		return this.processoStatus;
	}

	public void setProcessoStatus(ProcessoStatusEnum processoStatus) {
		this.processoStatus = processoStatus;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoAssunto> getProcessoAssuntoList() {
		return this.processoAssuntoList;
	}

	public void setProcessoAssuntoList(List<ProcessoAssunto> processoAssuntoList) {
		this.processoAssuntoList = processoAssuntoList;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_proc_prioridde_processo", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_prioridade_processo", nullable = false, updatable = false) })
	public List<PrioridadeProcesso> getPrioridadeProcessoList() {
		return this.prioridadeProcessoList;
	}

	public void setPrioridadeProcessoList(List<PrioridadeProcesso> prioridadeProcessoList) {
		this.prioridadeProcessoList = prioridadeProcessoList;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoPrioridadeProcesso> getProcessoPrioridadeProcessoList() {
		return this.processoPrioridadeProcessoList;
	}

	public void setProcessoPrioridadeProcessoList(List<ProcessoPrioridadeProcesso> processoPrioridadeProcessoList) {
		this.processoPrioridadeProcessoList = processoPrioridadeProcessoList;
	}

	@Transient
	public String getNumeroProcesso() {
		if (processo != null && processo.getNumeroProcesso() != null) {
			numeroProcesso = processo.getNumeroProcesso();
		} else if (numeroProcesso == null || numeroProcesso.isEmpty()) {
			numeroProcesso = isNumerado() ? NumeracaoUnicaUtil.formatNumeroProcesso(getNumeroSequencia(),
					getNumeroDigitoVerificador(), getAno(), getNumeroOrgaoJustica(), getNumeroOrigem()) : null;
		}
		return numeroProcesso;
	}

	/**
	 * @return Retorna o valor da causa com formatação decimal.
	 */
	@Transient
	public String getVlCausa() {
		String vlCausa = "";
		if (valorCausa != null) {
			NumberFormat formatter = DecimalFormat.getNumberInstance(new Locale("pt", "BR"));
			formatter.setMinimumIntegerDigits(1);
			formatter.setMaximumFractionDigits(2);
			formatter.setMinimumFractionDigits(2);
			vlCausa = formatter.format(valorCausa);
		}
		return vlCausa;
	}

	@Transient
	public boolean isNumerado() {
		return getNumeroSequencia() != null;
	}

	/*
	 * Traz as partes de um processo concatenada com o CPF ou CNPJ de acordo com
	 * o polo
	 */
	@Transient
	public List<String> getListaPartePolo(ProcessoParteParticipacaoEnum participacaoEnum) {
		List<String> list = new ArrayList<String>();
		StringBuilder partes = new StringBuilder();
		for (ProcessoParte processoParte : getListaPartePoloObj(participacaoEnum)) {
			partes.append(processoParte.getTipoParte().getTipoParte()).append(": ");
			partes.append(processoParte.getPessoa().getNome()).append(" (");
			partes.append(processoParte.getPessoa().getDocumentoCpfCnpj());
			partes.append(")");
			list.add(partes.toString());
			partes = new StringBuilder();
		}
		return list;
	}

	@Transient
	public List<String> getListaNomePessoa(ProcessoParteParticipacaoEnum participacaoEnum) {
		List<String> list = new ArrayList<String>();
		StringBuilder partes = new StringBuilder();
		for (ProcessoParte processoParte : getListaPartePoloObj(participacaoEnum)) {
			partes.append(processoParte.getPessoa().getNome());
			list.add(partes.toString());
			partes = new StringBuilder();
		}
		return list;
	}

	// TODO Usar so essa e remover a de cima, ver a melhor maneira de fazer isso
	// depois
	public List<ProcessoParte> getListaPartePoloObj(ProcessoParteParticipacaoEnum participacaoEnum) {
		if (participacaoEnum == null) {
			throw new IllegalArgumentException("A participação da parte é requerida");
		}
		List<ProcessoParte> list = new ArrayList<ProcessoParte>();
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getInParticipacao().equals(participacaoEnum)) {
				list.add(processoParte);
			}
		}
		return list;
	}

	// Traz uma lista com as partes Ativas
	@Transient
	public List<ProcessoParte> getListaParteAtivo() {
		this.listaParteAtivo = getListaPartePoloObj(ProcessoParteParticipacaoEnum.A);
		return listaParteAtivo;
	}

	// Traz uma lista com as partes Passivas
	@Transient
	public List<ProcessoParte> getListaPartePassivo() {
		this.listaPartePassivo = getListaPartePoloObj(ProcessoParteParticipacaoEnum.P);
		return listaPartePassivo;
	}

	// Tras uma lista com as partes Terceira
	@Transient
	public List<ProcessoParte> getListaParteTerceiro() {
		this.listaParteTerceiro = getListaPartePoloObj(ProcessoParteParticipacaoEnum.T);
		return listaParteTerceiro;
	}

	@Transient
	public void setListaFiscal(List<ProcessoParte> listaFiscal) {
		this.listaFiscal = listaFiscal;
	}

	// Traz uma lista com as partes fiscal da lei
	@Transient
	public List<ProcessoParte> getListaFiscal() {
		this.listaFiscal = getListaPartePoloObj(ProcessoParteParticipacaoEnum.T);
		return listaFiscal;
	}

	// Transients para pegar o valor em String das variaveis.
	@Transient
	public String getJurisdicaoStr() {
		return jurisdicao.getJurisdicao();
	}

	@Transient
	public String getClasseJudicialStr() {
		return getClasseJudicial().getClasseJudicial();
	}

	@Transient
	public String getValorCausaStr() {
		if (valorCausa == null) {
			return "0,00";
		} else {
			return NumberFormat.getCurrencyInstance().format(valorCausa);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoLote> getProcessoLoteList() {
		return this.processoLoteList;
	}

	public void setProcessoLoteList(List<ProcessoLote> processoLoteList) {
		this.processoLoteList = processoLoteList;
	}

	@Transient
	public Boolean getDependencia() {
		if (this.processo.getNumeroProcessoOrigem() != null) {
			return true;
		}

		return false;
	}

	@Transient
	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	@Transient
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	// TODO Futuramente haverá um relacionamento informando a qual justiça o
	// processo esta relacionado
	@Transient
	public String getJusticaOrdinaria() {
		return "Tribunal Regional Federal da 5º Região";
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_transitado_julgado")
	public Date getDtTransitadoJulgado() {
		return dtTransitadoJulgado;
	}

	public void setDtTransitadoJulgado(Date dtTransitadoJulgado) {
		this.dtTransitadoJulgado = dtTransitadoJulgado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao_inicial", updatable = false, insertable = false)
	public Localizacao getLocalizacaoInicial() {
		return localizacaoInicial;
	}

	public void setLocalizacaoInicial(Localizacao localizacaoInicial) {
		this.localizacaoInicial = localizacaoInicial;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura_inicial", updatable = false, insertable = false)
	public Localizacao getEstruturaInicial() {
		return estruturaInicial;
	}

	public void setEstruturaInicial(Localizacao estruturaInicial) {
		this.estruturaInicial = estruturaInicial;
	}

	@Column(name = "in_apreciado_segredo", updatable = false, insertable = false)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.ProcessoTrfApreciadoType")
	public ProcessoTrfApreciadoEnum getApreciadoSegredo() {
		return apreciadoSegredo;
	}

	public void setApreciadoSegredo(ProcessoTrfApreciadoEnum apreciadoSegredo) {
		this.apreciadoSegredo = apreciadoSegredo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao", updatable = false, insertable = false)
	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	// @Column(name = "in_apreciado_tutela")
	// public Boolean getApreciadoTutela() {
	// return apreciadoTutela;
	// }
	//
	// public void setApreciadoTutela(Boolean apreciadoTutela) {
	// this.apreciadoTutela = apreciadoTutela;
	// }

	@Transient
	public ProcessoTrf getProcessoOriginario() {
		return processoOriginario;
	}

	public void setProcessoOriginario(ProcessoTrf processoOriginario) {
		this.processoOriginario = processoOriginario;
	}

	@Column(name = "in_apreciado_sigilo")
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.ProcessoTrfApreciadoType")
	public ProcessoTrfApreciadoEnum getApreciadoSigilo() {
		return apreciadoSigilo;
	}

	public void setApreciadoSigilo(ProcessoTrfApreciadoEnum apreciadoSigilo) {
		this.apreciadoSigilo = apreciadoSigilo;
	}

	@Transient
	public String getNumeroProcesssoTitularidade() {
		return cargo != null ? numeroProcesso + cargo.getSigla() : numeroProcesso;
	}

	@Transient
	public ProcessoTrf getProcessoDependencia() {
		return processoDependencia;
	}

	public void setProcessoDependencia(ProcessoTrf processoDependencia) {
		this.processoDependencia = processoDependencia;
	}

	@Transient
	public Double getValorCausaIncidente() {
		if (valorCausaIncidente == null && getProcessoOriginario() != null) {
			valorCausaIncidente = getProcessoOriginario().getValorCausa();
		}
		return valorCausaIncidente;
	}

	public void setValorCausaIncidente(Double valorCausaIncidente) {
		this.valorCausaIncidente = valorCausaIncidente;
	}

	@Transient
	public Boolean getIsIncidente() {
		return isIncidente;
	}

	public void setIsIncidente(Boolean isIncidente) {
		this.isIncidente = isIncidente;
	}

	@Column(name = "in_selecionado_pauta", updatable = false, insertable = false)
	@NotNull
	public Boolean getSelecionadoPauta() {
		return selecionadoPauta;
	}

	public void setSelecionadoPauta(Boolean selecionadoPauta) {
		this.selecionadoPauta = selecionadoPauta;
	}

	@Column(name = "in_revisado", nullable = false)
	@NotNull
	public Boolean getRevisado() {
		return revisado;
	}

	public void setRevisado(Boolean revisado) {
		this.revisado = revisado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_revisor")
	public OrgaoJulgador getOrgaoJulgadorRevisor() {
		return orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_relator_processo")
	public Pessoa getPessoaRelator() {
		return pessoaRelator;
	}

	public void setPessoaRelator(Pessoa pessoaRelator) {
		this.pessoaRelator = pessoaRelator;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaRelator(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaRelator(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaRelator(pessoa.getPessoa());
		} else {
			setPessoaRelator((Pessoa)null);
		}
	}

	@Column(name = "in_selecionado_julgamento")
	public boolean getSelecionadoJulgamento() {
		return selecionadoJulgamento;
	}

	public void setSelecionadoJulgamento(boolean selecionadoJulgamento) {
		this.selecionadoJulgamento = selecionadoJulgamento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcou_revisado")
	public Pessoa getPessoaMarcouRevisado() {
		return pessoaMarcouRevisado;
	}

	public void setPessoaMarcouRevisado(Pessoa pessoaMarcouRevisado) {
		this.pessoaMarcouRevisado = pessoaMarcouRevisado;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaMarcouRevisado(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoaMarcouRevisado(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaMarcouRevisado(pessoa.getPessoa());
		} else {
			setPessoaMarcouRevisado((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcou_pauta")
	public Pessoa getPessoaMarcouPauta() {
		return pessoaMarcouPauta;
	}

	public void setPessoaMarcouPauta(Pessoa pessoaMarcouPauta) {
		this.pessoaMarcouPauta = pessoaMarcouPauta;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaMarcouPauta(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaMarcouPauta(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaMarcouPauta(pessoa.getPessoa());
		} else {
			setPessoaMarcouPauta((Pessoa)null);
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcou_julgamento")
	public Pessoa getPessoaMarcouJulgamento() {
		return pessoaMarcouJulgamento;
	}

	public void setPessoaMarcouJulgamento(Pessoa pessoaMarcouJulgamento) {
		this.pessoaMarcouJulgamento = pessoaMarcouJulgamento;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoaMarcouJulgamento(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída
	 */
	public void setPessoaMarcouJulgamento(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaMarcouJulgamento(pessoa.getPessoa());
		} else {
			setPessoaMarcouJulgamento((Pessoa)null);
		}
	}

	@Column(name = "in_outra_instancia")
	public Boolean getInOutraInstancia() {
		return inOutraInstancia;
	}

	public void setInOutraInstancia(Boolean inOutraInstancia) {
		this.inOutraInstancia = inOutraInstancia;
	}

	@Column(name = "in_bloqueia_peticao")
	public boolean getInBloqueiaPeticao() {
		return inBloqueiaPeticao;
	}

	public void setInBloqueiaPeticao(boolean inBloqueiaPeticao) {
		this.inBloqueiaPeticao = inBloqueiaPeticao;
	}

	@Column(name = "in_apreciado_justica_gratuita")
	public Boolean getApreciadoJusticaGratuita() {
		return apreciadoJusticaGratuita;
	}

	public void setApreciadoJusticaGratuita(Boolean apreciadoJusticaGratuita) {
		this.apreciadoJusticaGratuita = apreciadoJusticaGratuita;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo")
	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_endereco_wsdl")
	public EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}

	public void setEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
	}

	@Transient
	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_referencia")
	public ProcessoTrf getProcessoReferencia() {
		return processoReferencia;
	}

	public void setProcessoReferencia(ProcessoTrf processoReferencia) {
		this.processoReferencia = processoReferencia;
	}

	@Column(name = "ds_proc_referencia", length = 50)
	@Length(max = 50)
	public String getDesProcReferencia() {
		return desProcReferencia;
	}

	public void setDesProcReferencia(String desProcReferencia) {
		this.desProcReferencia = desProcReferencia;
	}

	@Transient
	public Boolean getCheck() {
		return check;
	}

	public void setCheck(Boolean check) {
		this.check = check;
	}

	@Column(name = "nr_instancia")
	public Character getInstancia() {
		return instancia;
	}

	public void setInstancia(Character instancia) {
		this.instancia = instancia;
	}

	@Transient
	public boolean isIncidental() {
		if (classeJudicial != null) {
			return classeJudicial.getIncidental();
		} else {
			return false;
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sugestao_sessao")
	public Date getDataSugestaoSessao() {
		return dataSugestaoSessao;
	}

	public void setDataSugestaoSessao(Date dataSugestaoSessao) {
		this.dataSugestaoSessao = dataSugestaoSessao;
	}

	@Column(name = "in_pronto_revisao")
	public Boolean getProntoRevisao() {
		return prontoRevisao;
	}

	public void setProntoRevisao(Boolean prontoRevisao) {
		this.prontoRevisao = prontoRevisao;
	}

	/**
	 * método responsável por retornar o numero do processo com a sigla do
	 * cargo.
	 */
	@Transient
	public String getNumeroProcessoCargo() {
		return processo.getNumeroProcesso();
//		String numero = getNumeroProcesso();
//		if (numero != null) {
//			String inExibicaoTitularidadeProcesso = ParametroUtil
//					.getFromContext("inExibicaoTitularidadeProcesso", true);
//			String aplicacaoSistema = ParametroUtil.getFromContext("aplicacaoSistema", true);
//			if ((inExibicaoTitularidadeProcesso.equals(aplicacaoSistema) || inExibicaoTitularidadeProcesso.equals("0"))
//					&& !Strings.isEmpty(numero)) {
//				return numero.concat(cargo == null ? "" : cargo.getSigla());
//			} else {
//				return numero;
//			}
//		} else {
//			return numero;
//		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrfConsultaSemFiltros)) {
			return false;
		}
		ProcessoTrfConsultaSemFiltros other = (ProcessoTrfConsultaSemFiltros) obj;
		if (getIdProcessoTrf() != other.getIdProcessoTrf()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrf();
		return result;
	}

}