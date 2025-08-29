package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;

@Name(IgnoredCheckupsDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class IgnoredCheckupsDAO extends GenericDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "ignoredCheckupsDAO";

	@SuppressWarnings("unchecked")
	public List<String> getAllIgnoredHashs() {
		return getEntityManager().createQuery("select o.ignoredHash from IgnoredCheckups o").getResultList();
	}


}