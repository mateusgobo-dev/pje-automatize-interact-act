package br.jus.cnj.pje.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.ResultadoSentencaParte;

/**
 * Lista de resultados de sentenças das partes de um processo
 * 
 * @since 1.4.2
 * @category PJE-JT
 * @created 2011-09-13
 * @author Kelly Leal, Rafael Barros
 */

@Name(ResultadoSentencaParteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ResultadoSentencaParteList extends EntityList<ResultadoSentencaParte> {

	private static final String SENTENCA_STRING = "sentença";
	private static final String JUSTICA_TRABALHO = "JT";
	public static final String TIPO_JUSTICA = ComponentUtil.getComponent("tipoJustica");

	public static final String NAME = "resultadoSentencaParteList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ResultadoSentencaParte o ";

	private static final String R1 = "o.resultadoSentenca.homologado = #{false}";
	private static final String R2 = "o.resultadoSentenca.processoTrf = #{processoTrfHome.instance}";

	private static final String DEFAULT_ORDER = "o.beneficioOrdem";

	private Boolean mostraSentencas = Boolean.FALSE;

	public ResultadoSentencaParteList() {
		super();
		setEjbql(DEFAULT_EJBQL);
		setOrder(DEFAULT_ORDER);
		setMaxResults(DEFAULT_MAX_RESULT);
		setMostraSentencas();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("homologado", SearchCriteria.igual, R1);
		addSearchField("processoTrf", SearchCriteria.igual, R2);
	}

	@Override
	public List<ResultadoSentencaParte> getResultList() {
		if (mostraSentencas) {
			return super.getResultList();
		}
		return new ArrayList<ResultadoSentencaParte>();
	}

	@Override
	public Long getResultCount() {
		if (mostraSentencas) {
			return super.getResultCount();
		}
		return 0L;
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
	
	/**
	 * PJEII-3000 - PJEII-3001
	 * Inclusão da chamada do método setMostrarSentencasRevisarMinuta para recuperação da lista de resultado de 
	 * sentença parte list na página de revisarMinuta.xhtml
	 */
	public void setMostrarSentencasRevisarMinuta() {
		if (ParametroJtUtil.instance().justicaTrabalho()) {
			mostraSentencas = Boolean.TRUE;
		} else {
			mostraSentencas = Boolean.FALSE;
		}
	}

	public void setMostraSentencas() {
		ProcessoHome processoHome = (ProcessoHome) Component.getInstance("processoHome");

		List<ResultadoSentencaParte> sentencaParteList = super.getResultList();

		if (TIPO_JUSTICA.equalsIgnoreCase(JUSTICA_TRABALHO) && processoHome.getTipoProcessoDocumento() != null
				&& processoHome.getTipoProcessoDocumento().getTipoProcessoDocumento().equalsIgnoreCase(SENTENCA_STRING)
				&& sentencaParteList != null && sentencaParteList.size() > 0) {
			mostraSentencas = Boolean.TRUE;
		} else {
			mostraSentencas = Boolean.FALSE;
		}
	}

	public Boolean getMostraSentencas() {
		return mostraSentencas;
	}
	
	public void setMostraSentencas(boolean mostraSentencas) {
		this.mostraSentencas = mostraSentencas;
	}
}