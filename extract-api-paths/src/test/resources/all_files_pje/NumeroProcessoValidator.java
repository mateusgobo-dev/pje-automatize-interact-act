package br.com.infox.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.NumeroProcessoUtil;

@org.jboss.seam.annotations.faces.Validator(id = "numeroProcessoValidator")
@Name("numeroProcessoValidator")
@BypassInterceptors
public class NumeroProcessoValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String numeroProcessoValue = (String) value;
		String aux = numeroProcessoValue.replaceAll("\\D", ""); 
		if(aux != null && !aux.isEmpty()){
			if ((!regexNumeroProcessoMask(numeroProcessoValue))
					&& (!regexNumeroProcesso(numeroProcessoValue))) {
				throw new ValidatorException(new FacesMessage("Número do processo inválido"));
			}
			if(!NumeroProcessoUtil.numeroProcessoValido(numeroProcessoValue)) {
				throw new ValidatorException(new FacesMessage("Número do processo inválido"));
			}
			if(!NumeroProcessoUtil.numeroProcessoValidoNaOrigem(numeroProcessoValue)) {
				throw new ValidatorException(new FacesMessage("Número do processo inexistente na instância inferior do PJe"));
			}
		}
	}
	
	/**
	 * Método que verifica se o parâmetro atende as regras da Expressão Regular 
	 * [_]{7}-[_]{1,2}.[_]{4}.[0-9]{1}.[0-9]{2}.[_]{1,4}, como por exemplo,
	 * _______-__.____.5.12.____.
	 * @author Ronny Paterson (ronny.silva@trt8.jus.br)
	 * @since 1.4.6
	 * @see 
	 * @category PJE-JT	 
	 */
	private boolean regexNumeroProcessoMask(String expressaoAvaliada) {
		if(expressaoAvaliada == null){
			return true;
		}
		String regex = "[_]{7}-[_]{1,2}.[_]{4}.[0-9]{1}.[0-9]{2}.[_]{1,4}";
		return regex(expressaoAvaliada, regex);
	}
	
	/**
	 * Método que verifica se o parâmetro atende as regras da Expressão Regular 
	 * [0-9]{7}-[0-9]{1,2}.[0-9]{4}.[0-9]{1}.[0-9]{2}.[0-9]{1,4}, como por exemplo,
	 * 0800023-63.2011.5.12.0002.
	 * @author Ronny Paterson (ronny.silva@trt8.jus.br)
	 * @since 1.4.0.1
	 * @see 
	 * @category PJE-JT	 
	 */
	private boolean regexNumeroProcesso(String expressaoAvaliada) {
		String regex = "[0-9]{7}-[0-9]{1,2}.[0-9]{4}.[0-9]{1}.[0-9]{2}.[0-9]{1,4}";
		return regex(expressaoAvaliada, regex);
	}
	
	/**
	 * Método que verifica se o conteúdo de uma String corresponde ao padrão
	 * informado nas regras da Expressão Regular.
	 * @author Ronny Paterson (ronny.silva@trt8.jus.br)
	 * @since 1.4.6
	 * @see 
	 * @category PJE-JT	 
	 */
	private boolean regex(String expressaoAvaliada, String regex) {		
		Pattern padrao = Pattern.compile(regex);
		Matcher combinador = padrao.matcher(expressaoAvaliada);		
		return combinador.matches();		
	}
}