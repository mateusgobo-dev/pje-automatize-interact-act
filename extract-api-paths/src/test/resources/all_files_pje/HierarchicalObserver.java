/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.observer;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import br.jus.pje.nucleo.entidades.HierarchicEntity;

/**
 * Componente de observação de eventos pertinentes a entidades hierárquicas.
 * 
 * @author cristof
 *
 */
@Name("hierarchicalObserver")
public class HierarchicalObserver {
	
	@Logger
	private Log logger;
	
	@In
	private EntityManager entityManager;
	
	/**
	 * Método de tratamento do evento de atualização de filhos de entidades hierárquicas.
	 * 
	 * @param entity a entidade a ser tratada.
	 */
	@SuppressWarnings("unchecked")
	@Observer(value=HierarchicEntity.UPDATE_CHILDREN_EVENT)
	@Transactional
	public <T extends HierarchicEntity<T>> void childrenUpdateObserver(T entity){
		entity = (T) entityManager.find(entity.getClass(), entity.getId());
		List<T> children = entity.getChildren();
		for(T child: children){
			boolean updated = child.updateHierarchy();
			if(updated && !child.isCircular()){
				childrenUpdateObserver(child);
			}
		}
		entityManager.flush();
	}
	
}
