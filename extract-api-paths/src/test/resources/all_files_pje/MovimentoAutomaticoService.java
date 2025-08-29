package br.jus.csjt.pje.business.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;

/**
 * Classe de lançamento de movimento programaticamente, influenciada por conceitos de DDD (Domain Driven Design), Fluent Interface e padrão de projeto
 * Builder.
 * <br/<br/>
 * Uso em java:<br/>
 * <code>
 * MovimentoAutomaticoService.preencherMovimento().deCodigo(12) (...) .lancarMovimento();
 * </code>
 * <br/<br/>
 * Uso em EL:<br/>
 * <code>
 * #{preencherMovimento().deCodigo(12) (...) .lancarMovimento()}
 * </code>
 * 
 * @author David Vieira
 * 
 */
@Name(MovimentoAutomaticoService.NAME)
@Scope(ScopeType.EVENT)
public class MovimentoAutomaticoService{

	public static final String NAME = "preencherMovimento";

	public MovimentoBuilder deCodigo(int codigoDoMovimento){
		return new MovimentoBuilder("" + codigoDoMovimento);
	}

	public MovimentoBuilder deCodigo(String codigoDoMovimento){
		return new MovimentoBuilder(codigoDoMovimento);
	}

	// é private, pois só a partir do MovimentoBuilder deve ser lançado o movimento
	private ProcessoEvento lancarMovimento(MovimentoBuilder movimentoBuilder, boolean autoFlush){
		return LancadorMovimentosService.instance().lancarMovimento(movimentoBuilder, autoFlush);
	}

	public class MovimentoBuilder{

		protected String codigoDoMovimento;
		protected Integer idDocumento;
		protected Integer idProcesso;
		protected Integer idUsuario;
		protected Date dataAtualizacao;
		protected List<ComplementoBuilder> complementoBuilders = new ArrayList<ComplementoBuilder>();

		public MovimentoBuilder(String codigoDoMovimento){
			this.codigoDoMovimento = codigoDoMovimento;

			// setar padrões
			// por padrão o movimento não terá associação com o documento
			this.idDocumento = null;
			// por padrão o processo virá do ProcessoTrfHome ou ProcessoHome
			setarProcessoPadrao();
			// por padrão o usuário virá do usuario logado ou, se não encontrado, do usuário do sistema
			setarUsuarioPadrao();
		}

		private void setarUsuarioPadrao(){
			try{
				Usuario usuario;
				if (Contexts.getSessionContext().get("usuarioLogado") != null){
					usuario = Authenticator.getUsuarioLogado();
				}
				else{
					usuario = ParametroUtil
							.instance().getUsuarioSistema();
				}
				this.idUsuario = usuario.getIdUsuario();
			} catch (Exception e){
				// swallow, deixar usuario nulo em caso de erro
			}
		}

		private void setarProcessoPadrao(){
			try{
				Integer idProcessoEncontrado = null;
				ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
				if (processoTrf != null){
					idProcessoEncontrado = processoTrf.getIdProcessoTrf();
				}
				else{
					Processo processo = ProcessoHome.instance().getInstance();
					if (processo != null){
						idProcessoEncontrado = processo.getIdProcesso();
					}
				}

				if(idProcessoEncontrado == null || idProcessoEncontrado.equals(0)){
					TramitacaoProcessualService tramitacaoProcessualService = (TramitacaoProcessualService) Component.getInstance("tramitacaoProcessualService");
					ProcessoTrf p= null;
					if(!tramitacaoProcessualService.isProcessInstanceNula()) {
						try {
							p = tramitacaoProcessualService.recuperaProcesso();
						} catch (IllegalArgumentException e) {
							p = null;
						} catch (IllegalStateException ise) {
							p = null;
						}
						if (p != null) {
							idProcessoEncontrado = p.getIdProcessoTrf();
						}
					}
				}
				this.idProcesso = idProcessoEncontrado;
			} catch (Exception e){
				// swallow, deixar processo nulo em caso de erro
			}
		}
		
		/**
		 * Encaminhará para o serviço de lancamento de movimentações o objeto gerado aqui para ser lançado temporariamente no fluxo relacionado
		 * 
		 * @param processInstance
		 */
		public void lancarMovimentoTemporariamente(org.jbpm.graph.exe.ProcessInstance processInstance) {
			LancadorMovimentosService.instance().setMovimentosTemporarios(processInstance, this);
			LancadorMovimentosService.instance().setCondicaoLancamentoMovimentosTemporarioNoFluxo(processInstance, "#{true}");
		}

		public ProcessoEvento lancarMovimento(boolean autoFlush){
			return MovimentoAutomaticoService.this.lancarMovimento(this, autoFlush);
		}
		

		public ProcessoEvento lancarMovimento(){
			return lancarMovimento(true);
		}

		public void lancarMovimentoSe(boolean condicao){
			if (condicao){
				lancarMovimento();
			}
		}

		public MovimentoBuilder associarAoUsuario(Usuario usuario){
			this.idUsuario = usuario.getIdUsuario();
			return this;
		}

		public MovimentoBuilder associarAoDocumento(ProcessoDocumento documento){
			Integer idProcessoDocumento = null;
			if(documento != null && documento.getIdProcessoDocumento() > 0) {
				idProcessoDocumento = documento.getIdProcessoDocumento();
			}else {
				throw new IllegalArgumentException("Documento não informado ou inválido.");
			}
			return associarAoDocumentoDeId(idProcessoDocumento);
		}

		public MovimentoBuilder associarAoDocumento(ProcessoDocumentoTrf documento){
			Integer idProcessoDocumento = null;
			if(documento != null && documento.getIdProcessoDocumento() > 0) {
				idProcessoDocumento = documento.getIdProcessoDocumento();
			}else {
				throw new IllegalArgumentException("Documento não informado ou inválido.");
			}
			return associarAoDocumentoDeId(idProcessoDocumento);
		}

		public MovimentoBuilder associarAoDocumentoDeId(Integer idDocumento){
			this.idDocumento = idDocumento;
			return this;
		}
		
		public MovimentoBuilder associarAoProcesso(Processo processo){
			this.idProcesso = processo.getIdProcesso();
			return this;
		}

		public MovimentoBuilder associarAoProcesso(ProcessoTrf processo){
			this.idProcesso = processo.getIdProcessoTrf();
			return this;
		}
		
		public MovimentoBuilder associarADataAtualizacao(Date dataAtualizacao){
			this.dataAtualizacao = dataAtualizacao;
			return this;
		}
		
		private ComplementoBuilder adicionarComplemento(String codigoDoComplemento, String nomeDoComplemento){
			ComplementoBuilder complementoBuilder = new ComplementoBuilder(this, codigoDoComplemento, nomeDoComplemento);
			complementoBuilders.add(complementoBuilder);
			return complementoBuilder;
		}

		public MovimentoBuilder comComplementoBuilder(ComplementoBuilder complementoBuilder){
			complementoBuilders.add(complementoBuilder);
			return MovimentoBuilder.this;
		}

		public ComplementoBuilder comComplementoDeCodigo(int codigoDoComplemento){
			return adicionarComplemento("" + codigoDoComplemento, null);
		}

		public ComplementoBuilder comComplementoDeCodigo(String codigoDoComplemento){
			return adicionarComplemento(codigoDoComplemento, null);
		}

		public ComplementoBuilder comComplementoDeNome(String nomeDoComplemento){
			return adicionarComplemento(null, nomeDoComplemento);
		}

		public ComplementoBuilder comProximoComplementoVazio(){
			return adicionarComplemento(null, null);
		}

		public class ComplementoBuilder{

			protected TipoComplementoEnum tipoComplemento = TipoComplementoEnum.GENERICO;
			protected final String codigoDoComplemento;
			protected final String nomeDoComplemento;

			protected String codigo = "";
			protected String texto = "";

			public ComplementoBuilder(MovimentoBuilder pai, String codigoDoComplemento, String nomeDoComplemento){
				this.codigoDoComplemento = codigoDoComplemento;
				this.nomeDoComplemento = nomeDoComplemento;
			}

			public ComplementoBuilder preencherComCodigo(String codigo){
				this.codigo = codigo;
				return this;
			}

			public MovimentoBuilder preencherComTexto(String texto){
				this.texto = texto;
				return MovimentoBuilder.this;
			}
			/*
			 * [PJEII-3765] - Rodrigo S. Menezes: disponibilizando métodos para inclusão de complementos do tipo 'data' e 'data_hora' 
			 * como objetos do tipo Date ou Calendar, empregando máscaras de data ou data e hora dependendo do método utilizado.
			 */
			public MovimentoBuilder preencherComData(Object data)
			{
				if(data instanceof Date || data instanceof Calendar)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					this.texto = sdf.format(data);
				}
				
				return MovimentoBuilder.this;
			}
			
			public MovimentoBuilder preencherComDataHora(Object dataHora)
			{
				if(dataHora instanceof Date || dataHora instanceof Calendar)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					this.texto = sdf.format(dataHora);
				}
				
				return MovimentoBuilder.this;				
			}
			// FIM PJEII-3765
			public MovimentoBuilder preencherComObjeto(Object objetoGenerico){
				try{
					if (objetoGenerico instanceof ElementoDominio){
						// Se for ElementoDominio, preencher com codigoGlossario
						this.codigo = ((ElementoDominio) objetoGenerico).getCodigoGlossario();
					}
					else if (objetoGenerico instanceof ProcessoDocumento){
						// Se for ProcessoDocumento, preencher com codigo do TipoProcessoDocumento
						this.codigo = ((ProcessoDocumento) objetoGenerico).getTipoProcessoDocumento().getCodigoDocumento();
					}
					else if (objetoGenerico instanceof ProcessoDocumentoTrf){
						// Se for ProcessoDocumento, preencher com codigo do TipoProcessoDocumento
						this.codigo = ((ProcessoDocumentoTrf) objetoGenerico).getProcessoDocumento().getTipoProcessoDocumento().getCodigoDocumento();
					}
					else if (objetoGenerico instanceof TipoProcessoDocumento){
						// Se for TipoProcessoDocumento, preencher com código do TipoProcessoDocumento
						this.codigo = ((TipoProcessoDocumento) objetoGenerico).getCodigoDocumento();
					}
					else{
						// Se for entidade, preencher com id no código e toString no texto
						this.codigo = HibernateUtil.getIdAsString(objetoGenerico);
					}
				} catch (Exception e){
					// swallow, deixar codigo nulo em caso de erro
				}
				// senão só salvar o toString no texto e deixar código nulo
				this.texto = objetoGenerico.toString();
				return MovimentoBuilder.this;
			}

			public ComplementoBuilder.TipoComplementoLivreBuilder doTipoLivre(){
				this.tipoComplemento = TipoComplementoEnum.LIVRE;
				return new TipoComplementoLivreBuilder();
			}

			public ComplementoBuilder.TipoComplementoDinamicoBuilder doTipoDinamico(){
				this.tipoComplemento = TipoComplementoEnum.DINAMICO;
				return new TipoComplementoDinamicoBuilder();
			}

			public ComplementoBuilder.TipoComplementoDominioBuilder doTipoDominio(){
				this.tipoComplemento = TipoComplementoEnum.DOMINIO;
				return new TipoComplementoDominioBuilder();
			}

			public class TipoComplementoLivreBuilder{

				public MovimentoBuilder preencherComTexto(String textoLivre){
					ComplementoBuilder.this.texto = textoLivre;
					return MovimentoBuilder.this;
				}

			}

			public class TipoComplementoDominioBuilder{

				public MovimentoBuilder preencherComElementoDeCodigo(int codigoDoElementoDominio){
					return this.preencherComElementoDeCodigo("" + codigoDoElementoDominio);
				}

				public MovimentoBuilder preencherComElementoDeCodigo(String codigoDoElementoDominio){
					ComplementoBuilder.this.codigo = "" + codigoDoElementoDominio;
					ComplementoBuilder.this.texto = LancadorMovimentosService.instance().getElementoDominioByCodigoCnj("" + codigoDoElementoDominio).getValor();
					return MovimentoBuilder.this;
				}

			}

			public class TipoComplementoDinamicoBuilder{

				public MovimentoBuilder preencherComObjeto(Object objetoGenerico){
					return ComplementoBuilder.this.preencherComObjeto(objetoGenerico);
				}
				// PJEII-3765
				public MovimentoBuilder preencherComData(Object data)
				{
					return ComplementoBuilder.this.preencherComData(data);
				}
				
				public MovimentoBuilder preencherComDataHora(Object dataHora)
				{
					return ComplementoBuilder.this.preencherComDataHora(dataHora);
				}
				// FIM PJEII-3765
			}

		}

	}

	public static MovimentoAutomaticoService instance(){
		return ComponentUtil.getComponent(MovimentoAutomaticoService.NAME);
	}
	
	public static MovimentoAutomaticoService preencherMovimento(){
		return MovimentoAutomaticoService.instance();
	}

}

enum TipoComplementoEnum {
	LIVRE,
	DOMINIO,
	DINAMICO,
	GENERICO;
}