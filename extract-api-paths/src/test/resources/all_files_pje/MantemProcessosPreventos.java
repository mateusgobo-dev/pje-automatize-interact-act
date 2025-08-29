package br.com.infox.trf.webservice;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * @author Márlon Campos
 */
@Name("mantemProcessosPreventos")
@BypassInterceptors
public class MantemProcessosPreventos {

	private static final LogProvider log = Logging.getLogProvider(MantemProcessosPreventos.class);

	// @Observer("org.jboss.seam.postInitialization")
	private void gravaPrevento(List<ProcessoTrf> listPrevento, int idProc) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		/*
		 * TODO falta a distinção de webservice que apontará a seção judiciária.
		 * Então, a regra um da uc12 ficará adiada até que seja defido os
		 * webservices
		 */
		int tam = listPrevento.size();

		for (int i = 0; i < tam; i++) {
			if (idProc != listPrevento.get(i).getIdProcessoTrf()) {
				String numProc = listPrevento.get(i).getProcesso().getNumeroProcesso();
				int idProcCone = listPrevento.get(i).getIdProcessoTrf();
				String sessao = ParametroUtil.instance().getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true);
				String orgao = listPrevento.get(i).getOrgaoJulgador().getOrgaoJulgador();
				String url = ParametroUtil.instance().getFromContext("dslinkprevencao", true);
				String link = url + listPrevento.get(i).getIdProcessoTrf();
				String hash = gerarHash(listPrevento.get(i).getDataDistribuicao(), listPrevento.get(i)
						.getNumeroProcesso());
				String classe = listPrevento.get(i).getClasseJudicialStr();
				Date data = new Date();

				String query = "insert into tb_processo_trf_conexao (id_processo_trf_conexo, id_processo_trf, ds_sessao_judiciaria, ds_orgao_julgador, "
						+ "tp_tipo_conexao, nr_processo, ds_link_sessao_judiciaria, ds_validacao_hash, ds_classe_judicial, dt_possivel_prevencao, dt_registro ) "
						+ "values ("
						+ idProcCone
						+ ", "
						+ idProc
						+ ", '"
						+ sessao
						+ "', '"
						+ orgao
						+ "', 'PR', '"
						+ numProc
						+ "', '"
						+ link
						+ "', '"
						+ hash
						+ "', '"
						+ classe
						+ "', '"
						+ data
						+ "', '"
						+ data
						+ "')";

				org.hibernate.Query qInsert = JbpmUtil.getJbpmSession().createSQLQuery(query).addSynchronizedQuerySpace("tb_processo_trf_conexao");
				qInsert.executeUpdate();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<ProcessoTrf> processosCPF(String cpf) {
		String query = "select a.processoTrf from ProcessoParte a where a.pessoa.idUsuario="
				+ "(select o.idUsuario from PessoaFisica o " + " inner join o.pessoaDocumentoIdentificacaoList p "
				+ " where p.tipoDocumento.codTipo = 'CPF' " + " and p.numeroDocumento = :cpf )";

		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("cpf", cpf);
		return (List<ProcessoTrf>) q.getResultList();
	}

	public void getProcessosPreventosCPF(String pesqCPF, int idProc) throws Exception {
		List<ProcessoTrf> pt = processosCPF(pesqCPF);
		gravaPrevento(pt, idProc);
	}

	@SuppressWarnings("unchecked")
	private List<ProcessoTrf> processosCNPJ(String cnpj) {
		String query = "select a.processoTrf from ProcessoParte a where a.pessoa.idUsuario="
				+ "(select o.idUsuario from PessoaJuridica o where " + "o.numeroCNPJ = :cnpj)";

		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("cnpj", cnpj);
		return (List<ProcessoTrf>) q.getResultList();
	}

	public void getProcessosPreventosCNPJ(String pesqCNPJ, int idProc) throws Exception {
		List<ProcessoTrf> pt = processosCNPJ(pesqCNPJ);
		gravaPrevento(pt, idProc);
	}

	@SuppressWarnings("unchecked")
	private List<ProcessoTrf> processosCPFCNPJ(String doc) {

		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where ");
		sb.append("o.processoStatus in ('D') and ");
		sb.append("exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp ");
		sb.append("where pp.processoTrf.idProcessoTrf = o.idProcessoTrf ");
		sb.append("and pp.pessoa.nome IN ( ");
		sb.append("select pf.pessoa.nome from PessoaDocumentoIdentificacao pf ");
		sb.append("where pf.numeroDocumento = :doc))");

		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("doc", doc);
		return (List<ProcessoTrf>) q.getResultList();
	}

	public void getProcessosPreventos(String doc, int idProc) throws Exception {
		List<ProcessoTrf> pt = processosCPFCNPJ(doc);
		List<ProcessoTrf> list = new ArrayList<ProcessoTrf>();
		list.addAll(processoConexao(idProc));
		for (ProcessoTrf trf : list) {
			if (pt.contains(trf)) {
				pt.remove(trf);
			}
		}
		gravaPrevento(pt, idProc);
	}

	@SuppressWarnings("unchecked")
	private List<ProcessoTrf> processoConexao(int idProc) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoTrfConexo from ProcessoTrfConexao o where ");
		sb.append("o.processoTrf.idProcessoTrf = :id");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("id", idProc);
		return q.getResultList();
	}

	private String gerarHash(Date data, String numeroProcesso) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		SimpleDateFormat format = new SimpleDateFormat("yyyymmddhhMMss");
		String dataHora = format.format(data.getTime());
		String text = numeroProcesso.concat(dataHora);

		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return String.valueOf(sha1hash);
	}
}