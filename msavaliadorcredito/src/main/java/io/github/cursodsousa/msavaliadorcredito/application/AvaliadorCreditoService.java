package io.github.cursodsousa.msavaliadorcredito.application;

import feign.FeignException;
import io.github.cursodsousa.msavaliadorcredito.application.ex.DadosClientesNotFoundException;
import io.github.cursodsousa.msavaliadorcredito.application.ex.ErroComunicacaoMicroservicesException;
import io.github.cursodsousa.msavaliadorcredito.application.ex.ErroSolicitacaoCartaoException;
import io.github.cursodsousa.msavaliadorcredito.domain.model.*;
import io.github.cursodsousa.msavaliadorcredito.infra.clients.ClientResourceClient;
import io.github.cursodsousa.msavaliadorcredito.infra.clients.CartoesResourceClient;
import io.github.cursodsousa.msavaliadorcredito.infra.clients.mqueue.SolicitacaoEmissaoCartaoPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvaliadorCreditoService {

    public static final int FATOR = 10;
    private final ClientResourceClient clientesClient;
    private final CartoesResourceClient cartoesClient;
    private final SolicitacaoEmissaoCartaoPublisher emissaoCartaoPublisher;

    public SituacaoCliente obterSituacaoCliente(String cpf) throws DadosClientesNotFoundException,
            ErroComunicacaoMicroservicesException {

        try{
            ResponseEntity<DadosCliente> dadosClienteResponseEntity = clientesClient.dadosCliente(cpf);
            ResponseEntity<List<CartaoCliente>> cartoesResponse = cartoesClient.getCartoesByCliente(cpf);

            return SituacaoCliente
                    .builder()
                    .cliente(dadosClienteResponseEntity.getBody())
                    .cartoes(cartoesResponse.getBody())
                    .build();
        }catch (FeignException.FeignClientException e){
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClientesNotFoundException();
            }
            throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }
    }

    public RetornoAvaliacaoCliente realizarAvaliacao(String cpf, Long renda)
            throws DadosClientesNotFoundException, ErroComunicacaoMicroservicesException {

        try {
            ResponseEntity<DadosCliente> dadosClienteResponseEntity = clientesClient.dadosCliente(cpf);
            ResponseEntity<List<Cartao>> cartoesResponse = cartoesClient.getCartoesRendaAte(renda);

            List<Cartao> cartoes = cartoesResponse.getBody();
            var listaCartoesAprovado = cartoes.stream().map(cartao -> {

                DadosCliente dadosCliente = dadosClienteResponseEntity.getBody();

                BigDecimal limiteBasico = cartao.getLimiteBasico();
                BigDecimal idadeBD = BigDecimal.valueOf(dadosCliente.getIdade());
                var fator = idadeBD.divide(BigDecimal.valueOf(FATOR));
                BigDecimal limiteAprovado = fator.multiply(limiteBasico);

                CartaoAprovado aprovado = new CartaoAprovado();
                aprovado.setCartao(cartao.getNome());
                aprovado.setBandeira(cartao.getBandeira());
                aprovado.setLimiteAprovado(limiteAprovado);

                return  aprovado;
            }).collect(Collectors.toList());

            return new  RetornoAvaliacaoCliente(listaCartoesAprovado);

        }catch (FeignException.FeignClientException e){
            int status = e.status();
            if (HttpStatus.NOT_FOUND.value() == status){
                throw new DadosClientesNotFoundException();
            }
            throw new ErroComunicacaoMicroservicesException(e.getMessage(), status);
        }
    }

    public ProtocoloSolicitacaoCartao solicitarEmissaoCartao(DadosSolicitacaoEmissaoCartao dados) {

        try {
            emissaoCartaoPublisher.solicitarCartao(dados);
            var protocolo = UUID.randomUUID().toString();
            return new ProtocoloSolicitacaoCartao(protocolo);
        }catch (Exception e){
            throw new ErroSolicitacaoCartaoException(e.getMessage());
        }

    }
}
