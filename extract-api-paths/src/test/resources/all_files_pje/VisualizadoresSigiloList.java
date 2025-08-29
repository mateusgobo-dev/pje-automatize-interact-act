package br.com.infox.pje.list;

import java.util.Collections;
import java.util.Date;
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
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.VisualizadoresSigilo;
import br.jus.pje.nucleo.util.DateUtil;

@Name(VisualizadoresSigiloList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class VisualizadoresSigiloList extends EntityList<VisualizadoresSigilo>{
	
	public static final String NAME = "visualizadoresSigiloList";
	
	private Boolean servidorAtivo = Boolean.TRUE;
	private String nome;
	private String numeroCPF;
	private OrgaoJulgador orgaoJulgador;

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_ORDER = "idVisualizadoresSigilo";
	
	private static final String R1 = "o.funcionario in (select pf.idUsuario from PessoaFisica pf "
			+ "inner join pf.pessoaDocumentoIdentificacaoList pdi "
			+ "where pdi.tipoDocumento.codTipo = 'CPF' and pdi.numeroDocumento = #{visualizadoresSigiloList.numeroCPF})";

	private static final String R2 = "o.funcionario in (SELECT l.idUsuario FROM UsuarioLogin l "
			+ "WHERE concat('%',lower(to_ascii(l.nome)),'%') like concat('%',lower(to_ascii(#{visualizadoresSigiloList.nome.replace(' ', '%')})),'%'))";
	
  	
  	private static final String R3 = "o.funcionario IN (SELECT l.usuario FROM UsuarioLocalizacao l "
			+ "WHERE l.usuarioLocalizacaoMagistradoServidor.orgaoJulgador = #{visualizadoresSigiloList.orgaoJulgador})";

	@Override
	protected void addSearchFields() {
		addSearchField("numeroCPF", SearchCriteria.igual, R1);
		addSearchField("nome", SearchCriteria.contendo, R2);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R3);	
	}
	
	@Override
	public void newInstance() {
		setServidorAtivo(null);
		setOrgaoJulgador(null);
		setNome(null);
		setNumeroCPF(null);
		super.newInstance();
	}
	
	public static VisualizadoresSigiloList instance() {
		return ComponentUtil.getComponent(NAME);
	}

	@Override
	protected String getDefaultEjbql() {
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

		StringBuilder sb = new StringBuilder("select o from VisualizadoresSigilo o ");
		sb.append(" WHERE 1=1 ");
		
		if(Boolean.TRUE.equals(getServidorAtivo())) {
			sb.append(" and o.dtInicio <= current_date " );
			sb.append(" and (o.dtFinal is null or o.dtFinal >= current_timestamp) ");
		}
		
		if(Boolean.FALSE.equals(getServidorAtivo())) {
			sb.append(" and (o.dtInicio > current_date " );
			sb.append(" or (o.dtFinal is not null and o.dtFinal < current_timestamp)) ");
		}
		
		if(orgaoJulgadorAtual != null) {
			sb.append(" AND o.orgaoJulgador.idOrgaoJulgador = "+ orgaoJulgadorAtual.getIdOrgaoJulgador() + " ");
		}
		
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return Collections.emptyMap();
	}
	
	@Override
	public List<VisualizadoresSigilo> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	/**
	 * Verifica se o visualizador em questão está ativo ou não, através das datas de início e fim da visualização
	 * 
	 * @param visualizador
	 * @return True se for ativo e False se não for ativo
	 */
	public Boolean isAtivo(VisualizadoresSigilo visualizador) {
		Date inicioDeHoje = DateUtil.getBeginningOfToday();
		Date fimDeHoje = DateUtil.getEndOfToday();
		Boolean inicio = visualizador.getDtInicio().before(inicioDeHoje);
		Boolean fim = true;
		if(visualizador.getDtFinal() != null) {
			fim = visualizador.getDtFinal().after(fimDeHoje);
		}
		return  (inicio && fim);
	}

	public Boolean getServidorAtivo() {
		return servidorAtivo;
	}

	public void setServidorAtivo(Boolean servidorAtivo) {
		this.servidorAtivo = servidorAtivo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}	

}
