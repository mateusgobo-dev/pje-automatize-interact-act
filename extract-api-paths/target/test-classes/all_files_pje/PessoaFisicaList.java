package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name(PessoaFisicaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaFisicaList extends EntityList<PessoaFisica> {

	public static final String NAME = "pessoaFisicaList";

	private String nome;
	private String cpf;

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "nome";

	private static String R1 = "o.nome like concat('%', #{pessoaFisicaList.nome}, '%')";
	private static final String R2 = " exists (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ " where pdi.tipoDocumento.codTipo = 'CPF' and " + " pdi.pessoa.idUsuario = o.idUsuario and "
			+ " pdi.numeroDocumento like '%' || #{pessoaFisicaList.cpf} || '%'))";

	@Override
	protected void addSearchFields() {
		addSearchField("nome", SearchCriteria.igual, R1);
		addSearchField("cpf", SearchCriteria.igual, R2);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaFisica o ");
		sb.append(" where not exists ");
		sb.append("(select r from RpvPessoaParte r where r.pessoa.idUsuario = o.idUsuario");
		sb.append(" and r.rpv.idRpv = #{rpvAction.rpv.idRpv} )");

		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("nome", "o.nome");
		return map;
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
}