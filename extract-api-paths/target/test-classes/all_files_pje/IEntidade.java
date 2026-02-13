package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Transient;

/**
 * Idealmente, todas as entidades deveriam implementar esta interface.
 * @author mwborges
 * @param <E> A classe da entidade em si.
 * @param <I> A classe do Id.
 */
public interface IEntidade<E extends Serializable, I extends Serializable> extends java.io.Serializable {
	@Transient
	Class<? extends E> getEntityClass();
	
	@Transient
	I getEntityIdObject();

	boolean isLoggable();
}
