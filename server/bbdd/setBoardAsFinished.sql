CREATE OR REPLACE FUNCTION setBoardAsFinished(roundid integer) RETURNS integer AS
$$
DECLARE
    rows integer;
BEGIN

    UPDATE rounds AS r
      SET estado = 'F' -- Se marca partida finalizada
    WHERE r.roundid = $1;
    
    GET DIAGNOSTICS rows = ROW_COUNT;

    RETURN rows;

END;
$$ LANGUAGE 'plpgsql';

