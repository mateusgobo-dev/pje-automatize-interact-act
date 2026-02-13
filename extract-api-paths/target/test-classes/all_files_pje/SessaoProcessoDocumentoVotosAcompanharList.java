package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

@Name(SessaoProcessoDocumentoVotosAcompanharList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class SessaoProcessoDocumentoVotosAcompanharList extends EntityList<SessaoProcessoDocumentoVoto> {

	public static final String NAME = "sessaoProcessoDocumentoVotosAcompanharList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select spdv from SessaoProcessoDocumentoVoto spdv "
			+ "where spdv.tipoVoto.relator = false " + "and spdv.tipoVoto.contexto != 'C' " + "and spdv.tipoVoto.contexto != 'P' ";

	private static final String DEFAULT_ORDER = "spdv";

	protected static final String R1 = "spdv.sessao.idSessao = #{sessaoProcessoDocumentoVotoHome.sessaoPauta.sessao.idSessao} ";
	protected static final String R2 = "spdv.processoTrf.idProcessoTrf = #{sessaoProcessoDocumentoVotoHome.sessaoPauta.processoTrf.idProcessoTrf} ";
	protected static final String R3 = "spdv.ojAcompanhado != #{sessaoProcessoDocumentoVotoHome.sessaoPauta.processoTrf.orgaoJulgador} ";
	protected static final String R4 = "spdv.ojAcompanhado != #{orgaoJulgadorAtual} ";
	protected static final String R5 = "spdv.orgaoJulgador != #{orgaoJulgadorAtual} ";
	protected static final String R6 = "spdv.orgaoJulgador != #{abaVotarAction.sessaoProcessoDocumentoVoto.orgaoJulgador} ";
	
	private List<SessaoProcessoDocumentoVoto> votosMarcados = new ArrayList<SessaoProcessoDocumentoVoto>(0);

	@Override
	protected void addSearchFields() {
		addSearchField("processoTrf1", SearchCriteria.igual, R1);
		addSearchField("processoTrf2", SearchCriteria.igual, R2);
		addSearchField("ojAcompanhado", SearchCriteria.igual, R3);
		addSearchField("ojUsuarioLogado", SearchCriteria.diferente, R4);
		addSearchField("orgaoJulgador", SearchCriteria.diferente, R5);
		addSearchField("orgaoJulgador2", SearchCriteria.diferente, R6);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public List<SessaoProcessoDocumentoVoto> getVotosMarcados() {
		return votosMarcados;
	}

	public void setVotosMarcados(List<SessaoProcessoDocumentoVoto> votosMarcados) {
		this.votosMarcados = votosMarcados;
	}
	
	@Override
	public List<SessaoProcessoDocumentoVoto> list(int maxResult) {
		votosMarcados = super.list(maxResult);
		
		return votosMarcados;
	}

}
