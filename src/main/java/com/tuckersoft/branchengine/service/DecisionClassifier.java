package com.tuckersoft.branchengine.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class DecisionClassifier {

    public ClassificationResult classify(String rawInput) {

        String texto = normalize(rawInput);

        String branchType;

        if (!texto.matches(".*[a-z].*")) {

            branchType = "ENTRADA_CORRUPTA";

        } else if (
                texto.contains("rechaza") ||
                        texto.contains("destruye") ||
                        texto.contains("desobedece") ||
                        texto.contains("renuncia")
        ) {

            branchType = "REBELDIA";

        } else if (
                texto.contains("vigilan") ||
                        texto.contains("simbolo") ||
                        texto.contains("conspiracion")
        ) {

            branchType = "SOSPECHA";

        } else if (
                texto.contains("netflix") ||
                        texto.contains("camara") ||
                        texto.contains("espectador") ||
                        texto.contains("videojuego")
        ) {

            branchType = "RUPTURA_CUARTA_PARED";

        } else {

            branchType = "OBEDIENCIA";
        }

        String handlerUnit = getHandlerUnit(branchType);
        String outcomeCode = getOutcomeCode(branchType);

        return new ClassificationResult(
                branchType,
                handlerUnit,
                outcomeCode
        );
    }


    private String normalize(String rawInput) {

        return Normalizer
                .normalize(rawInput, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }


    private String getHandlerUnit(String branchType) {

        return switch (branchType) {

            case "OBEDIENCIA" ->
                    "Mesa de Guión";

            case "REBELDIA" ->
                    "Control de Continuidad";

            case "SOSPECHA" ->
                    "Oficina de Seguridad";

            case "RUPTURA_CUARTA_PARED" ->
                    "Departamento Netflix";

            case "ENTRADA_CORRUPTA" ->
                    "Archivo de Errores";

            default ->
                    throw new IllegalArgumentException(
                            "Branch type desconocido: " + branchType
                    );
        };
    }


    private String getOutcomeCode(String branchType) {

        return switch (branchType) {

            case "OBEDIENCIA" ->
                    "ADVANCE_MAIN_PATH";

            case "REBELDIA" ->
                    "FORK_TIMELINE";

            case "SOSPECHA" ->
                    "INJECT_WHITE_BEAR_SYMBOL";

            case "RUPTURA_CUARTA_PARED" ->
                    "BREAK_FOURTH_WALL";

            case "ENTRADA_CORRUPTA" ->
                    "DISCARD_INPUT";

            default ->
                    throw new IllegalArgumentException(
                            "Branch type desconocido: " + branchType
                    );
        };
    }


    public record ClassificationResult(
            String branchType,
            String handlerUnit,
            String outcomeCode
    ) {
    }
}