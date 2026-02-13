package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;

@Name(UsuarioLocalizacaoVisibilidadeList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class UsuarioLocalizacaoVisibilidadeList extends EntityList<UsuarioLocalizacaoVisibilidade> {

	public static final String NAME = "usuarioLocalizacaoVisibilidadeList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from UsuarioLocalizacaoVisibilidade o ";
	private static final String DEFAULT_ORDER = "idUsuarioLocalizacaoVisibilidade";

	private static final String R1 = "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.usuario.idUsuario"
			+ " = #{pessoaServidorHome.instance.idUsuario}";
	private static final String R2 = "o.usuarioLocalizacaoMagistradoServidor.orgaoJulgador"
			+ " = #{orgaoJulgadorHome.instance}";
	private static final String R3 = "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.papel = "
			+ "#{orgaoJulgadorHome.instance.localizacao != null ? parametroUtil.getPapelMagistrado() : null}";

	@Override
	protected void addSearchFields() {
		addSearchField("usuario", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R2);
		addSearchField("papel", SearchCriteria.igual, R3);
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
		map.put("magistrado", "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.usuario.nome");
		map.put("localizacao", "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.localizacaoFisica.localizacao");
		return map;
	}

}