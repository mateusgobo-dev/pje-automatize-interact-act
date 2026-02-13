package br.com.infox.editor.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.service.ProcessaModeloService;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.HistoricoProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.Topico;
import br.jus.pje.nucleo.entidades.editor.topico.ITopicoComConclusao;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.Crypto.Type;

@Name(ProcessoDocumentoEstruturadoTopicoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessoDocumentoEstruturadoTopicoManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "processoDocumentoEstruturadoTopicoManager";

	@In
	private EstruturaDocumentoTopicoMagistradoManager estruturaDocumentoTopicoMagistradoManager;
	@In
	private ProcessaModeloService processaModeloService;
	
	public ProcessoDocumentoEstruturadoTopico criarProcessoDocumentoEstruturadoTopico(EstruturaDocumentoTopico documentoTopico, ProcessoDocumentoEstruturado documentoEstruturado) throws LinguagemFormalException {
		ProcessoDocumentoEstruturadoTopico documentoEstruturadoTopico = new ProcessoDocumentoEstruturadoTopico();

		documentoEstruturadoTopico.setTitulo(documentoTopico.getTopico().getTituloPadrao());
		documentoEstruturadoTopico.setConteudo(estruturaDocumentoTopicoMagistradoManager.getConteudoPadrao(documentoTopico, documentoEstruturado.getMagistrado()));
		
		documentoEstruturadoTopico.setPessoa(Authenticator.getPessoaLogada());
		documentoEstruturadoTopico.setNivel(documentoTopico.getNivel());
		documentoEstruturadoTopico.setOrdem(documentoTopico.getOrdem());
		documentoEstruturadoTopico.setNumeracao(documentoTopico.getNumeracao());
		documentoEstruturadoTopico.setEstruturaDocumentoTopico(documentoTopico);
		documentoEstruturadoTopico.setTopico(documentoTopico.getTopico());
		documentoEstruturadoTopico.setDataModificacao(new Date());
		documentoEstruturadoTopico.setProcessoDocumentoEstruturado(documentoEstruturado);
		documentoEstruturadoTopico.setHabilitado(documentoTopico.getTopico().isHabilitado());
		documentoEstruturadoTopico.setNumerado(documentoTopico.isNumerado());
		documentoEstruturadoTopico.setExibirTitulo(documentoTopico.getTopico().isExibirTitulo());
		return documentoEstruturadoTopico;
	}

	public ProcessoDocumentoEstruturadoTopico criarProcessoDocumentoEstruturadoItemTopico(ProcessoDocumentoEstruturadoTopico bloco) throws LinguagemFormalException {
		ProcessoDocumentoEstruturadoTopico processoItemTopico = new ProcessoDocumentoEstruturadoTopico();
		processoItemTopico.setTopico(bloco.getTopico().getItemTopico());
		processoItemTopico.setTitulo(processoItemTopico.getTopico().getTituloPadrao());
		processoItemTopico.setConteudo(getConteudoPadraoItem(processoItemTopico));
		processoItemTopico.setPessoa(Authenticator.getPessoaLogada());
		processoItemTopico.setNivel(bloco.getNivel() + 1);
		processoItemTopico.setDataModificacao(new Date());
		processoItemTopico.setProcessoDocumentoEstruturado(bloco.getProcessoDocumentoEstruturado());
		processoItemTopico.setProcessoDocumentoEstruturadoBloco(bloco);
		processoItemTopico.setNumerado(bloco.isNumerado());
		processoItemTopico.setHabilitado(bloco.isHabilitado());
		processoItemTopico.setExibirTitulo(bloco.getTopico().getItemTopico().isExibirTitulo());
		return processoItemTopico;
	}
	
	public ProcessoDocumentoEstruturadoTopico criarProcessoDocumentoEstruturadoItemTopico(ProcessoDocumentoEstruturadoTopico bloco, Topico topico) throws LinguagemFormalException {
		ProcessoDocumentoEstruturadoTopico processoItemTopico = new ProcessoDocumentoEstruturadoTopico();
		processoItemTopico.setTopico(topico);
		processoItemTopico.setTitulo(processoItemTopico.getTopico().getTituloPadrao());
		processoItemTopico.setConteudo(getConteudoPadraoItem(processoItemTopico));
		processoItemTopico.setPessoa(Authenticator.getPessoaLogada());
		processoItemTopico.setNivel(bloco.getNivel() + 1);
		processoItemTopico.setDataModificacao(new Date());
		processoItemTopico.setProcessoDocumentoEstruturado(bloco.getProcessoDocumentoEstruturado());
		processoItemTopico.setProcessoDocumentoEstruturadoBloco(bloco);
		processoItemTopico.setNumerado(bloco.isNumerado());
		processoItemTopico.setHabilitado(bloco.isHabilitado());
		processoItemTopico.setExibirTitulo(topico.isExibirTitulo());
		return processoItemTopico;
	}
	
	public ProcessoDocumentoEstruturadoTopico criarProcessoDocumentoEstruturadoConclusaoTopico(ProcessoDocumentoEstruturadoTopico bloco) throws LinguagemFormalException {
		ProcessoDocumentoEstruturadoTopico processoItemTopico = new ProcessoDocumentoEstruturadoTopico();
		processoItemTopico.setTopico(((ITopicoComConclusao<?>)bloco.getTopico()).getConclusaoTopico());
		processoItemTopico.setTitulo(processoItemTopico.getTopico().getTituloPadrao());
		processoItemTopico.setConteudo(getConteudoPadraoItem(processoItemTopico));
		processoItemTopico.setPessoa(Authenticator.getPessoaLogada());
		processoItemTopico.setNivel(bloco.getNivel() + 1);
		processoItemTopico.setDataModificacao(new Date());
		processoItemTopico.setProcessoDocumentoEstruturado(bloco.getProcessoDocumentoEstruturado());
		processoItemTopico.setProcessoDocumentoEstruturadoBloco(bloco);
		processoItemTopico.setNumerado(bloco.isNumerado());
		processoItemTopico.setHabilitado(bloco.isHabilitado());
		processoItemTopico.setExibirTitulo(processoItemTopico.getTopico().isExibirTitulo());
		return processoItemTopico;
	}
	
	public void persistirTopicos(ProcessoDocumentoEstruturado docEstruturado) {
		List<ProcessoDocumentoEstruturadoTopico> topicoList = docEstruturado.getProcessoDocumentoEstruturadoTopicoList();
		int ordemAux = 0; 
		for (ProcessoDocumentoEstruturadoTopico pdTopico : topicoList) {
			if(pdTopico.getOrdem() != null){
				ordemAux = pdTopico.getOrdem();
			}else {
				ordemAux+=1;
				pdTopico.setOrdem(ordemAux);
			}
			pdTopico.setProcessoDocumentoEstruturado(docEstruturado);
			persistirTopico(pdTopico);
		}
	}
	
	public void persistirTopico(ProcessoDocumentoEstruturadoTopico topico) {
		topico.setDataModificacao(new Date());
		atualizarSHA1(topico);
		if (topico.getTopico().getIdTopico() == null) {
			persist(topico.getTopico());
		}
		persist(topico);
	}

	private String getConteudoPadraoItem(ProcessoDocumentoEstruturadoTopico processoItemTopico) {
		return processoItemTopico.getTopico().getConteudoPadrao() != null ? processoItemTopico.getTopico().getConteudoPadrao() : "";
	}

	public void atualizarSHA1(ProcessoDocumentoEstruturadoTopico documentoEstruturadoTopico) {
		documentoEstruturadoTopico.setSha1Conteudo(Crypto.encode(documentoEstruturadoTopico.getConteudo(), Type.SHA1));
		documentoEstruturadoTopico.setSha1Titulo(Crypto.encode(documentoEstruturadoTopico.getTitulo(), Type.SHA1));
	}

	public boolean podeEditarConteudo(ProcessoDocumentoEstruturadoTopico processoTopico) {
		return !processoTopico.getTopico().isSomenteLeitura();
	}

	public boolean podeEditarTitulo(ProcessoDocumentoEstruturadoTopico processoTopico) {
		return processoTopico.isExibirTitulo();
	}
	
	public void recarregarConteudoTopico(ProcessoDocumentoEstruturadoTopico processoTopico) throws LinguagemFormalException  {
		EstruturaDocumentoTopico estruturaPadrao = processoTopico.getEstruturaDocumentoTopico();
		String conteudo;
		if (processoTopico.getTopico().getItemTopico() != null) {
			conteudo = estruturaDocumentoTopicoMagistradoManager.getConteudoPadrao(estruturaPadrao, processoTopico.getProcessoDocumentoEstruturado().getMagistrado());
		} else {
			conteudo = getConteudoPadraoItem(processoTopico);
		}
		processoTopico.setConteudo(processaModeloService.processaVariaveisModelo(conteudo));
	}
	
	public void removerTopicos(ProcessoDocumentoEstruturado docEstruturado) {
		List<ProcessoDocumentoEstruturadoTopico> topicoList = docEstruturado.getProcessoDocumentoEstruturadoTopicoListAtivosEInativos();
		if (topicoList != null){
			for (ProcessoDocumentoEstruturadoTopico pdTopico : topicoList) {
				removerTopico(pdTopico);
			}
		}
	}
	
	public void removerTopico(ProcessoDocumentoEstruturadoTopico pdTopico){
		removerHistoricoDoTopico(pdTopico);
		EntityUtil.getEntityManager().remove(pdTopico);
	
	}
	public void removerHistoricoDoTopico(ProcessoDocumentoEstruturadoTopico pdTopico){
        EntityManager entityManager = EntityUtil.getEntityManager();
        Query q = entityManager.createQuery("delete from HistoricoProcessoDocumentoEstruturadoTopico where processoDocumentoEstruturadoTopico = :topico");
		q.setParameter("topico", pdTopico);
        q.executeUpdate();
	}


}
