package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.QuadroAviso;

@Name(QuadroAvisoPapelMensagemList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class QuadroAvisoPapelMensagemList extends EntityList<QuadroAviso> {

	public static final String NAME = "quadroAvisoPapelMensagemList";
	private static final long serialVersionUID = 1L;

	private Date dataPublicacaoInicial;
	private Date dataPublicacaoFinal;

	private static final String DEFAULT_EJBQL = "select qa from QuadroAvisoPapel qap inner join qap.quadroAviso qa"
			+ " where qap.papel = #{authenticator.getPapelAtual()} " 
			+ " and qa.ativo = true ";

	private static final String DEFAULT_ORDER = "qa.topo desc, qa.dataPublicacao desc";

	private static final String R1 = "cast(qa.dataPublicacao as date) >= #{quadroAvisoPapelMensagemList.dataPublicacaoInicial}";
	private static final String R2 = "cast(qa.dataPublicacao as date) <= #{quadroAvisoPapelMensagemList.dataPublicacaoFinal}";
	private static final String R3 = "lower(qa.titulo) like concat('%', lower(#{quadroAvisoPapelMensagemList.entity.titulo}), '%')";

	@Override
	protected void addSearchFields() {
		addSearchField("dataPublicacao", SearchCriteria.igual, R1);
		addSearchField("dataExpiracao", SearchCriteria.igual, R2);
		addSearchField("titulo", SearchCriteria.contendo, R3);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public Date getDataPublicacaoFinal() {
		return dataPublicacaoFinal;
	}

	public void setDataPublicacaoFinal(Date dataPublicacaoFinal) {
		this.dataPublicacaoFinal = dataPublicacaoFinal;
	}

	public Date getDataPublicacaoInicial() {
		return dataPublicacaoInicial;
	}

	public void setDataPublicacaoInicial(Date dataPublicacaoInicial) {
		this.dataPublicacaoInicial = dataPublicacaoInicial;
	}

}
