/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoDAO;
import br.jus.cnj.pje.business.dao.ProcessoExpedienteDAO;
import br.jus.cnj.pje.business.dao.ProcessoExpedienteDAO.CriterioPesquisa;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoDiarioEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(ProcessoExpedienteManager.NAME)
public class ProcessoExpedienteManager extends BaseManager<ProcessoExpediente>{

	public static final String NAME = "processoExpedienteManager";

	@In
	private ProcessoParteExpedienteDAO processoParteExpedienteDAO;

	@In
	private ProcessoExpedienteDAO processoExpedienteDAO;

	@In
	private ProcessoDocumentoExpedienteManager processoDocumentoExpedienteManager;

	@In(create = true)
	private ProcessoJudicialManager processoJudicialManager;
	
	@In(create = true)
	private ProcessoDocumentoDAO processoDocumentoDAO;
	
	/**
	 * @return Instância da classe.
	 */
	public static ProcessoExpedienteManager instance() {
		return ComponentUtil.getComponent(ProcessoExpedienteManager.class);
	}
	
	@Override
	public ProcessoExpedienteDAO getDAO(){
		return processoExpedienteDAO;
	}

	public ProcessoExpediente find(Integer id){
		return this.processoExpedienteDAO.find(id);
	}
	
	public List<ProcessoExpediente> findByIds(List<Integer> ids){
		return getDAO().findByIds(ids);
	}

	public ProcessoExpediente getExpediente(){
		ProcessoExpediente pe = new ProcessoExpediente();
		pe.setDtCriacao(new Date());
		pe.setCheckado(false);
		pe.setUrgencia(false);
		return pe;
	}

	public ProcessoExpediente getExpediente(ProcessoTrf processo){
		ProcessoExpediente pe = this.getExpediente();
		pe.setProcessoTrf(processo);
		return pe;
	}

	public List<ProcessoExpediente> getAtosComunicacao(Pessoa[] destinatarios, Integer firstRow, Integer maxRows,
			CriterioPesquisa criterio) throws PJeDAOException{
		return processoExpedienteDAO.getAtosComunicacao(destinatarios, firstRow, maxRows, criterio);
	}

	public boolean addDocumentoReferido(ProcessoExpediente pe, ProcessoDocumento pd){
		for (ProcessoDocumentoExpediente pde : pe.getProcessoDocumentoExpedienteList()){
			if (pde.getProcessoDocumento() == pd){
				return false;
			}
		}
		ProcessoDocumentoExpediente pde = processoDocumentoExpedienteManager.getDocumentoReferido();
		pde.setProcessoDocumento(pd);
		pde.setProcessoExpediente(pe);
		pe.getProcessoDocumentoExpedienteList().add(pde);
		return true;
	}

	public ProcessoParteExpediente getExpedientePessoal(ProcessoExpediente pe, Pessoa p){
		List<ProcessoParteExpediente> atosPessoais = pe.getProcessoParteExpedienteList();
		for (ProcessoParteExpediente ppe : atosPessoais){
			if (ppe.getPessoaParte() != null && p != null
				&& ppe.getPessoaParte().getIdUsuario().intValue() == p.getIdUsuario().intValue()){
				return ppe;
			}
		}
		return null;
	}

	public ProcessoParteExpediente getExpedientePessoal(ProcessoExpediente pe, ProcessoParte pp){
		return getExpedientePessoal(pe, pp.getPessoa());
	}

	public int contagemAtos(Pessoa[] destinatarios, CriterioPesquisa criterio){
		return processoExpedienteDAO.contagemAtos(destinatarios, criterio);
	}

	public Long countProcessoExpedienteAtivo(ProcessoTrf processoTrf){
		return processoExpedienteDAO.countProcessoExpedienteAtivo(processoTrf);
	}

	public List<ProcessoParteExpediente> processoParteExpedienteComDocumentoList(ProcessoTrf processoTrf){
		return processoParteExpedienteDAO.processoParteExpedienteComDocumentoList(processoTrf);
	}

	public ProcessoDocumento getProcessoDocumentoAto(ProcessoExpediente processoExpediente){
		for (ProcessoDocumentoExpediente processoDocumentoExpediente : processoExpediente
				.getProcessoDocumentoExpedienteList()){
			if (processoDocumentoExpediente.getAnexo() != null && !processoDocumentoExpediente.getAnexo()){
				return processoDocumentoExpediente.getProcessoDocumentoAto();
			}
		}
		return null;
	}

	public void fecharExpedientes(ProcessoExpediente processoExpediente){
		for (ProcessoParteExpediente processoParteExpediente : processoExpediente.getProcessoParteExpedienteList()){
			processoParteExpediente.setFechado(true);
			processoParteExpediente.setPendenteManifestacao(false);
		}

		Events.instance().raiseEvent(Eventos.EVENTO_EXPEDIENTE_FECHADO, processoExpediente.getProcessoTrf());
	}

	public boolean contemExpedientesNaoVencidos(ProcessoExpediente processoExpediente){
		Date now = new Date();
		List<ProcessoParteExpediente> processoParteExpedienteList = processoExpediente.getProcessoParteExpedienteList();
		for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList){
			if (processoParteExpediente.getDtPrazoLegal() != null
				&& now.after(processoParteExpediente.getDtPrazoLegal())){
				return true;
			}
		}
		return false;
	}

	public boolean todosExpedientesFechados(ProcessoExpediente processoExpediente) {
		List<ProcessoParteExpediente> processoParteExpedienteList = processoExpediente.getProcessoParteExpedienteList();
		boolean resultado = true;
		for (ProcessoParteExpediente processoParteExpediente : processoParteExpedienteList) {
			if(!processoParteExpediente.getFechado()){
				resultado = false;
				break;
			}
		}
		return resultado;
	}
	
	public boolean isTodosPrazosVencidos(ProcessoExpediente processoExpediente){
		return !contemExpedientesNaoVencidos(processoExpediente);
	}

	public List<ProcessoExpediente> listNaoEnviados(ProcessoTrf proc){
		return processoExpedienteDAO.listNaoEnviados(proc);
	}
	
	public ProcessoExpediente getUltimoProcessoExpedienteApos(ProcessoTrf processo, Date data) {
		return processoExpedienteDAO.getUltimoProcessoExpedienteApos(processo, data);
	}
	
	public boolean existeExpedienteAposUltimaSentenca(Integer idProcesso) {
		ProcessoTrf processo = null;
		boolean retorno = true;
		Date dataAssinatura= null;
		
		try {
			processo = processoJudicialManager.findById(idProcesso);
			ProcessoDocumento ultimaSentenca = processoDocumentoDAO.getUltimaSentenca(processo);
			dataAssinatura = ultimaSentenca.getProcessoDocumentoBin().getDataAssinatura();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			retorno = false;
		}catch(Exception e){
			e.printStackTrace();
			retorno = false;
		}
		if(retorno){
			retorno =( getUltimoProcessoExpedienteApos(processo, dataAssinatura) != null);
		}
		return retorno;
	}
	
	/**
	 * Recupera a lista de identificadores de atos de comunicação que atendam aos seguintes critérios:
	 * <li>o meio de expedição é a publicação em diário ({@link ExpedicaoExpedienteEnum#P});</li>
	 * <li>não têm data de ciência ou resposta; e</li>
	 * <li> não consta como fechado.</li>
	 * 
	 * @return a lista de identificadores
	 * @throws PJeBusinessException
	 * 
	 * @see ProcessoExpediente#getMeioExpedicaoExpediente()
	 * @see ProcessoParteExpediente#getFechado()
	 * @see ProcessoParteExpediente#getDtCienciaParte()
	 * @see ProcessoParteExpediente#getResposta()
	 */
	public List<ProcessoExpediente> recuperarExpedientePendentes(boolean restringirComReciboDJE) throws PJeBusinessException {
		return recuperarExpedientesPendentes(restringirComReciboDJE, Collections.<ProcessoTrf> emptyList(), Arrays.asList(ExpedicaoExpedienteEnum.P));
	}
	
	/**
	 * Recupera a lista de identificadores de atos de comunicação que atendam aos seguintes critérios:
	 * <li>processos indicados na lista como argumento ({@link ProcessoTrf})
	 * <li>meio de expedição é igual ao(s) meio(s) indicados como argumento ({@link ExpedicaoExpedienteEnum})</li>
	 * <li>não têm data de ciência ou resposta; e</li>
	 * <li>não consta como fechado.</li> 
	 * 
	 * @param meios - meios de comunicação a serem filtrados
	 * @return Lista dos identificadores dos atos de comunicação
	 * @throws PJeBusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> recuperarIdentificadorExpedientePendentes(boolean restringirComReciboDJE, List<ProcessoTrf> processos, List<ExpedicaoExpedienteEnum> meios) throws PJeBusinessException {
		return (List<Integer>) CollectionUtils.collect(recuperarExpedientesPendentes(restringirComReciboDJE, processos, meios), new BeanToPropertyValueTransformer("idProcessoExpediente"));
	}

	@SuppressWarnings("unchecked")
	public List<Integer> recuperarIdentificadorExpedientePendentes(boolean restringirComReciboDJE) throws PJeBusinessException {
		return (List<Integer>) CollectionUtils.collect(recuperarExpedientesPendentes(restringirComReciboDJE, Collections.<ProcessoTrf> emptyList(), Arrays.asList(ExpedicaoExpedienteEnum.P)), new BeanToPropertyValueTransformer("idProcessoExpediente"));
	}
	
	/**
	 * Recupera os atos de comunicação que atendam aos seguintes critérios:
	 * <li>Apenas expedientes com reciboPublicacaoDJE - se requerido
	 * <li>o processo é um dos indicados como argumento ({@link ProcessoTrf})
	 * <li>o meio de expedição é igual ao(s) meio(s) indicados como argumento ({@link ExpedicaoExpedienteEnum})</li>
	 * <li>não têm data de ciência ou resposta; e</li>
	 * <li>não consta como fechado.</li>  
	 * @param restringirComReciboDJE
	 * @param processos
	 * @param meios
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<ProcessoExpediente> recuperarExpedientesPendentes(boolean restringirComReciboDJE, List<ProcessoTrf> processos, List<ExpedicaoExpedienteEnum> meios) throws PJeBusinessException {
		Search search = new Search(ProcessoExpediente.class);
		search.setDistinct(true);
		addCriteria(search, 
				Criteria.equals("processoParteExpedienteList.fechado", false),
				Criteria.in("meioExpedicaoExpediente", meios.toArray()),
				Criteria.in("processoTrf.idProcessoTrf", CollectionUtils.collect(processos, new BeanToPropertyValueTransformer("idProcessoTrf")).toArray()),
				Criteria.isNull("processoParteExpedienteList.dtCienciaParte"),
				Criteria.isNull("processoParteExpedienteList.resposta")
				);
		if(meios.contains(ExpedicaoExpedienteEnum.P)) {
			addCriteria(search,
					Criteria.or(
							Criteria.not(Criteria.equals("meioExpedicaoExpediente", ExpedicaoExpedienteEnum.P)),
							Criteria.and(
										Criteria.equals("processoParteExpedienteList.publicacaoDiarioEletronicoList.situacao", SituacaoPublicacaoDiarioEnum.A),
										Criteria.less("processoParteExpedienteList.publicacaoDiarioEletronicoList.dtExpectativaPublicacao", DateUtil.getEndOfToday())
									)
							)
					);
		}
		if(restringirComReciboDJE) {
			addCriteria(search,
					Criteria.not(Criteria.isNull("processoParteExpedienteList.publicacaoDiarioEletronicoList.reciboPublicacaoDiarioEletronico"))
					);
		}
		return list(search);
	}	

	public List<ProcessoExpediente> recuperarExpedientesPendentes(List<ProcessoTrf> processos, List<ExpedicaoExpedienteEnum> meios) throws PJeBusinessException {
		return this.recuperarExpedientesPendentes(false, processos, meios);
	}
	/**
	 * Método responsável por recuperar os recibos de publicação no DJE para o expediente especificado
	 * @param processoExpediente
	 * @return
	 */
	public String recuperarRecibosPublicacaoDJE(List<ProcessoExpediente> expedientes) {
		StringBuilder sb = new StringBuilder("");

		PublicacaoDiarioEletronicoManager publicacaoDiarioEletronicoManager = ComponentUtil.getComponent(PublicacaoDiarioEletronicoManager.class);
		for (ProcessoExpediente processoExpediente : expedientes) {
			if (processoExpediente.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.P) {
				for (ProcessoParteExpediente ppe : processoExpediente.getProcessoParteExpedienteList()) {
					PublicacaoDiarioEletronico publicacaoDJE = publicacaoDiarioEletronicoManager.getPublicacao(ppe);
					sb.append("Destinatário: " + ppe.getNomePessoaParte());
					
					if (publicacaoDJE != null && publicacaoDJE.getReciboPublicacaoDiarioEletronico() != null) {
						sb.append(" - Recibo gerado pelo DJE: " + publicacaoDJE.getReciboPublicacaoDiarioEletronico());
					}
					else
					{
						sb.append(" - Não foi possível enviar a publicação para o DJE (ocorreu alguma falha no sistema)");
					}
					
					sb.append("\n");
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Metodo que verifica se tem expedientes para ProcessoDocumento ou ProcessoDocumentoVinculado
	 * 
	 * @param idProcessoDocumento ProcessoDocumento
	 * @param idProcessoDocumentoVinculado ProcessoDocumentoVinculado
	 * @return True se tiver expediente para os documentos
	 */
	public boolean verificarExistenciaExpedientePublicacao(Integer idProcessoDocumento, ExpedicaoExpedienteEnum...meios){
		return processoExpedienteDAO.verificarExistenciaExpedientePublicacao(idProcessoDocumento, meios);
	}

	/**
	 * Obtem os processoDocumentos com meio Telegrama criados em uma determinada data.
	 * @param data a data a ser pesquisada.
	 */
	public List<ProcessoExpediente> getAtosComunicacaoTelegrama(Date data) throws PJeDAOException {
		return processoExpedienteDAO.getAtosComunicacaoTelegrama(data);
	}
	
	public ProcessoExpediente getUltimoExpedienteCriado(ProcessoTrf processo) {		
		ProcessoExpediente retornoUnicoDoMetodo;
		
		Search search = new Search(ProcessoExpediente.class);
		addCriteria(search, Criteria.equals("processoTrf.idProcessoTrf", processo.getIdProcessoTrf()));
		search.addOrder("idProcessoExpediente", Order.DESC);
		search.setMax(1);
		
		List<ProcessoExpediente> resultado = list(search);		
		
		if (resultado != null && !resultado.isEmpty())	{
			retornoUnicoDoMetodo = resultado.get(0);	
		} else {
			retornoUnicoDoMetodo = null;
		}
		
		return retornoUnicoDoMetodo;
	}

	/**
	 * Retorna true se o expediente for do tipo 'citação'.
	 * 
	 * @param expediente
	 * @return Boolean
	 */
	public boolean isCitacao(ProcessoExpediente expediente) {
		TipoProcessoDocumento tipoProcessoDocumento = expediente.getTipoProcessoDocumento();
		List<Integer> ids = ParametroUtil.instance().getListaIdTipoProcessoDocumentoCitacao();
		if (tipoProcessoDocumento == null) {
			return false;
		}
		return ids.contains(tipoProcessoDocumento.getIdTipoProcessoDocumento());
	}
	
	/**
	 * Retorna true se o expediente for do tipo 'intimação'.
	 * 
	 * @param expediente
	 * @return Boolean
	 */
	public boolean isIntimacao(ProcessoExpediente expediente) {
		TipoProcessoDocumento intimacao = ParametroUtil.instance().getTipoProcessoDocumentoIntimacao();
		return (ObjectUtils.allNotNull(expediente, intimacao) && intimacao.equals(expediente.getTipoProcessoDocumento()));
	}

	/**
	 * Retorna true se o expediente for do tipo 'notificação'.
	 * 
	 * @param expediente
	 * @return Boolean
	 */
	public boolean isNotificacao(ProcessoExpediente expediente) {
		TipoProcessoDocumento tipoProcessoDocumento = expediente.getTipoProcessoDocumento();
		List<Integer> ids = ParametroUtil.instance().getListaIdTipoProcessoDocumentoNotificacao();
		if (tipoProcessoDocumento == null) {
			return false;
		}
		return ids.contains(tipoProcessoDocumento.getIdTipoProcessoDocumento());
	}
}
