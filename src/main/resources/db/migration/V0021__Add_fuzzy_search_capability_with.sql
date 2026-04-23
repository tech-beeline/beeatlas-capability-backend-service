CREATE OR REPLACE FUNCTION capability.fuzzy_search_capability_with(
    search_string text,
    type_string text DEFAULT NULL
)
RETURNS TABLE(id int, vector text, entity_name varchar) AS $$
BEGIN
RETURN QUERY
SELECT
    capability.find_name_sort_table.id_ref,
    capability.find_name_sort_table.vector,
    capability.entity_type.name
FROM
    capability.find_name_sort_table
        JOIN
    capability.entity_type
    ON
            capability.find_name_sort_table.type_id = capability.entity_type.id
WHERE
        capability.word_similarity(search_string, capability.find_name_sort_table.vector) > 0.8
  AND (
        type_string IS NULL
        OR type_string ILIKE 'ALL'
            OR capability.entity_type.name = type_string
    )
ORDER BY
    capability.word_similarity(search_string, capability.find_name_sort_table.vector) DESC
    LIMIT 100;
END;
$$ LANGUAGE plpgsql;
