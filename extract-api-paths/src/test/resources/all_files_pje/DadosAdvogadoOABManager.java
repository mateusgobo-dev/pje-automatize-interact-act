/**
 * CNJ - Conselho Nacional de Justiça
 * 
 * Data: 30/04/2015
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.pje.business.dao.DadosAdvogadoOABDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

/**
 * Classe responsável pelo acesso aos dados de um advogado.
 * 
 * @author Adriano Pamplona
 */
@Name(DadosAdvogadoOABManager.NAME)
public class DadosAdvogadoOABManager extends BaseManager<DadosAdvogadoOAB> {
	public static final String NAME = "dadosAdvogadoOABManager";

	@In
	private DadosAdvogadoOABDAO dadosAdvogadoOABDAO;

	/**
	 * Inclui os advogados que não estiverem presentes na tabela de dados de advogados.
	 * 
	 * @param advogados Lista de advogados.
	 */
	public void atualizar(List<DadosAdvogadoOAB> advogados) {
		
		if (!ProjetoUtil.isVazio(advogados)) {
			Date dataCadastro = new Date();
			
			for (DadosAdvogadoOAB advogado : advogados) {
				
				removerDadosAdvogadoOABExistente(advogado);
				advogado.setDataCadastro(dataCadastro);
				
				getDAO().persist(advogado);
			}
			
			getDAO().flush();
		}
	}
	
	/**
	 * Consulta os dados dos advogados pelo CPF, número de inscrição (oab) e a
	 * UF.
	 * 
	 * @param advogado Advogado com numCPF, numInscricao e UF.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(DadosAdvogadoOAB advogado) {
		List<DadosAdvogadoOAB> resultado = new ArrayList<DadosAdvogadoOAB>();

		if (advogado != null) {
			resultado = consultar(
					advogado.getNumCPF(),
					advogado.getNumInscricao(),
					advogado.getUf());
		}

		return resultado;
	}
	
	/**
	 * Consulta os dados dos advogados pelo CPF.
	 * 
	 * @param numCPF
	 *            CPF.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(String numCPF) {
		List<DadosAdvogadoOAB> resultado = new ArrayList<DadosAdvogadoOAB>();

		if (ProjetoUtil.isNaoVazio(numCPF)) {
			resultado = getDAO().consultar(
					InscricaoMFUtil.retiraMascara(numCPF));
		}

		return resultado;
	}

	/**
	 * Consulta os dados dos advogados pelo número de inscrição (oab) e a UF.
	 * 
	 * @param numInscricao
	 *            Número de inscrição (OAB).
	 * @param uf
	 *            UF do número de inscrição.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(String numInscricao, String uf) {
		List<DadosAdvogadoOAB> resultado = new ArrayList<DadosAdvogadoOAB>();

		if (ProjetoUtil.isNaoVazio(numInscricao, uf)) {
			resultado = getDAO().consultar(
					StringUtil.retiraZerosEsquerda(numInscricao),
					StringUtils.upperCase(uf));
		}

		return resultado;
	}

	/**
	 * Consulta os dados dos advogados pelo CPF, número de inscrição (oab) e a
	 * UF.
	 * 
	 * @param numCPF
	 *            CPF do advogado.
	 * @param numInscricao
	 *            Número de inscrição (OAB).
	 * @param uf
	 *            UF da inscrição.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultar(String numCPF, String numInscricao,
			String uf) {
		List<DadosAdvogadoOAB> resultado = new ArrayList<DadosAdvogadoOAB>();

		if (ProjetoUtil.isNaoVazio(numCPF, numInscricao, uf)) {
			resultado = getDAO().consultar(
					InscricaoMFUtil.retiraMascara(numCPF),
					StringUtil.retiraZerosEsquerda(numInscricao),
					StringUtils.upperCase(uf));
		}

		return resultado;
	}
	
	/**
	 * Consulta os dados dos advogados pelo CPF e a UF.
	 * 
	 * @param numCPF
	 *            CPF do advogado.
	 * @param uf
	 *            UF da inscrição.
	 * @return Lista de advogados.
	 */
	public List<DadosAdvogadoOAB> consultarPeloCPFeUF(String numCPF, String uf) {
		List<DadosAdvogadoOAB> resultado = new ArrayList<DadosAdvogadoOAB>();

		if (ProjetoUtil.isNaoVazio(numCPF, uf)) {
			resultado = getDAO().consultarPeloCPFeUF(
					InscricaoMFUtil.retiraMascara(numCPF),
					StringUtils.upperCase(uf));
		}

		return resultado;
	}

	/**
	 * Retorna true se existir dados de advogado para os parâmetros passados.
	 * 
	 * @param advogado Advogado com numCPF, numInscricao e uf.
	 * @return Booleano
	 */
	public Boolean isExiste(DadosAdvogadoOAB advogado) {
		Boolean resultado = Boolean.FALSE;

		if (advogado != null) {
			resultado = isExiste(
					advogado.getNumCPF(),
					advogado.getNumInscricao(),
					advogado.getUf());
		}

		return resultado;
	}
	
	/**
	 * Retorna true se existir dados de advogado para os parâmetros passados.
	 * 
	 * @param numCPF
	 *            CPF do advogado.
	 * @param numInscricao
	 *            Número de inscrição (OAB).
	 * @param uf
	 *            UF da inscrição.
	 * @return Booleano
	 */
	public Boolean isExiste(String numCPF, String numInscricao, String uf) {
		Boolean resultado = Boolean.FALSE;

		if (ProjetoUtil.isNaoVazio(numCPF, numInscricao, uf)) {
			resultado = getDAO().isExiste(
					InscricaoMFUtil.retiraMascara(numCPF),
					StringUtil.retiraZerosEsquerda(numInscricao),
					StringUtils.upperCase(uf));
		}

		return resultado;
	}

	
	/**
	 * Remove os advogados previamente cadastrados.
	 * 
	 * @param advogado
	 */
	protected void removerDadosAdvogadoOABExistente(DadosAdvogadoOAB advogado) {
		List<DadosAdvogadoOAB> advogadosCadastrados = consultarPeloCPFeUF(
				advogado.getNumCPF(), 
				advogado.getUf());
		getDAO().remove(advogadosCadastrados);
		getDAO().flush();
	}
	
	@Override
	protected DadosAdvogadoOABDAO getDAO() {
		return dadosAdvogadoOABDAO;
	}
	
	/**
	 * Recupera Dados Advogado OAB pela Pessoa Advogado
	 * @param advogado
	 * @return List<DadosAdvogadoOAB>
	 */
	public List<DadosAdvogadoOAB> findByPessoaAdvogado(PessoaAdvogado pessoaAdvogado){      
	    return dadosAdvogadoOABDAO.findByPessoaAdvogado(pessoaAdvogado);
	}
}
