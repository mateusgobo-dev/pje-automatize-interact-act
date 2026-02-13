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
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaMagistradoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaMagistradoList extends EntityList<PessoaMagistrado> {

	public static final String NAME = "pessoaMagistradoList";
	private static final long serialVersionUID = 1L;
	private String numeroCPF;
	private Localizacao localizacao;
	private Boolean magistradoAtivo = Boolean.TRUE;

	private static final String DEFAULT_ORDER = "dataPosse,nome";

	private static final String R1 = "o in (select pf from PessoaFisica pf "
			+ "inner join pf.pessoaDocumentoIdentificacaoList pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento = #{pessoaMagistradoList.numeroCPF})";

	private static final String R2 = "o in (select l.usuario from UsuarioLocalizacao l "
			+ "where l.localizacaoFisica = #{pessoaMagistradoList.localizacao})";

	private static final String R3 = "concat('%',lower(to_ascii(nome)),'%') like concat('%',lower(to_ascii(#{pessoaMagistradoList.entity.nome.replace(' ', '%')})),'%')";
	
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaMagistrado o ");
		sb.append("WHERE o.pessoa.unificada = false");

		if(getMagistradoAtivo() != null) {
			if(getMagistradoAtivo()) {
				sb.append(" AND (bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.MAG + ") = " + PessoaFisica.MAG + ") ");
			}else {
				sb.append(" AND (bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.MAG + ") != " + PessoaFisica.MAG + ") ");
			}
		}
		
		if (Authenticator.isMagistrado()) {
			sb.append(" AND ");
			sb.append("o in (select ul.usuario from UsuarioLocalizacao ul where ");
			sb.append("ul.idUsuarioLocalizacao in (select ulms.idUsuarioLocalizacaoMagistradoServidor from UsuarioLocalizacaoMagistradoServidor  ulms) ");
			sb.append("and ul.localizacaoFisica = #{authenticator.getLocalizacaoAtual()})");
		}

		return sb.toString();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("numeroCPF", SearchCriteria.igual, R1);
		addSearchField("dataPosse", SearchCriteria.igual, R2);
		addSearchField("nome", SearchCriteria.contendo, R3);
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public List<PessoaMagistrado> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		if(StringUtil.CPF_EMPTYMASK.equals(numeroCPF)){
			numeroCPF = "";
		}
		this.numeroCPF = numeroCPF;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}
	
	public Boolean getMagistradoAtivo() {
		return magistradoAtivo;
	}
	
	public void setMagistradoAtivo(Boolean magistradoAtivo) {
		this.magistradoAtivo = magistradoAtivo;
	}

	@Override
	public void newInstance() {
		setLocalizacao(null);
		setNumeroCPF(null);
		super.newInstance();
	}

}