package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InformacaoSessoesResposta implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5312674512472568870L;
	
	 
	List<InformacaoSessaoResposta> sessoes = new ArrayList<InformacaoSessaoResposta>(0);
	
	public List<InformacaoSessaoResposta> getSessoes() {
		return sessoes;
	}
	
	public void setSessoes(List<InformacaoSessaoResposta> sessoes) {
		this.sessoes = sessoes;
	}

}
