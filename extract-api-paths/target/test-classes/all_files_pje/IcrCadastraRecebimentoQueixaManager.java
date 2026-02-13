package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;

@Name("icrCRQManager")
public class IcrCadastraRecebimentoQueixaManager extends
		InformacaoCriminalRelevanteManager<InformacaoCriminalRelevante> {
	@Override
	public Date getDtPublicacao(InformacaoCriminalRelevante entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
