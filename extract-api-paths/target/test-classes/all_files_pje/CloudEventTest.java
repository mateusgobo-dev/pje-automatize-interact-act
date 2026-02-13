package br.jus.cnj.pje.amqp;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.contexts.Lifecycle;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.jus.cnj.pje.amqp.model.dto.CloudEventBuilder;
import br.jus.cnj.pje.amqp.model.dto.ProcessoParteCloudEvent;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;

public class CloudEventTest {
	
	@Before
	public void initialize() {
		Map<String, Object> appCtx = new HashMap<>();
		appCtx.put(Parametros.NUMERO_ORGAO_JUSTICA, "403");
		appCtx.put(Parametros.APLICACAOSISTEMA, "2");
		
		Lifecycle.setupApplication(appCtx);
	}

	@Test
	public void test1_criarCloudEvent() throws JsonProcessingException, PJeBusinessException {
		CloudEvent ce = CloudEventBuilder.instance()
				.ofPayloadType(ProcessoParteCloudEvent.class)
				.withEntity(this.getProcessoParte())
				.withEvent(CloudEventVerbEnum.POST)
				.build();
		
		assertEquals("403.2.pje-legacy.ProcessoParte.POST", ce.getRoutingKey());
	}
	
	private ProcessoParte getProcessoParte() {
		ProcessoParte pa = new ProcessoParte();
		
		pa.setIdProcessoParte(1);
		pa.setProcessoTrf(this.getProcessoTrf());
		pa.setInSituacao(ProcessoParteSituacaoEnum.A);
		pa.setPessoa(this.getPessoa());
		
		return pa;
	}
	
	private ProcessoTrf getProcessoTrf() {
		Processo proc = new Processo();
		proc.setNumeroProcesso("5000014-10.2019.4.03.0000");
		
		ProcessoTrf procTrf = new ProcessoTrf();
		
		procTrf.setIdProcessoTrf(1);
		procTrf.setProcesso(proc);
		
		return procTrf;
		
	}
	
	private Pessoa getPessoa() {
		Pessoa pess = new Pessoa();
		
		pess.setIdPessoa(1);
		
		return pess;
	}
	
}
