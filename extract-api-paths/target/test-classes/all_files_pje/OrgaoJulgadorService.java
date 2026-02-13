package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("orgaoJulgadorService")
@Transactional
@Scope(ScopeType.EVENT)
public class OrgaoJulgadorService {

	@In(create = true)
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In(create = true)
	private OrgaoJulgadorCargoManager orgaoJulgadorCargoManager;
	
	@In(create = true)
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	@In(create = true)
	private ProcessoJudicialManager processoJudicialManager;
	
	public OrgaoJulgador findById(Integer id) throws PJeBusinessException{
		return orgaoJulgadorManager.findById(id);
	}

	public List<OrgaoJulgador> findAll() {
		// [PJEII-663] Prevenir NullPointerException em componentes marcados com BypassInterceptors
		if (orgaoJulgadorManager == null) {
			orgaoJulgadorManager = new OrgaoJulgadorManager();
		}
		return orgaoJulgadorManager.findAll();
	}

	/**
	 * [PJEII-663] Retorna o órgão julgador relacionado à localização informada
	 * @param localizacao
	 * @return orgaoJulgador
	 */
	public OrgaoJulgador getOrgaoJulgadorByLocalizacao(Localizacao localizacao){
		if (orgaoJulgadorManager == null) {
			orgaoJulgadorManager = new OrgaoJulgadorManager();
		}
		return orgaoJulgadorManager.getOrgaoJulgadorByLocalizacao(localizacao);
	}

	/**
	 * [PJEII-663] Retorna os órgãos julgadores de uma jurisdição
	 * @param jurisdicao
	 * @return lista de órgãos julgadores
	 */
	public List<OrgaoJulgador> findAllbyJurisdicao(Jurisdicao jurisdicao) {
		if (orgaoJulgadorManager == null) {
			orgaoJulgadorManager = new OrgaoJulgadorManager();
		}
		return orgaoJulgadorManager.findAllbyJurisdicao(jurisdicao);
	}

	/**
	 * Método responsável por recuperar os dados do magistrado (titular ou substituto) associado ao órgão julgador especificado
	 * @param orgaoJulgador Dados do órgão julgador
	 * @return Magistrado (titular ou substituto) associado ao órgão julgador especificado
	 */
	public Usuario getMagistradoOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		OrgaoJulgadorCargo orgaoJulgadorCargo = orgaoJulgadorCargoManager.getOrgaoJulgadorCargoEmExercicio(orgaoJulgador);
		if (orgaoJulgadorCargo != null) {
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = 
					usuarioLocalizacaoMagistradoServidorManager.getMagistradoPorCargo(orgaoJulgadorCargo);
			
			if (usuarioLocalizacaoMagistradoServidor != null) {
				return usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().getUsuario();
			}
		}
		return null;
	}
	
	/**
	 * Método responsável por obter a lista de órgãos julgadores dos magistrados
	 * que participaram em determinado processo. O órgão julgador em questão é o
	 * que o magistrado está cadastrado atualmente como titular.
	 * 
	 * Basicamente faz a busca reunindo a lista de Órgãos Julgadores de três regras de negócio: 
	 * 	- Próprio Órgão Julgador do Relator do Processo; 
	 * 	- Do Órgão Julgador do Relator busca quais os magistrados são 
	 * 		auxiliares/substitutos, para então buscar os Órgãos Julgadores 
	 * 		em que os auxiliares estão atualmente cadastrados como titular;
	 * 	- Dos magistrados que emitiram algum documento no processo busca-se em 
	 * 		qual Órgão Julgador ele atua como titular.
	 * 
	 * @see #obterOrgaoJulgadorTitularDosAuxiliares(OrgaoJulgador, OrgaoJulgadorColegiado)
	 * @see #obterOrgaosJulgadoresDosAtuantes(ProcessoTrf, boolean)
	 * 
	 * @param processo
	 *            Processo que será usado como base de pesquisa.
	 *            
	 * @param filtrarPorColegiado
	 *            Indicador para trazer apenas os Órgãos Julgadores que fazem
	 *            parte do mesmo Órgão Colegiado do Processo.
	 *            
	 * @return
	 * 		Lista de Órgãos Relacionados ao Processo sem duplicação.
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresRelacionados(
			ProcessoTrf processo, boolean filtrarPorColegiado) {
		
		Set<OrgaoJulgador> orgaosRelacionados = new HashSet<OrgaoJulgador>();
		
		if (processo != null) {
		
			OrgaoJulgadorColegiado ojColegiado = null;			
			if (filtrarPorColegiado) {
				ojColegiado = processo.getOrgaoJulgadorColegiado();
			}

			// OJ do Relator do Processo
			OrgaoJulgador ojRelator = processo.getOrgaoJulgador();			
			orgaosRelacionados.add(ojRelator);
			
			// OJ's onde os auxiliares são titular
			List<OrgaoJulgador> ojAuxiliares = obterOrgaoJulgadorTitularDosAuxiliares(ojRelator, ojColegiado);
			orgaosRelacionados.addAll(ojAuxiliares);
			
			// OJ's onde os atuantes no processo são titular			
			List<OrgaoJulgador> ojDosAtuantes = obterOrgaosJulgadoresDosAtuantes(processo, filtrarPorColegiado);
			orgaosRelacionados.addAll(ojDosAtuantes);
			
		}
		
		return new ArrayList<OrgaoJulgador>(orgaosRelacionados);
		
	}

	/**
	 * Método responsável por obter a lista dos Órgãos Julgadores em que os
	 * magistrados auxiliares/substitutos atuantes do Órgão Julgador informado
	 * no parâmetro atuam como titulares.
	 * 
	 * @see UsuarioLocalizacaoMagistradoServidorManager#getMagistradosAuxiliares(OrgaoJulgador)
	 * 
	 * @param orgaoJulgador
	 *            Órgão Julgador que possui os magistrados atuantes como
	 *            auxiliares/substitutos. *
	 * 
	 * @param orgaoJulgadorColegiado
	 *            Filtro para retornar apenas os Órgãos Julgadores de
	 *            determinado Órgão Colegiado. Pode ser nulo.
	 * 
	 * @return Lista de Orgãos Julgares em que os auxiliares/subtituos atuam
	 *         como titulares.
	 */
	public List<OrgaoJulgador> obterOrgaoJulgadorTitularDosAuxiliares(
			OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		List<OrgaoJulgador> orgaosTitularDosAuxiliares = new ArrayList<OrgaoJulgador>();
		
		List<UsuarioLocalizacaoMagistradoServidor> magistradosAuxiliares = usuarioLocalizacaoMagistradoServidorManager
				.getMagistradosAuxiliares(orgaoJulgador);
		
		if (magistradosAuxiliares != null) {
			for (UsuarioLocalizacaoMagistradoServidor magistradoAuxiliar : magistradosAuxiliares) {
				orgaosTitularDosAuxiliares.addAll(obterOrgaosJulgadoresResponsavel(
						magistradoAuxiliar.getUsuarioLocalizacao().getUsuario().getIdUsuario(), 
						orgaoJulgadorColegiado));
			}
		}
		
		return orgaosTitularDosAuxiliares;		
	}
	
	
	/**
	 * Método responsável por obeter o Órgão Julgador em que o usuário
	 * (magistrado), atua como titular.
	 * 
	 * @see UsuarioLocalizacaoMagistradoServidorManager#recuperaLocalizacaoDeOrgaosResponsavel(Integer,
	 *      OrgaoJulgadorColegiado)
	 * 
	 * @param idUsuario
	 *            id de usuário do magistrado.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            Filtro para obter apenas Órgãos de determinado Órgão Colegiado
	 * 
	 * @return Lista com os Órgãos Julgadores em que o Magistrado atua como
	 *         titular.
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresResponsavel(
			Integer idUsuario, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		List<OrgaoJulgador> orgaosTitular = new ArrayList<OrgaoJulgador>();
		
		if (idUsuario != null) {
			List<UsuarioLocalizacaoMagistradoServidor> localizacaoDeOrgaosResponsavel = usuarioLocalizacaoMagistradoServidorManager
					.recuperaLocalizacaoDeOrgaosResponsavel(
							idUsuario, 
							orgaoJulgadorColegiado);
			
			for (UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor : localizacaoDeOrgaosResponsavel) {
				orgaosTitular.add(usuarioLocalizacaoMagistradoServidor
						.getOrgaoJulgador());
			}			
		}
		
		return orgaosTitular;		
	}
	
	/**
	 * Método responsável por recuperar a lista de Órgãos Julgadores dos
	 * magistrados titulares que realizaram algum ato (assinaram documento) no
	 * processo.
	 * 
	 * @see ProcessoJudicialManager#getMagistradosAtuantes(ProcessoTrf)
	 * 
	 * @param processo
	 *            Processo que será usado como base de pesquisa.
	 * 
	 * @param filtrarPorColegiado
	 *            Indicador para trazer apenas os Órgãos Julgadores que fazem
	 *            parte do mesmo Órgão Colegiado do Processo.
	 * 
	 * @return Lista de Orgãos Julgares em que os magistrados atuantes no
	 *         processo estão cadastrados como titular
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresDosAtuantes(
			ProcessoTrf processo, boolean filtrarPorColegiado) {
		
		List<OrgaoJulgador> orgaosTitularDosParticipantes = new ArrayList<OrgaoJulgador>();
		
		List<Pessoa> magistradosAtuantes = processoJudicialManager
				.getMagistradosAtuantes(processo);
		
		if (magistradosAtuantes != null) {
			
			OrgaoJulgadorColegiado ojColegiado = null;
			if (filtrarPorColegiado) {
				ojColegiado = processo.getOrgaoJulgadorColegiado();
			}
			
			for (Pessoa pessoa : magistradosAtuantes) {
				orgaosTitularDosParticipantes.addAll(obterOrgaosJulgadoresResponsavel(
						pessoa.getIdUsuario(), 
						ojColegiado));
			}
		}		
		
		return orgaosTitularDosParticipantes;
		
	}
	
	/**
	 * Operação que consulta orgãos julgadores baseados em suas descrições.
	 * 
	 * @param descricao
	 * @return Lista de {@link OrgaoJulgador}
	 * @throws PJeBusinessException
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresPorDescricao(String descricao) throws PJeBusinessException{
		try {
			Search search = new Search(OrgaoJulgador.class);
			search.addCriteria(Criteria.contains("orgaoJulgador", descricao)); 
			search.addCriteria(Criteria.equals("ativo", Boolean.TRUE)); 
			return this.orgaoJulgadorManager.list(search);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new PJeBusinessException(e);
		}
	}
	
	/**
	 * Retorna a descrição do órgão julgador para onde o processo foi
	 * distribuído pela última vez.
	 * 
	 * @param processoTrf
	 * @return DescricaoOrgaoJulgadorUltimaDistribuicao
	 */
	public String obterDescricaoOrgaoJulgadorUltimaDistribuicao(ProcessoTrf processoTrf) {
		String descricaoOrgaoJulgador = orgaoJulgadorManager.obterDescricaoOrgaoJulgadorUltimaDistribuicao(processoTrf);
		if (!StringUtil.isEmpty(descricaoOrgaoJulgador)) {
			return descricaoOrgaoJulgador;
		} else {
			return "";
		}
	}

	/**
	 * Retorna se é permitido mostrar o órgão julgador da última distribuição.
	 * 
	 * @return permiteMostrarUltimaDistribuicao
	 */
	public boolean isPermiteMostrarUltimaDistribuicao(Integer idOrgaoJulgadorAtual) {		
		if (idOrgaoJulgadorAtual == null || idOrgaoJulgadorAtual.intValue() == 0) {
			return false;
		}		
		return Util.listaContem(ParametroUtil.instance().getIdsOrgaosJulgadoresVisualizacaoUltimaDistribuicao(), idOrgaoJulgadorAtual.toString());
	}	
	
	public List<OrgaoJulgador> findbyIds(List<Integer> idsOrgaoJulgador) {
		// [PJEII-663] Prevenir NullPointerException em componentes marcados com BypassInterceptors
		if (orgaoJulgadorManager == null) {
			orgaoJulgadorManager = new OrgaoJulgadorManager();
		}
		return orgaoJulgadorManager.findByIds(idsOrgaoJulgador);
	}
	
}
