package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.PessoaMagistrado;

public class EstatisticaProcessosConclusosBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 48726103062266062L;
	private PessoaMagistrado pessoaMagistrado;
	private List<EstatisticaProcessosConclusosTipoConclusaoBean> estatisticaProcessosConclusosTipoConclusaoBeanList = new ArrayList<EstatisticaProcessosConclusosTipoConclusaoBean>();

	public EstatisticaProcessosConclusosBean() {
	}

	public EstatisticaProcessosConclusosBean(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	public void setEstatisticaProcessosConclusosTipoConclusaoBeanList(
			List<EstatisticaProcessosConclusosTipoConclusaoBean> estatisticaProcessosConclusosTipoConclusaoBeanList) {
		this.estatisticaProcessosConclusosTipoConclusaoBeanList = estatisticaProcessosConclusosTipoConclusaoBeanList;
	}

	public List<EstatisticaProcessosConclusosTipoConclusaoBean> getEstatisticaProcessosConclusosTipoConclusaoBeanList() {
		return estatisticaProcessosConclusosTipoConclusaoBeanList;
	}

	public int getRowspan() {
		int rowspan = 0;
		for (EstatisticaProcessosConclusosTipoConclusaoBean bean : estatisticaProcessosConclusosTipoConclusaoBeanList) {
			rowspan += bean.getRowspan();
		}
		return rowspan;
	}
}