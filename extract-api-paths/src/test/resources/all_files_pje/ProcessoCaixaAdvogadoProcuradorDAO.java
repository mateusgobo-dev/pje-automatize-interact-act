/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Arrays;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente de acesso a dados da entidade {@link ProcessoCaixaAdvogadoProcurador}.
 * 
 * @author cristof
 * @since 1.4.6.2.RC4
 */
@Name("processoCaixaAdvogadoProcuradorDAO")
public class ProcessoCaixaAdvogadoProcuradorDAO extends BaseDAO<ProcessoCaixaAdvogadoProcurador> {
	
	@Override
	public Integer getId(ProcessoCaixaAdvogadoProcurador tag) {
		return tag.getIdProcessoCaixaAdvogadoProcurador();
	}

	/**
	 * Remove todos os processos indicados da caixa informada.
	 * 
	 * @param cx a caixa da qual os processos devem ser removidos.
	 * @param processos os processos a serem removidos
	 */
	public void remover(CaixaAdvogadoProcurador cx, ProcessoTrf...processos) {
		if(processos == null || processos.length == 0){
			return;
		}
		String query = "DELETE FROM ProcessoCaixaAdvogadoProcurador AS tag "
				+ "	WHERE tag.caixaAdvogadoProcurador = :caixa "
				+ "		AND tag.processoTrf IN (:processos)";
		Query q = entityManager.createQuery(query);
		q.setParameter("caixa", cx);
		q.setParameter("processos", Arrays.asList(processos));
		q.executeUpdate();
		entityManager.flush();
	}

}
