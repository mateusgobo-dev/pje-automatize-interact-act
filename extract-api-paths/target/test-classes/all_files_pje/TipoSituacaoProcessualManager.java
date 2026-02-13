/**
 * pje-manager
 * Copyright (C) 2014 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TipoSituacaoProcessualDAO;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link TipoSituacaoProcessual}.
 * 
 * @author cristof
 *
 */
@Name("tipoSituacaoProcessualManager")
public class TipoSituacaoProcessualManager extends BaseManager<TipoSituacaoProcessual> {
	
	@In
	private TipoSituacaoProcessualDAO tipoSituacaoProcessualDAO;

	@Override
	protected TipoSituacaoProcessualDAO getDAO() {
		return tipoSituacaoProcessualDAO;
	}
	
    public List<TipoSituacaoProcessual> recuperaTiposAtivos(){
    	Search s = new Search(TipoSituacaoProcessual.class);
    	addCriteria(s, 
    			Criteria.equals("ativo", true));
    	return list(s);
    }
    
    public List<TipoSituacaoProcessual> recuperaIncompativeis(TipoSituacaoProcessual tipo){
    	TipoSituacaoProcessual t = getDAO().find(getId(tipo));
    	List<TipoSituacaoProcessual> ret = new ArrayList<TipoSituacaoProcessual>(t.getTiposSituacoesIncompatives());
    	return ret;
    }

	public TipoSituacaoProcessual findByCodigo(String codigoTipoSituacao) {
		Search s = new Search(TipoSituacaoProcessual.class);
		addCriteria(s, 
				Criteria.equals("codigo", codigoTipoSituacao),
				Criteria.equals("ativo", true));
		s.setMax(1);
		List<TipoSituacaoProcessual> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

	public TipoSituacaoProcessual create() {
		TipoSituacaoProcessual tipo = new TipoSituacaoProcessual();
		tipo.setAtivo(true);
		return tipo;
	}

	public List<TipoSituacaoProcessual> pesquisaSituacaoProcessual(TipoSituacaoProcessual tipoSituacaoProcessual) {			
 		return tipoSituacaoProcessualDAO.pesquisaSituacaoProcessual(tipoSituacaoProcessual);
 	}
 
 	public List<TipoSituacaoProcessual> pesquisaSituacaoProcessualAll(TipoSituacaoProcessual tipoSituacaoProcessual) {			
 		return tipoSituacaoProcessualDAO.findAll();
} 
}
