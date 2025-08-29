package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * 
 * @author Rafael
 * 
 */
public class EstatisticaJFProcessosDistribuidosClasseEntidade implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2122299119718202824L;
	private ClasseJudicial classe;
	private List<ProcessoTrf> listProcessRem = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessDistr = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessDevolv = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessReativ = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessMudClassRee = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessMudClassBaixa = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessBaixados = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessRedistr = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessRemetidos = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessSusp = new ArrayList<ProcessoTrf>(0);
	private List<ProcessoTrf> listProcessArquivados = new ArrayList<ProcessoTrf>(0);

	public int getQtdTotal() {
		return ((listProcessRem.size() + 
			     listProcessDistr.size() + 
			     listProcessDevolv.size() + 
			     listProcessReativ.size() + 
			     listProcessMudClassRee.size()) 
		     - 
			    (listProcessMudClassBaixa.size() + 
			     listProcessBaixados.size() + 
			     listProcessRedistr.size() + 
			     listProcessSusp.size() +
			     listProcessArquivados.size() +
			     listProcessRemetidos.size()));
	}

	public int rowspanRelAC() {
		if (getListProcessRem().size() > 0 || getListProcessDistr().size() > 0 || getListProcessDevolv().size() > 0
				|| getListProcessReativ().size() > 0 || getListProcessMudClassRee().size() > 0
				|| getListProcessMudClassBaixa().size() > 0 || getListProcessBaixados().size() > 0
				|| getListProcessRedistr().size() > 0 || getListProcessRemetidos().size() > 0
				|| getListProcessSusp().size() > 0 || getListProcessArquivados().size() > 0) {
			return 2;
		}
		return 1;
	}

	public int getRowspanGrid() {
		int rowspan = 0;
		rowspan = (rowspan + (listProcessRem.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessDistr.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessDevolv.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessReativ.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessMudClassRee.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessMudClassBaixa.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessBaixados.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessRedistr.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessRemetidos.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessSusp.size() > 0 ? 1 : 0));
		rowspan = (rowspan + (listProcessArquivados.size() > 0 ? 1 : 0));
		return rowspan > 0 ? (rowspan * 2) + 2 : rowspan + 1;
	}

	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	public List<ProcessoTrf> getListProcessRem() {
		return listProcessRem;
	}

	public void setListProcessRem(List<ProcessoTrf> listProcessRem) {
		this.listProcessRem = listProcessRem;
	}

	public List<ProcessoTrf> getListProcessDistr() {
		return listProcessDistr;
	}

	public void setListProcessDistr(List<ProcessoTrf> listProcessDistr) {
		this.listProcessDistr = listProcessDistr;
	}

	public List<ProcessoTrf> getListProcessDevolv() {
		return listProcessDevolv;
	}

	public void setListProcessDevolv(List<ProcessoTrf> listProcessDevolv) {
		this.listProcessDevolv = listProcessDevolv;
	}

	public List<ProcessoTrf> getListProcessReativ() {
		return listProcessReativ;
	}

	public void setListProcessReativ(List<ProcessoTrf> listProcessReativ) {
		this.listProcessReativ = listProcessReativ;
	}

	public List<ProcessoTrf> getListProcessMudClassRee() {
		return listProcessMudClassRee;
	}

	public void setListProcessMudClassRee(List<ProcessoTrf> listProcessMudClassRee) {
		this.listProcessMudClassRee = listProcessMudClassRee;
	}

	public List<ProcessoTrf> getListProcessMudClassBaixa() {
		return listProcessMudClassBaixa;
	}

	public void setListProcessMudClassBaixa(List<ProcessoTrf> listProcessMudClassBaixa) {
		this.listProcessMudClassBaixa = listProcessMudClassBaixa;
	}

	public List<ProcessoTrf> getListProcessBaixados() {
		return listProcessBaixados;
	}

	public void setListProcessBaixados(List<ProcessoTrf> listProcessBaixados) {
		this.listProcessBaixados = listProcessBaixados;
	}

	public List<ProcessoTrf> getListProcessRedistr() {
		return listProcessRedistr;
	}

	public void setListProcessRedistr(List<ProcessoTrf> listProcessRedistr) {
		this.listProcessRedistr = listProcessRedistr;
	}

	public List<ProcessoTrf> getListProcessRemetidos() {
		return listProcessRemetidos;
	}

	public void setListProcessRemetidos(List<ProcessoTrf> listProcessRemetidos) {
		this.listProcessRemetidos = listProcessRemetidos;
	}

	public List<ProcessoTrf> getListProcessSusp() {
		return listProcessSusp;
	}

	public void setListProcessSusp(List<ProcessoTrf> listProcessSusp) {
		this.listProcessSusp = listProcessSusp;
	}

	public List<ProcessoTrf> getListProcessArquivados() {
		return listProcessArquivados;
	}

	public void setListProcessArquivados(List<ProcessoTrf> listProcessArquivados) {
		this.listProcessArquivados = listProcessArquivados;
	}

	public int getSizeRem() {
		return listProcessRem.size();
	}

	public int getSizeDistr() {
		return listProcessDistr.size();
	}

	public int getSizeDevolv() {
		return listProcessDevolv.size();
	}

	public int getSizeReativ() {
		return listProcessReativ.size();
	}

	public int getSizeMudClassRee() {
		return listProcessMudClassRee.size();
	}

	public int getSizeMudClassBaixa() {
		return listProcessMudClassBaixa.size();
	}

	public int getSizeBaixados() {
		return listProcessBaixados.size();
	}

	public int getSizeRedistr() {
		return listProcessRedistr.size();
	}

	public int getSizeRemet() {
		return listProcessRemetidos.size();
	}

	public int getSizeSusp() {
		return listProcessSusp.size();
	}

	public int getSizeArq() {
		return listProcessArquivados.size();
	}

	public String getProcessListRem() {
		return getProcessList(listProcessRem);
	}

	public String getProcessListDistr() {
		return getProcessList(listProcessDistr);
	}

	public String getProcessListDevolv() {
		return getProcessList(listProcessDevolv);
	}

	public String getProcessListReativ() {
		return getProcessList(listProcessReativ);
	}

	public String getProcessListMudClassRee() {
		return getProcessList(listProcessMudClassRee);
	}

	public String getProcessListMudClassBaixa() {
		return getProcessList(listProcessMudClassBaixa);
	}

	public String getProcessListBaixados() {
		return getProcessList(listProcessBaixados);
	}

	public String getProcessListRemetidos() {
		return getProcessList(listProcessRemetidos);
	}

	public String getProcessListRedistr() {
		return getProcessList(listProcessRedistr);
	}

	public String getProcessListSuspensos() {
		return getProcessList(listProcessSusp);
	}

	public String getProcessListArquivados() {
		return getProcessList(listProcessArquivados);
	}
	
	private String getProcessList(List<ProcessoTrf> list) {
		StringBuilder s = new StringBuilder(" ");
		for (ProcessoTrf p : list) {
			s.append(p.getNumeroProcesso());
			s.append("  ");
		}
		return s.toString();
	}

}