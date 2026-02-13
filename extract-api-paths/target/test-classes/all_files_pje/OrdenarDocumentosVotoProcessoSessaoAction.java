package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.jus.cnj.pje.entidades.vo.OrdenarDocumentosVotoProcessoSessaoVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author Rafael Matos
 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
 * @see Componente de controle do frame WEB-INF/xhtml/flx/ordenarDocumentosVotoProcessoSessao.xhtml.
 * @since 27/04/2015
 */
@Name(OrdenarDocumentosVotoProcessoSessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class OrdenarDocumentosVotoProcessoSessaoAction extends TramitacaoFluxoAction implements Serializable{

	private static final long serialVersionUID = 4210688126871355879L;

	public static final String NAME = "ordenarDocumentosVotoProcessoSessaoAction";
	
    private SessaoPautaProcessoTrf sessaoJulgamento;
    
    private List<OrdenarDocumentosVotoProcessoSessaoVO> listaVotos;
    
    @In
    private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
    
    @In
    private ProcessoJudicialManager processoJudicialManager;
    
    @In (create=true)
    private SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome;
    
    @In (create=true)
    private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
    
	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return new HashMap<String, String>();
	}
	
	@Override
    public void init(){
    	super.init();
		inicializar();
    }
	
	/**
	 * @author Rafael Matos
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @see Metodo responsável por inicializar a variavel 
	 * sessão de julgamento com os dados do processo no fluxo.
	 * @since 09/06/2015
	 */
	private void inicializar(){
		setSessaoJulgamento(sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfJulgado(ProcessoJbpmUtil.getProcessoTrf()));
		if (getSessaoJulgamento() == null) {
			throw new RuntimeException("Não foi possível recuperar a sessão de julgamento do processo!");
		}
		getSessaoPautaProcessoTrfHome().setInstanciaParaFluxo();
	}
	
	/**
	 * @author Rafael Matos
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @param OrdenarDocumentosVotoProcessoSessaoVO objeto com os dados dos votos
	 * @param novaOrdem nova ordem a ser atribuida ao voto
	 * @see Metodo que altera a ordem dos votos.
	 * @since 05/05/2015
	 */
	public void alterarOrdem(OrdenarDocumentosVotoProcessoSessaoVO o, Integer novaOrdem) throws PJeBusinessException{
		Integer ordemAtual = o.getOrdemDocumento();
		listaVotos.remove(o);
		o.setOrdemDocumento(novaOrdem);
		sessaoProcessoDocumentoVotoManager.atualizaOrdemVoto(o.getId(),novaOrdem);
		List<OrdenarDocumentosVotoProcessoSessaoVO> lsTemp = new ArrayList<OrdenarDocumentosVotoProcessoSessaoVO>();
		for (OrdenarDocumentosVotoProcessoSessaoVO voto : listaVotos) {
			if (voto.getOrdemDocumento().equals(novaOrdem)){
				voto.setOrdemDocumento(ordemAtual);
				sessaoProcessoDocumentoVotoManager.atualizaOrdemVoto(voto.getId(),ordemAtual);
			}
			lsTemp.add(voto);
		}
		
		lsTemp.add(o);
		listaVotos.clear();
		listaVotos.addAll(lsTemp);
		Collections.sort(listaVotos);
	}
	
	/**
	 * @author Rafael Matos
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @see Metodo responsavel por reornar os votos, atualizando caso não exista ordem cadastrada.
	 * @since 05/05/2015
	 */
	public void reordenaVotos() {
		List<OrdenarDocumentosVotoProcessoSessaoVO> lsTemp = new ArrayList<OrdenarDocumentosVotoProcessoSessaoVO>();
		int i = 1;
		for (OrdenarDocumentosVotoProcessoSessaoVO voto : listaVotos) {
			voto.setOrdemDocumento(i);
			sessaoProcessoDocumentoVotoManager.atualizaOrdemVoto(voto.getId(),i);
			i++;
			lsTemp.add(voto);
		}
		listaVotos.clear();
		listaVotos.addAll(lsTemp);
	}

	public void setListaVotos(List<OrdenarDocumentosVotoProcessoSessaoVO> listaVotos) {
		this.listaVotos = listaVotos;
	}

	public SessaoPautaProcessoTrfHome getSessaoPautaProcessoTrfHome() {
        return sessaoPautaProcessoTrfHome;
    }

    public void setSessaoPautaProcessoTrfHome(SessaoPautaProcessoTrfHome sessaoPautaProcessoTrfHome) {
        this.sessaoPautaProcessoTrfHome = sessaoPautaProcessoTrfHome;
    }
    
    public String getRelator(){
		Usuario relator = processoJudicialManager.getRelator(ProcessoJbpmUtil.getProcessoTrf());
		return relator == null ? null : relator.toString();
	}

	public SessaoPautaProcessoTrf getSessaoJulgamento() {
		return sessaoJulgamento;
	}

	public void setSessaoJulgamento(SessaoPautaProcessoTrf sessaoJulgamento) {
		this.sessaoJulgamento = sessaoJulgamento;
	}
	
	/**
	 * @author Rafael Matos
	 * @link https://www.cnj.jus.br/jira/browse/PJEII-20513
	 * @see Metodo responsavel por alimetar lista com votos, atualizando caso não exista ordem cadastrada.
	 * @since 05/05/2015
	 */
	public List<OrdenarDocumentosVotoProcessoSessaoVO> getListaVotos() {
		if (listaVotos==null){
			listaVotos = sessaoProcessoDocumentoVotoManager.recuperarSessaoVotosComDocumentosPorSessaoEhProcessoVO(getSessaoJulgamento().getSessao(), ProcessoJbpmUtil.getProcessoTrf());
			if (listaVotos!=null && listaVotos.size() > 0 && (listaVotos.get(0).getOrdemDocumento() == null || listaVotos.get(0).getOrdemDocumento() == 0)){
				reordenaVotos();
			}
		}
		if (listaVotos==null){
			listaVotos = new ArrayList<OrdenarDocumentosVotoProcessoSessaoVO>();
		}
		return listaVotos;
	}
}