package br.com.infox.pje.bean;

import java.io.Serializable;

import org.jboss.seam.Component;

import br.com.infox.cliente.home.SessaoComposicaoOrdemHome;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

public class VotoAcompanhadoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5293894310169678953L;
	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto;
	private boolean check;

	public VotoAcompanhadoBean(SessaoProcessoDocumentoVoto spdv, boolean check) {
		this.setSessaoProcessoDocumentoVoto(spdv);
		this.check = check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public boolean getCheck() {
		if (SessaoComposicaoOrdemHome.instance() != null &&
				SessaoComposicaoOrdemHome.instance().getSessaoVoto() != null &&
				sessaoProcessoDocumentoVoto != null &&
				SessaoComposicaoOrdemHome.instance().getSessaoVoto().getOjAcompanhado().equals(sessaoProcessoDocumentoVoto.getOrgaoJulgador())){
				return true;
			}
		return check;
	}

	public void setSessaoProcessoDocumentoVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		this.sessaoProcessoDocumentoVoto = sessaoProcessoDocumentoVoto;
	}

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
		return sessaoProcessoDocumentoVoto;
	}

	public String getMagistradoAssinou() {
		SessaoProcessoDocumentoVotoManager spdvm = (SessaoProcessoDocumentoVotoManager) Component.getInstance("sessaoProcessoDocumentoVotoManager");
		return spdvm.getMagistradoAssinou(sessaoProcessoDocumentoVoto);
	}

}