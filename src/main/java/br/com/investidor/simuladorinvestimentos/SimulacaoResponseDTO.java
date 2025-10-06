package br.com.investidor.simuladorinvestimentos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SimulacaoResponseDTO {
    private String ticker;
    private String longName;
    private double rendimentoMedioMensalCalculado;
    private double valorInvestido;
    private int quantidadeMeses;
    private double valorFinalBruto;
    private double impostoDeRenda;
    private double valorFinalLiquido;
    private double rendimentoLiquido;
    private LocalDateTime dataSimulacao;
}