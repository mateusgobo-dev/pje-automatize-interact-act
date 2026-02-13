package br.com.infox.cliente.home.icrrefactory;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.IcrSoltura;
import br.jus.pje.nucleo.enums.TipoSolturaEnum;

@Name("icrSolturaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSolturaAction extends IcrAssociarIcrPrisaoAction<IcrSoltura, IcrSolturaManager> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5520856335778043255L;

	@Override
	/*
	 * Icr não associa EstabelecimentoPrisional, efeito colateral da
	 * IcrAssociarPrisão que herda de IcrAssociarEstabelecimentoPrisionais
	 */
	protected EstabelecimentoPrisional getEstabelecimentoPrisional(IcrSoltura entity) {
		return entity.getIcrPrisao().getEstabelecimentoPrisional();
	}

	public TipoSolturaEnum[] getTipoSolturaValues() {
		return TipoSolturaEnum.values();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao) {
		//
	}
}
