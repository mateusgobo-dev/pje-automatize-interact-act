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

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.EstadoDAO;
import br.jus.pje.nucleo.entidades.Estado;

/**
 * Componente de gerenciamento da entidade {@link Estado}.
 * 
 * @author cristof
 *
 */
@Name(EstadoManager.NAME)
public class EstadoManager extends BaseManager<Estado> {
	
	public static final String NAME = "estadoManager";
	
	@In
	private EstadoDAO estadoDAO;

	/**
	 * @return Instância de EstadoManager.
	 */
	public static EstadoManager instance() {
		return (EstadoManager)Component.getInstance(EstadoManager.NAME);
	}
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected EstadoDAO getDAO() {
		return estadoDAO;
	}
	
	/**
	 * Recupera a lista de estados ativos na instalação ordenada pelo nome.
	 * 
	 * @return a lista de estados ativos
	 */
	public List<Estado> estadoItems() {
		return estadoDAO.estadoItems();
	}
	
	/**
	 * Recupera a lista de estados que têm a sigla dada.
	 * 
	 * @param sigla a sigla do estado
	 * @return a lista de estados que têm a sigla informada.
	 * @see #findBySigla
	 */
	@Deprecated
	public List<Estado> findByUf(String uf){
		return estadoDAO.findByUf(uf);
	}

	/**
	 * Recupera o estado ativo que tem por sigla a informada.
	 * 
	 * @param sigla a sigla do estado ativo que se pretende recuperar
	 * @return o estado ativo que tem a sigla dada, ou null se não existir
	 */
	public Estado findBySigla(String sigla){
		return estadoDAO.findBySigla(sigla);
	}
	
	/**
	 * Recupera os estados das jurisdicoes ativas.
	 * 
	 * @return Lista de estados.
	 */
	public List<Estado> recuperarPorJurisdicaoAtiva() {
		return estadoDAO.recuperarPorJurisdicaoAtiva();
	}
	
	/**
	 * Recupera os estados das jurisdições que possuem competência cadastrada.
	 * 
	 * @return Lista de estados
	 */
	public List<Estado> recuperarPorJurisdicaoCompetenciaAtiva() {
		return this.estadoDAO.recuperarPorJurisdicaoCompetenciaAtiva();
	}
}
