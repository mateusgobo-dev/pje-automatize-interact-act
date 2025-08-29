package br.com.infox.cliente.home;

import java.text.SimpleDateFormat;

import javax.persistence.EntityExistsException;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

@Name("processoEventoHome")
@BypassInterceptors
public class ProcessoEventoHome extends AbstractProcessoEventoHome<ProcessoEvento> {

	private static final long serialVersionUID = 1L;
	private Boolean visibilidadeExterna;

	public ProcessoEventoHome() {
		boolean visibilidadeExterna = false;
		if (Contexts.getSessionContext().get("usuarioLogado") != null) {
			if (Identity.instance().hasRole("advogado")
					|| Identity.instance().hasRole("Perito")
					|| Identity.instance().hasRole("assistAdvogado")
					|| Identity.instance().hasRole("jusPostulandi")
					|| Identity.instance().hasRole("advogado_procurador")
					|| Identity.instance().hasRole("procurador")
					|| Identity.instance().hasRole("assistProcuradoria")
					|| Identity.instance().hasRole("assistGestorAdvogado")) {
				visibilidadeExterna = true;
			}
		} else {
			visibilidadeExterna = true;
		}
		this.visibilidadeExterna = visibilidadeExterna;
	}

	public static ProcessoEventoHome instance() {
		return ComponentUtil.getComponent("processoEventoHome");
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (EntityExistsException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já existe.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Deprecated
	/**
	 * Substituído pelo método ProcessoEvento.getTextoFinal().
	 * @return Atributo 'movimento' do Evento.
	 */
	public String getMovimento(Integer id) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(Evento.class);
		criteria.add(Restrictions.eq("idEvento", id));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		Evento classe = (Evento)criteria.uniqueResult();
		if (classe != null) {
			return classe.getMovimento();
		}
		return null;
	}

	/**
	 * Consulta descrição do movimento conforme perfil de visibilidade
	 * 
	 * @param ProcessoEvento
	 * @return Descrição do movimento
	 */
	public String getTextoFinal(ProcessoEvento processoEvento) {
		return processoEvento.getTextoFinal(visibilidadeExterna);
	}

	/**
	 * Consulta descrição completa (com data) do movimento excludente conforme
	 * perfil de visibilidade
	 * 
	 * @param ProcessoEvento
	 *            processoEventoExcludente
	 * @return Texto completo do movimento excludente
	 */
	public String getTextoMovimentoExcludente(ProcessoEvento processoEventoExcludente) {
		StringBuilder retorno = new StringBuilder("Movimento excluído em '");

		SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		retorno.append(sf.format(processoEventoExcludente.getDataAtualizacao()));
		retorno.append("' por '");
		retorno.append(processoEventoExcludente.getUsuario().getNome());
		retorno.append("'");

		return retorno.toString();
	}

	@Override
	public String remove(ProcessoEvento obj) {
		setInstance(obj);
		super.update();
		newInstance();
		refreshGrid("processoEventoGrid");
		return "updated";
	}

	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}

}