package br.com.infox.validator;

import br.com.itx.util.FacesUtil;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * PJEII-22344 - Validação do componente <wi:suggest /> para considerar na obrigatoriedade se um valor foi selecionado.
 */
@BypassInterceptors
@Name("suggestionValidator")
@org.jboss.seam.annotations.faces.Validator
public class SuggestionValidator implements Validator {

    private static final String ATTR_OBJETO_SELECIONADO = "selection";
    private static final String ATTR_MENSAGEM_VALIDACAO = "requiredSelectionMessage";
    private static final String ATTR_OBJETO_SELECIONADO_OBRIGATORIO = "requiredSelection";

    /**
     * Valida a obrigatoriedade do componente.
     *
     * @param facesContext contexto jsf
     * @param component component inputText
     * @param o value do component
     *
     * @throws ValidatorException caso nenhum valor selecionado.
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object o) throws ValidatorException {
        if (sePreenchimentoOpcional(component)) {
            return;
        }

        Object objetoSelecionado = getAtributoObjeto(component);

        if (objetoSelecionado == null) {
            FacesMessage mensagemValidacao = criarMensagemValidacao(component);
            throw new ValidatorException(mensagemValidacao);
        }
    }

    /**
     * Cria o FacesMessage para apresentar no componente.
     *
     * @param component
     *
     * @return FacesMessage
     */
    private FacesMessage criarMensagemValidacao(UIComponent component) {
        String mensagemValidacao = obterMensagemValidacao(component);
        return FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, mensagemValidacao);
    }

    /**
     * Cria a mensagem de validação do componente.
     * Caso não tenha sido definida uma, a mensagem padrão é a definida na chave javax.faces.component.UIInput.REQUIRED
     * do bundle.
     *
     * @param component
     *
     * @return mensagem de validação do componente.
     */
    private String obterMensagemValidacao(UIComponent component) {
        String mensagem = getAtributoMensagem(component);

        if (StringUtils.isBlank(mensagem)) {
            mensagem = FacesUtil.getMessage(FacesUtil.BUNDLE_MENSAGENS, UIInput.REQUIRED_MESSAGE_ID);
        }

        return mensagem;
    }

    /**
     * Obtém do componente a informação se o campo é opcional.
     *
     * @param component
     *
     * return boolean informando se o campo é opcional ou não
     */
    private Boolean sePreenchimentoOpcional(UIComponent component) {
        String atributoObrigatoriedade = (String) component.getAttributes().get(ATTR_OBJETO_SELECIONADO_OBRIGATORIO);
        return !Boolean.valueOf(atributoObrigatoriedade);
    }

    /**
     * Obtém o atributo referente ao objeto selecionado no componente.
     *
     * @param component
     *
     * return objeto selecionado
     */
    private Object getAtributoObjeto(UIComponent component) {
        return component.getAttributes().get(ATTR_OBJETO_SELECIONADO);
    }

    /**
     * Obtém atributo da mensagem de validação para o componente.
     *
     * @param component
     *
     * @return atributo com a mensagem de validação.
     */
    private String getAtributoMensagem(UIComponent component) {
        return (String) component.getAttributes().get(ATTR_MENSAGEM_VALIDACAO);
    }
}
