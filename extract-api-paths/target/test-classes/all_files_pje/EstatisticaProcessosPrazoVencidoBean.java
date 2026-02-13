package br.com.infox.pje.bean;

import java.util.Date;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.DateUtil;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos com
 * prazo vencido
 * 
 * @author Laércio
 * 
 */
public class EstatisticaProcessosPrazoVencidoBean {

	private String processo;
	private ProcessoTrf processoTrf;
	private String classe;
	private String dataExpiracao;
	private Integer diasVencido;
	private String autorXreu;
	private String fase;

	public EstatisticaProcessosPrazoVencidoBean() {
	}

	public EstatisticaProcessosPrazoVencidoBean(ProcessoTrf processoTrf, String classe, Date dataExpiracao,
			Integer diasVencido, String fase) {
		super();
		this.processo = processoTrf.getProcesso().getNumeroProcesso();
		this.processoTrf = processoTrf;
		this.classe = classe;
		this.dataExpiracao = DateUtil.getDataFormatada(dataExpiracao, "dd/MM/yyyy");
		this.diasVencido = diasVencido;
		this.fase = fase;
	}

	public String getProcesso() {
		return processo;
	}

	public void setProcesso(String processo) {
		this.processo = processo;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public String getDataExpiracao() {
		return dataExpiracao;
	}

	public void setDataExpiracao(String dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}

	public Integer getDiasVencido() {
		return diasVencido;
	}

	public void setDiasVencido(Integer diasVencido) {
		this.diasVencido = diasVencido;
	}

	public String getAutorXreu() {
		return autorXreu;
	}

	public void setAutorXreu(String autorXreu) {
		this.autorXreu = autorXreu;
	}

	public String getFase() {
		return fase;
	}

	public void setFase(String fase) {
		this.fase = fase;
	}

	@Override
	public String toString() {
		return processo;
	}
}