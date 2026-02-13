/**
 * 
 */
package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO.CriterioPesquisa;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;

/**
 * @author cristof
 *
 */
@Name("atoComunicacaoAction")
@Scope(ScopeType.EVENT)
public class AtoComunicacaoAction {
	
	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private AtoComunicacaoService atoComunicacaoService;
	
	@In
	private UsuarioService usuarioService;
	
	@In
	private PessoaService pessoaService;
	
	private AtoComunicacaoDataModel atoComunicacaoDataModel = new AtoComunicacaoDataModel();
	
	@Create
	public void init(){
		logger.debug("Inicializando atoComunicacaoAction.");
		atoComunicacaoDataModel.setAtoComunicacaoService(atoComunicacaoService);
		atoComunicacaoDataModel.setCriterio(CriterioPesquisa.INTIMACAO_PENDENTE);
		try {
			atoComunicacaoDataModel.setDestinatario(pessoaService.findById(usuarioService.getUsuarioLogado().getIdUsuario()));
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao tentar identificar o usuário atual: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public AtoComunicacaoDataModel getAtoComunicacaoDataModel() {
		return atoComunicacaoDataModel;
	}

	public void setAtoComunicacaoDataModel(AtoComunicacaoDataModel atoComunicacaoDataModel) {
		this.atoComunicacaoDataModel = atoComunicacaoDataModel;
	}
	
}
