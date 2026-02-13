package br.jus.cnj.pje.servicos;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrevencaoServiceTest {

	@Test
	public void testePrevencao() {
		IndexSearcher searcher = null;
		try {
			Directory directory = FSDirectory.open(new File("/home/thiago/Desenvolvimento/Servidor/wildfly-9.0.2.Final/bin/indices/br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao/"));
			IndexReader reader = IndexReader.open(directory);
			searcher = new IndexSearcher(reader);
			TopDocs topDocs = searcher.search(criarQuery(), 10000);
			ScoreDoc[] hits = topDocs.scoreDocs;
			Assert.assertTrue(hits.length > 0);
//			for (int i = 0; i < hits.length; i++) {
//				int docId = hits[i].doc; 
//				Document d = searcher.doc(docId);
//				System.out.println(d.get("pessoa.idUsuario"));
//			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			try {
				searcher.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Query criarQuery() throws ParseException {
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(new TermQuery(new Term("pessoa.nome", "ROMARIO SOARES")), Occur.SHOULD);
		booleanQuery.add(new TermQuery(new Term("numeroDocumento", "CPF+95085068149")), Occur.SHOULD);
		return booleanQuery;
	}
	
	@Test
	public void testaParticionaIds() {
		Set<Integer> ids = load112Ids();
		PrevencaoService service = new PrevencaoService();
		
		List<List<Integer>> roundsTrips =  service.particionaIds(ids, 50);
		
		assertEquals(3, roundsTrips.size());
		assertEquals(50, roundsTrips.get(0).size());
		assertEquals(50, roundsTrips.get(1).size());
		assertEquals(12, roundsTrips.get(2).size());
	}
	
	private Set<Integer> load112Ids() {
		Set<Integer> ids = new HashSet<>();
		ids.add(7386);
		ids.add(7387);
		ids.add(7388);
		ids.add(7389);
		ids.add(7390);
		ids.add(7391);
		ids.add(7392);
		ids.add(7393);
		ids.add(7394);
		ids.add(7395);
		ids.add(7396);
		ids.add(7397);
		ids.add(7398);
		ids.add(7399);
		ids.add(7400);
		ids.add(7401);
		ids.add(7402);
		ids.add(7403);
		ids.add(7404);
		ids.add(7405);
		ids.add(7406);
		ids.add(7407);
		ids.add(7408);
		ids.add(7409);
		ids.add(7410);
		ids.add(7411);
		ids.add(7412);
		ids.add(7413);
		ids.add(7414);
		ids.add(7415);
		ids.add(7416);
		ids.add(7417);
		ids.add(7418);
		ids.add(7419);
		ids.add(7420);
		ids.add(7421);
		ids.add(7422);
		ids.add(7423);
		ids.add(7424);
		ids.add(7425);
		ids.add(7426);
		ids.add(7427);
		ids.add(7428);
		ids.add(7429);
		ids.add(7430);
		ids.add(7431);
		ids.add(7432);
		ids.add(7433);
		ids.add(7434);
		ids.add(7435);
		ids.add(7436);
		ids.add(7437);
		ids.add(7438);
		ids.add(7439);
		ids.add(7440);
		ids.add(7441);
		ids.add(7442);
		ids.add(7443);
		ids.add(7444);
		ids.add(7445);
		ids.add(7446);
		ids.add(7447);
		ids.add(7448);
		ids.add(7449);
		ids.add(7450);
		ids.add(7451);
		ids.add(7452);
		ids.add(7453);
		ids.add(7454);
		ids.add(7455);
		ids.add(7456);
		ids.add(7457);
		ids.add(7458);
		ids.add(7459);
		ids.add(7460);
		ids.add(7461);
		ids.add(7462);
		ids.add(7463);
		ids.add(7464);
		ids.add(7465);
		ids.add(7466);
		ids.add(7467);
		ids.add(7468);
		ids.add(7469);
		ids.add(7470);
		ids.add(7471);
		ids.add(7472);
		ids.add(7473);
		ids.add(7474);
		ids.add(7475);
		ids.add(7476);
		ids.add(7477);
		ids.add(7478);
		ids.add(7479);
		ids.add(7480);
		ids.add(7481);
		ids.add(7482);
		ids.add(7483);
		ids.add(7484);
		ids.add(7485);
		ids.add(7486);
		ids.add(7487);
		ids.add(7488);
		ids.add(7489);
		ids.add(7490);
		ids.add(7491);
		ids.add(7492);
		ids.add(7493);
		ids.add(7494);
		ids.add(7495);
		ids.add(7496);
		ids.add(7497);
		return ids;
	}
}
