/**
 * AtualizarPrazosExpedientesAbertos.java
 * 
 * Data: 30/01/2017
 */
package br.jus.cnj.pje.controleprazos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.quartz.QuartzJobsInfo;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Constants;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PessoaInvalidaException;
import br.jus.cnj.pje.nucleo.PjeRestClientException;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;

/**
 * Componente Seam destinado à atualização de prazos dos expedientes abertos. 
 * 
 * @author Adriano Pamplona
 */
@Name("atualizarPrazosExpedientesAbertos")
@Scope(ScopeType.EVENT)
@AutoCreate
public class AtualizarPrazosExpedientesAbertos {

	@Logger
	private Log log;
	
	@In (create = true)
	private PrazosProcessuaisService prazosProcessuaisService;
	
	@In (create = true)
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In (create = true)
	private AtoComunicacaoService atoComunicacaoService;
	
	@In (create = true)
	private QuartzJobsInfo quartzJobsInfo;
	
	@Asynchronous
	public QuartzTriggerHandle execute(@IntervalCron String cron) throws Exception {
		Boolean haErros = Boolean.FALSE;
		
		log.info("Processando a atualização da data de prazo legal para os expedientes abertos.");
		Util.beginAndJoinTransaction();
		List<ProcessoParteExpediente> expedientes = consultarExpedientesAbertos();
		for (int indice = 0; indice < expedientes.size(); indice++) {
            try{
    			ProcessoParteExpediente ppe = expedientes.get(indice);
    			Date prazoLegal = null;

				Boolean houveCiencia = BooleanUtils.isTrue(ppe.getDtCienciaParte() != null);

    			if(houveCiencia) {
    				prazoLegal = obterDataPrazoLegalProcessual(ppe);
    			}
    			else {
    				//Calcula a ciência somente para os expedientes do tipo Sistema (eletrônico)
    				if(ppe.getProcessoExpediente().getMeioExpedicaoExpediente().equals(ExpedicaoExpedienteEnum.E)) {
    					prazoLegal = obterDataPrazoLegalCiencia(ppe);
    					if(prazoLegal.after(ppe.getDtPrazoLegal())) {
    						DomicilioEletronicoService.instance().alterarDataFinalCiencia(ppe, prazoLegal);
    					}
    				}
    				//Demais tipos de comunicação (correios, diário etc.) não devem ter seus prazos de ciência recalculados
    				else {
    					continue;
    				}
    			}
    			
    			log.debug("\t- Expediente: id={0}, dtPrazoLegal anterior:{1}, dtPrazoLegal atualizado:{2}.", 
    					ppe.getIdProcessoParteExpediente(),
    					ppe.getDtPrazoLegal(),
    					prazoLegal);

    			ppe.setDtPrazoLegal(prazoLegal);
    			verificarNecessidadeDeCommit(indice);
            }catch (RuntimeException e){
                Util.rollbackAndOpenJoinTransaction();
                String texto = String.format("Não foi possível atualizar o prazo legal do  processoParteExpediente [%d].", expedientes.get(indice));
				log.error(texto);
				haErros = Boolean.TRUE;
            }
		}
		doAtualizacaoDePrazosFinalizado(expedientes, haErros);

		Util.commitTransction();
		
		log.info("Atualização da data de prazo legal para os expedientes abertos finalizado.");
			
		return null;
	}

	/**
	 * Consulta os expedientes abertos.
	 * 
	 * @return expedientes abertos.
	 * @throws PJeBusinessException
	 */
	protected List<ProcessoParteExpediente> consultarExpedientesAbertos() throws PJeBusinessException {
		return processoParteExpedienteManager.getAtosComunicacaoPendentes();
	}

	/**
	 * Consulta os meios de comunicação.
	 * 
	 * @return lista dos meios de comunicação.
	 */
	protected List<ExpedicaoExpedienteEnum> consultarMeiosDeComunicacao() {
		List<ExpedicaoExpedienteEnum> resultado = new ArrayList<ExpedicaoExpedienteEnum>(2);
		
        resultado.add(ExpedicaoExpedienteEnum.E);
        if (ParametroUtil.instance().getPresuncaoEntregaCorrespondenciaInteger() > 0 || 
        	ParametroUtil.instance().isJusticaTrabalhista()) {
        	resultado.add(ExpedicaoExpedienteEnum.C);
        }
        
        return resultado;
	}
	
	
	/**
	 * Retorna da data do prazo legal processual.
	 * 
	 * @param ppe ProcessoParteExpediente
	 * @return data do prazo legal.
	 */
	protected Date obterDataPrazoLegalProcessual(ProcessoParteExpediente ppe) {
		Date resultado = null;
		
		Map<Integer, Calendario> mapaCalendarios = prazosProcessuaisService.obtemMapaCalendarios();
		ProcessoTrf processoJudicial = ppe.getProcessoJudicial();
		Competencia competencia = processoJudicial.getCompetencia();
		CategoriaPrazoEnum categoriaPrazoProcessual = competencia.getCategoriaPrazoProcessual();

		Calendario calendario = mapaCalendarios.get(processoJudicial.getOrgaoJulgador().getIdOrgaoJulgador());
		if (calendario != null){
			resultado = prazosProcessuaisService.calculaPrazoProcessual(
					ppe.getDtCienciaParte(), 
					ppe.getPrazoLegal(), 
					ppe.getTipoPrazo(), 
					calendario, 
					categoriaPrazoProcessual, 
					ContagemPrazoEnum.M);
		}
		return resultado;
	}


	/**
	 * Retorna a data do prazo legal da ciência.
	 * 
	 * @param ppe ProcessoParteExpediente
	 * @return data do prazo legal.
	 * @throws PJeBusinessException
	 * @throws PjeRestClientException 
	 * @throws PessoaInvalidaException 
	 */
	protected Date obterDataPrazoLegalCiencia(ProcessoParteExpediente ppe) throws PJeBusinessException, PjeRestClientException, PessoaInvalidaException{
		Integer prazoPresuncaoCorreios = NumberUtils.createInteger(ParametroUtil.instance().getPresuncaoEntregaCorrespondencia());
		Map<Integer, Calendario> mapaCalendarios = prazosProcessuaisService.obtemMapaCalendarios();
		Boolean forcarPresuncaoCorreios = ParametroUtil.instance().isJusticaTrabalhista();	
		return atoComunicacaoService.obterDataPrazoLegalCiencia(
				ppe, 
				prazoPresuncaoCorreios, 
				mapaCalendarios, 
				forcarPresuncaoCorreios);
	}
	
	/**
	 * Efetua o commit a cada Constants.HIBERNATE_BATCH_SIZE ciclos.
	 * 
	 * @param contador
	 */
	protected void verificarNecessidadeDeCommit(int contador) {
        if (Util.isTransactionMarkedRollback()){
            Util.rollbackAndOpenJoinTransaction();
        } else if (contador > 0 && ((contador % Constants.HIBERNATE_BATCH_SIZE) == 0)){
        	EntityUtil.flush();
            Util.commitAndOpenJoinTransaction();
        }
    }
	
	/**
	 * Método gancho responsável por executar alguma ação após o processamento dos registros.
	 * 
	 * @param expedientes Expedientes processados.
	 * @param haErros True se houve erros durante o processamento.
	 */
	protected void doAtualizacaoDePrazosFinalizado(List<ProcessoParteExpediente> expedientes, Boolean haErros) {
		// Faz nada.
	}
	
	/**
	 * Classe estática com as constantes dos atributos/métodos da classe.
	 *
	 */
	public static final class ATTR {
		
		/**
		 * Contrutor
		 * 
		 */
		private ATTR() {
			// Construtor.
		}
		
		public static final String EXECUTE = "execute";
	}
}
