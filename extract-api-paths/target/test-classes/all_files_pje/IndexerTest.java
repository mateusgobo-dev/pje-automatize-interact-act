package br.jus.pje.indexacao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public class IndexerTest {
	
	private static Indexer indexer;
	
	private static Calendar dataReferencia;
	
	private Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
	private Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();

	
	@BeforeClass
	public static void beforeClass() throws Exception{
		indexer = new Indexer();
		dataReferencia = GregorianCalendar.getInstance();
		dataReferencia.set(1969, 6, 20, 3, 20, 0);
	}
	
	@Before
	public void before(){
		mapa.clear();
		owners.clear();
	}
	
	@Test(expected=NoSuchFieldException.class)
	public void testaErroMapeamento() throws NoSuchFieldException{
		indexer.createIndexMetadata(ErroMapeamentoSimples.class, mapa, owners);
	}
	
	@Test
	public void testaIndexacao() throws NoSuchFieldException{
		indexer.createIndexMetadata(ObjetoBase.class, mapa, owners);
		IndexingMapping mapeamentoObjetoBase = mapa.get(ObjetoBase.class);
		assertTrue(mapa.size() == 2);
		assertTrue(mapeamentoObjetoBase.getPrimitivos().size() == 9);
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("codigo"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("descricao"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("segredo"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("criacao"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("derivado"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("listaSimples"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("listaObjetos"));
		assertTrue(mapeamentoObjetoBase.getPrimitivos().containsKey("originario.id"));
		assertTrue(mapeamentoObjetoBase.getListas().size() == 2);
		assertTrue(mapeamentoObjetoBase.getListas().containsKey("listaSimples"));
		assertTrue(mapeamentoObjetoBase.getListas().containsKey("listaObjetos"));
		assertFalse(mapeamentoObjetoBase.getListas().containsKey("codigo"));
		assertTrue(mapeamentoObjetoBase.getObjetos().size() == 2);
		assertTrue(mapeamentoObjetoBase.getObjetos().containsKey("derivado"));
		assertTrue(owners.size() == 1);
		assertTrue(mapeamentoObjetoBase.getCondicoes().size() == 1);
		assertTrue(mapeamentoObjetoBase.getExtractors().isEmpty());
	}
	
	@Test
	public void testaConversao() throws NoSuchFieldException, JSONException{
		String source = "{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"sigiloso\":false,\"originario\":12,\"valores\":[\"valor 1\",\"valor 2\"],\"nome\":\"descricao12\",\"id_\":\"12\",\"codigomodificado\":\"codigo12\",\"filho\":{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao13\",\"id_\":\"13\",\"codigomodificado\":\"codigo13\"},\"objetos\":[{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao 15\",\"id_\":\"15\",\"codigomodificado\":\"codigo15\"},{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao 16\",\"id_\":\"16\",\"codigomodificado\":\"codigo16\"}]}";
		List<String> listaString = Arrays.asList(new String[]{"valor 1", "valor 2"});
		ObjetoBase base = new ObjetoBase(12L, "codigo12", "descricao12", false, dataReferencia.getTime(), 
				new ObjetoDerivado(13L, "codigo13", "descricao13", dataReferencia.getTime(), true, null), 
				listaString, new ArrayList<ObjetoDerivado>());
		List<ObjetoDerivado> derivados = new ArrayList<IndexerTest.ObjetoDerivado>();
		derivados.add(new ObjetoDerivado(15L, "codigo15", "descricao 15", dataReferencia.getTime(), true, base));
		derivados.add(new ObjetoDerivado(16L, "codigo16", "descricao 16", dataReferencia.getTime(), true, base));
		base.setListaObjetos(derivados);
		base.setOriginario(base);
		base.setInativo(new ObjetoDerivado(16L, "codigo16", "descricao 16", dataReferencia.getTime(), false, base));
		indexer.createIndexMetadata(ObjetoBase.class, mapa, owners);
		JSONObject expected = new JSONObject(source);
		assertEquals(expected.toString(), indexer.toIndexableJSON(base, mapa).toString());
	}
	
	@Test
	public void testaConversaoCondicional() throws NoSuchFieldException, JSONException{
		String source = "{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"seativo\":{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao 16\",\"id_\":\"16\",\"codigomodificado\":\"codigo16\"},\"sigiloso\":false,\"originario\":12,\"valores\":[\"valor 1\",\"valor 2\"],\"nome\":\"descricao12\",\"id_\":\"12\",\"codigomodificado\":\"codigo12\",\"filho\":{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao13\",\"id_\":\"13\",\"codigomodificado\":\"codigo13\"},\"objetos\":[{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao 15\",\"id_\":\"15\",\"codigomodificado\":\"codigo15\"},{\"datacriacao\":\"Sun Jul 20 03:20:00 BRT 1969\",\"ativo\":true,\"nome\":\"descricao 16\",\"id_\":\"16\",\"codigomodificado\":\"codigo16\"}]}";
		List<String> listaString = Arrays.asList(new String[]{"valor 1", "valor 2"});
		ObjetoBase base = new ObjetoBase(12L, "codigo12", "descricao12", false, dataReferencia.getTime(), 
				new ObjetoDerivado(13L, "codigo13", "descricao13", dataReferencia.getTime(), true, null), 
				listaString, new ArrayList<ObjetoDerivado>());
		List<ObjetoDerivado> derivados = new ArrayList<IndexerTest.ObjetoDerivado>();
		ObjetoDerivado od15 = new ObjetoDerivado(15L, "codigo15", "descricao 15", dataReferencia.getTime(), true, base);
		ObjetoDerivado od16 = new ObjetoDerivado(16L, "codigo16", "descricao 16", dataReferencia.getTime(), true, base);
		derivados.add(od15);
		derivados.add(od16);
		base.setListaObjetos(derivados);
		base.setOriginario(base);
		base.setInativo(new ObjetoDerivado(16L, "codigo16", "descricao 16", dataReferencia.getTime(), true, base));
		indexer.createIndexMetadata(ObjetoBase.class, mapa, owners);
		JSONObject expected = new JSONObject(source);
		assertEquals(expected.toString(), indexer.toIndexableJSON(base, mapa).toString());
	}
	
	@Test
	public void testOwners() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		List<String> listaString = Arrays.asList(new String[]{"valor 1", "valor 2"});
		ObjetoBase base = new ObjetoBase(12L, "codigo12", "descricao12", false, dataReferencia.getTime(), 
				new ObjetoDerivado(13L, "codigo13", "descricao13", dataReferencia.getTime(), true, null), 
				listaString, new ArrayList<ObjetoDerivado>());
		List<ObjetoDerivado> derivados = new ArrayList<IndexerTest.ObjetoDerivado>();
		derivados.add(new ObjetoDerivado(15L, "codigo15", "descricao 15", dataReferencia.getTime(), true, base));
		derivados.add(new ObjetoDerivado(16L, "codigo16", "descricao 16", dataReferencia.getTime(), true, base));
		base.setListaObjetos(derivados);
		base.setOriginario(base);
		base.setInativo(new ObjetoDerivado(16L, "codigo16", "descricao 16", dataReferencia.getTime(), false, base));
		indexer.createIndexMetadata(ObjetoBase.class, mapa, owners);
		assertTrue(!mapa.get(ObjetoDerivado.class).getPaths().isEmpty());
	}
	
	@Test
	public void testExtractor() throws NoSuchFieldException, JSONException{
		JSONObject expected = new JSONObject("{\"descricao\":{\"conteudo1\":\"Meu extrator mundo\"},\"id_\":\"Meu extrator\"}");
		indexer.createIndexMetadata(ObjetoExtrator.class, mapa, owners);
		assertEquals(1, mapa.get(ObjetoExtrator.class).getExtractors().size());
		ObjetoExtrator extraido = new ObjetoExtrator();
		extraido.setId("Meu extrator");
		assertEquals(expected.toString(), indexer.toIndexableJSON(extraido, mapa).toString());
	}
	
	@Test
	public void testTransation() throws NoSuchFieldException{
		indexer.createIndexMetadata(ObjetoBase.class, mapa, owners);
		assertEquals("filho.dederivado", indexer.translate(ObjetoBase.class, "derivado.derivado", mapa));
	}
	
	@Test
	public void testIndexMapping() throws NoSuchFieldException{
		indexer.createIndexMetadata(ProcessoDocumento.class, mapa, owners);
		JSONObject mapping = indexer.createJSONMappings(ProcessoDocumento.class, mapa);
		System.out.println(mapping);
		assertNotNull(mapping);
	}
	
	@Test
	public void testIndexMappingTrf() throws NoSuchFieldException{
		indexer.createIndexMetadata(ProcessoTrf.class, mapa, owners);
		JSONObject mapping = indexer.createJSONMappings(ProcessoTrf.class, mapa);
		System.out.println(mapping);
		assertNotNull(mapping);
	}
	
	@IndexedEntity(id="id", value="objetobase", mappings={
			@Mapping(beanPath="codigo", mappedPath="codigomodificado"),
			@Mapping(beanPath="descricao", mappedPath="nome"),
			@Mapping(beanPath="segredo", mappedPath="sigiloso"),
			@Mapping(beanPath="criacao", mappedPath="datacriacao"),
			@Mapping(beanPath="derivado", mappedPath="filho"),
			@Mapping(beanPath="listaSimples", mappedPath="valores"),
			@Mapping(beanPath="listaObjetos", mappedPath="objetos"),
			@Mapping(beanPath="originario.id", mappedPath="originario"),
			@Mapping(beanPath="inativo", mappedPath="seativo", when="ativo")
	})
	public class ObjetoBase{
		private Long id;
		private String codigo;
		private String descricao;
		private Boolean segredo;
		private Date criacao;
		private ObjetoDerivado derivado;
		private ObjetoBase originario;
		private ObjetoDerivado inativo;
		private List<String> listaSimples;
		private List<ObjetoDerivado> listaObjetos;
		public ObjetoBase(Long id, String codigo, String descricao, Boolean segredo, Date criacao, ObjetoDerivado derivado, List<String> listaSimples, List<ObjetoDerivado> listaObjetos) {
			super();
			this.id = id;
			this.codigo = codigo;
			this.descricao = descricao;
			this.segredo = segredo;
			this.criacao = criacao;
			this.derivado = derivado;
			this.listaSimples = listaSimples;
			this.listaObjetos = listaObjetos;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getCodigo() {
			return codigo;
		}
		public void setCodigo(String codigo) {
			this.codigo = codigo;
		}
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		public Boolean getSegredo() {
			return segredo;
		}
		public void setSegredo(Boolean segredo) {
			this.segredo = segredo;
		}
		public Date getCriacao() {
			return criacao;
		}
		public void setCriacao(Date criacao) {
			this.criacao = criacao;
		}
		public ObjetoDerivado getDerivado() {
			return derivado;
		}
		public void setDerivado(ObjetoDerivado derivado) {
			this.derivado = derivado;
		}
		public List<String> getListaSimples() {
			return listaSimples;
		}
		public void setListaSimples(List<String> listaSimples) {
			this.listaSimples = listaSimples;
		}
		public List<ObjetoDerivado> getListaObjetos() {
			return listaObjetos;
		}
		public void setListaObjetos(List<ObjetoDerivado> listaObjetos) {
			this.listaObjetos = listaObjetos;
		}
		public ObjetoBase getOriginario() {
			return originario;
		}
		public void setOriginario(ObjetoBase originario) {
			this.originario = originario;
		}
		public ObjetoDerivado getInativo() {
			return inativo;
		}
		public void setInativo(ObjetoDerivado inativo) {
			this.inativo = inativo;
		}
	}
	
	@IndexedEntity(id="id", value="derivado",
			owners={"dono", "pai"},
			mappings={
			@Mapping(beanPath="codigo", mappedPath="codigomodificado"),
			@Mapping(beanPath="descricao", mappedPath="nome"),
			@Mapping(beanPath="criacao", mappedPath="datacriacao"),
			@Mapping(beanPath="ativo", mappedPath="ativo"),
			@Mapping(beanPath="derivado", mappedPath="dederivado")
	})
	public class ObjetoDerivado{
		private Long id;
		private String codigo;
		private String descricao;
		private Date criacao;
		private Boolean ativo;
		private ObjetoBase dono;
		private ObjetoDerivado derivado;
		private ObjetoDerivado pai;
		public ObjetoDerivado(Long id, String codigo, String descricao, Date criacao, Boolean ativo, ObjetoBase dono) {
			this.id = id;
			this.codigo = codigo;
			this.descricao = descricao;
			this.criacao = criacao;
			this.ativo = ativo;
			this.dono = dono;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getCodigo() {
			return codigo;
		}
		public void setCodigo(String codigo) {
			this.codigo = codigo;
		}
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		public Date getCriacao() {
			return criacao;
		}
		public void setCriacao(Date criacao) {
			this.criacao = criacao;
		}
		public Boolean getAtivo() {
			return ativo;
		}
		public void setAtivo(Boolean ativo) {
			this.ativo = ativo;
		}
		public ObjetoBase getDono() {
			return dono;
		}
		public void setDono(ObjetoBase dono) {
			this.dono = dono;
		}
		public ObjetoDerivado getDerivado() {
			return derivado;
		}
		public void setDerivado(ObjetoDerivado derivado) {
			this.derivado = derivado;
		}
		public ObjetoDerivado getPai(){
			return pai;
		}
		public void setPai(ObjetoDerivado pai) {
			this.pai = pai;
		}
	}
	
	@IndexedEntity(id="id", value="erromapeamento", mappings={
			@Mapping(beanPath="descricao1", mappedPath="descricao")
	})
	public class ErroMapeamentoSimples{
		public String id;
		public String descricao;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
	}
	
	public static class Extrator implements IndexingExtractor{
		@Override
		public JSONObject extract(Object id) {
			JSONObject ret = new JSONObject();
			try {
				ret.put("conteudo1", id + " mundo");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
	}
	
	@IndexedEntity(id="id", value="extrator", 
			mappings={
				@Mapping(beanPath="id", mappedPath="descricao", extractor="br.jus.pje.indexacao.IndexerTest$Extrator")
	})
	public class ObjetoExtrator{
		public String id;
		public String descricao;
		public List<ObjetoBase> donos;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		public List<ObjetoBase> getDonos() {
			return donos;
		}
		public void setDonos(List<ObjetoBase> donos) {
			this.donos = donos;
		}
	}

}
