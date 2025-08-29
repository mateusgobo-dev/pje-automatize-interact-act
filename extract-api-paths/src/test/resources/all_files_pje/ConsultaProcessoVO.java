package br.jus.cnj.pje.entidades.vo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Assunto;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.SituacaoGuiaRecolhimentoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

public class ConsultaProcessoVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int idProcessoTrf;
	private ProcessoTrf processoTrf;
	private String numeroProcesso;
	
	private String numeroSequenciaProcessoPattern;
	private List<IntervaloNumeroSequencialProcessoVO> intervalosNumerosSequenciais = new ArrayList<IntervaloNumeroSequencialProcessoVO>(0);
	
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer numeroAno;
	private Integer ramoJustica;
	private Integer numeroOrgaoJustica;
	private Integer numeroOrigem;
	
	private Integer idOrgaoJulgador;
	private String orgaoJulgador;
	private String orgaoJulgadorColegiado;
	private Integer idOrgaoJulgadorColegiado;
	private Jurisdicao jurisdicao;
	private Integer idJurisdicao;
	
	private String classeJudicial;
	private ClasseJudicial classeJudicialObj;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>(0);
	
	private String assuntoJudicial;
	private Assunto assuntoJudicialObj;
	private List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>(0);
	
	private Boolean apenasPrioridade = false;
	private PrioridadeProcesso prioridadeObj;
	
	private OrgaoJulgador orgaoJulgadorObj;
	private OrgaoJulgadorColegiado orgaoJulgadorColegiadoObj;	
	
	private Date dataAutuacao;
	private Date dataAutuacaoInicial;
	private Date dataAutuacaoFinal;
	private String nomeParte;
	private Date dataNascimentoInicial;
	private Date dataNascimentoFinal;
	private String autor;
	private long qtAutor;
	private String reu;
	private long qtReu;
	private String cpfParte;
	private String cnpjParte;
	private String outroDocumentoParte;
	private String oabRepresentanteParte;

	private ProcessoStatusEnum processoStatus;
	private Boolean segredoJustica;
	private ProcessoTrfApreciadoEnum apreciadoSegredo;
	private Date dataDistribuicao;
	private Date dataDistribuicaoInicial;
	private Date dataDistribuicaoFinal;

	private Date dataChegada;
	private Boolean prioridade;
	private String prioridadesString;
	private Integer idTask;
	private Long idTaskInstance;
	private String numeroProcessoCargo;
	private String actorId;
	private Boolean conferido;
	private DocumentoAssinaturaVO documentoAssinatura;
	private String tags;
	private ConsultaProcessoTrfSemFiltro consultaProcesso;
	private Integer idCaixaAdvProc;

	private SituacaoGuiaRecolhimentoEnum situacaoGuiaRecolhimento;

	private Boolean apenasSemCaixa = false;
	private Boolean apenasCaixasComResultados= false;
	private Boolean apenasCaixasAtivas = false;
	private Map<Field, Object> mapaAtributos;
	private ConsultaProcessoVO universoContextoPesquisa;


	private List<String> numerosProcessos;
	
	@Deprecated
	public ConsultaProcessoVO(ConsultaProcessoTrf c, Integer idTask,Date dataChegada, Long idTaskInstance) {
		super();
		this.idProcessoTrf = c.getIdProcessoTrf();
		this.numeroProcesso = c.getNumeroProcesso();
		this.idOrgaoJulgador = c.getIdOrgaoJulgador();
		this.orgaoJulgador = c.getOrgaoJulgador();
		this.orgaoJulgadorColegiado = c.getOrgaoJulgadorColegiado();
		this.idOrgaoJulgadorColegiado = c.getIdOrgaoJulgadorColegiado();
		this.jurisdicao = c.getJurisdicao();
		this.classeJudicial = c.getClasseJudicial();
		this.classeJudicialObj = c.getClasseJudicialObj();
		this.dataAutuacao = c.getDataAutuacao();
		this.autor = c.getAutor();
		this.qtAutor = c.getQtAutor();
		this.reu = c.getReu();
		this.qtReu = c.getQtReu();
		this.processoStatus = c.getProcessoStatus();
		this.processoTrf = c.getProcessoTrf();
		this.segredoJustica = c.getSegredoJustica();
		this.apreciadoSegredo = c.getApreciadoSegredo();
		this.dataDistribuicao = c.getDataDistribuicao();
		this.prioridade = c.getPrioridade();
		this.prioridadesString = c.getPrioridadesString();
		this.idTask = idTask;
		this.setIdTaskInstance(idTaskInstance);
		this.dataChegada = dataChegada;
		this.numeroProcessoCargo = c.getNumeroProcessoCargo();
		this.situacaoGuiaRecolhimento = c.getSituacaoGuiaRecolhimento();

		salvarPesquisaInicial();
	}
	
	@Deprecated
	public ConsultaProcessoVO(ProcessoTrf c, Integer idTask,Date dataChegada, Long idTaskInstance){
		super();
		this.idProcessoTrf = c.getIdProcessoTrf();
		this.numeroProcesso = c.getNumeroProcesso();
		this.idOrgaoJulgador = c.getOrgaoJulgador() == null ? 0 : c.getOrgaoJulgador().getIdOrgaoJulgador();
		this.orgaoJulgador = c.getOrgaoJulgador() == null ? "" : c.getOrgaoJulgador().getOrgaoJulgador();
		this.orgaoJulgadorColegiado = c.getOrgaoJulgadorColegiado() == null ? "" : c.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado();
		this.idOrgaoJulgadorColegiado = c.getOrgaoJulgadorColegiado() == null ? 0 : c.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado();
		this.jurisdicao = c.getJurisdicao();
		this.classeJudicial = c.getClasseJudicialStr();
		this.classeJudicialObj = c.getClasseJudicial();
		this.dataAutuacao = c.getDataAutuacao();
		this.autor = "";
		this.qtAutor = -1;
		this.reu = "";
		this.qtReu = -1;
		this.processoStatus = c.getProcessoStatus();
		this.processoTrf = c;
		this.segredoJustica = c.getSegredoJustica();
		this.apreciadoSegredo = c.getApreciadoSegredo();
		this.dataDistribuicao = c.getDataDistribuicao();
		this.prioridade = c.getPrioridadeProcessoList().isEmpty() ? false : true; 
		this.prioridadesString = c.getPrioridadesString();
		this.idTask = idTask;
		this.setIdTaskInstance(idTaskInstance);
		this.dataChegada = dataChegada;
		this.numeroProcessoCargo = c.getNumeroProcessoCargo();	
		this.situacaoGuiaRecolhimento = c.getSituacaoGuiaRecolhimento();

		salvarPesquisaInicial();
	}
	
	public ConsultaProcessoVO(ProcessoTrf c, Integer idTask,Date dataChegada, Long idTaskInstance, String actorId){
		super();
		this.idProcessoTrf = c.getIdProcessoTrf();
		this.numeroProcesso = c.getNumeroProcesso();
		this.idOrgaoJulgador = c.getOrgaoJulgador() == null ? 0 : c.getOrgaoJulgador().getIdOrgaoJulgador();
		this.orgaoJulgador = c.getOrgaoJulgador() == null ? "" : c.getOrgaoJulgador().getOrgaoJulgador();
		this.orgaoJulgadorColegiado = c.getOrgaoJulgadorColegiado() == null ? "" : c.getOrgaoJulgadorColegiado().getOrgaoJulgadorColegiado();
		this.idOrgaoJulgadorColegiado = c.getOrgaoJulgadorColegiado() == null ? 0 : c.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado();
		this.jurisdicao = c.getJurisdicao();
		this.classeJudicial = c.getClasseJudicialStr();
		this.classeJudicialObj = c.getClasseJudicial();
		this.dataAutuacao = c.getDataAutuacao();
		this.autor = "";
		this.qtAutor = -1;
		this.reu = "";
		this.qtReu = -1;
		this.processoStatus = c.getProcessoStatus();
		this.processoTrf = c;
		this.segredoJustica = c.getSegredoJustica();
		this.apreciadoSegredo = c.getApreciadoSegredo();
		this.dataDistribuicao = c.getDataDistribuicao();
		this.prioridade = c.getPrioridadeProcessoList().isEmpty() ? false : true; 
		this.prioridadesString = c.getPrioridadesString();
		this.idTask = idTask;
		this.setIdTaskInstance(idTaskInstance);
		this.actorId = actorId;
		this.dataChegada = dataChegada;
		this.numeroProcessoCargo = c.getNumeroProcessoCargo();	
		this.situacaoGuiaRecolhimento = c.getSituacaoGuiaRecolhimento();

		salvarPesquisaInicial();
	}
	public ConsultaProcessoVO(ConsultaProcessoTrfSemFiltro c, Integer idTask,Date dataChegada, Long idTaskInstance, String actorId) {
		super();
		this.idProcessoTrf = c.getIdProcessoTrf();
		this.numeroProcesso = c.getNumeroProcesso();
		this.idOrgaoJulgador = c.getIdOrgaoJulgador() == null ? 0 : c.getIdOrgaoJulgador();
		this.orgaoJulgador = c.getOrgaoJulgador() == null ? "" : c.getOrgaoJulgador();
		this.orgaoJulgadorColegiado = c.getOrgaoJulgadorColegiado() == null ? "" : c.getOrgaoJulgadorColegiado();
		this.idOrgaoJulgadorColegiado = c.getIdOrgaoJulgadorColegiado() == null ? 0 : c.getIdOrgaoJulgadorColegiado();
		this.dataAutuacao = c.getDataAutuacao();
		this.autor = c.getAutor();
		this.qtAutor = c.getQtAutor();
		this.reu = c.getReu();
		this.qtReu = c.getQtReu();
		this.processoStatus = c.getProcessoStatus();
		this.segredoJustica = c.getSegredoJustica();
		this.apreciadoSegredo = c.getApreciadoSegredo();
		this.dataDistribuicao = c.getDataDistribuicao();
		this.prioridade = c.getPrioridade();
		//this.prioridadesString = c.getPrioridadesString();
		this.idTask = idTask;
		this.setIdTaskInstance(idTaskInstance);
		this.actorId = actorId;
		this.dataChegada = dataChegada;
		this.consultaProcesso = c;
		this.situacaoGuiaRecolhimento = c.getSituacaoGuiaRecolhimento();

		salvarPesquisaInicial();

	}

	public ConsultaProcessoVO(){
		super();
		salvarPesquisaInicial();
	}
	
	public ConsultaProcessoVO(Integer idJurisdicao) {
		super();
		this.idJurisdicao = idJurisdicao;
		
		this.salvarPesquisaInicial();
	}
	
	public ConsultaProcessoVO(Integer idJurisdicao, Integer idCaixa, ConsultaProcessoVO universoContextoPesquisaVO) {
		super();
		this.idJurisdicao = idJurisdicao;
		this.idCaixaAdvProc = idCaixa;
		this.setUniversoContextoPesquisa(universoContextoPesquisaVO);
		
		this.salvarPesquisaInicial();
	}

	public ConsultaProcessoVO(ConsultaProcessoVO universoContextoPesquisa) {
		super();
		this.setUniversoContextoPesquisa(universoContextoPesquisa);
		this.salvarPesquisaInicial();
	}

	public Boolean getApenasCaixasComResultados() {
		return apenasCaixasComResultados;
	}

	public void setApenasCaixasComResultados(Boolean apenasCaixasComResultados) {
		this.apenasCaixasComResultados = apenasCaixasComResultados;
	}

	public Boolean getApenasCaixasAtivas() {
		return apenasCaixasAtivas;
	}

	public void setApenasCaixasAtivas(Boolean apenasCaixasAtivas) {
		this.apenasCaixasAtivas = apenasCaixasAtivas;
	}

	public void setIdJurisdicao(Integer idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}

	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}
	

	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroSequenciaProcessoPattern() {
		return numeroSequenciaProcessoPattern;
	}

	public void setNumeroSequenciaProcessoPattern(String numeroSequenciaProcessoPattern) {
		this.numeroSequenciaProcessoPattern = numeroSequenciaProcessoPattern;
		this.geraNumeroSequenciaPatterList(numeroSequenciaProcessoPattern);
	}
	
	public List<IntervaloNumeroSequencialProcessoVO> getIntervalosNumerosSequenciais() {
		return intervalosNumerosSequenciais;
	}

	public void setIntervalosNumerosSequenciais(List<IntervaloNumeroSequencialProcessoVO> intervalosNumerosSequenciais) {
		this.intervalosNumerosSequenciais = intervalosNumerosSequenciais;
	}

	/**
	 * Dada uma string com uma expressão regular, gera uma lista VOs de intervalo de número sequenciais
	 * 
	 * @return
	 */
	private void geraNumeroSequenciaPatterList(String pattern) {
		this.intervalosNumerosSequenciais = new ArrayList<IntervaloNumeroSequencialProcessoVO>(0);
		if(StringUtil.isNotEmpty(pattern)){
			List<String> intervalos = new CopyOnWriteArrayList<String>(Arrays.asList(pattern.split(";")));
			for (String intervalo : intervalos) {
				if (intervalo.length()>1){
					String[] strRange = intervalo.split("-");
					if(strRange.length==1) {
						// quando o intervalo informado só tem um número, o valor inicial e o final serão o mesmo valor
						this.intervalosNumerosSequenciais.add(new IntervaloNumeroSequencialProcessoVO(strRange[0].length(), Integer.parseInt(strRange[0]), Integer.parseInt(strRange[0])));
					}
					if (strRange.length==2){
						// não é possível indicar intervalos quando o número inicial tem quantidade de caractéres diferente do número final
						if(strRange[0].length() == strRange[1].length()) {
							this.intervalosNumerosSequenciais.add(new IntervaloNumeroSequencialProcessoVO(strRange[0].length(), Integer.parseInt(strRange[0]), Integer.parseInt(strRange[1])));
						}
					}
				}
			}
		}
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	public String getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(String orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(String orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public Integer getIdOrgaoJulgadorColegiado() {
		return idOrgaoJulgadorColegiado;
	}

	public void setIdOrgaoJulgadorColegiado(Integer idOrgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}
	
	public void setIdJurisdicao(int idJurisdicao) {
		this.idJurisdicao = idJurisdicao;
	}
	
	public Integer getIdJurisdicao() {
		return idJurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}
	
	public ClasseJudicial getClasseJudicialObj() {
		return classeJudicialObj;
	}
	
	public List<ClasseJudicial> getClasseJudicialList() {
		return classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public String getAssuntoJudicial() {
		return assuntoJudicial;
	}

	public void setAssuntoJudicial(String assuntoJudicial) {
		if(assuntoJudicial != null && !StringUtil.fullTrim(assuntoJudicial).isEmpty()){
			this.assuntoJudicial = assuntoJudicial;
		}
	}

	public Assunto getAssuntoJudicialObj() {
		return assuntoJudicialObj;
	}

	public void setAssuntoJudicialObj(Assunto assuntoJudicialObj) {
		this.assuntoJudicialObj = assuntoJudicialObj;
	}

	public List<AssuntoTrf> getAssuntoTrfList() {
		return assuntoTrfList;
	}

	public void setAssuntoTrfList(List<AssuntoTrf> assuntoTrfList) {
		this.assuntoTrfList = assuntoTrfList;
	}

	public Boolean getApenasPrioridade() {
		return apenasPrioridade;
	}

	public void setApenasPrioridade(Boolean apenasPrioridade) {
		this.apenasPrioridade = apenasPrioridade;
	}

	public PrioridadeProcesso getPrioridadeObj() {
		return prioridadeObj;
	}

	public void setPrioridadeObj(PrioridadeProcesso prioridadeObj) {
		this.prioridadeObj = prioridadeObj;
	}

	public OrgaoJulgador getOrgaoJulgadorObj() {
		return orgaoJulgadorObj;
	}

	public void setOrgaoJulgadorObj(OrgaoJulgador orgaoJulgadorObj) {
		this.orgaoJulgadorObj = orgaoJulgadorObj;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiadoObj() {
		return orgaoJulgadorColegiadoObj;
	}

	public void setOrgaoJulgadorColegiadoObj(OrgaoJulgadorColegiado orgaoJulgadorColegiadoObj) {
		this.orgaoJulgadorColegiadoObj = orgaoJulgadorColegiadoObj;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Date getDataNascimentoInicial() {
		return dataNascimentoInicial;
	}

	public void setDataNascimentoInicial(Date dataNascimentoInicial) {
		this.dataNascimentoInicial = dataNascimentoInicial;
	}
	
	public Date getDataNascimentoFinal() {
		return dataNascimentoFinal;
	}

	public void setDataNascimentoFinal(Date dataNascimentoFinal) {
		this.dataNascimentoFinal = DateUtil.getEndOfDay(dataNascimentoFinal);
	}
	
	public String getOabRepresentanteParte() {
		return oabRepresentanteParte;
	}
	public void setOabRepresentanteParte(String oabRepresentanteParte) {
		if(oabRepresentanteParte != null && !StringUtil.fullTrim(oabRepresentanteParte).isEmpty()){
			this.oabRepresentanteParte = StringUtil.fullTrim(oabRepresentanteParte);
		}
	}
	
	public void setDocumentoIdentificacaoParte(String documentoIdentificacao) {
		if(documentoIdentificacao == null || StringUtil.fullTrim(documentoIdentificacao).isEmpty()){
			return;
		}
		String insc = documentoIdentificacao.replaceAll("\\D", "");
		if(insc != null && !insc.isEmpty()){
			if(insc.length() == 11 && InscricaoMFUtil.verificaCPF(insc)){
				this.setCpfParte(insc);
			}else if(insc.length() == 14 && InscricaoMFUtil.verificaCNPJ(insc)){
				this.setCnpjParte(insc);
			}else {
				this.setOutroDocumentoParte(StringUtil.fullTrim(documentoIdentificacao));
			}
		}
	}

	public String getCpfParte() {
		return cpfParte;
	}

	public void setCpfParte(String cpfParte) {
		if(cpfParte != null && !StringUtil.fullTrim(cpfParte).isEmpty()){
			String cpf = StringUtil.fullTrim(cpfParte).replaceAll("\\D", "");
		
			if(cpf.length() == 11 && InscricaoMFUtil.verificaCPF(cpf)){
				this.cpfParte = InscricaoMFUtil.acrescentaMascaraCPF(cpf);
			}
		}
	}

	public String getCnpjParte() {
		return cnpjParte;
	}

	public void setCnpjParte(String cnpjParte) {
		if(cnpjParte != null && !StringUtil.fullTrim(cnpjParte).isEmpty()){
			String cnpj = StringUtil.fullTrim(cnpjParte).replaceAll("\\D", "");
		
			if(cnpj.length() == 14 && InscricaoMFUtil.verificaCNPJ(cnpj)){
				this.cnpjParte = InscricaoMFUtil.mascaraCnpj(cnpj);
			}
		}
	}
	
	public String getOutroDocumentoParte() {
		return outroDocumentoParte;
	}

	public void setOutroDocumentoParte(String outroDocumentoParte) {
		if(outroDocumentoParte != null && !StringUtil.fullTrim(outroDocumentoParte).isEmpty()){
			this.outroDocumentoParte = StringUtil.fullTrim(outroDocumentoParte);
		}
	}
	public Date getDataDistribuicaoInicial() {
		return dataDistribuicaoInicial;
	}


	public void setIntervaloDataDistribuicao(Date dataDistribuicaoInicial, Date dataDistribuicaoFinal) {
		this.setDataDistribuicaoInicial(dataDistribuicaoInicial);
		this.setDataDistribuicaoFinal(dataDistribuicaoFinal);
	}
	
	public void setDataDistribuicaoInicial(Date dataDistribuicaoInicial) {
		this.dataDistribuicaoInicial = dataDistribuicaoInicial;
	}

	public Date getDataDistribuicaoFinal() {
		return dataDistribuicaoFinal;
	}

	public void setDataDistribuicaoFinal(Date dataDistribuicaoFinal) {
		if(dataDistribuicaoFinal != null){
			this.dataDistribuicaoFinal = DateUtil.getEndOfDay(dataDistribuicaoFinal);
		}
	}

	public Date getDataAutuacaoInicial() {
		return dataAutuacaoInicial;
	}

	public void setDataAutuacaoInicial(Date dataAutuacaoInicial) {
		this.dataAutuacaoInicial = dataAutuacaoInicial;
	}

	public Date getDataAutuacaoFinal() {
		return dataAutuacaoFinal;
	}

	public void setDataAutuacaoFinal(Date dataAutuacaoFinal) {
		this.dataAutuacaoFinal = DateUtil.getEndOfDay(dataAutuacaoFinal);
	}

	public void setClasseJudicialObj(ClasseJudicial classeJudicialObj) {
		this.classeJudicialObj = classeJudicialObj;
	}

	public Date getDataChegada() {
		return dataChegada;
	}

	public void setDataChegada(Date dataChegada) {
		this.dataChegada = dataChegada;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		if(classeJudicial != null && !StringUtil.fullTrim(classeJudicial).isEmpty()){
			this.classeJudicial = classeJudicial;
		}
	}

	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public long getQtAutor() {
		return qtAutor;
	}

	public void setQtAutor(long qtAutor) {
		this.qtAutor = qtAutor;
	}

	public String getReu() {
		return reu;
	}

	public void setReu(String reu) {
		this.reu = reu;
	}

	public long getQtReu() {
		return qtReu;
	}

	public void setQtReu(long qtReu) {
		this.qtReu = qtReu;
	}

	public ProcessoTrf getProcessoTrf() {
		
		if(processoTrf == null && getConsultaProcesso() != null) {
			processoTrf = getConsultaProcesso().getProcessoTrf();
		}
		return processoTrf;

	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoStatusEnum getProcessoStatus() {
		return this.processoStatus;
	}

	public void setProcessoStatus(ProcessoStatusEnum processoStatus) {
		this.processoStatus = processoStatus;
	}

	public Boolean getSegredoJustica() {
		return this.segredoJustica;
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	public ProcessoTrfApreciadoEnum getApreciadoSegredo() {
		return apreciadoSegredo;
	}

	public void setApreciadoSegredo(ProcessoTrfApreciadoEnum apreciadoSegredo) {
		this.apreciadoSegredo = apreciadoSegredo;
	}

	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	public Boolean getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(Boolean prioridade) {
		this.prioridade = prioridade;
	}

	public void setPrioridadesString(String prioridadesString) {
		this.prioridadesString = prioridadesString;
	}

	public String getPrioridadesString() {
		return prioridadesString;
	}

	public void setIdTask(Integer idTask) {
		this.idTask = idTask;
	}

	public Integer getIdTask() {
		return idTask;
	}

	public Long getIdTaskInstance() {
		return idTaskInstance;
	}

	public void setIdTaskInstance(Long idTaskInstance) {
		this.idTaskInstance = idTaskInstance;
	}

	public void setNumeroProcessoCargo(String numeroProcessoCargo) {
		this.numeroProcessoCargo = numeroProcessoCargo;
	}

	public String getNumeroProcessoCargo() {
		return numeroProcessoCargo;
	}

	public String getActorId() {
		return actorId;
	}
	
	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public Boolean getConferido() {
		return conferido;
	}

	public void setConferido(Boolean conferido) {
		this.conferido = conferido;
	}

	public DocumentoAssinaturaVO getDocumentoAssinatura() {
		return documentoAssinatura;
	}

	public void setDocumentoAssinatura(DocumentoAssinaturaVO documentoAssinatura) {
		this.documentoAssinatura = documentoAssinatura;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getNumeroAno() {
		return numeroAno;
	}

	public void setNumeroAno(Integer numeroAno) {
		this.numeroAno = numeroAno;
	}

	public Integer getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(Integer ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public Integer getNumeroOrgaoJustica() {
		return numeroOrgaoJustica;
	}

	public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
		this.numeroOrgaoJustica = numeroOrgaoJustica;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public List<String> getTagsList(){
		if(StringUtil.isEmpty(getTags())){
			return new ArrayList<String>(0);
		}
		return Arrays.asList(getTags().split(","));
	}
	
	public ConsultaProcessoTrfSemFiltro getConsultaProcesso() {
		return consultaProcesso;
	}
	public Integer getIdCaixaAdvProc() {
		return idCaixaAdvProc;
	}
	public void setIdCaixaAdvProc(Integer idCaixaAdvProc) {
		if(idCaixaAdvProc != null) {
			apenasSemCaixa = false;
		}
		this.idCaixaAdvProc = idCaixaAdvProc;
	}
	public Boolean getApenasSemCaixa() {
		if(apenasSemCaixa == null) {
			apenasSemCaixa = false;
		}
		return apenasSemCaixa;
	}
	public void setApenasSemCaixa(Boolean apenasSemCaixa) {
		if(apenasSemCaixa) {
			this.idCaixaAdvProc = null;
		}
		this.apenasSemCaixa = apenasSemCaixa;
	}
	
	private void salvarPesquisaInicial() {
		mapaAtributos = new HashMap<Field, Object>(0);
		for(Field f: ConsultaProcessoVO.class.getDeclaredFields()){
			try {
				mapaAtributos.put(f, f.get(this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isPesquisaAlterada() {
		for (Field field : mapaAtributos.keySet()) {
			try {
				Object value = field.get(this);
				if(value != null) {
					if(!value.equals(mapaAtributos.get(field))) {
						return true;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public ConsultaProcessoVO getUniversoContextoPesquisa() {
		return universoContextoPesquisa;
	}
	
	public void setUniversoContextoPesquisa(ConsultaProcessoVO universoContextoPesquisa) {
		this.universoContextoPesquisa = universoContextoPesquisa;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apenasCaixasAtivas == null) ? 0 : apenasCaixasAtivas.hashCode());
		result = prime * result + ((apenasCaixasComResultados == null) ? 0 : apenasCaixasComResultados.hashCode());
		result = prime * result + ((apenasSemCaixa == null) ? 0 : apenasSemCaixa.hashCode());
		result = prime * result + ((apreciadoSegredo == null) ? 0 : apreciadoSegredo.hashCode());
		result = prime * result + ((autor == null) ? 0 : autor.hashCode());
		result = prime * result + ((idCaixaAdvProc == null) ? 0 : idCaixaAdvProc.hashCode());
		result = prime * result + ((idJurisdicao == null) ? 0 : idJurisdicao.hashCode());
		result = prime * result + ((classeJudicial == null) ? 0 : classeJudicial.hashCode());
		result = prime * result + ((conferido == null) ? 0 : conferido.hashCode());
		result = prime * result + ((consultaProcesso == null) ? 0 : consultaProcesso.hashCode());
		result = prime * result + ((dataAutuacao == null) ? 0 : dataAutuacao.hashCode());
		result = prime * result + ((dataChegada == null) ? 0 : dataChegada.hashCode());
		result = prime * result + ((dataDistribuicao == null) ? 0 : dataDistribuicao.hashCode());
		result = prime * result + ((idOrgaoJulgador == null) ? 0 : idOrgaoJulgador.hashCode());
		result = prime * result + ((idOrgaoJulgadorColegiado == null) ? 0 : idOrgaoJulgadorColegiado.hashCode());
		result = prime * result + idProcessoTrf;
		result = prime * result + ((idTask == null) ? 0 : idTask.hashCode());
		result = prime * result + ((idTaskInstance == null) ? 0 : idTaskInstance.hashCode());
		result = prime * result + ((jurisdicao == null) ? 0 : jurisdicao.hashCode());
		result = prime * result + ((nomeParte == null) ? 0 : nomeParte.hashCode());
		result = prime * result + ((numeroProcesso == null) ? 0 : numeroProcesso.hashCode());

		result = prime * result + ((numeroSequencia == null) ? 0 : numeroSequencia.hashCode());
		result = prime * result + ((digitoVerificador == null) ? 0 : digitoVerificador.hashCode());
		result = prime * result + ((numeroAno == null) ? 0 : numeroAno.hashCode());
		result = prime * result + ((ramoJustica == null) ? 0 : ramoJustica.hashCode());
		result = prime * result + ((numeroOrgaoJustica == null) ? 0 : numeroOrgaoJustica.hashCode());
		result = prime * result + ((numeroOrigem == null) ? 0 : numeroOrigem.hashCode());
		
		result = prime * result + ((numeroProcessoCargo == null) ? 0 : numeroProcessoCargo.hashCode());
		result = prime * result + ((oabRepresentanteParte == null) ? 0 : oabRepresentanteParte.hashCode());
		result = prime * result + ((orgaoJulgador == null) ? 0 : orgaoJulgador.hashCode());
		result = prime * result + ((orgaoJulgadorColegiado == null) ? 0 : orgaoJulgadorColegiado.hashCode());
		result = prime * result + ((prioridade == null) ? 0 : prioridade.hashCode());
		result = prime * result + ((prioridadesString == null) ? 0 : prioridadesString.hashCode());
		result = prime * result + ((processoStatus == null) ? 0 : processoStatus.hashCode());
		result = prime * result + (int) (qtAutor ^ (qtAutor >>> 32));
		result = prime * result + (int) (qtReu ^ (qtReu >>> 32));
		result = prime * result + ((reu == null) ? 0 : reu.hashCode());
		result = prime * result + ((segredoJustica == null) ? 0 : segredoJustica.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((universoContextoPesquisa == null) ? 0 : universoContextoPesquisa.hashCode());

		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsultaProcessoVO other = (ConsultaProcessoVO) obj;
		
		if (apenasCaixasAtivas == null) {
			if (other.apenasCaixasAtivas != null)
				return false;
		} else if (!apenasCaixasAtivas.equals(other.apenasCaixasAtivas))
			return false;
		if (apenasCaixasComResultados == null) {
			if (other.apenasCaixasComResultados != null)
				return false;
		} else if (!apenasCaixasComResultados.equals(other.apenasCaixasComResultados))
			return false;
		if (apenasSemCaixa == null) {
			if (other.apenasSemCaixa != null)
				return false;
		} else if (!apenasSemCaixa.equals(other.apenasSemCaixa))
			return false;
		if (apreciadoSegredo != other.apreciadoSegredo)
			return false;
		if (autor == null) {
			if (other.autor != null)
				return false;
		} else if (!autor.equals(other.autor))
			return false;
		if (dataNascimentoInicial == null) {
			if (other.dataNascimentoInicial != null)
				return false;
		} else if (!dataNascimentoInicial.equals(other.dataNascimentoInicial))
			return false;
		if (dataNascimentoFinal == null) {
			if (other.dataNascimentoFinal != null)
				return false;
		} else if (!dataNascimentoFinal.equals(other.dataNascimentoFinal))
			return false;
		if (idCaixaAdvProc == null) {
			if (other.idCaixaAdvProc != null)
				return false;
		} else if (!idCaixaAdvProc.equals(other.idCaixaAdvProc))
			return false;
		if (classeJudicial == null) {
			if (other.classeJudicial != null)
				return false;
		} else if (!classeJudicial.equals(other.classeJudicial))
			return false;
		if (conferido == null) {
			if (other.conferido != null)
				return false;
		} else if (!conferido.equals(other.conferido))
			return false;
		if (consultaProcesso == null) {
			if (other.consultaProcesso != null)
				return false;
		} else if (!consultaProcesso.equals(other.consultaProcesso))
			return false;
		if (dataAutuacao == null) {
			if (other.dataAutuacao != null)
				return false;
		} else if (!dataAutuacao.equals(other.dataAutuacao))
			return false;
		if (dataChegada == null) {
			if (other.dataChegada != null)
				return false;
		} else if (!dataChegada.equals(other.dataChegada))
			return false;
		if (dataDistribuicao == null) {
			if (other.dataDistribuicao != null)
				return false;
		} else if (!dataDistribuicao.equals(other.dataDistribuicao))
			return false;
		if (idOrgaoJulgador == null) {
			if (other.idOrgaoJulgador != null)
				return false;
		} else if (!idOrgaoJulgador.equals(other.idOrgaoJulgador))
			return false;
		if (idOrgaoJulgadorColegiado == null) {
			if (other.idOrgaoJulgadorColegiado != null)
				return false;
		} else if (!idOrgaoJulgadorColegiado.equals(other.idOrgaoJulgadorColegiado))
			return false;
		if (idProcessoTrf != other.idProcessoTrf)
			return false;
		if (idTask == null) {
			if (other.idTask != null)
				return false;
		} else if (!idTask.equals(other.idTask))
			return false;
		if (idTaskInstance == null) {
			if (other.idTaskInstance != null)
				return false;
		} else if (!idTaskInstance.equals(other.idTaskInstance))
			return false;
		if (jurisdicao == null) {
			if (other.jurisdicao != null)
				return false;
		} else if (!jurisdicao.equals(other.jurisdicao))
			return false;
		if (idJurisdicao == null) {
			if (other.idJurisdicao != null)
				return false;
		} else if (!idJurisdicao.equals(other.idJurisdicao))
			return false;

		if (nomeParte == null) {
			if (other.nomeParte != null)
				return false;
		} else if (!nomeParte.equals(other.nomeParte))
			return false;
		if (numeroProcesso == null) {
			if (other.numeroProcesso != null)
				return false;
		} else if (!numeroProcesso.equals(other.numeroProcesso))
			return false;

		if (numeroSequencia == null) {
			if (other.numeroSequencia != null)
				return false;
		} else if (!numeroSequencia.equals(other.numeroSequencia))
			return false;
		if (digitoVerificador == null) {
			if (other.digitoVerificador != null)
				return false;
		} else if (!digitoVerificador.equals(other.digitoVerificador))
			return false;
		if (numeroAno == null) {
			if (other.numeroAno != null)
				return false;
		} else if (!numeroAno.equals(other.numeroAno))
			return false;
		if (ramoJustica == null) {
			if (other.ramoJustica != null)
				return false;
		} else if (!ramoJustica.equals(other.ramoJustica))
			return false;
		if (numeroOrgaoJustica == null) {
			if (other.numeroOrgaoJustica != null)
				return false;
		} else if (!numeroOrgaoJustica.equals(other.numeroOrgaoJustica))
			return false;
		if (numeroOrigem == null) {
			if (other.numeroOrigem != null)
				return false;
		} else if (!numeroOrigem.equals(other.numeroOrigem))
			return false;
		
		if (numeroProcessoCargo == null) {
			if (other.numeroProcessoCargo != null)
				return false;
		} else if (!numeroProcessoCargo.equals(other.numeroProcessoCargo))
			return false;
		if (oabRepresentanteParte == null) {
			if(other.oabRepresentanteParte != null)
				return false;
		} else if (!oabRepresentanteParte.equals(other.oabRepresentanteParte))
			return false;
		if (orgaoJulgador == null) {
			if (other.orgaoJulgador != null)
				return false;
		} else if (!orgaoJulgador.equals(other.orgaoJulgador))
			return false;
		if (orgaoJulgadorColegiado == null) {
			if (other.orgaoJulgadorColegiado != null)
				return false;
		} else if (!orgaoJulgadorColegiado.equals(other.orgaoJulgadorColegiado))
			return false;
		if (prioridade == null) {
			if (other.prioridade != null)
				return false;
		} else if (!prioridade.equals(other.prioridade))
			return false;
		if (prioridadesString == null) {
			if (other.prioridadesString != null)
				return false;
		} else if (!prioridadesString.equals(other.prioridadesString))
			return false;
		if (processoStatus != other.processoStatus)
			return false;
		if (qtAutor != other.qtAutor)
			return false;
		if (qtReu != other.qtReu)
			return false;
		if (reu == null) {
			if (other.reu != null)
				return false;
		} else if (!reu.equals(other.reu))
			return false;
		if (segredoJustica == null) {
			if (other.segredoJustica != null)
				return false;
		} else if (!segredoJustica.equals(other.segredoJustica))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (universoContextoPesquisa == null) {
			if (other.universoContextoPesquisa != null)
				return false;
		} else if (!universoContextoPesquisa.equals(other.universoContextoPesquisa))
			return false;
		
		return true;
	}

	public List<String> getNumerosProcessos() {
		return numerosProcessos;
	}

	public void setNumerosProcessos(List<String> numerosProcessosTemporario) {
		this.numerosProcessos = numerosProcessosTemporario;
	}

	public SituacaoGuiaRecolhimentoEnum getSituacaoGuiaRecolhimento() {
		return situacaoGuiaRecolhimento;
	}

	public void setSituacaoGuiaRecolhimento(SituacaoGuiaRecolhimentoEnum situacaoGuiaRecolhimento) {
		this.situacaoGuiaRecolhimento = situacaoGuiaRecolhimento;
	}

	public boolean isExibirSituacaoGuia() {
		return situacaoGuiaRecolhimento != null;
	}
}