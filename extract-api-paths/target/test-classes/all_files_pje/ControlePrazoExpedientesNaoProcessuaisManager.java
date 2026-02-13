package br.jus.cnj.pje.util;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.business.dao.ControlePrazoExpedientesNaoProcessuaisDAO;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


/**
 * Classe responsável por controlar os eventos relativos ao processo quando o mesmo está com prazo de expediente nao processual vencido
 * @author luiz.mendes
 *
 */
@Name("controlePrazoExpedientesNaoProcessuaisManager")
@Scope(ScopeType.EVENT)
public class ControlePrazoExpedientesNaoProcessuaisManager {
	@Logger
	private Log log;
	
	@In(create=true, required=false)
	private ControlePrazoExpedientesNaoProcessuaisDAO controlePrazoExpedientesNaoProcessuaisDAO;
	
	@In(create=true, required=false)
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In(required = false)
	private TaskInstanceHome taskInstanceHome;
	
	public boolean salvaVariaveisETramitaProcesso(Date dataPretendida, ProcessoTrf processo) {
		boolean retorno = false;
		if(dataPretendida != null && processo != null) {
			if(salvarVariaveisAguardarPrazo(dataPretendida)) {
				retorno = endTask();
			} else {
				log.error("-----------------------------");
				log.error("HOUVE UM ERRO AO SALVAR A DATA PARA AGUARDAR O PRAZO NÃO PROCESSUAL.");
				log.error("-----------------------------");
			}
		} else {
			log.error("-----------------------------");
			log.error("HOUVE UM ERRO AO SALVAR A DATA PARA AGUARDAR O PRAZO NÃO PROCESSUAL - OS PARAMETROS SÃO INVALIDOS.");
			log.error("-----------------------------");
		}
		return retorno;
	}
	
	public boolean endTask() {
		boolean retorno = false;
		if (taskInstanceHome != null) {
			String transicaoSaida = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			taskInstanceHome.end(transicaoSaida);
			retorno = true;
		}
		return retorno;
	}
	
	/**
	 * metodo para salvar a variavel de data do prazo do expediente nao processual
	 * @param tipoPrazo
	 * @param dataPretendida
	 * @throws Exception
	 */
	private boolean salvarVariaveisAguardarPrazo(Date dataPretendida) {
		try {
			tramitacaoProcessualService.gravaVariavel(Variaveis.NOME_VARIAVEL_DIA_PRAZO, dataPretendida);
			return true;
		} catch (Exception e) {
			log.error("-----------------------------");
			log.error("ocorreu um erro ao tentar salvar a variavel {0} com a data {1}", Variaveis.NOME_VARIAVEL_DIA_PRAZO, dataPretendida);
			log.error("-----------------------------");
			return false;
		}
	}
	
	public List<Integer> recuperaIdProcessosTrfPrazoExpirado(Date dataExpiracao){
		return controlePrazoExpedientesNaoProcessuaisDAO.recuperaIdProcessosTrfPrazoExpirado(dataExpiracao);
	}

}