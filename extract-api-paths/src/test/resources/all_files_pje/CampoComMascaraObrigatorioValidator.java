package br.com.infox.validator;


import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.converter.MaskConverter;


/**
 * Validator responsável por validar campos que são submetidos com máscara, por
 * exemplo ___.___.___-__, mas que ainda precisam ser tratados como obrigatórios
 * pelo ciclo de vida do JSF.
 */
@BypassInterceptors
@org.jboss.seam.annotations.faces.Validator
@Name("campoComMascaraObrigatorioValidator")
public class CampoComMascaraObrigatorioValidator implements Validator {

    private static final String CAMPO_OBRIGATORIO = "campo obrigatório";

    private final MaskConverter maskConverter = new MaskConverter();

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (isCampoObrigatorio(component)) {
            Object valorCampo = value;

            if (value != null) {
                valorCampo = maskConverter.getAsObject(context, component, value.toString());
            }

            if (valorCampo == null) {
                invalidarCampo();
            }
        }
    }

    /**
     * Valida se o componente foi definida como obrigatório.
     * 
     * @param component
     * 
     * @return {@code true} caso o componente seja obrigatório.
     */
    private boolean isCampoObrigatorio(UIComponent component) {
        return (Boolean) component.getAttributes().get("required");
    }

    /**
     * @throws ValidatorException para interromper a execução do ciclo de vida JSF.
     */
    private void invalidarCampo() {
        throw new ValidatorException(new FacesMessage(SEVERITY_ERROR, CAMPO_OBRIGATORIO, CAMPO_OBRIGATORIO));
    }
}
