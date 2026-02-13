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
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaConciliadorList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaConciliadorList extends EntityList<PessoaFisica> {

	public static final String NAME = "pessoaConciliadorList";
	private static final long serialVersionUID = 1L;

	private String numeroCPF;
	private Localizacao localizacaoFisica;
	private Localizacao localizacaoFisicaRoot;

	private static final String DEFAULT_ORDER = "nome";

	private static final String R1 = "o in (select pf from PessoaFisica pf "
			+ "inner join pf.pessoaDocumentoIdentificacaoList pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento = #{pessoaConciliadorList.numeroCPF})";

	private static final String R2 = "concat('%',lower(to_ascii(nome)),'%') like concat('%',lower(to_ascii(#{pessoaConciliadorList.entity.nome.replace(' ', '%')})),'%')";

	private static String R3() {
		return "o in (select ul.usuario from UsuarioLocalizacao ul "
				+ " WHERE ul.localizacaoFisica.idLocalizacao = #{empty pessoaConciliadorList.localizacaoFisica ? null : pessoaConciliadorList.localizacaoFisica.idLocalizacao} "
				+ " )";
	}
	
	@Override
	protected void addSearchFields() {
		addSearchField("numeroCPF", SearchCriteria.igual, R1);
		addSearchField("nome", SearchCriteria.contendo, R2);
		addSearchField("localizacao", SearchCriteria.igual, R3());
	}

	@Override
	public void newInstance() {
		setLocalizacaoFisica(null);
		setNumeroCPF(null);
		super.newInstance();
	}

	@Override
	protected String getDefaultEjbql() {
		Integer idLocalizacaoFisica = Authenticator.getIdLocalizacaoFisicaAtual();
		String idsLocalizacoesFisicasFilhas = Authenticator.getIdsLocalizacoesFilhasAtuais();
		if(idsLocalizacoesFisicasFilhas == null || idsLocalizacoesFisicasFilhas.trim().isEmpty()) {
			idsLocalizacoesFisicasFilhas = idLocalizacaoFisica.toString();
		}
		StringBuilder sb = new StringBuilder("SELECT o FROM PessoaFisica o ")
			.append(" WHERE o.unificada = FALSE ")
			.append(" AND o in (")
			.append("	SELECT u.usuario ")
			.append("	FROM UsuarioLocalizacao u ")
			.append("	WHERE u.papel.identificador = '"+Papeis.CONCILIADOR+"'")
			.append("	AND u.localizacaoFisica.idLocalizacao IN ("+idsLocalizacoesFisicasFilhas+")")
			.append(" ) ");
		
		return sb.toString();
	}
	
	@Override
	public List<PessoaFisica> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}	
		

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public void setNumeroCPF(String numeroCPF) {
		if(StringUtil.CPF_EMPTYMASK.equals(numeroCPF)){
			numeroCPF = "";
		}
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public Localizacao getLocalizacaoFisica() {
		return localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}

	public Localizacao getLocalizacaoFisicaRoot() {
		if(this.localizacaoFisicaRoot == null) {
			this.localizacaoFisicaRoot = Authenticator.getLocalizacaoFisicaAtual();
		}
		return this.localizacaoFisicaRoot;
	}

	public void setLocalizacaoFisicaRoot(Localizacao localizacaoFisicaRoot) {
		this.localizacaoFisicaRoot = localizacaoFisicaRoot;
	}
}