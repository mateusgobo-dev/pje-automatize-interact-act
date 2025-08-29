package br.com.infox.ibpm.component.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean que representa o movimento que será lançado.
 * 
 * A quantidade de movimentoBeans a serem lançados é determinada pela quantidade
 * do controle inputNumberSpinner.
 * 
 * Cada movimentoBean terá uma lista de complementoBean, que representa um
 * complemento do movimento a ser lançado. Este complemento pode ser múltiplo ou
 * não.
 * 
 * @author David, Kelly
 */
public class MovimentoBean implements Serializable {

	private static final long serialVersionUID = 6178664185395091282L;

	private List<ComplementoBean> complementoBeanList = new ArrayList<ComplementoBean>();

	public List<ComplementoBean> getComplementoBeanList() {
		return complementoBeanList;
	}

	public void setComplementoBeanList(List<ComplementoBean> complementoBeanList) {
		this.complementoBeanList = complementoBeanList;
	}
	
	/**
	 * [PJEII-2393]
	 * Verifica se algum ComplementoBean é do tipo dinâmico.
	 * @return true, caso algum complemento seja do tipo dinâmico.
	 */
	public boolean possuiComplementoDinamico() {
		if (complementoBeanList == null) {
			return false;
		}
		
		for (ComplementoBean complementoBean : complementoBeanList) {
			if (complementoBean.isTipoDinamico()) {
				return true;
			}
		}
		
		return false;
	}
}
