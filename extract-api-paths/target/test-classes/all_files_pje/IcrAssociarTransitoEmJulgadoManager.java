package br.com.infox.cliente.home.icrrefactory;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TransitoEmJulgado;
import br.jus.pje.nucleo.util.DateUtil;

public abstract class IcrAssociarTransitoEmJulgadoManager<T extends InformacaoCriminalRelevante> extends
		IcrBaseManager<T> {
	@Override
	protected void prePersist(T entity) throws IcrValidationException {
		super.prePersist(entity);
		if (entity.getTransitoEmJulgadoList() != null && !entity.getTransitoEmJulgadoList().isEmpty()) {
			for (TransitoEmJulgado icrTrans : entity.getTransitoEmJulgadoList()) {
				if (DateUtil.isDataMaior(entity.getData(), icrTrans.getData())) {
					throw new IcrValidationException("transitoEmJulgado.dataInvalida");
				}
			}
		}
	};

	public void validarTransitoEmJulgado(TransitoEmJulgado icrTrans) throws IcrValidationException {
		if (icrTrans.getIcr() == null) {
			throw new IcrValidationException("transitoEmJulgado.icrNaoInformada");
		}
		if (icrTrans.getIcr().getData() == null) {
			throw new IcrValidationException("transitoEmJulgado.dataIcrNaoInformada");
		}
		if (icrTrans.getData() == null) {
			throw new IcrValidationException("transitoEmJulgado.dataNaoInformada");
		}
		if (icrTrans.getProcessoParte() == null) {
			throw new IcrValidationException("transitoEmJulgado.parteNaoInformada");
		}
		if (DateUtil.isDataMenor(icrTrans.getData(), icrTrans.getIcr().getData())) {
			throw new IcrValidationException("transitoEmJulgado.dataInvalida");
		}
	}
}
