/**
 *  pje-web
 *  Copyright (C) 2014 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.RegistroIntimacaoDAO;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.RegistroIntimacao;

/**
 * Componente de gerenciamento negocial da entidade {@link RegistroIntimacao}
 * 
 * @author cristof
 *
 */
@Name("registroIntimacaoManager")
public class RegistroIntimacaoManager extends BaseManager<RegistroIntimacao> {
	
	@In
	private RegistroIntimacaoDAO registroIntimacaoDAO;

	@Override
	protected RegistroIntimacaoDAO getDAO() {
		return registroIntimacaoDAO;
	}

	public RegistroIntimacao getRegistroIntimacao() {
		RegistroIntimacao r = new RegistroIntimacao();
		r.setData(new Date());
		return r;
	}
	
	/**
	 * Este método retorna um {@link RegistroIntimacao} conforme seu respectivo {@link ProcessoParteExpediente}
	 * Caso a consulta retorne mais de um registro, retornará o primeiro da lista
	 * @param ppe ProcessoParteExpediente
	 * @return {@link RegistroIntimacao}
	 */	
	public RegistroIntimacao recuperarRegistroIntimacaoPorProcessoParteExpediente(ProcessoParteExpediente ppe){
		return registroIntimacaoDAO.recuperarRegistroIntimacaoPorProcessoParteExpediente(ppe);
	}

}
