package br.com.investidor.simuladorinvestimentos;

import lombok.Data;

@Data
public class SimulacaoRequestDTO {
    private double valorInvestido;
    private  int quantidadeMeses;
}
