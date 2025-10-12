package controller;

import model.ResultadoAnalise;
import service.VerificacaoService;
import model.AnaliseUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
public class VerificacaoController {

    private final VerificacaoService verificacaoService;

    @Autowired
    public VerificacaoController(VerificacaoService verificacaoService) {
        this.verificacaoService = verificacaoService;
    }

    @PostMapping("/verificar")
    public ResponseEntity<ResultadoAnalise> verificarMensagem(@RequestBody AnaliseUrl request) {
        ResultadoAnalise resultado = verificacaoService.analisar(request);
        return ResponseEntity.ok(resultado);
    }
}
