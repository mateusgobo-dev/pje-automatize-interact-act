package br.com.infox.list;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.bean.ConsultaEntidadeLog;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Name(EntidadeLogList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EntidadeLogList extends EntityList<EntityLog> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "entidadeLogList";

	private ConsultaEntidadeLog instance = new ConsultaEntidadeLog();
	private String nomeClasse;
	private String nomePackage;
	private String idEntidade;
	private Integer idPesquisa;

	private static final String DEFAULT_EJBQL = "select o from EntityLog o";
	private static final String DEFAULT_ORDER = "dataLog desc";

	private static final String R1 = "ip like concat(lower(#{entidadeLogList.instance.ip}),'%')";
	private static final String R2 = "idUsuario in (#{entidadeLogList.getIdsUsuariosPorNome(entidadeLogList.instance.nomeUsuario)})";
	private static final String R3 = "nomeEntidade = #{entidadeLogList.instance.nomeEntidade}";
	private static final String R4 = "tipoOperacao = #{entidadeLogList.instance.tipoOperacaoLogEnum}";
	// private static final String R5 =
	// "nomeEntidade = #{entidadeLogList.nomeClasse}";
	private static final String R5 = "nomePackage = #{entidadeLogList.nomePackage}";
	private static final String R6 = "idEntidade = #{entidadeLogList.idEntidade}";
	private static final String R7 = "cast(dataLog as date) >= #{entidadeLogList.instance.dataInicio}";
	private static final String R8 = "cast(dataLog as date)<= #{entidadeLogList.instance.dataFim}";

	@Override
	protected void addSearchFields() {
		addSearchField("ip", SearchCriteria.iniciando, R1);
		addSearchField("idUsuario", SearchCriteria.igual, R2);
		addSearchField("nomeEntidade", SearchCriteria.igual, R3);
		addSearchField("tipoOperacao", SearchCriteria.igual, R4);
		// addSearchField("nomeEntidade", SearchCriteria.igual, R5);
		addSearchField("nomePackage", SearchCriteria.igual, R5);
		addSearchField("idEntidade", SearchCriteria.igual, R6);
		addSearchField("dataLogInicio", SearchCriteria.maiorIgual, R7);
		addSearchField("dataLogFim", SearchCriteria.menorIgual, R8);
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

	@Override
 	public EntityManager getEntityManager(){
		return (EntityManager) Component.getInstance("entityManagerLog");
 	}
	
	public TipoOperacaoLogEnum[] getTipoOperacaoLogEnumValues() {
		return TipoOperacaoLogEnum.values();
	}

	public void limparTela() {
		instance = new ConsultaEntidadeLog();
		setIdPesquisa(null);
		setNomeClasse(null);
		setIdEntidade(null);
		setNomePackage(null);
	}

	public List<EntityLogDetail> getEntityLogDetailList(Long idEntityLog) {
		return getEntityManager().find(EntityLog.class, idEntityLog).getLogDetalheList();
	}

	public ConsultaEntidadeLog getInstance() {
		return instance;
	}

	public void setInstance(ConsultaEntidadeLog instance) {
		this.instance = instance;
	}

	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}

	public String getNomePackage() {
		return nomePackage;
	}

	public void setNomePackage(String nomePackage) {
		this.nomePackage = nomePackage;
	}

	public String getIdEntidade() {
		return idEntidade;
	}

	public void setIdEntidade(String idEntidade) {
		this.idEntidade = idEntidade;
	}

	public Integer getIdPesquisa() {
		return idPesquisa;
	}

	public void setIdPesquisa(Integer idPesquisa) {
		this.idPesquisa = idPesquisa;
	}
	
	@SuppressWarnings("unchecked")
	public List<EntityLog> getEntidades() {
		String query = "select distinct o.nomeEntidade as entidade from EntityLog o order by o.nomeEntidade";
		Query q = getEntityManager().createQuery(query);
		return (List<EntityLog>) q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getIdsUsuariosPorNome(String nome) {
		if (instance.getIdsUsuarios().size() == 0) {
			if( nome == null || nome.isEmpty() ) 
				return null;
			
			String query = "select o.idUsuario from UsuarioLogin o where o.ativo = true and lower(to_ascii(o.nome)) like concat('%',to_ascii(:nome), '%')";
			Query q = EntityUtil.getEntityManager().createQuery(query);
			q.setParameter("nome", nome.toLowerCase());
			List<Integer> ids = q.getResultList();
			if (ids.size() > 0) {
				instance.setIdsUsuarios(q.getResultList());
			} else {
				instance.getIdsUsuarios().add(-1);
			}
		}
		return instance.getIdsUsuarios();
	}
}
