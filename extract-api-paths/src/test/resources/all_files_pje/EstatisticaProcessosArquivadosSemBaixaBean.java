package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * arquivados sem baixa
 * 
 * @author Silas Álvares
 * 
 */
public class EstatisticaProcessosArquivadosSemBaixaBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5420173258338399250L;
	private ProcessoTrf processoTrf;
	private ClasseJudicial classeJudicial;
	private Date dataEvento;
	private String dataEventoStr;
	private String eventoProcessual;
	private String partes;
	private Date dataDesarquivamento;
	private String dataDesarquivamentoStr;
	private String entidade;

	public EstatisticaProcessosArquivadosSemBaixaBean() {
	}

	public EstatisticaProcessosArquivadosSemBaixaBean(ProcessoTrf processoTrf, Date dataEvento,
			Date dataDesarquivamento, String eventoProcessual) {
		setProcessoTrf(processoTrf);
		setClasseJudicial(processoTrf.getClasseJudicial());
		setDataEvento(dataEvento);
		setEventoProcessual(eventoProcessual);
		setDataDesarquivamento(dataDesarquivamento);
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public Date getDataEvento() {
		return dataEvento;
	}

	public void setDataEvento(Date dataEvento) {
		this.dataEvento = dataEvento;
	}

	public String getDataEventoStr() {
		return dataEventoStr;
	}

	public void setDataEventoStr(String dataEventoStr) {
		this.dataEventoStr = dataEventoStr;
	}

	public String getEventoProcessual() {
		return eventoProcessual;
	}

	public void setEventoProcessual(String eventoProcessual) {
		this.eventoProcessual = eventoProcessual;
	}

	public String getPartes() {
		return partes;
	}

	public void setPartes(String partes) {
		this.partes = partes;
	}

	public String getEntidade() {
		return entidade;
	}

	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}

	public Date getDataDesarquivamento() {
		return dataDesarquivamento;
	}

	public void setDataDesarquivamento(Date dataDesarquivamento) {
		this.dataDesarquivamento = dataDesarquivamento;
	}

	public String getDataDesarquivamentoStr() {
		return dataDesarquivamentoStr;
	}

	public void setDataDesarquivamentoStr(String dataDesarquivamentoStr) {
		this.dataDesarquivamentoStr = dataDesarquivamentoStr;
	}
}
