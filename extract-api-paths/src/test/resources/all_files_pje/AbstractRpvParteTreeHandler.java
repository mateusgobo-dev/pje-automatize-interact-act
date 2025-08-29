package br.com.infox.cliente.component.tree;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.pje.action.RpvAction;
import br.jus.pje.nucleo.entidades.RpvPessoaParte;

public abstract class AbstractRpvParteTreeHandler<T> extends AbstractTreeHandler<RpvPessoaParte>{

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots(){
		StringBuilder hql = new StringBuilder();
		hql.append("select o ");
		hql.append("from RpvPessoaParte o ");
		hql.append("where o.rpv.idRpv = " + getIdRpv() + " ");
		hql.append("and o.inParticipacao = '" + getInParticipacao() + "' ");

		hql.append("and o not in ");
		hql.append("(select distinct ppa2 from RpvParteRepresentante ppr ");
		hql.append("inner join ppr.rpvPessoaRepresentante ppa2 ");
		hql.append("where ppa2 = o) ");
		hql.append("order by o.pessoa.nome");

		return hql.toString();
	}

	protected abstract int getIdRpv();

	protected abstract String getInParticipacao();

	@Override
	protected String getQueryChildren(){
		StringBuilder hql = new StringBuilder();
		hql.append("select rpa2 from RpvParteRepresentante rpr  ");
		hql.append("inner join rpr.rpvPessoaRepresentante rpa2 ");
		hql.append("where rpa2.rpv.idRpv = " + getIdRpv() + " ");
		hql.append(" and rpr.rpvPessoaParte =:" + EntityNode.PARENT_NODE);
		hql.append(" order by case when rpa2.tipoParte.tipoParte = 'HERDEIRO' then 1 ");
		hql.append(" when rpa2.tipoParte.tipoParte = 'REPRESENTANTE' then 2 ");
		hql.append(" when rpa2.tipoParte.tipoParte = 'ADVOGADO' then 3 else 4 end ");
		return hql.toString();
	}

	private String getOficioQueryChildren(){
		StringBuilder hql = new StringBuilder();
		hql.append("select rpa2 from RpvParteRepresentante rpr  ");
		hql.append("inner join rpr.rpvPessoaRepresentante rpa2 ");
		hql.append("where rpa2.rpv.idRpv = " + getIdRpv() + " ");
		hql.append(" and rpr.rpvPessoaParte =:" + EntityNode.PARENT_NODE);
		hql.append(" and rpa2.tipoParte.tipoParte = 'ADVOGADO'");
		hql.append(" order by case when rpa2.tipoParte.tipoParte = 'HERDEIRO' then 1 ");
		hql.append(" when rpa2.tipoParte.tipoParte = 'ADVOGADO' then 2 else 3 end ");
		return hql.toString();
	}

	@Override
	protected String[] getQueryChildrenList(){
		String[] querys = new String[1];
		if (RpvAction.instance() != null &&
				RpvAction.instance().getIsOficio() &&
				!RpvAction.instance().existeParteIncapazRpv()){
			querys[0] = getOficioQueryChildren();
			return querys;
		}
		else{
			querys[0] = getQueryChildren();
			return querys;
		}
	}

}