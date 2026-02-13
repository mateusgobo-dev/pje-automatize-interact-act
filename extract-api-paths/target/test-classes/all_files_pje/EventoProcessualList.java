package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.pje.nucleo.entidades.Evento;

@Name(EventoProcessualList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EventoProcessualList extends EntityList<Evento> {

	public static final String NAME = "eventoProcessualList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from Evento o";
	private static final String DEFAULT_ORDER = "caminhoCompleto";
	private Boolean ativo = null;
	private Boolean padraoSgt = null;

	private static final String R1 = "o.eventoSuperior.caminhoCompleto like concat("
			+ "#{eventoProcessualList.entity.eventoSuperior.caminhoCompleto}, '%')";
	private static final String R2 = "o.ativo = #{eventoProcessualList.ativo} ";
	private static final String R3 = "o.padraoSgt = #{eventoProcessualList.padraoSgt} ";

	public EventoProcessualList() {
		super();
		setEjbql(DEFAULT_EJBQL);
		setOrder(DEFAULT_ORDER);
		setMaxResults(DEFAULT_MAX_RESULT);
	}

	@Override
	protected void addSearchFields() {
		addSearchField("ativo", SearchCriteria.igual, R2);
		addSearchField("padraoSgt", SearchCriteria.igual, R3);
		addSearchField("codEvento", SearchCriteria.contendo);
		addSearchField("codEventoOutro", SearchCriteria.contendo);
		addSearchField("evento", SearchCriteria.contendo);
		addSearchField("eventoSuperior", SearchCriteria.contendo, R1);
		addSearchField("complementar", SearchCriteria.igual);
	}
	
	/**
	 * Metodo responsavel por buscar os eventos superiores ativos.
	 * 
	 * @return
	 */
	public List<Evento> buscarEventosSuperioresAtivos(){
		return ComponentUtil.getComponent(EventoManager.class).getEventosSuperiores(true);
	}
	
	/**
	 * Metodo responsavel por buscar os eventos ativos.
	 * 
	 * @return
	 */
	public List<Evento> buscarEventosAtivos(){
		return ComponentUtil.getComponent(EventoManager.class).getEventosAtivos();
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

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Boolean getPadraoSgt() {
		return padraoSgt;
	}

	public void setPadraoSgt(Boolean padraoSgt) {
		this.padraoSgt = padraoSgt;
	}

	@Override
	public void newInstance() {
		super.newInstance();
		setAtivo(null);
	}
}