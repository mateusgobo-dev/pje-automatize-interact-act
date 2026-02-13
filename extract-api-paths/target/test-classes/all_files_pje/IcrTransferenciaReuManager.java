package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrTransferenciaReu;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrTRRManager")
public class IcrTransferenciaReuManager extends InformacaoCriminalRelevanteManager<IcrTransferenciaReu> {
	@Override
	protected void prePersist(IcrTransferenciaReu entity) throws IcrValidationException {
		super.prePersist(entity);
		if (entity != null
				&& entity.getEstabelecimentoPrisional().equals(entity.getIcrPrisao().getProcessoParte().getEstabelecimentoPrisionalAtual())) {
			throw new IcrValidationException("O réu já está preso no estabelecimento informado");
		}
		// a data da transferencia nao pode ser menor que a data da prisao
		if (DateUtil.isDataMenor(entity.getData(), entity.getIcrPrisao().getData())) {
			throw new IcrValidationException("icrTransferenciaReu.dataInferior");
		}
	}

	@Override
	public Date getDtPublicacao(IcrTransferenciaReu entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
