package br.com.infox.pje.list;

import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoDominio;

@Name(AplicacaoDominioList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AplicacaoDominioList extends EntityList<AplicacaoDominio>{

	public static final String NAME = "aplicacaoDominioList";

	private static final long serialVersionUID = 1L;

	private static final String R1 = "t.idTipoComplemento = #{tipoComplementoHome.instance.idTipoComplemento}  and o in elements(t.aplicacaoDominioList)";

	protected void addSearchFields(){
		addSearchField("idTipoComplemento", SearchCriteria.igual, R1);
	}

	protected Map<String, String> getCustomColumnsOrder(){
		return null;
	}

	protected String getDefaultEjbql(){
		return "select o from AplicacaoDominio o, TipoComplementoComDominio t, AplicabilidadeView av "
				+ " WHERE o.aplicabilidade.idAplicabilidade = av.idAplicabilidade ";
	}

	protected String getDefaultOrder(){
		return "av.codigoAplicacaoClasse, av.orgaoJustica, av.sujeitoAtivo";
	}

}