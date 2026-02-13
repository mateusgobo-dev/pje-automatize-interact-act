package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoTagManager;
import br.jus.cnj.pje.nucleo.manager.TagManager;
import br.jus.pje.nucleo.entidades.ProcessoTag;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("tagService")
@Scope(ScopeType.APPLICATION)
public class TagService implements Serializable {

	private static final long serialVersionUID = 1L;

	@In
	private TagManager tagManager;

	@In
	private ProcessoTagManager processoTagManager;

	@Logger
	protected Log logger;

	public void vincularTagProcessoViaFluxo(ProcessoTrf processoTrf, String nomeTag, Integer idLocalizacaoFisica, Integer idUsuario){
		try{
			vincularTagProcesso(processoTrf, nomeTag, idLocalizacaoFisica, idUsuario);
		}catch(Exception e){
			logger.error("Ocorreu erro ao vincular a Tag {0} ao Processo {1} - Exception: {2}", nomeTag, processoTrf.getProcesso().getNumeroProcesso(), e.getLocalizedMessage()); 
		}
	}

	public void vincularTagProcesso(ProcessoTrf processoTrf, String nomeTag, Integer idLocalizacaoFisica, Integer idUsuario) throws Exception{
		if (processoTrf == null){
			throw new Exception(" ProcessoTrf não informado");
		}
		if(nomeTag.trim().isEmpty() || nomeTag == null){
			throw new Exception(" Nome da Tag não informada");
		}
		if(idLocalizacaoFisica == null){
			throw new Exception(" Id da Localização não informado");
		}
		if(idUsuario == null){
			throw new Exception(" Id do Usuario não informado");
		}
		processoTagManager.criarProcessoTag(new Long(processoTrf.getIdProcessoTrf()), nomeTag, idLocalizacaoFisica, idUsuario);
	}

	public void desvincularTagProcessoViaFluxo(ProcessoTrf processoTrf, String nomeTag, Integer idLocalizacaoFisica){
		try{
			desvincularTagProcesso(processoTrf, nomeTag, idLocalizacaoFisica);
		}catch(Exception e){
			logger.error("Ocorreu erro ao Desvincular a Tag {0} ao Processo {1} - Exception: {2}", nomeTag, processoTrf.getProcesso().getNumeroProcesso(), e.getLocalizedMessage());
		}
	}

	public void desvincularTagProcesso(ProcessoTrf processoTrf, String nomeTag, Integer idLocalizacaoFisica) throws Exception{
		if(!processoTagManager.existeTag(new Long(processoTrf.getIdProcessoTrf()), nomeTag, idLocalizacaoFisica)){
			throw new PJeBusinessException("Tag esta desvinculada para o processo.");
		}
		List<ProcessoTag> ListProcTag = processoTagManager.recuperaProcessoTag(new Long(processoTrf.getIdProcessoTrf()), nomeTag, idLocalizacaoFisica);
		if(ListProcTag!=null && !ListProcTag.isEmpty() && processoTrf != null){
			for(ProcessoTag procTag : ListProcTag){
				processoTagManager.removerTag(new Long(processoTrf.getIdProcessoTrf()), procTag.getTag().getId());
			}
		}
	}
}
