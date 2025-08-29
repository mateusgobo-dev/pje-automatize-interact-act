/**
 * 
 * Inclui regras para validar acesso a processos por parte de usuários (JIRA PJE-22)
 * 
 * @author rosfran.borges 
 */

package br.jus.cnj.pje.security;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.access.SecurityUtil;
import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.TarefasTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.UsuarioHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name("processoSecurity")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoSecurity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4176891830345400743L;
	public static final String PAGES_PREFIX = "/pages";
	private static final LogProvider log = Logging.getLogProvider(ProcessoSecurity.class);
	private Map<String, Boolean> cacheRestricoesMovimentar = new HashMap<String, Boolean>(0);

	public boolean existeProcessoPessoa(PessoaFisica pessoa, String numeroProcesso) {
		String query = "select o from Processo o where o.numeroProcesso = :nrProcesso";
		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("nrProcesso", numeroProcesso);
		Processo processo = EntityUtil.getSingleResult(q);

		boolean acessoOk = (processo != null && processo.getActorId().equalsIgnoreCase(pessoa.getLogin()));

		if (!acessoOk) {
			log.info(MessageFormat.format("Bloqueado o acesso do perfil ''{0}'' no usuário ''{1}''.", Contexts
					.getSessionContext().get("identificadorPapelAtual"),
					(processo != null && processo.getActorId() != null) ? processo.getActorId() : ""));
		}

		return acessoOk;

	}

	@SuppressWarnings("unchecked")
	public boolean existeProcessoPartePessoa(PessoaFisica pessoa, String numeroProcesso) {
		boolean pessoaEncontrada = false;

		/* tentar consulta HQL mais eficaz */
		String s = "select ppa " + "from ProcessoParte ppa " + "where ppa.processoTrf.idProcessoTrf = :processoTrf "
				+ "and ppa.pessoa = :pessoa";
		Query q = EntityUtil.getEntityManager().createQuery(s);
		q.setParameter("processoTrf", Integer.parseInt(numeroProcesso));
		q.setParameter("pessoa", pessoa);
		List<ProcessoParte> partesEncontradas = q.getResultList();

		/* se não tiver partes no processo, nega acesso */
		if ((null == partesEncontradas) || (partesEncontradas.size() <= 0))
			return false;

		if (partesEncontradas.size() > 0) {

			for (ProcessoParte pa : partesEncontradas) {
				if ((pa.getPessoa().getIdUsuario() == pessoa.getIdUsuario())
						|| (verificarAdvogado(pa, pessoa) || verificarRepresentante(pa, pessoa))) {
					pessoaEncontrada = true;
					break;
				}

			} // for
		} // if

		boolean acessoOk = pessoaEncontrada;

		if (!acessoOk) {
			log.info(MessageFormat.format("Bloqueado o acesso do perfil ''{0}'' no usuário ''{1}''.", Contexts
					.getSessionContext().get("identificadorPapelAtual"), (pessoa != null) ? pessoa : ""));
		}

		return acessoOk;

	}

	@SuppressWarnings("unchecked")
	public boolean verificarRepresentante(ProcessoParte pp, Pessoa p) {

		StringBuilder sb = new StringBuilder();
		sb.append("select ppr from ProcessoParteRepresentante ppr ");
		sb.append("where ppr.processoParte = :processoParte ");
		sb.append("and ppr.representante = :representante ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("processoParte", pp);
		q.setParameter("representante", p);
		final List<ProcessoParteRepresentante> ls = q.getResultList();

		return (ls != null) && (ls.size() > 0);

	}

	public boolean verificarAdvogado(ProcessoParte pp, Pessoa p) {

		StringBuilder sql = new StringBuilder();
		sql.append("select COUNT(ppa) from ProcessoParteAdvogado ppa ");
		sql.append("where ppa.processoParte = :processoParte ");
		sql.append("and ppa.pessoaAdvogado = :pessoaAdvogado");
		Query q = EntityUtil.getEntityManager().createQuery(sql.toString());
		q.setParameter("processoParte", pp);
		q.setParameter("pessoaAdvogado", p);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();

		return cont.intValue() > 0;

	}

	public UsuarioLocalizacao getUsuarioLocalizacaoAtual() {
		UsuarioLocalizacao usuarioLocalizacaoAtual = null;
		usuarioLocalizacaoAtual = UsuarioHome.getUsuarioLocalizacaoAtual();
		usuarioLocalizacaoAtual = EntityUtil.getEntityManager().find(UsuarioLocalizacao.class,
				usuarioLocalizacaoAtual.getIdUsuarioLocalizacao());
		return usuarioLocalizacaoAtual;
	}

	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacao> findUsuarioLocalizacao(Pessoa p) {
		List<UsuarioLocalizacao> u_l = null;

		StringBuilder sb = new StringBuilder();
		sb.append("select o from UsuarioLocalizacao o where ");
		sb.append(" o.usuario = :usuario and ");
		sb.append("o.localizacao = :localizacao");

		String sql = sb.toString();
		EntityManager em = EntityUtil.getEntityManager();
		Query query = em.createQuery(sql).setParameter("usuario", p);

		u_l = query.getResultList();

		return u_l;
	}

	public boolean permiteProcessoDiretor(PessoaFisica pessoa, String numeroProcesso) {
		boolean isDiretor = false, acessoOk = false, acessoPerm = false;

		String query = "select o from ProcessoTrf o where o.idProcessoTrf = :nrProcesso";
		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("nrProcesso", Integer.parseInt(numeroProcesso));
		ProcessoTrf processo = EntityUtil.getSingleResult(q);
		/* cria novo processo - NÃO BLOQUEIA */
		if (null == processo)
			return true;
		String pap = (String) Contexts.getSessionContext().get("identificadorPapelAtual");

		Localizacao loc = getUsuarioLocalizacaoAtual().getLocalizacaoFisica();

		if (pap.indexOf(Papeis.DIRETOR_SECRETARIA) != -1) {
			isDiretor = true;

			Localizacao l = null;
			if (processo.getOrgaoJulgador() != null)
				l = processo.getOrgaoJulgador().getLocalizacao();

			if ((l != null) && (l.getLocalizacao() != null) && (l.getLocalizacao().length() > 0)) {
				if (loc.getLocalizacao().equals(l.getLocalizacao())) {
					acessoPerm = true;
				}

			} else {
				acessoPerm = true;
			}
		} // if

		/* é diretor? */
		if (isDiretor) {
			/*
			 * se tem permissão e é diretor, permite acesso; caso contrário,
			 * nega
			 */
			if (acessoPerm) {
				acessoOk = true;
			}

		} 

		return acessoOk;

	}

	public boolean permitirAcessoPorPerfil(PessoaFisica pessoa, ProcessoTrf processo) {
		boolean acessoOk = false;

		String identificadorPapelAtual = (String) Contexts.getSessionContext().get("identificadorPapelAtual");

		if (identificadorPapelAtual == null || identificadorPapelAtual.trim().length() == 0)
			return false;

		if (null == processo || processo.getOrgaoJulgador() == null
				|| processo.getOrgaoJulgador().getLocalizacao() == null)
			return true;

		// Localizacao localizacaoUsuario =
		// getUsuarioLocalizacaoAtual().getLocalizacao();
		// Localizacao localizacaoProcesso =
		// processo.getOrgaoJulgador().getLocalizacao();

		OrgaoJulgador orgaoProcesso = processo.getOrgaoJulgador();
		OrgaoJulgador orgaoUsuario = Authenticator.getOrgaoJulgadorAtual();

		if (orgaoProcesso.equals(orgaoUsuario))
			acessoOk = true;
		else {
			acessoOk = false;
			log.info(MessageFormat.format(
					"Bloqueado o acesso do perfil ''{0}'' no processo ''{1}'' para o usuário {2}.",
					identificadorPapelAtual, processo.getIdProcessoTrf(), pessoa));
		}
		return acessoOk;
	}

	/**
	 * Poderá abrir o processo ainda não protocolado se (ver regra do arquivo: consultaProcessoNaoProtocoladoGrid.component.xml
	 *		Mostra os processos não protocolados criado por alguém da localização do usuário logado:
	 *		- se forem sigilosos - quem vê é quem criou e/ou o responsável pela localização de quem criou;
	 *		- se não for sigiloso - quem vê é qualquer um da mesma localizacão de quem criou
	 * @param processo
	 * @return
	 */
	public boolean checkPageAutuacao(ProcessoTrf processo) {
		if(!(Authenticator.getIdLocalizacaoFisicaAtual().equals(processo.getLocalizacaoInicial().getIdLocalizacao()) && 
				(processo.getEstruturaInicial() == null || Authenticator.getIdLocalizacaoModeloAtual() == null ||
					Authenticator.getIdLocalizacaoModeloAtual().equals(processo.getEstruturaInicial().getIdLocalizacao()))) ) {
			return false;
		}
		if(!processo.getSegredoJustica() || (
				Authenticator.getUsuarioLocalizacaoAtual().getResponsavelLocalizacao()
				|| Authenticator.getPessoaLogada().getIdUsuario().equals(
							processo.getProcesso().getUsuarioCadastroProcesso().getIdUsuario()
						))) {
			return true;
		}
		return checkPageProcesso(String.valueOf(processo.getIdProcessoTrf()));
	}

	public boolean checkPageProcesso(String numeroProcesso) {
		boolean acessoOk = true;

		// if ( SecurityUtil.instance() != null )
		// acessoOk = SecurityUtil.instance().checkPage();

		if ((null == numeroProcesso) || (numeroProcesso.length() <= 0))
			return acessoOk;

		// if ( !acessoOk )
		// return false;

		// String servletPath = request.getServletPath();

		Context session = Contexts.getSessionContext();
		PessoaFisica pessoa = (PessoaFisica) session.get("usuarioLogado");
		acessoOk = permiteProcessoDiretor(pessoa, numeroProcesso);
		
		if (!acessoOk) {
			// verifica a permissão do acesso pra outro tipo de usuário
			ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent(ProcessoTrfHome.class);
			acessoOk = processoTrfHome.beforeCheckVisibilidadeProcesso();
		}
		/*
		 * || existeProcessoPessoa( pessoa, numeroProcesso ) ||
		 * existeProcessoPartePessoa( pessoa, numeroProcesso );
		 */

		if (!acessoOk) {
			log.info(MessageFormat.format("Bloqueado o acesso do perfil ''{0}'' no processo ''{1}'' "
					+ "pelo usuário {2}.", Contexts.getSessionContext().get("identificadorPapelAtual"),
					numeroProcesso, (pessoa != null) ? pessoa : ""));
		}

		return acessoOk;
	}

	public boolean checkPageProcessoPorPerfil(ProcessoTrf processoTrf) {
		if (SecurityUtil.instance() != null)
			if (!SecurityUtil.instance().checkPage())
				return false;

		if (processoTrf == null || processoTrf.getIdProcessoTrf() == 0)
			return false;

		PessoaFisica pessoa = (PessoaFisica) Contexts.getSessionContext().get("usuarioLogado");
		return permitirAcessoPorPerfil(pessoa, processoTrf);
	}

	public static ProcessoSecurity instance() {
		return ComponentUtil.getComponent("security");
	}

	public boolean checkPageMovimentar(Integer idProcesso) {
		return checkPageMovimentar(idProcesso, null, null);
	}

	public boolean checkPageMovimentar(Integer idProcesso, Integer taskId, Long newTaskId) {
		if(Contexts.getSessionContext().get(Papeis.PERFIL_VISUALIZACAO_PAINEL) != null &&
				Contexts.getSessionContext().get(Papeis.PERFIL_VISUALIZACAO_PAINEL).equals(1)){
			return false;
		}
		String uniqueRestriction = idProcesso + ":" + taskId + ":" + newTaskId;

		if (!cacheRestricoesMovimentar.containsKey(uniqueRestriction)) {

			boolean hasPermission = false;
			if(!Authenticator.isUsuarioExterno()) {
				if (idProcesso != null && idProcesso != 0) {
					if ((newTaskId == null || newTaskId == 0) && (taskId == null || taskId == 0)) {
						Events.instance().raiseEvent(TarefasTreeHandler.FILTER_TAREFAS_TREE);
						EntityManager em = EntityUtil.getEntityManager();
						Long count = EntityUtil.getSingleResult(em
								.createQuery(
										"select count(distinct o) "
												+ "from SituacaoProcesso o where o.idProcesso = :id "
												+ "group by o.idTaskInstance").setParameter("id", idProcesso));
						hasPermission = count == null ? false : count > 0;
					} else if (newTaskId != null && newTaskId != 0) {
						Events.instance().raiseEvent(TarefasTreeHandler.FILTER_TAREFAS_TREE);
						
						String sql = "select count(o) "
								+ "from SituacaoProcesso o where o.idProcesso = :idProcessoTrf "
								+ "and o.idTaskInstance = :taskId " 
								+ "group by o.idTaskInstance";
						Query q = EntityUtil.getEntityManager().createQuery(sql);
												
						q.setParameter("idProcessoTrf", idProcesso);
						q.setParameter("taskId", newTaskId);
						Long count = (Long) EntityUtil.getSingleResult(q);
						
						hasPermission = count == null ? false : count > 0;
					} else if (taskId != null && taskId != 0) {
						Events.instance().raiseEvent(TarefasTreeHandler.FILTER_TAREFAS_TREE);
						EntityManager em = EntityUtil.getEntityManager();
						Long count = EntityUtil.getSingleResult(em
								.createQuery(
										"select count(distinct o) "
												+ "from SituacaoProcesso o where o.idProcesso = :id "
												+ "and o.idTarefa = :taskId " + "group by o.idTaskInstance")
								.setParameter("id", idProcesso).setParameter("taskId", taskId));
						hasPermission = count == null ? false : count > 0;
					}
				}
			}
			cacheRestricoesMovimentar.put(uniqueRestriction, hasPermission);
		}
		return cacheRestricoesMovimentar.get(uniqueRestriction);
	}
	
	public boolean checkVisibilidadeProcesso(int idProcesso){
		if (SecurityUtil.instance() != null)
			if (!SecurityUtil.instance().checkPage())
				return false;
		ControleFiltros.instance().iniciarFiltro();
		Query query = EntityUtil.getEntityManager().createQuery(
				"select o.idProcessoTrf from ProcessoTrf o where o.idProcessoTrf = :id");
		query.setParameter("id", idProcesso);
		Object result = EntityUtil.getSingleResult(query);
		boolean check = result != null;
		if (!check){
			FacesMessages.instance().add(Severity.ERROR, "Sem permissão para acessar o processo solicitado.");
		}
		
		return check;
	}
	
	public boolean checkVisibilidadePeticionamentoAvulso(int idProcesso){
		Query query = EntityUtil.getEntityManager().createQuery("select o from ProcessoTrf o where o.idProcessoTrf = :id");
		query.setParameter("id", idProcesso);
		ProcessoTrf processoTrf = EntityUtil.getSingleResult(query);
		
		boolean check = true;
		
		if (processoTrf == null) {
			check = false;
			FacesMessages.instance().add(Severity.ERROR, "Processo inexistente para o id: " + idProcesso);
		}
		if (processoTrf.getInOutraInstancia() && ParametroUtil.instance().isBloquearProcessoRemetido()){
			check = false;
			FacesMessages.instance().add(Severity.ERROR, "Processo remetido para outra instância.");
		}
		if (processoTrf.getInBloqueiaPeticao()) {
			check = false;
			FacesMessages.instance().add(Severity.ERROR, "Processo bloqueado para peticionamento.");
		}
		
		return check;
	}

}
