package br.jus.cnj.pje.util.checkup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import br.com.infox.pje.manager.IgnoredCheckupsManager;
import br.jus.cnj.pje.util.checkup.spi.CheckupError;
import br.jus.cnj.pje.util.checkup.spi.CheckupRegister;
import br.jus.cnj.pje.util.checkup.spi.CheckupWorker;
import br.jus.cnj.pje.util.checkup.spi.ProgressBean;

@Name(CheckupService.NAME)
@Scope(ScopeType.CONVERSATION)
public class CheckupService implements CheckupRegister, Serializable {

	public static final String NAME = "checkupService";
	public static final String REGISTER_EVENT = NAME + "." + "REGISTER_EVENT";
	
	@Logger
	private Log log;
	
	private List<CheckupWorker> checkupWorkers = new ArrayList<CheckupWorker>();
	
	@In(required=true, create=true)
	private CheckupManager checkupManager;
	
	@In
	private IgnoredCheckupsManager ignoredCheckupsManager;
	
	private List<String> ignoredHashs;
	
	public ProgressBean getProgressBean(CheckupWorker worker) {
		ProgressBean retorno = checkupManager.getProgressBean(worker);
		if (retorno != null) {
			removeIgnoredCheckups(retorno);
		}
		return retorno;
	}

	private void removeIgnoredCheckups(ProgressBean retorno) {
		List<CheckupError> errors = retorno.getErrors();
		if (errors == null || ignoredHashs == null) {
			return;
		}
		for (ListIterator<CheckupError> iterator = errors.listIterator(); iterator.hasNext();) {
			CheckupError checkupError = (CheckupError) iterator.next();
			if (ignoredHashs.contains(checkupError.getID())) {
				iterator.remove();
			}
		}
	}

	@Create
	public void init() {
		//abrir inscrições para checkupsWorkers se registrarem
		Events.instance().raiseEvent(REGISTER_EVENT, this);
	}
	
	@Override
	public void registerCheckup(CheckupWorker worker) {
		if (!this.checkupWorkers.contains(worker)) {
			getCheckupWorkers().add(worker);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void runCheckups(Boolean showIgnored) {
		if (!showIgnored) {
			ignoredHashs = getAllIgnoredHashs();
		} else {
			ignoredHashs = Collections.EMPTY_LIST;
		}
		checkupManager.clearMapProgress();
		for (CheckupWorker checkupWorker : checkupWorkers) {
			if (checkupWorker.shouldRun() && !ignoredHashs.contains(checkupWorker.getID())) {
				ProgressBean pb = checkupManager.getProgressBean(checkupWorker);
				pb.setIsFinished(false); //para agilizar a mostra do icone de loading na tela
				try {
					checkupManager.put(checkupWorker, pb);
				} catch (Exception e) {}
				checkupManager.workAsynchronous(checkupWorker.getClass());
			}
		}
	}
	
	public List<String> getAllIgnoredHashs() {
		return ignoredCheckupsManager.getAllIgnoredHashs();
	}

	public Boolean getStillRunning() {
		Boolean retorno = false;
		for (CheckupWorker checkupWorker : checkupWorkers) {
			if (!getProgressBean(checkupWorker).getIsFinished()) {
				retorno = true;
			}
		}
		return retorno;
	}

	public List<CheckupWorker> getCheckupWorkers() {
		return checkupWorkers;
	}
	
	@Destroy
	public void clean() {
		Contexts.getApplicationContext().remove("checkupManager");
	}

	public void addIgnoredCheckup(String hash) {
		ignoredCheckupsManager.addIgnoredCheckup(hash);
	}

	public void removeIgnoredCheckup(String hash) {
		ignoredCheckupsManager.removeIgnoredCheckup(hash);
	}
	
}
