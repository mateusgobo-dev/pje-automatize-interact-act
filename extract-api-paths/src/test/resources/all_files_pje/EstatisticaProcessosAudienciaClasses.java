package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class EstatisticaProcessosAudienciaClasses implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 997675662231454285L;
	private ClasseJudicial classe;
	private List<ProcessoTrf> listProcessRemanescente = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessDesignado = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessRealizados = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessAdiados = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessCancelados = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessSuspensos = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessPendentes = new ArrayList<ProcessoTrf>(0);

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

	public List<ProcessoTrf> getListProcessDesignado() {
		return listProcessDesignado;
	}

	public void setListProcessDesignado(List<ProcessoTrf> listProcessDesignado) {
		this.listProcessDesignado = listProcessDesignado;
	}

	public List<ProcessoTrf> getListProcessRealizados() {
		return listProcessRealizados;
	}

	public void setListProcessRealizados(List<ProcessoTrf> listProcessRealizados) {
		this.listProcessRealizados = listProcessRealizados;
	}

	public List<ProcessoTrf> getListProcessAdiados() {
		return listProcessAdiados;
	}

	public void setListProcessAdiados(List<ProcessoTrf> listProcessAdiados) {
		this.listProcessAdiados = listProcessAdiados;
	}

	public void setListProcessCancelados(List<ProcessoTrf> listProcessCancelados) {
		this.listProcessCancelados = listProcessCancelados;
	}

	public List<ProcessoTrf> getListProcessCancelados() {
		return listProcessCancelados;
	}

	public void setListProcessSuspensos(List<ProcessoTrf> listProcessSuspensos) {
		this.listProcessSuspensos = listProcessSuspensos;
	}

	public List<ProcessoTrf> getListProcessSuspensos() {
		return listProcessSuspensos;
	}

	public int getSizeRemanescente() {
		return listProcessRemanescente.size();
	}

	public int getSizeAdiados() {
		return listProcessAdiados.size();
	}

	public int getSizeCancelados() {
		return listProcessCancelados.size();
	}

	public int getSizeSuspensos() {
		return listProcessSuspensos.size();
	}

	public int getSizeDesignado() {
		return listProcessDesignado.size();
	}

	public int getSizeRealizados() {
		return listProcessRealizados.size();
	}

	public int getSizePendentes() {
		return listProcessPendentes.size();
	}

	public String getListRemanescente() {
		return getProcessList(listProcessRemanescente);
	}

	public String getListCancelados() {
		return getProcessList(listProcessCancelados);
	}

	public String getListRealizados() {
		return getProcessList(listProcessRealizados);
	}

	public String getListAdiados() {
		return getProcessList(listProcessAdiados);
	}

	public String getListSuspensos() {
		return getProcessList(listProcessSuspensos);
	}

	public String getListDesignado() {
		return getProcessList(listProcessDesignado);
	}

	public String getListPendentes() {
		return getProcessList(listProcessPendentes);
	}
	
	private String getProcessList(List<ProcessoTrf> list) {
		StringBuilder s = new StringBuilder(" ");
		for (ProcessoTrf p : list) {
			s.append(p.getNumeroProcesso());
			s.append("  ");
		}
		return s.toString();
	}

	public int getRowspan() {
		if (listProcessRemanescente.size() == 0 &&
			listProcessDesignado.size() == 0 &&
			listProcessRealizados.size() == 0 &&
			listProcessAdiados.size() == 0 &&
			listProcessCancelados.size() == 0 &&
			listProcessSuspensos.size() == 0) {
			return 1;
		}else{
			return 2;
		}
	}	
	
	public long getTotalPendentes() {
		long totalPen = (listProcessRemanescente.size() + listProcessDesignado.size())
				- (listProcessRealizados.size() + listProcessAdiados.size() + listProcessCancelados.size() + listProcessSuspensos
						.size());
		return totalPen;
	}

	public void setListProcessPendentes(List<ProcessoTrf> listProcessPendentes) {
		this.listProcessPendentes = listProcessPendentes;
	}

	public List<ProcessoTrf> getListProcessPendentes() {
		return listProcessPendentes;
	}
}