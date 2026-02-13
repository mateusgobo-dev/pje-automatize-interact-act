
package br.jus.pje.nucleo.dto.domicilioeletronico;

import java.util.ArrayList;
import java.util.List;

public class DataDTO {
	public List<String> chaves = new ArrayList<>();
	public String continuationToken;

	public List<String> getChaves() {
		return chaves;
	}

	public void setChaves(List<String> chaves) {
		this.chaves = chaves;
	}

	public String getContinuationToken() {
		return continuationToken;
	}

	public void setContinuationToken(String continuationToken) {
		this.continuationToken = continuationToken;
	}

}
