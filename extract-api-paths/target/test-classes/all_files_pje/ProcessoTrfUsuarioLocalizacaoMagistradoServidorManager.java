package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoTrfUsuarioLocalizacaoMagistradoServidorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfUsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name("processoTrfUsuarioLocalizacaoMagistradoServidorManager")
public class ProcessoTrfUsuarioLocalizacaoMagistradoServidorManager extends BaseManager<ProcessoTrfUsuarioLocalizacaoMagistradoServidor>{

	public static final String NAME = "processoTrfUsuarioLocalizacaoMagistradoServidorManager";

	@In
	private ProcessoTrfUsuarioLocalizacaoMagistradoServidorDAO processoTrfUsuarioLocalizacaoMagistradoServidorDAO;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@Override
	protected ProcessoTrfUsuarioLocalizacaoMagistradoServidorDAO getDAO() {
		return processoTrfUsuarioLocalizacaoMagistradoServidorDAO;
	}

	public void addProcessoTrfMagistradoAuxiliar(ProcessoTrf obj,UsuarioLocalizacaoMagistradoServidor usuLocMagistradoServidor) 
				throws PJeBusinessException, NoSuchFieldException {
		if(isMagistradoVinculadoAoProcesso(obj,usuLocMagistradoServidor)){
			throw new PJeBusinessException("Magistrado já vinculado");
		}
		
		ProcessoTrfUsuarioLocalizacaoMagistradoServidor processoTrfMagistrado = 
				new ProcessoTrfUsuarioLocalizacaoMagistradoServidor();
		processoTrfMagistrado.setProcessoTrf(obj);
		processoTrfMagistrado.setUsuarioLocalizacaoMagistradoServidor(usuLocMagistradoServidor);
		usuLocMagistradoServidor.getProcessoMagistradoList().add(processoTrfMagistrado);
		getDAO().persist(processoTrfMagistrado);
		getDAO().flush();
	}

	private boolean isMagistradoVinculadoAoProcesso(ProcessoTrf obj,
			UsuarioLocalizacaoMagistradoServidor usuLocMagistradoServidor) throws NoSuchFieldException {
		return getDAO().buscaMagistradoNoProcesso(usuLocMagistradoServidor, obj).size() > 0;
	}

	public List<UsuarioLocalizacaoMagistradoServidor> getMagistradosVinculados(ProcessoTrf processoJudicial) {
		return getDAO().getMagistradosVinculados(processoJudicial);
	}

	public void remover(UsuarioLocalizacaoMagistradoServidor magistrado,ProcessoTrf processoJudicial) throws NoSuchFieldException {
		List<ProcessoTrfUsuarioLocalizacaoMagistradoServidor> processoMagistrados =
				getDAO().buscaMagistradoNoProcesso(magistrado,processoJudicial);
		for(ProcessoTrfUsuarioLocalizacaoMagistradoServidor p: processoMagistrados){
			getDAO().remove(p);
		}
		getDAO().flush();
		
	}	
	
	public void vincularMagistradoProcesso(ProcessoTrf processoTrf, UsuarioLocalizacaoMagistradoServidor usuLocMagistradoServidor) 
			throws PJeBusinessException, NoSuchFieldException {
		
		Boolean alteraRelator = (Boolean)ComponentUtil.getComponent(TramitacaoProcessualImpl.class)
				.recuperaVariavelTarefa("pje:vincularMagistrado:alteraRelator");
		
		if (alteraRelator == null || Boolean.TRUE.equals(alteraRelator)) {
			processoTrf.setPessoaRelator(pessoaMagistradoManager.findByLogin(
					usuLocMagistradoServidor.getUsuarioLocalizacao().getUsuario().getLogin()));
		}
		
		processoJudicialService.deslocarProcessoParaLocalizacaoDeJuiz(processoTrf, usuLocMagistradoServidor);
	}
}