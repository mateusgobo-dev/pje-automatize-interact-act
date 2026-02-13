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
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;

@Name(SubstituicaoMagistradoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SubstituicaoMagistradoList extends EntityList<SubstituicaoMagistrado> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "substituicaoMagistradoList";
	private static final String DEFAULT_ORDER = "o.dataInicio desc";
	
	private static final String R1 = "o.dataInicio = #{substituicaoMagistradoList.dataInicial} ";
	private static final String R2 = "o.dataFim = #{substituicaoMagistradoList.dataFinal} ";
	private static final String R3 = "#{substituicaoMagistradoList.vigenteEm} between o.dataInicio and o.dataFim ";
	
	private static final String R4 = "o.orgaoJulgador = #{substituicaoMagistradoList.orgaoJulgador} "; 
	private static final String R5 = "o.magistradoSubstituto = #{substituicaoMagistradoList.magistradoSubstituto} ";
	
	private Date dataInicial;
	private Date dataFinal;
	private Date vigenteEm;
	private OrgaoJulgador orgaoJulgador;
	private PessoaMagistrado magistradoSubstituto;
	
	@Override
	public void newInstance() {
		super.newInstance();
		this.dataInicial = null;
		this.dataFinal = null;
		this.vigenteEm = null;
		this.orgaoJulgador = null;
		this.magistradoSubstituto = null;
	}
	
	/**
	 * Cria uma Query que retorna as substituições de acordo com o usuário logado, 
	 * verificando se este é um magistrado presente na substituição ou um de seus acessores.
	 * @return query
	 */
	private String getDefautSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SubstituicaoMagistrado o "); 
		if(!Authenticator.hasRole(Papeis.MANIPULA_SUBSTITUICAO_MAGISTRADO)){
			sb.append("WHERE ");
			sb.append("( ");
			sb.append("  ( ");
			sb.append("    o.magistradoAfastado.idUsuario = #{authenticator.getUsuarioLogado().idUsuario} ");  
			sb.append("    OR  EXISTS( ");
			sb.append("		   SELECT 1 FROM VinculacaoUsuario vu WHERE vu.usuarioVinculado = #{authenticator.getUsuarioLogado()} and vu.usuario.idUsuario = o.magistradoAfastado.idUsuario ");
			sb.append("	   ) ");
			sb.append("  ) ");
			sb.append("OR ");
			sb.append("  ( ");
			sb.append("    o.magistradoSubstituto.idUsuario = #{authenticator.getUsuarioLogado().idUsuario} ");  
			sb.append("    OR  EXISTS( ");
			sb.append("		   SELECT 1 FROM VinculacaoUsuario vu WHERE vu.usuarioVinculado = #{authenticator.getUsuarioLogado()} and vu.usuario.idUsuario = o.magistradoSubstituto.idUsuario ");
			sb.append("	   ) ");
			sb.append("  ) ");
			sb.append(") ");
		}
		return sb.toString();
	}
	
	@Override
	protected void addSearchFields() {
		addSearchField("dataInicial", SearchCriteria.igual, R1);
		addSearchField("dataFinal", SearchCriteria.igual, R2);
		addSearchField("vigenteEm", SearchCriteria.igual, R3);
		addSearchField("magistradoAfastado", SearchCriteria.igual, R4);
		addSearchField("magistradoSubstituto", SearchCriteria.igual, R5);
	}

	@Override
	protected String getDefaultEjbql() {
		return getDefautSql();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	
	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("magistradoAfastado", "o.orgaoJulgador");
		map.put("magistradoSubstituto", "o.magistradoSubstituto");
		return map;
	}
	

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Date getVigenteEm() {
		return vigenteEm;
	}

	public void setVigenteEm(Date vigenteEm) {
		this.vigenteEm = vigenteEm;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public PessoaMagistrado getMagistradoSubstituto() {
		return magistradoSubstituto;
	}

	public void setMagistradoSubstituto(PessoaMagistrado magistradoSubstituto) {
		this.magistradoSubstituto = magistradoSubstituto;
	}
	
	

}
