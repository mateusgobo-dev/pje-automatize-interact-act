package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.jus.pje.nucleo.entidades.OficialJusticaCentralMandado;

@Name(PessoaOficialJusticaCentralMandadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaOficialJusticaCentralMandadoList extends EntityList<OficialJusticaCentralMandado> {

	public static final String NAME = "pessoaOficialJusticaCentralMandadoList";
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "centralMandado.centralMandado";

	@Override
	protected void addSearchFields() {
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder("SELECT o FROM OficialJusticaCentralMandado o ");
		sb.append("WHERE o.usuarioLocalizacao.usuario.idUsuario = #{oficialJusticaCentralMandadoHome.pessoa.idPessoa}");
		sb.append(" AND o.usuarioLocalizacao.papel in (#{oficialJusticaCentralMandadoHome.papeisOJ} )");
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