/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.entidades.listeners;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.jboss.seam.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.jus.pje.nucleo.entidades.HierarchicEntity;

/**
 * Classe de tratamento de chamadas de entidades hierárquicas.
 * 
 * @author cristof
 *
 */
public class HierarchicEntityListener {
	
	private static Logger logger = LoggerFactory.getLogger(HierarchicEntityListener.class);
	
	/**
	 * Trata a persistência de entidades hierárquicas.
	 * 
	 * @param hierarchic a entidade hierárquica em vias de ser persistida
	 */
	@PrePersist
	public <T extends HierarchicEntity<T>> void prePersist(T hierarchic){
		logger.trace("Interceptando chamada de persistência da entidade [{}].", hierarchic);
		hierarchic.updateHierarchy();
	}
	
	/**
	 * Trata a atualização de entidades de hierárquicas.
	 * 
	 * @param hierarchic a entidade hierárquica em vias de ser atualizada.
	 * @see HierarchicalObserver#childrenUpdateObserver(HierarchicEntity)
	 */
	@PreUpdate
	public <T extends HierarchicEntity<T>> void preUpdate(T hierarchic){
		logger.trace("Interceptando chamada de atualização da entidade [{}]", hierarchic);
		boolean updated = hierarchic.updateHierarchy();
		if(updated){
			Events.instance().raiseAsynchronousEvent(HierarchicEntity.UPDATE_CHILDREN_EVENT, hierarchic);
		}
		logger.trace("Finalizada a interceptação da chamada de atualização da entidade [{}]", hierarchic);
	}
	
}
