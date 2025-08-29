package br.jus.pje.indexacao;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.json.JSONObject;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle de indexação de objetos JavaBean.
 *
 * @author cristof
 */
@AutoCreate
@Name("indexador")
@Scope(ScopeType.APPLICATION)
@SuppressWarnings("unchecked")
public class Indexador {

    private static final String EVENT_REINDEX_TARGET = "pje:elasticsearch:reindex";
    private static final String EVENT_REINDEX_CHECK = "pje:elasticsearch:check";

    private static final String PARAM_REINDEX_BASE = "pje:elasticsearch:reindex:%s:%s:base";
    private static final String PARAM_REINDEX_ID = "pje:elasticsearch:reindex:%s:%s:id";
	private static final String PARAM_REINDEX_STARTED = "pje:elasticsearch:reindex:%s:%s:started";

    private static final Map<Class<?>, IndexingMapping> indexingMappingHashMap = new HashMap<Class<?>, IndexingMapping>(0);
    private static final Map<Class<?>, List<String>> owners = new HashMap<Class<?>, List<String>>();
    private static final Class<?>[] classes = {PessoaDocumentoIdentificacao.class};
    
    private static boolean instanciaElastic = ConfiguracaoIntegracaoCloud.isElasticInstanceReindex();

    @Logger
    private Log logger;

    @In
    private Context applicationContext;

    @In
    private ParametroService parametroService;

    @In
    private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;

    private Indexer indexer;
    private ElasticSearchProvider elasticSearchProvider;

    public static Map<Class<?>, IndexingMapping> getMapaIndexacao() {
        return indexingMappingHashMap;
    }

    protected static Map<Class<?>, List<String>> getOwners() {
        return owners;
    }

    public boolean isEnabled() {
        return elasticSearchProvider != null && elasticSearchProvider.isEnabled();
    }

    /**
     * Método de inicialização do componente de indexação. Esse componente
     * realiza a indexação das classes definidas no campo {@link #classes} deste
     * componente.
     */
    @Create
    public void init() throws ElasticSearchProvider.TimeOutException {
		indexer = new Indexer();
		elasticSearchProvider = ComponentUtil.getComponent(ElasticSearchProvider.class);
		if (elasticSearchProvider.isEnabled()) {
            try {
                processarInicioIndexador();
                createIndex();
                updateMappings();
                checkPendingOperations();
            } catch (ElasticSearchProvider.TimeOutException timeOutException) {
                logger.info("#Elastic - Falha ao iniciar Indexador por timeout do elasticsearch");
            }
		}
    }

    public void reindex() throws ElasticSearchProvider.TimeOutException {
        Integer fromId = 0;

        if (elasticSearchProvider.isEnabled() && instanciaElastic) {
            applicationContext.set(PARAM_REINDEX_STARTED, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

            processarInicioIndexador();
            createIndex();
            updateMappings();

            for (TargetKindEnum target : TargetKindEnum.VALUES) {
                setParameterId(target, fromId);
                ComponentUtil.getComponent(Events.class).raiseAsynchronousEvent(EVENT_REINDEX_TARGET, target, fromId);
            }
        }
    }

    @Transactional
    @Observer("org.jboss.seam.postInitialization")
    public void postInitialization() {
        ComponentUtil.getComponent(Events.class).raiseAsynchronousEvent(EVENT_REINDEX_CHECK);
    }

	@Transactional
    @Observer(EVENT_REINDEX_CHECK)
    public void checkPendingOperations() {
		for (TargetKindEnum target : TargetKindEnum.VALUES) {
			Integer fromId = getParameterId(target);
			if (fromId != null) {
				ComponentUtil.getComponent(Events.class).raiseAsynchronousEvent(EVENT_REINDEX_TARGET, target, fromId);
			}
		}
    }

    private void processarInicioIndexador() {
        for (Class<?> clazz : classes) {
            try {
                indexer.createIndexMetadata(clazz, indexingMappingHashMap, owners);
            } catch (Exception exception) {
                logger.info("#Elastic - Erro no mapeamento da classe {0}: {1}", clazz.getCanonicalName(), exception.getLocalizedMessage());
            }
        }
    }
    public void createIndex() throws ElasticSearchProvider.TimeOutException {
        elasticSearchProvider.create();
    }

    public void updateMappings() {
        for (Class<?> clazz : classes) {
            try {
                JSONObject mapping = indexer.createJSONMappings(clazz, indexingMappingHashMap);
				elasticSearchProvider.update(indexingMappingHashMap.get(clazz).getName(), mapping);
            } catch (Exception exception) {
                logger.info("#Elastic - Erro ao tentar atualizar os mapas de indexação para a classe {0}: {1}", clazz.getCanonicalName(), exception.getLocalizedMessage());
            }
        }
    }

    private Integer getParameterId(TargetKindEnum target) {
        try {
            applicationContext.remove(target.getParameterNameId(elasticSearchProvider.getIndexName()));
            String value = parametroService.valueOf(target.getParameterNameId(elasticSearchProvider.getIndexName()));
			return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private Integer getParameterId(TargetKindEnum target, Integer defaultValue) {
        return Optional.ofNullable(getParameterId(target)).orElse(defaultValue);
    }

    private void setParameterId(TargetKindEnum target, Integer fromId) {
        parametroService.setValue(target.getParameterNameId(elasticSearchProvider.getIndexName()), fromId == null ? null : fromId.toString());
    }

    public boolean isIndexing() {
        boolean isIdenxing = false;
        for (final TargetKindEnum target : TargetKindEnum.VALUES) {
            final Integer lastId = getParameterId(target);
            isIdenxing = isIdenxing || lastId != null;
        }
        return isIdenxing;
    }

    /**
     * Observador do evento de reindexao de todos os documentos de identificao da base de dados.
     * <p>
     * Esse observador no  transacional, por si, mas cria e encerra transaes
     * relativas  indexao de 100 documentos de identificao por vez, at o trmino da indexao.
     * <p>
     * O observador inclui, no contexto de aplicao, trs variveis:
     *
     * <li>pje:elasticsearch:reindex:target com o nome do elemento alvo da reindexacao</li>
     * <li>pje:elasticsearch:reindex:started com a data em que a reindexao teve seu incio</li>
     * <li>pje:elasticsearch:reindex:percent com o nmero percentual aproximado de documentos indexados</li>
     * <p>
     * Aps a indexao, essas variveis so retiradas do contexto.
     *
     * @param size o nmero total de registros de documentos de identificao a serem indexados.
     */
    @Observer(Eventos.REINDEXAR_DOCUMENTOS_IDENTIFICACAO)
    public void indexarDocumentosIdentificacao(Long size) {
        indexar(TargetKindEnum.DocumentosIdentificacao, getParameterId(TargetKindEnum.DocumentosIdentificacao, 0));
    }

	@Observer(EVENT_REINDEX_TARGET)
    public void indexar(TargetKindEnum target, Integer fromId) {
		if (elasticSearchProvider.isEnabled() && instanciaElastic) {
			final int batchSize = 500;

            applicationContext.set("pje:elasticsearch:reindex:started", "S");

            Integer lastId = fromId;
            int offset = 0;
			try {
				if (fromId == 0) {
                    elasticSearchProvider.destroy(target.name);
					setParameterId(target, 1);
				}

				int size = getCount(target, lastId);
                int total = getCount(target, 0);
                offset = total - size;

				logger.info("#Elastic - Indexando um total de {0} entidades do tipo {1}.", size, target.name);
				

				while (offset < total && elasticSearchProvider.isEnabled()) {					
                    final long inicio = System.currentTimeMillis();
                    List<Integer> ids = indexar(target, lastId, batchSize);            
                    

                    final Integer savedId = getParameterId(target);
                    if (savedId != null) {
                        lastId = ids.isEmpty() ? null : ids.get(ids.size() - 1);
                        setParameterId(target, lastId);

                        offset += ids.size();

                        int percent = (int)(((float)offset)/((float)total) * 100);
                        long interval = Math.max((System.currentTimeMillis() - inicio) / 1000, 1);
                        applicationContext.set("pje:elasticsearch:reindex:percent", percent);
                        logger.info("#Elastic - Indexados {0} de {1} ({2}%) registros da entidade {3} com um total de {4} no ElastiSearch (media de {5} un/s).", offset, total, percent, target.name, elasticSearchProvider.count(target.name), batchSize / interval);
                    } else {
                        break;
                    }
				}

				if (elasticSearchProvider.isEnabled()) {
                    logger.info("#Elastic - Indexados {0} registros da entidade {1} com um total de {2} no ElasticSearch", total, target.name, elasticSearchProvider.count(target.name));
					setParameterId(target, null);
				}

			} catch (Exception exception) {
				logger.info("#Elastic - Erro ao realizar a indexação de {1}. {0}", exception, target);
			} finally {
				applicationContext.remove(target.getParameterNameBase(elasticSearchProvider.getIndexName()));
				applicationContext.remove(PARAM_REINDEX_STARTED);
                applicationContext.remove("pje:elasticsearch:reindex:started");
			}
		}
    }

    private List<Integer> indexar(TargetKindEnum target, Integer fromId, Integer size) {
		final List<Integer> indexados = new ArrayList<>();
		if (elasticSearchProvider.isEnabled() && instanciaElastic && target != null && fromId != null) {
			final List<Integer> ids = listIds(target, fromId, size);
			for (Integer id : ids) {
				try {
					this.indexar(target.entityClass, id);
					indexados.add(id);
				} catch (Exception exception) {
					logger.info("#Elastic - Falha ao indexar id {0} do tipo {1} com erro: {2}", id, target.name, exception);
				}
			}
		}
		return indexados;
    }

    private Search buildSearch(TargetKindEnum target, int fromId) {
        final Search search = new Search(target.entityClass);
        try {
			if (target == TargetKindEnum.DocumentosIdentificacao) {
				search.setRetrieveField("idDocumentoIdentificacao");
				search.addCriteria(Criteria.equals("ativo", Boolean.TRUE));
				search.addCriteria(Criteria.equals("usadoFalsamente", Boolean.FALSE));
			}
            search.addCriteria(Criteria.greater(search.getRetrieveField(), fromId));

        } catch (NoSuchFieldException noSuchFieldException) {
            logger.info("#Elastic - Erro ao construir o objeto de pesquisa para o target: {0}.", target.name);
        }

        return search;
    }

    private int getCount(TargetKindEnum target, Integer fromId) {
        Search search = buildSearch(target, fromId);
        return getManager(target.entityClass).count(search).intValue();
    }

    private List<Integer> listIds(TargetKindEnum target, Integer fromId, Integer size) {
        final Search search = buildSearch(target, fromId);
        search.addOrder(search.getRetrieveField(), Order.ASC);
        search.setMax(size);
        return getManager(target.entityClass).list(search);
    }

    @Transactional
    public <T> void indexar(Class<T> clazz, Object id) throws PJeBusinessException, ElasticSearchProvider.TimeOutException {
		if (elasticSearchProvider.isEnabled() && indexer.isIndexable(clazz, indexingMappingHashMap)) {
			T object = getManager(clazz).findById(id);
			if (object != null) {
				Set<Owner> owners = getOwners(clazz, object);
				if (owners.isEmpty()) {
					indexar(object);
				} else {
					for (Owner owner : owners) {
						indexar(owner.getOwnerClass(), owner.getOwnerId());
					}
				}
			}
		}
    }

    private <T> void indexar(Object object) throws ElasticSearchProvider.TimeOutException {
        if (fullfillIndexableConditions(object)) {
			final String type = getIndexableName(object.getClass());
			final JSONObject jsonObject = toIndexableJSON(object);
			final String id = jsonObject.get("id_").toString();

            elasticSearchProvider.indexar(type, id, jsonObject);
        }
    }

    private boolean fullfillIndexableConditions(Object object) {
        if (object == null) {
            return false;
        }
        if (ProcessoTrf.class.isAssignableFrom(object.getClass())) {
            return ((ProcessoTrf) object).getProcessoStatus() == ProcessoStatusEnum.D;
        }
        if (ProcessoEvento.class.isAssignableFrom(object.getClass())) {
            return ((ProcessoEvento) object).getDataAtualizacao() != null;
        }
        return true;
    }

    private <T> BaseManager<T> getManager(Class<T> clazz) {
        if (ProcessoTrf.class.isAssignableFrom(clazz))
            return (BaseManager<T>) Component.getInstance("processoJudicialManager");

        if (Sessao.class.isAssignableFrom(clazz))
            return (BaseManager<T>) Component.getInstance("sessaoJudicialManager");

        String sManagerName = Introspector.decapitalize(clazz.getSimpleName()) + "Manager";
        BaseManager<T> manager = null;
        try {
            manager = (BaseManager<T>) Component.getInstance(sManagerName);
        } catch (Exception ex) {
            logger.error("Não foi possível obter o 'BaseManager' {0} para a classe {1}.", ex, sManagerName, clazz);
        }
        return manager;
    }

    public JSONObject toIndexableJSON(Object o) {
        return indexer.toIndexableJSON(o, indexingMappingHashMap);
    }

    public boolean isIndexable(Class<?> clazz) {
        return indexer.isIndexable(clazz, indexingMappingHashMap);
    }

    public String translate(Class<?> clazz, String path) {
        return indexer.translate(clazz, path, indexingMappingHashMap);
    }

    public String getIndexableName(Class<?> clazz) {
        if (!isIndexable(clazz)) {
            throw new IllegalArgumentException("A classe não é indexável.");
        }
        return indexingMappingHashMap.get(clazz).getName();
    }

    public boolean isIndexable(Class<?> clazz, String field) {
        if (!isIndexable(clazz)) {
            return false;
        } else {
            return indexingMappingHashMap.get(clazz).getPrimitivos().containsKey(field);
        }
    }

    public Set<Owner> getOwners(Class<?> clazz, Object o) {
        Set<Owner> ret = new HashSet<Owner>();
        try {
            indexer.getOwners(clazz, o, indexingMappingHashMap, owners, ret);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            logger.error("Ocorreu um erro ao invocar Indexer.getOwners para o objeto: {0}", e, o);
        }
        return ret;
    }

    public ElasticSearchProvider getSearchProvider() {
        if (elasticSearchProvider.isEnabled()) {
            return elasticSearchProvider;
        }
        return null;
    }

    public enum TargetKindEnum {
        DocumentosIdentificacao("documentoidentificacao", PessoaDocumentoIdentificacao.class, Eventos.REINDEXAR_DOCUMENTOS_IDENTIFICACAO)/*,
		Movimentos("movimentos", ProcessoEvento.class, Eventos.REINDEXAR_MOVIMENTOS),
		VotosColegiados("votoscolegiados", SessaoProcessoDocumentoVoto.class, Eventos.REINDEXAR_VOTOSCOLEGIADO),
		Processos("processos", ProcessoTrf.class, Eventos.REINDEXAR_PROCESSOS),
		Documentos("documentos", ProcessoDocumento.class, Eventos.REINDEXAR_DOCUMENTOS)*/;

        public static final TargetKindEnum[] VALUES = TargetKindEnum.values();

        final String name;
        final Class<? extends Serializable> entityClass;
        final String reindexEvent;
        TargetKindEnum(String name, Class<? extends Serializable> entityClass, String reindexEvent) {
            this.name = name;
            this.entityClass = entityClass;
            this.reindexEvent = reindexEvent;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getParameterNameId(String indexName) {
            return String.format(PARAM_REINDEX_ID, indexName, name);
        }

        public String getParameterNameBase(String indexName) {
            return String.format(PARAM_REINDEX_BASE, indexName, name);
        }
    }
}
