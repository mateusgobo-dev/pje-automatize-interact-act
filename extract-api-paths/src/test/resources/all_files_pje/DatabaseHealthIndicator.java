package br.jus.cnj.pje.status;

import br.com.itx.util.EntityUtil;

public class DatabaseHealthIndicator extends AbstractHealthIndicator{

	private final String QUERY_CHECK = "SELECT 1 ";
	
	@Override
	public Health doHealthCheck() {
		
		try {
			EntityUtil.getEntityManager().createNativeQuery(QUERY_CHECK);
			this.getDetails().put("success", "Banco de dados acessado com sucesso.");
			this.setHealth(new Health(Status.UP, this.getDetails()));
		} catch (Exception e) {
			this.getDetails().put("error", "Não foi possível acessar o banco de dados.");
			this.setHealth(new Health(Status.DOWN, this.getDetails()));
		}
		
		return this.getHealth();
	}

}
