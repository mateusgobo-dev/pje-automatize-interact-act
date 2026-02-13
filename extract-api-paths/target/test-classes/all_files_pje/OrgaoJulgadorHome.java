/* $Id: OrgaoJulgadorHome.java 26403 2011-06-30 02:01:22Z hiran $ */

package br.com.infox.cliente.home;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.JurisdicaoSuggestBean;
import br.com.infox.cliente.component.tree.OrgaoJulgadorLocalizacaoTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.CorporativoClient;
import br.jus.cnj.pje.webservice.client.corporativo.OrgaoGenericoDTO;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OjClasseTipoAudiencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.StringUtil;

@Name("orgaoJulgadorHome")
@BypassInterceptors
public class OrgaoJulgadorHome extends AbstractOrgaoJulgadorHome<OrgaoJulgador> {
	
	private Time de;
	private Time ate;
	
	private TipoAudiencia tipoAudiencia;
	private List<ClasseJudicial> classeJudicialList;
	
	private String codigoClasseAbaTipoAudiencia;
	private String nomeClasseAbaTipoAudiencia;

	private OrgaoJulgador varaAtendida;
	
	private Boolean nomeOrgaoAlterado = false;
	private String nomeOrgaoCorporativo;
	
	public String getCodigoClasseAbaTipoAudiencia() {
		return codigoClasseAbaTipoAudiencia;
	}

	public void setCodigoClasseAbaTipoAudiencia(String codigoClasseAbaTipoAudiencia) {
		this.codigoClasseAbaTipoAudiencia = codigoClasseAbaTipoAudiencia;
	}

	public String getNomeClasseAbaTipoAudiencia() {
		return nomeClasseAbaTipoAudiencia;
	}

	public void setNomeClasseAbaTipoAudiencia(String nomeClasseAbaTipoAudiencia) {
		this.nomeClasseAbaTipoAudiencia = nomeClasseAbaTipoAudiencia;
	}

	public List<ClasseJudicial> getClasseJudicialList() {
		pesquisarTipoAudiencia();
		return classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public TipoAudiencia getTipoAudiencia() {
		return tipoAudiencia;
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		this.tipoAudiencia = tipoAudiencia;
	}

	public void setDe(Time de) {
		this.de = de;
	}

	public Time getDe() {
		return this.de;
	}

	public void setAte(Time ate) {
		this.ate = ate;
	}

	public Time getAte() {
		return this.ate;
	}

	public OrgaoJulgador getVaraAtendida() {
		return varaAtendida;
	}

	public void setVaraAtendida(OrgaoJulgador varaAtendida) {
		this.varaAtendida = varaAtendida;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void newInstance() {
		limparTree();
		refreshGrid("orgaoJulgadorGrid");
		super.newInstance();
	}

	private JurisdicaoSuggestBean getJurisdicaoSuggestBean() {
		return getComponent("jurisdicaoSuggest");
	}

	@Override
	public String remove(OrgaoJulgador obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		super.update();
		newInstance();
		refreshGrid("orgaoJulgadorGrid");
		return "updated";
	}

	private void limparTree() {
		OrgaoJulgadorLocalizacaoTreeHandler ret1 = getComponent("orgaoJulgadorLocalizacaoFormTree");
		ret1.clearTree();
	}

	public void setTab() {
		super.setTab("TempoAudiencia");
	}

	@SuppressWarnings("deprecation")
	@Override
	public String persist() {
		String ret = null;
		try {
			if ((getJurisdicaoSuggestBean().getInstance() == null)) {
				FacesMessages fm = FacesMessages.instance();
				fm.add(FacesMessage.SEVERITY_ERROR, "Informe a Jurisdição!.");
			} else {
				getInstance().setJurisdicao(getJurisdicaoSuggestBean().getInstance());
			}
			/**
			 *  PJEII-726 Validando a sigla informada pelo usuario
			 */
			if (!validaSigla(getInstance())){
					return ret;
			}

			refreshGrid("orgaoJulgadorGrid");
			Boolean achou = this.isLocalizacaoPossuiOJouOJCDistinto(getInstance(), getInstance().getLocalizacao());

			if (!achou) {
				ret = super.persist();
				limparTree();
				return ret;
			} else {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						FacesUtil.getMessage("entity_messages", "localizacao.erro.relacionamentoOJDistinto"));
				return "false";
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public String update() {
		String ret = null;
		try {
			refreshGrid("orgaoJulgadorGrid");
			Boolean achou = this.isLocalizacaoPossuiOJouOJCDistinto(getInstance(), getInstance().getLocalizacao());

			if (!achou) {
				if(validaCodigoOrigem(getInstance())) {
					FacesMessages.instance().add(StatusMessage.Severity.ERROR,
							"Erro: Código de origem para esta Jurisdição já existe em algum outro Orgão Julgador.");
					
					return "false";
				}
				
				if (!getInstance().getJurisdicao().equals(getJurisdicaoSuggestBean().getInstance())) {
					getInstance().setJurisdicao(getJurisdicaoSuggestBean().getInstance());
				}

				/**
				 *  PJEII-726 Validando a sigla informada pelo usuario
				 */
				if (!validaSigla(getInstance())){
					return "false";
				}
				ret = super.update();
				limparTree();
				return ret;

			} else {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						FacesUtil.getMessage("entity_messages", "localizacao.erro.relacionamentoOJDistinto"));
				return "false";
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	/**
	 * Verifica se HÁ um OJC ou um OJ diferente do atual relacionado à localização indicada
	 * 
	 * @param oj
	 * @param localizacao
	 * @return false se não houver nada diferente, true se houver pelo menos um OJ ou um OJC diferente vinculado à localização
	 */
	private boolean isLocalizacaoPossuiOJouOJCDistinto(OrgaoJulgador oj, Localizacao localizacao) {
  		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
  		OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager = ComponentUtil.getComponent("orgaoJulgadorColegiadoManager");
  		
  		if(CollectionUtilsPje.isEmpty(orgaoJulgadorColegiadoManager.getColegiadosByLocalizacaoExata(localizacao))) {
  			OrgaoJulgador ojPesquisa = orgaoJulgadorManager.getOrgaoJulgadorByLocalizacaoExata(localizacao);
  			if(ojPesquisa == null || (ojPesquisa.getIdOrgaoJulgador() == oj.getIdOrgaoJulgador())) {
  				return false;
  			}
  		}
  		
  		return true;
	}
	
	/**
	 *  PJEII-726 - Método que realiza a validação da sigla informada pelo usuário
	 * @param oj
	 * @return true caso a sigla informada seja válida
	 * 			false caso não seja
	 */
	private boolean validaSigla(OrgaoJulgador oj){
		boolean retorno=true;
		// Caso seja obrigatória a informação da sigla
		if (isSiglaObrigatoria()){
			if (null ==oj.getSigla()|| oj.getSigla().isEmpty()){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"Ero: Informar a sigla é obrigatório.");
				return false;						
			} 
		} 
		
		if (possuiSigla(oj.getSigla(), oj)) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Erro: Sigla já existe em algum outro Orgão Julgador.");
			return false;	
		}
		return retorno;
	}

	/**
	 *  PJEII-503 - Retorna true caso seja obrigatório informar a sigla durante o cadastro/modificação de um Orgão Julgador.
	 * @return
	 */
	private boolean isSiglaObrigatoria() {
		return "JT".equals(ParametroUtil.instance().getTipoJustica());
	}

	private boolean validaCodigoOrigem(OrgaoJulgador oj) {
		OrgaoJulgadorManager ojm = ComponentUtil.getComponent("orgaoJulgadorManager");
		
		return ojm.codigoOrigemJaUtilizadoParaJurisdicao(oj);
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		OrgaoJulgador oj = getInstance();
		boolean beforePersistOrUpdate = super.beforePersistOrUpdate();
		
		if(oj.getPostoAvancado() != null && oj.getPostoAvancado() == false && !oj.getVarasAtendidas().isEmpty()) {
			oj.getVarasAtendidas().clear();
		}
		
		if(validaCodigoOrigem(oj)) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Erro: Código de origem para esta Jurisdição já existe em algum outro Orgão Julgador.");
			
			beforePersistOrUpdate = false;
		}
		
		return beforePersistOrUpdate;
	}

	@Override
	public void setId(Object id) {
		/**
		 *  PJEII-516 Para funcionar atualizar/novo
		 */
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			getJurisdicaoSuggestBean().setInstance(getInstance().getJurisdicao());
		}
		if (id == null) {
			getJurisdicaoSuggestBean().setInstance(getInstance().getJurisdicao());
			getJurisdicaoSuggestBean().setInstance(null);
		}
	}

	public boolean possuiSigla(String ds_sigla, OrgaoJulgador oj) {
		/**
		 *  PJEII-503 - Assinatura do metodo alterada para receber o Órgão Julgador
		 *  Query modificada para excluir da pesquisa o id do Órgão Julgador que atualmente possui a sigla
		 */
		Query query = getEntityManager().createQuery("select count(o) from OrgaoJulgador o where o.sigla = :ds_sigla and o.idOrgaoJulgador != :idOj");
		query.setParameter("ds_sigla", ds_sigla);
		query.setParameter("idOj", oj.getIdOrgaoJulgador());
		try {
			Long retorno = (Long) query.getSingleResult();			
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}

	@SuppressWarnings("unchecked")
	public List<Localizacao> getLocalizacoesDisponiveis() {
		StringBuilder sb = new StringBuilder();
		sb.append("select l from Localizacao l where ");
		sb.append("(l.estruturaFilho is not null and l.ativo = true ");
		sb.append("and not exists (select o from OrgaoJulgador o where o.localizacao = l))");
		if (isManaged()) {
			sb.append(" or l = :instance");
		}
		Query query = getEntityManager().createQuery(sb.toString());
		if (isManaged()) {
			query.setParameter("instance", getInstance().getLocalizacao());
		}
		return query.getResultList();
	}

	public static OrgaoJulgadorHome instance() {
		return ComponentUtil.getComponent("orgaoJulgadorHome");
	}

	public OrgaoJulgador getOrgaoJulgador(Localizacao localizacao) {
		OrgaoJulgadorCompetenciaHome.instance().newInstance();
		try {
			OrgaoJulgadorCompetencia.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		Query query = getEntityManager()
				.createQuery("select o from OrgaoJulgador o where o.localizacao = :localizacao");
		query.setParameter("localizacao", localizacao);
		return EntityUtil.getSingleResult(query);
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorList(List<Localizacao> localizacaoList) {
		OrgaoJulgadorCompetenciaHome.instance().newInstance();
		try {
			OrgaoJulgadorCompetencia.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		Query query = getEntityManager().createQuery(
				"select o from OrgaoJulgador o where o.localizacao in (:localizacaoList)");
		query.setParameter("localizacaoList", Util.isEmpty(localizacaoList)?null:localizacaoList);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorListByOjc(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o where o.ativo = true ");
		if(orgaoJulgadorColegiado!= null){
			sb.append(" and o in (select ojc.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador ojc where ");
		    sb.append(" ojc.orgaoJulgadorColegiado = :orgaoJulgadorColegiado) ");
		}
		sb.append(" ORDER BY CASE WHEN o.orgaoJulgador >= 'A' THEN upper(to_ascii(o.orgaoJulgador)) ELSE fn_to_number(o.orgaoJulgador) END, upper(to_ascii(o.orgaoJulgador)) ");
		Query query = getEntityManager().createQuery(sb.toString());
		if(orgaoJulgadorColegiado!= null){
			query.setParameter("orgaoJulgadorColegiado", orgaoJulgadorColegiado);
		}
		return query.getResultList();
	}	

	public void clearSearchTempoAudiencia() {
		Contexts.removeFromAllContexts("orgaoJulgadorSuggest");
	}

	@SuppressWarnings("unchecked")
	public List<Usuario> getUsuarios(OrgaoJulgador orgaoJulgador) {

		List<Localizacao> localizacoes = getLocalizacoes(orgaoJulgador);

		String hql = "select ul.usuario from UsuarioLocalizacao ul where ul.localizacaoFisica = :localizacaoOrgaoJulgador ";

		Query query = getEntityManager().createQuery(hql);

		if (!localizacoes.isEmpty()) {
			query.setParameter("localizacoes", localizacoes);
		}

		query.setParameter("localizacaoOrgaoJulgador", orgaoJulgador.getLocalizacao());

		return query.getResultList();
	}

	private List<Localizacao> getLocalizacoesFilhas(Localizacao loc) {
		List<Localizacao> localizacoes = new ArrayList<Localizacao>(0);
		for (Localizacao local : loc.getLocalizacaoList()) {
			if (local.getLocalizacaoList().size() > 0) {
				localizacoes.addAll(getLocalizacoesFilhas(local));
			}
			localizacoes.add(local);
		}
		return localizacoes;
	}

	public List<Localizacao> getLocalizacoes(OrgaoJulgador orgaoJulgador) {
		return getLocalizacoesFilhas(orgaoJulgador.getLocalizacao().getEstruturaFilho());
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getListaOJSegundoGrau() {
		if (Authenticator.isPermissaoCadastroTodosPapeis()) {
			StringBuilder sql = new StringBuilder("SELECT o FROM OrgaoJulgador o ");
			sql.append(" ORDER BY CASE WHEN o.orgaoJulgador >= 'A' THEN upper(to_ascii(o.orgaoJulgador)) ELSE fn_to_number(o.orgaoJulgador) END, upper(to_ascii(o.orgaoJulgador)) ");
			Query q = EntityUtil.createQuery(sql.toString());
			return q.getResultList();
		} else {
			UsuarioLocalizacaoMagistradoServidorHome.instance().getInstance()
					.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
			List<OrgaoJulgador> listaOJ = new ArrayList<OrgaoJulgador>();
			if (Authenticator.getOrgaoJulgadorAtual() != null) {
				listaOJ.add(Authenticator.getOrgaoJulgadorAtual());
			}
			return listaOJ;
		}
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getListaOJ() {		
		List<OrgaoJulgador> listaOJ = null;
		StringBuilder sql = new StringBuilder("select o.orgaoJulgador from OrgaoJulgadorColegiadoOrgaoJulgador o ");
		sql.append(" where (o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :idOrgaoJulgadorColegiado or :idOrgaoJulgadorColegiado = 0)");
		sql.append(" and o.dataInicial <= current_date and (o.dataFinal >= current_date or o.dataFinal is null)"); 
		sql.append(" ORDER BY CASE WHEN o.orgaoJulgador.orgaoJulgador >= 'A' THEN upper(to_ascii(o.orgaoJulgador.orgaoJulgador)) ELSE fn_to_number(o.orgaoJulgador.orgaoJulgador) END, upper(to_ascii(o.orgaoJulgador.orgaoJulgador)) ");
		Query q = EntityUtil.createQuery(sql.toString());
		
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null && Authenticator.getOrgaoJulgadorAtual() == null) {
			q.setParameter("idOrgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual()
					.getIdOrgaoJulgadorColegiado());
			listaOJ = q.getResultList();
		} else {
			if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null && Authenticator.getOrgaoJulgadorAtual() != null) {
				listaOJ = new ArrayList<OrgaoJulgador>();
				listaOJ.add(Authenticator.getOrgaoJulgadorAtual());
				UsuarioLocalizacaoMagistradoServidorHome.instance().getInstance()
						.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
			} else {
				if (Authenticator.getOrgaoJulgadorColegiadoAtual() == null
						&& Authenticator.getOrgaoJulgadorAtual() == null
						&& (Authenticator.isPermissaoCadastroTodosPapeis())) {
					OrgaoJulgadorColegiado orgaoJulgadorColegiado = UsuarioLocalizacaoMagistradoServidorHome.instance()
							.getInstance().getOrgaoJulgadorColegiado();
					if (orgaoJulgadorColegiado != null) {
						q.setParameter("idOrgaoJulgadorColegiado", orgaoJulgadorColegiado.getIdOrgaoJulgadorColegiado());
					} else {
						q.setParameter("idOrgaoJulgadorColegiado", 0);
					}
					
					listaOJ = q.getResultList();
				}
			}
		}
		
		if (listaOJ != null) {
			return new ArrayList<OrgaoJulgador>(new LinkedHashSet<OrgaoJulgador>(listaOJ));
		} else {
			return null;
		}
	}

	public void pesoChangedInGrid() {
		EntityUtil.flush();
	}
	
	@SuppressWarnings("unchecked")
	public List<ClasseJudicial> getClasseJudicialListSelecionadas(){
		 List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
		 if(tipoAudiencia != null && getInstance() != null){
			 StringBuilder sb = new StringBuilder();
			 sb.append("select o.classeJudicial ");
			 sb.append("from OjClasseTipoAudiencia o ");
			 sb.append("where o.tipoAudiencia=:tipoAudiencia ");
			 sb.append("and o.orgaoJulgador=:orgaoJulgador ");
			 sb.append("and (o.dtFim = null or o.dtFim>=now()) ");

			 Query q = getEntityManager().createQuery(sb.toString());
			 q.setParameter("tipoAudiencia", tipoAudiencia);
			 q.setParameter("orgaoJulgador", getInstance());
			 classeJudicialList.addAll(q.getResultList());
		 }
		 return classeJudicialList;
	}
	
	@SuppressWarnings("unchecked")
	public void pesquisarTipoAudiencia(){
		classeJudicialList = new ArrayList<ClasseJudicial>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct c ");
		sb.append("from CompetenciaClasseAssunto o ");
		sb.append("join o.classeAplicacao ca ");
		sb.append("join ca.classeJudicial c ");
		sb.append("where exists (select o1 from OrgaoJulgadorCompetencia o1 ");
		sb.append("				 where o1.competencia=o.competencia and ");
		sb.append("				 o1.orgaoJulgador=:orgaoJulgador and ");
		sb.append("				 (o1.dataFim = null or o1.dataFim >= now())) and ");
		sb.append("not exists (select o2 from OjClasseTipoAudiencia o2 ");
		sb.append("			   where o2.classeJudicial=c and ");
		sb.append("			   o2.orgaoJulgador=:orgaoJulgador and ");
		sb.append("			   (o2.dtFim = null or o2.dtFim >= now())) and ");
		sb.append("(o.dataFim = null or o.dataFim >= now()) ");
		if((codigoClasseAbaTipoAudiencia != null && !codigoClasseAbaTipoAudiencia.equals("")) || (nomeClasseAbaTipoAudiencia != null && !nomeClasseAbaTipoAudiencia.equals(""))){
			sb.append("and ( ");
		}
		if(codigoClasseAbaTipoAudiencia != null && !codigoClasseAbaTipoAudiencia.equals("")){
			sb.append("lower(to_ascii(c.codClasseJudicial)) like lower(to_ascii(:codClasseJudicial)) ");
		}
		if(codigoClasseAbaTipoAudiencia != null && !codigoClasseAbaTipoAudiencia.equals("") && nomeClasseAbaTipoAudiencia != null && !nomeClasseAbaTipoAudiencia.equals("")){
			sb.append("or ");
		}
		if(nomeClasseAbaTipoAudiencia != null && !nomeClasseAbaTipoAudiencia.equals("")){
			sb.append("lower(to_ascii(c.classeJudicial)) like lower(to_ascii(:classeJudicial)) ");
		}
		if((codigoClasseAbaTipoAudiencia != null && !codigoClasseAbaTipoAudiencia.equals("")) || (nomeClasseAbaTipoAudiencia != null && !nomeClasseAbaTipoAudiencia.equals(""))){
			sb.append(") ");
		}
		sb.append("order by c.codClasseJudicial ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", getInstance());
		if(codigoClasseAbaTipoAudiencia != null && !codigoClasseAbaTipoAudiencia.equals("")){
			q.setParameter("codClasseJudicial", "%"+codigoClasseAbaTipoAudiencia+"%");
		}
		if(nomeClasseAbaTipoAudiencia != null && !nomeClasseAbaTipoAudiencia.equals("")){
			q.setParameter("classeJudicial", "%"+nomeClasseAbaTipoAudiencia+"%");
		}
		classeJudicialList.addAll(q.getResultList());
	}
	
	public void acaoAbaTipoAudiencia(){
		limparTipoAudiencia();
		tipoAudiencia = null;
	}
	
	public void limparTipoAudiencia(){
		classeJudicialList = null;
		codigoClasseAbaTipoAudiencia = "";
		nomeClasseAbaTipoAudiencia = "";
	}
	
	@SuppressWarnings("unchecked")
	public void adicionarClasse(ClasseJudicial classeJudicial){
		if(tipoAudiencia==null){
			FacesMessages.instance().add(Severity.ERROR, "Selecione um Tipo de Audiência");
			return;
		}
		OjClasseTipoAudiencia ojClasseTipoAudiencia = new OjClasseTipoAudiencia();
		ojClasseTipoAudiencia.setClasseJudicial(classeJudicial);
		ojClasseTipoAudiencia.setOrgaoJulgador(getInstance());
		ojClasseTipoAudiencia.setTipoAudiencia(tipoAudiencia);
		ojClasseTipoAudiencia.setDtInicio(new Date());
	
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCompetencia o ");
		sb.append("where o.orgaoJulgador=:orgaoJulgador and ");
		sb.append("o.competencia in (select o1 from CompetenciaClasseAssunto o1 where o1.classeAplicacao.classeJudicial=:classeJudicial) ");
		sb.append("and (o.dataFim = null or o.dataFim >= now()) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", getInstance());
		q.setParameter("classeJudicial", classeJudicial);
		List<OrgaoJulgadorCompetencia> list = q.getResultList();
		Date maiorDataOrgaoJulgadorCompetencia = null;
		for (OrgaoJulgadorCompetencia orgaoJulgadorCompetencia : list) {
			if(maiorDataOrgaoJulgadorCompetencia == null || (orgaoJulgadorCompetencia.getDataFim() != null && orgaoJulgadorCompetencia.getDataFim().after(maiorDataOrgaoJulgadorCompetencia))){
				maiorDataOrgaoJulgadorCompetencia = orgaoJulgadorCompetencia.getDataFim();
			}
		}
		
		sb = new StringBuilder();
		sb.append("select o from CompetenciaClasseAssunto o ");
		sb.append("where o.classeAplicacao.classeJudicial=:classeJudicial and ");
		sb.append("o.competencia in (select o1 from OrgaoJulgadorCompetencia o1 where o1.orgaoJulgador=:orgaoJulgador) ");
		sb.append("and (o.dataFim = null or o.dataFim >= now()) ");
		q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", getInstance());
		q.setParameter("classeJudicial", classeJudicial);
		List<CompetenciaClasseAssunto> l = q.getResultList();
		Date maiorDataCompetenciaClasseAssunto = null;
		for (CompetenciaClasseAssunto competenciaClasseAssunto : l) {
			if(maiorDataCompetenciaClasseAssunto == null || (competenciaClasseAssunto.getDataFim() != null && competenciaClasseAssunto.getDataFim().after(maiorDataCompetenciaClasseAssunto))){
				maiorDataCompetenciaClasseAssunto = competenciaClasseAssunto.getDataFim();
			}
		}
		
		Date dataFim = null;
		if(maiorDataOrgaoJulgadorCompetencia != null && maiorDataCompetenciaClasseAssunto == null || maiorDataOrgaoJulgadorCompetencia == null && maiorDataCompetenciaClasseAssunto != null){
			dataFim = maiorDataOrgaoJulgadorCompetencia != null ? maiorDataOrgaoJulgadorCompetencia : maiorDataCompetenciaClasseAssunto; 
		}else if(maiorDataOrgaoJulgadorCompetencia != null && maiorDataCompetenciaClasseAssunto != null){
			dataFim = maiorDataOrgaoJulgadorCompetencia.before(maiorDataCompetenciaClasseAssunto) ? maiorDataOrgaoJulgadorCompetencia : maiorDataCompetenciaClasseAssunto;
		}else{
			dataFim = null;
		}
		
		ojClasseTipoAudiencia.setDtFim(dataFim);
		getEntityManager().persist(ojClasseTipoAudiencia);
		getEntityManager().flush();
		classeJudicialList.remove(classeJudicial);
		FacesMessages.instance().add(Severity.INFO, "Classe adicionada com sucesso.");
	}
	
	public void removerClasse(ClasseJudicial classeJudicial){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OjClasseTipoAudiencia o ");
		sb.append("where o.classeJudicial=:classeJudicial and ");
		sb.append("o.orgaoJulgador=:orgaoJulgador and ");
		sb.append("o.tipoAudiencia=:tipoAudiencia and ");
		sb.append("(o.dtFim = null or o.dtFim>=now()) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("classeJudicial", classeJudicial);
		q.setParameter("orgaoJulgador", getInstance());
		q.setParameter("tipoAudiencia", tipoAudiencia);
		
		OjClasseTipoAudiencia ojClasseTipoAudiencia = EntityUtil.getSingleResult(q);
		if(ojClasseTipoAudiencia != null){
			ojClasseTipoAudiencia.setDtFim(new Date());
			getEntityManager().remove(ojClasseTipoAudiencia);
			getEntityManager().flush();
			FacesMessages.instance().add(Severity.INFO, "Classe removida com sucesso.");
		}
	}
	
	/**
	 * Método criado devido à ISSUE PJEII-4023 - Alteração realizada em 22/11/2012 por Rafael Barros
	 */
	public void alteraPresuncaoCorreios(){
		
		String prazoMeioCorreioOrgao = OrgaoJulgadorHome.instance().getInstance().getPresuncaoCorreios();
		String prazoMeioCorreioRegional = "";
		prazoMeioCorreioRegional = ParametroUtil.getFromContext("presuncaoEntregaCorrespondencia", true);
		
		/**
		 * Caso o parametro definido para o regional esteja preenchido, somente pode-se incluir parametro para o órgão se este for maior
		 */
		if ((!prazoMeioCorreioRegional.equalsIgnoreCase(""))  && (!prazoMeioCorreioRegional.equalsIgnoreCase("-1")) && (prazoMeioCorreioRegional!=null)){
			if ((Integer.parseInt(prazoMeioCorreioOrgao) > Integer.parseInt(prazoMeioCorreioRegional)) 
					&& (Integer.parseInt(prazoMeioCorreioOrgao) > 0)){
				OrgaoJulgadorHome.instance().update();
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "orgaoJulgadorPresuncaoCorreios_updated"));
			}
			else{
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Registro não alterado. O prazo definido pelo regional é maior que o prazo definido para o órgão.");				
			}
		} else {
			OrgaoJulgadorHome.instance().update();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "orgaoJulgadorPresuncaoCorreios_updated"));
		}
		
	}

	public void postoAvancadoSelecionado() {
		OrgaoJulgador orgaoJulgado = getInstance();
		
		if(orgaoJulgado.getPostoAvancado() != null && orgaoJulgado.getPostoAvancado() == true) {
			orgaoJulgado.setInstancia("1");
		} else {
			orgaoJulgado.setInstancia(null);
		}
		
		orgaoJulgado.setNumeroVara(null);
		orgaoJulgado.setCodigoOrigem(null);
	}
	
	public String removerVaraDoPosto(OrgaoJulgador vara) {
		OrgaoJulgador posto = getInstance();
		List<OrgaoJulgador> varasAtendidas = posto.getVarasAtendidas();
		boolean removido = false;
		
        if(varasAtendidas != null) {
        	Iterator<OrgaoJulgador> it = varasAtendidas.iterator();
        	
        	while(it.hasNext()) {
        		OrgaoJulgador varaAtendida = it.next();
        		
        		if(varaAtendida.equals(vara)) {
        			it.remove();
        			removido = true;
        			break;
        		}
        	}
        }
        
        if(removido) {
        	getEntityManager().merge(posto);
        	getEntityManager().flush();
            FacesMessages.instance().add("Registro removido com sucesso!");
            return "removed";
        }
        
        return null;
	}

	public void gravarVaraAtendida() {
		OrgaoJulgador posto = getInstance();
		
		if(varaAtendida != null) {
			List<OrgaoJulgador> varasAtendidas = posto.getVarasAtendidas();
			
			if(varasAtendidas.contains(varaAtendida)) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Vara já está associada.");
			} else if(varaAtendida.equals(posto)) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Não é possível associar o posto a ele mesmo.");
			} else {
				varasAtendidas.add(varaAtendida);
				getEntityManager().merge(posto);
				getEntityManager().flush();
				refreshGrid("postoAvancadoGrid");
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Registro adicionado com sucesso!");
			}
			
			varaAtendida = null;
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add("Selecione uma vara.");
		}
		
	}
	
	/**
	 * Busca entre as localiações folha do tribunal ou folhas abaixo da localização física do usuário logado
	 * - eliminando as localizacoes fisicas já utilizadas por OJs ou OJCs
	 * 
	 * @return
	 * @throws PJeBusinessException
	 */
	public List <Localizacao> getLocalizacoesFisicasFolhaCompativeis() throws PJeBusinessException {
		LocalizacaoManager localizacaoManager = ComponentUtil.getComponent(LocalizacaoManager.class);
		List<Integer> idsLocalizacoesFisicas = new ArrayList<>();
		List<Localizacao> localizacoesList = new ArrayList<>();
		
		idsLocalizacoesFisicas.add(Authenticator.getIdLocalizacaoFisicaAtual());
		localizacoesList = localizacaoManager.getLocalizacoesFisicasFolhaCompativeis(idsLocalizacoesFisicas);

		// filtra as localizacoes fisicas que nao possuem nem OJ, nem OJC
		List<Localizacao> localizacoesSemOJouOJCList = new ArrayList<>();
		if(localizacoesList.size() > 0) {
			OrgaoJulgador oj = getInstance();
			for (Localizacao localizacao : localizacoesList) {
				if(!this.isLocalizacaoPossuiOJouOJCDistinto(oj, localizacao)) {
					localizacoesSemOJouOJCList.add(localizacao);
				}
			}
		}
		Collections.sort(localizacoesSemOJouOJCList, (Localizacao p1, Localizacao p2)->p1.getLocalizacao().compareTo(p2.getLocalizacao()));		
		
	    return localizacoesSemOJouOJCList;
	}
	
	public void setNomeCorporativo() {
		try {
			setNomeOrgaoCorporativo(null);
			CorporativoClient client = new CorporativoClient();
			if (getInstance().getCodigoCorporativo() != null) {
				OrgaoGenericoDTO ret = client.recuperaOrgao(getInstance().getCodigoCorporativo().toString());
				if(ret != null) {
					this.verificaOrgaoPertenceTribunal(ret);

					if (BooleanUtils.isNotTrue(ComponentUtil.getComponent(OrgaoJulgadorManager.class).existeOrgaoJulgadorPorNomeEInstancia(
							ret.getNomeOrgao(), getInstance().getInstancia(), getInstance().getIdOrgaoJulgador()))) {

						this.preencheCampoNomeOrgao(ret);
					}
				}
			}
		}
		catch (PJeBusinessException e) {
			getInstance().setCodigoCorporativo(null);
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,e.getCode());
		}
	}

	private void verificaOrgaoPertenceTribunal(OrgaoGenericoDTO ret) throws PJeBusinessException {
		String numeroOrgaoJustica = ComponentUtil.getComponent(ParametroService.class).valueOf("numeroOrgaoJustica");
		if(ret.getTribunal() == null || !ret.getTribunal().getJtr().equals(numeroOrgaoJustica)) {
			throw new PJeBusinessException("O órgão de código "+ getInstance().getCodigoCorporativo() + " não pertence ao seu Tribunal.");
		}
	}

	private void preencheCampoNomeOrgao(OrgaoGenericoDTO ret) {
		if (StringUtil.isEmpty(getInstance().getOrgaoJulgador())) {
			getInstance().setOrgaoJulgador(ret.getNomeOrgao());
		} else {
			if (!getInstance().getOrgaoJulgador().equalsIgnoreCase(ret.getNomeOrgao())) {
				setNomeOrgaoAlterado(true);
				setNomeOrgaoCorporativo(ret.getNomeOrgao());
			}
		}
	}
	
	public void confirmaAlteracaoNome(Boolean alteraNome) {
		setNomeOrgaoAlterado(false);
		if(alteraNome != null && alteraNome) {
				getInstance().setOrgaoJulgador(getNomeOrgaoCorporativo());
		}
		
		setNomeOrgaoCorporativo(null);
			
	}

	public Boolean getNomeOrgaoAlterado() {
		return nomeOrgaoAlterado;
	}

	public void setNomeOrgaoAlterado(Boolean nomeOrgaoAlterado) {
		this.nomeOrgaoAlterado = nomeOrgaoAlterado;
	}

	public String getNomeOrgaoCorporativo() {
		return nomeOrgaoCorporativo;
	}

	public void setNomeOrgaoCorporativo(String nomeOrgaoCorporativo) {
		this.nomeOrgaoCorporativo = nomeOrgaoCorporativo;
	}
}