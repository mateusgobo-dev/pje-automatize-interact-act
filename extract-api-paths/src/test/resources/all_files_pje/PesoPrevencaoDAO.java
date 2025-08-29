package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.PesoPrevencao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

@Name(PesoPrevencaoDAO.NAME)
public class PesoPrevencaoDAO extends BaseDAO<PesoPrevencao> {

	public static final String NAME = "pesoPrevencaoDAO";

	@Override
	public Object getId(PesoPrevencao e) {
		return e.getIdPesoPrevencao();
	}
	
	public Double buscarPesoPrevencaoIncidental(ProcessoTrf processoTrf) throws PJeBusinessException {
		int qtd = 0;

		if(processoTrf.getProcessoReferencia() != null && processoTrf.getProcessoReferencia().getProcessoTrfConexaoList() != null){
			for (ProcessoTrfConexao ptc : processoTrf.getProcessoReferencia().getProcessoTrfConexaoList()) {
				if (ptc.getTipoConexao().equals(TipoConexaoEnum.DP) && ptc.getPrevencao().equals(PrevencaoEnum.PR)) {
					qtd++;
				}
			}
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select pp from PesoPrevencao pp ")
			.append("where pp.ativo = true ")
			.append("and ((:qtd between pp.intervaloInicial and pp.intervaloFinal and pp.tipoIntervalo='E') ")
			.append("or (:qtd <= pp.intervaloFinal and pp.tipoIntervalo='A') ")
			.append("or (:qtd >= pp.intervaloInicial and pp.tipoIntervalo='M')) ");
		
		Query query = getEntityManager().createQuery(sql.toString());
		query.setParameter("qtd", qtd);
		
		
		@SuppressWarnings("unchecked")
		List<PesoPrevencao> pesoPrevencaoList = query.getResultList();
		if (pesoPrevencaoList.size() == 0) {
			throw new PJeBusinessException(String.format(
				"Não há intervalo de peso de prevenção para a quantidade de preventos do processo originário: %d.", qtd));
		} else if (pesoPrevencaoList.size() > 1) {
			throw new PJeBusinessException(
					String.format(
						"Foi encontrado mais de um intervalo de peso de prevenção para a quantidade de preventos do processo originário: %d.", qtd));
		}
		
		try{
			return ((PesoPrevencao) pesoPrevencaoList.get(0)).getValorPeso();
		}catch(NoResultException e){
			throw new PJeBusinessException("Não foi possivel encontrar nenhum intervalo de peso de prevenção na base de dados.");
		}
		
	}

}
