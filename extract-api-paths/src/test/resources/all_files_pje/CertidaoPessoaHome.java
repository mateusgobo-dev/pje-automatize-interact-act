package br.com.infox.cliente.home;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.csjt.pje.business.service.ResultadoSentencaService;
import br.jus.pje.nucleo.entidades.CertidaoPessoa;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ResultadoSentenca;

@Name(CertidaoPessoaHome.NAME)
@BypassInterceptors
public class CertidaoPessoaHome extends AbstractCertidaoPessoaHome<CertidaoPessoa> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(CertidaoPessoaHome.class);
	private String numeroHash;
	private Boolean hashValidado;
	private String CPFouCNPJRadio;
	private Boolean podeGerar = Boolean.FALSE;
	private Boolean podeValidar = Boolean.TRUE;
	private String certidaoNegativa;
	public static final String NAME = "certidaoPessoaHome";

	public static CertidaoPessoaHome instance() {
		return ComponentUtil.getComponent(CertidaoPessoaHome.NAME);
	}

	public CertidaoPessoaHome() {
		CPFouCNPJRadio = "cpf";
	}

	public void setarPodeGerar() {
		setPodeGerar(Boolean.FALSE);
	}
	
    /**
     * Método que decide qual modelo de certidão gerar
     * @author Thiago Oliveira / Gabriel Azevedo
     * @since 16/08/2012
     * @return void
     */
    public void gerarCertidao(){
        String modeloCertidaoNegativa = ParametroUtil.getFromContext("codModeloCertidaoNegativa", true);
        int codModeloCertidaoNegativa = -1 ;
        if(modeloCertidaoNegativa != null  ){
        	   codModeloCertidaoNegativa = Integer.parseInt(modeloCertidaoNegativa);
        }
        if(codModeloCertidaoNegativa == instance.getTipoCertidao().getModeloDocumento().getIdModeloDocumento()){
        	this.gerarCertidaoNegativa();        
        }else{
        	this.persist();
        }
    }

    /**
     * Método que gera a certidão no menu Certidão --> Emitir Certidão
     * @author Thiago Oliveira / Gabriel Azevedo
     * @since 16/08/2011
     * @return void
     */
    public void gerarCertidaoNegativa() {

    		// Busca informações da pessoa física ou jurídica
            Pessoa pessoa = this.getPessoa();
            if(pessoa != null){
            	instance.setNome(pessoa.getNome());
            }

            // Lista os processos dessa pessoa
            List<ProcessoTrf> listaProcessos = this.listaProcessos();
            List<ProcessoTrf> listaProcessosTela = new ArrayList<ProcessoTrf>(listaProcessos);      
            ResultadoSentencaService rs = ComponentUtil.getComponent(ResultadoSentencaService.NAME);

            String codMovimentoBaixaDefinitivaDistribuicao = CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_BAIXA_DEFINITIVA;
            String codMovimentoArquivamentoDefinitivo = CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_ARQUIVAMENTO_DEFINITIVO;
            String codMovimentoAusenciaReclamante =  CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_ARQUIVAMENTO_AUSENCIA_RECLAMANTE;
            String codMovimentoDesarquivamento =  CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_DESARQUIVAMENTO;
            String codMovimentoArquivamentoSumarissimo = CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_ARQUIVAMENTO_SUMARISSIMO;
            String nomeSecaoJudiciaria = ParametroUtil.getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true);

            // Verifica se alguma desses processos estão arquivos. Se houver, retira da lista para ser apresentadas.
            for (ProcessoTrf processo : listaProcessos) {
                List<ProcessoEvento> listaEventos = this.getListaProcessoEvento(processo);
                boolean arquivado = false;

                    // Para cada lista de Eventos do processo, faz a comparação
                    for (ProcessoEvento processoEvento : listaEventos) {
                        if(codMovimentoBaixaDefinitivaDistribuicao == processoEvento.getEvento().getCodEvento() ||
                           codMovimentoArquivamentoDefinitivo == processoEvento.getEvento().getCodEvento() ||
                           codMovimentoAusenciaReclamante == processoEvento.getEvento().getCodEvento() ||
                           codMovimentoArquivamentoSumarissimo == processoEvento.getEvento().getCodEvento()){
                                arquivado = true;                                       
                        }

                        if(codMovimentoDesarquivamento == processoEvento.getEvento().getCodEvento()){
                        	arquivado = false;
                        }
                    }

                    // Verifica se o réu está excluído da lide
                    ResultadoSentenca resultadoSentenca = rs.getResultadoSentencaExcluidoDaLide(processo, this.getIsCNPJ(), instance.getNumeroCPFouCNPJ());
                    if(resultadoSentenca != null){
                        arquivado = true;                               
                    }
                    
                    if(arquivado == true){
                        listaProcessosTela.remove(processo);
                    }
            }

            String dataInstalacaoOrgao = ParametroUtil.getFromContext("dataInstalacaoOrgao",true);
            String certidao = instance.getTipoCertidao().getModeloDocumento().getModeloDocumento();
            instance.setDataEmissao(new Date());
            String secao = ParametroUtil.getFromContext(Parametros.NOME_SECAO_JUDICIARIA, true);
            certidao = certidao.replace("#{certidaoPessoaHome.instance.nome}",instance.getNome());
            certidao = certidao.replace("#{certidaoPessoaHome.instance.numeroCPFouCNPJ}",instance.getNumeroCPFouCNPJ());
            certidao = certidao.replace("#{certidaoPessoaHome.instance.dataEmissao}",instance.getDataEmissao().toString());
            certidao = certidao.replace("#{nomeSecaoJudiciaria}", secao);
            certidao = certidao.replace("#{dataInstalacaoOrgao}", dataInstalacaoOrgao);
            certidao = certidao.replace("#{processoTrfHome.instance.orgaoJulgador}", nomeSecaoJudiciaria);
            
            SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");              
            certidao = certidao.replace("#{dataAtual}", formatoData.format(instance.getDataEmissao()));
            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
            certidao = certidao.replace("#{horaAtual}", formatoHora.format(instance.getDataEmissao()));

            StringBuilder certidaoCNAT = new StringBuilder();
            if(listaProcessosTela == null || listaProcessosTela.size() == 0){
                certidaoCNAT = new StringBuilder("não existe processo tramitando / NADA CONSTA.");
                certidao = certidao.replace("#{jt.certidaoCNAT}", certidaoCNAT);                        
            } else{
                certidaoCNAT = new StringBuilder("consta(m) tramitando  os processos:<br>");
                certidaoCNAT.append("<ul>");
                for (ProcessoTrf processo : listaProcessosTela) {
                        certidaoCNAT.append("<li>");
                        certidaoCNAT.append(processo.getNumeroProcesso());
                        certidaoCNAT.append(" - ");
                        certidaoCNAT.append(processo.getOrgaoJulgador());
                        certidaoCNAT.append("</li>");
                }
                certidaoCNAT.append("</ul>");
                certidao = certidao.replace("#{jt.certidaoCNAT}", certidaoCNAT);
            }                       

            instance.setCertidao(certidao);
            try{
            	instance.setHash(gerarHash());
            }catch(Exception e){                            
            }
            
            podeGerar = Boolean.TRUE;
            super.persist();
    }

	private Boolean verificarPendencia(String cpf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o.pessoa) from ProcessoParte o ");
		sb.append("where o.pessoa in (select pf from PessoaFisica pf ");
		sb.append("inner join pf.pessoaDocumentoIdentificacaoList pd ");
		sb.append("inner join pd.tipoDocumento td ");
		sb.append("where td.tipoDocumento = 'CPF' and pd.numeroDocumento = :cpf) ");
		sb.append("and o.processoTrf.processoStatus = 'D'");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("cpf", cpf);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@Override
	public String persist() {
		if (instance.getNome().isEmpty()) {
			podeGerar = Boolean.FALSE;
		} else {
			podeGerar = Boolean.TRUE;
		}
		certidaoNegativa = "";
		if (instance.getTipoCertidao().getTipoCertidao().equals("Distribuição")) {
			if (!verificarPendencia(instance.getNumeroCPFouCNPJ())) {
				certidaoNegativa = "não";
			}
		}

		String certidao = instance.getTipoCertidao().getModeloDocumento().getModeloDocumento();
		instance.setDataEmissao(new Date());
		certidao = certidao.replace("#{certidaoPessoaHome.instance.nome}", instance.getNome());
		certidao = certidao.replace("#{certidaoPessoaHome.instance.numeroCPFouCNPJ}", instance.getNumeroCPFouCNPJ());
		certidao = certidao.replace("#{certidaoPessoaHome.instance.dataEmissao}", instance.getDataEmissao().toString());
		certidao = certidao.replace("#{certidaoPessoaHome.certidaoNegativa}", certidaoNegativa);
		instance.setCertidao(certidao);
		try {
			instance.setHash(gerarHash());
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		return super.persist();
	}
	
    /**
     * Método que busca informações da pessoa física ou jurídica
     * @author U006184 - Thiago Oliveira
     * @since 16/08/2012
     * @return Pessoa
     */
    @SuppressWarnings("unchecked")
    private Pessoa getPessoa(){
    	StringBuilder sb = new StringBuilder();

        if(this.getIsCNPJ()){
            sb.append("select pj from PessoaJuridica pj ");
            sb.append("inner join pj.pessoaDocumentoIdentificacaoList pd ");
            sb.append("inner join pd.tipoDocumento td ");
            sb.append("where td.tipoDocumento.codTipo = 'CPJ' ");                   
        }else{
            sb.append("select pf from PessoaFisica pf ");
            sb.append("inner join pf.pessoaDocumentoIdentificacaoList pd ");
            sb.append("inner join pd.tipoDocumento td ");
            sb.append("where td.tipoDocumento.codTipo = 'CPF' ");
        }
        sb.append("and pd.numeroDocumento = :doc");  
        Query q = getEntityManager().createQuery(sb.toString());
	    q.setParameter("doc", instance.getNumeroCPFouCNPJ());
	    List<Pessoa> listaPessoa = q.getResultList();
	    if(listaPessoa != null && listaPessoa.size() > 0)
	    	return listaPessoa.get(0);
	    else
	    	return null;
    }
    
    /**
     * Método que retorna todos os processos da pessoa física ou jurídica em que faz parte do polo passivo
     * @author U006184 - Thiago Oliveira
     * @since 16/08/2012
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<ProcessoTrf> listaProcessos(){
        StringBuilder sb = new StringBuilder();
        sb.append("select p from ProcessoTrf p ");
        sb.append("inner join p.processoParteList pp ");
        sb.append("inner join pp.tipoParte tp ");
        sb.append("where p.processoStatus = 'D' ");
        sb.append("and tp.inPoloPassivo = true ");
        sb.append("and pp.inSituacao not in ('I', 'S') ");
        if(this.getIsCNPJ()){
            sb.append("and pp.pessoa in (select pj from PessoaJuridica pj ");
            sb.append("inner join  pj.pessoaDocumentoIdentificacaoList pd ");
            sb.append("where pd.numeroDocumento = :doc ");
            sb.append("and pd.tipoDocumento.codTipo = 'CPJ')");
        }else{
            sb.append("and pp.pessoa in (select pf from PessoaFisica pf ");
            sb.append("inner join  pf.pessoaDocumentoIdentificacaoList pd ");
            sb.append("where pd.numeroDocumento = :doc ");
            sb.append("and pd.tipoDocumento.codTipo = 'CPF')");
        }
        sb.append("order by p.orgaoJulgador.numeroVara asc");
        Query q = getEntityManager().createQuery(sb.toString());
	    q.setParameter("doc", instance.getNumeroCPFouCNPJ());	
	    return q.getResultList();
    }
    
    /**
     * Método que retorna os movimentos (eventos) do processo
     * @author U006184 - Thiago Oliveira
     * @since 16/08/2012
     * @param processoTrf
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ProcessoEvento> getListaProcessoEvento(ProcessoTrf processoTrf){
        StringBuilder sb = new StringBuilder();
        sb.append("select o from ProcessoEvento o ");
        sb.append("where o.processo = :processo ");
        sb.append("order by o.dataAtualizacao asc");

        Query q = getEntityManager().createQuery(sb.toString());
        q.setParameter("processo", processoTrf.getProcesso());
        List<ProcessoEvento> lista = q.getResultList();
        return lista;
    }

	@Override
	public void newInstance() {
		// podeGerar = Boolean.FALSE;
		super.newInstance();
	}

	private CertidaoPessoa trazerCertidaoPessoa(String hash) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(CertidaoPessoa.class);
		criteria.add(Restrictions.eq("hash", hash));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (CertidaoPessoa)criteria.uniqueResult();
	}

	public String validarHash() throws IOException {
		newInstance();
		instance = trazerCertidaoPessoa(getNumeroHash());
		if (instance == null) {
			setHashValidado(Boolean.FALSE);
			setNumeroHash("");
			Redirect.instance().setViewId("/EmitirCertidao/ValidarCertidao/certidaoHashError.seam");
		} else {
			setHashValidado(Boolean.TRUE);
			Redirect.instance().setViewId("/EmitirCertidao/certidaoGerada.seam?cid="+Conversation.instance().getId());
		}
		Redirect.instance().execute();
		return "";
	}
	
	public void teste(){
		System.out.println("Teste");		
	}

	public String gerarHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dataHora = format.format(instance.getDataEmissao().getTime());
		String text = String.valueOf(instance.getTipoCertidao().getIdTipoCertidao())
				.concat(instance.getNumeroCPFouCNPJ()).concat(instance.getNome()).concat(dataHora);

		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash;
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		setNumeroHash(convertToHex(sha1hash));
		return getNumeroHash();
	}

	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public void setarNumeroHash(String numeroHash) {
		  if(!( !getNumeroHash().equals("")  && numeroHash.equals("") )){
			  setNumeroHash(numeroHash);
          }
	}

	public void setNumeroHash(String numeroHash) {
		this.numeroHash = numeroHash;
	}

	public String getNumeroHash() {
		return numeroHash;
	}

	public String getCPFouCNPJRadio() {
		return CPFouCNPJRadio;
	}

	public void setCPFouCNPJRadio(String CPFouCNPJRadio) {
		this.CPFouCNPJRadio = CPFouCNPJRadio;
	}

	public boolean getIsCPF() {
		try {
			return CPFouCNPJRadio.equals("cpf");
		} catch (Exception e) {
			return false;
		}
	}

	public boolean getIsCNPJ() {
		try {
			return CPFouCNPJRadio.equals("cnpj");
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean getPodeGerar() {
		return podeGerar;
	}

	public void setPodeGerar(Boolean podeGerar) {
		this.podeGerar = podeGerar;
	}

	public Boolean getHashValidado() {
		return hashValidado;
	}

	public void setHashValidado(Boolean hashValidado) {
		this.hashValidado = hashValidado;
	}

	public String getCertidaoNegativa() {
		return certidaoNegativa;
	}

	public void setCertidaoNegativa(String certidaoNegativa) {
		this.certidaoNegativa = certidaoNegativa;
	}

	public void setPodeValidar(Boolean podeValidar) {
		this.podeValidar = podeValidar;
	}

	public Boolean getPodeValidar() {
		return podeValidar;
	}

	public void setarPodeValidar() {
		setPodeValidar(Boolean.FALSE);
	}

}