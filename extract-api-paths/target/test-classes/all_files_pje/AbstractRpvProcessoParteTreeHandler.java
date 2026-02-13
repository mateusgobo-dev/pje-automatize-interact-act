package br.com.infox.cliente.component.tree;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

public abstract class AbstractRpvProcessoParteTreeHandler<T> extends AbstractTreeHandler<ProcessoParte> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		StringBuilder hql = new StringBuilder();
		hql.append("select ppa ");
		hql.append("from ProcessoParte ppa ");
		hql.append("where ppa.processoTrf.idProcessoTrf = " + getIdProcesso() + " ");
		hql.append("and ppa.inParticipacao = '" + getInParticipacao() + "' ");
		if (getIdAutorCabeca() != null) {
			hql.append("and ppa.pessoa.idUsuario = '" + getIdAutorCabeca() + "' ");
		}
		hql.append("and ppa not in ");
		hql.append("(select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2 = ppa) ");
		hql.append("order by ppa.pessoa.nome");
		return hql.toString();
	}

	protected abstract ProcessoParteParticipacaoEnum getInParticipacao();

	protected abstract int getIdProcesso();

	protected abstract Integer getIdAutorCabeca();

	@Override
	protected String getQueryChildren() {
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2.processoTrf.idProcessoTrf = " + getIdProcesso() + " ");
		hql.append("and ppr.processoParte =:" + EntityNode.PARENT_NODE);
		return hql.toString();
	}

}