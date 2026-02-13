package br.com.infox.cliente.home;

import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Area;

@Name(AreaHome.NAME)
@BypassInterceptors
public class AreaHome extends AbstractHome<Area> {

	public static final String NAME = "areaHome";
	private static final long serialVersionUID = -1241324100221190981L;

	private int localizacao = Authenticator.getLocalizacaoAtual().getIdLocalizacao();

	public static AreaHome instance() {
		return ComponentUtil.getComponent(AreaHome.NAME);
	}

	@SuppressWarnings("unchecked")
	@Factory(value = "areaLocalizacaoItens")
	public List<Area> getAreaLocalizacaoItens() {
		List<Area> resultList = getEntityManager().createQuery(
				"Select distinct o from Area o, CentralMandadoLocalizacao cl "
						+ "where cl.localizacao.idLocalizacao = " + localizacao + " and "
						+ "cl.centralMandado.idCentralMandado = "
						+ "o.centralMandado.idCentralMandado and o.ativo = true order by o.dsArea").getResultList();
		return resultList;

	}

}
