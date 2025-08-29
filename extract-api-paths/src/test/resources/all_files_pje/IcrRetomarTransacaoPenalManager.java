package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrRetomarTransacaoPenal;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;

@Name("icrRTPManager")
public class IcrRetomarTransacaoPenalManager extends IcrAssociarIcrManager<IcrRetomarTransacaoPenal> {
	@Override
	public Boolean possuiDataPublicacao() {
		return true;
	}

	@Override
	protected String[] getFiltrosIcr() {
		String[] filtros = new String[2];
		// foi encerrada
		filtros[0] = ("icr not in(select distinct(o.transacaoPenal) from IcrEncerramentoDeTransacaoPenal o where o.ativo=true)");
		// já foi retomada
		filtros[1] = ("icr not in(select distinct(o.suspensaoTransacaoPenal) from IcrRetomarTransacaoPenal o where o.ativo=true)");
		return filtros;
	}

	@Override
	public Date getDtPublicacao(IcrRetomarTransacaoPenal entity) {
		return entity.getDataDecisao();
	}

	@Override
	protected InformacaoCriminalRelevante getIcrAfetada(IcrRetomarTransacaoPenal entity) {
		return entity.getSuspensaoTransacaoPenal();
	}

	@Override
	protected TipoIcrEnum[] getTiposDeIcrAceitos() {
		return IcrRetomarTransacaoPenal.getTiposDeIcrAceitos();
	}
}
