package ia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Validação de evidências (fotos, vídeos, documentos) por IA de visão, via Groq.
 *
 * IMPORTANTE — leia antes de usar:
 * - O modelo usado é qwen/qwen3.8-27b (modelo de visão atual da Groq — os
 *   modelos Llama de visão foram descontinuados tanto na Groq quanto pela
 *   própria Meta, que encerrou sua API oficial em 06/07/2026).
 * - A chave da API NUNCA deve ficar hardcoded aqui, nem indo pro Git.
 *   É lida da variável de ambiente GROQ_API_KEY (configure no seu sistema
 *   ou nas variáveis de execução da IDE — Run > Edit Configurations).
 * - Para validar VIDEO, é necessário ter o FFmpeg instalado e disponível
 *   no PATH do sistema (não é biblioteca Java, é uma ferramenta externa).
 *   Baixe em https://ffmpeg.org/download.html se ainda não tiver.
 */
public class ValidacaoEvidenciaIA {

    private static final String URL_API = "https://api.groq.com/openai/v1/chat/completions";
    // Modelo de visão da Groq. Pode ser trocado SEM recompilar definindo a
    // variável de ambiente GROQ_MODELO (a Groq troca/descontinua modelos).
    private static final String MODELO_PADRAO = "qwen/qwen3.8-27b";
    private static final String MODELO = lerEnv("GROQ_MODELO", MODELO_PADRAO);
    private static final int TENTATIVAS_MAXIMAS = 3;
    private static final List<String> TIPOS_VALIDOS = List.of("FOTO", "DOCUMENTO", "QR_CODE", "VIDEO");
    private static final HttpClient CLIENTE_HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private ValidacaoEvidenciaIA() {
    }

    private static String lerEnv(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor.trim();
    }

    /**
     * Valida uma evidência usando IA de visão. Suporta FOTO, DOCUMENTO,
     * QR_CODE (analisados diretamente) e VIDEO (extrai um frame do meio
     * do vídeo via FFmpeg e analisa esse frame).
     *
     * @param caminhoArquivo caminho local do arquivo (imagem ou vídeo)
     * @param tipoEvidencia  um de: FOTO, DOCUMENTO, QR_CODE, VIDEO (bate com TB_EVIDENCIA)
     * @param nomeAcao       nome da ação sustentável que a evidência deveria comprovar
     * @return resultado estruturado com veredito, confiança e motivo
     */
    public static ResultadoValidacaoIA validar(String caminhoArquivo, String tipoEvidencia, String nomeAcao) {
        String tipoNormalizado = tipoEvidencia == null ? "" : tipoEvidencia.toUpperCase(Locale.ROOT);

        if (!TIPOS_VALIDOS.contains(tipoNormalizado)) {
            return new ResultadoValidacaoIA(
                    false, 0.0,
                    "Tipo de evidencia invalido: " + tipoEvidencia + ". Use um de: " + TIPOS_VALIDOS
            );
        }

        String chaveApi = System.getenv("GROQ_API_KEY");
        if (chaveApi == null || chaveApi.isBlank()) {
            return ResultadoValidacaoIA.indisponivel(
                    "GROQ_API_KEY não configurada no servidor. Defina a variável de ambiente antes de rodar."
            );
        }

        String caminhoImagemParaAnalise = caminhoArquivo;
        boolean eraVideo = "VIDEO".equals(tipoNormalizado);

        if (eraVideo) {
            try {
                caminhoImagemParaAnalise = extrairFrameDoVideo(caminhoArquivo);
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return ResultadoValidacaoIA.indisponivel("Erro ao extrair frame do video: " + e.getMessage());
            }
        }

        String imagemBase64;
        try {
            imagemBase64 = codificarImagemParaBase64(caminhoImagemParaAnalise);
        } catch (IOException e) {
            return ResultadoValidacaoIA.indisponivel("Erro ao ler o arquivo de imagem: " + e.getMessage());
        } finally {
            if (eraVideo) {
                apagarArquivoTemporario(caminhoImagemParaAnalise);
            }
        }

        String prompt = montarPrompt(tipoNormalizado, nomeAcao, eraVideo);
        String mime = detectarMime(caminhoImagemParaAnalise);
        boolean comParametrosDeRaciocinio = true;
        String ultimoErro = "Falha desconhecida ao consultar a IA.";

        for (int tentativa = 1; tentativa <= TENTATIVAS_MAXIMAS; tentativa++) {
            String corpoRequisicao = montarCorpoRequisicao(prompt, imagemBase64, mime, comParametrosDeRaciocinio);

            try {
                HttpRequest requisicao = HttpRequest.newBuilder()
                        .uri(URI.create(URL_API))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + chaveApi)
                        .timeout(Duration.ofSeconds(40))
                        .POST(HttpRequest.BodyPublishers.ofString(corpoRequisicao, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> resposta = CLIENTE_HTTP.send(requisicao, HttpResponse.BodyHandlers.ofString());
                int status = resposta.statusCode();

                if (status == 200) {
                    return interpretarResposta(resposta.body());
                }

                ultimoErro = "Erro na API da Groq (HTTP " + status + "): " + extrairMensagemErro(resposta.body());
                System.out.println("[IA] tentativa " + tentativa + " falhou: " + ultimoErro);

                // Modelo que nao aceita os parametros de raciocinio: tenta de novo sem eles.
                if (status == 400 && comParametrosDeRaciocinio
                        && resposta.body() != null && resposta.body().toLowerCase(Locale.ROOT).contains("reasoning")) {
                    comParametrosDeRaciocinio = false;
                    continue;
                }

                // So vale tentar de novo em limite de uso (429) e erro do servidor (5xx).
                if (status != 429 && status < 500) {
                    break;
                }
            } catch (IOException e) {
                ultimoErro = "Falha de comunicação com a API: " + e.getMessage();
                System.out.println("[IA] tentativa " + tentativa + " falhou: " + ultimoErro);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResultadoValidacaoIA.indisponivel("Validação interrompida.");
            }

            if (tentativa < TENTATIVAS_MAXIMAS) {
                try {
                    Thread.sleep(1500L * tentativa);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return ResultadoValidacaoIA.indisponivel("Validação interrompida.");
                }
            }
        }

        return ResultadoValidacaoIA.indisponivel(ultimoErro);
    }

    /** Descobre o tipo da imagem pelos primeiros bytes (e nao pela extensão do arquivo). */
    private static String detectarMime(String caminho) {
        try (java.io.InputStream entrada = Files.newInputStream(Path.of(caminho))) {
            byte[] c = entrada.readNBytes(12);
            if (c.length >= 4 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G') return "image/png";
            if (c.length >= 12 && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
                    && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') return "image/webp";
            if (c.length >= 3 && c[0] == 'G' && c[1] == 'I' && c[2] == 'F') return "image/gif";
        } catch (IOException e) {
            // cai no padrão abaixo
        }
        return "image/jpeg";
    }

    /** Pega só a mensagem curta do erro da Groq ({"error":{"message":"..."}}), sem despejar o corpo todo. */
    private static String extrairMensagemErro(String corpo) {
        try {
            return new JSONObject(corpo).getJSONObject("error").getString("message");
        } catch (Exception e) {
            return corpo == null ? "(sem corpo)" : corpo.substring(0, Math.min(corpo.length(), 200));
        }
    }

    /**
     * Extrai um frame do meio do vídeo usando FFmpeg (processo externo) e
     * salva num arquivo JPG temporário. Usa o timestamp do meio da duração
     * do vídeo em vez do primeiro segundo, para evitar pegar um frame
     * preto/em branco comum no início de gravações.
     */
    private static String extrairFrameDoVideo(String caminhoVideo) throws IOException, InterruptedException {
        if (!Files.exists(Path.of(caminhoVideo))) {
            throw new IOException("Arquivo de video nao encontrado: " + caminhoVideo);
        }

        double duracaoSegundos = obterDuracaoVideo(caminhoVideo);
        double instanteExtracao = duracaoSegundos > 2 ? duracaoSegundos / 2 : 0.5;

        Path arquivoTemporario = Files.createTempFile("frame-evidencia-", ".jpg");

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-y",
                "-ss", String.valueOf(instanteExtracao),
                "-i", caminhoVideo,
                "-frames:v", "1",
                "-q:v", "2",
                arquivoTemporario.toString()
        );
        builder.redirectErrorStream(true);

        Process processo = builder.start();
        boolean terminouATempo = processo.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);

        if (!terminouATempo) {
            processo.destroyForcibly();
            throw new IOException("FFmpeg demorou demais para extrair o frame (timeout de 30s).");
        }

        if (processo.exitValue() != 0 || !Files.exists(arquivoTemporario) || Files.size(arquivoTemporario) == 0) {
            throw new IOException(
                    "FFmpeg nao conseguiu extrair um frame. Verifique se o FFmpeg esta "
                    + "instalado e no PATH (rode 'ffmpeg -version' no terminal para confirmar)."
            );
        }

        return arquivoTemporario.toString();
    }

    /**
     * Usa ffprobe (parte do pacote do FFmpeg) para descobrir a duração do
     * vídeo em segundos. Se falhar por qualquer motivo, assume 1 segundo
     * (mantém o comportamento antigo de pegar perto do início).
     */
    private static double obterDuracaoVideo(String caminhoVideo) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "ffprobe", "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    caminhoVideo
            );
            Process processo = builder.start();
            String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            processo.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            return Double.parseDouble(saida);
        } catch (Exception e) {
            return 1.0;
        }
    }

    private static void apagarArquivoTemporario(String caminho) {
        try {
            Files.deleteIfExists(Path.of(caminho));
        } catch (IOException e) {
            System.out.println("Aviso: nao foi possivel apagar o frame temporario " + caminho + ": " + e.getMessage());
        }
    }

    private static String codificarImagemParaBase64(String caminhoImagem) throws IOException {
        Path caminho = Path.of(caminhoImagem);

        if (!Files.exists(caminho)) {
            throw new IOException(
                    "Arquivo nao encontrado neste caminho exato: " + caminhoImagem + ". "
                    + "Se o arquivo estiver dentro de uma pasta do OneDrive, confirme que ele "
                    + "esta baixado de verdade (clique direito > 'Sempre manter neste dispositivo'), "
                    + "nao so aparecendo como 'disponivel online'."
            );
        }

        if (!Files.isReadable(caminho)) {
            throw new IOException("Arquivo encontrado, mas sem permissao de leitura: " + caminhoImagem);
        }

        byte[] bytesImagem = Files.readAllBytes(caminho);

        if (bytesImagem.length == 0) {
            throw new IOException(
                    "Arquivo encontrado, mas esta vazio (0 bytes): " + caminhoImagem + ". "
                    + "Isso costuma acontecer com arquivo do OneDrive so 'online', nao baixado de verdade."
            );
        }

        return Base64.getEncoder().encodeToString(bytesImagem);
    }

    private static String montarPrompt(String tipoEvidencia, String nomeAcao, boolean eraVideo) {
        String contextoTipo = switch (tipoEvidencia) {
            case "DOCUMENTO" -> "um DOCUMENTO/COMPROVANTE OFICIAL (ex: recibo, certificado, atestado emitido "
                    + "por uma instituição). Verifique se tem características de documento oficial: "
                    + "papel timbrado ou cabeçalho institucional, data, nome de pessoa ou instituição, "
                    + "carimbo, assinatura ou código de autenticação. Textos genéricos, sem identificação "
                    + "de nenhuma instituição, ou capturas de tela de conversas não contam como documento oficial.";
            case "QR_CODE" -> "um QR CODE. Verifique se existe um QR code legível e nitido na imagem.";
            default -> "uma FOTO da ação sendo realizada. Verifique se a cena mostrada é compatível "
                    + "com a ação descrita (local, objetos, contexto visual).";
        };

        String avisoVideo = eraVideo
                ? " (Esta imagem é um frame extraído do meio de um vídeo enviado como evidência — "
                  + "avalie como se fosse a própria cena do vídeo.)"
                : "";

        return "Você é um validador rigoroso de evidências de um aplicativo de gamificação de ações "
                + "sustentáveis (SoulGame). Um usuário enviou esta imagem como comprovação da ação "
                + "\"" + nomeAcao + "\"." + avisoVideo + "\n\n"
                + "O tipo de evidência esperado é " + contextoTipo + "\n\n"
                + "Critérios de decisão:\n"
                + "- valido=true APENAS se a imagem claramente corresponde ao tipo esperado E tem relação "
                + "direta e plausível com a ação \"" + nomeAcao + "\".\n"
                + "- valido=false se a imagem não tiver relação com a ação, for de outro assunto "
                + "completamente diferente, estiver ilegível/cortada demais para avaliar, ou parecer "
                + "genérica/reaproveitada (ex: imagem de banco de dados, print de outra tela sem relação).\n"
                + "- confianca deve refletir o quão certo você está do seu proprio veredito (não da "
                + "qualidade da ação em si): 0.9 a 1.0 para casos claros em qualquer direção, "
                + "0.5 a 0.7 para casos ambíguos onde um humano deveria revisar.\n\n"
                + "Responda APENAS com um objeto JSON, sem nenhum texto antes ou depois, "
                + "no formato exato: "
                + "{\"valido\": true ou false, \"confianca\": numero de 0.0 a 1.0, "
                + "\"motivo\": \"explicação breve e específica em português, citando o que você viu na imagem\"}.";
    }

    private static String montarCorpoRequisicao(String prompt, String imagemBase64, String mime, boolean comParametrosDeRaciocinio) {
        JSONObject conteudoTexto = new JSONObject()
                .put("type", "text")
                .put("text", prompt);

        JSONObject urlImagem = new JSONObject()
                .put("url", "data:" + mime + ";base64," + imagemBase64);

        JSONObject conteudoImagem = new JSONObject()
                .put("type", "image_url")
                .put("image_url", urlImagem);

        JSONObject mensagemUsuario = new JSONObject()
                .put("role", "user")
                .put("content", new JSONArray().put(conteudoTexto).put(conteudoImagem));

        JSONObject corpo = new JSONObject()
                .put("model", MODELO)
                .put("messages", new JSONArray().put(mensagemUsuario))
                .put("temperature", 0.2)
                .put("max_completion_tokens", 1024)
                .put("response_format", new JSONObject().put("type", "json_object"));

        // Qwen 3.8 na Groq: sem isso o "raciocínio" gasta os tokens e/ou a
        // resposta JSON vem misturada com texto de pensamento. Em JSON mode a
        // Groq exige reasoning_format "hidden" ou "parsed" (nunca "raw").
        if (comParametrosDeRaciocinio) {
            corpo.put("reasoning_effort", "none").put("reasoning_format", "hidden");
        }

        return corpo.toString();
    }

    private static ResultadoValidacaoIA interpretarResposta(String corpoResposta) {
        try {
            JSONObject respostaCompleta = new JSONObject(corpoResposta);
            String conteudoTextoResposta = respostaCompleta
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            JSONObject veredito = new JSONObject(extrairJson(conteudoTextoResposta));

            boolean valido = veredito.optBoolean("valido", false);
            double confianca = veredito.optDouble("confianca", 0.0);
            String motivo = veredito.optString("motivo", "Sem motivo informado pela IA.");

            return new ResultadoValidacaoIA(valido, confianca, motivo);
        } catch (Exception e) {
            return ResultadoValidacaoIA.indisponivel(
                    "Não foi possível interpretar a resposta da IA: " + e.getMessage()
            );
        }
    }

    /** Remove cercas de markdown / texto solto e devolve só o trecho {...} da resposta. */
    private static String extrairJson(String texto) {
        String limpo = texto == null ? "" : texto.replaceAll("(?s)<think>.*?</think>", "").trim();
        int abre = limpo.indexOf('{');
        int fecha = limpo.lastIndexOf('}');
        return (abre >= 0 && fecha > abre) ? limpo.substring(abre, fecha + 1) : limpo;
    }
}
