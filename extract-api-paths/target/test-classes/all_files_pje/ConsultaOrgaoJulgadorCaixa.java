package br.com.infox.cliente.bean;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

public class ConsultaOrgaoJulgadorCaixa implements Serializable{

	private static final long serialVersionUID = 1L;

	private OrgaoJulgador orgaoJulgador;
	private Caixa caixa;

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador){
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador(){
		return orgaoJulgador;
	}

	public void setCaixa(Caixa caixa){
		this.caixa = caixa;
	}

	public Caixa getCaixa(){
		return caixa;
	}
}
