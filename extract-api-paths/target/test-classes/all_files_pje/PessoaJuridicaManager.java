package br.com.infox.pje.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.RamoAtividadeHome;
import br.com.infox.cliente.home.TipoDocumentoIdentificacaoHome;
import br.com.infox.cliente.home.TipoPessoaHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.trf.webservice.ConsultaClienteWebService;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PJeDAOExceptionFactory;
import br.jus.cnj.pje.business.dao.PessoaJuridicaDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AbstractUsuarioManager;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.RamoAtividade;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

@Name(PessoaJuridicaManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate

public class PessoaJuridicaManager extends AbstractUsuarioManager<PessoaJuridica, PessoaJuridicaDAO> {

	public static final String NAME = "pessoaJuridicaManager";

	@In
	private PessoaJuridicaDAO pessoaJuridicaDAO;
	
	
	@In
	private CepManager cepManager;


	public PessoaJuridica findByCNPJ(String cnpj) {
		PessoaJuridica pessoaJuridica = null;
		
		if (InscricaoMFUtil.validarCpfCnpj(cnpj)) {
			pessoaJuridica = pessoaJuridicaDAO.findByCNPJ(InscricaoMFUtil.mascaraCnpj(cnpj));
		}
		
		return pessoaJuridica;
	}
	
	public List<PessoaJuridica> findByMultipleCNPJ(List<String> listaCNPJ){
		List<PessoaJuridica> listaPessoasJuridicas = new ArrayList<PessoaJuridica>();
		
		List<String> listaPesquisa = new ArrayList<String>();
		
		for(String cnpj : listaCNPJ) {
			if (InscricaoMFUtil.validarCpfCnpj(cnpj)) {
				listaPesquisa.add(InscricaoMFUtil.mascaraCnpj(cnpj));
			}
		}
		
		if(!CollectionUtilsPje.isEmpty(listaPesquisa)) {
			listaPessoasJuridicas = this.pessoaJuridicaDAO.findByMultipleCNPJ(listaPesquisa);
		}
		
		return listaPessoasJuridicas;
	}

	/**
	 * Consulta a pessoa jurídica pelo CNPJ, a consulta não leva em consideração se o documento 
	 * consultado é o principal ou não.
	 * 
	 * @param cnpj CNPJ
	 * @return PessoaJuridica
	 */
	public PessoaJuridica findByDocumentoCNPJ(String cnpj) {
		PessoaJuridica pessoaJuridica = null;

		if (InscricaoMFUtil.validarCpfCnpj(cnpj)) {
			pessoaJuridica = pessoaJuridicaDAO.findByDocumentoCNPJ(InscricaoMFUtil.mascaraCnpj(cnpj));
		}

		return pessoaJuridica;
	}

	public PessoaJuridica getPessoaJuridicaByNome(String nomePessoa) {
		return pessoaJuridicaDAO.getPessoaJuridicaByNome(nomePessoa);
	}

	@Override
	protected BaseDAO<PessoaJuridica> getDAO() {
		return pessoaJuridicaDAO;
	}

	@Override
	public PessoaJuridica persist(PessoaJuridica entity) throws PJeBusinessException {
		String cnpj = InscricaoMFUtil.retiraMascara(entity.getNumeroCNPJ());

		if (cnpj != null) {
			// se inserindo
			if (entity.getIdPessoa() == null) {
				// verifica se CNPJ ja esta sendo usado
				if (findByCNPJ(cnpj) != null) {
					throw new PJeBusinessException("pje.pessoaJuridicaManager.error.cnpjExistente", null, entity.getDocumentoCpfCnpj());
				}
			}
			// se nao tiver login coloca como sendo o cnpj
			if (entity.getLogin() == null || entity.getLogin().trim().isEmpty() || !entity.getLogin().equals(cnpj)) {
				entity.setLogin(cnpj);
			}
		} else {
			// Caso em que a pessoa jurídica não possui este documento
			entity.setLogin(String.valueOf(UUID.randomUUID()));
		}

		entity.setAtraiCompetencia(Boolean.FALSE);
		if (entity.getInTipoPessoa() == null) {
			entity.setInTipoPessoa(TipoPessoaEnum.J);
		}

		TipoPessoa tipoPessoaJuridica = ParametroUtil.instance().getTipoPessoaJuridica();
		if (entity.getTipoPessoa() == null) {
			entity.setTipoPessoa(tipoPessoaJuridica);
		}
		
		entity.setNomeFantasia(entity.getNomeFantasia());
		
		return super.persist(entity);
	}
	
	/**
	* Recupera as pessoas jurídicas conforme o nome passado como parâmetro
	* @param nomePessoaJuridica
	* @return retorna o objeto PessoaJuridica encontrado
	*/
	public PessoaJuridica pesquisarPorNomeOrgaoVinculacao(String nomePessoaJuridica){
		PessoaJuridica pj = null;
		
		if(nomePessoaJuridica != null){
			try{
				Search search = new Search(PessoaJuridica.class);
				
				if(nomePessoaJuridica != null){
					search.addCriteria(Criteria.equals("nome", nomePessoaJuridica));
				}
				
				if(list(search).size() > 0){
					pj = (PessoaJuridica) list(search).get(0);
				}
				
			}catch (NoSuchFieldException e) {
				throw PJeDAOExceptionFactory.getDaoException(e);
			}
		}
		return pj;
	}
	
	
	/** Recupera uma pessoa jurídica da receita federal a partir do cnpj passado como parâmetro e retorna uma entidade PessoaJuridica preenchida
	 * @param cnpj
	 * @param matriz caso seja um cnpj de filial, o objeto da matriz ao qual ele será relacionado é passado como parâmetro. Caso contrário, o parâmetro matriz é passado como null
	 * @return PessoaJuridica montada a partir dos dados recuperados da receita
	 * @throws Exception 
	**/
	public PessoaJuridica recuperaPessoaJuridicaPelaReceita(String cnpj, PessoaJuridica matriz) throws Exception{
		PessoaJuridica pessoaJuridica = null;
		DadosReceitaPessoaJuridica dadosReceitaPJ = null;
		
		dadosReceitaPJ = (DadosReceitaPessoaJuridica) ConsultaClienteWebService.instance().consultaDados(TipoPessoaEnum.J, cnpj, true);
		
		if( dadosReceitaPJ != null ) {
			pessoaJuridica = new PessoaJuridica();

			// Documentos de identificacao
			pessoaJuridica.setNome(dadosReceitaPJ.getRazaoSocial());
			pessoaJuridica.setNomeFantasia(dadosReceitaPJ.getNomeFantasia());
			pessoaJuridica.setDataAbertura(dadosReceitaPJ.getDataRegistro());
			pessoaJuridica.setNumeroCNPJ(InscricaoMFUtil.mascaraCnpj(cnpj));
			pessoaJuridica.setPessoaJuridicaMatriz(matriz);
			
			if(matriz != null){
				pessoaJuridica.setMatriz(Boolean.FALSE);
			} else {
				pessoaJuridica.setMatriz(Boolean.TRUE);
			}
	
			// Endereco
			cepManager = (CepManager)Component.getInstance("cepManager");
			Cep cep = cepManager.findByCep(dadosReceitaPJ.getNumCep());
			
			if (cep != null){
				Endereco endereco = new Endereco();
				endereco.setCep(cep);
				endereco.setNomeBairro(dadosReceitaPJ.getDescricaoBairro());
				endereco.setNomeLogradouro(dadosReceitaPJ.getDescricaoLogradouro());
				endereco.setNumeroEndereco(dadosReceitaPJ.getNumLogradouro());
				endereco.setComplemento(dadosReceitaPJ.getDescricaoComplemento());
				endereco.setUsuario(pessoaJuridica);
				pessoaJuridica.getEnderecoList().add(endereco);
			}
	
			/* DADOS INCLUIDOS POR CAUSA DA AMARRACAO DE USUARIO COM PESSOA */
			pessoaJuridica.setLogin(InscricaoMFUtil.retiraMascara(cnpj));
			pessoaJuridica.setStatusSenha(StatusSenhaEnum.I);
			pessoaJuridica.setAtivo(true);
			pessoaJuridica.setBloqueio(false);
			pessoaJuridica.setProvisorio(false);
			pessoaJuridica.setInTipoPessoa(TipoPessoaEnum.J);
			
			TipoPessoa tipoPessoa = TipoPessoaHome.instance().buscarPorCodigo(dadosReceitaPJ.getCodigoNaturezaJuridica());
			if(tipoPessoa != null) {
				pessoaJuridica.setTipoPessoa(tipoPessoa);
				TipoPessoa tmpTP = tipoPessoa;
				do{
					if(tmpTP.getCodTipoPessoa() != null && tmpTP.getCodTipoPessoa().equals("ADMP")){
						pessoaJuridica.setOrgaoPublico(true);
						break;
					}
				}while((tmpTP = tmpTP.getTipoPessoaSuperior()) != null);
			}
			else {
				pessoaJuridica.setTipoPessoa(ParametroUtil.instance().getTipoPessoaJuridica());
			}
			
			RamoAtividade ramoAtividade = RamoAtividadeHome.instance().buscarPorCodigo(dadosReceitaPJ.getCodigoCnaeFiscal());
			pessoaJuridica.setRamoAtividade(ramoAtividade);
		}
		pessoaJuridica.getPessoaDocumentoIdentificacaoList().add(criarDocumentoIdentificacao(pessoaJuridica));
		
		return pessoaJuridica;
	}
	
 	
 	/**
 	 * Mtodo responsvel por criar um {@link PessoaDocumentoIdentificacao} de acordo com a pessoa jurdica informada.
 	 * 
 	 * @param pessoaJuridica Pessoa jurdica.
 	 * @return Um {@link PessoaDocumentoIdentificacao} de acordo com a pessoa jurdica informada.
 	 */
 	private PessoaDocumentoIdentificacao criarDocumentoIdentificacao(PessoaJuridica pessoaJuridica) {
 		PessoaDocumentoIdentificacao documento = new PessoaDocumentoIdentificacao();
 		documento.setAtivo(Boolean.TRUE);
 		documento.setPessoa(pessoaJuridica);
 		documento.setNome(pessoaJuridica.getNome());
 		documento.setNumeroDocumento(pessoaJuridica.getNumeroCNPJ());
 		documento.setUsadoFalsamente(false);
 		documento.setDocumentoPrincipal(true);
 		documento.setAtivo(true);
 		documento.setTipoDocumento(TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao(TipoDocumentoIdentificacaoHome.tipoCPJ));
 		documento.setOrgaoExpedidor("Secretaria da Receita Federal");
 		
 		return documento;
 	}

	/**
	 * metodo responsavel por buscar no banco de dados a PessoaJuridica da pessoa passada em parametro.
	 * nao dispara exceçao caso nao encontre o objeto, retornando null.
	 * @param pessoa
	 * @return PessoaFisica / null
	 */
	public PessoaJuridica encontraPessoaJuridicaPorPessoa(Pessoa pessoa) {
		 return pessoaJuridicaDAO.find(pessoa.getIdPessoa());
	}
	
	/**
 	 * Recupera pessoa juridica pela ID
 	 * @param idPessoaJuridica
 	 * @return
 	 */
 	public PessoaJuridica recuperaPessoaJuridica(Integer idPessoaJuridica) {
 		return pessoaJuridicaDAO.recuperaPessoaJuridicaPelaID(idPessoaJuridica);
 	}
 	
 	/**
 	 * recupera as pessoas juridicas pelo nome fantasia
 	 * @param nomeFantasia
 	 * @return
 	 */
 	public List<PessoaJuridica> recuperaPessoasJuridicasPorNomeFantasia(String nomeFantasia) {
 		return pessoaJuridicaDAO.recuperaPessoasJuridicasPorNomeFantasia(nomeFantasia);
 	}
 
 	/**
 	 * metodo auxiliar que recupera as pessoas juridicas pela data de abertura
 	 * @param dataAbertura
 	 * @return
 	 */
 	public List<PessoaJuridica> recuperaPessoasJuridicasPorDataAbertura(Date dataAbertura) {
 		return pessoaJuridicaDAO.recuperaPessoasJuridicasPorDataAbertura(dataAbertura);
 	}
 	
 	/**
 	 * recupera as pessoas juridicas por uma lista de ID´s
 	 * @param idsPessoasJuridicas
 	 * @return List<PessoaJuridica> / lista vazia
 	 */
 	public List<PessoaJuridica> recuperarPessoasJuridicasPorListaID(List<Integer> idsPessoasJuridicas) {
 		List<PessoaJuridica> retorno = new ArrayList<PessoaJuridica>(0);
 		for (Integer id : idsPessoasJuridicas) {
 			retorno.add(recuperaPessoaJuridica(id));
 		}
 		return retorno;
 	}

	/**
 	 * metodo que verifica se existe uma instancia da pessoa passada em parametro no banco de dados da respectiva tabela
 	 * @param pessoa
 	 * @return true / false
 	 */
 	public boolean verficarPessoaExisteTabelaPessoaJuridica(Pessoa pessoa) {
 		return (encontraPessoaJuridicaPorPessoa(pessoa) != null ? Boolean.TRUE:Boolean.FALSE);
 	}
 	
 	public List<PessoaJuridica> findByDocumentoCNPJOuNome(String cnpj, String nome){
 		return pessoaJuridicaDAO.findByDocumentoCNPJOuNome(cnpj, nome);
 	}

}