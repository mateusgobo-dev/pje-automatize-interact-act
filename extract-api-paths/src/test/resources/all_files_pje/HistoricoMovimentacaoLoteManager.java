/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.HistoricoMovimentacaoLoteDAO;
import br.jus.pje.nucleo.entidades.HistoricoMovimentacaoLote;

/**
 * Componente de controle negocial da entidade {@link HistoricoMovimentacaoLote}.
 * 
 * @author cristof
 *
 */
@Name("historicoMovimentacaoLoteManager")
public class HistoricoMovimentacaoLoteManager extends BaseManager<HistoricoMovimentacaoLote> {
	
	@In
	private HistoricoMovimentacaoLoteDAO historicoMovimentacaoLoteDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected HistoricoMovimentacaoLoteDAO getDAO() {
		return historicoMovimentacaoLoteDAO;
	}

}
