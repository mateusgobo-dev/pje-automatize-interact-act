package br.com.jt.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(RecursoParteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class RecursoParteList extends EntityList<ProcessoParte> {

	public static final String NAME = "recursoParteList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = "select pp from ProcessoParte pp ";
	private static final String DEFAULT_ORDER = "pp.pessoa.nome";
	
	public static final String R1 = "pp.processoTrf.idProcessoTrf = #{processoTrfHome.managed ? processoTrfHome.instance.idProcessoTrf : processoHome.instance.idProcesso} ";
	public static final String R2 = "pp not in (#{assistenteAdmissibilidadeRecursoAction.assistenteAdmissibilidadeRecurso.listaProcessoParte}) ";
	public static final String R3 = "lower(pp.pessoa.nome) like concat('%', lower(#{recursoParteList.nome}), '%') ";
	public static final String R4 = "exists (select 1 from ProcessoParteRepresentante ppr " +
			                                "where ppr.processoParte = pp " +
			                                "and ppr.parteRepresentante.pessoa.idUsuario = #{assistenteAdmissibilidadeRecursoAction.assistenteAdmissibilidadeRecurso.processoDocumento.usuarioInclusao.idUsuario}) ";
	
	private String nome;
	
	protected void addSearchFields() {
		addSearchField("id", SearchCriteria.igual, R1);
		addSearchField("partes", SearchCriteria.igual, R2);
		addSearchField("nome", SearchCriteria.igual, R3);
		addSearchField("advogado", SearchCriteria.igual, R4);
	}
	
	@Override
	public void newInstance() {
		this.nome = null;
		super.newInstance();
		getResultList();
	}

	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	protected String getDefaultEjbql() {
		String ejbql = DEFAULT_EJBQL +
						" where (not exists (select 1 from PessoaAdvogado pa where pa.idUsuario = pp.pessoa.idUsuario) or not exists (select 1 from PessoaProcurador pr where pr.idUsuario = pp.pessoa.idUsuario)) "+
						" and pp.tipoParte.idTipoParte != #{parametroUtil.tipoParteAdvogado.idTipoParte} ";
		
		return ejbql;
	}

	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

}