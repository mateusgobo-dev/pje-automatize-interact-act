package br.jus.cnj.pje.entidades.vo;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoVoto;

public class SessaoComposicaoVotoLoteVO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private OrgaoJulgador orgaoJulgador;
	private boolean relator;
	private TipoVoto tipoVoto;
	private OrgaoJulgador orgaoJulgadorAcompanhado;
	
	
	
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	public OrgaoJulgador getOrgaoJulgadorAcompanhado() {
		return orgaoJulgadorAcompanhado;
	}
	public void setOrgaoJulgadorAcompanhado(OrgaoJulgador orgaoJulgadorAcompanhado) {
		this.orgaoJulgadorAcompanhado = orgaoJulgadorAcompanhado;
	}
	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}
	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}
	public boolean isRelator() {
		return relator;
	}
	public void setRelator(boolean relator) {
		this.relator = relator;
	}
	
	

}
