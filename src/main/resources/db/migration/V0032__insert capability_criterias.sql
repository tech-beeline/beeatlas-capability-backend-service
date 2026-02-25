INSERT INTO capability.enum_criterias (id, name, type, interval, description, revers, max_desc, min_desc)
VALUES (102,
        'Доступность ТС',
        'binary',
        2,
        'Доступность ТС',
        true,
        'Не доступен',
        'Доступен');

INSERT INTO capability.enum_criterias (id, name, type, interval, description, revers, max_desc, min_desc)
VALUES (103,
        'Кол-во недоступных ТС',
        'дискретный',
        2,
        'Кол-во недоступных ТС',
        true,
        'Есть недоступные ТС',
        'Нет недоступных ТС');