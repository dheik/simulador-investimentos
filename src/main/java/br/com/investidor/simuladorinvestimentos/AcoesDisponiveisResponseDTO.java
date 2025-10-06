package br.com.investidor.simuladorinvestimentos;

import lombok.Data;
import java.util.List;

@Data
public class AcoesDisponiveisResponseDTO {
    private List<String> stocks;
}
