package br.jus.cnj.pje.business.dao;

import java.util.Arrays;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoParteExpedienteCaixaAdvogadoProcuradorDAO")
public class ProcessoParteExpedienteCaixaAdvogadoProcuradorDAO extends BaseDAO<ProcessoParteExpedienteCaixaAdvogadoProcurador>{
	
	@Override
	public Integer getId(ProcessoParteExpedienteCaixaAdvogadoProcurador e) {
		// TODO Auto-generated method stub
		return e.getIdProcessoParteExpedienteCaixaAdvogadoProcurador();
	}
	
	/**
	 * Remove todos os processos indicados da caixa informada.
	 * 
	 * @param cx a caixa da qual os processos devem ser removidos.
	 * @param expediente os expedientes a serem removidos
	 */
	public void remover(CaixaAdvogadoProcurador cx, ProcessoParteExpediente...expedientes) {
		if(expedientes == null || expedientes.length == 0){
			return;
		}
		String query = "DELETE FROM ProcessoParteExpedienteCaixaAdvogadoProcurador AS tag "
				+ "	WHERE tag.caixaAdvogadoProcurador = :caixa "
				+ "		AND tag.processoParteExpediente IN (:expedientes)";
		Query q = entityManager.createQuery(query);
		q.setParameter("caixa", cx);
		q.setParameter("expedientes", Arrays.asList(expedientes));
		q.executeUpdate();
		entityManager.flush();
	}
	
}
