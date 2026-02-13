package br.jus.cnj.pje.view;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;

import org.hibernate.Hibernate;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.annotations.Logger;

import org.richfaces.model.FilterField;
import org.richfaces.model.Ordering;
import org.richfaces.model.SortField2;
import org.ajax4jsf.model.SerializableDataModel;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.DataModelListener;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A classe UnifiedEntityDataModel oferece uma solução robusta para a exibição de dados paginados em uma interface
 * de usuário, garantindo eficiência no acesso e manipulação de grandes volumes de dados. Esta implementação é 
 * especialmente projetada para trabalhar em conjunto com componentes JSF, fornecendo funcionalidades avançadas 
 * como paginação, filtragem, ordenação e cache local de dados, otimizando o desempenho e a experiência do usuário.
 * 
 * Esta classe estende {@link DataModel} e implementa as interfaces {@link org.ajax4jsf.model.ExtendedDataModel},
 * {@link SerializableDataModel}, e {@link org.richfaces.model.Modifiable}, fornecendo métodos para manipulação 
 * e exibição de dados em componentes visuais de forma dinâmica e eficiente.
 * 
 * A classe mantém um cache local dos dados já recuperados, evitando consultas desnecessárias ao banco de dados, 
 * o que é especialmente útil em contextos onde a latência de rede ou a carga no servidor pode ser um problema.
 * Ela também suporta a inicialização de associações 'lazy', evitando exceções comuns de inicialização tardia 
 * (como LazyInitializationException) em ambientes de persistência.
 * 
 * Além disso, esta classe oferece integração com visitantes de dados (DataVisitor), permitindo o processamento 
 * eficiente dos dados exibidos ao usuário. Com suporte completo para ordenação e filtros dinâmicos, é uma 
 * solução flexível e poderosa para exibição de listas grandes de entidades em interfaces JSF.
 * 
 * @param <E> O tipo de entidade gerenciada pelo DataModel.
 * 
 * @author Jônatas Pereira da Silva
 */
public class UnifiedEntityDataModel<E> extends SerializableDataModel implements Serializable, org.richfaces.model.Modifiable {
    private static final long serialVersionUID = 1L;

    @Logger
    private Log logger = Logging.getLog(UnifiedEntityDataModel.class);

    private List<E> dataSource;  // Lista completa de dados, antes de qualquer filtro (se estiver em memória)
    private List<E> pageData;  // Dados da página atual
    private Map<Serializable, E> wrappedData = new HashMap<>();  // Dados armazenados em cache
    private List<Serializable> wrappedKeys = new ArrayList<>();  // Chaves dos objetos da página atual
    private Serializable currentId;  // Chave primária atual
    private Class<E> entityClass;  // Classe da entidade
    private DataRetriever<E> dataRetriever;  // Interface para recuperação de dados

    // ========================
    // Atributos de controle de linha e entidade atual
    // ========================
    private int rowIndex = -1;  // Índice da linha atual
    private E current; // Objeto Atual que está sendo processado
    private FacesContext context; // Contexto do JSF

    // ========================
    // Atributos de paginação
    // ========================
    private int firstPage = 0; // Primeira página exibida
    private int maxPagesAllowed = 100; // Número máximo de páginas permitidas
    private int pageSize = 10;  // Tamanho da página (número de registros por página)
    private int currentPage = 0;  // Página atual
    private int totalRowCount;  // Número total de registros
    private boolean refreshPage = true;  // Indica se os dados devem ser atualizados

    // ========================
    // Atributos de ordenação
    // ========================
    private Comparator<E> customComparator; // Comparador customizado para ordenar dados

    // ========================
    // Outros atributos auxiliares
    // ========================
    private EntityManager entityManager; // Gerenciador de entidades do JPA
    private List<DataModelListener> listeners = new ArrayList<>();  // Lista de ouvintes para mudanças nos dados

    // ========================
    // Construtores
    // ========================

    // Construtor que recebe o DataRetriever e a classe da entidade
    public UnifiedEntityDataModel(Class<E> entityClass, DataRetriever<E> dataRetriever) {
        this.entityClass = entityClass;
        this.dataRetriever = dataRetriever;
    }

    // Construtor alternativo
    public UnifiedEntityDataModel(DataRetriever<E> dataRetriever) {
        this.dataRetriever = dataRetriever;
    }

    // Construtor que inclui o contexto do JSF
    public UnifiedEntityDataModel(Class<E> entityClass, FacesContext context, DataRetriever<E> retriever) {
        this.entityClass = entityClass;
        this.context = context;
        this.dataRetriever = retriever;
    }

    // ========================
    // Métodos de paginação
    // ========================

    /**
     * Define os limites de paginação: página inicial e máximo de páginas permitidas.
     */
    public void setPageLimits(int firstPage, int maxPagesAllowed) {
        this.firstPage = firstPage;
        this.maxPagesAllowed = maxPagesAllowed;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public int getMaxPagesAllowed() {
        return maxPagesAllowed;
    }
    
    public DataRetriever<E> getDataRetriever() {
        return this.dataRetriever;
    }

    /**
     * Atualiza os dados da página e recalcula a paginação.
     */
    @Override
    public void update() {
        try {
            // Limpa as chaves e dados em cache, forçando a recarga dos dados
            wrappedKeys.clear();
            wrappedData.clear();

            // Calcula o deslocamento inicial
            int firstRow = (currentPage > 0 ? currentPage - 1 : 0) * pageSize;
 
            // Recupera os novos dados
            pageData = dataRetriever.list(firstRow, pageSize);

            // Atualiza a contagem total de registros
            totalRowCount = dataRetriever.count();

            // Armazena as novas chaves e dados no cache
            for (E rowData : pageData) {
                Serializable rowKey = (Serializable) dataRetriever.getId(rowData);
                wrappedKeys.add(rowKey);
                wrappedData.put(rowKey, rowData);
            }

            // Atualiza o objeto atual para o primeiro item da página atual
            if (!pageData.isEmpty()) {
                current = pageData.get(0); // Atualiza o objeto atual
            } else {
                current = null;
            }

            refreshPage = false;
            
            logger.info("Dados atualizados com sucesso para a página " + currentPage);

        } catch (Exception e) {
            logger.error("Erro ao atualizar os dados: " + e.getMessage(), e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                                "Erro ao atualizar os dados", 
                                                "Detalhes: " + e.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    /**
     * Aplica um filtro à lista e recalcula a paginação e o número total de registros.
     *
     * @param filter A função de filtro a ser aplicada.
     */
    public void applyFilter(Predicate<E> filter) {
        try {
            if (dataSource == null) {
                // Armazena os dados originais antes de aplicar qualquer filtro
            	dataSource = pageData;
            }

            // Aplica o filtro e recalcula a lista
            List<E> filteredData = dataSource.stream()
                .filter(filter)
                .collect(Collectors.toList());

            // Recalcula o número total de registros
            totalRowCount = filteredData.size();

            // Recalcula o número de páginas
            pageData = filteredData.subList(currentPage * pageSize, Math.min((currentPage + 1) * pageSize, filteredData.size()));

            // Aplica ordenação se houver
            if (customComparator != null) {
                applySort(customComparator);
            }

            logger.info("Filtro aplicado. Total de registros filtrados: " + totalRowCount);

        } catch (Exception e) {
            logger.error("Erro ao aplicar o filtro: " + e.getMessage(), e);
        }
    }

    /**
     * Limpa o filtro e restaura os dados originais, juntamente com a ordenação e paginação original.
     */
    public void clearFilter() {
        if (dataSource != null) {
            pageData = dataSource;
            dataSource = null;

            // Recalcula a quantidade total de registros
            totalRowCount = pageData.size();

            // Recalcula a paginação original
            pageData = pageData.subList(currentPage * pageSize, Math.min((currentPage + 1) * pageSize, totalRowCount));

            logger.info("Filtro limpo e dados originais restaurados.");
        }
    }

    // ========================
    // Métodos de ordenação
    // ========================

    /**
     * Aplica a ordenação na lista atual.
     *
     * @param comparator O comparador a ser utilizado para a ordenação. Se null, usa a ordenação padrão.
     */
    public void applySort(Comparator<E> comparator) {
        try {
            if (comparator != null) {
                customComparator = comparator;
                pageData.sort(customComparator);
            } else {
                // Se não houver um comparador personalizado, mantém a ordenação original (sem ordenar)
                customComparator = null;
            }
            logger.info("Ordenação aplicada com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao aplicar a ordenação: " + e.getMessage(), e);
        }
    }

    /**
     * Aplica a ordenação padrão (sem ordenação customizada).
     */
    public void applyDefaultSort() {
        applySort(null);
    }

    // ========================
    // Métodos de dados e navegação
    // ========================

   /**
     * Retorna os dados da linha atual.
     * 
     * @return Dados da linha atual.
     * @throws IllegalStateException Se a linha atual não estiver disponível.
     */
    @Override
    public E getRowData() {
        if (currentId == null) {
            return null;
        }

        // Verifica se o dado já está no cache (wrappedData)
        current = wrappedData.get(currentId);
        if (current != null) {
            // Inicialize associações lazy explicitamente antes de retornar a entidade
            initializeLazyAssociations(current);
            return current;
        }

        // Se o dado não estiver no cache, busca o dado correspondente ao currentId diretamente da fonte de dados (dataRetriever)
        current = (E) dataRetriever.list(currentPage * pageSize, pageSize).stream()
                .filter(data -> currentId.equals(dataRetriever.getId(data)))
                .findFirst()
                .orElse(null);

        // Se o dado for recuperado, inicializa as associações lazy e armazena no cache (wrappedData)
        if (current != null) {
            initializeLazyAssociations(current);
            wrappedData.put(currentId, current);
        }

        return current;
    }

    /**
     * Define a chave da linha atual e atualiza o objeto atual (current).
     * 
     * @param key A chave da linha.
     */
    @Override
    public void setRowKey(Object key) {
        this.currentId = (Serializable) key;
        this.current = wrappedData.get(currentId); // Atualiza o objeto atual
    }

    /**
     * Retorna o objeto atual diretamente.
     */
    public E getCurrent() {
        return current;
    }

    /**
     * Retorna a chave da linha atual.
     * 
     * @return Chave da linha atual.
     */
    public Object getCurrentIdRowKey() {
        return currentId;
    }

    /**
     * Inicializa associações lazy da entidade, evitando LazyInitializationException.
     *
     * @param entity Entidade cujas associações devem ser inicializadas.
     */
    private void initializeLazyAssociations(E entity) {
        try {
            Hibernate.initialize(entity); // Inicializa a entidade principal
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao inicializar associações lazy: Argumento inválido ao inicializar", e);
        } catch (NullPointerException e) {
            logger.error("Erro ao inicializar associações lazy: Tentativa de inicializar uma referência nula", e);
        }
    }

    // ========================
    // Outros métodos utilitários
    // ========================

    /**
     * Verifica se a linha atual está disponível.
     * 
     * @return true se a linha estiver disponível, false caso contrário.
     */
    @Override
    public boolean isRowAvailable() {
        return pageData != null && rowIndex >= 0 && rowIndex < pageData.size();
    }

    /**
     * Retorna o número total de registros.
     * 
     * @return Número total de registros.
     */
    @Override
    public int getRowCount() {
        return totalRowCount;
    }

	@Override
	public Object getRowKey() {
		E rowData = getRowData();
	    return rowData != null ? dataRetriever.getId(rowData) : null;
    }

    /**
     * Retorna a chave de uma linha específica com base nos dados da linha.
     * 
     * @param rowData Dados da linha.
     * @return Chave da linha correspondente.
     */
    @SuppressWarnings("unchecked")
    public Object getRowKey(Object rowData) {
        // Validação do tipo usando entityClass
        if (!entityClass.isInstance(rowData)) {
            throw new IllegalArgumentException("O objeto fornecido não é uma instância de " + entityClass.getName());
        }
        return dataRetriever.getId((E) rowData);
    }

    /**
     * Recupera uma entidade pelo ID usando a classe da entidade.
     *
     * @param id O ID da entidade a ser recuperada.
     * @return A entidade recuperada ou null se não for encontrada.
     */
    public E findById(Serializable id) {
        try {
            return entityManager.find(entityClass, id);
        } catch (Exception e) {
            logger.error("Erro ao buscar entidade " + entityClass.getSimpleName() + " com ID: " + id, e);
            return null;
        }
    }

    /**
     * Cria dinamicamente uma nova instância da entidade usando reflexão.
     *
     * @return A nova instância da entidade.
     */
    public E createNewEntity() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Erro ao criar uma nova instância de " + entityClass.getSimpleName(), e);
            return null;
        }
    }

    /**
     * Retorna o índice da linha atual.
     * 
     * @return Índice da linha atual.
     */
    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Define o índice da linha atual.
     * 
     * @param rowIndex Índice da linha.
     */
    @Override
    public void setRowIndex(int rowIndex) {
        if (rowIndex == -1 || (pageData != null && rowIndex >= 0 && rowIndex < pageData.size())) {
            this.rowIndex = rowIndex;
        } else {
            this.rowIndex = -1;
        }
    }

    /**
     * Percorre os dados da página com o visitante.
     * 
     * @param context O contexto do Faces.
     * @param visitor O visitante que processa os dados.
     * @param range O intervalo de dados.
     * @param argument Argumento adicional.
     * @throws IOException Em caso de erro ao processar.
     */
    @Override
    public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
        int firstRow = ((SequenceRange) range).getFirstRow();
        int maxRows = ((SequenceRange) range).getRows();

        // Verifica se é necessário atualizar os dados
        boolean requiresDataRefresh = pageData == null || pageSize != maxRows || currentPage != firstRow / maxRows || refreshPage;
        int pageIndex = firstRow / maxRows;

        try {
            // Atualiza os dados da página se necessário
            if (requiresDataRefresh) {
                pageSize = maxRows;
                currentPage = pageIndex;

                // Verifica se os dados estão sendo obtidos de uma fonte em memória (dataSource)
                if (dataSource != null && !dataSource.isEmpty()) {
                    // Paginação utilizando o stream diretamente sobre dataSource
                    pageData = dataSource.stream()
                            .skip(firstRow)    // Pula os elementos de acordo com o índice inicial
                            .limit(maxRows)    // Limita o número de elementos ao valor de maxRows
                            .collect(Collectors.toList());
                } else {
                    // Recupera os dados da página através do dataRetriever
                    pageData = dataRetriever.list(currentPage * pageSize, pageSize);
                    totalRowCount = dataRetriever.count();
                }

                refreshPage = false;

                // Limpa o cache para a página atual
                wrappedKeys.clear();
                wrappedData.clear();
            }

            // Garante que os dados da página não sejam nulos
            if (pageData == null) {
                pageData = new ArrayList<>();
            }

            // Processa os dados da página usando o visitante
            for (int i = 0; i < pageData.size(); i++) {
                E rowData = pageData.get(i);
                
                // Em vez de usar hashCode(), usa-se dataRetriever.getId() para uma chave de linha confiável
                Serializable rowKey = (Serializable) dataRetriever.getId(rowData);

                // Armazena as chaves e dados no cache local (wrappedKeys e wrappedData)
                if (!wrappedKeys.contains(rowKey)) {
                    wrappedKeys.add(rowKey);
                }
                wrappedData.put(rowKey, rowData);

                // Define a chave da linha atual e processa a entidade
                setRowKey(rowKey);
                setRowIndex(i);
                visitor.process(context, getRowKey(), argument);
            }
        } catch (IOException e) {
            logger.error("Erro ao tentar recuperar os registros: Falha de I/O", e);
            FacesMessage msg = new FacesMessage(String.format("Erro ao tentar recuperar os registros: %s", e.getLocalizedMessage()));
            context.addMessage(null, msg);
        } catch (RuntimeException e) {
            logger.error("Erro ao tentar recuperar os registros: Erro inesperado de tempo de execução", e);
            FacesMessage msg = new FacesMessage(String.format("Erro ao tentar recuperar os registros: %s", e.getLocalizedMessage()));
            context.addMessage(null, msg);
        }
    }

    @Override
    public Object getWrappedData() {
        return pageData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setWrappedData(Object data) {
        this.pageData = (List<E>) data;
    }

    /**
     * Define se os dados da página devem ser atualizados.
     * 
     * @param refreshPage Se true, os dados serão atualizados.
     */
    public void setRefreshPage(boolean refreshPage) {
        this.refreshPage = refreshPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.refreshPage = true;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        this.refreshPage = true;
    }

    @Override
    public void addDataModelListener(DataModelListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeDataModelListener(DataModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public DataModelListener[] getDataModelListeners() {
        return listeners.toArray(new DataModelListener[0]);
    }

	/**
	 * Método que será chamado quando um sortBy for selecionado no rich:dataTable.
	 * Ele faz a ordenação dos dados baseados no campo selecionado e na direção (ascendente ou descendente).
	 *
	 * @param filterFields Lista de campos de filtro.
	 * @param sortFields   Lista de campos de ordenação.
	 */
    @SuppressWarnings("unchecked")
    @Override
    public void modify(List<FilterField> filterFields, List<SortField2> sortFields) {
        if (sortFields != null && !sortFields.isEmpty()) {
            Comparator<E> comparator = null;

            for (SortField2 sortField : sortFields) {
                String fieldName = sortField.getExpression().getExpressionString();
                boolean descending = sortField.getOrdering() == Ordering.DESCENDING;

                comparator = (e1, e2) -> {

                	try {
                        Object value1 = e1.getClass().getDeclaredField(fieldName).get(e1);
                        Object value2 = e2.getClass().getDeclaredField(fieldName).get(e2);

                        Comparable<Object> comp1 = (Comparable<Object>) value1;
                        Comparable<Object> comp2 = (Comparable<Object>) value2;

                        return descending ? comp2.compareTo(comp1) : comp1.compareTo(comp2);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                dataSource = dataSource.stream()
                        .sorted(comparator)
                        .collect(Collectors.toList());
                setRefreshPage(Boolean.TRUE);
            }
        }
    }
    
    /**
     * Interface que deve ser implementada para fornecer os métodos necessários à recuperação de objetos pagináveis.
     * Esta interface é usada para abstrair a lógica de recuperação de dados e facilitar a implementação de diferentes fontes de dados.
     *
     * @param <E> o tipo de objeto que será manipulado por este modelo de dados.
     */
    public interface DataRetriever<E> {

        /**
         * Recupera o identificador único de um objeto.
         * Esse identificador será utilizado para distinguir o objeto dentro do modelo de dados.
         *
         * @param entity o objeto cujo identificador único será recuperado.
         * @return o identificador único do objeto.
         */
        Object getId(E entity);

        /**
         * Recupera um objeto por meio de seu identificador único.
         * Se o objeto não for encontrado, o método pode retornar {@code null}.
         *
         * @param id o identificador único do objeto que se deseja recuperar.
         * @return o objeto correspondente ao identificador fornecido, ou {@code null} se não existir.
         * @throws Exception se ocorrer algum erro durante a recuperação do objeto.
         */
        E findById(Object id) throws Exception;

        /**
         * Recupera uma lista de objetos que correspondem a uma determinada página de resultados.
         * Este método é usado para implementar a paginação de forma eficiente.
         *
         * @param firstRow o índice da primeira linha a ser recuperada.
         * @param maxRows  o número máximo de linhas (objetos) a serem recuperadas.
         * @return uma lista de objetos que correspondem à página solicitada.
         */
        List<E> list(int firstRow, int maxRows);

        /**
         * Conta o número total de objetos disponíveis para paginação.
         * Este método é utilizado para determinar o tamanho total da coleção de dados paginados.
         *
         * @return o número total de objetos disponíveis.
         */
        int count();

        /**
         * Recupera uma lista de objetos filtrados que correspondem a uma determinada página de resultados.
         * Este método é usado para implementar a paginação de forma eficiente com base em filtros aplicados.
         *
         * @param firstRow o índice da primeira linha a ser recuperada.
         * @param maxRows  o número máximo de linhas (objetos) a serem recuperadas.
         * @return uma lista de objetos que correspondem à página filtrada solicitada.
         */
        List<E> listFiltred(int firstRow, int maxRows);

        /**
         * Conta o número total de objetos disponíveis que correspondem ao filtro aplicado.
         * Este método é usado para determinar o tamanho total da coleção de dados filtrados.
         *
         * @param filter o filtro a ser aplicado à coleção de dados.
         * @return o número total de objetos que correspondem ao filtro.
         */
        int countFiltred(Object filter);

        /**
         * Limpa o cache do contador de objetos, forçando a contagem total a ser recalculada.
         */
        void clearCount();

        /**
         * Retorna o índice da primeira página.
         * 
         * @return o índice da primeira página.
         */
        int firstPage();

        /**
         * Retorna o índice da última página.
         *
         * @return o índice da última página.
         */
        int lastPage();

        /**
         * Retorna o índice da página atual.
         *
         * @return o índice da página atual.
         */
        int currentPage();

        /**
         * Retorna o número máximo de páginas permitidas.
         *
         * @return o número máximo de páginas permitidas.
         */
        int maxPagesAllowed();

        /**
         * Atualiza a página atual e força o recarregamento dos dados, caso necessário.
         *
         * @return {@code true} se a página foi atualizada com sucesso, {@code false} caso contrário.
         */
        boolean refreshPage();

        /**
         * Aplica uma ordenação personalizada à lista de objetos utilizando o comparador fornecido.
         *
         * @param comparator o comparador a ser utilizado para ordenar os objetos.
         * @return uma lista de objetos ordenada de acordo com o comparador.
         */
        List<E> applySort(Comparator<E> comparator);

        /**
         * Aplica um filtro à lista e recalcula a paginação e o número total de registros.
         *
         * @param filter A função de filtro a ser aplicada.
         */
        void applyFilter(Predicate<E> filter);

        /**
         * Limpa os filtros aplicados aos dados, restaurando o estado original.
         */
        void clearFilter();

        /**
         * Retorna o objeto atual do modelo de dados.
         * 
         * @return o objeto atualmente selecionado.
         */
        E getCurrent();

        /**
         * Atualiza os dados da página e recalcula a paginação, forçando o recarregamento da página.
         */
        void update();
    }
}
