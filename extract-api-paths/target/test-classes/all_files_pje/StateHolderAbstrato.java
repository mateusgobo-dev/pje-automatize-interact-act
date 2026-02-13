/**
 * CNJ - Conselho Nacional de Justiça
 *
 * Data: 29/04/2016
 */
package br.com.infox.converter;

import java.io.Serializable;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;

import org.apache.commons.beanutils.BeanUtils;

/**
 * State holder para conversores e validadores.
 * 
 * @author Adriano Pamplona
 * @see StateHolder
 */
public abstract class StateHolderAbstrato implements StateHolder, Serializable {
	private boolean _transient;

	/**
	 * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.
	 *      FacesContext, java.lang.Object)
	 */
	@Override
	public void restoreState(final FacesContext facesContext, final Object objeto) {
		try {
			BeanUtils.copyProperties(this, objeto);
		} catch (Exception e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(final FacesContext facesContext) {
		try {
			return BeanUtils.cloneBean(this);
		} catch (final Exception e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * @see javax.faces.component.StateHolder#isTransient()
	 */
	@Override
	public boolean isTransient() {
		return _transient;
	}

	/**
	 * @see javax.faces.component.StateHolder#setTransient(boolean)
	 */
	@Override
	public void setTransient(boolean _transient) {
		this._transient = _transient;
	}
}
