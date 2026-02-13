package br.com.infox.trf.webservice;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.itx.util.EntityUtil;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

public abstract class ConsultaClienteReceitaPF implements IConsultaClienteReceita {

	protected void atualizarDados(DadosReceitaPessoaFisica dadosReceitaPessoaFisica, String pesqCPF) {
		DadosReceitaPessoaFisica dadoAntigo = consultaDadosBase(pesqCPF);
		dadosReceitaPessoaFisica.setDataAtualizacao(new Date());
		EntityManager em = EntityUtil.getEntityManager();
		if (dadoAntigo != null) {
			dadosReceitaPessoaFisica.setIdDadosReceitaPessoaFisica(dadoAntigo.getIdDadosReceitaPessoaFisica());
			em.merge(dadosReceitaPessoaFisica);
		} else {
			em.persist(dadosReceitaPessoaFisica);
		}
		EntityUtil.flush(em);
	}

	@SuppressWarnings("unchecked")
	public DadosReceitaPessoaFisica consultaDadosBase(String pesqCPF) {
		String sql = "select o from DadosReceitaPessoaFisica o where numCPF = :numCPF";
		Query query = EntityUtil.getEntityManager().createQuery(sql);
		query.setParameter("numCPF", pesqCPF);
		List<DadosReceitaPessoaFisica> list = query.getResultList();
		return list.size() == 0 ? null : list.get(0);
	}
	
}
