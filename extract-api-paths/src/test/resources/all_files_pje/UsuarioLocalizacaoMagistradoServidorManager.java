package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.UsuarioLocalizacaoMagistradoServidorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.servicos.DateService;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.VinculacaoUsuario;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(UsuarioLocalizacaoMagistradoServidorManager.NAME)
public class UsuarioLocalizacaoMagistradoServidorManager extends BaseManager<UsuarioLocalizacaoMagistradoServidor>{

	public static final String NAME = "usuarioLocalizacaoMagistradoServidorManager";
	
	boolean isAmbienteColegiado;
	boolean isUsuarioAdministradorCadastro;
	OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual;
	OrgaoJulgador orgaoJulgadorAtual;

	@In
	private UsuarioLocalizacaoMagistradoServidorDAO usuarioLocalizacaoMagistradoServidorDAO;
	
	@Override
	protected UsuarioLocalizacaoMagistradoServidorDAO getDAO() {
		return usuarioLocalizacaoMagistradoServidorDAO;
	}
	
    @Create
	public void inicializaDadosUsuario() {
		this.isAmbienteColegiado = !ComponentUtil.getComponent(ParametroUtil.class).isPrimeiroGrau();
		this.isUsuarioAdministradorCadastro = Authenticator.isPermissaoCadastroTodosPapeis();
		this.orgaoJulgadorColegiadoAtual = Authenticator.getOrgaoJulgadorColegiadoAtual();
		this.orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
	}


	public List<Usuario> juizes() {
		return usuarioLocalizacaoMagistradoServidorDAO.listJuizes();
	}
	
	public String getRelator(OrgaoJulgador orgaoJulgador){
		if(orgaoJulgador == null){
			return null;
		}
		return usuarioLocalizacaoMagistradoServidorDAO.getRelator(orgaoJulgador);
	}
	
	public List<Cargo> getCargoVisibilidadeList(UsuarioLocalizacaoMagistradoServidor locAtual) {
		return usuarioLocalizacaoMagistradoServidorDAO.getCargoVisibilidadeList(locAtual);
	}	
	
	public boolean possuiVisibilidadeTodosCargos(List<Cargo> cargoVisibilidadeList) {
		if (cargoVisibilidadeList.isEmpty() || cargoVisibilidadeList.size() > 1) {
			return false;
		} else {
			return cargoVisibilidadeList.get(0) == null;
		}
	}
	
	/**
	 * Retorna a lista atual ordenada de magistrados em determinado órgão julgador .
	 * 
	 * @param oj Órgão julgador.
	 * @param pessoaMagistrado Magistrado a ser retirado do resultado.
	 * @return A lista atual de magistrados em determinado órgão julgador.
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> getMagistrados(OrgaoJulgador oj, PessoaMagistrado pessoaMagistrado) {
		List<UsuarioLocalizacaoMagistradoServidor> localizacaoMagistradosOrig = usuarioLocalizacaoMagistradoServidorDAO.getMagistrados(oj, null, new Date());
		if (pessoaMagistrado != null) {
			Iterator<UsuarioLocalizacaoMagistradoServidor> iterator = localizacaoMagistradosOrig.iterator();
			while (iterator.hasNext()) {
				UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = (UsuarioLocalizacaoMagistradoServidor) iterator.next();
				if (usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().getUsuario().getIdUsuario() == pessoaMagistrado.getIdUsuario()) {
					iterator.remove();
					break;
				}
			}
		}
		return localizacaoMagistradosOrig;
	}

	/**
	 * Retorna a lista atual de magistrados que atuam como auxiliares em determinado órgão julgador.
	 * 
	 * @param oj Órgão julgador.
	 * @return A lista atual de magistrados que atuam como auxiliares em determinado órgão julgador.
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> getMagistradosAuxiliares(OrgaoJulgador oj) {
		return usuarioLocalizacaoMagistradoServidorDAO.getMagistradosAuxiliares(oj, null, new Date());
	}
	
	/**
	 * Verifica se a localização passada como parâmetro pertence a um juiz
	 * auxiliar de determinado órgão julgador
	 * 
	 * @param pessoa
	 * @param orgaoJulgador
	 * @return 
	 * 
	 * @see #getMagistradosAuxiliares(OrgaoJulgador)
	 */
	public boolean isLocalizacaoDeMagistradoAuxiliar(
			UsuarioLocalizacaoMagistradoServidor ulms,
			OrgaoJulgador orgaoJulgador) {

		if (ulms != null && ulms.getOrgaoJulgadorCargo().getAuxiliar()
				&& orgaoJulgador != null) {

			List<UsuarioLocalizacaoMagistradoServidor> magistradosAuxiliares = getMagistradosAuxiliares(orgaoJulgador);

			if (magistradosAuxiliares != null) {
				for (UsuarioLocalizacaoMagistradoServidor ulmsDoOrgao : magistradosAuxiliares) {
					if (ulmsDoOrgao.equals(ulms)) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	/**
	 * Pesquisa quais as localizações/órgão julgador do magistrado em que atua
	 * como titular
	 * 
	 * @param idUsuario
	 *            id de usuário do magistrado
	 * @param orgaoJulgadorColegiado
	 *            - filtro por órgão julgador, pode ser nulo.
	 * @return
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> recuperaLocalizacaoDeOrgaosResponsavel(
			Integer idUsuario, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		Search s = new Search(UsuarioLocalizacaoMagistradoServidor.class);
		
		addCriteria(s, 
				Criteria.lessOrEquals("dtInicio", new Date()),
				Criteria.equals("usuarioLocalizacao.usuario.idUsuario", idUsuario),
				Criteria.equals("magistradoTitular", true));
		if (orgaoJulgadorColegiado != null) {
			addCriteria(s,Criteria.equals("orgaoJulgadorColegiado", orgaoJulgadorColegiado));
		}
		addCriteria(s,Criteria.or(
						Criteria.isNull("dtFinal"),
						Criteria.greaterOrEquals("dtFinal", new Date())));
		
		List<UsuarioLocalizacaoMagistradoServidor> ret = list(s);
		
		return ret;
		
	}	

	/**
	 * Método responsável por recuperar os dados do magistrado por cargo
	 * @param orgaoJulgadorCargo Dados do cargo
	 * @return Dados do magistrado
	 */
	public UsuarioLocalizacaoMagistradoServidor getMagistradoPorCargo(OrgaoJulgadorCargo orgaoJulgadorCargo) {
		try {
			return usuarioLocalizacaoMagistradoServidorDAO.getMagistradoPorCargo(orgaoJulgadorCargo);
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Método responsável por recuperar as localizoões dos magistrados titulares
	 * de determinado órgão julgador, podendo o filtro ser realizado por
	 * colegiado também.
	 * 
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} para pesquisa
	 * 
	 * @param ojColegiado
	 *            {@link OrgaoJulgadorColegiado} para pesquisa (pode ser
	 *            nulo)
	 * 
	 * @return {@link UsuarioLocalizacaoMagistradoServidor} do
	 *         {@link OrgaoJulgador} em que exista um magistrado atuando como
	 *         titular.
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesMagistradosTitulares(
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado ojColegiado) {
		return usuarioLocalizacaoMagistradoServidorDAO
				.obterLocalizacoesMagistrados(orgaoJulgador, ojColegiado, null, null, true);
	}
	
	/**
	 * Método responsável por recuperar a localização do magistrado pricipal de um 
	 * de determinado órgão julgador, podendo o filtro ser realizado por
	 * colegiado também. Entende-se por magistrado principal, aquele que é titular, 
	 * priorizando o que recebe distribuição caso tenha mais de um titular.
	 * 
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} para pesquisa
	 * 
	 * @param ojColegiado
	 *            {@link OrgaoJulgadorColegiado} para pesquisa (pode ser
	 *            nulo)
	 * 
	 * @return UsuarioLocalizacaoMagistradoServidor a localização do magistrado principal.
	 *         
	 *         
	 */
	public UsuarioLocalizacaoMagistradoServidor obterLocalizacaoMagistradoPrincipal(OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado ojColegiado) {

		UsuarioLocalizacaoMagistradoServidor localizacaoMagistradoPrincipal = null;

		List<UsuarioLocalizacaoMagistradoServidor> localizacoesMagistradosTitulares = usuarioLocalizacaoMagistradoServidorDAO
				.obterLocalizacoesMagistrados(orgaoJulgador, ojColegiado, null, null, true);

		for (UsuarioLocalizacaoMagistradoServidor loc : localizacoesMagistradosTitulares) {
			localizacaoMagistradoPrincipal = loc;
			if (Boolean.TRUE.equals(loc.getOrgaoJulgadorCargo().getRecebeDistribuicao())) {
				break;
			}
		}

		return localizacaoMagistradoPrincipal;
	}
	
	
	/**
	 * Método responsável por recuperar os dados de localização do magistrado por
	 * cargo, indicando se deseja o titular ou de determinado colegiado
	 * 
	 * Este método faz a pesquisa para recuperar apenas uma localização de acordo as chaves do cargo
	 * 
	 * @param orgaoJulgadorCargo
	 *            Cargo dentro do OJ para pesquisa.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            Colegiado dentro da localização do OJ (opcional, pode ser
	 *            nulo)
	 * 
	 * @param magistradoTitular
	 *            <code>true</code> para trazer somente o titular
	 *            <code>false</code> para trazer o qual não é titular
	 *            <code>null</code> para não filtrar por este parâmetro
	 * 
	 * @return
	 * 		A localização do magistrador/servidor de acordo com os parâmetros informados.
	 * 
	 */
	public UsuarioLocalizacaoMagistradoServidor obterLocalizacaoMagistrado(
			OrgaoJulgadorCargo orgaoJulgadorCargo,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			Boolean magistradoTitular) {
		
		return usuarioLocalizacaoMagistradoServidorDAO
				.obterLocalizacaoMagistrado(orgaoJulgadorCargo,
						orgaoJulgadorColegiado, 
						magistradoTitular);
	}
	
	/**
	 * Método responsável por obter a lista de localizações  
	 * 
	 * dos magistrados
	 * que participaram em determinado processo. As localizações retornadas são as que 
	 * os magistrados estlão cadastrados atualmente como titular.
	 * 
	 * Basicamente faz a busca reunindo a lista de Localizações de três regras de negócio: 
	 * 	- Própria Localização do Relator do Processo; 
	 * 	- Do Órgão Julgador do Relator busca quais os magistrados são 
	 * 		auxiliares/substitutos, para então buscar as localizações  
	 * 		em que os auxiliares estão atualmente cadastrados como titular;
	 * 	- Dos magistrados que emitiram algum documento no processo busca-se em 
	 * 		qual Localização ele atua como titular.
	 * 
	 * @see #obterLocalizacaoMagistradoTitular(OrgaoJulgador, OrgaoJulgadorColegiado)
	 * @see #obterLocalizacaoTitularDosAuxiliares(OrgaoJulgador, OrgaoJulgadorColegiado)
	 * @see #obterLocalizacaoTitularDosMagistradosAtuantes(ProcessoTrf, boolean)
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
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacaoAtualMagistradosRelacionados(
			ProcessoTrf processo, boolean filtrarPorColegiado) {
		
		Set<UsuarioLocalizacaoMagistradoServidor> localizacoes = new HashSet<UsuarioLocalizacaoMagistradoServidor>();
		
		if (processo != null) {
			
			OrgaoJulgadorColegiado ojColegiado = null;			
			if (filtrarPorColegiado) {
				ojColegiado = processo.getOrgaoJulgadorColegiado();
			}
			
			// Localizações do Relator do Processo
			OrgaoJulgador ojProcesso = processo.getOrgaoJulgador();			
			localizacoes.addAll(obterLocalizacoesMagistradosTitulares(ojProcesso, ojColegiado));
			
			// Localizações titular do Auxiliares
			localizacoes.addAll(obterLocalizacaoTitularDosAuxiliares(ojProcesso, ojColegiado));
			
			// Localizações em que os atuantes no processo são titular			
			localizacoes.addAll(obterLocalizacaoTitularDosMagistradosAtuantes(
					processo, filtrarPorColegiado));			
		}
		
		return new ArrayList<UsuarioLocalizacaoMagistradoServidor>(localizacoes);		
	}
	
	/**
	 * Método responsável por obter a lista dos de localizações onde os
	 * magistrados auxiliares/substitutos do Órgão Julgador atuam como
	 * titulares.
	 * 
	 * @see #getMagistradosAuxiliares(OrgaoJulgador)
	 * 
	 * @param orgaoJulgador
	 *            Órgão Julgador que possui os magistrados atuantes como
	 *            auxiliares/substitutos.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            Filtro para retornar apenas os Órgãos Julgadores de
	 *            determinado Órgão Colegiado. Pode ser nulo.
	 * 
	 * @return Lista de Orgãos Julgares em que os auxiliares/subtituos atuam
	 *         como titulares.
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacaoTitularDosAuxiliares(
			OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		HashSet<UsuarioLocalizacaoMagistradoServidor> locTitularDosAuxiliares = new HashSet<UsuarioLocalizacaoMagistradoServidor>();
		
		List<UsuarioLocalizacaoMagistradoServidor> locMagistradosAuxiliares = usuarioLocalizacaoMagistradoServidorDAO
				.getMagistradosAuxiliares(orgaoJulgador, orgaoJulgadorColegiado, new Date());
		
		if (locMagistradosAuxiliares != null) {
			for (UsuarioLocalizacaoMagistradoServidor locMagistradoAuxiliar : locMagistradosAuxiliares) {
				locTitularDosAuxiliares.addAll(recuperaLocalizacaoDeOrgaosResponsavel(
						locMagistradoAuxiliar.getUsuarioLocalizacao().getUsuario().getIdUsuario(),
						orgaoJulgadorColegiado));
			}
		}		
		
		return new ArrayList<UsuarioLocalizacaoMagistradoServidor>(locTitularDosAuxiliares);		
	}
	
	/**
	 * Método responsável por recuperar as localizações titulares atual dos
	 * magistrados que atuaram em determinado processo, podendo o filtro ser
	 * realizado pelo órgão colegiado.
	 * 
	 * @see #recuperaLocalizacaoDeOrgaosResponsavel(Integer,
	 *      OrgaoJulgadorColegiado)
	 * @see ProcessoJudicialManager#getMagistradosAtuantes(ProcessoTrf)
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} que será utilizado para recuperar os
	 *            magistrados atuantes de determinado processo
	 * 
	 * @param filtrarPorColegiado
	 *            <code>true</code> para filtrar as localizações pelo Órgão
	 *            Colegiado do Processo e <code>false</code> para não realizar o
	 *            filtro.
	 * 
	 * @return {@link List} de {@link UsuarioLocalizacaoMagistradoServidor}
	 *         contendo a localização atual em que os magistrados atuantes do
	 *         processo estão cadastrados como titular
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacaoTitularDosMagistradosAtuantes(
			ProcessoTrf processo, boolean filtrarPorColegiado) {

		List<UsuarioLocalizacaoMagistradoServidor> locTitularDosMagistradosAtuantes = new ArrayList<UsuarioLocalizacaoMagistradoServidor>();
		ProcessoJudicialManager processoJudicialManager = ComponentUtil.getComponent("processoJudicialManager", true);
		List<Pessoa> magistradosAtuantes = processoJudicialManager
				.getMagistradosAtuantes(processo);

		if (magistradosAtuantes != null) {

			OrgaoJulgadorColegiado ojColegiado = null;
			if (filtrarPorColegiado) {
				ojColegiado = processo.getOrgaoJulgadorColegiado();
			}

			for (Pessoa pessoa : magistradosAtuantes) {
				locTitularDosMagistradosAtuantes
						.addAll(recuperaLocalizacaoDeOrgaosResponsavel(
								pessoa.getIdUsuario(), ojColegiado));
			}

		}
		return locTitularDosMagistradosAtuantes;
	}
	
 	/**
 	 * Exclui lotações/localizações originadas de vinculação de usuário.
 	 * @param vinculacaoUsuario vinculação de usuários que originou as lotações a serem excluídas
 	 * @param orgaoJulgador orgao julgador da lotação a ser excluída 
 	 * @param orgaoJulgadorColegiado orgao julgador colegiado da lotação a ser excluída
 	 */
 	public void removerLotacoes(VinculacaoUsuario vinculacaoUsuario, OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado){
 		getDAO().removerLotacoes(vinculacaoUsuario, orgaoJulgador, orgaoJulgadorColegiado);		
 	}
 
 	/**
 	 * Obtem todas as localizações de um dado usuário
 	 * @param usuario usuário em questão.
 	 * @return lista de localizações desse usuário
 	 */
 	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesUsuario(Usuario usuario) {
 		return getDAO().obterLocalizacoesUsuario(usuario);
 	}
 	
 	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesMagistrado(Usuario usuario) {
 		return getDAO().obterLocalizacoesUsuario(usuario, true, null);
 	}

	/**
	 * @see #UsuarioLocalizacaoMagistradoServidorDAO(OrgaoJulgador, OrgaoJulgadorColegiado, 
			Boolean, Boolean, Boolean)         
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesMagistrados(
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado ojColegiado, 
			Boolean isCargoDistribuivel, Boolean isCargoAuxiliar, Boolean isMagistradoTitular){
		
		return usuarioLocalizacaoMagistradoServidorDAO.obterLocalizacoesMagistrados(
				orgaoJulgador, ojColegiado, isCargoDistribuivel, isCargoAuxiliar, isMagistradoTitular);  
	}
	
	/**
	 * @see #UsuarioLocalizacaoMagistradoServidorDAO(UsuarioLocalizacaoMagistradoServidor)         
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesServidores(
			UsuarioLocalizacaoMagistradoServidor localizacaoMagistrado){
		return usuarioLocalizacaoMagistradoServidorDAO.obterLocalizacoesServidores(localizacaoMagistrado);
		
	}
	
	/**
	 * @see #UsuarioLocalizacaoMagistradoServidorDAO#obterLocalizacaoMagistradoAssessorado(UsuarioLocalizacaoMagistradoServidor)         
	 */
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesMagistradosAssessorado(UsuarioLocalizacaoMagistradoServidor localizacaoAssessor){
		return usuarioLocalizacaoMagistradoServidorDAO.obterLocalizacoesMagistradosAssessorado(localizacaoAssessor);
	}
	
	public List<UsuarioLocalizacaoMagistradoServidor> obterLocalizacoesAcessoresMagistrado(UsuarioLocalizacaoMagistradoServidor localizacaoMagistrado){
		return usuarioLocalizacaoMagistradoServidorDAO.obterLocalizacoesAcessoresMagistrado(localizacaoMagistrado);
	}
	

	public UsuarioLocalizacaoMagistradoServidor obterLocalizacaoAtivaPriorizandoColegiado(Integer idUsuario, OrgaoJulgador orgaoJulgador, OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		
		UsuarioLocalizacaoMagistradoServidor localizacaoAtiva = null;

		if(orgaoJulgador != null && orgaoJulgadorColegiado != null){
			try {
				UsuarioManager usuarioManager = ComponentUtil.getComponent(UsuarioManager.class);
				Usuario usuario = usuarioManager.findById(idUsuario);
				List<UsuarioLocalizacaoMagistradoServidor> localizacoesUsuario = obterLocalizacoesUsuario(usuario);
				Date dataAtual = DateService.instance().getDataHoraAtual();
				for (UsuarioLocalizacaoMagistradoServidor locUsuario :  localizacoesUsuario) {
					if(
						orgaoJulgador.equals(locUsuario.getOrgaoJulgador()) &&
						locUsuario.getOrgaoJulgadorColegiado() != null &&
						(locUsuario.getDtInicio() == null || DateUtil.isDataMenorIgual(locUsuario.getDtInicio(), dataAtual)) &&
						(locUsuario.getDtFinal() == null || DateUtil.isDataMaiorIgual(locUsuario.getDtFinal(), dataAtual))
					){
						localizacaoAtiva = locUsuario;
						if (orgaoJulgadorColegiado.equals(locUsuario.getOrgaoJulgadorColegiado())){
							break;	
						}
					}
				}
			} catch (PJeBusinessException e) {
			}
		}	
			
		return localizacaoAtiva;
	}

	/**
	 * Verifica se dada uma configuração de usuário localização, se já existe a mesma configuração na base de dados de forma duplicada
	 * 
	 * @param usuario
	 * @param ojc
	 * @param oj
	 * @param localizacaoFisica
	 * @param localizacaoModelo
	 * @param papel
	 * @param dataInicio
	 * @param idUsuarioLocalizacaoMagistradoServidor
	 * @return
	 */
	public boolean verificaLocalizacaoInformadaJaExiste(Usuario usuario, OrgaoJulgadorColegiado ojc, OrgaoJulgador oj, Localizacao localizacaoFisica, Localizacao localizacaoModelo, 
    		Papel papel, Date dataInicio, Integer idUsuarioLocalizacaoMagistradoServidor) {
		return getDAO().verificaLocalizacaoInformada(usuario, ojc, oj, localizacaoFisica, localizacaoModelo, papel, dataInicio, idUsuarioLocalizacaoMagistradoServidor);
	}
	
	public boolean isOrgaoJulgadorColegiadoObrigatorio() {
		boolean orgaoJulgadorColegiadoObrigatorio = Boolean.FALSE;
		if(this.isAmbienteColegiado && this.orgaoJulgadorColegiadoAtual != null) {
			orgaoJulgadorColegiadoObrigatorio = Boolean.TRUE;
		}
		return orgaoJulgadorColegiadoObrigatorio;
	}

	public boolean isOrgaoJulgadorObrigatorio() {
		boolean orgaoJulgadorObrigatorio = Boolean.FALSE;
		if(this.orgaoJulgadorAtual != null) {
			orgaoJulgadorObrigatorio = Boolean.TRUE;
		}
		return orgaoJulgadorObrigatorio;
	}
	
	public boolean isLocalizacaoModeloObrigatoria(Localizacao localizacaoFisica) {
		return (localizacaoFisica != null && localizacaoFisica.getEstruturaFilho() != null);
	}

	/**
	 * Verifica se os dados do cadastro estão minimamente preenchidos
	 * 1. valida se os campos obrigatorios foram preenchidos
	 * 2. verifica se a localização física está corretamente selecionada
	 * 3. verifica se a localização modelo, se estiver selecionada, pertence à estrutura modelo da localização física
	 * 4. faz validação de papéis em localizações que devem ser relativamente fixas
	 * 5. verifica se o registro já existe na base de dados
	 * @throws PJeBusinessException 
	 */
	public void validarDadosServidor(Usuario usuario, OrgaoJulgadorColegiado ojc, OrgaoJulgador oj, Localizacao localizacaoFisica, Localizacao localizacaoModelo, 
    		Papel papel, Integer idUsuarioLocalizacaoMagistradoServidor) throws PJeBusinessException {
		
		if(this.isOrgaoJulgadorColegiadoObrigatorio() && ojc == null) {
			throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.campoObrigatorio.ojc");
		}else if(this.isOrgaoJulgadorObrigatorio() && oj == null) {
			throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.campoObrigatorio.oj");
		}else if(localizacaoFisica == null) {
			throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.campoObrigatorio.localizacaoFisica");
		}else if(this.isLocalizacaoModeloObrigatoria(localizacaoFisica) && localizacaoModelo == null) {
			throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.campoObrigatorio.localizacaoModelo");
		}else if(papel == null) {
			throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.campoObrigatorio.localizacaoPapel");
		}else {
			Localizacao localizacaoFisicaSelecionada = null;
			if(ojc != null && ojc.getLocalizacao() != null) {
				localizacaoFisicaSelecionada = ojc.getLocalizacao();
			}else if(oj != null && oj.getLocalizacao() != null){
				localizacaoFisicaSelecionada = oj.getLocalizacao();
			}else if(localizacaoFisica != null) {
				localizacaoFisicaSelecionada = localizacaoFisica;
			}
			
			if(localizacaoFisicaSelecionada == null) {
				throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.inconsistencia.localizacaoFisica");
			}else {
				LocalizacaoManager localizacaoManager = ComponentUtil.getComponent("localizacaoManager");
				if(localizacaoModelo != null && !(localizacaoFisica != null 
						&& localizacaoFisicaSelecionada.getEstruturaFilho() != null 
						&& !localizacaoManager.isLocalizacaoDescendente(localizacaoFisicaSelecionada.getEstruturaFilho(), localizacaoModelo))) {
					throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.localizacaoModeloNaoVinculadaLocalizacaoFisica");
				}else if (Authenticator.isDiretorDistribuicao(papel.getIdentificador())) {
	                if (localizacaoModelo != null && !localizacaoModelo.equals(
	                            ParametroUtil.instance().getLocalizacaoDirecaoDistribuicao())) {
						throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.localizacaoNaoDirDistribuicao");
	                }
	            } else if (Authenticator.isDiretorSecretaria(papel.getIdentificador())) {
	                if (localizacaoModelo != null && !localizacaoModelo.equals(
	                		ParametroUtil.instance().getLocalizacaoDirecaoSecretaria()) &&
	                        !localizacaoModelo.equals(
	                        		ParametroUtil.instance().getLocalizacaoDirecaoSecretariaSRREO())) {
						throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.localizacaoNaoDirSecretaria");
	                }
	            } else if (Authenticator.isPapelAdministrador(papel) &&
	                    (ojc != null || oj != null)) {
					throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.adminComOrgaoJulgador");
	            }
			}
			if(this.verificaLocalizacaoInformadaJaExiste(usuario, ojc, oj, localizacaoFisicaSelecionada, localizacaoModelo, papel, new Date(), idUsuarioLocalizacaoMagistradoServidor)) {
				throw new PJeBusinessException("pje.usuarioLocalizacaoMagistradoServidor.error.cadastroDuplicado");
			}
		}
	}

	public void removerVisualizadorProcessosSigilosos(Integer idUsuario, Integer idOrgaoJulgador, Integer idUsuLocMagistrado) {
		Long count = this.usuarioLocalizacaoMagistradoServidorDAO.countUsuarioLocalizacaoMagistradoServidor(idUsuario, idOrgaoJulgador, idUsuLocMagistrado);
		if (count != null && count.equals(0L)) {
			ProcessoVisibilidadeSegredoManager processoVisibilidadeSegredoManager = ComponentUtil.getComponent(ProcessoVisibilidadeSegredoManager.class);
			processoVisibilidadeSegredoManager.removerVisualizadorProcessoSigilosoNoOrgaoJulgador(idUsuario, idOrgaoJulgador);
		}
	}
}