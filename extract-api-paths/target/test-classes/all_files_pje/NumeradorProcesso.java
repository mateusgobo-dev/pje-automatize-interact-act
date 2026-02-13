package br.jus.cnj.pje.nucleo;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;

@Name("numeradorProcesso")
@Scope(ScopeType.APPLICATION)
public class NumeradorProcesso {

	@In
	private ParametroService parametroService;
	
	private static final Integer NUMERO_INICIAL_PROCESSO = 1;

	public Integer getProximoNumero(Integer numeroOrgaoJustica, Integer numeroOrigem) {
		Query query = EntityUtil.getEntityManager().createNativeQuery(
			"SELECT proximo_numero_processo(:numeroOrgaoJustica, :numeroOrigem, :numeroInicial)")
			.setParameter("numeroOrgaoJustica", numeroOrgaoJustica.toString())
			.setParameter("numeroOrigem", numeroOrigem.toString())
			.setParameter("numeroInicial", this.getNumeroInicial().toString());
		
		return ((Number) query.getSingleResult()).intValue();
	}

	private Integer getNumeroInicial() {
		String numeroInicialProcesso = parametroService.valueOf("numeroInicialProcesso");
		if (StringUtils.isEmpty(numeroInicialProcesso)) {
			return NUMERO_INICIAL_PROCESSO;
		} else {
			try {
				return Integer.parseInt(numeroInicialProcesso);
			} catch (NumberFormatException ex) {
				return NUMERO_INICIAL_PROCESSO;
			}
		}
	}
}