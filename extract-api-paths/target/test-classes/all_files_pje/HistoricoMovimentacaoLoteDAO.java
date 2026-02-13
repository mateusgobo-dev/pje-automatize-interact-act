/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.HistoricoMovimentacaoLote;

/**
 * Componente de acesso a dados da entidade {@link HistoricoMovimentacaoLote}.
 * 
 * @author cristof
 *
 */
@Name("historicoMovimentacaoLoteDAO")
public class HistoricoMovimentacaoLoteDAO extends BaseDAO<HistoricoMovimentacaoLote> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Long getId(HistoricoMovimentacaoLote e) {
		return e.getIdHistoricoMovimentacaoLote();
	}

}
