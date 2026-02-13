package br.com.infox.cliente.home;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AutoridadePublica;

@SuppressWarnings("serial")
@Name(AutoridadePublicaHome.NAME)
public class AutoridadePublicaHome extends AbstractHome<AutoridadePublica> {

	public static final String NAME = "autoridadePublicaHome";

	@In
	private PessoaAutoridadeHome pessoaAutoridadeHome;

	@Override
	public String remove(AutoridadePublica obj) {
		String ret = inactive(obj);
		refreshGrid("autoridadePublicaGrid");
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add("Operação realizada com sucesso!");
		return ret;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		instance.setAutoridade(pessoaAutoridadeHome.getInstance());
		if (!validarDatas()) {
			if (instance.getIdAutoridade() != 0) {
				getEntityManager().refresh(instance);
			}
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "A data de inicio não pode ser maior que a data fim!");
			return false;
		}
		if (existeChoquesDeData()) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,
					"O período informado esta em choque com " + "outros previamente cadastrados!");
			return false;
		}
		return super.beforePersistOrUpdate();
	}

	private boolean validarDatas() {

		if (instance.getDataFim() == null) {
			return true;
		}
		return instance.getDataInicio().compareTo(instance.getDataFim()) < 0;
	}

	private boolean existeChoquesDeData() {
		StringBuilder queryString = new StringBuilder("");
		queryString.append("select count(o) from AutoridadePublica o where o.autoridade.idUsuario = :id ");
		queryString.append("and ((:dataInicio between o.dataInicio and o.dataFim) or ");
		queryString.append("(:dataFim between o.dataInicio and o.dataFim))");

		if (isManaged()) {
			queryString.append(" and o.idAutoridade != :idAutoridadePublica ");
		}

		Query query = getEntityManager().createQuery(queryString.toString());
		query.setParameter("id", instance.getAutoridade().getIdUsuario());
		query.setParameter("dataInicio", instance.getDataInicio());
		query.setParameter("dataFim", instance.getDataFim());

		if (isManaged()) {
			query.setParameter("idAutoridadePublica", instance.getIdAutoridade());
		}
		try {
			Long retorno = (Long) query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}		
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("autoridadePublicaGrid");
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add("Operação realizada com sucesso!");
		return super.afterPersistOrUpdate(ret);
	}

}
