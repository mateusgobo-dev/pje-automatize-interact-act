package br.jus.cnj.pje.nucleo.service;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.pjecommons.model.services.bnmp.PecaMinDTO;
import br.jus.cnj.pje.webservice.client.bnmp.PecaMinBnmpRestClient;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class ProcessoJudicialServiceTest {
	
	@InjectMocks
	private ProcessoJudicialService processoJudicialService;

	
	private PecaMinBnmpRestClient pecaMinBnmpRestClient;

	private static MockedStatic<ComponentUtil> componentUtilStatic;
	
	private ProcessoTrf processo = Mockito.mock(ProcessoTrf.class);
	
	@BeforeClass
	public static void staticSetup() {

		componentUtilStatic = mockStatic(ComponentUtil.class);
	}
	
	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
		processoJudicialService = Mockito.spy(processoJudicialService);
	}


	@Test
	public void criarFluxoAssinaturaPecaBNMPTest() {
		
		String fluxoAssinatura = "FLUXO_ASSINATURA";
		pecaMinBnmpRestClient = Mockito.spy(new PecaMinBnmpRestClient());
		when(ComponentUtil.getComponent(PecaMinBnmpRestClient.class)).thenReturn(pecaMinBnmpRestClient);
		doNothing().when(processoJudicialService).dispararFluxoBNMP(eq(processo), eq(fluxoAssinatura), Mockito.any(PecaMinDTO.class), eq(null), eq(null),eq(null));
		
		//DADO que o processo tem três peças pendentes de assinatura
		List<PecaMinDTO> pecas = Arrays.asList(new PecaMinDTO(),new PecaMinDTO(),new PecaMinDTO());
		doReturn(pecas).when(pecaMinBnmpRestClient).obterPecasPendentesDeAssinatura(processo);
		
		//AÇÃO
		processoJudicialService.criarFluxoAssinaturaPecaBNMP(processo, fluxoAssinatura);
			
		//VERIFICA se foram disparados três fluxos para assinatura (um para cada peça)
		verify(processoJudicialService, times(3)).dispararFluxoBNMP(eq(processo), eq(fluxoAssinatura), Mockito.any(PecaMinDTO.class), eq(null), eq(null),eq(null));

	}
	
	@AfterClass
	public static void close() {
		componentUtilStatic.close();
	}


	
}