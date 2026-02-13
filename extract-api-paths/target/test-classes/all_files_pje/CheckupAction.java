package br.jus.cnj.pje.util.checkup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.cnj.pje.util.checkup.spi.CheckupError;
import br.jus.cnj.pje.util.checkup.spi.CheckupWorker;
import br.jus.cnj.pje.util.checkup.spi.ProgressBean;

@Name(CheckupAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class CheckupAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1259351021856406148L;
	
	public static final String NAME = "checkupAction";
	
	@In(required=true, create=true)
	private CheckupService checkupService;
	private Boolean enablePool = false;
	private Map<String, Boolean> openedMap = new HashMap<String, Boolean>();
	private Boolean showIgnored = false;
	private List<String> ignoredHashs;
	
	public ProgressBean getProgressBean(CheckupWorker worker) {
		return checkupService.getProgressBean(worker);
	}
	
	public void runCheckups() {
		enablePool = true;
		ignoredHashs = null;
		checkupService.runCheckups(showIgnored);
	}
	
	public Boolean getEnablePool() {
		if (enablePool) {
			enablePool = checkupService.getStillRunning();
		}
		return enablePool;
	}
	
	public List<CheckupWorker> getCheckupWorkers() {
		return checkupService.getCheckupWorkers();
	}
	
	public void addIgnoredCheckup(String hash) {
		checkupService.addIgnoredCheckup(hash);
	}
	
	public void removeIgnoredCheckup(String hash) {
		checkupService.removeIgnoredCheckup(hash);
	}
	
	public Boolean isOpened(CheckupWorker worker) {
		if (!this.openedMap.containsKey(worker.getID())) {
			this.openedMap.put(worker.getID(), false);
		}
		return this.openedMap.get(worker.getID());
	}
	
	@BypassInterceptors
	public void expand(CheckupWorker worker) {
		this.openedMap.put(worker.getID(), true);
	}
	
	@BypassInterceptors
	public void collapse(CheckupWorker worker) {
		this.openedMap.put(worker.getID(), false);
	}

	public Boolean getShowIgnored() {
		return showIgnored;
	}
	
	public void setShowIgnored(Boolean showIgnored) {
		this.showIgnored = showIgnored;
	}

	public List<String> getAllIgnoredHashs() {
		if (ignoredHashs == null) {
			ignoredHashs = checkupService.getAllIgnoredHashs();
		}
		return ignoredHashs;
	}
	
	public boolean isIgnored(CheckupWorker worker) {
		return getAllIgnoredHashs().contains(worker.getID());
	}
	
	public boolean isIgnored(CheckupError error) {
		return getAllIgnoredHashs().contains(error.getID());
	}
}
