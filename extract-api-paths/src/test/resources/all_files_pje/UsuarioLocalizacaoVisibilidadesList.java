package br.com.infox.pje.list;

import java.util.HashMap;
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
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;

@Name(UsuarioLocalizacaoVisibilidadesList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class UsuarioLocalizacaoVisibilidadesList extends EntityList<UsuarioLocalizacaoVisibilidade> {

	public static final String NAME = "usuarioLocalizacaoVisibilidadesList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.localizacaoFisica.faixaInferior, usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.papel.nome, idUsuarioLocalizacaoVisibilidade";

	private static final String R1 = "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.usuario.idUsuario"
			+ " = #{pessoaServidorHome.instance.idUsuario}";
	
	private static final String R2 = "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.papel != #{parametroUtil.getPapelMagistrado()}";
	
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

		StringBuilder sb = new StringBuilder("select o from UsuarioLocalizacaoVisibilidade o ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND o.usuarioLocalizacaoMagistradoServidor.vinculacaoUsuario IS NULL ");

		if(orgaoJulgadorColegiadoAtual != null || orgaoJulgadorAtual != null) {
			sb.append(" AND o.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor IN (")
				.append(" SELECT ulms.idUsuarioLocalizacaoMagistradoServidor FROM UsuarioLocalizacaoMagistradoServidor AS ulms ")
				.append(" WHERE 1=1 ");
			
			if(orgaoJulgadorColegiadoAtual != null) {
				sb.append(" AND ulms.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+ orgaoJulgadorColegiadoAtual.getIdOrgaoJulgadorColegiado());
			}
			if(orgaoJulgadorAtual != null) {
				sb.append(" AND ulms.orgaoJulgador.idOrgaoJulgador = "+ orgaoJulgadorAtual.getIdOrgaoJulgador());
			}
			sb.append(" ) ");
		}

		if(localizacaoFisica != null && localizacaoFisica.getFaixaInferior() != null) {
			sb.append(" AND o.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor IN (")
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
		Map<String, String> map = new HashMap<String, String>();
		map.put("magistrado", "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.usuario.nome");
		map.put("localizacao", "o.usuarioLocalizacaoMagistradoServidor.usuarioLocalizacao.localizacaoFisica.localizacao");
		return map;
	}

}