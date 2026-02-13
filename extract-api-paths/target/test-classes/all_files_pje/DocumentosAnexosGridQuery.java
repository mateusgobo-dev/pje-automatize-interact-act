package br.jus.csjt.pje.commons.component.grid;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.grid.GridColumn;
import br.com.itx.component.grid.GridQuery;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class DocumentosAnexosGridQuery extends GridQuery {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void checkApreciarTodos() {
		GridColumn column = getColumn("apreciado");
		column.setCheckedAll(true);
		
		List<ProcessoDocumento> itens = getFullList();
		
		for(ProcessoDocumento pd : itens) {
			if(pd.getAtivo() != null && pd.getAtivo()) {
				getSelectedRowsList().add(pd);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void uncheckApreciarTodos() {
		GridColumn column = getColumn("apreciado");
		column.setCheckedAll(false);
		
		List<ProcessoDocumento> itens = getFullList();
		
		for(ProcessoDocumento pd : itens) {
			if(pd.getAtivo() != null && pd.getAtivo()) {
				getSelectedRowsList().remove(pd);
			}
		}
	}
}
