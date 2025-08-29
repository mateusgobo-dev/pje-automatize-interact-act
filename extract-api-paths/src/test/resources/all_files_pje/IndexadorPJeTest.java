package br.jus.pje.indexacao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

public class IndexadorPJeTest {
	
	private static Indexer indexador;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		indexador = new Indexer();
	}
	
	@Test
	public void testaIndexacaoProcessoTrf() throws NoSuchFieldException{
		Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
		Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();
		indexador.createIndexMetadata(ProcessoTrf.class, mapa, owners);
		IndexingMapping idxprocesso = mapa.get(ProcessoTrf.class);
		assertEquals(12, mapa.size());
		assertEquals(16, idxprocesso.getPrimitivos().size());
		assertEquals(7, idxprocesso.getObjetos().size());
		assertEquals(2, idxprocesso.getListas().size());
		assertEquals(0, idxprocesso.getExtractors().size());
	}
	
	@Test
	public void testaConversao() throws NoSuchFieldException{
		String expected = "{\"distribuicao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"sigiloso\":false,\"partes\":[{\"tipo\":\"REQUERENTE\",\"pessoa\":{\"documentos\":[{\"codigo\":\"CPF\",\"tipo\":\"INSCRIÇÃO NO CADASTRO DE PESSOAS FÍSICAS\",\"nome\":\"Fulano dos Anzois Pereira\",\"id_\":\"0\",\"numero\":\"9837346433\"}],\"nome\":\"Fulano dos Anzóis Pereira Ramos\"},\"sigilosa\":false,\"polo\":\"Ativo\",\"id_\":\"0\"},{\"tipo\":\"REQUERENTE\",\"pessoa\":{\"documentos\":[{\"codigo\":\"CPF\",\"tipo\":\"INSCRIÇÃO NO CADASTRO DE PESSOAS FÍSICAS\",\"nome\":\"Fulano dos Anzois Pereira\",\"id_\":\"0\",\"numero\":\"9837346433\"}],\"nome\":\"Réu sem Ramos\"},\"sigilosa\":false,\"polo\":\"Passivo\",\"id_\":\"0\"}],\"id_\":\"0\",\"numero\":\"0001437-74.2001.2.00.0000\"}";
		Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
		Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();
		indexador.createIndexMetadata(ProcessoTrf.class, mapa, owners);
		JSONObject json = indexador.toIndexableJSON(geraProcesso(), mapa);
		assertNotNull(json);
		assertEquals(expected, json.toString());
	}
	
	@Test
	public void testaDocumento() throws NoSuchFieldException{
		Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
		Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();
		indexador.createIndexMetadata(ProcessoDocumento.class, mapa, owners);
		assertEquals(1, mapa.get(ProcessoDocumento.class).getExtractors().size());
		JSONObject json = indexador.toIndexableJSON(getDocumento(), mapa);
		assertNotNull(json);
	}
	
	@Test
	public void testaTraducao() throws NoSuchFieldException{
		Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
		Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();
		indexador.createIndexMetadata(ProcessoDocumento.class, mapa, owners);
		assertEquals("tipo.tipo", indexador.translate(ProcessoDocumento.class, "tipoProcessoDocumento.tipoProcessoDocumento", mapa));
	}
	
	@Test
	public void testaMapeamentoES() throws NoSuchFieldException{
		Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
		Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();
		indexador.createIndexMetadata(ProcessoTrf.class, mapa, owners);
		JSONObject mapeamento = indexador.createJSONMappings(ProcessoTrf.class, mapa);
		System.out.println(mapeamento);
		assertNotNull(mapeamento);
	}
	
	private ProcessoTrf geraProcesso(){
		Calendar cal = new GregorianCalendar(1969, 6, 20, 3, 20);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		TipoParte reqte = new TipoParte();
		reqte.setTipoParte("Requerente");
		TipoParte reqdo = new TipoParte();
		reqdo.setTipoParte("Requerido");
		TipoDocumentoIdentificacao tcpf = new TipoDocumentoIdentificacao("CPF");
		tcpf.setTipoDocumento("Inscrição no cadastro de pessoas físicas");
		TipoDocumentoIdentificacao tcnpj = new TipoDocumentoIdentificacao("CPJ");
		tcnpj.setTipoDocumento("Inscrição no cadastro de pessoas jurídicas");
		PessoaDocumentoIdentificacao pdi = new PessoaDocumentoIdentificacao();
		pdi.setNome("Fulano dos Anzois Pereira");
		pdi.setNumeroDocumento("9837346433");
		pdi.setTipoDocumento(tcpf);
		PessoaFisica autor = new PessoaFisica();
		autor.setNome("Fulano dos Anzóis Pereira Ramos");
		autor.getPessoaDocumentoIdentificacaoList().add(pdi);
		PessoaFisica reu = new PessoaFisica();
		reu.setNome("Réu sem Ramos");
		reu.getPessoaDocumentoIdentificacaoList().add(pdi);
		ProcessoParte poloativo = new ProcessoParte();
		poloativo.setPartePrincipal(true);
		poloativo.setParteSigilosa(false);
		poloativo.setPessoa(autor);
		poloativo.setTipoParte(reqte);
		poloativo.setInParticipacao(ProcessoParteParticipacaoEnum.A);
		ProcessoParte polopassivo = new ProcessoParte();
		polopassivo.setPartePrincipal(true);
		polopassivo.setParteSigilosa(false);
		polopassivo.setPessoa(reu);
		polopassivo.setTipoParte(reqte);
		polopassivo.setInParticipacao(ProcessoParteParticipacaoEnum.P);
		Processo p = new Processo();
		p.setNumeroProcesso("0001437-74.2001.2.00.0000");
		ProcessoTrf proc = new ProcessoTrf();
		proc.getProcessoParteList().add(poloativo);
		proc.getProcessoParteList().add(polopassivo);
		proc.setProcesso(p);
		proc.setSegredoJustica(false);
		proc.setDataDistribuicao(cal.getTime());
		return proc;
	}
	
	private ProcessoDocumento getDocumento(){
		Calendar cal = new GregorianCalendar(1969, 6, 20, 3, 20);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		TipoProcessoDocumento tpd = new TipoProcessoDocumento();
		tpd.setCodigoDocumento("9876");
		tpd.setTipoProcessoDocumento("Tipo de documento de teste");
		Processo proc = new Processo();
		proc.setIdProcesso(987);
		proc.setNumeroProcesso("0000001-23.2014.2.00.0000");
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setExtensao("text/html");
		pdb.setSize(1234);
		pdb.setNomeArquivo("noname");
		pdb.setModeloDocumento("<p>Meu texto aqui.</p>");
		ProcessoDocumento doc = new ProcessoDocumento();
		doc.setTipoProcessoDocumento(tpd);
		doc.setProcesso(proc);
		doc.setProcessoDocumento("Documento de teste");
		doc.setDataJuntada(cal.getTime());
		doc.setDocumentoSigiloso(true);
		doc.setProcessoDocumentoBin(pdb);
		return doc;
	}
	
}
