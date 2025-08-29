package br.jus.cnj.pje.business.dao;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.indexacao.IndexingMapping;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * @author cristof
 *
 */
public class ElasticDAOTest {
	
	private static final Logger logger = Logger.getLogger(ElasticDAOTest.class.getCanonicalName());
	private Map<Class<?>, IndexingMapping> mapa = new HashMap<Class<?>, IndexingMapping>();
	private Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();

	private class Indexer extends br.jus.pje.indexacao.Indexer {
		public void createIndexMetadata(Class<?> clazz, Map<Class<?>, IndexingMapping> mapa, Map<Class<?>, List<String>> owners) throws NoSuchFieldException {
			super.createIndexMetadata(clazz, mapa, owners);
		}
	}
	
	@BeforeClass
	public static void beforeClass(){
		logger.setLevel(Level.ALL);
	}

	/**
	 * Test method for
	 * {@link br.jus.cnj.pje.business.dao.ElasticDAO#search(br.jus.pje.search.Search)}
	 * .
	 * 
	 * @throws NoSuchFieldException
	 * @throws PJeBusinessException
	 */
	@Test
	public final void testSearch() throws NoSuchFieldException, PJeBusinessException, JSONException{
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		assertNotNull(dao);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("descricao", "CD001"));
		s.addCriteria(Criteria.equals("possuido.codigo", "CD002"));
		s.addCriteria(Criteria.equals("derivados.codigo", "CD003"));
		s.addCriteria(Criteria.equals("ativo", true).asFilter());
		s.addCriteria(Criteria.or(Criteria.greaterOrEquals("possuido.valorOriginario", 2345), Criteria.lessOrEquals("possuido.valorFinal", 6543)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.has("query"), is(true));
		assertThat(query.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("bool").has("must"), is(true));
	}
	
	@Test
	public void testSimpleFieldSearch() throws NoSuchFieldException, PJeBusinessException, JSONException {
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"funcional\":true}}]}}}},\"from\":1,\"size\":1}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		assertNotNull(dao);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("ativo", true));
		s.setFirst(1);
		s.setMax(1);
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testSimpleFieldFilter() throws NoSuchFieldException, PJeBusinessException, JSONException {
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"term\":{\"funcional\":true}}]}}}},\"from\":1,\"size\":1}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		assertNotNull(dao);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("ativo", true).asFilter());
		s.setFirst(1);
		s.setMax(1);
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testSimpleAnd() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"funcional\":true}},{\"match\":{\"descricao\":{\"query\":\"descrição pesquisada\",\"operator\":\"and\"}}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		assertNotNull(dao);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("ativo", true));
		s.addCriteria(Criteria.contains("descricao", "descrição pesquisada"));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testComplexAnd() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"funcional\":true}},{\"match\":{\"descricao\":{\"query\":\"descrição pesquisada\",\"operator\":\"and\"}}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		assertNotNull(dao);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(
				Criteria.and(
						Criteria.equals("ativo", true), 
						Criteria.contains("descricao", "descrição pesquisada")));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testOr() throws NoSuchFieldException, JSONException{
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(
				Criteria.or(
						Criteria.equals("ativo", true),
						Criteria.equals("descricao", "Descrição principal")));
		JSONObject query = dao.buildQuery(s, mapa);
		JSONArray must = query.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("bool").getJSONArray("must");
		assertContains(must, "bool");
	}
	
	@Test
	public void testAndOr() throws NoSuchFieldException, JSONException{
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("derivado.codigo", "CD001"));
		s.addCriteria(
				Criteria.or(
						Criteria.equals("ativo", true),
						Criteria.equals("descricao", "DESCRICAO")));
		JSONObject query = dao.buildQuery(s, mapa);
		JSONArray must = query.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("bool").getJSONArray("must");
		assertContains(must, "bool");
	}
	
	@Test
	public void testBeanPath() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorFinal\":44.2}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("possuido.valorFinal", 44.2));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBeanPathAnd() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorFinal\":44.2}},\"path\":\"filho\"}},{\"match_phrase\":{\"funcional\":true}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("possuido.valorFinal", 44.2));
		s.addCriteria(Criteria.equals("ativo", true));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBeanPathOr() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorFinal\":44.2}},\"path\":\"filho\"}},{\"match_phrase\":{\"funcional\":true}}]}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(
				Criteria.or(
						Criteria.equals("possuido.valorFinal", 44.2),
						Criteria.equals("ativo", true)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBeanPathAndOr() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorFinal\":44.2}},\"path\":\"filho\"}},{\"match_phrase\":{\"funcional\":true}}]}},{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorOriginario\":10}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("possuido.valorOriginario", 10.0));
		s.addCriteria(
				Criteria.or(
						Criteria.equals("possuido.valorFinal", 44.2),
						Criteria.equals("ativo", true)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testNestedAndOr() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorOriginario\":10}},\"path\":\"filho\"}},{\"nested\":{\"query\":{\"match_phrase\":{\"filho.valorFinal\":44.2}},\"path\":\"filho\"}}]}},{\"match_phrase\":{\"funcional\":true}}]}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(
				Criteria.or(
						Criteria.and(
								Criteria.equals("possuido.valorOriginario", 10.0),
								Criteria.equals("possuido.valorFinal", 44.2)),
						Criteria.equals("ativo", true)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testGreater() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"filho.valorOriginario\":{\"gt\":10}}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.greater("possuido.valorOriginario", 10.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testGreaterOrEquals() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"filho.valorOriginario\":{\"gte\":10}}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.greaterOrEquals("possuido.valorOriginario", 10.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testLess() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"filho.valorOriginario\":{\"lt\":10}}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.less("possuido.valorOriginario", 10.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testLessOrEquals() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"filho.valorOriginario\":{\"lte\":10}}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.lessOrEquals("possuido.valorOriginario", 10.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBetween() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"filho.valorOriginario\":{\"gte\":10,\"lte\":20}}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.between("possuido.valorOriginario", 10.0, 20.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testIn() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"bool\":{\"should\":[{\"match\":{\"filho.valorOriginario\":10}},{\"match\":{\"filho.valorOriginario\":20}}]}},\"path\":\"filho\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.in("possuido.valorOriginario", new Double[]{10.0, 20.0}));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testId() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"id_\":10}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.equals("codigo", 10.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test 
	public void testNot() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"not\":{\"term\":{\"id_\":10}}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.not(Criteria.equals("codigo", 10.0)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test 
	public void testNotBetween() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"not\":{\"range\":{\"id_\":{\"gte\":10,\"lte\":20}}}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.not(Criteria.between("codigo", 10.0, 20.0)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test 
	public void testNotEquals() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"not\":{\"term\":{\"id_\":10}}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ObjetoSimples.class);
		Search s = new Search(ObjetoSimples.class);
		s.addCriteria(Criteria.notEquals("codigo", 10.0));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testProcessoTrfSimpleField() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"sigiloso\":false}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		assertNotNull(dao);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("segredoJustica", false));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testProcessoTrfSimpleFilteredField() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"term\":{\"sigiloso\":false}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		assertNotNull(dao);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("segredoJustica", false).asFilter());
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testProcessoTrfAnd() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"sigiloso\":false}},{\"match_phrase\":{\"codigojuizo\":\"CONSTF2\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		assertNotNull(dao);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.and(
						Criteria.equals("segredoJustica", false), 
						Criteria.equals("orgaoJulgadorCargo.sigla", "CONSTF2")));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testProcessoTrfLongAnd() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"sigiloso\":false}},{\"nested\":{\"query\":{\"match\":{\"partes.pessoa.nome\":{\"query\":\"francisco occhiuto\",\"operator\":\"and\"}}},\"path\":\"partes.pessoa\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		assertNotNull(dao);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.and(
						Criteria.equals("segredoJustica", false), 
						Criteria.contains("processoParteList.pessoa.nome", "francisco occhiuto")));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testProcessoTrfFilteredAnd() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"term\":{\"sigiloso\":false}},{\"term\":{\"codigojuizo\":\"CONSTF2\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		assertNotNull(dao);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.and(
						Criteria.equals("segredoJustica", false), 
						Criteria.equals("orgaoJulgadorCargo.sigla", "CONSTF2")).asFilter());
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testProcessoTrfSimpleOr() throws NoSuchFieldException, JSONException{
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.or(
						Criteria.equals("segredoJustica", true),
						Criteria.equals("segredoJustica", false)));
		JSONObject query = dao.buildQuery(s, mapa);
		JSONArray must = query.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("bool").getJSONArray("must");
		assertContains(must, "bool");
	}
	
	@Test
	public void testProcessoTrfAndOr() throws NoSuchFieldException, JSONException{
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("orgaoJulgadorCargo.sigla", "CONSTF2"));
		s.addCriteria(
				Criteria.or(
						Criteria.equals("segredoJustica", true),
						Criteria.equals("segredoJustica", false)));
		JSONObject query = dao.buildQuery(s, mapa);
		JSONArray must = query.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("bool").getJSONArray("must");
		assertContains(must, "bool");
	}

	@Test
	public void testBeanPathProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"classe.sigla\":\"RevDis\"}},\"path\":\"classe\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("classeJudicial.classeJudicialSigla", "RevDis"));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBeanPathAndProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"classe.sigla\":\"RevDis\"}},\"path\":\"classe\"}},{\"match_phrase\":{\"sigiloso\":false}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("classeJudicial.classeJudicialSigla", "RevDis"));
		s.addCriteria(Criteria.equals("segredoJustica", false));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testBeanPathOrProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"nested\":{\"query\":{\"match_phrase\":{\"classe.sigla\":\"RevDis\"}},\"path\":\"classe\"}},{\"match_phrase\":{\"sigiloso\":false}}]}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.or(
						Criteria.equals("classeJudicial.classeJudicialSigla", "RevDis"),
						Criteria.equals("segredoJustica", false)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBeanPathAndOrProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"nested\":{\"query\":{\"match_phrase\":{\"classe.sigla\":\"RevDis\"}},\"path\":\"classe\"}},{\"match_phrase\":{\"sigiloso\":false}}]}},{\"match_phrase\":{\"valorcausa\":0}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("valorCausa", 0));
		s.addCriteria(
				Criteria.or(
						Criteria.equals("classeJudicial.classeJudicialSigla", "RevDis"),
						Criteria.equals("segredoJustica", false)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testNestedOrAndProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"classe.sigla\":\"RevDis\"}},\"path\":\"classe\"}},{\"match_phrase\":{\"sigiloso\":false}}]}},{\"match_phrase\":{\"valorcausa\":0}}]}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.or(
						Criteria.and(
								Criteria.equals("classeJudicial.classeJudicialSigla", "RevDis"),
								Criteria.equals("segredoJustica", false)),
							Criteria.equals("valorCausa", 0)));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testNestedOrAndFilteredProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"match_phrase\":{\"classe.sigla\":\"RevDis\"}},\"path\":\"classe\"}},{\"term\":{\"sigiloso\":false}}]}},{\"term\":{\"valorcausa\":0}}]}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(
				Criteria.or(
						Criteria.and(
								Criteria.equals("classeJudicial.classeJudicialSigla", "RevDis"),
								Criteria.equals("segredoJustica", false)),
							Criteria.equals("valorCausa", 0)).asFilter());
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testGreaterProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"situacoes.dataInicial\":{\"gt\":\"2014-02-01 00:00:00.0\"}}},\"path\":\"situacoes\"}}]}}}}}";
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2014, 1, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 000);
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.greater("situacoes.dataInicial", cal.getTime()));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testGreaterOrEqualsProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"situacoes.dataInicial\":{\"gte\":\"2014-02-01 00:00:00.0\"}}},\"path\":\"situacoes\"}}]}}}}}";
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2014, 1, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 000);
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.greaterOrEquals("situacoes.dataInicial", cal.getTime()));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testLessProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"situacoes.dataInicial\":{\"lt\":\"2014-02-01 00:00:00.0\"}}},\"path\":\"situacoes\"}}]}}}}}";
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2014, 1, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 000);
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.less("situacoes.dataInicial", cal.getTime()));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}

	@Test
	public void testLessOrEqualsProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"situacoes.dataInicial\":{\"lte\":\"2014-02-01 00:00:00.0\"}}},\"path\":\"situacoes\"}}]}}}}}";
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2014, 1, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 000);
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.lessOrEquals("situacoes.dataInicial", cal.getTime()));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testBetweenProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"situacoes.dataInicial\":{\"gte\":\"2014-02-03 00:00:00.0\",\"lte\":\"2014-02-03 23:59:59.999\"}}},\"path\":\"situacoes\"}}]}}}}}";
		Calendar min = GregorianCalendar.getInstance();
		min.set(2014, 1, 3, 0, 0, 0);
		min.set(Calendar.MILLISECOND, 000);
		Calendar max = GregorianCalendar.getInstance();
		max.set(2014, 1, 3, 23, 59, 59);
		max.set(Calendar.MILLISECOND, 999);
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.between("situacoes.dataInicial", min.getTime(), max.getTime()));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testInProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"bool\":{\"should\":[{\"match\":{\"situacoes.tipo\":\"jus:andamento\"}},{\"match\":{\"situacoes.tipo\":\"jus:arquivado\"}}]}},\"path\":\"situacoes\"}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.in("situacoes.tipoSituacaoProcessual.codigo", new String[]{"jus:andamento", "jus:arquivado"}));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	@Test
	public void testIdProcessoTrf() throws NoSuchFieldException, JSONException{
		String expected = "{\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"id_\":1329}}]}}}}}";
		ElasticTestDAO dao = prepareDAO(ProcessoTrf.class);
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.equals("idProcessoTrf", 1329));
		JSONObject query = dao.buildQuery(s, mapa);
		assertThat(query.toString(), is(expected));
	}
	
	private ElasticTestDAO prepareDAO(Class<?> clazz) throws NoSuchFieldException{
		Indexer indexer = new Indexer();
		indexer.createIndexMetadata(clazz, mapa, owners);
		ElasticTestDAO dao = new ElasticTestDAO();
		dao.setIndexer(indexer);
		return dao;
	}
	

	private void assertContains(JSONArray array, String field, String...value) throws JSONException {
		if (array == null) {
			fail("JSONArray is null");
		} else if (array.length() == 0) {
			fail("JSONArray has no elements");
		}
		if (field == null || field.isEmpty()) {
			fail("Expected field name is null or empty.");
		}
		for (int i = 0; i < array.length(); i++) {
			JSONObject o = array.getJSONObject(i);
			if (o.has(field)) {
				if(value != null && value.length > 0){
					for(String c: value){
						if(o.getJSONObject(field).toString().equalsIgnoreCase(c)){
							return;
						}
					}
				}else{
					return;
				}
			}
		}
		fail("Array does not contain the expected field.");
		// StringBuilder pat = new StringBuilder("\\{\\\"");
		// pat.append(field).append("\\\"\\:");
		// System.out.println(pat.toString());
		// Pattern pattern = Pattern.compile("{\"match\"");
		// if(!pattern.matcher(array.join("<-->")).matches()){
		// fail("Array does not contain the expected field.");
		// }
	}

	private class ElasticTestDAO extends ElasticDAO<Object> {

	}

	@IndexedEntity(value = "objetosimples", id = "codigo", 
		mappings = { 
			@Mapping(beanPath = "descricao", mappedPath = "descricao"), 
			@Mapping(beanPath = "dataCriacao", mappedPath = "criacao"),
			@Mapping(beanPath = "ativo", mappedPath = "funcional"), 
			@Mapping(beanPath = "derivado.codigo", mappedPath = "derivado"), 
			@Mapping(beanPath = "possuido", mappedPath = "filho"),
			@Mapping(beanPath = "derivados", mappedPath = "filhos")
	})
	private class ObjetoSimples {
		private String codigo;
		private String descricao;
		private Date dataCriacao;
		private Boolean ativo;
		private ObjetoSimples derivado;
		private ObjetoDerivado possuido;
		private Collection<ObjetoDerivado> derivados;

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

		public Date getDataCriacao() {
			return dataCriacao;
		}

		public void setDataCriacao(Date dataCriacao) {
			this.dataCriacao = dataCriacao;
		}

		public Boolean getAtivo() {
			return ativo;
		}

		public void setAtivo(Boolean ativo) {
			this.ativo = ativo;
		}

		public ObjetoSimples getDerivado() {
			return derivado;
		}

		public void setDerivado(ObjetoSimples derivado) {
			this.derivado = derivado;
		}

		public ObjetoDerivado getPossuido() {
			return possuido;
		}

		public void setPossuido(ObjetoDerivado possuido) {
			this.possuido = possuido;
		}

		public Collection<ObjetoDerivado> getDerivados() {
			return derivados;
		}

		public void setDerivados(Collection<ObjetoDerivado> derivados) {
			this.derivados = derivados;
		}
	}

	@IndexedEntity(value = "derivado", id = "codigo", owners = { "dono" }, mappings = { @Mapping(beanPath = "codigo", mappedPath = "codigo"), @Mapping(beanPath = "inicio", mappedPath = "inicio"),
			@Mapping(beanPath = "fim", mappedPath = "fim"), @Mapping(beanPath = "valorOriginario", mappedPath = "valorOriginario"), @Mapping(beanPath = "valorFinal", mappedPath = "valorFinal"),
			@Mapping(beanPath = "dono.codigo", mappedPath = "codigoDono") })
	private class ObjetoDerivado {

		private String codigo;
		private Date inicio;
		private Date fim;
		private Double valorOriginario;
		private Double valorFinal;
		private ObjetoSimples dono;

		public ObjetoDerivado() {
		}

		public String getCodigo() {
			return codigo;
		}

		public void setCodigo(String codigo) {
			this.codigo = codigo;
		}

		public Date getInicio() {
			return inicio;
		}

		public void setInicio(Date inicio) {
			this.inicio = inicio;
		}

		public Date getFim() {
			return fim;
		}

		public void setFim(Date fim) {
			this.fim = fim;
		}

		public Double getValorOriginario() {
			return valorOriginario;
		}

		public void setValorOriginario(Double valorOriginario) {
			this.valorOriginario = valorOriginario;
		}

		public Double getValorFinal() {
			return valorFinal;
		}

		public void setValorFinal(Double valorFinal) {
			this.valorFinal = valorFinal;
		}

		public ObjetoSimples getDono() {
			return dono;
		}

		public void setDono(ObjetoSimples dono) {
			this.dono = dono;
		}
	}

}
