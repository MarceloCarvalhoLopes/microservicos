package io.github.cursodsousa.msavaliadorcredito.application;

import io.github.cursodsousa.msavaliadorcredito.application.ex.DadosClientesNotFoundException;
import io.github.cursodsousa.msavaliadorcredito.application.ex.ErroComunicacaoMicroservicesException;
import io.github.cursodsousa.msavaliadorcredito.application.ex.ErroSolicitacaoCartaoException;
import io.github.cursodsousa.msavaliadorcredito.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("avaliacoes-credito")
@RequiredArgsConstructor
public class AvaliadorCreditoController {

    private final AvaliadorCreditoService avaliadorCreditoService;

    @GetMapping
    public String status(){
        return "OK";
    }

    @GetMapping(value = "situacao-cliente", params = "cpf")
    public ResponseEntity consultaSituacaoCliente(@RequestParam("cpf")String cpf){

        try {
            SituacaoCliente situacaoCliente =  situacaoCliente = avaliadorCreditoService.obterSituacaoCliente(cpf);
            return ResponseEntity.ok(situacaoCliente);
        } catch (DadosClientesNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ErroComunicacaoMicroservicesException e) {
           return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());
        }

    }

    @PostMapping
    public ResponseEntity realizarAvaliacao(@RequestBody CartaoAprovado.DadosAvaliacao dados) {
        try {
            RetornoAvaliacaoCliente retornoAvaliacaoCliente = avaliadorCreditoService
                    .realizarAvaliacao(dados.getCpf(), dados.getRenda());
            return ResponseEntity.ok(retornoAvaliacaoCliente);
        } catch (DadosClientesNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ErroComunicacaoMicroservicesException e) {
            return ResponseEntity.status(HttpStatus.resolve(e.getStatus())).body(e.getMessage());

        }
    }


    @PostMapping("solicitacoes-cartao")
    public ResponseEntity solicitarCartao(@RequestBody DadosSolicitacaoEmissaoCartao dados){
        try{
            ProtocoloSolicitacaoCartao protocoloSolicitacaoCartao = avaliadorCreditoService
                    .solicitarEmissaoCartao(dados);
            return ResponseEntity.ok(protocoloSolicitacaoCartao);
        }catch (ErroSolicitacaoCartaoException e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


}
