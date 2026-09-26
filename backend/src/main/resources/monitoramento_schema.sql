-- Execute no Oracle antes de usar a nova versão do backend.
-- Preserva os resumos existentes e pode ser executado novamente.
DECLARE
    quantidade NUMBER;
BEGIN
    SELECT COUNT(*) INTO quantidade FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'ADA_RESUMO' AND COLUMN_NAME = 'PRESSAOSISTOLICA';
    IF quantidade = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADA_RESUMO ADD (pressaoSistolica NUMBER(6,2))';
    END IF;
    SELECT COUNT(*) INTO quantidade FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'ADA_RESUMO' AND COLUMN_NAME = 'PRESSAODIASTOLICA';
    IF quantidade = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADA_RESUMO ADD (pressaoDiastolica NUMBER(6,2))';
    END IF;
    FOR coluna IN (
        SELECT COLUMN_NAME FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'ADA_RESUMO' AND COLUMN_NAME IN ('PASSOS', 'SONOMIN', 'BATIMENTOSULTIMOS') AND NULLABLE = 'N'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE ADA_RESUMO MODIFY (' || coluna.COLUMN_NAME || ' NULL)';
    END LOOP;
END;
/
