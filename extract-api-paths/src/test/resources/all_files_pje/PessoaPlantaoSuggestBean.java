package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.PlantaoOficialJusticaHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Plantao;

@Name("pessoaPlantaoSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PessoaPlantaoSuggestBean extends AbstractSuggestBean<Pessoa> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		if (getSearch().getLocalizacao() != null) {
	    	LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
	    	String localizacoes = localizacaoService.getTreeIds(getSearch().getLocalizacao());
	    	
			return "select distinct o from PessoaFisica o inner join o.usuarioLocalizacaoList ul "
				+ " where ul.localizacaoFisica.idLocalizacao in " + localizacoes
				+ " and lower(TO_ASCII(o.nome)) like " + "lower(concat('%', TO_ASCII(:" + INPUT_PARAMETER + "), '%')) " 
				+ " and bitwise_and(o.especializacoes, " + PessoaFisica.OFJ + ") = " + PessoaFisica.OFJ
				+ " order by o.nome";
		}
		return null;
	}

	protected Plantao getSearch() {
		PlantaoOficialJusticaHome ph = (PlantaoOficialJusticaHome) Component.getInstance("plantaoOficialJusticaHome");
		if (ph.getTab().equals("form")) {
			return ph.getInstance();
		}
		return (Plantao) Component.getInstance("plantaoSearch");
	}

	@Override
	protected String getEventSelected() {
		return "pessoaPlantaoSuggEvent";
	}

}
