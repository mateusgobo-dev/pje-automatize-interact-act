package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Bean para exibição da listagem do relatório na estatística
 * 
 * @author Allan
 * 
 */
public class EstatisticaProcTramitacaoSecaoListBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3658171575551172085L;
	private String processo;
	private String classeJudicial;
	private Date dataDistribuicao;
	private String tipoApelacao;
	private String remessa;
	private long totalJulgados;

	public void setProcesso(String processo) {
		this.processo = processo;
	}

	public String getProcesso() {
		return processo;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setTipoApelacao(String tipoApelacao) {
		this.tipoApelacao = tipoApelacao;
	}

	public String getTipoApelacao() {
		return tipoApelacao;
	}

	public void setRemessa(String remessa) {
		this.remessa = remessa;
	}

	public String getRemessa() {
		return remessa;
	}

	public void setTotalJulgados(long totalJulgados) {
		this.totalJulgados = totalJulgados;
	}

	public long getTotalJulgados() {
		return totalJulgados;
	}

}