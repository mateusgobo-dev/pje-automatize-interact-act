/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoParteSigiloDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.nucleo.enums.SigiloStatusEnum;

/**
 * Componente de gerenciamento de negócio da entidade {@link ProcessoParteSigilo}.
 * 
 * @author cristof
 *
 */
@Name("processoParteSigiloManager")
public class ProcessoParteSigiloManager extends BaseManager<ProcessoParteSigilo> {
	
	@In
	private ProcessoParteSigiloDAO processoParteSigiloDAO;
	
	@Override
	protected ProcessoParteSigiloDAO getDAO() {
		return processoParteSigiloDAO;
	}
	
	/**
	 * Cria um {@link ProcessoParteSigilo} novo para a parte indicada, independentemente de já
	 * existir um registro tal no sistema.
	 * 
	 * @param parte a parte que passará a ser sigilosa.
	 * @param responsavel o responsável pela criação do sigilo
	 * @param motivo o motivo da criação do sigilo
	 * @return o {@link ProcessoParteSigilo} não persistido em banco.
	 * @throws PJeBusinessException
	 */
	public ProcessoParteSigilo criar(ProcessoParte parte, PessoaFisica responsavel, String motivo) throws PJeBusinessException{
		ProcessoParteSigilo ret = new ProcessoParteSigilo();
		ret.setDataAlteracao(new Date());
		ret.setMotivo(motivo);
		ret.setProcessoParte(parte);
		ret.setStatus(SigiloStatusEnum.C);
		ret.setUsuarioCadastro(responsavel);
		return ret;
	}

	/**
	 * metodo responsavel por recuperar todos os @ProcessoParteSigilo da pessoa passada em parametro
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<ProcessoParteSigilo> recuperaProcessoParteSigilo(Pessoa _pessoa) {
		return processoParteSigiloDAO.recuperaProcessoParteSigilo(_pessoa);
	}
	
}
