-- Devuelve los resultados. Devuelve:
--             - roundid
--             - playername
--             - timeround
--             - points
--             - otherinfo
--
DROP FUNCTION IF EXISTS getResults(integer);
CREATE OR REPLACE FUNCTION getResults(gameid integer) 
            RETURNS TABLE(playername character varying,
                          timeround integer,
                          points integer,
                          otherinfo character varying(2048)) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT p.playername,
                         r.roundtime,
                         r.points,
                         r.otherinfo
                  FROM  results AS r,
                        players AS p
                  WHERE p.playerid=r.playerid AND
                        r.gameid=$1);

END;
$$ LANGUAGE 'plpgsql';
