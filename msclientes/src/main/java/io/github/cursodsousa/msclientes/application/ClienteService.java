package io.github.cursodsousa.msclientes.application;

import io.github.cursodsousa.msclientes.domain.Cliente;
import io.github.cursodsousa.msclientes.infra.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {


    private final ClienteRepository clienteRepository;

    @Transactional
     public Cliente save(Cliente cliente) {
         return clienteRepository.save(cliente);
     }

     public Optional<Cliente> getByCpf(String cpf){
        return clienteRepository.findByCpf(cpf);
     }
}
