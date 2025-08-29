package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * 
 * @author Wilson
 * 
 */
public class EstatisticaJFConclusaoListaProcessosClasse implements
		Comparable<EstatisticaJFConclusaoListaProcessosClasse>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5529523280274228341L;
	private ClasseJudicial classe;
	private List<ProcessoTrf> listProcessRemanescente = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessConclusosSentenca = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessDevolvidosSentenca = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessConvertidosDiligencia = new ArrayList<ProcessoTrf>(0);
	private int rowspan = 1;

	public int getRowspan() {
		return rowspan;
	}

	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}

	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	public List<ProcessoTrf> getListProcessRemanescente() {
		return listProcessRemanescente;
	}

	public void setListProcessRemanescente(List<ProcessoTrf> listProcessRemanescente) {
		this.listProcessRemanescente = listProcessRemanescente;
	}

	public List<ProcessoTrf> getListProcessConclusosSentenca() {
		return listProcessConclusosSentenca;
	}

	public void setListProcessConclusosSentenca(List<ProcessoTrf> listProcessConclusosSentenca) {
		this.listProcessConclusosSentenca = listProcessConclusosSentenca;
	}

	public List<ProcessoTrf> getListProcessDevolvidosSentenca() {
		return listProcessDevolvidosSentenca;
	}

	public void setListProcessDevolvidosSentenca(List<ProcessoTrf> listProcessDevolvidosSentenca) {
		this.listProcessDevolvidosSentenca = listProcessDevolvidosSentenca;
	}

	public List<ProcessoTrf> getListProcessConvertidosDiligencia() {
		return listProcessConvertidosDiligencia;
	}

	public void setListProcessConvertidosDiligencia(List<ProcessoTrf> listProcessConvertidosDiligencia) {
		this.listProcessConvertidosDiligencia = listProcessConvertidosDiligencia;
	}

	public int getSizeRemanescente() {
		return listProcessRemanescente.size();
	}

	public int getSizeConclusosSentenca() {
		return listProcessConclusosSentenca.size();
	}

	public int getSizeDevolvidosSentenca() {
		return listProcessDevolvidosSentenca.size();
	}

	public int getSizeConvertidosDiligencia() {
		return listProcessConvertidosDiligencia.size();
	}

	public int getSizePendentesSentenca() {
		return (getSizeRemanescente() + getSizeConclusosSentenca()) - getSizeConvertidosDiligencia();
	}

	public String getListRemanescente() {
		return getProcessList(listProcessRemanescente);
	}

	public String getListConclusosSentenca() {
		return getProcessList(listProcessConclusosSentenca);
	}

	public String getListDevolvidosSentenca() {
		return getProcessList(listProcessDevolvidosSentenca);
	}

	public String getListConvertidosDiligencia() {
		return getProcessList(listProcessConvertidosDiligencia);
	}
	
	private String getProcessList(List<ProcessoTrf> list) {
		StringBuilder s = new StringBuilder(" ");
		for (ProcessoTrf p : list) {
			s.append(p.getNumeroProcesso());
			s.append("  ");
		}
		return s.toString();
	}

	@Override
	public int compareTo(EstatisticaJFConclusaoListaProcessosClasse o) {
		return Integer.valueOf(classe.getIdClasseJudicial()).compareTo(o.getClasse().getIdClasseJudicial());
	}
}