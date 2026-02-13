
package br.jus.pdpj.notificacao.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.LogNotificacaoManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pdpj.commons.models.dtos.webhooks.WebhookWrapperMessage;
import br.jus.pje.nucleo.entidades.LogNotificacao;

public class WebhookWrapperServiceTest {
	
	private final static String NOTIFICACAO_ID="a81312j…È";
	private final static String NUMERO_UNICO_PROCESSO="1234567.00.1998.8.26.1234";
	private final static String IP_REQUISICAO="0.0.0.11";
	private final static Timestamp DATA_CRIACAO = new Timestamp(1710449723169l);
	
	@Spy
	@InjectMocks
	private WebhookWrapperService webhookWrapperService = new WebhookWrapperService();
	
	@Mock
	private HttpServletRequest httpRequest;
	
	@Mock
	private ParametroService parametroService;
	
	@Mock
	private Logger log;
	
	@Captor
	ArgumentCaptor<LogNotificacao> logCaptor;

	private LogNotificacaoManager logWebHookManager;
	private static MockedStatic<ComponentUtil> componentUtilStatic;
	private static MockedStatic<LogNotificacao> logNotificacaoStatic;
	private static ParametroUtil parametroUtil; 
	@BeforeClass
	public static void staticSetup() {
		componentUtilStatic = mockStatic(ComponentUtil.class);
		logNotificacaoStatic = mockStatic(LogNotificacao.class);
	}
	
	@Before
	public void setUp() throws Exception{
		
		MockitoAnnotations.openMocks(this);
		logWebHookManager = mock(LogNotificacaoManager.class);
		parametroUtil = mock(ParametroUtil.class);
		
		when(ComponentUtil.getComponent(LogNotificacaoManager.class)).thenReturn(logWebHookManager);
		when(LogNotificacao.createNew()).thenReturn(new LogNotificacao());
		when(ParametroUtil.instance()).thenReturn(parametroUtil);
		when(parametroUtil.getNumeroOrgaoJustica()).thenReturn("807");
	}
	
	@Test
	public void logarRequisicaoTest() {
		
		    //DADO
            doReturn(IP_REQUISICAO).when(httpRequest).getRemoteAddr();
      		WebhookWrapperMessage mensagem = new WebhookWrapperMessage();
      		mensagem.setNotificacaoId(NOTIFICACAO_ID);
      		mensagem.setNumeroUnicoProcesso(NUMERO_UNICO_PROCESSO);
 
			 try {
				doNothing().when(logWebHookManager).persistAndFlush(Mockito.any());
				
				 //AO
		    	  webhookWrapperService.logarRequisao(mensagem);
		           
		    	  //VERIFICAO
		    	  verify(logWebHookManager).persistAndFlush(logCaptor.capture());
		    	  assertEquals( NUMERO_UNICO_PROCESSO,logCaptor.getValue().getNrProcesso());
		    	  assertEquals( NOTIFICACAO_ID,logCaptor.getValue().getIdNotificacao());
		    	  assertEquals(IP_REQUISICAO, logCaptor.getValue().getIpRequisicao());
		    	  
			} catch (PJeBusinessException e) {
				fail();
			}
	}
	
	@Test
	public void validaAssinaturaTest() {
		
		doReturn(null).when(parametroService).valueOf("pje:tjdft:notificacao:ignorarValidacaoHash");
		
		//DADO
		WebhookWrapperMessage mensagem = new WebhookWrapperMessage();
		mensagem.setCriadoEm(DATA_CRIACAO);
		mensagem.setNotificacaoId(NOTIFICACAO_ID);
		mensagem.setNumeroUnicoProcesso(NUMERO_UNICO_PROCESSO);
		String chave = "chaveTeste8";
		
		//VALOR ESPERADO para o hash de assinatura com UTF-8 e os valores de chave e mensagem dados.
		String hashAssinaturaRequestUtf = "6ad379edc4594df22d7d3f0079212bf82f093c74d0e990f6d08c0a870f3849f6";		
		String hashAssinaturaRequestIso = "f53e708c6bb96977b0a87c7860e474ff73311ced7c247b318010c209c59231ab";	
		
		try {
			//AO
			boolean assinaturaOk = webhookWrapperService.validaAssinatura(mensagem, hashAssinaturaRequestUtf, chave);
			boolean assinaturaInvalida = webhookWrapperService.validaAssinatura(mensagem, hashAssinaturaRequestIso, chave);
			
			//VERIFICAO
			assertTrue(assinaturaOk);
			assertFalse(assinaturaInvalida);
			
		} catch (JsonProcessingException | InvalidKeyException | NoSuchAlgorithmException e) {
			fail();
		} 
	}
	
	@Test
	public void testarNotificacaoQuandoNumeroDoProcessoPertenceAoTribunal() {
		WebhookWrapperMessage message = new WebhookWrapperMessage();
		message.setNumeroUnicoProcesso("0718288-43.2024.8.07.0001");
		boolean isProcessoPertecenteAoTribunal = webhookWrapperService.isNumeracaoProcessualPertecenteAoTribunalLocal(message);
		assertTrue(isProcessoPertecenteAoTribunal);
	}
	
	@Test
	public void testarNotificacaoQuandoNumeroDoProcessoEstaInvalido() {
		WebhookWrapperMessage message = new WebhookWrapperMessage();
		message.setNumeroUnicoProcesso("0718288-43.2024.8.07.99");
		boolean isProcessoPertecenteAoTribunal = webhookWrapperService.isNumeracaoProcessualPertecenteAoTribunalLocal(message);
		assertFalse(isProcessoPertecenteAoTribunal);
	}
	
	@Test
	public void  testarNotificacaoQuandoNumeroDoProcessoNaoPertenceAoTribunal() {
		WebhookWrapperMessage message = new WebhookWrapperMessage();
		message.setNumeroUnicoProcesso("1065337-30.2023.4.01.3400");
		boolean isProcessoPertecenteAoTribunal = webhookWrapperService.isNumeracaoProcessualPertecenteAoTribunalLocal(message);
		assertFalse(isProcessoPertecenteAoTribunal);
	}
	
	@AfterClass
	public static void close() {
		componentUtilStatic.close();
		logNotificacaoStatic.close();
	}

}
