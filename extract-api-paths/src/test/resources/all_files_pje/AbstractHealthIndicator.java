package br.jus.cnj.pje.status;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractHealthIndicator{

	private Health health;
	private Map<String, Object> details = new LinkedHashMap<>();
	
	public abstract Health doHealthCheck();
	
	public Health getHealth() {
		return health;
	}

	public void setHealth(Health health) {
		this.health = health;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

}
