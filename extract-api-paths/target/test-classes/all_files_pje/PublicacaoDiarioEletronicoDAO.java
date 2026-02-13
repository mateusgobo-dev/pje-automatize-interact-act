package br.jus.cnj.pje.business.dao;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.utils.Constantes;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoDiarioEnum;
import br.jus.pje.nucleo.enums.TipoPesquisaDJEEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(PublicacaoDiarioEletronicoDAO.NAME)
public class PublicacaoDiarioEletronicoDAO extends BaseDAO<PublicacaoDiarioEletronico> {

	public static final String NAME = "publicacaoDiarioEletronicoDAO";

	@Override
	public Integer getId(PublicacaoDiarioEletronico pdje){
		return pdje.getIdPublicacaoDiario();
	}

	/**
	 * Para todas as publicacoes "aguardando" com data de expectativa <= data_verificacao e (numero de verificacoes = 0 or data_ultima_verificacao = (data_verificacao -1)):
	 *		- somar 1 no número de verificações;
	 *		- adicionar data_ultima_verificacao = data da pesquisa - se a data da pesquia > data_ultima_verificacao
	 * @param dtVerificacao
	 */
	public void indicaVerificacaoPublicacoes(Calendar dtVerificacao) {
		StringBuilder sb = new StringBuilder()
				.append("UPDATE PublicacaoDiarioEletronico ")
				.append(" SET qtdVerificacoes = qtdVerificacoes + 1, ")
				.append(" dtUltimaVerificacao = :dtVerificacaoAtual, ")
				.append(" tipoUltimaPesquisa = :tipoPesquisaData ")
				.append(" WHERE idPublicacaoDiario in ( ")
				.append(" 		SELECT dje.idPublicacaoDiario ")
				.append(" 			FROM PublicacaoDiarioEletronico dje ")
				.append(" 			JOIN dje.processoParteExpediente ppe ")
				.append(" 			WHERE ")
				.append(" 				(dje.dtUltimaVerificacao IS NULL ")
				.append(" 				    OR dje.dtUltimaVerificacao < :dtVerificacaoAtual ")
				.append(" 				    OR dje.tipoUltimaPesquisa = :tipoPesquisaMateria) ")
				.append(" 				AND dje.situacao = :aguardandoPublicacao ")
				.append(" 				AND ppe.fechado = false ")
				.append(" 				AND ppe.dtCienciaParte IS NULL ")
				.append(" 				AND ppe.resposta IS NULL ")
				.append(" 		) ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("aguardandoPublicacao", SituacaoPublicacaoDiarioEnum.A);
		Date dtVerificacaoAtual = dtVerificacao.getTime();
		q.setParameter("dtVerificacaoAtual", dtVerificacaoAtual);
		q.setParameter("tipoPesquisaData", TipoPesquisaDJEEnum.DATA);
		q.setParameter("tipoPesquisaMateria", TipoPesquisaDJEEnum.MATERIA);
		
		q.executeUpdate();
	}
	
	public void indicaVerificacaoPublicacao(ProcessoExpediente pe) {
		this.indicaVerificacaoPublicacao(pe.getIdProcessoExpediente());
	}
	
	public void indicaVerificacaoPublicacao(Integer idProcessoExpediente) {
		StringBuilder sb = new StringBuilder()
				.append("UPDATE PublicacaoDiarioEletronico ")
				.append(" SET qtdVerificacoes = qtdVerificacoes + 1, ")
				.append(" dtUltimaVerificacao = :dtAtual, ")
				.append(" tipoUltimaPesquisa = :tipoPesquisaMateria ")
				.append(" WHERE idPublicacaoDiario in ( ")
				.append(" 		SELECT dje.idPublicacaoDiario ")
				.append(" 			FROM PublicacaoDiarioEletronico dje ")
				.append(" 			JOIN dje.processoParteExpediente.processoExpediente pe ")
				.append(" 			JOIN dje.processoParteExpediente ppe ")
				.append(" 			WHERE ")
				.append(" 				pe.idProcessoExpediente = :idProcessoExpediente ")
				.append(" 				AND (dje.dtUltimaVerificacao IS NULL ")
				.append(" 				    OR dje.dtUltimaVerificacao < :dtAtual ")
				.append(" 				    OR dje.tipoUltimaPesquisa = :tipoPesquisaData) ")
				.append(" 				AND dje.situacao = :aguardandoPublicacao ")
				.append(" 				AND ppe.fechado = false ")
				.append(" 				AND ppe.dtCienciaParte IS NULL ")
				.append(" 				AND ppe.resposta IS NULL ")
				.append(" 		) ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("aguardandoPublicacao", SituacaoPublicacaoDiarioEnum.A);
		q.setParameter("dtAtual", DateUtil.getBeginningOfToday());
		q.setParameter("idProcessoExpediente", idProcessoExpediente);
		q.setParameter("tipoPesquisaMateria", TipoPesquisaDJEEnum.MATERIA);
		q.setParameter("tipoPesquisaData", TipoPesquisaDJEEnum.DATA);
		
		q.executeUpdate();
	}
	
	public void sinalizaPendenciaPublicacao(Integer idProcessoExpediente) {
		StringBuilder sb = new StringBuilder()
				.append("UPDATE PublicacaoDiarioEletronico ")
				.append(" SET situacao = :erro ")
				.append(" WHERE idPublicacaoDiario in ( ")
				.append(" 		SELECT dje.idPublicacaoDiario ")
				.append(" 			FROM PublicacaoDiarioEletronico dje ")
				.append(" 			JOIN dje.processoParteExpediente.processoExpediente pe ")
				.append(" 			WHERE ")
				.append(" 				dje.situacao != :publicada ")
				.append(" 				AND pe.idProcessoExpediente = :idProcessoExpediente ")
				.append(" 		) ")
				.append(" 	AND qtdVerificacoes > :numMaximoVerificacoes ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("publicada", SituacaoPublicacaoDiarioEnum.P);
		q.setParameter("erro", SituacaoPublicacaoDiarioEnum.F);
		q.setParameter("numMaximoVerificacoes", Constantes.MAX_TENTATIVAS_VERIFICACAO_PUBLICACAO_DJE);
		q.setParameter("idProcessoExpediente", idProcessoExpediente);
		
		q.executeUpdate();
	}
}