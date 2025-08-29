package br.com.infox.pje.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

/**
 * Bean para exibição da listagem do relatório na estatística de procesos
 * entidades por vara
 * 
 * @author Luiz Carlos Menezes
 * 
 */
public class EstatisticaPautaAudienciaBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1479513097520360605L;
	private StatusAudienciaEnum statusAudienciaEnum;
	private String dtInicio;
	private ProcessoTrf processoTrf;
	private ClasseJudicial classeJudicial;
	private TipoAudiencia tipoAudiencia;
	private String autorXreu;
	private String processoAssunto;
	private long totalNumeroDepoimento;

	public EstatisticaPautaAudienciaBean() {
	}

	public EstatisticaPautaAudienciaBean(StatusAudienciaEnum statusAudienciaEnum, Date dtInicio,
			ProcessoTrf processoTrf, ClasseJudicial classeJudicial, TipoAudiencia tipoAudiencia,
			long totalNumeroDepoimento) {
		super();
		this.statusAudienciaEnum = statusAudienciaEnum;
		setDtInicio(dtInicio);
		this.processoTrf = processoTrf;
		this.classeJudicial = classeJudicial;
		this.tipoAudiencia = tipoAudiencia;
		StringBuilder sb = new StringBuilder();
		for (ProcessoAssunto processoAssunto : processoTrf.getProcessoAssuntoList()) {
			sb.append(processoAssunto.getAssuntoTrf().getCodAssuntoTrf());
			sb.append(" - ");
			sb.append(processoAssunto.getAssuntoTrf());
			if (processoAssunto != processoTrf.getProcessoAssuntoList().get(
					processoTrf.getProcessoAssuntoList().size() - 1)) {
				sb.append(", ");
			}

		}
		this.processoAssunto = sb.toString();
		this.totalNumeroDepoimento = totalNumeroDepoimento;
	}

	public void setAutorXreu(String autorXreu) {
		this.autorXreu = autorXreu;
	}

	public String getAutorXreu() {
		return this.autorXreu;
	}

	public void setStatusAudienciaEnum(StatusAudienciaEnum statusAudienciaEnum) {
		this.statusAudienciaEnum = statusAudienciaEnum;
	}

	public StatusAudienciaEnum getStatusAudienciaEnum() {
		return statusAudienciaEnum;
	}

	public void setDtInicio(Date dtInicio) {
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		this.dtInicio = formatador.format(dtInicio);
	}

	public String getDtInicio() {
		return dtInicio;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTotalNumeroDepoimento(long totalNumeroDepoimento) {
		this.totalNumeroDepoimento = totalNumeroDepoimento;
	}

	public long getTotalNumeroDepoimento() {
		return totalNumeroDepoimento;
	}

	public void setProcessoAssunto(String processoAssunto) {
		this.processoAssunto = processoAssunto;
	}

	public String getProcessoAssunto() {
		return this.processoAssunto;
	}

	@Override
	public String toString() {
		return statusAudienciaEnum + "-" + dtInicio + "-" + processoTrf + "-" + classeJudicial;
	}

}