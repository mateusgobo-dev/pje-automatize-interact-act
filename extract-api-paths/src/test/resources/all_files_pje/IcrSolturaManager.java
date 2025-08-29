package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.IcrSoltura;
import br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoSolturaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrSOLManager")
public class IcrSolturaManager extends InformacaoCriminalRelevanteManager<IcrSoltura> {
	@In(create = true, value = "icrPRIManager")
	private IcrPrisaoManager icrPrisaoManager;

	@Override
	protected void prePersist(IcrSoltura entity) throws IcrValidationException {
		super.prePersist(entity);
		if (DateUtil.isDataMenor(entity.getData(), entity.getIcrPrisao().getData())) {
			throw new IcrValidationException("icrSoltura.dataInferior");
		}
		/*
		 * So permite cadastrar como tipo 'Final do Prazo da Temporária', se a
		 * prisão vinculada for do tipo 'Temporaria'
		 */
		if (entity.getInTipoSoltura().equals(TipoSolturaEnum.FPT)) {
			if (!entity.getIcrPrisao().getInTipoPrisao().equals(TipoPrisaoEnum.TMP)) {
				throw new IcrValidationException("icrSoltura.tipoImcompativelComTipoPrisao");
			}
		}
		if (entity.getId() == null) {
			IcrPrisao icrPrisao = entity.getIcrPrisao();
			icrPrisao.setDtEncerramentoPrisao(entity.getData());
			icrPrisao.setDsMotivoEncerramento(entity.getInTipoSoltura().getLabel());
			icrPrisao.setMotivoEncerramentoPrisao(MotivoEncerramentoPrisaoEnum.SO);
			icrPrisaoManager.persist(icrPrisao);
		}
	}

	@Override
	protected void preInactive(IcrSoltura entity) throws IcrValidationException {
		for (IcrPrisao icrPrisao : entity.getProcessoParte().getIcrPrisoesAtivas()) {
			if ((icrPrisao.getDtEncerramentoPrisao() == null) || (entity.getData().before(icrPrisao.getData()))) {
				throw new IcrValidationException("A soltura não pode ser excluída. Existem outras prisões em aberto");
			}
		}
		IcrPrisao icrPrisao = entity.getIcrPrisao();
		icrPrisao.setDtEncerramentoPrisao(null);
		icrPrisao.setDsMotivoEncerramento(null);
		icrPrisao.setMotivoEncerramentoPrisao(null);
		icrPrisaoManager.persist(icrPrisao);
	}

	@Override
	public Date getDtPublicacao(IcrSoltura entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
