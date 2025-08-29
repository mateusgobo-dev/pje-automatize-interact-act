package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório de procesos distribuídos julgados
 * 
 * @author thiago
 * 
 */
public class EstatisticaProcessoDistribuidoJulgadoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3834715989497936006L;
	private String codEstado;
	private List<ProcessoDistribuidoJulgadoListBean> distribuidoJulgadoListBean = new ArrayList<ProcessoDistribuidoJulgadoListBean>();
	private Integer totalVarasEstado;
	private Double totalProcDistribuidos;
	private double totalProcJulgados;
	private double somaPercDistribuidosJulgados;
	private double percTotalDistribuidosJulgados;

	public String getCodEstado() {
		return codEstado;
	}

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public List<ProcessoDistribuidoJulgadoListBean> getDistribuidoJulgadoListOrdenadoBean() {
		List<Integer> lista = new ArrayList<Integer>();

		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (ProcessoDistribuidoJulgadoListBean o : distribuidoJulgadoListBean) {
			String varas = o.getVara();
			if (varas.indexOf("ª") > 0) {
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				lista.add(Integer.valueOf(subSequence.toString().trim()));
			}
		}

		// ordena as varas da lista de forma crescente
		Collections.sort(lista);

		// cria a lista de retorno ao usuário ordenado de acordo com o número da
		// vara
		List<ProcessoDistribuidoJulgadoListBean> processoDistribuidoJulgadoListBean2 = new ArrayList<ProcessoDistribuidoJulgadoListBean>();
		for (Integer u : lista) {
			for (ProcessoDistribuidoJulgadoListBean o : distribuidoJulgadoListBean) {
				String varas = o.getVara();
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				if (varas.indexOf("ª") > 0) {
					if (Integer.valueOf(subSequence.toString().trim()).equals(u)
							&& !processoDistribuidoJulgadoListBean2.contains(o)) {
						processoDistribuidoJulgadoListBean2.add(o);
					}
				}
			}
		}

		// adiciona as varas que não tem número
		for (ProcessoDistribuidoJulgadoListBean o : distribuidoJulgadoListBean) {
			if (!processoDistribuidoJulgadoListBean2.contains(o)) {
				processoDistribuidoJulgadoListBean2.add(o);
			}
		}

		return processoDistribuidoJulgadoListBean2;
	}

	public List<ProcessoDistribuidoJulgadoListBean> getDistribuidoJulgadoListBean() {
		return distribuidoJulgadoListBean;
	}

	public void setDistribuidoJulgadoListBean(List<ProcessoDistribuidoJulgadoListBean> distribuidoJulgadoListBean) {
		this.distribuidoJulgadoListBean = distribuidoJulgadoListBean;
	}

	public Integer getTotalVarasEstado() {
		return totalVarasEstado;
	}

	public void setTotalVarasEstado(Integer totalVaras) {
		this.totalVarasEstado = totalVaras;
	}

	public Double getTotalProcDistribuidos() {
		return totalProcDistribuidos;
	}

	public void setTotalProcDistribuidos(Double totalProcDistribuidos) {
		this.totalProcDistribuidos = totalProcDistribuidos;
	}

	public double getTotalProcJulgados() {
		return totalProcJulgados;
	}

	public void setTotalProcJulgados(double totalProcJulgados) {
		this.totalProcJulgados = totalProcJulgados;
	}

	public double getSomaPercDistribuidosJulgados() {
		return somaPercDistribuidosJulgados;
	}

	public void setSomaPercDistribuidosJulgados(double somaPercDistribuidosJulgados) {
		this.somaPercDistribuidosJulgados = somaPercDistribuidosJulgados;
	}

	public double getPercTotalDistribuidosJulgados() {
		return percTotalDistribuidosJulgados;
	}

	public void setPercTotalDistribuidosJulgados(double percTotalDistribuidosJulgados) {
		this.percTotalDistribuidosJulgados = percTotalDistribuidosJulgados;
	}

}