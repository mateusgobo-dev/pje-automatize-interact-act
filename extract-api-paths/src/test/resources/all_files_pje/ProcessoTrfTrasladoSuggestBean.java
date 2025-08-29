package br.com.infox.cliente.component.suggest;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.filters.ConsultaProcessoTrfFilter;
import br.jus.pje.nucleo.entidades.filters.ProcessoTrfFilter;
import br.jus.pje.search.Search;

@Name(ProcessoTrfTrasladoSuggestBean.NAME)
@Scope(ScopeType.CONVERSATION)
public class ProcessoTrfTrasladoSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	public static final String NAME = "processoTrfTrasladoSuggest";
	private static final long serialVersionUID = 1L;

	public static ProcessoTrfTrasladoSuggestBean instance() {
		return ComponentUtil.getComponent(NAME);
	}

	@Override
	public String getEjbql() {
		
		return null;
	}

	@Override
	public List<ProcessoTrf> suggestList(Object typed) {
		ProcessoJudicialManager manager = ProcessoJudicialManager.instance();
		
		Search s = new Search(ProcessoTrf.class);
		try {
			s.addCriteria(manager.getCriteriosConsultarProcessos(null, null, null, null, null, null, null, null, null, 
					null, null, null, null, null, null, null, null,
					null, null, null, null, null, null, null, null, 
					null, null, null, null, null, null ,null ,(String)typed, null, null));
			s.setMax(getLimitSuggest());
		} catch (NoSuchFieldException | PJeBusinessException e) {
			e.printStackTrace();
			return null;
		}
		
		return manager.list(s);
		
		
	}

}
