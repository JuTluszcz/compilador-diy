package recovery;

import source.DungeonCompiladorConstants;   // gerado pelo JavaCC

/**
 * Conjuntos FOLLOW usados na estratégia de recuperação por pânico.
 * Cada RecoverySet contém os tokens (kind) que podem aparecer
 * imediatamente após o não-terminal correspondente.
 */
public class Follow {

    /* ­­­­—  Não-terminais de topo  —­ */
    public static final RecoverySet main        = new RecoverySet();        // programa
    public static final RecoverySet bloco       = new RecoverySet();        // bloco
    public static final RecoverySet declaracao  = new RecoverySet();        // declaracao
    public static final RecoverySet comando     = new RecoverySet();        // comando

    /* —  Não-terminais que só encadeiam  — */
    public static final RecoverySet atribuicao  = comando;
    public static final RecoverySet condicional = comando;
    public static final RecoverySet lacoWhile   = comando;
    public static final RecoverySet lacoDoUntil = comando;

    /* —  Expressões em cascata  — */
    public static final RecoverySet expressao   = new RecoverySet();
    public static final RecoverySet expAND      = expressao;
    public static final RecoverySet expRel      = expressao;
    public static final RecoverySet expAditiva  = expressao;
    public static final RecoverySet expMult     = expressao;
    public static final RecoverySet fator       = expressao;

    /* —  Pontual, raramente útil mas mantido por clareza  — */
    public static final RecoverySet tipo        = new RecoverySet();

    /* ------------------------------------------------------ */
    static {

        /* FOLLOW(programa)  —­ raiz */
        main.add(DungeonCompiladorConstants.EOF);

        /* FOLLOW(bloco)  —­ aparece em 4 contextos */
        bloco.add(DungeonCompiladorConstants.FECHABLOCO);   // “e_eh_aqui”
        bloco.add(DungeonCompiladorConstants.ELSE);         // IF ... ELSE
        bloco.add(DungeonCompiladorConstants.END);          // IF/WHILE ... END
        bloco.add(DungeonCompiladorConstants.UNTIL);        // DO ... UNTIL

        /* FOLLOW(declaracao) — fecha ou inicia nova sentença */
        declaracao.union(bloco);                            // tudo que encerra bloco
        declaracao.add(DungeonCompiladorConstants.TIPOINT); // nova declaração
        declaracao.add(DungeonCompiladorConstants.TIPOFLOAT);
        declaracao.add(DungeonCompiladorConstants.TIPOBOL);
        declaracao.add(DungeonCompiladorConstants.TIPOCHAR);
        declaracao.add(DungeonCompiladorConstants.IDENT);   // começa um comando
        declaracao.add(DungeonCompiladorConstants.IF);
        declaracao.add(DungeonCompiladorConstants.WHILE);
        declaracao.add(DungeonCompiladorConstants.DO);

        /* FOLLOW(comando) — mesmo conjunto que declaracao */
        comando.union(declaracao);

        /* FOLLOW(expressao) — tokens que surgem logo após uma expressão de topo */
        expressao.add(DungeonCompiladorConstants.FIMINST);  // “fim_de_turno”
        expressao.add(DungeonCompiladorConstants.THEN);     // IF expr THEN
        expressao.add(DungeonCompiladorConstants.DO);       // WHILE expr DO
        expressao.add(DungeonCompiladorConstants.UNTIL);    // ... UNTIL expr
        expressao.add(DungeonCompiladorConstants.FECHABLOCO); // segurança extra

        /* FOLLOW(tipo) —  sempre seguido por IDENT em declaracao */
        tipo.add(DungeonCompiladorConstants.IDENT);
    }
}