package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class EstatisticaProcessosSubListaTipoConclusosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2719759376718447026L;
	private String classeJudicial;
	private String numeroProcesso;
	private Date dataConclusao;
	private String autorXreu;
	private ProcessoTrf processoTrf;

	public EstatisticaProcessosSubListaTipoConclusosBean() {
	}

	public EstatisticaProcessosSubListaTipoConclusosBean(ClasseJudicial classeJudicial, ProcessoTrf processo,
			Date dataConclusao) {
		this.classeJudicial = classeJudicial.getCodClasseJudicial();
		this.dataConclusao = dataConclusao;
		this.numeroProcesso = processo.getNumeroProcesso();
		this.processoTrf = processo;
	}

	public String getAutorXreu() {
		return autorXreu;
	}

	public void setAutorXreu(String autorXreu) {
		this.autorXreu = autorXreu;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public Date getDataConclusao() {
		return dataConclusao;
	}

	public void setDataConclusao(Date dataConclusao) {
		this.dataConclusao = dataConclusao;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}
}