package br.com.infox.editor.manager;

import java.io.Serializable;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.ibpm.help.HelpUtil;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;

@Name(ProcessoDocumentoEstruturadoIndexManager.NAME)
@AutoCreate
public class ProcessoDocumentoEstruturadoIndexManager extends ProcessoDocumentoEstruturadoManager implements Serializable {

	private static final int LIMITE_CONSULTA = 3000;

	private static final long serialVersionUID = 1L;
	
	private static final LogProvider log = Logging.getLogProvider(ProcessoDocumentoEstruturadoIndexManager.class);

	public static final String NAME = "processoDocumentoEstruturadoIndexManager";
	
	private static String[] camposIndexaveis = {ProcessoDocumentoEstruturadoTopico.NOME_INDEX_TITULO, 
		ProcessoDocumentoEstruturadoTopico.NOME_INDEX_CONTEUDO};
	
	
	public Query createLuceneQueryTexto(String textoPesquisar) throws ParseException {
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_31, camposIndexaveis, HelpUtil.getAnalyzer());
		Query query = parser.parse(textoPesquisar);
		return query;
	}
	
	public Query createLuceneQueryIdPessoa(int idPessoa) throws ParseException {
		QueryParser parserId = new QueryParser(Version.LUCENE_31, ProcessoDocumentoEstruturadoTopico.NOME_INDEX_PESSOA, new StandardAnalyzer(Version.LUCENE_31));
		Query queryIdPessoa = parserId.parse(String.valueOf(idPessoa));
		return queryIdPessoa;
	}	
	
	public Query createLuceneQueryData(String data) throws ParseException {
		QueryParser parserId = new QueryParser(Version.LUCENE_31, ProcessoDocumentoEstruturadoTopico.NOME_INDEX_DATA_MODIFICACAO, new StandardAnalyzer(Version.LUCENE_31));
		Query query = parserId.parse(data);
		return query;
	}		
	
	public Query createLuceneQueryIdTipoDocumento(int idTipoDocumento) throws ParseException {
		QueryParser parserId = new QueryParser(Version.LUCENE_31, ProcessoDocumentoEstruturadoTopico.NOME_INDEX_TIPO_DOCUMENTO, new StandardAnalyzer(Version.LUCENE_31));
		Query query = parserId.parse(String.valueOf(idTipoDocumento));
		return query;
	}		
	
	@SuppressWarnings("unchecked")
	public BeanPesquisaIndexadaId getFullTextQueryIdPesquisa(String termoPesquisa, Query... querys) throws ParseException {
		MeasureTime mt = new MeasureTime(true);
		FullTextEntityManager em = (FullTextEntityManager) EntityUtil.getEntityManager();
		
		BooleanQuery bq = new BooleanQuery();
		Query luceneQueryTexto = createLuceneQueryTexto(termoPesquisa);
		bq.add(luceneQueryTexto, Occur.MUST);
		for (Query query : querys) {
			bq.add(query, Occur.MUST);
		}

		FullTextQuery textQuery = em.createFullTextQuery(bq, ProcessoDocumentoEstruturadoTopico.class);
		
		textQuery.setProjection("idProcessoDocumentoEstruturadoTopico");
		
		textQuery.setMaxResults(LIMITE_CONSULTA);
		
		Sort sort = new Sort(new SortField(ProcessoDocumentoEstruturadoTopico.NOME_INDEX_DATA_MODIFICACAO, SortField.STRING_VAL, true));	
//		Sort sort = new Sort(ProcessoDocumentoEstruturadoTopico.NOME_INDEX_DATA_MODIFICACAO, true);	
		textQuery.setSort(sort);
		
		BeanPesquisaIndexadaId beanPesquisaIndexadaId = new BeanPesquisaIndexadaId();
		beanPesquisaIndexadaId.query = luceneQueryTexto;
		beanPesquisaIndexadaId.resultList = textQuery.getResultList();
		log.info("getFullTextQueryIdPesquisa - " + mt.getTime());
		return beanPesquisaIndexadaId;
	}			
	
	public void reindex() {
		ReindexManager manager =  ComponentUtil.getComponent(ReindexManager.NAME);
		manager.reindex(ProcessoDocumentoEstruturadoTopico.class);
	}
	
	
	public class BeanPesquisaIndexadaId {
		private List<Object[]> resultList;
		private Query query;
		
		public List<Object[]> getResultList() {
			return resultList;
		}
		
		public Query getQuery() {
			return query;
		}
	}
	
}
