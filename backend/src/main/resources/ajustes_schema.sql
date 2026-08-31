-- O diagrama recebido define estes dois campos com tamanhos/tipos incompatíveis
-- com os dados usados pelo aplicativo. Execute uma vez se o seu schema ainda
-- estiver exatamente como no diagrama e as tabelas estiverem vazias.
ALTER TABLE ADA_MEDICAMENTOS MODIFY (nm_medicamento VARCHAR2(150));
ALTER TABLE ADA_AGENDA MODIFY (horarioLocal VARCHAR2(5));
