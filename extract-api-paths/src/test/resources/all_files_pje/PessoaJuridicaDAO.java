/*
 * package br.com.infox.pje.dao;
 * 
 * import java.io.Serializable; import java.util.HashMap; import java.util.Map;
 * 
 * import org.jboss.seam.ScopeType; import
 * org.jboss.seam.annotations.AutoCreate; import
 * org.jboss.seam.annotations.Name; import org.jboss.seam.annotations.Scope;
 * 
 * import br.jus.pje.nucleo.entidades.PessoaJuridica; import
 * br.com.infox.core.dao.GenericDAO; import
 * br.com.infox.pje.query.PessoaJuridicaQuery;
 * 
 * @Name(PessoaJuridicaDAO.NAME)
 * 
 * @Scope(ScopeType.CONVERSATION)
 * 
 * @AutoCreate public class PessoaJuridicaDAO extends GenericDAO implements
 * Serializable {
 * 
 * private static final long serialVersionUID = 1L;
 * 
 * public static final String NAME = "pessoaJuridicaDAO";
 * 
 * public PessoaJuridica getPessoaJuridicaByNome(String nomePessoa) {
 * Map<String, Object> parameters = new HashMap<String, Object>();
 * parameters.put(PessoaJuridicaQuery.QUERY_PARAMETER_NOME, nomePessoa); return
 * getNamedSingleResult(PessoaJuridicaQuery.PESSOA_JURIDICA_BY_NOME,
 * parameters); }
 * 
 * }
 */