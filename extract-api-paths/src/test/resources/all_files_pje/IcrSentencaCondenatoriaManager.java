package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.PenaTotal;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;

@Name("icrSCOManager")
public class IcrSentencaCondenatoriaManager extends IcrAssociarPenaTotalManager<IcrSentencaCondenatoria> {
	@Override
	public Date getDtPublicacao(IcrSentencaCondenatoria entity) {
		// TODO Auto-generated method stub
		return entity.getDtPublicacao();
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return true;
	}

	public PenaTotal buscarUltimaPenaPrivativaLiberdadePessoa(ProcessoTrf processoTrf, Pessoa pessoa) {

		String hql = " select o from PenaTotal o " + " where o.tipoPena.generoPena = ? "
				+ " and o.icrSentencaCondenatoria.processoParte.processoTrf.idProcessoTrf = ? "
				+ " and o.icrSentencaCondenatoria.processoParte.pessoa.idUsuario = ? "
				+ " and o.icrSentencaCondenatoria.ativo = true " + " and o.penasSubstitutivas is empty "
				+ " order by o.icrSentencaCondenatoria.data desc, o.id desc ";

		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter(1, GeneroPenaEnum.PL);// privativa de liberdade
		qry.setParameter(2, processoTrf.getIdProcessoTrf());
		qry.setParameter(3, pessoa.getIdUsuario());

		List<PenaTotal> result = qry.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		}

		return null;
	}
	
	
}
