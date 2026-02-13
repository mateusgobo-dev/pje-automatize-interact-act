package br.com.infox.editor.interpretadorDocumento;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
    * Classe que implementa a espeficifação da linguagem formal de geração de texto baseado nos modelos.
    * <p>
    * @see      verificarSintaxe, converterDocumento
    * @see      getDetalheErroSintaxe, getListaViariaveis
    * @see      setModelo, setTabelaValores
    * @see      LinguagemFormalException
    * @author   Marcos Scapin, Francis Tscheliski
*/
public class InterpretadorDocumentos {
    public final String MIME_TYPE_TEXT = "text/plain";
    public final String MIME_TYPE_HTML = "text/html";

    private String mimeTypeDefault = MIME_TYPE_TEXT;
    private String mimeType = mimeTypeDefault;
    private List<String> modelo;
    private List<String> docInterpretado;
    private Map<String, Valor> tabelaValores = new HashMap<String, Valor>();
    private List<Token> listaTokens = new LinkedList<Token>();
    private int indLookAhead = 0;
    private int linhaAnterior = 0;
    private List<Token> lookAhead = new LinkedList<Token>();
    private int indPilhaPARA = -1;  // inicializa com -1 porque sempre soma++ quando entra em um PARA
    private Stack<ItemPilhaPARA> pilhaPARA = new Stack();
    private String detalheErroSintaxe;
    private Stack<ItemPilhaPalavraReservadaSemFim> palavrasReservadasSemFim = new Stack();

    /**
     * Recupera o mime-type do modelo do documento
     * @return  String com o mime-type corrente
     * @see     setMimeType, setModelo
    */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Seta o mime-type do modelo do documento
     * <p>
     * Tipo suportados: text/plain (default), text/html
     * 
     * @param  mimeType String com 
     * @see             getMimeType, setModelo
    */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private List<String> quebraLinhasModelo(String modelo) throws AnalisadorSintaticoException {
        List<String> listaLinhas = new LinkedList<String>();
        String quebra = "\\n";  // MIME_TYPE_TEXT
        
        try {
            modelo = unescapeModelo(modelo);
        	
        	// seta a regex de quebra de acordo com o mime-type
            if (getMimeType().equalsIgnoreCase(MIME_TYPE_HTML)) {
                modelo = modelo.replaceAll("\\n", "");
                quebra = "(</p>|</P>|<br\\s*/*>|<BR\\s*/*>)";
            }

            // quebra o modelo em linhas
            Pattern pattern = Pattern.compile(quebra);
            Matcher matcher = pattern.matcher(modelo);
            if (!matcher.find()) {
                listaLinhas.add(modelo);
            }
            else {
                matcher.reset();
                int inicioLinha = 0;
                while (matcher.find()) {
                    int fimLinha = matcher.start();
                    String quebraLinha = matcher.group();

                    // texto antes da quebra + a quebra encontrada pelo pattern
                    String textoAntes = modelo.substring(inicioLinha, fimLinha);
                    listaLinhas.add(textoAntes + quebraLinha);

                    inicioLinha = fimLinha + quebraLinha.length();
                }
                // texto depois do quebra
                String textoDepois = modelo.substring(inicioLinha);
                listaLinhas.add(textoDepois);
            }
        }
        catch (Exception ex) {
            throw new AnalisadorSintaticoException(ex);
        }
        return listaLinhas;
    }
    
    /**
     * Seta o modelo do documento para ser usado na geração do documento (ver converterDocumento).
     * @param  modelo   uma String com o modelo a ser convertido
     * @see             converterDocumento, getMimeType, setMimeType
    */
    public void setModelo(String modelo) throws AnalisadorSintaticoException {
        this.modelo = quebraLinhasModelo(modelo);
    }
    
	private String unescapeModelo(String modelo) {
		modelo = StringEscapeUtils.unescapeXml(modelo);
		modelo = StringEscapeUtils.unescapeHtml(modelo);
		modelo = modelo.replaceAll("&apos;", "'");
		modelo = modelo.replaceAll("&gt;", ">");
		modelo = modelo.replaceAll("&lt;", "<");
		modelo = modelo.replaceAll("&#33;", "!");
		modelo = modelo.replaceAll("&#61;", "=");
		modelo = modelo.replaceAll("&quot;", "\"");
		modelo = modelo.replaceAll("&#36;", "$");
		modelo = modelo.replaceAll("&#123;", "{");
		modelo = modelo.replaceAll("&#125;", "}");
		modelo = modelo.replaceAll("&#126;", "~");
		modelo = modelo.replaceAll("&#94;", "^");
		modelo = modelo.replaceAll("&#95;", "_");
		return modelo;
	}
    
    public void setValor(String chave, Valor valor) throws NomeVariavelInvalidoException {
        if (chave.indexOf(".") >= 0)
            throw new NomeVariavelInvalidoException(chave);
        else
            this.tabelaValores.put(chave.toLowerCase(), valor);
    }
    
    /**
     * Atualiza os valores das variáveis do tipo ValorBooleano utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor Date contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorBooleano
    */
    public void setValor(String chave, Boolean valor) throws NomeVariavelInvalidoException {
        setValor(chave, new ValorBooleano(valor));
    }
    
    /**
     * Atualiza os valores das variáveis do tipo ValorData utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor Date contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorData
    */
    public void setValor(String chave, Date valor) throws NomeVariavelInvalidoException {
        setValor(chave, new ValorData(valor));
    }
    
    /**
     * Atualiza os valores das variáveis do tipo ValorInteiro utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor Integer contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorInteiro
    */
    public void setValor(String chave, Integer valor) throws NomeVariavelInvalidoException {
        setValor(chave, new ValorInteiro(valor));
    }
	    
    /**
     * Atualiza os valores das variáveis do tipo ValorReal utilizadas no modelo a ser convertido.
     * Considera que o valor double é moeda (Real R$)
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor Double contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorReal
    */
    public void setValor(String chave, Double valor) throws NomeVariavelInvalidoException {
        setValor(chave, new ValorReal(valor));
    }
	    
    /**
     * Atualiza os valores das variáveis do tipo ValorTexto utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor String contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorTexto
    */
    public void setValor(String chave, String valor) throws NomeVariavelInvalidoException {
        setValor(chave, new ValorTexto(valor));
    }
	    
    /**
     * Atualiza os valores das variáveis do tipo ValorLista utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor String[] contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorLista, TipoValorInvalidoException
    */
    public void setValor(String chave, Object[] valor) throws TipoValorInvalidoException, NomeVariavelInvalidoException {
        setValor(chave, new ValorLista(Arrays.asList(valor)));
    }
	    
    /**
     * Atualiza os valores das variáveis do tipo ValorLista utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor List<String> contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorLista, TipoValorInvalidoException
    */
    public void setValor(String chave, List<Object> valor) throws TipoValorInvalidoException, NomeVariavelInvalidoException {
        setValor(chave, new ValorLista(valor));
    }
	    
    /**
     * Atualiza os valores das variáveis do tipo ValorEstruturado utilizadas no modelo a ser convertido.
     * @param   chave String contendo o nome da variável a ser atualizada (OBS: as chaves são case insensitive)
     * @param   valor Map<String, Object> contendo o valor a ser atribuído à variável em "chave"
     * @see     setModelo, getListaVariaveis, converterDocumento
     * @see     LinguagemFormalException, Valor, ValorEstruturado, TipoValorInvalidoException
    */
    public void setValor(String chave, Map<String, Object> valor) throws TipoValorInvalidoException, NomeVariavelInvalidoException {
        setValor(chave, new ValorEstruturado(valor));
    }
	    
    /**
     * Obtém a lista de variáveis utilizadas no modelo a ser convertido.
     * @return  HashMap<String, String> contendo as chaves preenchidas onde cada uma representa uma variável do modelo
     * @see     setModelo, setTabelaValores, converterDocumento
     * @see     LinguagemFormalException, HashMap
    */
    public Map<String, Valor> getListaVariaveis() throws LinguagemFormalException {
        try {
            // executa o léxico
            analisadorLexico(modelo);
        } catch (AnalisadorLexicoException ex) {
            throw new LinguagemFormalException(ex);
        }
        
        return tabelaValores;
    }
	    
    /**
     * Verifica a sintaxe de dado modelo de texto segundo a especificção da linguagem formal de geração de texto.
     * Esse método retorna um valor booleano indicando se o modelo é válido.
     * Se retornar true, é válido. Se retornar false, é inválido e o detalhamento 
     * do erro de sintaxe pode ser obtido pelo método getDetalheErroSintaxe().
     *
     * @param   modelo  uma String com o modelo a ser a sintaxe validada
     * @return  boolean representando a validade do modelo
     * @see     getDetalheErroSintaxe()
    */
    public boolean verificarSintaxe(String modelo) throws AnalisadorSintaticoException {
        boolean sintakeOK = true;
        List<String> docFinal = quebraLinhasModelo(modelo);
        detalheErroSintaxe = "";
        
        // 2º passo: analisador LÉXICO
        try {
            analisadorLexico(docFinal);
        } catch (AnalisadorLexicoException ex) {
            sintakeOK = false;
            detalheErroSintaxe = ex.getLocalizedMessage();
        }

        // 3º passo: analisador SINTÁTICO
        if (sintakeOK) {
            lookAhead.addAll(listaTokens);
            indLookAhead = 0;        
            try {
                analisadorSintatico();
            } catch (AnalisadorSintaticoException ex) {
                sintakeOK = false;
                detalheErroSintaxe = ex.getLocalizedMessage();
            }
        }

        return sintakeOK;
        
    }    

    /**
     * Converte dado modelo de texto segundo a especificção da linguagem formal em um documento final.
     * <p>
     * Esse método retorna uma String contendo o texto interpretado a partir do modelo.
     * <p>
     * Se ocorrer um erro de execução ou sintaxe durante a interpretação do modelo é disparada a exceção LinguagemFormalException
     * cuja descrição contém o detalhamento do erro.
     * @return  boolean representando a validade do modelo
     * @see     setModelo, getListaVariaveis, setTabelaValores, LinguagemFormalException
    */
    public String converterDocumento() throws LinguagemFormalException {
        String docFinal = "";
        
        try {
            Set<String> chaves = tabelaValores.keySet();
            for (String chave : chaves) {
                if (tabelaValores.get(chave) == null)
                    tabelaValores.put(chave, new ValorTexto(""));
            }

            // 1º passo: analisador LÉXICO
            analisadorLexico(modelo);

            // 2º passo: analisador SINTÁTICO
            lookAhead.addAll(listaTokens);
            indLookAhead = 0;        
            analisadorSintatico();

            // 3º passo: interpretar o MODELO
            indLookAhead = 0;
            docFinal = interpretadorModelo();

        }
        catch (Exception ex) {
            throw new LinguagemFormalException(ex);
        }

        return docFinal;
    }

    /**
     * Se forem encontrados erros durante a execução do método verificarSintaxe, o detalhe do erro pode ser obtido por esse método.
     * <p>
     * @return  String contendo o detalhamento do erro encontrado pelo método verificarSintaxe
     * @see     verificarSintaxe()
    */
    public String getDetalheErroSintaxe() {
        return detalheErroSintaxe;
    }
    
    private String getIndiceValorNaPilha(Valor valor) {
        String naPilha = "";
        for (int i=0; i<pilhaPARA.size(); i++) {
            if (pilhaPARA.get(i).getValorIdentificador().equals(valor)) {
                naPilha = ((Integer) pilhaPARA.get(i).getIndicePARA()).toString();
                break;
            }
        }
        return naPilha;
    }
    
    private Valor maisUmNivel(Valor valor, String resto) {
        Valor valorNivel = null;
        if (valor instanceof ValorEstruturado) {
            ValorEstruturado valorEstruturado = (ValorEstruturado) valor;
            valorNivel = getValorAtributo(valorEstruturado, resto);
        }
        else if (valor instanceof ValorLista) {
            ValorLista valorLista = (ValorLista) valor;
            valorNivel = getValorElemento(valorLista, resto);
        }
        return valorNivel;
    }
    
    private Valor getValorAtributo(ValorEstruturado estrutura, String atributo) {
        String nomeAtributo;
        // separa a variável
        int posSeparador = atributo.indexOf(".");
        if (posSeparador < 0)
            nomeAtributo = atributo;
        else {
            nomeAtributo = atributo.substring(0, posSeparador);
        }
        // recupera o valor
        Valor valorAtributo = estrutura.getEstrutura().get(nomeAtributo);
        String restoAtributo = "";
        if (posSeparador >= 0)
            restoAtributo = atributo.substring(posSeparador+1);
        // verifica se está dentro de um PARA, logo é lista e tem resto
        String indiceValorNaPilha = getIndiceValorNaPilha(valorAtributo);
        if (!indiceValorNaPilha.equals("")) {
            if (!restoAtributo.equals(""))
                restoAtributo = "." + restoAtributo;
            restoAtributo = indiceValorNaPilha + restoAtributo;
        }
        // testa o nível
        if ((restoAtributo != null) && (!restoAtributo.equalsIgnoreCase("")))
            valorAtributo = maisUmNivel(valorAtributo, restoAtributo);
        return valorAtributo;
    }

    private Valor getValorElemento(ValorLista lista, String elemento) {
        String indiceElemento;
        // separa a variável
        int posSeparador = elemento.indexOf(".");
        if (posSeparador < 0)
            indiceElemento = elemento;
        else
            indiceElemento = elemento.substring(0, posSeparador);
        // recupera o valor
        Valor valorElemento;
        try {
            valorElemento = lista.getLista().get(Integer.parseInt(indiceElemento));
        }
        catch (Exception ex) {
            valorElemento = new ValorTexto("");
        }
        String restoElemento = "";
        if (posSeparador >= 0)
            restoElemento = elemento.substring(posSeparador+1);
        // verifica se está dentro de um PARA, logo é lista e tem resto
        String indiceValorNaPilha = getIndiceValorNaPilha(valorElemento);
        if (!indiceValorNaPilha.equals("")) {
            if (!restoElemento.equals(""))
                restoElemento = "." + restoElemento;
            restoElemento = indiceValorNaPilha + restoElemento;
        }
        // testa o nível
        if ((restoElemento != null) && (!restoElemento.equalsIgnoreCase("")))
            valorElemento = maisUmNivel(valorElemento, restoElemento);
        return valorElemento;
    }

    private Valor getValor(String variavel) {
        String nomeVariavel;
        // separa a variável
        int posSeparador = variavel.indexOf(".");
        if (posSeparador < 0)
            nomeVariavel = variavel;
        else 
            nomeVariavel = variavel.substring(0, posSeparador);
        // recupera o valor
        Valor valor = tabelaValores.get(nomeVariavel.toLowerCase());
        String restoVariavel = "";
        if (posSeparador >= 0)
            restoVariavel = variavel.substring(posSeparador+1);
        // verifica se está dentro de um PARA, logo é lista e tem resto
        String indiceValorNaPilha = getIndiceValorNaPilha(valor);
        if (!indiceValorNaPilha.equals("")) {
            if (!restoVariavel.equals(""))
                restoVariavel = "." + restoVariavel;
            restoVariavel = indiceValorNaPilha + restoVariavel;
        }
        // testa o nível
        if ((restoVariavel != null) && (!restoVariavel.equalsIgnoreCase("")))
            valor = maisUmNivel(valor, restoVariavel);
        return valor;
    }
    
    private String getValorVAL(String variavel, String modificador) {
        String valor;
        
        // recupera o valor
        Valor auxValor = getValor(variavel.toLowerCase());
        // trata o valor
        if (auxValor != null) {
            // testa o modificador $
            boolean valorExato = "$".equals(modificador);
            // trata o valor por extenso ou não
            if (!valorExato)
                valor = auxValor.getValorExtenso();
            else
                valor = auxValor.getValor();
            // trata a capitalização
            if (!valorExato) {
                if (variavel.equals(variavel.toLowerCase())) {
                    valor = valor.toLowerCase();
                }
                else if (variavel.equals(variavel.toUpperCase())) {
                    valor = valor.toUpperCase();
                }
                else if ((variavel.substring(0, 1).equals(variavel.substring(0, 1).toUpperCase())) && 
                         (variavel.substring(1).equals(variavel.substring(1).toLowerCase()))) {
                    String toBeCapped2 = valor.toLowerCase();
                    String[] tokens = toBeCapped2.split("\\s");
                    StringBuilder toBeCapped = new StringBuilder();
                    for(int i = 0; i < tokens.length; i++) {
                        if ((tokens[i] != null) && (tokens[i].trim().length() > 0)) { 
                            if ((i==0) || (!(",a,o,e,do,da,de,na,em,nem,no,que,".contains("," + tokens[i] + ",")))) {
                                char capLetter = Character.toUpperCase(tokens[i].charAt(0));
                                toBeCapped.append(" ");
                                toBeCapped.append(capLetter);
                                toBeCapped.append(tokens[i].substring(1, tokens[i].length()));
                            }
                            else {
                                toBeCapped.append(" ");
                                toBeCapped.append(tokens[i].trim());
                            }
                        }
                    }
                    valor = toBeCapped.toString().trim();
                }
            }
        }
        else
            valor = "";
        return valor;
    }
    
    private void reconheceSimbolo(String token, String lexema, int linha) throws AnalisadorLexicoException {
        // tratamento de identificadores
        try {
            if (token.equalsIgnoreCase("identificador")) {
                // verifica a existencia de $
                if (lexema.startsWith("$")) {
                    // insere um modificador na lista de tokens
                    listaTokens.add(new Token("modificador", "$", linha));
                    // ignora o $ no nome da variável
                    lexema = lexema.replace("$", "");
                }
                // testa se a variável é composta
                String[] partesNome = lexema.split("\\.");
                String nomeVariavel;
                if (partesNome.length <= 0)
                    nomeVariavel = lexema;
                else 
                    nomeVariavel = partesNome[0];
                // se identificador ainda não estiver na lista, insere na lista de variaveis
                if (!tabelaValores.containsKey(nomeVariavel.toLowerCase()))
                    tabelaValores.put(nomeVariavel.toLowerCase(), null);
            }
            // insere na lista de tokens
            listaTokens.add(new Token(token, lexema, linha));
        }
        catch (Exception ex) {
            throw new AnalisadorLexicoException(ex,linha);
        }
    }
	    
    private String reconheceTermoCondicao(String condicao, int pos, int contLinha) throws AnalisadorLexicoException {

        // se a condicao inicia com uma letra, eh um identificador
        if (Character.isLetter(condicao.charAt(0))) {
            // enquanto for letra ou numero ou _, continua buscando o identificador
            while ((Character.isLetterOrDigit(condicao.charAt(pos))) || (condicao.charAt(pos) == '_') || (condicao.charAt(pos) == '.')) {
                pos++;

                if (pos >= condicao.length()) 
                    break;
            }

            reconheceSimbolo("identificador", condicao.substring(0, pos), contLinha);

        } else if (Character.isDigit(condicao.charAt(0))) {

            // enquanto for digito, continua buscando o número
            while ((Character.isDigit(condicao.charAt(pos)))) {
                pos++;

                if (pos >= condicao.length()) 
                    break;
            }

            reconheceSimbolo("numero", condicao.substring(0, pos), contLinha);
        } else if (condicao.charAt(pos) == '"') {
            int posFim = condicao.indexOf('"', pos + 1);

            if (posFim < 0) {
                throw new AnalisadorLexicoException("(Literal não finalizado. \" (aspas) faltando.", contLinha);
            }

            reconheceSimbolo("literal", condicao.substring(1, posFim), contLinha); // nao guarda as aspas
            pos = posFim + 1;
        }

        // retira a primeira parte da condicao
        condicao = condicao.substring(pos).trim();        

        return condicao;
    }
	    
    private void reconheceTermosCondicao(String condicao, int contLinha) throws AnalisadorLexicoException {
        int pos = 0;

        condicao = condicao.trim();

        // busca o PRIMEIRO termo da condicao        
        condicao = reconheceTermoCondicao(condicao, pos, contLinha);
        pos = 0;

        // busca o SEGUNDO termo da condicao (se houver). Testa a existencia de um operador relacional, ou no caso do ! (negacao)
        if (!condicao.isEmpty()) {
            switch (condicao.charAt(pos)) {
                case '=': {
                    reconheceSimbolo("operadorRelacional", condicao.substring(0, pos + 1), contLinha);
                    pos++;
                } break;
                case '>':
                case '<': {
                    if ((condicao.charAt(pos + 1) == '=') || (condicao.charAt(pos + 1) == '<') || (condicao.charAt(pos + 1) == '>')) {
                        pos = pos + 2;
                    } else {
                        pos++;
                    }

                    reconheceSimbolo("operadorRelacional", condicao.substring(0, pos), contLinha);
                } break;
                case '!': {
                    if (condicao.charAt(pos + 1) == '=') {
                        reconheceSimbolo("operadorRelacional", condicao.substring(0, pos + 2), contLinha);
                        pos = pos + 2;
                    } else {
                        reconheceSimbolo("operadorBooleano", condicao.substring(0, pos + 1), contLinha);
                        pos++;
                    }

                } break;

            }
        }

        condicao = condicao.substring(pos).trim();
        pos = 0;

        // busca o TERCEIRO termo da condição (se houver)
        if (!condicao.isEmpty()) {
            condicao = reconheceTermoCondicao(condicao, pos, contLinha);
        }
    }

    private void analisadorLexico(List<String> doc) throws AnalisadorLexicoException {
        String linha;
        int contLinha = 0;
        
        try {
            if (listaTokens != null) {
                listaTokens.clear();
            }
            if (lookAhead != null) {
                lookAhead.clear();
            }
            if (docInterpretado != null) {
                docInterpretado.clear();
            }

            // para cada linha do documento
            for (contLinha = 0; contLinha < doc.size(); contLinha++) {
                linha = doc.get(contLinha).trim();
                
                // procura os textos entre {} na linha
                String listaComandos = "(VAL|SE|SENAO|SENAOSE|FIMSE|PARA|FIMPARA)";
                String padraoPesquisa = "\\{" + listaComandos + "[\\$\\w\\s\\.\\\"<>!=]*\\}";
                //String padraoPesquisa = "\\{((VAL [\\$\\w\\.]+)|((SE|SENAOSE) [(\\w\\.)|(\\\"[\\W\\w\\s\\.]*\\\")|(<>!=)]+)|(PARA [\\w\\.]+)|(SENAO)|(FIMSE)|(FIMPARA))\\}";
                Pattern pattern = Pattern.compile(padraoPesquisa, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(linha);
                // processa a análise léxica da linha
                if (!matcher.find()) {
                    reconheceSimbolo("texto", linha, contLinha + 1);
                }
                else {
                    matcher.reset();
                    int fechaChave = 0;
                    while (matcher.find()) {
                        int abreChave = matcher.start();
                        String comando = matcher.group();
                        
                        // texto antes da {palavra reservada}
                        String textoAntes = linha.substring(fechaChave, abreChave);
                        if (textoAntes.length() > 0) {
                            reconheceSimbolo("texto", textoAntes, contLinha + 1);
                        }
                        fechaChave = abreChave + comando.length();
                        
                        // tratamento da {palavra reservada}
                        String padraoReservada = "\\{" + listaComandos + "( |\\})";
                        Pattern patternReservada = Pattern.compile(padraoReservada);
                        Matcher matcherReservada = patternReservada.matcher(comando);
                        if (matcherReservada.find()) {
                            String palavraReservada = matcherReservada.group().replaceAll("(\\{| |\\})", "");
                            
                            // reconhecimento do { (início)
                            reconheceSimbolo("separador", "{", contLinha + 1);
                            // reconhecimento da palavra reservada
                            reconheceSimbolo(palavraReservada, palavraReservada, contLinha + 1);
                            
                            // reconhecimento do complemento do comando, se houver
                            if (palavraReservada.equalsIgnoreCase("SE") || 
                                palavraReservada.equalsIgnoreCase("SENAOSE") || 
                                palavraReservada.equalsIgnoreCase("PARA") || 
                                palavraReservada.equalsIgnoreCase("VAL")) {
                                
                                String complementoComando = comando.substring(1 + palavraReservada.length(), comando.trim().length()-1).trim();
                                if (palavraReservada.equalsIgnoreCase("SE") || palavraReservada.equalsIgnoreCase("SENAOSE")) {
                                    reconheceTermosCondicao(complementoComando, contLinha + 1);
                                }
                                else if (palavraReservada.equalsIgnoreCase("PARA")) {
                                    reconheceSimbolo("identificador", complementoComando, contLinha + 1);
                                }
                                else if (palavraReservada.equalsIgnoreCase("VAL")) {
                                    reconheceSimbolo("identificador", complementoComando, contLinha + 1);
                                }
                            }
                            
                            // reconhecimento do } (final)
                            reconheceSimbolo("separador", "}", contLinha + 1);

                        }
                        
                    }
                    // texto depois do último {}
                    String textoDepois = linha.substring(fechaChave);
                    if (textoDepois.length() > 0) {
                        reconheceSimbolo("texto", textoDepois, contLinha + 1);
                    }
                }
                
            }
        } catch (Exception ex) {
            throw new AnalisadorLexicoException(ex, contLinha+1);
        }
    }
    
    private void reconhecer(String tokenBNF) throws AnalisadorSintaticoException {
        
        if (indLookAhead < lookAhead.size()) {
            if ("identificador".equals(tokenBNF) || "texto".equals(tokenBNF) || "numero".equals(tokenBNF) || "literal".equals(tokenBNF) || 
                "operadorBooleano".equals(tokenBNF) || "operadorRelacional".equals(tokenBNF)) {
                // se "tokenBNF" for igual ao elemento do "lookAhead"
                if (lookAhead.get(indLookAhead).getToken().equals(tokenBNF)) {
                    indLookAhead++; // avanca para o proximo elemento da lista
                }
                else {
                    throw new AnalisadorSintaticoException("\"" + tokenBNF + "\" esperado mas \"" + lookAhead.get(indLookAhead).getLexema() + "\" encontrado.", lookAhead.get(indLookAhead).getLinha());
                }
            } else { // se nao for identificador, literal ou operadorBooleano
                // se o LEXEMA for igual ao tokenBNF, reconhece
                if (lookAhead.get(indLookAhead).getLexema().equals(tokenBNF)) {
                    if (("SE".equalsIgnoreCase(tokenBNF)) || ("PARA".equalsIgnoreCase(tokenBNF))) {
                        palavrasReservadasSemFim.push(new ItemPilhaPalavraReservadaSemFim(tokenBNF, lookAhead.get(indLookAhead).getLinha()));
                    }
                    else if (!palavrasReservadasSemFim.empty()) {
                        if ("SE".equalsIgnoreCase(palavrasReservadasSemFim.peek().getPalavra()) && ("FIMSE".equalsIgnoreCase(tokenBNF))) {
                            palavrasReservadasSemFim.pop();
                        }
                        else if ("PARA".equalsIgnoreCase(palavrasReservadasSemFim.peek().getPalavra()) && ("FIMPARA".equalsIgnoreCase(tokenBNF))) {
                            palavrasReservadasSemFim.pop();
                        }
                    }
                    indLookAhead++; // avanca para o proximo elemento da lista
                } else {
                    throw new AnalisadorSintaticoException("\"" + tokenBNF + "\" esperado mas \"" + lookAhead.get(indLookAhead).getLexema() + "\" encontrado.", lookAhead.get(indLookAhead).getLinha());
                }
            }
        }
        else 
            throw new AnalisadorSintaticoException("\"" + tokenBNF + "\" esperado.", lookAhead.get(indLookAhead).getLinha());
    }
	    
    private void doCondicao() throws AnalisadorSintaticoException {
        // operador booleano eh opcional
        if ("operadorBooleano".equals(lookAhead.get(indLookAhead).getToken())) {
            reconhecer("operadorBooleano");
        }

        if ("identificador".equals(lookAhead.get(indLookAhead).getToken())) {
            reconhecer("identificador");

            // se o proximo token for um OPERADOR_RELACIONAL
            if ("operadorRelacional".equals(lookAhead.get(indLookAhead).getToken())) { 
                reconhecer("operadorRelacional");

                // verifica o proximo token entre IDENTIFICADOR, NUMERO ou LITERAL
                if ("identificador".equals(lookAhead.get(indLookAhead).getToken())) { 
                    reconhecer("identificador");
                } else if ("numero".equals(lookAhead.get(indLookAhead).getToken())) { 
                    reconhecer("numero");

                } else { // if ("literal".equals(lookAhead.get(indLookAhead).getToken())) { 
                    reconhecer("literal");
                }
            }
        } else if ("numero".equals(lookAhead.get(indLookAhead).getToken())) {
            reconhecer("numero");
            reconhecer("operadorRelacional");
            reconhecer("identificador");            
        } else { // if ("literal".equals(lookAhead.get(indLookAhead).getToken())) {
            reconhecer("literal");
            reconhecer("operadorRelacional");
            reconhecer("identificador");            
        }

    }

    private void doComando() throws AnalisadorSintaticoException {
        boolean executouAlgo = false;

        // se houver elementos na lista
        if (indLookAhead < lookAhead.size()) {
            // se o token no lookAhead for um "texto"
            if ("texto".equals(lookAhead.get(indLookAhead).getToken())) {
                reconhecer("texto");
                executouAlgo = true;
            } 
            else if ("{".equals(lookAhead.get(indLookAhead).getLexema())) {
                if ("VAL".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                    reconhecer("{");
                    reconhecer("VAL");
                    if ("$".equals(lookAhead.get(indLookAhead).getLexema()))
                        reconhecer("$");
                    reconhecer("identificador");
                    reconhecer("}");
                    doComando();

                    executouAlgo = true;
                }
                // se o proximo item for SE
                else if ("SE".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                    reconhecer("{");
                    reconhecer("SE");
                    doCondicao();
                    reconhecer("}");
                    doComando();

                    // verifica a existencia do bloco SENAOSE
                    // verifica o indLookAhead + 1, pois o atual eh o {
                    while ("SENAOSE".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                        reconhecer("{");
                        reconhecer("SENAOSE");
                        doCondicao();
                        reconhecer("}");
                        doComando();                        
                    }

                    // verifica a existencia do bloco SENAO
                    // verifica o indLookAhead + 1, pois o atual eh o {
                    if ("SENAO".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                        reconhecer("{");
                        reconhecer("SENAO");
                        reconhecer("}");
                        doComando();                        
                    }                    

                    reconhecer("{");
                    reconhecer("FIMSE");
                    reconhecer("}");  

                    executouAlgo = true;
                }
                // se o proximo item for PARA 
                else if ("PARA".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                    reconhecer("{");
                    reconhecer("PARA");
                    reconhecer("identificador");
                    reconhecer("}");
                    doComando();
                    reconhecer("{");
                    reconhecer("FIMPARA");
                    reconhecer("}");     

                    executouAlgo = true;
                }
            }

            if (executouAlgo) {
                doComando();
            }
        }
        else {
            if (!palavrasReservadasSemFim.empty()) {
                String esperado = "DESCONHECIDO";
                if ("SE".equalsIgnoreCase(palavrasReservadasSemFim.peek().getPalavra())) {
                    esperado = "FIMSE";
                }
                else if ("PARA".equalsIgnoreCase(palavrasReservadasSemFim.peek().getPalavra())) {
                    esperado = "FIMPARA";
                }
                throw new AnalisadorSintaticoException("\"{" + esperado + "}\" esperado.", palavrasReservadasSemFim.peek().getLinha());
            }
        }
    }

    private void doModelo() throws AnalisadorSintaticoException {
        doComando();
    }

    private void analisadorSintatico() throws AnalisadorSintaticoException {
        try {
            doModelo();
        } catch (Exception ex) {
            throw new AnalisadorSintaticoException(ex);
        }
    }
	    
    private void processaTexto(String texto, int linhaAtual) throws InterpretadorException {
        try {

            int linhasDoc = docInterpretado.size();
            String textoAnterior = "";
            
            if (texto.trim().startsWith("^")) {
                // retira o caractere ^ e mantém todos os espaços existentes
                texto = texto.trim().substring(1);
            }

            if (linhasDoc > 0)
                textoAnterior = docInterpretado.get(linhasDoc-1);
            
            if (linhaAnterior != linhaAtual) {
                linhaAnterior = linhaAtual;
                docInterpretado.add(texto);
            }
            else
                docInterpretado.set(linhasDoc-1, textoAnterior + texto);
            
        }
        catch (Exception ex) {
            throw new InterpretadorException(ex);
        }
        
    }

    private boolean intCondicao() throws AnalisadorSintaticoException, InterpretadorException {
        Valor valor;
        String valorNulo = "|*|";
        String id1 = valorNulo;
        String id2 = valorNulo;
        double num1 = 0.0;
        double num2 = 0.0;
        String lit1 = valorNulo;
        String lit2 = valorNulo;
        String opRelacional = "";

        boolean ehNot = false;
        boolean resultado = false;

        try {
            // operador booleano é opcional
            if ("operadorBooleano".equals(lookAhead.get(indLookAhead).getToken())) {
                ehNot = true;
                reconhecer("operadorBooleano");
            }
        
            if ("identificador".equals(lookAhead.get(indLookAhead).getToken())) {
                valor = getValor(lookAhead.get(indLookAhead).getLexema().toLowerCase());
                if (valor instanceof ValorLista)
                    id1 = Integer.toString(((ValorLista) valor).getLista().size());
                else if (valor instanceof ValorEstruturado)
                    id1 = Integer.toString(((ValorEstruturado) valor).getEstrutura().size());
                else if (valor instanceof ValorReal)
                    id1 = ((ValorReal) valor).getNumero().toString();
                else
                    id1 = valor.getValor();
                reconhecer("identificador");

                // se o proximo token for um OPERADOR_RELACIONAL
                if ("operadorRelacional".equals(lookAhead.get(indLookAhead).getToken())) { 
                    opRelacional = lookAhead.get(indLookAhead).getLexema();
                    reconhecer("operadorRelacional");

                    // verifica o proximo token entre IDENTIFICADOR, NUMERO ou LITERAL
                    if ("identificador".equals(lookAhead.get(indLookAhead).getToken())) { 
                        valor = getValor(lookAhead.get(indLookAhead).getLexema().toLowerCase());
                        if (valor instanceof ValorLista)
                            id2 = Integer.toString(((ValorLista) valor).getLista().size());
                        else if (valor instanceof ValorEstruturado)
                            id2 = Integer.toString(((ValorEstruturado) valor).getEstrutura().size());
                        else if (valor instanceof ValorReal)
                            id2 = ((ValorReal) valor).getNumero().toString();
                        else
                            id2 = valor.getValor();
                        reconhecer("identificador");

                    } else if ("numero".equals(lookAhead.get(indLookAhead).getToken())) { 
                        num2 = Double.parseDouble(lookAhead.get(indLookAhead).getLexema());
                        reconhecer("numero");

                    } else { // if ("literal".equals(lookAhead.get(indLookAhead).getToken())) { 
                        lit2 = lookAhead.get(indLookAhead).getLexema();
                        reconhecer("literal");
                    }

                    // avalia a condição

                    // se for IDENTIFICADOR e IDENTIFICADOR
                    if (!id2.equalsIgnoreCase(valorNulo)) {
                        // tenta tratar como NÚMEROS
                        try {
                            if ("=".equals(opRelacional))
                                resultado = Double.parseDouble(id1) == Double.parseDouble(id2);
                            else if (">".equals(opRelacional))
                                resultado = Double.parseDouble(id1) > Double.parseDouble(id2);
                            else if ("<".equals(opRelacional))
                                resultado = Double.parseDouble(id1) < Double.parseDouble(id2);
                            else if ("<=".equals(opRelacional))
                                resultado = Double.parseDouble(id1) <= Double.parseDouble(id2);
                            else if (">=".equals(opRelacional))
                                resultado = Double.parseDouble(id1) >= Double.parseDouble(id2);
                            else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
                                resultado = Double.parseDouble(id1) != Double.parseDouble(id2);
                            else
                                throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
                        } catch (NumberFormatException e) { // se não for NÚMERO, trata como STRING
                            if ("=".equals(opRelacional))
                                resultado = id1.equals(id2);
                            else if (">".equals(opRelacional))
                                resultado = (id1.compareTo(id2) > 0);
                            else if ("<".equals(opRelacional))
                                resultado = (id1.compareTo(id2) < 0);
                            else if (">=".equals(opRelacional))
                                resultado = (id1.compareTo(id2) >= 0);
                            else if ("<=".equals(opRelacional))
                                resultado = (id1.compareTo(id2) <= 0);
                            else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
                                resultado = (id1.compareTo(id2) != 0);
                            else
                                throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
                        }
                    } else if ((!lit2.equalsIgnoreCase(valorNulo)) && (!id1.equalsIgnoreCase(valorNulo))) { // se for IDENTIFICADOR e LITERAL
                        if ("=".equals(opRelacional))
                            resultado = id1.equalsIgnoreCase(lit2);
                        else if (">".equals(opRelacional))
                            resultado = (id1.compareTo(lit2) > 0);
                        else if ("<".equals(opRelacional))
                            resultado = (id1.compareTo(lit2) < 0);
                        else if (">=".equals(opRelacional))
                            resultado = (id1.compareTo(lit2) >= 0);
                        else if ("<=".equals(opRelacional))
                            resultado = (id1.compareTo(lit2) <= 0);                    
                        else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
                            resultado = (id1.compareTo(lit2) != 0);
                        else
                            throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
                    } else { // se for IDENTIFICADOR e NUMERO
                        try {
							if (id1.equalsIgnoreCase(""))
							    id1 = "0";
							if ("=".equals(opRelacional))
							    resultado = Integer.parseInt(id1) == num2;
							else if (">".equals(opRelacional))
							    resultado = Double.parseDouble(id1) > num2;
							else if ("<".equals(opRelacional))
							    resultado = Integer.parseInt(id1) < num2;
							else if (">=".equals(opRelacional))
							    resultado = Integer.parseInt(id1) >= num2;
							else if ("<=".equals(opRelacional))
							    resultado = Integer.parseInt(id1) <= num2;
							else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
							    resultado = Integer.parseInt(id1) != num2;
							else
							    throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
						} catch (NumberFormatException e) { // compara tudo como string
							String auxNum2 = String.valueOf(num2);
							if ("=".equals(opRelacional))
								resultado = id1.equalsIgnoreCase(auxNum2);
							else if (">".equals(opRelacional))
								resultado = (id1.compareTo(auxNum2) > 0);
							else if ("<".equals(opRelacional))
								resultado = (id1.compareTo(auxNum2) < 0);
							else if (">=".equals(opRelacional))
								resultado = (id1.compareTo(auxNum2) >= 0);
							else if ("<=".equals(opRelacional))
								resultado = (id1.compareTo(auxNum2) <= 0);
							else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
								resultado = (id1.compareTo(auxNum2) != 0);
							else
							    throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
						}
                    }
                } else { // se for IDENTIFICADOR e não tem um operador relacional
                    // tenta tratar ID1 como INTEIRO
                    try {
                        // se o valor for zero ou nula, o resultado será FALSO
                        resultado = (Double.parseDouble(id1) > 0);
                    } catch (Exception e) {
                        // se for false, N, NÃO ou nula, o resultado será FALSO
                        if (!((id1.equalsIgnoreCase("false")) || (id1.equalsIgnoreCase("n")) || (id1.equalsIgnoreCase("não")) || id1.isEmpty()))
                            resultado = true;
                    }
                }
            } else if ("numero".equals(lookAhead.get(indLookAhead).getToken())) {

                num1 = Double.parseDouble(lookAhead.get(indLookAhead).getLexema());
                reconhecer("numero");

                opRelacional = lookAhead.get(indLookAhead).getLexema();
                reconhecer("operadorRelacional");

                // o segundo termo DEVE ser um número
                valor = getValor(lookAhead.get(indLookAhead).getLexema().toLowerCase());
                if (valor instanceof ValorLista)
                    num2 = ((ValorLista) valor).getLista().size();
                else if (valor instanceof ValorEstruturado)
                    num2 = ((ValorEstruturado) valor).getEstrutura().size();
                else if (valor instanceof ValorReal)
                    num2 = ((ValorReal) valor).getNumero();
                else
                    num2 = Double.parseDouble(valor.getValor());
                reconhecer("identificador");

                if ("=".equals(opRelacional))
                    resultado = num1 == num2;
                else if (">".equals(opRelacional))
                    resultado = num1 > num2;
                else if ("<".equals(opRelacional))
                    resultado = num1 < num2;
                else if (">=".equals(opRelacional))
                    resultado = num1 >= num2;
                else if ("<=".equals(opRelacional))
                    resultado = num1 <= num2;
                else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
                    resultado = num1 != num2;
                else
                    throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
            } else { // if ("literal".equals(lookAhead.get(indLookAhead).getToken())) {
                lit1 = lookAhead.get(indLookAhead).getLexema();
                reconhecer("literal");

                opRelacional = lookAhead.get(indLookAhead).getLexema();
                reconhecer("operadorRelacional");

                valor = getValor(lookAhead.get(indLookAhead).getLexema().toLowerCase());
                if (valor instanceof ValorLista)
                    lit2 = Integer.toString(((ValorLista) valor).getLista().size());
                else if (valor instanceof ValorEstruturado)
                    lit2 = Integer.toString(((ValorEstruturado) valor).getEstrutura().size());
                else if (valor instanceof ValorReal)
                    lit2 = ((ValorReal) valor).getNumero().toString();
                else
                    lit2 = valor.getValor();
                reconhecer("identificador");            

                if ("=".equals(opRelacional))
                    resultado = lit1.equals(lit2);
                else if (">".equals(opRelacional))
                    resultado = (lit1.compareTo(lit2) > 0);
                else if (">=".equals(opRelacional))
                    resultado = (lit1.compareTo(lit2) >= 0);
                else if ("<".equals(opRelacional))
                    resultado = (lit1.compareTo(lit2) < 0);
                else if ("<=".equals(opRelacional))
                    resultado = (lit1.compareTo(lit2) <= 0);
                else if (("!=".equals(opRelacional)) || ("<>".equals(opRelacional)) || ("><".equals(opRelacional)))
                    resultado = (lit1.compareTo(lit2) != 0);
                else
                    throw new InterpretadorException("Operador relacional \""+ opRelacional + "\" inválido", lookAhead.get(indLookAhead).getLinha());
            }
        }
        catch (AnalisadorSintaticoException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InterpretadorException(ex);
        }
        
        if (ehNot)
            return !resultado;
        else
            return resultado;
    }

    private void intComando() throws AnalisadorSintaticoException, InterpretadorException {
        boolean executouAlgo = false;
        boolean resultCondicao;
        boolean EntrouNoSe = false;
        boolean EntrouNoSenaoSe = false;

        // se houver elementos na lista
        if (indLookAhead < lookAhead.size()) {
            // se o token no lookAhead for um "texto"
            if ("texto".equals(lookAhead.get(indLookAhead).getToken())) {
                processaTexto(lookAhead.get(indLookAhead).getLexema(), lookAhead.get(indLookAhead).getLinha());

                reconhecer("texto");
                executouAlgo = true;
            }
            else if ("{".equals(lookAhead.get(indLookAhead).getLexema())) {
                if ("VAL".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                    String modificador = "";
                    reconhecer("{");
                    reconhecer("VAL");
                    if ("$".equals(lookAhead.get(indLookAhead).getLexema())) {
                        modificador = "$";
                        reconhecer("$");
                    }
                    reconhecer("identificador");
                    reconhecer("}");
                    processaTexto(getValorVAL(lookAhead.get(indLookAhead-2).getLexema(), modificador), lookAhead.get(indLookAhead-2).getLinha());
                    
                    executouAlgo = true;
                }                
                // se o proximo item for SE
                else if ("SE".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                    reconhecer("{");
                    reconhecer("SE");
                    resultCondicao = intCondicao();
                    reconhecer("}");

                    if (resultCondicao == true) {
                        intComando(); // interpreta todos os comandos deste {SE
                        EntrouNoSe = true;
                    }
                    else {
                        doComando(); // somente reconhece, NÃO INTERPRETA                    
                    }

                    // verifica a existencia do bloco SENAOSE
                    // verifica o indLookAhead + 1, pois o atual eh o {
                    while ("SENAOSE".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                        reconhecer("{");
                        reconhecer("SENAOSE");
                        resultCondicao = intCondicao();
                        reconhecer("}");

                        if (resultCondicao == true && !EntrouNoSe && !EntrouNoSenaoSe) {
                            intComando(); // INTERPRETA todos os comandos deste {SENAOSE
                            EntrouNoSenaoSe = true;
                        }
                        else {
                            doComando(); // somente reconhece, NAO INTERPRETA                    
                        }
                    }

                    // verifica a existencia do bloco SENAO
                    // verifica o indLookAhead + 1, pois o atual eh o {
                    if ("SENAO".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                        reconhecer("{");
                        reconhecer("SENAO");
                        reconhecer("}");

                        if (!EntrouNoSe && !EntrouNoSenaoSe) {
                            intComando(); // INTERPRETA todos os comandos deste {SENAO
                        } else {
                            doComando(); // somente reconhece, NAO INTERPRETA
                        }
                    }                    

                    reconhecer("{");
                    reconhecer("FIMSE");
                    reconhecer("}");  

                    executouAlgo = true;

                // se o proximo item for PARA    
                } 
                else if ("PARA".equals(lookAhead.get(indLookAhead + 1).getToken())) {
                    reconhecer("{");
                    reconhecer("PARA");
                    String nomeIdentificador = lookAhead.get(indLookAhead).getLexema();
                    Valor valorIdentificador = getValor(nomeIdentificador);
                    if (valorIdentificador instanceof ValorLista) {
                        int limitePARA = ((ValorLista) valorIdentificador).getLista().size();
                        reconhecer("identificador");
                        reconhecer("}");
                        int auxIndLookAhead = indLookAhead;
                        indPilhaPARA++;
                        pilhaPARA.push(new ItemPilhaPARA(valorIdentificador));
                        for (int i=0; i<limitePARA; i++) {
                            pilhaPARA.get(indPilhaPARA).setIndicePARA(i);
                            intComando();
                            if (i<limitePARA-1)
                                indLookAhead = auxIndLookAhead;
                        }
                        pilhaPARA.pop();
                        indPilhaPARA--;
                    }
                    else {
                        reconhecer("identificador");
                        reconhecer("}");
                        intComando();
                    }
                    reconhecer("{");
                    reconhecer("FIMPARA");
                    reconhecer("}");     

                    executouAlgo = true;
                }                
            }

            if (executouAlgo) {
                intComando();
            }
        }
    }

    private void intModelo() throws AnalisadorSintaticoException, InterpretadorException {
        intComando();
    }

    private String interpretadorModelo() throws AnalisadorSintaticoException, InterpretadorException {
        StringBuilder strDocFinal = new StringBuilder();
        
        // zera o conteúdo a cada execução
        docInterpretado = new LinkedList<String>();

        //varrer o lookAhead para gerar o modelo
        intModelo();
        
        // devolve o documento como String
        try {
            for (int contLinha = 0; contLinha < docInterpretado.size(); contLinha++) {
                String quebra = "\n";
                String linha = docInterpretado.get(contLinha);
                // ajusta a quebra de linha controlada (~) em documentos TEXT
                if (getMimeType().equalsIgnoreCase(MIME_TYPE_TEXT)) {
                    if (linha.endsWith("~")) {
                        // tira a quebra se tiver ~ no fim
                        linha = linha.substring(0, linha.length()-1);
                        // não coloca quebra no final da linha
                        quebra = "";
                    }
                }
                // ajusta a quebra de linha controlada (~) em documentos HTML
                if (getMimeType().equalsIgnoreCase(MIME_TYPE_HTML)) {
                    String quebraPattern = "~(\\s)*(</P>|<BR\\s*/*>)";
                    Pattern pattern = Pattern.compile(quebraPattern, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(linha);
                    if (matcher.find()) {
                        // tira a quebra se tiver ~ no fim
                        linha = linha.substring(0, matcher.start());
                        // retira o <p> do começo da próxima linha, se houver
                        if ((contLinha+1) < docInterpretado.size()) {
                            String proximaLinha = docInterpretado.get(contLinha+1).trim();
                            if ((proximaLinha.length() > 2) && (proximaLinha.substring(0, 2).equalsIgnoreCase("<p"))) {
                                docInterpretado.set(contLinha+1, proximaLinha.substring(proximaLinha.indexOf(">")+1));
                            }
                        }
                        // não coloca quebra no final da linha
                        quebra = "";
                    }
                }
                strDocFinal.append(linha);
                strDocFinal.append(quebra);
            }
        }
        catch (Exception ex) {
            throw new InterpretadorException(ex);
        }

        return strDocFinal.toString();
    }
    
}