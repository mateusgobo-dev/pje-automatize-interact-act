package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.Etnia;

@Name("etniaHome")
@BypassInterceptors
public class EtniaHome extends AbstractEtniaHome<Etnia>{

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(EtniaHome.class);

	@Override
	public String remove(Etnia obj){
		obj.setAtivo(Boolean.FALSE);
		return super.remove(obj);
	}

	@Override
	public String update(){
		String ret = null;
		try{
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			ret = getUpdatedMessage().getValue().toString();
			instance().add(StatusMessage.Severity.ERROR, "Registro alterado com sucesso");
		} catch (Exception e){
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException){
				instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

	@Override
	public boolean isEditable(){
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
	
	@Override
	/**
	 * Cria uma instância nova da entidade tipada, permitindo que o desenvolvedor
	 * solicite que a entidade antiga seja desligada no contexto de gerenciamento JPA.
	 *  
	 * @param detach true, para desligar a entidade antiga do contexto de gerenciamento JPA.
	 */
	public void clearInstance(boolean detach){

		if (super.isManaged()) {
			try {
				// Faz com que o hibernate pare de gerenciar o objeto, mantendo suas propriedades para reaproveitamento nos locks.
				if(detach){
					((Session) getEntityManager().getDelegate()).evict(instance);
				}
			} catch (Exception e) {
				// Ignora a possível exceção lançada, por exemplo, caso a
				// entidade não seja encontrada.
				log.error("Erro ao limpar a instância atual: [" + e.getLocalizedMessage() + "].", e);
			}
		}

		setId(null);
		clearForm();
		instance = createInstance();
	}

}