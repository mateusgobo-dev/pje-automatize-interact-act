/**
 * ConsultaPJeTest.java
 * 
 * Data: 05/01/2015
 */
package br.jus.cnj.pje.ws;

import java.net.URL;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.jus.cnj.pje.intercomunicacao.util.LogMessageSOAPHandler;

/**
 * Classe de teste da interface do ConsultaPJe.
 * 
 * @author Adriano Pamplona
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class ConsultaPJeTest {

	private static String URL_WSDL = "http://localhost:8080/pje/ConsultaPJe?wsdl";
	//private static String URL_WSDL = "http://wwwh.cnj.jus.br/pjemni/ConsultaPJe?wsdl";
	//private static String URL_WSDL = "http://localhost:8080/pje-web/ConsultaPJe?wsdl";
	private static Boolean HABILITAR_EXIBIR_SOAP = Boolean.FALSE;
	private static ConsultaPJe consultaPJe = null;
	private static String ID_PAPEL_ADVOGADO = "advogado";
	
	static {
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
		new javax.net.ssl.HostnameVerifier() {
			@Override
			public boolean verify(String hostname,
			javax.net.ssl.SSLSession sslSession) {
				if (hostname.equals("localhost")) {
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * Teste da consulta de prioridades.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test1_ConsultarPrioridadeProcesso() throws Exception {
		System.out.println("\n test1_ConsultarPrioridadeProcesso: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
		List<PrioridadeProcesso> resposta = consultaPJe.consultarPrioridadeProcesso();
		
		System.out.println(String.format("\n Retornaram %d prioridades processuais.", resposta.size()));		
		Assert.assertNotNull(resposta);
		Assert.assertFalse(resposta.isEmpty());		
	}

	
	/**
	 * Teste da consulta de jurisdições.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test2_ConsultarJurisdicoes() throws Exception {
		System.out.println("\n test2_ConsultarJurisdicoes: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
		List<Jurisdicao> resposta = consultaPJe.consultarJurisdicoes();
		
		System.out.println(String.format("\n Retornaram %d jurisdições.", resposta.size()));		
		Assert.assertNotNull(resposta);
		Assert.assertFalse(resposta.isEmpty());		
	}

	/**
	 * Teste da consulta de classes judiciais.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test3_ConsultarClassesJudiciais() throws Exception {
		System.out.println("\n test3_ConsultarClassesJudiciais: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();

		// Consultando as classes judiciais vinculadas à primeira Jurisdição da lista.
		List<Jurisdicao> respostaJurisdicao = consultaPJe.consultarJurisdicoes();
		Jurisdicao juris = respostaJurisdicao.get(0); // Obtém a primeira Jurisdição da lista.
		List<ClasseJudicial> respostaClassesJuris = consultaPJe.consultarClassesJudiciais(juris);		
		
		System.out.println(String.format("\n Retornaram %d classes judiciais para jurisdição %s", respostaClassesJuris.size(), juris.getDescricao()));		
		Assert.assertNotNull(respostaClassesJuris);
		Assert.assertFalse(respostaClassesJuris.isEmpty());			
	}

	/**
	 * Teste da consulta de órgãos julgadores.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test4_ConsultarOrgaosJulgadores() throws Exception {
		System.out.println("\n test4_ConsultarOrgaosJulgadores: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
				
		List<OrgaoJulgador> respostaOrgaos = consultaPJe.consultarOrgaosJulgadores();
		
		System.out.println(String.format("\n Retornaram %d órgãos julgadores.", respostaOrgaos.size()));		
		Assert.assertNotNull(respostaOrgaos);
		Assert.assertFalse(respostaOrgaos.isEmpty());		
	}

	
	/**
	 * Teste da consulta de órgãos julgadores colegiados.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test5_consultarOrgaosJulgadoresColegiados() throws Exception {
		System.out.println("\n test5_consultarOrgaosJulgadoresColegiados: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
				
		List<OrgaoJulgadorColegiado> respostaOrgaos = consultaPJe.consultarOrgaosJulgadoresColegiados();
		
		System.out.println(String.format("\n Retornaram %d órgãos julgadores colegiados.", respostaOrgaos.size()));		
		Assert.assertNotNull(respostaOrgaos);
		Assert.assertFalse(respostaOrgaos.isEmpty());	
	}

	
	/**
	 * Teste da consulta de tipos de audiência.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test6_consultarTiposAudiencia() throws Exception {
		System.out.println("\n test6_consultarTiposAudiencia: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
				
		List<TipoAudiencia> resposta = consultaPJe.consultarTiposAudiencia();
		
		System.out.println(String.format("\n Retornaram %d tipos de audiência.", resposta.size()));		
		Assert.assertNotNull(resposta);
		Assert.assertFalse(resposta.isEmpty());	
	}

	
	/**
	 * Teste da consulta de salas de audiência.
	 * 
	 * @throws Exception
	 */
	//@Test
	public void test7_consultarSalasAudiencia() throws Exception {
		System.out.println("\n test7_consultarSalasAudiencia: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
	
		// Consultando todas as salas de audiência
		List<SalaAudiencia> respostaSalas = consultaPJe.consultarSalasAudiencia(null);		
		System.out.println(String.format("\n Retornaram %d sala(s) de audiência.", respostaSalas.size()));		
		Assert.assertNotNull(respostaSalas);
		Assert.assertFalse(respostaSalas.isEmpty());	
		
		// Consultando as salas de audiência do primeiro Órgão Julgador da lista.
		List<OrgaoJulgador> respostaOrgaos = consultaPJe.consultarOrgaosJulgadores();
		OrgaoJulgador orgao = respostaOrgaos.get(0); // Obtém o primeiro Órgão Julgador da lista.

		if (orgao != null){
			List<SalaAudiencia> respostaSalaOrgao = consultaPJe.consultarSalasAudiencia(orgao);		
			System.out.println(String.format("\n Retornaram %d sala(s) de audiência para o órgão %s.", respostaSalaOrgao.size(), orgao.getDescricao()));		
			Assert.assertNotNull(respostaSalaOrgao);
			Assert.assertFalse(respostaSalaOrgao.isEmpty());
		}
	}

	
	/**
	 * Teste da consulta de tipos de documentos.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test8_consultarTiposDocumentoProcessual() throws Exception {
		System.out.println("\n test8_consultarTiposDocumentoProcessual: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
	
		// Consultando todos os tipos de documentos.
		List<TipoDocumentoProcessual> respostaDocs = consultaPJe.consultarTiposDocumentoProcessual(null);		
		System.out.println(String.format("\n Retornaram %d tipo(s) de documento.", respostaDocs.size()));		
		Assert.assertNotNull(respostaDocs);
		Assert.assertFalse(respostaDocs.isEmpty());	
		
		// Consultando os tipos de documentos do papel Advogado.
		// TODO: melhorar esta parte quando a issue PJEII-19627 for concluída.		
		List<TipoDocumentoProcessual> respostaDocsPorPapel = consultaPJe.consultarTiposDocumentoProcessual(ID_PAPEL_ADVOGADO);		
		System.out.println(String.format("\n Retornaram %d tipo(s) de documento para o papel %s.", respostaDocsPorPapel.size(), ID_PAPEL_ADVOGADO));		
		Assert.assertNotNull(respostaDocsPorPapel);
		Assert.assertFalse(respostaDocsPorPapel.isEmpty());					
	}

	/**
	 * Teste da consulta de assuntos judiciais.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test9_consultarAssuntosJudiciais() throws Exception {
		System.out.println("\n test9_consultarAssuntosJudiciais: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();

		// Consultando os assuntos judiciais vinculadas à primeira Jurisdição da lista e também à primeira classe da lista.
		List<Jurisdicao> respostaJurisdicao = consultaPJe.consultarJurisdicoes();
		Jurisdicao juris = new Jurisdicao();
		juris = respostaJurisdicao.get(0); // Obtém a primeira Jurisdição da lista.
		System.out.println(String.format("\n Jurisdição: %s", juris.getDescricao())); 
		
		
		List<ClasseJudicial> respostaClasse = consultaPJe.consultarClassesJudiciais(juris);				
		if (respostaClasse.size() > 0){
			ClasseJudicial classe = respostaClasse.get(0); // Obtém a primeira Classe da lista para primeira Jurisdição da lista anterior.
			System.out.println(String.format("\n Classe Judicial: %s", classe.getDescricao()));			
			List<AssuntoJudicial> respostaAssuntosJuris = consultaPJe.consultarAssuntosJudiciais(juris, classe);		
			System.out.println(String.format("\n Retornaram %d assuntos judiciais para jurisdição %s e classe judicial %s", 
				respostaAssuntosJuris.size(), 
				juris.getDescricao(), 
				classe.getDescricao()));		
			Assert.assertNotNull(respostaAssuntosJuris);
			Assert.assertFalse(respostaAssuntosJuris.isEmpty());
		}
	}

	/**
	 * Teste da consulta de competências.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test10_consultarCompetencias() throws Exception {
		System.out.println("\n test10_consultarCompetencias: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();

		// Consultando as competências vinculadas à primeira Jurisdição da lista respostaJurisdicao.
		List<Jurisdicao> respostaJurisdicao = consultaPJe.consultarJurisdicoes();
		Jurisdicao juris = respostaJurisdicao.get(0); // Obtém a primeira Jurisdição da lista.
		List<Competencia> respostaComp = consultaPJe.consultarCompetencias(juris, null, null);
		System.out.println(String.format("\n Retornaram %d competências para jurisdição %s", respostaComp.size(), juris.getDescricao()));		
		Assert.assertNotNull(respostaComp);
		Assert.assertFalse(respostaComp.isEmpty());			
		
		// Consultando as competências vinculadas à Jurisdição obtida da lista respostaJurisdicao mais classe judicial.
		// Obtendo a primeira classe judicial da lista respostaClasse para Jurisdição obtida da lista respostaJurisdicao.
		List<ClasseJudicial> respostaClasse = consultaPJe.consultarClassesJudiciais(juris);				
		if (respostaClasse.size() > 0){
			ClasseJudicial classe = respostaClasse.get(0); // Obtém a primeira Classe da lista.
			List<Competencia> respostaCompClasse = consultaPJe.consultarCompetencias(juris, classe, null);
			System.out.println(String.format("\n Retornaram %d competências para jurisdição %s e classe judicial %s", 
					respostaCompClasse.size(), juris.getDescricao(), classe.getDescricao()));		
			Assert.assertNotNull(respostaCompClasse);
			Assert.assertFalse(respostaCompClasse.isEmpty());
			
			// Consultando as competências vinculadas à Jurisdição obtida da lista respostaJurisdicao mais classe judicial e coleção de assuntos.
			// Obtendo a coleção de assuntos judiciais para Jurisdição obtida da lista respostaJurisdicao e Classe Judicial obtida da lista respostaClasse.
			List<AssuntoJudicial> respostaAssuntosJuris = consultaPJe.consultarAssuntosJudiciais(juris, classe);	
			if (respostaAssuntosJuris.size() > 0){
				List<Competencia> respostaCompClasseAssunto = consultaPJe.consultarCompetencias(juris, classe, respostaAssuntosJuris);
				System.out.println(String.format("\n Retornaram %d competências para jurisdição %s, classe judicial %s e uma coleção de assuntos.",
						respostaCompClasseAssunto.size(),
						juris.getDescricao(), 
						classe.getDescricao()));
				Assert.assertNotNull(respostaCompClasseAssunto);
				Assert.assertFalse(respostaCompClasseAssunto.isEmpty());
			}
		}
	}

	
	/**
	 * Teste da recuperação do XML do fluxo configurado para a classe judicial informada.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test11_recuperarInformacoesFluxo() throws Exception {
		System.out.println("\n test11_recuperarInformacoesFluxo: ----------------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();

		// Obtendo a primeira Jurisdição da lista respostaJurisdicao.
		List<Jurisdicao> respostaJurisdicao = consultaPJe.consultarJurisdicoes();
		Jurisdicao juris = respostaJurisdicao.get(0); // Obtém a primeira Jurisdição da lista.
				
		// Obtendo a primeira classe judicial da lista respostaClasse para Jurisdição obtida da lista respostaJurisdicao.
		List<ClasseJudicial> respostaClasse = consultaPJe.consultarClassesJudiciais(juris);				
		if (respostaClasse.size() > 0){
			ClasseJudicial classe = respostaClasse.get(0); // Obtém a primeira Classe da lista.

			if (classe != null){
				// Obter o XML do fluxo configurado para classe.
				Fluxo respostaXML = consultaPJe.recuperarInformacoesFluxo(classe);
				System.out.println(String.format("\n Fluxo da classe judicial %s: %s", 
					 classe.getDescricao(),
					 respostaXML.getXml()
					 ));		
				Assert.assertNotNull(respostaXML);
				Assert.assertFalse(respostaXML.getXml() == null);
			}
		}
	}

	/**
	 * Teste da consulta de tipos de documentos disponíveis na instalação do PJe.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test12_consultarTodosTiposDocumentoProcessual() throws Exception {
		System.out.println("\n test12_consultarTiposDocumentoProcessual: ------------------------");
		ConsultaPJe consultaPJe = obterConsultaPJe();
	
		// Consultando todos os tipos de documentos.
		List<TipoDocumentoProcessual> respostaDocs = consultaPJe.consultarTodosTiposDocumentoProcessual();		
		System.out.println(String.format("\n Retornaram %d tipo(s) de documento.", respostaDocs.size()));		
		Assert.assertNotNull(respostaDocs);
		Assert.assertFalse(respostaDocs.isEmpty());	
	}
	
	/**
	 * Teste da consulta de papéis.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test13_consultarPapeis() throws Exception {
		System.out.println("\n test13_consultarPapeis: ------------------------------------------");
		final String CPF = "09283185000163";
		
		ConsultaPJe consultaPJe = obterConsultaPJe();
		
		List<Papel> papeis = consultaPJe.consultarPapeis(CPF);		
		System.out.println(String.format("\n Retornaram %d papéis.", papeis.size()));		
		Assert.assertNotNull(papeis);
		Assert.assertFalse(papeis.isEmpty());
		for (Papel papel : papeis) {
			System.out.println(papel.getIdentificador() + ": "+ papel.getNome());
		}
		Exception erro = null;
		try {
			papeis = consultaPJe.consultarPapeis(null);
		} catch (Exception e) {
			erro = e;
		}
		Assert.assertNotNull(erro);
		Assert.assertEquals("Usuário 'null' não é um usuário válido.", erro.getMessage());
		
		erro = null;
		try {
			papeis = consultaPJe.consultarPapeis("aaa");
		} catch (Exception e) {
			erro = e;
		}
		Assert.assertNotNull(erro);
		Assert.assertEquals("Usuário 'aaa' não é um usuário válido.", erro.getMessage());
	}
	
	@Test
	public void test12_consultarProcessosPorProcessoReferencia() throws Exception {
		ConsultaPJe consultaPJe = obterConsultaPJe();
		
		Assert.assertTrue(!consultaPJe.consultarProcessosPorProcessoReferencia("5020193-33.2017.4.03.0000").isEmpty());
		Assert.assertTrue(!consultaPJe.consultarProcessosPorProcessoReferencia("50201933320174030000").isEmpty());
	}
	
	/**
	 * @return Serviço ConsultaPJe.
	 * 
	 * @throws RuntimeException
	 */
	@SuppressWarnings("rawtypes")
	private static ConsultaPJe obterConsultaPJe() throws RuntimeException {
	
		if (consultaPJe == null) {
			try {
				URL url = new URL(URL_WSDL);
				
				WebService webService = ConsultaPJeImpl.class.getAnnotation(WebService.class);
				QName qname = new QName(
						webService.targetNamespace(),
						"ConsultaPJeService");
	
				Service service = Service.create(url, qname);
	
				consultaPJe = service.getPort(ConsultaPJe.class);
				BindingProvider bp = (BindingProvider) consultaPJe;
				SOAPBinding binding = (SOAPBinding) bp.getBinding();
				if (HABILITAR_EXIBIR_SOAP) {
					List<Handler> handlers = binding.getHandlerChain();
					handlers.add(new LogMessageSOAPHandler());
					binding.setHandlerChain(handlers);
				}
				
				bp.getRequestContext().put(
						BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
						url.toString().replace("?wsdl", ""));
			} catch (Exception e) {
				String mensagem = e.getMessage();
				mensagem = String.format(
						"Erro ao criar instância do serviço do ConsultaPJe, erro: %s",
						mensagem);
				throw new RuntimeException(mensagem);
			}
		}
		return consultaPJe;
	}
}
