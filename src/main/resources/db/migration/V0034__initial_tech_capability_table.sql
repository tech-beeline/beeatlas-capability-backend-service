DO $$
DECLARE
rec RECORD;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM capability.tech_capability) THEN
    INSERT INTO capability.tech_capability (
      id, code, name, description, owner, created_date, last_modified_date, deleted_date,
      status, author, link, responsibility_product_id, source
    ) VALUES
      ( 1,  'TC.00001', 'Платформа каталогов возможностей', 'Хранение, версионирование и публикация бизнес/тех возможностей и связей.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 2,  'TC.00002', 'ГИС платформа', 'Геопространственный анализ, карты/слои, интеграция с источниками геоданных.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 3,  'TC.00003', 'Платформа геолого-геофизических данных', 'Хранилище геол./геофиз. данных, метаданные, контроль качества, доступ и аудит.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 4,  'TC.00004', 'Платформа сейсмической обработки', 'Загрузка, обработка и интерпретация сейсмических данных, расчёт атрибутов.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 5,  'TC.00005', 'Платформа дистанционного зондирования', 'Обработка спутниковых/аэро данных, ортотрансформирование, классификация, аналитика.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 6,  'TC.00006', 'Петрофизическое моделирование', 'Инструменты петрофизики и обработки каротажа; расчёт свойств пласта.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 7,  'TC.00007', 'Управление бурением', 'Планирование бурения, контроль исполнения, отчётность и инциденты по скважинам.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 8,  'TC.00008', 'SCADA и телеметрия', 'Сбор телеметрии, мониторинг датчиков, тревоги, телемеханика и архив данных.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 9,  'TC.00009', 'Оптимизация добычи и предиктивная аналитика', 'Модели прогнозирования, оптимизация режимов, диагностика и предупреждение отказов.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration'),
      ( 10, 'TC.00010', 'Управление контрактами (CLM)', 'Согласование, версии, обязательства, сроки, риски и хранение договоров.', '', to_timestamp('09-12-2025 00:00:00','DD-MM-YYYY HH24:MI:SS'), NULL, NULL, 'Proposed', 'migration', NULL, NULL, 'migration');

INSERT INTO capability.tech_capability_relations (id_rel, id_parent, id_child) VALUES
                                                                                   ( 1,  4,   1),  -- Каталог возможностей
                                                                                   ( 2,  11,  2),  -- Геопространственный анализ данных
                                                                                   ( 3,  7,   3),  -- Геол./геофиз. идентификация и отбор
                                                                                   ( 4,  9,   4),  -- Перспективные месторождения
                                                                                   ( 5,  12,  5),  -- Дистанционный анализ
                                                                                   ( 6,  13,  6),  -- Петрофизический анализ
                                                                                   ( 7,  55,  7),  -- Программа бурения
                                                                                   ( 8,  53,  8),  -- Мониторинг датчиков и манометров
                                                                                   ( 9,  52,  9),  -- Оптимизация добычи на скважинах
                                                                                   (10,  27, 10);  -- Согласование контрактов

FOR rec IN
SELECT id, code, name, description
FROM capability.tech_capability
WHERE id BETWEEN 1 AND 10
    LOOP
INSERT INTO capability.find_name_sort_table (id, vector, type_id, id_ref)
VALUES (
    nextval('capability.find_name_sort_table_id_seq'),
    rec.name || '<!!!>' || COALESCE(rec.description, '') || '<!!!>' || COALESCE(rec.code, ''),
    1,
    rec.id
    );
END LOOP;
END IF;
END $$;

