package br.com.infox.validator;

import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

@org.jboss.seam.annotations.faces.Validator(id = "prazoLegalValidator")
@Name("prazoLegalValidator")
@BypassInterceptors
public class PrazoLegalValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		
		if(value instanceof Integer){
			Integer valor = (Integer) value;
			
			if (valor <= 0) {
				String msgErro = "O prazo do expediente deve ser superior a zero. Se não houver necessidade de controle do prazo do expediente, selecione o Tipo do Prazo 'sem prazo'";
				
				FacesMessage fm = new FacesMessage(msgErro);
				FacesMessages.instance().add(Severity.ERROR, msgErro);
				
				throw new ValidatorException(fm);
				
			}
		}else if(value instanceof Date){
			//[PJEII-17754] - Impede que o usuário infome data retroativa para o tipo de prazo "data certa".
			
			Date dataInformada = (Date) value;
			Calendar dataDeHoje = Calendar.getInstance();
			
			dataDeHoje.setTime(new Date());
			
			dataDeHoje.set(Calendar.HOUR_OF_DAY, 0);
			dataDeHoje.set(Calendar.MINUTE, 0);  
			dataDeHoje.set(Calendar.SECOND, 0); 
			
			if(dataInformada.before(dataDeHoje.getTime()) && !dataInformada.equals(dataDeHoje.getTime())){
				String msgErro = "Não é permitido informar data retroativa.";
				
				FacesMessage fm = new FacesMessage(msgErro);
				FacesMessages.instance().add(Severity.ERROR, msgErro);
				
				throw new ValidatorException(fm);
			}
		}
		
	}

}
