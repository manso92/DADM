CREATE OR REPLACE FUNCTION getNextPlayer(roundid integer) RETURNS integer AS
$$
DECLARE
  numplayers integer;
BEGIN

    SELECT count(*) INTO numplayers 
    FROM roundplayers AS rp
    WHERE rp.roundid = $1;

    numplayers := numplayers + 1;

    IF EXISTS(SELECT g.gameid 
              FROM rounds AS r, games AS g
              WHERE r.roundid = $1 AND r.gameid=g.gameid AND
                    g.maxplayers < numplayers)
    THEN
        RETURN -1; --Si superamos el numero max de jugadores
    END IF;

    RETURN numplayers;
END;
$$ LANGUAGE 'plpgsql';

