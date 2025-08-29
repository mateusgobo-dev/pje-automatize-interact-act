package br.com.infox.pje.list;

import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoComplemento;

@Name(AplicacaoComplementoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AplicacaoComplementoList extends EntityList<AplicacaoComplemento>{

	public static final String NAME = "aplicacaoComplementoList";

	private static final long serialVersionUID = 1L;

	private static final String R1 = "o.aplicacaoMovimento.eventoProcessual = #{eventoHome.instance}";

	protected void addSearchFields(){
		addSearchField("eventoProcessual", SearchCriteria.igual, R1);
	}

	protected Map<String, String> getCustomColumnsOrder(){
		return null;
	}

	protected String getDefaultEjbql(){
		return "select o from AplicacaoComplemento o, AplicabilidadeView av "
				+ " WHERE o.aplicacaoMovimento.aplicabilidade.idAplicabilidade = av.idAplicabilidade ";
	}

	protected String getDefaultOrder(){
		return "av.codigoAplicacaoClasse, av.orgaoJustica, av.sujeitoAtivo";
	}
}