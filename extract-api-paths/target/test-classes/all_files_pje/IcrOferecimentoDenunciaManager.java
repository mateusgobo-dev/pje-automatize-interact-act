package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrOFDManager")
public class IcrOferecimentoDenunciaManager extends InformacaoCriminalRelevanteManager<InformacaoCriminalRelevante> {


	@Override
	public Date getDtPublicacao(InformacaoCriminalRelevante entity) {
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		return false;
	}
}
