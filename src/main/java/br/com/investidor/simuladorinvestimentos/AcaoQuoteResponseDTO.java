package br.com.investidor.simuladorinvestimentos;

import lombok.Data;
import java.util.List;

@Data
public class AcaoQuoteResponseDTO {
    private List<AcaoDetalheDTO> results;
}
