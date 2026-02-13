package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interceptor.IgnoreFacesTransactionMessageEvent;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.FiltroDinamicoManager;
import br.jus.pje.nucleo.entidades.FiltroDinamicoConsulta;
import br.jus.pje.nucleo.entidades.FiltroDinamicoEntidade;
import br.jus.pje.nucleo.entidades.FiltroDinamicoParametro;
import br.jus.pje.nucleo.enums.TipoParametroFiltroDinamico;

/**
 * Classe destinada a controlar o componente pje:filtroDinamico
 * @author Éverton Nogueira Pereira
 *
 */
@Name(FiltroDinamicoAction.NAME)
@Scope(ScopeType.PAGE)
public class FiltroDinamicoAction {

	public static final String NAME = "filtroDinamicoAction";
	
	private FiltroDinamicoConsulta consultaSelecionada;
	
	private FiltroDinamicoConsulta cadastroConsulta;

	private List<FiltroDinamicoParametro> listaParametros;
	
	private List<FiltroDinamicoConsulta> consultas;
	
	private Map<String, Object> mapa;
	
	@In(create=true)
	private FiltroDinamicoManager filtroDinamicoManager;
	
	@In
	protected FacesMessages facesMessages;
	
	private FiltroDinamicoEntidade cadastroEntidade;
	
	/**
	 * @Descrição: Pega a consulta digitada e busca os parâmetros existentes nela.
	 * @Retorno: void
	 */
	public void verificarConsulta(){
		cadastroConsulta.setParametros(filtroDinamicoManager.obtemParametrosDaConsulta(cadastroConsulta));
	}
	
	/**
	 * @Descrição: Retorna uma lista de tipos de parametros (entidade, inteiro, string....) 
	 * @Retorno: TipoParametroFiltroDinamico[]
	 */
	public TipoParametroFiltroDinamico[] tiposParametro(){
		return TipoParametroFiltroDinamico.values();
	}
	
	/**
	 * @Descrição: busca todas as entidades cadastradas
	 * @Retorno: List<FiltroDinamicoEntidade>
	 */
	public List<FiltroDinamicoEntidade> todasEntidades(){
		return filtroDinamicoManager.obtemTodasEntidades();
	}
	
	/**
	 * @Descrição: Retorna uma lista da entidade do parâmetro informado.
	 * @Parametros: Parametro da consulta
	 * @Retorno: List<?>
	 */
	public List<?> listItems(String parametro){
		List<?> result = null;
		String q = getQuery(parametro);
		if (q != null){
			Query query = EntityUtil.createQuery(q);
			result = query.getResultList();
		}else{
			result = Collections.emptyList();;
		}
		return result;
	}
	
	/**
	 * @Descrição: Monta um HQL de uma consulta na entidade do parâmetro informado.
	 * @Parametros: Parametro da consulta
	 * @Retorno: String
	 */
	private String getQuery(String parametro) {
		FiltroDinamicoParametro p = getFiltro(parametro);
		if(p != null){
			StringBuilder query = new StringBuilder();
			query.append(" SELECT p ");
			query.append(" FROM "+p.getEntidade().getEntidade()+" p ");
			query.append(" ORDER BY p."+p.getEntidade().getAtributoOrdenacao());
			return query.toString();
		}
		return null;
	}
	
	/**
	 * @Descrição: Executa uma consulta e adiciona o resultado na lista recebida por parâmetro.
	 * @Parametros: lista - Lista vazia pronta para receber o resultado da consulta
	 * 				listaPreenchida - Lista preenchida com um valor pré-filtrado. 
	 * Este parametro tem como intuito evitar que no retorno da consulta venham dados que não 
	 * podem ser vistos, logo só será retornado dados que existam nessa lista.
	 * @Retorno: void
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" }) 
	public void executarConsulta(List lista, List listaPreenchida){
		if(listaPreenchida != null && !listaPreenchida.isEmpty()){
			lista.clear();
			List resultado = executaSQL(montaSQL());
			lista.addAll(obtemValoresContidosListaPreenchida(resultado, listaPreenchida));
		}else{
			lista = new ArrayList();
		}
		if(lista.isEmpty()){
			facesMessages.add(Severity.WARN, "A consulta não obteve nenhum resultado!");
		}
	}
	
	/**
	 * @Descrição: Filtrar o resultado para retornar apenas o que existe na lista Preenchida.
	 * @Parametros: resultado - Resultado do HQL cadastrado
	 * 				listaPreenchida - Lista preenchida com um valor pré-filtrado. 
	 * @Retorno: List
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List obtemValoresContidosListaPreenchida(List resultado, List listaPreenchida) {
		List retorno = new ArrayList();
		for (Object object : resultado) {
			if(listaPreenchida.contains(object)){
				retorno.add(object);
			}
		}
		return retorno;
	}

	@SuppressWarnings("rawtypes") 
	public void limparCampos(List lista){
		setCadastroConsulta(null);
		setCadastroEntidade(null);
		setConsultaSelecionada(null);
		lista.clear();
		mapa = null;
	}
	
	public void cadastrarConsulta(){
		filtroDinamicoManager.cadastraConsulta(cadastroConsulta);
		facesMessages.add(Severity.INFO, "Consulta cadastrada com sucesso!");
		limparCampos(null);
	}
	
	public void cadastrarEntidade(){
		try{
			filtroDinamicoManager.cadastraEntidade(cadastroEntidade);
			facesMessages.add(Severity.INFO, "Entidade cadastrada com sucesso!");
		}catch(PJeDAOException e){
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
			facesMessages.add(Severity.ERROR, "A entidade informada já está cadastrada!");
		}finally {
			setCadastroEntidade(null);
		}
	}
	
	/**
	 * @Descrição: Monta a consulta a ser executada, setando os parâmetros dos devidos tipos.
	 * 			   Quando um valor é numérico ele é salvo como BigDecimal, precisando ser feito um cast para o tipo numérico esperado
	 * 			   Atualmente só está sendo tratado tipo Inteiro, caso seja utilizado outros valores como Long, Float, Double, deve ser 
	 * 			   adicionado o devido tratamento como feito abaixo e adicionado o tipo no enum {@link TipoParametroFiltroDinamico} 
	 * @Retorno: Query
	 */
	private Query montaSQL() {
		String hql = getConsultaSelecionada().getHql();
		hql = hql.replace("$", ":$");
		Query query = EntityUtil.createQuery(hql);
		List<FiltroDinamicoParametro> parametrosDaConsulta = obtemParametros();
		for (FiltroDinamicoParametro parametro : parametrosDaConsulta) {
			Object param = null;
			if(parametro.getTipoParametro().equals(TipoParametroFiltroDinamico.INT)){
				Number number = (Number) getMapa().get(parametro.getParametro());
				param = number.intValue();
			}else{
				param = getMapa().get(parametro.getParametro());
			}
			query.setParameter(parametro.getParametro(), param);
		}
		return query;
	}
	
	/**
	 * @Descrição: Executa uma Query
	 * @Parametros: Query
	 * @Retorno: List<?>
	 */
	private List<?> executaSQL(Query query) {
		return query.getResultList();
	}

	/**
	 * @Descrição: Busca uma entidade PARAMETRO dentro da lista de parametros existentes em uma consulta, 
	 * 			   de acordo com um parametro (string) informado.
	 * @Parametros: Parametro
	 * @Retorno: FiltroDinamicoParametro
	 */
	private FiltroDinamicoParametro getFiltro(String parametro){
		if(parametro != null && getConsultaSelecionada() != null){
			return filtroDinamicoManager.obtemEntidadeByParametro(parametro, getConsultaSelecionada().getParametros());
		}
		return null;
	}
	
	/**
	 * @Descrição: Retorna as consultas de acordo com a funcionalidade
	 * @Parametros: Funcionalidade
	 * @Retorno: List<FiltroDinamicoConsulta>
	 */
	public List<FiltroDinamicoConsulta> obtemConsultasByFuncionalidade(String funcionalidade) {
		return filtroDinamicoManager.obtemConsultasByFuncionalidade(funcionalidade);
	}
	
	/**
	 * @Descrição: Busca os parametros da consulta selecionada
	 * @Retorno: List<FiltroDinamicoParametro>
	 */
	public List<FiltroDinamicoParametro> obtemParametros(){
		if(listaParametros == null && getConsultaSelecionada() != null){
			listaParametros = getConsultaSelecionada().getParametros();
		}
		return listaParametros;
	}
	
	public List<FiltroDinamicoParametro> getListaParametros() {
		return listaParametros;
	}

	public void setListaParametros(List<FiltroDinamicoParametro> listaParametros) {
		this.listaParametros = listaParametros;
	}
	
	public FiltroDinamicoConsulta getConsultaSelecionada() {
		return consultaSelecionada;
	}

	public void setConsultaSelecionada(FiltroDinamicoConsulta consultaSelecionada) {
		this.listaParametros = null;
		this.consultaSelecionada = consultaSelecionada;
	}

	public Map<String, Object> getMapa() {
		if(mapa == null){
			mapa = new HashMap<String, Object>();
		}
		return mapa;
	}

	public void setMapa(Map<String, Object> valor) {
		this.mapa = valor;
	}

	public FiltroDinamicoConsulta getCadastroConsulta() {
		if(cadastroConsulta == null){
			cadastroConsulta = new FiltroDinamicoConsulta();
		}
		return cadastroConsulta;
	}

	public void setCadastroConsulta(FiltroDinamicoConsulta cadastroConsulta) {
		this.cadastroConsulta = cadastroConsulta;
	}
	
	public FiltroDinamicoEntidade getCadastroEntidade() {
		if(cadastroEntidade == null){
			cadastroEntidade = new FiltroDinamicoEntidade();
		}
		return cadastroEntidade;
	}

	public void setCadastroEntidade(FiltroDinamicoEntidade cadastroEntidade) {
		this.cadastroEntidade = cadastroEntidade;
	}

	public List<FiltroDinamicoConsulta> getConsultas() {
		if(consultas == null){
			consultas = filtroDinamicoManager.obtemTodasConsultas();
		}
		return consultas;
	}

	public void setConsultas(List<FiltroDinamicoConsulta> consultas) {
		this.consultas = consultas;
	}
}