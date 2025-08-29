package br.com.infox.trf.webservice;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.itx.util.EntityUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaJuridica;

public abstract class ConsultaClienteReceitaPJ implements IConsultaClienteReceita {

	protected void atualizarDados(DadosReceitaPessoaJuridica dadosReceitaPessoaJuridica, String pesqCNPJ) {
		DadosReceitaPessoaJuridica dadoAntigo = consultaDadosBase(pesqCNPJ);
		dadosReceitaPessoaJuridica.setDataAtualizacao(new Date());
		EntityManager em = EntityUtil.getEntityManager();
		if (dadoAntigo != null) {
			dadosReceitaPessoaJuridica.setIdDadosReceitaPessoaJuridica(dadoAntigo.getIdDadosReceitaPessoaJuridica());
			em.merge(dadosReceitaPessoaJuridica);
		} else {
			em.persist(dadosReceitaPessoaJuridica);
		}
		EntityUtil.flush(em);
	}

	@SuppressWarnings("unchecked")
	public DadosReceitaPessoaJuridica consultaDadosBase(String pesqCNPJ) {
		String sql = "select o from DadosReceitaPessoaJuridica o where numCNPJ = :numCNPJ";
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("numCNPJ", pesqCNPJ);
		List<DadosReceitaPessoaJuridica> list = query.getResultList();
		return list.size() == 0 ? null : list.get(0);
	}
	
}
