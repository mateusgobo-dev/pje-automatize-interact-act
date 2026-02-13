/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * Componente de acesso aos dados da entidade {@link PessoaAdvogado}.
 * 
 * @author cristof
 *
 */
@Name("pessoaAdvogadoDAO")
public class PessoaAdvogadoDAO extends AbstractPessoaFisicaEspecializadaDAO<PessoaAdvogado> {
	
	@Override
	public Integer getId(PessoaAdvogado p) {
		return p.getIdUsuario();
	}
	
	/**
	 * Atribui a uma pessoa física dada um perfil de advogado.
	 * 
	 * @param pessoa a pessoa física a quem será atribuído o perfil
	 * @return a {@link PessoaAdvogado} que foi atribuída à pessoa física.
	 */
	public PessoaAdvogado especializa(PessoaFisica pessoa){
		if(!entityManager.contains(pessoa)){
			entityManager.persist(pessoa);
		}
		entityManager.flush();
		String query = "INSERT INTO tb_pessoa_advogado (id,in_tipo_inscricao) VALUES (?1, 'A')";
		Query q = EntityUtil.createNativeQuery(entityManager, query, "tb_pessoa_advogado");
		q.setParameter(1, pessoa.getIdUsuario());
		if(q.executeUpdate() > 0) {
			return entityManager.find(PessoaAdvogado.class, pessoa.getIdUsuario());
		} else {
			return null;
		}
	}
	
	/**
	 * Suprime de uma pessoa física o perfil de advogado
	 * @param pessoa
	 * @return
	 */
	public PessoaAdvogado desespecializa(PessoaFisica pessoa){
		PessoaAdvogado adv = null;
		adv = (PessoaAdvogado)entityManager.find(PessoaAdvogado.class, pessoa.getIdPessoa());
		if(adv != null){
			adv.getPessoa().suprimePessoaEspecializada(adv);
			entityManager.flush();
			return adv;
		}
		
		return null;
	}

    /**
     * Retorna a lista de advogados do processo informado como parmetro, no polo, tipo de inscrio e status indicados.
     * 
     * @param idProcesso Identificador do processo.
     * @param polo Polo processual.
     * @param tipoInscricao Tipo de inscrição.
     * @param isAtivo Indica se o advogado está ativo ou não. 
     * @return Lista de advogados do processo informado como parmetro, no polo, tipo de inscrio e status indicados.
     */
    @SuppressWarnings("unchecked")
    public List<PessoaAdvogado> findByProcesso(int idProcesso, ProcessoParteParticipacaoEnum polo, 
    		PessoaAdvogadoTipoInscricaoEnum tipoInscricao, boolean isAtivo) {
    	
        StringBuilder sb = new StringBuilder("select pa from PessoaAdvogado pa where pa.idUsuario IN ( ")
        	.append("select pp.idPessoa from ProcessoParte pp ")
        	.append("where pp.processoTrf.idProcessoTrf = :idProcessoTrf ")
        	.append("and pp.tipoParte.idTipoParte = :idTipoParteAdvogado ");
        
        if (isAtivo) {
        	sb.append("and pp.inSituacao = 'A' ");
        } else {
        	sb.append("and pp.inSituacao != 'A' ");
        }
        if (polo != null){
            sb.append("and pp.inParticipacao = :polo ");
        }
        sb.append(") ");
        if (tipoInscricao != null){
            sb.append("and pa.tipoInscricao = :tipoInscricao ");
        }

        Query q = entityManager.createQuery(sb.toString());
        q.setParameter("idProcessoTrf", idProcesso);
        q.setParameter("idTipoParteAdvogado", Integer.parseInt(ParametroUtil.getFromContext(Parametros.TIPOPARTEADVOGADO, false)));
        if (polo != null){
            q.setParameter("polo", polo);
        }    
        if (tipoInscricao != null){
            q.setParameter("tipoInscricao", tipoInscricao);
        }
        
        return q.getResultList();       
    }
	
	/**
	 * Recupera Pessoa Advogado pelo Processo Parte
	 * @param processoParte
	 * @return List<pessoaAdvogado>
	 */
	public PessoaAdvogado findByProcessoParte(ProcessoParte processoParte) {
		Query query = getEntityManager().createQuery("FROM PessoaAdvogado where idUsuario = :idPessoa ")
			.setParameter("idPessoa", processoParte.getIdPessoa());
		
		return EntityUtil.getSingleResult(query);
	}
    
}
