package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.IcrFuga;

@Name("icrFugaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrFugaAction extends IcrAssociarIcrPrisaoAction<IcrFuga, IcrFugaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7006747522330196465L;

	@Override
	protected EstabelecimentoPrisional getEstabelecimentoPrisional(IcrFuga entity) {
		return entity.getIcrPrisao().getEstabelecimentoPrisional();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		// TODO Auto-generated method stub
	}
}
