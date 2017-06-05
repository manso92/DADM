CREATE OR REPLACE FUNCTION inRound(roundid integer,playerid uuid) RETURNS integer AS
$$
DECLARE

BEGIN

    IF NOT EXISTS(SELECT b.playerid
                  FROM   rounds a, roundplayers b
                  WHERE  a.roundid  = $1        AND 
                         a.roundid  = b.roundid AND
                         b.playerid = $2) 
    THEN
        RETURN 0; -- No estas en esa partida
    END IF;


    RETURN 1; --Si estas
END;
$$ LANGUAGE 'plpgsql';

