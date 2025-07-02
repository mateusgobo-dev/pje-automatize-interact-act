package br.com.peticoes.model;
import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Processo {
    public int total;
    public int numberOfElements;
    public int maxElementsSize;
    public List<Conteudo> content;

    @Getter
    public static class Conteudo {
        public String numeroProcesso;
        public int nivelSigilo;
        public int idCodexTribunal;
        public String siglaTribunal;
        public List<Tramitacao> tramitacoes;
    }

    @Getter
    public static class Tramitacao {
        public int idCodex;
        public Tribunal tribunal;
        public boolean liminar;
        public int nivelSigilo;
        public double valorAcao;
        public String dataHoraUltimaDistribuicao;
        public List<Classe> classe;
        public List<Assunto> assunto;
        public List<Parte> partes;
        public boolean ativo;
        public OrgaoJulgador orgaoJulgador;
        public int idFonteDadosCodex;
        public boolean permitePeticionar;
    }

    @Getter
    public static class Tribunal {
        public String sigla;
        public String nome;
        public String segmento;
        public String jtr;
    }

    @Getter
    public static class Classe {
        public int codigo;
        public String descricao;
    }

    @Getter
    public static class Assunto {
        public int codigo;
        public String descricao;
        public String hierarquia;
    }

    @Getter
    public static class Parte {
        public String polo;
        public String tipoParte;
        public String nome;
        public List<OutroNome> outrosNomes;
        public String tipoPessoa;
        public List<Documento> documentosPrincipais;
        public boolean sigilosa;
    }

    @Getter
    public static class OutroNome {
        public String nome;
        public String tipo;
    }

    @Getter
    public static class Documento {
        public String numero;
        public String tipo;
    }

    @Getter
    public static class OrgaoJulgador {
        public int id;
        public String nome;
    }
}
