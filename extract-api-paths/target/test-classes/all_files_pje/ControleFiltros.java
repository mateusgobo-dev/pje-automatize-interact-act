	package br.com.infox.cliente.component;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.TarefasTreeHandler;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.filters.ConsultaProcessoTrfFilter;
import br.jus.pje.nucleo.entidades.filters.ProcessoFilter;
import br.jus.pje.nucleo.entidades.filters.ProcessoTrfFilter;
import br.jus.pje.nucleo.entidades.filters.SituacaoProcessoFilter;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;

@Name(ControleFiltros.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ControleFiltros implements Serializable{

	private static final long serialVersionUID = -6055110653814410318L;
	public static final String INICIALIZAR_FILTROS = "br.com.infox.cliente.component.inicializarFiltros";
	public static final String INICIALIZAR_FILTROS_CONSULTA_ADVOGADO = "br.com.infox.cliente.component.inicializarFiltrosConsultaAdvogado";
	public static final String NAME = "controleFiltros";
	private static final LogProvider log = Logging.getLogProvider(ControleFiltros.class);
	private boolean firstTime = true;
	private Integer idUsuarioLocalizacaoAtual = Authenticator.getIdUsuarioLocalizacaoAtual();
	private GregorianCalendar dataAtual;	

	@Observer(INICIALIZAR_FILTROS_CONSULTA_ADVOGADO)
	public void iniciarFiltroConsultaAdvogado() {
		iniciarFiltro(true);
	}

	@Observer({ INICIALIZAR_FILTROS, TarefasTreeHandler.FILTER_TAREFAS_TREE })
	public void iniciarFiltro() {
		iniciarFiltro(false);
	}

	public void iniciarFiltro(boolean consultaAdvogado, boolean forceUpdate) {
		if (forceUpdate) {
			firstTime = true;
		}
		iniciarFiltro(consultaAdvogado);
	}

	private void iniciarFiltro(boolean consultaAdvogado){
		carregarDataAtual();
		
		idUsuarioLocalizacaoAtual = Authenticator.getIdUsuarioLocalizacaoAtual();
		if (idUsuarioLocalizacaoAtual == null || (!firstTime && idUsuarioLocalizacaoAtual.equals(Authenticator.getIdUsuarioLocalizacaoAtual()) && existeVisibilidade())){
			return;
		}
		firstTime = false;
		
		HibernateUtil.disableAllFilters();

		idUsuarioLocalizacaoAtual = Authenticator.getIdUsuarioLocalizacaoAtual();
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		Integer idOrgaoJulgadorColegiado = Authenticator.getIdOrgaoJulgadorColegiadoAtual() != null ? Authenticator.getIdOrgaoJulgadorColegiadoAtual() : 0;
		Integer idUsrLocMagistrado = Authenticator.getIdUsuarioLocalizacaoMagistradoServidorAtual() != null ? Authenticator.getIdUsuarioLocalizacaoMagistradoServidorAtual() : 0;

		List<Integer> idsLocalizacoesFisicasFilhasList = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
		Integer idLocalizacaoModeloAtual = Authenticator.getIdLocalizacaoModeloAtual();
		Papel papelAtual = Authenticator.getPapelAtual();

		boolean isServidor = Authenticator.isUsuarioInterno();
		boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();
		boolean papelIdentificado = true;

		// filtro para todos os usuários - internos ou externos
		setarFiltrosSegredoDeJustica(usuarioLogado, Authenticator.isVisualizaSigiloso(), idsLocalizacoesFisicasFilhasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);

		if(isServidor) {
			// Os filtros de Oficial estão sendo feitos na grid
			if(!(Authenticator.isPapelOficialJustica() || Authenticator.isPapelOficialJusticaDistribuidor())) {
				if(!existeVisibilidade()) {
					lancarErro("Usuario sem visibilidade");
					return;
				}
				ativarFiltros(idsLocalizacoesFisicasFilhasList, idOrgaoJulgadorColegiado, idLocalizacaoModeloAtual, papelAtual, idUsrLocMagistrado, isServidorExclusivoOJC);
			}
		}else {
			if (Authenticator.isAdvogado() || Authenticator.isAssistenteAdvogado()) {
			// Advogados e seus assistentes.
				if (!consultaAdvogado){
					setarFiltrosAdvogado(usuarioLogado, consultaAdvogado);
				}
			}else if (Authenticator.isPerito()){
				// Perito
				setarFiltrosPerito(usuarioLogado);
	
			}else if (Authenticator.isProcurador() && !Authenticator.isRepresentanteGestor()){
				// Procuradores
				TipoProcessoDocumento tipoProcessoDocumentoExpediente = ParametroUtil.instance().getTipoProcessoDocumentoExpediente();
				if (tipoProcessoDocumentoExpediente == null){
					lancarErro("Parâmetro não encontrado.");
					return;
				}
				setarFiltrosProcurador(usuarioLogado, tipoProcessoDocumentoExpediente, consultaAdvogado);
			}else if (Authenticator.isAssistenteProcurador()){
				// Assistentes de procuradores
				TipoProcessoDocumento tipoProcessoDocumentoExpediente = ParametroUtil.instance().getTipoProcessoDocumentoExpediente();
				if (tipoProcessoDocumentoExpediente == null){
					lancarErro("Parâmetro não encontrado.");
					return;
				}
				setarFiltrosAssistenteProcuradoria(usuarioLogado, tipoProcessoDocumentoExpediente);
			}else {
				// Pessoa não identificada pelos papeis anteriores - aplicando filtro padrao de juspostulandi
				setarFiltrosPostulandi(usuarioLogado);
				if (!Authenticator.isJusPostulandi() && !Authenticator.isUsuarioPush()){
					papelIdentificado = false;
				}
			}
		}
		
		if (papelIdentificado){
			log.debug(MessageFormat.format("Filtro de papel especifico executado para usuário [{0} | {1}].", usuarioLogado, LogUtil.getIdPagina()));	
		} else {
			log.debug(MessageFormat.format("Não foi identificado um papel especifico, foi executado o filtro padrao de JUSPOSTULANDI para usuário [{0} | {1}].", usuarioLogado, LogUtil.getIdPagina()));
		}
	}
	
	private void carregarDataAtual(){
		dataAtual = new GregorianCalendar();
		dataAtual.set(GregorianCalendar.MINUTE, 00);
		dataAtual.set(GregorianCalendar.MILLISECOND, 00);
		dataAtual.set(GregorianCalendar.SECOND, 00);
		dataAtual.set(GregorianCalendar.HOUR_OF_DAY, 00);
	}
	
	private boolean existeVisibilidade(){
		return	Authenticator.temVisibilidade();
	}

	public void desabilitarFiltro(String filterName) {
		HibernateUtil.disableFilters(filterName);
	}

	private void enableFilterProcessoServidor(List<Integer> idsLocalizacoesFisicasList,
			Integer idOrgaoJulgadorColegiado, Integer idUsuarioLocMagServ, boolean isServidorExclusivoOJC) {
		
		this.setarFiltroLocalizacaoServidorEntidade(ProcessoTrfFilter.FILTER_LOCALIZACAO_SERVIDOR, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
		this.setarFiltroLocalizacaoServidorEntidade(ConsultaProcessoTrfFilter.FILTER_LOCALIZACAO_SERVIDOR, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
		
		this.setarFiltroOrgaoJulgadorColegiado(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);
		this.setarFiltroOrgaoJulgadorColegiado(ConsultaProcessoTrfFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);
		
		enableFilterProcessoTrfVisibilidade(idUsuarioLocMagServ);
	}
	
	public void setarFiltroLocalizacaoServidorEntidade(
			String filtroLocalizacaoServidor,
			List<Integer> idsLocalizacoesFisicasList,
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC) {

		HibernateUtil.disableFilters(filtroLocalizacaoServidor);

		HibernateUtil.setFilterParameterList(filtroLocalizacaoServidor,
				ProcessoFilter.FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS, idsLocalizacoesFisicasList);
		HibernateUtil.setFilterParameter(filtroLocalizacaoServidor,
				ProcessoFilter.FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO, isServidorExclusivoOJC);
		HibernateUtil.setFilterParameter(filtroLocalizacaoServidor,
				ProcessoFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);
	}
	
	public void setarFiltroOrgaoJulgadorColegiado(String filtroOrgaoJulgadorColegiado, Integer idOrgaoJulgadorColegiado) {
		HibernateUtil.disableFilters(filtroOrgaoJulgadorColegiado);
		if(idOrgaoJulgadorColegiado != null && idOrgaoJulgadorColegiado > 0) {
			HibernateUtil.setFilterParameter(filtroOrgaoJulgadorColegiado,
					ProcessoFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);
		}
	}

	public static boolean isPapeisNaoFiltraveis() {
		return isPapeisExternosNaoFiltraveis();
	}	
	
	public static boolean isPapeisNaoFiltraveis(Identity identity) {
		return isPapeisExternosNaoFiltraveis(identity);
	}
			
	public static boolean isPapeisExternosNaoFiltraveis(){
		return isPapeisExternosNaoFiltraveis(Identity.instance());
	}
	
	public static boolean isPapeisExternosNaoFiltraveis(Identity identity){
		TipoUsuarioExternoEnum tipousuarioExterno = Authenticator.getTipoUsuarioExternoAtual();
		return tipousuarioExterno != null && !TipoUsuarioExternoEnum.O.equals(tipousuarioExterno);
	}

	private void enableFilterServMagistradoSituacaoProcesso(
				List<Integer> idsLocalizacoesFisicasList, Integer idOrgaoJulgadorColegiado, 
				Integer idLocalizacaoModelo, Papel papel, Integer idUsuarioLocMagServ,
				boolean isServidorExclusivoOJC) {
		
		this.setarFiltroLocalizacaoServidorEntidade(SituacaoProcessoFilter.FILTER_LOCALIZACAO_SERVIDOR, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
		this.setarFiltroOrgaoJulgadorColegiado(SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);

		int idUsuarioLocalizaoMagistradoServidor = idUsuarioLocMagServ.intValue() > 0 ? idUsuarioLocMagServ : -1;
		HibernateUtil.setFilterParameter(SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_CARGO,
			SituacaoProcessoFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO, idUsuarioLocalizaoMagistradoServidor);
		HibernateUtil.setFilterParameter(SituacaoProcessoFilter.FILTER_ORGAO_JULGADOR_CARGO,
			SituacaoProcessoFilter.FILTER_PARAM_DATA_ATUAL, dataAtual.getTime());
		
		enableFiltroUsuarioLocalizacaoFluxo(idLocalizacaoModelo, papel);
	}

	private void enableFiltroUsuarioLocalizacaoFluxo(Integer idLocalizacaoModelo, Papel papel) {
		if(Contexts.getSessionContext() != null && 
				(!Identity.instance().hasRole(Papeis.VISUALIZA_PAINEL_COMPLETO) ||
				Contexts.getSessionContext().get(Papeis.PERFIL_VISUALIZACAO_PAINEL) == null ||
				Contexts.getSessionContext().get(Papeis.PERFIL_VISUALIZACAO_PAINEL).equals(0))
				&& idLocalizacaoModelo != null){
			
			HibernateUtil.setFilterParameter(SituacaoProcessoFilter.FILTER_PAPEL_LOCALIZACAO_FLUXO,
					SituacaoProcessoFilter.FILTER_PARAM_ID_LOCALIZACAO_MODELO, idLocalizacaoModelo);
			HibernateUtil.setFilterParameter(SituacaoProcessoFilter.FILTER_PAPEL_LOCALIZACAO_FLUXO,
					SituacaoProcessoFilter.FILTER_PARAM_ID_PAPEL, papel.getIdPapel());
		}
	}

	public static ControleFiltros instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public static void lancarErro(String msg) {
		FacesMessages.instance().add(Severity.ERROR, msg);
		Redirect.instance().setViewId("/error.seam");
		Redirect.instance().execute();
	}

	public String[] setPooledActors(String expression) {
		if (expression == null) {
			return new String[0];
		}
		expression = expression.substring(expression.indexOf("(") + 1);
		expression = expression.replaceAll("'", "");
		expression = expression.replace(")", "");
		expression = expression.replace("}", "");
		return expression.split(",");
	}

	public static boolean existeOrgaoJulgadorByLocalizacaoMagistradoServidor(Integer idUsuarioLocalizacao){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(ojc.orgaoJulgador) from OrgaoJulgadorCargo ojc ");
		sb.append("where ojc.idOrgaoJulgadorCargo in ( ");
		sb.append("select ulv.orgaoJulgadorCargo.idOrgaoJulgadorCargo ");
		sb.append("from UsuarioLocalizacaoVisibilidade ulv where ");
		sb.append("ulv.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :idUsuarioLocalizacaoMagistradoServidor)");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idUsuarioLocalizacaoMagistradoServidor", idUsuarioLocalizacao);		
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}	
		
	private void ativarFiltros(List<Integer> idsLocalizacoesFisicasList, Integer idOrgaoJulgadorColegiado,
			Integer idLocalizacaoModelo, Papel papel, Integer idUsrLocMagistrado, boolean isServidorExclusivoOJC){
		
		enableFilterServMagistradoSituacaoProcesso(idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, idLocalizacaoModelo, papel, idUsrLocMagistrado, isServidorExclusivoOJC);
		enableFilterProcessoServidor(idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, idUsrLocMagistrado, isServidorExclusivoOJC);
	}

	private void enableFilterProcessoTrfVisibilidade(Integer idUsuarioLocMagServ){
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO,
				idUsuarioLocMagServ);
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO,
				ProcessoTrfFilter.FILTER_PARAM_DATA_ATUAL, dataAtual.getTime());
		
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO,
				ConsultaProcessoTrfFilter.FILTER_PARAM_ID_USUARIO_LOCALIZACAO,
				idUsuarioLocMagServ);
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ORGAO_JULGADOR_CARGO,
				ConsultaProcessoTrfFilter.FILTER_PARAM_DATA_ATUAL, dataAtual.getTime());
	}

	public void setarFiltrosAdvogado(Usuario usuarioLogado, boolean consultaAdvogado){
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ADVOGADO,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ADVOGADO,
				ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA, Authenticator.getIdLocalizacaoAtual());
		if (!consultaAdvogado){
			HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ADVOGADO,
					ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
			HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ADVOGADO,
					ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA, Authenticator.getIdLocalizacaoAtual());
		}
	}

	public void setarFiltrosPostulandi(Usuario usuarioLogado){
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_JUS_POSTULANDI,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_JUS_POSTULANDI,
				ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA, Authenticator.getIdLocalizacaoFisicaAtual());
		
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_JUS_POSTULANDI,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_JUS_POSTULANDI,
				ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA, Authenticator.getIdLocalizacaoFisicaAtual());
	}

	public void setarFiltrosPerito(Usuario usuarioLogado){
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_PERITO,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_PERITO,
				ConsultaProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
	}

	public void setarFiltrosProcurador(Usuario usuarioLogado, TipoProcessoDocumento tipoProcessoDocumentoExpediente, boolean consultaAdvogado){
		// Filtro ProcessoTrf
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_PROCURADOR,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		int idTipoProcessoDocumento = tipoProcessoDocumentoExpediente.getIdTipoProcessoDocumento();
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_PROCURADOR,
				ProcessoTrfFilter.FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE, idTipoProcessoDocumento);
		// Filtro ConsultaProcessoTrf
		if (!consultaAdvogado){
			HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_PROCURADOR,
					ConsultaProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
			HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_PROCURADOR,
					ConsultaProcessoTrfFilter.FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE,
					idTipoProcessoDocumento);
		}
	}

	public void setarFiltrosAssistenteProcuradoria(Usuario usuarioLogado, TipoProcessoDocumento tipoProcessoDocumentoExpediente){
		int idTipoProcessoDocumento = tipoProcessoDocumentoExpediente.getIdTipoProcessoDocumento();
		// Filtro ProcessoTrf
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA,
				ProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA,
				ProcessoTrfFilter.FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE, idTipoProcessoDocumento);
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-2302 - 2012-09-05 Alteracoes feitas pela JT.
		 * Inclusão do parâmetro "idLocalizacao" no filtro "assistenteProcuradoriaProcessoTrf".
		 */
		HibernateUtil.setFilterParameter(ProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA,
				ProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA, Authenticator.getIdLocalizacaoAtual());
		/*
		 * PJE-JT: Fim.
		 */
		// Filtro ConsultaProcessoTrf
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA,
				ConsultaProcessoTrfFilter.FILTER_PARAM_ID_USUARIO, usuarioLogado.getIdUsuario());
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA,
				ConsultaProcessoTrfFilter.FILTER_PARAM_ID_TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE,
				idTipoProcessoDocumento);
		/*
		 * PJE-JT: Ricardo Scholz : PJEII-2302 - 2012-09-05 Alteracoes feitas pela JT.
		 * Inclusão do parâmetro "idLocalizacao" no filtro "assistenteProcuradoriaProcessoTrf".
		 */
		HibernateUtil.setFilterParameter(ConsultaProcessoTrfFilter.FILTER_ASSISTENTE_PROCURADORIA,
				ConsultaProcessoTrfFilter.FILTER_PARAM_ID_LOCALIZACAO_FISICA, Authenticator.getIdLocalizacaoAtual());
		/*
		 * PJE-JT: Fim.
		 */
	}

	/***
	 * 
	 * @param usuarioLogado
	 * @param visualizaSigiloso
	 * @param idsLocalizacoesFisicasList
	 * @param idOrgaoJulgadorColegiado
	 * @param isServidorExclusivoOJC
	 */
	public void setarFiltrosSegredoDeJustica(
			Usuario usuarioLogado,  boolean visualizaSigiloso, 
			List<Integer> idsLocalizacoesFisicasList,
			Integer idOrgaoJulgadorColegiado, boolean isServidorExclusivoOJC){
		
		Integer idUsuario = usuarioLogado.getIdUsuario();
		// filtro do cabecalho do processo
		this.setarFiltroSegredoSigiloEntidade(ConsultaProcessoTrfFilter.FILTER_SEGREDO_JUSTICA, 
				idUsuario, visualizaSigiloso, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
		
		// filtro de abertura/pesquisa do processo
		this.setarFiltroSegredoSigiloEntidade(ProcessoTrfFilter.FILTER_SEGREDO_JUSTICA, 
				idUsuario, visualizaSigiloso, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);

		// filtros das tarefas
		this.setarFiltroSegredoSigiloEntidade(SituacaoProcessoFilter.FILTER_SEGREDO_JUSTICA, 
				idUsuario, visualizaSigiloso, idsLocalizacoesFisicasList, idOrgaoJulgadorColegiado, isServidorExclusivoOJC);
	}
	
	public void setarFiltroSegredoSigiloEntidade(
			String filtroSegredoSigilo, Integer idUsuario, 
			boolean visualizaSigiloso, 
			List<Integer> idsLocalizacoesFisicasList,
			Integer idOrgaoJulgadorColegiado,
			boolean isServidorExclusivoOJC) {

		HibernateUtil.disableFilters(filtroSegredoSigilo);

		HibernateUtil.setFilterParameter(filtroSegredoSigilo,
				ProcessoFilter.FILTER_PARAM_ID_USUARIO, idUsuario);
		HibernateUtil.setFilterParameter(filtroSegredoSigilo, 
				ProcessoFilter.FILTER_PARAM_VISUALIZA_SIGILOSO, visualizaSigiloso);
		HibernateUtil.setFilterParameterList(filtroSegredoSigilo, 
				ProcessoFilter.FILTER_PARAM_IDS_LOCALIZACOES_FISICAS_FILHAS, idsLocalizacoesFisicasList);
		HibernateUtil.setFilterParameter(filtroSegredoSigilo, 
				ProcessoFilter.FILTER_PARAM_ID_ORGAO_JULGADOR_COLEGIADO, idOrgaoJulgadorColegiado);
		HibernateUtil.setFilterParameter(filtroSegredoSigilo, 
				ProcessoFilter.FILTER_PARAM_SERVIDOR_EXCLUSIVO_COLEGIADO, isServidorExclusivoOJC);

	}
}
