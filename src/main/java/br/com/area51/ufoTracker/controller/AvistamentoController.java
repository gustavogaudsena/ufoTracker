package br.com.area51.ufoTracker.controller;

import br.com.area51.ufoTracker.model.Avistamento;
import br.com.area51.ufoTracker.model.AvistamentoFiltro;
import br.com.area51.ufoTracker.repository.AvistamentoRepository;
import com.oracle.svm.core.annotate.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/avistamento")
@RequiredArgsConstructor@Getter
public class AvistamentoController {
    private final AvistamentoRepository avistamentoRepository;

    //GET /avistamento -LISTAR TODOS
    @GetMapping
    public List<Avistamento> listarAvistamentos() {
        return avistamentoRepository.listarTodos();
    }
    @GetMapping
    public List<Avistamento> listarComFiltro(@ModelAttribute AvistamentoFiltro f){
        if(f.isVazio()) return avistamentoRepository.listarTodos();
        Predicate<Avistamento> filtros = a -> true;

        if(f.localizacao() != null && !f.localizacao().isBlank()){
            //Adicionar essa busca
            filtros = filtros.and(a -> a.getLocalizacao().equalsIgnoreCase(f.localizacao()));

        }
        if(f.confiabilidadeMin() != null){
            filtros = filtros.and(a -> a.getNivelConfiabilidade() >= f.confiabilidadeMin());
        }
        if(f.de() != null){
            filtros = filtros.and(a -> !a.getDataHora().isBefore(f.de()));
        }
        if(f.ate() != null){
            filtros = filtros.and(a -> !a.getDataHora().isAfter(f.ate()));
        }
        return avistamentoRepository.listarTodos().stream()
                .filter(filtros).toList();
    }







    //GET /avistamento/id /12
    @GetMapping("/{id}")
    public ResponseEntity<Avistamento> buscarPorId(@PathVariable Long id) {
        return avistamentoRepository.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/aleatorio")
    public ResponseEntity<Avistamento> buscarAleatorio() {
        List<Avistamento> lista = avistamentoRepository.listarTodos();
        if (lista.isEmpty()) return ResponseEntity.noContent().build();

        Avistamento escolhido = lista.get(new Random().nextInt(lista.size()));
        return ResponseEntity.ok(escolhido);
    }

    // 📍 GET /avistamentos/buscar?localizacao=Roswell
    @GetMapping("/buscar")
    public List<Avistamento> buscarPorLocal(@RequestParam String localizacao) {
        return avistamentoRepository.listarTodos().stream()
                .filter(a -> a.getLocalizacao().toLowerCase().contains(localizacao.toLowerCase()))
                .collect(Collectors.toList());
    }

    // 🔎 GET /avistamentos?confiabilidadeMin=7
    @GetMapping(params = "confiabilidadeMin")
    public List<Avistamento> filtrarPorConfiabilidade(@RequestParam int confiabilidadeMin) {
        return avistamentoRepository.listarTodos().stream()
                .filter(a -> a.getNivelConfiabilidade() >= confiabilidadeMin)
                .collect(Collectors.toList());
    }
}
