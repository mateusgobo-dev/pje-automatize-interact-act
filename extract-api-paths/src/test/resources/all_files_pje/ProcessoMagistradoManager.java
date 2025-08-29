package br.jus.cnj.pje.nucleo.manager;

import java.util.AbstractMap;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.business.dao.OrgaoJulgadorDAO;
import br.jus.cnj.pje.business.dao.PessoaMagistradoDAO;
import br.jus.cnj.pje.business.dao.ProcessoMagistradoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SubstituicaoMagistrado;
import br.jus.pje.nucleo.enums.TipoAtuacaoDetalhadaMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;
import br.jus.pje.nucleo.enums.TipoRelacaoProcessoMagistradoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoMagistradoManager.NAME)
public class ProcessoMagistradoManager extends BaseManager<ProcessoMagistrado>{

	public static final String NAME = "processoMagistradoManager";

	@Logger
	private Log log;
	
	@In
	private ProcessoMagistradoDAO processoMagistradoDAO;
	
	@In
	private OrgaoJulgadorDAO orgaoJulgadorDAO;
	
	@In
	private PessoaMagistradoDAO pessoaMagistradoDAO;
	
	@Override
	protected ProcessoMagistradoDAO getDAO() {
		return processoMagistradoDAO;
	}

	/**
	 * Método responsável por verificar se existe alguma vinculação regimental de
	 * magistrado relator no processo informado
	 */
	public boolean existeVinculacaoRegimentalRelator(ProcessoTrf processoTrf) {
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				TipoRelacaoProcessoMagistradoEnum.REGIM, TipoAtuacaoMagistradoEnum.RELAT, null, true);
		return !magistradosVinculados.isEmpty();
	}
	
	/**
	 * Método responsável por verificar se existe alguma vinculação de relator
	 * designado no processo informado
	 */
	public boolean existeVinculacaoRelatorDesignado(ProcessoTrf processoTrf) {
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				TipoRelacaoProcessoMagistradoEnum.DESIG, TipoAtuacaoMagistradoEnum.RELAT, null, true);
		return !magistradosVinculados.isEmpty();
	}
	
	/**
	 * Método responsável por verificar se existe alguma vinculação de reserva de
	 * magistrado relator no processo informado
	 */
	public boolean existeVinculacaoReservaRelator(ProcessoTrf processoTrf) {
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				TipoRelacaoProcessoMagistradoEnum.RESER, TipoAtuacaoMagistradoEnum.RELAT, null, true);
		return !magistradosVinculados.isEmpty();
	}
			
	/**
	 * Método responsável por verificar se existe alguma vinculação regimental de
	 * magistrado revisor no processo informado
	 */
	public boolean existeVinculacaoRegimentalRevisor(ProcessoTrf processoTrf) {
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				TipoRelacaoProcessoMagistradoEnum.REGIM, TipoAtuacaoMagistradoEnum.REVIS, null, true);
		return !magistradosVinculados.isEmpty();
	}
	
	/**
	 * Método responsável por recuperar a última vinculação de magistrado atia
	 * registrada no processo.
	 * 
	 * @see ProcessoMagistradoDAO#obterMagistradosRelacionados(ProcessoTrf,
	 *      TipoRelacaoProcessoMagistradoEnum, TipoAtuacaoMagistradoEnum,
	 *      Boolean, Boolean)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} para consulta dos magistrados vinculados.
	 * 
	 * @param tipoVinculacaoProcessoMagistrado
	 *            {@link TipoRelacaoProcessoMagistradoEnum} para recuperar de
	 *            acordo com o tipo de vinculação. Use <code>null</code> para
	 *            ignorar esse parâmetro
	 * 
	 * @param tipoAtuacaoMagistrado
	 *            {@link TipoAtuacaoMagistradoEnum} que representa o atuação do
	 *            magitrado (relator, revisor, vogal). Use <code>null</code>
	 *            para não realizar filtro por este atributo.
	 * 
	 * @return {@link ProcessoMagistrado} representando a vinculação do
	 *         magistrado ao processo. Retorna <code>null</code>caso não exista
	 *         o tipo de vinculação desejada.
	 */
	public ProcessoMagistrado obterUltimaVinculacao(ProcessoTrf processoTrf,
			TipoRelacaoProcessoMagistradoEnum tipoVinculacaoProcessoMagistrado,
			TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado) {
		
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				tipoVinculacaoProcessoMagistrado, tipoAtuacaoMagistrado, null, true);

		return (!magistradosVinculados.isEmpty() ? magistradosVinculados.get(0) : null);
	}
	
	/**
	 * Método responsável por recuperar a última vinculação de magistrado atia
	 * registrada no processo.
	 * 
	 * @see ProcessoMagistradoDAO#obterMagistradosRelacionados(ProcessoTrf,
	 *      TipoRelacaoProcessoMagistradoEnum, TipoAtuacaoMagistradoEnum,
	 *      Boolean, Boolean)
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} para consulta dos magistrados vinculados.
	 * 
	 * @param tipoVinculacaoProcessoMagistrado
	 *            {@link TipoRelacaoProcessoMagistradoEnum} para recuperar de
	 *            acordo com o tipo de vinculação. Use <code>null</code> para
	 *            ignorar esse parâmetro
	 * 
	 * @param tipoAtuacaoMagistrado
	 *            {@link TipoAtuacaoMagistradoEnum} que representa o atuação do
	 *            magitrado (relator, revisor, vogal). Use <code>null</code>
	 *            para não realizar filtro por este atributo.
	 * 
	 * @param magistradoTitular
	 *            <code>true</code> indica se deve trazer apenas vinculações
	 *            registradas por magistrados titulares de gabinete ou, caso
	 *            <code>false</code> por seus substituto. Use <code>null</code>
	 *            para ignorar esse parâmetro
	 * 
	 * @return {@link ProcessoMagistrado} representando a vinculação do
	 *         magistrado ao processo. Retorna <code>null</code>caso não exista
	 *         o tipo de vinculação desejada.
	 */
	public ProcessoMagistrado obterUltimaVinculacao(ProcessoTrf processoTrf,
			TipoRelacaoProcessoMagistradoEnum tipoVinculacaoProcessoMagistrado,
			TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado,
			Boolean magistradoTitular) {
		
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				tipoVinculacaoProcessoMagistrado, tipoAtuacaoMagistrado, magistradoTitular, true);

		return (!magistradosVinculados.isEmpty() ? magistradosVinculados.get(0) : null);
	}

	/**
	 * Método responsável por gravar as informações de vinculação regimental do
	 * relator ao processo.
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} que terá o magistrado vinculado.
	 * 
	 * @param magistradoRelator
	 *            {@link PessoaMagistrado} que será vinculado ao processo.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            {@link OrgaoJulgadorColegiado} colegiado de vinculação do
	 *            magistrado ao processo
	 * 
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} de vinculação do magistrado
	 * 
	 * @param orgaoJulgadorCargo
	 *            {@link OrgaoJulgadorCargo} de vinculação do magistrado
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
	 * @return {@link ProcessoMagistrado} representando o vinculo recém criado.
	 * 
	 * @throws PJeBusinessException
	 *             caso já exista uma vinculação regimental de relator ativa
	 *             registrada no processo
	 */
	public ProcessoMagistrado registrarVinculacaoRegimentalRelatorProcesso(ProcessoTrf processoTrf,
			PessoaMagistrado magistradoRelator, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorCargo orgaoJulgadorCargo, boolean isVinculoMagistradoTitular,
			SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException {

		if (existeVinculacaoRegimentalRelator(processoTrf)) {
			log.info("Já existe uma vinculação regimenal de relator registrada para o processo " + processoTrf.getNumeroProcesso());
			throw new PJeBusinessException("processoMagistrado.validacao.jaExisteViculacaoRegimentalRelator");
		}
		
		// verificar se existe reserva de processo, entretanto se a reserva for do próprio magistrado tem que deixar passar
		List<ProcessoMagistrado> magistradosVinculados = obterMagistradosRelacionados(processoTrf,
				TipoRelacaoProcessoMagistradoEnum.RESER, TipoAtuacaoMagistradoEnum.RELAT, null, true);
		
		if (!magistradosVinculados.isEmpty()) {
			for (ProcessoMagistrado processoMagistrado : magistradosVinculados) {
				if (!processoMagistrado.getMagistrado().equals(magistradoRelator)
						|| !processoMagistrado.getOrgaoJulgadorCargo().equals(orgaoJulgadorCargo)
						|| !processoMagistrado.getOrgaoJulgadorColegiado().equals(orgaoJulgadorColegiado)){
					log.info("Já existe uma reserva registrada pra o processo " + processoTrf.getNumeroProcesso());
					throw new PJeBusinessException("processoMagistrado.validacao.jaExisteViculacaoReservaRelator");
				} else {
					// vinculação sendo feita sobre uma reserva anterior do próprio magistrado, então deve-se pegar as informações da substituição					
					if (processoMagistrado.getSubstituicaoMagistradoVigente() != null) {
						substituicaoMagistrado = processoMagistrado.getSubstituicaoMagistradoVigente();
						isVinculoMagistradoTitular = false;
					}					
				}
			}
		}
		
		ProcessoMagistrado vinculacaoMagistrado = registrarVinculacaoMagistradoProcesso(processoTrf, magistradoRelator,
				orgaoJulgadorColegiado, orgaoJulgador, orgaoJulgadorCargo, TipoAtuacaoMagistradoEnum.RELAT,
				TipoRelacaoProcessoMagistradoEnum.REGIM, isVinculoMagistradoTitular, substituicaoMagistrado);

		return vinculacaoMagistrado;
	}
	
	/**
	 * Método responsável por gravar as informações de vinculação regimental do
	 * revisor ao processo.
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} que terá o magistrado vinculado.
	 *            
	 * @param magistradoRevisor
	 *            {@link PessoaMagistrado} que será vinculado ao processo.
	 *            
	 * @param orgaoJulgadorColegiado
	 *            {@link OrgaoJulgadorColegiado} colegiado de vinculação do
	 *            revisor ao processo
	 *            
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} de vinculação do magistrado revisor
	 *            
	 * @param orgaoJulgadorCargo
	 *            {@link OrgaoJulgadorCargo} de vinculação do magistrado revisor
	 *            
	 * @param isVinculoRevisorTitular
	 *            <code>true</code> caso trata-se de magistrado titular do OJ
	 *            informado ou <code>false</code>caso o magistrado informado
	 *            seja um substituto
	 *            
	 * @param substituicaoMagistrado
	 *            {@link SubstituicaoMagistrado} que representa que o magistrado
	 *            revisor informado está atuando como substituto do titular do
	 *            OJ. Pode ser nulo caso não seja substituto.
	 * 
	 * @return {@link ProcessoMagistrado} representando o vinculo recém criado.
	 * 
	 * @throws PJeBusinessException
	 *             caso já exista uma vinculação regimental de revisor ativa
	 *             registrada no processo
	 */
	public ProcessoMagistrado registrarVinculacaoRegimentalRevisorProcesso(ProcessoTrf processoTrf,
			PessoaMagistrado magistradoRevisor, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorCargo orgaoJulgadorCargo, boolean isVinculoRevisorTitular,
			SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException {
		
		if (existeVinculacaoRegimentalRevisor(processoTrf)) {
			log.info("Já existe uma vinculação regimental de revisor registrada para o processo " + processoTrf.getNumeroProcesso());
			throw new PJeBusinessException("processoMagistrado.validacao.jaExisteViculacaoRegimentalRevisor");
		}
			
		ProcessoMagistrado vinculacaoMagistrado = registrarVinculacaoMagistradoProcesso(processoTrf, magistradoRevisor,
				orgaoJulgadorColegiado, orgaoJulgador, orgaoJulgadorCargo, TipoAtuacaoMagistradoEnum.REVIS,
				TipoRelacaoProcessoMagistradoEnum.REGIM, isVinculoRevisorTitular, substituicaoMagistrado);

		return vinculacaoMagistrado;
	}
	
	/**
	 * Método responsável por gravar as informações de vinculação de relator
	 * designado ao processo. Normalmente é utilizado quando um vogal ganha o
	 * julgamento e o mesmo precisa lavavrar o acórdão e o inteiro teor,
	 * passando a ser o responsável pelo processo.
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} que terá o magistrado vinculado.
	 * 
	 * @param magistradoRelator
	 *            {@link PessoaMagistrado} que está reservando o processo.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            {@link OrgaoJulgadorColegiado} colegiado de vinculação do
	 *            magistrado. Normamente é o mesmo do processo
	 * 
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} de vinculação do magistrado
	 * 
	 * @param orgaoJulgadorCargo
	 *            {@link OrgaoJulgadorCargo} de vinculação pessoal do magistrado
	 * 
	 * @param isVinculoMagistradoTitular
	 *            Indica se quem está se vinculando é um magistrado substituto
	 *            ou o próprio titular do órgão julgador
	 *            
	 * @return {@link ProcessoMagistrado} representando o vinculo recém criado.
	 * 
	 * @throws PJeBusinessException
	 *             caso já exista ativa uma vinculação de designação de relator
	 *             registrada no processo ou vinculação regimental de relator
	 *
	 */
	public ProcessoMagistrado registrarVinculacaoRelatorDesignado(ProcessoTrf processoTrf,
			PessoaMagistrado magistradoRelator, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorCargo orgaoJulgadorCargo, boolean isVinculoMagistradoTitular)
			throws PJeBusinessException {

		if (existeVinculacaoRelatorDesignado(processoTrf)) {
			log.info("Já existe uma vinculação de relator designado registrado no processo " + processoTrf.getNumeroProcesso());
			throw new PJeBusinessException("processoMagistrado.validacao.jaExisteViculacaoRelatorDesignado");
		}
			
		ProcessoMagistrado vinculacaoMagistrado = registrarVinculacaoMagistradoProcesso(processoTrf, magistradoRelator,
				orgaoJulgadorColegiado, orgaoJulgador, orgaoJulgadorCargo, TipoAtuacaoMagistradoEnum.RELAT,
				TipoRelacaoProcessoMagistradoEnum.DESIG, isVinculoMagistradoTitular, null);

		return vinculacaoMagistrado;
	}

	
	/**
	 * Método responsável por gravar as informações de vinculação de reserva do
	 * relator ao processo. Mais utilizado pelo Juiz substituto quando quer
	 * reservar um processo para que ele possa atuar nele
	 * 
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} que terá o magistrado vinculado.
	 * 
	 * @param magistrado
	 *            {@link PessoaMagistrado} que está reservando o processo.
	 * 
	 * @param orgaoJulgadorColegiado
	 *            {@link OrgaoJulgadorColegiado} colegiado de vinculação
	 *            magistrado.
	 * 
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} de vinculação do magistrado
	 * 
	 * @param orgaoJulgadorCargo
	 *            {@link OrgaoJulgadorCargo} de vinculação do magistrado
	 * 
	 * @param substituicaoMagistrado
	 *            {@link SubstituicaoMagistrado} que representa que o magistrado
	 *            informado está atuando como substituto do titular do OJ. Pode
	 *            ser nulo caso não seja substituto.
	 * 
	 * @return {@link ProcessoMagistrado} representando o vinculo recém criado.
	 * 
	 * @throws PJeBusinessException
	 *             caso já exista ativa uma vinculação de reserva registrada no
	 *             processo ou vinculação regimental de relator
	 */
	public ProcessoMagistrado registrarVinculacaoReservaRelatorProcesso(ProcessoTrf processoTrf,
			PessoaMagistrado magistrado, OrgaoJulgadorColegiado orgaoJulgadorColegiado,
			OrgaoJulgador orgaoJulgador, OrgaoJulgadorCargo orgaoJulgadorCargo,
			SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException {
		
		if (existeVinculacaoRegimentalRelator(processoTrf)) {
			log.info("Já existe uma vinculação regimenal de relator registrada para o processo " + processoTrf.getNumeroProcesso());
			throw new PJeBusinessException("processoMagistrado.validacao.jaExisteViculacaoRegimentalRelator");
		}
		if (existeVinculacaoReservaRelator(processoTrf)) {
			log.info("Já existe uma reserva registrada pra o processo " + processoTrf.getNumeroProcesso());
			throw new PJeBusinessException("processoMagistrado.validacao.jaExisteViculacaoReservaRelator");
		}
		
		ProcessoMagistrado vinculacaoMagistrado = registrarVinculacaoMagistradoProcesso(processoTrf, magistrado,
				orgaoJulgadorColegiado, orgaoJulgador, orgaoJulgadorCargo, TipoAtuacaoMagistradoEnum.RELAT,
				TipoRelacaoProcessoMagistradoEnum.RESER, (substituicaoMagistrado == null), substituicaoMagistrado);
		
		return vinculacaoMagistrado;
	}
	/**
	 * Método responsável por gravar as informações de vinculação de magistrados
	 * no processo.
	 * 
	 * @param processoTrf
	 *            {@link ProcessoTrf} onde será registrada a informação de
	 *            vinculação.
	 *            
	 * @param magistrado
	 *            {@link PessoaMagistrado} que representa o magistrado que está
	 *            se vinculando ao processo.
	 *            
	 * @param orgaoJulgadorColegiado
	 *            {@link OrgaoJulgadorColegiado} do magistrado que está se
	 *            vinculando
	 *            
	 * @param orgaoJulgador
	 *            {@link OrgaoJulgador} do magistrado que está se vinculando
	 *            
	 * @param orgaoJulgadorCargo
	 *            {@link OrgaoJulgadorCargo}do magistrado que está se vinculando
	 *            
	 * @param tipoAtuacaoMagistrado
	 *            {@link TipoAtuacaoMagistradoEnum} que representa o papel do
	 *            magistrado no processo (Relator, Revisor ou Vogal)
	 *            
	 * @param tipoRelacaoProcessoMagistrado
	 *            {@link TipoRelacaoProcessoMagistradoEnum} o tipo de
	 *            vinculação, se regimental, reserva ou convocação
	 *            extraordinária
	 *            
	 * @param isVinculoMagistradoTitular
	 *            <code>true</code> caso trata-se de magistrado titular do OJ
	 *            informado ou <code>false</code>caso o magistrado informado
	 *            seja um substituto
	 *            
	 * @param substituicaoMagistrado
	 *            {@link SubstituicaoMagistrado} em casos em que o magistrado
	 *            substituto está se vinculando, é necessário informar o
	 *            registro de substituição para fins de levantamento sobre a
	 *            quantidade de processos em que o substituto se vinculou
	 *            
	 * @return {@link PessoaMagistrado} que representa a vinculação do
	 *         magistrado ao processo.
	 */
	private ProcessoMagistrado registrarVinculacaoMagistradoProcesso(ProcessoTrf processoTrf, PessoaMagistrado magistrado,
			OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgador,
			OrgaoJulgadorCargo orgaoJulgadorCargo, TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado,
			TipoRelacaoProcessoMagistradoEnum tipoRelacaoProcessoMagistrado, boolean isVinculoMagistradoTitular,
			SubstituicaoMagistrado substituicaoMagistrado) throws PJeBusinessException {
		
		ProcessoMagistrado vinculacaoMagistrado = new ProcessoMagistrado();
		
		vinculacaoMagistrado.setAtivo(true);
		vinculacaoMagistrado.setDataVinculacao(new Date());
		vinculacaoMagistrado.setMagistrado(magistrado);
		vinculacaoMagistrado.setMagistradoTitular(isVinculoMagistradoTitular);
		vinculacaoMagistrado.setOrgaoJulgador(orgaoJulgador);
		vinculacaoMagistrado.setOrgaoJulgadorCargo(orgaoJulgadorCargo);
		vinculacaoMagistrado.setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
		vinculacaoMagistrado.setProcesso(processoTrf);
		vinculacaoMagistrado.setSubstituicaoMagistradoVigente(substituicaoMagistrado);
		vinculacaoMagistrado.setTipoAtuacaoMagistrado(tipoAtuacaoMagistrado);
		vinculacaoMagistrado.setTipoRelacaoProcessoMagistrado(tipoRelacaoProcessoMagistrado);
		
		super.persist(vinculacaoMagistrado);		
				
		log.info("Registrado " + tipoRelacaoProcessoMagistrado.getLabel() + " do " + tipoAtuacaoMagistrado.getLabel()
				+ " para o processo " + processoTrf.getNumeroProcesso() + "(" + processoTrf.getIdProcessoTrf() + ") em "
				+ vinculacaoMagistrado.getDataVinculacao());

		return vinculacaoMagistrado;
	}
	
	/**
	 * Dado um processo, retorna vinculos de magistrados que podem influenciar na composição de julgamento desse processo.
	 * @param processo cujas vinculações de interesse serão obtidas.
	 * @return lista de vinculações de magistrado com processo.
	 */
	public List<ProcessoMagistrado> obterMagistradosRelacionados(ProcessoTrf processo,
			TipoRelacaoProcessoMagistradoEnum tipoVinculacao, TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado,
			Boolean magistradoTitular, Boolean ativo) {		
		return getDAO().obterMagistradosRelacionados(processo, tipoVinculacao, tipoAtuacaoMagistrado, magistradoTitular, ativo, null);
	}
	
	/**
	 * Dado um processo, retorna vinculos de magistrados que podem influenciar na composição de julgamento desse processo.
	 * @param processo cujas vinculações de interesse serão obtidas.
	 * @return lista de vinculações de magistrado com processo.
	 */
	public List<ProcessoMagistrado> obterMagistradosRelacionados(ProcessoTrf processo,
			TipoRelacaoProcessoMagistradoEnum tipoVinculacao, TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado,
			Boolean magistradoTitular, Boolean ativo, Integer idOrgaoJulgador) {		
		return getDAO().obterMagistradosRelacionados(processo, tipoVinculacao, tipoAtuacaoMagistrado, magistradoTitular, ativo, idOrgaoJulgador);
	}
	

	/**
	 * Método responsável por recuperar os magistrados relacionados ao processo
	 * representados na entidade {@link ProcessoMagistrado} que possam ser
	 * representados por {@link TipoAtuacaoDetalhadaMagistradoEnum}
	 * 
	 * Este método foi criado para recuperar apenas o tipo de atuação detalhada
	 * do magistrado (@see {@link TipoAtuacaoDetalhadaMagistradoEnum}), seu id
	 * pessoal e seu nome de forma mais performática utilizando query nativa.
	 * 
	 * Importante observar que algumas relações podem não ser retornadas nesse
	 * pesquisa por não possuir regra em
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum#valueOf(String)}
	 * 
	 * @param processo
	 *            Processo utilizado para pesquisar os magistrados relacionados.
	 * 
	 * @return Um map contendo o {@link TipoAtuacaoDetalhadaMagistradoEnum} do
	 *         magistrado, onde cada registro recupera um {@link Entry} cotendo
	 *         o id de usuário e o nome do magistrado.
	 */
	public Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> obterAtuacaoDetalhadaMagistradosRelacionados(ProcessoTrf processo) {
		return getDAO().obterAtuacaoDetalhadaMagistradosRelacionados(processo);
	}
		
	/**
	 * Método responsável por recuperar as atuações dos magistrados relacionados
	 * ao processo de forma mais performática (query nativa), obtendo apenas a
	 * atuação {@link TipoAtuacaoDetalhadaMagistradoEnum} e identificações do
	 * magistrado id {@link Integer} e seu nome {@link String}.
	 * 
	 * Caso não haja vinculação de relator e revisor ao processo o método irá
	 * buscar as informações do magistrado buscando o responsável pelo OJ setado
	 * no processo {@link ProcessoTrf#getOrgaoJulgador()} para o Relator e
	 * {@link ProcessoTrf#getOrgaoJulgadorRevisor()} para o revisor
	 * 
	 * @see #obterAtuacaoDetalhadaMagistradosRelacionados(ProcessoTrf)
	 * @see #adicionarRelatorAtuacaoDetalhada(ProcessoTrf, Map)
	 * @see #adicionarRevisorAtuacaoDetalhada(ProcessoTrf, Map)
	 * @see #ordernarAtuacoesDetalhadas(Map)
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} que será verificado os magistrado
	 *            atuantes.
	 * 
	 * @return um {@link Map} contendo o identificador da atuação como
	 *         chave(key) {@link TipoAtuacaoDetalhadaMagistradoEnum}
	 *         correspondente a um {@link Entry} como value, onde estão o id
	 *         {@link Integer} e o nome {@link String} do magistrado atuante.
	 */
	public Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> obterAtuacaoDetalhadaMagistradosDoProcesso(ProcessoTrf processo) {		
		Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> atuacaoDetalhadaMagistrados = obterAtuacaoDetalhadaMagistradosRelacionados(processo);						
		if (atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR) == null) {
			adicionarRelatorAtuacaoDetalhada(processo, atuacaoDetalhadaMagistrados);
		} 		
		if (atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.REVISOR) == null) {
			adicionarRevisorAtuacaoDetalhada(processo, atuacaoDetalhadaMagistrados);
		}		
		return ordernarAtuacoesDetalhadas(atuacaoDetalhadaMagistrados);
	}
	
	
	public String recuperarNomeMagistradoRepresentante(Integer idOrgaoJulgador, ProcessoTrf processo){
		String nomeMagistradoRepresentante = "";
		
		List<ProcessoMagistrado> magistradosRepresentantes = obterMagistradosRelacionados(processo, null, null, null, true, idOrgaoJulgador);
		if (!magistradosRepresentantes.isEmpty()){
			nomeMagistradoRepresentante = magistradosRepresentantes.get(0).getMagistrado().getNome();
		}
		else{
			Entry<Integer, String> magistradoResponsavel = orgaoJulgadorDAO.obterResponsavel(idOrgaoJulgador, processo.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado(), null);
			if (magistradoResponsavel != null){
				nomeMagistradoRepresentante = magistradoResponsavel.getValue();
			}
		}
		
		return nomeMagistradoRepresentante;
	}
	
	/**
	 * Método responsáve por adicionar as informações de relator nas informações
	 * detalhadas quando este ainda não registrou vinculação.
	 * 
	 * Irá verificar se existe algum relator setado no
	 * {@link ProcessoTrf#getPessoaRelator()}, se não encontrar registro então
	 * irá buscar o responsável pelo OJ setado em
	 * {@link ProcessoTrf#getOrgaoJulgador()}
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} para busca do relator.
	 * 
	 * @param atuacaoDetalhadaMagistrados
	 *            {@link Map} onde será adicionado o magistrado relator.
	 */
	private void adicionarRelatorAtuacaoDetalhada(ProcessoTrf processo, Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> atuacaoDetalhadaMagistrados) {		
		if (processo.getPessoaRelator() != null) {
			atuacaoDetalhadaMagistrados.put(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR,
					new AbstractMap.SimpleEntry<Integer, String>(processo.getPessoaRelator().getIdUsuario(), processo.getPessoaRelator().getNome()));
		} else {
			Entry<Integer, String> responsavelOJ = orgaoJulgadorDAO.obterResponsavel(processo.getOrgaoJulgador(), processo.getOrgaoJulgadorColegiado(), new Date());
			atuacaoDetalhadaMagistrados.put(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR, responsavelOJ);
		}
	}
	
	/**
	 * Método responsáve por adicionar as informações de revisor nas informações
	 * detalhadas quando este ainda não registrou vinculação.
	 * 
	 * Irá buscar o responsável pelo OJ setado em
	 * {@link ProcessoTrf#getOrgaoJulgadorRevisor()}
	 * 
	 * @param processo
	 *            {@link ProcessoTrf} para busca do revisor.
	 * 
	 * @param atuacaoDetalhadaMagistrados
	 *            {@link Map} onde será adicionado o magistrado revisor.
	 */
	private void adicionarRevisorAtuacaoDetalhada(ProcessoTrf processo, Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> atuacaoDetalhadaMagistrados) {		
		if (processo.getOrgaoJulgadorRevisor() != null) {
			Entry<Integer, String> responsavelOJ = orgaoJulgadorDAO.obterResponsavel(processo.getOrgaoJulgadorRevisor(), processo.getOrgaoJulgadorColegiado(), new Date());
			atuacaoDetalhadaMagistrados.put(TipoAtuacaoDetalhadaMagistradoEnum.REVISOR, responsavelOJ);
		}
	}
	
	/**
	 * Método resposável por ordernar a lista de atuações do magistrado de
	 * acordo com a prioridade:
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum#RELATOR}
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum#RELATOR_CONVOCADO}
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum#RELATOR_DESIGNADO}
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum#REVISOR}
	 * {@link TipoAtuacaoDetalhadaMagistradoEnum#REVISOR_CONVOCADO}
	 * 
	 * @param atuacaoDetalhadaMagistrados
	 * 			{@link Map} com as informações a serem ordenadas.
	 * @return
	 * 			um novo {@link Map} ordenado.
	 */
	private Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> ordernarAtuacoesDetalhadas(
			Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> atuacaoDetalhadaMagistrados) {
		Map<TipoAtuacaoDetalhadaMagistradoEnum,  Entry<Integer, String>> retorno = new LinkedHashMap<TipoAtuacaoDetalhadaMagistradoEnum,  Entry<Integer, String>>();		
		adicionarAtuacaoAoMap(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR, atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR), retorno);
		adicionarAtuacaoAoMap(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR_CONVOCADO, atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR_CONVOCADO), retorno);
		adicionarAtuacaoAoMap(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR_DESIGNADO, atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.RELATOR_DESIGNADO), retorno);
		adicionarAtuacaoAoMap(TipoAtuacaoDetalhadaMagistradoEnum.REVISOR, atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.REVISOR), retorno);
		adicionarAtuacaoAoMap(TipoAtuacaoDetalhadaMagistradoEnum.REVISOR_CONVOCADO, atuacaoDetalhadaMagistrados.get(TipoAtuacaoDetalhadaMagistradoEnum.REVISOR_CONVOCADO), retorno);
		return retorno;
	}

	/**
	 * método auxiliar para verificar se existe masgistrado vinculado antes de adicionar ao map
	 */
	private void adicionarAtuacaoAoMap(TipoAtuacaoDetalhadaMagistradoEnum tipoAtuacao,
			Entry<Integer, String> magistrado,
			Map<TipoAtuacaoDetalhadaMagistradoEnum, Entry<Integer, String>> mapParaAdicionar) {
		if (magistrado != null && tipoAtuacao != null && mapParaAdicionar != null) {
			mapParaAdicionar.put(tipoAtuacao, magistrado);
		}
	}
	
	/**
	 * Recupera o nome do magistrado que está como relator do processo.
	 * 
	 * @param processo {@link ProcessoTrf} onde será verificado o relator.
	 * @return String com o nome do magistado que representa o relator do processo.
	 */
	public String obterNomeRelator(ProcessoTrf processo) {
		Entry<Integer, String> relatorVinculado = this.getDAO().obterIdentificadorMagistradoUltimaVinculacao(
				processo, null, TipoAtuacaoMagistradoEnum.RELAT, null);

		if (relatorVinculado == null) {
			relatorVinculado = this.obterResponsavelNaoVinculado(processo);
		}

		return relatorVinculado != null ? relatorVinculado.getValue() : StringUtils.EMPTY;
	}
	
	/**
	 * Recupera a {@link PessoaMagistrado} que está como relator do processo.
	 * 
	 * @param processo {@link ProcessoTrf} onde será verificado o relator.
	 * @return {@link PessoaMagistrado} que representa o relator do processo.
	 */
	public PessoaMagistrado obterRelator(ProcessoTrf processo) {
		PessoaMagistrado relator = null;

		Entry<Integer, String> relatorVinculado = getDAO().obterIdentificadorMagistradoUltimaVinculacao(
				processo, null, TipoAtuacaoMagistradoEnum.RELAT, null);

		if (relatorVinculado == null) {
			relatorVinculado = obterResponsavelNaoVinculado(processo);
		}

		if (relatorVinculado != null) {
			Search search = new Search(PessoaMagistrado.class);
			addCriteria(search, Criteria.equals("idUsuario", relatorVinculado.getKey()));
			relator = (PessoaMagistrado)list(search).get(0);
		}

		return relator;
	}
	
	public List<Integer> obterResponsaveis(ProcessoTrf processo) {
		return getDAO().obterResponsaveis(processo);
	}

	
	/**
	 * Método responsável por desativar todas as vinculações ativas do processo.
	 * Seta o atributo in_ativo para false em todas as vinculações. Útil em
	 * casos de redistribuição de processos
	 */
	public void desativarVinculacoes(ProcessoTrf processo) {
		List<ProcessoMagistrado> magistradosRelacionados = obterMagistradosRelacionados(processo, null, null, null, true);
		if (magistradosRelacionados != null) {
			for (ProcessoMagistrado processoMagistrado : magistradosRelacionados) {
				processoMagistrado.setAtivo(false);
				getDAO().persist(processoMagistrado);
			}
		}
	}
	
	public void removerVinculacoes(ProcessoTrf processo){
		getDAO().removerVinculacoes(processo);
	}
	
	
	/**
	 * Obtém o responsável pelo processo que não esteja vinculado ao processo na estrutura de 
	 * {@link ProcessoMagistrado}, indo pegar as informações em {@link ProcessoTrf#getPessoaRelator()} 
	 * ou o responsáve pelo OJ em {@link ProcessoTrf#getOrgaoJulgador()}
	 * 
	 * @param processo {@link ProcessoTrf} para busca.
	 * @return um {@link Entry} onde estão o id {@link Integer} e o nome {@link String} do magistrado
	 * 
	 */
	private Entry<Integer, String> obterResponsavelNaoVinculado(ProcessoTrf processo) {
		Entry<Integer, String> result = null;
		
		if (processo.getPessoaRelator() != null) {
			result = new AbstractMap.SimpleEntry<Integer, String>(processo.getPessoaRelator().getIdUsuario(), processo.getPessoaRelator().getNome());
		} else {
			result = orgaoJulgadorDAO.obterResponsavel(processo.getOrgaoJulgador(), processo.getOrgaoJulgadorColegiado(), new Date());
		}
		
		return result;
	}

	public List<ProcessoMagistrado> obterAssociacoesMagistradoSubstituto(SubstituicaoMagistrado substituicaoMagistrado) {
		return getDAO().obterAssociacoesMagistradoSubstituto(substituicaoMagistrado);
	}
	
}