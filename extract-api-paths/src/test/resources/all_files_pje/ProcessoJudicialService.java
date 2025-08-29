package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.home.AlertaHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.LocalizacaoUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.HistoricoDeslocamentoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoInstanceManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteSigiloManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoSegredoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoVisibilidadeSegredoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.cache.ProcessoParteCache;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.PecaMinDTO;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.util.CustomTransactionalWork;
import br.jus.cnj.pje.vo.VariavelSinalizacaoFluxoVO;
import br.jus.cnj.pje.webservice.client.bnmp.PecaMinBnmpRestClient;
import br.jus.pje.jt.entidades.HistoricoDeslocamentoOrgaoJulgador;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Fluxo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoInstance;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.nucleo.entidades.ProcessoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.ProcessoTrfApreciadoEnum;
import br.jus.pje.nucleo.enums.SegredoEntreProcessosJudiciaisEnum;
import br.jus.pje.nucleo.enums.SegredoStatusEnum;
import br.jus.pje.nucleo.enums.SigiloStatusEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Operator;

/**
 * @author cristof
 *
 */
@Name("processoJudicialService")
public class ProcessoJudicialService extends BaseService{
	
	@Logger
	private Log log;
	
	private ProcessoParteCache processoParteCache;

	private ProcessoParteCache getProcessoParteCache() {
		if (processoParteCache == null) {
			processoParteCache = (ProcessoParteCache) Component.getInstance(ProcessoParteCache.COMPONENT_NAME);
		}

		return processoParteCache;
	}

	public static ProcessoJudicialManager getProcessoJudicialManager() {
		return (ProcessoJudicialManager)Component.getInstance(ProcessoJudicialManager.NAME);
	}

	public static ProcessoJudicialService instance() {
		return ComponentUtil.getComponent(ProcessoJudicialService.class);
	}
	
	/**
	 * Recupera o processo judicial a partir de seu identificador interno.
	 * 
	 * @param id o identificador interno do processo.
	 * @return o processo judicial
	 * @throws PJeBusinessException caso alguma regra de negócio tenha sido violada
	 * @see ProcessoTrf
	 */
	public ProcessoTrf findById(Integer id) throws PJeBusinessException {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class).findById(id);
	}
	
	/**
	 * Método responsável por verificar se um devido processo está em segredo de justiça
	 * @param processoTrf processo a ser verificado
	 * @return Boolean: Retorna TRUE caso o processo esteja em segredo de justiça
	 */
	public boolean verificaSegredoJustica(ProcessoTrf processoTrf)
	{
		SituacaoProcesso situacaoProcesso = ComponentUtil.getComponent(ProcessoTrfManager.class).getSituacaoProcesso(processoTrf, true);
		return (situacaoProcesso != null && situacaoProcesso.getSegredoJustica());
	}
	
	/**
 	 * Indica se um determinado processo judicial tem pelo menos um
 	 * alerta, de qualquer criticidade, ativo.
 	 * 
 	 * @param processoJudicial o processo a respeito do qual se pretende
 	 * recuperar a informação
 	 * @return true, se houver pelo menos um alerta ativo no processo
 	*/
 	public boolean possuiAlertasAtivos(ProcessoTrf processoJudicial) throws PJeBusinessException {
 		return ComponentUtil.getComponent(ProcessoAlertaManager.class).possuiAlertasAtivos(processoJudicial);
 	}
 
	/**
	 * Recupera o processo judicial a partir da instância de processo de negócio dada.
	 * 
	 * @param processInstance a instância de processo de negócio que estaria vinculada ao 
	 * processo desejado
	 * @return o processo judicial vinculado ? instância de processo de negócio dada
	 * @throws PJeBusinessException
	 */
	public ProcessoTrf findByProcessInstance(ProcessInstance processInstance) throws PJeBusinessException {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class).findByProcessInstance(processInstance);
	}

	public Usuario recuperaRelator(ProcessoTrf processoJudicial){
		if(processoJudicial == null || processoJudicial.getOrgaoJulgador() == null){
			return null;
		}
		return ComponentUtil.getComponent(ProcessoJudicialManager.class).getRelator(processoJudicial);
	}
	
	/**
	 * Método responsável por recuperar a lista de fluxos de processo de negócio ativos vinculados a um processo judicial. 
	 * Também sinaliza as tarefas que estão configuradas para aguardar petição (pje:aguardaPeticao).  
	 * @param processoJudicial o processo cujos fluxos se pretende recuperar
	 * @return a lista de fluxos ativos
	 */
	public List<ProcessInstance> getBusinessProcesses(ProcessoTrf processoJudicial){
		return getBusinessProcesses(processoJudicial, true);
	}
	
	/**
	 * Método responsável por recuperar a lista de fluxos de processo de negócio ativos vinculados a um processo judicial
	 * @param processoJudicial o processo cujos fluxos se pretende recuperar
	 * @param sinalizarTarefas Informa se o sistema deve sinalizar as tarefas que estão configuradas para aguardar petição (pje:aguardaPeticao)
	 * @return a lista de fluxos ativos
	 */
	public List<ProcessInstance> getBusinessProcesses(ProcessoTrf processoJudicial, boolean sinalizarTarefas){
		List<Long> idsProcessInstances = ComponentUtil.getComponent(ProcessoJudicialManager.class).getBusinessProcessIds(processoJudicial);
		List<ProcessInstance> ret = new ArrayList<ProcessInstance>();
		ProcessInstance pi = null;
		
		for (Long id: idsProcessInstances) {
			try {
				pi = ManagedJbpmContext.instance().getProcessInstance(id);
			} catch (IllegalStateException e) {
				ProcessInstance process = org.jboss.seam.bpm.ProcessInstance.instance();

				if (process != null && !process.hasEnded()) {
					e.printStackTrace();
					throw new NegocioException("Erro na obtenção do arquivo de bundle do Seam.");
				}
			}
			if(pi == null) {
				continue;
			}
			if (sinalizarTarefas) {
				String aguardaPeticao = (String) pi.getContextInstance().getVariable("pje:aguardaPeticao");
				
				if(aguardaPeticao != null && aguardaPeticao.equalsIgnoreCase("true")){
					System.out.println("Incluindo a pi [" + pi.getProcessDefinition().getName() + " -  " + pi.getId() + "], que está em [" + pi.getRootToken().getName() + "]");
					System.out.println("Tokens => [" + pi.findAllTokens() + "]");
					pi.getRootToken().signal();
				}
			}
			
			ret.add(pi);
		}
		
		return ret;
	}
	
	/**
	 * Método observador do evento de preclusão de manifestação, que deve ocorrer 
	 * quando da resposta do advogado ou quando do término do prazo.
	 * O comportamento esperado é que, havendo algum fluxo vinculado ao processo judicial
	 * indicado e existindo, nesse fluxo, uma variável de tarefa ou de fluxo com nome
	 * pje:aguardaPeticao, o sistema "empurre" o processo para a transição padrão do nó ou,
	 * inexistindo essa transição, para a primeira transição disponível.
	 * 
	 * @param processoJudicial o processo judicial no qual houve a preclusão.
	 */
	@Observer(value={Eventos.EVENTO_PRECLUSAO_MANIFESTACAO})
	@Transactional
	public void observaPreclusaoManifestacao(ProcessoTrf processoJudicial) {
		sinalizaPreclusaoManifestacao(processoJudicial);
	}

	public void sinalizaPreclusaoManifestacao(ProcessoTrf processoJudicial) {
		sinalizarFluxo(processoJudicial, "pje:aguardaPeticao", true, false, true);
	}

	@Observer(value={Eventos.EVENTO_CIENCIA_DADA})
	@Transactional
	public void observaCienciaDada(ProcessoTrf processoJudicial){
		sinalizarFluxo(processoJudicial, "pje:aguardaCiencia", "true", false, true);
	}

	/**
     * Verifica se o {@link ProcessoTrf} parâmetro possui instância em tarefa configurada para sinalização.
     * A tarefa deve possuir variável de tarefa como nome 'pje:aguardaPublicacaoDJE' e valor 'true'.
     * O processo será movimentado para a transição padrão configurada na tarefa, ou, caso não tenha sido configurada, a primeira transição. 
     *
     * @param processoJudicial {@link ProcessoTrf} que será movimentado
     */
    @Observer(value={Eventos.EVENTO_PUBLICACAO_DJE})
    @Transactional
	public void observaPublicacaoDJE(ProcessoTrf processoJudicial) {
		sinalizaPublicacaoDJE(processoJudicial);
	}

	public void sinalizaPublicacaoDJE(ProcessoTrf processoJudicial) {
		sinalizarFluxo(processoJudicial, "pje:aguardaPublicacaoDJE", true, false, true);
	}

	/**
     * Verifica se o {@link ProcessoTrf} parâmetro possui instância em tarefa configurada para sinalização.
     * A tarefa deve possuir variável de tarefa como nome 'pje:aguardaDisponibilizacaoDJE' e valor 'true'.
     * O processo será movimentado para a transição padrão configurada na tarefa, ou, caso não tenha sido configurada, a primeira transição. 
     *
     * @param processoJudicial {@link ProcessoTrf} que será movimentado
     */
    @Observer(value={Eventos.EVENTO_DISPONIBILIZACAO_DJE})
    @Transactional
	public void observaDisponibilizacaoDJE(ProcessoTrf processoJudicial) {
		sinalizaDisponibilizacaoDJE(processoJudicial);
	}

	public void sinalizaDisponibilizacaoDJE(ProcessoTrf processoJudicial) {
		sinalizarFluxo(processoJudicial, "pje:aguardaDisponibilizacaoDJE", true, false, true);
	}

	/**
	 * Verifica se o {@link ProcessoTrf} parâmetro possui instância em tarefa configurada para sinalização.
	 * A tarefa deve possuir variável de tarefa como nome 'pje:aguardaDataAudiencia' e valor 'true'.
	 * O processo será movimentado para a transição padrão configurada na tarefa, ou, caso não tenha sido configurada, a primeira transição. 
	 *
	 * @param processoJudicial {@link ProcessoTrf} que será movimentado
	 */
	@Observer(value={Eventos.EVENTO_DATA_AUDIENCIA})
	@Transactional
	public void observaDataAudiencia(ProcessoTrf processoJudicial) {
		sinalizaDataAudiencia(processoJudicial);
	}

	public void sinalizaDataAudiencia(ProcessoTrf processoJudicial) {
		sinalizarFluxo(processoJudicial, "pje:aguardaDataAudiencia", true, false, true);
	}

	@Observer(value={Eventos.EVENTO_EXPEDIENTE_FECHADO})
	public void observaExpedienteFechado(ProcessoTrf processoJudicial){
		sinalizarFluxo(processoJudicial, "pje:aguardaFecharExpediente", "true", false, true);
	}
	
	@Observer(value={Eventos.EVENTO_ESTOURO_PRAZO})
	@Transactional
	public void observaEstouroPrazo(ProcessoTrf processoJudicial) {
		sinalizaEstouroPrazo(processoJudicial);
	}

	public void sinalizaEstouroPrazo(ProcessoTrf processoJudicial) {
		sinalizarFluxo(processoJudicial, "pje:aguardaEstouroPrazo", true, true, false);
	}

	@Observer(value={Eventos.EVENTO_ENCERRA_SESSAO})
	public void observaEncerraSessaoPlenaria(ProcessoTrf processoJudicial) throws Exception{
		try{
			sinalizarFluxo(processoJudicial, "pje:fluxo:votacaoColegiado:emJulgamento", true, false, true);
		}catch(Exception e){
			log.error("[EVENTO_ENCERRA_SESSAO] Tentativa de encaminha processo para a próxima tarefa não sou bem sucedida ", e);
			e.printStackTrace();
			throw e;
		}
	}
	
	@Observer(value={Eventos.EVENTO_PROCESSO_JULGADO_COLEGIADO})
	@Transactional
	public void observaJulgamentoColegiadoOcorrido(Integer idJulgamento){
		if(idJulgamento == null){
			log.warn("Erro ao observar o julgamento colegiado do processo: o identificador do julgamento está nulo.");
			return;
		}
		try {
			SessaoPautaProcessoTrf julg = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class).findById(idJulgamento);
			ProcessoTrf processo = julg.getProcessoTrf();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Variaveis.VARIAVEL_FLUXO_COLEGIADO_JULGAMENTO, idJulgamento);
			
			String situacao = null;
			
			if(julg.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.JG)){
				OrgaoJulgador vencedor = julg.getOrgaoJulgadorVencedor();
			
				Integer idCargo = null;
				Integer idMagistrado = null;
				for (SessaoPautaProcessoComposicao composicao : julg.getSessaoPautaProcessoComposicaoList()) {
					if (composicao.getOrgaoJulgador() != null && composicao.getOrgaoJulgador().equals(vencedor)) {
						if (composicao.getCargoAtuacao() != null) {
							idCargo = composicao.getCargoAtuacao().getIdOrgaoJulgadorCargo();	
						}
						if (composicao.getMagistradoPresente() != null) {
							idMagistrado = composicao.getMagistradoPresente().getIdUsuario();
						}
						break;
					}
				}			
				if (idCargo == null) {
					List<OrgaoJulgadorCargo> cargos = ComponentUtil.getComponent(OrgaoJulgadorCargoManager.class).recuperaAtivos(vencedor, true);
					idCargo = (cargos != null && !cargos.isEmpty()) ? cargos.get(0).getIdOrgaoJulgadorCargo() : null;
				}
			
				if(vencedor == null || idCargo == null){
					log.warn("Erro ao observar o julgamento colegiado do processo com identificador {0}: o órgão julgador vencedor ou o cargo não foram encontrados.", processo.getIdProcessoTrf());
					return;
				}
				
			
				map.put(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_COLEGIADO_VENCEDOR, vencedor.getIdOrgaoJulgador());
				map.put(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_COLEGIADO_CARGO_VENCEDOR, idCargo);
				map.put(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_COLEGIADO_MAGISTRADO_VENCEDOR, idMagistrado);
				situacao = "julgado";
			}
			if(julg.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.NJ) && julg.getRetiradaJulgamento() != null && julg.getRetiradaJulgamento()){
				situacao = "retiradoJulgamento";
			}
			else if(julg.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.NJ) && julg.getAdiadoVista().equals(AdiadoVistaEnum.AD)){
				situacao = "adiado";
			}
			else if(julg.getSituacaoJulgamento().equals(TipoSituacaoPautaEnum.NJ) && julg.getAdiadoVista().equals(AdiadoVistaEnum.PV)){
				situacao = "pedidoVista";
				map.put(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_OJ_PEDIDO_VISTA, julg.getOrgaoJulgadorPedidoVista().getIdOrgaoJulgador());
			}
			
			map.put(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_SITUACAO, situacao);
			
			sinalizarFluxo(processo, Variaveis.VARIAVEL_FLUXO_COLEGIADO_AGUARDA_SESSAO, true, true, true, map);
		} catch (PJeBusinessException e) {
			log.error("Erro ao tentar observar o julgamento colegiado de identificador {0}: {1}", idJulgamento, e.getLocalizedMessage());
		}
	}
	
	@Observer(value={Eventos.EVENTO_DECISAO_VOGAL_COLEGIADA_LIBERADA})
	@Transactional
	public void observaDecisaoVogalLiberada(Integer idProcessoJudicial){
		if(idProcessoJudicial == null){
			return;
		}
		ProcessoTrf processo = EntityUtil.getEntityManager().getReference(ProcessoTrf.class, idProcessoJudicial);
		if(processo == null){
			log.error("Erro ao tentar observar o julgamento colegiado do processo com identificador {0}", idProcessoJudicial);
			return;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Variaveis.VARIAVEL_FLUXO_JULGAMENTO_SITUACAO, "vogalLiberada");
		sinalizarFluxo(processo, Variaveis.VARIAVEL_FLUXO_COLEGIADO_AGUARDA_RELATOR, true, true, true, map);
	}
	
	/**
	 * Método responsável por iniciar o fluxo incidental.
	 * 
	 * @param idDocumento Id do documento ao qual será associado o fluxo
	 */
	@Observer(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL)
	@Transactional
	public void iniciarFluxoIncidental(Integer idDocumento) {
		iniciarFluxoIncidental(idDocumento, null);
	}
	
	@Observer(Eventos.INICIAR_FLUXO_PETICAO_INCIDENTAL_COM_VARIAVEIS_EXTRAS)
	@Transactional
	public void iniciarFluxoIncidental(Integer idDocumento, Map<String, Object> variaveisExtras) {
		try {
			ProcessoDocumento documento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).findById(idDocumento);
			documento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).refresh(documento);
			TipoProcessoDocumento tipoDocumento = documento.getTipoProcessoDocumento();
			ProcessoTrf processoTrf = this.findById(documento.getProcesso().getIdProcesso());
			documento.setProcessoTrf(processoTrf);
			
			if (!validarTipoDocumento(documento, tipoDocumento)) {
				return;
			}

			if (validarCriacaoFluxo(documento, tipoDocumento)) {
				Actor actor = Actor.instance();
				if (actor.getId() == null) {
					actor.setId(documento.getUsuarioInclusao().getLogin());
				}
				criarVariaveisFluxo(documento, tipoDocumento, variaveisExtras);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	@Observer(value={Eventos.EVENTO_ENCERRAMENTO_PRAZO_NAO_PROCESSUAL})
	public void observaEncerramentoPrazoNaoProcessual(Integer idProcessoTrf, Date dataExpiracao) {
		sinalizaEncerramentoPrazoNaoProcessual(idProcessoTrf, dataExpiracao);
	}

	public void sinalizaEncerramentoPrazoNaoProcessual(Integer idProcessoTrf, Date dataExpiracao) {
		if (idProcessoTrf == null) {
			return;
		}

		ProcessoTrf processo = ComponentUtil.getComponent(ProcessoTrfManager.class).find(ProcessoTrf.class,
				idProcessoTrf);

		if (processo == null) {
			log.error(
					"Erro ao tentar observar o encerramento do prazo não processual do processo com identificador {0}.",
					idProcessoTrf);
			return;
		}

		List<VariavelSinalizacaoFluxoVO> listaVariaveis = new ArrayList<VariavelSinalizacaoFluxoVO>();

		VariavelSinalizacaoFluxoVO variavelAguardandoPrazo = new VariavelSinalizacaoFluxoVO(
				Variaveis.NOME_VARIAVEL_AGUARDANDO_PRAZO, true, Operator.equals, true, true);
		listaVariaveis.add(variavelAguardandoPrazo);
		VariavelSinalizacaoFluxoVO variavelSinalizacaoPrazo = new VariavelSinalizacaoFluxoVO(
				Variaveis.NOME_VARIAVEL_DIA_PRAZO, dataExpiracao, Operator.lessOrEquals, false, true);
		listaVariaveis.add(variavelSinalizacaoPrazo);

		sinalizarFluxo(processo, listaVariaveis, null, null);
	}

	private Boolean validarCriacaoFluxo(ProcessoDocumento documento,
			TipoProcessoDocumento tipoDocumento) throws PJeBusinessException {
		boolean criarFluxo = false;
		String parametro = ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.SEMPRE_DISPARAR_FLUXO_INCIDENTAL);
		if (StringUtil.isNotEmpty(parametro) && ProjetoUtil.compareObjects(parametro, true, Operator.equals)) {
			criarFluxo = true;
		}
		else if (StringUtil.isNotEmpty(tipoDocumento.getVariavelFluxo())) {
			boolean existeFluxo = ComponentUtil.getComponent(FluxoManager.class).existeFluxoComVariavel(documento.getProcessoTrf(), tipoDocumento.getVariavelFluxo());
			criarFluxo = !existeFluxo;
		}
		
		return criarFluxo;
	}

	private Boolean validarTipoDocumento(ProcessoDocumento documento, TipoProcessoDocumento tipoDocumento) {
		if (documento.getProcessoTrf() == null || !ProcessoStatusEnum.D.equals(documento.getProcessoTrf().getProcessoStatus()) || 
				tipoDocumento == null || tipoDocumento.getFluxo() == null) {
			
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * Método responsável por iniciar o fluxo de notificação de substituição de advogado por meio de uma habilitação nos autos.
	 * 
	 * O código do fluxo deverá ser informado por meio do parâmetro: pje:fluxo:notificarHabilitacaoAutos
	 * 
	 * @param idProcessoTrf Id do processo ao qual será associado o fluxo
	 * @param idAdvogado Id do advogado que foi substituído e que deverá ser notificado.
	 */
	@Observer(value={Eventos.INICIAR_FLUXO_NOTIFICAR_HABILITACAO_AUTOS})
	@Transactional
	public void iniciarFluxoNotificarHabilitacaoAutos(Integer idProcessoTrf, Integer idAdvogado){
		try {
			boolean criarFluxo = false;
			ProcessoTrf processoTrf = EntityUtil.getEntityManager().getReference(ProcessoTrf.class, idProcessoTrf);
			
			
			String codigoFluxo = ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.PJE_FLUXO_NOTIFICAR_HABILITACAO_AUTOS);
			
			if (StringUtil.isNotEmpty(codigoFluxo)) {
				criarFluxo = true;
			}
			
			if(criarFluxo){
				Map<String, Object> vars = new HashMap<String, Object>();

				vars.put("idAvogadoNotificar", idAdvogado);
				vars.put("processo", processoTrf.getIdProcessoTrf());
				
				
				incluirNovoFluxo(processoTrf, codigoFluxo, null, null, null, true, vars);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Método responsável por criar as variáveis de fluxo e incluí-las ao novo
	 * fluxo de Petição Incidental.
	 * 
	 * @param documento Documento ao qual será associado o fluxo
	 * @param tipoDocumento Tipo de documento ao qual será associado o fluxo
	 * @throws PJeBusinessException
	 */
	private void criarVariaveisFluxo(ProcessoDocumento documento, TipoProcessoDocumento tipoDocumento, Map<String, Object> variaveisExtras) throws PJeBusinessException {
		Map<String, Object> vars = new HashMap<String, Object>();
		if (tipoDocumento.getVariavelFluxo() != null && !tipoDocumento.getVariavelFluxo().isEmpty()) {
			vars.put(tipoDocumento.getVariavelFluxo(), true);
		}
		vars.put(Variaveis.VARIAVEL_FLUXO_PETICAO_INCIDENTAL, documento.getIdProcessoDocumento());
		vars.put("processo", documento.getProcessoTrf().getIdProcessoTrf());
		if (!CollectionUtilsPje.isEmpty(variaveisExtras)) vars.putAll(variaveisExtras);
		incluirNovoFluxo(documento.getProcessoTrf(), documento.getTipoProcessoDocumento().getFluxo().getCodFluxo(), null, null, null, true, vars);
	}
	
	
 
	
	/**
	 * Funciona da mesma forma que <code>sinalizarFluxo(ProcessoTrf, String, Object, boolean, boolean, Map<String, Object>, Integer)</code> 
	 * Exceto que o mapa com novas variáveis e o id do órgão julgador são assumidos como nulos.  
	 * @see #sinalizarFluxo(ProcessoTrf, String, Object, boolean, boolean, Map<String, Object>, Integer)	
	 * */
	public void sinalizarFluxo(final ProcessoTrf processo, final String nomeVariavel, final Object valorEsperado, final boolean limitarTarefa, final boolean apagarVariavel){
		sinalizarFluxo(processo, nomeVariavel, valorEsperado, limitarTarefa, apagarVariavel, null, null);
	}

	
	/**
	 * Funciona da mesma forma que <code>sinalizarFluxo(ProcessoTrf, String, Object, boolean, boolean, Map<String, Object>, Integer)</code> 
	 * Exceto que o id do órgão julgador é assumido como nulo.  
	 * @see #sinalizarFluxo(ProcessoTrf, String, Object, boolean, boolean, Map<String, Object>, Integer)	
	 * */
	public void sinalizarFluxo(final ProcessoTrf processo, final String nomeVariavel, final Object valorEsperado, final boolean limitarTarefa, final boolean apagarVariavel, Map<String, Object> novasVariaveis){
		sinalizarFluxo(processo, nomeVariavel, valorEsperado, limitarTarefa, apagarVariavel, novasVariaveis, null);
	}

	public void sinalizarFluxo(final ProcessoTrf processo, final String nomeVariavel, final Object valorEsperado, final boolean limitarTarefa, final boolean apagarVariavel, Map<String, Object> novasVariaveis, Integer idOrgaoJulgador){
		VariavelSinalizacaoFluxoVO variavelSinalizacao = new VariavelSinalizacaoFluxoVO(nomeVariavel, valorEsperado, Operator.equals, limitarTarefa, apagarVariavel);
		List<VariavelSinalizacaoFluxoVO> lista = new ArrayList<VariavelSinalizacaoFluxoVO>();
		lista.add(variavelSinalizacao);
		this.sinalizarFluxo(processo, lista, novasVariaveis, idOrgaoJulgador);
	}

	/**
	 * Recupera todas as instâncias de fluxo ativas de um dado processo judicial e sinaliza para o prosseguimento no fluxo
	 * caso seja identificada a variável com o nome e valor dados. 
	 * A variável poderá ser recuperada da tarefa atual, se existente, ou da instância do fluxo, se indicada que a pesquisa deve
	 * abranger o contexto de fluxo.
	 * 
	 * @param processo o processo judicial objeto da verificação.
	 * @param variaveisSinalizacao 
	 * - nomeVariavel o nome da variável a ser pesquisada -- a primeira variável da lista será utilizada como nome da variavel de sinalizacao ao final do processo
	 * - valorEsperado o valor esperado para que haja a sinallização
	 * - operador identifica qual o operador será utilizado na comparação da variável e o valorEsperado, o default é: "equals", outros valores: "less", "lessOrEquals", "greater", "greaterOrEquals"
	 * - limitarTarefa marca indicativa de que o método deve procurar a variável apenas em eventual instância da tarefa
	 * - apagarVariavel marca indicativa de que, havendo sucesso na sinalização, a variável deve ser apagada
	 * @param novasVariaveis variáveis a serem incluídas nas PIs que se subsumirem ao caso
	 * @param idOrgaoJulgador atua somente em instâncias de fluxo relacionadas ao órgão julgador em questão
	 */
	public void sinalizarFluxo(final ProcessoTrf processo, List<VariavelSinalizacaoFluxoVO> variaveisSinalizacao, Map<String, Object> novasVariaveis, Integer idOrgaoJulgador) {
	    if (processo == null || CollectionUtilsPje.isEmpty(variaveisSinalizacao)) {
	        log.warn("Sinalização de fluxo descartada por erro nos parâmetros: processo ou variáveis inválidos.");
	        return;
	    }

	    Integer idProcesso = null;
	    try {
	        idProcesso = processo.getIdProcessoTrf();
	    } catch (Exception e) {
	        log.error("Erro ao obter o ID do processo. Processo inválido.", e);
	        return;
	    }

	    for (VariavelSinalizacaoFluxoVO variavel : variaveisSinalizacao) {
	        if (variavel == null || !variavel.isParametrosValidos()) {
	            log.warn("Sinalização de fluxo para o processo de id {0} descartada por erro nos parâmetros da variável.", idProcesso);
	            return;
	        }
	    }

	    List<Integer> idsLocalizacoes = null;
	    if (idOrgaoJulgador != null) {
	        try {
	            OrgaoJulgadorManager ojManager = (OrgaoJulgadorManager) Component.getInstance(OrgaoJulgadorManager.class);
	            OrgaoJulgador oj = ojManager.findById(idOrgaoJulgador);
	            if (oj == null || oj.getLocalizacao() == null) {
	                throw new PJeBusinessException("Órgão julgador ou localização inexistente.");
	            }

	            Integer idLocalizacao = oj.getLocalizacao().getIdLocalizacao();
	            LocalizacaoManager localizacaoManager = (LocalizacaoManager) Component.getInstance(LocalizacaoManager.class);
	            List<Localizacao> localizacaoFisicaList = localizacaoManager.getArvoreDescendente(idLocalizacao, true);
	            if (localizacaoFisicaList != null) {
	                String idsLocalizacoesFisicas = LocalizacaoUtil.converteLocalizacoesList(localizacaoFisicaList);
	                idsLocalizacoes = CollectionUtilsPje.convertStringToIntegerList(idsLocalizacoesFisicas);
	            }
	        } catch (PJeBusinessException e) {
	            log.error("Erro ao obter localizações físicas para o órgão julgador de id {0}: {1}", idOrgaoJulgador, e.getLocalizedMessage());
	        } catch (Exception e) {
	            log.error("Erro inesperado ao processar localizações para o órgão julgador de id {0}: {1}", idOrgaoJulgador, e.getLocalizedMessage());
	        }
	    }

	    List<Long> pis = null;
	    try {
	        pis = getProcessoJudicialManager().getBusinessProcessIds(processo, idsLocalizacoes);
	    } catch (Exception e) {
	        log.error("Erro ao obter os IDs de processos judiciais para o processo de id {0}.", idProcesso, e);
	        return;
	    }

	    if (pis == null || pis.isEmpty()) {
	        log.warn("Nenhum ID de processo judicial encontrado para o processo de id {0}.", idProcesso);
	        return;
	    }

	    for (Long id : pis) {
	        BusinessProcess bp = null;
	        try {
	            bp = BusinessProcess.instance();
	            bp.resumeProcess(id);
	        } catch (IllegalStateException e) {
	            ProcessInstance process = org.jboss.seam.bpm.ProcessInstance.instance();
	            if (process != null && !process.hasEnded()) {
	                log.error("Erro ao retomar o processo de id {0} no fluxo.", id);
	                throw new NegocioException("Erro na obtenção do arquivo de bundle do Seam.");
	            }
	        } catch (Exception e) {
	            log.error("Erro inesperado ao retomar o processo de id {0} no fluxo.", id, e);
	            continue;
	        }

	        ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance.instance();
	        if (pi == null || pi.hasEnded()) {
	            log.error("ProcessInstance {0} não encontrado ou já encerrado.", id);
	            continue;
	        }

	        Collection<TaskInstance> tasks = null;
	        try {
	            tasks = pi.getTaskMgmtInstance() != null ? pi.getTaskMgmtInstance().getTaskInstances() : null;
	        } catch (Exception e) {
	            log.warn("Erro ao obter as tarefas do processo de id {0}.", id, e);
	            continue;
	        }

	        if (tasks == null || tasks.isEmpty()) {
	            continue;
	        }

	        Collection<TaskInstance> openTasks = tasks.stream().filter(TaskInstance::isOpen).collect(Collectors.toList());
	        if (openTasks.isEmpty()) {
	            continue;
	        }

	        Token tk = null;
	        try {
	            tk = pi.getRootToken();
	            if (tk == null || tk.getAvailableTransitions().isEmpty()) {
	                continue;
	            }
	        } catch (Exception e) {
	            log.warn("Erro ao verificar transições disponíveis para o token no processo de id {0}.", id);
	            continue;
	        }

	        boolean houveTramitacao = false;
	        int numVariaveisSinalizacao = variaveisSinalizacao.size();
	        for (TaskInstance ti : openTasks) {
	            try {
	                JbpmUtil.restaurarVariaveis(ti);
	            } catch (Exception e) {
	                log.warn("Erro ao restaurar variáveis da tarefa no processo de id {0}.", id, e);
	                continue;
	            }

	            int numVariaveisSinalizacaoEncontradas = 0;

	            for (VariavelSinalizacaoFluxoVO variavel : variaveisSinalizacao) {
	                if (variavel == null) {
	                    continue;
	                }

	                Object valor = null;
	                try {
	                    valor = ti.getVariableLocally(variavel.getNomeVariavel());
	                    if (valor == null && !variavel.isLimitarTarefa()) {
	                        valor = pi.getContextInstance().getVariable(variavel.getNomeVariavel());
	                    }
	                } catch (Exception e) {
	                    log.warn("Erro ao obter valor da variável {0} no processo de id {1}.", variavel.getNomeVariavel(), id, e);
	                }

	                if (valor != null && ProjetoUtil.compareObjects(valor, variavel.getValorEsperado(), variavel.getOperador())) {
	                    numVariaveisSinalizacaoEncontradas++;
	                }
	            }

	            if (numVariaveisSinalizacaoEncontradas == numVariaveisSinalizacao) {
	                try {
	                    tk = ti.getToken();
	                    if (tk == null || tk.getAvailableTransitions().isEmpty()) {
	                        continue;
	                    }
	                } catch (Exception e) {
	                    log.warn("Erro ao verificar transições disponíveis para o token da tarefa no processo de id {0}.", id, e);
	                    continue;
	                }

	                String transition = null;
	                try {
	                    transition = (String) ti.getVariableLocally(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
	                } catch (Exception e) {
	                    log.warn("Transição padrão não é uma string válida no processo de id {0}.", id, e);
	                }

	                for (VariavelSinalizacaoFluxoVO variavel : variaveisSinalizacao) {
	                    if (variavel != null && variavel.isApagarVariavel()) {
	                        try {
	                            ti.deleteVariableLocally(variavel.getNomeVariavel());
	                        } catch (Exception e) {
	                            log.warn("Erro ao apagar a variável {0} no processo de id {1}.", variavel.getNomeVariavel(), id, e);
	                        }
	                    }
	                }

	                try {
	                    boolean sinalizar = false;
	                    Transition noSaida = null;
	                    if (transition != null) {
	                        for (Transition t : tk.getAvailableTransitions()) {
	                            if (transition.equals(t.getName())) {
	                                sinalizar = true;
	                                noSaida = t;
	                                break;
	                            }
	                        }
	                    } else {
	                        sinalizar = true;
	                    }

	                    if (sinalizar) {
	                        if (!houveTramitacao) {
	                            ajustarVariaveisFluxo(pi, novasVariaveis, variaveisSinalizacao);
	                        }
	                        JbpmUtil.desbloqueia(tk);
	                        if (noSaida != null) {
	                            tk.signal(noSaida);
	                        } else {
	                            tk.signal();
	                        }
	                        houveTramitacao = true;
	                    }
	                } catch (Exception e) {
	                    log.error("Não foi possível sinalizar o prosseguimento da tramitação do processo de id {0}: {1}", idProcesso, e.getLocalizedMessage());
	                    JbpmUtil.clearWithoutClose(ManagedJbpmContext.instance());
	                    throw new RuntimeException("Erro durante a sinalização do fluxo: " + e.getLocalizedMessage(), e);
	                }
	            }
	        }
	    }
	}

	/**
	 * Método responsável por realizar o ajuste das variáveis no fluxo, apagando
	 * e setando novas variáveis se estas forem informadas.
	 * 
	 * @param pi                  {@link ProcessInstance} corrente que terá suas variáveis
	 *                            ajustadas.
	 * @param novasVariaveis      um {@link Map} contendo o nome da variável ({@link String}) e
	 *                            seu valor ({@link Object}) a ser gravado no
	 *                            {@link ProcessInstance} informado.
	 * @param variaveisSinalizacao Lista de {@link VariavelSinalizacaoFluxoVO} com informações de
	 *                            variáveis a serem ajustadas no fluxo.
	 */
	private void ajustarVariaveisFluxo(ProcessInstance pi, Map<String, Object> novasVariaveis,
	                                   List<VariavelSinalizacaoFluxoVO> variaveisSinalizacao) {

	    if (pi == null) {
	        log.warn("ProcessInstance é nulo. Ajuste de variáveis não será realizado.");
	        return;
	    }

	    if (CollectionUtilsPje.isNotEmpty(variaveisSinalizacao)) {
	        VariavelSinalizacaoFluxoVO primeiraVariavel = variaveisSinalizacao.get(0);
	        if (primeiraVariavel != null && primeiraVariavel.getNomeVariavel() != null) {
	            String nomeVariavelPrincipal = primeiraVariavel.getNomeVariavel();

	            pi.getContextInstance().setVariable(Eventos.EVENTO_SINALIZACAO, nomeVariavelPrincipal);
	        } else {
	            log.warn("Primeira variável de sinalização está nula ou sem nome. EVENTO_SINALIZACAO não foi configurado.");
	        }

	        for (VariavelSinalizacaoFluxoVO variavel : variaveisSinalizacao) {
	            if (variavel != null && variavel.getNomeVariavel() != null) {
	                if (!variavel.isLimitarTarefa() && variavel.isApagarVariavel()) {
	                    try {
	                        pi.getContextInstance().deleteVariable(variavel.getNomeVariavel());
	                        log.debug("Variável {} apagada com sucesso.", variavel.getNomeVariavel());
	                    } catch (Exception e) {
	                        log.error("Erro ao apagar a variável {} no fluxo: {}", variavel.getNomeVariavel(), e.getMessage());
	                    }
	                }
	            } else {
	                log.warn("Variável de sinalização nula ou sem nome encontrada na lista. Ignorando.");
	            }
	        }
	    }

	    if (novasVariaveis != null && !novasVariaveis.isEmpty()) {
	        try {
	            pi.getContextInstance().addVariables(novasVariaveis);
	            log.debug("Novas variáveis adicionadas ao fluxo: {}", novasVariaveis.keySet());
	        } catch (Exception e) {
	            log.error("Erro ao adicionar novas variáveis ao fluxo: {}", e.getMessage());
	        }
	    } else {
	        log.debug("Nenhuma nova variável foi informada para adicionar ao fluxo.");
	    }
	}
		
	/**
	 * @see #sinalizarFluxo(ProcessoTrf, String, Object, boolean, boolean, Map)
	 * 
	 * @param novasVariaveis
	 *            uma string contendo o nome das variáveis e seu valor separado
	 *            por '=' (igual) ex:. 'idDocumento=50,pje:nomeRelator=João da Silva,idProcessoTrf=444'
	 * 
	 *            Caso o valor da variável seja um valor número, seu valor será
	 *            convertido para Long, caso contrário será mantido como string
	 */
	public void sinalizarFluxo(final ProcessoTrf processo,
			final String nomeVariavel, final Object valorEsperado,
			final boolean limitarTarefa, final boolean apagarVariavel,
			String novasVariaveis) {
		
		Map<String, Object> mapaNovasVariaveis = CollectionUtilsPje.stringToMap(novasVariaveis);
		sinalizarFluxo(processo, nomeVariavel, valorEsperado, limitarTarefa, apagarVariavel, mapaNovasVariaveis);
		
	}
	
	// Depreciado por o comportamento ser diverso do efetivamente esperado. Veja sinalizarFluxo
	@Deprecated
	public void transitaParaProximaTarefa(final ProcessoTrf processo, final boolean utilizaVariavelTarefa, final String variavel) {
		try {
			//Cria um trabalho transacional tratando o contexto bpm
			new CustomTransactionalWork<Void>(true) {

				@Override
				protected Void work() {
					return transitarParaProximaTarefa(processo,
							utilizaVariavelTarefa, variavel);
				}

				private Void transitarParaProximaTarefa(final ProcessoTrf processo, final boolean utilizaVariavelTarefa, final String variavel) {
					List<Long> idsProcessInstances = ComponentUtil.getComponent(ProcessoJudicialManager.class).getBusinessProcessIds(processo);

					for (Long id : idsProcessInstances) {
						BusinessProcess bp = BusinessProcess.instance();

						try {
							bp.resumeProcess(id.longValue());
						} catch (IllegalStateException e) {
							ProcessInstance process = org.jboss.seam.bpm.ProcessInstance.instance();

							if (!process.hasEnded()) {
								e.printStackTrace();
								throw new NegocioException("Erro na obtenção do arquivo de bundle do Seam.");
							}
						}

						ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance
								.instance();
						TaskInstance ti = null;
						Collection<TaskInstance> taskInstances = pi
								.getTaskMgmtInstance().getTaskInstances();
						String variavelTarefa = null;

						if (utilizaVariavelTarefa) {
							variavelTarefa = (String) pi.getContextInstance()
									.getVariable(variavel);
						}

						if (utilizaVariavelTarefa
								&& (variavelTarefa == null || variavelTarefa
										.equals("false"))) {
							continue;
						}

						if (taskInstances == null) {
							continue;
						}

						for (TaskInstance taskInstance : taskInstances) {
							if (taskInstance.isOpen()) {
								ti = taskInstance;
								break;
							}
						}

						if (ti == null) {
							continue;
						}

						String defaultTransition = null;
						if (ti != null) {
							defaultTransition = (String) ti
									.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
						}

						Token tk = pi.getRootToken();
						if (!tk.getProcessInstance().hasEnded()
								&& tk.getAvailableTransitions().size() > 0) {
							if (defaultTransition != null) {
								for (Transition t : tk.getAvailableTransitions()) {
									if (t.getName().equals(defaultTransition)) {
										FluxoService fluxoService = (FluxoService) Component.getInstance(FluxoService.class, true);
										fluxoService
												.iniciarHomesProcessos(processo);
										fluxoService.iniciarBusinessProcess(ti
												.getId());
										JbpmUtil.desbloqueia(tk);
										tk.signal(t);
										break;
									}
								}
							}
						}
					}

					return null;
				}

			}.workInTransaction();
		} catch (Exception e) {
			log.error("Erro ao transitar processo: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Observer(value={Eventos.EVENTO_ACORDAO_GERADO})
	public void observaAcordaoGerado(ProcessoDocumento doc) throws PJeBusinessException{
		ProcessoTrf processoJudicial = EntityUtil.getEntityManager().getReference(ProcessoTrf.class, doc.getProcesso().getIdProcesso());
 		Map<String, Object> variavel = new HashMap<String,Object>(0);
 		variavel.put(Variaveis.ATO_PROFERIDO, doc.getIdProcessoDocumento());
 		variavel.put(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, doc.getIdProcessoDocumento());
		
		sinalizarFluxo(processoJudicial, "pje:aguardaJulgamentoColegiado", true, false, true,variavel);
 
	}
	
	public boolean temAtosComunicacaoPendentes(ProcessoTrf processoJudicial){
		return false;
	}
	
	public void incluirNovoFluxo(ProcessoTrf processoJudicial, String codigoFluxo) throws PJeBusinessException {
	    incluirNovoFluxo(processoJudicial, codigoFluxo, null, null, null, true);
	}

	public void incluirNovoFluxo(int idProcessoJudicial, String codigoFluxo) throws PJeBusinessException {
	    incluirNovoFluxo(idProcessoJudicial, codigoFluxo, null, null, null, true);
	}

	public void incluirNovoFluxo(ProcessoTrf processoJudicial, String codigoFluxo, Map<String, Object> variables) throws PJeBusinessException {
	    incluirNovoFluxo(processoJudicial, codigoFluxo, null, null, null, true, variables);
	}

	public void incluirNovoFluxo(int idProcessoJudicial, String codigoFluxo, Map<String, Object> variables) throws PJeBusinessException {
	    incluirNovoFluxo(idProcessoJudicial, codigoFluxo, null, null, null, true, variables);
	}

	public void incluirNovoFluxo(ProcessoTrf processoJudicial, String codigoFluxo, 
	        Integer idOrgaoJulgador, Integer idOrgaoJulgadorCargo, Integer idOrgaoJulgadorColegiado, boolean flush) throws PJeBusinessException {
	    incluirNovoFluxo(processoJudicial, codigoFluxo, idOrgaoJulgador, idOrgaoJulgadorCargo, idOrgaoJulgadorColegiado, flush, new HashMap<>(0));
	}

	public void incluirNovoFluxo(int idProcessoJudicial, String codigoFluxo, 
	        Integer idOrgaoJulgador, Integer idOrgaoJulgadorCargo, Integer idOrgaoJulgadorColegiado, boolean flush) throws PJeBusinessException {
	    incluirNovoFluxo(idProcessoJudicial, codigoFluxo, idOrgaoJulgador, idOrgaoJulgadorCargo, idOrgaoJulgadorColegiado, flush, new HashMap<>(0));
	}
	
	// MÉTODO DUPLICADO, QUALQUER ALTERAÇÃO, REPLICAR NO MÉTODO ABAIXO
	public void incluirNovoFluxo(int idProcessoJudicial, String codigoFluxo, Integer idOrgaoJulgador, 
	        Integer idOrgaoJulgadorCargo, Integer idOrgaoJulgadorColegiado, boolean flush,Map<String,Object> variables) throws PJeBusinessException {
	     
	    Fluxo fluxo = ComponentUtil.getComponent(FluxoService.class).findByCodigo(codigoFluxo);
	    if (fluxo == null) {
	        throw new PJeBusinessException("pje.fluxo.inexistente");
	    }

	    Map<String, Object> startState = new HashMap<>();
	    startState.put(Variaveis.VARIAVEL_PROCESSO, idProcessoJudicial);
	    if (variables != null && !variables.isEmpty()) {
	    	startState.putAll(variables);
	    }
	    Contexts.getEventContext().set(Variaveis.PJE_FLUXO_VARIABLES_STARTSTATE , startState);
	    
	    BusinessProcess.instance().createProcess(fluxo.getFluxo().trim());
	    ProcessoInstance instanciaLocal = ProcessoHome.insereProcessoInstance(
	            idProcessoJudicial, BusinessProcess.instance().getProcessId());

	    instanciaLocal.setIdLocalizacao(getIdLocalizacao(idOrgaoJulgador));
	    instanciaLocal.setOrgaoJulgadorCargo((idOrgaoJulgadorCargo == null || idOrgaoJulgadorCargo == 0) ? instanciaLocal.getOrgaoJulgadorCargo() : idOrgaoJulgadorCargo);
	    instanciaLocal.setOrgaoJulgadorColegiado((idOrgaoJulgadorColegiado == null || idOrgaoJulgadorColegiado == 0) ? instanciaLocal.getOrgaoJulgadorColegiado() : idOrgaoJulgadorColegiado);
	    instanciaLocal.setAtivo(Boolean.TRUE);

		ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance.instance();

		if (pi != null && pi.getId() == instanciaLocal.getIdProcessoInstance() && pi.getEnd() != null) {
			instanciaLocal.setAtivo(Boolean.FALSE);
		}

	    if (flush) {
	        ComponentUtil.getComponent(ProcessoJudicialManager.class).flush();
	    }
	}
	
	// MÉTODO DUPLICADO, QUALQUER ALTERAÇÃO, REPLICAR NO MÉTODO ACIMA
	public void incluirNovoFluxo(ProcessoTrf processoJudicial, String codigoFluxo, Integer idOrgaoJulgador, 
	        Integer idOrgaoJulgadorCargo, Integer idOrgaoJulgadorColegiado, boolean flush,Map<String,Object> variables) throws PJeBusinessException {
		incluirNovoFluxo(processoJudicial, codigoFluxo, idOrgaoJulgadorCargo, idOrgaoJulgadorColegiado, flush, variables, getIdLocalizacao(idOrgaoJulgador));
	}
	
	public void incluirNovoFluxo(ProcessoTrf processoJudicial, String codigoFluxo, Integer idOrgaoJulgadorCargo, Integer idOrgaoJulgadorColegiado, boolean flush,Map<String,Object> variables, Integer idLocalizacao) throws PJeBusinessException {  
	    FluxoService fluxoService = (FluxoService) Component.getInstance(FluxoService.class, true);
	    Fluxo fluxo = fluxoService.findByCodigo(codigoFluxo);
	    if (fluxo == null) {
	        throw new PJeBusinessException("pje.fluxo.inexistente");
	    }

	    Map<String, Object> startState = new HashMap<>();
	    startState.put(Variaveis.VARIAVEL_PROCESSO, processoJudicial.getIdProcessoTrf());
	    if (variables != null && !variables.isEmpty()) {
	    	startState.putAll(variables);
	    }
	    Contexts.getEventContext().set(Variaveis.PJE_FLUXO_VARIABLES_STARTSTATE , startState);
	    
	    BusinessProcess.instance().createProcess(fluxo.getFluxo().trim());
	    ProcessoInstance instanciaLocal = ProcessoHome.insereProcessoInstance(

	    processoJudicial.getIdProcessoTrf(), BusinessProcess.instance().getProcessId());

	    instanciaLocal.setIdLocalizacao((idLocalizacao == null || idLocalizacao == 0) ? instanciaLocal.getIdLocalizacao() : idLocalizacao);
	    instanciaLocal.setOrgaoJulgadorCargo((idOrgaoJulgadorCargo == null || idOrgaoJulgadorCargo == 0) ? instanciaLocal.getOrgaoJulgadorCargo() : idOrgaoJulgadorCargo);
	    instanciaLocal.setOrgaoJulgadorColegiado((idOrgaoJulgadorColegiado == null || idOrgaoJulgadorColegiado == 0) ? instanciaLocal.getOrgaoJulgadorColegiado() : idOrgaoJulgadorColegiado);
	    instanciaLocal.setAtivo(Boolean.TRUE);
	    if (flush) {
	        ComponentUtil.getComponent(ProcessoJudicialManager.class).flush();
	    }
	}
	
	private Integer getIdLocalizacao(Integer idOrgaoJulgador) throws PJeBusinessException {
	    OrgaoJulgador oj = (idOrgaoJulgador == null || idOrgaoJulgador == 0) ? null : 
    		ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(idOrgaoJulgador);
    
	    Integer idLocalizacaoOJ = (oj == null || oj.getLocalizacao() == null) ? null : oj.getLocalizacao().getIdLocalizacao();
		
	    return idLocalizacaoOJ;
	}
	
	@Deprecated
	/***
	 * A função de deslocamento de orgaos julgadores de processos não deve mais ser utilizada, para fazer as atividades relacionadas a isso, utilize o deslocamento de fluxo ou a vinculação
	 * dos processos a outros cargos dentro do mesmo órgão julgador
	 * 
	 * @param idOrgaoJulgadorDestino
	 * @throws PJeBusinessException
	 */
	public void deslocarOrgaoJulgador(Integer idOrgaoJulgadorDestino) throws PJeBusinessException {
		try {
			//Obtem o processo trf corrente
			ProcessoTrf processoTrf = ComponentUtil.getComponent(ProcessoTrfHome.class).getInstance();
			if(processoTrf == null){
				log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Não foi possível identificar o processo a ser deslocado.");
				throw new PJeBusinessException("Não foi possível identificar o processo a ser deslocado.");
			}
			
			OrgaoJulgador orgaoJulgadorOrigem = processoTrf.getOrgaoJulgador();
			
			//Obtem OrgaoJulgadorColegiado e OrgaoJulgadorCargo da origem
			OrgaoJulgadorColegiado orgaoJulgadorColegiadoOrigem = processoTrf.getOrgaoJulgadorColegiado();
			Integer idOrgaoJulgadorColegiadoOrigem = null;
			if(orgaoJulgadorColegiadoOrigem != null){
				idOrgaoJulgadorColegiadoOrigem = orgaoJulgadorColegiadoOrigem.getIdOrgaoJulgadorColegiado();
			}
			OrgaoJulgadorCargo orgaoJulgadorCargoOrigem = processoTrf.getOrgaoJulgadorCargo();
			
			log.info("[DESLOCAMENTO ORGAO JULGADOR - INFO] OrgaoJulgador id: " + ((orgaoJulgadorOrigem != null) ? orgaoJulgadorOrigem.getIdOrgaoJulgador() : "null") + 
					 " OrgaoJulgadorColegiado id: " + ((orgaoJulgadorColegiadoOrigem != null) ? orgaoJulgadorColegiadoOrigem.getIdOrgaoJulgadorColegiado() : "null")  +
					 " e OrgaoJulgadorCargo id: " + ((orgaoJulgadorCargoOrigem != null) ? orgaoJulgadorCargoOrigem.getIdOrgaoJulgadorCargo() : "null")  + " de origem obtidos com sucesso");
			
			//Obtem OrgaoJulgadorColegiado e OrgaoJulgadorCargo do destino
			OrgaoJulgador orgaoJulgadorDestino = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(idOrgaoJulgadorDestino);
			if(orgaoJulgadorDestino == null){
				log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Não foi possível recuperar o órgão julgador de destino.");
				throw new PJeBusinessException("Não foi possível recuperar o órgão julgador de destino.");
			}
			OrgaoJulgadorColegiado orgaoJulgadorColegiadoDestino = null;
			
			List<OrgaoJulgadorColegiadoOrgaoJulgador> orgaoJulgadorColegiadoDestinoList = orgaoJulgadorDestino.getOrgaoJulgadorColegiadoOrgaoJulgadorList();
		
			if(orgaoJulgadorColegiadoDestinoList != null && idOrgaoJulgadorColegiadoOrigem != null){
				for(OrgaoJulgadorColegiadoOrgaoJulgador orgaoJulgadorColegiadoDestinoAux: orgaoJulgadorColegiadoDestinoList){
					if(orgaoJulgadorColegiadoDestinoAux.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() == idOrgaoJulgadorColegiadoOrigem){
						orgaoJulgadorColegiadoDestino = orgaoJulgadorColegiadoDestinoAux.getOrgaoJulgadorColegiado();
						break;
					}
				}
			}
			
			OrgaoJulgadorCargo orgaoJulgadorCargoDestino = null;
			List<OrgaoJulgadorCargo> orgaoJulgadorCargoDestinoList = orgaoJulgadorDestino.getOrgaoJulgadorCargoList();
			if(orgaoJulgadorCargoDestinoList != null && orgaoJulgadorCargoOrigem != null && orgaoJulgadorCargoOrigem.getCargo() != null){
				if(orgaoJulgadorCargoDestinoList.size() == 1){
					orgaoJulgadorCargoDestino = orgaoJulgadorCargoDestinoList.get(0);
				}
				else{
					for(OrgaoJulgadorCargo orgaoJulgadorCargoDestinoAux: orgaoJulgadorCargoDestinoList){
						if(orgaoJulgadorCargoDestinoAux.getCargo() != null && orgaoJulgadorCargoDestinoAux.getCargo().getCargo().equalsIgnoreCase(orgaoJulgadorCargoOrigem.getCargo().getCargo())){
							orgaoJulgadorCargoDestino = orgaoJulgadorCargoDestinoAux;
							break;
						}
					}
				}
					
			}
			
			log.info("[DESLOCAMENTO ORGAO JULGADOR - INFO] OrgaoJulgador id: " + ((orgaoJulgadorDestino != null) ? orgaoJulgadorDestino.getIdOrgaoJulgador() : "null") + 
					 " OrgaoJulgadorColegiado id: " + ((orgaoJulgadorColegiadoDestino != null) ? orgaoJulgadorColegiadoDestino.getIdOrgaoJulgadorColegiado() : "null")  +
					 " e OrgaoJulgadorCargo id: " + ((orgaoJulgadorCargoDestino != null) ? orgaoJulgadorCargoDestino.getIdOrgaoJulgadorCargo() : "null")  + " de destino obtidos com sucesso");
	
			trocaOrgaoJulgador(processoTrf, orgaoJulgadorDestino, orgaoJulgadorColegiadoDestino, orgaoJulgadorCargoDestino);

			registrarHistoricoDeslocamento(processoTrf, 
					orgaoJulgadorOrigem, orgaoJulgadorColegiadoOrigem,	orgaoJulgadorCargoOrigem, 
					orgaoJulgadorDestino, orgaoJulgadorColegiadoDestino, orgaoJulgadorCargoDestino);
			
			ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processoTrf);
			
			log.info("[DESLOCAMENTO ORGAO JULGADOR - INFO] Troca do orgão julgador com id origem: "  + ((orgaoJulgadorOrigem != null)?orgaoJulgadorOrigem.getIdOrgaoJulgador():"null") + " para o id destino: " +
					 ((orgaoJulgadorDestino != null)?orgaoJulgadorDestino.getIdOrgaoJulgador():"null") + " referente ao processo: " + ((processoTrf !=null)?processoTrf.getNumeroProcesso():"null") + " efetuada com sucesso");
		
		} catch (Exception e){
			log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Erro ao efetuar a troca do orgão julgador de destino com Id: " + idOrgaoJulgadorDestino, e);
			throw new PJeBusinessException("Erro ao efetuar a troca do orgão julgador de destino com Id: " + idOrgaoJulgadorDestino, e);
		}
	}

	/**
	 * Método responsável por registrar o histórico de deslocamento do processo.
	 */
	private void registrarHistoricoDeslocamento(ProcessoTrf processo, OrgaoJulgador orgaoOrigem,
		OrgaoJulgadorColegiado colegiadoOrigem, OrgaoJulgadorCargo cargoOrigem, OrgaoJulgador orgaoDestino,
		OrgaoJulgadorColegiado colegiadoDestino, OrgaoJulgadorCargo cargoDestino) throws PJeException {
	
		//Salva na tabela de historico para que possa ser possível voltar ao orgão julgador original
		
		HistoricoDeslocamentoOrgaoJulgador historicoDeslocamentoOrgaoJulgador = null;
		HistoricoDeslocamentoOrgaoJulgadorManager historicoDeslocamentoOrgaoJulgadorManager = ComponentUtil.getComponent(HistoricoDeslocamentoOrgaoJulgadorManager.class);
		historicoDeslocamentoOrgaoJulgador = historicoDeslocamentoOrgaoJulgadorManager.obterHistoricoSemDatasDefinidas(processo);
		
		//Verifica se já existe uma entrada para o processo corrente com data de retorno nula
		if(historicoDeslocamentoOrgaoJulgador == null){
			if(historicoDeslocamentoOrgaoJulgadorManager.verificaDeslocamentoOrgaoJulgadorEmAndamento(processo)){
				log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Já existe um processo com deslocamento de Orgão Julgador em andamento");
				throw new PJeBusinessException("Já existe um processo com deslocamento de Orgão Julgador em andamento");
			}
			
			historicoDeslocamentoOrgaoJulgador = new HistoricoDeslocamentoOrgaoJulgador();
			historicoDeslocamentoOrgaoJulgador.setProcessoTrf(processo);
		} 
			
		historicoDeslocamentoOrgaoJulgador.setOrgaoJulgadorOrigem(orgaoOrigem);
		historicoDeslocamentoOrgaoJulgador.setOrgaoJulgadorColegiadoOrigem(colegiadoOrigem);
		historicoDeslocamentoOrgaoJulgador.setOrgaoJulgadorCargoOrigem(cargoOrigem);
			
		historicoDeslocamentoOrgaoJulgador.setOrgaoJulgadorDestino(orgaoDestino);
		historicoDeslocamentoOrgaoJulgador.setOrgaoJulgadorColegiadoDestino(colegiadoDestino);
		historicoDeslocamentoOrgaoJulgador.setOrgaoJulgadorCargoDestino(cargoDestino);
			
		historicoDeslocamentoOrgaoJulgador.setDataDeslocamento(new Date());
			
		historicoDeslocamentoOrgaoJulgadorManager.persistAndFlush(historicoDeslocamentoOrgaoJulgador);
	}
	
    /**
     * 
     * Método que altera o orgão julgador do processo corrente, usando para isso o nome do paramâmetro de sistema configurado,
     * que será utilizado para obter o orgão julgador de destino.
     * 
     * @param parametroOrgaoJulgadorDestino - Nome no parametro do sistema a ser utilizado para obter o orgão julgador de destino
     * @throws PJeBusinessException
     */
    public void deslocarOrgaoJulgador(String parametroOrgaoJulgadorDestino) throws PJeBusinessException {
        String idOrgaoJulgadorDestinoString = ParametroUtil.getParametro(parametroOrgaoJulgadorDestino);
        
        //Se o valor do parâmetro não estiver configurado lança uma exceção
        if(idOrgaoJulgadorDestinoString == null){
            
            log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Orgão Julgador de destino não configurado corretamente nos parâmetros do sistema");
            throw new PJeBusinessException("Orgão Julgador de destino não configurado corretamente nos parâmetros do sistema");
        }
            
        log.info("[DESLOCAMENTO ORGAO JULGADOR - INFO] parâmetro Orgão Julgador de destino obtido com sucesso id: " + idOrgaoJulgadorDestinoString);
        Integer idOrgaoJulgadorDestino = Integer.valueOf(idOrgaoJulgadorDestinoString);
        deslocarOrgaoJulgador(idOrgaoJulgadorDestino);
    }
    
	/**
	 * Método que altera o orgão julgador do processo corrente, voltando a apontar para o 
	 * Orgão Julgador Original que utilizava antes de ser deslocado.
	 * 
	 * 
	 * @throws PJeBusinessException
	 */
	public void retornaOrgaoJulgadorDeslocado() throws PJeBusinessException {
		
		try {
		
			//Obtem o processo trf corrente
			ProcessoTrf processoTrf = ComponentUtil.getComponent(ProcessoTrfHome.class).getInstance();
			
			// Obtem historico com data de retorno nula
			HistoricoDeslocamentoOrgaoJulgadorManager historicoDeslocamentoOrgaoJulgadorManager = ComponentUtil.getComponent(HistoricoDeslocamentoOrgaoJulgadorManager.class);
			HistoricoDeslocamentoOrgaoJulgador historicoDeslocamentoOrgaoJulgador = historicoDeslocamentoOrgaoJulgadorManager.obterHistoricoSemDataRetorno(processoTrf); 
			
			if(historicoDeslocamentoOrgaoJulgador == null){
				
				log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Erro ao efetuar o retorno do orgão julgador original, nenhum registro definido para o processo " + processoTrf.getNumeroProcesso());
				throw new PJeBusinessException("Erro ao efetuar o retorno do orgão julgador original, nenhum registro definido para o processo " + processoTrf.getNumeroProcesso());
				
			}
			
			OrgaoJulgador orgaoJulgadorOrigem = historicoDeslocamentoOrgaoJulgador.getOrgaoJulgadorOrigem();
			OrgaoJulgadorColegiado orgaoJulgadorColegiadoOrigem = historicoDeslocamentoOrgaoJulgador.getOrgaoJulgadorColegiadoOrigem();
			OrgaoJulgadorCargo orgaoJulgadorCargoOrigem = historicoDeslocamentoOrgaoJulgador.getOrgaoJulgadorCargoOrigem();
			
			OrgaoJulgador orgaoJulgadorDestino = historicoDeslocamentoOrgaoJulgador.getOrgaoJulgadorDestino();
			
			log.info("[DESLOCAMENTO ORGAO JULGADOR - INFO] OrgaoJulgador id: " + ((orgaoJulgadorOrigem != null) ? orgaoJulgadorOrigem.getIdOrgaoJulgador() : "null") + 
					 " OrgaoJulgadorColegiado id: " + ((orgaoJulgadorColegiadoOrigem != null) ? orgaoJulgadorColegiadoOrigem.getIdOrgaoJulgadorColegiado() : "null")  +
					 " e OrgaoJulgadorCargo id: " + ((orgaoJulgadorCargoOrigem != null) ? orgaoJulgadorCargoOrigem.getIdOrgaoJulgadorCargo() : "null")  + " de retorno obtidos com sucesso");
			
			trocaOrgaoJulgador(processoTrf, orgaoJulgadorOrigem, orgaoJulgadorColegiadoOrigem, orgaoJulgadorCargoOrigem);
			
			historicoDeslocamentoOrgaoJulgador.setDataRetorno(new Date());
			
			//Preencha data de retorno do orgão julgador
			historicoDeslocamentoOrgaoJulgadorManager.persistAndFlush(historicoDeslocamentoOrgaoJulgador);
			ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processoTrf);
			
			log.info("[DESLOCAMENTO ORGAO JULGADOR - INFO] Retorno do orgão julgador com id origem: "  + ((orgaoJulgadorOrigem != null)?orgaoJulgadorOrigem.getIdOrgaoJulgador(): "null") + " cujo id destino era: " +
					((orgaoJulgadorDestino != null)? orgaoJulgadorDestino.getIdOrgaoJulgador():"null") + " referente ao processo: " + ((processoTrf != null)?processoTrf.getNumeroProcesso():"null") + " efetuada com sucesso");
			
		} catch (Exception e){
			
			log.error("[DESLOCAMENTO ORGAO JULGADOR - ERROR] - Erro ao efetuar o retorno do orgão julgador original", e);
			throw new PJeBusinessException("Erro ao efetuar o retorno do orgão julgador original", e);
			
		}
	}
	
	private void trocaOrgaoJulgador(ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgadorDestino,
			OrgaoJulgadorColegiado orgaoJulgadorColegiadoDestino, OrgaoJulgadorCargo orgaoJulgadorCargoDestino){
		//Troca o orgão julgador, que passa a ser o orgão julgador do destino
		processoTrf.setOrgaoJulgador(orgaoJulgadorDestino);
		processoTrf.setOrgaoJulgadorColegiado(orgaoJulgadorColegiadoDestino);
		processoTrf.setOrgaoJulgadorCargo(orgaoJulgadorCargoDestino);
		if(orgaoJulgadorCargoDestino != null){
			processoTrf.setCargo(orgaoJulgadorCargoDestino.getCargo());
		}
	}
	
	public void deslocarOrgaoJulgador(ProcessoTrf processoJudicial, Integer idOrgaoDestino, Integer idOrgaoJulgadorColegiado, Integer idOrgaoJulgadorCargo) throws PJeBusinessException{
		deslocarOrgaoJulgador(processoJudicial, idOrgaoDestino, idOrgaoJulgadorColegiado,idOrgaoJulgadorCargo, false);
	}

	public void deslocarOrgaoJulgador(ProcessoTrf processoJudicial, Integer idOrgaoDestino, Integer idOrgaoJulgadorColegiado, Integer idOrgaoJulgadorCargo, boolean deslocarPeso) throws PJeBusinessException{
		deslocarOrgaoJulgador(processoJudicial, idOrgaoDestino, idOrgaoJulgadorColegiado,idOrgaoJulgadorCargo, deslocarPeso, false);
	}
	
	@Deprecated
	/***
	 * A função de deslocamento de orgaos julgadores de processos não deve mais ser utilizada, para fazer as atividades relacionadas a isso, utilize o deslocamento de fluxo ou a vinculação
	 * dos processos a outros cargos dentro do mesmo órgão julgador
	 * 
	 * @param processoJudicial
	 * @param idOrgaoDestino
	 * @param idOrgaoJulgadorColegiado
	 * @param idOrgaoJulgadorCargo
	 * @param deslocarPeso
	 * @param deslocarFluxos
	 * @throws PJeBusinessException
	 */
	public void deslocarOrgaoJulgador(ProcessoTrf processoJudicial, Integer idOrgaoDestino, Integer idOrgaoJulgadorColegiado, Integer idOrgaoJulgadorCargo, boolean deslocarPeso, boolean deslocarFluxos) throws PJeBusinessException{
		if(idOrgaoDestino != null && idOrgaoJulgadorCargo != null){
			try{
//				OrgaoJulgadorCargo cargoOrigem = processoJudicial.getOrgaoJulgadorCargo();
				OrgaoJulgador orgaoDestino = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(idOrgaoDestino);
				OrgaoJulgadorColegiado orgaoColegiadoDestino = null;
				if (idOrgaoJulgadorColegiado != null) {
					for(OrgaoJulgadorColegiadoOrgaoJulgador ojcoj: orgaoDestino.getOrgaoJulgadorColegiadoOrgaoJulgadorList()){
						if(ojcoj.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() == idOrgaoJulgadorColegiado){
							orgaoColegiadoDestino = ojcoj.getOrgaoJulgadorColegiado();
							break;
						}
					}					
				}
				if(orgaoColegiadoDestino == null && !ParametroUtil.instance().isPrimeiroGrau()){
					// não há o órgão colegiado destino no órgão julgador dado para a instância de 2º grau.
					return;
				}
				OrgaoJulgadorCargo cargoDestino = null;
				for(OrgaoJulgadorCargo ojc: orgaoDestino.getOrgaoJulgadorCargoList()){
					if(ojc.getIdOrgaoJulgadorCargo() == idOrgaoJulgadorCargo){
						cargoDestino = ojc;
						break;
					}
				}
				if(cargoDestino == null){
					// não há o órgão colegiado destino no órgão julgador dado.
					return;
				}
	
				if(deslocarFluxos){
	                ProcessoInstanceManager processoInstanceManager = ComponentUtil.getComponent(ProcessoInstanceManager.class);
	                List<ProcessoInstance> recuperaAtivas = processoInstanceManager.recuperaAtivas(processoJudicial);
	                for (ProcessoInstance pi : recuperaAtivas) {
	                    if (pi.getIdLocalizacao() != null 
	                    		&& processoJudicial.getOrgaoJulgador() != null && processoJudicial.getOrgaoJulgador().getLocalizacao() != null 
	                    		&& pi.getIdLocalizacao() == processoJudicial.getOrgaoJulgador().getLocalizacao().getIdLocalizacao()){
	                    	
	                        pi.setIdLocalizacao(getIdLocalizacao(orgaoDestino.getIdOrgaoJulgador()));
	                        pi.setOrgaoJulgadorColegiado(orgaoColegiadoDestino != null ? orgaoColegiadoDestino.getIdOrgaoJulgadorColegiado() : null);
	                        pi.setOrgaoJulgadorCargo(cargoDestino.getIdOrgaoJulgadorCargo());
	                        processoJudicial.getProcesso().setCaixa(null);
	                        processoInstanceManager.persist(pi);
	                    }
	                }
					processoInstanceManager.flush();
				}

				processoJudicial.setOrgaoJulgador(orgaoDestino);
				processoJudicial.setOrgaoJulgadorColegiado(orgaoColegiadoDestino);
				processoJudicial.setOrgaoJulgadorCargo(cargoDestino);
				processoJudicial.setCargo(cargoDestino.getCargo());
				
				if(deslocarPeso){
					DistribuicaoService.instance().atualizarAcumuladoresProcessoRetificado(processoJudicial);
				}

				ComponentUtil.getComponent(ProcessoJudicialManager.class).mergeAndFlush(processoJudicial);

			}catch (PJeBusinessException e){
				throw new PJeBusinessException("Não foi possível deslocar o processo para outro órgão julgador.", e);
			} catch (Exception e) {
				throw new PJeBusinessException("Não foi possível atualizar o peso do processo nos cargos envolvidos no deslocamento.", e);
			}
		}
	}
	
	/**
	 * Indica se uma dada pessoa é advogado de outra em um determinado processo judicial.
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende obter a informação
	 * @param advogado a pessoa que, potencialmente, seria advogado do representado
	 * @param representado a pessoa que seria representada pelo advogado
	 * @return true, se o advogado representar o representado no processo dado como tal.
	 * @throws PJeBusinessException se houver algum erro na recuperação da informação.
	 */
	public boolean isAdvogado(ProcessoTrf processoJudicial, Pessoa advogado, Pessoa representado) throws PJeBusinessException{
		TipoParte tipoAdvogado = null;
		try {
			tipoAdvogado = EntityUtil.getEntityManager().getReference(TipoParte.class, Integer.parseInt(ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.TIPOPARTEADVOGADO)));
		} catch (NumberFormatException e) {
			throw new PJeBusinessException("pje.parametro.numberFormatException", e, Parametros.TIPOPARTEADVOGADO, ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.TIPOPARTEADVOGADO));
		}
		return isRepresentante(processoJudicial, advogado, representado, tipoAdvogado);
	}
	
	/**
	 * Indica se uma dada pessoa é representante de outra em um determinado processo judicial.
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende obter a informação
	 * @param representante a pessoa que, potencialmente, seria representante
	 * @param representado a pessoa potencialmente representada
	 * @param tipoRepresentacao o tipo de representação a ser pesquisado
	 * @return true, se houver a representação pesquisada
	 */
	public boolean isRepresentante(ProcessoTrf processoJudicial, Pessoa representante, Pessoa representado, TipoParte tipoRepresentacao){
		return ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class).isRepresentante(processoJudicial, representante, representado, tipoRepresentacao);
	}
	
	/**
	 * Recupera a lista de pessoas que atuam como advogados em favor de uma pessoa em um dado processo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende obter a informação
	 * @param representado a pessoa potencialmente representada
	 * @return a lista de pessoas que atuam como advogados ativos do representado no processo
	 * @throws PJeBusinessException, caso haja erro na recuperação do tipo de parte advogado, 
	 * 	caso o parâmetro {@link Parametros#TIPOPARTEADVOGADO} não esteja bem definido 
	 * ou se o representado não fizer parte do processo
	 */
	public List<Pessoa> recuperaAdvogados(ProcessoTrf processoJudicial, Pessoa representado) throws PJeBusinessException{
		TipoParte tipoAdvogado = null;
		try {
			tipoAdvogado = EntityUtil.getEntityManager().getReference(TipoParte.class, Integer.parseInt(ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.TIPOPARTEADVOGADO)));
		} catch (NumberFormatException e) {
			throw new PJeBusinessException("pje.parametro.numberFormatException", e, Parametros.TIPOPARTEADVOGADO, ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.TIPOPARTEADVOGADO));
		}
		return recuperaRepresentantes(processoJudicial, representado, tipoAdvogado);
	}
	
	/**
	 * Recupera a lista de representantes de uma pessoa em um dado processo judicial.
	 * 
	 * @param processoJudicial o processo em relação ao qual se pretende obter a informação
	 * @param representado a pessoa cujos advogados se pretende identificar
	 * @param tipoRepresentacao o tipo de representação que se pretende identificar
	 * @return a lista de representantes do tipo dado da pessoa no processo, ou uma lista vazia, se ela não tiver representantes desse tipo
	 * @throws PJeBusinessException caso a pessoa indicada não componha o processo como parte
	 */
	public List<Pessoa> recuperaRepresentantes(ProcessoTrf processoJudicial, Pessoa representado, TipoParte tipoRepresentacao) throws PJeBusinessException{
		List<Pessoa> representantes = ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class).recuperaRepresentantes(processoJudicial, representado, tipoRepresentacao);
		if(representantes.size() == 0){
			if(!ComponentUtil.getComponent(ProcessoParteManager.class).isParte(processoJudicial, representado, tipoRepresentacao)){
				throw new PJeBusinessException("pje.processoJudicialService.error.pessoaNaoEhParte", null, representado.getNome(), processoJudicial.getNumeroProcesso());
			}
		}
		return representantes;
	}

	public String getNomeExibicaoPolo(ProcessoTrf processoJudicial, ProcessoParteParticipacaoEnum polo){
		String retorno = "Não encontrado";
		
		if(processoJudicial != null){
			List<ProcessoParte> partes = null;
			List<ProcessoParte> parte = new ArrayList<ProcessoParte>(0);
			ProcessoParteManager processoParteManager = ComponentUtil.getProcessoParteManager();
			
			try {
				partes = processoParteManager.recuperaPartesParaExibicao(processoJudicial.getIdProcessoTrf(),true,null,null);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
	
			for (ProcessoParte p : partes) {
				if(p.getInParticipacao().equals(polo) && p.getPartePrincipal().equals(Boolean.TRUE)){
					parte.add(p);
				}
			}
			
			if (!parte.isEmpty()){
				
				List<ProcessoParte> partesNaoBaixadas = new ArrayList<ProcessoParte>(parte);
				for(ProcessoParte item : parte){
					if(item.getIsBaixado() || item.getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())){
						partesNaoBaixadas.remove(item);
					}
				}
				if(!partesNaoBaixadas.isEmpty()){
					// Ordena a lista pela propriedade ordem para a lista condizer com a ordenação do cadastro do processo.
					processoParteManager.ordenarListaPolosPorOrdem(partesNaoBaixadas, polo);
					StringBuilder sb = new StringBuilder();
					sb.append(processoParteManager.recuperaNomeUsadoNoProcesso(partesNaoBaixadas.get(0)));
					if (partesNaoBaixadas.size() > 1){
						sb.append(" e outros");
					}
					retorno = sb.toString();
				}
			}
		}
		return retorno; 
	}

	/**
	 * Verifica e obtém a informação de se o processo está em uma tarefa.
	 * 
	 * @param processoTrf o processo em relação ao qual se pretende obter a informação
	 * @param idTarefa o id da tarefa a se verificar
	 * 
	 * @return 1 se o processo estiver na tarefa ou 0 se não estiver.
	 * 
	 */
	public Long existeIdTarefaNoProcesso(ProcessoTrf processoTrf, Integer idTarefa){       
        return ComponentUtil.getComponent(ConsultaProcessoTrfManager.class).countConsultaProcessoSituacao(processoTrf, idTarefa);       
	}
	
	public Long existeTarefaNoProcesso(ProcessoTrf processoTrf, String nmTarefa){       
        return ComponentUtil.getComponent(ConsultaProcessoTrfManager.class).countConsultaProcessoSituacao(processoTrf, nmTarefa);       
	}


	/**
	 * Verifica se o processo em execução está vinculado a um dado assunto.
	 * 
	 * @param codigo
	 *            código do assunto processual
	 * @return true se houver o vínculo
	 * @see #possuiAssunto(ProcessoTrf, String)
	 * @category PJEII-3650
	 */
	public boolean possuiAssunto(String codigo) {
		ProcessoTrf processo = getProcessoEmExecucao();
		return possuiAssunto(processo, codigo);
	}

	/**
	 * Verifica se dado processo está vinculado a um assunto com determinado
	 * código, de acordo com a tabela unificada de assuntos.
	 * 
	 * @param processo
	 * @param codigo
	 *            código do assunto processual
	 * @return true se houver o vínculo
	 * @category PJEII-3650
	 */
	public boolean possuiAssunto(ProcessoTrf processo, String codigo) {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class).possuiAssunto(processo, codigo);
	}

	/**
	 * Verifica se dada classe judicial do processo está dentre as classes passadas por parametro.
	 * 
	 * @param processo
	 * @param Array de classes
	 * @return true se estiver dentro do conjunto
	 * @category PJEII-
	 */
	public boolean inClassesJudiciais(String ... codClasses) {
		ProcessoTrf processo = getProcessoEmExecucao();
		String codClasseJudProcesso = processo.getClasseJudicial().getCodClasseJudicial();
		
		for (int i = 0; i < codClasses.length; i++){
			if (codClasses[i].equals(codClasseJudProcesso))
				return true;
		}
	        
		return false;
	}
	
	/**
	 * Recupera o processo em execução.
	 */
	private ProcessoTrf getProcessoEmExecucao() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if (processoTrf == null) {
			Processo processo = ProcessoHome.instance().getInstance();
			if (processo != null){
				try {
					processoTrf = findById(processo.getIdProcesso());
				} catch (PJeBusinessException e) {
					// deixar processo nulo em caso de erro
				}
			}
		}
		return processoTrf;
	}
	
//PJEII-4765 - JE - Método chamado na EL da tarefa para trocar o orgao julgador vencedor  
	@SuppressWarnings("unchecked")
	public void deslocarOrgaoJulgador() 
	{

		EntityManager em = EntityUtil.getEntityManager();
		
		ProcessoTrf p = ProcessoJbpmUtil.getProcessoTrf();
		
		StringBuilder sqlPes = new StringBuilder();
		sqlPes.append(" select o from ");
		sqlPes.append(" SessaoPautaProcessoTrf o");
		sqlPes.append(" where o.processoTrf = :processoTrf and o.situacaoJulgamento = :situacao");
		Query query = em.createQuery(sqlPes.toString());
		query.setParameter("processoTrf", p);
		query.setParameter("situacao", TipoSituacaoPautaEnum.JG);

		List<SessaoPautaProcessoTrf> pautaProcessoTrf = (List<SessaoPautaProcessoTrf>) query.getResultList();
 		
		try {
			if (pautaProcessoTrf != null && !pautaProcessoTrf.isEmpty()) 
			{
				OrgaoJulgador ojv = pautaProcessoTrf.get(0).getOrgaoJulgadorVencedor();
				if (ojv != null && !ojv.equals(pautaProcessoTrf.get(0).getProcessoTrf().getOrgaoJulgador())) {
					deslocarOrgaoJulgador(ojv.getIdOrgaoJulgador());
				}
			}
		} catch (PJeBusinessException e) {
			log.error("[DESLOACAR_ORGAO_JULGADOR] Tentativa de encaminha orgao julgador para vendedor não foi bem sucedida ", e);
			e.printStackTrace();
 		}
	}
	
	/**
	 * Indica se o processo judicial dado é visível para o usuário no papel que atualmente ele adotou.
	 * O processo será visível se:
	 *  <li>não estiver sob segredo ou sigilo</li>
	 *  <li>estiver sob segredo ou sigilo e o usuário estiver no rol de visualizadores; ou 
	 *  <li>estiver sob segredo ou sigilo e for usuário que tenha o papel {@link Papeis#VISUALIZA_SIGILOSO} 
	 *  e o processo pertencer ? sua localização, salvo se papel for não filtrável {@link ControleFiltros#isPapeisNaoFiltraveis(Identity)}.</li>
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende identificar a visibilidade
	 * @param loc a localização atual utilizada
	 * @param identity o conjunto de papeis pertencente ao usuário
	 * @return true, se o processo for visível.
	 * 
	 * @see ProcessoVisibilidadeSegredoManager#visivel(ProcessoTrf, Usuario)
	 */
	public boolean visivel(ProcessoTrf processoJudicial, UsuarioLocalizacao loc, Identity identity){
		return visivel(processoJudicial, loc, identity, false);
	}

	/**
	 * Indica se o processo judicial dado é visível para o usuário no papel que atualmente ele adotou.
	 * O processo será visível se:
	 *  <li>não estiver sob segredo ou sigilo</li>
	 *  <li>estiver sob segredo ou sigilo e o usuário estiver no rol de visualizadores; ou 
	 *  <li>estiver sob segredo ou sigilo e for usuário que tenha o papel {@link Papeis#VISUALIZA_SIGILOSO} 
	 *  e estiver no juizo do processo</li>
	 * 
	 * @param processoJudicial o processo a respeito do qual se pretende identificar a visibilidade
	 * @param loc a localização atual utilizada
	 * @param identity o conjunto de papeis pertencente ao usuário
	 * @param forcarValidacao se 'true' verifica as condições de visualização mesmo que o processo não seja sigiloso
	 * @return true, se o processo for visível.
	 * 
	 */
	public boolean visivel(ProcessoTrf processoJudicial, UsuarioLocalizacao loc, Identity identity, boolean forcarValidacao){
		if(!processoJudicial.getSegredoJustica() && !forcarValidacao){
			return true;
		}else if(isUsuarioLogadoComPapeisAdequadosParaSigilo(loc, identity)){
			ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
			UsuarioLocalizacaoMagistradoServidor locInterna = loc.getUsuarioLocalizacaoMagistradoServidor();

			if(locInterna != null) {
				Localizacao localizacaoFisicaPessoa = loc.getLocalizacaoFisica();
				OrgaoJulgadorColegiado ojcPessoa = locInterna.getOrgaoJulgadorColegiado();
				boolean isServidorExclusivoOJC = this.isServidorExclusivoOJC(locInterna);

				if(processoTrfManager.isPessoaJuizoProcesso(processoJudicial, localizacaoFisicaPessoa, ojcPessoa, isServidorExclusivoOJC)) {
					return true;
				}else {
					ProcessoInstanceManager processoInstanceManager = ComponentUtil.getComponent(ProcessoInstanceManager.class);
					LocalizacaoManager localizacaoManager = ComponentUtil.getComponent(LocalizacaoManager.class);
					List<Localizacao> localizacoesFilhas = localizacaoManager.getArvoreDescendente(localizacaoFisicaPessoa.getIdLocalizacao(), true);
					List<Integer> idsLocalizacoesFisicasList = CollectionUtilsPje.convertStringToIntegerList(LocalizacaoUtil.converteLocalizacoesList(localizacoesFilhas));
					// verificar se há algum fluxo deslocado para a localizacao do usuario
					if(processoInstanceManager.existeProcessoInstancePorLocalizacaoPessoa(processoJudicial, idsLocalizacoesFisicasList, ojcPessoa, isServidorExclusivoOJC)) {
						return true;
					}
				}
			}
		}
		
		return loc != null && ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class).visivel(processoJudicial, loc.getUsuario(), Authenticator.getReferenciaProcuradoriaAtualUsuarioLogado());
	}

	private boolean isUsuarioLogadoComPapeisAdequadosParaSigilo(UsuarioLocalizacao loc, Identity identity) {
		return identity != null && loc != null && (identity.hasRole(Papeis.VISUALIZA_SIGILOSO) || identity.hasRole(Papeis.MANIPULA_SIGILOSO));
	}
	
	private boolean isServidorExclusivoOJC(UsuarioLocalizacaoMagistradoServidor locInterna) {
		boolean isServidorExclusivoOJC = false;
		
		if(locInterna != null) {
			OrgaoJulgador ojPessoa = locInterna.getOrgaoJulgador();
			OrgaoJulgadorColegiado ojcPessoa = locInterna.getOrgaoJulgadorColegiado();
			isServidorExclusivoOJC = (ojcPessoa != null && ojPessoa == null);
		}

		return isServidorExclusivoOJC;
	}

	/**
	 * O usuario deve ter o papel MANIPULA_SIGILOSO e (pertencer ao juizo do processo)
	 * - Apenas as pessoas do juizo do processo podem manipular o sigilo do processo
	 * 
	 * @param processoJudicial
	 * @param loc
	 * @param identity
	 * @return
	 */
	public boolean manipulavel(ProcessoTrf processoJudicial, UsuarioLocalizacao loc, Identity identity){
		ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
		UsuarioLocalizacaoMagistradoServidor locInterna = loc.getUsuarioLocalizacaoMagistradoServidor();
		
		if(identity == null || locInterna == null){
			return false;
		}else if(identity.hasRole(Papeis.MANIPULA_SIGILOSO)){
			Localizacao localizacaoFisicaPessoa = loc.getLocalizacaoFisica();
			OrgaoJulgadorColegiado ojcPessoa = locInterna.getOrgaoJulgadorColegiado();
			boolean isServidorExclusivoOJC = this.isServidorExclusivoOJC(locInterna);

			boolean pessoaJuizoProcesso = processoTrfManager.isPessoaJuizoProcesso(processoJudicial, localizacaoFisicaPessoa, ojcPessoa, isServidorExclusivoOJC);
			if (pessoaJuizoProcesso) {
				return true;
			}
			boolean existeFluxoDeslocado = existeFluxoDeslocadoParaLocalizacao(processoJudicial);
			if (existeFluxoDeslocado) {
				return true;
			}
		}
		return false;
	}
	
	public void confirmarSolicitacaoSegredo(ProcessoSegredo solicitacao) throws PJeBusinessException{
		ProcessoSegredoManager processoSegredoManager = ComponentUtil.getComponent(ProcessoSegredoManager.class);
		ProcessoSegredo sol = processoSegredoManager.findById(solicitacao.getIdProcessoSegredo());
		ProcessoTrf processo = sol.getProcessoTrf();
		sol.setApreciado(true);
		sol.setStatus(SegredoStatusEnum.C);
		processoSegredoManager.persistAndFlush(solicitacao);
		List<ProcessoSegredo> solicitacoes = processoSegredoManager.getSolicitacoes(processo);
		boolean pendenteApreciacao = false;
		for(ProcessoSegredo s: solicitacoes){
			if(!s.getApreciado()){
				pendenteApreciacao = true;
				break;
			}
		}
		if(pendenteApreciacao){
			processo.setApreciadoSegredo(ProcessoTrfApreciadoEnum.A);
			processo.setApreciadoSigilo(ProcessoTrfApreciadoEnum.A);
		}else{
			processo.setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);
			processo.setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
		}
		processo.setSegredoJustica(true);
		ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processo);
	}

	public void recusarSolicitacaoSegredo(ProcessoSegredo solicitacao) throws PJeBusinessException{
		ProcessoSegredoManager processoSegredoManager = ComponentUtil.getComponent(ProcessoSegredoManager.class);
		ProcessoSegredo sol = processoSegredoManager.findById(solicitacao.getIdProcessoSegredo());
		ProcessoTrf processo = sol.getProcessoTrf();
		sol.setApreciado(true);
		sol.setStatus(SegredoStatusEnum.R);
		processoSegredoManager.persistAndFlush(solicitacao);
		List<ProcessoSegredo> solicitacoes = processoSegredoManager.getSolicitacoes(processo);
		boolean pendente = false;
		for(ProcessoSegredo s: solicitacoes){
			if(!s.getApreciado()){
				pendente = true;
			}
		}
		if(!pendente){
			processo.setApreciadoSegredo(ProcessoTrfApreciadoEnum.N);
			processo.setApreciadoSigilo(ProcessoTrfApreciadoEnum.N);
			processo.setSegredoJustica(false);
			processo.setNivelAcesso(0);
			ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processo);
		}
	}

	public void inverterSigilo(ProcessoTrf processo, UsuarioLogin responsavel, String motivoSegredo) throws PJeBusinessException {
		ProcessoSegredoManager processoSegredoManager = ComponentUtil.getComponent(ProcessoSegredoManager.class);
		List<ProcessoSegredo> solicitacoes = processoSegredoManager.getSolicitacoes(processo);
		ProcessoSegredo segredo = new ProcessoSegredo();
		segredo.setApreciado(true);
		segredo.setMotivo(motivoSegredo);
		segredo.setDtAlteracao(new Date());
		segredo.setUsuarioLogin(responsavel);
		segredo.setProcessoTrf(processo);
		processoSegredoManager.persistAndFlush(segredo);
		SegredoStatusEnum situacao = null;
		if(processo.getSegredoJustica()){
			removerTodosVisualizadores(processo);
			processo.setSegredoJustica(false);
			processo.setApreciadoSegredo(ProcessoTrfApreciadoEnum.N);
			processo.setApreciadoSigilo(ProcessoTrfApreciadoEnum.N);
			situacao = SegredoStatusEnum.R;
			processo.setObservacaoSegredo(null);
		}else{
			processo.setSegredoJustica(true);
			processo.setApreciadoSegredo(ProcessoTrfApreciadoEnum.S);
			processo.setApreciadoSigilo(ProcessoTrfApreciadoEnum.S);
			situacao = SegredoStatusEnum.C;
			processo.setObservacaoSegredo(motivoSegredo);
			habilitarVisibilidadePartesPoloAtivoFiscalLei(processo);
		}
		for(ProcessoSegredo s: solicitacoes){
			if(!s.getApreciado()){
				s.setApreciado(true);
				s.setStatus(situacao);
			}
		}
		segredo.setStatus(situacao);
		ComponentUtil.getComponent(ProcessoJudicialManager.class).persistAndFlush(processo);
	}
	
	/**
	 * Método responsável por habilitar a visibilidade ao processo sigiloso 
	 * dos integrantes do pólo ativo e do fiscal da lei (caso exista).
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return o número de pessoas a quem se atribuiu a liberação
	 * @throws PJeBusinessException 
	 */
	public int habilitarVisibilidadePartesPoloAtivoFiscalLei(ProcessoTrf processoTrf) throws PJeBusinessException {		
		int cont = 0;
		if (habilitarVisibilidadeFiscalDaLei(processoTrf))
			cont++;
		for (ProcessoParte pp : processoTrf.getListaParteAtivo()) {
			if (acrescentaVisualizador(processoTrf, pp.getPessoa(), pp.getProcuradoria()))
				cont++;
		}
		return cont;
	}
	
	/**
	 * Método responsável por habilitar a visibilidade ao processo sigiloso se a pessoa
	 * for integrante do pólo ativo ou fiscal da lei (caso exista).
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @param pessoa a pessoa a ser acrescida como visualizadora
	 * @param procuradoria a procuradoria a ser acrescida como visualizadora
	 * @return o número de pessoas a quem se atribuiu a liberação
	 * @throws PJeBusinessException 
	 */
	public int habilitarVisibilidadeSePartePoloAtivoFiscalLei(ProcessoTrf processoTrf, Pessoa pessoa, Procuradoria procuradoria) throws PJeBusinessException {
		int cont = 0;
		if (habilitarVisibilidadeFiscalDaLei(processoTrf))
			cont++;
		if (processoTrf.getListaParteAtivo().stream().anyMatch(pp -> pp.getPessoa().getIdPessoa().equals(pessoa.getIdPessoa()))) {
			acrescentaVisualizador(processoTrf, pessoa, procuradoria);
			cont++;
		}
		return cont;
	}

	private boolean habilitarVisibilidadeFiscalDaLei(ProcessoTrf processoTrf) throws PJeBusinessException {
		Optional<Pessoa> fiscalLei = Optional.ofNullable(PessoaManager.instance().getFiscalLei(processoTrf.getJurisdicao()));
		if (fiscalLei.isPresent()) {
			Optional<ProcessoParte> parteFiscalLei = processoTrf.getListaParteTerceiro().stream().filter(pp -> pp.getPessoa().getIdPessoa().equals(fiscalLei.get().getIdPessoa())).findFirst();
			if (parteFiscalLei.isPresent()) {
				return acrescentaVisualizador(processoTrf, parteFiscalLei.get().getPessoa(), parteFiscalLei.get().getProcuradoria());
			}
		}
		return false;
	}

	/**
	 * Libera a visualização de um processo judicial dado para todos as pessoas (servidores ou servidores e magistrados) vinculados ao
	 * órgão julgador ou ao órgão julgador colegiado.
	 * 
	 * Quando se tratar de liberação para órgão julgador (e não para órgão julgador colegiado), a liberação será limitada aos servidores.
	 * Quando se tratar de liberação para órgão julgador colegiado, a liberação abrangerá, também, os magistrados vinculados aos demais
	 * órgãos julgadores do órgão julgador colegiado.
	 * 
	 * @param processo o processo objeto da operação.
	 * @param responsavel a pessoa responsável pela liberação
	 * @param colegiado marca indicativa de que se pretende realizar a liberação para os componentes do órgão julgador colegiado.
	 * @return o número de pessoas a quem se atribuiu a liberação
	 * @throws PJeBusinessException
	 */
	public int liberarVisualizacaoOrgaoJulgador(ProcessoTrf processo, boolean colegiado) throws PJeBusinessException {
		if(!processo.getSegredoJustica()){
			return 0;
		}
		int cont = 0;
		List<PessoaFisica> servidores = null;
		if(!colegiado){
			servidores = ComponentUtil.getComponent(UsuarioService.class).getServidores(processo.getOrgaoJulgador());
		}else if(processo.getOrgaoJulgadorColegiado() != null){
			servidores = ComponentUtil.getComponent(UsuarioService.class).getServidores(processo.getOrgaoJulgadorColegiado());
		}else{
			return cont;
		}
		for(PessoaFisica p: servidores){
			if((!colegiado && Pessoa.instanceOf(p, PessoaServidor.class) && !Pessoa.instanceOf(p, PessoaMagistrado.class))
					// Liberação para servidores (e só servidores) do órgão singular
					|| (colegiado && (Pessoa.instanceOf(p, PessoaServidor.class) || Pessoa.instanceOf(p, PessoaMagistrado.class)))
					// Liberação para servidores e magistrados do órgão colegiado
					){
				if(acrescentaVisualizador(processo, p, null, false)){
					cont++;
				}
			}
		}
		if(cont > 0){
			ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class).flush();
		}
		return cont;
	}

	/**
	 * Libera a visualização de um processo judicial dado para todas as partes ativas do processo.
	 * 
	 * @param processo o processo objeto da operação.
	 * @return o número de pessoas a quem se atribuiu a liberação
	 * @throws PJeBusinessException
	 */
	public int liberarVisualizacaoTodasPartes(ProcessoTrf processo) throws PJeBusinessException {
		if(!processo.getSegredoJustica()){
			return 0;
		}

		int cont = 0;
		List<ProcessoParte> partes = ComponentUtil.getComponent(ProcessoParteManager.class).recuperaPartes(processo, true, null, null);
		List<Integer> idsPessoaProcessoParteConferidos = new ArrayList<Integer>();
		for(ProcessoParte pp: partes){
			Integer idPessoaPP = pp.getPessoa().getIdPessoa();
			if (!idsPessoaProcessoParteConferidos.contains(idPessoaPP)) {
				if(acrescentaVisualizador(processo, pp.getPessoa(), pp.getProcuradoria(), false)){
					cont++;
					idsPessoaProcessoParteConferidos.add(idPessoaPP);
				}
			}
		}
		
		if(cont > 0){
			ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class).flush();
		}
		return cont;
	}
	
	/**
	 * Remove todos visualizadores de um processo judicial.
	 * 
	 * @param processo o processo objeto da operação.
	 * @throws PJeBusinessException
	 */
	public void removerTodosVisualizadores(ProcessoTrf processo) throws PJeBusinessException{
		if(BooleanUtils.isTrue(processo.getSegredoJustica())){
			List<ProcessoVisibilidadeSegredo> visualizadores = ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class).recuperarVisualizadores(processo, null, null);
			if(visualizadores!=null && !visualizadores.isEmpty()){
				for (ProcessoVisibilidadeSegredo processoVisibilidadeSegredo : visualizadores) {
					removeVisualizador(processo, processoVisibilidadeSegredo.getPessoa(), false);
				}
			}
		}
	}
	

	public long contagemVisualizadores(ProcessoTrf processo) throws PJeBusinessException{
		return ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class).contagemVisualizadores(processo);
	}

	/**
	 * Recupera a lista de visualizadores de um dado processo judicial sigiloso.
	 * 
	 * @param processo o processo judicial a ser verificado
	 * @param first o primeiro resultado a ser recuperado
	 * @param max o máximo de resultados recuperados por chamada
	 * @return a lista de visualizadores, que deverá ser vazia se o processo não for sigiloso.
	 * @throws PJeBusinessException
	 */
	public List<ProcessoVisibilidadeSegredo> recuperaVisualizadores(ProcessoTrf processo, Integer first, Integer max) throws PJeBusinessException{
		return ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class).recuperarVisualizadores(processo, first, max);
	}
	
	/**
	 * Acrescenta uma pessoa como visualizadora de um processo judicial sigiloso.
	 * 
	 * @param processo o processo judicial ao qual será adicionado um visualizador
	 * @param pessoa a pessoa a ser acrescida como visualizadora
	 * @param procuradoria a procuradoria a ser acrescida como visualizadora
	 * @return true, se foi acrescentada a pessoa como visualizadora; false, se o processo não é sigiloso ou se a pessoa indicada já era
	 * visualizadora do processo
	 * @throws PJeBusinessException
	 */
	public boolean acrescentaVisualizador(ProcessoTrf processo, Pessoa pessoa, Procuradoria procuradoria) throws PJeBusinessException {
		return acrescentaVisualizador(processo, pessoa, procuradoria, true);
	}
	
	/**
	 * Acrescenta uma pessoa como visualizadora de um processo judicial sigiloso.
	 * 
	 * @param processo o processo judicial ao qual será adicionado um visualizador
	 * @param pessoa a pessoa a ser acrescida como visualizadora
	 * @param procuradoria a procuradoria a ser acrescida como visualizadora
	 * @param flush marca indicativa de que, após a criação, deve ser realizado um flush para o banco de dados
	 * @return true, se foi acrescentada a pessoa como visualizadora; false, se o processo não é sigiloso ou se a pessoa indicada já era
	 * visualizadora do processo
	 * @throws PJeBusinessException
	 */
	public boolean acrescentaVisualizador(ProcessoTrf processo, Pessoa pessoa, Procuradoria procuradoria, boolean flush) throws PJeBusinessException {
		if(!processo.getSegredoJustica()){
			return false;
		}
		
		ProcessoVisibilidadeSegredo pvs = null;
		ProcessoVisibilidadeSegredoManager processoVisibilidadeSegredoManager = ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class);
		if(!processoVisibilidadeSegredoManager.visivel(processo, (Usuario) pessoa)){
			pvs = processoVisibilidadeSegredoManager.criar(pessoa, processo, procuradoria);
			if(flush){
				processoVisibilidadeSegredoManager.persistAndFlush(pvs);
			}else{
				processoVisibilidadeSegredoManager.persist(pvs);
			}
			return true;
		} else {
			pvs = processoVisibilidadeSegredoManager.recuperaProcessoVisibilidadeSegredo(pessoa, processo);
			if(procuradoria != null && pvs != null && pvs.getProcuradoria() == null) {
				processoVisibilidadeSegredoManager.atualizaProcuradoriaSegredo(pvs, procuradoria);
				return true;
			}
		}
		return false;
	}

	public boolean removeVisualizador(ProcessoTrf processo, Pessoa pessoa) throws PJeBusinessException{
 		return removeVisualizador(processo, pessoa, true);
	}
	
	public boolean removeVisualizador(ProcessoTrf processo, Pessoa pessoa, boolean flush) throws PJeBusinessException {
		if(!processo.getSegredoJustica()){
			return false;
		}
		ProcessoVisibilidadeSegredoManager processoVisibilidadeSegredoManager = ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class);
		ProcessoVisibilidadeSegredo pvs = processoVisibilidadeSegredoManager.recuperaProcessoVisibilidadeSegredo(pessoa, processo);
		if(pvs != null){
			processoVisibilidadeSegredoManager.remove(pvs);
			if(flush){
				processoVisibilidadeSegredoManager.flush();
			}
			return true;
		}
		return false;
	}
	
	public ProcessoParte recuperaParte(ProcessoTrf processo, int idParte) throws PJeBusinessException{
		ProcessoParte parte = ComponentUtil.getComponent(ProcessoParteManager.class).findById(idParte);
		if(!processo.equals(parte.getProcessoTrf())){
			throw new PJeBusinessException("A parte com o código {0} não pertence ao processo {1}.", new IllegalArgumentException(), idParte, processo.getProcesso().getNumeroProcesso());
		}
		return parte;
	}
	
	/**
	 * Recupera uma lista com todas as partes sigilosas de um dado processo judicial.
	 * 
	 * @param processo o processo judicial
	 * @param somenteAtivas marca indicativa de que se pretende recuperar somente as partes ativas
	 * @param first indicação do primeiro resultado da lista que se pretende recuperar (nulo para recuperar a partir do primeiro)
	 * @param maxResults indicação do máximo de resultados que se pretende recuperar (nulo para recuperar todos)
	 * @return a lista de partes sigilosas
	 * @throws PJeBusinessException
	 */
	public List<ProcessoParte> recuperaPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas, Integer first, Integer maxResults) throws PJeBusinessException{
		return ComponentUtil.getComponent(ProcessoParteManager.class).recuperaPartesSigilosas(processo, somenteAtivas, first, maxResults);
	}
	
	public int tornarPartesSigilosas(ProcessoTrf processo, String motivo, ProcessoParteParticipacaoEnum...polos) throws PJeBusinessException{
		int cont = 0;
		for(ProcessoParte parte: processo.getListaPartePoloObj(polos)){
			if(tornarParteSigilosa(parte, motivo, false)){
				cont++;
			}
		}
		if(cont > 0){
			ComponentUtil.getComponent(ProcessoJudicialManager.class).flush();
		}
		return cont;
	}
	
	public boolean tornarParteSigilosa(ProcessoParte parte, String motivo) throws PJeBusinessException{
		return tornarParteSigilosa(parte, motivo, true);
	}
	
	private boolean tornarParteSigilosa(ProcessoParte parte, String motivo, boolean flush) throws PJeBusinessException {
		if(!parte.getParteSigilosa()){
			ProcessoParteSigiloManager processoParteSigiloManager = ComponentUtil.getComponent(ProcessoParteSigiloManager.class);
			ProcessoParteSigilo sigiloParte = processoParteSigiloManager.criar(parte, (PessoaFisica) ComponentUtil.getComponent(UsuarioService.class).getUsuarioLogado(), motivo);
			parte.setParteSigilosa(true);
			if(flush){
				processoParteSigiloManager.persistAndFlush(sigiloParte);
			}else{
				processoParteSigiloManager.persist(sigiloParte);
			}

			getProcessoParteCache().refreshProcessoParteByProcessoTrfEPessoaCache(
					parte.getProcessoTrf().getIdProcessoTrf(), parte.getIdPessoa());

			return true;
		}
		return false;
	}
	
	public int tornarPartesVisiveis(ProcessoTrf processo, String motivo, ProcessoParteParticipacaoEnum...polos) throws PJeBusinessException{
		int cont = 0;
		for(ProcessoParte parte: processo.getListaPartePoloObj(polos)){
			if(tornarParteVisivel(parte, motivo, false)){
				cont++;
			}
		}
		if(cont > 0){
			ComponentUtil.getComponent(ProcessoJudicialManager.class).flush();
		}
		return cont;
	}

	public boolean tornarParteVisivel(ProcessoParte parte, String motivo) throws PJeBusinessException {
		return tornarParteVisivel(parte, motivo, true);
	}

	private boolean tornarParteVisivel(ProcessoParte parte, String motivo, boolean flush) throws PJeBusinessException {
		if(parte.getParteSigilosa()){
			ProcessoParteSigiloManager processoParteSigiloManager = ComponentUtil.getComponent(ProcessoParteSigiloManager.class);
			ProcessoParteSigilo sigiloParte = processoParteSigiloManager.criar(parte, (PessoaFisica) ComponentUtil.getComponent(UsuarioService.class).getUsuarioLogado(), motivo);
			sigiloParte.setStatus(SigiloStatusEnum.R);
			parte.setParteSigilosa(false);
			if(flush){
				processoParteSigiloManager.persistAndFlush(sigiloParte);
			}else{
				processoParteSigiloManager.persist(sigiloParte);
			}
			return true;
		}
		return false;
	}

	public long contagemPartesSigilosas(ProcessoTrf processo, boolean somenteAtivas) throws PJeBusinessException {
		return ComponentUtil.getComponent(ProcessoParteManager.class).contagemPartesSigilosas(processo, somenteAtivas);
	}

	/**
	 * Recupera o número de partes existentes no processo.
	 * 
	 * @param processoJudicial o processo a respeito do qual se quer a informação
	 * @param somenteAtivas marca indicativa de que a contagem deve se limitar ?s partes ativas.
	 * @return o número de partes
	 */
	public long contagemPartes(ProcessoTrf processo, boolean somenteAtivas) {
		return ComponentUtil.getComponent(ProcessoParteManager.class).contagemPartes(processo, somenteAtivas);
	}
	
	public String nomeParaExibicao(String parte, long cont){
		return ComponentUtil.getComponent(ProcessoParteManager.class).nomeExibicao(parte, cont);
	}
	
	public String nomeParaExibicao(ProcessoTrf processoTrf, String parte, long cont) throws PJeBusinessException{
		ProcessoParteManager processoParteManager = ComponentUtil.getProcessoParteManager();
		return processoParteManager.nomeParaExibicao(processoTrf, cont, parte);
	}

	/** 
	 * Retorna o nome da parte não sigilosa do processo. Caso seja sigilosa, retorna "(Em segredo de justiça)".
	 * 
	 * @param processo Processo a respeito do qual se quer a informação.
	 * @param polo 'A' Ativo , 'P 'Passivo, 'O' Outros.
	 * @return O nome da parte não sigilosa do processo. Se sigilosa, retorna "(Em segredo de justiça)".
	 */
	public String nomeExibicaoConsultaPublica(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo){
		return ComponentUtil.getComponent(ProcessoParteManager.class).nomeExibicaoConsultaPublica(processo, polo);
	}
	
	@Transactional
	@Restrict(value="#{identity.loggedIn}")
	public void incluirAlerta(String textoAlerta, CriticidadeAlertaEnum criticidade, Integer...idProcessos){
		try{
			ProcessoAlertaManager procAlertaManager = ComponentUtil.getComponent(ProcessoAlertaManager.class);
			
			for(Integer id: idProcessos){
				
				ProcessoTrf proc = this.findById(id);
				procAlertaManager.incluirAlertaAtivo(proc, textoAlerta, criticidade);
			}
			procAlertaManager.flush();
		}catch (PJeBusinessException e){
			throw new PJeRuntimeException("Erro ao tentar incluir alerta.", e);
		}
	}

	/**
	 * Recupera a lista de partes que estão em situação baixado, inativo ou excluído do processo dado.
	 * 
	 * @param processo o processo de refer?ncia
	 * @return a lista de partes excluídas
	 */
	public List<ProcessoParte> getPartesExcluidas(ProcessoTrf processo) {
		return ComponentUtil.getComponent(ProcessoParteManager.class).recuperaPartesExcluidas(processo);
	}

	/**
	 * Método responsável por recuperar a última movimentação do processo especificado
	 * [PJEII-3616]
	 * @param processo
	 * @return ?ltima movimentação do processo especificado
	 */
	public ProcessoEvento recuperarUltimoMovimento(ProcessoTrf processo)  {
		try {
			return ComponentUtil.getComponent(ProcessoJudicialManager.class).recuperarUltimoMovimento(processo);
		} catch (Exception e) {
			throw new PJeRuntimeException("Erro ao tentar recuperar o último movimento do processo.", e);
		}
	}
	
	/**
	 * Método responsável por indicar se o processo possui parte sem CPF/CNPJ
	 * @param processoTrf
	 * @return true, se o processo possui parte sem CPF/CNPJ. false caso contrário.
	 */
	public boolean processoComParteSemCPFCNPJ(ProcessoTrf processoTrf){
		boolean achouDoc;
		
		if(processoTrf != null){
			for(Pessoa pessoa : processoTrf.getPessoaPoloAtivoList()){
				achouDoc = false;
				if (!pessoa.getInTipoPessoa().equals(TipoPessoaEnum.A)) {  // Não deve considerar ente ou autoridade.
					for(PessoaDocumentoIdentificacao docIdent : pessoa.getPessoaDocumentoIdentificacaoList()){
						if(docIdent == null){
							return true;
						} else if (docIdent.getTipoDocumento().getCodTipo().equalsIgnoreCase("cpf") ||
									docIdent.getTipoDocumento().getCodTipo().equalsIgnoreCase("cpj")){
							achouDoc = true;
							break;
						}
					}					
					if(achouDoc == false){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Método responsável por verificar se existe pedido de justiça gratuita e
	 * se o mesmo já foi apreciado.
	 * 
	 * Adiciona também um alerta ao processo caso seja requisitado pelo
	 * consumidor
	 *
	 * @param processoTrf
	 *            {@link ProcessoTrf} a ser pesquisado
	 * 
	 * @param adicionarAlerta
	 *            Indicador para adicionar mensagem de alerta ao processo caso
	 *            exista pedido de justiça gratuita e ainda não teve apreciação.
	 * @see {@link AlertaHome#inserirAlerta(ProcessoTrf, String, CriticidadeAlertaEnum)}
	 *  
	 * @return <code>true</code> caso não exista pedido de justiça gratuita ou
	 *         exista e já apreciado, portanto válido.
	 * 
	 *         <code>false</code> caso exista pedido de justiça gratuita e este
	 *         ainda não foi apreciado, portanto inválido.
	 */

	public boolean validarPedidoJusticaGratuitaApreciado(
			ProcessoTrf processoTrf, boolean adicionarAlerta) {
		
		if (processoTrf.getJusticaGratuita()) {
			// possui pedido de justiça gratuita, valida se já foi apreciado
			
			if (processoTrf.getApreciadoJusticaGratuita()) {
				// já foi apreciado, então está ok
				return true;
				
			} else {
			
				if (adicionarAlerta) {
					AlertaHome.instance().inserirAlerta(processoTrf, "Existe pedido de justiça gratuita pendente de apreciação.", CriticidadeAlertaEnum.A);
					EntityUtil.getEntityManager().flush();				
				}
				
				// pedido não foi apreciado, processo não está pronto para remessa
				return false;
			}
			
		} else {
			
			// não há pedido de justiça gratuita, então está ok
			return true;
			
		}
		
	}
	
	/**
	 * Método responsável por verificar se as partes do polo ativo e passivo
	 * possuem representação de advogados.
	 * 
	 * @see ProcessoJudicialService#recuperaAdvogados(ProcessoTrf, Pessoa)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} a ser pesquisado
	 * 
	 * @param adicionarAlerta
	 *            Indicador para adicionar mensagem de alerta ao processo caso
	 *            exista parte sem representação de advogado.
	 * @see {@link AlertaHome#inserirAlerta(ProcessoTrf, String, CriticidadeAlertaEnum)}
	 * @return <code>false</code> caso todas as partes do polo ativo e passivo
	 *         possuam representantes;
	 * 
	 *         <code>true</code> caso alguma das partes do polo ativo ou
	 *         passivo não possua representante.
	 */
	public boolean existeParteSemRepresentacao(ProcessoTrf processoTrf,
			boolean adicionarAlerta) {
				
		int idTipoParteAdvogado = Integer.parseInt(ComponentUtil.getComponent(ParametroService.class)
				.valueOf(Parametros.TIPOPARTEADVOGADO));
		
		// recupera as partes ativas do polo ativo e passivo
		List<ProcessoParte> listaPartes = processoTrf
				.getListaParteAtivo();
		
		listaPartes.addAll(processoTrf
				.getListaPartePassivo());
		
		List<String> nomePartesSemAdvogado = new ArrayList<String>();
		
		for (ProcessoParte processoParte : listaPartes) {	
			
			if (processoParte.getTipoParte().getIdTipoParte() == idTipoParteAdvogado) {
				continue;
			}
			
			try {
				
				List<Pessoa> advogadosParte = recuperaAdvogados(processoTrf, processoParte.getPessoa());
				
				if (advogadosParte == null || advogadosParte.isEmpty()) {
					nomePartesSemAdvogado.add(processoParte.getNomeParte());
				}
				
			} catch (PJeBusinessException e) {				
				// erro retornado ao recuperar advogador, indica que a parte não
				// possui representante e também não participa como advogado
				nomePartesSemAdvogado.add(processoParte.getNomeParte());
			}	
		}
		
		if (!nomePartesSemAdvogado.isEmpty()) {
			
			if (adicionarAlerta) {
				StringBuilder msg = new StringBuilder("Existe(m) parte(s) sem representação no processo: ");
				msg.append(StringUtil.concatList(nomePartesSemAdvogado, ", ", " e "));
				
				AlertaHome.instance().inserirAlerta(processoTrf, msg.toString(), CriticidadeAlertaEnum.A);
				EntityUtil.getEntityManager().flush();				
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Realiza o deslocamento do processo para a localização de um magistrado.
	 * 
	 * RN: Quando o Juiz Substituto(Auxiliar) estiver executando o ato, o processo
	 * será deslocado para o órgão julgador onde o Juiz Substituto for titular.
	 * 
	 * @param processoTrf
	 * @param uslmsOrgaoresponsavel
	 * @return
	 * @throws PJeBusinessException
	 */
	public boolean deslocarProcessoParaLocalizacaoDeJuiz(ProcessoTrf processoTrf, UsuarioLocalizacaoMagistradoServidor uslmsOrgaoresponsavel)
			throws PJeBusinessException {
		boolean retorno = false;
		if (processoTrf != null && uslmsOrgaoresponsavel != null) {
						
			//Retirar da caixa
            processoTrf.getProcesso().setCaixa(null);
			
			deslocarOrgaoJulgador(processoTrf, uslmsOrgaoresponsavel.getOrgaoJulgador().getIdOrgaoJulgador(),
				uslmsOrgaoresponsavel.getOrgaoJulgadorColegiado() != null ? uslmsOrgaoresponsavel.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() : null,
				uslmsOrgaoresponsavel.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
									
			retorno = true;
		}
		return retorno;
	}
	
	/**
	 * Retorna true se existir fluxo iniciado para o tipo de documento e processo do documento passado por parâmetro.
	 * A validação somente será feita se o tipo de documento tiver o atributo 'variavelFluxo' definido.
	 * 
	 * @param processoTrf ProcessoTrf.
	 * @param documento ProcessoDocumento.
	 * @return boleano.
	 * @throws PJeBusinessException
	 */
	public Boolean isExisteFluxoIniciadoParaDocumento(ProcessoTrf processoTrf, ProcessoDocumento documento) throws PJeBusinessException {
		Boolean resultado = Boolean.FALSE;
		
		if (documento != null && processoTrf != null && documento.getTipoProcessoDocumento() != null) {
			TipoProcessoDocumento tipoProcessoDocumento = documento.getTipoProcessoDocumento();
			String variavelFluxo = tipoProcessoDocumento.getVariavelFluxo();
			resultado = (StringUtils.isNotBlank(variavelFluxo) ? 
					ComponentUtil.getComponent(FluxoManager.class).existeFluxoComVariavel(processoTrf, variavelFluxo) : 
					Boolean.FALSE);
		}
		return resultado;
	}
	
	public String nomeParaExibicaoProcesso(ProcessoTrf processo, ProcessoParteParticipacaoEnum polo){
		return ComponentUtil.getComponent(ProcessoParteManager.class).nomeExibicao(processo, polo);
	}
	

	public void criarFluxoAssinaturaPecaBNMP(ProcessoTrf processo, String siglaFluxo) {
		criarFluxoAssinaturaPecaBNMP(processo, siglaFluxo, null,null);
	}
	
	public void criarFluxoAssinaturaPecaBNMP(ProcessoTrf processo, String siglaFluxo, Integer idLocalizacao) {		
		criarFluxoAssinaturaPecaBNMP(processo, siglaFluxo, idLocalizacao, null);
	}
	
	public void criarFluxoAssinaturaPecaBNMP(ProcessoTrf processo, String siglaFluxo, Integer idLocalizacao, Integer idOrgaoJulgadorColegiado) {		
		criarFluxoAssinaturaPecaBNMP(processo,siglaFluxo,idLocalizacao,idOrgaoJulgadorColegiado,null);	
	}
	
	public void criarFluxoAssinaturaPecaBNMP(ProcessoTrf processo, String siglaFluxo, Map<String, Object> variaveis) {
		criarFluxoAssinaturaPecaBNMP(processo, siglaFluxo, null, null, variaveis);
	}


	public void criarFluxoAssinaturaPecaBNMP(ProcessoTrf processo, String siglaFluxo, Integer idLocalizacao, Integer idOrgaoJulgadorColegiado,Map<String, Object> variaveis) {
		
		PecaMinBnmpRestClient pecaMinBnmpRestClient = ComponentUtil.getComponent(PecaMinBnmpRestClient.class);
		List<PecaMinDTO> pecasPendentes = pecaMinBnmpRestClient.obterPecasPendentesDeAssinatura(processo);
			
		pecasPendentes.stream().forEach(peca -> 
			dispararFluxoBNMP(processo, siglaFluxo, peca, idLocalizacao, idOrgaoJulgadorColegiado, variaveis)			
		);		
	}
	
	public void dispararFluxoBNMP(ProcessoTrf processo, String siglaFluxo,
			PecaMinDTO peca, Integer idLocalizacao, Map<String, Object> variaveis) {
		dispararFluxoBNMP(processo,siglaFluxo,peca,idLocalizacao,null,variaveis);
	}

	public void dispararFluxoBNMP(ProcessoTrf processo, String siglaFluxo,
			PecaMinDTO peca, Integer idLocalizacao, Integer idOrgaoJulgadorColegiado, Map<String, Object> variaveis) {

		if (variaveis == null || variaveis.isEmpty()) {
			variaveis = new HashMap<>();	
		}
		
		FluxoManager fluxoManager = ComponentUtil.getComponent(FluxoManager.class);

		String variavelControleBnmp = Variaveis.ID_PECA_BNMP + "-" + peca.getId();
		try {
			if (!fluxoManager.existeFluxoComVariavel(processo, variavelControleBnmp)) {
				variaveis.put("pje:fluxo:url:bnmp:peca:idPeca", peca.getId());
				variaveis.put("pje:fluxo:url:bnmp:peca:rji", peca.getNumeroIndividuo());
				variaveis.put(Variaveis.ID_PECA_BNMP, peca.getId());
				variaveis.put(variavelControleBnmp, peca.getId());
				incluirNovoFluxo(processo, siglaFluxo, null, idOrgaoJulgadorColegiado, true, variaveis, idLocalizacao);

			}
		} catch (PJeBusinessException e) {
			log.error("Erro ao tentar disparar novo fluxo com os dados da peça BNMP.", e);
		}
	}
	
	
	/**
	 * Método responsável por gravar a variável de tarefa especificada em todas as tarefas ativas do processo
	 * @param processoTrf Dados do processo
	 * @param nomeVariavel Nome da variável
	 * @param valorVariavel Valor da variável
	 */
	public void gravarVariavelTarefaTodasTarefas(ProcessoTrf processoTrf, String nomeVariavel, Object valorVariavel) {
		List<ProcessInstance> lista = getBusinessProcesses(processoTrf, false);
		
		for (ProcessInstance pi : lista) {
			TaskInstance taskInstance = null;
			Token token = pi.getRootToken();
			
			if (pi.getTaskMgmtInstance() != null && pi.getTaskMgmtInstance().getTaskInstances() != null) {
				for (org.jbpm.taskmgmt.exe.TaskInstance t : pi.getTaskMgmtInstance().getTaskInstances()) {
					if (t.getTask().getTaskNode().equals(token.getNode())) {
						taskInstance = t;
					}
				}
			}
			
			if (taskInstance != null) {
				taskInstance.setVariableLocally(nomeVariavel, valorVariavel);
			}
		}
	}

	public boolean existeFluxoDeslocadoParaLocalizacao(ProcessoTrf processoJudicial) {
		List<Integer> idsLocalizacoesFilhas = Authenticator.getIdsLocalizacoesFilhas();
		if (!CollectionUtilsPje.isEmpty(idsLocalizacoesFilhas)) {
			return ProcessoJudicialManager.instance().
					existeFluxoDeslocadoParaLocalizacaoDoUsuario(processoJudicial, StringUtil.listToString(idsLocalizacoesFilhas));
		}
		return false;

	}
	
	/**
	 * Mtodo para verificar o segredo entre dois processos (p1 e p2).
	 */
	public SegredoEntreProcessosJudiciaisEnum verificaSegredo(ProcessoTrf p1, ProcessoTrf p2) {
		if (p1.getDataAutuacao() == null || p2.getDataAutuacao() == null) {
			return SegredoEntreProcessosJudiciaisEnum.SEGREDO_NAO_IDENTIFICADO;
		} else if (p1.getSegredoJustica().booleanValue() && !p2.getSegredoJustica().booleanValue()) {
			return SegredoEntreProcessosJudiciaisEnum.SOMENTE_P1_TRAMITA_EM_SEGREDO;
		} else if (!p1.getSegredoJustica().booleanValue() && p2.getSegredoJustica().booleanValue()) {
			return SegredoEntreProcessosJudiciaisEnum.SOMENTE_P2_TRAMITA_EM_SEGREDO;
		} else if (!p1.getSegredoJustica().booleanValue() && !p2.getSegredoJustica().booleanValue()) {
			return SegredoEntreProcessosJudiciaisEnum.NENHUM_DOS_PROCESSOS_TRAMITAM_EM_SEGREDO;
		} else if (p1.getSegredoJustica().booleanValue() && p2.getSegredoJustica().booleanValue()) {
			if (p1.getDataAutuacao().before(p2.getDataAutuacao())) {
				return SegredoEntreProcessosJudiciaisEnum.OS_DOIS_PROCESSOS_TRAMITAM_EM_SEGREDO_SENDO_P1_MAIS_ANTIGO_QUE_P2;
			} else if (p2.getDataAutuacao().before(p1.getDataAutuacao())) {
				return SegredoEntreProcessosJudiciaisEnum.OS_DOIS_PROCESSOS_TRAMITAM_EM_SEGREDO_SENDO_P2_MAIS_ANTIGO_QUE_P1;
			}
		}
		return SegredoEntreProcessosJudiciaisEnum.SEGREDO_NAO_IDENTIFICADO;
	}		
}
