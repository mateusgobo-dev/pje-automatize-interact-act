/**
 * AtualizarPrazosExpedientesAbertosDomicilioEletronico.java
 * 
 * Data: 30/05/2023
 */
package br.jus.cnj.pje.controleprazos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.csjt.pje.business.service.CalendarioEventoService;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

/**
 * Componente Seam destinado à atualização de prazos dos expedientes abertos dos expedientes enviados ao Domicílio Eletrônico. 
 * 
 * @author Adriano Pamplona
 */
@Name("atualizarPrazosExpedientesAbertosDomicilioEletronico")
@Scope(ScopeType.EVENT)
@AutoCreate
public class AtualizarPrazosExpedientesAbertosDomicilioEletronico extends AtualizarPrazosExpedientesAbertos {

	@In (create = true)
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In (create = true)
	private CalendarioEventoService calendarioEventoService;
	
	
	/**
	 * Consulta os expedientes abertos.
	 * 
	 * @return expedientes abertos.
	 * @throws PJeBusinessException
	 */
	@Override
	protected List<ProcessoParteExpediente> consultarExpedientesAbertos() throws PJeBusinessException {
		List<ProcessoParteExpediente> expedientes = new ArrayList<>();
		
		// O job será processado somente se a tabela de eventos tiver evento lançado e sem prazo recalculado.
		if (calendarioEventoService.isRecalcularPrazos()) {
			expedientes = processoParteExpedienteManager.getAtosComunicacaoPendentesDomicilioEletronico();			
		}
		return expedientes;
	}
	
	@Override
	protected void doAtualizacaoDePrazosFinalizado(List<ProcessoParteExpediente> expedientes, Boolean haErros) {
		// Se não houve erros no processamento e existe expedientes processados ENTÃO define que os prazos foram calculados.
		if (BooleanUtils.isFalse(haErros) && ProjetoUtil.isNotVazio(expedientes)) {
			calendarioEventoService.prazosRecalculados();
		}
	}
}
