package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrFuga;
import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrFUGManager")
public class IcrFugaManager extends InformacaoCriminalRelevanteManager<IcrFuga> {
	@In(create = true, value = "icrPRIManager")
	IcrPrisaoManager icrPrisaoManager;

	@Override
	protected void prePersist(IcrFuga entity) throws IcrValidationException {
		super.prePersist(entity);
		if (DateUtil.isDataMenor(entity.getData(), entity.getIcrPrisao().getData())) {
			throw new IcrValidationException("Data inválida, inferior a data da prisão");
		}
		if (entity.getId() == null && entity.getIcrPrisao().getDtEncerramentoPrisao() != null) {
			throw new IcrValidationException("Não é possível cadastrar uma fuga para uma prisão encerrada.");
		}
		// if(entity.getId() == null){
		IcrPrisao icrPrisao = entity.getIcrPrisao();
		icrPrisao.setDtEncerramentoPrisao(entity.getData());
		icrPrisao.setMotivoEncerramentoPrisao(MotivoEncerramentoPrisaoEnum.FU);
		icrPrisao.setDsMotivoEncerramento(TipoIcrEnum.FUG.getLabel());
		icrPrisaoManager.persist(icrPrisao);
		// }
	}

	@Override
	protected void preInactive(IcrFuga entity) throws IcrValidationException {
		for (IcrPrisao icrPrisao : entity.getProcessoParte().getIcrPrisoesAtivas()) {
			if (icrPrisao.getDtEncerramentoPrisao() == null) {
				throw new IcrValidationException("A fuga não pode ser excluída. Existem outras prisões em aberto");
			}
		}
		IcrPrisao icrPrisao = entity.getIcrPrisao();
		icrPrisao.setDtEncerramentoPrisao(null);
		icrPrisao.setDsMotivoEncerramento(null);
		icrPrisao.setMotivoEncerramentoPrisao(null);
		icrPrisaoManager.persist(icrPrisao);
	}

	@Override
	public Date getDtPublicacao(IcrFuga entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
