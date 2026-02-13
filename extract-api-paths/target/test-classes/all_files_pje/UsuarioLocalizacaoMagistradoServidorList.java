package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name(UsuarioLocalizacaoMagistradoServidorList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class UsuarioLocalizacaoMagistradoServidorList extends EntityList<UsuarioLocalizacaoMagistradoServidor> {

	public static final String NAME = "usuarioLocalizacaoServidorList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "usuarioLocalizacao.localizacaoFisica.faixaInferior, usuarioLocalizacao.papel.nome, idUsuarioLocalizacaoMagistradoServidor";

	private static final String R1 = "o.usuarioLocalizacao.usuario.idUsuario = #{pessoaServidorHome.instance.idUsuario}";
	private static final String R2 = "o.usuarioLocalizacao.papel != #{parametroUtil.getPapelMagistrado()}";

	@Override
	protected void addSearchFields() {
		addSearchField("usuario", SearchCriteria.igual, R1);
		addSearchField("papel", SearchCriteria.igual, R2);
	}

	@Override
	protected String getDefaultEjbql() {
		Localizacao localizacaoFisica = Authenticator.getLocalizacaoFisicaAtual();
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual = Authenticator.getOrgaoJulgadorColegiadoAtual();
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

		StringBuilder sb = new StringBuilder("select o from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND o.vinculacaoUsuario IS NULL ");
		
		if(orgaoJulgadorColegiadoAtual != null) {
			sb.append(" AND o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+ orgaoJulgadorColegiadoAtual.getIdOrgaoJulgadorColegiado());
		}
		if(orgaoJulgadorAtual != null) {
			sb.append(" AND o.orgaoJulgador.idOrgaoJulgador = "+ orgaoJulgadorAtual.getIdOrgaoJulgador());
		}

		if(localizacaoFisica != null && localizacaoFisica.getFaixaInferior() != null) {
			sb.append(" AND o.idUsuarioLocalizacaoMagistradoServidor IN (")
				.append(" SELECT ul.idUsuarioLocalizacao FROM UsuarioLocalizacao AS ul ")
				.append(" JOIN ul.localizacaoFisica loc ")
				.append(" WHERE loc.faixaInferior IS NOT NULL ")
				.append(" AND loc.faixaInferior >= "+localizacaoFisica.getFaixaInferior())
				.append(" AND loc.faixaSuperior IS NOT NULL ")
				.append(" AND loc.faixaSuperior <= "+localizacaoFisica.getFaixaSuperior())
				.append(" ) ");
		}
		
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

}