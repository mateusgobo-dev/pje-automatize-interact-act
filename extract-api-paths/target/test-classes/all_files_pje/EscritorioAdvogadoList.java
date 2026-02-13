package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Localizacao;

@Name(EscritorioAdvogadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EscritorioAdvogadoList extends EntityList<Localizacao> {

	public static final String NAME = "escritorioAdvogadoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Localizacao o "
			+ "where o.localizacao in (select p.nome from Pessoa p "
			+ "						 where p.tipoPessoa.idTipoPessoa = #{parametroUtil.tipoPessoaEscritorioAdvocacia.idTipoPessoa})";
	private static final String DEFAULT_ORDER = "localizacao";

	private static final String R1 = "o not in (select u.localizacaoFisica from UsuarioLocalizacao u where u.usuario.idUsuario = #{pessoaAdvogadoHome.instance.idUsuario})";
	private static final String R2 = "#{pessoaAdvogadoHome.cnpj} != '0'";
	private static final String R3 = "o.localizacao in (select p.nome from Pessoa p "
			+ "where p in (select pl.pessoa from PessoaLocalizacao pl "
			+ "where pl.pessoa in (select pdi.pessoa from PessoaDocumentoIdentificacao pdi "
			+ "where pdi.tipoDocumento = 'CPJ' " + "and pdi.numeroDocumento = #{pessoaAdvogadoHome.cnpj})))";

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual, R1);
		addSearchField("localizacao", SearchCriteria.igual, R2);
		addSearchField("idLocalizacao", SearchCriteria.igual, R3);
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
		return null;
	}

}