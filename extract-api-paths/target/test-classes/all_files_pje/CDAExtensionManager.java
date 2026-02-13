/**
 * CDAExtensionManager.java.
 *
 * Data: 19/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v223.extensionmanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.pje.action.DadosCDAAction;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v223.cda.CDA;
import br.jus.cnj.intercomunicacao.v223.cda.ColecaoCDA;
import br.jus.cnj.pje.intercomunicacao.v223.converter.CDAParaPjeCdaConverter;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.CdaManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Manager para tratar a extensão do CDA.
 * 
 * @author Adriano Pamplona
 */
public class CDAExtensionManager implements ExtensionManager<ColecaoCDA> {

	@Override
	public void execute(ProcessoTrf processoTrf, ColecaoCDA colecaoCDA) {
		List<Cda> listMNICda = new ArrayList<>();
		boolean isVazio = processoTrf.getColecaoCda().isEmpty();
		DadosCDAAction cdaAction = new DadosCDAAction();
		try {
			if (colecaoCDA != null) {
				List<CDA> cdas = colecaoCDA.getCdas();
				for (CDA mniCDA : cdas) {
					CDAParaPjeCdaConverter converter = CDAParaPjeCdaConverter.instance();
					Cda pjeCDA = converter.converter(mniCDA);
					pjeCDA.setProcessoTrf(processoTrf);
					
					cdaAction.validarCda(pjeCDA);
					
					CdaManager manager = CdaManager.instance();
					manager.persist(pjeCDA);
					
					listMNICda.add(pjeCDA);
					if(isVazio) {
						processoTrf.getColecaoCda().add(pjeCDA);
					}
					
				}
				
				ParametroService parametroService = ComponentUtil.getParametroService();
				
				if(BooleanUtils.toBoolean(parametroService.valueOf(Parametros.PJE_EF_AJUSTAR_VALOR_CAUSA_CDA))){
					somarValorCausa(processoTrf, listMNICda);
				}

			}
		} catch (PJeBusinessException e) {
			throw new PJeRuntimeException(e);
		} catch (NegocioException e) {
			throw new NegocioException(e.getMensagem());
	    }

	}
	
	/**
	 * Soma os valores das CDA's e atualiza o valor da causa do processo.
	 * 
	 * @param processoTrf ProcessoTrf
	 * @param listMNICda Lista das novas CDA's.
	 */
	protected void somarValorCausa(ProcessoTrf processoTrf, List<Cda> listMNICda) {
		BigDecimal valorCausa = BigDecimal.ZERO;
		
		// Recuperando as CDA's do processo para efetuar a soma.
		List<Cda> listProcessoTrfCda = processoTrf.getColecaoCda();
		if (ProjetoUtil.isNotVazio(listProcessoTrfCda)) {
			for (Cda cda : listProcessoTrfCda) {
				if (cda.getAtivo()) {
					valorCausa = valorCausa.add(cda.getValor());
				}
			}
		}
		
		// Loop nas novas CDA's para efetuar a soma e atualizar o valor da causa.
		if (ProjetoUtil.isNotVazio(listMNICda) && listProcessoTrfCda.isEmpty()) {
			for (Cda cda : listMNICda) {
				if (cda.getAtivo()) {
					valorCausa = valorCausa.add(cda.getValor());
				}
			}
		}
		
		if (!valorCausa.equals(BigDecimal.ZERO)) {
			processoTrf.setValorCausa(valorCausa.doubleValue());
		}
	}
}
