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
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.AudParteImportacao;

@Name(AudParteImportacaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AudParteImportacaoList extends EntityList<AudParteImportacao> {

	private static final String DEFAULT_EJBQL = "select o from AudParteImportacao o ";
	private static final String DEFAULT_ORDER = "o.idAudParteImportacao";

	private static final long serialVersionUID = 1401056105531721717L;
	public static final String NAME = "audParteImportacaoList";
	private static final String R1 = "o.audImportacao.idAudImportacao = #{audParteImportacaoList.idAudImportacao} ";
	private static final String R2 = "o.poloAtivoParte = #{audParteImportacaoList.poloAtivoParte} ";
	private List<AudParteImportacao> audParteImportacaoList = new ArrayList<AudParteImportacao>();

	private Integer idAudImportacao;
	private String poloAtivoParte;

	@Override
	protected void addSearchFields() {
		addSearchField("idAudImportacao", SearchCriteria.igual, R1);
		addSearchField("poloAtivoParte", SearchCriteria.igual, R2);
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

	public List<AudParteImportacao> getAudPartesImportacao(int maxResult, AudImportacao audImportacao, String poloAtivo) {
		return audParteImportacaoList;
	}

	public Integer getIdAudImportacao() {
		return idAudImportacao;
	}

	public void setIdAudImportacao(Integer idAudImportacao) {
		this.idAudImportacao = idAudImportacao;
	}

	public String getPoloAtivoParte() {
		return poloAtivoParte;
	}

	public void setPoloAtivoParte(String poloAtivoParte) {
		this.poloAtivoParte = poloAtivoParte;
	}

	public void poloAtivoParte(String poloAtivoParte) {
		this.poloAtivoParte = poloAtivoParte;
	}
}
