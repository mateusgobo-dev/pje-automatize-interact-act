package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.jus.cnj.pje.business.dao.TipoPessoaDAO;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(TipoPessoaManager.NAME)
public class TipoPessoaManager extends BaseManager<TipoPessoa>{
	public static final String NAME = "tipoPessoaManager";
	
	@In
	private TipoPessoaDAO tipoPessoaDAO;
	
	@In(create=true)
 	private ParametroService parametroService;
	
	@In
 	private PessoaFisicaManager pessoaFisicaManager;
 	
 	@In
 	private PessoaJuridicaManager pessoaJuridicaManager;
 	
 	@In
 	private PessoaAutoridadeManager pessoaAutoridadeManager;

	@Override
	protected TipoPessoaDAO getDAO() {
		return tipoPessoaDAO;
	}

	/**
	 * Retorna o TipoPessoa pelo cd_tipo_pessoa
	 * 
	 * @param tipo
	 * @return 
	 */
	public TipoPessoa findByCdTipoPessoa(String tipo) {
		return tipoPessoaDAO.findByCdTipoPessoa(tipo);
	}

	  
 	/**
 	 * Método responsável por verificar se o tamanho do documento é condizente
 	 * com o {@link TipoPessoa}. Muitas pessoas esto cadastradas com o
 	 * {@link TipoPessoaEnum#J} porém o tamanho do documento condiz com um CPF.
 	 * Nesse caso o {@link TipoPessoaEnum} deve ser {@link TipoPessoaEnum#F}.
 	 * 
 	 * <br />
 	 * <br />
 	 * Este método não faz a validação dos dígitos do documento CPF/CNPJ.
 	 * 
 	 * @param documento
 	 *            número de documento do usuário
 	 * @return <code>TipoPessoaEnum</code> de acordo com o tamanho do documento.
 	 */
  	public TipoPessoaEnum tipoPessoaCondizenteComDocumento(String documento) {
  		return (InscricaoMFUtil.retiraMascara(documento).length() == InscricaoMFUtil.TAMANHO_CPF ? TipoPessoaEnum.F : TipoPessoaEnum.J);
  	}
	/**
 	 * Recupera o tipo de pessoa pelo ID
 	 * @param idTipoPessoa
 	 * @return
 	 */
 	public TipoPessoa recuperaTipoPessoa(int idTipoPessoa) {
 		return tipoPessoaDAO.recuperaTipoPessoa(idTipoPessoa);
 	}
 	
 	/**
 	 * metodo responsavel por obter o TipoPessoa pelo TipoPessoaEnum passado em parametro.
 	 * @param TipoPessoaEnum
 	 * @return TipoDePessoa correspondete
 	 */
 	public TipoPessoa obtemTipoPessoa(TipoPessoaEnum tipoPessoaEnum) {
 		TipoPessoa retorno = null;
 		if(tipoPessoaEnum != null) {
 			if(tipoPessoaEnum == TipoPessoaEnum.F) {
 				retorno = recuperaTipoPessoa(Integer.parseInt(parametroService.findByName("idTipoPessoaFisica").getValorVariavel()));
 			} else if(tipoPessoaEnum == TipoPessoaEnum.J) {
 				retorno = recuperaTipoPessoa(Integer.parseInt(parametroService.findByName("idTipoPessoaJuridica").getValorVariavel()));
 			} else if(tipoPessoaEnum == TipoPessoaEnum.A) {
 				retorno = recuperaTipoPessoa(Integer.parseInt(parametroService.findByName("idTipoPessoaEntidade").getValorVariavel()));
 			}
 		}
 		return retorno;
 	}
 	
 	/**
 	 * metodo responsavel por verificar se o tipo de pessoa setado esta corretamente lancado na respectiva tabela
 	 * ex. TipoPessoaEnum.F dever ter lanamento na tabela de pessoaFisica
 	 * @param pessoa
 	 */
 	public boolean verificarTipoPessoaCorreto(Pessoa pessoa) {
 		if(
 			(pessoa.getInTipoPessoa().equals(TipoPessoaEnum.F) && pessoaFisicaManager.verficarPessoaExisteTabelaPessoaFisica(pessoa)) || 
 			(pessoa.getInTipoPessoa().equals(TipoPessoaEnum.J) && pessoaJuridicaManager.verficarPessoaExisteTabelaPessoaJuridica(pessoa)) ||
 			(pessoa.getInTipoPessoa().equals(TipoPessoaEnum.A) && pessoaAutoridadeManager.verficarPessoaExisteTabelaEnteAutoridade(pessoa))) {
 			return true;
 		}else {
 			return false;
 		}
 	}
 	
 	/**
 	 * metodo que retornar o TipoPessoaEnum baseado se a pessoa existe nas tabelas principais (Pessoa fisica, juridica e ente-autoridade).
 	 * retorna o TipoPessoaEnum da tabela onde foi encontrado
 	 * ex. retorna TipoPessoaEnum.F se encontrar registros na tabela de pessoa fisica.
 	 * @param pessoaPrincipal
 	 * @return
 	 */
 	public TipoPessoaEnum recuperaTipoPessoaPorExistenciaTabela(Pessoa pessoaPrincipal) {
		TipoPessoaEnum retorno = null;
		boolean pessoaPrincipalFisica = pessoaFisicaManager.verficarPessoaExisteTabelaPessoaFisica(pessoaPrincipal);
		if(!pessoaPrincipalFisica) {
			boolean pessoaPrincipalJuridica = pessoaJuridicaManager.verficarPessoaExisteTabelaPessoaJuridica(pessoaPrincipal);
			if(!pessoaPrincipalJuridica) {
				boolean pessoaPrincipalAutoridade = pessoaAutoridadeManager.verficarPessoaExisteTabelaEnteAutoridade(pessoaPrincipal);
				if(pessoaPrincipalAutoridade) {
					retorno = TipoPessoaEnum.A;
				}
			}else {
				retorno = TipoPessoaEnum.J;
			}
		} else {
			retorno = TipoPessoaEnum.F;
		}	
		return retorno;
	}
}