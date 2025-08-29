package br.jus.cnj.pje.business.dao;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;

@Name("mandadoPrisaoDAO")
public class MandadoPrisaoDAO extends MandadoAlvaraDAO<MandadoPrisao>{

	@SuppressWarnings("unchecked")
	public List<MandadoPrisao> recuperarDemaisMandadosDoProcesso(ProcessoTrf processo, Pessoa pessoa,
			MandadoPrisao mandadoPrisao, Boolean naoCumpridos) throws PJeDAOException{

		String hql = " select o from MandadoPrisao o " + " where o.processoTrf.idProcessoTrf = :idProcessoTrf "
			+ " and   o.pessoa.idUsuario = :idUsuario ";

		if (naoCumpridos == null || naoCumpridos){
			hql += " and o.situacaoExpedienteCriminal != :situacaoExpedienteCriminal";			
		}

		if (mandadoPrisao != null && mandadoPrisao.getId() != null){
			hql += " and   o.id <> :idMandado ";
		}

		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter("idProcessoTrf", processo.getIdProcessoTrf());
		qry.setParameter("idUsuario", pessoa.getIdUsuario());
		
		if (naoCumpridos == null || naoCumpridos){
			qry.setParameter("situacaoExpedienteCriminal", SituacaoExpedienteCriminalEnum.CP);
		}
		
		if (mandadoPrisao.getId() != null){
			qry.setParameter("idMandado", mandadoPrisao.getId());
		}

		List<MandadoPrisao> resultList = qry.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<MandadoPrisao> recuperarMandadosPessoa(Pessoa pessoa, Boolean naoCumpridos) throws PJeDAOException{
		String hql = " select o from MandadoPrisao o " + " where o.pessoa.idUsuario = :idUsuario ";

		if (naoCumpridos == null || naoCumpridos){
			hql += " and   o.dataCumprimento is null ";
		}

		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter("idUsuario", pessoa.getIdUsuario());

		List<MandadoPrisao> resultList = qry.getResultList();
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public List<MandadoPrisao> recuperarMandados(Integer numero, Pessoa pessoa, SituacaoExpedienteCriminalEnum sitExpCrim){
		String hql = " select o from MandadoPrisao o "
			+ " where o.pessoa.idUsuario = :idUsuario "
			+ " and o.situacaoExpedienteCriminal = :sitExpCriminal "; 

		if (numero != null){
			hql += "  and o.numero = :numero";
		}

		Query qry = getEntityManager().createQuery(hql);

		if (numero != null){
			qry.setParameter("numero", numero);
		}

		qry.setParameter("idUsuario", pessoa.getIdUsuario());
		qry.setParameter("sitExpCriminal", sitExpCrim);
		List<MandadoPrisao> resultList = qry.getResultList();
		return resultList;
	}
}
