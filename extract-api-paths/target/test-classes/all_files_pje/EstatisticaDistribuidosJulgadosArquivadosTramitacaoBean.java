package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * distribuídos arquivados
 * 
 * @author Geldo PC
 * 
 */
public class EstatisticaDistribuidosJulgadosArquivadosTramitacaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6948672457539520618L;
	private String codEstado;
	private List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean> distribuidosJulgadosArquivadosTramitacaoListBean = new ArrayList<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean>();
	private Integer totalVarasEstados;
	private int totalProcDistribuidos;
	private int totalProcArquivados;
	private int totalProcJulgados;
	private int totalProcTramitacao;

	public void setCodEstado(String codEstado) {
		this.codEstado = codEstado;
	}

	public String getCodEstado() {
		return codEstado;
	}

	public List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean> getDistribuidosJulgadosArquivadosTramitacaoListOrdenadoBean() {
		List<Integer> lista = new ArrayList<Integer>();

		// pega todos os números das varas e coloca em uma lista para ordenação
		// posterior
		for (EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean o : distribuidosJulgadosArquivadosTramitacaoListBean) {
			String varas = o.getVaras();
			if (varas.indexOf("ª") > 0) {
				CharSequence subSequence = varas.subSequence(0, varas.indexOf("ª"));
				lista.add(Integer.valueOf(subSequence.toString().trim()));
			}
		}

		// ordena as varas da lista de forma crescente
		Collections.sort(lista);

		// cria a lista de retorno ao usuário ordenado de acordo com o número da
		// vara
		List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean> processoDistribuidoJulgadoListBean2 = new ArrayList<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean>();
		for (Integer u : lista) {
			for (EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean o : distribuidosJulgadosArquivadosTramitacaoListBean) {
				String varas = o.getVaras();
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
		for (EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean o : distribuidosJulgadosArquivadosTramitacaoListBean) {
			if (!processoDistribuidoJulgadoListBean2.contains(o)) {
				processoDistribuidoJulgadoListBean2.add(o);
			}
		}

		return processoDistribuidoJulgadoListBean2;
	}

	public void setDistribuidosJulgadosArquivadosTramitacaoListBean(
			List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean> distribuidosJulgadosArquivadosTramitacaoListBean) {
		this.distribuidosJulgadosArquivadosTramitacaoListBean = distribuidosJulgadosArquivadosTramitacaoListBean;
	}

	public List<EstatisticaDistribuidosJulgadosArquivadosTramitacaoListBean> getDistribuidosJulgadosArquivadosTramitacaoListBean() {
		return distribuidosJulgadosArquivadosTramitacaoListBean;
	}

	public void setTotalVarasEstados(Integer totalVarasEstados) {
		this.totalVarasEstados = totalVarasEstados;
	}

	public Integer getTotalVarasEstados() {
		return totalVarasEstados;
	}

	public void setTotalProcDistribuidos(int totalProcDistribuidos) {
		this.totalProcDistribuidos = totalProcDistribuidos;
	}

	public int getTotalProcDistribuidos() {
		return totalProcDistribuidos;
	}

	public void setTotalProcArquivados(int totalProcArquivados) {
		this.totalProcArquivados = totalProcArquivados;
	}

	public int getTotalProcArquivados() {
		return totalProcArquivados;
	}

	public void setTotalProcJulgados(int totalProcJulgados) {
		this.totalProcJulgados = totalProcJulgados;
	}

	public int getTotalProcJulgados() {
		return totalProcJulgados;
	}

	public void setTotalProcTramitacao(int totalProcTramitacao) {
		this.totalProcTramitacao = totalProcTramitacao;
	}

	public int getTotalProcTramitacao() {
		return totalProcTramitacao;
	}

}