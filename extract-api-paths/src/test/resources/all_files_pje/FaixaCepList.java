package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.Bairro;
import br.jus.pje.nucleo.entidades.FaixaCep;

@Name(FaixaCepList.NAME)
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class FaixaCepList extends EntityList<FaixaCep> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "faixaCepList";
	private static final String DEFAULT_EJBQL = "select o from FaixaCep o";
	private static final String DEFAULT_ORDER = "o.cepInicial";
	private Bairro bairro;
	// private Cep cepInicial;
	// private Cep cepFinal;

	private static String R1 = "#{bairroHome.instance} = o.bairro";

	// private static String R2 =
	// "#{bairroHome.instance.municipio} = o.cepInicial.municipio";
	// private static String R3 =
	// "#{bairroHome.instance.municipio} = o.cepFinal.municipio";

	@Override
	protected void addSearchFields() {
		// addSearchField("cepInicial", SearchCriteria.igual, R2);
		// addSearchField("cepFinal", SearchCriteria.igual, R3);
		addSearchField("bairro", SearchCriteria.igual, R1);
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

	public Bairro getBairro() {
		return bairro;
	}

	public void setBairro(Bairro bairro) {
		this.bairro = bairro;
	}

	/*
	 * public void setCepInicial(Cep cepInicial) { this.cepInicial = cepInicial;
	 * }
	 * 
	 * public Cep getCepInicial() { return cepInicial; }
	 * 
	 * public void setCepFinal(Cep cepFinal) { this.cepFinal = cepFinal; }
	 * 
	 * public Cep getCepFinal() { return cepFinal; }
	 */
}
