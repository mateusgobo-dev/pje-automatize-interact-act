package br.com.infox.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.HibernateUtil;

@Name("hibernateStatistics")
public class HibernateStatistics {

	private Statistics statistics;

	public Statistics getStatistics() {
		if (statistics == null) {
			statistics = HibernateUtil.getSession().getSessionFactory().getStatistics();
		}
		return statistics;
	}

	public void logSummary() {
		getStatistics().logSummary();
	}

	public List<QueryInfo> getQueryInfo(int maxTimeAllowed) {
		List<QueryInfo> list = new ArrayList<QueryInfo>();
		String[] queries = getStatistics().getQueries();
		for (String query : queries) {
			QueryStatistics queryStatistics = getStatistics().getQueryStatistics(query);
			long maxTime = queryStatistics.getExecutionMaxTime();
			if (maxTime > maxTimeAllowed) {
				QueryInfo queryInfo = new QueryInfo(query, queryStatistics);
				list.add(queryInfo);
			}
		}
		Collections.sort(list);
		return list;
	}

	public List<EntityInfo> getEntityInfo() {
		List<EntityInfo> list = new ArrayList<EntityInfo>();
		String[] entityNames = getStatistics().getEntityNames();
		for (String entity : entityNames) {
			EntityStatistics entityStatistics = getStatistics().getEntityStatistics(entity);
			if (entityStatistics.getLoadCount() > 0) {
				EntityInfo entityInfo = new EntityInfo(entity, entityStatistics);
				list.add(entityInfo);
			}
		}
		Collections.sort(list);
		return list;
	}

	public List<CollectionInfo> getCollectionInfo() {
		List<CollectionInfo> list = new ArrayList<CollectionInfo>();
		String[] collections = getStatistics().getCollectionRoleNames();
		for (String collection : collections) {
			CollectionStatistics collectionStatistics = getStatistics().getCollectionStatistics(collection);
			if (collectionStatistics.getLoadCount() > 0) {
				CollectionInfo collectionInfo = new CollectionInfo(collection, collectionStatistics);
				list.add(collectionInfo);
			}
		}
		Collections.sort(list);
		return list;
	}

	public class QueryInfo implements Comparable<QueryInfo> {
		private String query;
		private QueryStatistics queryStatistics;

		public QueryInfo(String query, QueryStatistics queryStatistics) {
			this.query = query;
			this.queryStatistics = queryStatistics;
		}

		@Override
		public int compareTo(QueryInfo o) {
			return -1 * (int) (queryStatistics.getExecutionMaxTime() - o.getQueryStatistics().getExecutionMaxTime());
		}

		public QueryStatistics getQueryStatistics() {
			return queryStatistics;
		}

		public String getQuery() {
			return query;
		}

	}

	public class EntityInfo implements Comparable<EntityInfo> {
		private String entity;
		private EntityStatistics entityStatistics;

		public EntityInfo(String entity, EntityStatistics entityStatistics) {
			this.entity = entity;
			this.entityStatistics = entityStatistics;
		}

		@Override
		public int compareTo(EntityInfo o) {
			return (int) (-1 * (entityStatistics.getLoadCount() - o.getEntityStatistics().getLoadCount()));
		}

		public EntityStatistics getEntityStatistics() {
			return entityStatistics;
		}

		public String getEntity() {
			return entity;
		}
	}

	public class CollectionInfo implements Comparable<CollectionInfo> {
		private String collection;
		private CollectionStatistics collectionStatistics;

		public CollectionInfo(String collection, CollectionStatistics collectionStatistics) {
			super();
			this.collection = collection;
			this.collectionStatistics = collectionStatistics;
		}

		@Override
		public int compareTo(CollectionInfo o) {
			return (int) (-1 * (collectionStatistics.getLoadCount() - o.getCollectionStatistics().getLoadCount()));
		}

		public String getCollection() {
			return collection;
		}

		public CollectionStatistics getCollectionStatistics() {
			return collectionStatistics;
		}
	}
}
