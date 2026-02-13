package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name(OrgaoJulgadorMagistradoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class OrgaoJulgadorMagistradoList extends EntityList<UsuarioLocalizacaoMagistradoServidor> {

	public static final String NAME = "orgaoJulgadorMagistradoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from UsuarioLocalizacaoMagistradoServidor o "
			+ "where o.orgaoJulgador = #{orgaoJulgadorHome.instance} "
			+ "and o.usuarioLocalizacao.papel = #{parametroUtil.instance().getPapelMagistrado()} "
			+ "and o.usuarioLocalizacao.usuario in (select pm from PessoaMagistrado pm)";

	private static final String DEFAULT_ORDER = "usuarioLocalizacao";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("idLocalizacao", "o.usuarioLocalizacao.localizacaoFisica.localizacao");
		return map;
	}

}