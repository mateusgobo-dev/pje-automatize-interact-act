/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 * <p>
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 **/
package br.jus.pje.indexacao;

import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.json.JSONObject;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provedor de indexação de objetos em repositório de índices elasticsearch.
 *
 * Este provedor somente executará a indexação se:
 * <li>tiverem sido definidos os parâmetros {@link Parametros#ELASTICSEARCHIDXURL} e {@link Parametros#ELASTICSEARCHIDXNAME}</li>
 * <li>tiverem sido mapeados os beans indexáveis utilizando as anotações {@link IndexedEntity} e {@link Mapping}</li>
 *
 *	Este componente criará automaticamente os índices respectivos para cada uma das classes indexáveis autônomas,
 *	adotando como nome do índice {@link IndexedEntity#value()}.
 *
 *	Idealmente, os índices devem ser previamente criados no servidor elasticsearch indicando os critérios de indexação desejados
 *	e, especialmente, os analyzers a serem adotados.
 *
 * @author cristof
 *
 * @see "www.elasticsearch.org"
 *
 */
@AutoCreate
@Name("elasticSearchProvider")
@SuppressWarnings({"deprecation", "resource", "unchecked"})
public class ElasticSearchProvider {

    @Logger
    private Log logger;

    @In
    private ParametroService parametroService;

    private static String indexURL = ConfiguracaoIntegracaoCloud.getElasticUrl();
    
    private static String indexName = ConfiguracaoIntegracaoCloud.getElasticIndexName();

    private final Indexer indexer = new Indexer();

    private Map<Class<?>, IndexingMapping> indexMapping;

    private Map<Class<?>, List<String>> ownerMapping;

    private boolean enabled = true;
    private long timeout;
    private long tentativas;

    public static class TimeOutException extends IOException {
        public TimeOutException(String message, Exception exception) {
            super(message, exception);
        }
    }

    public void enable() {
        this.enabled = true;
    }

    private void disable(String message, Object...args){
        logger.error(message, args);
        logger.error("Tentativa: {0}", ++tentativas);
        if (this.tentativas >= 3) {
            this.timeout = System.currentTimeMillis();
            this.enabled = false;
            this.tentativas = 0;

            //todo - gerar email avisando sobre problema.
        }
    }

    public boolean isEnabled() {
        enabled = enabled && (System.currentTimeMillis() - timeout) > 1000 * 60 * 60 * 15;

        boolean isConfigured = indexURL != null && !indexURL.isEmpty() && indexName != null && !indexName.isEmpty();
        if (!isConfigured) {
            logger.warn("A indexação não está habilitada nesta instalação. Verifique os parâmetros {0} e {1}.", Parametros.ELASTICSEARCHIDXURL, Parametros.ELASTICSEARCHIDXURL);
        }

        boolean isEnabled = enabled && isConfigured;
        if (!isEnabled) {
            logger.warn("A indexação está desabilitada temporariamente.");
        }
        return isEnabled;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void create() throws TimeOutException {
        this.create(indexURL, indexName);
    }

    public void destroy(String type) throws TimeOutException {
        this.destroy(indexURL, indexName, type);
    }

    public Integer count(String type) throws TimeOutException {
        return this.count(indexURL, indexName, type);
    }

    public void update(String type, JSONObject jsonObject) throws TimeOutException {
        update(indexURL, indexName, type, jsonObject);
    }

    public <T> void indexar(String type, Class<T> clazz, Object id) throws TimeOutException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, PJeBusinessException {
        if (indexer.isIndexable(clazz, indexMapping)) {
            Set<Owner> owners = new HashSet<Owner>();

            T object = getManager(clazz).findById(id);
            indexer.getOwners(clazz, object, indexMapping, ownerMapping, owners);
            if (owners.isEmpty()) {
                indexar(type, object);
            } else {
                for (Owner o : owners) {
                    indexar(type, o.getOwnerClass(), o.getOwnerId());
                }
            }
        }
    }

    private <T> void indexar(String type, Object object) throws TimeOutException {
        if (object != null) {
            if ((ProcessoTrf.class.isAssignableFrom(object.getClass()) && ((ProcessoTrf) object).getProcessoStatus() == ProcessoStatusEnum.D)) {
                JSONObject jsonObject = indexer.toIndexableJSON(object, indexMapping);
                indexar(type, (String) jsonObject.get("id_"), jsonObject);
            }
        }
    }

    public void indexar(String type, String id, JSONObject jsonObject) throws TimeOutException {
        indexar(indexURL, indexName, type, id, jsonObject);
    }

    public JSONObject search(String type, JSONObject jsonObject) throws TimeOutException {
        return search(indexURL, indexName, type, jsonObject);
    }

    private void update(String url, String index, String type, JSONObject jsonObject) throws TimeOutException {
        if (isEnabled() && jsonObject != null) {
            try {
            	HttpPost httpPost = new HttpPost(url + "/" + index + "/_mapping/" + type);
            	httpPost.setEntity(new StringEntity(jsonObject.toString()));
            	HttpResponse httpResponse = getHttpClient().execute(httpPost);
            	logger.info("#Elastic - POST: {0} statusCode: {1}, json: {2}", httpPost.getURI().toURL(), httpResponse.getStatusLine().getStatusCode(), jsonObject);
            	            	
            } catch (IOException ioException) {
                this.disable("Falha ao realizar update(url: {0}, index: {1}, type: {2}, json: {3})", url, index, type, jsonObject);
                throw new TimeOutException("Falha ao realizar update", ioException);
            }
        }
    }

    private void indexar(String url, String index, String type, String id, JSONObject jsonObject) throws TimeOutException {
        if (isEnabled() && jsonObject != null) {
            try {
                
            	HttpPost httpPost = new HttpPost(url + "/" + index + "/" + type + "/" + id);
            	StringEntity stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
            	stringEntity.setContentType("application/json");
            	
            	httpPost.setEntity(stringEntity);
            	HttpResponse httpResponse = getHttpClient().execute(httpPost);
            	logger.info("#Elastic - POST: {0} statusCode: {1}, json: {2}", httpPost.getURI().toURL(), httpResponse.getStatusLine().getStatusCode(), jsonObject);
            	if (httpResponse.getStatusLine().getStatusCode() >= 300) {
            		logger.error(Severity.ERROR, "#Elastic - Erro ao tentar indexar informa  es: {0}, {1}.", httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
            	}
            	
            } catch (IOException ioException) {
                this.disable("Falha ao realizar indexar(url: {0}, index: {1}, type: {2}, id: {3}, json: {4})", url, index, type, id, jsonObject);
                throw new TimeOutException("#Elastic - Falha ao realizar indexar", ioException);
            }
        }
    }

    private JSONObject search(String url, String index, String type, String query) throws TimeOutException {
        if (isEnabled() && query != null && !query.isEmpty()) {
            try {
                HttpGet httpGet = new HttpGet(url + "/" + index + "/" + type + "/" + "_search?pretty&size=100?q=" + query.replaceAll("\\s", "%20"));
                HttpResponse httpResponse = getHttpClient().execute(httpGet);
                logger.info("#Elastic - GET: {0} statusCode: {1}", httpGet.getURI().toURL(), httpResponse.getStatusLine().getStatusCode());
                return buildResponse(httpResponse.getEntity().getContent());
            } catch (IOException ioException) {
                this.disable("Falha ao realizar search(url: {0}, index: {1}, type: {2}, query: {3})", url, index, type, query);
                throw new TimeOutException("Falha ao realizar search", ioException);
            }
        }
        return null;
    }

    private JSONObject search(String url, String index, String type, JSONObject jsonObject) throws TimeOutException {
        if (isEnabled() && jsonObject != null) {
            try {
                StringEntity stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
                stringEntity.setContentType("application/json");

                HttpPost httpPost = new HttpPost(url + "/" + index + "/" + type + "/_search?pretty&size=100");
                httpPost.setEntity(stringEntity);
                HttpResponse httpResponse = getHttpClient().execute(httpPost);
                logger.info("#Elastic - POST: {0} statusCode: {1}", httpPost.getURI().toURL(), httpResponse.getStatusLine().getStatusCode());
                return buildResponse(httpResponse.getEntity().getContent());
            } catch (IOException ioException) {
                this.disable("Falha ao realizar search(url: {0}, index: {1}, type: {2}, json: {3})", url, index, type, jsonObject);
                throw new TimeOutException("Falha ao realizar search", ioException);
            }
        }
        return null;
    }

    private void create(String url, String index) throws TimeOutException {
        if (isEnabled()) {
            try {
                StringEntity stringEntity = new StringEntity("{}", "UTF-8");
                stringEntity.setContentType("application/json");

                HttpPut httpPut = new HttpPut(url + "/" + index);
                httpPut.setEntity(stringEntity);

                HttpResponse httpResponse = getHttpClient().execute(httpPut);
                logger.info("#Elastic - PUT: {0} statusCode: {1}", httpPut.getURI().toURL(), httpResponse.getStatusLine().getStatusCode());
            } catch (IOException ioException) {
                this.disable("Falha ao realizar create(url: {0}, index: {1})", url, index);
                throw new TimeOutException("Falha ao realizar create", ioException);
            }
        }
    }

    private void destroy(String url, String index, String type) throws TimeOutException {
        if (isEnabled()) {
            try {
                HttpDelete httpDelete = new HttpDelete(url + "/" + index);
                HttpResponse httpResponse = getHttpClient().execute(httpDelete);                
                logger.info("#Elastic - DELETE: {0} statusCode: {1}", httpDelete.getURI().toURL(), httpResponse.getStatusLine().getStatusCode());
            } catch (IOException ioException) {
                this.disable("#Elastic - Falha ao realizar destroy(url: {0}, index: {1}, type: {2})", url, index, type);
                throw new TimeOutException("Falha ao realizar destroy", ioException);
            }
        }
    }

    private Integer count(String url, String index, String type) throws TimeOutException {
        if (isEnabled()) {
            try {
                HttpGet httpGet = new HttpGet(url + "/" + index + "/" + type + "/_count");

                HttpResponse httpResponse = getHttpClient().execute(httpGet);
                JSONObject jsonObject = buildResponse(httpResponse.getEntity().getContent());
                logger.info("#Elastic - GET: {0} statusCode: {1}", httpGet.getURI().toURL(), httpResponse.getStatusLine().getStatusCode());
                return jsonObject.getInt("count");
            } catch (IOException ioException) {
                this.disable("Falha ao realizar count(url: {0}, index: {1}, type: {2})", url, index, type);
                throw new TimeOutException("Falha ao realizar status", ioException);
            } catch (Exception exception) {
            }
        }
        return null;
    }

    private <T> BaseManager<T> getManager(Class<T> clazz) {
        if (ProcessoTrf.class.isAssignableFrom(clazz)) {
            return (BaseManager<T>) Component.getInstance("processoJudicialManager");
        } else {
            return (BaseManager<T>) Component.getInstance(Introspector.decapitalize(clazz.getSimpleName()) + "Manager");
        }
    }

    private HttpClient getHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter("http.protocol.version", org.apache.http.HttpVersion.HTTP_1_1);
        httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        return httpClient;
    }

    private JSONObject buildResponse(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return new JSONObject(stringBuilder.toString());
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
