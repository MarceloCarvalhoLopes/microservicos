package io.github.cursodsousa.mscartoes.application.representation;

import io.github.cursodsousa.mscartoes.domain.BandeiraCartao;
import io.github.cursodsousa.mscartoes.domain.Cartao;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartaoSaveRequest {

    private String nome;
    private BandeiraCartao bandeira;
    private BigDecimal renda;
    private BigDecimal limiteBasico;

    public Cartao toModel(){
        return  new Cartao(nome, bandeira, renda, limiteBasico);
    }
}
