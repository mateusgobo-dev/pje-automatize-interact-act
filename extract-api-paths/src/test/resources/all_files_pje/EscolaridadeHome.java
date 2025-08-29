package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.pje.nucleo.entidades.Escolaridade;

@Name("escolaridadeHome")
@BypassInterceptors
public class EscolaridadeHome extends AbstractEscolaridadeHome<Escolaridade> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(EtniaHome.class);
	
	@Override
	public String persist() {
		if (checkEscolaridade() <= 0) {
			refreshGrid("escolaridadeGrid");
			return super.persist();
		} else {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Esta Escolaridade já está Cadastrada.");
			return "false";
		}
	}

	@Override
	public String update() {
		refreshGrid("escolaridadeGrid");
		return super.update();
	}

	private Long checkEscolaridade() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from Escolaridade o ");
		sb.append("where o.escolaridade = :nomeEscolaridade ");
		sb.append("and o.idEscolaridade <> :id");
		Query q = getEntityManager().createQuery(sb.toString()).setMaxResults(1);
		q.setParameter("nomeEscolaridade", getInstance().getEscolaridade());
		q.setParameter("id", getInstance().getIdEscolaridade());
		Long result = (Long) q.getSingleResult();
		return result;
	}

	@Override
	public String remove(Escolaridade obj) {
		obj.setAtivo(Boolean.FALSE);
		return super.remove(obj);
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