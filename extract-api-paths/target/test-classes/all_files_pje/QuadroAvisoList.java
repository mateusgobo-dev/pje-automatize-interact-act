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
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.QuadroAviso;

@Name(QuadroAvisoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class QuadroAvisoList extends EntityList<QuadroAviso> {

	public static final String NAME = "quadroAvisoList";
	private static final long serialVersionUID = 1L;

	private Date dataPublicacao;
	private Date dataPublicacaoFinal;
	private Date dataPublicacaoInicial;

	private static final String DEFAULT_ORDER = "o.usuarioInclusao";
	private static final String R1 = "cast(o.dataPublicacao as date) >= #{quadroAvisoList.dataPublicacaoInicial}";
	private static final String R2 = "cast(o.dataPublicacao as date) <= #{quadroAvisoList.dataPublicacaoFinal}";

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from QuadroAviso o ");
		String papel = Authenticator.getPapelAtual().getIdentificador();

		if (!papel.equalsIgnoreCase("administrador") && !papel.equalsIgnoreCase("admin")) {
			sb.append("where o.usuarioInclusao in ( select ul.usuario from UsuarioLocalizacao ul where ul.usuario = ");
			sb.append(Authenticator.getUsuarioLogado().getIdUsuario()).append(")");
		}
		return sb.toString();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("titulo", SearchCriteria.contendo);
		addSearchField("dataPublicacao", SearchCriteria.igual, R1);
		addSearchField("dataExpiracao", SearchCriteria.igual, R2);
		addSearchField("ativo", SearchCriteria.igual);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("titulo", "o.titulo");
		map.put("dataPublicacao", "o.dataPublicacao");
		return map;
	}
	
	public void clearQuadroAviso(){
		this.setDataPublicacao(null);
		this.setDataPublicacaoFinal(null);
		this.setDataPublicacaoInicial(null);
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public Date getDataPublicacao() {
		return dataPublicacao;
	}

	public void setDataPublicacao(Date dataPublicacao) {
		this.dataPublicacao = dataPublicacao;
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
