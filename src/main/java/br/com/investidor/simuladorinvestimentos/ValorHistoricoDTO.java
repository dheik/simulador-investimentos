package br.com.investidor.simuladorinvestimentos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValorHistoricoDTO {
    private long date;
    private double close;
}