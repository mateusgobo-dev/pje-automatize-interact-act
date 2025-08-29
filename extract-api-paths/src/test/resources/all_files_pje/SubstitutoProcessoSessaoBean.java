package br.com.infox.pje.bean;

import java.io.Serializable;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;

public class SubstitutoProcessoSessaoBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private OrgaoJulgador orgaoJulgador;
	private PessoaMagistrado magistradoSubstituto;
	
	public SubstitutoProcessoSessaoBean(OrgaoJulgador oj){
		orgaoJulgador = oj;
	}
	
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public PessoaMagistrado getMagistradoSubstituto() {
		return magistradoSubstituto;
	}

	public void setMagistradoSubstituto(PessoaMagistrado magistradoSubstituto) {
		this.magistradoSubstituto = magistradoSubstituto;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof SubstitutoProcessoSessaoBean)){
			return false;
		}
		SubstitutoProcessoSessaoBean bean = (SubstitutoProcessoSessaoBean) obj;
		if(!orgaoJulgador.equals(bean.getOrgaoJulgador())){
			return false;
		}
		return true;
	}

}