package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoPeticaoNaoLidaDAO;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;

@Scope(ScopeType.PAGE)
@Name(AgrupadorPeticoesAvulsas.NAME)
public class AgrupadorPeticoesAvulsas implements Serializable {
	
	public static final String PROCESSO_PETICAO_AVULSA_MANUAL_GRID = "processoPeticaoAvulsaManualGrid";
	private static final long serialVersionUID = 1L;
	public static final String NAME = "agrupadorPeticoesAvulsasHome";
	private List<ProcessoDocumentoPeticaoNaoLida> listaProcessoComPeticaoAvulsa = new ArrayList<ProcessoDocumentoPeticaoNaoLida>(0);	
	private Boolean checkAllProcessoComPeticaoAvulsa = Boolean.FALSE;
	private Long resultadoTotal;
	
	@In
	private ProcessoDocumentoPeticaoNaoLidaDAO processoDocumentoPeticaoNaoLidaDAO;

	
	public String getGridId() {
		return PROCESSO_PETICAO_AVULSA_MANUAL_GRID;
	}
	
	/**
	 * [PJEII-7201] : Antonio Lucas/Sérgio Ricardo (14/05/2013)
	 * Remove os registros selecionados da listagem do agrupador.
	 */
	public void removerDoAgrupador() {
		for (ProcessoDocumentoPeticaoNaoLida pd : getListaProcessoComPeticaoAvulsa()) {
			pd.setRetirado(true);
			EntityUtil.getEntityManager().merge(pd);
		}
		EntityUtil.getEntityManager().flush();	
		listaProcessoComPeticaoAvulsa.clear();
		setCheckAllProcessoComPeticaoAvulsa(Boolean.FALSE);
		refreshGrid(getGridId());
		this.refreshResultadoTotal();
	}

	public Long getResultadoGeral() {
		return processoDocumentoPeticaoNaoLidaDAO.countAgrupadorPeticoesAvulsas();
	}

	public Long getResultadoTotal(){
		if (resultadoTotal != null) {
			return resultadoTotal;
		}
		this.resultadoTotal = getResultadoGeral();
		return resultadoTotal;
	}
	public void refreshResultadoTotal(){
		this.resultadoTotal = null;
		this.resultadoTotal = getResultadoGeral();
	}
	
	
	public void criarListaPedido(ProcessoDocumentoPeticaoNaoLida obj, String grid){
		if (grid.equals(PROCESSO_PETICAO_AVULSA_MANUAL_GRID)) {
			if (obj == null){
				listaProcessoComPeticaoAvulsa = checkAll(grid, listaProcessoComPeticaoAvulsa, checkAllProcessoComPeticaoAvulsa);
				refreshGrid(PROCESSO_PETICAO_AVULSA_MANUAL_GRID);
			}
			else{
				listaProcessoComPeticaoAvulsa = addRemove(obj, listaProcessoComPeticaoAvulsa);
			}
		}
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> addRemove(ProcessoDocumentoPeticaoNaoLida obj, List<ProcessoDocumentoPeticaoNaoLida> lista){
		if (lista.contains(obj)){
			lista.remove(obj);
		}
		else{
			lista.add(obj);
		}
		return lista;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ProcessoDocumentoPeticaoNaoLida> checkAll(String grid, List<ProcessoDocumentoPeticaoNaoLida> lista, Boolean checkAll){
		GridQuery gq = ComponentUtil.getComponent(grid);
		lista.clear();
		if (checkAll) {
			List<Map> l = gq.getFullList();
			for (Map h : l) {
				lista.add((ProcessoDocumentoPeticaoNaoLida) h.get("pd"));
			}
		}
		return lista;
	}
	
	//TODO: metodo copiado do AbstractHome; Ver se nao deveria ficar em um lugar melhor do que em um home.
	public void refreshGrid(String gridId) {
		GridQuery g = (GridQuery) Component.getInstance(gridId, false);
		if (g != null) {
			g.refresh();
		}
	}



	public Boolean getCheckAllProcessoComPeticaoAvulsa() {
		return checkAllProcessoComPeticaoAvulsa;
	}

	public void setCheckAllProcessoComPeticaoAvulsa(
			Boolean checkAllProcessoComPeticaoAvulsa) {
		this.checkAllProcessoComPeticaoAvulsa = checkAllProcessoComPeticaoAvulsa;
	}

	public List<ProcessoDocumentoPeticaoNaoLida> getListaProcessoComPeticaoAvulsa() {
		return listaProcessoComPeticaoAvulsa;
	}

	public void setListaProcessoComPeticaoAvulsa(
			List<ProcessoDocumentoPeticaoNaoLida> listaProcessoComPeticaoAvulsa) {
		this.listaProcessoComPeticaoAvulsa = listaProcessoComPeticaoAvulsa;
	}

}
