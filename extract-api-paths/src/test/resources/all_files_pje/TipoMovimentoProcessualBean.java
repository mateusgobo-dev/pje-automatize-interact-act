/**
 * pje-web
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.view.EntityTreeModel.DataRetriever;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Operator;
import br.jus.pje.search.Search;

/**
 * Componente de seleção de tipos de movimentação processual.
 * 
 * @author cristof
 *
 */
public class TipoMovimentoProcessualBean {
	
	private EventoManager eventoManager;
	
	private EntityTreeModel<Evento> treeModel;
	
	/**
	 * Construtor padrão a ser utilizado.
	 * 
	 * @param eventoManager o componente de gerência da entidade de tipo de movimentação
	 */
	public TipoMovimentoProcessualBean(EventoManager eventoManager){
		this.eventoManager = eventoManager;
		final EventoManager manager = this.eventoManager;
		DataRetriever<Evento> retriever = new DataRetriever<Evento>() {
			@Override
			public Object getId(Evento entity) {
				return manager.getId(entity);
			}
			@Override
			public List<Evento> getRoots(Search search) {
				try {
					List<Criteria> crits = new ArrayList<Criteria>();
					Agrupamento grp = null;
					for(Criteria c: search.getCriterias().values()){
						if(c.getOperator() == Operator.equals && c.getAttribute().equals("eventoAgrupamentoList.agrupamento")){
							grp = (Agrupamento) c.getValue().get(0);
						}else if(!c.isChild()){
							crits.add(c.copy());
						}
					}
					Search s = new Search(Evento.class);
					s.addCriteria(Criteria.equals("eventoAgrupamentoList.agrupamento", grp));
					List<Evento> grouped = manager.list(s);
					List<Criteria> breads = new ArrayList<Criteria>();
					for(Evento ev: grouped){
						breads.add(Criteria.startsWith("breadcrumb", ev.getBreadcrumb()));
					}
					Criteria or = Criteria.or(breads.toArray(new Criteria[breads.size()]));
					s = new Search(Evento.class);
					s.addCriteria(crits);
					s.addCriteria(or);
					List<Evento> filtered = manager.list(s);
					Set<Evento> ret = new HashSet<Evento>(filtered.size());
					for(Evento e: filtered){
						ret.add(e.getHierarchy().get(0));
					}
					return new ArrayList<Evento>(ret);
				} catch (Exception e) {
					return Collections.emptyList();
				}
			}
			@Override
			public List<Evento> listChildren(Evento parent, Search search) {
				List<Criteria> criterios = new ArrayList<Criteria>();
				Agrupamento grp = null;
				for(Criteria c: search.getCriterias().values()){
					if(c.getOperator() == Operator.equals && c.getAttribute().equals("eventoAgrupamentoList.agrupamento")){
						grp = (Agrupamento) c.getValue().get(0);
					}else if(!c.isChild()){
						criterios.add(c.copy());
					}
				}
				if(grp == null){
					return Collections.emptyList();
				}else{
					try {
						return manager.findFilteredChildren(parent, grp, criterios.toArray(new Criteria[criterios.size()]));
					} catch (PJeBusinessException e) {
						e.printStackTrace();
						return Collections.emptyList();
					}
				}
			}
		};
		treeModel = new EntityTreeModel<Evento>(Evento.class, retriever);
	}
	
	/**
	 * Recupera a árvore de tipos de movimentação.
	 * 
	 * Não deve ser utilizado no momento (em desenvolvimento).
	 * 
	 * @return a árvore de tipos de movimentações 
	 */
	public EntityTreeModel<Evento> getTreeModel() {
		return treeModel;
	}
	
	/**
	 * Recupera os tipos de movimentação que são descendentes de um dado tipo, pertencem a um agrupamento
	 * e têm o código indicado no filtro ou o texto do filtro em sua descrição.
	 * 
	 * @param tipoPai o tipo de que devem ser descendentes os elementos da lista
	 * @param filtro o filtro textual
	 * @param agrupamento o agrupamento a que devem estar vinculados os tipos de movimentação
	 * @return a lista de tipos de movimentação.
	 * @throws PJeBusinessException
	 */
	public List<Evento> recuperaTipos(Evento tipoPai, String filtro, Agrupamento agrupamento) throws PJeBusinessException{
		String trimmed = filtro == null ? null : StringUtil.fullTrim(filtro).replaceAll("%", "");
		if(trimmed != null && !trimmed.isEmpty()){
			Criteria or = Criteria.or(
					Criteria.equals("code", trimmed),
					Criteria.contains("description", trimmed));
			return eventoManager.findFilteredChildren(tipoPai, agrupamento, or); 
		}else{
			return eventoManager.findFilteredChildren(tipoPai, agrupamento);
		}
	}
	
}
