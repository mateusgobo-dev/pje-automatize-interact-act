package br.com.infox.pje.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Edson
 * 
 */
public class ProcessosDistribuidoSessaoBean {
	private String processo;
	private String classeJudicial;
	// data da distribuição reflete a data de inclusão do evento distribuição do
	// processo
	private Date dataDistribuicao;

	public Date getDataDistribuicao() {
		return dataDistribuicao;
	}

	public void setDataDistribuicao(Date dataDistribuicao) {
		this.dataDistribuicao = dataDistribuicao;
	}

	public String getDataDistribuicaoFormatada() {
		return new SimpleDateFormat("dd/MM/yyyy").format(dataDistribuicao);
	}

	public String getProcesso() {
		return processo;
	}

	public void setProcesso(String processo) {
		this.processo = processo;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
}