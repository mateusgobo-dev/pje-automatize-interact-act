package br.com.infox.cliente.util;

import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordMarkerFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.PhoneticFilter;

/**
 * Analyzer for Brazilian language. Supports an external list of stopwords
 * (words that will not be indexed at all) and an external list of exclusions
 * (word that will not be stemmed, but indexed).
 * 
 */
public final class PJEBrazilianAnalyzer extends Analyzer {
	
	private final static Version LUCENE_VERSION = Version.LUCENE_35;

	/**
	 * List of typical Brazilian stopwords.
	 */
	public final static String[] BRAZILIAN_STOP_WORDS = { "a", "ainda", "alem", "ambas", "ambos", "antes", "ao",
			"aonde", "aos", "apos", "aquele", "aqueles", "as", "assim", "com", "como", "contra", "contudo", "cuja",
			"cujas", "cujo", "cujos", "da", "das", "de", "dela", "dele", "deles", "demais", "depois", "desde", "desta",
			"deste", "dispoe", "dispoem", "diversa", "diversas", "diversos", "do", "dos", "durante", "e", "ela",
			"elas", "ele", "eles", "em", "entao", "entre", "essa", "essas", "esse", "esses", "esta", "estas", "este",
			"estes", "ha", "isso", "isto", "logo", "mais", "mas", "mediante", "menos", "mesma", "mesmas", "mesmo",
			"mesmos", "na", "nas", "nao", "nas", "nem", "nesse", "neste", "nos", "o", "os", "ou", "outra", "outras",
			"outro", "outros", "pelas", "pelas", "pelo", "pelos", "perante", "pois", "por", "porque", "portanto",
			"proprio", "propios", "quais", "qual", "qualquer", "quando", "quanto", "que", "quem", "quer", "se", "seja",
			"sem", "sendo", "seu", "seus", "sob", "sobre", "sua", "suas", "tal", "tambem", "teu", "teus", "toda",
			"todas", "todo", "todos", "tua", "tuas", "tudo", "um", "uma", "umas", "uns" };

	/**
	 * Contains the stopwords used with the StopFilter.
	 */
	private Set<?> stoptable = new HashSet<>();

	/**
	 * Contains words that should be indexed but not stemmed.
	 */
	private Set<?> excltable = new HashSet<>();

	/**
	 * Builds an analyzer with the default stop words (
	 * {@link #BRAZILIAN_STOP_WORDS}).
	 */
	public PJEBrazilianAnalyzer() {
		stoptable = StopFilter.makeStopSet(LUCENE_VERSION, BRAZILIAN_STOP_WORDS);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 */
	public PJEBrazilianAnalyzer(String[] stopwords) {
		stoptable = StopFilter.makeStopSet(LUCENE_VERSION, stopwords);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 */
	public PJEBrazilianAnalyzer(Map<?,?> stopwords) {
		stoptable = new HashSet<>(stopwords.keySet());
	}


	/**
	 * Builds an exclusionlist from an array of Strings.
	 */
	public void setStemExclusionTable(String[] exclusionlist) {
		excltable = StopFilter.makeStopSet(LUCENE_VERSION, exclusionlist);
	}

	/**
	 * Builds an exclusionlist from a Hashtable.
	 */
	public void setStemExclusionTable(Map<?,?> exclusionlist) {
		excltable = new HashSet<>(exclusionlist.keySet());
	}

	/**
	 * Creates a TokenStream which tokenizes all the text in the provided
	 * Reader.
	 * 
	 * @return A TokenStream build from a StandardTokenizer filtered with
	 *         StandardFilter, StopFilter, GermanStemFilter and LowerCaseFilter.
	 */
	@Override
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		Class<? extends Encoder> clazz = DoubleMetaphone.class;
		Encoder encoder = null;
		try {
			encoder = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		TokenStream result = new StandardTokenizer(LUCENE_VERSION, reader);

		result = new StandardFilter(LUCENE_VERSION, result);
		result = new StopFilter(LUCENE_VERSION, result, stoptable);
		result = new KeywordMarkerFilter(result, excltable);
		result = new BrazilianStemFilter(result);
		result = new LowerCaseFilter(LUCENE_VERSION, result);
		result = new PhoneticFilter(result, encoder, fieldName, true);

		return result;
	}
}
