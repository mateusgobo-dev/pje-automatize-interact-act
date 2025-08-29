/**
 *  pje-web
 *  Copyright (C) 2014 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.RegistroIntimacao;

/**
 * Componente de acesso a dados da entidade {@link RegistroIntimacao}.
 * 
 * @author cristof
 *
 */
@Name("registroIntimacaoDAO")
public class RegistroIntimacaoDAO extends BaseDAO<RegistroIntimacao> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(RegistroIntimacao r) {
		return r.getId();
	}
	
	/**
	 * Este método retorna um {@link RegistroIntimacao} conforme seu respectivo {@link ProcessoParteExpediente}
	 * Caso a consulta retorne mais de um registro, retornará o primeiro da lista
	 * @param ppe ProcessoParteExpediente
	 * @return {@link RegistroIntimacao}
	 */
	@SuppressWarnings("unchecked")
	public RegistroIntimacao recuperarRegistroIntimacaoPorProcessoParteExpediente(ProcessoParteExpediente ppe){
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM RegistroIntimacao o ");
		sb.append("WHERE o.processoParteExpediente = :ppe ");
		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ppe", ppe);
		List<RegistroIntimacao> registros = q.getResultList();
		
		if(registros != null && !registros.isEmpty()){
			return registros.get(0);
		} else {
			return null;
		}
		
	}

}
