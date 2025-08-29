/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoEventoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componete de gerenciamento da entidade {@link ProcessoEvento}.
 * 
 * @author cristof
 *
 */
@Name(ProcessoEventoManager.NAME)
public class ProcessoEventoManager extends BaseManager<ProcessoEvento> {
	
	public static final String NAME = "processoEventoManager";

	
	private static final String EL_BEGIN = "#{";

	private static final String EL_END = "}";

	private static final String PROPERTIES_FILE = "META-INF/movimentacao.xml";

	@In
	private ProcessoEventoDAO processoEventoDAO;

    /**
     * @return Instância da classe.
     */
    public static ProcessoEventoManager instance() {
        return ComponentUtil.getComponent(NAME);
    }
    
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected ProcessoEventoDAO getDAO() {
		return processoEventoDAO;
	}
	
	@In
	private ParametroService parametroService;

	private Properties properties;

	public ProcessoEventoManager(){
		init();
	}

	public ProcessoEventoManager(Properties properties){
		this.properties = properties;
	}

	public void init(){
		if (properties != null){
			return;
		}
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		properties = new Properties();
		if (is == null){
			return;
		}
		try{
			properties.loadFromXML(is);
		} catch (IOException e){
			e.printStackTrace();
		} finally{
			if (is != null){
				try{
					is.close();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public ProcessoEvento getMovimentacao(ProcessoTrf processo, Evento tipoMovimento){
		ProcessoEvento ret = new ProcessoEvento();
		ret.setProcesso(processo.getProcesso());
		ret.setEvento(tipoMovimento);
		ret.setProcessado(false);
		ret.setVerificadoProcessado(false);
		return ret;
	}
	
	/**
	 * Recupera a lista de movimentações de processos da instalação que não foram
	 * contabilizadas do ponto de vista estatístico.
	 * 
	 * @return lista de movimentações não contabilizadas
	 * @see ProcessoEvento#isProcessado()
	 * @see ProcessoEvento#isVerificadoProcessado()
	 * 
	 * @author Daniel (Infox)
	 */
	public List<ProcessoEvento> recuperaNaoContabilizadas(){
		return processoEventoDAO.recuperaNaoContabilizadas();
	}
	
	/**
	 * Recupera a movimentação mais moderna ocorrida em um dado processo judicial.
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual se pretende obter a movimentação
	 * @return a movimentação processual imediatamente anterior à data atual
	 */
	public ProcessoEvento recuperaUltimaMovimentacao(ProcessoTrf processoJudicial){
		return recuperaUltimaMovimentacao(processoJudicial, new Date());
	}
	
	/**
	 * Recupera a movimentação mais moderna ocorrida em um dado processo judicial antes de uma
	 * determinada data.
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual se pretende obter a movimentação
	 * @param data a data antes da qual se pretende identificar a movimentação mais moderna
	 * @return a movimentação imediatamente anterior à data dada, ou nulo, se ela não existir.
	 */
	public ProcessoEvento recuperaUltimaMovimentacao(ProcessoTrf processoJudicial, Date data){
		return processoEventoDAO.recuperaUltimaMovimentacao(processoJudicial, data);
	}
	
	public ProcessoEvento recuperaUltimaMovimentacaoPublica(Integer idProcesso, Date referencia) throws PJeBusinessException{
		return processoEventoDAO.recuperaUltimaMovimentacaoPublica(idProcesso, referencia);
	}
	
	/**
	 * Recupera o primeiro movimento processual que teve por documento
	 * o indicado.
	 * 
	 * @param documento o documento vinculado ao movimento que se pretende identificar
	 * @return o primeiro movimento processual que teve o documento dado como vinculado,
	 * ou nulo, se não há movimento processual com esse documento
	 */
	public ProcessoEvento findByDocumento(ProcessoDocumento documento){
		return processoEventoDAO.findByDocumento(documento);
	}

	public List<ProcessoEvento> recuperar(ProcessoDocumento documento){
		return processoEventoDAO.recuperar(documento);
	}
	
	/**
	 * Dado um documento e uma lista de códigos de movimentos separados por vírgula, retornará true se todos os movimentos estiverem associados ao documento,
	 * se houver pelo menos 1 movimento não associado retornará false
	 * Caso seja passado o identificador de um documento anexo, a função verificará se o documento principal aos movimentos indicados
	 * 
	 * @param idProcessoDocumento
	 * @param codigosEvento
	 * @return
	 * @throws PJeBusinessException
	 */
	public boolean verificarEventosAssociados(Integer idProcessoDocumento, String codigoEventos) throws PJeBusinessException {
		boolean resultado = Boolean.TRUE;
		
		if (idProcessoDocumento != null && codigoEventos != null && !codigoEventos.trim().isEmpty()) {
			resultado = Boolean.FALSE;
			ProcessoDocumento processoDocumento = ComponentUtil.getComponent(ProcessoDocumentoManager.class).findById(idProcessoDocumento);
			if(processoDocumento != null && processoDocumento.getDocumentoPrincipal() != null) {
				processoDocumento = processoDocumento.getDocumentoPrincipal();
			}
			
			if(processoDocumento != null) {
				List<ProcessoEvento> processoEventos = processoEventoDAO.recuperar(processoDocumento);
				if (processoEventos != null && codigoEventos != null && processoEventos.size() >= codigoEventos.split(",").length) {
					return Arrays.asList(codigoEventos.split(","))
							.stream().allMatch(cod -> 
									(!cod.trim().isEmpty() && 
									  processoEventos
										.stream().anyMatch(p -> p.getEvento().getCodEvento().equals(cod.trim()) )
									)
							);
				}
			}
		}
		
		return resultado;
	}

	/**
	 * Indica se o processo indicado teve lançada uma movimentação cujo código (nacional ou local) é o indicado
	 * ou é derivado do indicado.
	 * 
	 * @param processo o processo judicial a ser pesquisado
	 * @param movimento o tipo de movimentação a ser pesquisada
	 * @return true, se o processo teve um movimento do tipo dado lançado ou se
	 * ele teve algum movimento dele derivado lançado.
	 * @throws PJeBusinessException 
	 * @since 1.4.8
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento) throws PJeBusinessException {
		return temMovimento(processo, movimento, null);
	}
	
	/**
	 * Indica se o processo indicado teve lançada uma movimentação cujo código (nacional ou local) é o indicado
	 * ou é derivado do indicado.
	 * 
	 * @param processo o processo judicial a ser pesquisado
	 * @param movimento o tipo de movimentação a ser pesquisada
	 * @param dataLimite a data a partir da qual será feita a pesquisa, ou null caso não se pretenda limitar
	 * @return true, se o processo teve um movimento do tipo dado lançado ou se
	 * ele teve algum movimento dele derivado lançado.
	 * @throws PJeBusinessException
	 * @since 1.4.8
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento, Date dataLimite) throws PJeBusinessException{
		try{
			return processoEventoDAO.temMovimento(processo, movimento, dataLimite);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e.getCode(), e, e.getParams());
		}
	}
	
	/**
	 * Indica se o processo indicado teve lançada uma movimentação cujo código (nacional ou local) é o indicado
	 * ou é derivado do indicado.
	 * 
	 * @param processo o processo judicial a ser pesquisado
	 * @param movimento o tipo de movimentação a ser pesquisada
	 * @param dataLimite a data a partir da qual será feita a pesquisa, ou null caso não se pretenda limitar
	 * @return true, se o processo teve um movimento do tipo dado lançado ou se
	 * ele teve algum movimento dele derivado lançado.
	 * @throws PJeBusinessException
	 * @since 1.4.8
	 */
	public boolean temMovimento(Integer idProcesso, Evento movimento, Date dataLimite) throws PJeBusinessException{
		try{
			return processoEventoDAO.temMovimento(idProcesso, movimento, dataLimite);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e.getCode(), e, e.getParams());
		}
	}
		
	public boolean temAlgumMovimento(Integer idProcesso, Date dataLimite, Evento... movimento) throws PJeBusinessException{
		try{
			return processoEventoDAO.temAlgumMovimento(idProcesso, dataLimite, movimento);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e.getCode(), e, e.getParams());
		}
	}
			
	/**
	 * Identifica se um dado processo judicial teve, a partir de uma data informada, lançada alguma 
	 * movimentação do tipo e com os complementos dados.
	 * 
	 * @param processo o processo judicial objeto da pesquisa
	 * @param movimento o movimento esperado
	 * @param dataLimite a data limite a partir da qual será feita a pesquisa
	 * @param complementos a lista de complementos que devem constar na movimentação
	 * @return true, se houver pelo menos uma movimentação no período dado
	 * @throws PJeBusinessException 
	 * @since 1.4.8
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento, Date dataLimite, Map<TipoComplemento, String> complementos) throws PJeBusinessException {
		try{
			return processoEventoDAO.temMovimento(processo, movimento, dataLimite, complementos);
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e.getCode(), e, e.getParams());
		}
	}
	
	public ProcessoEvento getMovimentacao(ProcessoTrf processo, Evento evento, ProcessoDocumento doc){
		ProcessoEvento mov = new ProcessoEvento();
		mov.setEvento(evento);
		mov.setProcesso(processo.getProcesso());
		mov.setProcessoDocumento(doc);
		String textoFinal = evento.getMovimento();
		mov.setTextoParametrizado(textoFinal);
		mov.setTextoFinalInterno(textoFinal);
		mov.setTextoFinalExterno(textoFinal);
		mov.setVisibilidadeExterna(evento.getVisibilidadeExterna());
		return mov;
	}

	@Override
	public ProcessoEvento persist(ProcessoEvento pe) throws PJeBusinessException, PJeDAOException{
		if(pe.getDataAtualizacao() == null)
			pe.setDataAtualizacao(new Date());

		Usuario usuarioLogado = Authenticator.getUsuarioLogado();

		if (usuarioLogado != null){
			return processoEventoDAO.persist(pe, usuarioLogado);
		}
		else{
			Integer idUsuarioSistema = Integer.parseInt(parametroService.valueOf(Parametros.ID_USUARIO_SISTEMA));
			return processoEventoDAO.persist(pe, idUsuarioSistema);
		}
	}

	public String processaMovimentacao(ProcessoEvento evento, Object... beans){
		String desBase = evento.getTextoParametrizado();
		StringBuilder ret = new StringBuilder();
		String chave = null;
		int from = 0;
		int idx = 0;
		while ((chave = findExpression(evento.getTextoParametrizado(), from)) != null){
			String prop = (String) properties.get(chave);
			ret.append(desBase.substring(from, desBase.indexOf(EL_BEGIN + chave)));
			from = ret.length() + chave.length() + EL_BEGIN.length() + EL_END.length();
			if (prop != null){
				BeanUtilsBean bub = BeanUtilsBean.getInstance();
				try{
					String value = bub.getProperty(beans[idx], prop);
					ret.append(value.toLowerCase());
				} catch (IllegalAccessException e){
					e.printStackTrace();
				} catch (InvocationTargetException e){
					e.printStackTrace();
				} catch (NoSuchMethodException e){
					e.printStackTrace();
				}
			}
			else{
				ret.append(chave);
			}
			if (beans.length > (idx + 1)){
				idx++;
			}
		}
		ret.append(desBase.substring(from));
		return ret.toString();
	}

	protected String findExpression(String str, int from){
		int begin = str.indexOf(EL_BEGIN, from);
		if (begin != -1){
			int end = str.indexOf(EL_END, begin);
			if (end == -1){
				throw new IllegalArgumentException("Expressão não balanceada [" + str + "].");
			}
			return str.substring(begin + EL_BEGIN.length(), end);
		}
		else{
			return null;
		}
	}
	
	public List<ProcessoEvento> getMovimentosLancadosParaProcesso(Evento evento,SessaoJT sessaoJT,Processo processo){
		return processoEventoDAO.getMovimentosLancadosParaProcesso(evento, sessaoJT,processo);
	}
	
	public AplicacaoMovimento recuperaAplicacao(Evento tipoMovimento, String justica, String aplicacao, String sujeito) throws PJeBusinessException{
		Search s = new Search(AplicacaoMovimento.class);
		addCriteria(s, 
				Criteria.equals("eventoProcessual", tipoMovimento),
				Criteria.equals("aplicabilidade.orgaoJusticaList.nome", justica),
				Criteria.equals("aplicabilidade.aplicacaoClasseList.aplicacaoClasse", aplicacao),
				Criteria.equals("aplicabilidade.sujeitoAtivoList.nome", sujeito));
		List<AplicacaoMovimento> ret = list(s); 
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	/**
	* Retorna a lista de movimentos de um determinado processo, a partir de uma data de referencia.
	*
	* @param processo
	* @param dataReferencia
	* @param incluirSigilosos
	* @return List<ProcessoEvento>
	*/
	public List<ProcessoEvento> recuperaMovimentos(ProcessoTrf processo, Date dataReferencia, boolean incluirSigilosos){
		Search s = new Search(ProcessoEvento.class);
		addCriteria(s, 
				Criteria.equals("processo.idProcesso", processo.getIdProcessoTrf()),
				Criteria.equals("ativo", true));
		if(!incluirSigilosos){
			addCriteria(s, Criteria.equals("visibilidadeExterna", true));
		}
		if(dataReferencia != null){
			addCriteria(s, Criteria.greaterOrEquals("dataAtualizacao", dataReferencia));
		}
		return list(s);
	}

	/**
	 * Recupera a movimentação mais moderna ocorrida em um dado processo judicial cujo código do tipo de movimento
	 * tenha sido um dos especificados
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual se pretende obter a movimentação
	 * @param codigosEvento String contendo lista de códigos de tipo de movimento para os quais se pretende identificar a movimentação mais moderna separados por vírgula
	 * @return a movimentação mais moderna, ou nulo, se ela não existir.
	 */
	public ProcessoEvento recuperaUltimaMovimentacao(ProcessoTrf processoJudicial, String codigosEvento){
		String[] arrayCodigos = codigosEvento.split("\\,");
		return processoEventoDAO.recuperaUltimaMovimentacao(processoJudicial, arrayCodigos);
	}
	
	/**
	 * Identifica se um dado processo possui o movimento indicado, ainda ativo, 
	 * a partir da data informada e vinculado ao documento repassado como parâmetro. 
	 * 
	 * @param processo {@link ProcessoTrf} o processo a ser pesquisado
	 * @param movimento {@link Evento} o tipo de movimentação cujo lançamento se pretende identificar
	 * @param dataLimite {@link Date} a data a partir da qual a pesquisa deve ser feita, ou null, para ignorar esse critério
	 * @param processoDocumento {@link ProcessoDocumento} o documento vinculado à movimentação pesquisada
	 * 
	 * @return true ou false
	 */
	public boolean temMovimento(ProcessoTrf processo, Evento movimento, Date dataLimite, ProcessoDocumento processoDocumento) {
		return processoEventoDAO.temMovimento(processo, movimento, dataLimite, processoDocumento);
	}	
	
	public Boolean existeConclusaoAberta(ProcessoTrf processoTrf) {
		
		if(!processoEventoDAO.existeConclusaoLancada(processoTrf)) {
			return Boolean.FALSE;
		}
		
		return processoEventoDAO.existeConclusaoAberta(processoTrf);
	}	
	
	/**
	* Retorna o evento do expediente passado por parâmetro.
	*
	* @param ppe ProcessoParteExpediente
	* @return ProcessoEvento
	*/
	public ProcessoEvento recuperaMovimento(ProcessoParteExpediente ppe){
		ProcessoEvento movimentacao = null;
		
		if ( ppe.getProcessoExpediente().getProcessoDocumento() != null) {
			ProcessoExpediente expediente = ppe.getProcessoExpediente();
			ProcessoDocumento documento = expediente.getProcessoDocumento();	
			movimentacao = processoEventoDAO.findByDocumento(documento);
		}
		return movimentacao;
	}
}
