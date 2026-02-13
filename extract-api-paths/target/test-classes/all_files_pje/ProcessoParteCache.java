package br.jus.cnj.pje.nucleo.manager.cache;

import java.util.concurrent.TimeUnit;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Scope(ScopeType.APPLICATION)
@Name(ProcessoParteCache.COMPONENT_NAME)
public class ProcessoParteCache {

	public final static String COMPONENT_NAME = "processoParteCache";

	private LoadingCache<String, Optional<ProcessoParte>> processoParteByProcessoTrf = CacheBuilder.newBuilder()
			.maximumSize(5000).expireAfterAccess(30, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Optional<ProcessoParte>>() {

				@Override
				public Optional<ProcessoParte> load(String key) throws Exception {
					return Optional.fromNullable(getProcessoParteByProcessoTrf(key));
				}
			});

	private LoadingCache<Integer, Optional<Integer>> idOrgaoJulgadorPorProcessoParteCache = CacheBuilder.newBuilder()
			.maximumSize(5000).expireAfterWrite(24, TimeUnit.HOURS)
			.build(new CacheLoader<Integer, Optional<Integer>>() {

				@Override
				public Optional<Integer> load(Integer idProcessoParte) throws Exception {
					return Optional.fromNullable(recuperarIdOrgaoJulgadorPorProcessoParte(idProcessoParte));
				}
			});

	public ProcessoParte getProcessoParteByProcessoTrfEPessoaCache(Integer idProcessoTrf, Integer idPessoa) {
		try {
			Optional<ProcessoParte> processoParte = processoParteByProcessoTrf
					.get(String.valueOf(idProcessoTrf) + "#" + String.valueOf(idPessoa));

			return processoParte.isPresent() ? processoParte.get() : null;
		} catch (Exception e) {
			return null;
		}
	}

	public void refreshProcessoParteByProcessoTrfEPessoaCache(Integer idProcessoTrf, Integer idPessoa) {
		try {
			processoParteByProcessoTrf.refresh(String.valueOf(idProcessoTrf) + "#" + String.valueOf(idPessoa));
		} catch (Exception e) {
		}
	}

	public void refreshProcessoParteByPessoaCache(Integer idPessoa) {
		try {
			java.util.Optional<String> key = processoParteByProcessoTrf.asMap().keySet().stream()
					.filter(s -> s.contains("#" + String.valueOf(idPessoa))).findFirst();

			if (key.isPresent()) {
				processoParteByProcessoTrf.refresh(key.get());
			}
		} catch (Exception e) {
		}
	}

	public Integer getIdOrgaoJulgadorPorProcessoParteCache(Integer idProcessoParte) {
		try {
			Optional<Integer> idOrgaoJulgador = idOrgaoJulgadorPorProcessoParteCache.get(idProcessoParte);

			return idOrgaoJulgador.isPresent() ? idOrgaoJulgador.get() : null;
		} catch (Exception e) {
			return null;
		}
	}

	private ProcessoParte getProcessoParteByProcessoTrf(String key) {
		String[] split = key.split("#");

		Integer idProcessoTrf = Integer.valueOf(split[0]);
		Integer idPessoa = Integer.valueOf(split[1]);

		Query query = EntityUtil.getEntityManager().createQuery("select o from ProcessoParte o "
				+ " where o.inSituacao = 'A' and o.processoTrf.idProcessoTrf = :idProcessoTrf and o.idPessoa = :idPessoa");

		query.setParameter("idProcessoTrf", idProcessoTrf);
		query.setParameter("idPessoa", idPessoa);

		try {
			return (ProcessoParte) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	private Integer recuperarIdOrgaoJulgadorPorProcessoParte(Integer idProcessoParte) {
		Query query = EntityUtil.getEntityManager()
				.createNativeQuery("select oj.id_orgao_julgador from client.tb_processo_parte pp"
						+ " inner join client.tb_processo_trf p on pp.id_processo_trf = p.id_processo_trf"
						+ " inner join client.tb_orgao_julgador oj on p.id_orgao_julgador = oj.id_orgao_julgador"
						+ " where pp.id_processo_parte = :idProcessoParte");

		query.setParameter("idProcessoParte", idProcessoParte);

		try {
			Number id = (Number) query.getSingleResult();

			return id.intValue();
		} catch (NoResultException e) {
			return null;
		}
	}
}