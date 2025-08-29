/* $Id: JurisdicaoMunicipioSuggestBean.java 10746 2010-08-12 23:23:46Z jplacerda $ */

package br.jus.csjt.pje.view.action.component;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.jt.entidades.VaraItinerante;

/**
 * @author Rafael Barros / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class VaraItineranteSuggestBean
 * @description Classe responsável pela implementação da suggest da entidade
 *              JurisdicaoMunicipio
 */
@Name("varaItineranteSuggest")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class VaraItineranteSuggestBean extends AbstractSuggestBean<VaraItinerante> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from JurisdicaoMunicipio o ");
		sb.append(" where o.jurisdicao.idJurisdicao = #{jurisdicaoHome.instance.idJurisdicao}");
		sb.append("   and ");
		sb.append("lower(TO_ASCII(o.municipio.municipio)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) order by 1");
		return sb.toString();
	}

}
