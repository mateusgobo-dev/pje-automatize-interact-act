package br.com.infox.pje.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaFisicaDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.AbstractUsuarioManager;
import br.jus.cnj.pje.nucleo.manager.PessoaNomeAlternativoManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaFisicaManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class PessoaFisicaManager extends AbstractUsuarioManager<PessoaFisica, PessoaFisicaDAO> {

	public static final String NAME = "pessoaFisicaManager";

	public PessoaFisica getPessoaFisicaByNome(String nomePessoa) {
		return getPessoaFisicaDAO().getPessoaFisicaByNome(nomePessoa);
	}

	public List<PessoaFisica> getServidores(OrgaoJulgador orgao) throws PJeBusinessException{
		return getPessoaFisicaDAO().getServidores(orgao);
	}
	
	public List<PessoaFisica> getServidores(OrgaoJulgadorColegiado orgao) throws PJeBusinessException{
		return getPessoaFisicaDAO().getServidores(orgao);
	}
	
	public PessoaFisica findByCPF(String cpf) {
		PessoaFisica pessoaFisica = null;
		
		if (InscricaoMFUtil.validarCpfCnpj(cpf)) {
			pessoaFisica = getPessoaFisicaDAO().findByCPF(InscricaoMFUtil.mascararCpf(cpf));
		}
		
		return pessoaFisica;
	}
	
	public List<PessoaFisica> findByMultipleCPF(List<String> listaCPF){
		List<PessoaFisica> listaPessoasFisicas = new ArrayList<PessoaFisica>();
		
		List<String> listaPesquisa = new ArrayList<String>();
		
		for(String cpf : listaCPF) {
			if (InscricaoMFUtil.validarCpfCnpj(cpf)) {
				listaPesquisa.add(InscricaoMFUtil.mascararCpf(cpf));
			}
		}
		
		if(!CollectionUtilsPje.isEmpty(listaPesquisa)) {
			listaPessoasFisicas = this.getPessoaFisicaDAO().findByMultipleCPF(listaPesquisa);
		}
		
		return listaPessoasFisicas;
	}	

	@Override
	protected BaseDAO<PessoaFisica> getDAO() {
		return ComponentUtil.getComponent(PessoaFisicaDAO.class);
	}
	
	private PessoaFisicaDAO getPessoaFisicaDAO() {
		return ComponentUtil.getComponent(PessoaFisicaDAO.class);
	}
	
	@Override
	public PessoaFisica persist(PessoaFisica entity)
			throws PJeBusinessException {
		
		String cpf = InscricaoMFUtil.retiraMascara(entity.getDocumentoCpfCnpj());
		
		if(cpf != null){
			//se inserindo
			if (entity.getIdPessoa() == null){
				//verifica se CPF ja esta sendo usado
				if(findByCPF((cpf)) != null){
					throw new PJeBusinessException(
							"pje.pessoaFisicaManager.error.cpfExistente", null,
							entity.getDocumentoCpfCnpj());
				}
			}

			//login sera sempre o CPF que este documento existir
			if(entity.getLogin() == null || entity.getLogin().trim().isEmpty() || !entity.getLogin().equals(cpf)){
				entity.setLogin(cpf);
			}
		} else {
			// Caso em que a parte não possui este documento
			entity.setLogin(UUID.randomUUID() + "");
		}
		
		if(entity.getInTipoPessoa() == null){
			entity.setInTipoPessoa(TipoPessoaEnum.F);
		}
		
		if(entity.getTipoPessoa() == null){
			entity.setTipoPessoa(ParametroUtil.instance().getTipoPessoaFisica());			
		}
		
		if(entity.getInTipoPessoa() != TipoPessoaEnum.F){
			throw new PJeBusinessException(
					"pje.pessoaFisicaManager.error.tipoPessoaInvalido", null,
					entity.getInTipoPessoa());
		}
		
		TipoPessoa tipoPessoaFisica = ParametroUtil.instance().getTipoPessoaFisica();
		if(entity.getTipoPessoa() != tipoPessoaFisica){
			throw new PJeBusinessException(
					"pje.pessoaFisicaManager.error.tipoPessoaInvalido", null,
					entity.getTipoPessoa());
		}
		
		return super.persist(entity);
	}
	 	
	public boolean isUsuarioPje(Pessoa pessoa) throws PJeBusinessException{
		if (pessoa != null) {
			PessoaFisica pessoaFisica = findById(pessoa.getIdPessoa());
			if(pessoaFisica != null && (pessoaFisica.getPessoaAdvogado() != null ||
					pessoaFisica.getPessoaAssistenteAdvogado() != null ||
					pessoaFisica.getPessoaProcurador() != null ||
					pessoaFisica.getPessoaAssistenteProcuradoria() != null ||
					pessoaFisica.getPessoaMagistrado() != null ||
					pessoaFisica.getPessoaPerito() != null ||
					pessoaFisica.getPessoaServidor() != null ||
					pessoaFisica.getPessoaOficialJustica() != null || 	
					(Authenticator.getPessoaLogada().getAssinatura() != null || Authenticator.getPessoaLogada().getCertChain() != null))){

				return true; 
			}
		}
		return false;
	 }

	/**
	 * Retorna uma lista com os ID's das pessoas físicas menores de idade 
	 * que se enquadrem nos parametros passados
	 * 
	 * 
	 * @link	http://www.cnj.jus.br/jira/browse/PJESPRTII-2
	 * @param   nome	Nome da pessoa a ser pesquisado
	 * @param   documento	CPF da pessoa a ser pesquisado 
	 * @return  Lista de ID's de pessoas físicas menores de idade encotrados
	*/
	public List<Integer> getMenores(String nome, String cpf) throws Exception{
		List<Integer> menores = null;
		try {

			Search menoresPesquisa = new Search(PessoaFisica.class);

			ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
			menoresPesquisa.addCriteria(processoTrfManager.getCriteriasPesquisaNomeDocumento(nome, cpf));
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.YEAR, -18);
			Date data18AnosAtras = calendar.getTime();

			
			menoresPesquisa.setRetrieveField("idPessoa");
			menoresPesquisa.setDistinct(true);
			menoresPesquisa.addCriteria(Criteria.greater("dataNascimento", data18AnosAtras));

			menores = list(menoresPesquisa);
		} catch (NoSuchFieldException e) {
			throw new Exception("Erro ao buscar lista de menores");
		}
		
		
		return menores;
	}
  	
	/**
	 * metodo responsavel por buscar no banco de dados a PessoaFisica da pessoa passada em parametro.
	 * nao dispara exceçao caso nao encontre o objeto, retornando null.
	 * @param pessoa
	 * @return PessoaFisica / null
	 */
  	public PessoaFisica encontraPessoaFisicaPorPessoa(Pessoa pessoa) {
  		return getPessoaFisicaDAO().find(pessoa.getIdPessoa());
  	}
	/**
 	 * metodo auxiliar que recupera do banco de dados pessoas fisicas pela data de nascimento
 	 * @param dataNascimento
 	 * @return
 	 */
 	public List<PessoaFisica> recuperaPessoasFisicasPorDataNascimento(Date dataNascimento) {
 		return getPessoaFisicaDAO().recuperaPessoasFisicasPorDataNascimento(dataNascimento);
 	}
 	
	/**
 	 * metodo auxiliar que recupera do banco de dados pessoas fisicas pelo nome alternativo
 	 * @param nomeAlternativo
 	 * @return
 	 */
 	public List<Pessoa> recuperaPessoasFisicasPorNomeAlternativo(String nomeAlternativo) {
 		List<Pessoa> pessoasProprietariasNomesAlternativos = null;
 		
 		List<PessoaNomeAlternativo> nomesAlternativos = ComponentUtil.getComponent(PessoaNomeAlternativoManager.class).recuperaNomesAlternativos(nomeAlternativo);
 		if(nomesAlternativos != null && !nomesAlternativos.isEmpty()) {
 			pessoasProprietariasNomesAlternativos = new ArrayList<Pessoa>(0);
 			
 			for (PessoaNomeAlternativo pessoaNomeAlternativo : nomesAlternativos) {
 				if(!pessoasProprietariasNomesAlternativos.contains(pessoaNomeAlternativo.getPessoa())) {
 					pessoasProprietariasNomesAlternativos.add(pessoaNomeAlternativo.getPessoa());
 				}
 			}
 		}
 		return pessoasProprietariasNomesAlternativos;
 	}

	/**
 	 * metodo que verifica se existe uma instancia da pessoa passada em parametro no banco de dados da respectiva tabela
 	 * @param pessoa
 	 * @return true / false
 	 */
 	public boolean verficarPessoaExisteTabelaPessoaFisica(Pessoa pessoa) {
 		return (encontraPessoaFisicaPorPessoa(pessoa) != null ? Boolean.TRUE:Boolean.FALSE);
 	}
	
	/**
	 * Método responsável por recuperar o  magistrado presente na sessão de julgamento.
	 * @param mapaSessaoComposicao
	 * @param orgaoJulgador
	 * @return pessoaFisica
	 */
	private PessoaFisica getPessoaFisicaMagistradoPresenteSessao(Map<OrgaoJulgador, PessoaMagistrado> mapaSessaoComposicao, OrgaoJulgador orgaoJulgador) {
		return mapaSessaoComposicao.get(orgaoJulgador).getPessoa();
	}
	
	/**
	 * Método responsável por recuperar os dados do magistrado relator vencedor do julgamento processo especificado.
	 * @param mapaSessaoComposicao
	 * @param oJRelatorVencedor
	 * @return pessoaFisica
	 */
	public PessoaFisica getPessoaFisicatMagistradoRelatorVencedor(Map<OrgaoJulgador, PessoaMagistrado> mapaSessaoComposicao, OrgaoJulgador oJRelatorVencedor) {
		
		return getPessoaFisicaMagistradoPresenteSessao(mapaSessaoComposicao,oJRelatorVencedor);
	}
	
	/**
	 *  Método responsável por recuperar os dados do magistrado relator originário do processo especificado.
	 * @param mapaSessaoComposicao
	 * @param oJRelatorOriginario
	 * @return
	 */
	public PessoaFisica getPessoaFisicaMagistradoRelatorOriginario(Map<OrgaoJulgador, PessoaMagistrado> mapaSessaoComposicao, OrgaoJulgador oJRelatorOriginario) {
		return getPessoaFisicaMagistradoPresenteSessao(mapaSessaoComposicao,oJRelatorOriginario);
	}

	public boolean podeCadastrarNomeSocial(PessoaFisica pessoa) {
		boolean poloAtivo = false;
		if(ProcessoParteHome.instance().getPolo() != null) {
			poloAtivo = ProcessoParteHome.POLO_ATIVO.equals(ProcessoParteHome.instance().getPolo());
		}
		return poloAtivo || Authenticator.hasRole(Papeis.SERVIDOR) || (pessoa != null && Authenticator.getPessoaLogada().getIdUsuario().equals(pessoa.getIdUsuario()));
	}

	public String montaQueryProcessoParteNomeSocial(String nome) {
		return this.getPessoaFisicaDAO().montaQueryProcessoParteNomeSocial(nome);
	}
	
	public List<PessoaFisica> findByNomeAndCPF(String cpf, String nome) {		
		String numeroCpf = cpf.trim();		
		if (numeroCpf.length() > 0) {
			numeroCpf = InscricaoMFUtil.mascararCpf(cpf);
		}
		
		return getPessoaFisicaDAO().findByNomeAndCPF(numeroCpf, nome);
	}

}