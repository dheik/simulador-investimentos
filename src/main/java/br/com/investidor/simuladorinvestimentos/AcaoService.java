package br.com.investidor.simuladorinvestimentos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AcaoService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${brapi.api.token}")
    private String apiToken;

    private static final String BRAPI_API_URL = "https://brapi.dev/api/";

    // Os métodos buscarDetalhesAcao e listarAcoesDisponiveis não precisam de debug agora.
    // ... (mantenha os métodos buscarDetalhesAcao e listarAcoesDisponiveis como estão)
    public Optional<AcaoDetalheDTO> buscarDetalhesAcao(String ticker) {
        String url = BRAPI_API_URL + "quote/" + ticker + "?token=" + apiToken;
        try {
            AcaoQuoteResponseDTO response = restTemplate.getForObject(url, AcaoQuoteResponseDTO.class);
            if (response != null && !response.getResults().isEmpty()) {
                return Optional.of(response.getResults().get(0));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar dados da ação " + ticker + ": " + e.getMessage());
        }
        return Optional.empty();
    }
    public Optional<AcoesDisponiveisResponseDTO> listarAcoesDisponiveis() {
        String url = BRAPI_API_URL + "available" + "?token=" + apiToken;
        try {
            return Optional.ofNullable(restTemplate.getForObject(url, AcoesDisponiveisResponseDTO.class));
        } catch (Exception e) {
            System.err.println("Erro ao buscar lista de ações disponíveis: " + e.getMessage());
        }
        return Optional.empty();
    }


    // --- MÉTODO DE SIMULAÇÃO COM LOGS DE DEBUG ---
    public Optional<SimulacaoResponseDTO> simularInvestimento(String ticker, SimulacaoRequestDTO request) {
        System.out.println("\n[DEBUG] -----------------------------------------");
        System.out.println("[DEBUG] Iniciando simulação para o ticker: " + ticker);

        Optional<AcaoDetalheDTO> acaoOptional = this.buscarDetalhesAcao(ticker);
        if (acaoOptional.isEmpty()) {
            System.out.println("[DEBUG] FALHA: Ação não encontrada no endpoint /quote. A simulação será interrompida.");
            return Optional.empty();
        }
        System.out.println("[DEBUG] SUCESSO: Detalhes da ação encontrados: " + acaoOptional.get().getLongName());

        Optional<Double> taxaRendimentoOptional = this.calcularRendimentoMedioMensal(ticker);
        if (taxaRendimentoOptional.isEmpty()) {
            System.out.println("[DEBUG] FALHA: Não foi possível calcular o rendimento histórico. A simulação será interrompida.");
            return Optional.empty();
        }
        double taxaRendimentoMensal = taxaRendimentoOptional.get();
        System.out.println("[DEBUG] SUCESSO: Rendimento médio mensal calculado: " + taxaRendimentoMensal);

        // Se chegamos até aqui, o resto do código deve funcionar.
        // ... (cálculos de imposto e montagem do DTO)
        double valorInvestido = request.getValorInvestido();
        int meses = request.getQuantidadeMeses();
        double valorFinalBruto = valorInvestido * Math.pow(1 + taxaRendimentoMensal, meses);
        double lucroBruto = valorFinalBruto - valorInvestido;
        double impostoDeRenda = 0;
        if (lucroBruto > 0) {
            impostoDeRenda = lucroBruto * 0.15;
        }
        double valorFinalLiquido = valorFinalBruto - impostoDeRenda;
        double rendimentoLiquido = valorFinalLiquido - valorInvestido;

        SimulacaoResponseDTO responseDTO = new SimulacaoResponseDTO();
        responseDTO.setTicker(acaoOptional.get().getSymbol());
        responseDTO.setLongName(acaoOptional.get().getLongName());
        responseDTO.setRendimentoMedioMensalCalculado(taxaRendimentoMensal);
        responseDTO.setValorInvestido(valorInvestido);
        responseDTO.setQuantidadeMeses(meses);
        responseDTO.setValorFinalBruto(valorFinalBruto);
        responseDTO.setImpostoDeRenda(impostoDeRenda);
        responseDTO.setValorFinalLiquido(valorFinalLiquido);
        responseDTO.setRendimentoLiquido(rendimentoLiquido);
        responseDTO.setDataSimulacao(LocalDateTime.now());

        System.out.println("[DEBUG] SUCESSO: Simulação concluída para: " + ticker);
        System.out.println("[DEBUG] -----------------------------------------");
        return Optional.of(responseDTO);
    }

    private Optional<Double> calcularRendimentoMedioMensal(String ticker) {
        String url = BRAPI_API_URL + "quote/" + ticker + "?range=3mo&interval=1d&token=" + apiToken;
        System.out.println("[DEBUG] Buscando histórico na URL: " + url);

        try {
            AcaoQuoteResponseDTO response = restTemplate.getForObject(url, AcaoQuoteResponseDTO.class);

            if (response == null || response.getResults().isEmpty() ||
                    response.getResults().get(0).getHistoricalDataPrice() == null ||
                    response.getResults().get(0).getHistoricalDataPrice().size() < 2) {

                System.out.println("[DEBUG] FALHA: Resposta do histórico inválida ou com dados insuficientes.");
                return Optional.empty();
            }

            List<ValorHistoricoDTO> historico = response.getResults().get(0).getHistoricalDataPrice();

            double valorInicial = historico.get(0).getClose();
            double valorFinal = historico.get(historico.size() - 1).getClose();
            System.out.println("[DEBUG] Histórico encontrado. Valor inicial: " + valorInicial + ", Valor final: " + valorFinal);

            if (valorInicial <= 0) {
                System.out.println("[DEBUG] FALHA: Valor inicial do histórico é zero ou negativo.");
                return Optional.empty();
            }

            double rendimentoTotal = valorFinal / valorInicial;
            double rendimentoMedioMensal = Math.pow(rendimentoTotal, 1.0 / 3.0) - 1;
            return Optional.of(rendimentoMedioMensal);

        } catch (Exception e) {
            System.err.println("[DEBUG] ERRO CRÍTICO ao calcular rendimento histórico para " + ticker + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}