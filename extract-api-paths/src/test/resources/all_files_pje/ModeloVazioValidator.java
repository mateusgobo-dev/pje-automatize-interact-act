package br.com.infox.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;

@org.jboss.seam.annotations.faces.Validator(id = "modeloVazioValidator")
@Name("modeloVazioValidator")
@BypassInterceptors
public class ModeloVazioValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		try {
			String modelo = (String) value;
			if (ProcessoDocumentoBinHome.isModeloVazio(modelo)) {
				throw new ValidatorException(new FacesMessage("É obrigatória a inclusão de texto no editor."));
			}
		} catch (Exception e) {
			throw new ValidatorException(new FacesMessage("É obrigatória a inclusão de texto no editor."), e);
		}
	}
}