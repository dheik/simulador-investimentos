package br.com.investidor.simuladorinvestimentos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcaoDetalheDTO {

    private String symbol;
    private String longName;
    private double regularMarketPrice;
    private double regularMarketChangePercent;
    private long marketCap;
    private String logourl;
    private List<ValorHistoricoDTO> historicalDataPrice;
}