/**
 * pje-web
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.business.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Conjunto de testes do {@link Search} combinado com o {@link BaseDAO#createQueryString(Search, Map)}.
 * 
 * Para sua execução, é imprescindível que exista uma base de dados preenchida definida na unidade de persistência
 * testpu.
 * 
 * @author Paulo Cristovão de Araújo Silva Filho
 *
 */
public class JPASearchTest {
	
	/**
	 * Instancia, por reflexão, a propriedade {@link BaseDAO#entityManager}.
	 * 
	 * @param baseDAO o DAO que será utilizado na consulta.
	 * @throws Exception 
	 */
	private void initDAO(BaseDAO<?> baseDAO) throws Exception{
		baseDAO.init();
	}
	
	private void inject(Object value, String property, Object dest) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field f = null; 
		Class<?> parent = dest.getClass();
		while(parent != null){
			try{
				f = parent.getDeclaredField(property);
			}catch(NoSuchFieldException e){
				parent = parent.getSuperclass();
			}
		}
		if(f == null){
			throw new NoSuchFieldException(property);
		}
		boolean access = true;
		if(!f.isAccessible()){
			access = false;
			f.setAccessible(true);
		}
		f.set(dest, value);
		if(!access){
			f.setAccessible(false);
		}
	}
	
	/**
	 * Testa a montagem do SQL esperado.
	 * 
	 * @throws Exception
	 */
	@Test @Ignore
	public void testaMountSQL() throws Exception{
		String path = "processoAudienciaList.processoDocumento.processoDocumentoBin.signatarios.pessoa.enderecoList.cep.numeroCep";
		String expected = "SELECT o.dataDistribuicao FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.processoAudienciaList AS p0 " +
				"INNER JOIN p0.processoDocumento.processoDocumentoBin.signatarios AS p1 " +
				"INNER JOIN p1.pessoa.enderecoList AS p2 " +
				"WHERE p2.cep.numeroCep = :prm0";
		Search search = new Search(ProcessoTrf.class);
		search.setRetrieveField("dataDistribuicao");
		Criteria crit = Criteria.equals(path, "70363110");
		search.addCriteria(crit);
		Map<String, Object> params = new HashMap<String, Object>();
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		pdb.init();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("70363110"));
	}
	
	@Test @Ignore
	public void testaCampoSimples() throws Exception{
		String path = "dataDistribuicao";
		String expected = "SELECT COUNT(o) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.dataDistribuicao < :prm0";
		Search search = new Search(ProcessoTrf.class);
		Date d = new Date();
		search.addCriteria(Criteria.less(path, d));
		search.setCount(true);
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((Date) params.get("prm0"), is(d));
	}

	@Test @Ignore
	public void testaNegaLess() throws Exception{
		String path = "dataDistribuicao";
		String expected = "SELECT COUNT(o) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.dataDistribuicao >= :prm0";
		Search search = new Search(ProcessoTrf.class);
		Date d = new Date();
		search.addCriteria(Criteria.not(Criteria.less(path, d)));
		search.setCount(true);
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((Date) params.get("prm0"), is(d));
	}

	@Test @Ignore
	public void testaCampoDerivado() throws Exception{
		String path = "classeJudicial.codClasseJudicial";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.classeJudicial.codClasseJudicial = :prm0";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path, "202"));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("202"));
	}

	@Test @Ignore
	public void testaCampoDerivadoComLista() throws Exception{
		String path = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path, "2º GRAU"));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
	}

	@Test @Ignore
	public void testaCombinacaoCriterios() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path1, "1º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("1º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
	}

	@Test @Ignore
	public void testaCombinacaoCriterios2() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa != :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.notEquals(path3, 0.0));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}

	@Test @Ignore
	public void testaCombinacaoCriterios3() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa = :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.equals(path3, 0.0));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}

	@Test @Ignore
	public void testaDistinct() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa = :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.equals(path3, 0.0));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}
	
	@Test @Ignore
	public void testaDistinct2() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa != :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.not(Criteria.equals(path3, 0.0)));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}
	
	@Test @Ignore
	public void testaDistinct3() throws Exception{
		String path1 = "processo.processoDocumentoList.processoDocumentoBin.signatarios.pessoa.enderecoList.cep.numeroCep";
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.processo.processoDocumentoList AS p0 " +
				"INNER JOIN p0.processoDocumentoBin.signatarios AS p1 " +
				"INNER JOIN p1.pessoa.enderecoList AS p2 " +
				"WHERE p2.cep.numeroCep = :prm0";
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(Criteria.equals(path1, "24220-120"));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("24220-120"));
	}
	
	@Test @Ignore
	public void testaProperty() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT o.idProcessoTrf FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa = :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.setRetrieveField("idProcessoTrf");
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.equals(path3, 0.0));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}

	@Test @Ignore
	public void testaCount() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT COUNT(o.classeJudicial) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa = :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.setCount(true);
		search.setRetrieveField("classeJudicial");
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.equals(path3, 0.0));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}
	
	@Test @Ignore
	public void testaCountDistinct() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT COUNT(DISTINCT o.classeJudicial) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND o.valorCausa = :prm2";
		Search search = new Search(ProcessoTrf.class);
		search.setCount(true);
		search.setDistinct(true);
		search.setRetrieveField("classeJudicial");
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		search.addCriteria(Criteria.equals(path3, 0.0));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
	}

	@Test @Ignore
	public void testaUsoOu() throws Exception{
		String path1 = "classeJudicial.classeAplicacaoList.aplicacaoClasse.aplicacaoClasse";
		String path2 = "classeJudicial.codClasseJudicial";
		String path3 = "valorCausa";
		String expected = "SELECT COUNT(DISTINCT o) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.classeJudicial.classeAplicacaoList AS p0 " +
				"WHERE p0.aplicacaoClasse.aplicacaoClasse = :prm0 " +
				"AND o.classeJudicial.codClasseJudicial = :prm1 " +
				"AND (o.valorCausa = :prm2 OR o.valorCausa != :prm3)";
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.equals(path1, "2º GRAU"));
		search.addCriteria(Criteria.equals(path2, "202"));
		Criteria or = Criteria.or(Criteria.equals(path3, 0.0), Criteria.notEquals(path3, 1.0));
		search.addCriteria(or);
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		search.setCount(true);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((String) params.get("prm0"), is("2º GRAU"));
		assertThat((String) params.get("prm1"), is("202"));
		assertThat((Double) params.get("prm2"), is(equalTo(0.0)));
		assertThat((Double) params.get("prm3"), is(equalTo(1.0)));
	}

	@Test @Ignore
	public void testaNull() throws Exception{
		String path = "dataDistribuicao";
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.dataDistribuicao IS NULL";
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.isNull(path));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(0)));
	}
	
	@Test @Ignore
	public void testaNotNull() throws Exception{
		String path = "dataDistribuicao";
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.dataDistribuicao IS NOT NULL";
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.not(Criteria.isNull(path)));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(0)));
	}
	
	@Test @Ignore
	public void testaEventoNull() throws Exception{
		String path = "eventoSuperior";
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.Evento AS o WHERE o.eventoSuperior IS NULL";
		Search search = new Search(Evento.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.isNull(path));
		EventoDAO evm = new EventoDAO();
		initDAO(evm);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(evm.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(0)));
	}
	
	@Test @Ignore
	public void testaEmpty() throws Exception{
		String path = "processoParteList";
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.processoParteList IS EMPTY";
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.empty(path));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(0)));
	}
	
	@Test @Ignore
	public void testaNotEmpty() throws Exception{
		String path = "processoParteList";
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o WHERE o.processoParteList IS NOT EMPTY";
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.addCriteria(Criteria.not(Criteria.empty(path)));
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(0)));
	}
	
	@Test @Ignore
	public void testaORAND() throws Exception{
		String path1 = "ano";
		String pathComum = "processoStatus";
		String expected = "SELECT COUNT(DISTINCT o) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"WHERE ((o.ano = :prm0 AND o.processoStatus = :prm1) OR (o.ano = :prm2 AND o.processoStatus != :prm3))";
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Criteria c0 = Criteria.and(Criteria.equals(path1, 2012), Criteria.equals(pathComum, ProcessoStatusEnum.D));
		Criteria c1 = Criteria.and(Criteria.equals(path1, 2013), Criteria.not(Criteria.equals(pathComum, ProcessoStatusEnum.D)));
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.setCount(true);
		search.addCriteria(Criteria.or(c0, c1));
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((Integer) params.get("prm0"), is(equalTo(2012)));
		assertThat((ProcessoStatusEnum) params.get("prm1"), is(equalTo(ProcessoStatusEnum.D)));
		assertThat((Integer) params.get("prm2"), is(equalTo(2013)));
		assertThat((ProcessoStatusEnum) params.get("prm3"), is(equalTo(ProcessoStatusEnum.D)));
	}

	@Test @Ignore
	public void testaORANDInline() throws Exception{
		String path1 = "ano";
		String pathComum = "processoStatus";
		String expected = "SELECT COUNT(DISTINCT o) FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"WHERE ((o.ano = :prm0 AND o.processoStatus = :prm1) OR (o.ano = :prm2 AND o.processoStatus != :prm3))";
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Search search = new Search(ProcessoTrf.class);
		search.setDistinct(true);
		search.setCount(true);
		search.addCriteria(
				Criteria.or(
						Criteria.and(
								Criteria.equals(path1, 2012), 
								Criteria.equals(pathComum, ProcessoStatusEnum.D)), 
						Criteria.and(
								Criteria.equals(path1, 2013), 
								Criteria.not(
										Criteria.equals(pathComum, ProcessoStatusEnum.D)))));
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat((Integer) params.get("prm0"), is(equalTo(2012)));
		assertThat((ProcessoStatusEnum) params.get("prm1"), is(equalTo(ProcessoStatusEnum.D)));
		assertThat((Integer) params.get("prm2"), is(equalTo(2013)));
		assertThat((ProcessoStatusEnum) params.get("prm3"), is(equalTo(ProcessoStatusEnum.D)));
	}
	
	@Test @Ignore
	public void testaSearchParte() throws Exception{
		Criteria obg = Criteria.equals("processoStatus", ProcessoStatusEnum.D);
		Criteria obg2 = Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.A);
		Criteria obg3 = Criteria.equals("processoParteList.parteSigilosa", false);
		Criteria c1 = Criteria.contains("processoParteList.pessoa.nome", "baylon");
		Criteria c2 = Criteria.contains("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.nome", "baylon");
		Criteria c3 = Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.ativo", true);
		Criteria c4 = Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.usadoFalsamente", false);
		Criteria c5 = Criteria.contains("processoParteList.pessoa.pessoaNomeAlternativoList.pessoaNomeAlternativo", "baylon");
		c5.setRequired("processoParteList.pessoa.pessoaNomeAlternativoList", false);
		Criteria orC = Criteria.or(c1, Criteria.and(c2, c3, c4),c5);
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(obg);
		search.addCriteria(obg2);
		search.addCriteria(obg3);
		search.addCriteria(orC);
		search.setDistinct(true);
		String expected = "SELECT DISTINCT o " +
				"FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.processoParteList AS p0 " +
				"INNER JOIN p0.pessoa.pessoaDocumentoIdentificacaoList AS p1 " +
				"LEFT JOIN p0.pessoa.pessoaNomeAlternativoList AS p2 " +
				"WHERE o.processoStatus = :prm0 " +
				"AND ( LOWER(to_ascii(p0.pessoa.nome)) LIKE :prm1 OR ( LOWER(to_ascii(p1.nome)) LIKE :prm2 AND p1.ativo = :prm3 AND p1.usadoFalsamente = :prm4) " +
				"OR LOWER(to_ascii(p2.pessoaNomeAlternativo)) LIKE :prm5) " +
				"AND p0.inSituacao = :prm6 AND p0.parteSigilosa = :prm7";
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(8)));
		assertThat((ProcessoStatusEnum) params.get("prm0"), is(equalTo(ProcessoStatusEnum.D)));
		assertThat((String) params.get("prm1"), is("%baylon%"));
		assertThat((String) params.get("prm2"), is("%baylon%"));
		assertThat((Boolean) params.get("prm3"), is(true));
		assertThat((Boolean) params.get("prm4"), is(false));
		assertThat((String) params.get("prm5"), is("%baylon%"));
		assertThat((ProcessoParteSituacaoEnum) params.get("prm6"), is(ProcessoParteSituacaoEnum.A));
		assertThat((Boolean) params.get("prm7"), is(false));
	}
	
//	@Test @Ignore
	public void testaAuto() throws Exception{
		Criteria c1 = Criteria.startsWith("processoParteList.pessoa.nome", "baylon");
		Criteria c2 = Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", "12345");
//		c2.exclusiveJoin("processoParteList.pessoa.pessoaDocumentoIdentificacaoList", true);
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(c1);
		search.addCriteria(c2);
		search.setDistinct(true);
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.processoParteList AS p0 " +
				"INNER JOIN p0.pessoa AS p1 " +
				"INNER JOIN o.processoParteList AS p2 " +
				"INNER JOIN p2.pessoa AS p3 " +
				"INNER JOIN p3.pessoaDocumentoIdentificacaoList AS p4 " +
				"WHERE p1.nome ( LOWER(to_ascii(p1.nome)) LIKE :prm0 " +
				"AND p4.numeroDocumento = prm1";
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(search, params), is(expected));
		assertThat(params.size(), is(equalTo(2)));
		assertThat((String) params.get("prm0"), is("baylon%"));
		assertThat((String) params.get("prm1"), is("12345"));
	}
	
//	@Test @Ignore
	public void testaAcervo() throws Exception {
		String expected = "SELECT DISTINCT o " +
				"FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.processoParteList AS p2 " +
				"LEFT JOIN p2.processoParteRepresentanteList AS p3 " +
				"INNER JOIN p3.representante AS p4 " +
				"LEFT JOIN o.visualizadores AS p0 " +
				"LEFT JOIN p0.pessoa AS p1 " +
				"WHERE ((p2.processoParteRepresentanteList IS NOT EMPTY AND p4.idUsuario = :prm0) OR p2.pessoa.idPessoa IN (:prm1)) " +
				"AND o.processoStatus = :prm2 " +
				"AND (o.segredoJustica = :prm3 OR (o.segredoJustica = :prm4 AND p1.idUsuario = :prm5)) " +
				"AND p2.inSituacao = :prm6";
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Search s = new Search(ProcessoTrf.class);
		s.setDistinct(true);
		Criteria representante = Criteria.equals("processoParteList.processoParteRepresentanteList.representante.idUsuario", 5692);
		representante.setRequired("processoParteList.processoParteRepresentanteList", false);
		Criteria visivel = Criteria.equals("visualizadores.pessoa.idUsuario", 5692);
		visivel.setRequired("visualizadores", false);
		visivel.setRequired("visualizadores.pessoa", false);
		Criteria sigilo = Criteria.or(Criteria.equals("segredoJustica", false), Criteria.and(Criteria.equals("segredoJustica", true), visivel));
		Criteria distribuido = Criteria.equals("processoStatus", ProcessoStatusEnum.D);
		Criteria parte = Criteria.and(
				Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.A),
				Criteria.or(
						Criteria.and(
								Criteria.not(Criteria.empty("processoParteList.processoParteRepresentanteList")),
								representante),
						Criteria.in("processoParteList.pessoa.idPessoa", new Integer[]{5692})
						));
		s.addCriteria(distribuido);
		s.addCriteria(sigilo);
		s.addCriteria(parte);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(s, params), is(expected));
	}

	@Test @Ignore
	public void testaComparacaoInterna() throws Exception {
		String expected = "SELECT DISTINCT o " +
				"FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o " +
				"INNER JOIN o.processoParteList AS p0 " +
				"INNER JOIN p0.processoParteRepresentanteList AS p1 " +
				"WHERE p0.pessoa = p1.representante";
		ProcessoJudicialDAO pdb = new ProcessoJudicialDAO();
		initDAO(pdb);
		Search s = new Search(ProcessoTrf.class);
		s.setDistinct(true);
		Criteria single = Criteria.equals(Criteria.path("processoParteList.pessoa"), Criteria.path("processoParteList.processoParteRepresentanteList.representante"));
		s.addCriteria(single);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(pdb.createQueryString(s, params), is(expected));
	}
	
	@Test @Ignore
	public void testaLeftJoin() throws Exception{
		String expected = "SELECT DISTINCT o FROM br.jus.pje.nucleo.entidades.SessaoProcessoDocumento AS o LEFT JOIN o.sessao AS p0 WHERE (o.sessao IS NULL OR p0.dataFechamentoSessao IS NULL )";
		SessaoProcessoDocumentoDAO spd = new SessaoProcessoDocumentoDAO();
		initDAO(spd);
		Search s = new Search(SessaoProcessoDocumento.class);
		s.setDistinct(true);
		Criteria dtFech = Criteria.isNull("sessao.dataFechamentoSessao");
		dtFech.setRequired("sessao", false);
		Criteria or = Criteria.or(Criteria.isNull("sessao"), dtFech);
		s.addCriteria(or);
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(spd.createQueryString(s, params), is (expected));
	}
	
	@Test
	public void testaOrAninhado() throws Exception{
		String expected = "SELECT o FROM br.jus.pje.nucleo.entidades.ProcessoTrf AS o "
				+ "LEFT JOIN o.visualizadores AS p0 "
				+ "LEFT JOIN p0.pessoa AS p1 "
				+ "WHERE "
				+ "(o.segredoJustica = :prm0 "
				+ "OR (o.segredoJustica = :prm1 AND p1.idPessoa IN (:prm2)) "
				+ "OR (o.segredoJustica = :prm3 OR (o.segredoJustica = :prm4 AND o.orgaoJulgadorCargo.idOrgaoJulgadorCargo = :prm5)))";
		ProcessoJudicialDAO spd = new ProcessoJudicialDAO();
		initDAO(spd);
		
		Search s = new Search(ProcessoTrf.class);
		Criteria naoSigiloso = Criteria.equals("segredoJustica", false);
		Criteria sigiloso = Criteria.equals("segredoJustica", true);
		Criteria orgao = Criteria.equals("orgaoJulgadorCargo.idOrgaoJulgadorCargo", 10);
		Criteria visivel = Criteria.in("visualizadores.pessoa.idPessoa", new Integer[]{1,2,3});
		visivel.setRequired("visualizadores", false);
		visivel.setRequired("visualizadores.pessoa", false);
		s.addCriteria(Criteria.or(naoSigiloso, Criteria.and(sigiloso, visivel), Criteria.or(naoSigiloso, Criteria.and(sigiloso, orgao))));
		
		Map<String, Object> params = new HashMap<String, Object>();
		assertThat(spd.createQueryString(s, params), is (expected));
	}
}
