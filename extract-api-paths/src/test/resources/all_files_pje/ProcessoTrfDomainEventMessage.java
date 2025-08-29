package br.jus.cnj.pje.amqp.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;

/**
 * Classe que representa os dados do processo que são enviados para o RabbitMQ.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoTrfDomainEventMessage implements CloudEventPayload<ProcessoTrfDomainEventMessage, ProcessoTrf> {
	private int idProcessoTrf;
	private Boolean segredoJustica;
	private Boolean justicaGratuita;
	private Boolean incidental = Boolean.FALSE;
	private ClasseJudicialCloudEvent classeJudicial;
	private ClasseJudicialInicialEnum classeJudicialInicial;
	private Date dataAutuacao;
	private Date dataTransitadoJulgado;
	private Date dataDistribuicao;
	private Double valorCausa;
	private int nivelAcesso;
	private JurisdicaoDomainEventMessage jurisdicao;
	private OrgaoJulgadorDomainEventMessage orgaoJulgador;
	private OrgaoJulgadorColegiadoDomainEventMessage orgaoJulgadorColegiado;
	private PessoaDomainEventMessage pessoaRelator;
	private String status;
	private String observacaoSegredo;
	private String prioridades;
	private String numeroProcesso;
	private Character instancia;
	private CompetenciaCloudEvent competencia;
	private List<ProcessoParteDomainEventMessage> listaParteAtivo = new ArrayList<>();
	private List<ProcessoParteDomainEventMessage> listaPartePassivo = new ArrayList<>();
	private List<ProcessoParteDomainEventMessage> listaParteTerceiro = new ArrayList<>();
	private List<ProcessoParteDomainEventMessage> listaFiscal = new ArrayList<>();
	private List<AssuntoTrfDomainEventMessage> listaAssunto = new ArrayList<>();

	/**
	 * Construtor.
	 *
	 * @param processo
	 */
	public ProcessoTrfDomainEventMessage(ProcessoTrf processo) {
		if (processo != null) {
			setIdProcessoTrf(processo.getIdProcessoTrf());
			setSegredoJustica(processo.getSegredoJustica());
			setJusticaGratuita(processo.getJusticaGratuita());
			setIncidental(processo.getIsIncidente());
			setClasseJudicial(new ClasseJudicialCloudEvent(processo.getClasseJudicial()));
			setClasseJudicialInicial(processo.getInicial());
			setDataAutuacao(processo.getDataAutuacao());
			setDataTransitadoJulgado(processo.getDtTransitadoJulgado());
			setDataDistribuicao(processo.getDataDistribuicao());
			setValorCausa(processo.getValorCausa());
			setNivelAcesso(processo.getNivelAcesso());
			setJurisdicao(new JurisdicaoDomainEventMessage(processo.getJurisdicao()));
			setOrgaoJulgador(new OrgaoJulgadorDomainEventMessage(processo.getOrgaoJulgador()));
			setOrgaoJulgadorColegiado(new OrgaoJulgadorColegiadoDomainEventMessage(processo.getOrgaoJulgadorColegiado()));
			setPessoaRelator(new PessoaDomainEventMessage(processo.getPessoaRelator()));
			setStatus(processo.getProcessoStatus().getLabel());
			setObservacaoSegredo(processo.getObservacaoSegredo());
			setPrioridades(processo.getPrioridadesString());
			setNumeroProcesso(processo.getNumeroProcesso());
			setInstancia(processo.getInstancia());
			setCompetencia(new CompetenciaCloudEvent(processo.getCompetencia()));
			setListaParteAtivo(processo.getListaParteAtivo());
			setListaPartePassivo(processo.getListaPartePassivo());
			setListaParteTerceiro(processo.getListaParteTerceiro());
			setListaFiscal(processo.getListaFiscal());
			setListaAssunto(processo.getAssuntoTrfList());
		}
	}

	@Override
	public ProcessoTrfDomainEventMessage convertEntityToPayload(ProcessoTrf entity) {
		return new ProcessoTrfDomainEventMessage(entity);
	}

	@Override
	public Long getId(ProcessoTrf entity) {
		return (entity != null ? Long.valueOf(entity.getIdProcessoTrf()) : null);
	}

	/**
	 * @return the idProcessoTrf
	 */
	public int getIdProcessoTrf() {
		return idProcessoTrf;
	}

	/**
	 * @param idProcessoTrf the idProcessoTrf to set
	 */
	public void setIdProcessoTrf(int idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}

	/**
	 * @return the segredoJustica
	 */
	public Boolean getSegredoJustica() {
		return segredoJustica;
	}

	/**
	 * @param segredoJustica the segredoJustica to set
	 */
	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	/**
	 * @return the justicaGratuita
	 */
	public Boolean getJusticaGratuita() {
		return justicaGratuita;
	}

	/**
	 * @param justicaGratuita the justicaGratuita to set
	 */
	public void setJusticaGratuita(Boolean justicaGratuita) {
		this.justicaGratuita = justicaGratuita;
	}

	/**
	 * @return the incidental
	 */
	public Boolean getIncidental() {
		return incidental;
	}

	/**
	 * @param incidental the incidental to set
	 */
	public void setIncidental(Boolean incidental) {
		this.incidental = incidental;
	}

	/**
	 * @return the classeJudicial
	 */
	public ClasseJudicialCloudEvent getClasseJudicial() {
		return classeJudicial;
	}

	/**
	 * @param classeJudicial the classeJudicial to set
	 */
	public void setClasseJudicial(ClasseJudicialCloudEvent classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	/**
	 * @return the inicial
	 */
	public ClasseJudicialInicialEnum getClasseJudicialInicial() {
		return classeJudicialInicial;
	}

	/**
	 * @param inicial the inicial to set
	 */
	public void setClasseJudicialInicial(ClasseJudicialInicialEnum inicial) {
		this.classeJudicialInicial = inicial;
	}

	/**
	 * @return the dataAutuacao
	 */
	public Date getDataAutuacao() {
		return dataAutuacao;
	}

	/**
	 * @param dataAutuacao the dataAutuacao to set
	 */
	public void setDataAutuacao(Date dataAutuacao) {
		this.dataAutuacao = dataAutuacao;
	}

	/**
	 * @return the dtTransitadoJulgado
	 */
	public Date getDataTransitadoJulgado() {
		return dataTransitadoJulgado;
	}

	/**
	 * @param dtTransitadoJulgado the dtTransitadoJulgado to set
	 */
	public void setDataTransitadoJulgado(Date dtTransitadoJulgado) {
		this.dataTransitadoJulgado = dtTransitadoJulgado;
	}

	/**
	 * @return the dataDistribuicao
	 */
	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	/**
	 * @param dataDistribuicao the dataDistribuicao to set
	 */
	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	/**
	 * @return the valorCausa
	 */
	public Double getValorCausa() {
		return valorCausa;
	}

	/**
	 * @param valorCausa the valorCausa to set
	 */
	public void setValorCausa(Double valorCausa) {
		this.valorCausa = valorCausa;
	}

	/**
	 * @return the nivelAcesso
	 */
	public int getNivelAcesso() {
		return nivelAcesso;
	}

	/**
	 * @param nivelAcesso the nivelAcesso to set
	 */
	public void setNivelAcesso(int nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

	/**
	 * @return the jurisdicao
	 */
	public JurisdicaoDomainEventMessage getJurisdicao() {
		return jurisdicao;
	}

	/**
	 * @param jurisdicao the jurisdicao to set
	 */
	public void setJurisdicao(JurisdicaoDomainEventMessage jurisdicao) {
		this.jurisdicao = jurisdicao;
	}

	/**
	 * @return the orgaoJulgador
	 */
	public OrgaoJulgadorDomainEventMessage getOrgaoJulgador() {
		return orgaoJulgador;
	}

	/**
	 * @param orgaoJulgador the orgaoJulgador to set
	 */
	public void setOrgaoJulgador(OrgaoJulgadorDomainEventMessage orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	/**
	 * @return the orgaoJulgadorColegiado
	 */
	public OrgaoJulgadorColegiadoDomainEventMessage getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	/**
	 * @param orgaoJulgadorColegiado the orgaoJulgadorColegiado to set
	 */
	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiadoDomainEventMessage orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	/**
	 * @return the pessoaRelator
	 */
	public PessoaDomainEventMessage getPessoaRelator() {
		return pessoaRelator;
	}

	/**
	 * @param pessoaRelator the pessoaRelator to set
	 */
	public void setPessoaRelator(PessoaDomainEventMessage pessoaRelator) {
		this.pessoaRelator = pessoaRelator;
	}

	/**
	 * @return the processoStatus
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param processoStatus the processoStatus to set
	 */
	public void setStatus(String processoStatus) {
		this.status = processoStatus;
	}

	/**
	 * @return the observacaoSegredo
	 */
	public String getObservacaoSegredo() {
		return observacaoSegredo;
	}

	/**
	 * @param observacaoSegredo the observacaoSegredo to set
	 */
	public void setObservacaoSegredo(String observacaoSegredo) {
		this.observacaoSegredo = observacaoSegredo;
	}

	/**
	 * @return the prioridadesString
	 */
	public String getPrioridades() {
		return prioridades;
	}

	/**
	 * @param prioridadesString the prioridadesString to set
	 */
	public void setPrioridades(String prioridadesString) {
		this.prioridades = prioridadesString;
	}

	/**
	 * @return the numeroProcesso
	 */
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	/**
	 * @param numeroProcesso the numeroProcesso to set
	 */
	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	/**
	 * @return the instancia
	 */
	public Character getInstancia() {
		return instancia;
	}

	/**
	 * @param instancia the instancia to set
	 */
	public void setInstancia(Character instancia) {
		this.instancia = instancia;
	}

	/**
	 * @return the competencia
	 */
	public CompetenciaCloudEvent getCompetencia() {
		return competencia;
	}

	/**
	 * @param competencia the competencia to set
	 */
	public void setCompetencia(CompetenciaCloudEvent competencia) {
		this.competencia = competencia;
	}

	/**
	 * @return the listaParteAtivo
	 */
	public List<ProcessoParteDomainEventMessage> getListaParteAtivo() {
		return listaParteAtivo;
	}

	/**
	 * @param listaParteAtivo the listaParteAtivo to set
	 */
	public void setListaParteAtivoCloudEvent(List<ProcessoParteDomainEventMessage> listaParteAtivo) {
		this.listaParteAtivo = listaParteAtivo;
	}
	
	/**
	 * @param listaParteAtivo the listaParteAtivo to set
	 */
	public void setListaParteAtivo(List<ProcessoParte> listaParteAtivo) {
		for (ProcessoParte parte : listaParteAtivo) {
			getListaParteAtivo().add(new ProcessoParteDomainEventMessage(parte));
		}
	}

	/**
	 * @return the listaPartePassivo
	 */
	public List<ProcessoParteDomainEventMessage> getListaPartePassivo() {
		return listaPartePassivo;
	}

	/**
	 * @param listaPartePassivo the listaPartePassivo to set
	 */
	public void setListaPartePassivoCloudEvent(List<ProcessoParteDomainEventMessage> listaPartePassivo) {
		this.listaPartePassivo = listaPartePassivo;
	}
	
	/**
	 * @param listaPartePassivo the listaPartePassivo to set
	 */
	public void setListaPartePassivo(List<ProcessoParte> listaPartePassivo) {
		for (ProcessoParte parte : listaPartePassivo) {
			getListaPartePassivo().add(new ProcessoParteDomainEventMessage(parte));
		}
	}

	/**
	 * @return the listaParteTerceiro
	 */
	public List<ProcessoParteDomainEventMessage> getListaParteTerceiro() {
		return listaParteTerceiro;
	}

	/**
	 * @param listaParteTerceiro the listaParteTerceiro to set
	 */
	public void setListaParteTerceiroCloudEvent(List<ProcessoParteDomainEventMessage> listaParteTerceiro) {
		this.listaParteTerceiro = listaParteTerceiro;
	}
	
	/**
	 * @param listaParteTerceiro the listaParteTerceiro to set
	 */
	public void setListaParteTerceiro(List<ProcessoParte> listaParteTerceiro) {
		for (ProcessoParte parte : listaParteTerceiro) {
			getListaParteTerceiro().add(new ProcessoParteDomainEventMessage(parte));
		}
	}

	/**
	 * @return the listaFiscal
	 */
	public List<ProcessoParteDomainEventMessage> getListaFiscal() {
		return listaFiscal;
	}

	/**
	 * @param listaFiscal the listaFiscal to set
	 */
	public void setListaFiscalCloudEvent(List<ProcessoParteDomainEventMessage> listaFiscal) {
		this.listaFiscal = listaFiscal;
	}
	
	/**
	 * @param listaFiscal the listaFiscal to set
	 */
	public void setListaFiscal(List<ProcessoParte> listaFiscal) {
		for (ProcessoParte parte : listaFiscal) {
			getListaFiscal().add(new ProcessoParteDomainEventMessage(parte));
		}
	}

	/**
	 * @return the assuntoTrfList
	 */
	public List<AssuntoTrfDomainEventMessage> getListaAssunto() {
		return listaAssunto;
	}

	/**
	 * @param assuntoTrfList the assuntoTrfList to set
	 */
	public void setListaAssuntoCloudEvent(List<AssuntoTrfDomainEventMessage> assuntoTrfList) {
		this.listaAssunto = assuntoTrfList;
	}
	
	/**
	 * @param assuntoTrfList the assuntoTrfList to set
	 */
	public void setListaAssunto(List<AssuntoTrf> assuntoTrfList) {
		for (AssuntoTrf assunto : assuntoTrfList) {
			getListaAssunto().add(new AssuntoTrfDomainEventMessage(assunto));
		}
	}

}
