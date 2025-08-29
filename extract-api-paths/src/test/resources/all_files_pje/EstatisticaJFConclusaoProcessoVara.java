package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Wilson
 * 
 */
public class EstatisticaJFConclusaoProcessoVara implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1074721902125757340L;
	private String vara;
	private String secao;
	private List<EstatisticaJFConclusaoProcessoClasses> subListClasse;
	private List<EstatisticaJFConclusaoListaProcessosClasse> subList;
	private int totalProcessoRemanescente;
	private int totalProcessoConclusosSentenca;
	private int totalProcessoDevolvidosSentenca;
	private int totalProcessoConvertidosDiligencia;
	private int totalProcessoPendentesSentenca;

	public String getVara() {
		return vara;
	}

	public void setVara(String vara) {
		this.vara = vara;
	}

	public List<EstatisticaJFConclusaoProcessoClasses> getSubListClasse() {
		return subListClasse;
	}

	public void setSubListClasse(List<EstatisticaJFConclusaoProcessoClasses> subListClasse) {
		this.subListClasse = subListClasse;
	}

	public List<EstatisticaJFConclusaoListaProcessosClasse> getSubList() {
		return subList;
	}

	public void setSubList(List<EstatisticaJFConclusaoListaProcessosClasse> subList) {
		this.subList = subList;
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

	public int getTotalProcessoConclusosSentenca() {
		return totalProcessoConclusosSentenca;
	}

	public void setTotalProcessoConclusosSentenca(int totalProcessoConclusosSentenca) {
		this.totalProcessoConclusosSentenca = totalProcessoConclusosSentenca;
	}

	public int getTotalProcessoDevolvidosSentenca() {
		return totalProcessoDevolvidosSentenca;
	}

	public void setTotalProcessoDevolvidosSentenca(int totalProcessoDevolvidosSentenca) {
		this.totalProcessoDevolvidosSentenca = totalProcessoDevolvidosSentenca;
	}

	public int getTotalProcessoConvertidosDiligencia() {
		return totalProcessoConvertidosDiligencia;
	}

	public void setTotalProcessoConvertidosDiligencia(int totalProcessoConvertidosDiligencia) {
		this.totalProcessoConvertidosDiligencia = totalProcessoConvertidosDiligencia;
	}

	public int getTotalProcessoPendentesSentenca() {
		return totalProcessoPendentesSentenca;
	}

	public void setTotalProcessoPendentesSentenca(int totalProcessoPendentesSentenca) {
		this.totalProcessoPendentesSentenca = totalProcessoPendentesSentenca;
	}

	private void inicializarProcessos() {
		totalProcessoRemanescente = 0;
		totalProcessoConclusosSentenca = 0;
		totalProcessoDevolvidosSentenca = 0;
		totalProcessoConvertidosDiligencia = 0;
		totalProcessoPendentesSentenca = 0;
	}

	private void recalcularTotaisAnalitico() {
		inicializarProcessos();
		for (EstatisticaJFConclusaoListaProcessosClasse classe : getSubList()) {
			if (classe.getListProcessRemanescente() != null) {
				totalProcessoRemanescente += classe.getListProcessRemanescente().size();
			}
			if (classe.getListProcessConclusosSentenca() != null) {
				totalProcessoConclusosSentenca += classe.getListProcessConclusosSentenca().size();
			}
			if (classe.getListProcessDevolvidosSentenca() != null) {
				totalProcessoDevolvidosSentenca += classe.getListProcessDevolvidosSentenca().size();
			}
			if (classe.getListProcessConvertidosDiligencia() != null) {
				totalProcessoConvertidosDiligencia += classe.getListProcessConvertidosDiligencia().size();
			}
		}
		totalProcessoPendentesSentenca = (totalProcessoRemanescente + totalProcessoConclusosSentenca)
				- totalProcessoConvertidosDiligencia;
	}
}