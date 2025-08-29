/**
 * PoloProcessualParaProcessoParteConverter.java
 * 
 * Data de criação: 10/08/2020
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * Conversor de PoloProcessual para ProcessoParte.
 * 
 * @author Adriano Pamplona
 */
@Name (PoloProcessualParaProcessoParteConverter.NAME)
public class PoloProcessualParaProcessoParteConverter
		extends
		IntercomunicacaoConverterAbstrato<PoloProcessual, List<ProcessoParte>> {

	public static final String NAME = "v222.poloProcessualParaProcessoParteConverter";
	
	/**
	 * @return Instância de PoloProcessualParaProcessoParteConverter.
	 */
	public static PoloProcessualParaProcessoParteConverter instance() {
		return ComponentUtil.getComponent(PoloProcessualParaProcessoParteConverter.class);
	}
	
	@Override
	public List<ProcessoParte> converter(PoloProcessual poloProcessual) {
		List<ProcessoParte> resultado = new ArrayList<>();
		
		if (isNotNull(poloProcessual)) {
			ProcessoParte processoParte = new ProcessoParte();
			
			Map<ModalidadePoloProcessual, ProcessoParteParticipacaoEnum> mapaParticipacao = new HashMap<>();
			mapaParticipacao.put(ModalidadePoloProcessual.AT, ProcessoParteParticipacaoEnum.A);
			mapaParticipacao.put(ModalidadePoloProcessual.PA, ProcessoParteParticipacaoEnum.P);
			mapaParticipacao.put(ModalidadePoloProcessual.TC, ProcessoParteParticipacaoEnum.T);
			mapaParticipacao.put(ModalidadePoloProcessual.TJ, ProcessoParteParticipacaoEnum.N);
			mapaParticipacao.put(ModalidadePoloProcessual.AD, ProcessoParteParticipacaoEnum.N);
			mapaParticipacao.put(ModalidadePoloProcessual.FL, ProcessoParteParticipacaoEnum.N);
			mapaParticipacao.put(ModalidadePoloProcessual.VI, ProcessoParteParticipacaoEnum.N);
			
			ProcessoParteParticipacaoEnum participacao = mapaParticipacao.get(poloProcessual.getPolo());
			processoParte.setInParticipacao(participacao);
			List<Parte> partes = poloProcessual.getParte();
			for (Parte parte : partes) {
				PessoaMNIParaPessoaPJEConverter pessoaConverter = ComponentUtil.getComponent(PessoaMNIParaPessoaPJEConverter.class);
				processoParte.setPessoa(pessoaConverter.converter(parte.getPessoa()));
				
				RepresentanteProcessualParaProcessoParteRepresentanteConverter representanteConverter = RepresentanteProcessualParaProcessoParteRepresentanteConverter.instance();
				processoParte.setProcessoParteRepresentanteList(representanteConverter.converterColecao(parte.getAdvogado()));
				
				resultado.add(processoParte);
			}
		}		
		return resultado;
	}
	
}
