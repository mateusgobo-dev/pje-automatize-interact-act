package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.SessaoProcessoMultDocsVoto;

/**
 * Classe responsável por controlar as requisições da página view/popupDemaisDocumentosVoto.xhtml
 * 
 * @author Carlos Lisboa
 */
@Name(PopupDemaisDocumentoVotoAction.NAME)
@Scope(ScopeType.PAGE)
public class PopupDemaisDocumentoVotoAction extends BaseAction<SessaoProcessoDocumentoVoto> {
	
	private static final long serialVersionUID = 7268299519335868377L;

	public static final String NAME = "popupDemaisDocumentoVotoAction";
	
	@In
	private SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;
	
	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private transient ProcessoTrfManager processoTrfManager;
	
	@RequestParameter(value="idSessaoProcDoc")
	Integer idSessaoProcDoc;
	
	private SessaoProcessoDocumentoVoto voto;
	private SessaoProcessoDocumentoVoto votoRelator;
	private List<SessaoProcessoMultDocsVoto> listSessaoProcessoMultDocsVoto = new ArrayList<SessaoProcessoMultDocsVoto>(0);
	
	@Create
	public void init() {
		if(idSessaoProcDoc != null){
			inicializarInformacoes();
		}
	}
	
	/**
	 * Inicializa os valores das variáveis de instância do objeto.
	 */
	private void inicializarInformacoes(){
		voto = recuperarSessaoProcessoDocumentoVoto(idSessaoProcDoc);
		votoRelator = recuperarVotoRelator(voto.getProcessoTrf());
		listSessaoProcessoMultDocsVoto.addAll(voto.getSessaoProcessoMultDocsVoto());
	}
	
	/**
	 * Recupera o voto de acordo com o identificador informado.
	 * 
	 * @param idSessaoProcDoc Identificador do voto.
	 * 
	 * @return O voto.
	 */
	private SessaoProcessoDocumentoVoto recuperarSessaoProcessoDocumentoVoto(Integer idSessaoProcDoc) {
		SessaoProcessoDocumentoVoto sessaoProcDocVoto = new SessaoProcessoDocumentoVoto();
		try {
			sessaoProcDocVoto = getManager().findById(idSessaoProcDoc);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar o voto: {0}", e.getMessage());
		}
		return sessaoProcDocVoto;
	}

	/**
	 * Recupera o voto do relator de acordo com o processo informado.
	 * 
	 * @param processoTrf {@link ProcessoTrf}.
	 * @return O voto do relator.
	 */
	private SessaoProcessoDocumentoVoto recuperarVotoRelator(ProcessoTrf processoTrf) {
		SessaoProcessoDocumentoVoto votoRelator = new SessaoProcessoDocumentoVoto();
		List<SessaoProcessoDocumento> votosRecuperados = sessaoProcessoDocumentoManager.recuperaElementosJulgamento(
				processoTrf, null, Authenticator.getOrgaoJulgadorAtual());
		
		for(SessaoProcessoDocumento spd: votosRecuperados){
			if(SessaoProcessoDocumentoVoto.class.isAssignableFrom(spd.getClass())){
				SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto) spd;
				if(voto.getOrgaoJulgador().equals(voto.getProcessoTrf().getOrgaoJulgador())){
					votoRelator = voto;
				}
			}
		}
		return votoRelator;
	}

	/**
	 * Verifica se existe mais de um documento do voto para devida apresentação.
	 * 
	 * @param doc Voto.
	 * @return Verdadeiro se existe mais de um documento do voto. Falso, caso contrário.
	 */
	public boolean verificarDemaisDocsVoto(SessaoProcessoDocumento doc){
		SessaoProcessoDocumentoVoto sessaoProcDocVoto = sessaoProcessoDocumentoVotoManager.recuperarVoto(doc.getProcessoDocumento());
		if(sessaoProcDocVoto != null){
			List<SessaoProcessoMultDocsVoto> list = sessaoProcDocVoto.getSessaoProcessoMultDocsVoto();
			if (list != null && list.size() > 1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retorna o nome das partes do processo.
	 * 
	 * @return O nome das partes do processo.
	 */
	public String getNomePartes() {
		ProcessoTrf processoTrf = voto.getProcessoTrf();
		return processoTrfManager.getPartesNomesResumidoPoloAtivo(processoTrf) + " X " + processoTrfManager.getPartesNomesResumidoPoloPassivo(processoTrf);
	}

	@Override
	protected BaseManager<SessaoProcessoDocumentoVoto> getManager() {
		return sessaoProcessoDocumentoVotoManager;
	}

	@Override
	public EntityDataModel<SessaoProcessoDocumentoVoto> getModel() {
		return null;
	}
	
	// ACCESSOR METHODS
	
	public SessaoProcessoDocumentoVoto getVoto() {
		return voto;
	}

	public SessaoProcessoDocumentoVoto getVotoRelator() {
		return votoRelator;
	}
	
	/**
	 * Retorna a variável listSessaoProcessoMultDocsVoto com os seus elementos 
	 * ordenados de acordo com a data de inclusão do documento.
	 * 
	 * @return A variável listSessaoProcessoMultDocsVoto com os seus elementos 
	 * ordenados de acordo com a data de inclusão do documento.
	 */
	public List<SessaoProcessoMultDocsVoto> getListSessaoProcessoMultDocsVoto() {
		Collections.sort(listSessaoProcessoMultDocsVoto);
		return listSessaoProcessoMultDocsVoto;
	}

}
