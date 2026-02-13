package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Wilson
 * 
 */
public class EstatisticaJFProcessosAudienciaVara implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4098306657394798193L;
	private String vara;
	private String secao;
	private List<EstatisticaProcessosAudienciaClasses> subList;
	private int totalProcessoRemanescente;
	private int totalProcessoDesignado;
	private int totalProcessoRealizados;
	private int totalProcessoAdiados;
	private int totalProcessoCancelados;
	private int totalProcessoSuspensas;
	private int totalProcessoPendentes;

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public List<EstatisticaProcessosAudienciaClasses> getSubList() {
		return subList;
	}

	public void setSubList(List<EstatisticaProcessosAudienciaClasses> list) {
		this.subList = list;
		recalcularTotaisAnalitico();
	}

	public void setSecao(String secao) {
		this.secao = secao;
	}

	public String getSecao() {
		return secao;
	}

	public int getTotalProcessoRemanescente() {
		return totalProcessoRemanescente;
	}

	public void setTotalProcessoRemanescente(int totalProcessoRemanescente) {
		this.totalProcessoRemanescente = totalProcessoRemanescente;
	}

	public int getTotalProcessoDesignado() {
		return totalProcessoDesignado;
	}

	public void setTotalProcessoDesignado(int totalProcessoDesignado) {
		this.totalProcessoDesignado = totalProcessoDesignado;
	}

	public int getTotalProcessoRealizados() {
		return totalProcessoRealizados;
	}

	public void setTotalProcessoRealizados(int totalProcessoRealizados) {
		this.totalProcessoRealizados = totalProcessoRealizados;
	}

	public int getTotalProcessoAdiados() {
		return totalProcessoAdiados;
	}

	public void setTotalProcessoAdiados(int totalProcessoAdiados) {
		this.totalProcessoAdiados = totalProcessoAdiados;
	}

	public int getTotalProcessoCancelados() {
		return totalProcessoCancelados;
	}

	public void setTotalProcessoCancelados(int totalProcessoCancelados) {
		this.totalProcessoCancelados = totalProcessoCancelados;
	}

	public void setTotalProcessoPendentes(int totalProcessoPendentes) {
		this.totalProcessoPendentes = totalProcessoPendentes;
	}

	public int getTotalProcessoPendentes() {
		totalProcessoPendentes = (totalProcessoRemanescente + totalProcessoDesignado)
				- (totalProcessoRealizados + totalProcessoAdiados + totalProcessoCancelados + totalProcessoSuspensas);
		return totalProcessoPendentes;
	}

	public void setTotalProcessoSuspensas(int totalProcessoSuspensas) {
		this.totalProcessoSuspensas = totalProcessoSuspensas;
	}

	public int getTotalProcessoSuspensas() {
		return totalProcessoSuspensas;
	}

	private void inicializarProcessos() {
		totalProcessoRemanescente = 0;
		totalProcessoDesignado = 0;
		totalProcessoDesignado = 0;
		totalProcessoRealizados = 0;
		totalProcessoAdiados = 0;
	}

	private void recalcularTotaisAnalitico() {
		inicializarProcessos();
		for (EstatisticaProcessosAudienciaClasses classe : getSubList()) {
			if (classe.getListProcessRemanescente() != null) {
				totalProcessoRemanescente += classe.getListProcessRemanescente().size();
			}
			if (classe.getListProcessDesignado() != null) {
				totalProcessoDesignado += classe.getListProcessDesignado().size();
			}
			if (classe.getListProcessRealizados() != null) {
				totalProcessoRealizados += classe.getListProcessRealizados().size();
			}
			if (classe.getListProcessAdiados() != null) {
				totalProcessoAdiados += classe.getListProcessAdiados().size();
			}
			if (classe.getListProcessCancelados() != null) {
				totalProcessoCancelados += classe.getListProcessCancelados().size();
			}
			if (classe.getListProcessSuspensos() != null) {
				setTotalProcessoSuspensas(getTotalProcessoSuspensas() + classe.getListProcessSuspensos().size());
			}
		}
	}
}