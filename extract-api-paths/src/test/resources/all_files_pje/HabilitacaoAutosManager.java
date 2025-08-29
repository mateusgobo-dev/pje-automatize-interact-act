package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.dao.ProcessoTrfDAO;
import br.com.itx.util.FacesUtil;
import br.com.jt.pje.dao.HabilitacaoAutosDAO;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoPeticaoNaoLidaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.pje.jt.entidades.HabilitacaoAutos;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name("habilitacaoAutosManager")
public class HabilitacaoAutosManager extends BaseManager<HabilitacaoAutos>{

	@In(create=true)
	private ProcessoParteManager processoParteManager;

	@In(create=true)
	private ProcessoParteRepresentanteManager processoParteRepresentanteManager;
	
	@In(create = true)
	private HabilitacaoAutosDAO habilitacaoAutosDAO;
	
	@In(create = true)
	private ProcessoTrfDAO processoTrfDAO;
	
	@In(create = true)
	ProcessoDocumentoPeticaoNaoLidaManager processoDocumentoPeticaoNaoLidaManager;
	
	@In(create = true)
	ProcessoDocumentoManager processoDocumentoManager;
		
	@In
	PapelManager papelManager;
	
	@In
	ProcuradoriaManager procuradoriaManager;

	@Override
	protected BaseDAO<HabilitacaoAutos> getDAO(){
		
		return this.habilitacaoAutosDAO;
	}

	public List<ProcessoParte> getProcessoPartePoloAtivoList(ProcessoTrf processoTrf) {
		return habilitacaoAutosDAO.getProcessoParteListByPoloByProcessoTrf(processoTrf, ProcessoParteParticipacaoEnum.A);
	}

	@Override
	public void persistAndFlush(HabilitacaoAutos habilitacaoAutos) throws PJeBusinessException {
		for (ProcessoParte parte : habilitacaoAutos.getRepresentados()) {
			for (ProcessoParteRepresentante processoParteRepresentante : parte.getProcessoParteRepresentanteList()) {

				//Somente novos representantes.
				if(processoParteRepresentante.getIdProcessoParteRepresentante() == 0){
					processoParteManager.persistAndFlush( processoParteRepresentante.getParteRepresentante() );
					processoParteRepresentanteManager.persistAndFlush(processoParteRepresentante);
				}

			}
		}

		for(ProcessoParteRepresentante representante: habilitacaoAutos.getRepresentantesRemovidos()){
			processoParteRepresentanteManager.persistAndFlush( representante );
		}
		super.persistAndFlush(habilitacaoAutos);
		
	}

	public List<ProcessoParte> getProcessoPartePoloPassivoList(ProcessoTrf processoTrf) {
		return habilitacaoAutosDAO.getProcessoParteListByPoloByProcessoTrf(processoTrf, ProcessoParteParticipacaoEnum.P);
	}
	
	
	public List<ProcessoParte> getProcessoHabilitacaoPendente(ProcessoTrf processoTrf) {
		return habilitacaoAutosDAO.getProcessoHabilitacaoPendente(processoTrf);
	}

	
	public void clear(){
		
		habilitacaoAutosDAO.clear();
		
	}
	
	public ProcessoTrf getProcessoTrfByProcesso(Processo processo) {
		return processoTrfDAO.find(ProcessoTrf.class, processo.getIdProcesso());
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLidaHabilitacaoAutos()
	{
		return habilitacaoAutosDAO.getProcessoDocumentoPeticaoNaoLidaHabilitacaoAutos();
	}
	
	/**
	 * Recupera a habilitação nos autos relativo ao documento com número informado como sua petição
	 * @param processo Processo no qual o pedido de habilitação nos autos foi feito
	 * @param docNum Identificador do documento que é a petição do pedido de habilitação nos autos.
	 * @return Registro da habilitação nos autos 
	 */
	public HabilitacaoAutos recuperaHabilitacao(ProcessoTrf processo, Integer docNum){
		List<HabilitacaoAutos> habilitacaoAutosList = habilitacaoAutosDAO.findByProcessoTrf(processo);
		ProcessoDocumento documento = new ProcessoDocumento();
		
		try {
			documento = processoDocumentoManager.findById(docNum);
			if (documento == null) {
				FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "msg.habilitacao.documento.nao.encontrado"), docNum);
				throw new NegocioException("Documento não encontrado.");
				}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			throw new NegocioException("Erro desconhecido ao buscar o documento.");
		}
		if (habilitacaoAutosList == null || habilitacaoAutosList.isEmpty()){
			FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "msg.habilitacao.peticao.nao.encontrada"), processo.getNumeroProcesso());
			throw new NegocioException("Petição não encontrada.");
		}
		for (HabilitacaoAutos habilitacaoAutos: habilitacaoAutosList) {
			if (habilitacaoAutos.getDocumentos().contains(documento)) {
				return habilitacaoAutos;
			}
		}
		FacesMessages.instance().add(Severity.ERROR, FacesUtil.getMessage("entity_messages", "msg.habilitacao.nao.encontrada"), processo.getNumeroProcesso(), docNum);
		throw new NegocioException("Habilitação nos autos não encontrada.");		
	}
	
	
	public boolean verificarHabilitacaoAutosParaAdvogado( PessoaFisica pessoaFisica, UsuarioLocalizacao usuarioLocalizacao ){
		if( papelManager.possuiPapelAdvogado(usuarioLocalizacao) &&  pessoaFisica.getPessoaAdvogado() instanceof PessoaAdvogado ){			
			return true;
		}
		
		return false;		
	}
	
	public boolean verificarHabilitacaoAutosParaDefensor( PessoaFisica pessoaFisica, UsuarioLocalizacao usuarioLocalizacao ){
		Procuradoria procuradoria = procuradoriaManager.recuperaPorLocalizacao( Authenticator.getLocalizacaoAtual());		
		if( procuradoria != null &&  pessoaFisica.getPessoaProcurador() instanceof PessoaProcurador ){			
			if(procuradoria.getTipo() == TipoProcuradoriaEnum.P ){
				return false;
			}
			else{
				return true;
			}
		}		
		return false;
	}
}
