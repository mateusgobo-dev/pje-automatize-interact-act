package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.business.dao.ProcessoMagistradoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SubstituicaoMagistradoManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name(ProcessoMagistradoService.NAME)
public class ProcessoMagistradoService extends BaseService implements Serializable{
	
	public static final String NAME = "processoMagistradoService";

	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log log;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;
	
	@In
	private SubstituicaoMagistradoManager substituicaoMagistradoManager; 
	
	@In
	private ProcessoMagistradoManager processoMagistradoManager;
	
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private OrgaoJulgadorCargoManager orgaoJulgadorCargoManager; 
	
	@In
	private ProcessoMagistradoDAO processoMagistradoDAO;
	
	/**
	 * Registra a vinculação do relator ao processo caso ela ainda não
	 * esteja definida, além de deslocar o processo para o OrgaoJulgador e
	 * OrgaoJulgador cargo configurado na localização de vinculação do
	 * magistrado.
	 * 
	 * Caso a localização do magistrado seja uma localização de juiz substituto
	 * em vigência essa será registrada na vinculação
	 *
	 * @see #registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf,
	 *      UsuarioLocalizacaoMagistradoServidor, boolean,
	 *      SubstituicaoMagistrado)
	 * 
	 * @param processoTrf
	 * @param localizacaoDoRelator
	 *            localização do magistrado relator que está se vinculando. Será
	 *            utilizado o atributo orgaoJulgadorCargoVinculacao para
	 *            determinar o destino, portanto ele deve estar setado nas
	 *            configurações da localização. true caso a data tenha sido
	 *            definida false caso não haja relator ou a data já esteja
	 *            definida
	 * 
	 * @return <code>true</code> caso a data tenha sido definida ou
	 *         <code>false</code> caso não haja relator ou a data já esteja
	 *         definida
	 * 
	 * @throws PJeBusinessException
	 */
	public boolean registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf processoTrf,
			UsuarioLocalizacaoMagistradoServidor localizacaoDoRelator) throws PJeBusinessException {		
		SubstituicaoMagistrado substituicaoVigente = substituicaoMagistradoManager
				.obterSubstituicaoMagistradoVigente(processoTrf.getOrgaoJulgador(), processoTrf.getOrgaoJulgadorColegiado(), localizacaoDoRelator);
		return registrarVinculacaoDoRelatorAoProcesso(processoTrf, localizacaoDoRelator, (substituicaoVigente == null),
				substituicaoVigente);
	}
	
	/**
	 * 
	 * Registra a vinculação do relator ao processo caso ela ainda não esteja
	 * definida, além de deslocar o processo para o OrgaoJulgador e
	 * OrgaoJulgador cargo configurado na localização de vinculação do
	 * magistrado.
	 * 
	 * Neste método não será identificada uma substituição vigente, devendo o
	 * invocador informá-la ou informar null caso a vinculação não seja
	 * referente a um período de substituição
	 * 
	 * @see #registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf,
	 *      UsuarioLocalizacaoMagistradoServidor, boolean,
	 *      SubstituicaoMagistrado)
	 * 
	 * @param processoTrf
	 * @param localizacaoDoRelator
	 *            localização do magistrado relator que está se vinculando. Será
	 *            utilizado o atributo orgaoJulgadorCargoVinculacao para
	 *            determinar o destino, portanto ele deve estar setado nas
	 *            configurações da localização. true caso a data tenha sido
	 *            definida false caso não haja relator ou a data já esteja
	 *            definida
	 * 
	 * @param substituicaoVigente
	 *            {@link SubstituicaoMagistrado} que representa que o magistrado
	 *            informado está atuando como substituto do titular do OJ. Pode
	 *            ser nulo caso não seja substituto.
	 * 
	 * @return <code>true</code> caso a data tenha sido definida ou
	 *         <code>false</code> caso não haja relator ou a data já esteja
	 *         definida
	 * 
	 * @throws PJeBusinessException
	 * 
	 * 
	 */
	public boolean registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf processoTrf,
			UsuarioLocalizacaoMagistradoServidor localizacaoDoRelator, SubstituicaoMagistrado substituicaoVigente)
			throws PJeBusinessException {	
		return registrarVinculacaoDoRelatorAoProcesso(processoTrf, localizacaoDoRelator, (substituicaoVigente == null),
				substituicaoVigente);	
	}
	
	/**
	 * 
	 * Registra a vinculação do relator ao processo caso ela ainda não esteja
	 * definida, além de deslocar o processo para o OrgaoJulgador e
	 * OrgaoJulgador cargo configurado na localização de vinculação do
	 * magistrado.
	 * 
	 * Este método é usado caso haja a necessidade de se informar que se trata
	 * de uma vinculação de juiz substituto mas não se tenha o período de
	 * substituição para ser associado ao vinculo. Neste caso será apenas
	 * inserida uma marcação no vínculo. 
	 * 
	 * @see #registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf,
	 *      UsuarioLocalizacaoMagistradoServidor, boolean,
	 *      SubstituicaoMagistrado)
	 * 
	 * @param processoTrf
	 * @param localizacaoDoRelator
	 *            localização do magistrado relator que está se vinculando. Será
	 *            utilizado o atributo orgaoJulgadorCargoVinculacao para
	 *            determinar o destino, portanto ele deve estar setado nas
	 *            configurações da localização. true caso a data tenha sido
	 *            definida false caso não haja relator ou a data já esteja
	 *            definida
	 * 
	 * @param isVinculoMagistradoTitular
	 *            <code>true</code> caso trata-se de magistrado titular do OJ
	 *            informado ou <code>false</code>caso o magistrado informado
	 *            seja um substituto
	 * 
	 * @return <code>true</code> caso a data tenha sido definida ou
	 *         <code>false</code> caso não haja relator ou a data já esteja
	 *         definida
	 * 
	 * @throws PJeBusinessException
	 * 
	 */
	public boolean registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf processoTrf,
			UsuarioLocalizacaoMagistradoServidor localizacaoDoRelator, boolean isVinculoMagistradoTitular)
			throws PJeBusinessException {	
		return registrarVinculacaoDoRelatorAoProcesso(processoTrf, localizacaoDoRelator, isVinculoMagistradoTitular, null);	
	}
	
	/**
	 * Registra a vinculação do relator ao processo caso ela ainda não
	 * esteja definida, além de deslocar o processo para o OrgaoJulgador e
	 * OrgaoJulgador cargo configurado na localização de vinculação do
	 * magistrado.
	 * 
	 * @see ProcessoMagistradoManager#registrarVinculacaoRegimentalRelatorProcesso(ProcessoTrf,
	 *      PessoaMagistrado, OrgaoJulgadorColegiado, OrgaoJulgador,
	 *      OrgaoJulgadorCargo, boolean, SubstituicaoMagistrado)
	 * 
	 * @param processoTrf
	 * @param localizacaoDoRelator
	 *            localização do magistrado relator que está se vinculando. Será
	 *            utilizado o atributo orgaoJulgadorCargoVinculacao para
	 *            determinar o destino, portanto ele deve estar setado nas
	 *            configurações da localização. true caso a data tenha sido
	 *            definida false caso não haja relator ou a data já esteja
	 *            definida
	 * 
	 * @param isVinculoMagistradoTitular
	 *            <code>true</code> caso trata-se de magistrado titular do OJ
	 *            informado ou <code>false</code>caso o magistrado informado
	 *            seja um substituto
	 * 
	 * @param substituicaoMagistrado
	 *            {@link SubstituicaoMagistrado} que representa que o magistrado
	 *            informado está atuando como substituto do titular do OJ. Pode
	 *            ser nulo caso não seja substituto.
	 * 
	 * @return <code>true</code> caso a data tenha sido definida ou
	 *         <code>false</code> caso não haja relator ou a data já esteja
	 *         definida
	 * 
	 * @throws PJeBusinessException
	 */
	private boolean registrarVinculacaoDoRelatorAoProcesso(ProcessoTrf processoTrf,
			UsuarioLocalizacaoMagistradoServidor localizacaoDoRelator, boolean isVinculoMagistradoTitular,
			SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException {
		
		if (localizacaoDoRelator.getOrgaoJulgadorCargoVinculacao() == null) {
			throw new PJeBusinessException("processoMagistrado.validacao.naoExisteCargoVinculacao");
		}
		
		PessoaMagistrado magistrado = pessoaMagistradoManager
				.findById(localizacaoDoRelator.getUsuarioLocalizacao().getUsuario().getIdUsuario());
		
		ProcessoMagistrado vinculacao = processoMagistradoManager.registrarVinculacaoRegimentalRelatorProcesso(
				processoTrf, magistrado, 
				localizacaoDoRelator.getOrgaoJulgadorColegiado(),
				localizacaoDoRelator.getOrgaoJulgadorCargoVinculacao().getOrgaoJulgador(), 
				localizacaoDoRelator.getOrgaoJulgadorCargoVinculacao(),
				isVinculoMagistradoTitular,
				substituicaoMagistrado);
		
		if (vinculacao != null) {
			deslocarOrgaoJulgador(processoTrf, 
					localizacaoDoRelator.getOrgaoJulgadorCargoVinculacao().getOrgaoJulgador().getIdOrgaoJulgador(),
					localizacaoDoRelator.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(),
					localizacaoDoRelator.getOrgaoJulgadorCargoVinculacao().getIdOrgaoJulgadorCargo());						
			return true;
		}
		
		return false;		
	}	
	
	/**
	 * Registra a vinculação do revisor ao processo caso ainda não esteja
	 * definida. É preciso que um revisor já esteja definido também.
	 * 
	 * O método ainda identifica se o revisor está em um período de substituição
	 * vigente, associando este ao vínculo.
	 * 
	 * @param processoTrf
	 * @param localizacaoDoRevisor
	 *            - localização do magistrado revisor. informar caso haja troca
	 *            do revisor que está se vinculando. Neste caso será setado o OJ
	 *            definido em orgaoJulgadorCargoVinculacao
	 * 
	 * @see {@link ProcessoMagistradoManager#registrarVinculacaoRegimentalRevisorProcesso(ProcessoTrf, PessoaMagistrado, OrgaoJulgadorColegiado, OrgaoJulgador, OrgaoJulgadorCargo, SubstituicaoMagistrado)}
	 * 
	 * @return true caso a vinculação tenha sido feita false caso não haja
	 *         revisor ou a vinculação já esteja definida
	 * 
	 * @throws PJeBusinessException
	 *             Caso não tenha sido configurado na localização o cargo de
	 *             vinculação
	 *             {@link UsuarioLocalizacaoMagistradoServidor#getOrgaoJulgadorCargoVinculacao()}
	 */
	public boolean registrarVinculacaoDoRevisorAoProcesso(ProcessoTrf processoTrf, 
			UsuarioLocalizacaoMagistradoServidor localizacaoDoRevisor) throws PJeBusinessException {
				
		if (localizacaoDoRevisor.getOrgaoJulgadorCargoVinculacao() == null) {
			throw new PJeBusinessException("processoMagistrado.validacao.naoExisteCargoVinculacao");
		}
		
		if (processoTrf.getOrgaoJulgadorRevisor() == null) {
			throw new PJeBusinessException("processoMagistrado.validacao.processoSemRevisor");
		}
		
		OrgaoJulgador OJRevisorParaVinculacao = localizacaoDoRevisor.getOrgaoJulgadorCargoVinculacao().getOrgaoJulgador();						
		if (OJRevisorParaVinculacao != null && !processoTrf.getOrgaoJulgadorRevisor().equals(OJRevisorParaVinculacao)) {
			processoTrf.setOrgaoJulgadorRevisor(OJRevisorParaVinculacao);
		}
		
		SubstituicaoMagistrado substituicaoVigente = substituicaoMagistradoManager
				.obterSubstituicaoMagistradoVigente(processoTrf.getOrgaoJulgadorRevisor(), processoTrf.getOrgaoJulgadorColegiado(), localizacaoDoRevisor);

		PessoaMagistrado magistrado = pessoaMagistradoManager
				.findById(localizacaoDoRevisor.getUsuarioLocalizacao().getUsuario().getIdUsuario());
		
		ProcessoMagistrado vinculacao = processoMagistradoManager.registrarVinculacaoRegimentalRevisorProcesso(
				processoTrf, magistrado, 
				localizacaoDoRevisor.getOrgaoJulgadorColegiado(),
				localizacaoDoRevisor.getOrgaoJulgadorCargoVinculacao().getOrgaoJulgador(), 
				localizacaoDoRevisor.getOrgaoJulgadorCargoVinculacao(),
				(substituicaoVigente == null),
				substituicaoVigente);
		
		processoJudicialManager.persistAndFlush(processoTrf);
		
		return (vinculacao != null);
	}
	
	/**
	 * Método que registra um vínculo de reserva do processo, realizando seu
	 * deslocamento para uma nova localização.
	 * 
	 * @param processoJudicial processo a ser deslocado
	 * @param localizacaoMagistrado localização/perfil do magistrado que está reservando o processo
	 */
	public void registrarVinculacaoReservaDoRelatorAoProcesso(ProcessoTrf processoJudicial,
			SubstituicaoMagistrado substituicaoReferencia)
			throws PJeBusinessException {
				
		
		PessoaMagistrado magistrado = substituicaoReferencia.getMagistradoSubstituto();
		
		processoMagistradoManager.registrarVinculacaoReservaRelatorProcesso(
				processoJudicial, magistrado, 
				substituicaoReferencia.getOrgaoJulgadorColegiado(),
				substituicaoReferencia.getOrgaoJulgador(),
				substituicaoReferencia.getCargoMagistradoSubstituto(),
				substituicaoReferencia);
						
		deslocarOrgaoJulgador(processoJudicial, 
				substituicaoReferencia.getOrgaoJulgador().getIdOrgaoJulgador(),
				substituicaoReferencia.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(),
				substituicaoReferencia.getCargoMagistradoSubstituto().getIdOrgaoJulgadorCargo());
		
	}	
	
	/**
	 * Método responsável por remover uma reserva realizada por um magistrado,
	 * retornando o processo para o titular do OJ onde processo se encontra
	 * 
	 * @param reservaRelator
	 * @throws PJeBusinessException
	 */
	public void removerVinculacaoReservaDoRelator(ProcessoMagistrado reservaRelator) throws PJeBusinessException {
				
		processoMagistradoManager.remove(reservaRelator);
		
		ProcessoTrf processoJudicial = reservaRelator.getProcesso();
		OrgaoJulgadorCargo ojCargo = orgaoJulgadorManager.recuperarCargoResponsavel(
				processoJudicial.getOrgaoJulgador(),
				processoJudicial.getOrgaoJulgadorColegiado(), new Date());
		
		deslocarOrgaoJulgador(processoJudicial, 
				processoJudicial.getOrgaoJulgador().getIdOrgaoJulgador(),
				processoJudicial.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(),
				ojCargo.getIdOrgaoJulgadorCargo());
	}
	
	/**
	 * Registra a vinculação de relator designado ao processo e também desloca a
	 * localização do processo para o magistrado/gabinete que está se
	 * vinculando.
	 * 
	 * Utilizado, por exemplo, quando um magistrado diferente do relator do
	 * processo vence o julgamento e o processo precisa ser deslocado para seu
	 * gabinete.
	 * 
	 * @see ProcessoMagistradoManager#registrarVinculacaoRelatorDesignado(ProcessoTrf,
	 *      PessoaMagistrado, OrgaoJulgadorColegiado, OrgaoJulgador,
	 *      OrgaoJulgadorCargo, boolean)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} que terá a vinculação registrada
	 * 
	 * @param idMagistrado
	 *            Id de usuário do magistrado que será vinculado como relator
	 *            designado
	 * 
	 * @param idOjCargoVinculacao
	 *            {@link OrgaoJulgadorCargo} do magistrado no OJ
	 * 
	 * @throws PJeBusinessException
	 *             Nos casos em que os parâmetro obrigatórios não são
	 *             informados. Outros casos em
	 *             {@link ProcessoMagistradoManager#registrarVinculacaoRelatorDesignado(ProcessoTrf, PessoaMagistrado, OrgaoJulgadorColegiado, OrgaoJulgador, OrgaoJulgadorCargo, boolean)}
	 */
	public boolean registrarVinculacaoDoRelatorDesignado(ProcessoTrf processoTrf, Integer idMagistrado,
			Integer idOjCargoVinculacao) throws PJeBusinessException {
				
		PessoaMagistrado magistrado = pessoaMagistradoManager.findById(idMagistrado);		
		if (magistrado == null) {			
			throw new PJeBusinessException("processoMagistrado.validacao.magistradoNaoInformado");
		}
		
		OrgaoJulgadorCargo ojCargo = orgaoJulgadorCargoManager.findById(idOjCargoVinculacao);
		if (ojCargo == null) {
			throw new PJeBusinessException("processoMagistrado.validacao.cargoVinculacaoNaoInformado");
		}		
		
		ProcessoMagistrado vinculacao = processoMagistradoManager.registrarVinculacaoRelatorDesignado(
				processoTrf, magistrado, 
				processoTrf.getOrgaoJulgadorColegiado(),
				ojCargo.getOrgaoJulgador(), 
				ojCargo,
				!ojCargo.getAuxiliar());
		
		if (vinculacao != null) {
			deslocarOrgaoJulgador(processoTrf,
					vinculacao.getOrgaoJulgador().getIdOrgaoJulgador(),
					vinculacao.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(),
					vinculacao.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());										
			return true;
		}
		
		return false;
	}
	
	/**
	 * Método responsável por recuperar processos que estão no OJ e OJc do
	 * magistrado afastado
	 * {@link SubstituicaoMagistrado#getPerfilMagistradoAfastado()} e que podem
	 * ser resevados pelo magistrado substituto
	 * {@link SubstituicaoMagistrado#getPerfilMagistradoSubstituto()}
	 * 
	 * @param substituicao
	 * @return {@link List} de {@link ProcessoTrf} que podem ser reservados.
	 */
	public List<ProcessoTrf> obterProcessosParaVinculacaoReserva(SubstituicaoMagistrado substituicao) {
		List<ProcessoTrf> processosParaReserva = processoMagistradoDAO.obterProcessosSemVinculacaoRelator(
				substituicao.getOrgaoJulgador(),
				substituicao.getOrgaoJulgadorColegiado());
		return processosParaReserva;
	}
	
	/**
	 * Método responsável por realizar o deslocamento da responsável pelo
	 * processo
	 */
	private void deslocarOrgaoJulgador(ProcessoTrf processoJudicial, Integer idOrgaoDestino,
			Integer idOrgaoJulgadorColegiado, Integer idOrgaoJulgadorCargo) throws PJeBusinessException {

		//Retirar da caixa
		processoJudicial.getProcesso().setCaixa(null);
		
		processoJudicialService.deslocarOrgaoJulgador(processoJudicial, 
				idOrgaoDestino,
				idOrgaoJulgadorColegiado,
				idOrgaoJulgadorCargo);						
	}

}
