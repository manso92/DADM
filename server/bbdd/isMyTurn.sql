CREATE OR REPLACE FUNCTION isMyTurn(roundid integer,playerid uuid) RETURNS integer AS
$$
DECLARE
  numplayers integer;
BEGIN

    IF NOT EXISTS(SELECT r.roundid 
                  FROM   rounds AS r
                  WHERE  r.roundid = $1 AND 
                         (r.estado = 'A' OR r.estado = 'C' ) )
    THEN
        RETURN -1; -- Partida no lista para jugar o no existe
    END IF;

    IF NOT EXISTS(SELECT b.playerid
                  FROM   rounds a, roundplayers b
                  WHERE  a.roundid  = $1        AND 
                         a.roundid  = b.roundid AND
                         b.playerid = $2        AND 
                         a.turn = b.turn)
--                         1+(lastmovenumber%numberofplayers) = b.turn)
    THEN
        RETURN 0; -- No te toca
    END IF;


    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

