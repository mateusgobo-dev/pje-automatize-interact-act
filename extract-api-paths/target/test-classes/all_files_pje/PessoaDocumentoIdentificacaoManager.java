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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.home.TipoDocumentoIdentificacaoHome;
import br.jus.cnj.pje.business.dao.PessoaDocumentoIdentificacaoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Pais;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;

/**
 * Componente de gerenciamento negocial da entidade {@link PessoaDocumentoIdentificacao}.
 * 
 * @author cristof
 *
 */
@Name(PessoaDocumentoIdentificacaoManager.NAME)
public class PessoaDocumentoIdentificacaoManager extends BaseManager<PessoaDocumentoIdentificacao> {
	
	public static final String NAME = "pessoaDocumentoIdentificacaoManager";
	public static final String RECEITA_FEDERAL = "Secretaria da Receita Federal do Brasil";
	
	@In
	private PessoaDocumentoIdentificacaoDAO pessoaDocumentoIdentificacaoDAO;
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected PessoaDocumentoIdentificacaoDAO getDAO() {
		return pessoaDocumentoIdentificacaoDAO;
	}
	
	/**
	 * Recupera todos os documentos identificadores ativos de uma dada pessoa.
	 * 
	 * @param pessoa a pessoa cujos documentos se pretende recuperar
	 * @return a lista de documentos ativos da pessoa.
	 */
	public List<PessoaDocumentoIdentificacao> pessoaDocumentoIdentificacaoList(Pessoa pessoa) {
		return pessoaDocumentoIdentificacaoDAO.getDocumentosAtivos(pessoa);
	}
	
	/**
	 * Recupera o documento identificador do tipo CPF que está ativo, não foi utilizado falsamente e
	 * que tem o código dado.
	 * 
	 * [PJEII-3199] Método responsável por recuperar a Pessoa Documento identificação através do cpf.
	 * 
	 * @param cpf o código identificador da inscrição
	 * @return o documento CPF dado, se existente, ativo e não utilizado falsamente, ou null, se não existir. 
	 */
	public List<PessoaDocumentoIdentificacao> findByNumeroDocumento(String nrDocumento){
		return pessoaDocumentoIdentificacaoDAO.findByNumeroDocumento(nrDocumento);
	}
	
	/**
	 * Recupera o documento identificador do tipo CPF que está ativo, não foi utilizado falsamente e
	 * que tem o código dado.
	 * 
	 * @param cpf o código identificador da inscrição
	 * @return o documento CPF dado, se existente, ativo e não utilizado falsamente, ou null, se não existir. 
	 */
	public PessoaDocumentoIdentificacao findByCPF(String cpf) throws PJeBusinessException {
		return pessoaDocumentoIdentificacaoDAO.findByCPF(cpf);
	}

	/**
	 * [PJEII-3199] Antes da inserção da Pessoa Física é necessário realizar a inserção da Pessoa Documentação.
	 * @param pessoa
	 * @param pessoaFisica
	 */
	public PessoaDocumentoIdentificacao preencherPessoaDocumentoIdentificacao(PessoaDocumentoIdentificacao 
			pessoa, PessoaFisica pessoaFisica, String cpf) {
		pessoa.setNome(pessoaFisica.getNome());
		pessoa.setAtivo(pessoaFisica.getAtivo());
		pessoa.setDataExpedicao(pessoaFisica.getDataCPF());
		pessoa.setNumeroDocumento(cpf);
		pessoa.setOrgaoExpedidor(RECEITA_FEDERAL);
		pessoa.setPais(pessoaFisica.getPaisNascimento());
		pessoa.setPessoa(pessoaFisica);
		pessoa.setTipoDocumento(TipoDocumentoIdentificacaoHome.getHome().getTipoDocumentoIdentificacao("CPF"));
		pessoa.setDocumentoPrincipal(true);
		return pessoa;
	}
	
	/**
	 * Prepara um documento identificador do tipo CPF para a pessoa indicada.
	 * 
	 * @param pessoa a pessoa 
	 * @param pessoaFisica
	 */
	public PessoaDocumentoIdentificacao preparaDocumentoIdentificador(Pessoa pessoa, String codigoDocumento, TipoDocumentoIdentificacao tipo, boolean principal, 
				Date dataExpedicao, String expedidor, Pais paisExpedidor) {
		PessoaDocumentoIdentificacao doc = new PessoaDocumentoIdentificacao();
		doc.setAtivo(true);
		doc.setDataExpedicao(dataExpedicao);
		doc.setDocumentoPrincipal(principal);
		doc.setNumeroDocumento(codigoDocumento);
		doc.setNome(pessoa.getNome());
		doc.setOrgaoExpedidor(expedidor);
		doc.setPais(paisExpedidor);
		doc.setPessoa(pessoa);
		doc.setTipoDocumento(tipo);
		return doc;
	}
	
	/**
	 * Recupera o documento ativo e não utilizado falsamente do tipo dado que tem o código de documento informado.
	 * 
	 * @param codigoDocumento o código do documento
	 * @param tipo o tipo de documento que se pretende recuperar
	 * @return o documento identificador indicado, ou nulo se inexistente.
	 * @throws PJeBusinessException caso tenha havido algum erro na recuperação, como quando há mais de um documento nas condições dadas
	 */
	public PessoaDocumentoIdentificacao recuperaDocumento(String codigoDocumento, TipoDocumentoIdentificacao tipo) throws PJeBusinessException{
		return pessoaDocumentoIdentificacaoDAO.recuperaDocumento(codigoDocumento, tipo);
	}

	/**
	 * Recupera a lista de documentos não usados falsamente do tipo dado que pertencem à pessoa.
	 * 
	 * @param pessoa a pessoa a que pertencem os documentos
	 * @param tipo o tipo de documento de interesse
	 * @param incluirInativos marca indicativa de que se pretende recuperar também os documentos
	 * inativos do tipo dado vinculado à pessoa.
	 * @return a lista de documentos
	 */
	public List<PessoaDocumentoIdentificacao> recuperaDocumentos(Pessoa pessoa, TipoDocumentoIdentificacao tipo, boolean incluirInativos) throws PJeBusinessException{
		return pessoaDocumentoIdentificacaoDAO.recuperaDocumentos(pessoa, tipo, incluirInativos);
	}
	
	public List<PessoaDocumentoIdentificacao> recuperaDocumentos(String codigoDocumento, TipoDocumentoIdentificacao tipo, boolean incluirInativos) throws PJeBusinessException{
		return pessoaDocumentoIdentificacaoDAO.recuperaDocumentos(codigoDocumento, tipo, incluirInativos);
	}
	
	/**
	 * Recupera o {@link PessoaDocumentoIdentificacao} principal da pessoa informada ou null se não encontrar nenhum documento
	 * @param pessoa
	 * @return {@link PessoaDocumentoIdentificacao} ou null se nenhum for encontrado
	 */
	public PessoaDocumentoIdentificacao recuperarDocumentoPrincipal(Pessoa pessoa){
		PessoaDocumentoIdentificacao documentoIdentificacao = null;
		
		List<PessoaDocumentoIdentificacao> listaDocs = this.pessoaDocumentoIdentificacaoDAO.recuperaDocumentos(pessoa, null, null, false, true);
		
		if(!CollectionUtilsPje.isEmpty(listaDocs)){
			documentoIdentificacao = listaDocs.get(0);
		}
		
		return documentoIdentificacao;
	}

	/**
	 * Recupera todos os documentos de identificação de uma dada pessoa.
	 * 
	 * @param pessoa A pessoa cujos documentos se pretende recuperar.
	 * @return Lista de documentos da pessoa.
	 */
	public List<PessoaDocumentoIdentificacao> recuperarDocumentos(Pessoa pessoa) {
		return pessoaDocumentoIdentificacaoDAO.recuperarDocumentos(pessoa);
	}
	
	/**
	 * Método responsável por recuperar os documentos de identificação temporarios.
	 * 
	 * @param partes Partes de um processo.
	 * @return {@link PessoaDocumentoIdentificacao}.
	 */
	public List<PessoaDocumentoIdentificacao> recuperarDocumentosTemporarios(List<ProcessoParte> partes) {
		List<Integer> idPessoas = new ArrayList<Integer>();
		
		for (ProcessoParte parte : partes) {
			idPessoas.add(parte.getPessoa().getIdPessoa());
		}
		
		return pessoaDocumentoIdentificacaoDAO.recuperarDocumentosTemporarios(idPessoas);
	}
	
	/**
	 * Método responsável por atualizar o valor do atributo 'nomeUsuarioLogin' da entidade 'PessoaDocumentoIdentificacao' 
 	 * de acordo com o valor presente no atributo 'nome' da entidade 'UsuarioLogin'.
	 * 
	 * @param pessoa Pessoa.
	 */
	public void atualizarNomePessoaDocumento(Pessoa pessoa) {
		pessoaDocumentoIdentificacaoDAO.atualizarNomePessoaDocumento(pessoa);
	}
	
	/**
	 * Método responsável por verificar se existe um tipo de documento marcado como principal associado à pessoa. 
	 * 
	 * @param pessoaDocumentoIdentificacao {@link PessoaDocumentoIdentificacao}
	 * @return Verdadeiro se existe um tipo de documento marcado como principal associado à pessoa. Falso, caso contrário.
	 */
	public boolean verificarExisteTipoDocumentoPrincipalAssociado(
			PessoaDocumentoIdentificacao pessoaDocumentoIdentificacao) throws PJeBusinessException {
		
		boolean resultado = false;
		List<PessoaDocumentoIdentificacao> documentos = recuperaDocumentos(
				pessoaDocumentoIdentificacao.getPessoa(), pessoaDocumentoIdentificacao.getTipoDocumento(), false);
		
		for (PessoaDocumentoIdentificacao documento : documentos) {
			if (!documento.equals(pessoaDocumentoIdentificacao) && Boolean.TRUE.equals(documento.getDocumentoPrincipal())) {
				resultado = true;
				break;
			}
		}
		return resultado;
	}
	
	public List<PessoaDocumentoIdentificacao> recuperarDocumentosIdentificacaoPaginados(Integer idPessoa, Boolean ativo, Integer page, Integer pageSize){
		return this.pessoaDocumentoIdentificacaoDAO.recuperarDocumentosIdentificacaoPaginados(idPessoa, ativo, page, pageSize);
	}

	public PessoaDocumentoIdentificacao obterDocumentoCpfPessoa(Pessoa pessoa) {
		return pessoaDocumentoIdentificacaoDAO.obterDocumentoCpfPessoa(pessoa);
	}


}
