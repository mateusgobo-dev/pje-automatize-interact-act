package br.jus.cnj.pje.status;

import javax.persistence.Query;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.webservice.client.ConsultaClienteReceitaPFCNJ;
import br.jus.pje.nucleo.util.StringUtil;

public class ConsultaReceitaHealthIndicator extends AbstractHealthIndicator{

	@Override
	public Health doHealthCheck() {
		
		try {
			ConsultaClienteReceitaPFCNJ.instance().getDadosReceitaPessoaFisicaSemAtualizarBaseDeDados(this.getInscricaoPesquisada(), true);
			this.getDetails().put("success", "O pje-legacy consegue se comunicar com o serviço de consulta RFB corretamente.");
			this.setHealth(new Health(Status.UP, this.getDetails()));
		} catch (Exception e) {
			this.getDetails().put("error", e.getMessage());
			this.setHealth(new Health(Status.DOWN, this.getDetails()));
			e.printStackTrace();
		}
		
		return this.getHealth();
	}
	
	private String getInscricaoPesquisada() {
		String strQuery = "SELECT pdi.numeroDocumento FROM PessoaDocumentoIdentificacao pdi WHERE pdi.tipoDocumento.codTipo = 'CPF'";
		
		Query query = EntityUtil.getEntityManager().createQuery(strQuery);
		query.setMaxResults(1);

		String result = (String)query.getSingleResult();
		
		if(!StringUtil.isEmpty(result)) {
			result = InscricaoMFUtil.retiraMascara(result);
		}
		
		return result;
	}
	
}
