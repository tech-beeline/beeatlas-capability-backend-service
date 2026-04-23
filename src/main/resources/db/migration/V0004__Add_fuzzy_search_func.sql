CREATE EXTENSION pg_trgm;
CREATE INDEX trgm_vector_idx ON capability.find_name_sort_table USING gist (vector gist_trgm_ops);
CREATE OR REPLACE FUNCTION capability.fuzzy_search_capability(search_string text) RETURNS TABLE(id int, vector text, entity_name varchar) AS $$
BEGIN
RETURN QUERY SELECT capability.find_name_sort_table.id_ref, capability.find_name_sort_table.vector, capability.entity_type.name
                 FROM capability.find_name_sort_table
                          JOIN capability.entity_type ON capability.find_name_sort_table.type_id = capability.entity_type.id
                 WHERE public.word_similarity(search_string, capability.find_name_sort_table.vector) > 0.5
                 LIMIT 50;
END;
$$ LANGUAGE plpgsql;