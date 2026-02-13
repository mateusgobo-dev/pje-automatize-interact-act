/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.jbpm.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.demo.html.HTMLParser;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.ibpm.search.Indexer;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.FileUtil;

@Name(Reindexer.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class Reindexer {

	public static final String NAME = "reindexer";
	private static final LogProvider log = Logging.getLogProvider(Reindexer.class);

	@SuppressWarnings("unchecked")
	public void execute() {
		MeasureTime mt = new MeasureTime().start();
		log.info("----------- Criando indices de documentos -------------");
		Session session = ManagedJbpmContext.instance().getSession();
		List<TaskInstance> list = session.createQuery("select ti from org.jbpm.taskmgmt.exe.TaskInstance as ti").list();
		try {
			File path = Indexer.getIndexerPath();
			delete(path);
			Indexer indexer = new Indexer(path);
			for (TaskInstance ti : list) {
				Map<String, String> fields = new HashMap<String, String>();
				fields.put("conteudo", getTextoIndexavel(SearchHandler.getConteudo(ti)));
				indexer.index(ti.getId() + "", Collections.EMPTY_MAP, fields);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("----------- indices de documentos criado ------------- " + mt.getTime() + " ms");
	}

	public static void delete(File path) {
		File[] files = path.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					file.delete();
				}
			}
		}
	}

	public static String getTextoIndexavel(String texto) {
		BufferedReader br = null;
		Reader reader = null;
		try {
			reader = new HTMLParser(new StringReader(texto)).getReader();
			br = new BufferedReader(reader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line).append(System.getProperty("line.separator"));
			}
			return sb.toString();
		} catch (Exception e) {
		} finally {
			FileUtil.close(reader);
			FileUtil.close(br);
		}
		return texto;
	}

	public static Reindexer instance() {
		return (Reindexer) Component.getInstance(NAME);
	}

}