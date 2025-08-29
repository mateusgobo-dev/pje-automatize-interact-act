/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.EnderecoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * @author cristof
 * 
 */
@Name(EnderecoManager.NAME)
public class EnderecoManager extends BaseManager<Endereco>{

	public static final String NAME = "enderecoManager";
	
	@In
	private EnderecoDAO enderecoDAO;

	@Override
	protected EnderecoDAO getDAO(){
		return this.enderecoDAO;
	}

	/**
	 * Recupera o endereço mais recente de uma dada pessoa.
	 * 
	 * @param p a pessoa cujo endereço se pretende recuperar.
	 * @return o endereço mais recentemente cadastrado, ou null se não houver nenhum.
	 */
	public Endereco recuperaEnderecoRecente(Pessoa p) {
		try{
			return enderecoDAO.recuperaEnderecoRecente(p);
		}catch(NoResultException e){
			return null;
		}
	}
	
	/**
	 * Cria um endereço novo preenchido previamente com os dados já existentes no 
	 * CEP dado.
	 * 
	 * @param cep o CEP a partir do qual será criado o endereço
	 * @return um endereço novo baseado nas informações do CEP indicado.
	 */
	public Endereco criaEndereco(Cep cep){
		Endereco end = new Endereco();
		end.setCep(cep);
		end.setDataAlteracao(new Date());
		end.setNomeCidade(cep.getMunicipio().getMunicipio());
		end.setNomeEstado(cep.getMunicipio().getEstado().getEstado());
		if(cep.getNomeLogradouro() != null && !cep.getNomeLogradouro().trim().isEmpty()){
			end.setNomeLogradouro(cep.getNomeLogradouro());
		}
		if(cep.getComplemento() != null && !cep.getComplemento().trim().isEmpty()){
			end.setComplemento(cep.getComplemento());
		}
		if(cep.getNomeBairro() != null && !cep.getNomeBairro().trim().isEmpty()){
			end.setNomeBairro(cep.getNomeBairro());
		}
		return end;
	}
	
	/**
	 * Indica se um dado endereço está em uso em algum processo judicial.
	 * 
	 * @param endereco o endereço a ser pesquisado
	 * @return true, se houver pelo menos um processo judicial ao qual o endereço está vinculado.
	 */
	public boolean estaEmUso(Endereco endereco){
		Search s = new Search(ProcessoParteEndereco.class);
		addCriteria(s, Criteria.equals("endereco", endereco));
		return count(s) > 0;
	}
	
	/**
	 * Salva o endereço se ele não existir para a pessoa passada por parâmetro.
	 * 
	 * @param pessoa Pessoa que o endereço está associado.
	 * @param endereco Endereço que será pesquisado.
	 * @return Endereço.
	 * @throws PJeBusinessException 
	 */
	public Endereco salvarSeNaoExistir(Usuario usuario, Endereco endereco) throws PJeBusinessException {
		
		if (usuario != null && endereco != null) {
			Endereco enderecoPersistente = getDAO().obter(usuario, endereco);
			if (enderecoPersistente == null) {
				endereco.setUsuario(usuario);
				endereco.setUsuarioCadastrador(getUsuarioLogado());
				endereco = persist(endereco);
			} else {
				endereco = enderecoPersistente;
			}
		}
		return endereco;
	}
	
	/**
 	 * metodo responsavel por encaminhar o objeto para persistencia
 	 * @param endereco
 	 * @throws Exception
 	 */
 	public void salvarEndereco(Endereco endereco) throws Exception {
 		enderecoDAO.salvarEndereco(endereco);
 	}
 	
 	public List<Endereco> recuperarEnderecosPessoa(Integer idPessoa) {
 		return this.enderecoDAO.findEnderecosByIdPessoa(idPessoa);
 	}

 	public boolean isUsadoEmExpediente(Endereco endereco) {
		Search s = new Search(ProcessoParteExpedienteEndereco.class);
		addCriteria(s, Criteria.equals("endereco", endereco));
		return count(s) > 0;
	}

 	/**
 	 * Metodo responsavel por buscar os ids dos enderecos de uma pessoa.
 	 * Caso o endereco esteja repetido (com base no endereco completo), 
 	 * o endereco mais antigo sera retornado.
 	 * 
 	 * @param idPessoa
 	 * @param cepFiltro
 	 * @param enderecoCompletoFiltro
 	 * @return
 	 */
 	public List<Integer> getIdsEnderecosUnicosPeloEnderecoCompleto(Integer idPessoa, String cepFiltro, String enderecoCompletoFiltro){
 		return this.enderecoDAO.getIdsEnderecosUnicosPeloEnderecoCompleto(idPessoa, cepFiltro, enderecoCompletoFiltro);
 	}
 	
 	/**
 	 * Retorna a quantidade de enderecos de um usuario
 	 * @param idUsuario
 	 * @return
 	 */
 	public Long retornarQuantidadeEnderecosPorUsuario(Integer idUsuario) {
 		return this.enderecoDAO.retornarQuantidadeEnderecosPorUsuario(idUsuario);
 	}
 	
}
