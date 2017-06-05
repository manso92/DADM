CREATE OR REPLACE FUNCTION getNextMoveNumber(roundid integer) RETURNS integer AS
$$
DECLARE
  nummoves integer;
BEGIN

    SELECT r.lastmovenumber+1 INTO nummoves 
    FROM rounds AS r
    WHERE r.roundid = $1;

    RETURN nummoves;
END;
$$ LANGUAGE 'plpgsql';

