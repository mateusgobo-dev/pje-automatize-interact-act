package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.IgnoredCheckupsDAO;
import br.jus.pje.nucleo.entidades.IgnoredCheckups;

@Name(IgnoredCheckupsManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class IgnoredCheckupsManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "ignoredCheckupsManager";

	@In
	private IgnoredCheckupsDAO ignoredCheckupsDAO;

	public List<String> getAllIgnoredHashs() {
		return ignoredCheckupsDAO.getAllIgnoredHashs();
	}
	
	public void addIgnoredCheckup(String hash) {
		if (ignoredCheckupsDAO.find(IgnoredCheckups.class, hash) == null) {
			ignoredCheckupsDAO.update(new IgnoredCheckups(hash));
		}
	}

	public void removeIgnoredCheckup(String hash) {
		IgnoredCheckups find = ignoredCheckupsDAO.find(IgnoredCheckups.class, hash);
		if (find != null) {
			ignoredCheckupsDAO.remove(find);
		}
	}

}