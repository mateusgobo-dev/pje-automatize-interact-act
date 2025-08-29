package br.com.infox.editor.manager;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.ProcessoDocumentoTrfLocalDAO;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.jt.entidades.AssistenteAdmissibilidade;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrfLocal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ProcessoDocumentoEstruturadoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoDocumentoEstruturadoManager extends GenericManager {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "processoDocumentoEstruturadoManager";

	@In
	private ProcessoDocumentoEstruturadoTopicoManager processoDocumentoEstruturadoTopicoManager;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	@In
	private ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager;
	@In
	private CabecalhoManager cabecalhoManager;
	@In
	private AnotacaoManager anotacaoManager;
	
	private static final LogProvider log = Logging.getLogProvider(ProcessoDocumentoEstruturadoManager.class);

	/**
	 * Método wrapper
	 * Remove um documento associado a uma variável de fluxo;
	 * Primeiro testa se existe um Documento Estruturado com o id associado a variável de fluxo.
	 * Se tiver, remove o documento estruturado e todas as estruturas associadas a ele (inclusive o documento não estruturado associado)
	 * Se não tiver, tenta remover o documento não estruturado, caso exista.
	 * 
	 * @param variavelDeFluxo apontando pro id do documento a ser apagado
	 * @see {@link #removerProcessoDocumentoEstruturadoNaoAssinadoAtravesDeVariavelDoFluxo(String)}
	 * @see {@link ProcessoDocumentoHome#excluirDocumentoNaoAssinadoAtravesDeVariavelDeFluxo(String)}
	 */
	public void removerDocumento(String variavelDeFluxo) {
		if (recuperaDocumentoEstruturado(variavelDeFluxo) != null){
			removerProcessoDocumentoEstruturadoNaoAssinadoAtravesDeVariavelDoFluxo(variavelDeFluxo);
		}
		else{
			ProcessoDocumentoHome processoDocumentoHome = (ProcessoDocumentoHome) Component.getInstance(ProcessoDocumentoHome.NAME) ;
			processoDocumentoHome.removerDocumentoNaoAssinado(variavelDeFluxo);
		}
		FacesMessages.instance().clear();
	}

	/**
	 * Método wrapper
	 * Remove um ProcessoDocumentoEstruturado associado a uma variável de fluxo
	 * @param variavelDeFluxo apontando pro id do documento a ser apagado
	 * @see {@link #removerProcessoDocumentoEstruturadoNaoAssinadoAtravesDeVariavelDoFluxo(String)}
	 */
	public void removerDocumentoEstruturado(String variavelDeFluxo) {
		removerProcessoDocumentoEstruturadoNaoAssinadoAtravesDeVariavelDoFluxo(variavelDeFluxo);
	}

	public ProcessoDocumentoEstruturado recuperaDocumentoEstruturado(String variavelDeFluxo) {
		ProcessoDocumentoEstruturado pde = null;
		try{
			String idProcessoDocumento = Contexts.getBusinessProcessContext().get(variavelDeFluxo).toString();
			if (idProcessoDocumento != null){
				pde = EntityUtil.find(ProcessoDocumentoEstruturado.class, Integer.valueOf(idProcessoDocumento));
			}
		}catch (Exception e) {
			log.debug("Erro ao tentar recuperar ProcessoDocumentoEstruturado de id: " + variavelDeFluxo + "através de variável de fluxo: " + e.getMessage());
			log.debug(e);
		}
		return pde;
	}
	
	/**
	 * Remove um ProcessoDocumentoEstruturado associado a uma variável de fluxo
	 * Depois testa se existe um ProcessoDocumentoEstruturado associado aquele id,
	 * se tiver remove toda a estrutura associada ao ProcessoDocumentoEstruturado.
	 * Se não tiver, não faz nada.
	 * 
	 * @param var variável de fluxo associada ao id do texto sendo editado.
	 */
	public void removerProcessoDocumentoEstruturadoNaoAssinadoAtravesDeVariavelDoFluxo(String var) {
		try{
			ProcessoDocumentoEstruturado pde = recuperaDocumentoEstruturado(var);
			if (pde != null){
				removerProcessoDocumentoEstruturado(pde);
			}
		}catch (Exception e) {
			log.debug("Erro ao tentar remover ProcessoDocumentoEstruturado de id: " + var + "através de variável de fluxo: " + e.getMessage());
			log.debug(e);
		}

	}

	public ProcessoDocumentoEstruturado getProcessoDocumentoEstruturadoByIdProcessoDocumento(int idProcessoDocumento) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumentoEstruturado o ");
		sb.append("where o.processoDocumentoTrfLocal.processoDocumento.idProcessoDocumento = :idProcessoDocumento");
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("idProcessoDocumento", idProcessoDocumento);
		query.setMaxResults(1);
		return EntityUtil.getSingleResult(query);
	}

	public boolean isModeloDocumentoValido(ProcessoDocumentoEstruturado documentoEstruturado) {
		String modeloDocumentoBin = StringUtil.replaceQuebraLinha(documentoEstruturado.getModeloDocumento());
		String modelo = getModeloProcessoDocumento(documentoEstruturado);
		return modeloDocumentoBin.equals(modelo);
	}

	private String getModeloProcessoDocumento(ProcessoDocumentoEstruturado documentoEstruturado) {
		StringBuilder sbModelo = new StringBuilder();
		sbModelo.append(cabecalhoManager.getCabecalhoProcessado(documentoEstruturado.getCabecalho()));
		for (ProcessoDocumentoEstruturadoTopico pdTopico : documentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()) {
			if (pdTopico.isHabilitado()) {
				if (pdTopico.isExibirTitulo()) {
					sbModelo.append(StringUtil.replaceQuebraLinha(pdTopico.getTitulo()));
				}
				sbModelo.append(StringUtil.replaceQuebraLinha(pdTopico.getConteudo()));
			}
		}
		return StringUtil.replaceQuebraLinha(sbModelo.toString());
	}

	public void atualizarSHA1(ProcessoDocumentoEstruturado processoDocumentoEstruturado) {
		List<ProcessoDocumentoEstruturadoTopico> pdeTopicoList = processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList();
		for (ProcessoDocumentoEstruturadoTopico pdeTopico : pdeTopicoList) {
			processoDocumentoEstruturadoTopicoManager.atualizarSHA1(pdeTopico);
		}
	}

	public void atualizarTopicos(ProcessoDocumentoEstruturado docEstruturado) {
		List<ProcessoDocumentoEstruturadoTopico> topicoList = docEstruturado.getProcessoDocumentoEstruturadoTopicoList();
		for (ProcessoDocumentoEstruturadoTopico pdTopico : topicoList) {
			if (pdTopico.getIdProcessoDocumentoEstruturadoTopico() != null) {
				update(pdTopico.getTopico());
				processoDocumentoEstruturadoTopicoManager.update(pdTopico);
			} else {
				persist(pdTopico.getTopico());
				processoDocumentoEstruturadoTopicoManager.persist(pdTopico);
			}
		}
	}

	private void persistirDocumentoEstruturado(ProcessoDocumentoEstruturado docEstruturado) {
		try {
			ProcessoDocumento pd = processoDocumentoManager.inserirProcessoDocumento(docEstruturado.getProcessoDocumentoTrfLocal());
			docEstruturado.setIdProcessoDocumentoEstruturado(pd.getIdProcessoDocumento());
			persist(docEstruturado);
			processoDocumentoEstruturadoTopicoManager.persistirTopicos(docEstruturado);
			anotacaoManager.salvarAnotacoes();
		} catch (PJeBusinessException e) {
 			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o Documento: {0}", e.getMessage());
 			e.printStackTrace();
		}
	}

	private void atualizarDocumentoEstruturado(ProcessoDocumentoEstruturado docEstruturado) {
		ProcessoDocumentoTrfLocal pdTrfLocal = docEstruturado.getProcessoDocumentoTrfLocal();
		ProcessoDocumento pd = pdTrfLocal.getProcessoDocumento();
		atualizarSHA1(docEstruturado);
		processoDocumentoManager.atualizarProcessoDocumento(pd);
		processoDocumentoTrfLocalManager.update(pdTrfLocal);
		atualizarTopicos(docEstruturado);
		anotacaoManager.salvarAnotacoes();
		update(docEstruturado);
	}

	public void gravarDocumentoEstruturado(ProcessoDocumentoEstruturado docEstruturado) {
		atualizarSHA1(docEstruturado);
		if (docEstruturado.getIdProcessoDocumentoEstruturado() == null) {
			persistirDocumentoEstruturado(docEstruturado);
		} else {
			atualizarDocumentoEstruturado(docEstruturado);
		}
	}
	
	/**
	 * IMPORTANTE: Método refatorado para corrigir recuperação do documento para o editor estruturado (PJEII-5157 - 01/02/2013)
	 * 
	 * Método que recupera o documento estruturado referente ao último acórdão um determinado processo
	 * @author fernando.junior (17/01/2013)
	 */
	public ProcessoDocumentoEstruturado getUltimoAcordaoEstruturadoByIdProcessoTrf(int idProcessoTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumentoEstruturado o, EstruturaTipoDocumento etd ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("and etd.estruturaDocumento = o.estruturaDocumento ");
		sb.append("and etd.tipoProcessoDocumento.codigoDocumento = '7000' "); // 7000 = Acórdão
		sb.append("order by o.idProcessoDocumentoEstruturado desc");
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("idProcessoTrf", idProcessoTrf);
		query.setMaxResults(1);
		return EntityUtil.getSingleResult(query);
	}

	/**
	 * @deprecated pois nome não indica o que o método fazia na realidade 
	 * {@link ProcessoDocumentoEstruturadoManager#removerApenasParteEstruturadaDoUltimoProcessoDocumentoEstruturadoAssociadoAoProcessoTrf(ProcessoTrf)}
	 * @param processoTrf processo do qual vai se removido o documento
	 */
	@Deprecated
	public void limparTabelaTopicos(ProcessoTrf processoTrf) {
		removerApenasParteEstruturadaDoUltimoProcessoDocumentoEstruturadoAssociadoAoProcessoTrf(processoTrf);
    }
	/**
	 * remove apenas a parte estruturada do ultimo {@link ProcessoDocuementoEstuturado} associado a um {@link ProcessoTrf};
	 * @see {@link #removerApenasParteEstruturadaDoProcessoDocumentoEstruturado(ProcessoDocumentoEstruturado)}
	 * @param processoTrf processo do qual vai se removido o documento
	 */
	public void removerApenasParteEstruturadaDoUltimoProcessoDocumentoEstruturadoAssociadoAoProcessoTrf(ProcessoTrf processoTrf){
		/**
		 * PJEII-5824 Colocando ordenação ao buscar os documentos de um processo
		 * PJE-JT Antonio Lucas 12/03/2013
		 */
        String hql = "select pde from ProcessoDocumentoEstruturado pde " +
        		"where pde.processoTrf = :p ORDER BY pde.idProcessoDocumentoEstruturado DESC";
        Query q = EntityUtil.createQuery(hql);
        q.setParameter("p", processoTrf);
        ProcessoDocumentoEstruturado pd = EntityUtil.getSingleResult(q);
       
        if (pd != null){
            removerApenasParteEstruturadaDoProcessoDocumentoEstruturado(pd);
        }
	}
	
	
	/**
	 * remove apenas a parte estruturada do {@link ProcessoDocuementoEstuturado}
	 * O documento gerado permanece na base, pois fica armazenado no {@link ProcessoDocumento} com mesmo id do {@link ProcessoDocumentoEstruturado} 
	 * @param pde objeto do qual se remove apenas a parte estruturada do documento
	 */
	public void removerApenasParteEstruturadaDoProcessoDocumentoEstruturado(ProcessoDocumentoEstruturado pde){
		anotacaoManager.removerAnotacoes(pde);
		processoDocumentoEstruturadoTopicoManager.removerTopicos(pde);
		AssistenteAdmissibilidade assistenteAdmissibilidade = pde.getAssistenteAdmissibilidade();
		if (assistenteAdmissibilidade != null){
			EntityUtil.getEntityManager().remove(assistenteAdmissibilidade);
		}
		EntityUtil.getEntityManager().remove(pde);
		EntityUtil.getEntityManager().flush();

	}
	
	/**
	 * remove um {@link ProcessoDocuementoEstuturado}, inclusive o
	 * documento gerado que está armazenado no {@link ProcessoDocumento} com mesmo id do {@link ProcessoDocumentoEstruturado}
	 * Os itens das seguintes classes associados ao documento a ser excluído também são removidos nesse método:
	 * <ul>
	 * <li>{@link Anotacao}</li>
	 * <li>{@link processoDocumentoEstruturadoTopico}</li>
	 * <li>{@link AssistenteAdmissibilidade}</li>
	 * <li>{@link ProcessoDocumentoEstruturado}</li>
	 * <li>{@link ProcessoDocumentoTrfLocal}</li>
	 * <li>{@link ProcessoDocumento}</li>
	 * </ul> 
	 * @param pde objeto a ser removido
	 */
	@Transactional
	public void removerProcessoDocumentoEstruturado(ProcessoDocumentoEstruturado pde){
		try{
			pde = EntityUtil.find(ProcessoDocumentoEstruturado.class, pde.getIdProcessoDocumentoEstruturado());
			removerApenasParteEstruturadaDoProcessoDocumentoEstruturado(pde);
			//Após remover o processoDocumentoEstruturado, pode remover o processoDocumentoTrfLocal associado a ele.
			ProcessoDocumentoTrfLocalDAO processoDocumentoTrfLocalDAO = (ProcessoDocumentoTrfLocalDAO) Component.getInstance(ProcessoDocumentoTrfLocalDAO.NAME) ;
			processoDocumentoTrfLocalDAO.removerProcessoDocumentoTrfEProcessoDocumentoNaoAssinados(pde.getIdProcessoDocumentoEstruturado());
		}catch (Exception e) {
			log.debug("Erro ao remover ProcessoDocumento Estruturado", e);
			throw new AplicationException("Erro ao remover ProcessoDocumento Estruturado", e);
			
		}
		
	}
}
