package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;

@Name(AplicacaoMovimentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AplicacaoMovimentoList extends EntityList<AplicacaoMovimento>{

	public static final String NAME = "aplicacaoMovimentoList";

	private static final long serialVersionUID = 1L;

	private static final String R1 = "o.eventoProcessual = #{eventoHome.instance}";

	protected void addSearchFields(){
		addSearchField("eventoProcessual", SearchCriteria.igual, R1);
	}

	protected Map<String, String> getCustomColumnsOrder(){
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}
	
	protected String getDefaultEjbql(){
		return "select o from AplicacaoMovimento o, AplicabilidadeView av "
				+ " WHERE o.aplicabilidade.idAplicabilidade = av.idAplicabilidade ";
	}

	protected String getDefaultOrder(){
		return "av.codigoAplicacaoClasse, av.orgaoJustica, av.sujeitoAtivo";
	}
}