package br.com.infox.cliente.home;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name("tipoAudienciaHome")
@BypassInterceptors
public class TipoAudienciaHome extends AbstractTipoAudienciaHome<TipoAudiencia> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(TipoAudienciaHome.class);

	@Override
	public String persist() {
		String ret = null;
		if (verificaTipoAudiencia()) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Tipo de Audiência já cadastrada.");
			return "";
		}
		try {
			ret = super.persist();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		refreshGrid("tipoAudienciaGrid");
		return ret;
	}

	@Override
	public String remove(TipoAudiencia obj) {
		setInstance(obj);
		getInstance().setAtivo(false);
		String ret = super.remove(obj);
		newInstance();
		return ret;

	}

	@Override
	public String update() {
		if (verificaTipoAudiencia()) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Tipo de Audiência já cadastrada.");
			return "";
		}
		return super.update();
	}

	/**
	 * Método que verifica se já existe o tipo de audiência que está sendo
	 * cadastrado.
	 * 
	 * @return
	 */
	public boolean verificaTipoAudiencia() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from TipoAudiencia o ");
		sb.append("where lower(to_ascii(o.tipoAudiencia)) like lower(to_ascii(#{tipoAudienciaHome.instance.tipoAudiencia})) ");
		if (getInstance().getIdTipoAudiencia() != 0) {
			sb.append("and o.idTipoAudiencia != :id");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		if (getInstance().getIdTipoAudiencia() != 0) {
			q.setParameter("id", getInstance().getIdTipoAudiencia());
		}
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}

	}
}