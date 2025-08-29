package br.com.infox.converter;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.ui.AbstractEntityLoader;

/**
 * PJE-JT: David Vieira: [PJE-779] Conversor para Entidades que já inclui a
 * conversão de valores não selecionados. Criado por conta de um bug no
 * encadeamento de conversores do Seam/Facelet
 */
@Name("entityConverterNoSelectionWorkaround")
@Scope(CONVERSATION)
@Install(precedence = BUILT_IN)
@Converter
@BypassInterceptors
public class EntityConverterNoSelectionWorkaround implements javax.faces.convert.Converter, Serializable {

	private static final String NO_SELECTION_VALUE = "org.jboss.seam.ui.NoSelectionConverter.noSelectionValue";
	private AbstractEntityLoader entityLoader;

	public AbstractEntityLoader getEntityLoader() {
		if (entityLoader == null) {
			return AbstractEntityLoader.instance();
		} else {
			return entityLoader;
		}
	}

	public void setEntityLoader(AbstractEntityLoader entityLoader) {
		this.entityLoader = entityLoader;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public String getAsString(FacesContext facesContext, UIComponent cmp, Object value) throws ConverterException {
		if (value == null) {
			return NO_SELECTION_VALUE;
		}
		if (value instanceof String) {
			return (String) value;
		}
		return getEntityLoader().put(value);
	}

	@Override
	@Transactional
	public Object getAsObject(FacesContext facesContext, UIComponent cmp, String value) throws ConverterException {
		if (value == null || value.length() == 0 || (value instanceof String && value.equals(NO_SELECTION_VALUE))) {
			return null;
		}
		return getEntityLoader().get(value);
	}

}