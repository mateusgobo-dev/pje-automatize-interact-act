package br.com.infox.pje.bean;

import java.io.Serializable;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * entidades por vara
 * 
 * @author Luiz Carlos Menezes
 * 
 */
public class EstatisticaProcessoEntidadesVaraBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7773834567139219808L;
	private String entidade;
	private long totalProcEntidade;

	public EstatisticaProcessoEntidadesVaraBean() {
	}

	public EstatisticaProcessoEntidadesVaraBean(String entidade, long totalProcEntidade) {
		super();
		this.entidade = entidade;
		this.totalProcEntidade = totalProcEntidade;
	}

	public String getEntidade() {
		return entidade;
	}

	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}

	public void setTotalProcEntidade(long totalProcEntidade) {
		this.totalProcEntidade = totalProcEntidade;
	}

	public long getTotalProcEntidade() {
		return totalProcEntidade;
	}

	@Override
	public String toString() {
		return entidade + "-" + totalProcEntidade;
	}

}