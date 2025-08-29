package br.com.infox.ibpm.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;

import com.google.gson.Gson;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.log.EntityLog;

@Name("gerenciadorCachePlacarSessao")
@Scope(ScopeType.APPLICATION)
public class GerenciadorCachePlacarSessao extends AbstractGerenciadorCache {
	
	private static final String QUERY_PLACAR_SESSAO = "SELECT vs.id_sessao, " +
	                                                  "       SUM(vs.AJ) AS AJ, " +
			                                          "       SUM(vs.EJ) AS EJ, " +
	                                                  "       SUM(vs.JG) AS JG, " +
			                                          "       SUM(vs.NJ) AS NJ, " +
			                                          "       SUM(vs.PV) AS PV, " +
			                                          "       SUM(vs.AD) AS AD, " +
			                                          "       SUM(vs.RJ) AS RJ, " +
			                                          "       SUM(vs.AJ + vs.EJ + vs.JG + vs.NJ) AS TP " +
			                                          "FROM " +
                                                      "( " +
                                                      "    SELECT ses.id_sessao, " +
                                                      "           CASE WHEN tp_situacao_julgamento = 'AJ' THEN 1 ELSE 0 END AS AJ, " + 
                                                      "           CASE WHEN tp_situacao_julgamento = 'EJ' THEN 1 ELSE 0 END AS EJ, " +
                                                      "           CASE WHEN tp_situacao_julgamento = 'JG' THEN 1 ELSE 0 END AS JG, " +
                                                      "           CASE WHEN tp_situacao_julgamento = 'NJ' THEN 1 ELSE 0 END AS NJ, " +
                                                      "           CASE WHEN in_adiado_vista = 'PV' THEN 1 ELSE 0 END AS PV, " +
                                                      "           CASE WHEN in_adiado_vista = 'AD' AND in_retirado_julgamento = false THEN 1 ELSE 0 END AS AD, " +
                                                      "           CASE WHEN in_adiado_vista = 'AD' AND in_retirado_julgamento = true THEN 1 ELSE 0 END AS RJ " +
                                                      "    FROM tb_sessao_pauta_proc_trf  spt " +
                                                      "    INNER JOIN tb_sessao ses ON ses.id_sessao = spt.id_sessao " +
                                                      "    WHERE ses.in_iniciar = true " +
                                                      "    AND spt.dt_exclusao_processo IS NULL" +
                                                      ") AS vs " +
                                                      "GROUP BY vs.id_sessao";
	public static final String PLACAR_SESSAO_ATUALIZADO = "br.com.infox.ibpm.util.placarSessaoAtualizado";
	
	private List<PlacarSessao> placares = new ArrayList<PlacarSessao>(0);
	
	@Create
	public void init() {
		atualizarPlacares();
	}

	public List<PlacarSessao> getPlacares() {
		return this.placares;
	}

	public void execute(List<EntityLog> logs) {
		for (EntityLog log : logs) {
			if(log.getNomeEntidade().equalsIgnoreCase("SessaoPautaProcessoTrf") ||
			   log.getNomeEntidade().equalsIgnoreCase("Sessao")	) {
				atualizarPlacares();
				Events.instance().raiseEvent(GerenciadorCachePlacarSessao.PLACAR_SESSAO_ATUALIZADO, this.placares);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void atualizarPlacares() {
		placares = new ArrayList<PlacarSessao>(0);
		Query q = EntityUtil.getEntityManager().createNativeQuery(GerenciadorCachePlacarSessao.QUERY_PLACAR_SESSAO)
						.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
						.setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
		
		List<Object[]> rs = q.getResultList();
		if(rs != null) {
			for (Object[] r: rs) {
				placares.add(preencherPlacar(r));
			}
		}
	}

	private PlacarSessao preencherPlacar(Object[] r) {
		PlacarSessao placar = new PlacarSessao();
		placar.setIdSessao((Integer)r[0]);
		placar.setAguardandoJulgamento(((BigInteger)r[1]).intValue());
		placar.setEmJulgamento(((BigInteger)r[2]).intValue());
		placar.setJulgados(((BigInteger)r[3]).intValue());
		placar.setNaoJulgados(((BigInteger)r[4]).intValue());
		placar.setPedidosVista(((BigInteger)r[5]).intValue());
		placar.setAdiados(((BigInteger)r[6]).intValue());
		placar.setRetiradosJulgamento(((BigInteger)r[7]).intValue());
		placar.setTotalProcessos(((BigInteger)r[8]).intValue());
		return placar;
	}
	
	public class PlacarSessao {
		private int idSessao;
		private int aguardandoJulgamento;
		private int emJulgamento;
		private int julgados;
		private int naoJulgados;
		private int pedidosVista;
		private int adiados;
		private int retiradosJulgamento;
		private int totalProcessos;
		
		public int getIdSessao() {
			return idSessao;
		}
		public void setIdSessao(int idSessao) {
			this.idSessao = idSessao;
		}
		public int getAguardandoJulgamento() {
			return aguardandoJulgamento;
		}
		public void setAguardandoJulgamento(int aguardandoJulgamento) {
			this.aguardandoJulgamento = aguardandoJulgamento;
		}
		public int getEmJulgamento() {
			return emJulgamento;
		}
		public void setEmJulgamento(int emJulgamento) {
			this.emJulgamento = emJulgamento;
		}
		public int getJulgados() {
			return julgados;
		}
		public void setJulgados(int julgados) {
			this.julgados = julgados;
		}
		public int getNaoJulgados() {
			return naoJulgados;
		}
		public void setNaoJulgados(int naoJulgados) {
			this.naoJulgados = naoJulgados;
		}
		public int getPedidosVista() {
			return pedidosVista;
		}
		public void setPedidosVista(int pedidosVista) {
			this.pedidosVista = pedidosVista;
		}
		public int getAdiados() {
			return adiados;
		}
		public void setAdiados(int adiados) {
			this.adiados = adiados;
		}
		public int getRetiradosJulgamento() {
			return retiradosJulgamento;
		}
		public void setRetiradosJulgamento(int retiradosJulgamento) {
			this.retiradosJulgamento = retiradosJulgamento;
		}
		public int getTotalProcessos() {
			return this.totalProcessos;
		}
		public void setTotalProcessos(int totalProcessos) {
			this.totalProcessos = totalProcessos;
		}
		public String toJsonString() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}
}
