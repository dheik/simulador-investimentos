package br.com.investidor.simuladorinvestimentos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investimentos")
public class InvestimentoController {

    @Autowired
    private AcaoService acaoService;

    @GetMapping("/{ticker}")
    public ResponseEntity<AcaoDetalheDTO> getDetalhesAcao(@PathVariable String ticker) {
        return acaoService.buscarDetalhesAcao(ticker)
                .map(ResponseEntity::ok) // Se encontrou, retorna 200 OK com o DTO
                .orElse(ResponseEntity.notFound().build()); // Se não, retorna 404 Not Found
    }

    @GetMapping("/acoes-disponiveis")
    public ResponseEntity<AcoesDisponiveisResponseDTO> getAcoesDisponiveis() {
        return acaoService.listarAcoesDisponiveis()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.internalServerError().build()); // Retorna erro 500 se a API falhar
    }

    @PostMapping("/{ticker}/simular")
    public ResponseEntity<SimulacaoResponseDTO> postSimularInvestimento(
            @PathVariable String ticker,
            @RequestBody SimulacaoRequestDTO request) {

        return acaoService.simularInvestimento(ticker, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        }
}