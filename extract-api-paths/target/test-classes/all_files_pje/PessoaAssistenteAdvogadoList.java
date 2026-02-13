package br.com.infox.pje.list;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaAssistenteAdvogadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaAssistenteAdvogadoList extends EntityList<PessoaAssistenteAdvogado> {

	public static final String NAME = "pessoaAssistenteAdvogadoList";
	private static final long serialVersionUID = 1L;

	private Localizacao localizacao;
	private PessoaAdvogado advogado;
	private String numeroCPF;
	private Boolean assistenteAdvogadoAtivo = Boolean.TRUE;

	private static final String DEFAULT_ORDER = "nome";

	private static final String R1 = "o.pessoa in (select pf from PessoaFisica pf "
			+ "inner join pf.pessoaDocumentoIdentificacaoList pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento = #{pessoaAssistenteAdvogadoList.numeroCPF})";

	private static final String R2 = "concat('%',lower(to_ascii(nome)),'%') like concat('%',lower(to_ascii(#{pessoaAssistenteAdvogadoList.entity.nome.replace(' ', '%')})),'%')";

	private static String R3() {
		return "o.pessoa in (select a.usuario from PessoaAssistenteAdvogadoLocal a, "
				+ " UsuarioLocalizacao ulAdvogado "
				+ " WHERE ulAdvogado.localizacaoFisica.idLocalizacao = a.localizacaoFisica.idLocalizacao "
				+ " AND ulAdvogado.usuario.idUsuario = #{advogadoSuggest.instance.idUsuario} "
				+ " AND ulAdvogado.papel.identificador = '"+ParametroUtil.instance().getPapelAdvogado().getIdentificador()+"' "
				+ " )";
	}
	
	@Override
	protected void addSearchFields() {
		addSearchField("numeroCPF", SearchCriteria.igual, R1);
		addSearchField("nome", SearchCriteria.contendo, R2);
		addSearchField("advogado", SearchCriteria.igual, R3());
	}

	@Override
	public void newInstance() {
		setLocalizacao(null);
		setNumeroCPF(null);
		setAdvogado(null);
		setAssistenteAdvogadoAtivo(Boolean.TRUE);
		Contexts.removeFromAllContexts("advogadoSuggest");
		super.newInstance();
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder("SELECT o FROM PessoaAssistenteAdvogado o WHERE o.pessoa.unificada = FALSE ");
		if (Authenticator.isUsuarioExterno()) {
			sb.append("AND o.pessoa in (select a.usuario from PessoaAssistenteAdvogadoLocal a ")
				.append("where a.localizacaoFisica = #{authenticator.getLocalizacaoAtual()}) ");
		}
		if (getAssistenteAdvogadoAtivo() != null) {
			if (getAssistenteAdvogadoAtivo()) {
				sb.append("AND bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.ASA + ") = " + PessoaFisica.ASA);
			} else {
				sb.append("AND bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.ASA + ") != " + PessoaFisica.ASA);
			}
		}
		return sb.toString();
	}
	
	@Override
	public List<PessoaAssistenteAdvogado> getResultList() {
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

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
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
	
	public Boolean getAssistenteAdvogadoAtivo() {
		return assistenteAdvogadoAtivo;
	}
	
	public void setAssistenteAdvogadoAtivo(Boolean assistenteAdvogadoAtivo) {
		this.assistenteAdvogadoAtivo = assistenteAdvogadoAtivo;
	}

	public PessoaAdvogado getAdvogado() {
		return advogado;
	}

	public void setAdvogado(PessoaAdvogado advogado) {
		this.advogado = advogado;
	}
}