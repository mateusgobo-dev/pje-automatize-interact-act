/**
 * 
 */
package br.jus.cnj.pje.nucleo.observer;

import java.util.Date;

import javax.persistence.LockModeType;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.ApplicationContext;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoDependenciaEleitoralManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.ItemsLog;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.enums.CriticidadeEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;

/**
 * @author eduardo.pereira
 *
 */
@Name("redistribuicaoLoteObserver")
public class RedistribuicaoLoteObserver {
	
	@Logger
	private Log logger;
	
	@In
	private ApplicationContext applicationContext;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private OrgaoJulgadorCargoManager orgaoJulgadorCargoManager;
	
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	@In
	private VinculacaoDependenciaEleitoralManager vinculacaoDependenciaEleitoralManager;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In(required=false)
	private ProcessInstance processInstance;
	
	
	@In(create = true)
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In(create = true)
	private FluxoManager fluxoManager;
	
	private ItemsLog item;
	
	@Observer(value={Eventos.REDISTRIBUIR_PROCESSO})
	@Transactional()
	public void distribuir(Integer idProcesso, Integer idCargo, Integer idColegiado, Integer cargoOrigemId, Integer colegiadoOrigem, TipoDistribuicaoEnum tipoDistribuicaoEnum,TipoRedistribuicaoEnum tipoRedistribuicaoEnum,String usuarioLogin){
		try{
			ProcessoTrf processo = processoJudicialService.findById(idProcesso);
			logger.info(Severity.INFO, "Redistribuição por prevenção do processo [{0}] iniciada...",processo.getNumeroProcesso());
			
			ProcessoTrfLogDistribuicao logDist = new ProcessoTrfLogDistribuicao();
			
			OrgaoJulgadorCargo cargo = orgaoJulgadorCargoManager.findById(idCargo);
			OrgaoJulgador orgaoJulgador = cargo.getOrgaoJulgador(); 

			OrgaoJulgadorCargo cargoOrigem = orgaoJulgadorCargoManager.findById(cargoOrigemId);
			OrgaoJulgador orgaoJulgadorOrigem = cargoOrigem.getOrgaoJulgador();
			
			processo.setOrgaoJulgadorCargo(cargo);
			processo.setOrgaoJulgador(orgaoJulgador);
			
			logger.info(Severity.INFO, "Redistribuição por prevenção do processo [{0}]: Alterando a vinculação da cadeia de processos...",processo.getNumeroProcesso());
			VinculacaoDependenciaEleitoral vinculacaoDependenciaEleitoral = vinculacaoDependenciaEleitoralManager.recuperaVinculacaoDependencia(processo);
			vinculacaoDependenciaEleitoral.setCargoJudicial(cargo);
			
			if(idColegiado != null){
				OrgaoJulgadorColegiado colegiado = orgaoJulgadorColegiadoManager.findById(idColegiado);
				processo.setOrgaoJulgadorColegiado(colegiado);
			}
			
			logger.info(Severity.INFO, "Redistribuição por prevenção do processo [{0}]: Preenchimento das informações de redistribuição",processo.getNumeroProcesso());
			
			carregarEntidadeRedistribuicao(tipoDistribuicaoEnum,tipoRedistribuicaoEnum, processo, orgaoJulgador,orgaoJulgadorOrigem,usuarioLogin);
			
			// Lancar as movimentacoes de redistribuicao
			logger.info(Severity.INFO, "Redistribuição por prevenção do processo [{0}]: Criação dos items de log do processo e log de redistribuição...",processo.getNumeroProcesso());
			
			logDist.setProcessoTrf(processo);
			logDist.setInTipoDistribuicao(TipoDistribuicaoEnum.PP);

			item = criarNovoItemsLogInfo(logDist);
			item.setItem("Redistribuição por prevenção conforme Art. 260 do Código Eleitoral.");
			logDist.getItemsLogList().add(item);

			item = criarNovoItemsLogInfo(logDist);
			String msgTipoOrgaoJulgador = "Órgão julgador prevento na redistribuição por prevenção: ";
			item.setItem(msgTipoOrgaoJulgador + orgaoJulgador);
			logDist.getItemsLogList().add(item);

			item = criarNovoItemsLogInfo(logDist);
			String msgTipoCargoSorteado = "Cargo Prevento na Redistribuição por prevenção: ";
			item.setItem(msgTipoCargoSorteado + cargo.getDescricao());
			logDist.getItemsLogList().add(item);

			logDist.setOrgaoJulgador(orgaoJulgador);
			logDist.setOrgaoJulgadorCargo(cargo);
			
			processo.setProcessoStatus(ProcessoStatusEnum.D);
			processo.setDataDistribuicao(new Date());
			processo.setPessoaRelator(null);

			EntityUtil.getEntityManager().persist(logDist);
			EntityUtil.getEntityManager().merge(vinculacaoDependenciaEleitoral);
			
			EntityUtil.getEntityManager().lock(cargoOrigem, LockModeType.WRITE);
			EntityUtil.getEntityManager().merge(cargoOrigem);
			
			EntityUtil.getEntityManager().lock(cargo, LockModeType.WRITE);
			EntityUtil.getEntityManager().merge(cargo);
			
			EntityUtil.getEntityManager().merge(processo);
			
			processoJudicialManager.ajustarFluxo(idProcesso);
			
			lancarMovimentoRedistribuicao260(processo,tipoRedistribuicaoEnum);
			
			processoJudicialService.sinalizarFluxo(processo, Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION, "Término", false, false);
			
			EntityUtil.getEntityManager().flush();
			
			logger.info(Severity.INFO, "Redistribuição por prevenção do processo [{0}]: Processo redistribuido para [{1}]!",processo.getNumeroProcesso(),orgaoJulgador.getOrgaoJulgador());
			
		}catch (Exception e){
			logger.error(Severity.ERROR, "Erro na Redistribuição por prevenção: [{0}].", e.getLocalizedMessage());
		}finally{
			synchronized (applicationContext) {
				long numero = (Long) applicationContext.get("pje:redistribuicao:lote:numeroProcessos");
				numero = numero - 1;
				if(numero == 0){
					// desligar o semáforo e remover as variaveis do contexto da aplicação
					applicationContext.remove("pje:semaforo:redistribuir:lote");
					applicationContext.remove("pje:redistribuicao:lote:numeroProcessos");
					logger.info(Severity.INFO, "Redistribuição por prevenção: Semaforos desligados.");				
					logger.info(Severity.INFO, "Redistribuição por prevenção: Fim da redistribuição em lote.");
				}else{
					applicationContext.set("pje:redistribuicao:lote:numeroProcessos", numero);
				}
			}
		}
	}

	private void carregarEntidadeRedistribuicao(TipoDistribuicaoEnum tipoDistribuicaoEnum,TipoRedistribuicaoEnum tipoRedistribuicaoEnum,
			ProcessoTrf processo, OrgaoJulgador orgaoJulgador,OrgaoJulgador orgaoJulgadorOrigem,String usuarioLogin) {
		ProcessoTrfRedistribuicao processoTrfRedistribuicao = new ProcessoTrfRedistribuicao();
		processoTrfRedistribuicao.setProcessoTrf(processo);
		processoTrfRedistribuicao.setOrgaoJulgador(orgaoJulgador);
		processoTrfRedistribuicao.setOrgaoJulgadorAnterior(orgaoJulgadorOrigem);
		processoTrfRedistribuicao.setMotivoRedistribuicao(getDescricaoMotivoRedistribuicao(tipoRedistribuicaoEnum));
		processoTrfRedistribuicao.setInTipoDistribuicao(tipoDistribuicaoEnum);
		processoTrfRedistribuicao.setInTipoRedistribuicao(tipoRedistribuicaoEnum);
		processoTrfRedistribuicao.setDataRedistribuicao(new Date());
		processoTrfRedistribuicao.setUsuario(usuarioManager.findByLogin(usuarioLogin));
		EntityUtil.getEntityManager().persist(processoTrfRedistribuicao);
	}

	private String getDescricaoMotivoRedistribuicao(TipoRedistribuicaoEnum tipoRedistribuicaoEnum) {
		String motivo = "Por "+tipoRedistribuicaoEnum.getLabel();
		return motivo;
	}

	private ItemsLog criarNovoItemsLogInfo(ProcessoTrfLogDistribuicao logDist) {
		item = new ItemsLog();
		item.setProcessoTrfLog(logDist);
		item.setInCriticidade(CriticidadeEnum.I);
		return item;
	}
	
	private void lancarMovimentoRedistribuicao260(ProcessoTrf processo,TipoRedistribuicaoEnum tipoRedistribuicaoEnum) throws PJeBusinessException{
		if(processo == null || tipoRedistribuicaoEnum == null){
			throw new PJeBusinessException("Não foi possivel efetuar o lançamento de movimentação para a redistribuição em lote.");
		}
		
		//Redistribuído por #{tipo_de_distribuicao_redistribuicao} em razão de #{motivo_da_redistribuicao}
		MovimentoAutomaticoService
		.preencherMovimento()
		.deCodigo(CodigoMovimentoNacional.COD_MOVIMENTO_REDISTRIBUICAO)
		.comComplementoDeNome(CodigoMovimentoNacional.NOME_COMPLEMENTO_TIPO_DE_DISTRIBUICAO_REDISTRIBUICAO).doTipoDominio().preencherComElementoDeCodigo(getCodigoElementoDominioPrevencao260(processo))
		.comComplementoDeNome(CodigoMovimentoNacional.NOME_COMPLEMENTO_MOTIVO_DA_REDISTRIBUICAO).preencherComTexto(tipoRedistribuicaoEnum.getLabel())
		.associarAoProcesso(processo)
		.lancarMovimento();
	}
	
	private String getCodigoElementoDominioPrevencao260(ProcessoTrf processo) throws PJeBusinessException{
		if(processo == null || processo.getComplementoJE() == null){
			throw new PJeBusinessException("Não foi possivel efetuar o lançamento de movimentação para a redistribuição em lote, sem dados eleitorais.");
		}
		return (processo.getComplementoJE().getEleicao().isGeral()) ? CodigoMovimentoNacional.COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.PREVENCAO_ART260_ELEICAO_ESTADUAL : CodigoMovimentoNacional.COD_COMPLEMENTO_TIPO_DISTRIBUICAO_REDISTRIBUICAO.PREVENCAO_ART260_ELEICAO_MUNICIPAL;
	}
	
}