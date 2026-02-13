package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name(PessoaAssistenteProcuradoriaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaAssistenteProcuradoriaList extends EntityList<PessoaAssistenteProcuradoria> {

	public static final String NAME = "pessoaAssistenteProcuradoriaList";

	private static final long serialVersionUID = 1L;

	private String numeroCPF;
	private Boolean assistenteProcuradoriaAtivo = Boolean.TRUE;
	private TipoProcuradoriaEnum tipoProcuradoria; 
	private Procuradoria procuradoria;

	private static final String DEFAULT_ORDER = "nome";

	private static final String R1 = "o in (select pf from PessoaFisica pf "
			+ "inner join pf.pessoaDocumentoIdentificacaoList pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento = #{pessoaAssistenteProcuradoriaList.numeroCPF})";

	private static final String R2 = "concat('%',lower(to_ascii(nome)),'%') like concat('%',lower(to_ascii(#{pessoaAssistenteProcuradoriaList.entity.nome.replace(' ', '%')})),'%')";
	
	private static String R3() {
		StringBuilder sb = new StringBuilder("o.pessoa in (select a.usuario from PessoaAssistenteProcuradoriaLocal a ")
				.append(" WHERE a.procuradoria.tipo = #{pessoaAssistenteProcuradoriaList.tipoProcuradoria}) ");
		return sb.toString();
	}
	
	private static String R4() {
		StringBuilder sb = new StringBuilder("o.pessoa in (select a.usuario from PessoaAssistenteProcuradoriaLocal a ")
				.append(" WHERE a.procuradoria = #{pessoaAssistenteProcuradoriaList.procuradoria}) ");
		return sb.toString();
	}
	
	@Override
	protected void addSearchFields() {
		this.addSearchField("numeroCPF", SearchCriteria.igual, R1);
		this.addSearchField("nome", SearchCriteria.contendo, R2);
		this.addSearchField("tipoProcuradoria", SearchCriteria.igual, R3());
		this.addSearchField("procuradoria", SearchCriteria.igual, R4());
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM PessoaAssistenteProcuradoria o WHERE o.pessoa.unificada = FALSE ");
		if (Authenticator.isUsuarioExterno()) {
			sb.append("AND o.pessoa in (select a.usuario from PessoaAssistenteProcuradoriaLocal a ")
				.append("where a.localizacaoFisica = #{authenticator.getLocalizacaoAtual()}) ");
		}

		if(getAssistenteProcuradoriaAtivo() != null && getAssistenteProcuradoriaAtivo() == true){
			sb.append("AND (bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.ASP + ") = " + PessoaFisica.ASP + ") ");
		}else if(getAssistenteProcuradoriaAtivo() != null && getAssistenteProcuradoriaAtivo() == false){
			sb.append("AND (bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.ASP + ") != " + PessoaFisica.ASP + ") ");
		}
		return sb.toString();
	}
	
	@Override
	public List<PessoaAssistenteProcuradoria> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}		

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public String getNumeroCPF() {
		return this.numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public Boolean getAssistenteProcuradoriaAtivo() {
		return assistenteProcuradoriaAtivo;
	}
	
	public void setAssistenteProcuradoriaAtivo(Boolean assistenteProcuradoriaAtivo) {
		this.assistenteProcuradoriaAtivo = assistenteProcuradoriaAtivo;
	}
	
	public TipoProcuradoriaEnum getTipoProcuradoria() {
		return tipoProcuradoria;
	}

	public void setTipoProcuradoria(TipoProcuradoriaEnum tipoProcuradoria) {
		this.tipoProcuradoria = tipoProcuradoria;
	}

	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	public List<Procuradoria> getProcuradoriasList() {
		ProcuradoriaManager procuradoriaManager = ComponentUtil.getComponent("procuradoriaManager");
		return procuradoriaManager.getlistProcuradorias(tipoProcuradoria);
	}

	@Override
	public void newInstance() {
		setNumeroCPF(null);
		setTipoProcuradoria(null);
		setProcuradoria(null);
		super.newInstance();
	}
}