package br.jus.cnj.pje.nucleo.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente abstrato de gerenciamento negocial de objetos.
 * 
 * @author Daniel Miranda
 *
 * @param <T> o tipo de objeto tratado
 */
@Scope(ScopeType.EVENT)
@Transactional
public abstract class BaseManager<T> implements Manager<T>{
	
	@Logger
	protected Log logger;

	/**
	 * Recupera o componente de recuperação de dados pertinentes a este gerente.
	 * 
	 * @return o componente de recuperação de dados.
	 */
	protected abstract BaseDAO<T> getDAO();

	/**
	 * Recupera o identificador unívoco do objeto dado.
	 * 
	 * @param t o objeto cujo identificador se pretende recuperar
	 * @return o identificador
	 */
	public Object getId(T t){
		return getDAO().getId(t);
	}

	/**
	 * Envia as modificações nos objetos gerenciados ao serviço de dados, que serão tornadas 
	 * definitivas caso a transação seja completada com sucesso.
	 * 
	 * @throws PJeBusinessException caso haja algum erro durante a gravação
	 */
	public void flush() throws PJeBusinessException{
		getDAO().flush();
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.Manager#persist(java.lang.Object)
	 */
	@Override
	public T persist(T entity) throws PJeBusinessException{
		return getDAO().persist(entity);
	};
	
	/**
	 * Método que, além de persistir o objeto, sincroniza todos os objetos 'sujos' do {@link EntityManager} com o banco de dados.
	 * 
	 * @param t
	 */
	public void persistAndFlush(T t) throws PJeBusinessException{
		persist(t);
		this.flush();
	}

	/**
	 * Assegura que o objeto dado deve passar a ser controlado pelo serviço de acesso a dados, fazendo as atribuições pertinentes ao estado atual.
	 * 
	 * @param entity a entidade que deve passar a ser controlada
	 * @return o objeto controlado
	 * @throws PJeBusinessException caso tenha havido algum erro durante a execução
	 */
	public T merge(T entity) throws PJeBusinessException{
		return getDAO().merge(entity);
	}
	
	public T mergeAndFlush(T entity){
		T r = getDAO().merge(entity);
		getDAO().flush();
		return r;
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.Manager#findAll()
	 */
	@Override
	public List<T> findAll() throws PJeBusinessException{
		return this.getDAO().findAll();
	}

	/**
	 * Recupera a lista de objetos dados, limitando a resposta quanto ao primeiro registro recuperado e ao número máximo
	 * de objetos recuperados, também limitando a consulta a um critério simples de pesquisa.
	 * 
	 * @param firstRow o primeiro registro a ser recuperado, ou null para que seja o primeiro disponível
	 * @param maxRows o número máximo de registros a recuperar, ou null, para que todos o sejam.
	 * @param ownero objeto vinculado à entidade E como propriedade propertyName, ou null, para dispensar a restrição
	 * @param propertyName o nome da propriedade owner na entidade E, de modo a permitir fazer uma restrição de consulta 
	 * tal que os objetos retornados sejam todos os E em que e.propertyName = owner, ou null, para dispensar a restrição
	 * @return a lista de objetos que satisfazem o critério
	 * @throws PJeBusinessException caso haja algum erro na recuperação
	 * @since 1.4.6.2.RC4
	 */
	public <O>List<T> findByRange(Integer firstRow, Integer maxRows, O owner, String propertyName) throws PJeBusinessException{
		return getDAO().findByRange(firstRow, maxRows, owner, propertyName);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.Manager#findById(java.lang.Object)
	 */
	@Override
	public T findById(Object id) throws PJeBusinessException{
		return this.getDAO().find(id);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.Manager#refresh(java.lang.Object)
	 */
	@Override
	public T refresh(T entity) throws PJeBusinessException{
		return this.getDAO().refresh(entity);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.Manager#remove(java.lang.Object)
	 */
	@Override
	public void remove(T entity) throws PJeBusinessException{
		this.getDAO().remove(entity);
	}

	/**
	 * Inativa um dado objeto utilizando o método setAtivo, se existente.
	 * 
	 * @param entity a entidade que deve ser inativada
	 * @throws PJeBusinessException caso haja algum erro durante a inativação.
	 */
	@SuppressWarnings("unchecked")
	public void inactivate(T entity) throws PJeBusinessException{

		Class<T> clazz = (Class<T>) entity.getClass();
		Method setInativo = null;
		try{
			setInativo = clazz.getMethod("setAtivo", Boolean.class);
		} catch (SecurityException e){
			throw new PJeBusinessException(
					"SecurityException thrown when trying to access method [getAtivo] on class ["
						+ clazz.getCanonicalName() + "].", e);
		} catch (NoSuchMethodException e){
			throw new PJeBusinessException(
					"A entidade não comporta a remoção meramente lógica por meio de alteração do atributo [ativo]!");
		}
		try{
			setInativo.invoke(entity, false);
		} catch (IllegalArgumentException e){
			throw new PJeBusinessException("Não foi possível inativar a entidade.", e);
		} catch (IllegalAccessException e){
			throw new PJeBusinessException("Não foi possível inativar a entidade.", e);
		} catch (InvocationTargetException e){
			throw new PJeBusinessException("Não foi possível inativar a entidade.", e);
		}
	}

	/**
	 * Recupera a lista de entidades que atendam aos critérios dados pelo objeto de consulta {@link Search}.
	 *  
	 * @param search o objeto de consulta
	 * @return a lista de objetos consultados
	 * @throws PJeBusinessException caso tenha havido algum erro ao realizar a consulta.
	 * @since 1.4.6.2.RC4
	 */
	public <E> List<E> list(Search search) {
		return getDAO().list(search);
	}

	/**
	 * Recupera o número total de registros que atendem aos critérios do objeto de consulta.
	 * 
	 * @param search o objeto de consulta
	 * @return o número total de objetos que atendem aos critérios
	 * @throws PJeBusinessException caso haja algum erro durante a execução
	 */
	public Long count(Search search) {
		return getDAO().count(search);
	};
	
	/**
	 * Acrescenta a um objeto de consulta o conjunto de critérios dados.
	 * 
	 * @param search o objeto de consulta ao qual serão acrescentados critérios
	 * @param criterias os critérios que devem ser acrescentados
	 * @throws IllegalArgumentException caso algum dos critérios contenha campos inexistentes
	 */
	protected void addCriteria(Search search, Criteria... criterias){
		try {
			search.addCriteria(Arrays.asList(criterias));
		} catch (NoSuchFieldException e) {
			String msg = String.format("Erro ao montar os critérios básicos de pesquisa: %s.", e.getLocalizedMessage());
			logger.error(msg);
			throw new IllegalArgumentException(msg, e);
		}
	}
	
	/**
	 * Retorna o usuário logado no sistema.
	 * 
	 * @return Usuário logado.
	 */
	protected Usuario getUsuarioLogado() {
		return Authenticator.getUsuarioLogado();
	}
	
	/**
	 * Retorna true se o usuário logado tem a permissão passada por parâmetro.
	 * 
	 * @param role Permissão verificada.
	 * @return Booleano.
	 */
	protected Boolean usuarioHasRole(String role) {
		Identity identity = Identity.instance();
		return identity.hasRole(role);
	}
	
	/**
	 * Retorna o número do processo com máscara.
	 * 
	 * @param numeroProcesso Número do processo.
	 * @return String do número do processo.
	 */
	protected String mascaraNumeroProcesso(String numeroProcesso) {
		return NumeroProcessoUtil.mascaraNumeroProcesso(numeroProcesso);
	}
	
	/**
	 * Valida o search passado por parâmetro, a validação consiste na verificação 
	 * se o class do search é é mesmo passado por parâmetro.
	 * 
	 * @param search Search validado
	 * @param classe Tipo esperado para o search.
	 * @throws PJeBusinessException
	 */
	protected void validarSearch(Search search, Class<?> classe) throws PJeBusinessException {
		if (search != null && classe != null && search.getEntityClass() != classe) {
			throw new PJeBusinessException("Tipo de dado do search indevido: {0}", 
					new IllegalArgumentException(), 
					search.getEntityClass());
		}
	}
}