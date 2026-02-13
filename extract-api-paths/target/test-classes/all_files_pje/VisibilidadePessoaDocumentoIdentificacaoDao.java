package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.VisibilidadePessoaDocumentoIdentificacao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;


@Name(VisibilidadePessoaDocumentoIdentificacaoDao.NAME)
@Scope(ScopeType.EVENT)
public class VisibilidadePessoaDocumentoIdentificacaoDao extends BaseDAO<VisibilidadePessoaDocumentoIdentificacao> {

	public static final String NAME = "visibilidadePessoaDocumentoIdentificacaoDao";
	private static String numeroIdenfificacaoTipo = "";

	@Override
	public Object getId(VisibilidadePessoaDocumentoIdentificacao e) {
		return e.getId();
	}

	public boolean existeVisibilidade(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		String jpql = "select p.id from VisibilidadePessoaDocumentoIdentificacao p where p.documento.idDocumentoIdentificacao = :documento and p.pessoa.idUsuario = :pessoa";
		try {
			getEntityManager()
			.createQuery(jpql)
			.setParameter("documento", documento.getIdDocumentoIdentificacao())
			.setParameter("pessoa", pessoa.getIdUsuario())
			.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}

	public boolean verificaSeDocumentoInformadoIgualAoCadastrado(Pessoa pessoa, PessoaDocumentoIdentificacao documento){
		String jpql = "select d.idDocumentoIdentificacao from PessoaDocumentoIdentificacao d " +
		"where d.tipoDocumento = :tipo " +
		"and d.pessoa = :pessoa " +
		"and d.ativo = true " +
		"and d.numeroDocumento = :numero";
		try {
			getEntityManager()
			.createQuery(jpql)
			.setParameter("tipo", documento.getTipoDocumento())
			.setParameter("pessoa", pessoa)
			.setParameter("numero", documento.getNumeroDocumento())
			.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}
	
	public boolean verificaSeExisteDocumentoInformado(Pessoa pessoa, PessoaDocumentoIdentificacao documento){
		String jpql = "select d.idDocumentoIdentificacao from PessoaDocumentoIdentificacao d " +
		"where d.tipoDocumento = :tipo " +
		"and d.pessoa = :pessoa " +
		"and d.ativo = true";		
		try {
			getEntityManager()
			.createQuery(jpql)
			.setParameter("tipo", documento.getTipoDocumento())
			.setParameter("pessoa", pessoa)			
			.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}
	
	/**
	* Método responsável por verificar se já existe um documento com os mesmo
	* dados de nome, tipo, numero, data de expedição e orgao expedidor
	* cadastrado.
	*
	* @param pessoa
	* @param documento
	* @return Boolean
	*/
	public PessoaDocumentoIdentificacao verificaSeDocumentoJaExistePorNomeTipoNumeroDtExpEOrgExp(Pessoa pessoa, PessoaDocumentoIdentificacao documento) {
		PessoaDocumentoIdentificacao auxResult = new PessoaDocumentoIdentificacao();
		StringBuilder sb = new StringBuilder("SELECT d FROM PessoaDocumentoIdentificacao d ");
		sb.append("WHERE d.tipoDocumento = :tipo ");
		sb.append("AND d.pessoa = :pessoa ");
		sb.append("AND d.numeroDocumento = :numeroDoc ");
		if (documento.getDataExpedicao() != null) {
			sb.append("AND d.dataExpedicao = :dtExp ");
		}
		if (documento.getOrgaoExpedidor() != null) {
			sb.append("AND d.orgaoExpedidor = :orgaoExp ");
		}
		try {
			Query query = this.getEntityManager().createQuery(sb.toString()).setParameter("tipo", documento.getTipoDocumento()).setParameter("pessoa", pessoa).setParameter("numeroDoc", formatarNumeroDocumento(documento));
			if (documento.getDataExpedicao() != null) {
				query.setParameter("dtExp", documento.getDataExpedicao());
			}
			if (documento.getOrgaoExpedidor() != null) {
				query.setParameter("orgaoExp", documento.getOrgaoExpedidor());
			}
			auxResult = (PessoaDocumentoIdentificacao) query.getSingleResult();
		} catch (NoResultException e) {
			e.getMessage();
		}
		return auxResult;
	}
	
	/**
	* Método responsável verificar se existe documentos ativos de um
	* determinado Tipo e Pessoa e que não seja o próprio documento
	*
	* @param documento
	* @param pessoa
	* @return <code>Boolean</code>
	 */
	 public Boolean verificaSeExisteDocumentoAtivoPorTipoPessoa(PessoaDocumentoIdentificacao documento, Pessoa pessoa) {
		 Boolean existe = Boolean.FALSE;
		 StringBuilder sb = new StringBuilder("SELECT COUNT(pdi) FROM PessoaDocumentoIdentificacao pdi ");
		 sb.append("WHERE pdi.idDocumentoIdentificacao != :idDoc ");
		 sb.append("AND pdi.tipoDocumento = :tipo ");
		 sb.append("AND pdi.pessoa = :pessoa ");
		 sb.append("AND pdi.ativo = true ");
		 if ((Long) this.getEntityManager().createQuery(sb.toString()).setParameter("idDoc", documento.getIdDocumentoIdentificacao()).setParameter("pessoa", pessoa).setParameter("tipo", documento.getTipoDocumento())
				 .getSingleResult() > 0) {
			 existe = Boolean.TRUE;
		 }
		 return existe;
	 }
	
	 /**
	 * Método responsável por verificar se existe o documento informado com um
	 * determinado número vinculado a outra pessoa.
	 * @param documento idependente do tipo
	 * @return true se documento é utilizado por outra pesso
	*/
	public Boolean verificaDisponibilidadeNumeroDocumento(PessoaDocumentoIdentificacao documento) {
		 Boolean existe = Boolean.FALSE;
		 StringBuilder sb = new StringBuilder("SELECT COUNT(pdi) FROM PessoaDocumentoIdentificacao pdi ")
		 .append("WHERE pdi.numeroDocumento = :numeroDoc ")
		 .append("AND pdi.tipoDocumento = :tipo ")
		 .append("AND pdi.pessoa.unificada = false "); 
		if (documento.getIdDocumentoIdentificacao() != 0) {
			 sb.append("AND pdi.idDocumentoIdentificacao != :idDoc ");
		}
		sb.append("AND pdi.pessoa IS NOT NULL AND pdi.pessoa.idUsuario != :idPessoa");
		Query query = this.getEntityManager().createQuery(sb.toString())
				 .setParameter("numeroDoc", this.formatarNumeroDocumento(documento))
				 .setParameter("tipo", documento.getTipoDocumento());
		 if (documento.getIdDocumentoIdentificacao() != 0) {
			 query.setParameter("idDoc", documento.getIdDocumentoIdentificacao());
		 }
	
		 if(documento.getPessoa().getIdUsuario() != 0) {
			 query.setParameter("idPessoa", documento.getPessoa().getIdUsuario());
		 }
	
		 if ((Long) query.getSingleResult() > 0) {
			 existe = Boolean.TRUE;
		 }
	
		 return existe;
	}
		
	/**
	 * metodo para formatar o tipo de documento, em especial a OAB
	 * @param documento
	 * @return retorna string do documento formatado
	 */
	private String formatarNumeroDocumento(PessoaDocumentoIdentificacao documento){
		if (documento.getTipoDocumento().getCodTipo().equalsIgnoreCase("OAB")) {
			numeroIdenfificacaoTipo = documento.getEstado().getCodEstado() + documento.getNumeroDocumento() + documento.getLetraOAB();
		
			numeroIdenfificacaoTipo = numeroIdenfificacaoTipo.replaceAll("_", "");
		} else{
			numeroIdenfificacaoTipo = documento.getNumeroDocumento();
		}
		 return numeroIdenfificacaoTipo;
	}

	/**
	 * metodo responsavel por recuperar todas as visibilidades em documentos de identificacao da pessoa passada em parametro.
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<VisibilidadePessoaDocumentoIdentificacao> recuperarVisibilidades(Pessoa _pessoa) throws Exception {
		List<VisibilidadePessoaDocumentoIdentificacao> resultado = null;
		Search search = new Search(VisibilidadePessoaDocumentoIdentificacao.class);
		try {
			search.addCriteria(Criteria.equals("pessoa.idPessoa", _pessoa.getIdPessoa()));			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar as visibilidades em documentos de identificação da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}
		
}
