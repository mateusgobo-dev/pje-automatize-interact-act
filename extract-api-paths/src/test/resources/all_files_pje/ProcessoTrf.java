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


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.ParamDef;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;
import org.hibernate.validator.constraints.Length;

import br.jus.cnj.pje.pjecommons.utils.NumeroProcessoUtil;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.filters.ProcessoTrfFilter;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;
import br.jus.pje.nucleo.enums.ComposicaoJulgamentoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.SituacaoGuiaRecolhimentoEnum;
import br.jus.pje.nucleo.util.NumeracaoUnicaUtil;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Entidade destinada a representar o processo judicial no sistema PJe.
 * 
 * @author Rodrigo Menezes
 */
@Entity
@Table(name = ProcessoTrf.TABLE_NAME)
@FilterDefs(value = {
		@FilterDef(name = ProcessoTrfFilter.FILTER_ADVOGADO, parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA) }),
		@FilterDef(name = ProcessoTrfFilter.FILTER_JUS_POSTULANDI, parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA) }),
		@FilterDef(name = ProcessoTrfFilter.FILTER_PROCURADOR, parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE) }),
		@FilterDef(name = ProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA, parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE) }),
		@FilterDef(name = ProcessoTrfFilter.FILTER_PERITO, parameters = { 
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO) }),
		
		@FilterDef(name = ProcessoTrfFilter.FILTER_LOCALIZACAO_SERVIDOR, 
			parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_BOOLEAN, name = ProcessoTrfFilter.FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO),
			}),
		
		@FilterDef(name = ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, parameters = { 
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO)}),
		
		@FilterDef(name = ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO, parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_DATE, name = ProcessoTrfFilter.FILTER_PARAM_DATA_ATUAL) }),
		
		@FilterDef(name = ProcessoTrfFilter.FILTER_CARGO, parameters = {
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_DATE, name = ProcessoTrfFilter.FILTER_PARAM_DATA_ATUAL) }),

		@FilterDef(name = ProcessoTrfFilter.FILTER_SEGREDO_JUSTICA, 
			parameters = { 
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO), 
				@ParamDef(type = ProcessoTrfFilter.TYPE_BOOLEAN, name = ProcessoTrfFilter.FILTER_PARAM_VISUALIZA_SIGILOSO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS),
				@ParamDef(type = ProcessoTrfFilter.TYPE_INT, name = ProcessoTrfFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO),
				@ParamDef(type = ProcessoTrfFilter.TYPE_BOOLEAN, name = ProcessoTrfFilter.FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO),
			})})

@Filters(value = {
		@Filter(name = ProcessoTrfFilter.FILTER_PROCURADOR, condition = ProcessoTrfFilter.CONDITION_PROCURADOR),
		@Filter(name = ProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA, condition = ProcessoTrfFilter.CONDITION_ASSISTENTE_PROCURADORIA),
		@Filter(name = ProcessoTrfFilter.FILTER_ADVOGADO, condition = ProcessoTrfFilter.CONDITION_ADVOGADO),
		@Filter(name = ProcessoTrfFilter.FILTER_JUS_POSTULANDI, condition = ProcessoTrfFilter.CONDITION_JUS_POSTULANDI),
		@Filter(name = ProcessoTrfFilter.FILTER_PERITO, condition = ProcessoTrfFilter.CONDITION_PERITO),

		@Filter(name = ProcessoTrfFilter.FILTER_LOCALIZACAO_SERVIDOR, condition=ProcessoTrfFilter.CONDITION_LOCALIZACOES_SERVIDOR),
		@Filter(name = ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, condition = ProcessoTrfFilter.CONDITION_ORGAO_COLEGIADO),
		@Filter(name = ProcessoTrfFilter.FILTER_SEGREDO_JUSTICA, condition = ProcessoTrfFilter.CONDITION_SEGREDO_JUSTICA),
		@Filter(name = ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO, condition = ProcessoTrfFilter.CONDITION_ORGAO_JULGADOR_CARGO),
		@Filter(name = ProcessoTrfFilter.FILTER_CARGO, condition = ProcessoTrfFilter.CONDITION_CARGO)})

@IndexedEntity(id="idProcessoTrf", value="processo", 
	mappings = { 
		@Mapping(beanPath="numeroSequencia", mappedPath="nseq"),
		@Mapping(beanPath="numeroDigitoVerificador", mappedPath="ndv"),
		@Mapping(beanPath="ano", mappedPath="ano"),
		@Mapping(beanPath="numeroOrgaoJustica", mappedPath="norgao"),
		@Mapping(beanPath="numeroOrigem", mappedPath="norigem"),
		@Mapping(beanPath="numeroProcesso", mappedPath="numero"),
		@Mapping(beanPath="segredoJustica", mappedPath="sigiloso"),
		@Mapping(beanPath="classeJudicial", mappedPath="classe"),
		@Mapping(beanPath="assuntoTrfList", mappedPath="assuntos"),
		@Mapping(beanPath="dataDistribuicao", mappedPath="distribuicao"),
		@Mapping(beanPath="orgaoJulgadorCargo.sigla", mappedPath="codigojuizo"),
		@Mapping(beanPath="orgaoJulgador", mappedPath="orgaojulgador"),
		@Mapping(beanPath="orgaoJulgadorColegiado", mappedPath="colegiado"),
		@Mapping(beanPath="processoOriginario.numeroProcesso", mappedPath="originario"),
		@Mapping(beanPath="processoParteList", mappedPath="partes"),
		@Mapping(beanPath="pessoaRelator.nome", mappedPath="relator"),
		@Mapping(beanPath="valorPesoProcessual", mappedPath="pesoprocessual"),
		@Mapping(beanPath="valorPesoDistribuicao", mappedPath="pesodistribuicao"),
		@Mapping(beanPath="valorCausa", mappedPath="valorcausa"),
		@Mapping(beanPath="competencia", mappedPath="competencia"),
		@Mapping(beanPath="jurisdicao", mappedPath="jurisdicao"),
		@Mapping(beanPath="situacoes", mappedPath="situacoes"),
		@Mapping(beanPath="objeto", mappedPath="objeto")
})
public class ProcessoTrf implements java.io.Serializable, FieldHandled {

	private static final long serialVersionUID = -8887074424731345869L;

	public static final String TABLE_NAME = "tb_processo_trf";

	private static final int POLO_PASSIVO = 1;
	private static final int POLO_ATIVO = 2;

	private int idProcessoTrf;
	private AssuntoTrf assuntoTrf;
	private boolean selecionadoJulgamento;
	private Boolean segredoJustica = null;
	
	private Boolean justicaGratuita;
	private Boolean inOutraInstancia = Boolean.FALSE;
	private boolean inBloqueiaPeticao = false;
	private Boolean isIncidente = Boolean.FALSE;
	private Boolean selecionadoPauta = Boolean.FALSE;
	private Boolean revisado = Boolean.FALSE;
	private Boolean check;
	private ClasseJudicial classeJudicial;
	private ClasseJudicialInicialEnum inicial;
	private Date dataAutuacao;
	private Date dtTransitadoJulgado;
	private Date dtSolicitacaoInclusaoPauta;
	private Date dataDistribuicao;
	private Double valorCausa;
	private Double valorCausaIncidente;
	private Double valorPesoProcessual;
	private Double valorPesoDistribuicao;
	private EnderecoWsdl enderecoWsdl;
	private Integer numeroSequencia;
	private Integer ano;
	private Integer numeroDigitoVerificador;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;
	private int nivelAcesso;
	
	private Jurisdicao jurisdicao;
	private Localizacao localizacaoInicial;
	private Localizacao estruturaInicial;
	private Municipio municipioFatoPrincipal;
	
	private OrgaoJulgador orgaoJulgador;
	private OrgaoJulgador orgaoJulgadorRevisor;
	private Date dataVinculacaoRevisor;
	private OrgaoJulgadorCargo orgaoJulgadorCargo;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
	private Pessoa pessoaMarcouRevisado;
	private Pessoa pessoaMarcouPauta;
	private Pessoa pessoaMarcouJulgamento;
	private PessoaMagistrado pessoaRelator;
	private Date dataVinculacaoRelator;
	private Processo processo;
	private ProcessoStatusEnum processoStatus;
	
	private ProcessoTrf processoOriginario;
	private ProcessoTrf processoDependencia;
	private ProcessoTrf processoReferencia;
	private ProcessoTrfApreciadoEnum apreciadoSegredo = ProcessoTrfApreciadoEnum.N;
	private ProcessoTrfApreciadoEnum apreciadoSigilo = ProcessoTrfApreciadoEnum.N;
	private String observacaoSegredo;
	private String prioridadesString;
	private String numeroProcesso;
	private String nomeParte;
	private String desProcReferencia;
	private String objeto;
	private TipoPessoa tipoPessoa;
	private Character violacaoFaixaValoresCompetencia;

	private ClasseJudicial classeJudicialOutraInstancia;
	private Boolean apreciadoJusticaGratuita = Boolean.FALSE;
	private Boolean tutelaLiminar;
	private Boolean apreciadoTutelaLiminar;
	private Character instancia;
	private PessoaMagistrado pessoaApreciouJusticaGratuita;
	private Cargo cargo;
	private Date dataSugestaoSessao;
	private Boolean prontoRevisao;
	private Sessao sessaoSugerida;
	
	private Competencia competencia;
	private Boolean pautaVirtual = Boolean.FALSE;

	private List<ProcessoParte> processoParteList = new ArrayList<>();
	private List<ProcessoPericia> processoPericiaList = new ArrayList<>();
	private List<ProcessoAssunto> processoAssuntoList = new ArrayList<>();
	private List<PrioridadeProcesso> prioridadeProcessoList = new ArrayList<>();
	private List<ProcessoPrioridadeProcesso> processoPrioridadeProcessoList = new ArrayList<>();
	private List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfList = new ArrayList<>();
	private List<ProcessoLote> processoLoteList = new ArrayList<>();
	private List<ProcessoTrfConexao> processoTrfConexaoList = new ArrayList<>();
	private List<ProcessoAudiencia> processoAudienciaList = new ArrayList<>();
	private Set<ProcessoAlerta> processoAlertaList = new HashSet<>();
	private List<ProcessoParte> listaParteAtivo;
	private List<ProcessoParte> listaPartePassivo;
	private List<ProcessoParte> listaParteTerceiro;
	private List<ProcessoParte> listaFiscal;
	private List<ProcessoProcedimentoOrigem> procedimentoOrigemList = new ArrayList<>();
	private List<AssuntoTrf> assuntoTrfList = new ArrayList<>();

	private List<EstatisticaProcessoJusticaFederal> estatisticaProcessoJusticaFederalList = new ArrayList<>();
	private List<Rpv> rpvList = new ArrayList<>();
	private List<LogHistoricoMovimentacao> logHistoricoMovimentacaotList = new ArrayList<>();
	
	private Boolean mandadoDevolvido = false;
	private Boolean deveMarcarAudiencia = false;

	private ComplementoProcessoJE complementoJE;
	
	private TipoParte tipoParteAdvogado = null;

	private List<ProcessoVisibilidadeSegredo> visualizadores = new ArrayList<>();

	private List<ProcessoCaixaAdvogadoProcurador> caixasRepresentantes =new ArrayList<>();
	
	private ConsultaProcessoTrfSemFiltro consultaProcessoTrf;
	
	private List<SituacaoProcessual> situacoes = new ArrayList<>();
	
	private Boolean exigeRevisor;
	
	private ComposicaoJulgamentoEnum composicaoJulgamento;
	
	private FieldHandler handler;
	private Integer idAreaDireito;
	
	private SituacaoGuiaRecolhimentoEnum situacaoGuiaRecolhimento;

	private List<Cda> colecaoCda = new ArrayList<Cda>(0);
	
	private Boolean inBloqueioMigracao = Boolean.FALSE;

	private String nomesPartesFormatada;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "processoTrf",  cascade = {CascadeType.ALL})
	@LazyToOne(LazyToOneOption.NO_PROXY)
	public ComplementoProcessoJE getComplementoJE() {
		if (this.handler != null && this.complementoJE != null) {
			return (ComplementoProcessoJE) this.handler.readObject(
					this, "complementoJE", complementoJE);
		}
		return this.complementoJE;
	}
	
	@Transient	
	public String getComplementoJEUfMunicipioExtenso() {		
		String complementoJE = "";
		if (getComplementoJE() != null && getComplementoJE().getMunicipioEleicao() != null) {
			complementoJE = getComplementoJE().getMunicipioEleicao().getMunicipio();
		}
		if (complementoJE != null && !complementoJE.isEmpty()) {
			complementoJE += " - ";
		}
		if (getComplementoJE() != null && getComplementoJE().getEstadoEleicao() != null) {
			complementoJE += getComplementoJE().getEstadoEleicao().getEstado();
		}
		return complementoJE;
	}

	public void setComplementoJE(ComplementoProcessoJE complementoJE) {
		if (this.handler != null) {
			this.complementoJE = (ComplementoProcessoJE) this.handler.writeObject(
					this, "complementoJE", this.complementoJE, complementoJE);
		}
		this.complementoJE = complementoJE;
	}

	public ProcessoTrf() {
	}

	/**
	 * Recupera o identificador do processo judicial. Esse identificador é, por arquitetura, igual ao identificador
	 * do objeto {@link Processo} que pode ser obtido por meio de {@link ProcessoTrf#getProcesso()}
	 * 
	 * @return o identificador unívoco deste processo no banco de dados
	 * @see ProcessoTrf#getProcesso()
	 * @see Processo#getIdProcesso()
	 */
	@Id
	@Column(name = "id_processo_trf", unique = true, nullable = false, updatable = false)
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	/**
	 * Atribui a este processo judicial um identificador unívoco. Esse identificador a ser atribuído
	 * deve ser idêntico ao do {@link Processo} vinculado ao processo judicial.
	 * 
	 * @param idProcesso o identificador a ser atribuído
	 * 
	 * @see ProcessoTrf#getIdProcessoTrf()
	 * @see ProcessoTrf#getProcesso()
	 * @see Processo#getIdProcesso()
	 */
	public void setIdProcessoTrf(int idProcesso) {
		this.idProcessoTrf = idProcesso;
	}

	/**
	 * Recupera o objeto {@link Processo} vinculado ao processo judicial. O {@link Processo} é a ponte entre o
	 * processo judicial e o fluxo de negócio a ele atribuído.
	 * 
	 * @return o {@link Processo} vinculado ao processo judicial.
	 */
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id_processo_trf")
	public Processo getProcesso() {
		return processo;
	}

	/**
	 * Atribui a este processo judicial um {@link Processo}. O {@link Processo} é a ponte entre o processo judicial
	 * e o fluxo de negócio a ele atribuído.
	 *  
	 * @param processo o {@link Processo} a ser atribuído
	 */
	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	/**
	 * Recupera a classe atualmente vinculada ao processo judicial.
	 * 
	 * @return a classe do processo judicial
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_classe_judicial", nullable = false)
	@NotNull
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	/**
	 * Atribui a este processo judicial uma classe.
	 * 
	 * @param classeJudicial a classe a ser atribuída
	 */
	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	/**
	 * Recupera o magistrado responsável pela apreciação do pedido de assistência judiciária gratuita.
	 * 
	 * @return o magistrado responsável pela apreciação do pedido
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pes_apreciou_jus_gratuita")
	public PessoaMagistrado getPessoaApreciouJusticaGratuita() {
		return pessoaApreciouJusticaGratuita;
	}

	/**
	 * Atribui a este processo judicial um magistrado responsável pela apreciação do pedido de assistência 
	 * judiciária gratuita.
	 *   
	 * @param pessoaApreciouJusticaGratuita o magistrdo que apreciou o pedido.
	 */
	public void setPessoaApreciouJusticaGratuita(PessoaMagistrado pessoaApreciouJusticaGratuita) {
		this.pessoaApreciouJusticaGratuita = pessoaApreciouJusticaGratuita;
	}

	/**
	 * Recupera a classe deste processo judicial quando da tramitação em outra instância jurisdicional.
	 *  
	 * @return a classe em outra instância jurisdicional
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cl_judicial_outra_instancia")
	public ClasseJudicial getClasseJudicialOutraInstancia() {
		return classeJudicialOutraInstancia;
	}

	/**
	 * Atribui a este processo judicial uma classe que teria sido a sua em outra instância jurisdicional.
	 * 
	 * @param classeJudicialOutraInstancia a classe a ser atribuída.
	 * 
	 */
	public void setClasseJudicialOutraInstancia(ClasseJudicial classeJudicialOutraInstancia) {
		this.classeJudicialOutraInstancia = classeJudicialOutraInstancia;
	}

	/**
	 * Recupera a jurisdição a que está vinculado este processo judicial.
	 * 
	 * @return a jurisdição vinculada
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_jurisdicao")
	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	/**
	 * Atribui uma jurisdição a este processo judicial.
	 * 
	 * @param jurisdicao a jurisdição a ser vinculada.
	 * 
	 */
	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	/**
	 * Recupera a lista de partes que compõe esse processo. Essa lista inclui autores, réus, terceiros interessados 
	 * e as pessoas a eles vinculadas. A lista é retornada em ordem crescente de inclusão na lista.
	 *   
	 * @return a lista de partes vinculadas a esse processo.
	 * 
	 */
	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	@OrderBy("idProcessoParte ASC")
	public List<ProcessoParte> getProcessoParteList() {
		return processoParteList;
	}

	/**
	 * Atribui uma lista de partes a esse processo. 
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param processoParteList a lista a ser atribuida
	 * 
	 */
	public void setProcessoParteList(List<ProcessoParte> processoParteList) {
		this.processoParteList = processoParteList;
	}

	/**
	 * Recupera a lista de partes do polo ativo do processo judicial que nele constam como inativas.
	 *  
	 * @return a lista de partes do polo ativo que constam como inativas
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteInativoPoloAtivoList() {
		return getProcessoParteInativoList(ProcessoParteParticipacaoEnum.A);
	}

	/**
	 * Recupera a lista de partes do polo passivo do processo judicial que nele constam como inativas.
	 *  
	 * @return a lista de partes do polo passivo que constam como inativas
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteInativoPoloPassivoList() {
		return getProcessoParteInativoList(ProcessoParteParticipacaoEnum.P);
	}

	/**
	 * Recupera a lista de partes terceiros interessados não vinculado a um polo processual 
	 * que constam como inativas.
	 * 
	 * @return a lista de partes terceiros interessados inativas
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteInativoOutrosParticipantesList() {
		return getProcessoParteInativoList(ProcessoParteParticipacaoEnum.T);
	}

	/**
	 * Recupera a lista de partes do polo indicado que constam como inativas.
	 * 
	 * @param inParticipacao o polo a respeito do qual se pretende obter a lista
	 * @return a listas de partes inativas do polo indicado
	 * @see Transient
	 */
	@Transient
	private List<ProcessoParte> getProcessoParteInativoList(ProcessoParteParticipacaoEnum inParticipacao) {
		List<ProcessoParte> ppList = getProcessoParteList();
		List<ProcessoParte> ppInativoList = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : ppList) {
			if (processoParte.getInParticipacao() == inParticipacao) {
				if (!processoParte.getIsAtivo()) {
					ppInativoList.add(processoParte);
				}
			}
		}
		return ppInativoList;
	}

	/**
	 * Recupera a lista de perícias realizadas no processo judicial.
	 * 
	 * @return lista de {@link ProcessoPericia} do processo.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoPericia> getProcessoPericiaList() {
		return processoPericiaList;
	}

	/**
	 * Atribui a este processo uma lista de perícias.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.
	 *   
	 * @param processoPericiaList a lista a ser atribuída
	 */
	public void setProcessoPericiaList(List<ProcessoPericia> processoPericiaList) {
		this.processoPericiaList = processoPericiaList;
	}

	/**
	 * Recupera a lista de complementos de classe vinculadas a esse processo judicial.
	 * 
	 * @return a lista de complementos de classe cadastrados para esse processo judicial
	 * @see ComplementoClasse
	 * 
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ComplementoClasseProcessoTrf> getComplementoClasseProcessoTrfList() {
		return this.complementoClasseProcessoTrfList;
	}

	/**
	 * Atribui a este processo uma lista de complementos de classe.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3.2, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.
	 *   
	 * @param complementoClasseProcessoTrfList a lista a ser atribuída
	 */
	public void setComplementoClasseProcessoTrfList(List<ComplementoClasseProcessoTrf> complementoClasseProcessoTrfList) {
		this.complementoClasseProcessoTrfList = complementoClasseProcessoTrfList;
	}

	/**
	 * Número sequencial (NNNNNNN) do processo segundo a Resolução CNJ 65.
	 * 
	 * @return o número sequencial do processo judicial
	 */
	@Column(name = "nr_sequencia")
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	/**
	 * Atribui a este processo um número sequencial (NNNNNNN) na forma prevista pela Resolução CNJ 65.
	 * 
	 * @param numeroSequencia o número a ser atribuído
	 */
	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	/**
	 * Recupera o ano (AAAA) da propositura do processo judicial na forma prevista pela Resolução CNJ 65.
	 * 
	 * @return o ano da propositura
	 */
	@Column(name = "nr_ano")
	public Integer getAno() {
		return ano;
	}

	/**
	 * Atribui a este processo o ano (AAAA) de sua propositura na forma prevista pela Resolução CNJ 65.
	 * 
	 * @param ano o ano de propositura a ser atribuído
	 */
	public void setAno(Integer ano) {
		this.ano = ano;
	}

	/**
	 * Recupera o dígito verificador (DD) do processo judicial. Esse dígito é resultado do cálculo determinado
	 * pela Resolução CNJ 65.
	 * 
	 * @return o número do dígito verificador
	 * @see NumeroProcessoUtil#calcDigitoVerificador(long, long, long, long)
	 */
	@Column(name = "nr_digito_verificador")
	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	/**
	 * Atribui a este processo um dígito verificador (DD) na forma prevista pela Resolução CNJ 65.
	 * 
	 * @param numeroDigitoVerificador o número a ser atribuído.
	 * @see NumeroProcessoUtil#calcDigitoVerificador(long, long, long, long)
	 */
	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	/**
	 * Recupera o número identificador do tribunal em que tramita o processo judicial.
	 * Esse número é o equivalente ao JTR de que trata a Resolução CNJ 65.
	 * 
	 * @return o número JTR de que trata a Resolução CNJ 65.
	 * 
	 */
	@Column(name = "nr_identificacao_orgao_justica")
	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}

	/**
	 * Atribui a este processo um número identificador de seu tribunal (JTR) na forma prevista pela
	 * Resolução CNJ 65.
	 * 
	 * @param numeroOrgaoJustica o número identificador da Justiça e do Tribunal (JTR) a ser atribuído
	 */
	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	/**
	 * Recupera a data em que foi autuado o processo judicial. 
	 * Por data de autuação, considera-se aquela em que os dados básicos para seu protocolo 
	 * estão completos. 
	 * 
	 * @return a data de autuação do processo
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_autuacao")
	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	/**
	 * Atribui a este processo uma data de autuação.
	 * Por data de autuação, considera-se aquela em que os dados básicos para seu protocolo 
	 * estão completos. 
	 * 
	 * @param dataAutuacao a data a ser atribuída.
	 */
	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	/**
	 * Recupera o número de origem (OOOO) deste processo na forma prevista pela Resolução CNJ 65.
	 * Este número de origem será:
	 * <li>para instâncias de revisão comuns, o código 0000</li>
	 * <li>para turmas recursais, código iniciado por 9 (9OOO)</li>
	 * <li>para as Justiças Militar, Eleitoral e Trabalhista, o código do órgão julgador distribuído no primeiro grau</li>
	 * <li>para a Justiça dos Estados, o código do fórum em que foi distribuído o processo</li>
	 * <li>para a Justiça Federal, o código da subseção judiciária em que foi distribuído o processo</li> 
	 * 
	 * @return o código de origem do tribunal.
	 */
	@Column(name = "nr_origem_processo")
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	/**
	 * Atribui a este processo um número de origem (OOOO) na forma prevista pela Resolução CNJ 65.
	 * Este número de origem será:
	 * <li>para instâncias de revisão comuns, o código 0000</li>
	 * <li>para turmas recursais, código iniciado por 9 (9OOO)</li>
	 * <li>para as Justiças Militar, Eleitoral e Trabalhista, o código do órgão julgador distribuído no primeiro grau</li>
	 * <li>para a Justiça dos Estados, o código do fórum em que foi distribuído o processo</li>
	 * <li>para a Justiça Federal, o código da subseção judiciária em que foi distribuído o processo</li> 
	 * 
	 * @param numeroOrigem o código de origem a ser atribuído
	 */
	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	/**
	 * Recupera o valor da causa do processo.
	 * 
	 * @return o valor numérico da causa, na moeda atual.
	 */
	@Column(name = "vl_causa")
	public Double getValorCausa() {
		return this.valorCausa;
	}

	/**
	 * Atribui a este processo um valor da causa, na moeda atual.
	 * 
	 * @param valorCausa o valor numérico, na moeda atual.
	 */
	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	/**
	 * Indica se a tramitação deste processo judicial deve ser feita sob segredo de justiça.
	 *   
	 * @return true, se a tramitação estiver sob segredo de justiça.
	 */
	@Column(name = "in_segredo_justica")
	public Boolean getSegredoJustica() {
		if(this.segredoJustica == null){
		 	//[PJEII-5316] - Se a classe judicial for sigilosa, o processo é sigiloso
		 	this.segredoJustica = getClasseJudicial() != null && 
		 		getClasseJudicial().getSegredoJustica() != null && 
		 		getClasseJudicial().getSegredoJustica();
	 	}
		return this.segredoJustica;
	}

	/**
	 * Atribui a este processo judicial marca indicativa de que sua tramitação deve ser feita sob segredo de justiça.
	 * 
	 * @param segredoJustica a indicação se o processo deve (true) ou não (false) tramitar sob segredo. 
	 */
	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	/**
	 * Indica se uma parte deste processo judicial recebeu os benefícios da justiça gratuita de que trata a Lei 1.050/1960.
	 *   
	 * @return true, se houve concessão de pedido de gratuidade
	 */
	@Column(name = "in_justica_gratuita")
	public Boolean getJusticaGratuita() {
		return this.justicaGratuita;
	}

	/**
	 * Atribui a este processo judicial a característica de que houve concessão de gratuidade judiciária na forma da Lei 1.050/1960.
	 * 
	 * @param justicaGratuita a indicação de que houve (true) ou não (false) a concessão de pedido de gratuidade judiciária
	 */
	public void setJusticaGratuita(Boolean justicaGratuita) {
		this.justicaGratuita = justicaGratuita;
	}

	/**
	 * Indica se houve concessão de pedido liminar ou antecipatório no processo judicial.
	 * 
	 * @return true, se houve uma concessão tal.
	 */
	@Column(name = "in_tutela_liminar")
	public Boolean getTutelaLiminar() {
		return this.tutelaLiminar;
	}

	/**
	 * Atribui a este processo a característica de que houve concessão de pedido liminar ou antecipatório.
	 * 
	 * @param tutelaLiminar a indicação de que houve a concessão de pedido liminar ou antecipatório
	 */
	public void setTutelaLiminar(Boolean tutelaLiminar) {
		this.tutelaLiminar = tutelaLiminar;
	}

	/**
	 * Indica se houve uma apreciação de pedido liminar ou antecipatório neste processo judicial, independentemente
	 * do resultado dessa apreciação.
	 * 
	 * @return true, se, em algum momento, foi apreciado um pedido liminar ou antecipatório no processo.
	 */
	@Column(name = "in_apreciado_tutela_liminar")
	public Boolean getApreciadoTutelaLiminar() {
		return apreciadoTutelaLiminar;
	}

	/**
	 * Atribui a este processo judicial a indicação relativa à existência de apreciação de pedido liminar ou antecipatório.
	 * 
	 * @param apreciadoTutelaLiminar a indicação de que houve (true) ou não (false) houve apreciação de pedido liminar
	 */
	public void setApreciadoTutelaLiminar(Boolean apreciadoTutelaLiminar) {
		this.apreciadoTutelaLiminar = apreciadoTutelaLiminar;
	}

	/**
	 * Recupera texto indicativo da causa por que se solicitou a decretação de tramitação do processo sob segredo.
	 * 
	 * @return texto de justificativa da tramitação do processo sob segredo
	 * @see #getSegredoJustica()
	 */
	@Column(name = "ds_observacao_segredo", length = 100)
	@Length(max = 100)
	public String getObservacaoSegredo() {
		return this.observacaoSegredo;
	}

	/**
	 * Atribui a este processo judicial uma justificativa para a tramitação sigilosa.
	 * 
	 * @param observacaoSegredo o texto justificativo
	 */
	public void setObservacaoSegredo(String observacaoSegredo) {
		this.observacaoSegredo = observacaoSegredo;
	}

	/**
	 * Recupera o número do processo judicial formatado.
	 * 
	 * @return o número do processo judicial, ou String vazia se ele ainda não foi numerado. 
	 */
	@Override
	public String toString() {
		return !isNumerado() ? "" : getNumeroProcesso();
	}

	/**
	 * Recupera indicação relativa ao fato de esse processo judicial ser inicial, inicial incidental ou inicial recursal.
	 * 
	 * @return a indicação relativa a este processo ser inicial simples ({@link ClasseJudicialInicialEnum#I}), 
	 * 		inicial incidental ({@link ClasseJudicialInicialEnum#D}) ou inicial recursal ({@link ClasseJudicialInicialEnum#R}).
	 */
	@Column(name = "in_inicial", length = 1)
	@Enumerated(EnumType.STRING)
	public ClasseJudicialInicialEnum getInicial() {
		return this.inicial;
	}

	/**
	 * Atribui a este processo judicial a indicação de ele ter sido proposto como processo inicial simples, inicial incidental
	 * a outro processo ou inicial recursal.
	 * 
	 * @param inicial a indicação do tipo de propositura inicial deste processo.
	 */
	public void setInicial(ClasseJudicialInicialEnum inicial) {
		this.inicial = inicial;
	}

	/**
	 * Recupera a situação atual desse processo, se em elaboração, verificado ou distribuído.
	 * 
	 * @return {@link ProcessoStatusEnum#E}, caso o processo estea em elaboração, 
	 * 		{@link ProcessoStatusEnum#V}, se já houve verificação da completude da autuação, ou
	 * 		{@link ProcessoStatusEnum#D}, se o processo já foi distribuído 
	 * 
	 * @see AutuacaoService#autuarProcesso(ProcessoTrf)
	 * @see #validarProcessoParaAutuacao()
	 */
	@Column(name = "cd_processo_status", length = 1)
	@Enumerated(EnumType.STRING)
	public ProcessoStatusEnum getProcessoStatus() {
		return this.processoStatus;
	}

	/**
	 * Atribui a este processo judicial uma situação da verificação de seus dados de autuação.
	 * 
	 * @param processoStatus o status a ser atribuído
	 */
	public void setProcessoStatus(ProcessoStatusEnum processoStatus) {
		this.processoStatus = processoStatus;
	}

	/**
	 * Recupera a lista de assuntos vinculados a este processo judicial.
	 * 
	 * @return a lista de assuntos vinculados
	 * @see AssuntoTrf
	 */
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "processoTrf", orphanRemoval = true)
	public List<ProcessoAssunto> getProcessoAssuntoList() {
		return this.processoAssuntoList;
	}

	/**
	 * Atribui a este processo uma lista de assuntos.
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param processoAssuntoList a lista a ser atribuída
	 */
	public void setProcessoAssuntoList(List<ProcessoAssunto> processoAssuntoList) {
		this.processoAssuntoList = processoAssuntoList;
	}

	/**
	 * Recupera a lista de prioridades associadas a esse processo judicial.
	 * 
	 * @return a lista de prioridades a ser atribuída
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_proc_prioridde_processo", joinColumns = { @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_prioridade_processo", nullable = false, updatable = false) })
	public List<PrioridadeProcesso> getPrioridadeProcessoList() {
		return this.prioridadeProcessoList;
	}

	/**
	 * Atribui a este processo uma lista de prioridades associadas.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param prioridadeProcessoList a lista a ser atribuída
	 */
	public void setPrioridadeProcessoList(List<PrioridadeProcesso> prioridadeProcessoList) {
		this.prioridadeProcessoList = prioridadeProcessoList;
	}

	/**
	 * Recupera a lista de relacionamentos de prioridades existentes entre esse processo e as prioridades cadastradas no sistema.
	 * Para recuperar a lista de prioridades diretamente, use {@link #getPrioridadeProcessoList()}.
	 * 
	 * @return a lista de relacionamentos de prioridades.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoPrioridadeProcesso> getProcessoPrioridadeProcessoList() {
		return this.processoPrioridadeProcessoList;
	}

	/**
	 * Atribui a este processo judicial uma lista de relacionamentos de prioridades.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param processoPrioridadeProcessoList a lista a ser atribuída
	 */
	public void setProcessoPrioridadeProcessoList(List<ProcessoPrioridadeProcesso> processoPrioridadeProcessoList) {
		this.processoPrioridadeProcessoList = processoPrioridadeProcessoList;
	}

	/**
	 * Recupera a lista de alertas vinculados a este processo judicial.
	 * 
	 * @return a lista de alertas vinculados.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public Set<ProcessoAlerta> getProcessoAlertaList() {
		return processoAlertaList;
	}

	/**
	 * Atribui a este processo uma lista de alertas.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param processoAlertaList a lista a ser atribuída
	 */
	public void setProcessoAlertaList(Set<ProcessoAlerta> processoAlertaList) {
		this.processoAlertaList = processoAlertaList;
	}

	/**
	 * Recupera a representação em String do número do processo judicial formatado como previsto 
	 * pela Resolução CNJ 65.
	 * 
	 * @return o número do processo como String no formato NNNNNNN-DD.AAAA.J.TR.OOOO 
	 * * @see Transient
	 */
	@Transient
	public String getNumeroProcesso() {
		if (processo != null && processo.getNumeroProcesso() != null) {
			numeroProcesso = processo.getNumeroProcesso();
		} else if (numeroProcesso == null || numeroProcesso.isEmpty()) {
			numeroProcesso = isNumerado() ? NumeracaoUnicaUtil.formatNumeroProcesso(this) : null;
		}
		return numeroProcesso;
	}
	/**
	 * Recupera o valor da causa deste processo judicial formatado para a {@link Locale} pt-BR.
	 * 
	 * @return o valor da causa com formatação decimal.
	 * @see Transient
	 */
	@Transient
	public String getVlCausa() {
		return StringUtil.formatarValorMoeda(valorCausa, false);
	}

	/**
	 * Indica se este processo judicial já foi numerado.
	 *  
	 * @return true, se já foi atribuído um número sequencial para este processo.
	 * @see Transient
	 */
	@Transient
	public boolean isNumerado() {
		return getNumeroSequencia() != null;
	}

	/**
	 * Recupera representação em lista de Strings das partes do processo judicial quanto ao polo indicado,
	 * concatenando o nome da parte com o número de seu cadastro no Ministério da Fazenda (CPF/CNPJ).
	 *  
	 * @param participacaoEnum o polo processual a ser recuperado.
	 * @return a lista de Strings contendo as partes do polo processual indicado.
	 * @see Transient
	 */
	@Transient
	public List<String> getListaPartePolo(ProcessoParteParticipacaoEnum participacaoEnum) {
		List<String> list = new ArrayList<String>();
		StringBuffer partes = new StringBuffer();
		for (ProcessoParte processoParte : getListaPartePoloObj(participacaoEnum)) {
			partes.append(processoParte.getTipoParte().getTipoParte()).append(": ");
			partes.append(processoParte.getPessoa().getNome()).append(" (");
			partes.append(processoParte.getPessoa().getDocumentoCpfCnpj());
			partes.append(")");
			list.add(partes.toString());
			partes = new StringBuffer();
		}
		return list;
	}

	/**
	 * Recupera representação em lista de Strings das partes do processo judicial quanto ao polo indicado.
	 * 
	 * @param participacaoEnum o polo processual a ser recuperado
	 * @return a lista de Strings contendo as partes do polo processual indicadoo
	 * @see Transient
	 */
	@Transient
	public List<String> getListaNomePessoa(ProcessoParteParticipacaoEnum participacaoEnum) {
		List<String> list = new ArrayList<String>();
		StringBuffer partes = new StringBuffer();
		for (ProcessoParte processoParte : getListaPartePoloObj(participacaoEnum)) {
			partes.append(processoParte.getPessoa().getNome());
			list.add(partes.toString());
			partes = new StringBuffer();
		}
		return list;
	}

	/**
	 * Recupera a lista de pessoas jurídicas que são representadas por autoridades que compõem o polo
	 * passivo do processo judicial.
	 * 
	 * @return a lista de pessoas jurídicas representadas pelas autoridades do polo passivo
	 * @see Transient
	 */
	@Transient
	public List<PessoaJuridica> getProcessoPartePoloPassivoOrgaoVinculacao() {
		List<PessoaJuridica> processoPartePoloPassivoOrgaoVinculacaoList = new ArrayList<PessoaJuridica>(0);
		for (ProcessoParte processoParte : getListaPartePassivo()) {
			if (processoParte.getPessoa() instanceof PessoaAutoridade) {
				PessoaJuridica orgaoVinculacao = ((PessoaAutoridade) processoParte.getPessoa()).getOrgaoVinculacao();
				if (!processoPartePoloPassivoOrgaoVinculacaoList.contains(orgaoVinculacao)) {
					processoPartePoloPassivoOrgaoVinculacaoList.add(orgaoVinculacao);
				}
			}
		}
		return processoPartePoloPassivoOrgaoVinculacaoList;
	}

	/**
	 * Recupera a lista de partes ativas do processo judicical no(s) polo(s) indicado(s).
	 * 
	 * @param participacaoEnum os polos que se pretendem recuperar 
	 * @return a lista de partes do processo no(s) polo(s) indicado(s).
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaPartePoloObj(ProcessoParteParticipacaoEnum... participacaoEnum) {
		return getListaPartePoloObj(false, participacaoEnum);
	}
	
	/**
	 * Recupera a lista de partes do processo judicial no(s) polo(s) indicado(s). 
	 * Aceita qualquer tipo de Situacao ou somente Situacoes Ativo e Baixado 
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20151 
	 * @param incluiInativos - lista todas as situacoes (incluindo os inativos) ou somente Situacoes: Ativo, Baixado e Suspenso
	 * @param participacaoEnum - os polos que se pretendem recuperar
	 * @return a lista de partes do processo no(s) polo(s) indicado(s).
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaPartePoloObj(Boolean incluiInativos, ProcessoParteParticipacaoEnum... participacaoEnum) {
		return this.getListaPartePoloObj(false, incluiInativos, participacaoEnum);
	}

	/**
	 * Recupera a lista de partes do processo judicial no(s) polo(s) indicado(s)
	 * @param apenasAtivos - se true, ignora as outras situacoes de parte, se false, traz obrigatoriamente suspenso e baixado
	 * @param incluiInativos - se o apenasAtivos for false, esta flag é usada para retornar também as partes com situação inativa
	 * @param participacaoEnum
	 * @return
	 */
	@Transient
	public List<ProcessoParte> getListaPartePoloObj(Boolean apenasSituacaoAtivos, Boolean incluiInativos, ProcessoParteParticipacaoEnum... participacaoEnum) {
		if (participacaoEnum == null) {
			throw new IllegalArgumentException("A participação da parte é requerida");
		}
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(0);
		List<ProcessoParteParticipacaoEnum> tipoParticipacao = Arrays.asList(participacaoEnum);
		for (ProcessoParte processoParte : processoParteList) {
			if(tipoParticipacao.contains(processoParte.getInParticipacao())){
				if(processoParte.getIsAtivo() || (!apenasSituacaoAtivos && (incluiInativos || processoParte.getIsBaixado() || processoParte.getIsSuspenso()))){
					list.add(processoParte);
				}
			}
		}
		return list;
	}
	
	/**
	 * Recupera a lista de partes ativas que compõem o polo ativo do processo.
	 * 
	 * @return a lista de partes do polo ativo
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaParteAtivo() {
		this.listaParteAtivo = getListaPartePoloObj(ProcessoParteParticipacaoEnum.A);
		return listaParteAtivo;
	}

	/**
	 * Recupera a lista de partes ativas que compõem o polo passivo do processo.
	 * 
	 * @return a lista de partes do polo passivo
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaPartePassivo() {
		this.listaPartePassivo = getListaPartePoloObj(ProcessoParteParticipacaoEnum.P);
		return listaPartePassivo;
	}

	/**
	 * Recupera a lista de partes ativas que compõem o polo de terceiros interessados.
	 *  
	 * @return a lista de partes componentes do polo de terceiros interessados.
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaParteTerceiro() {
		this.listaParteTerceiro = getListaPartePoloObj(ProcessoParteParticipacaoEnum.T);
		return listaParteTerceiro;
	}

	/**
	 * Recupera uma representação em String do nome da jurisdição.
	 * 
	 * @return o nome da jurisdição
	 * @see Jurisdicao#getJurisdicao()
	 * @see Transient
	 */
	@Transient
	public String getJurisdicaoStr() {
		if (jurisdicao == null) {
			return null;
		}
		return jurisdicao.getJurisdicao();
	}

	/**
	 * Recupera uma representação em String do nome da classe judicial.
	 * 
	 * @return o nome da classe judicial
	 * @see ClasseJudicial#getClasseJudicial()
	 * @see Transient
	 */
	@Transient
	public String getClasseJudicialStr() {
		return getClasseJudicial().getClasseJudicial();
	}

	/**
	 * Recupera o valor da causa no formato "DDDDD,DD".
	 * 
	 * @return o valor da causa formatado
	 * @see #getVlCausa()
	 * @see Transient
	 */
	@Transient
	@Deprecated
	public String getValorCausaStr() {
		if (valorCausa == null) {
			return "0,00";
		} else {
			return NumberFormat.getCurrencyInstance().format(valorCausa);
		}
	}

	/**
	 * Recupera o órgão julgador a que está cadastrado o processo judicial.
	 * 
	 * @return o {@link OrgaoJulgador} a que está vinculado o processo judicial
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador")
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	/**
	 * Atribui a este processo um órgão julgador.
	 * 
	 * @param orgaoJulgador o órgão julgador a que deve ser vinculado o processo
	 */
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	/**
	 * Recupera a lista de lotes de processos a que está vinculado o processo judicial.
	 * 
	 * @return a lista de lotes de processos.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoLote> getProcessoLoteList() {
		return this.processoLoteList;
	}

	/**
	 * Atribui a este processo judicial uma lista de lotes de processos.
	 *
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param processoLoteList a lista a ser atribuído.
	 */
	public void setProcessoLoteList(List<ProcessoLote> processoLoteList) {
		this.processoLoteList = processoLoteList;
	}

	/**
	 * Recupera a lista de vinculações deste processo com outros processos judiciais.
	 * 
	 * @return a lista de vinculações deste processo com outros processos judiciais.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoTrfConexao> getProcessoTrfConexaoList() {
		return this.processoTrfConexaoList;
	}
	/*
	 * PJE-JT: Ricardo Scholz : PJE-1368 - 2012-06-12 Alteracoes feitas pela JT.
	 */
	/**
	 * Retorna uma lista de objetos <code>String</code> contendo os números dos 
	 * processos relacionados a esta instância, ou uma lista vazia, caso não haja
	 * processos relacionados. 
	 * 
	 * @return a lista de strings com os números de processos vinculados a este processo
	 * @see Transient
	 */
	@Transient
	public List<String> getProcessoTrfConexaoListStr(){
		List<String> resultado = new ArrayList<String>();
		if(this.processoTrfConexaoList != null){
			for(ProcessoTrfConexao conn : this.processoTrfConexaoList){	
				if (conn.getProcessoTrfConexo() != null) {
					resultado.add(conn.getProcessoTrfConexo().getNumeroProcesso());
				}
			}
		}
		return resultado;
	}
	/*
	 * PJE-JT: Fim.
	 */

	/**
	 * Retorna a lista de dados estatísticos da Justiça Federal aos quais este processo está vinculado.
	 * 
	 * @return a lista de dados estatísticos da Justiça Federal a que este processo está vinculado
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<EstatisticaProcessoJusticaFederal> getEstatisticaProcessoJusticaFederalList() {
		return estatisticaProcessoJusticaFederalList;
	}

	/**
	 * Atribui a este processo uma lista de dados estatísticos da Justiça Federal a que ele está vinculado.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param estatisticaProcessoJusticaFederalList a lista a ser atribuída
	 */
	public void setEstatisticaProcessoJusticaFederalList(
			List<EstatisticaProcessoJusticaFederal> estatisticaProcessoJusticaFederalList) {
		this.estatisticaProcessoJusticaFederalList = estatisticaProcessoJusticaFederalList;
	}

	/**
	 * Atribui a este processo judiciai uma lista de vinculações de processos.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param processoTrfConexaoList a lista a ser atribuída
	 */
	public void setProcessoTrfConexaoList(List<ProcessoTrfConexao> processoTrfConexaoList) {
		this.processoTrfConexaoList = processoTrfConexaoList;
	}

	/**
	 * Indica se este processo judicial está vinculado a um processo originário.
	 * 
	 * @return true, se houver um processo judicial de origem vinculado a este processo
	 * @see Processo#getNumeroProcessoOrigem()
	 * @see Transient
	 */
	@Transient
	public Boolean getDependencia() {
		if (this.processo.getNumeroProcessoOrigem() != null) {
			return true;
		}
		return false;
	}

	/**
	 * Recupera o nome da parte desse processo.
	 * 
	 * @return o nome da parte desse processo
	 */
	@Transient
	@Deprecated
	public String getNomeParte() {
		return nomeParte;
	}

	/**
	 * Atribui à propriedade nomeParte um valor.
	 * 
	 * @param nomeParte o nome a ser atribuído
	 * @see #getNomeParte()
	 * 
	 */
	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	/**
	 * Recupera o assunto principal do processo.
	 * 
	 * @return o assunto principal do processo judicial.
	 * @see Transient
	 */
	@Transient
	@Deprecated
	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	/**
	 * Atribui a este processo um assunto principal temporário. Não deve ser utilizado para registro do assunto do processo.
	 * 
	 * @param assuntoTrf o assunto a ser atribuído
	 */
	@Deprecated
	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	/**
	 * Recupera a data em que se considerou que houve o trânsito em julgado da decisão deste processo.
	 * 
	 * @return a data em que se considerou ter havido o trânsito em julgado.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_transitado_julgado")
	public Date getDtTransitadoJulgado() {
		return dtTransitadoJulgado;
	}

	/**
	 * Atribui a este processo judicial uma data de trânsito em julgado.
	 * 
	 * @param dtTransitadoJulgado a data de trânsito em julgado da decisão deste processo. 
	 */
	public void setDtTransitadoJulgado(Date dtTransitadoJulgado) {
		this.dtTransitadoJulgado = dtTransitadoJulgado;
	}

	/**
	 * Recupera a {@link Localizacao} inicial deste processo judicial.
	 * 
	 * @return a localização inicial deste processo
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_localizacao_inicial")
	public Localizacao getLocalizacaoInicial() {
		return localizacaoInicial;
	}
	
	/**
	 * Recupera a data de solicitação de inclusão em pauta.
	 * 
	 * @return a data em que o processo foi incluído.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_solicitacao_inclusao_pauta")
	public Date getDtSolicitacaoInclusaoPauta() {
		return dtSolicitacaoInclusaoPauta;
	}

	/**
	 * Atribui a esse processo judicial uma data de solicitação de inclusão em
	 * pauta.
	 * 
	 * @param dtSolicitacaoInclusaoPauta
	 *            a data da inclusao a ser atribuída
	 */
	public void setDtSolicitacaoInclusaoPauta(Date dtSolicitacaoInclusaoPauta) {
		this.dtSolicitacaoInclusaoPauta = dtSolicitacaoInclusaoPauta;
	}

	/**
	 * Atribui a este processo judicial uma localização inicial.
	 * 
	 * @param localizacaoInicial a {@link Localizacao} a ser atribuída
	 */
	public void setLocalizacaoInicial(Localizacao localizacaoInicial) {
		this.localizacaoInicial = localizacaoInicial;
	}

	/**
	 * Recupera a estrutura de localização da {@link Localizacao} inicial do processo judicial. 
	 * 
	 * @return a estrutura da localização inicial do processo.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_estrutura_inicial")
	public Localizacao getEstruturaInicial() {
		return estruturaInicial;
	}

	/**
	 * Atribui a este processo uma estrutura da localização inicial.
	 * 
	 * @param estruturaInicial a estrutura a ser atribuída.
	 */
	public void setEstruturaInicial(Localizacao estruturaInicial) {
		this.estruturaInicial = estruturaInicial;
	}

	/**
	 * Indica se o pedido de que a tramitação do processo seja feita em segredo foi apreciado.
	 * 
	 * @return {@link ProcessoTrfApreciadoEnum#A}, se o pedido pende de apreciação,
	 * 		{@link ProcessoTrfApreciadoEnum#N}, se o pedido foi apreciado e negado,
	 * 		{@link ProcessoTrfApreciadoEnum#S}, se o pedido foi apreciado e deferido
	 */
	@Column(name = "in_apreciado_segredo")
	@Enumerated(EnumType.STRING)
	public ProcessoTrfApreciadoEnum getApreciadoSegredo() {
		return apreciadoSegredo;
	}

	/**
	 * Atribui a este processo judicial um tipo de apreciação do pedido de tramitação em segredo.
	 * 
	 * @param apreciadoSegredo o tipo de apreciação realizado
	 * @see #getApreciadoSegredo()
	 */
	public void setApreciadoSegredo(ProcessoTrfApreciadoEnum apreciadoSegredo) {
		this.apreciadoSegredo = apreciadoSegredo;
	}

	/**
	 * Recupera a data de distribuição do processo judicial.
	 * 
	 * @return a data em que o processo foi distribuído.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_distribuicao")
	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	/**
	 * Atribui a esse processo judicial uma data de distribuição.
	 * 
	 * @param dataDistribuicao a data da distribuição a ser atribuída
	 */
	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	/**
	 * Recupera o processo judicial originário desse processo.
	 *  
	 * @return o processo originário vinculado
	 * @see Transient
	 */
	@Transient
	public ProcessoTrf getProcessoOriginario() {
		return processoOriginario;
	}

	/**
	 * Atribui a este processo um processo judicial originário.
	 * 
	 * @param processoOriginario o processo judicial a ser atribuído.
	 */
	public void setProcessoOriginario(ProcessoTrf processoOriginario) {
		this.processoOriginario = processoOriginario;
	}

	/**
	 * Indica se o pedido de que a tramitação do processo seja feita sob sigilo foi apreciado.
	 * 
	 * @return {@link ProcessoTrfApreciadoEnum#A}, se o pedido pende de apreciação,
	 * 		{@link ProcessoTrfApreciadoEnum#N}, se o pedido foi apreciado e negado,
	 * 		{@link ProcessoTrfApreciadoEnum#S}, se o pedido foi apreciado e deferido
	 */
	@Column(name = "in_apreciado_sigilo")
	@Enumerated(EnumType.STRING)
	public ProcessoTrfApreciadoEnum getApreciadoSigilo() {
		return apreciadoSigilo;
	}

	/**
	 * Atribui a este processo judicial um tipo de apreciação do pedido de tramitação em sigilo.
	 * 
	 * @param apreciadoSigilo o tipo de apreciação realizado
	 * @see #getApreciadoSigilo()
	 */
	public void setApreciadoSigilo(ProcessoTrfApreciadoEnum apreciadoSigilo) {
		this.apreciadoSigilo = apreciadoSigilo;
	}

	/**
	 * Recupera o número do processo combinado com a sigla do cargo judicial a que ele está vinculado.
	 * Esse método está depreciado por ele se basear em modelo arquitetural em que não existiam
	 * cargos judiciais nos órgãos julgadores.
	 * 
	 * @return o número do processo judicial concatenado com o identificador do cargo a que ele está vinculado.
	 * @see Transient
	 */
	@Transient
	@Deprecated
	public String getNumeroProcesssoTitularidade() {
		if (cargo != null && cargo.getIdCargo() != 0 && getNumeroProcesso() != null ) {
			return getNumeroProcesso().concat(cargo.getSigla());
		} else
			return numeroProcesso;
	}

	/**
	 * Recupera o processo judicial que justificou um relacionamento de dependência com este processo.
	 * 
	 * @return o processo judicial considerado como causador da dependência.
	 * @see Transient
	 */
	@Transient
	public ProcessoTrf getProcessoDependencia() {
		return processoDependencia;
	}

	/**
	 * Atribui a este processo judicial um processo judicial considerado como dependente.
	 * 
	 * @param processoDependencia o processo a ser considerado como dependente
	 */
	public void setProcessoDependencia(ProcessoTrf processoDependencia) {
		this.processoDependencia = processoDependencia;
	}

	/**
	 * Recupera o valor da causa do processo incidental, atribuindo, por padrão, o mesmo valor da causa do processo
	 * judicial de que este processo é incidental.
	 * 
	 * @return o valor da causa do processo incidental.
	 * @see Transient
	 */
	@Transient
	public Double getValorCausaIncidente() {
		if (valorCausaIncidente == null && getProcessoOriginario() != null) {
			valorCausaIncidente = getProcessoOriginario().getValorCausa();
		}
		return valorCausaIncidente;
	}

	/**
	 * Atribui a este processo um valor da causa incidental.
	 * 
	 * @param valorCausaIncidente o valor a ser atribuído.
	 */
	public void setValorCausaIncidente(Double valorCausaIncidente) {
		this.valorCausaIncidente = valorCausaIncidente;
	}

	/**
	 * Indica se este processo judicial é considerado um processo incidental.
	 * 
	 * @return true, se o processo for incidetal, ou false, caso contrário.
	 */
	@Column(name = "in_incidente")
	public Boolean getIsIncidente() {
		return isIncidente;
	}

	/**
	 * Atribui a este processo judicial a característica de ser ou não incidental.
	 * 
	 * @param isIncidente a indicação de que o processo é (true) ou não (false) incidental
	 */
	public void setIsIncidente(Boolean isIncidente) {
		this.isIncidente = isIncidente;
	}

	/**
	 * Indica se este processo judicial, quando em instância que comporta julgamento colegiado,
	 * foi selecionado para inclusão em pauta de sessão de julgamento colegiada.
	 * 
	 * @return true, se o processo foi selecionado para julgamento, ou false, caso contrário.
	 */
	@Column(name = "in_selecionado_pauta", nullable = false)
	@NotNull
	public Boolean getSelecionadoPauta() {
		return selecionadoPauta;
	}

	/**
	 * Atribui a este processo marca indicativa de que o processo foi selecionado para inclusão em pauta de julgamento colegiado.
	 * 
	 * @param selecionadoPauta indicação de que o processo foi (true) ou não (false) selecionado para inclusão em pauta de julgamento colegiado
	 */
	public void setSelecionadoPauta(Boolean selecionadoPauta) {
		this.selecionadoPauta = selecionadoPauta;
	}

	/**
	 * Indica se este processo judicial, quando em instância que comporta julgamento colegiado,
	 * foi objeto de revisão.
	 *  
	 * @return true, se o processo foi revisado.
	 */
	@Column(name = "in_revisado", nullable = false)
	@NotNull
	public Boolean getRevisado() {
		return revisado;
	}

	/**
	 * Atribui a este processo a indicação de que ele foi ou não revisado.
	 * 
	 * @param revisado indicação de que o processo foi (true) ou não (false) revisado.
	 */
	public void setRevisado(Boolean revisado) {
		this.revisado = revisado;
	}

	/**
	 * Recupera o órgão julgador revisor do processo, em uma instância colegiada.
	 * 
	 * @return o órgão julgador revisor, ou null, se inexistente
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_revisor")
	public OrgaoJulgador getOrgaoJulgadorRevisor() {
		return orgaoJulgadorRevisor;
	}

	/**
	 * @deprecated Utilizar estrutura de vinculação de magistrado {@link ProcessoMagistrado}
	 * 
	 * Recupera a data em que o revisor se vinculou ao processo por ter
	 * realizado algum despacho
	 * 
	 * @return a data em que ocorreu o despacho que vinculou o revisor ao
	 *         processo.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_vinculacao_revisor")
	@Deprecated
	public Date getDataVinculacaoRevisor() {
		return dataVinculacaoRevisor;
	}

	/**
	 * @deprecated Utilizar estrutura de vinculação de magistrado {@link ProcessoMagistrado}
	 * 
	 * Atribui a data de vinculação do revisor ao processo. Em determinadas
	 * situaçãoes o revisor se vincula (vinculação regimental) ao processo por
	 * ter realizado determinado despacho/ato. 
	 * 
	 * @param dataVinculacaoRevisor
	 */
	@Deprecated
	public void setDataVinculacaoRevisor(Date dataVinculacaoRevisor) {
		this.dataVinculacaoRevisor= dataVinculacaoRevisor;
	}


	/**
	 * Atribui a este processo judicial, em uma instância colegiada, um órgão julgador revisor.
	 * 
	 * @param orgaoJulgadorRevisor o órgão julgador a ser atribuído
	 */
	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}

	/**
	 * Recupera o relator do processo judicial, em uma instância colegiada.
	 * 
	 * @return o magistrado relator do processo, ou null, se inexistente
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_relator_processo")
	public PessoaMagistrado getPessoaRelator() {
		return pessoaRelator;
	}

	/**
	 * Atribui a este processo judicial, em uma instância colegiada, um relator.
	 * 
	 * @param pessoaRelator o magistrado relator a ser atribuído
	 */
	public void setPessoaRelator(PessoaMagistrado pessoaRelator) {
		this.pessoaRelator = pessoaRelator;
	}

	/**
	 * Indica se o processo judicial foi selecionado para julgamento colegiado. 
	 * 
	 * @return true, se já foi selecionado para julgamento colegiado
	 */
	@Column(name = "in_selecionado_julgamento")
	public boolean getSelecionadoJulgamento() {
		return selecionadoJulgamento;
	}

	/**
	 * Atribui a este processo judicial indicação relativa ao fato de ele ter ou não sido selecionado para julgamento
	 * colegiado.
	 * 
	 * @param selecionadoJulgamento a indicação quanto ao processo ter (true) ou não (false) sido selecionado
	 * para julgamento
	 */
	public void setSelecionadoJulgamento(boolean selecionadoJulgamento) {
		this.selecionadoJulgamento = selecionadoJulgamento;
	}

	/**
	 * Recupera a pessoa que marcou o processo como revisado.
	 * 
	 * @return a pessoa responsável pela marcação
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcou_revisado")
	public Pessoa getPessoaMarcouRevisado() {
		return pessoaMarcouRevisado;
	}

	/**
	 * Atribui a este processo judicial a pessoa responsável por ter marcado o processo como revisado.
	 * 
	 * @param pessoaMarcouRevisado a pessoa a ser atribuída.
	 */
	public void setPessoaMarcouRevisado(Pessoa pessoaMarcouRevisado) {
		this.pessoaMarcouRevisado = pessoaMarcouRevisado;
	}
	
	/**
	 * Sobrecarga do método {@link #setPessoaMarcouRevisado(Pessoa)} em razão de PJEII-2726.
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

	/**
	 * Recupera a pessoa que marcou o processo como selecionado para pauta.
	 * 
	 * @return a pessoa responsável pela marcação
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcou_pauta")
	public Pessoa getPessoaMarcouPauta() {
		return pessoaMarcouPauta;
	}

	/**
	 * Atribui a este processo judicial a pessoa responsável por ter marcado o processo como selecionado
	 * para pauta.
	 *  
	 * @param pessoaMarcouPauta a pessoa a ser atribuída
	 */
	public void setPessoaMarcouPauta(Pessoa pessoaMarcouPauta) {
		this.pessoaMarcouPauta = pessoaMarcouPauta;
	}

	/**
	 * Sobrecarga do método {@link #setPessoaMarcouPauta(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa pessoa especializada a ser atribuída.
	 * 
	 */
	public void setPessoaMarcouPauta(PessoaFisicaEspecializada pessoa){
		if(pessoa != null) {
			setPessoaMarcouPauta(pessoa.getPessoa());
		} else {
			setPessoaMarcouPauta((Pessoa)null);
		}
	}
	/**
	 * Recupera a pessoa que marcou o processo como selecionado para julgamento.
	 * 
	 * @return a pessoa responsável pela marcação
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pessoa_marcou_julgamento")
	public Pessoa getPessoaMarcouJulgamento() {
		return pessoaMarcouJulgamento;
	}

	/**
	 * Atribui a este processo a pessoa responsável por ter marcado o processo como selecionado para
	 * julgamento.
	 * 
	 * @param pessoaMarcouJulgamento a pessoa a ser atribuída
	 */
	public void setPessoaMarcouJulgamento(Pessoa pessoaMarcouJulgamento) {
		this.pessoaMarcouJulgamento = pessoaMarcouJulgamento;
	}
	
	/**
	 * Sobrecarga do método {@link #setPessoaMarcouJulgamento(Pessoa)} em razão de PJEII-2726.
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

	/**
	 * Indica se o processo é oriundo de outra instância.
	 * 
	 * @return true, se o processo é oriundo de outra instância.
	 */
	@Column(name = "in_outra_instancia")
	public Boolean getInOutraInstancia() {
		return inOutraInstancia;
	}

	/**
	 * Atribui a este processo marca indicativa quanto ao fato de o processo ser oriundo de outra instância.
	 * 
	 * @param inOutraInstancia a indicação de que o processo é (true) ou não (false) de outra instância.
	 */
	public void setInOutraInstancia(Boolean inOutraInstancia) {
		this.inOutraInstancia = inOutraInstancia;
	}
	
	/**
	 * Indica se o processo encontra-se bloqueado para peticionar.
	 * 
	 * @return true, se o processo encontra-se bloqueado para peticionar.
	 */
	@Column(name = "in_bloqueia_peticao")
	public boolean getInBloqueiaPeticao() {
		return inBloqueiaPeticao;
	}

	/**
	 * Atribui a este processo marca indicativa quanto ao fato dele estar bloqueado para peticionar.
	 * 
	 * @param inBloqueiaPeticao a indicação de que o processo é (true) ou não (false) bloqueado para peticionar.
	 */
	public void setInBloqueiaPeticao(boolean inBloqueiaPeticao) {
		this.inBloqueiaPeticao = inBloqueiaPeticao;
	}

	/**
	 * Indica se o processo teve pedido de deferimento de assistência judiciária gratuita apreciada.
	 *  
	 * @return true, se o pedido foi apreciado.
	 * @see #getJusticaGratuita()
	 */
	@Column(name = "in_apreciado_justica_gratuita")
	public Boolean getApreciadoJusticaGratuita() {
		return apreciadoJusticaGratuita;
	}

	/**
	 * Atribui a este processo marca relativa ao fato de ter sido apreciado pedido de deferimento
	 * de assistência judiciária gratuita.
	 * 
	 * @param apreciadoJusticaGratuita indicação quanto ao fato de o pedido ter (true) ou não (false) sido apreciado.
	 */
	public void setApreciadoJusticaGratuita(Boolean apreciadoJusticaGratuita) {
		this.apreciadoJusticaGratuita = apreciadoJusticaGratuita;
	}

	/**
	 * Recupera o tipo de cargo ao qual está vinculado este processo.
	 * 
	 * @return o tipo de cargo ao qual está vinculado o processo
	 * @see #getOrgaoJulgadorCargo()
	 * @see Cargo
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo")
	public Cargo getCargo() {
		return cargo;
	}

	/**
	 * Atribui a este processo um tipo de cargo.
	 * 
	 * @param cargo o cargo a ser vinculado.
	 */
	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	/**
	 * Recupera o WSDL de acesso ao conteúdo desse processo em outra instância de sistema.
	 * 
	 * @return o WSDL de acesso.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_endereco_wsdl")
	public EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}
	
	@Deprecated
	@Transient
	private TipoParte getTipoParteAdvogado(){
		return tipoParteAdvogado;
	}
	
	/**
	 * Recupera a lista de partes principais do processo judicial (sem advogados) que figuram em seu polo ativo.
	 *  
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoPartePoloAtivoSemAdvogadoList() {
		return getProcessoPartePoloAtivoSemAdvogadoList(getTipoParteAdvogado());
	}

	/**
	 * Recupera a lista de partes do polo ativo do processo judicial, excluída as partes do tipo indicado.
	 * 
	 * @param tipoParteAdvogado o tipo de parte a ser excluído.
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoPartePoloAtivoSemAdvogadoList(TipoParte tipoParteAdvogado) {
		List<ProcessoParte> processoPartePoloAtivoSemAdvogadoList = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : getListaParteAtivo()) {
			boolean isPartePrincipal = (tipoParteAdvogado != null ? (!processoParte.getTipoParte().equals(tipoParteAdvogado)) : processoParte.getPartePrincipal());
			if (isPartePrincipal) {
				processoPartePoloAtivoSemAdvogadoList.add(processoParte);
			}
		}
		return processoPartePoloAtivoSemAdvogadoList;
	}

	/**
	 * Recupera a lista de partes do polo passivo do processo judicial, excluídos os advogados.
	 * 
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoPartePoloPassivoSemAdvogadoList() {
		return getProcessoPartePoloPassivoSemAdvogadoList(getTipoParteAdvogado());
	}

	/**
	 * Recupera a lista de partes autoras e rés do processo, excluindo seus advogados.
	 *  
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteAutorReuList() {
		List<ProcessoParte> processoPartePoloAtivoSemAdvogadoListTeste = new ArrayList<ProcessoParte>(0);

		for (ProcessoParte processoParte : getListaParteAtivo()) {
			if (!processoParte.getTipoParte().equals(getTipoParteAdvogado())) {
				processoPartePoloAtivoSemAdvogadoListTeste.add(processoParte);
			}
		}
		for (ProcessoParte processoParte : getListaPartePassivo()) {
			if (!processoParte.getTipoParte().equals(getTipoParteAdvogado())) {
				processoPartePoloAtivoSemAdvogadoListTeste.add(processoParte);
			}
		}
		return processoPartePoloAtivoSemAdvogadoListTeste;
	}

	/**
	 * Recupera a lista de partes do polo passivo do processo judicial, excluída as partes do tipo indicado.
	 * 
	 * @param tipoParteAdvogado o tipo de parte a ser excluído.
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoPartePoloPassivoSemAdvogadoList(TipoParte tipoParteAdvogado) {
		List<ProcessoParte> processoPartePoloPassivoSemAdvogadoList = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : getListaPartePassivo()) {
			boolean isPartePrincipal = (tipoParteAdvogado != null ? (!processoParte.getTipoParte().equals(tipoParteAdvogado)) : processoParte.getPartePrincipal()); 
			if (isPartePrincipal) {
				processoPartePoloPassivoSemAdvogadoList.add(processoParte);
			}
		}
		return processoPartePoloPassivoSemAdvogadoList;
	}
	
	/**
	 * Recupera as partes do tipo inventariante
	 * 
	 * @param tipoParte
	 * @param numeroDocumento 
	 * @return Inventariante do processo
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteByTipoParteList(String tipoParte,String numeroDocumento) {
		return processoParteList.stream()
				.filter(x -> x.getTipoParte().getTipoParte().equals(tipoParte) && 
						checkNumeroDocumento(x.getPessoa(), numeroDocumento))
				.collect(Collectors.toList());
	}
	
	private Boolean checkNumeroDocumento(Pessoa pessoa, String numeroDocumento) {
		return pessoa.getPessoaDocumentoIdentificacaoList().stream()
				.anyMatch(x -> x.getNumeroDocumento().equals(numeroDocumento));
	}
	

	/**
	 * Recupera todas as partes do processo, excluídos os advogados.
	 * 
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoParteSemAdvogadoList() {
		List<ProcessoParte> processoParteSemAdvogadoList = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getIsAtivo() || processoParte.getIsBaixado()) {
				boolean isPartePrincipal = (getTipoParteAdvogado() != null ? (!processoParte.getTipoParte().equals(getTipoParteAdvogado())) : processoParte.getPartePrincipal());
				if (isPartePrincipal) {
					processoParteSemAdvogadoList.add(processoParte);
				}
			}
		}
		return processoParteSemAdvogadoList;
	}
	
	
	/**
	 * Recupera outros participantes do processo, excluídos os advogados.
	 * 
	 * @return a lista de partes
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getProcessoOutrosParticipantesSemAdvogadoList() {
		List<ProcessoParte> processoOutrosParticipantesSemAdvogadoList = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getInParticipacao() == ProcessoParteParticipacaoEnum.T) {
				if (processoParte.getIsAtivo() || processoParte.getIsBaixado()) {
					if (!processoParte.getTipoParte().equals(getTipoParteAdvogado())) {
						processoOutrosParticipantesSemAdvogadoList.add(processoParte);
					}
				}
			}
		}
		return processoOutrosParticipantesSemAdvogadoList;
	}

	
	/**
	 * Atribui um endereço WSDL de acesso ao conteúdo desse processo em outra instância de sistema.
	 * 
	 * @param enderecoWsdl o endereço WSDL a ser atribuído
	 */
	public void setEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
	}

	/**
	 * Recupera um tipo de pessoa associada ao processo.
	 * 
	 * @return o tipo de pessoa.
	 * @see Transient
	 */
	@Deprecated
	@Transient
	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	/**
	 * Atribui um tipo de pessoa a esse processo.
	 * 
	 * @param tipoPessoa o tipo a ser associado.
	 */
	@Deprecated
	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	/**
	 * Recupera o órgão julgador colegiado a que está vinculado este processo judicial.
	 * 
	 * @return o órgão julgador colegiado associado, ou null se não houver.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_colegiado")
	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	/**
	 * Atribui um órgão julgador colegiado a este processo judicial.
	 * 
	 * @param orgaoJulgadorColegiado o órgão a ser atribuído.
	 */
	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	/**
	 * Recupera o cargo do órgão julgador singular a que está vinculado este processo judicial.
	 * 
	 * @return o cargo do órgão julgador singular a que está vinculado este processo
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador_cargo")
	public OrgaoJulgadorCargo getOrgaoJulgadorCargo() {
		return orgaoJulgadorCargo;
	}

	/**
	 * Atribui a processo judicial a um cargo do órgão julgador por ele responsável.
	 *  
	 * @param orgaoJulgadorCargo o cargo a ser atribuído.
	 */
	public void setOrgaoJulgadorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		this.orgaoJulgadorCargo = orgaoJulgadorCargo;
	}

	/**
	 * Recupera o processo judicial de referência deste processo judicial. 
	 * O processo de referência é um processo que não se caracteriza como processo causador 
	 * da dependência nem como processo originário.
	 * 
	 * @return o processo de referência
	 * @see #getProcessoDependencia()
	 * @see #getProcessoOriginario()
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_proc_referencia")
	public ProcessoTrf getProcessoReferencia() {
		return processoReferencia;
	}

	/**
	 * Atribui a este processo judicial um processo de referência.
	 * O processo de referência é um processo que não se caracteriza como processo causador 
	 * da dependência nem como processo originário.
	 * 
	 * @param processoReferencia o processo a ser atribuído
	 * @see #setProcessoDependencia()
	 * @see #setProcessoOriginario()
	 */
	public void setProcessoReferencia(ProcessoTrf processoReferencia) {
		this.processoReferencia = processoReferencia;
	}

	/**
	 * Recupera uma descricao textual do processo de referencia deste processo judicial.
	 * O processo de referencia e um processo que nao se caracteriza como processo causador 
	 * da dependencia nem como processo originario.
	 * 
	 * [PJEII-6318] Antonio Lucas
	 * Esse campo esta sendo usado no cadastro de processos incidentais, 
	 * quando o processo referencia nao e um processo do PJe. 
	 * Nesses casos, esse campo fica com o numero do processo referencia
	 * 
	 * @return a descricao do processo de referencia
	 * @see #getProcessoReferencia()
	 * @see #getProcessoDependencia()
	 * @see #getProcessoOriginario()
	 */
	@Column(name = "ds_proc_referencia", length = 50)
	@Length(max = 50)
	public String getDesProcReferencia() {
		return desProcReferencia;
	}

	/**
	 * Atribui a este processo judicial uma descrição de seu processo de referência.
	 * O processo de referência é um processo que não se caracteriza como processo causador 
	 * da dependência nem como processo originário.
	 * 
	 * @param desProcReferencia a descrição a ser atribuída
	 * @see #setProcessoReferencia()
	 * @see #setProcessoDependencia()
	 * @see #setProcessoOriginario()
	 */
	public void setDesProcReferencia(String desProcReferencia) {
		this.desProcReferencia = desProcReferencia;
	}
	
	/**
	 * Recupera uma descrição resumida e específica do objeto discutido no processo.
	 * Essa descrição é informal, ou seja, é incluída pelo servidor do órgão responsável pelo
	 * processo quando necessário.
	 * 
	 * @return o objeto do processo
	 * 
	 * @since 1.6.5.13
	 */
	
	@Column(name = "ds_objeto")
	public String getObjeto() {
		return objeto;
	}

	/**
	 * Atribui a este processo uma descrição resumida e específica de seu objeto.
	 * Essa descrição é informal, ou seja, é incluída pelo servidor do órgão responsável pelo
	 * processo quando necessário.
	 * 
	 * @param objeto o objeto a ser definido
	 */
	public void setObjeto(String objeto) {
		this.objeto = objeto;
	}

	/**
	 * Indica se este processo judicial foi selecionado para alguma atividade negocial em uma lista.
	 * 
	 * @return true, se o processo foi selecionado.
	 * @see Transient
	 */
	@Transient
	public Boolean getCheck() {
		return check;
	}

	/**
	 * Atribui a este processo marca relativa ao fato de ele ter sido selecionado para a realização 
	 * de uma atividade negocial em uma lista.
	 * 
	 * @param check indicação de que o processo foi (true) ou não (false) selecionado.
	 */
	public void setCheck(Boolean check) {
		this.check = check;
	}

	/**
	 * Recupera a instância a que pertence este processo judicial.
	 * 
	 * @return caracter indicativo da instância (1, 2, 3, 4)
	 */
	@Column(name = "nr_instancia")
	public Character getInstancia() {
		return instancia;
	}

	/**
	 * Atribui a este processo uma instância de tramitação.
	 * 
	 * @param instancia caracter (1,2,3,4) indicativo da instância de tramitação
	 */
	public void setInstancia(Character instancia) {
		this.instancia = instancia;
	}

	/**
	 * Recupera informação sobre o fato de a classe deste processo judicial ser ou não incidental.
	 * 
	 * @return true, se a classe está definida e marcada como incidental, ou false, se a classe não está definida ou 
	 * não é incidental.
	 * @see #getIsIncidente()
	 * @see Transient
	 */
	@Transient
	public boolean isIncidental() {
		if (classeJudicial != null && classeJudicial.getIncidental() != null) {
			return classeJudicial.getIncidental();
		} else {
			return false;
		}
	}

	/**
	 * Recupera a data escolhida como sugerida para a da sessão do julgamento deste processo, quando em instância colegiada.
	 * 
	 * @return a data escolhida
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sugestao_sessao")
	public Date getDataSugestaoSessao() {
		return dataSugestaoSessao;
	}

	/**
	 * Atribui a este processo uma data como a escolhida como sugerida para da sessão do julgamento 
	 * deste processo, quando em instância colegiada.
	 * 
	 * @param dataSugestaoSessao a data a ser atribuída
	 */
	public void setDataSugestaoSessao(Date dataSugestaoSessao) {
		this.dataSugestaoSessao = dataSugestaoSessao;
	}

	/**
	 * Indica se o processo foi marcado ou não como pronto para revisão.
	 * 
	 * @return true, se o processo está pronto para a revisão.
	 */
	@Column(name = "in_pronto_revisao")
	public Boolean getProntoRevisao() {
		return prontoRevisao;
	}

	/**
	 * Atribui ao processo marca indicativa do fato de ele estar ou não pronto para revisão.
	 *  
	 * @param prontoRevisao indicação de que o processo está (true) ou não (false) pronto para revisão.
	 * 
	 */
	public void setProntoRevisao(Boolean prontoRevisao) {
		this.prontoRevisao = prontoRevisao;
	}

	/**
	 * Recupera o município em que o fato principal discutido no processo aconteceu. 
	 * Deve ser utilizado, essencialmente, em processos criminais e quanto ao fato criminoso
	 * que tem maior pena cominada.
	 * 
	 * @return o município do fato principal.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio_fato_principal", nullable = true)
	public Municipio getMunicipioFatoPrincipal() {
		return municipioFatoPrincipal;
	}

	/**
	 * Atribui ao processo um município no qual teria sido realizado o fato principal discutido.
	 * Deve ser utilizado, essencialmente, em processos criminais e quanto ao fato criminoso
	 * que tem maior pena cominada.
	 * 
	 * @param municipioFatoPrincipal o município do fato principal
	 */
	public void setMunicipioFatoPrincipal(Municipio municipioFatoPrincipal) {
		this.municipioFatoPrincipal = municipioFatoPrincipal;
	}

	/**
	 * Recupera a lista de procedimentos extrajudiciais que deram origem ao processo judicial.
	 * 
	 * @return a lista de procedimentos extrajudiciais
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<ProcessoProcedimentoOrigem> getProcedimentoOrigemList() {
		return procedimentoOrigemList;
	}

	/**
	 * Atribui a este processo uma lista de procedimentos extrajudiciais como sendo aqueles que
	 * lhe deram origem.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.
	 *   
	 * @param procedimentoOrigemList a lista de procedimentos
	 */
	public void setProcedimentoOrigemList(List<ProcessoProcedimentoOrigem> procedimentoOrigemList) {
		this.procedimentoOrigemList = procedimentoOrigemList;
	}

	/**
	 * Recupera o peso processual, ou seja, a carga de trabalho presumida calculada a partir 
	 * do peso da classe, do maior peso dos assuntos e do peso das partes no processo.
	 * 
	 * @return o peso processual
	 * @see #getValorPesoDistribuicao()
	 * @see ClasseJudicial#getValorPeso()
	 * @see AssuntoTrf#getValorPeso()
	 * @see ProcessoPesoParte#getValorPeso()
	 */
	@Column(name = "vl_peso_processual")
	public Double getValorPesoProcessual() {
		return this.valorPesoProcessual;
	}

	/**
	 * Atribui a este processo um peso processual, ou seja, um valor que representa 
	 * a carga de trabalho presumida calculada a partir do peso da classe, do maior 
	 * peso dos assuntos e do peso das partes no processo.
	 * 
	 * @param valorPesoProcessual o peso a ser atribuído
	 * @see #getValorPesoDistribuicao()
	 * @see ClasseJudicial#getValorPeso()
	 * @see AssuntoTrf#getValorPeso()
	 * @see ProcessoPesoParte#getValorPeso()
	 */
	public void setValorPesoProcessual(Double valorPesoProcessual) {
		this.valorPesoProcessual = valorPesoProcessual;
	}

	/**
	 * Recupera o peso de distribuição do processo, ou seja, a carga de trabalho presumida 
	 * no órgão julgador a que o processo pertence, calculada a partir do peso processual
	 * ponderado com o peso de prevenção e com o peso de distribuição do órgão julgador.
	 * 
	 * @return o peso de distribuição
	 * @see #getValorPesoProcessual()
	 * @see PesoPrevencao#getValorPeso()
	 * @see OrgaoJulgadorCargo#getValorPeso()
	 */
	@Column(name = "vl_peso_distribuicao")
	public Double getValorPesoDistribuicao() {
		return this.valorPesoDistribuicao;
	}

	/**
	 * Atribui a este processo um peso de distribuição, que deve representar a carga de 
	 * trabalho presumida no órgão julgador a que o processo pertence, calculada a 
	 * partir do peso processual ponderado com o peso de prevenção e com o peso de 
	 * distribuição do órgão julgador.
	 * 
	 * @param valorPesoDistribuicao o peso a ser atribuído
	 * @see #getValorPesoProcessual()
	 * @see PesoPrevencao#getValorPeso()
	 * @see OrgaoJulgadorCargo#getValorPeso()
	 */
	public void setValorPesoDistribuicao(Double valorPesoDistribuicao) {
		this.valorPesoDistribuicao = valorPesoDistribuicao;
	}

	/**
	 * Recupera a lista de requisições de pagamento realizadas com base neste processo judicial.
	 * 
	 * @return a lista de requisições de pagamento.
	 */
	@OneToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	public List<Rpv> getRpvList() {
		return rpvList;
	}
	
	@OneToMany(mappedBy="processo")
	@OrderBy("id DESC")
	public List<SituacaoProcessual> getSituacoes(){
		return situacoes;
	}
	
	public void setSituacoes(List<SituacaoProcessual> situacoes) {
		this.situacoes = situacoes;
	}
	
	/**
	 * Atribui a este processo uma lista de requisições de pagamento.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.
	 * 
	 * @param rpvList a lista a ser atribuída
	 */
	public void setRpvList(List<Rpv> rpvList) {
		this.rpvList = rpvList;
	}

	/**
	 * Recupera uma String resultado da concatenação do número do processo com o código
	 * do cargo a que ele está vinculada.
	 * Esse método não deve ser utilizado, especialmente por depender do parâmetro de
	 * contexto inExibicaoTitularidadeProcesso
	 * 
	 * @return a representação da String com a sigla do cargo
	 * @see #getNumeroProcesssoTitularidade()
	 */
	@Transient
	@Deprecated
	public String getNumeroProcessoCargo() {
		return getNumeroProcesso();
	}

	/**
	 * Retorna dada uma lista de polos solicitados, a lista de partes principais pode considerar apenas situações ativas, se não for apenas ativas trará sempre baixados e suspensos
	 * É possível também indicar se incluirá as partes inativas
	 * 
	 * @param apenasSituacaoAtivos
	 * @param incluiInativos
	 * @param inParticipacao o(s) tipo(s) de participação(ões) cujas partes principais serão recuperadas
	 * @return a lista de partes, ordenadas por seu identificador
	 */
	public List<ProcessoParte> getListaPartePrincipal(boolean apenasSituacaoAtivos, boolean incluiInativos, ProcessoParteParticipacaoEnum... inParticipacao) {
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : getListaPartePoloObj(apenasSituacaoAtivos, incluiInativos,inParticipacao)) {
			if (processoParte.getPartePrincipal()) {
				list.add(processoParte);
			}
		}
		return ordenarListaPorId(list);
	}

	/**
	 * Retorna dada uma lista de polos solicitados, a lista de partes principais considerando sempre as situações: ativo / baixado / suspenso - permite indicar se incluirá as partes inativas
	 * @param incluiInativos
	 * @param inParticipacao
	 * @return
	 */
	public List<ProcessoParte> getListaPartePrincipal(boolean incluiInativos, ProcessoParteParticipacaoEnum... inParticipacao) {
		return this.getListaPartePrincipal(false, incluiInativos, inParticipacao);
	}
	
	/**
	 * Retorna dada uma lista de polos solicitados, a lista de partes principais, considerando as situacoes ativo / baixado / suspenso, ignorando partes inativas
	 * @param inParticipacao
	 * @return
	 */
	public List<ProcessoParte> getListaPartePrincipal(ProcessoParteParticipacaoEnum... inParticipacao) {
		return getListaPartePrincipal(false, false, inParticipacao);
	}

	/**
	 * Recupera a lista de partes principais de um processo judicial que compõem seu polo ativo.
	 * 
	 * @return a lista de partes principais do polo ativo
	 * @see #getListaPartePrincipal(ProcessoParteParticipacaoEnum...)
	 */
	@Transient
	public List<ProcessoParte> getListaPartePrincipalAtivo() {
		return getListaPartePrincipal(ProcessoParteParticipacaoEnum.A);
	}

	/**
	 * Recupera a lista de partes principais de um processo judicial que compõem seu polo passivo.
	 * 
	 * @return a lista de partes principais do polo passivo
	 * @see #getListaPartePrincipal(ProcessoParteParticipacaoEnum...)
	 */
	@Transient
	public List<ProcessoParte> getListaPartePrincipalPassivo() {
		return getListaPartePrincipal(ProcessoParteParticipacaoEnum.P);
	}
	
	/**
	 * Recupera a lista de partes principais de um processo judicial que figuram como terceiros
	 * interessados desvinculados dos polos ativo ou passivo.
	 * 
	 * @return a lista de terceiros interessados
	 * @see #getListaPartePrincipal(ProcessoParteParticipacaoEnum...)
	 */
	@Transient
	public List<ProcessoParte> getListaPartePrincipalTerceiro() {
		return getListaPartePrincipal(ProcessoParteParticipacaoEnum.T);
	}

	/**
	 * Atribui ao processo uma lista de partes que seriam fiscais da lei.
	 * 
	 * @param listaFiscal a lista a ser atribuída
	 */
	@Transient
	public void setListaFiscal(List<ProcessoParte> listaFiscal) {
		this.listaFiscal = listaFiscal;
	}

	/**
	 * Recupera a lista de partes que figuram como terceiros desvinculados dos polos ativo
	 * ou passivo.
	 * Rigorosamente, pretendia-se recuperar o Ministério Público, mas a implementação retorna
	 * o indicado acima.
	 * 
	 * @return a lista de terceiros interessados desvinculados
	 * @see #getListaPartePoloObj(ProcessoParteParticipacaoEnum...)
	 */
	@Transient
	public List<ProcessoParte> getListaFiscal() {
		this.listaFiscal = getListaPartePoloObj(ProcessoParteParticipacaoEnum.T);
		return listaFiscal;
	}

	/**
	 * Recupera a quantidade de partes principais que compõem o processo no polo indicado.
	 * 
	 * @param inPolo o polo processual a respeito do qual se pretende identificar o número de partes
	 * @return a quantidade de partes
	 */
	private Integer buscarQuantidadePartes(ProcessoParteParticipacaoEnum inPolo) {
		Integer quantPartes = 0;
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getIsAtivo() || processoParte.getIsBaixado()) {
				if (processoParte.getPartePrincipal() && processoParte.getInParticipacao().equals(inPolo)) {
					quantPartes++;
				}
			}
		}
		return quantPartes;
	}

	/**
	 * Calcula o peso das partes no processo judicial a partir dos parâmetros dados para um determinado polo,
	 * conforme os requisitos R-006 e R-007 do documento de especificação da distribuição no PJe.
	 * 
	 * @param processoPesoParteList a lista de intervalos de domínio do peso das partes (R-006)
	 * @param quantidadePartes a quantidade de partes
	 * @param inPolo o polo processual de interesse
	 * @return o peso das partes neste processo
	 * @throws Exception caso não tenham sido definidos os intervalos de domínio do peso das partes 
	 */
	private Double buscarPesoPartes(List<ProcessoPesoParte> processoPesoParteList, Integer quantidadePartes,
			ProcessoParteParticipacaoEnum inPolo) throws Exception {

		Double valorPesoPolo = 0.0;
		Double valorPesoGeral = 0.0;

		for (ProcessoPesoParte processoPesoParte : processoPesoParteList) {
			if (quantidadePartes >= processoPesoParte.getNumeroPartesInicial()
					&& (processoPesoParte.getNumeroPartesFinal() == null || quantidadePartes <= processoPesoParte
							.getNumeroPartesFinal())) {

				if (processoPesoParte.getInPolo().equals(inPolo)) {
					valorPesoPolo = processoPesoParte.getValorPeso();
				} else if (processoPesoParte.getInPolo().equals(ProcessoParteParticipacaoEnum.T)) {
					valorPesoGeral = processoPesoParte.getValorPeso();
				}
			}
		}

		if (valorPesoPolo + valorPesoGeral == 0.0) {
			String polo = (inPolo.equals(ProcessoParteParticipacaoEnum.A) ? "Ativo" : "Passivo");
			throw new Exception(String.format(
					"Não há intervalo de pesos cadastrado para a quantidade de partes %d no Polo %s.",
					quantidadePartes, polo));
		}

		return (valorPesoPolo > valorPesoGeral ? valorPesoPolo : valorPesoGeral);
	}

	/**
	 * Calcula o peso da classe processual neste processo.
	 * 
	 * @return o peso da classe deste processo
	 * @throws Exception
	 *             caso o processo não tenha classe vinculada ou se a classe não
	 *             tiver peso definido. Para classes incidentais é permitido o
	 *             peso zero.
	 */
	private Double calcularPesoClasse() throws Exception {
		
		if (this.getClasseJudicial() == null) {
			throw new Exception("Não há classe judicial vinculada ao processo.");
		}
		
		if (this.getClasseJudicial().getValorPeso() == null
				|| ((!this.isIncidental()) && this.getClasseJudicial().getValorPeso() == 0.0)) {
			
			throw new Exception(String.format("Não há peso configurado para a classe judicial %s", 
					this.getClasseJudicial().getClasseJudicial()));
		}
		
		return this.getClasseJudicial().getValorPeso();
	}

	/**
	 * Calcula o peso de interesse para os assuntos do processo, conforme R-005c do documento de 
	 * especificação da distribuição do processo no PJe.
	 * 
	 * @return o maior peso entre os pesos dos assuntos do processo
	 * @throws Exception caso o processo não tenha assuntos vinculados ou se algum dos assuntos
	 * não tenha peso definido.
	 */
	private Double calcularPesoAssunto() throws Exception {
		List<ProcessoAssunto> processoAssuntoList = getProcessoAssuntoList();
		if (processoAssuntoList == null || processoAssuntoList.size() == 0) {
			throw new Exception("Não há assuntos judiciais cadastrados no processo.");
		}
		Double maiorPesoAssunto = 0.0;
		for (ProcessoAssunto processoAssunto : processoAssuntoList) {
			if (processoAssunto.getAssuntoTrf().getValorPeso() == null || processoAssunto.getAssuntoTrf().getValorPeso() == 0.0) {
				throw new Exception(String.format("Não há peso configurado para o assunto judicial %s",
						processoAssunto.getAssuntoTrf()));
			}

			if (processoAssunto.getAssuntoTrf().getValorPeso() > maiorPesoAssunto) {
				maiorPesoAssunto = processoAssunto.getAssuntoTrf().getValorPeso();
			}
		}
		return maiorPesoAssunto;
	}

	/**
	 * Calcula o peso das partes no processo judicial a partir dos intervalos de domínio dados,
	 * conforme os requisitos R-006 e R-007 do documento de especificação da distribuição no PJe.
	 * 
	 * @param processoPesoParteList os intervalos de domínio do peso das partes, conforme R-006
	 * @return o maior dos pesos das partes, considerados seus polos processuais
	 * @throws Exception caso o processo não tenha partes cadastradas ou  
	 * caso os intervalos de domínio não tenham sido definidos
	 * @see #buscarPesoPartes(List, Integer, ProcessoParteParticipacaoEnum)
	 */
	private Double calcularPesoQuantidadePartes(List<ProcessoPesoParte> processoPesoParteList) throws Exception {
		if (processoParteList == null || processoParteList.size() == 0) {
			throw new Exception("Não há partes cadastradas no processo.");
		}
		if (processoPesoParteList == null || processoPesoParteList.size() == 0) {
			throw new Exception("Não há pesos cadastrados para a quantidade de partes incluídas no processo.");
		}
		Integer quantPartesPoloAtivo = buscarQuantidadePartes(ProcessoParteParticipacaoEnum.A);
		Integer quantPartesPoloPassivo = buscarQuantidadePartes(ProcessoParteParticipacaoEnum.P);

		Double pesoPoloAtivo = buscarPesoPartes(processoPesoParteList, quantPartesPoloAtivo,
				ProcessoParteParticipacaoEnum.A);
		Double pesoPoloPassivo = buscarPesoPartes(processoPesoParteList, quantPartesPoloPassivo,
				ProcessoParteParticipacaoEnum.P);

		return (pesoPoloAtivo > pesoPoloPassivo ? pesoPoloAtivo : pesoPoloPassivo);
	}

	/**
	 * Calcula o peso deste processo, conforme R-016 do documento de especificação da distribuição no PJe.
	 * 
	 * @param processoPesoParteList os intervalos de domínio de peso das partes
	 * @return o peso do processo
	 * @throws Exception caso o processo não tenha partes cadastradas, caso os intervalos de domínio não 
	 * tenham sido definidos, caso o processo não tenha classe vinculada, caso a classe não tenha peso definido, 
	 * caso o processo não tenha assuntos vinculados ou se algum dos assuntos não tenha peso definido
	 */
	public Double calcularPesoProcessual(List<ProcessoPesoParte> processoPesoParteList) throws Exception {
		Double pesoPartes = calcularPesoQuantidadePartes(processoPesoParteList);
		Double pesoClasse = calcularPesoClasse();
		Double pesoAssunto = calcularPesoAssunto();
		return pesoPartes * pesoClasse * pesoAssunto;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProcessoTrf)) {
			return false;
		}
		ProcessoTrf other = (ProcessoTrf) obj;
		if (getIdProcessoTrf() != other.getIdProcessoTrf()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdProcessoTrf();
		return result;
	}

	/**
	 * Recupera a sessão indicada como sugerida para o julgamento do processo em uma instância colegiada.
	 * 
	 * @return a sessão sugerida
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_sessao_sugerida")
	public Sessao getSessaoSugerida() {
		return sessaoSugerida;
	}

	/**
	 * Atribui a este processo uma sessão como sendo a sugerida para seu julgamento em uma instância colegiada
	 * 
	 * @param sessaoSugerida a sessão a ser atribuída
	 */
	public void setSessaoSugerida(Sessao sessaoSugerida) {
		this.sessaoSugerida = sessaoSugerida;
	}

	/**
	 * Atribui a um parâmetro temporário o texto descritivo de suas prioridades.
	 * 
	 * @param prioridadesString a descrição das prioridades
	 */
	public void setPrioridadesString(String prioridadesString) {
		this.prioridadesString = prioridadesString;
	}

	/**
	 * Recupera uma String com a lista de prioridades deste processo, separada por quebras de linha (\n).
	 * 
	 * @return a lista de prioridades
	 * @see Transient
	 */
	@Transient
	public String getPrioridadesString() {
		StringBuilder s = new StringBuilder();
		for (ProcessoPrioridadeProcesso p : processoPrioridadeProcessoList) {
			s.append(p.getPrioridadeProcesso().getPrioridade() + "\n");
		}
		this.prioridadesString = s.toString();
		return this.prioridadesString;
	}

	/**
	 * Recupera a lista de advogados ativos que figuram no polo ativo do processo.
	 * 
	 * @return a lista de advogados
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaAdvogadosPoloAtivo() {
		return getListaAdvogados(ProcessoParteParticipacaoEnum.A);
	}
	
	
	/**
	 * Recupera lista de nomes separados por vírgula dos advogados do polo ativo.
	 * @return a lista de nomes de advogados
	 * @see transient
	 */
	@Transient
	public String getListaNomeAdvogadosPoloAtivo() {
		String listaNomeAdv = "";
		if (getListaAdvogadosPoloAtivo() != null) {
			for (ProcessoParte pp : getListaAdvogadosPoloAtivo()) {
				if (listaNomeAdv.isEmpty()) {
					listaNomeAdv = pp.getNomeParte();
				} else {
					listaNomeAdv += ", " + pp.getNomeParte();
				}			 
			}
		}
		return listaNomeAdv;
	}
	
	/**
	 * Recupera a lista de advogados ativos que figuram no polo passivo do processo.
	 * 
	 * @return a lista de advogados
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaAdvogadosPoloPassivo() {
		return getListaAdvogados(ProcessoParteParticipacaoEnum.P);
	}
	
	/**
	 * Recupera lista de nomes separados por vírgula dos advogados do polo ativo.
	 * @return a lista de nomes de advogados
	 * @see transient
	 */
	@Transient
	public String getListaNomeAdvogadosPoloPassivo() {
		String listaNomeAdv = "";
		if (getListaAdvogadosPoloPassivo() != null) {
			for (ProcessoParte pp : getListaAdvogadosPoloPassivo()) {
				if (listaNomeAdv.isEmpty()) {
					listaNomeAdv = pp.getNomeParte();
				} else {
					listaNomeAdv += ", " + pp.getNomeParte();
				}			 
			}
		}
		return listaNomeAdv;
	}

	/**
	 * Recupera a lista de advogados ativos que figuram no polo passado por parâmetro.
	 * 
	 * @param inParticipacao o polo de interesse
	 * @return a lista de advogados do polo de interesse
	 * @see ParametroUtil#getTipoAdvogado()
	 * @see Transient
	 */
	@Transient
	public List<ProcessoParte> getListaAdvogados(ProcessoParteParticipacaoEnum inParticipacao) {
		TipoParte tipoParteAdvogado = getTipoParteAdvogado();
		return getListaAdvogados(inParticipacao, tipoParteAdvogado);
	}

	/**
	 * Recupera a lista de partes ativas do processo que figuram no polo e são do tipo indicados.
	 * 
	 * @param inParticipacao o tipo de participação de interesse
	 * @param tipoParteAdvogado o tipo de participação de interesse
	 * @return a lista de partes
	 */
	@Transient
	public List<ProcessoParte> getListaAdvogados(ProcessoParteParticipacaoEnum inParticipacao,
			TipoParte tipoParteAdvogado) {
		List<ProcessoParte> processoParteAdvogadoList = new ArrayList<ProcessoParte>(0);
		List<ProcessoParte> processoParteList = null;
		switch (inParticipacao) {
		case A:
			processoParteList = getListaParteAtivo();
			break;
		case P:
			processoParteList = getListaPartePassivo();
			break;
		default:
			processoParteList = getListaPartePoloObj(ProcessoParteParticipacaoEnum.T);
		}
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getTipoParte().equals(tipoParteAdvogado)) {
				processoParteAdvogadoList.add(processoParte);
			}
		}
		return processoParteAdvogadoList;
	}

	/**
	 * Recupera o nome da primeira parte do polo indicado, acompanhado da expressão "e outros" se houver mais 
	 * de uma parte no aludido polo. 
	 * 
	 * Não deve ser utilizado em implementações futuras, devendo-se preferir o uso de 
	 * {@link ProcessoJudicialService#getNomeExibicaoPolo(ProcessoTrf, ProcessoParteParticipacaoEnum)} 
	 * 
	 * @param opcao {@link ProcessoTrf#POLO_ATIVO} ou {@link ProcessoTrf#POLO_PASSIVO}
	 * @param tipo indicação quanto a que a String de resposta contenha o tipo da parte (autor, réu etc.) precedendo o nome 
	 * da primeira parte do polo de interesse.
	 * @return texto descritivo do polo processual, acompanhado ou não de seu tipo.
	 * 
	 * @author Rodrigo Cartaxo / Haroldo Arouca / Sérgio Pacheco
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	private String getNomeDaPrimeiraPartePoloPassivoOuAtivo(int opcao, boolean tipo) {
		List<ProcessoParte> items;
		if (opcao == ProcessoTrf.POLO_ATIVO) {
			items = getListaAutor();
		} else {
			items = getListaReu();
		}
		String complemento = "";
		Integer quantidadeOutros = items.size() - 1;

		if (quantidadeOutros == 1)
			complemento = " e outros";
		if (quantidadeOutros > 1)
			complemento = " e outros (" + quantidadeOutros.toString() + ")";

		for (ProcessoParte parte : items) {
			if (parte.getPartePrincipal() && tipo)
				return parte.getPoloTipoParteStr() + ": " + parte.getNomeParte() + complemento;

			if (parte.getPartePrincipal() && !tipo)
				return parte.getNomeParte() + complemento;

		}

		return null;
	}

	/**
	 * Recupera texto descritivo do primeiro requerido do processo, com indicação de sua designação interna (requerido, réu etc.)
	 * e, quando cabível, com a expressão "e outros".
	 * 
	 * Não deve ser utilizado em implementações futuras, devendo-se preferir o uso de {@link ProcessoJudicialService#getNomeExibicaoPolo(ProcessoTrf, ProcessoParteParticipacaoEnum)} 
	 * 
	 * @return o texto descritivo do primeiro requerido no formato TIPO_DA_PARTE: NOME_PARTE [e outros] 
	 * @author Rodrigo Cartaxo Haroldo Arouca
	 * @since 1.2.0
	 * @see
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getTipoNomeReuProcesso() {
		return getNomeDaPrimeiraPartePoloPassivoOuAtivo(ProcessoTrf.POLO_PASSIVO, true);
	}

	/**
	 * Recupera texto descritivo do primeiro autor do processo, com indicação de sua designação interna (autor, impetrante etc.)
	 * e, quando cabível, com a expressão "e outros".
	 * 
	 * Não deve ser utilizado em implementações futuras, devendo-se preferir o uso de {@link ProcessoJudicialService#getNomeExibicaoPolo(ProcessoTrf, ProcessoParteParticipacaoEnum)} 
	 * 
	 * @return o texto descritivo do primeiro autor no formato TIPO_DA_PARTE: NOME_PARTE [e outros] 
	 * @author Rodrigo Cartaxo Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getTipoNomeAutorProcesso() {
		return getNomeDaPrimeiraPartePoloPassivoOuAtivo(ProcessoTrf.POLO_ATIVO, true);
	}

	/**
	 * Recupera texto descritivo do primeiro requerido do processo com a expressão "e outros",
	 * quando cabível.
	 * 
	 * Não deve ser utilizado em implementações futuras, devendo-se preferir o uso de {@link ProcessoJudicialService#getNomeExibicaoPolo(ProcessoTrf, ProcessoParteParticipacaoEnum)} 
	 * 
	 * @return o texto descritivo do primeiro requerido, no formato NOME_PARTE [e outros]
	 * @author Rodrigo Cartaxo Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeReuProcesso() {
		return getNomeDaPrimeiraPartePoloPassivoOuAtivo(ProcessoTrf.POLO_PASSIVO, false);
	}

	/**
	 * Recupera texto descritivo do primeiro requerente do processo com a expressão "e outros",
	 * quando cabível.
	 * 
	 * Não deve ser utilizado em implementações futuras, devendo-se preferir o uso de {@link ProcessoJudicialService#getNomeExibicaoPolo(ProcessoTrf, ProcessoParteParticipacaoEnum)}
	 *  
	 * @return o texto descritivo do primeiro autor, no formato NOME_PARTE [e outros]
	 * @author Rodrigo Cartaxo Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeAutorAtivoProcesso() {
		return getNomeDaPrimeiraPartePoloPassivoOuAtivo(ProcessoTrf.POLO_ATIVO, false);
	}

	/**
	 * Recupera texto contendo os nomes das partes componentes do polo ativo do processo separados por vírgula,
	 * precedidos da expressão "Advogado(s) do reclamante: ".
	 * 
	 * O método não deve mais ser utilizado por conter limitação negocial da descrição (uso de "reclamante" no texto), além
	 * de conter erro negocial quanto à designação do tipo dado (advogado, quando poderia ser qualquer outro tipo).
	 * 
	 * @return o texto descritivo
	 * @author Rodrigo Cartaxo / Haroldo Arouca / Sérgio Pacheco
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getTipoNomeAdvogadoAutorList() {
		TipoParte tipoParteAdvogado = getTipoParteAdvogado();
		return getTipoNomeAdvogadoAutorList(tipoParteAdvogado);
	}

	/**
	 * Recupera texto contendo os nomes das partes do tipo dado componentes do polo ativo do processo separados por vírgula,
	 * precedidos da expressão "Advogado(s) do reclamante: ".
	 * 
	 * O método não deve mais ser utilizado por conter limitação negocial da descrição (uso de "reclamante" no texto), além
	 * de conter erro negocial quanto à designação do tipo dado (advogado, quando poderia ser qualquer outro tipo).
	 * 
	 * @param tipoParteAdvogado o tipo de parte que se pretende listar.
	 * @return o texto descritivo
	 */
	@Transient
	@Deprecated
	public String getTipoNomeAdvogadoAutorList(TipoParte tipoParteAdvogado) {
		List<ProcessoParte> partesPoloAtivo = null;

		partesPoloAtivo = getListaParteAtivo();

		String processoParteAdvogadoStr = "";
		int contadorAdvogados = 0;

		for (ProcessoParte parte : partesPoloAtivo) {

			if (parte.getTipoParte().equals(tipoParteAdvogado)) {

				if (contadorAdvogados == 0) {
					processoParteAdvogadoStr = processoParteAdvogadoStr + "Advogado(s) do reclamante: ";
				}

				contadorAdvogados++;
				processoParteAdvogadoStr = processoParteAdvogadoStr + parte.getNomeParte().toUpperCase() + ", ";
			}
		}

		if (contadorAdvogados > 0) {
			processoParteAdvogadoStr = processoParteAdvogadoStr.substring(0, processoParteAdvogadoStr.length() - 2);
		}

		return processoParteAdvogadoStr;
	}

	/**
	 * Recupera texto contendo os nomes das partes componentes do polo passivo do processo separados por vírgula,
	 * precedidos da expressão "Advogado(s) do reclamante: ".
	 * 
	 * O método não deve mais ser utilizado por conter limitação negocial da descrição (uso de "reclamante" no texto), além
	 * de conter erro negocial quanto à designação do tipo dado (advogado, quando poderia ser qualquer outro tipo).
	 * 
	 * @return o texto descritivo
	 * @author Rodrigo Cartaxo / Haroldo Arouca / Sérgio Pacheco
	 * @since 1.2.0
	 * @see
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getTipoNomeAdvogadoReuList() {
		TipoParte tipoParteAdvogado = getTipoParteAdvogado();
		return getTipoNomeAdvogadoReuList(tipoParteAdvogado);
	}

	/**
	 * Recupera o nome do último usuário que pertence à localização do órgão julgador deste processo 
	 * cujo papel tenha a designação "magistrado".
	 * 
	 * O método deve ser depreciado por o seu resultado ser diverso do que se dá a entender pela designação.
	 * 
	 * @return o nome do último usuário da localização do processo que tenha o papel "magistrado".
	 * @author Rodrigo Cartaxo / Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeJuizOrgaoJulgador() {
		String nomeDoJuizOrgaoJulgador = "";
		
		if (orgaoJulgador != null) {
			Localizacao localizacao = orgaoJulgador.getLocalizacao();
			if (localizacao != null) {
				for (UsuarioLocalizacao usuarioLocalizacao : localizacao.getUsuarioLocalizacaoList()) {
					if ( usuarioLocalizacao.getPapel().getIdentificador().equalsIgnoreCase("MAGISTRADO")) {
						nomeDoJuizOrgaoJulgador = usuarioLocalizacao.getUsuario().getNome();
					}
				}
			}
		}
		return nomeDoJuizOrgaoJulgador;
	}

	/**
	 * Recupera texto contendo os nomes das partes do tipo dado componentes do polo passivo do processo separados por vírgula,
	 * precedidos da expressão "Advogado(s) do reclamante: ".
	 * 
	 * O método não deve mais ser utilizado por conter limitação negocial da descrição (uso de "reclamante" no texto), além
	 * de conter erro negocial quanto à designação do tipo dado (advogado, quando poderia ser qualquer outro tipo).
	 * 
	 * @param tipoParteAdvogado o tipo de parte que se pretende listar.
	 * @return o texto descritivo
	 */
	@Transient
	@Deprecated
	public String getTipoNomeAdvogadoReuList(TipoParte tipoParteAdvogado) {
		List<ProcessoParte> partesPoloPassivo = null;

		partesPoloPassivo = getListaPartePassivo();

		StringBuilder processoParteAdvogadoStr = new StringBuilder();
		int contadorAdvogados = 0;

		for (ProcessoParte parte : partesPoloPassivo) {

			if (parte.getTipoParte().equals(tipoParteAdvogado)) {

				if (contadorAdvogados == 0) {
					processoParteAdvogadoStr.append("Advogado(s) do reclamado: ");
				}

				contadorAdvogados++;
				processoParteAdvogadoStr.append(parte.getNomeParte().toUpperCase());
				processoParteAdvogadoStr.append(", ");
			}
		}
		String retorno = "";
		if (contadorAdvogados > 0) {
			retorno = processoParteAdvogadoStr.substring(0, processoParteAdvogadoStr.length() - 2);
		}

		return retorno;
	}

	/**
	 * Recupera a lista de audiências vinculadas a este processo.
	 * 
	 * @return a lista de audiências
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "processoTrf")
	@OrderBy("dtMarcacao DESC")
	public List<ProcessoAudiencia> getProcessoAudienciaList() {
		return processoAudienciaList;
	}

	/**
	 * Atribui a este processo uma lista de audiências.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.
	 *   
	 * @param processoAudienciaList a lista a ser atribuída
	 */
	public void setProcessoAudienciaList(List<ProcessoAudiencia> processoAudienciaList) {
		this.processoAudienciaList = processoAudienciaList;
	}

	/**
	 * Recupera, por iteração simples, a audiência ativa, não cancelada e futura mais próxima do momento 
	 * da chamada ao método.
	 * 
	 * @return a audiência futura mais próxima
	 * @author Emmanuel Magalhães
	 * @since 1.2.0
	 * @category=PJE-JT
	 * @deprecated Substituído por {@link br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager#getProximaAudienciaDesignada}
	 */
	@Transient
	@Deprecated 
	public ProcessoAudiencia getProximaAudiencia() {

		Date hoje = new Date();
		ProcessoAudiencia proxima = null;
		for (ProcessoAudiencia p : processoAudienciaList) {
			if (p.getInAtivo() && p.getDtCancelamento() == null && p.getDtInicio().after(hoje)) {
				proxima = (proxima == null || p.getDtInicio().before(proxima.getDtInicio())) ? p : proxima;
			}
		}
		return proxima;
	}

	/**
	 * Recupera a primeira audiência da lista de audiências do processo judicial.
	 * 
	 * O método deve ser evitado por não assegurar qualquer critério na identificação dessa primeira 
	 * audiência, já que não afere se a primeira audiência registrada é efetivamente a mais antiga do 
	 * processo, nem, tampouco, se ela foi redesignada, cancelada ou inativada.
	 * 
	 * @return a primeira audiência registrada no processo
	 */
	@Transient
	@Deprecated
	public ProcessoAudiencia getPrimeiraAudiencia() {
		return processoAudienciaList.isEmpty() ? null : processoAudienciaList.get(0);
	}
	
	/**
	 * Recupera a lista de partes componentes do polo passivo do processo que têm o tipo
	 * idêntico àquele apontado na classe judicial do processo.
	 * 
	 * Esse método deve ser reimplementado para se tornar seguro quando a classe judicial 
	 * não tem polo passivo identificável (nulo).
	 * 
	 * @return a lista dos pretensos réus (não inclui advogados, tutores etc.)
	 * @author Sérgio Pacheco / Rafael Carvalho
	 * @since 1.2.0
	 * @see ClasseJudicial#getPoloPassivo()
	 * @category PJE-JT
	 */
	@Transient
	public List<ProcessoParte> getListaReu() {
		return getListaPartePrincipalPassivo();
	}
	
	/**
	 * Recupera a lista de partes componentes do polo passivo do processo ordenada
	 * por seus identificadores
	 * 
	 * @return a lista de partes do polo passivo ordenada por seus identificadores
	 * 
	 * @see #getListaReu()
	 *  @see https://www.cnj.jus.br/jira/browse/PJEII-1079
	 */
	@Transient
	public List<ProcessoParte> getListaReuOrdenadaPorId() {
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>(2);
		partes.addAll(getListaReu());
		
		return ordenarListaPorId(partes);
	}

	/**
	 * Recupera a lista de partes componentes do polo ativo do processo que têm o tipo
	 * idêntico àquele apontado na classe judicial do processo.
	 * 
	 * @return a lista dos pretensos autores (não inclui advogados, tutores etc.)
	 * @author Sérgio Pacheco / Rafael Carvalho
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	public List<ProcessoParte> getListaAutor() {
		return getListaPartePrincipalAtivo();
	}

	/**
	 * Recupera a lista de partes componentes do polo ativo do processo ordenada
	 * por seus identificadores
	 * 
	 * @return a lista de partes do polo ativo ordenada por seus identificadores
	 * 
	 * @see #getListaAutor()
	 *  @see https://www.cnj.jus.br/jira/browse/PJEII-1079
	 */
	@Transient
	public List<ProcessoParte> getListaAutorOrdenadaPorId() {
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>(0);
		partes.addAll(getListaAutor());
		
		return ordenarListaPorId(partes);		
	}
	
	/**
	 * Ordena uma lista de {@link ProcessoParte} dada pelo identificador.
	 * 
	 * @return a lista dada, ordenada pelo identificador do {@link ProcessoParte}
	 * 
	 */
	
	private List<ProcessoParte> ordenarListaPorId(List<ProcessoParte> lista) {
		Collections.sort(lista);
		return lista;
	}

	/**
	 * Recupera uma descrição textual da lista de partes dada contendo o nome e o CPF, separados por
	 * vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e partir da premissa
	 *  de que a lista de partes é composta apenas por pessoas físicas. 
	 * 
	 * @return a lista de nomes seguidos de cpf das partes na lista informada
	 * @author Sérgio Pacheco e Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCpfParteList(List<ProcessoParte> partes) {
		StringBuilder lista = new StringBuilder();
		String nomeParte = "";
		for (ProcessoParte parte : partes) {
			if (lista.length() > 0)
				lista.append(", ");

			if (parte == null) {
				System.out.println("ProcessoTrf.getNomeCpfParteList(): Parte nula na lista!!!");
			} else {
				String cpf = "";
				nomeParte = parte.getNomeParte();
				if ((nomeParte == null) || (nomeParte.length() < 1))
					nomeParte = "parte sem nome informado";
				PessoaDocumentoIdentificacao pdi;
				pdi = parte.getPessoa().buscaDocumentoIdentificacao("CPF");
				if (pdi != null) {
					cpf = pdi.getNumeroDocumento();
					if ((cpf == null) || (cpf.length() < 1))
						cpf = "não informado";
					lista.append(nomeParte);
					lista.append(" CPF: ");
					lista.append(cpf);
				}
			}
		}

		return lista.toString();
	}

	/**
	 * Recupera uma descrição textual da lista de partes dada contendo o nome e o CNPJ, separados por
	 * vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e partir da premissa
	 *  de que a lista de partes é composta apenas por pessoas jurídicas. 
	 * 
	 * @return a lista de nomes seguidos do CNPJ das partes na lista informada
	 * @author Sérgio Pacheco e Haroldo Arouca
	 * @since 1.2.0
	 * @see
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCnpjParteList(List<ProcessoParte> partes) {
		StringBuilder lista = new StringBuilder();
		String nomeParte = "";
		for (ProcessoParte parte : partes) {
			if (lista.length() > 0)
				lista.append(", ");

			if (parte == null) {
				System.out.println("ProcessoTrf.getNomeCnpjParteList(): Parte nula na lista!!!");
			} else {
				String cnpj = "";
				nomeParte = parte.getNomeParte();
				if ((nomeParte == null) || (nomeParte.length() < 1))
					nomeParte = "parte sem nome informado";
				PessoaDocumentoIdentificacao pdi;
				pdi = parte.getPessoa().buscaDocumentoIdentificacao("CPJ");
				if (pdi != null) {
					cnpj = pdi.getNumeroDocumento();
					if ((cnpj == null) || (cnpj.length() < 1))
						cnpj = "não informado";
					lista.append(nomeParte);
					lista.append(" CNPJ: ");
					lista.append(cnpj);
				}
			}
		}

		return lista.toString();
	}

	/**
	 * Recupera uma descrição textual da lista de partes dada contendo o nome e o CPF 
	 * ou o nome e o CNPJ, separados por vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e 
	 *  partir da premissa de que a lista de partes é composta apenas por pessoas identificadas. 
	 *  Além disso, ele reproduz código de dois outros métodos, tornando difícil a manutenção.  
	 * 
	 * @return a lista de nomes seguidos de CPF ou CNPJ das partes na lista informada
	 * @author Sérgio Pacheco
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCpfOuCnpjParteList(List<ProcessoParte> partes) {
		StringBuilder lista = new StringBuilder();
		String nomeParte = "";
		String cpfOuCnpj = "";
		String documento = "";
		for (ProcessoParte parte : partes) {
			if (lista.length() > 0)
				lista.append(", ");

			if (parte == null) {
				System.out.println("Parte nula na lista!!!");
			} else {
				String cpf = "";
				String cnpj = "";
				PessoaDocumentoIdentificacao pdi;
				pdi = parte.getPessoa().buscaDocumentoIdentificacao("CPF");
				if (pdi != null) {
					cpf = pdi.getNumeroDocumento();
				}
				if ((cpf != null) && (cpf.length() > 0)) {
					documento = "CPF";
					cpfOuCnpj = cpf;
				} else {
					pdi = parte.getPessoa().buscaDocumentoIdentificacao("CPJ");
					if (pdi != null) {
						cnpj = pdi.getNumeroDocumento();
					}
					if ((cnpj != null) && (cnpj.length() > 0)) {
						documento = "CNPJ";
						cpfOuCnpj = cnpj;
					}
				}

				nomeParte = parte.getNomeParte();

				if (documento.length() < 1)
					documento = "cpf ou cnpj";

				if (cpfOuCnpj.length() < 1)
					cpfOuCnpj = "não informado";

				if ((nomeParte == null) || (nomeParte.length() < 1))
					nomeParte = "parte sem nome informado";

				lista.append(nomeParte);
				lista.append(" ");
				lista.append(documento);
				lista.append(": ");
				lista.append(cpfOuCnpj);

			}
		}

		return lista.toString();
	}

	/**
	 * Recupera uma descrição textual da lista de requeridos contendo o nome e o CPF ou 
	 * o nome e o CNPJ, separados por vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e partir da premissa
	 *  de que a lista de partes é composta apenas por pessoas identificadas. 
	 * 
	 * @return a lista de nomes dos requeridos
	 * @author Sérgio Pacheco / Rafael Carvalho
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCpfOuCnpjReuList() {
		return getNomeCpfOuCnpjParteList(getListaReu());
	}

	/**
	 * Recupera uma descrição textual da lista de requeridos contendo o nome e o CPF, separados por vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e partir da premissa
	 *  de que a lista de partes é composta apenas por pessoas identificadas. 
	 * 
	 * @return a lista de nomes dos requeridos
	 * @author Sérgio Pacheco / Rafael Carvalho / Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCpfReuList() {
		return getNomeCpfParteList(getListaReu());
	}

	/**
	 * Recupera uma descrição textual da lista de requeridos contendo o nome e o CNPJ, separados por vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e partir da premissa
	 *  de que a lista de partes é composta apenas por pessoas identificadas. 
	 * 
	 * @return a lista de nomes dos requeridos
	 * @author Sérgio Pacheco / Rafael Carvalho / Haroldo Arouca
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCnpjReuList() {
		return getNomeCnpjParteList(getListaReu());
	}

	/**
	 * Recupera uma descrição textual da lista de requerentes contendo o nome e o CPF, separados por vírgulas.
	 * 
	 *  Esse método está sendo depreciado em razão de sua lógica ser muito específica e partir da premissa
	 *  de que a lista de partes é composta apenas por pessoas identificadas. 
	 * 
	 * @return a lista de nomes dos requerentes
	 * @author Sérgio Pacheco / Rafael Carvalho
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	@Deprecated
	public String getNomeCpfAutorList() {
		return getNomeCpfParteList(getListaAutor());
	}

	/**
	 * Recupera a lista de assuntos do processo.
	 * 
	 * @return a lista de assuntos
	 * @see #getProcessoAssuntoList()
	 */
	@ManyToMany(cascade={CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_processo_assunto", 
		joinColumns = @JoinColumn(name = "id_processo_trf", nullable = false, updatable = false, insertable = false), 
		inverseJoinColumns = @JoinColumn(name = "id_assunto_trf", nullable = false, updatable = false, insertable = false)
	)
	public List<AssuntoTrf> getAssuntoTrfList() {
		return this.assuntoTrfList;
	}
	
	/*
	 * PJE-JT: Ricardo Scholz : PJE-1368 - 2012-06-12 Alteracoes feitas pela JT.
	 */
	/**
	 * Recupera uma lista de Strings contendo a descrição do assunto.
	 * 
	 * @return a lista de assuntos como Strings
	 * @see AssuntoTrf#getAssuntoTrf()
	 * @see Transient
	 */
	@Transient
	public List<String> getAssuntoTrfListStr(){
		List<String> result = new ArrayList<String>();
		if(this.assuntoTrfList != null){
			for(AssuntoTrf a: this.assuntoTrfList){
				result.add(a.getAssuntoTrf());
			}
		}
		return result;
	}
	/*
	 * PJE-JT: Fim.
	 */

	/**
	 * Atribui a este processo uma lista de assuntos.
	 * 
	 * Esse método não deve ser utilizado diretamente na implementação do hibernate 3, 
	 * uma vez que a lista é por ele gerenciada. A manipulação da lista deve ser feita por meio 
	 * de tratamento da lista recuperada pelo método get da lista correspondente.  
	 * 
	 * @param assuntoTrfList a lista a ser atribuída
	 */
	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}

	/**
	 * Recupera a lista de partes ativas que compõem o polo ativo do processo.
	 * 
	 * Depreciado em favor de {@link #getListaParteAtivo()}.
	 * 
	 * @return a lista de requerentes ativos
	 * @see #getListaParteAtivo()
	 */
	@Transient
	@Deprecated
	public List<ProcessoParte> getProcessoParteAtivoList() {
		List<ProcessoParte> ppList = getProcessoParteList();
		List<ProcessoParte> ppAtivoList = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : ppList) {
			if (processoParte.getIsAtivo()) {
				ppAtivoList.add(processoParte);
			}
		}
		return ppAtivoList;
	}

	/**
	 * Atribui ao processo judicial marca indicativa de que teria havido violação, 
	 * por seu valor da causa, da faixa de valores fixada na classe judicial.
	 * Para quando o processo protocolado viola a faixa de valores mímimo e
	 * máximo da competência à qual pertence.
	 * 
	 * @param violacaoFaixaValoresCompetencia 'D', para indicar que, embora distribuído,
	 * houve violação do valor indicado
	 * @author Bernardo Gouvea / Valério Wittler
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	public void setViolacaoFaixaValoresCompetencia(Character violacaoFaixaValoresCompetencia) {
		this.violacaoFaixaValoresCompetencia = violacaoFaixaValoresCompetencia;
	}

	/**
	 * Indica se o valor da causa do processo judicial violou a faixa de valores vinculadas à
	 * classe judicial.
	 *  
	 * @return 'D', se houve violação da faixa de valores, ou null.
	 */
	@Column(name = "in_violacao_faixa_valores", nullable = true)
	public Character getViolacaoFaixaValoresCompetencia() {
		return violacaoFaixaValoresCompetencia;
	}

	/**
	 * Recupera a lista de peritos que atuaram neste processo e que nele constam
	 * como terceiros desvinculados das partes.
	 * 
	 * @return a lista de peritos
	 * @author Athos / Ricardo / Sérgio
	 * @since 1.2.0
	 * @category PJE-JT
	 */
	@Transient
	public List<ProcessoParte> getListaPerito() {
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>(0);

		for (ProcessoParte parte : getListaParteTerceiro()) {

			for (ProcessoPericia processoPericia : this.getProcessoPericiaList()) {
				PessoaPerito pessoaPerito = processoPericia.getPessoaPerito();
				if (pessoaPerito != null) {
					if (pessoaPerito.getIdUsuario().equals(parte.getPessoa().getIdUsuario())) {
						partes.add(parte);
						break;
					}
				}
			}

		}

		return partes;
	}

	/**
	 * @author Athos / Ricardo
	 * @since 1.2.0
	 * @see
	 * @category PJE-JT
	 * @return a lista dos leiloeiros do processo.
	 */
	@Transient
	public List<ProcessoParte> getListaLeiloeiro() {
		List<ProcessoParte> partes = new ArrayList<ProcessoParte>(0);

		for (ProcessoParte parte : getListaParteTerceiro()) {
			TipoParte tipoParte = parte.getTipoParte();
			if ((tipoParte != null) && (tipoParte.getTipoParte().toUpperCase().contains("LEILOEIRO"))) {
				partes.add(parte);
			}
		}
		return partes;
	}

	
	/**
	 * Recupera a lista de pessoas que compõem o polo ativo.
	 * 
	 * @return a lista de requerentes
	 * @see #getListaPartePoloObj(ProcessoParteParticipacaoEnum...)
	 */
	@Transient 
	public List<Pessoa> getPessoaPoloAtivoList() {
		return getPessoaList(ProcessoParteParticipacaoEnum.A);
	}
	
	/**
	 * Recupera a lista de autoridades que figuram no polo ativo.
	 * 
	 * @return a lista de autoridades
	 * @see #getAutoridades(ProcessoParteParticipacaoEnum)
	 */
	@Transient
	public List<PessoaAutoridade> getAutoridadesPoloAtivo(){
		return getAutoridades(ProcessoParteParticipacaoEnum.A);
	}
	
	/**
	 * Recupera a lista de autoridades que figuram no polo passivo.
	 * 
	 * @return a lista de autoridades
	 * @see #getAutoridades(ProcessoParteParticipacaoEnum)
	 */
	@Transient
	public List<PessoaAutoridade> getAutoridadesPoloPassivo(){
		return getAutoridades(ProcessoParteParticipacaoEnum.P);
	}
	
	/**
	 * Recupera a lista de autoridades que figuram no processo no polo indicado.
	 * 
	 * @param tipoParticipacao o polo de interesse
	 * @return a lista de autoridades
	 */
	@Transient
	public List<PessoaAutoridade> getAutoridades(ProcessoParteParticipacaoEnum tipoParticipacao){
		List<PessoaAutoridade> autoridades = new ArrayList<PessoaAutoridade>();
		for(Pessoa p: getPessoaList(tipoParticipacao)){
			if(PessoaAutoridade.class.isAssignableFrom(p.getClass())){
				autoridades.add((PessoaAutoridade) p);
			}
		}
		return autoridades;
	}
	

	/**
	 * Recupera a lista de pessoas que compõem o polo passivo.
	 * 
	 * @return a lista de requeridos
	 * @see #getListaPartePoloObj(ProcessoParteParticipacaoEnum...)
	 */
	@Transient 
	public List<Pessoa> getPessoaPoloPassivoList() {
		return getPessoaList(ProcessoParteParticipacaoEnum.P);
	}

	/**
	 * Recupera a lista de pessoas que compõem o polo indicado.
	 * 
	 * @param inParticipacao o polo de interesse
	 * @return a lista de pessoas
	 * @see #getListaPartePoloObj(ProcessoParteParticipacaoEnum...)
	 */
	@Transient 
	public List<Pessoa> getPessoaList(ProcessoParteParticipacaoEnum inParticipacao) {
		List<ProcessoParte> processoParteList = null;
		switch (inParticipacao) {
		case A:
			processoParteList = getListaPartePrincipalAtivo();
			break;
		case P:
			processoParteList = getListaPartePrincipalPassivo();
			break;
		case T:
			processoParteList = getListaPartePrincipalTerceiro();
			break;
		default:
			return Collections.emptyList();
		}
		List<Pessoa> pessoaList = new ArrayList<Pessoa>(0);
		for (ProcessoParte processoParte : processoParteList) {
			Pessoa pessoa = processoParte.getPessoa();
			if(!pessoaList.contains(pessoa)) {
				pessoaList.add(pessoa);
			}
		}
		return pessoaList;
	}

	/**
	 * Recupera a lista de tipos de pessoas que compõem o polo ativo.
	 * 
	 * @return a lista de tipos
	 */
	@Transient
	public List<TipoPessoa> getTipoPessoaPoloAtivoList() {
		return getTipoPessoaList(ProcessoParteParticipacaoEnum.A);
	}

	/**
	 * Recupera a lista de tipos de pessoas que compõem o polo passivo.
	 * 
	 * @return a lista de tipos
	 */
	@Transient
	public List<TipoPessoa> getTipoPessoaPoloPassivoList() {
		return getTipoPessoaList(ProcessoParteParticipacaoEnum.P);
	}

	/**
	 * Recupera a lista de tipos de pessoas que compõem o polo de interesse.
	 * 
	 * @param inParticipacao o polo de interesse
	 * @return a lista de tipos
	 */
	@Transient
	public List<TipoPessoa> getTipoPessoaList(ProcessoParteParticipacaoEnum inParticipacao) {
		List<TipoPessoa> tipoPessoaList = new ArrayList<TipoPessoa>(0);
		for (Pessoa pessoa : getPessoaList(inParticipacao)) {
			TipoPessoa tipoPessoa = pessoa.getTipoPessoa();
			if(!tipoPessoaList.contains(tipoPessoa)) {
				tipoPessoaList.add(tipoPessoa);
			}
		}
		return tipoPessoaList;
	}
	
	/**
	 * Recupera o assunto processual principal.
	 * 
	 * @return o {@link ProcessoAssunto}
	 */
	@Transient
	public ProcessoAssunto getProcessoAssuntoPrincipal(){
		for(ProcessoAssunto aux: getProcessoAssuntoList()){
			if(aux.getAssuntoPrincipal()){
				return aux;
			}
		}
		
		return null;
	}
	
	/**
	 * Recupera a lista de partes ativas sem seus advogados.
	 * 
	 * Marcado como depreciado por já existir o método {@link #getProcessoPartePoloAtivoSemAdvogadoList()}
	 * 
	 * @return a lista de partes do polo ativo sem os advogados
	 * @author U006184 - Thiago Oliveira
	 * @see https://www.cnj.jus.br/jira/browse/PJEII-1146
	 */
	@Transient
	@Deprecated
	public List<ProcessoParte> getListaParteAtivoSemAdvogado() {
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(0);
		
		for (ProcessoParte processoParte : getListaPartePoloObj(ProcessoParteParticipacaoEnum.A)) {
			if (!processoParte.getTipoParte().equals(getTipoParteAdvogado()))
				list.add(processoParte);
		}
		
		return ordenarListaPorId(list);	
	}

	/**
	 * Recupera a lista de partes passivas sem seus advogados.
	 * 
	 * Marcado como depreciado por já existir o método {@link #getProcessoPartePoloPassivoSemAdvogadoList()}
	 * 
	 * @return a lista de partes do polo passivo sem os advogados
	 * @author U006184 - Thiago Oliveira
	 * @see https://www.cnj.jus.br/jira/browse/PJEII-1146
	 */
	@Transient
	@Deprecated
	public List<ProcessoParte> getListaPartePassivoSemAdvogado() {
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(0);
		
		for (ProcessoParte processoParte : getListaPartePoloObj(ProcessoParteParticipacaoEnum.P)) {
			if (!processoParte.getTipoParte().equals(getTipoParteAdvogado()))
				list.add(processoParte);
		}
		
		return ordenarListaPorId(list);
	}
	
	/**
	 * Verifica se há uma parte pública (procura em todas as partes)
	 * @return true caso haja, false caso não haja.
	 */
	@Transient
	public boolean haParteOrgaoPublico() {
		boolean haParteOrgaoPublico = false;
		
		for(ProcessoParte pp : getProcessoParteList()) {
			if(pp.getPessoa() instanceof PessoaJuridica) {
				PessoaJuridica pj = (PessoaJuridica) pp.getPessoa();
				
				if(pj.getOrgaoPublico() != null && pj.getOrgaoPublico()) {
					haParteOrgaoPublico = true;
					break;
				}
			}
		}
		
		return haParteOrgaoPublico;
	}
	
		
	/**
	 * Indica se um mandado foi devolvido pelo Oficial de Justiça.
	 * 
	 * @return true, se, o mandado foi devolvido pelo Oficial de Justiça.
	 */
	@Column(name = "in_mandado_devolvido")
	@NotNull
	public Boolean getMandadoDevolvido(){
		return mandadoDevolvido;
	}
	public void setMandadoDevolvido(Boolean mandadoDevolvido){
		this.mandadoDevolvido = mandadoDevolvido;
	}

	/**
	 * Indica se deve ser marcada audiencias para o processo.
	 * Usado para o agrupador de audiencias nao designadas
	 * 
	 * @return true, se devem ser marcadas audiencias para esse processo.
	 */
	@Column(name = "in_deve_marcar_audiencia")
	@NotNull
	public Boolean getDeveMarcarAudiencia() {
		return deveMarcarAudiencia;
	}

	public void setDeveMarcarAudiencia(Boolean naoDeveMarcarAudiencia) {
		this.deveMarcarAudiencia = naoDeveMarcarAudiencia;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="id_processo_trf")
	public List<ProcessoVisibilidadeSegredo> getVisualizadores() {
		return visualizadores;
	}
	
	public void setVisualizadores(List<ProcessoVisibilidadeSegredo> visualizadores) {
		this.visualizadores = visualizadores;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="processoTrf")
	public List<ProcessoCaixaAdvogadoProcurador> getCaixasRepresentantes() {
		return caixasRepresentantes;
	}
	
	public void setCaixasRepresentantes(List<ProcessoCaixaAdvogadoProcurador> caixasRepresentantes) {
		this.caixasRepresentantes = caixasRepresentantes;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_trf", nullable = false,insertable=false,updatable=false)
	public ConsultaProcessoTrfSemFiltro getConsultaProcessoTrf() {
		return this.consultaProcessoTrf;
	}

	public void setConsultaProcessoTrf(ConsultaProcessoTrfSemFiltro consultaProcessoTrf) {
		this.consultaProcessoTrf = consultaProcessoTrf;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_competencia")
	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	@OneToMany(fetch = FetchType.LAZY,mappedBy="processoTrf")
	public List<LogHistoricoMovimentacao> getLogHistoricoMovimentacaotList() {
		return logHistoricoMovimentacaotList;
	}

	public void setLogHistoricoMovimentacaotList(List<LogHistoricoMovimentacao> logHistoricoMovimentacaotList) {
		this.logHistoricoMovimentacaotList = logHistoricoMovimentacaotList;
	}
	
	/**
	 * @deprecated Utilizar estrutura de vinculação de magistrado {@link ProcessoMagistrado}
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_vinculacao_relator")
	@Deprecated
	public Date getDataVinculacaoRelator() {
		return dataVinculacaoRelator;
	}

	/**
	 * @deprecated Utilizar estrutura de vinculação de magistrado {@link ProcessoMagistrado}
	 */
	@Deprecated
	public void setDataVinculacaoRelator(Date dataVinculacaoRelator) {
		this.dataVinculacaoRelator = dataVinculacaoRelator;
	}

	@Column(name="in_exige_revisor", nullable=true)
	public Boolean getExigeRevisor() {
		return exigeRevisor;
	}

	public void setExigeRevisor(Boolean exigeRevisor) {
		this.exigeRevisor = exigeRevisor;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tp_composicao_julgamento", nullable=true, length = 1)
	public ComposicaoJulgamentoEnum getComposicaoJulgamento() {
		return composicaoJulgamento;
	}

	public void setComposicaoJulgamento(ComposicaoJulgamentoEnum composicaoJulgamento) {
		this.composicaoJulgamento = composicaoJulgamento;
	}
	
	@Column(name = "id_area_direito")
	public Integer getIdAreaDireito() {
		return idAreaDireito;
	}

	public void setIdAreaDireito(Integer idAreaDireito) {
		this.idAreaDireito = idAreaDireito;
	}

	/**
	 * Indicativo se o processo será pautado em um julgamento virtual.
	 * 
	 * @return <code>Boolean</code>, <code>true</code> se o processo for marcado
	 *         para ser julgado numa {@link Sessao} virtual. Valor padrão:
	 *         <code>false</code>.
	 */
	@Column(name = "in_pauta_virtual")
	public Boolean getPautaVirtual() {
		return pautaVirtual;
	}

	public void setPautaVirtual(Boolean pautaVirtual) {
		this.pautaVirtual = pautaVirtual;
	}

	@Column(name = "cd_nivel_acesso")
	@NotNull
	public int getNivelAcesso() {
		return nivelAcesso;
	}

	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

	@Override
	@javax.persistence.Transient
	public void setFieldHandler(FieldHandler handler) {
		this.handler = handler;
	}

	@Override
	@javax.persistence.Transient
	public FieldHandler getFieldHandler() {
		return this.handler;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tp_situacao_guia_recolhimento", nullable=true, length = 2)
	public SituacaoGuiaRecolhimentoEnum getSituacaoGuiaRecolhimento() {
		return situacaoGuiaRecolhimento;
	}

	public void setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum situacaoGuiaRecolhimento) {
		this.situacaoGuiaRecolhimento = situacaoGuiaRecolhimento;
	}
	
	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String PROCESSO = "processo";
	}
	
	/**
	 * @return O valor do atributo colecaoCda
	 */
	@OneToMany(fetch=FetchType.LAZY, mappedBy="processoTrf", cascade = {CascadeType.ALL})
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<Cda> getColecaoCda() {
        return this.colecaoCda;
    }
    
    /**
	 * @param colecaoCda atribui um valor ao atributo colecaoCda
	 */
	public void setColecaoCda(List<Cda> colecaoCda) {
        this.colecaoCda = colecaoCda;
    }
	
	/**
	 * Indica se o processo encontra-se bloqueado devido migracao.
	 * 
	 * @return true, se o processo encontra-se bloqueado por migracao.
	 */
	@Column(name = "in_bloqueio_migracao")
	public Boolean getInBloqueioMigracao() {
		return inBloqueioMigracao;
	}

	public void setInBloqueioMigracao(Boolean inBloqueioMigracao) {
		this.inBloqueioMigracao = inBloqueioMigracao;
	}

	@Transient
	public String getNomesPartesFormatada() {
		return nomesPartesFormatada;
	}

	public void setNomesPartesFormatada(String nomesPartesFormatada) {
		this.nomesPartesFormatada = nomesPartesFormatada;
	}
}
