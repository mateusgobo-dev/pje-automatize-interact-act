/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoSegredoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente de gerenciamento da entidade {@link ProcessoSegredo}.
 * 
 * @author cristof
 *
 */
@Name(ProcessoSegredoManager.NAME)
public class ProcessoSegredoManager extends BaseManager<ProcessoSegredo> {
	
	public static final String NAME = "processoSegredoManager";
	
	@In
	private ProcessoSegredoDAO processoSegredoDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoSegredoDAO getDAO() {
		return processoSegredoDAO;
	}
	
	/**
	 * Recupera a lista de solicitações de segredo ou sigilo realizadas para um dado processo judicial.
	 * 
	 * @param processo o processo a respeito do qual se pretende fazer a pesquisa.
	 * @return a lista de solicitações, que pode ser vazia
	 * @throws PJeBusinessException
	 */
	public List<ProcessoSegredo> getSolicitacoes(ProcessoTrf processo) throws PJeBusinessException{
		return processoSegredoDAO.getSolicitacoesSegredo(processo);
	}
	
	/**
	 * Recupera a lista e solicitações de segredo ou sigilo realizadas para um dado processo judicial e
	 * que ainda não foram rejeitadas ou acatadas.
	 * 
	 * @param processo o processo a respeito do qual se pretende fazer a pesquisa
	 * @return a lista de solicitações não apreciadas
	 * @throws PJeBusinessException
	 */
	public List<ProcessoSegredo> getSolicitacoesPendentes(ProcessoTrf processo) throws PJeBusinessException {
		return processoSegredoDAO.getSolicitacoesPendentes(processo);
	}
	
	public void removerProcessoSegredoPendente(ProcessoTrf processo) throws PJeBusinessException { 
		processoSegredoDAO.removerProcessoSegredoPendente(processo); 
	}
	   
	public void removerProcessoSegredoPendenteUsuarioLogado(ProcessoTrf processo) throws PJeBusinessException { 
		processoSegredoDAO.removerProcessoSegredoPendenteUsuarioLogado(processo); 
	}
 
	public ProcessoSegredo gravarProcessoSegredo(ProcessoSegredo processoSegredo) throws PJeBusinessException { 
		processoSegredo.setDtAlteracao(new Date()); 
		processoSegredo = processoSegredoDAO.persist(processoSegredo); 
		return processoSegredo;  
	}

	/**
	 * metodo responsavel por recuperar todos os @ProcessoSegredo cadastrados pela pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 */
	public List<ProcessoSegredo> recuperaSegredoProcessosCadastrados(Pessoa _pessoa) {
		return processoSegredoDAO.recuperaSegredoProcessosCadastrados(_pessoa);
	}
}
