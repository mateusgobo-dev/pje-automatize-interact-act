package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Bean usado para as propriedades da estatística dos processos Analíticos por
 * assunto.
 * 
 * @author MarlonAssis
 */
public class EstatisticaProcessoDistribuidoAnaliticoAssuntoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9043639867130558198L;
	private AssuntoTrf assuntoTrf = new AssuntoTrf();
	private Set<ProcessoTrf> listaRemanescentes = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaDistribuidos = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaDevolvidos = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaReativados = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaClasseReentr = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaClassesBaixa = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaBaixasDefinitiva = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaRedistribuidos = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaRemetidos = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaSuspensos = new HashSet<ProcessoTrf>();
	private Set<ProcessoTrf> listaArquivadosSemBaixa = new HashSet<ProcessoTrf>();

	public int getTotalAA(){
		return ((getListaRemanescentesSize() + getListaDistribuidosSize() + getListaDevolvidosSize() 
			   + getListaReativadosSize() + getListaClasseReentrSize())
			  - (getListaClassesBaixaSize() + getListaBaixasDefinitivaSize() + getListaRedistribuidosSize()
			   + getListaRemetidosSize() + getListaSuspensosSize() + getListaArquivadosSemBaixaSize()));
	}	
	
	public int getListaRemanescentesSize() {
		return listaRemanescentes.size();
	}

	public int getListaDevolvidosSize() {
		return listaDevolvidos.size();
	}

	public int getListaDistribuidosSize() {
		return listaDistribuidos.size();
	}

	public int getListaReativadosSize() {
		return listaReativados.size();
	}

	public int getListaClasseReentrSize() {
		return listaClasseReentr.size();
	}

	public int getListaClassesBaixaSize() {
		return listaClassesBaixa.size();
	}

	public int getListaBaixasDefinitivaSize() {
		return listaBaixasDefinitiva.size();
	}

	public int getListaRedistribuidosSize() {
		return listaRedistribuidos.size();
	}

	public int getListaRemetidosSize() {
		return listaRemetidos.size();
	}

	public int getListaSuspensosSize() {
		return listaSuspensos.size();
	}

	public int getListaArquivadosSemBaixaSize() {
		return listaArquivadosSemBaixa.size();
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setListaRemanescentes(Set<ProcessoTrf> listaRemanescentes) {
		this.listaRemanescentes = listaRemanescentes;
	}

	public Set<ProcessoTrf> getListaRemanescentes() {
		return listaRemanescentes;
	}

	public void setListaDistribuidos(Set<ProcessoTrf> listaDistribuidos) {
		this.listaDistribuidos = listaDistribuidos;
	}

	public Set<ProcessoTrf> getListaDistribuidos() {
		return listaDistribuidos;
	}

	public void setListaDevolvidos(Set<ProcessoTrf> listaDevolvidos) {
		this.listaDevolvidos = listaDevolvidos;
	}

	public Set<ProcessoTrf> getListaDevolvidos() {
		return listaDevolvidos;
	}

	public void setListaReativados(Set<ProcessoTrf> listaReativados) {
		this.listaReativados = listaReativados;
	}

	public Set<ProcessoTrf> getListaReativados() {
		return listaReativados;
	}

	public void setListaClasseReentr(Set<ProcessoTrf> listaClasseReentr) {
		this.listaClasseReentr = listaClasseReentr;
	}

	public Set<ProcessoTrf> getListaClasseReentr() {
		return listaClasseReentr;
	}

	public void setListaClassesBaixa(Set<ProcessoTrf> listaClassesBaixa) {
		this.listaClassesBaixa = listaClassesBaixa;
	}

	public Set<ProcessoTrf> getListaClassesBaixa() {
		return listaClassesBaixa;
	}

	public void setListaBaixasDefinitiva(Set<ProcessoTrf> listaBaixasDefinitiva) {
		this.listaBaixasDefinitiva = listaBaixasDefinitiva;
	}

	public Set<ProcessoTrf> getListaBaixasDefinitiva() {
		return listaBaixasDefinitiva;
	}

	public void setListaRedistribuidos(Set<ProcessoTrf> listaRedistribuidos) {
		this.listaRedistribuidos = listaRedistribuidos;
	}

	public Set<ProcessoTrf> getListaRedistribuidos() {
		return listaRedistribuidos;
	}

	public void setListaRemetidos(Set<ProcessoTrf> listaRemetidos) {
		this.listaRemetidos = listaRemetidos;
	}

	public Set<ProcessoTrf> getListaRemetidos() {
		return listaRemetidos;
	}

	public void setListaSuspensos(Set<ProcessoTrf> listaSuspensos) {
		this.listaSuspensos = listaSuspensos;
	}

	public Set<ProcessoTrf> getListaSuspensos() {
		return listaSuspensos;
	}

	public void setListaArquivadosSemBaixa(Set<ProcessoTrf> listaArquivadosSemBaixa) {
		this.listaArquivadosSemBaixa = listaArquivadosSemBaixa;
	}

	public Set<ProcessoTrf> getListaArquivadosSemBaixa() {
		return listaArquivadosSemBaixa;
	}
}