package br.com.infox.cliente.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoParteExpedienteHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

/**
 * Classe utilizada para o componente da Grid antiga, processoDocumentoBin.xhtml, afim de otimizar a performance que esta prejudicada devo a
 * quantidade grande de comparações no rendered dos links para detalhe do processo.
 * 
 * @author Daniel
 * 
 */
@Name(ProcessoDocumentoBinAction.NAME)
@Scope(ScopeType.PAGE)
public class ProcessoDocumentoBinAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public final static String NAME = "processoDocumentoBinAction";
	public final static String PODE_DAR_CIENCIA = "podeDarCiencia";
	public final static String PODE_VISUALIZAR_DOCUMENTO = "podeVisualizarDocumento";
	public final static String PODE_VISUALIZAR_DOCUMENTO_TERCEIROS = "podeVisualizarDocumentoTerceiros";

	private Map<String, Boolean> map = new HashMap<String, Boolean>();
	private int idProcessoDocumento;
	private List<ProcessoDocumento> processoTrfDocumentoGridList;

	private Map<Integer, Boolean> isDocumentoExpedienteMap = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> isDocumentoAtoMap = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> showDocAtoMagAdvogadoMap = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> showDocAtoMagProcuradorMap = new HashMap<Integer, Boolean>();

	@In
	private transient ProcessoTrfManager processoTrfManager;
	@In
	private transient ProcessoDocumentoManager processoDocumentoManager;
	@In
	private transient ProcessoDocumentoBinManager processoDocumentoBinManager;
	@In
	private transient ProcessoDocumentoExpedienteManager processoDocumentoExpedienteManager;
	@In
	private transient ProcessoParteExpedienteManager processoParteExpedienteManager;
	@In
	private transient ParametroUtil parametroUtil;
	
	@In
	private transient Identity identity;
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;

	/**
	 * Método que verificará as regras necessárias para exibição do link. Assim o EL será aliviado para uma única expression.
	 * 
	 * @param pd - ProcessoDocumento em questão
	 * @return true se pode visualizar. No entanto é necessário informar o key do método, para obter a regra relativa ao perfil informado.
	 */
	public Map<String, Boolean> getRenderedMap(ProcessoDocumento pd){
		if (pd.getIdProcessoDocumento() != idProcessoDocumento){
			idProcessoDocumento = pd.getIdProcessoDocumento();
			map.put(PODE_DAR_CIENCIA, checkPodeDarCiencia(pd));
			map.put(PODE_VISUALIZAR_DOCUMENTO, checkPodeVisualizarDocumento(pd));
			map.put(PODE_VISUALIZAR_DOCUMENTO_TERCEIROS, checkPodeVisualizarDocumentoTerceiros(pd));
		}
		return map;
	}

	private boolean checkPodeVisualizarDocumentoTerceiros(ProcessoDocumento pd){
		if (processoDocumentoManager.isLiberadoConsultaPublica(pd))
			return true;
		else if (isInteiroTeor(pd))
			return podeVisualizarAcordaoVinculadoInteiroTeor(pd);
		else if (isVotoOuRelatorio(pd))
			return podeVisualizarAcordaoVinculadoVotoRelatorio(pd);
		else if (isDocumentoExpediente(pd)){
			ProcessoExpediente processoExpediente = getExpedienteDocumento(pd);
			return processoParteExpedienteManager.countPartesNaoCientes(processoExpediente) == 0;
		}
		else if (isDocumentoAto(pd)){
			return false;
		}
		else{
			return outroPerfilPodeVisualizar(pd);
		}
	}

	private boolean checkPodeVisualizarDocumento(ProcessoDocumento pd){
		if (processoDocumentoManager.isLiberadoConsultaPublica(pd)){
			return true;
		}
		if (isAdvogadoRole() || isProcuradorRole()){
			if (isInteiroTeor(pd))
				return podeVisualizarAcordaoVinculadoInteiroTeor(pd);
			if (isVotoOuRelatorio(pd))
				return podeVisualizarAcordaoVinculadoVotoRelatorio(pd);
			else if (isDocumentoAto(pd))
				return podeVisualizarDocumentoAto(pd);
			else if (isDocumentoExpediente(pd))
				return ProcessoParteExpedienteHome.instance().podeTomarCiencia(pd)
						&& !ProcessoParteExpedienteHome.instance().semCiencia(pd);
		}
		return outroPerfilPodeVisualizar(pd);
	}

	private boolean checkPodeDarCiencia(ProcessoDocumento pd){
		return (Identity.instance().hasRole("procurador") || Identity.instance().hasRole("advogado") || Identity
				.instance().hasRole("procChefe"))
				&& isDocumentoExpediente(pd)
				&& ProcessoParteExpedienteHome.instance().semCiencia(pd)
				&& ProcessoParteExpedienteHome.instance().podeTomarCiencia(pd);
	}

	private ProcessoExpediente getExpedienteDocumento(ProcessoDocumento pd){
		String sql = "select o.processoExpediente from ProcessoDocumentoExpediente o "
				+ "where o.processoDocumento = :processoDocumento";
		Query query = EntityUtil.createQuery(sql);
		query.setParameter("processoDocumento", pd);
		return EntityUtil.getSingleResult(query);
	}

	private boolean podeVisualizarAcordaoVinculadoVotoRelatorio(ProcessoDocumento votoRelatorio){
		List<ProcessoDocumento> processoDocumentoList = getProcessoTrfDocumentoGridList();
		for (int i = processoDocumentoList.indexOf(votoRelatorio) - 1; i > 0; i--){
			ProcessoDocumento processoDocumento = processoDocumentoList.get(i);
			if (isAcordao(processoDocumento)){
				return podeVisualizarAcordao(processoDocumento);
			}
		}
		return false;
	}

	private boolean podeVisualizarAcordaoVinculadoInteiroTeor(ProcessoDocumento inteiroTeor){
		List<ProcessoDocumento> processoDocumentoList = getProcessoTrfDocumentoGridList();
		if(processoDocumentoList.size() > processoDocumentoList.indexOf(inteiroTeor)+1){
			ProcessoDocumento acordao = processoDocumentoList.get(processoDocumentoList.indexOf(inteiroTeor) + 1);
			return podeVisualizarAcordao(acordao);
		}else{
			return false;
		}
	}

	private boolean podeVisualizarAcordao(ProcessoDocumento acordao){
		return processoDocumentoManager.isLiberadoConsultaPublica(acordao) || podeVisualizarDocumentoAto(acordao);
	}

	private boolean isInteiroTeor(ProcessoDocumento pd){
		return pd.getTipoProcessoDocumento().equals(parametroUtil.getTipoProcessoDocumentoInteiroTeor());
	}

	private boolean isAcordao(ProcessoDocumento processoDocumento){
		return processoDocumento.getTipoProcessoDocumento().equals(parametroUtil.getTipoProcessoDocumentoAcordao());
	}

	private boolean isVotoOuRelatorio(ProcessoDocumento pd){
		return pd.getTipoProcessoDocumento().equals(parametroUtil.getTipoProcessoDocumentoVoto())
				|| pd.getTipoProcessoDocumento().equals(parametroUtil.getTipoProcessoDocumentoRelatorio());
	}

	private boolean outroPerfilPodeVisualizar(ProcessoDocumento pd){
		return pd.getAtivo() && (
			ProcessoParteExpedienteHome.instance().podeTomarCiencia(pd) && 
			(!ProcessoParteExpedienteHome.instance().semCiencia(pd) || !(isAdvogadoRole() || isProcuradorRole())) || 
			Authenticator.getUsuarioLogado().equals(pd.getUsuarioInclusao())) && 
		ProcessoDocumentoHome.instance().validaDocumentoAdvogadoDetalheProcesso(pd);
	}

	private boolean podeVisualizarDocumentoAto(ProcessoDocumento pd){
		boolean showDetalhe = !ProcessoParteExpedienteHome.instance().semCiencia(pd)
				&& ProcessoParteExpedienteHome.instance().podeTomarCiencia(pd);
		if (isAdvogadoRole()){
			return showDetalhe && showDocAtoMagistradoAdvogado(pd);
		}
		else if (isProcuradorRole()){
			return showDetalhe && showDocAtoMagistradoProcurador(pd);
		}
		return false;
	}

	/**
	 * Verifica se o usuario logado é um tipo de procurador.
	 * 
	 * @return true se for um tipo de procurador.
	 */
	private boolean isProcuradorRole(){
		return Identity.instance().hasRole("procurador") 
					|| Identity.instance().hasRole("procChefe")
					|| Identity.instance().hasRole("assistProcuradoria");
	}

	/**
	 * Verifica se o usuario logado é um tipo de advogado.
	 * 
	 * @return true se for um tipo de advogado.
	 */
	private boolean isAdvogadoRole(){
		return Identity.instance().hasRole("advogado") || Identity.instance().hasRole("assistAdvogado")
				|| Identity.instance().hasRole("assistGestorAdvogado");
	}

	private boolean isDocumentoExpediente(ProcessoDocumento processoDocumento){
		Boolean isDocumentoExpediente = isDocumentoExpedienteMap.get(processoDocumento.getIdProcessoDocumento());
		if (isDocumentoExpediente == null){
			isDocumentoExpediente = processoDocumentoManager.isDocumentoExpediente(processoDocumento) 
									|| processoDocumento.getTipoProcessoDocumento().getInTipoExpediente() != null;
			isDocumentoExpedienteMap.put(processoDocumento.getIdProcessoDocumento(), isDocumentoExpediente);
		}
		return isDocumentoExpediente;
	}

	private boolean isDocumentoAto(ProcessoDocumento processoDocumento){
		Boolean isDocumentoAto = isDocumentoAtoMap.get(processoDocumento.getIdProcessoDocumento());
		if (isDocumentoAto == null){
			isDocumentoAto = processoDocumentoManager.isDocumentoAto(processoDocumento);
			isDocumentoAtoMap.put(processoDocumento.getIdProcessoDocumento(), isDocumentoAto);
		}
		return isDocumentoAto;
	}

	/**
	 * Método que verifica se deve ser mostrado ou não o documento do ato do magistrado.
	 * 
	 * @param pd Processo Documento a ser verificado
	 * @return true se deve ser exibido.
	 */
	private boolean showDocAtoMagistradoAdvogado(ProcessoDocumento pd){
		Boolean value = showDocAtoMagAdvogadoMap.get(pd.getIdProcessoDocumento());
		if (value == null){
			value = isAtoVinculadoExpedienteComCienciaPartes(pd);
			showDocAtoMagAdvogadoMap.put(pd.getIdProcessoDocumento(), value);
		}
		return value;
	}

	/**
	 * Método que verifica se deve ser mostrado ou não o documento do ato do magistrado.
	 * 
	 * @param pd Processo Documento a ser verificado
	 * @return true se deve ser exibido.
	 */
	public boolean showDocAtoMagistradoProcurador(ProcessoDocumento pd){
		Boolean value = showDocAtoMagProcuradorMap.get(pd.getIdProcessoDocumento());
		if (value == null){
			ProcessoTrf processoTrf = processoTrfManager.getProcessoTrfByProcesso(pd.getProcesso());
			value = isAtoVinculadoExpedienteComCienciaPartes(pd)
					|| processoParteExpedienteManager.existeExpedienteEntidadeIntimacao(processoTrf,
							Authenticator.getPessoaLogada());

			showDocAtoMagProcuradorMap.put(pd.getIdProcessoDocumento(), value);
		}
		return value;
	}

	private boolean isAtoVinculadoExpedienteComCienciaPartes(ProcessoDocumento pd){
		List<ProcessoDocumento> processoDocumentoList = processoDocumentoExpedienteManager.getListaProcessoDocumentoVinculadoAto(pd);
		return processoDocumentoList.size() == 0 || temDocumentoComCienciaPartes(processoDocumentoList);
	}

	private boolean temDocumentoComCienciaPartes(List<ProcessoDocumento> processoDocumentoList){
		for (ProcessoDocumento processoDocumento : processoDocumentoList){
			if (!ProcessoParteExpedienteHome.instance().semCiencia(processoDocumento) && isParteExpedienteDocumento(processoDocumento)){
				return true;
			}
		}
		return false;
	}

	private boolean isParteExpedienteDocumento(ProcessoDocumento processoDocumento){
		ProcessoExpediente processoExpediente = getExpedienteDocumento(processoDocumento);
		List<Pessoa> pessoaList = PessoaAdvogadoHome.instance().getPessoaAdvogadoProcurador();
		for (ProcessoParteExpediente processoParteExpediente : processoExpediente.getProcessoParteExpedienteList()){
			if (pessoaList.contains(processoParteExpediente.getPessoaParte())){
				return true;
			}
		}
		return false;
	}

	public void limparMap(){
		map = new HashMap<String, Boolean>();
		isDocumentoAtoMap = new HashMap<Integer, Boolean>();
		showDocAtoMagAdvogadoMap = new HashMap<Integer, Boolean>();
		showDocAtoMagProcuradorMap = new HashMap<Integer, Boolean>();
	}

	public void setProcessoTrfDocumentoGridList(List<ProcessoDocumento> processoTrfDocumentoGridList){
		this.processoTrfDocumentoGridList = processoTrfDocumentoGridList;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getProcessoTrfDocumentoGridList(){
		if (processoTrfDocumentoGridList == null){
			GridQuery grid = ComponentUtil.getComponent("processoTrfDocumentoGrid");
			processoTrfDocumentoGridList = grid.getResultList();
		}
		return processoTrfDocumentoGridList;
	}
	
	/**
	 * Verifica se o usuário tem permissão para
	 * excluir um documento
	 * @param pd
	 * @return
	 */
	public boolean podeExcluirDocumento(ProcessoDocumento pd){
		/* PJEII-17000 - Evitar que documentos do processo
		 * sejam excluídos por servidores de outro órgão julgador
		 */
		if(identity.hasRole(Papeis.DESENTRANHA_DOCUMENTO) || identity.hasRole(Papeis.MAGISTRADO)){
			OrgaoJulgador oj = Authenticator.getOrgaoJulgadorAtual();
			if(oj != null && oj.equals(pd.getProcessoTrf().getOrgaoJulgador())){
				return true;
			}else if (oj == null){
				OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();
				if(ojc != null && ojc.equals(pd.getProcessoTrf().getOrgaoJulgadorColegiado())){
					return true;
				}
			}
		}
		return false;
	}

	public boolean permiteVisualizarOpcaoDeExcluirDocumento(ProcessoDocumento processoDocumento) {
		return this.permiteExclusaoLogicaDocumento(processoDocumento);
	}
	
	/**
	 * Verifica se o usuário poderá visualizar o botão de INATIVAR o documento de acordo com as condições abaixo:
	 * - Não é a petição inicial
	 * - Usuario logado tem permissão de excluir documentos
	 * - Documento está ativo
	 * - Processo NÃO está em elaboração
	 * - Documento foi juntado
	 * 
	 * @param processoDocumento
	 * @return
	 */
	public boolean permiteExclusaoLogicaDocumento(ProcessoDocumento processoDocumento) {
		boolean retorno = false;
		
		if(isProcessoBloqueadoMigracao(processoDocumento)) {
			return false;
		}
		
		if (processoDocumento != null) {
			Integer idTipoProcessoDocumentoPeticaoInicial = processoDocumento.getProcessoTrf()
				.getClasseJudicial().getTipoProcessoDocumentoInicial().getIdTipoProcessoDocumento();
			
			retorno = idTipoProcessoDocumentoPeticaoInicial != processoDocumento.getTipoProcessoDocumento().getIdTipoProcessoDocumento() &&
				podeExcluirDocumento(processoDocumento) && processoDocumento.getAtivo() &&
					!isEmElaboracao(processoDocumento) && processoDocumento.getDataJuntada() != null;
		}
		
		return retorno;
	}

	/**
	 * Verifica se o usuário poderá visualizar o botão de EXCLUSÃO (para exclusão física) do documento de acordo com as condições abaixo:
	 * - Usuario logado é quem criou o documento ou é da localização de quem criou o documento
	 * - Documento está ativo
	 * - Processo está em elaboração OU documento não foi juntado
	 * - Documento não foi criado em uma atividade específica (ex.: fluxo, diligência, voto, acórdão, ... )
	 * 
	 * @param processoDocumento
	 * @return
	 */
	public boolean permiteExclusaoFisicaDocumento(ProcessoDocumento processoDocumento) {
		return processoDocumento != null && (
				(processoDocumento.getLocalizacao() != null && Authenticator.getIdLocalizacaoFisicaAtual().equals(
					processoDocumento.getLocalizacao().getIdLocalizacao())) || 
				(processoDocumento.getUsuarioInclusao() != null && Authenticator.getIdUsuarioLogado().equals(
					processoDocumento.getUsuarioInclusao().getIdUsuario()))
			) && processoDocumento.getAtivo() && 
			(isEmElaboracao(processoDocumento) || processoDocumento.getDataJuntada() == null) && 
			!processoDocumento.getExclusivoAtividadeEspecifica()
			&& !isProcessoBloqueadoMigracao(processoDocumento);
	}

	public boolean permiteExcluirDocumento(ProcessoDocumento processoDocumento) {
		return this.permiteExclusaoFisicaDocumento(processoDocumento);
	}
	
	/**
	 * Método público responsável por verificar se o processo está ou não com o status "E - Em elaboração".
	 * 
	 * @param processoDocumento
	 * @return true se o processo encontra-se no status "E - Em elaboração"
	 */
	public boolean isEmElaboracao(ProcessoDocumento processoDocumento){		
		return processoDocumento != null && processoDocumento.getProcessoTrf() != null && 
			ProcessoStatusEnum.E.equals(processoDocumento.getProcessoTrf().getProcessoStatus());
	}
	
	/**
	 * Método responsável por realizar a verificação para exibição ou não do cadeado de assinatura no agrupador
	 * de documentos do processo.
	 * Obs.: Somente para processos "E" Em elaboração ou que não estejam distribuídos e de acordo com as regras
	 * de negócio.
	 * 
	 * @param idProcessoTrf
	 * @param processoDocumento
	 * @return true se exibe o cadeado do assinador 
	 */
	public boolean exibeCadeadoAssinadorProcessoNaoProtocolado(Processo processo, ProcessoDocumento processoDocumento){
		boolean ret = false;
		if(processo != null && processoDocumento != null){
			ret = processoDocumentoManager
					.exibeCadeadoAssinadorProcessoNaoProtocolado(processo.getIdProcesso(),processoDocumento);
		}
		return ret;
	}
	
	/**
	 * Método que verifica se o documento está assinado ou não.
	 * 
	 * @param processoDocumento
	 * @return true se o documento estiver assinado
	 */
	public boolean isDocumentoAssinado(ProcessoDocumento processoDocumento){
		return processoDocumentoBinManager.temAssinatura(processoDocumento.getProcessoDocumentoBin());
	}
	
	/**
	 * Método que verifica se o documento é uma petição inicial
	 * 
	 * @param processoDocumento
	 * @return true se o documento passado é uma petição inicial
	 */
	public boolean isDocumentoPeticaoInicial(ProcessoDocumento processoDocumento){
		return processoDocumentoManager.isDocumentoPeticaoInicial(processoDocumento);
	}
	
	/**
	 * Método que verifica as regras do documento PAI, se exibe ou não o cadeado
	 * 
	 * @param processoDocumento
	 * @return true se exibe o cadeado
	 */
	public boolean exibeCadeadoDocumentoPai(ProcessoDocumento processoDocumento){
		boolean exibeCadeadoAssinado = exibeCadeadoAssinadorProcessoNaoProtocolado(processoDocumento.getProcesso(), processoDocumento);
		boolean retorno = processoDocumentoManager.exibeCadeadoDocumentoPai(processoDocumento,exibeCadeadoAssinado);
		return retorno;
	}
	
	/**
	 * Método que verifica as regras do documento FILHO, se exibe ou não o cadeado
	 * 
	 * @param processoDocumento
	 * @return true se exibe o cadeado
	 */
	public boolean exibeCadeadoDocumentoFilho(ProcessoDocumento processoDocumento){
		boolean exibeCadeadoAssinado = exibeCadeadoAssinadorProcessoNaoProtocolado(processoDocumento.getProcesso(), processoDocumento);
		boolean retorno = processoDocumentoManager.exibeCadeadoDocumentoFilho(processoDocumento,exibeCadeadoAssinado);
		return retorno;
	}
	
	/**
	 * Método que verifica as regras do documento FILHO para processos protocolados, se exibe ou não o cadeado
	 * 
	 * @param processoDocumento
	 * @return true se exibe o cadeado
	 */
	public boolean exibeCadeadoDocumentoPaiFilhoProcessoProtocolado(ProcessoDocumento processoDocumento){
		boolean retorno = false;
		if (processoDocumento!=null){
			retorno = processoDocumentoManager.exibeCadeadoDocumentoPaiFilhoProcessoProtocolado(processoDocumento);
		}
		return retorno;
	}
	
	/**
	 * Método responsável por realizar a criação de um protocolarDocumentoBean a partir do 
	 * id do processo documento
	 * 
	 * @param idProcessoDocumento
	 * @return protolarDocumentoBean instanciado
	 */
	public ProtocolarDocumentoBean getProtocolarBean(Integer idProcessoDocumento){
		if (this.protocolarDocumentoBean == null) {
			ProtocolarDocumentoBean protocolarDocumentoBean = null;
			if(idProcessoDocumento != null){
				try {
					ProcessoDocumento pdd = processoDocumentoManager.findById(idProcessoDocumento);
					ProcessoDocumento documentoPrincipal = (pdd.getDocumentoPrincipal() == null) ? pdd : pdd.getDocumentoPrincipal();
					protocolarDocumentoBean = new ProtocolarDocumentoBean(documentoPrincipal.getProcessoTrf().getIdProcessoTrf(), true, false, true, false, false, true, false);
					protocolarDocumentoBean.setDocumentoPrincipal(documentoPrincipal);
					protocolarDocumentoBean.loadArquivosAnexadosDocumentoPrincipal();
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				}
			}
			this.protocolarDocumentoBean = protocolarDocumentoBean;
		}
		return this.protocolarDocumentoBean;
	}
	
	public String getViewValidaDocumento(){
		return "/Processo/Consulta/validaDocumento.xhtml";
	}
	
	public String getViewAssinaDocumento(){
		return "/Processo/Consulta/assinaDocumento.xhtml";
	}
	
	/**
	 * Método para obter a url onde o documento do processo será exibido. Caso o processo ainda não tenha sido protocolado, o documento será exibido
	 * utilizando o componente "documentoHTML.seam", senão, utilizará o "visualizarExpediente.seam".
	 * @param	processoDoc
	 * @return	Caso o processo tenha sido protocolado, retornará a url do componente "visualizarExpediente". Caso contrário, retornará a url do 
	 * 			componente "documentoHTML".
	 */
	public String obterUrlExibicaoDocumento(ProcessoDocumento processoDoc){
		String retorno = StringUtils.EMPTY;
		if (processoDoc != null && processoDoc.getProcessoTrf()!= null && processoDoc.getProcessoTrf().getProcessoStatus().equals(ProcessoStatusEnum.D)) {
			retorno = "/Painel/painel_usuario/popup/visualizarExpediente.seam";
		}else {
			retorno = "/Painel/painel_usuario/documentoHTML.seam";
		}
		return retorno;
	}
	
	public Boolean isProcessoBloqueadoMigracao(ProcessoDocumento processoDocumento) {
		
		if(processoDocumento != null) {
			ProcessoTrf processoTrf = processoTrfManager.getProcessoTrfByProcesso(processoDocumento.getProcesso());
			return processoTrf.getInBloqueioMigracao();
		}else {
			return Boolean.FALSE;
		}
	}	
}