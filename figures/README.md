# Capturas de ecrã para o relatório

O capítulo 9 compara **protótipo Figma** (esquerda) com **implementação real** (direita). Coloca os pares de imagens nesta pasta:

| Protótipo Figma | App real | Conteúdo |
|-----------------|----------|----------|
| `figma_intro.png` | `ecra_intro.png` | Ecrã de onboarding / intro sliders |
| `figma_login.png` | `ecra_login.png` | Ecrã de início de sessão |
| `figma_dashboard.png` | `ecra_dashboard.png` | Dashboard principal |
| `figma_projetos.png` | `ecra_projetos.png` | Lista de projetos |
| `figma_tarefa.png` | `ecra_tarefa.png` | Detalhe de tarefa com registo de tempo |
| `figma_admin.png` | `ecra_admin.png` | Administração de utilizadores |
| `figma_estatisticas.png` | `ecra_estatisticas.png` | Exportação de estatísticas |

## Como obter os prints

1. **Figma:** abrir o protótipo, seleccionar cada ecrã e exportar como PNG (ou fazer screenshot).
2. **App:** executar no emulador Android Studio e capturar o ecrã correspondente (`Device Manager` → ícone de câmara, ou `Ctrl+S` / `Cmd+S` no emulador).

## Compilar o PDF

```bash
cd /caminho/para/COMMOV
pdflatex RelatorioFinalCOMMOV.tex
pdflatex RelatorioFinalCOMMOV.tex
```

Repete `pdflatex` duas vezes para gerar o índice correctamente.

## Antes de entregar

1. Substitui os links do Figma e Trello em `RelatorioFinalCOMMOV.tex`.
2. Coloca os 14 ficheiros PNG (7 pares) nesta pasta.
3. Recompila o PDF.
