package br.com.infox.pje.bean;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;

public class SessaoJulgamentoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3864706952502972934L;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private boolean check;
	private boolean rendered;
	private boolean destacadoSessao;

	public SessaoJulgamentoBean(SessaoPautaProcessoTrf sppTrf, boolean check, boolean rendered) {
		this.sessaoPautaProcessoTrf = sppTrf;
		this.check = check;
		this.setRendered(rendered);
	}

	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
	}

	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public boolean getCheck() {
		return check;
	}

	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	public boolean isRendered() {
		return rendered;
	}

	public boolean getDestacadoSessao() {
		return destacadoSessao;
	}

	public void setDestacadoSessao(boolean destacadoSessao) {
		this.destacadoSessao = destacadoSessao;
	}

}