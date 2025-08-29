package br.com.infox.cliente.home.icrrefactory;

import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.Pena;
import br.jus.pje.nucleo.entidades.PenaTotal;

public class IcrAssociarPenaTotalManager<T extends IcrSentencaCondenatoria>
		extends IcrAssociarPenaIndividualizadaManager<T> {

	@Override
	protected void prePersist(T entity) throws IcrValidationException {
		super.prePersist(entity);
		validarPenaTotal(entity);

	};

	@Override
	protected void doPersist(T entity) throws IcrValidationException {
		if (entity.getPenaTotalList() == null
				|| entity.getPenaTotalList().isEmpty()) {
			throw new IcrValidationException(
					"É obrigatório que a Informação Criminal Relevante possua Pena cadastrada. Favor informar a Pena.");
		}
		super.doPersist(entity);
	};

	public void validarPenaTotal(T entity) throws IcrValidationException {
		validarPenas(entity.getPenaTotalList());

	}

	@Override
	public void validarPena(Pena pena) throws IcrValidationException {
		if(pena instanceof PenaTotal){
			for(PenaTotal penaTotal :pena.getIcrSentencaCondenatoria().getPenaTotalList()){
				if (penaTotal != pena) {
					if (pena.getTipoPena().equals(penaTotal.getTipoPena())) {
						throw new IcrValidationException(
								"Não é permitido cadastrar mais de uma Pena de mesmo Genero e Espécie!");
					}
				}
			}
		}
		super.validarPena(pena);
	}
}
