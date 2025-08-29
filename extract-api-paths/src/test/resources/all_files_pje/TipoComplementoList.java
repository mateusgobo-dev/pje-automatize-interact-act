package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;

@Name(TipoComplementoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class TipoComplementoList extends EntityList<TipoComplemento>{

	public static final String NAME = "tipoComplementoList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from TipoComplemento o";
	private static final String DEFAULT_ORDER = "nome";

	private static final String R1 = "LOWER(o.codigo) like LOWER(concat(#{tipoComplementoList.entity.codigo}, '%'))";

	private static final String R2 = "LOWER(o.nome) like LOWER(concat(#{tipoComplementoList.entity.nome}, '%'))";

	private static final String R3 = "o.class = #{tipoComplementoList.entity.tipoComplemento.name()}";

	public TipoComplementoList(){
		super();
		setEjbql(DEFAULT_EJBQL);
		setOrder(DEFAULT_ORDER);
		setMaxResults(DEFAULT_MAX_RESULT);
	}

	protected void addSearchFields(){
		addSearchField("ativo", SearchCriteria.igual);
		addSearchField("codigo", SearchCriteria.igual, R1);
		addSearchField("nome", SearchCriteria.igual, R2);
		addSearchField("tipoComplemento", SearchCriteria.igual, R3);

	}

	protected Map<String, String> getCustomColumnsOrder(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("tipoComplemento", "o.class");
		return map;
	}

	protected String getDefaultEjbql(){
		return DEFAULT_EJBQL;
	}

	protected String getDefaultOrder(){
		return DEFAULT_ORDER;
	}

	public boolean showGrid(){
		return false;
	}

}