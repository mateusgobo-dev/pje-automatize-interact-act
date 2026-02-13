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
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaServidorList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PessoaServidorList extends EntityList<PessoaServidor> {

	public static final String NAME = "pessoaServidorList";
	private static final long serialVersionUID = 1L;
	private String numeroCPF;
	private Papel papel;
	private Localizacao localizacao;
	private Boolean servidorAtivo = Boolean.TRUE;
	private OrgaoJulgador orgaoJulgador; 
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	private static final String DEFAULT_ORDER = "nome";

	private static final String R1 = "o.idUsuario in (select pf.idUsuario from PessoaFisica pf "
			+ "inner join pf.pessoaDocumentoIdentificacaoList pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento = #{pessoaServidorList.numeroCPF})";

	private static final String R2 = "lower(to_ascii(numeroMatricula)) like concat('%',lower(to_ascii(#{pessoaServidorList.entity.numeroMatricula})),'%')";

	private static final String R3 = "o.idUsuario in (select lo.usuario.idUsuario from UsuarioLocalizacao lo "
			+ "where lo.papel.idPapel = #{pessoaServidorList.papel.idPapel})";

	private static final String R4 = "o in (select l.usuario from UsuarioLocalizacao l "
			+ "where l.localizacaoFisica = #{pessoaServidorList.localizacao})";

	private static final String R5 = "concat('%',lower(to_ascii(nome)),'%') like concat('%',lower(to_ascii(#{pessoaServidorList.entity.nome.replace(' ', '%')})),'%')";
	
 	private static final String R6 = "o.idUsuario IN (SELECT l.usuario FROM UsuarioLocalizacao l "
  			+ "WHERE l.usuarioLocalizacaoMagistradoServidor.orgaoJulgadorColegiado = #{pessoaServidorList.orgaoJulgadorColegiado})";
  	
  	private static final String R7 = "o.idUsuario IN (SELECT l.usuario FROM UsuarioLocalizacao l "
			+ "WHERE l.usuarioLocalizacaoMagistradoServidor.orgaoJulgador = #{pessoaServidorList.orgaoJulgador})";

	@Override
	protected String getDefaultEjbql() {
		String idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhasAtuais();
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual = Authenticator.getOrgaoJulgadorColegiadoAtual();
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaServidor o ");
		sb.append(" WHERE o.pessoa.unificada = FALSE ");
		
		if(getServidorAtivo() != null) {
			if(getServidorAtivo()) {
				sb.append(" AND (bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.SER + ") = " + PessoaFisica.SER + ") ");
			}else {
				sb.append(" AND (bitwise_and(o.pessoa.especializacoes, " + PessoaFisica.SER + ") != " + PessoaFisica.SER + ") ");
			}
		}

		sb.append(" AND o.idUsuario IN (")
			.append(" SELECT ul.usuario.idUsuario FROM UsuarioLocalizacao AS ul ")
			.append(" JOIN ul.localizacaoFisica loc ")
			.append(" WHERE loc.idLocalizacao IN (")
			.append(idsLocalizacoesFisicas)
			.append(")");

		sb.append(" AND ul.papel.identificador != '"+ Papeis.MAGISTRADO +"'");
		if(orgaoJulgadorAtual != null || orgaoJulgadorColegiadoAtual != null) {
			sb.append(" AND ul.idUsuarioLocalizacao IN ")
				.append(" (SELECT ulms.idUsuarioLocalizacaoMagistradoServidor FROM UsuarioLocalizacaoMagistradoServidor AS ulms ")
				.append("	WHERE 1=1 ");
			
			if(orgaoJulgadorColegiadoAtual != null) {
				sb.append(" AND ulms.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "+ orgaoJulgadorColegiadoAtual.getIdOrgaoJulgadorColegiado());
			}
			if(orgaoJulgadorAtual != null) {
				sb.append(" AND ulms.orgaoJulgador.idOrgaoJulgador = "+ orgaoJulgadorAtual.getIdOrgaoJulgador());
			}
			sb.append(" )");
		}
		sb.append(" )");

		return sb.toString();
	}

	@Override
	protected void addSearchFields() {
		addSearchField("nome", SearchCriteria.contendo, R5);
		addSearchField("numeroCPF", SearchCriteria.igual, R1);
		addSearchField("numeroMatricula", SearchCriteria.contendo, R2);
		addSearchField("dataPosse", SearchCriteria.igual, R3);
		addSearchField("checkado", SearchCriteria.igual, R4);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R6);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R7);
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	public List<PessoaServidor> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
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

	public Papel getPapel() {
		return papel;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}
	
	public Boolean getServidorAtivo() {
		return servidorAtivo;
	}
	
	public void setServidorAtivo(Boolean servidorAtivo) {
		this.servidorAtivo = servidorAtivo;
	}
	
	@Override
	public void newInstance() {
		setLocalizacao(null);
		setPapel(null);
		setNumeroCPF(null);
		setServidorAtivo(Boolean.TRUE);
		setOrgaoJulgador(null);
		setOrgaoJulgadorColegiado(null);
		super.newInstance();
	}
	
	public OrgaoJulgador getOrgaoJulgador() {
  		return orgaoJulgador;
  	}
  
  	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
  		this.orgaoJulgador = orgaoJulgador;
  	}
  
  	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
  		return orgaoJulgadorColegiado;
  	}
  
  	public void setOrgaoJulgadorColegiado(
  			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
  		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}
	
}