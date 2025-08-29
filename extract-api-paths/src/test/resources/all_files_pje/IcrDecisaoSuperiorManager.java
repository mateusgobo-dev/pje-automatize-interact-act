package br.com.infox.cliente.home.icrrefactory;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

public class IcrDecisaoSuperiorManager<T extends InformacaoCriminalRelevante> extends
		IcrAssociarIcrManager<T> {

	@Override
	protected String getMensagemDataIcrMenorQueDataSentenca(){
		return "icrAnulacaoDeSentenca.dataPublicacaoInvalida2";
	}
	
	@Override
	protected String getMensagemDataPublicacaoInvalida() {
		return "icrAnulacaoDeSentenca.dataPublicacaoInvalida2";
	}

	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return null;
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(T entity) {
		return null;
	}
	
}
