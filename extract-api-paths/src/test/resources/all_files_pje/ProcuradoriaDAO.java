/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

/**
 * @author cristof
 * 
 */
@Name("procuradoriaDAO")
public class ProcuradoriaDAO extends BaseDAO<Procuradoria>{

	@Override
	public Integer getId(Procuradoria e){
		return e.getIdProcuradoria();
	}

	@SuppressWarnings("unchecked")
	public List<Pessoa> obtemRepresentados(Procuradoria p){
		String sql = "SELECT ppe.pessoa FROM PessoaProcuradoriaEntidade ppe WHERE ppe.procuradoria = :proc";
		Query q = entityManager.createQuery(sql);
		q.setParameter("proc", p);
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Procuradoria> getListProcuradorias(){
		String sql = "SELECT o FROM Procuradoria o WHERE o.ativo = true ORDER BY upper(to_ascii(o.nome)) ";
		Query q = entityManager.createQuery(sql);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Procuradoria>  getlistProcuradorias(TipoProcuradoriaEnum tipoProcuradoria){
		if(tipoProcuradoria != null) {
			String sql = "";
			Query q = null;
			Authenticator.instance();
			if (Authenticator.isPermiteVisualizarProcuradoriaDefensoria()) {
				sql = "SELECT o FROM Procuradoria o WHERE o.ativo = true AND o.tipo = :tipo ORDER BY upper(to_ascii(o.nome))";
				q = entityManager.createQuery(sql);
				q.setParameter("tipo", tipoProcuradoria);
				return q.getResultList();
			}
			
			if (Authenticator.isProcurador() || Authenticator.isAssistenteProcurador()){
				sql = "SELECT o "
						+" FROM Procuradoria o WHERE o.ativo = true AND o.tipo = :tipo"
						+" AND (o.localizacao = :localizacao) ORDER BY upper(to_ascii(o.nome))";
				q = entityManager.createQuery(sql);
				q.setParameter("tipo", tipoProcuradoria);
				q.setParameter("localizacao", Authenticator.getLocalizacaoAtual());
				return q.getResultList();
			}
		}

		return Collections.emptyList();
	}
	
	/**
	 * Retorna o órgão de representação a partir da pessoa jurídica vinculada a esse.
	 * 
	 * A pesquisa foi realizada pelo Id pois há a possibilidade de recuperação do PJ pela receita
	 * e, quando isso acontecer, o objeto será transacional pois não está persistido no banco. 
	 * 
	 * @param pessoaJuridica
	 * @return Procuradoria
	 */
	public Procuradoria findByPessoaJuridica(PessoaJuridica pessoaJuridica){
		String sql = "SELECT o FROM Procuradoria o WHERE o.pessoaJuridica.idPessoaJuridica = :idPessoaJuridica ";
		Query q = entityManager.createQuery(sql);
		q.setParameter("idPessoaJuridica", pessoaJuridica.getIdPessoaJuridica());
		return (Procuradoria) (q.getResultList().size() > 0 ? q.getResultList().get(0) : null);		
	}	
	
}
