package br.com.infox.validator;

import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.util.DateUtil;

@org.jboss.seam.annotations.faces.Validator(id = "prazoLegalSessaoMuralValidator")
@BypassInterceptors
@Name("prazoLegalSessaoMuralValidator")
public class PrazoLegalSessaoMuralValidator implements Validator {
	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if(value instanceof Integer){
			Integer valor = (Integer) value;
			if (valor <= 0) {
				String msgErro = "O prazo do expediente deve ser superior a zero. Se não houver necessidade de controle do prazo do expediente, selecione o Tipo do Prazo 'sem prazo'";
				gerarMsgErro(msgErro);
			}
		}else if(value instanceof Date){
			Date dataInformada = (Date) value;
			Date dataDeHoje = DateUtil.getBeginningOfDay(new Date());
			
			if(DateUtil.isDataMenor(dataInformada, dataDeHoje)){
				String msgErro = "Não é permitido informar data retroativa.";
				gerarMsgErro(msgErro);
			}
		}
	}

	private void gerarMsgErro(String msgErro) {
		if(FacesUtil.getCurrentMessages().isEmpty() || !verificarSeMensagemJaFoiAdicionada(msgErro)){
			FacesMessage fm = new FacesMessage(msgErro);
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			throw new ValidatorException(fm);
		}
	}
	
	private boolean verificarSeMensagemJaFoiAdicionada(String msg){
		boolean retorno = false;
		List<FacesMessage> currentMessages = FacesUtil.getCurrentMessages();
		for (FacesMessage facesMessage : currentMessages) {
			if(facesMessage.getDetail().equals(msg)){
				retorno = true;
				break;
			}
		}
		return retorno;
	}
}
