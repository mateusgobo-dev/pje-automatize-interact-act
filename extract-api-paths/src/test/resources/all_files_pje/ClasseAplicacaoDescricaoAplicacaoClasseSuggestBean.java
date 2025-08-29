package br.com.infox.cliente.component.suggest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.CompetenciaClasseAssuntoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;

@Name("classeAplicacaoDescricaoAplicacaoClasseSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ClasseAplicacaoDescricaoAplicacaoClasseSuggestBean extends AbstractSuggestBean<AplicacaoClasse> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		if (getHome().getClasseJudicial() == null) {
			return null;
		}

		int cj = getHome().getClasseJudicial().getIdClasseJudicial();

		StringBuilder sb = new StringBuilder();
		sb.append(" select distinct ap from ClasseAplicacao o ");
		sb.append(" inner join o.aplicacaoClasse ap ");
		sb.append(" where lower(TO_ASCII(ap.aplicacaoClasse)) like  lower(concat('%', TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append(" ), '%')) ");
		sb.append(" and o.orgaoJustica.idOrgaoJustica = ").append(ParametroUtil.instance().getOrgaoJustica().getIdOrgaoJustica());
		sb.append(" and o.classeJudicial.idClasseJudicial = ").append(cj);
		sb.append(" and o.ativo = true ");
		sb.append(" order by ap.aplicacaoClasse");
		return sb.toString();
	}

	protected CompetenciaClasseAssuntoHome getHome() {
		return (CompetenciaClasseAssuntoHome) Component.getInstance("competenciaClasseAssuntoHome");
	}

	@Override
	public String getDefaultValue() {
		String aplicacaoClasse = "";
		if (getInstance() != null) {
			if (getInstance().getAplicacaoClasse() != null) {
				aplicacaoClasse = getInstance().getAplicacaoClasse();
			}
		}
		return aplicacaoClasse;
	}
}
