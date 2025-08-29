package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(ProcessoParteRepresentanteDAO.NAME)
public class ProcessoParteRepresentanteDAO extends BaseDAO<ProcessoParteRepresentante>{
	public static final String NAME = "processoParteRepresentanteDAO";

	@Override
	public Object getId(ProcessoParteRepresentante e) {
		return e.getProcessoParteRepresentante();
	}
	
	/**
	 * Consulta as pessoas que são representadas pelo representante passado por parâmetro.
	 * 
	 * @param representante Pessoa do representante.
	 * @param processo ProcessoTrf
	 * @return lista dos representados.
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> consultarRepresentados(Pessoa representante, ProcessoTrf processo) {
		Session session = HibernateUtil.getSession();
		
		Criteria criteria = session.createCriteria(ProcessoParteRepresentante.class);
		criteria.createAlias("processoParte", "processoParte", Criteria.LEFT_JOIN);
		criteria.setProjection(Projections.property("processoParte.pessoa"));
		criteria.createAlias("parteRepresentante", "parteRepresentante", Criteria.LEFT_JOIN);
		criteria.createAlias("parteRepresentante.processoTrf", "processoTrf", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("inSituacao", ProcessoParteSituacaoEnum.A));
		criteria.add(Restrictions.eq("parteRepresentante.pessoa", representante));
		criteria.add(Restrictions.eq("parteRepresentante.processoTrf", processo));
		
		return criteria.list();
	}
	
	/**
	 * Método responsável por consultar as pessoas representadas pelo
	 * representante no polo em questão
	 * 
	 * @param representante
	 *            a pessoa do representante
	 * @param processo
	 *            o processo
	 * @param polo
	 *            o polo
	 * @return <code>List</code>, pessoas representadas pelo representante
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> consultarRepresentadosPeloPolo(Pessoa representante, ProcessoTrf processo, ProcessoParteParticipacaoEnum polo) {
		Session session = HibernateUtil.getSession();
		
		Criteria criteria = session.createCriteria(ProcessoParteRepresentante.class);
		criteria.createAlias("processoParte", "processoParte", Criteria.LEFT_JOIN);
		criteria.setProjection(Projections.property("processoParte.pessoa"));
		criteria.createAlias("parteRepresentante", "parteRepresentante", Criteria.LEFT_JOIN);
		criteria.createAlias("parteRepresentante.processoTrf", "processoTrf", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("inSituacao", ProcessoParteSituacaoEnum.A));
		criteria.add(Restrictions.eq("parteRepresentante.pessoa", representante));
		criteria.add(Restrictions.eq("parteRepresentante.inParticipacao", polo));
		criteria.add(Restrictions.eq("parteRepresentante.processoTrf", processo));
		
		return criteria.list();
	}
	

	/**
	 * Retorna um ProcessoParteRepresentante caso o representante represente a parte (representado) no processo, senão retorna nulo.
	 * 
	 * @param representante Pessoa do representante.
	 * @param representando Pessoa da parte representada.
	 * @param processo		ProcessoTrf
	 * @return ProcessoParteRepresentante
	 */
	public ProcessoParteRepresentante consultarProcessoParteRepresentante(Pessoa representante, Pessoa representando, ProcessoTrf processo) {
		return consultarProcessoParteRepresentante(representante, representando, processo, null);
	}
	
	/**
	 * Retorna um ProcessoParteRepresentante caso o representante represente a parte (representado) no processo, senão retorna nulo.
	 * 
	 * @param representante Pessoa do representante.
	 * @param representando Pessoa da parte representada.
	 * @param processo		ProcessoTrf
	 * @param situacao		ProcessoParteSituacaoEnum situacao do representante
	 * @return ProcessoParteRepresentante
	 */

	public ProcessoParteRepresentante consultarProcessoParteRepresentante(Pessoa representante, Pessoa representado, ProcessoTrf processo, ProcessoParteSituacaoEnum situacao) {
		
		String query = "SELECT representantes FROM ProcessoParteRepresentante AS representantes " +
				"	WHERE representantes.representante = :representante " +
				"		AND representantes.processoParte.pessoa = :representado " +
				"		AND representantes.processoParte.processoTrf = :processo ";
		if(situacao != null){
			query = query + " AND representantes.inSituacao = :situacao";
		}
		Query q = entityManager.createQuery(query);
		q.setParameter("representante", representante);
		q.setParameter("representado", representado);
		q.setParameter("processo", processo);
		if(situacao != null){
			q.setParameter("situacao", situacao);
		}
		try{
			return (ProcessoParteRepresentante) q.getSingleResult();
		}catch (NoResultException e){
			return null;
		}
		
	}

	/**
	 * Retorna a lista de representantes ativos de uma parte.
	 * @param ProcessoParte pp
	 * @return List<ProcessoParteRepresentante> lista de representantes Ativos de uma parte
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParteRepresentante> retornarRepresentantesParte(ProcessoParte pp) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM ProcessoParteRepresentante o ");
		sb.append("WHERE o.processoParte = :processoParte ");
		sb.append("AND o.inSituacao = 'A' ");
		
		Query q = entityManager.createQuery(sb.toString());
		q.setParameter("processoParte",  pp);
		
		List<ProcessoParteRepresentante> representantes = q.getResultList();
		
		return representantes;
	}

	/**
	 * Recupera os representantes de acordo com o processoParteInformado
	 * @param processoParte
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> obtemProcessoParteRepresentantes(ProcessoParte processoParte,Integer idProcesso){
		
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct ppa2 from ProcessoParteRepresentante ppr ");
		hql.append("inner join ppr.parteRepresentante ppa2 ");
		hql.append("where ppa2.processoTrf.idProcessoTrf = :idProcesso ");
		hql.append("and ppr.processoParte = :processoParte ");
		hql.append("and ppa2.inSituacao = 'A' ");
		hql.append("and ppr.inSituacao = 'A'");
		
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("idProcesso", idProcesso);
		q.setParameter("processoParte", processoParte);
		
		return q.getResultList();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperarRepresentantesParaExibicao(Integer idProcessoParte, boolean somenteAtivas){
		List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT rep.id_processo_parte, rep.in_participacao, rep.in_parte_principal, rep.id_pessoa, ul.ds_nome AS nome_pessoa, "); 
		sql.append("       (SELECT nr_documento_identificacao ");
		sql.append("       FROM tb_pess_doc_identificacao ");
		sql.append("       WHERE id_pessoa=rep.id_pessoa ");
		sql.append("       AND cd_tp_documento_identificacao IN ('CPF','CNPJ') ");
		sql.append("       AND in_usado_falsamente = false ");
		sql.append("       AND in_ativo = true ");
		sql.append("       ) AS nr_cpf_cnpj, "); 
		sql.append("       tp.id_tipo_parte, tp.ds_tipo_parte, pp.id_processo_trf, rep.in_situacao, pf.ds_nome_social, rep.in_segredo ");
		sql.append("FROM tb_proc_parte_represntante ppr ");
		sql.append("INNER JOIN tb_processo_parte rep ON ppr.id_parte_representante = rep.id_processo_parte ");
		sql.append("INNER JOIN tb_processo_parte pp ON ppr.id_processo_parte = pp.id_processo_parte ");
		sql.append("INNER JOIN tb_tipo_parte tp ON tp.id_tipo_parte = rep.id_tipo_parte ");
		sql.append("INNER JOIN tb_usuario_login ul ON ul.id_usuario = rep.id_pessoa ");
		sql.append("LEFT JOIN tb_pessoa_fisica pf ON pf.id_pessoa_fisica = ul.id_usuario ");
		sql.append("WHERE pp.id_processo_parte = :idProcessoParte ");

		if(somenteAtivas){
			sql.append("AND rep.in_situacao in ('A','B','S') ");
			sql.append("AND ppr.in_situacao in ('A','B','S') ");
		}
		
		sql.append("ORDER BY rep.id_processo_parte");
	 		
		Query q = getEntityManager().createNativeQuery(sql.toString());
		q.setParameter("idProcessoParte", idProcessoParte);
	
	
		/**
		 * -------------------------------
		 * Ordem dos campos no resultList:
		 * -------------------------------
		 * 0 - id_processo_parte
		 * 1 - in_participacao
		 * 2 - in_parte_principal
		 * 3 - id_pessoa
		 * 4 - nome_pessoa
		 * 5 - nr_cpf_cnpj
		 * 6 - id_tipo_parte
		 * 7 - ds_tipo_parte
		 * 8 - id_processo_trf
		 * 9 - in_situacao
		 * 10 - ds_nome_social
		 * 11 - in_segredo
		 */
		List<Object[]> resultList = q.getResultList();
		for (Object[] borderTypes: resultList) {
			ProcessoParte pp = new ProcessoParte();
			ProcessoTrf ptf = new ProcessoTrf();
			
			Pessoa pes;
			PessoaDocumentoIdentificacao pdi = new PessoaDocumentoIdentificacao();
			TipoParte tp = new TipoParte();
			
			pp.setIdProcessoParte(((Integer)borderTypes[0]));
			Character participacao = (Character)borderTypes[1];
			pp.setInParticipacao(ProcessoParteParticipacaoEnum.valueOf(participacao.toString()));
			pp.setPartePrincipal(((Boolean)borderTypes[2]));
			Character situacao = (Character)borderTypes[9];
			pp.setInSituacao(ProcessoParteSituacaoEnum.valueOf(situacao.toString()));

			if(pdi.getNumeroDocumento() != null && InscricaoMFUtil.retiraMascara(pdi.getNumeroDocumento()).length() > 11) {
				TipoDocumentoIdentificacao cnpj = new TipoDocumentoIdentificacao();
				cnpj.setTipoPessoa(TipoPessoaEnum.J);
				cnpj.setCodTipo("CPJ");
				pdi.setTipoDocumento(cnpj);
				pes = new Pessoa();
			}
			else {
				TipoDocumentoIdentificacao cpf = new TipoDocumentoIdentificacao();
				cpf.setTipoPessoa(TipoPessoaEnum.F);
				cpf.setCodTipo("CPF");
				pdi.setTipoDocumento(cpf);
				
				pes = new PessoaFisica();
				String nomeSocial = (String)borderTypes[10];
				((PessoaFisica) pes).setNomeSocial(nomeSocial);
			}
			
			
			
			
			pes.setIdPessoa(((Integer)borderTypes[3]));
			pes.setIdUsuario(((Integer)borderTypes[3]));
			pes.setNome(((String)borderTypes[4]));
			pdi.setNumeroDocumento(((String)borderTypes[5]));
			tp.setIdTipoParte(((Integer)borderTypes[6]));
			tp.setTipoParte(((String)borderTypes[7]));
			ptf.setIdProcessoTrf(((Integer)borderTypes[8]));
			pp.setParteSigilosa((Boolean) borderTypes[11]);
			
			pes.getPessoaDocumentoIdentificacaoList().add(pdi);
			pp.setPessoa(pes);
			pp.setTipoParte(tp);
			pp.setProcessoTrf(ptf);
			
			processoParteList.add(pp);
		}
	
		return processoParteList;
	 }
	
	
}