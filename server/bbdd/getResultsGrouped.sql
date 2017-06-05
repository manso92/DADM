-- Devuelve los resultados. Devuelve:
--             - roundid
--             - playername
--             - timeround
--             - points
--             - otherinfo
--
DROP FUNCTION IF EXISTS getResultsGrouped(integer);
CREATE OR REPLACE FUNCTION getResultsGrouped(gameid integer)
            RETURNS TABLE(playername character varying,
                          timeround bigint,
                          points bigint) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT p.playername,
                         SUM(r.roundtime),
                         SUM(r.points)
                  FROM  results AS r,
                        players AS p
                  WHERE p.playerid=r.playerid AND
                        r.gameid=$1
                  GROUP BY p.playername);

END;
$$ LANGUAGE 'plpgsql';
