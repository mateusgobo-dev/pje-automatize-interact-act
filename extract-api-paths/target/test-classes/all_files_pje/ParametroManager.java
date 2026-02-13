package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ParametroDAO;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.Pessoa;

@Name("parametroManager")
public class ParametroManager extends BaseManager<Parametro> {
	
	@In
	ParametroDAO parametroDAO;
	
	@Override
	protected BaseDAO<Parametro> getDAO() {
		return parametroDAO;
	}

	/**
	 * metodo responsavel por recuperar todos os parametros cadastrados pela pessoa passada em parametro
	 * @param pessoaSecundaria
	 * @return
	 * @throws Exception 
	 */
	public List<Parametro> recuperarParametrosCadastrados(Pessoa pessoa) throws Exception {
		return parametroDAO.recuperarParametrosCadastrados(pessoa);
	}

	/**
	 * metodo responsavel por recuperar o Parametro com o ID passado em parametro.
	 * caso nao encontre, nao lança exceçao
	 * @param parseInt
	 * @return
	 */
	public Parametro recuperaParametro(Integer idParametro) {
		return parametroDAO.find(idParametro);
	}

	public void atualizaDataHoraUltimaExecucaoJOB(String dataDaUltimaExecucaoJOB) throws Exception {
		parametroDAO.atualizaDataHoraUltimaExecucaoJOB(dataDaUltimaExecucaoJOB);
	}
}