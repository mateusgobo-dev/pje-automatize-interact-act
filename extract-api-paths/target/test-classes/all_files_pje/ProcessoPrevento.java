package br.com.infox.pje.webservices;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.Query;

import org.jboss.seam.contexts.Lifecycle;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@WebService()
public class ProcessoPrevento {

	@SuppressWarnings({ "unchecked", "static-access" })
	@WebMethod()
	public List<DadosProcessoPrevento> getProcessosPreventos(String cpfCnpj){
		Lifecycle.beginCall();
		Query q;
		if (cpfCnpj.length() == 14) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoTrf o where ");
			sb.append("o.processoStatus in ('D', 'V') and ");
			sb.append("exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp ");
			sb.append("where pp.processoTrf.idProcessoTrf = o.idProcessoTrf ");
			sb.append("and pp.pessoa.idUsuario IN ( ");
			sb.append("select pf.idUsuario from PessoaFisica pf ");
			sb.append("inner join pf.pessoaDocumentoIdentificacaoList pdi ");
			sb.append("where pdi.tipoDocumento.codTipo = 'CPF' ");
			sb.append("and pdi.numeroDocumento = :cpf))");
			q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("cpf", cpfCnpj);

		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from ProcessoTrf o where ");
			sb.append("o.processoStatus in ('D', 'V') and ");
			sb.append("exists (select pp.processoTrf.idProcessoTrf from ProcessoParte pp ");
			sb.append("where pp.processoTrf.idProcessoTrf = o.idProcessoTrf ");
			sb.append("and pp.pessoa.nome IN ( ");
			sb.append("select pf.nome from PessoaDocumentoIdentificacao pf ");
			sb.append("where pf.numeroDocumento = :cnpj))");
			q = EntityUtil.getEntityManager().createQuery(sb.toString());
			q.setParameter("cnpj", cpfCnpj);
		}
		if (q.getResultList().size() <= 0) {
			Lifecycle.endCall();
			return null;
		}

		List<ProcessoTrf> processoTrf = q.getResultList();
		List<DadosProcessoPrevento> dpp = new ArrayList<DadosProcessoPrevento>();

		String url = ParametroUtil.getFromContext("dslinkprevencao", true);

		for (ProcessoTrf trf : processoTrf) {
			DadosProcessoPrevento pp = new DadosProcessoPrevento();
			pp.setId(trf.getIdProcessoTrf());
			pp.setOrgaoJulgador(trf.getOrgaoJulgador().getOrgaoJulgador());
			pp.setNumeroProcesso(trf.getNumeroProcesso());
			pp.setSessaoJudiciaria(ParametroUtil.instance().getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true));
			pp.setClasseJudicial(trf.getClasseJudicialStr());
			pp.setLink(url + trf.getIdProcessoTrf());
			pp.setHash(gerarHash(trf.getDataDistribuicao(), trf.getNumeroProcesso()));
			dpp.add(pp);
		}
		Lifecycle.endCall();
		return dpp;
	}

	private String gerarHash(Date data, String numeroProcesso){
		SimpleDateFormat format = new SimpleDateFormat("yyyymmddhhMMss");
		String dataHora = format.format(data.getTime());
		String text = numeroProcesso.concat(dataHora);

		MessageDigest md;
		try{
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash;
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			return String.valueOf(sha1hash);
		}catch(Exception e){
			return "";
		}
	}

}
