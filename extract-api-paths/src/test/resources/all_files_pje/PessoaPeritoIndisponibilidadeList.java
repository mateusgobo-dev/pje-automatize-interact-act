package br.com.infox.pje.list;

import java.util.Date;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.home.PessoaPeritoIndisponibilidadeHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;

@Name(PessoaPeritoIndisponibilidadeList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaPeritoIndisponibilidadeList extends EntityList<PessoaPeritoIndisponibilidade> {

	public static final String NAME = "pessoaPeritoIndisponibilidadeList";
	
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.pessoaPeritoEspecialidade";

	private Date dataInicio;
	
	private Date dataFim;

	private static final String R1 = "lower(to_ascii(o.pessoaPeritoEspecialidade.pessoaPerito.nome)) like concat('%',lower(to_ascii(#{pessoaPeritoIndisponibilidadeHome.pessoaPeritoEspecialidade})),'%')";

	private static final String R2 = "cast(o.dtInicio as date) = #{pessoaPeritoIndisponibilidadeList.dataInicio}";

	private static final String R3 = "cast(o.dtFim as date) = #{pessoaPeritoIndisponibilidadeList.dataFim}";

	@Override
	protected void addSearchFields() {
		addSearchField("pessoaPeritoEspecialidade.pessoaPerito.nome", SearchCriteria.contendo, R1);
		addSearchField("dtInicio", SearchCriteria.contendo, R2);
		addSearchField("dtFim", SearchCriteria.igual, R3);
		addSearchField("horaInicio", SearchCriteria.igual);
		addSearchField("horaFim", SearchCriteria.igual);
		addSearchField("ativo", SearchCriteria.igual);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder query = new StringBuilder("select DISTINCT o ")
			.append(" FROM PessoaPeritoIndisponibilidade o ")
			.append(" 	JOIN o.pessoaPeritoEspecialidade.pessoaPerito pessoaPerito ");
		return query.toString() + obterRestricaoVisibilidade();
	}
	
	private String obterRestricaoVisibilidade() {
		StringBuilder query = new StringBuilder();
		if (Authenticator.isPapelPerito()) {
			query.append(" where pessoaPerito.idUsuario = #{usuarioLogado.idUsuario}");
		} else {
			String idsLocalizacoesFilhas = Authenticator.getIdsLocalizacoesFilhasAtuais();
			query.append(" join pessoaPerito.orgaoJulgadorPessoaPeritoList ojList ")
				.append(" join ojList.orgaoJulgador.localizacao localizacao where localizacao.idLocalizacao in ("+idsLocalizacoesFilhas+")");
		}
		return query.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		return dataFim;
	}

	@Override
	public void newInstance() {
		dataInicio = null;
		dataFim = null;
		PessoaPeritoIndisponibilidadeHome.instance().setPessoaPeritoEspecialidade(null);
		super.newInstance();
	}
}