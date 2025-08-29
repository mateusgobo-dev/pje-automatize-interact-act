package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;

@Name(ProcessoManager.NAME)
public class ProcessoManager extends BaseManager<Processo>{
	public static final String NAME = "processoManager";
	
	@In
	private ProcessoDAO processoDAO;

	@Override
	protected ProcessoDAO getDAO() {
		return processoDAO;
	}
	
	
	public Processo findByNumeroProcesso(String numeroProcesso){
		return getDAO().findByNumeroProcesso(numeroProcesso);
	}


	/**
	 * metodo responsavel por recuperar os processos cadadadastrados pela pessoa passada em parametro, 
	 * que estejam devidamente protocolados
	 * @param _pessoaCadastradora
	 * @return
	 * @throws Exception 
	 */
	public List<Processo> recuperarProcessosProtocolados(Pessoa _pessoaCadastradora) throws Exception {
		return processoDAO.recuperarProcessos(_pessoaCadastradora, true);
	}


	/**
	 * metodo responsavel por recuperar o processo pelo ID.
	 * caso nao encontre, retonar null
	 * @param idProcesso
	 * @return
	 */
	public Processo recuperaProcesso(Integer idProcesso) {
		return processoDAO.find(idProcesso);
	}
	
	public List<Integer> recuperarIdsProcessosPorNumero(List<String> processos) {
		return processoDAO.recuperarIdsProcessosPorNumero(processos);
	}
}
