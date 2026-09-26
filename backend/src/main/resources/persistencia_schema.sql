-- Tratamentos continuos nao possuem data de termino.
DECLARE
    quantidade NUMBER;
BEGIN
    SELECT COUNT(*) INTO quantidade FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'ADA_MEDICAMENTOS' AND COLUMN_NAME = 'FIM' AND NULLABLE = 'N';
    IF quantidade > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADA_MEDICAMENTOS MODIFY (fim NULL)';
    END IF;
END;
/
-- Corrige o nome da coluna presente no schema original de exames.
DECLARE
    antiga NUMBER;
    atual NUMBER;
BEGIN
    SELECT COUNT(*) INTO antiga FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'ADA_EXAMES' AND COLUMN_NAME = 'TIPOMINE';
    SELECT COUNT(*) INTO atual FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'ADA_EXAMES' AND COLUMN_NAME = 'TIPOMIME';
    IF antiga = 1 AND atual = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADA_EXAMES RENAME COLUMN tipoMine TO tipoMime';
    END IF;
END;
/
