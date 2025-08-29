/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Parametro;

/**
 * DAO para serviços de data/hora. Recuperação da data/hora do servidor de 
 * banco de dados. Possivelmente será descartado quando a utilização de uma
 * protocolizadora virar padrão no PJe.
 * @author Ricardo Scholz / David Vieira
 */
@Name("dateDAO")
public class DateDAO extends GenericDAO {

	/*
	 * PJE-JT: Ricardo Scholz / David Vieira : PJEII-6850 - 2013-05-21
	 * Criação de método que recupera a data atual do servidor de banco de 
	 * dados. As várias instâncias do JBoss não apresentam precisão de
	 * sincronização suficiente, causando diversos problemas na ordenação de 
	 * documentos, que é realizada com base na data de assinatura. Abordagem 
	 * ideal deve utilizar uma protocolizadora. 
	 */
	/**
	 * Método que fornece a data oficial do sistema. Atual implementação
	 * recupera a data do servidor de banco de dados.
	 * 
	 * @return um objeto data, contendo o timestamp atual do servidor de banco
	 * de dados.
	 */
	public Date getDataHoraAtual() {
		return (Date) EntityUtil.getEntityManager().
				createQuery("SELECT CURRENT_TIMESTAMP() FROM " + 
						Parametro.class.getSimpleName()).
							setMaxResults(1).getSingleResult();
	}
	/*
	 * PJE-JT: Fim.
	 */
}
